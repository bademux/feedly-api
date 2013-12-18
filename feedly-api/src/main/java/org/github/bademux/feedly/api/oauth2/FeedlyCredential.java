/*
 * Copyright 2013 Bademus
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *    Contributors:
 *                 Bademus
 */

package org.github.bademux.feedly.api.oauth2;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.CredentialRefreshListener;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpExecuteInterceptor;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.util.Clock;

import java.io.IOException;
import java.util.Collection;
import java.util.logging.Logger;

public class FeedlyCredential extends Credential {

  static final Logger LOGGER = Logger.getLogger(FeedlyCredential.class.getName());

  /**
   * Feedly user id
   */
  private String userId;

  /**
   * Feedly Plan "standard" or "pro"
   */
  private String plan;


  public String getUserId() { return userId; }

  public FeedlyCredential setUserId(final String userId) { this.userId = userId;  return this;}

  public String getPlan() { return plan; }

  public FeedlyCredential setPlan(final String plan) { this.plan = plan; return this;}

  public FeedlyCredential() { this(new Builder()); }

  protected FeedlyCredential(Builder builder) {
    super(builder);
    userId = builder.userId;
    plan = builder.plan;
  }

  @Override
  public FeedlyCredential setAccessToken(String accessToken) {
    return (FeedlyCredential) super.setAccessToken(accessToken);
  }

  @Override
  public FeedlyCredential setRefreshToken(String refreshToken) {
    return (FeedlyCredential) super.setRefreshToken(refreshToken);
  }

  @Override
  public FeedlyCredential setExpirationTimeMilliseconds(Long expirationTimeMilliseconds) {
    return (FeedlyCredential) super.setExpirationTimeMilliseconds(expirationTimeMilliseconds);
  }

  @Override
  public FeedlyCredential setExpiresInSeconds(Long expiresIn) {
    return (FeedlyCredential) super.setExpiresInSeconds(expiresIn);
  }

  @Override
  public FeedlyCredential setFromTokenResponse(TokenResponse tokenResponse) {
    return setFromTokenResponse((FeedlyTokenResponse) tokenResponse);
  }

  public FeedlyCredential setFromTokenResponse(FeedlyTokenResponse tokenResponse) {
    userId = tokenResponse.getUserId();
    plan = tokenResponse.getPlan();
    return (FeedlyCredential) super.setFromTokenResponse(tokenResponse);
  }

  @Override
  protected FeedlyTokenResponse executeRefreshToken() throws IOException {
    String refreshToken = getRefreshToken();
    if (refreshToken == null) {
      return null;
    }
    return new FeedlyRefreshTokenRequest(getTransport(), getJsonFactory(),
                                         new GenericUrl(getTokenServerEncodedUrl()), refreshToken)
        .setClientAuthentication(getClientAuthentication())
        .setRequestInitializer(getRequestInitializer()).execute();
  }

  /**
   * FeedlyCredential builder.
   *
   * <p> Implementation is not thread-safe. </p>
   */
  public static class Builder extends Credential.Builder {

    /**
     * Feedly user id
     */
    String userId;

    /**
     * Feedly Plan
     */
    String plan;


    public Builder() { super(FeedlyBearerToken.authorizationHeaderAccessMethod()); }

    public String getUserId() { return userId; }

    public void setUserId(final String userId) { this.userId = userId; }

    public String getPlan() { return plan; }

    public void setPlan(final String plan) { this.plan = plan; }

    @Override
    public FeedlyCredential build() { return new FeedlyCredential(this); }

    @Override
    public Builder setTransport(HttpTransport transport) {
      return (Builder) super.setTransport(transport);
    }

    @Override
    public Builder setClock(Clock clock) { return (Builder) super.setClock(clock); }

    @Override
    public Builder setJsonFactory(JsonFactory jsonFactory) {
      return (Builder) super.setJsonFactory(jsonFactory);
    }

    @Override
    public Builder setTokenServerUrl(GenericUrl tokenServerUrl) {
      return (Builder) super.setTokenServerUrl(tokenServerUrl);
    }

    @Override
    public Builder setTokenServerEncodedUrl(String tokenServerEncodedUrl) {
      return (Builder) super.setTokenServerEncodedUrl(tokenServerEncodedUrl);
    }

    @Override
    public Builder setClientAuthentication(HttpExecuteInterceptor clientAuthentication) {
      return (Builder) super.setClientAuthentication(clientAuthentication);
    }

    @Override
    public Builder setRequestInitializer(HttpRequestInitializer requestInitializer) {
      return (Builder) super.setRequestInitializer(requestInitializer);
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
