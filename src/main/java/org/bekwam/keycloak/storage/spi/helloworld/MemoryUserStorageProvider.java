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

import java.util.Collections;
import java.util.Map;
import java.util.Set;
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

    /**
     * List of users that will appear in the Users screen
     */
    private final Map<String, String> ramUsers = Map.of(
            "user1", "password1",
            "user2", "password2",
            "user3", "password3"
    );

    public MemoryUserStorageProvider(
            KeycloakSession keycloakSession,
            ComponentModel componentModel,
            String myParameter) {
        this.keycloakSession = keycloakSession;
        this.componentModel = componentModel;
        this.myParameter = myParameter;
    }

    @Override
    public boolean updateCredential(RealmModel realmModel, UserModel userModel, CredentialInput credentialInput) {
        if (credentialInput.getType().equals(PasswordCredentialModel.TYPE))
            throw new ReadOnlyException("user is read only for this update");
        return false;
    }

    @Override
    public void disableCredentialType(RealmModel realmModel, UserModel userModel, String s) {}

    @Override
    public Set<String> getDisableableCredentialTypes(RealmModel realmModel, UserModel userModel) {
        return Collections.EMPTY_SET;
    }

    @Override
    public boolean supportsCredentialType(String credentialType) {
        return credentialType.equals(PasswordCredentialModel.TYPE);
    }

    @Override
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
    @Override
    public boolean isValid(RealmModel realmModel, UserModel userModel, CredentialInput credentialInput) {
        logger.trace("MemoryUserStorageProvider.isValid()");
        if( credentialInput.getType().equals("password")) {
            String password = ramUsers.get(userModel.getUsername());
            if( password != null ) {
                return password.equals(credentialInput.getChallengeResponse());
            }
            return false;
        } else {
            throw new UnsupportedOperationException("only credential type 'password' is supported");
        }
    }

    @Override
    public void close() {}

    @Override
    public UserModel getUserById(String id, RealmModel realmModel) {
        logger.trace("getUserById(), id=" + id);
        StorageId storageId = new StorageId(id);
        String username = storageId.getExternalId();
        return getUserByUsername(username, realmModel);
    }

    @Override
    public UserModel getUserByUsername(String username, RealmModel realmModel) {
        if( ramUsers.containsKey(username) ) {
            return new AbstractUserAdapterFederatedStorage(keycloakSession, realmModel, componentModel) {
                @Override
                public String getUsername() {
                    return username;
                }

                @Override
                public void setUsername(String s) {
                    throw new UnsupportedOperationException("memory read-only for now");
                }
            };
        }
        return null;
    }

    @Override
    public UserModel getUserByEmail(String s, RealmModel realmModel) {
        return null;
    }

    @Override
    public int getUsersCount(RealmModel realm) {
        return this.getUsers(realm).size();
    }

    @Override
    public Stream<UserModel> getUsersStream(RealmModel realm) {
        return this.getUsersStream(realm, 0, Integer.MAX_VALUE);
    }

    @Override
    public Stream<UserModel> getUsersStream(RealmModel realmModel, Integer first, Integer max) {
        return ramUsers
                .entrySet()
                .stream()
                .skip(first)
                .limit(max - first+1)
                .map( kv -> new AbstractUserAdapterFederatedStorage(keycloakSession, realmModel, componentModel) {
                    @Override
                    public String getUsername() {
                        return kv.getKey();
                    }

                    @Override
                    public void setUsername(String s) {
                        throw new UnsupportedOperationException("memory read-only for now");
                    }
                });
    }

    @Override
    public Stream<UserModel> searchForUserStream(RealmModel realm, String search) {
        logger.trace("searchForUserStream() #1");
        return UserQueryProvider.Streams.super.searchForUserStream(realm, search);
    }

    @Override
    public Stream<UserModel> searchForUserStream(RealmModel realm, String search, Integer firstResult, Integer maxResults) {
        logger.trace("searchForUserStream() #2; search=" + search);
        return ramUsers
                .entrySet()
                .stream()
                .map( kv -> new AbstractUserAdapterFederatedStorage(keycloakSession, realm, componentModel) {
                    @Override
                    public String getUsername() {
                        return kv.getKey();
                    }

                    @Override
                    public void setUsername(String s) {
                        throw new UnsupportedOperationException("memory read-only for now");
                    }
                });
    }

    @Override
    public Stream<UserModel> searchForUserStream(RealmModel realm, Map<String, String> params) {
        logger.trace("searchForUserStream() #3");
        // this method needs to exist for paging
        return this.searchForUserStream(realm, "", 0, Integer.MAX_VALUE);
    }

    @Override
    public Stream<UserModel> searchForUserStream(RealmModel realmModel, Map<String, String> params, Integer firstResult, Integer maxResults) {
        logger.trace("searchForUserStream() #4; params=" + params);
        return this.searchForUserStream(realmModel, "", firstResult, maxResults);
    }

    @Override
    public Stream<UserModel> getGroupMembersStream(RealmModel realmModel, GroupModel groupModel, Integer firstResult, Integer maxResults) {
        return null;
    }

    @Override
    public Stream<UserModel> searchForUserByUserAttributeStream(RealmModel realmModel, String s, String s1) {
        return null;
    }
}
