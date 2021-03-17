package org.jrtech.common.authorization;

import org.jrtech.common.authorization.model.Attribute;
import org.junit.Before;

public class TestAuditEventAuthorization {

	private static final String[] ENTITIES = new String[] { "AuditEventItem" };

    private static final Attribute[] ATTRIBUTES = new Attribute[] { BaseSecurityConstants.ATTR_NAME,
            new Attribute("Source", String.class.getSimpleName()), };

    @Before
    public void init() {

    }

}
