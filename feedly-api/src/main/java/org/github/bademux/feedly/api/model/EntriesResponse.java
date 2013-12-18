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

import java.util.List;
import java.util.Map;

import static org.github.bademux.feedly.api.model.Entry.Content.Direction;
import static org.github.bademux.feedly.api.model.Entry.Location;

public final class EntriesResponse extends GenericJson implements Continuable {

  @Key
  private String id;
  @Key
  private Direction direction;
  @Key
  private String title;
  @Key
  private String continuation;
  @Key
  private List<Map<String, String>> self;
  @Key
  private List<Location> alternate;
  @Key
  private Long updated;
  @Key
  private List<org.github.bademux.feedly.api.model.Entry> items;

  public String getId() { return id; }

  public Direction getDirection() { return direction; }

  public String getTitle() { return title; }

  public String getContinuation() { return continuation; }

  public List<Map<String, String>> getSelf() { return self; }

  public List<Location> getAlternate() { return alternate; }

  public Long getUpdated() { return updated; }

  public List<org.github.bademux.feedly.api.model.Entry> items() { return items; }

  @Override
  public EntriesResponse set(String fieldName, Object value) {
    return (EntriesResponse) super.set(fieldName, value);
  }

  @Override
  public EntriesResponse clone() { return (EntriesResponse) super.clone(); }
}
