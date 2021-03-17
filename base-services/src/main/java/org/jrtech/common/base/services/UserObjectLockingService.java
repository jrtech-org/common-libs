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

import org.jrtech.common.utils.locking.InvalidObjectLockOwnershipException;
import org.jrtech.common.utils.locking.LockItem;
import org.jrtech.common.utils.locking.ObjectAlreadyLockedException;

public class UserObjectLockingService extends CentralObjectLockingService implements UserScopeableService {

	private static final long serialVersionUID = -5337958229081336468L;
	
    private String userId;
    
    public UserObjectLockingService() {
        this("[N/A]");
    }
    
    public UserObjectLockingService(String userId) {
        this.userId = userId;
    }
	
	@Override
	public void setUserId(String userId) {
		this.userId = userId;
	}

	@Override
	public String getUserId() {
		return userId;
	}

	@Override
	public void lockObject(LockItem lockItem) throws ObjectAlreadyLockedException {
	    lockItem.setLockedBy(userId);
	    super.lockObject(lockItem);
	}

	@Override
	public LockItem stealLock(LockItem lockItem) {
	    lockItem.setLockedBy(userId);
	    return super.stealLock(lockItem);
	}
	
	@Override
	public void unlockObject(LockItem lockItem) throws InvalidObjectLockOwnershipException {
		lockItem.setLockedBy(userId);
		
	    super.unlockObject(lockItem);
	}
}
