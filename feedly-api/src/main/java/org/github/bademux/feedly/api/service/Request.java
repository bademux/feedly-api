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

package org.github.bademux.feedly.api.service;

import com.google.api.client.http.EmptyContent;
import com.google.api.client.http.GZipEncoding;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpContent;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpMethods;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.HttpResponseInterceptor;
import com.google.api.client.http.UriTemplate;
import com.google.api.client.http.json.JsonHttpContent;
import com.google.api.client.util.GenericData;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

import static com.google.api.client.util.Preconditions.checkArgument;
import static com.google.api.client.util.Preconditions.checkNotNull;

/**
 * Abstract Google client request for a {@link AbstractClient}.
 *
 * <p> Implementation is not thread-safe. </p>
 *
 * @param <T> type of the response
 */
public abstract class Request<T> extends GenericData {

  /** client. */
  private final AbstractClient abstractClient;

  /** HTTP method. */
  private final String requestMethod;

  /** URI template for the path relative to the base URL. */
  private final String uriTemplate;

  /** HTTP content or {@code null} for none. */
  private final HttpContent httpContent;

  /** HTTP headers used for the Google client request. */
  private HttpHeaders requestHeaders = new HttpHeaders();

  /** HTTP headers of the last response or {@code null} before request has been executed. */
  private HttpHeaders lastResponseHeaders;

  /** Status code of the last response or {@code -1} before request has been executed. */
  private int lastStatusCode = -1;

  /** Status message of the last response or {@code null} before request has been executed. */
  private String lastStatusMessage;

  /** Whether to disable GZip compression of HTTP content. */
  private boolean disableGZipContent;

  /** Response class to parse into. */
  private Class<T> responseClass;

  /**
   * @param abstractClient client
   * @param requestMethod  HTTP Method
   * @param uriTemplate    URI template for the path relative to the base URL. If it starts with a
   *                       "/" the base path from the base URL will be stripped out. The URI
   *                       template can also be a full URL. URI template expansion is done using
   *                       {@link UriTemplate#expand(String, String, Object, boolean)}
   * @param jsonContent    POJO that can be serialized into JSON content or {@code null} for none
   * @param responseClass  response class to parse into
   */
  protected Request(AbstractClient abstractClient, String requestMethod,
                    String uriTemplate, Object jsonContent, Class<T> responseClass) {
    this(abstractClient, requestMethod, uriTemplate,
         jsonContent == null ? null
                             : new JsonHttpContent(abstractClient.getJsonFactory(), jsonContent),
         responseClass);
  }

  /**
   * @param abstractClient client
   * @param requestMethod  HTTP Method
   * @param uriTemplate    URI template for the path relative to the base URL. If it starts with a
   *                       "/" the base path from the base URL will be stripped out. The URI
   *                       template can also be a full URL. URI template expansion is done using
   *                       {@link UriTemplate#expand(String, String, Object, boolean)}
   * @param httpContent    HttpContent
   * @param responseClass  response class to parse into
   */
  protected Request(AbstractClient abstractClient, String requestMethod,
                    String uriTemplate, HttpContent httpContent, Class<T> responseClass) {
    this.responseClass = checkNotNull(responseClass);
    this.abstractClient = checkNotNull(abstractClient);
    this.requestMethod = checkNotNull(requestMethod);
    this.uriTemplate = checkNotNull(uriTemplate);
    this.httpContent = httpContent;
  }


  /** Returns whether to disable GZip compression of HTTP content. */
  public final boolean getDisableGZipContent() {
    return disableGZipContent;
  }

  /**
   * Sets whether to disable GZip compression of HTTP content.
   *
   * <p> By default it is {@code false}. </p>
   *
   * <p> Overriding is only supported for the purpose of calling the super implementation and
   * changing the return type, but nothing else. </p>
   */
  public Request<T> setDisableGZipContent(boolean disableGZipContent) {
    this.disableGZipContent = disableGZipContent;
    return this;
  }

  /** Returns the HTTP method. */
  public final String getRequestMethod() {
    return requestMethod;
  }

  /** Returns the URI template for the path relative to the base URL. */
  public final String getUriTemplate() {
    return uriTemplate;
  }

  /** Returns the HTTP content or {@code null} for none. */
  public final HttpContent getHttpContent() {
    return httpContent;
  }

  /**
   * Returns the Google client.
   *
   * <p> Overriding is only supported for the purpose of calling the super implementation and
   * changing the return type, but nothing else. </p>
   */
  public AbstractClient getAbstractClient() {
    return abstractClient;
  }

