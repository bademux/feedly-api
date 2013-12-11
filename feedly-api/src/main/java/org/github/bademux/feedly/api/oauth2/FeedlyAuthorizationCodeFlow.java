/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *  Contributors:
 *               Bademus
 */

package org.github.bademux.feedly.api.oauth2;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.ClientParametersAuthentication;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.CredentialRefreshListener;
import com.google.api.client.auth.oauth2.DataStoreCredentialRefreshListener;
import com.google.api.client.auth.oauth2.StoredCredential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpExecuteInterceptor;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.util.Clock;
import com.google.api.client.util.Preconditions;
import com.google.api.client.util.store.DataStore;
import com.google.api.client.util.store.DataStoreFactory;

import org.github.bademux.feedly.api.service.Feedly;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import static org.github.bademux.feedly.api.oauth2.FeedlyOAuthConstants.AUTHORIZATION_SERVER_URL;
import static org.github.bademux.feedly.api.oauth2.FeedlyOAuthConstants.TOKEN_SERVER_URL;
import static org.github.bademux.feedly.api.oauth2.FeedlyStoredCredential.getDefaultDataStore;

public class FeedlyAuthorizationCodeFlow extends AuthorizationCodeFlow {

  /** Credential created listener or {@code null} for none. */
  private final CredentialCreatedListener credentialCreatedListener;

  /** Stored credential data store or {@code null} for none. */
  private final DataStore<FeedlyStoredCredential> credentialDataStore;

  /**
   * @param transport    HTTP transport
   * @param jsonFactory  JSON factory
   * @param clientId     client identifier
   * @param clientSecret client secret
   */
  public FeedlyAuthorizationCodeFlow(HttpTransport transport, JsonFactory jsonFactory,
                                     String clientId, String clientSecret) {
    this(new Builder(transport, jsonFactory, clientId, clientSecret));
  }

  protected FeedlyAuthorizationCodeFlow(Builder builder) {
    super(builder);
    credentialCreatedListener = builder.getCredentialCreatedListener();
    credentialDataStore = builder.getFeedlyCredentialDataStore();
  }

  /**
   * Use this on "normal" Feedly flow if you already get authorizationCode
   */
  @Override
  public FeedlyAuthorizationCodeTokenRequest newTokenRequest(String authorizationCode) {
    return new FeedlyAuthorizationCodeTokenRequest(getTransport(), getJsonFactory(),
                                                   getTokenServerEncodedUrl(), authorizationCode)
        .setClientAuthentication(getClientAuthentication())
        .setRequestInitializer(getRequestInitializer())
        .setScopes(getScopes());
  }


  /**
   * Creates a new credential for the given user ID based on the given token response and store in
   * the credential store.
   *
   * @param response token response
   * @param userId   user ID or {@code null} if not using a persisted credential store
   * @return newly created credential
   */
  @Override
  public FeedlyCredential createAndStoreCredential(TokenResponse response, String userId)
      throws IOException {
    return createAndStoreCredential((FeedlyTokenResponse) response, userId);
  }

  public FeedlyCredential createAndStoreCredential(FeedlyTokenResponse response, String userId)
      throws IOException {
    FeedlyCredential credential = newFeedlyCredential(userId).setFromTokenResponse(response);
    DataStore<FeedlyStoredCredential> store = getFeedlyCredentialDataStore();
    if (store != null) {
      store.set(userId, new FeedlyStoredCredential(credential));
    }
    if (credentialCreatedListener != null) {
      credentialCreatedListener.onCredentialCreated(credential, response);
    }
    return credential;
  }

  /**
   * Loads the credential of the given user ID from the credential store.
   *
   * @param userId user ID or {@code null} if not using a persisted credential store
   * @return credential found in the credential store of the given user ID or {@code null} for none
   * found
   */
  @Override
  public FeedlyCredential loadCredential(String userId) throws IOException {
    DataStore<FeedlyStoredCredential> store = getFeedlyCredentialDataStore();
    if (store == null) {
      return null;
    }
    FeedlyStoredCredential stored = store.get(userId);
    if (stored == null) {
      return null;
    }
    return newFeedlyCredential(userId)
        .setAccessToken(stored.getAccessToken())
        .setRefreshToken(stored.getRefreshToken())
        .setExpirationTimeMilliseconds(stored.getExpirationTimeMilliseconds())
        .setUserId(stored.getUserId())
        .setPlan(stored.getPlan());
  }

