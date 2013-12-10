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

package org.github.bademux.feedly.api.service;

import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.util.ObjectParser;
import com.google.api.client.util.Preconditions;

import java.util.logging.Logger;

/** Abstract thread-safe client. */
public abstract class AbstractClient {

  static final Logger LOGGER = Logger.getLogger(AbstractClient.class.getName());

  /** The request factory for connections to the server. */
  private final HttpRequestFactory requestFactory;

  /**
   * Root URL of the service, for example {@code "https://www.googleapis.com/"}. Must be
   * URL-encoded
   * and must end with a "/".
   */
  private final String rootUrl;

  /** Service path, for example {@code "tasks/v1/"}. Must be URL-encoded and must end with a "/". */
  private final String servicePath;

  /** Object parser or {@code null} for none. */
  private final JsonObjectParser objectParser;

  /** Whether discovery pattern checks should be suppressed on required parameters. */
  private boolean suppressPatternChecks;

  /** Whether discovery required parameter checks should be suppressed. */
  private boolean suppressRequiredParameterChecks;

  /** @param builder builder */
  protected AbstractClient(Builder builder) {
    rootUrl = normalizeRootUrl(builder.rootUrl);
    servicePath = normalizeServicePath(builder.servicePath);
    requestFactory = builder.transport.createRequestFactory(builder.httpRequestInitializer);
    objectParser = builder.objectParser;
    suppressPatternChecks = builder.suppressPatternChecks;
    suppressRequiredParameterChecks = builder.suppressRequiredParameterChecks;
  }

  /**
   * Returns the URL-encoded root URL of the service, for example {@code
   * "https://www.googleapis.com/"}.
   *
   * <p> Must end with a "/". </p>
   */
  public final String getRootUrl() {
    return rootUrl;
  }

  /**
   * Returns the URL-encoded service path of the service, for example {@code "tasks/v1/"}.
   *
   * <p> Must end with a "/" and not begin with a "/". It is allowed to be an empty string {@code
   * ""} or a forward slash {@code "/"}, if it is a forward slash then it is treated as an empty
   * string </p>
   */
  public final String getServicePath() {
    return servicePath;
  }

  /**
   * Returns the URL-encoded base URL of the service, for example {@code
   * "https://www.googleapis.com/tasks/v1/"}.
   *
   * <p> Must end with a "/". It is guaranteed to be equal to {@code getRootUrl() +
   * getServicePath()}. </p>
   */
  public final String getBaseUrl() {
    return rootUrl + servicePath;
  }

  /** Returns the HTTP request factory. */
  public final HttpRequestFactory getRequestFactory() {
    return requestFactory;
  }

  /**
   * Returns the object parser or {@code null} for none.
   *
   * <p> Overriding is only supported for the purpose of calling the super implementation and
   * changing the return type, but nothing else. </p>
   */
  public ObjectParser getObjectParser() {
    return objectParser;
  }

  /**
   * Returns whether discovery pattern checks should be suppressed on required parameters.
   */
  public final boolean getSuppressPatternChecks() {
    return suppressPatternChecks;
  }

  /**
   * Returns whether discovery required parameter checks should be suppressed.
   */
  public final boolean getSuppressRequiredParameterChecks() {
    return suppressRequiredParameterChecks;
  }

  /**
   * If the specified root URL does not end with a "/" then a "/" is added to the end.
   */
  static String normalizeRootUrl(String rootUrl) {
    Preconditions.checkNotNull(rootUrl, "root URL cannot be null.");
    if (!rootUrl.endsWith("/")) {
      rootUrl += "/";
    }
    return rootUrl;
  }

  /**
   * If the specified service path does not end with a "/" then a "/" is added to the end. If the
   * specified service path begins with a "/" then the "/" is removed.
   */
  static String normalizeServicePath(String servicePath) {
    Preconditions.checkNotNull(servicePath, "service path cannot be null");
    if (servicePath.length() == 1) {
      Preconditions.checkArgument(
          "/".equals(servicePath), "service path must equal \"/\" if it is of length 1.");
      servicePath = "";
    } else if (servicePath.length() > 0) {
      if (!servicePath.endsWith("/")) {
        servicePath += "/";
      }
      if (servicePath.startsWith("/")) {
        servicePath = servicePath.substring(1);
      }
    }
    return servicePath;
  }

  /**
   * Returns the JSON Factory.
   */
  public JsonFactory getJsonFactory() {
    return objectParser.getJsonFactory();
  }

  /**
   * Builder for {@link AbstractClient}.
   *
   * <p> Implementation is not thread-safe. </p>
   */
  public abstract static class Builder {

    /** HTTP transport. */
    final HttpTransport transport;

    /** HTTP request initializer or {@code null} for none. */
    HttpRequestInitializer httpRequestInitializer;

    /** Object parser to use for parsing responses. */
    final JsonObjectParser objectParser;

    /** The root URL of the service, for example {@code "https://www.googleapis.com/"}. */
    String rootUrl;

    /** The service path of the service, for example {@code "tasks/v1/"}. */
    String servicePath;

    /** Whether discovery pattern checks should be suppressed on required parameters. */
    boolean suppressPatternChecks;

