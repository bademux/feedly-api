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


public class Tag extends IdGenericJson implements Stream {

  public static final String PREFIX = "tag";

  /** List of entries the user has recently read - limited to the feeds the users subscribes to */
  public static final String READ = "global.read";
  /** Users can save articles for later. Equivalent of starring articles in Google Reader. */
  public static final String SAVED = "global.saved";

  @Key
  private String label;

  /**
   * @hide
   * Use org.github.bademux.feedly.api.service.Feedly#newTag(java.lang.String)
   * @param name
   * @param userId
   */
  public Tag(String name, String userId) {
    super(PREFIX, name, userId);
    this.label = name;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(final String label) {
    this.label = label;
  }

  @Override
  public Tag set(String fieldName, Object value) { return (Tag) super.set(fieldName, value); }

  @Override
  public Tag clone() { return (Tag) super.clone(); }

  public Tag() {super(PREFIX);}

  @SuppressWarnings("serial")
  public static class Tags extends ArrayList<Tag> {}
}


