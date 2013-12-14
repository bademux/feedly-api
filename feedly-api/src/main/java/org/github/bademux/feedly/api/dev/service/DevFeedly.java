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

package org.github.bademux.feedly.api.dev.service;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;

import org.github.bademux.feedly.api.oauth2.FeedlyCredential;
import org.github.bademux.feedly.api.service.Feedly;

/**  Test Service */
public class DevFeedly extends Feedly{

  public static final String DEFAULT_ROOT_URL = "http://sandbox.feedly.com/";


  public DevFeedly(HttpTransport transport, JsonFactory jsonFactory,
                   FeedlyCredential httpRequestInitializer) {
    super(new Builder(transport, jsonFactory, httpRequestInitializer));
  }

  public static final class Builder extends Feedly.Builder {

    public Builder(HttpTransport transport, JsonFactory jsonFactory,
                   FeedlyCredential httpRequestInitializer) {
      super(transport, jsonFactory, DEFAULT_ROOT_URL, DEFAULT_SERVICE_PATH, httpRequestInitializer);
    }
  }
}
