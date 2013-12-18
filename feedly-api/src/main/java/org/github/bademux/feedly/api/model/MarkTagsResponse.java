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

import com.google.api.client.util.GenericData;
import com.google.api.client.util.Key;

import java.util.List;
import java.util.Map;


public class MarkTagsResponse extends GenericData {

  @Key
  private Map<String, List<String>> taggedEntries;

  public Map<String, List<String>> taggedEntries() { return taggedEntries; }

  public String getTagIdBy(String entryId) {
    for (Entry<String, List<String>> item : taggedEntries.entrySet()) {
      List<String> entryIds = item.getValue();
      if (entryIds != null && entryIds.contains(entryId)) {
        return item.getKey();
      }
    }
    return null;
  }

  @Override
  public MarkTagsResponse set(String fieldName, Object value) {
    return (MarkTagsResponse) super.set(fieldName, value);
  }

  @Override
  public MarkTagsResponse clone() {
    return (MarkTagsResponse) super.clone();
  }
}