  /**
   * Returns a new credential instance based on the given user ID.
   *
   * @param userId user ID or {@code null} if not using a persisted credential store
   */
  private FeedlyCredential newFeedlyCredential(String userId) {
    FeedlyCredential.Builder builder = new FeedlyCredential.Builder()
        .setTransport(getTransport()).setJsonFactory(getJsonFactory())
        .setTokenServerEncodedUrl(getTokenServerEncodedUrl())
        .setClientAuthentication(getClientAuthentication())
        .setRequestInitializer(getRequestInitializer())
        .setClock(getClock());
    DataStore<StoredCredential> store = getCredentialDataStore();
    if (store != null) {
      builder.addRefreshListener(
          new DataStoreCredentialRefreshListener(userId, store));
    }
    builder.getRefreshListeners().addAll(getRefreshListeners());
    return builder.build();
  }

  /**
   * Returns the stored credential data store or {@code null} for none.
   */
  public final DataStore<FeedlyStoredCredential> getFeedlyCredentialDataStore() {
    return credentialDataStore;
  }

  /**
   * Feedly authorization code flow builder.
   *
   * <p> Implementation is not thread-safe. </p>
   */
  public static class Builder extends AuthorizationCodeFlow.Builder {

    DataStore<FeedlyStoredCredential> credentialDataStore;

    /**
     * @param transport    HTTP transport
     * @param jsonFactory  JSON factory
     * @param clientId     client identifier
     * @param clientSecret client secret
     */
    public Builder(HttpTransport transport, JsonFactory jsonFactory, String clientId,
                   String clientSecret) {
      this(transport, jsonFactory, clientId, clientSecret,
           TOKEN_SERVER_URL, AUTHORIZATION_SERVER_URL);
    }

    protected Builder(HttpTransport transport, JsonFactory jsonFactory,
                      String clientId, String clientSecret,
                      String tokenServerUrl, String authorizationServerEncodedUrl) {
      super(FeedlyBearerToken.authorizationHeaderAccessMethod(), transport, jsonFactory,
            new GenericUrl(tokenServerUrl),
            new ClientParametersAuthentication(clientId, clientSecret),
            clientId, authorizationServerEncodedUrl);
      setScopes(Arrays.asList(Feedly.SCOPE));
    }


    @Override
    public FeedlyAuthorizationCodeFlow build() {
      return new FeedlyAuthorizationCodeFlow(this);
    }

    @Override
    public Builder setDataStoreFactory(DataStoreFactory dataStoreFactory) throws IOException {
      return setFeedlyCredentialDataStore(getDefaultDataStore(dataStoreFactory));
    }


    public final DataStore<FeedlyStoredCredential> getFeedlyCredentialDataStore() {
      return credentialDataStore;
    }

    public Builder setFeedlyCredentialDataStore(
        DataStore<FeedlyStoredCredential> credentialDataStore) {
      this.credentialDataStore = credentialDataStore;
      return this;
    }

    @Override
    public Builder setCredentialDataStore(DataStore<StoredCredential> credentialDataStore) {
      throw new AssertionError();
    }

    @Override
    public Builder setCredentialCreatedListener(
        CredentialCreatedListener credentialCreatedListener) {
      return (Builder) super.setCredentialCreatedListener(credentialCreatedListener);
    }

    @Override
    public Builder setRequestInitializer(HttpRequestInitializer requestInitializer) {
      return (Builder) super.setRequestInitializer(requestInitializer);
    }

    @Override
    public Builder setScopes(Collection<String> scopes) {
      Preconditions.checkState(!scopes.isEmpty());
      return (Builder) super.setScopes(scopes);
    }

    @Override
    public Builder setMethod(Credential.AccessMethod method) {
      return (Builder) super.setMethod(method);
    }

    @Override
    public Builder setTransport(HttpTransport transport) {
      return (Builder) super.setTransport(transport);
    }

    @Override
    public Builder setJsonFactory(JsonFactory jsonFactory) {
      return (Builder) super.setJsonFactory(jsonFactory);
    }

    @Override
    public Builder setTokenServerUrl(GenericUrl tokenServerUrl) {
      return (Builder) super.setTokenServerUrl(tokenServerUrl);
    }

    @Override
    public Builder setClientAuthentication(HttpExecuteInterceptor clientAuthentication) {
      return (Builder) super.setClientAuthentication(clientAuthentication);
    }

    @Override
    public Builder setClientId(String clientId) {
      return (Builder) super.setClientId(clientId);
    }

    @Override
    public Builder setAuthorizationServerEncodedUrl(String authorizationServerEncodedUrl) {
      return (Builder) super.setAuthorizationServerEncodedUrl(authorizationServerEncodedUrl);
    }

    @Override
    public Builder setClock(Clock clock) {
      return (Builder) super.setClock(clock);
    }

    @Override
    public Builder addRefreshListener(CredentialRefreshListener refreshListener) {
      return (Builder) super.addRefreshListener(refreshListener);
    }

    @Override
    public Builder setRefreshListeners(Collection<CredentialRefreshListener> refreshListeners) {
      return (Builder) super.setRefreshListeners(refreshListeners);
    }
  }
}
