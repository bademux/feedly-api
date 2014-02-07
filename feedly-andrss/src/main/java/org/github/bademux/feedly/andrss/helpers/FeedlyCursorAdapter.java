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

package org.github.bademux.feedly.andrss.helpers;

import android.content.AsyncQueryHandler;
import android.content.Context;
import android.database.Cursor;
import android.widget.SimpleCursorAdapter;

import org.github.bademux.feedly.api.util.db.BackgroundQueryHandler;

import static org.github.bademux.feedly.api.util.db.BackgroundQueryHandler.AsyncQueryListener;
import static org.github.bademux.feedly.api.provider.FeedlyContract.Entries;

public class FeedlyCursorAdapter extends SimpleCursorAdapter implements AsyncQueryListener {

  public FeedlyCursorAdapter(final Context context, final BackgroundQueryHandler queryHandler) {
    //The constructor does not take a Cursor - avoiding querying the db on the main thread.
    super(context, android.R.layout.simple_list_item_1, null, from, new int[]{android.R.id.text1},
          SimpleCursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);

    mQueryHandler = queryHandler;
    token = queryHandler.addQueryListener(this);
  }

  public void startQuery() {
    mQueryHandler.startQuery(token, null, Entries.CONTENT_URI, from, null, null, null);
  }

  @Override
  public void onQueryComplete(final Object cookie, final Cursor cursor) { changeCursor(cursor); }

  private final AsyncQueryHandler mQueryHandler;

  private int token;

  private static final String[] from = new String[]{Entries.TITLE};
}