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

/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *    Contributors:
 *                 Bademus
 */

package org.github.bademux.feedly.api.model;

import com.google.api.client.util.Key;

import java.util.ArrayList;


public class Category extends IdJsonEntity implements Markable, Stream {

  public static final String PREFIX = "category";

  public static final String UNCATEGORIZED = "global.uncategorized";
  public static final String ALL = "global.all";
  public static final String MUST_READS = "global.must";

  @Key
  private String label;

  /** Use org.github.bademux.feedly.api.service.Feedly#newCategory(java.lang.String) */
  public Category(String name, String userId) {
    super(PREFIX, name, userId);
    this.label = name;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(final String label) {
    this.label = label;
  }

  public Category() {super(PREFIX);}

  @Override
  public Category set(String fieldName, Object value) {
    return (Category) super.set(fieldName, value);
  }

  @Override
  public Category clone() { return (Category) super.clone(); }

  @SuppressWarnings("serial")
  public static class Categories extends ArrayList<Category> {}
}


