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

public class LockItem implements Serializable, Cloneable {

    private static final long serialVersionUID = -2858547988970562213L;
    
    private long id;
    private String entityName;
    private String entityId;
    private String lockedBy;
    private long lockedUtcTimestamp;
    private String source;

    public LockItem(String entityName, String entityId, String lockedBy, String source) {
        super();
        this.entityName = entityName;
        this.entityId = entityId;
        this.lockedBy = lockedBy;
        this.source = source;
    }
	
    public long getId() {
        return id;
    }
	
    public void setId(long id) {
        this.id = id;
    }
	
    public String getEntityName() {
        return entityName;
    }
	
    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }
	
    public String getEntityId() {
        return entityId;
    }
	
    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }
	
    public String getLockedBy() {
        return lockedBy;
    }
	
    public void setLockedBy(String lockedBy) {
        this.lockedBy = lockedBy;
    }
	
    public long getLockedUtcTimestamp() {
        return lockedUtcTimestamp;
    }
	
    public void setLockedUtcTimestamp(long lockedUtcTimestamp) {
        this.lockedUtcTimestamp = lockedUtcTimestamp;
    }
	
    public String getSource() {
        return source;
    }
	
    public void setSource(String source) {
        this.source = source;
    }
	
    @Override
    public int hashCode() {
        final int prime = 31; 
        int totalHashCode = 1;

        totalHashCode = prime * totalHashCode + (int) (id ^ (id >>> 32));
        totalHashCode = prime * totalHashCode + ((entityName == null) ? 0 : entityName.hashCode());
        totalHashCode = prime * totalHashCode + ((entityId == null) ? 0 : entityId.hashCode());
        totalHashCode = prime * totalHashCode + ((lockedBy == null) ? 0 : lockedBy.hashCode());
        totalHashCode = prime * totalHashCode + (int) (lockedUtcTimestamp ^ (lockedUtcTimestamp >>> 32));
        totalHashCode = prime * totalHashCode + ((source == null) ? 0 : source.hashCode());
		
        return totalHashCode;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;

		if (!(obj instanceof LockItem))
			return false;

		final LockItem other = (LockItem) obj;
		if (id != other.id)
			return false;
		if (entityName == null) {
			if (other.entityName != null)
				return false;
		} else if (!entityName.equals(other.entityName)) {
			return false;
		}
		if (entityId == null) {
			if (other.entityId != null)
				return false;
		} else if (!entityId.equals(other.entityId)) {
			return false;
		}
		if (lockedBy == null) {
			if (other.lockedBy != null)
				return false;
		} else if (!lockedBy.equals(other.lockedBy)) {
			return false;
		}
		if (lockedUtcTimestamp != other.lockedUtcTimestamp)
			return false;
		if (source == null) {
			if (other.source != null)
				return false;
		} else if (!source.equals(other.source)) {
			return false;
		}

		return true;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();

		sb.append("id: ").append(id).append("\n");
		sb.append("entityName: ").append(entityName).append("\n");
		sb.append("entityId: ").append(entityId).append("\n");
		sb.append("lockedBy: ").append(lockedBy).append("\n");
		sb.append("lockedUtcTimestamp: ").append(lockedUtcTimestamp).append("\n");
		sb.append("source: ").append(source).append("\n");

		return sb.toString();
	}
	
}
