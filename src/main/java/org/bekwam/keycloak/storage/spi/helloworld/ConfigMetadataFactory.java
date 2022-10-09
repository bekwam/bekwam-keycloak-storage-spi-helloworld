package org.bekwam.keycloak.storage.spi.helloworld;

import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;

import java.util.List;

/**
 * Creates block of Config items displayed in Keycloak Admin UI's User Federation
 * tab
 *
 * @author carl
 * @since 1.0
 */
public class ConfigMetadataFactory {
    public List<ProviderConfigProperty> create() {
        ProviderConfigurationBuilder builder = ProviderConfigurationBuilder
                .create();

        builder
                .property().name(Constants.PROVIDER_PROPERTY_MY_PARAMETER)
                .type(ProviderConfigProperty.STRING_TYPE)
                .label("My Parameter")
                .defaultValue("My Value")
                .helpText("A sample parameter defined by this spi")
                .add();

       return builder.build();
    }
}
