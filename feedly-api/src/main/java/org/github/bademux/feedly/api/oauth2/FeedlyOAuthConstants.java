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


public class FeedlyOAuthConstants {

  /**
   * Authorization server URL
   */
  public static final String AUTHORIZATION_SERVER_URL = "http://cloud.feedly.com/v3/auth/auth";

  /**
   * Token server URL
   */
  public static final String TOKEN_SERVER_URL = "http://cloud.feedly.com/v3/auth/token";


  public static final String REDIRECT_URI_LOCAL = "http://localhost";

  public static final String REDIRECT_URN = "urn:ietf:wg:oauth:2.0:oob";

  private FeedlyOAuthConstants() {
  }
}
