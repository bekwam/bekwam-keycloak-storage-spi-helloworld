package org.bekwam.keycloak.storage.spi.helloworld;

import org.jboss.logging.Logger;
import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ServerInfoAwareProviderFactory;
import org.keycloak.storage.UserStorageProviderFactory;

import java.util.List;
import java.util.Map;

/**
 * Entry point for SPI
 *
 * This class initializes the main functionality which is MemoryUserStorageProvider.
 * It pulls a value from the admin UI's User Federation tab.  This value is
 * "myParameter" which and passed to MemoryUserStorageProvider.
 *
 * On the Server Info screen, information about this provider comes from the
 * fixed PROVIDER_NAME in this class and a file "info.properties" that contains
 * generated Maven information.
 *
 * META-INF/services hooks this class into Keycloak via the UserStorageProviderFactory
 * file
 *
 * @since 1.0
 * @author carl
 */
public class MemoryUserStorageProviderFactory
        implements UserStorageProviderFactory<MemoryUserStorageProvider>,
            ServerInfoAwareProviderFactory {

    private static final Logger logger =
            Logger.getLogger(MemoryUserStorageProviderFactory.class);

    public static final String PROVIDER_NAME = "bekwam-keycloak-storage-spi-hw";

    protected static final List<ProviderConfigProperty> configMetadata
            = new ConfigMetadataFactory().create();

    @Override
    public Map<String, String> getOperationalInfo() {
        return new PropertiesServerInfoDelegate().getProperties();
    }

    @Override
    public MemoryUserStorageProvider create(
            KeycloakSession keycloakSession,
            ComponentModel componentModel
    ) {

        logger.trace("MemoryUserStorageProvider.create()");

        Config config = Config.from(componentModel);

        return new MemoryUserStorageProvider(
                keycloakSession,
                componentModel,
                config.getMyParameter()
        );
    }

    @Override
    public String getId() {
        return PROVIDER_NAME;
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return configMetadata;
    }
}
