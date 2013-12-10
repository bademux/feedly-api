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
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package org.github.bademux.feedly.api.service;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;

import org.github.bademux.feedly.api.oauth2.FeedlyAuthorizationCodeFlow;

/**
 * Test Flow
 */
public class DevFeedlyAuthorizationCodeFlow extends FeedlyAuthorizationCodeFlow {

  public static final String AUTHORIZATION_SERVER_URL = "http://sandbox.feedly.com/v3/auth/auth";

  public static final String TOKEN_SERVER_URL = "http://sandbox.feedly.com/v3/auth/token";


  public DevFeedlyAuthorizationCodeFlow(HttpTransport transport, JsonFactory jsonFactory,
                                        String clientId, String clientSecret) {
    super(new Builder(transport, jsonFactory, clientId, clientSecret));
  }


  public static class Builder extends FeedlyAuthorizationCodeFlow.Builder {

    /**
     * @param transport    HTTP transport
     * @param jsonFactory  JSON factory
     * @param clientId     client identifier
     * @param clientSecret client secret
     */
    public Builder(HttpTransport transport, JsonFactory jsonFactory, String clientId,
                   String clientSecret) {
      super(transport, jsonFactory, clientId, clientSecret,
            TOKEN_SERVER_URL, AUTHORIZATION_SERVER_URL);
    }
  }
}