  /** Returns the HTTP headers used for the Google client request. */
  public final HttpHeaders getRequestHeaders() {
    return requestHeaders;
  }

  /**
   * Sets the HTTP headers used for the Google client request.
   *
   * <p> These headers are set on the request after {@link #buildHttpRequest} is called, this means
   * that {@link com.google.api.client.http.HttpRequestInitializer#initialize} is called first.
   * </p>
   *
   * <p> Overriding is only supported for the purpose of calling the super implementation and
   * changing the return type, but nothing else. </p>
   */
  public Request<T> setRequestHeaders(HttpHeaders headers) {
    this.requestHeaders = headers;
    return this;
  }

  /**
   * Returns the HTTP headers of the last response or {@code null} before request has been executed
   */
  public final HttpHeaders getLastResponseHeaders() {
    return lastResponseHeaders;
  }

  /**
   * Returns the status code of the last response or {@code -1} before request has been executed.
   */
  public final int getLastStatusCode() {
    return lastStatusCode;
  }

  /**
   * Returns the status message of the last response or {@code null} before request has been
   * executed.
   */
  public final String getLastStatusMessage() {
    return lastStatusMessage;
  }

  /**  Returns the response class to parse into. */
  public final Class<T> getResponseClass() {
    return responseClass;
  }

  /**
   * Creates a new instance of {@link GenericUrl} suitable for use against this service.
   *
   * <p> Subclasses may override by calling the super implementation. </p>
   *
   * @return newly created {@link GenericUrl}
   */
  public GenericUrl buildHttpRequestUrl() {
    return new GenericUrl(
        UriTemplate.expand(abstractClient.getBaseUrl(), uriTemplate, this, true));
  }

  /**
   * Create a request suitable for use against this service.
   *
   * <p> Subclasses may override by calling the super implementation. </p>
   */
  public HttpRequest buildHttpRequest() throws IOException {
    return buildHttpRequest(false);
  }

  /**
   * Create a request suitable for use against this service, but using HEAD instead of GET.
   *
   * <p> Only supported when the original request method is GET. </p>
   *
   * <p> Subclasses may override by calling the super implementation. </p>
   */
  protected HttpRequest buildHttpRequestUsingHead() throws IOException {
    return buildHttpRequest(true);
  }

  /**  Create a request suitable for use against this service. */
  private HttpRequest buildHttpRequest(boolean usingHead) throws IOException {
    checkArgument(!usingHead || requestMethod.equals(HttpMethods.GET));
    String requestMethodToUse = usingHead ? HttpMethods.HEAD : requestMethod;
    final GenericUrl requestUrl = buildHttpRequestUrl();
    //Reduce the risk of caching
    if (requestMethod.equals(HttpMethods.GET)) {
      requestUrl.set("ck", (new Date()).getTime());
    }
    final HttpRequest httpRequest = getAbstractClient()
        .getRequestFactory().buildRequest(requestMethodToUse, requestUrl, httpContent);
    httpRequest.setParser(getAbstractClient().getObjectParser());
    // custom methods may use POST with no content but require a Content-Length header
    if (httpContent == null && (requestMethod.equals(HttpMethods.POST)
                                || requestMethod.equals(HttpMethods.PUT) || requestMethod
        .equals(HttpMethods.PATCH))) {
      httpRequest.setContent(new EmptyContent());
    }

    httpRequest.getHeaders().putAll(requestHeaders);
    if (!disableGZipContent) {
      httpRequest.setEncoding(new GZipEncoding());
    }
    final HttpResponseInterceptor responseInterceptor = httpRequest.getResponseInterceptor();
    httpRequest.setResponseInterceptor(new HttpResponseInterceptor() {

      public void interceptResponse(HttpResponse response) throws IOException {
        if (responseInterceptor != null) {
          responseInterceptor.interceptResponse(response);
        }
        if (!response.isSuccessStatusCode() && httpRequest.getThrowExceptionOnExecuteError()) {
          throw newExceptionOnError(response);
        }
      }
    });
    return httpRequest;
  }

  /**
   * Sends the metadata request to the server and returns the raw metadata {@link HttpResponse}.
   *
   * <p> Callers are responsible for disconnecting the HTTP response by calling {@link
   * HttpResponse#disconnect}. Example usage: </p>
   *
   * <pre>
   * HttpResponse response = request.executeUnparsed();
   * try {
   * // process response..
   * } finally {
   * response.disconnect();
   * }
   * </pre>
   *
   * <p> Subclasses may override by calling the super implementation. </p>
   *
   * @return the {@link HttpResponse}
   */
  public HttpResponse executeUnparsed() throws IOException {
    return executeUnparsed(false);
  }

