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

public class Preferences extends GenericJson {

  @com.google.api.client.util.Key
  private Integer autoMarkAsReadOnSelect;

  public Integer getAutoMarkAsReadOnSelect() {
    return autoMarkAsReadOnSelect;
  }

  public void setAutoMarkAsReadOnSelect(final Integer autoMarkAsReadOnSelect) {
    this.autoMarkAsReadOnSelect = autoMarkAsReadOnSelect;
  }

  @Override
  public Preferences set(String fieldName, Object value) {
    return (Preferences) super.set(fieldName, value);
  }

  @Override
  public Preferences clone() { return (Preferences) super.clone(); }
}
