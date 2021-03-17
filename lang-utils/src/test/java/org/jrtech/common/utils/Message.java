package org.jrtech.common.utils;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Message implements Serializable {

	private static final long serialVersionUID = 8110596528276141921L;

	private long messageId;
	private String createdBy;
	private Timestamp createdAt;
	private String modifiedBy;
	private Timestamp modifiedAt;
	private List<MessageRelationship> endRelationships;
	private List<MessageRelationship> startRelationships;
	private String value;
	private String metadataValue;
	private String transactionReference;
	private String type;
	private String direction;
	private String counterparty;
	private String status;
	public String publicField;

	private Map<String, String> extProps = new HashMap<>();

	public Message() {
		super();
		endRelationships = new ArrayList<MessageRelationship>();
		startRelationships = new ArrayList<MessageRelationship>();
	}

	public long getMessageId() {
		return messageId;
	}

	public void setMessageId(long messageId) {
		this.messageId = messageId;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public Timestamp getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Timestamp createdAt) {
		this.createdAt = createdAt;
	}

	public String getModifiedBy() {
		return modifiedBy;
	}

	public void setModifiedBy(String modifiedBy) {
		this.modifiedBy = modifiedBy;
	}

	public Timestamp getModifiedAt() {
		return modifiedAt;
	}

	public void setModifiedAt(Timestamp modifiedAt) {
		this.modifiedAt = modifiedAt;
	}

	public List<MessageRelationship> getEndRelationships() {
		return endRelationships;
	}

	public void setEndRelationships(List<MessageRelationship> endRelationships) {
		this.endRelationships = endRelationships;
	}

	public List<MessageRelationship> getStartRelationships() {
		return startRelationships;
	}

	public void setStartRelationships(List<MessageRelationship> startRelationships) {
		this.startRelationships = startRelationships;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getMetadataValue() {
		return metadataValue;
	}

	public void setMetadataValue(String metadataValue) {
		this.metadataValue = metadataValue;
	}

	public String getTransactionReference() {
		return transactionReference;
	}

	public void setTransactionReference(String transactionReference) {
		this.transactionReference = transactionReference;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getDirection() {
		return direction;
	}

	public void setDirection(String direction) {
		this.direction = direction;
	}

	public String getCounterparty() {
		return counterparty;
	}

	public void setCounterparty(String counterparty) {
		this.counterparty = counterparty;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int totalHashCode = 1;

		totalHashCode = prime * totalHashCode + (int) (messageId ^ (messageId >>> 32));
		totalHashCode = prime * totalHashCode + ((createdBy == null) ? 0 : createdBy.hashCode());
		totalHashCode = prime * totalHashCode + ((createdAt == null) ? 0 : createdAt.hashCode());
		totalHashCode = prime * totalHashCode + ((modifiedBy == null) ? 0 : modifiedBy.hashCode());
		totalHashCode = prime * totalHashCode + ((modifiedAt == null) ? 0 : modifiedAt.hashCode());
		totalHashCode = prime * totalHashCode + ((value == null) ? 0 : value.hashCode());
		totalHashCode = prime * totalHashCode + ((metadataValue == null) ? 0 : metadataValue.hashCode());
		totalHashCode = prime * totalHashCode + ((transactionReference == null) ? 0 : transactionReference.hashCode());
		totalHashCode = prime * totalHashCode + ((type == null) ? 0 : type.hashCode());
		totalHashCode = prime * totalHashCode + ((direction == null) ? 0 : direction.hashCode());
		totalHashCode = prime * totalHashCode + ((counterparty == null) ? 0 : counterparty.hashCode());
		totalHashCode = prime * totalHashCode + ((status == null) ? 0 : status.hashCode());

		return totalHashCode;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;

		if (!(obj instanceof Message))
			return false;

		final Message other = (Message) obj;
		if (messageId != other.messageId)
			return false;
		if (createdBy == null) {
			if (other.createdBy != null)
				return false;
		} else if (!createdBy.equals(other.createdBy)) {
			return false;
		}
		if (createdAt == null) {
			if (other.createdAt != null)
				return false;
		} else if (!createdAt.equals(other.createdAt)) {
			return false;
		}
		if (modifiedBy == null) {
			if (other.modifiedBy != null)
				return false;
		} else if (!modifiedBy.equals(other.modifiedBy)) {
			return false;
		}
		if (modifiedAt == null) {
			if (other.modifiedAt != null)
				return false;
		} else if (!modifiedAt.equals(other.modifiedAt)) {
			return false;
		}
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value)) {
			return false;
		}
		if (metadataValue == null) {
			if (other.metadataValue != null)
				return false;
		} else if (!metadataValue.equals(other.metadataValue)) {
			return false;
		}
		if (transactionReference == null) {
			if (other.transactionReference != null)
				return false;
		} else if (!transactionReference.equals(other.transactionReference)) {
			return false;
		}
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type)) {
			return false;
		}
		if (direction == null) {
			if (other.direction != null)
				return false;
		} else if (!direction.equals(other.direction)) {
			return false;
		}
		if (counterparty == null) {
			if (other.counterparty != null)
				return false;
		} else if (!counterparty.equals(other.counterparty)) {
			return false;
		}
		if (status == null) {
			if (other.status != null)
				return false;
		} else if (!status.equals(other.status)) {
			return false;
		}

		return true;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();

		sb.append("messageId: ").append(messageId).append("\n");
		sb.append("createdBy: ").append(createdBy).append("\n");
		sb.append("createdAt: ").append(createdAt).append("\n");
		sb.append("modifiedBy: ").append(modifiedBy).append("\n");
		sb.append("modifiedAt: ").append(modifiedAt).append("\n");
		sb.append("value: ").append(value).append("\n");
		sb.append("metadataValue: ").append(metadataValue).append("\n");
		sb.append("transactionReference: ").append(transactionReference).append("\n");
		sb.append("type: ").append(type).append("\n");
		sb.append("direction: ").append(direction).append("\n");
		sb.append("counterparty: ").append(counterparty).append("\n");
		sb.append("status: ").append(status).append("\n");

		return sb.toString();
	}

	public String getPropertyValue(String propertyName) {
		return extProps.get(propertyName);
	}

	public void setPropertyValue(String propertyName, String propertyValue) {
		extProps.put(propertyName, propertyValue);
	}

}
