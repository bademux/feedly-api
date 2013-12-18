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

public final class StreamsResponse extends GenericJson implements Continuable {

  @Key
  private List<String> ids;
  @Key
  private String continuation;

  public List<String> ids() { return ids; }

  public String getContinuation() { return continuation; }

  @Override
  public StreamsResponse set(String fieldName, Object value) {
    return (StreamsResponse) super.set(fieldName, value);
  }

  @Override
  public StreamsResponse clone() { return (StreamsResponse) super.clone(); }
}