    /** Whether discovery required parameter checks should be suppressed. */
    boolean suppressRequiredParameterChecks;

    /**
     * Returns an instance of a new builder.
     *
     * @param transport              The transport to use for requests
     * @param jsonFactory            JSON factory
     * @param rootUrl                root URL of the service. Must end with a "/"
     * @param servicePath            service path
     * @param httpRequestInitializer HTTP request initializer or {@code null} for none
     */
    protected Builder(HttpTransport transport, JsonFactory jsonFactory, String rootUrl,
                      String servicePath, HttpRequestInitializer httpRequestInitializer) {
      this.transport = Preconditions.checkNotNull(transport);
      this.objectParser = new JsonObjectParser(jsonFactory);
      setRootUrl(rootUrl);
      setServicePath(servicePath);
      this.httpRequestInitializer = httpRequestInitializer;
    }

    /** Builds a new instance of {@link AbstractClient}. */
    public abstract AbstractClient build();

    /**  Returns the HTTP transport. */
    public final HttpTransport getTransport() {
      return transport;
    }

    /**
     * Returns the object parser or {@code null} for none.
     *
     * <p> Overriding is only supported for the purpose of calling the super implementation and
     * changing the return type, but nothing else. </p>
     */
    public JsonObjectParser getObjectParser() {
      return objectParser;
    }

    /**
     * Returns the URL-encoded root URL of the service, for example {@code
     * https://www.googleapis.com/}.
     *
     * <p> Must be URL-encoded and must end with a "/". </p>
     */
    public final String getRootUrl() {
      return rootUrl;
    }

    /**
     * Sets the URL-encoded root URL of the service, for example {@code
     * https://www.googleapis.com/}
     * . <p> If the specified root URL does not end with a "/" then a "/" is added to the end. </p>
     *
     * <p> Overriding is only supported for the purpose of calling the super implementation and
     * changing the return type, but nothing else. </p>
     */
    public Builder setRootUrl(String rootUrl) {
      this.rootUrl = normalizeRootUrl(rootUrl);
      return this;
    }

    /**
     * Returns the URL-encoded service path of the service, for example {@code "tasks/v1/"}.
     *
     * <p> Must be URL-encoded and must end with a "/" and not begin with a "/". It is allowed to
     * be
     * an empty string {@code ""}. </p>
     */
    public final String getServicePath() {
      return servicePath;
    }

    /**
     * Sets the URL-encoded service path of the service, for example {@code "tasks/v1/"}.
     *
     * <p> It is allowed to be an empty string {@code ""} or a forward slash {@code "/"}, if it is
     * a
     * forward slash then it is treated as an empty string. This is determined when the library is
     * generated and normally should not be changed. </p>
     *
     * <p> If the specified service path does not end with a "/" then a "/" is added to the end. If
     * the specified service path begins with a "/" then the "/" is removed. </p>
     *
     * <p> Overriding is only supported for the purpose of calling the super implementation and
     * changing the return type, but nothing else. </p>
     */
    public Builder setServicePath(String servicePath) {
      this.servicePath = normalizeServicePath(servicePath);
      return this;
    }

    /** Returns the HTTP request initializer or {@code null} for none. */
    public final HttpRequestInitializer getHttpRequestInitializer() {
      return httpRequestInitializer;
    }

    /**
     * Sets the HTTP request initializer or {@code null} for none.
     *
     * <p> Overriding is only supported for the purpose of calling the super implementation and
     * changing the return type, but nothing else. </p>
     */
    public Builder setHttpRequestInitializer(HttpRequestInitializer httpRequestInitializer) {
      this.httpRequestInitializer = httpRequestInitializer;
      return this;
    }

    /** Returns whether discovery pattern checks should be suppressed on required parameters. */
    public final boolean getSuppressPatternChecks() {
      return suppressPatternChecks;
    }

    /**
     * Sets whether discovery pattern checks should be suppressed on required parameters.
     *
     * <p> Default value is {@code false}. </p>
     *
     * <p> Overriding is only supported for the purpose of calling the super implementation and
     * changing the return type, but nothing else. </p>
     */
    public Builder setSuppressPatternChecks(boolean suppressPatternChecks) {
      this.suppressPatternChecks = suppressPatternChecks;
      return this;
    }

    /** Returns whether discovery required parameter checks should be suppressed. */
    public final boolean getSuppressRequiredParameterChecks() {
      return suppressRequiredParameterChecks;
    }

    /**
     * Sets whether discovery required parameter checks should be suppressed.
     *
     * <p> Default value is {@code false}. </p>
     *
     * <p> Overriding is only supported for the purpose of calling the super implementation and
     * changing the return type, but nothing else. </p>
     */
    public Builder setSuppressRequiredParameterChecks(boolean suppressRequiredParameterChecks) {
      this.suppressRequiredParameterChecks = suppressRequiredParameterChecks;
      return this;
    }

    /**
     * Suppresses all discovery pattern and required parameter checks.
     *
     * <p> Overriding is only supported for the purpose of calling the super implementation and
     * changing the return type, but nothing else. </p>
     */
    public Builder setSuppressAllChecks(boolean suppressAllChecks) {
      return setSuppressPatternChecks(true).setSuppressRequiredParameterChecks(true);
    }
  }
}
