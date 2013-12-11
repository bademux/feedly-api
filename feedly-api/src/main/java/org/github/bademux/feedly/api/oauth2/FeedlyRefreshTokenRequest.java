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

/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.github.bademux.feedly.api.oauth2;

import com.google.api.client.auth.oauth2.RefreshTokenRequest;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.auth.oauth2.TokenResponseException;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpExecuteInterceptor;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;

import java.io.IOException;
import java.util.Collection;

/**
 * Feedly-specific implementation of the OAuth 2.0 request to refresh an access token using a
 * refresh token as specified in <a href="http://tools.ietf.org/html/rfc6749#section-6">Refreshing
 * an AccessToken</a>.
 *
 * <p> Use {@link com.google.api.client.auth.oauth2.Credential} to access protected resources from
 * the resource server using the {@link TokenResponse} returned by {@link #execute()}. On error, it
 * will instead throw {@link TokenResponseException}. </p>
 *
 * <p> Sample usage: </p>
 *
 * <pre>
 * static void refreshAccessToken() throws IOException {
 * try {
 * TokenResponse response =
 * new FeedlyRefreshTokenRequest(new NetHttpTransport(), new JacksonFactory(),
 * "tGzv3JOkF0XG5Qx2TlKWIA", "s6BhdRkqt3", "7Fjfp0ZBr1KtDRbnfVdmIw").execute();
 * System.out.println("Access token: " + response.getAccessToken());
 * } catch (TokenResponseException e) {
 * if (e.getDetails() != null) {
 * System.err.println("Error: " + e.getDetails().getError());
 * if (e.getDetails().getErrorDescription() != null) {
 * System.err.println(e.getDetails().getErrorDescription());
 * }
 * if (e.getDetails().getErrorUri() != null) {
 * System.err.println(e.getDetails().getErrorUri());
 * }
 * } else {
 * System.err.println(e.getMessage());
 * }
 * }
 * }
 * </pre>
 *
 * <p> Implementation is not thread-safe. </p>
 */
public class FeedlyRefreshTokenRequest extends RefreshTokenRequest {

  /**
   * @param transport      HTTP transport
   * @param jsonFactory    JSON factory
   * @param tokenServerUrl token server URL
   * @param refreshToken   refresh token issued to the client
   */
  public FeedlyRefreshTokenRequest(final HttpTransport transport,
                                   final JsonFactory jsonFactory,
                                   final GenericUrl tokenServerUrl, final String refreshToken) {
    super(transport, jsonFactory, tokenServerUrl, refreshToken);
  }

  @Override
  public FeedlyRefreshTokenRequest setRequestInitializer(
      HttpRequestInitializer requestInitializer) {
    return (FeedlyRefreshTokenRequest) super.setRequestInitializer(requestInitializer);
  }

  @Override
  public FeedlyRefreshTokenRequest setTokenServerUrl(GenericUrl tokenServerUrl) {
    return (FeedlyRefreshTokenRequest) super.setTokenServerUrl(tokenServerUrl);
  }

  @Override
  public FeedlyRefreshTokenRequest setScopes(Collection<String> scopes) {
    return (FeedlyRefreshTokenRequest) super.setScopes(scopes);
  }

  @Override
  public FeedlyRefreshTokenRequest setGrantType(String grantType) {
    return (FeedlyRefreshTokenRequest) super.setGrantType(grantType);
  }

  @Override
  public FeedlyRefreshTokenRequest setClientAuthentication(
      HttpExecuteInterceptor clientAuthentication) {
    return (FeedlyRefreshTokenRequest) super.setClientAuthentication(clientAuthentication);
  }

  @Override
  public FeedlyRefreshTokenRequest setRefreshToken(String refreshToken) {
    return (FeedlyRefreshTokenRequest) super.setRefreshToken(refreshToken);
  }

  @Override
  public FeedlyTokenResponse execute() throws IOException {
    return executeUnparsed().parseAs(FeedlyTokenResponse.class);
  }

  @Override
  public FeedlyRefreshTokenRequest set(String fieldName, Object value) {
    return (FeedlyRefreshTokenRequest) super.set(fieldName, value);
  }
}
