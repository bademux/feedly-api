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


public class MarkReadsResponse extends GenericData {

  @Key
  private List<String> entries;
  @Key
  private List<Item> feeds;

  public static class Item {

    @Key
    private String id;
    @Key
    private Long asOf;

    public String getId() { return id; }

    public Long getAsOf() { return asOf; }
  }

  public List<Item> feeds() { return feeds; }

  public Long feedAsOf(String feedId) {
    for (int i = 0; i < feeds.size(); i++) {
      if (feedId.equals(feeds.get(i).id)) { return feeds.get(i).asOf; }
    }
    return null;
  }

  public List<String> entries() { return entries; }

  @Override
  public MarkReadsResponse set(String fieldName, Object value) {
    return (MarkReadsResponse) super.set(fieldName, value);
  }

  @Override
  public MarkReadsResponse clone() {
    return (MarkReadsResponse) super.clone();
  }
}


