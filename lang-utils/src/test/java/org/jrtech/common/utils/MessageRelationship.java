package org.jrtech.common.utils;



import java.io.Serializable;

public class MessageRelationship implements Serializable {

    private static final long serialVersionUID = -173397077407940900L;

    private Message start;
    private Message end;
    private String name;
    private long messageRelationshipId;

    public MessageRelationship() {
        super();
    }
	
    public Message getStart() {
        return start;
    }
	
    public void setStart(Message start) {
        this.start = start;
    }
	
    public Message getEnd() {
        return end;
    }
	
    public void setEnd(Message end) {
        this.end = end;
    }
	
    public String getName() {
        return name;
    }
	
    public void setName(String name) {
        this.name = name;
    }
	
    public long getMessageRelationshipId() {
        return messageRelationshipId;
    }
	
    public void setMessageRelationshipId(long messageRelationshipId) {
        this.messageRelationshipId = messageRelationshipId;
    }
	
    @Override
    public int hashCode() {
        final int prime = 31; 
        int totalHashCode = 1;

        totalHashCode = prime * totalHashCode + ((start == null) ? 0 : start.hashCode());
        totalHashCode = prime * totalHashCode + ((end == null) ? 0 : end.hashCode());
        totalHashCode = prime * totalHashCode + ((name == null) ? 0 : name.hashCode());
        totalHashCode = prime * totalHashCode + (int) (messageRelationshipId ^ (messageRelationshipId >>> 32));
		
        return totalHashCode;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;

		if (!(obj instanceof MessageRelationship))
			return false;

		final MessageRelationship other = (MessageRelationship) obj;
		if (start == null) {
			if (other.start != null)
				return false;
		} else if (!start.equals(other.start)) {
			return false;
		}
		if (end == null) {
			if (other.end != null)
				return false;
		} else if (!end.equals(other.end)) {
			return false;
		}
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name)) {
			return false;
		}
		if (messageRelationshipId != other.messageRelationshipId)
			return false;

		return true;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();

		sb.append("start: ").append(start).append("\n");
		sb.append("end: ").append(end).append("\n");
		sb.append("name: ").append(name).append("\n");
		sb.append("messageRelationshipId: ").append(messageRelationshipId).append("\n");

		return sb.toString();
	}
	
}
