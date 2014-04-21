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
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import org.github.bademux.feedly.andrss.R;
import org.github.bademux.feedly.api.util.db.BackgroundQueryHandler;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.util.Date;

import javax.annotation.Nullable;

import static org.github.bademux.feedly.api.provider.FeedlyContract.Entries;
import static org.github.bademux.feedly.api.provider.FeedlyContract.EntriesByCategory;
import static org.github.bademux.feedly.api.provider.FeedlyContract.Files;
import static org.github.bademux.feedly.api.util.db.BackgroundQueryHandler.AsyncQueryListener;

public class FeedlyContentAdapter extends SimpleCursorAdapter implements AsyncQueryListener {

  public void startQueryOnCategory(String uri) {
    Uri.Builder builder = EntriesByCategory.CONTENT_URI.buildUpon().appendPath(uri);
    mQueryHandler.startQuery(token, null, builder.build(), FROM, null, null, null);
  }

  public void startQueryOnFeed(String uri) {
    mQueryHandler.startQuery(token, null, Entries.CONTENT_URI, FROM,
                             Entries.ORIGIN_STREAMID + "=?", new String[]{uri}, null);
  }

  @Override
  public void onQueryComplete(final int token, final Object cookie, final Cursor cursor) {
    changeCursor(cursor);
  }

  protected void handleCrawled(final TextView view, final Long timestamp) {
    if (timestamp == null) {
      return;
    }
    synchronized (mDateFormat) {
      view.setText(mDateFormat.format(new Date(timestamp)));
    }
  }

  protected void handleSummary(final TextView view, final String text) {
    if (text == null) {
      return;
    }
    view.setText(Html.fromHtml(text, imageGetter, null));
  }

  protected void handleVisual(final FrameLayout view, final String source) {
    Drawable drawable = imageGetter.getDrawable(source);
    if (drawable != null) {
      view.setBackground(drawable);
    }
  }

  public FeedlyContentAdapter(final Context context, final BackgroundQueryHandler queryHandler) {
    //The constructor does not take a Cursor - avoiding querying the db on the main thread.
    super(context, R.layout.fragment_content_list_item_simple, null, FROM, TO, 0);
    mDateFormat = android.text.format.DateFormat.getDateFormat(context);
    setViewBinder(viewBinder);
    mQueryHandler = queryHandler;
    mContext = context;
    token = mQueryHandler.addQueryListener(this);
  }

  private final BackgroundQueryHandler mQueryHandler;

  private final DateFormat mDateFormat;

  private final Context mContext;

  private int token;

  private final ViewBinder viewBinder = new ViewBinder() {
    @Override
    public boolean setViewValue(final View view, final Cursor cursor, final int columnIndex) {
      switch (view.getId()) {
        case R.id.content_list_visual:
          handleVisual((FrameLayout) view, cursor.getString(columnIndex)); return true;
        case R.id.content_list_summary:
          handleSummary((TextView) view, cursor.getString(columnIndex)); return true;
        case R.id.content_list_meta_crawled:
          handleCrawled((TextView) view, cursor.getLong(columnIndex)); return true;
        default:
      }
      return false;
    }
  };

  private final Html.ImageGetter imageGetter = new Html.ImageGetter() {
    @Override
    public synchronized Drawable getDrawable(@Nullable final String source) {
      if (source == null) {
        return null;
      }

      Uri uri = Files.CONTENT_URI.buildUpon().appendPath(source).build();
      InputStream stream = null;
      try {
        stream = mContext.getContentResolver().openInputStream(uri);
        return Drawable.createFromStream(stream, null);
      } catch (FileNotFoundException e) {
        Log.e(TAG, "content_list_visual is missing", e);
      } finally {
        if (stream != null) {
          try {
            stream.close();
          } catch (IOException e) {}
        }
      }

      return null;
    }
  };

  private static final String[] FROM = new String[]{Entries.TITLE,
                                                    Entries.VISUAL_URL,
                                                    Entries.SUMMARY,
                                                    Entries.ENGAGEMENT,
                                                    Entries.ORIGIN_TITLE,
                                                    Entries.AUTHOR,
                                                    Entries.CRAWLED,
                                                    Entries.ID};

  private static final int[] TO = new int[]{R.id.content_list_title,
                                            R.id.content_list_visual,
                                            R.id.content_list_summary,
                                            R.id.content_list_meta_read,
                                            R.id.content_list_meta_src_title,
                                            R.id.content_list_meta_author,
                                            R.id.content_list_meta_crawled};


  static final String TAG = "FeedlyContentAdapter";
}