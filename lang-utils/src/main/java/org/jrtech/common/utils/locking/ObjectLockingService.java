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


import java.io.Serializable;
import java.util.List;

public interface ObjectLockingService extends Serializable {
	
	/**
	 * Lock object
	 * @param lockItem with 
	 * @throws ObjectAlreadyLockedException
	 */
	public void lockObject(LockItem lockItem) throws ObjectAlreadyLockedException;
	
	/**
	 * Force the lock creation either by stealing existing lock or just create a new one.
	 * 
	 * @param lockItem
	 * @return
	 * <li>null - data was not locked
	 * <li>not null - lock from the previous 
	 */
	public LockItem stealLock(LockItem lockItem);
	
	public void unlockObject(LockItem lockItem) throws InvalidObjectLockOwnershipException;

	public List<LockItem> retrieveLocksByEntity(String entityName);
	
	public LockItem retrieveLockInfo(String entityName, String entityId);

	public LockItem retrieveLockInfo(LockItem lockItem);

	public List<LockItem> retrieveLockInfo();
	
	public List<LockItem> clearLocksBySource(String source);
	
	public List<LockItem> clearLocksBySourcePattern(String sourcePattern);

	public List<LockItem> clearLocksByUser(String userName);
	
	public List<LockItem> clearLocksByTimeout(String exceptionSource);
	
	public void reset();
	
	public List<ObjectLockInfo> checkObjectLocks(List<ObjectLockInfo> listToCheck);
	
	public int getTimeout();

	public void setTimeout(int timeoutInSeconds);
	
	public static class ObjectLockInfo implements Serializable {

		private static final long serialVersionUID = 8800271060210121518L;

        private String entityName;
		
		private String entityId;
		
		private boolean locked = false;
		
		public ObjectLockInfo(String entityName, String entityId) {
			this.entityName = entityName;
			this.entityId = entityId;
        }

		public String getEntityName() {
	        return entityName;
        }

		public String getEntityId() {
	        return entityId;
        }

		public boolean isLocked() {
	        return locked;
        }

		public void setLocked(boolean locked) {
	        this.locked = locked;
        }
	}
}
