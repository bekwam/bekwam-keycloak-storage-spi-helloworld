# bekwam-keycloak-storage-spi-helloworld
A sample Keycloak User Federation Storage Provider (legacy as of 19)

This is an example of the Keycloak User Storage SPI.  It can be extended to handle more complex storage cases (ex, a relational database).

To deploy this, do a mvn install and copy the JAR to $KEYCLOAK_HOME/providers.

You'll see the new provider in the Server Info and the User Federation pages.

To trace through the code with logging using the following command.

    $ bin/kc start-dev --features-disabled=admin2 --log-level=org.bekwam:TRACE
    
This disables the admin2 feature in 19 which has a bug as of 2022/10/09 preventing the display of a text field for "My Parameter".

If you're creating a new User Storage SPI for the latest Keycloak, be sure to read this link about the new storage SPI "Map Storage SPI": [changes](https://www.keycloak.org/docs/latest/upgrading/#changes-affecting-developers).
