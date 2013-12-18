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

import static com.google.api.client.util.Preconditions.checkNotNull;
import static com.google.api.client.util.Strings.isNullOrEmpty;


public abstract class IdJsonEntity extends GenericJson {

  private final String prefix;

  @Key
  private String id;

  /**
   * Support current user format 'user/-/...'
   * see https://groups.google.com/forum/?fromgroups=#!topic/feedly-cloud/R0GC4IMgejI
   */
  public IdJsonEntity(String prefix, String name, String userId) {
    this(prefix, name);
    StringBuilder sb = new StringBuilder("user/");
    if (isNullOrEmpty(userId)) {
      sb.append('-');
    } else {
      sb.append(userId);
    }
    id = sb.append('/').append(id).toString();
  }

  protected IdJsonEntity(String prefix, String name) {
    this(prefix);
    id = new StringBuilder(prefix).append('/').append(checkNotNull(name)).toString();
  }

  protected IdJsonEntity(String prefix) { this.prefix = checkNotNull(prefix); }

  public String getId() { return id; }

  public String getName() { return id.substring(id.indexOf(prefix + '/') + prefix.length() + 1); }
}


