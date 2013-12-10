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

package org.github.bademux.feedly.api.model;

import com.google.api.client.json.GenericJson;

public final class Profile extends GenericJson {

  @com.google.api.client.util.Key
  private String id;
  @com.google.api.client.util.Key
  private String client;
  @com.google.api.client.util.Key
  private String email;
  @com.google.api.client.util.Key
  private String givenName;
  @com.google.api.client.util.Key
  private String familyName;
  @com.google.api.client.util.Key
  private String gender;
  @com.google.api.client.util.Key
  private String locale;
  @com.google.api.client.util.Key
  private String picture;
  @com.google.api.client.util.Key
  private Long created;
  @com.google.api.client.util.Key
  private String wave;

  @com.google.api.client.util.Key
  private Boolean wordPressConnected;
  @com.google.api.client.util.Key
  private Long wordPressId;
  @com.google.api.client.util.Key
  private Long wordPressPrimaryBlogId;
  @com.google.api.client.util.Key
  private String wordPressAccessToken;

  @com.google.api.client.util.Key
  private Boolean evernoteConnected;
  @com.google.api.client.util.Key
  private Boolean pocketConnected;

  public String getUserId() {
    return id;
  }

  public String getClient() {
    return client;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(final String email) {
    this.email = email;
  }

  public String getGivenName() { return givenName; }

  public void setGivenName(final String givenName) { this.givenName = givenName; }

  public String getFamilyName() { return familyName; }

  public void setFamilyName(final String familyName) { this.familyName = familyName; }

  public String getGender() { return gender; }

  public void setGender(final String gender) { this.gender = gender; }

  public String getLocale() { return locale; }

  public void setLocale(final String locale) { this.locale = locale; }

  public String getPicture() { return picture; }

  public void setPicture(final String picture) { this.picture = picture; }

  public Long getCreated() {
    return created;
  }

  public String getWave() {
    return wave;
  }

  public Boolean getWordPressConnected() {
    return wordPressConnected;
  }

  public Long getWordPressId() {
    return wordPressId;
  }

  public Long getWordPressPrimaryBlogId() {
    return wordPressPrimaryBlogId;
  }

  public String getWordPressAccessToken() { return wordPressAccessToken; }

  public Boolean getEvernoteConnected() {
    return evernoteConnected;
  }

  public Boolean getPocketConnected() {
    return pocketConnected;
  }

  @Override
  public Profile set(String fieldName, Object value) {
    return (Profile) super.set(fieldName, value);
  }

  @Override
  public Profile clone() {
    return (Profile) super.clone();
  }
}
