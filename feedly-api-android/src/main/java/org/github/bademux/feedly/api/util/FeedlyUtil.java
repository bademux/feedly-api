/*
 * Copyright (c) 2013 Google Inc.
 *
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

package org.github.bademux.feedly.api.util;

import com.google.api.client.auth.oauth2.AuthorizationCodeResponseUrl;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.repackaged.com.google.common.base.Preconditions;
import com.google.api.client.util.store.DataStoreFactory;

import android.app.Activity;

import org.github.bademux.feedly.api.dev.oauth2.DevFeedlyAuthorizationCodeFlow;
import org.github.bademux.feedly.api.dev.service.DevFeedly;
import org.github.bademux.feedly.api.oauth2.FeedlyAuthorizationCodeFlow;
import org.github.bademux.feedly.api.oauth2.FeedlyCredential;
import org.github.bademux.feedly.api.oauth2.FeedlyOAuthConstants;
import org.github.bademux.feedly.api.oauth2.FeedlyTokenResponse;
import org.github.bademux.feedly.api.service.Feedly;
import org.github.bademux.feedly.api.util.store.AndroidDataStoreFactory;

import java.io.IOException;

import static org.github.bademux.feedly.api.oauth2.FeedlyOAuthConstants.REDIRECT_URI_LOCAL;

public final class FeedlyUtil {

  public FeedlyUtil(Activity activity, String clientId, String clientSecrets)
      throws IOException {
    dataStoreFactory = new AndroidDataStoreFactory(activity);
    flow = new DevFeedlyAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY,
                                                      clientId, clientSecrets)
        .setDataStoreFactory(dataStoreFactory).build();

    credential = flow.loadCredential(USER_ID);
  }

  public String getRequestUrl() throws IOException {
    return flow.newAuthorizationUrl().setRedirectUri(REDIRECT_URI_LOCAL).build();
  }

  public FeedlyCredential processResponse(final String responseUrlStr) throws Exception {
    AuthorizationCodeResponseUrl responseUrl = new AuthorizationCodeResponseUrl(responseUrlStr);
    if (responseUrl.getError() != null) {
      throw new Exception(responseUrl.getError());
    }
    final String access_code = responseUrl.getCode();
    if (access_code == null) {
      throw new Exception("access_code can't be null");
    }

    FeedlyTokenResponse token = flow.newTokenRequest(responseUrl.getCode())
        .setRedirectUri(FeedlyOAuthConstants.REDIRECT_URI_LOCAL).execute();

    return credential = flow.createAndStoreCredential(Preconditions.checkNotNull(token), USER_ID);
  }

  public synchronized Feedly service() {
    if (serviceInstance == null) {
      serviceInstance = new DevFeedly.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential).build();
    }
    return serviceInstance;
  }

  public synchronized void logout() throws IOException {
    service().clearCredential();
    flow.getFeedlyCredentialDataStore().clear();
  }

  public boolean isAuthenticated() {
    return credential != null
           && credential.getAccessToken() != null && credential.getRefreshToken() != null;
  }

  public FeedlyCredential getCredential() { return credential; }

  private FeedlyCredential credential;

  private FeedlyAuthorizationCodeFlow flow;

  /**
   * Global instance of the {@link com.google.api.client.util.store.DataStoreFactory}. The best
   * practice is to make it a single
   * globally shared instance across your application.
   */
  public final DataStoreFactory dataStoreFactory;

  /** Global instance of the HTTP transport. */
  protected static final HttpTransport HTTP_TRANSPORT = AndroidHttp.newCompatibleTransport();

  /** Global instance of the JSON factory. */
  protected static final JsonFactory JSON_FACTORY = new AndroidJsonFactory();

  private volatile Feedly serviceInstance;

  private final static String USER_ID = "UUID";
}
