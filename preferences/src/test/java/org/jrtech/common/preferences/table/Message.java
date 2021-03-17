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
package org.jrtech.common.preferences.table;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Message implements Serializable {

    private static final long serialVersionUID = -7612211599808862073L;

    private String messageId;

    private MessageType type;
    
    private String ownpartyCode;

    private String counterpartyCode;

    private String transactionReference;

    private Date creationTimetamp;

    private String createdBy;

    private Date modificationTimestamp;

    private String modifiedBy;

    private Map<String, Object> extendedProperties;

    public Message() {
        extendedProperties = new HashMap<>();
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public String getCounterpartyCode() {
        return counterpartyCode;
    }

    public void setCounterpartyCode(String counterpartyCode) {
        this.counterpartyCode = counterpartyCode;
    }

    public String getTransactionReference() {
        return transactionReference;
    }

    public void setTransactionReference(String transactionReference) {
        this.transactionReference = transactionReference;
    }

    public Date getCreationTimetamp() {
        return creationTimetamp;
    }

    public void setCreationTimetamp(Date creationTimetamp) {
        this.creationTimetamp = creationTimetamp;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Date getModificationTimestamp() {
        return modificationTimestamp;
    }

    public void setModificationTimestamp(Date modificationTimestamp) {
        this.modificationTimestamp = modificationTimestamp;
    }

    public String getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(String modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    public Map<String, Object> getExtendedProperties() {
        return extendedProperties;
    }

    public void setExtendedProperties(Map<String, Object> extendedProperties) {
        this.extendedProperties = extendedProperties;
    }

    @SuppressWarnings("unchecked")
    public <T> T getExtendedPropertyValue(String attributeName) throws ClassCastException {
        if (extendedProperties == null)
            return null;

        return (T) extendedProperties.get(attributeName);
    }

    public <T> void setExtendedPropertyValue(String attributeName, T value) {
        if (extendedProperties == null)
            extendedProperties = new HashMap<>();
        
        extendedProperties.put(attributeName, value);
    }

    public String getOwnpartyCode() {
        return ownpartyCode;
    }

    public void setOwnpartyCode(String ownpartyCode) {
        this.ownpartyCode = ownpartyCode;
    }
}
