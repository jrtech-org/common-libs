package org.jrtech.common.authorization;

import java.text.MessageFormat;
import java.util.Map;

import org.jrtech.common.authorization.model.Entity;
import org.junit.Test;

public class TestBaseEntityCatalog {

    @Test
    public void testLoadingEntityCatalog() {
        System.out.println("Entities:");
        Map<String, Entity> entityCatalog = BaseEntityCatalog.getEntityCatalog();
        entityCatalog.values().stream().forEach(e -> {
            System.out.println("\t" + e.getName());
            System.out.println("\tActions:");
            BaseSecurityConstants.SECURITY_AUTHORIZATION_MODEL.getEntityActions(e).ifPresent(ea -> {
                ea.getActions().forEach(action -> System.out.println("\t\t" + action));
            });
            System.out.println();
        });
    }
    
    @Test
    public void testLoadingAuthorizationModelCatalog() {
        Map<String, AuthorizationModel> authModelCatalog = BaseEntityCatalog.getAuthorizationModelCatalog();
        authModelCatalog.values().stream().forEach(authModel -> {
            System.out.println(MessageFormat.format("{0} ({1})", authModel.getName(), authModel.getApplication().getName()));
            authModel.getEntityActionsCollection().stream().forEach(ea -> {
                System.out.println(MessageFormat.format("\t{0}", ea.getEntity().getName()));
                ea.getActions().forEach(action -> System.out.println(MessageFormat.format("\t\t{0}", action)));
            });
            System.out.println();
        });
    }
}
