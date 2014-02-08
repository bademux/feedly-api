/*
 * Copyright 2014 Bademus
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
 * Copyright 2014 Bademus
 *
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

package org.github.bademux.feedly.api.util.db;

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.util.SparseArray;

import java.lang.ref.WeakReference;

public final class BackgroundQueryHandler extends AsyncQueryHandler {

  /**
   * Assign the given {@link AsyncQueryListener} to receive query events from asynchronous calls.
   *
   * @return token that can be used in {@link AsyncQueryListener}#start* operations
   */
  public synchronized int addQueryListener(final AsyncQueryListener listener) {
    int token = mListeners.size() + 1;
    mListeners.put(token, listener);
    return token;
  }

  public synchronized void contentObserver(final Uri uri, final ContentChangeListener listener) {
    final ContentResolver contentResolver = mResolver.get();
    if (contentResolver != null) {
      contentResolver.registerContentObserver(uri, true, new ContentObserver(null) {
        @Override
        public void onChange(final boolean selfChange, Uri uri) { listener.onChange(); }
      });
    }
  }

  /** {@inheritDoc} */
  @Override
  protected void onQueryComplete(final int token, final Object cookie, final Cursor cursor) {
    final AsyncQueryListener listener = mListeners.get(token);
    if (listener != null) {
      listener.onQueryComplete(cookie, cursor);
    } else if (cursor != null) {
      cursor.close();
    }
  }

  public BackgroundQueryHandler(ContentResolver contentResolver) {
    super(contentResolver);
    mResolver = new WeakReference<>(contentResolver);
  }

  private final WeakReference<ContentResolver> mResolver;

  private final SparseArray<AsyncQueryListener> mListeners = new SparseArray<AsyncQueryListener>(5);

  public interface AsyncQueryListener {

    /**
     * Called when an asynchronous query is completed.
     *
     * @param cookie the cookie object passed in from {@link #startQuery}.
     * @param cursor The cursor holding the results from the query.
     */
    void onQueryComplete(final Object cookie, final Cursor cursor);
  }

  public interface ContentChangeListener {

    void onChange();
  }
}
