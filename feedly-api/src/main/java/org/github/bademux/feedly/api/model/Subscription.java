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

package org.github.bademux.feedly.api.model;

import com.google.api.client.util.Key;

import java.util.ArrayList;
import java.util.List;


public class Subscription extends Feed {

  @Key
  private String sortid;
  @Key
  private Long updated;
  @Key
  private List<Category> categories;
//  @Key
//  private List<String> topics;

  public Subscription(String url, String title) {
    this.id = new StringBuffer(PREFIX).append('/').append(url).toString();
    this.title = title;
  }

  public void setTitle(String title) { this.title = title; }

  public String getSortid() { return sortid; }

  public Long getUpdated() { return updated; }

  public List<Category> getCategories() { return categories; }

  public void setCategories(final List<Category> categories) { this.categories = categories; }

  public void addCategory(final Category category) {
    if (categories == null) {
      categories = new ArrayList<Category>();
    }
    this.categories.add(category);
  }

  /**
   * @return a ids of tags for this feed. feedly uses it to group feeds together and build a map of
   * topics the user is interested in.
   */
//  public List<String> getTopics() { return topics; }

  @Override
  public Subscription set(String fieldName, Object value) {
    return (Subscription) super.set(fieldName, value);
  }

  @Override
  public Subscription clone() {
    return (Subscription) super.clone();
  }

  public Subscription() {}

  @SuppressWarnings("serial")
  public static class Subscriptions extends ArrayList<Subscription> {}
}


