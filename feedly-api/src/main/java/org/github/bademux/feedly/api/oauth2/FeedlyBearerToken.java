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

import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.HttpRequest;

import java.io.IOException;
import java.util.List;

public class FeedlyBearerToken extends BearerToken {

  static final class FeedlyWorkadoundAuthorizationHeaderAccessMethod
      implements Credential.AccessMethod {

    FeedlyWorkadoundAuthorizationHeaderAccessMethod() {}

    public void intercept(HttpRequest request, String accessToken) throws IOException {
      request.getHeaders().setAuthorization(accessToken);
    }

    public String getAccessTokenFromRequest(HttpRequest request) {
      List<String> authorizationAsList = request.getHeaders().getAuthorizationAsList();
      if (authorizationAsList != null) {
        for (String header : authorizationAsList) {
          return header;
        }
      }
      return null;
    }
  }

  /**
   * <p>
   * hackfixed com.google.api.client.auth.oauth2.BearerToken.AuthorizationHeaderAccessMethod
   * see https://groups.google.com/forum/?fromgroups=#!topic/feedly-cloud/zidIbXiId18
   * </p>
   *
   * Returns a new instance of an immutable and thread-safe OAuth 2.0 method for accessing protected
   * resources using the <a href="http://tools.ietf.org/html/rfc6750#section-2.1">Authorization
   * Request Header Field</a>.
   *
   * <p>
   * According to the specification, this method MUST be supported by resource servers.
   * </p>
   */
  public static Credential.AccessMethod authorizationHeaderAccessMethod() {
    return new FeedlyWorkadoundAuthorizationHeaderAccessMethod();
  }
}
