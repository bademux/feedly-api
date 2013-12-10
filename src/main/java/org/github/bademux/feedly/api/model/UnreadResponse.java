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
import com.google.api.client.util.Key;

import java.util.List;


public class UnreadResponse extends GenericJson {

  @Key
  private List<Item> unreadcounts;

  public List<Item> unreadCounts() { return unreadcounts; }

  public static class Item {

    @Key
    private String id;
    @Key
    private Integer count;
    @Key
    private Long updated;

    public String getId() { return id; }

    public Integer getCount() { return count; }

    public Long getUpdated() { return updated; }

    public Class<? extends Markable> getType() {
      String prefix;
      if (id.startsWith(Subscription.PREFIX + '/')) {
        return Feed.class;
      } else if (id.lastIndexOf(Category.PREFIX + '/') != -1) {
        return Category.class;
      }
      return null;
    }
  }

  @Override
  public UnreadResponse set(String fieldName, Object value) {
    return (UnreadResponse) super.set(fieldName, value);
  }

  @Override
  public UnreadResponse clone() {
    return (UnreadResponse) super.clone();
  }
}


