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
import android.os.Handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class BackgroundQueryHandler extends AsyncQueryHandler {

  /**
   * Assign the given {@link AsyncQueryListener} to receive query events from asynchronous calls.
   *
   * @return token that can be used in {@link AsyncQueryListener}#start* operations
   */
  public synchronized int addQueryListener(final AsyncQueryListener listener) {
    if(listener != null){
    mQueryListeners.add(listener);
    }
    return mQueryListeners.size() - 1;
  }

  /** {@inheritDoc} */
  @Override
  protected void onQueryComplete(final int token, final Object cookie, final Cursor cursor) {
    final AsyncQueryListener listener = mQueryListeners.get(token);
    if (listener != null) {
      listener.onQueryComplete(token, cookie, cursor);
    } else if (cursor != null) {
      cursor.close();
    }
  }

  public synchronized void addContentChangeListener(final Uri uri,
                                                    final ContentChangeListener listener) {
    if (uri == null || listener == null) {
      return;
    }

    ContentObserver observer = new ContentObserver(mHandler) {
      @Override
      public void onChange(final boolean selfChange) { listener.onChange(); }
    };

    mResolver.registerContentObserver(uri, true, observer);
    //add new ContentObserver and release any previously associated with uri
    ContentObserver oldContentObserver = mContentListeners.put(uri, observer);
    if (oldContentObserver != null) {
      mResolver.unregisterContentObserver(oldContentObserver);
    }
  }

  public synchronized void unregisterContentObserver(final Uri uri) {
    if (uri != null) {
      mResolver.unregisterContentObserver(mContentListeners.get(uri));
    }
  }

  public synchronized void unregisterAllContentObservers() {
    for (ContentObserver observer : mContentListeners.values()) {
      mResolver.unregisterContentObserver(observer);
    }
  }

  /**
   * @param handler - create new handler inUI thread to use it with
   *                android.widget.BaseAdapter#notifyDataSetChanged(). Can be null
   */
  public BackgroundQueryHandler(ContentResolver contentResolver, Handler handler) {
    super(contentResolver);
    mHandler = handler;
    mResolver = contentResolver;
  }

  public BackgroundQueryHandler(ContentResolver contentResolver) {
    this(contentResolver, null);
  }


  private final Handler mHandler;

  private final ContentResolver mResolver;

  private final Map<Uri, ContentObserver> mContentListeners = new HashMap<Uri, ContentObserver>(3);

  private final List<AsyncQueryListener> mQueryListeners = new ArrayList<AsyncQueryListener>(3);

  public interface AsyncQueryListener {

    /**
     * Called when an asynchronous query is completed.
     *
     * @param token
     * @param cookie the cookie object passed in from {@link #startQuery}.
     * @param cursor The cursor holding the results from the query.
     */
    void onQueryComplete(final int token, final Object cookie, final Cursor cursor);
  }

  public interface ContentChangeListener {

    void onChange();
  }
}
