/*
 * Copyright (c) 2016-2026 Jumin Rubin
 * LinkedIn: https://www.linkedin.com/in/juminrubin/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jrtech.common.base.services;

import com.hazelcast.config.EvictionPolicy;
import com.hazelcast.config.IndexConfig;
import com.hazelcast.config.IndexType;

import org.jrtech.common.hazelcast.HazelcastInstanceHelper;
import org.jrtech.common.hazelcast.HazelcastOperationHelper;
import org.jrtech.common.utils.LocaleUtils;
import org.jrtech.common.utils.locking.InvalidObjectLockOwnershipException;
import org.jrtech.common.utils.locking.LockItem;
import org.jrtech.common.utils.locking.ObjectAlreadyLockedException;
import org.jrtech.common.utils.locking.ObjectLockingService;

import java.util.*;
import java.util.stream.Collectors;

public class CentralObjectLockingService implements ObjectLockingService {

    private static final long serialVersionUID = 940297631844085636L;

    public static final String OBJECT_LOCK_TABLE = "jrxObjectLockTable";

    private static final String ATTR_ENTITY_NAME = "entityName";

    private static final String ATTR_ENTITY_ID = "entityId";

    private static final String ATTR_LOCKED_BY = "lockedBy";

    private static final String ATTR_LOCKED_TIMESTAMP = "lockedUtcTimestamp";

    private static final String ATTR_SOURCE = "source";

    private static final IndexConfig[] INDEX_DEFS = new IndexConfig[] {
		// @formatter:off
		new IndexConfig(IndexType.HASH, ATTR_ENTITY_NAME),
		new IndexConfig(IndexType.HASH, ATTR_ENTITY_ID), 
        new IndexConfig(IndexType.HASH, ATTR_LOCKED_BY), 
        new IndexConfig(IndexType.SORTED, ATTR_LOCKED_TIMESTAMP),
        new IndexConfig(IndexType.HASH, ATTR_SOURCE)
        // @formatter:on
    };

    private int timeoutInSeconds = 3600;

    private long timeoutInMilis = timeoutInSeconds * 1000;

    public static CentralObjectLockingService getInstance() {
        return new CentralObjectLockingService();
    }

    public CentralObjectLockingService() {
        HazelcastInstanceHelper helper = new HazelcastInstanceHelper();
        helper.createCache(OBJECT_LOCK_TABLE, EvictionPolicy.NONE, 0, 0, timeoutInSeconds, true, INDEX_DEFS);
    }

    protected Set<LockItem> queryBySource(String source) {
        if (source == null) {
            return new HashSet<LockItem>();
        }
        Map<String, LockItem> lockTable = HazelcastOperationHelper.getCacheByName(OBJECT_LOCK_TABLE);
        final String regexSource = source.replace(".*", "[WILDCARD]").replace(".", "[.]").replace("/", "[/]")
                .replace("[WILDCARD]", ".*");
        return lockTable.values().stream().filter(li -> {
            if (li.getSource() == null)
                return false;
            else
                return li.getSource().matches(regexSource);
        }).collect(Collectors.toSet());
    }

    protected Set<LockItem> queryByUser(String userPrincipal) {
        if (userPrincipal == null) {
            return new HashSet<LockItem>();
        }
        Map<String, LockItem> lockTable = HazelcastOperationHelper.getCacheByName(OBJECT_LOCK_TABLE);
        return lockTable.values().stream()
                .filter(li -> li.getLockedBy() == null ? false : li.getLockedBy().equals(userPrincipal))
                .collect(Collectors.toSet());
    }

    protected Set<LockItem> queryByEntity(String entityName) {
        if (entityName == null) {
            return new HashSet<LockItem>();
        }

        Map<String, LockItem> lockTable = HazelcastOperationHelper.getCacheByName(OBJECT_LOCK_TABLE);
        return lockTable.values().stream()
                .filter(li -> li.getEntityName() == null ? false : li.getEntityName().equals(entityName))
                .collect(Collectors.toSet());
    }

    protected Set<LockItem> queryByTimeout(long beforeMilis) {
        if (beforeMilis < 1) {
            return new HashSet<LockItem>();
        }

        Map<String, LockItem> lockTable = HazelcastOperationHelper.getCacheByName(OBJECT_LOCK_TABLE);
        return lockTable.values().stream().filter(li -> li.getLockedUtcTimestamp() < beforeMilis)
                .collect(Collectors.toSet());
    }

    protected String formulateKey(LockItem lockItem) {
        return formulateKey(lockItem.getEntityName(), lockItem.getEntityId());
    }

    protected String formulateKey(String entityName, String entityId) {
        return entityName + "-" + entityId;
    }

    @Override
    public void lockObject(LockItem lockItem) throws ObjectAlreadyLockedException {
        if (lockItem == null)
            return;

        String key = formulateKey(lockItem);
        LockItem existingLockInfo = HazelcastOperationHelper.retrieveCacheValue(OBJECT_LOCK_TABLE, key);
        if (existingLockInfo != null) {
            if (existingLockInfo.getSource() != null && existingLockInfo.getLockedBy() != null
                    && existingLockInfo.getSource().equals(lockItem.getSource())
                    && existingLockInfo.getLockedBy().equals(lockItem.getLockedBy())) {
                // The same user from the same source requests for lock again -> Update lock time stamp for getting the
                // next timeout again.
                existingLockInfo.setLockedUtcTimestamp(getCurrentUtcMiliseconds());
                lockItem.setLockedUtcTimestamp(existingLockInfo.getLockedUtcTimestamp());
            } else {
                throw new ObjectAlreadyLockedException(existingLockInfo);
            }

            return;
        }

        lockItem.setId(UUID.randomUUID().getMostSignificantBits());
        lockItem.setLockedUtcTimestamp(Calendar.getInstance(LocaleUtils.TIMEZONE_UTC).getTimeInMillis());
        existingLockInfo = HazelcastOperationHelper.storeToCacheIfAbsent(OBJECT_LOCK_TABLE, key, lockItem);
        if (existingLockInfo != null) {
            throw new ObjectAlreadyLockedException(existingLockInfo);
        }
    }

    @Override
    public LockItem stealLock(LockItem lockItem) {
        String key = formulateKey(lockItem);
        LockItem existingLockInfo = HazelcastOperationHelper.retrieveCacheValue(OBJECT_LOCK_TABLE, key);

        lockItem.setId(UUID.randomUUID().getMostSignificantBits());
        lockItem.setLockedUtcTimestamp(getCurrentUtcMiliseconds());
        HazelcastOperationHelper.storeToCache(OBJECT_LOCK_TABLE, key, lockItem);

        return existingLockInfo;
    }

    @Override
    public void unlockObject(LockItem lockItem) throws InvalidObjectLockOwnershipException {
        String key = formulateKey(lockItem);

        LockItem existingLock = HazelcastOperationHelper.retrieveCacheValue(OBJECT_LOCK_TABLE, key);
        if (existingLock == null)
            return;

        if (!existingLock.getLockedBy().equals(lockItem.getLockedBy())) {
            throw new InvalidObjectLockOwnershipException(existingLock, lockItem);
        }

        HazelcastOperationHelper.removeFromCache(OBJECT_LOCK_TABLE, key);
    }

    @Override
    public LockItem retrieveLockInfo(LockItem lockItem) {
        return retrieveLockInfo(lockItem.getEntityName(), lockItem.getEntityId());
    }

    @Override
    public LockItem retrieveLockInfo(String entityName, String entityId) {
        String key = formulateKey(entityName, entityId);

        LockItem storedLockItem = HazelcastOperationHelper.retrieveCacheValue(OBJECT_LOCK_TABLE, key);

        if (storedLockItem == null)
            return null;

        return storedLockItem;
    }

    @Override
    public List<LockItem> retrieveLockInfo() {
        return HazelcastOperationHelper.retrieveAllCacheValues(OBJECT_LOCK_TABLE);
    }

    @Override
    public List<LockItem> retrieveLocksByEntity(String entityName) {
        if (entityName == null)
            return new ArrayList<LockItem>();

        Set<LockItem> items = queryByEntity(entityName);
        if (items == null || items.isEmpty())
            return new ArrayList<LockItem>();

        return new ArrayList<LockItem>(items);
    }

    @Override
    public List<LockItem> clearLocksBySource(String source) {
        if (source == null)
            return new ArrayList<LockItem>();

        Set<LockItem> items = queryBySource(source);
        if (items == null || items.isEmpty())
            return new ArrayList<LockItem>();

        List<Object> keyList = new ArrayList<Object>();
        for (LockItem item : items) {
            keyList.add(formulateKey(item));
        }

        HazelcastOperationHelper.removeFromCacheBulk(OBJECT_LOCK_TABLE, keyList);

        return new ArrayList<LockItem>(items);
    }

    @Override
    public List<LockItem> clearLocksByUser(String userName) {
        if (userName == null)
            return new ArrayList<LockItem>();

        Set<LockItem> items = queryByUser(userName);
        if (items == null || items.isEmpty())
            return new ArrayList<LockItem>();

        List<Object> keyList = new ArrayList<Object>();
        for (LockItem item : items) {
            keyList.add(formulateKey(item));
        }

        HazelcastOperationHelper.removeFromCacheBulk(OBJECT_LOCK_TABLE, keyList);

        return new ArrayList<LockItem>(items);
    }

    @Override
    public List<LockItem> clearLocksByTimeout(String exceptionSource) {
        List<LockItem> clearedItems = new ArrayList<LockItem>();
        long currentMilis = getCurrentUtcMiliseconds();

        Set<LockItem> items = queryByTimeout(currentMilis - timeoutInMilis);
        if (items == null || items.isEmpty())
            return new ArrayList<LockItem>();

        List<Object> keyList = new ArrayList<Object>();
        for (LockItem item : items) {
            if (exceptionSource != null && item.getSource().matches(exceptionSource)) {
                continue;
            }
            keyList.add(formulateKey(item));
            clearedItems.add(item);
        }

        HazelcastOperationHelper.removeFromCacheBulk(OBJECT_LOCK_TABLE, keyList);

        return clearedItems;
    }

    @Override
    public List<LockItem> clearLocksBySourcePattern(String sourcePattern) {
        if (sourcePattern == null)
            return new ArrayList<LockItem>();

        Set<LockItem> items = queryBySource(sourcePattern);
        if (items == null || items.isEmpty())
            return new ArrayList<LockItem>();

        List<Object> keyList = new ArrayList<Object>();
        for (LockItem item : items) {
            keyList.add(formulateKey(item));
        }

        HazelcastOperationHelper.removeFromCacheBulk(OBJECT_LOCK_TABLE, keyList);

        return new ArrayList<LockItem>(items);
    }

    @Override
    public void reset() {
        HazelcastOperationHelper.clearCache(OBJECT_LOCK_TABLE);
    }

    @Override
    public List<ObjectLockInfo> checkObjectLocks(List<ObjectLockInfo> listToCheck) {
        if (listToCheck == null)
            return null;

        for (ObjectLockInfo objectLockInfo : listToCheck) {
            objectLockInfo.setLocked(HazelcastOperationHelper.containsKey(OBJECT_LOCK_TABLE,
                    formulateKey(objectLockInfo.getEntityName(), objectLockInfo.getEntityId())));
        }

        return listToCheck;
    }

    public int getTimeout() {
        return timeoutInSeconds;
    }

    public void setTimeout(int timeoutInSeconds) {
        this.timeoutInSeconds = timeoutInSeconds;
        this.timeoutInMilis = timeoutInSeconds * 1000;
    }

    protected long getCurrentUtcMiliseconds() {
        return Calendar.getInstance(LocaleUtils.TIMEZONE_UTC).getTimeInMillis();
    }
}
