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

import java.util.ArrayList;
import java.util.List;


public class FeedInfo extends Feed {

  public enum State {
    @Value("alive")alive, @Value("dormant")DORMANT, @Value("dead")DEAD, @NullValue UNKNOWN
  }

  @Key
  private List<String> keywords;
  @Key
  private Double velocity;
  @Key
  private Boolean featured;
  @Key
  private Boolean sponsored;
  @Key
  private Boolean curated;
  @Key
  private Integer subscribers;
  @Key
  private State state;

  public List<String> getKeywords() {
    return keywords;
  }

  public Double getVelocity() {
    return velocity;
  }

  public Boolean getFeatured() {
    return featured;
  }

  public Boolean getSponsored() {
    return sponsored;
  }

  public Boolean getCurated() {
    return curated;
  }

  public Integer getSubscribers() {
    return subscribers;
  }

  public State getState() {
    return state;
  }

  @Override
  public FeedInfo set(String fieldName, Object value) { return (FeedInfo) super.set(fieldName, value); }

  @Override
  public FeedInfo clone() { return (FeedInfo) super.clone(); }

  @SuppressWarnings("serial")
  public static class FeedsInfo extends ArrayList<FeedInfo> {}
}
