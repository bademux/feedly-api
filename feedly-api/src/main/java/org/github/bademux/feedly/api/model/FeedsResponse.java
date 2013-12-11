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

import java.util.List;

public class FeedsResponse extends GenericJson {

  @com.google.api.client.util.Key
  private List<FeedInfo> results;
  @com.google.api.client.util.Key
  private String hint;
  @com.google.api.client.util.Key
  private List<String> related;


  public List<FeedInfo> getResults() {
    return results;
  }

  public String getHint() {
    return hint;
  }

  public List<String> getRelated() {
    return related;
  }

  @Override
  public FeedsResponse set(String fieldName, Object value) {
    return (FeedsResponse) super.set(fieldName, value);
  }

  @Override
  public FeedsResponse clone() {
    return (FeedsResponse) super.clone();
  }
}
