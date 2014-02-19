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

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.View;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import org.github.bademux.feedly.andrss.R;
import org.github.bademux.feedly.api.util.db.BackgroundQueryHandler;

import java.text.DateFormat;
import java.util.Date;

import static org.github.bademux.feedly.api.provider.FeedlyContract.Entries;
import static org.github.bademux.feedly.api.provider.FeedlyContract.EntriesByCategory;
import static org.github.bademux.feedly.api.util.db.BackgroundQueryHandler.AsyncQueryListener;

public class FeedlyContentAdapter extends SimpleCursorAdapter implements AsyncQueryListener {

  public FeedlyContentAdapter(final Context context, final BackgroundQueryHandler queryHandler) {
    //The constructor does not take a Cursor - avoiding querying the db on the main thread.
    super(context, R.layout.fragment_content_list_item_simple, null, FROM, TO, 0);
    mDateFormat = android.text.format.DateFormat.getDateFormat(context);
    setViewBinder(createBinder());
    mQueryHandler = queryHandler;

    token = mQueryHandler.addQueryListener(this);
  }

  public void startQueryOnCategory(String uri) {
    Uri.Builder builder = EntriesByCategory.CONTENT_URI.buildUpon().appendPath(uri);
    mQueryHandler.startQuery(token, null, builder.build(), FROM, null, null, null);
  }

  public void startQueryOnFeed(String uri) {
    mQueryHandler.startQuery(token, null, Entries.CONTENT_URI, FROM,
                             Entries.ORIGIN_STREAMID + "=?", new String[]{uri}, null);
  }

  @Override
  public void onQueryComplete(final int token, final Object cookie,
                              final Cursor cursor) { changeCursor(cursor); }

  public ViewBinder createBinder() {
    return new ViewBinder() {
      @Override
      public boolean setViewValue(final View view, final Cursor cursor, final int columnIndex) {
        switch (view.getId()) {
          case R.id.content_list_thumbnail: return true;
          case R.id.content_list_meta_crawled:
            Long timestamp = cursor.getLong(columnIndex);
            if (timestamp != null) {
              synchronized (mDateFormat) {
                ((TextView) view).setText(mDateFormat.format(new Date(timestamp)));
              }
            }
            return true;
          default:
        }

        return false;
      }
    };
  }


  private final DateFormat mDateFormat;

  private BackgroundQueryHandler mQueryHandler;

  private int token;
  private static final String[] FROM = new String[]{Entries.TITLE,
                                                    "substr(" + Entries.SUMMARY + ",1,32)",
                                                    Entries.ENGAGEMENT,
                                                    Entries.AUTHOR,
                                                    Entries.CRAWLED,
                                                    Entries.ID};

  private static final int[] TO = new int[]{R.id.content_list_title,
                                            R.id.content_list_preview,
                                            R.id.content_list_meta_read,
                                            R.id.content_list_meta_author,
                                            R.id.content_list_meta_crawled};
}