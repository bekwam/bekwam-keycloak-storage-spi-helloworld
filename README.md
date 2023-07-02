# bekwam-keycloak-storage-spi-helloworld
A sample Keycloak User Federation Storage Provider (legacy as of 19)

This is an example of the Keycloak User Storage SPI.  It can be extended to handle more complex storage cases (ex, a relational database).

To deploy this, do a mvn install and copy the JAR to $KEYCLOAK_HOME/providers.

You'll see the new provider in the Server Info and the User Federation pages.

To trace through the code with logging using the following command.

    $ bin/kc.sh start-dev --log-level=org.bekwam:TRACE
    
If you're creating a new User Storage SPI for the latest Keycloak, be sure to read this link about the new storage SPI "Map Storage SPI": [changes](https://www.keycloak.org/docs/latest/upgrading/#changes-affecting-developers).
