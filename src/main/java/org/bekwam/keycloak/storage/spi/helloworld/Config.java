package org.bekwam.keycloak.storage.spi.helloworld;

import org.keycloak.component.ComponentModel;

import java.util.Objects;

/**
 * Transport object for MemoryUserStorageProvider config
 *
 * @author carl
 * @since 1.0
 */
class Config {

    private final String myParameter;

    public Config(String myParameter) {
        this.myParameter = myParameter;
    }

    public static Config from(ComponentModel config) {
        return new Config(
                config.getConfig().getFirst(Constants.PROVIDER_PROPERTY_MY_PARAMETER)
        );
    }

    public String getMyParameter() {
        return myParameter;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Config config = (Config) o;
        return Objects.equals(myParameter, config.myParameter);
    }

    @Override
    public int hashCode() {
        return Objects.hash(myParameter);
    }

    @Override
    public String toString() {
        return "Config{" +
                "myParameter='" + myParameter + '\'' +
                '}';
    }
}
