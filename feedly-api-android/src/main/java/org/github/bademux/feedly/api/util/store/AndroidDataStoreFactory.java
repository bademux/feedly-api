/*
 * Copyright 2013 Bademus.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
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
 *   Contributors:
 *                Bademus
 */

package org.github.bademux.feedly.api.util.store;

import com.google.api.client.util.IOUtils;
import com.google.api.client.util.Lists;
import com.google.api.client.util.Preconditions;
import com.google.api.client.util.store.AbstractDataStore;
import com.google.api.client.util.store.AbstractDataStoreFactory;
import com.google.api.client.util.store.DataStore;
import com.google.api.client.util.store.DataStoreUtils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static android.content.Context.MODE_PRIVATE;

public class AndroidDataStoreFactory extends AbstractDataStoreFactory {

  @Override
  protected <V extends Serializable> DataStore<V> createDataStore(String id) {
    return new AndroidDataStore<V>(this, context.getSharedPreferences(id, MODE_PRIVATE), id);
  }

  static class AndroidDataStore<V extends Serializable> extends AbstractDataStore<V> {

    public AndroidDataStore(final AndroidDataStoreFactory dataStore,
                            final SharedPreferences preferences, final String id) {
      super(dataStore, id);
      this.preferences = preferences;
    }

    @Override
    public Set<String> keySet() {
      lock.lock();
      try {
        return preferences.getAll().keySet();
      } finally {
        lock.unlock();
      }
    }

    @Override
    public Collection<V> values() throws IOException {
      lock.lock();
      try {
        List<V> result = Lists.newArrayList();
        for (Object serialized : preferences.getAll().values()) {
          result.add(IOUtils.<V>deserialize(((String) serialized).getBytes("UTF8")));
        }
        return Collections.unmodifiableList(result);
      } finally {
        lock.unlock();
      }
    }

    @Override
    public V get(final String key) throws IOException {
      if (key == null) {
        return null;
      }
      lock.lock();
      try {
        String serialized = preferences.getString(key, null);
        if (serialized == null) {
          return null;
        }
        return IOUtils.deserialize(Base64.decode(serialized, Base64.DEFAULT));
      } finally {
        lock.unlock();
      }
    }

    @Override
    public DataStore<V> set(final String key, final V value) throws IOException {
      Preconditions.checkNotNull(key);
      Preconditions.checkNotNull(value);
      lock.lock();
      try {
        String serialized = Base64.encodeToString(IOUtils.serialize(value), Base64.DEFAULT);
        preferences.edit().putString(key, serialized).commit();
      } finally {
        lock.unlock();
      }
      return this;
    }

    @Override
    public DataStore<V> clear() {
      lock.lock();
      try {
        preferences.edit().clear().commit();
      } finally {
        lock.unlock();
      }
      return this;
    }

    @Override
    public DataStore<V> delete(final String key) {
      if (key == null) {
        return this;
      }
      lock.lock();
      try {
        preferences.edit().remove(key).commit();
      } finally {
        lock.unlock();
      }
      return this;
    }

    @Override
    public AndroidDataStoreFactory getDataStoreFactory() {
      return (AndroidDataStoreFactory) super.getDataStoreFactory();
    }

    @Override
    public String toString() {
      return DataStoreUtils.toString(this);
    }

    /** Lock on access to the store. */
    private final Lock lock = new ReentrantLock();

    private final SharedPreferences preferences;
  }

  public AndroidDataStoreFactory(Context context) { this.context = context; }

  private final Context context;
}
