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

package org.github.bademux.feedly.api;

import com.google.api.client.auth.oauth2.AuthorizationCodeRequestUrl;
import com.google.api.client.auth.oauth2.AuthorizationCodeResponseUrl;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.GenericJson;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;

import org.github.bademux.feedly.api.dev.oauth2.DevFeedlyAuthorizationCodeFlow;
import org.github.bademux.feedly.api.dev.service.DevFeedly;
import org.github.bademux.feedly.api.model.Category;
import org.github.bademux.feedly.api.model.Subscription;
import org.github.bademux.feedly.api.model.Tag;
import org.github.bademux.feedly.api.oauth2.FeedlyAuthorizationCodeFlow;
import org.github.bademux.feedly.api.oauth2.FeedlyCredential;
import org.github.bademux.feedly.api.oauth2.FeedlyTokenResponse;
import org.github.bademux.feedly.api.service.Feedly;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import static com.google.api.client.repackaged.com.google.common.base.Preconditions.checkArgument;
import static com.google.api.client.repackaged.com.google.common.base.Preconditions.checkNotNull;
import static org.github.bademux.feedly.api.oauth2.FeedlyOAuthConstants.REDIRECT_URI_LOCAL;

public abstract class AbstractIntegrationTest {

  public void setUp() throws IOException {
    FeedlyCredential credential = login();
    //setup Feedly service
    service = new DevFeedly.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential).build();
  }

  public void cleanUp() throws IOException {
    //cleanUp
    for (Subscription subscription : service.subscriptions().list().execute()) {
      service.subscriptions().detete(subscription).execute();
    }

    for (Category category : service.categories().list().execute()) {
      service.categories().detete(category).execute();
    }

    List<Tag> tags = service.tags().list().execute();
    if (!tags.isEmpty()) {
      service.tags().deteteByTag(tags).execute();
    }
  }

  public static Subscription newSubscriptionWithCategory(Feedly service, String feedUrl)
      throws IOException {
    Subscription subscription = new Subscription(feedUrl, "Test");
    subscription.addCategory(service.newCategory("Test1"));
    subscription.addCategory(service.newCategory("Test2"));
    service.subscriptions().update(subscription).execute();
    return subscription;
  }

  public FeedlyCredential login() throws IOException {
    Properties test_prop = load("test_credential.properties");
    TEST_USER = checkNotNull(test_prop.getProperty("wordpress.user"));
    TEST_PASSWORD = checkNotNull(test_prop.getProperty("wordpress.password"));

    //login
    Properties secrets = load("user_secrets.properties");
    String clientId = checkNotNull(secrets.getProperty("feedly.client_id"));
    String clientSecret = checkNotNull(secrets.getProperty("feedly.client_secret"));
    LOG.info("Using client_id:" + clientId + ", client_secret:" + clientSecret);
    FeedlyAuthorizationCodeFlow flow = new DevFeedlyAuthorizationCodeFlow.Builder(
        HTTP_TRANSPORT, JSON_FACTORY, clientId, clientSecret)
        .setDataStoreFactory(new FileDataStoreFactory(DATA_STORE_DIR)).build();

    FeedlyCredential credential = flow.loadCredential(TEST_USER);
    if (credential == null) {
      credential = checkNotNull(newFeedlyCredential(flow));
    }

    LOG.info("Use acess_token" + credential.getAccessToken() +
             "UserId: " + credential.getUserId() + " Plan:" + credential.getPlan());
    //setup Feedly service
    return credential;
  }

  private static FeedlyCredential newFeedlyCredential(FeedlyAuthorizationCodeFlow flow)
      throws IOException {
    LOG.info("Fetch new access_token");
    AuthorizationCodeRequestUrl oauthUrlObject = flow.newAuthorizationUrl()
        .setRedirectUri(REDIRECT_URI_LOCAL).setState("com.feedly.developer.test-state");
    String requestUrl = oauthUrlObject.build();
    LOG.info("OAuth2 Url: " + requestUrl);
    String responseUrlStr = authWithWordpress(TEST_USER, TEST_PASSWORD, requestUrl);

    // direct the end-user's browser to an authorization page to grant access to their protected data.
    AuthorizationCodeResponseUrl responseUrl = new AuthorizationCodeResponseUrl(responseUrlStr);
    if (responseUrl.getError() != null || responseUrl.getCode() == null) {
      LOG.severe(responseUrl.getErrorDescription());
      return null;
    }

    //check state
    checkArgument(oauthUrlObject.getState().equals(responseUrl.getState()));

    FeedlyTokenResponse token = flow.newTokenRequest(responseUrl.getCode())
        .setRedirectUri(oauthUrlObject.getRedirectUri()).execute();

    return flow.createAndStoreCredential(checkNotNull(token), TEST_USER);
  }

  public static Properties load(String fileName) throws IOException {
    Properties prop = new Properties();
    prop.load(AbstractIntegrationTest.class.getClassLoader().getResourceAsStream(fileName));
    return prop;
  }

  public static String authWithWordpress(String usr, String pwd, String oauthUrl)
      throws IOException {
    //1. This will load the home page and get Auth link
    String authUrl = Jsoup.connect(oauthUrl).timeout(5000).get()
        .getElementsByAttributeValueContaining("href", "wordpress.com").attr("href");

    //2. Auth
    Element loginform = Jsoup.connect(authUrl).referrer(oauthUrl).get().getElementById("loginform");
    if (loginform == null) {
      throw new IllegalStateException("Can't parse loginform");
    }

    //2.1 Login
    Connection.Response response = Jsoup.connect(loginform.attr("action"))
        .data("log", usr)
        .data("pwd", pwd)
        .data("wp-submit", loginform.select("input[name=wp-submit]").attr("value"))
        .data("action", loginform.select("input[name=action]").attr("value"))
        .data("redirect_to", loginform.select("input[name=redirect_to]").attr("value"))
        .method(Connection.Method.POST).timeout(5000).execute();

    // 2.2 Approve
    Element loginformApprove = response.parse().getElementById("loginform");
    if (loginformApprove == null) {
      throw new IllegalStateException("Can't parse loginform");
    }

    String redirectUrl = Jsoup.connect(loginformApprove.attr("action")).cookies(response.cookies())
        .data("client_id", loginformApprove.select("input[name=client_id]").attr("value"))
        .data("response_type", loginformApprove.select("input[name=response_type]").attr("value"))
        .data("redirect_uri", loginformApprove.select("input[name=redirect_uri]").attr("value"))
        .data("state", loginformApprove.select("input[name=state]").attr("value"))
        .data("action", loginformApprove.select("input[name=action]").attr("value"))
        .data("blog_id", loginformApprove.select("input[name=blog_id]").attr("value"))
        .data("_wpnonce", loginformApprove.select("input[name=_wpnonce]").attr("value"))
        .data("wp-submit", loginformApprove.select("input[name=wp-submit]").attr("value"))
        .data("redirect_to", loginformApprove.select("input[name=redirect_to]").attr("value"))
        .method(Connection.Method.GET).followRedirects(false)
        .timeout(5000).execute().header("Location");

    // 3  link with code
    return Jsoup.connect(redirectUrl).ignoreContentType(true).followRedirects(false)
        .timeout(5000).method(Connection.Method.GET).execute().header("Location");
  }

  protected <T extends GenericJson> T findIn(Collection<T> list, String id) {
    if (!list.isEmpty() && id != null) {
      for (T item : list) {
        if (((String) item.get("id")).endsWith(id)) {
          return item;
        }
      }
    }
    return null;
  }

  protected Feedly service;
  protected static String TEST_USER;
  protected static String TEST_PASSWORD;

  //init logger and markAs conf
  private final static Logger LOG = Logger.getLogger(AbstractIntegrationTest.class.getSimpleName());

  //Workaround for http://issues.gradle.org/browse/GRADLE-2524
  static {
    try {
      LogManager.getLogManager()
          .readConfiguration(
              AbstractIntegrationTest.class.getResourceAsStream("/logging.properties")
          );
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /** Directory to store user credentials. */
  public static final File DATA_STORE_DIR =
      new File(AbstractIntegrationTest.class.getResource("/").getPath(), "tmp");

  /** Global instance of the HTTP transport. */
  protected static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();

  /** Global instance of the JSON factory. */
  protected static final JsonFactory JSON_FACTORY = new GsonFactory();
}