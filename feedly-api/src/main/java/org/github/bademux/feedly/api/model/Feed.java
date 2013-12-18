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

import com.google.api.client.json.GenericJson;
import com.google.api.client.util.Key;


public abstract class Feed extends GenericJson implements Markable, Stream {

  public static final String PREFIX = "feed";

  @Key
  protected String id;
  @Key
  protected String title;
  @Key
  private String website;

  public String getId() {
    return id;
  }

  public String getTitle() {
    return title;
  }

  public String getWebsite() {
    return website;
  }

  public String getUrl() { return id.substring(PREFIX.length() + 1); }

  @Override
  public Feed set(String fieldName, Object value) { return (Feed) super.set(fieldName, value); }

  @Override
  public Feed clone() { return (Feed) super.clone(); }
}
