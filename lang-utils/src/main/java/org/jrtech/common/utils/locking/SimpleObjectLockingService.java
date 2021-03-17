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
package org.jrtech.common.utils.locking;


import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleObjectLockingService implements ObjectLockingService {

	private static final long serialVersionUID = 1529615555734967598L;

    private static Logger log = LoggerFactory.getLogger(SimpleObjectLockingService.class);

	protected static final ConcurrentHashMap<String, LockItem> LOCK_TABLE = new ConcurrentHashMap<String, LockItem>();

	private int timeoutInSeconds = 3600;

	private long timeoutInMilis = timeoutInSeconds * 1000;

	public static SimpleObjectLockingService getInstance() {
		return new SimpleObjectLockingService();
	}

	protected String formulateKey(LockItem lockItem) {
		return formulateKey(lockItem.getEntityName(), lockItem.getEntityId());
	}

	protected String formulateKey(String entityName, String entityId) {
		return entityName + "-" + entityId;
	}

	@Override
	public void lockObject(LockItem lockItem) throws ObjectAlreadyLockedException {
		String key = formulateKey(lockItem);
		if (LOCK_TABLE.containsKey(key)) {
			LockItem existingLockInfo = LOCK_TABLE.get(key);
			if (existingLockInfo.getSource() != null && existingLockInfo.getLockedBy() != null
			        && existingLockInfo.getSource().equals(lockItem.getSource())
			        && existingLockInfo.getLockedBy().equals(lockItem.getLockedBy())) {
				// The same user from the same source requests for lock again -> Update lock time stamp for getting the
				// next timeout again.
				existingLockInfo.setLockedUtcTimestamp(System.currentTimeMillis());
				lockItem.setLockedUtcTimestamp(existingLockInfo.getLockedUtcTimestamp());
				log.debug("object already locked by '"+existingLockInfo.getLockedBy()+"': " + key);
			} else {
				throw new ObjectAlreadyLockedException(LOCK_TABLE.get(key));
			}

			return;
		}

		lockItem.setId(UUID.randomUUID().getMostSignificantBits());
		lockItem.setLockedUtcTimestamp(System.currentTimeMillis());
		if (LOCK_TABLE.putIfAbsent(key, lockItem) != null) {
			// putIfAbsent return NULL when there is no entry with key.
			// If the return value not NULL, it means there is already a value with this key
			// -> throw ObjectAlreadyLockedException
			throw new ObjectAlreadyLockedException(LOCK_TABLE.get(key));
		}
		log.debug("object locked by '"+lockItem.getLockedBy()+"': " + key);
	}

	@Override
	public LockItem stealLock(LockItem lockItem) {
		String key = formulateKey(lockItem);
		LockItem existingLockInfo = LOCK_TABLE.get(key);

		lockItem.setId(UUID.randomUUID().getMostSignificantBits());
		lockItem.setLockedUtcTimestamp(System.currentTimeMillis());
		LOCK_TABLE.put(key, lockItem);
		log.debug("steal lock by '"+lockItem.getLockedBy()+"': " + key);
		return existingLockInfo;
	}

	@Override
	public void unlockObject(LockItem lockItem) throws InvalidObjectLockOwnershipException {
		String key = formulateKey(lockItem);

		LockItem existingLock = LOCK_TABLE.get(key);
		if (existingLock == null) return;
		
		if (!existingLock.getLockedBy().equals(lockItem.getLockedBy())) {
			throw new InvalidObjectLockOwnershipException(existingLock, lockItem);
		}
		
		LOCK_TABLE.remove(key);
		log.debug("object unlocked by '"+lockItem.getLockedBy()+"': " + key);
	}

	@Override
	public LockItem retrieveLockInfo(LockItem lockItem) {
		return retrieveLockInfo(lockItem.getEntityName(), lockItem.getEntityId());
	}

	@Override
	public LockItem retrieveLockInfo(String entityName, String entityId) {
		String key = formulateKey(entityName, entityId);

		LockItem storedLockItem = LOCK_TABLE.get(key);

		if (storedLockItem == null)
			return null;

		LockItem lockItem = new LockItem(storedLockItem.getEntityName(), storedLockItem.getEntityId(),
		        storedLockItem.getLockedBy(), storedLockItem.getSource());

		lockItem.setLockedUtcTimestamp(storedLockItem.getLockedUtcTimestamp());

		return lockItem;
	}

	@Override
	public List<LockItem> retrieveLocksByEntity(String entityName) {
		List<LockItem> result = new ArrayList<LockItem>();

		for (LockItem storedLockItem : LOCK_TABLE.values()) {
			if (storedLockItem.getEntityName().equals(entityName)) {
				LockItem copy = new LockItem(storedLockItem.getEntityName(), storedLockItem.getEntityId(),
				        storedLockItem.getLockedBy(), storedLockItem.getSource());
				copy.setId(storedLockItem.getId());
				copy.setLockedUtcTimestamp(storedLockItem.getLockedUtcTimestamp());
				result.add(copy);
			}
		}

		return result;
	}

	@Override
	public List<LockItem> retrieveLockInfo() {
		return new ArrayList<LockItem>(LOCK_TABLE.values());
	}

	@Override
	public List<LockItem> clearLocksBySource(String source) {
		if (source == null)
			return new ArrayList<LockItem>();

		List<LockItem> clearedItems = new ArrayList<LockItem>();
		for (LockItem item : LOCK_TABLE.values()) {
			if (source.equals(item.getSource())) {
				clearedItems.add(item);
			}
		}
		removeItemsFromLockTable(clearedItems);

		return clearedItems;
	}

	@Override
	public List<LockItem> clearLocksByUser(String userName) {
		if (userName == null)
			return new ArrayList<LockItem>();

		List<LockItem> clearedItems = new ArrayList<LockItem>();
		for (LockItem item : LOCK_TABLE.values()) {
			if (userName.equals(item.getLockedBy())) {
				clearedItems.add(item);
			}
		}
		removeItemsFromLockTable(clearedItems);

		return clearedItems;
	}

	@Override
	public void reset() {
		LOCK_TABLE.clear();
	}

	@Override
	public List<ObjectLockInfo> checkObjectLocks(List<ObjectLockInfo> listToCheck) {
		if (listToCheck == null)
			return null;

		for (ObjectLockInfo objectLockInfo : listToCheck) {
			objectLockInfo.setLocked(LOCK_TABLE.containsKey(formulateKey(objectLockInfo.getEntityName(),
			        objectLockInfo.getEntityId())));
		}

		return listToCheck;
	}

	@Override
	public List<LockItem> clearLocksByTimeout(String exceptionSource) {
		List<LockItem> clearedItems = new ArrayList<LockItem>();
		long currentMilis = System.currentTimeMillis();
		for (LockItem item : LOCK_TABLE.values()) {
			if (currentMilis - item.getLockedUtcTimestamp() > timeoutInMilis) {
				if (exceptionSource != null && item.getSource().matches(exceptionSource)) {
					continue; // skip
				}
				clearedItems.add(item);
			}
		}

		removeItemsFromLockTable(clearedItems);

		return clearedItems;
	}

	public int getTimeout() {
		return timeoutInSeconds;
	}

	public void setTimeout(int timeoutInSeconds) {
		this.timeoutInSeconds = timeoutInSeconds;
		this.timeoutInMilis = timeoutInSeconds * 1000;
	}

	@Override
	public List<LockItem> clearLocksBySourcePattern(String sourcePattern) {
		if (sourcePattern == null)
			return new ArrayList<LockItem>();

		List<LockItem> clearedItems = new ArrayList<LockItem>();
		for (LockItem item : LOCK_TABLE.values()) {
			if (item.getSource().matches(sourcePattern)) {
				clearedItems.add(item);
			}
		}
		removeItemsFromLockTable(clearedItems);

		return clearedItems;
	}

	protected final void removeItemsFromLockTable(List<LockItem> removeList) {
		for (LockItem item : removeList) {
			String key = formulateKey(item);
			LOCK_TABLE.remove(key);
			log.debug("object unlocked: " + key);
		}
	}
}