  /**
   * Sends the metadata request using HEAD to the server and returns the raw metadata {@link
   * HttpResponse} for the response headers.
   *
   * <p> Only supported when the original request method is GET. The response content is assumed to
   * be empty and ignored. Calls {@link HttpResponse#ignore()} so there is no need to disconnect
   * the
   * response. Example usage: </p>
   *
   * <pre>
   * HttpResponse response = request.executeUsingHead();
   * // look at response.getHeaders()
   * </pre>
   *
   * <p> Subclasses may override by calling the super implementation. </p>
   *
   * @return the {@link HttpResponse}
   */
  protected HttpResponse executeUsingHead() throws IOException {
    HttpResponse response = executeUnparsed(true);
    response.ignore();
    return response;
  }

  /**
   * Sends the metadata request using the given request method to the server and returns the raw
   * metadata {@link HttpResponse}.
   */
  private HttpResponse executeUnparsed(boolean usingHead) throws IOException {
    HttpResponse response;

    response = buildHttpRequest(usingHead).execute();
    // process response
    lastResponseHeaders = response.getHeaders();
    lastStatusCode = response.getStatusCode();
    lastStatusMessage = response.getStatusMessage();
    return response;
  }

  /**
   * Returns the exception to throw on an HTTP error response as defined by {@link
   * HttpResponse#isSuccessStatusCode()}.
   *
   * <p> It is guaranteed that {@link HttpResponse#isSuccessStatusCode()} is {@code false}. Default
   * implementation is to call {@link HttpResponseException#HttpResponseException(HttpResponse)},
   * but subclasses may override. </p>
   *
   * @param response HTTP response
   * @return exception to throw
   */
  protected IOException newExceptionOnError(HttpResponse response) {
    return new HttpResponseException(response);
  }

  /**
   * Sends the metadata request to the server and returns the parsed metadata response.
   *
   * <p> Subclasses may override by calling the super implementation. </p>
   *
   * @return parsed HTTP response
   */
  public T execute() throws IOException {
    return executeUnparsed().parseAs(responseClass);
  }

  /**
   * Sends the metadata request to the server and returns the metadata content input stream of
   * {@link HttpResponse}.
   *
   * <p> Callers are responsible for closing the input stream after it is processed. Example
   * sample:
   * </p>
   *
   * <pre>
   * InputStream is = request.executeAsInputStream();
   * try {
   * // Process input stream..
   * } finally {
   * is.close();
   * }
   * </pre>
   *
   * <p> Subclasses may override by calling the super implementation. </p>
   *
   * @return input stream of the response content
   */
  public InputStream executeAsInputStream() throws IOException {
    return executeUnparsed().getContent();
  }

  /**
   * Sends the metadata request to the server and writes the metadata content input stream of
   * {@link
   * HttpResponse} into the given destination output stream.
   *
   * <p> This method closes the content of the HTTP response from {@link
   * HttpResponse#getContent()}.
   * </p>
   *
   * <p> Subclasses may override by calling the super implementation. </p>
   *
   * @param outputStream destination output stream
   */
  public void executeAndDownloadTo(OutputStream outputStream) throws IOException {
    executeUnparsed().download(outputStream);
  }

  // @SuppressWarnings was added here because this is generic class.
  // see: http://stackoverflow.com/questions/4169806/java-casting-object-to-a-generic-type and
  // http://www.angelikalanger.com/GenericsFAQ/FAQSections/TechnicalDetails.html#Type%20Erasure
  // for more details
  @SuppressWarnings("unchecked")
  @Override
  public Request<T> set(String fieldName, Object value) {
    return (Request<T>) super.set(fieldName, value);
  }

  /**
   * Ensures that the specified required parameter is not null or {@link
   * AbstractClient#getSuppressRequiredParameterChecks()} is true.
   *
   * @param value the value of the required parameter
   * @param name  the name of the required parameter
   * @throws IllegalArgumentException if the specified required parameter is null and {@link
   *                                  AbstractClient#getSuppressRequiredParameterChecks()} is false
   */
  protected final void checkRequiredParameter(Object value, String name) {
    checkArgument(
        abstractClient.getSuppressRequiredParameterChecks() || value != null,
        "Required parameter %s must be specified", name);
  }
}
