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

package org.github.bademux.feedly.api.util.db;

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.database.Cursor;

import java.lang.ref.WeakReference;

public final class QueryHandler extends AsyncQueryHandler {

  /**
   * Assign the given {@link AsyncQueryListener} to receive query events from
   * asynchronous calls. Will replace any existing listener.
   */
  public void setQueryListener(AsyncQueryListener listener) {
    mListener = new WeakReference<AsyncQueryListener>(listener);
  }

  /** {@inheritDoc} */
  @Override
  protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
    final AsyncQueryListener listener = mListener.get();
    if (listener != null) {
      listener.onQueryComplete(token, cookie, cursor);
    } else if (cursor != null) {
      cursor.close();
    }
  }

  private WeakReference<AsyncQueryListener> mListener;

  public QueryHandler(ContentResolver contentResolver, AsyncQueryListener listener) {
    this(contentResolver);
    setQueryListener(listener);
  }

  public QueryHandler(ContentResolver contentResolver) { super(contentResolver); }

  public interface AsyncQueryListener {
    /**
     * Called when an asynchronous query is completed.
     *
     * @param token the token to identify the query, passed in from
     *            {@link #startQuery}.
     * @param cookie the cookie object passed in from {@link #startQuery}.
     * @param cursor The cursor holding the results from the query.
     */
    void onQueryComplete(int token, Object cookie, Cursor cursor);
  }
}
