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
import android.widget.SimpleCursorTreeAdapter;

import org.github.bademux.feedly.api.util.db.BackgroundQueryHandler;

import static org.github.bademux.feedly.api.util.db.BackgroundQueryHandler.AsyncQueryListener;
import static org.github.bademux.feedly.api.provider.FeedlyContract.Categories;
import static org.github.bademux.feedly.api.provider.FeedlyContract.Feeds;
import static org.github.bademux.feedly.api.provider.FeedlyContract.FeedsByCategory;

public class FeedlyCursorTreeAdapter extends SimpleCursorTreeAdapter {

  public FeedlyCursorTreeAdapter(Context context, final BackgroundQueryHandler queryHandler) {
    //The constructor does not take a Cursor - avoiding querying the db on the main thread.
    super(context, null,
          android.R.layout.simple_expandable_list_item_1, group, new int[]{android.R.id.text1},
          android.R.layout.simple_expandable_list_item_1, child, new int[]{android.R.id.text1});

    mQueryHandler = queryHandler;

    tokenGroup = mQueryHandler.addQueryListener(new AsyncQueryListener() {
      @Override
      public void onQueryComplete(final Object cookie, final Cursor cursor) {
        setGroupCursor(cursor);
      }
    });
    tokenChild = mQueryHandler.addQueryListener(new AsyncQueryListener() {
      @Override
      public void onQueryComplete(final Object cookie, final Cursor cursor) {
        setChildrenCursor((Integer) cookie, cursor);
      }
    });
  }

  public void startQueryGroup() {
    mQueryHandler.startQuery(tokenGroup, null, Categories.CONTENT_URI, group, null, null, null);
  }

  @Override
  protected Cursor getChildrenCursor(final Cursor groupCursor) {
    String id = groupCursor.getString(groupCursor.getColumnIndex(Categories.ID));
    Uri.Builder builder = FeedsByCategory.CONTENT_URI.buildUpon().appendPath(id);
    mQueryHandler.startQuery(tokenChild, groupCursor.getPosition(), builder.build(),
                             child, null, null, null);
    return null;
  }

  private final BackgroundQueryHandler mQueryHandler;

  private final int tokenGroup, tokenChild;

  private static final String[] group = new String[]{Categories.LABEL, Categories.ID};

  private static final String[] child = new String[]{Feeds.TITLE};
}