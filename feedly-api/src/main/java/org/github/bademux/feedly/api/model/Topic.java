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

import static com.google.api.client.repackaged.com.google.common.base.Preconditions.checkNotNull;


public class Topic extends IdJsonEntity implements Stream {

  public static final String PREFIX = "topic";

  public enum Interest {
    @Value("high")HIGH, @Value("medium")MEDIUM, @Value("low")LOW, @NullValue UNKNOWN
  }

  @Key
  private Interest interest;
  @Key
  private Long updated;
  @Key
  private Long created;

  /**
   * Use {org.github.bademux.feedly.api.service.Feedly#newTopic(java.lang.String, org.github.bademux.feedly.api.model.Topic.Interest)}
   * @param name
   * @param interest
   * @param userId
   */
  public Topic(String name, Interest interest, String userId) {
    super(PREFIX, name, userId);
    this.interest = checkNotNull(interest);
  }

  /**
   * Creates new 'Global' topic, used with 'mixes' API
   * @param name
   * @param interest
   */
  public Topic(String name, Interest interest) {
    super(PREFIX, name);
    this.interest = checkNotNull(interest);
  }

  public Interest getInterest() {
    return interest;
  }

  public void setInterest(final Interest interest) {
    this.interest = interest;
  }

  public Long getUpdated() {
    return updated;
  }

  public Long getCreated() {
    return created;
  }

  public Topic() {super(PREFIX);}


  @Override
  public Topic set(String fieldName, Object value) { return (Topic) super.set(fieldName, value); }

  @Override
  public Topic clone() { return (Topic) super.clone(); }

  @SuppressWarnings("serial")
  public static class Topics extends ArrayList<Topic> {}
}


