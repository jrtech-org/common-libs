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


public class InvalidObjectLockOwnershipException extends Exception {

    private static final long serialVersionUID = -5023912767862361485L;

    private LockItem existingLockInfo;

    private LockItem newLockInfo;

    public InvalidObjectLockOwnershipException(LockItem existingLockInfo, LockItem newLockInfo) {
        super("User: '" + newLockInfo.getLockedBy() + "' cannot release object lock owned by: "
                + existingLockInfo.getLockedBy() + " from: " + existingLockInfo.getSource() + "");
        this.existingLockInfo = existingLockInfo;
        this.newLockInfo = newLockInfo;
    }

    public LockItem getExistingLockInfo() {
        return existingLockInfo;
    }

    public LockItem getNewLockInfo() {
        return newLockInfo;
    }

}
