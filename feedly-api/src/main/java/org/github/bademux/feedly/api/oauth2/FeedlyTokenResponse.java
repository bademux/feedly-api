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

import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.util.Key;

/**
 * <a href="http://developer.feedly.com/v3/auth/#exchanging-a-code-for-a-refresh-token-and-an-access-token">Exchanging
 * a code for a refresh token and an access token</a>.
 *
 * <p> Implementation is not thread-safe. </p>
 */
public class FeedlyTokenResponse extends TokenResponse {

  /**
   * The feedly user Id
   */
  @Key("id")
  private String userId;

  /**
   * Indicated the user plan (standard or pro)
   */
  @Key
  private String plan;

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public String getPlan() {
    return plan;
  }

  public void setPlan(String plan) {
    this.plan = plan;
  }

  @Override
  public FeedlyTokenResponse setAccessToken(String accessToken) {
    return (FeedlyTokenResponse) super.setAccessToken(accessToken);
  }

  @Override
  public FeedlyTokenResponse setTokenType(String tokenType) {
    return (FeedlyTokenResponse) super.setTokenType(tokenType);
  }

  @Override
  public FeedlyTokenResponse setExpiresInSeconds(Long expiresIn) {
    return (FeedlyTokenResponse) super.setExpiresInSeconds(expiresIn);
  }

  @Override
  public FeedlyTokenResponse setRefreshToken(String refreshToken) {
    return (FeedlyTokenResponse) super.setRefreshToken(refreshToken);
  }

  @Override
  public FeedlyTokenResponse setScope(String scope) {
    return (FeedlyTokenResponse) super.setScope(scope);
  }

  @Override
  public FeedlyTokenResponse set(String fieldName, Object value) {
    return (FeedlyTokenResponse) super.set(fieldName, value);
  }

  @Override
  public FeedlyTokenResponse clone() {
    return (FeedlyTokenResponse) super.clone();
  }
}
