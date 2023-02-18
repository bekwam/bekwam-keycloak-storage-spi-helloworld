package org.bekwam.keycloak.storage.spi.helloworld;

import org.jboss.logging.Logger;
import org.keycloak.component.ComponentModel;
import org.keycloak.credential.CredentialInput;
import org.keycloak.credential.CredentialInputUpdater;
import org.keycloak.credential.CredentialInputValidator;
import org.keycloak.models.GroupModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.credential.PasswordCredentialModel;
import org.keycloak.storage.ReadOnlyException;
import org.keycloak.storage.StorageId;
import org.keycloak.storage.UserStorageProvider;
import org.keycloak.storage.adapter.AbstractUserAdapterFederatedStorage;
import org.keycloak.storage.user.UserLookupProvider;
import org.keycloak.storage.user.UserQueryProvider;

import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalField;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Manages a fixed, RAM-based user storage for demonstration purposes
 *
 * The search functions are simplistic and will return all 3 records regardless
 * of criteria.  Use "View all users" to see these users merged with any other
 * users managed by Keycloak or other federated identities.
 *
 * @since 1.0
 * @author carl
 */
public class MemoryUserStorageProvider implements
        UserStorageProvider,
        UserLookupProvider,
        CredentialInputValidator,
        CredentialInputUpdater,
        UserQueryProvider.Streams {

    private static final Logger logger = Logger.getLogger(MemoryUserStorageProvider.class);

    protected final KeycloakSession keycloakSession;
    protected final ComponentModel componentModel;
    protected final String myParameter;

    record CustomUser(
            String username,
            String password,
            String email,
            String firstName,
            String lastName,
            ZonedDateTime createdDate
    ) {}

    /**
     * List of users that will appear in the Users screen
     */
    private final List<CustomUser> ramUsers = List.of(
            new CustomUser("user1", "password1", "user1@example.com", "George", "Washington", ZonedDateTime.parse("2023-01-01T12:00:00.000+05:00")),
            new CustomUser("user2", "password2", "user2@example.com", "Abraham", "Lincoln", ZonedDateTime.parse("2023-01-15T13:00:00.000+05:00")),
            new CustomUser("user3", "password3", "user3@example.com", "Franklin", "Roosevelt", ZonedDateTime.parse("2023-02-02T09:00:00.000+05:00"))
    );


    public UserModel userModel(RealmModel realmModel, CustomUser user) {
        return
                new AbstractUserAdapterFederatedStorage(
                        MemoryUserStorageProvider.this.keycloakSession,
                        realmModel,
                        MemoryUserStorageProvider.this.componentModel) {
                    @Override
                    public String getUsername() {
                        return user.username();
                    }

                    @Override
                    public void setUsername(String s) {
                        throw new UnsupportedOperationException("memory read-only for now");
                    }

                    @Override
                    public String getEmail() {
                        return user.email();
                    }

                    @Override
                    public void setEmail(String email) {
                        throw new UnsupportedOperationException("memory read-only for now");
                    }

                    @Override
                    public String getFirstName() {
                        return user.firstName();
                    }

                    @Override
                    public void setFirstName(String firstName) {
                        throw new UnsupportedOperationException("memory read-only for now");
                    }

                    @Override
                    public String getLastName() {
                        return user.lastName();
                    }

                    @Override
                    public void setLastName(String lastName) {
                        throw new UnsupportedOperationException("memory read-only for now");
                    }

                    @Override
                    public Long getCreatedTimestamp() {
                        return Timestamp.valueOf(user.createdDate().toLocalDateTime()).getTime();
                    }

                    @Override
                    public void setCreatedTimestamp(Long timestamp) {
                        throw new UnsupportedOperationException("memory read-only for now");
                    }
                };
    }

    public MemoryUserStorageProvider(
            KeycloakSession keycloakSession,
            ComponentModel componentModel,
            String myParameter) {
        this.keycloakSession = keycloakSession;
        this.componentModel = componentModel;
        this.myParameter = myParameter;
    }

    public boolean updateCredential(RealmModel realmModel, UserModel userModel, CredentialInput credentialInput) {
        if (credentialInput.getType().equals(PasswordCredentialModel.TYPE))
            throw new ReadOnlyException("user is read only for this update");
        return false;
    }

    public void disableCredentialType(RealmModel realmModel, UserModel userModel, String s) {}

    public boolean supportsCredentialType(String credentialType) {
        return credentialType.equals(PasswordCredentialModel.TYPE);
    }

    public boolean isConfiguredFor(RealmModel realmModel, UserModel userModel, String credentialType) {
        return credentialType.equals(PasswordCredentialModel.TYPE);
    }

    /**
     * This is a cleartext comparison but it could apply an encoding and a hash
     * depending on the store
     *
     * @param realmModel
     * @param userModel
     * @param credentialInput
     * @return
     */
    public boolean isValid(RealmModel realmModel, UserModel userModel, CredentialInput credentialInput) {
        logger.trace("MemoryUserStorageProvider.isValid()");
        if( credentialInput.getType().equals(PasswordCredentialModel.TYPE)) {
            Optional<CustomUser> user = findUserInRAMStore(userModel.getUsername());
            if( user.isPresent() ) {
                logger.debug("found user=" + user.get().username() + "; checking password");
                return user.get().password().equals(credentialInput.getChallengeResponse());
            }
            logger.debug("did not find user=" + user.get().username());
            return false;
        } else {
            throw new UnsupportedOperationException("only credential type '" + PasswordCredentialModel.TYPE +"' is supported");
        }
    }

    public void close() {}

    public int getUsersCount(RealmModel realm) {
        return (int)this.getUsersStream(realm).count();
    }


    public Stream<UserModel> getUsersStream(RealmModel realm) {
        return this.getUsersStream(realm, 0, Integer.MAX_VALUE);
    }

    public Stream<UserModel> getUsersStream(RealmModel realmModel, Integer first, Integer max) {
        return ramUsers
                .stream()
                .skip(first)
                .limit(max - first+1)
                .map( u -> userModel(realmModel, u));
    }

    public Stream<UserModel> searchForUserStream(RealmModel realm, String search) {
        logger.trace("searchForUserStream() #1");
        return UserQueryProvider.Streams.super.searchForUserStream(realm, search);
    }

    public Stream<UserModel> searchForUserStream(RealmModel realmModel, String search, Integer firstResult, Integer maxResults) {
        logger.trace("searchForUserStream() #2; search=" + search);
        return ramUsers
                .stream()
                .map( u -> userModel(realmModel, u));
    }

    public Stream<UserModel> searchForUserStream(RealmModel realm, Map<String, String> params) {
        logger.trace("searchForUserStream() #3");
        // this method needs to exist for paging
        return this.searchForUserStream(realm, "", 0, Integer.MAX_VALUE);
    }

    public Stream<UserModel> searchForUserStream(RealmModel realmModel, Map<String, String> params, Integer firstResult, Integer maxResults) {
        logger.trace("searchForUserStream() #4; params=" + params);
        return this.searchForUserStream(realmModel, "", firstResult, maxResults);
    }

    public Stream<UserModel> getGroupMembersStream(RealmModel realmModel, GroupModel groupModel, Integer firstResult, Integer maxResults) {
        return null;
    }

    public Stream<UserModel> searchForUserByUserAttributeStream(RealmModel realmModel, String s, String s1) {
        return null;
    }

    public UserModel getUserByEmail(RealmModel realmModel, String s) {
        return null;
    }

    @Override
    public UserModel getUserByUsername(RealmModel realmModel, String username) {
        var user = findUserInRAMStore(username);
        if( user.isPresent() ) {
            return userModel(realmModel, user.get());
        }
        return null;
    }

    @Override
    public UserModel getUserById(RealmModel realmModel, String id) {
        logger.trace("getUserById(), id=" + id);
        StorageId storageId = new StorageId(id);
        String username = storageId.getExternalId();
        return getUserByUsername(realmModel, username);
    }

    @Override
    public Stream<String> getDisableableCredentialTypesStream(RealmModel realmModel, UserModel userModel) {
        return Stream.of(PasswordCredentialModel.TYPE);
    }

    private Optional<CustomUser> findUserInRAMStore(String username) {
        if( username == null || username.isBlank() ) {
            return Optional.empty();
        }
        return ramUsers
                .stream()
                .filter(
                        u -> u.username().equals(username)
                )
                .findFirst();

    }
}
