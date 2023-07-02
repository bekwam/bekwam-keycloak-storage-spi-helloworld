# bekwam-keycloak-storage-spi-helloworld
A sample Keycloak User Federation Storage Provider (legacy as of 19)

This is an example of the Keycloak User Storage SPI.  It can be extended to handle more complex storage cases (ex, a relational database).

To deploy this, do a mvn install and copy the JAR to $KEYCLOAK_HOME/providers.

You'll see the new provider in the Server Info and the User Federation pages.

To trace through the code with logging using the following command.

    $ bin/kc.sh start-dev --log-level=org.bekwam:TRACE
    
If you're creating a new User Storage SPI for the latest Keycloak, be sure to read this link about the new storage SPI "Map Storage SPI": [changes](https://www.keycloak.org/docs/latest/upgrading/#changes-affecting-developers).

## Test Provider

The provider comes with three hardcoded users with the following passwords

* username1 / password1
* username2 / password2
* username3 / password3

You can use the Account screen to verify that the provider is working

http://localhost:8080/realms/myrealm/account/#/

If a Realm "myrealm" was created for the provider.
