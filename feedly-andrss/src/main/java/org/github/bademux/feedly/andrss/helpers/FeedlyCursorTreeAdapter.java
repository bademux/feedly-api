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
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.widget.SimpleCursorTreeAdapter;

import static org.github.bademux.feedly.api.provider.FeedlyContract.Categories;
import static org.github.bademux.feedly.api.provider.FeedlyContract.Feeds;
import static org.github.bademux.feedly.api.provider.FeedlyContract.FeedsByCategory;

public class FeedlyCursorTreeAdapter extends SimpleCursorTreeAdapter {

  public FeedlyCursorTreeAdapter(Context context) {
    //The constructor does not take a Cursor - avoiding querying the db on the main thread.
    super(context, null,
          android.R.layout.simple_expandable_list_item_1, group, new int[]{android.R.id.text1},
          android.R.layout.simple_expandable_list_item_1, child, new int[]{android.R.id.text1});

    mQueryHandler = createAsyncQueryHandler(context.getContentResolver());
  }

  public void startQueryGroup() {
    mQueryHandler.startQuery(TOKEN_GROUP, null, Categories.CONTENT_URI, group, null, null, null);
  }

  @Override
  protected Cursor getChildrenCursor(final Cursor groupCursor) {
    String id = groupCursor.getString(groupCursor.getColumnIndex(Categories.ID));
    Uri.Builder builder = FeedsByCategory.CONTENT_URI.buildUpon().appendPath(id);
    mQueryHandler.startQuery(TOKEN_CHILD, groupCursor.getPosition(), builder.build(),
                             child, null, null, null);
    return null;
  }


  private AsyncQueryHandler createAsyncQueryHandler(ContentResolver contentResolver) {
    return new AsyncQueryHandler(contentResolver) {
      @Override
      protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
        switch (token) {
          case TOKEN_GROUP:
            setGroupCursor(cursor);
            break;
          case TOKEN_CHILD:
            setChildrenCursor((Integer) cookie, cursor);
            break;
          default:
            if (cursor != null) {
              cursor.close();
            }
        }
      }
    };
  }

  private final AsyncQueryHandler mQueryHandler;

  private static final String[] group = new String[]{Categories.LABEL, Categories.ID};

  private static final String[] child = new String[]{Feeds.TITLE};

  private static final int TOKEN_GROUP = 0, TOKEN_CHILD = 1;
}