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
import com.google.api.client.util.NullValue;
import com.google.api.client.util.Value;


public abstract class Feed extends IdGenericJson implements Markable, Stream {

  public static final String PREFIX = "feed";


  public enum State {
    @Value("alive")alive, @Value("dormant")DORMANT, @Value("dead")DEAD,
    @Value("dead.flooded")DEAD_FLOODED, @NullValue UNKNOWN
  }

  @Key
  protected String title;
  @Key
  private String website;
  @Key
  private Double velocity;
  @Key
  private State state;


  protected Feed(final String url) { super(PREFIX, url); }

  public String getTitle() {
    return title;
  }

  public String getWebsite() {
    return website;
  }

  /**
   * @return The average number of articles published weekly. It's updated every few days.
   */
  public Double getVelocity() { return velocity; }

  public State getState() {
    return state;
  }

  public String getUrl() { return getId().substring(PREFIX.length() + 1); }

  protected Feed() { super(PREFIX); }
  
  @Override
  public Feed set(String fieldName, Object value) { return (Feed) super.set(fieldName, value); }

  @Override
  public Feed clone() { return (Feed) super.clone(); }
}
