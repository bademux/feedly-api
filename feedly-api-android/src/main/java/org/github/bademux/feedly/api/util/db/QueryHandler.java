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
