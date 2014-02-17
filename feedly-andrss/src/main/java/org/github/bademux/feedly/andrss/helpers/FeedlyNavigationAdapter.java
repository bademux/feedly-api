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
import android.view.ViewGroup;
import android.widget.SimpleCursorTreeAdapter;

import org.github.bademux.feedly.andrss.R;
import org.github.bademux.feedly.api.util.db.BackgroundQueryHandler;

import static org.github.bademux.feedly.api.provider.FeedlyContract.Categories;
import static org.github.bademux.feedly.api.provider.FeedlyContract.Feeds;
import static org.github.bademux.feedly.api.provider.FeedlyContract.FeedsByCategory;
import static org.github.bademux.feedly.api.util.db.BackgroundQueryHandler.AsyncQueryListener;
import static org.github.bademux.feedly.api.util.db.BackgroundQueryHandler.ContentChangeListener;

public class FeedlyNavigationAdapter extends SimpleCursorTreeAdapter implements AsyncQueryListener {

  public void startQueryGroup() {
    mQueryHandler.startQuery(tokenGroup, null, Categories.CONTENT_URI, GROUP, null, null, null);
  }

  @Override
  protected Cursor getChildrenCursor(final Cursor groupCursor) {
    String id = getCategoryId(groupCursor);
    Uri.Builder builder = FeedsByCategory.CONTENT_URI.buildUpon().appendPath(id);
    mQueryHandler.startQuery(tokenChild, groupCursor.getPosition(), builder.build(),
                             CHILD, null, null, null);
    return null;
  }

  @Override
  public void onQueryComplete(final int token, final Object cookie, final Cursor cursor) {
    if (token == tokenGroup) {
      setGroupCursor(cursor);
    } else if (token == tokenChild) {
      setChildrenCursor((Integer) cookie, cursor);
    }
  }

  @Override
  public View getGroupView(int groupPosition, boolean isExpanded,
                           View convertView, ViewGroup parentView) {

    return super.getGroupView(groupPosition, isExpanded, convertView, parentView);
  }

  public final String getCategoryId(final int groupPosition) {
    return getCategoryId(getGroup(groupPosition));
  }

  public final String getFeedId(final int groupPosition, final int childPosition) {
    return getFeedId(getChild(groupPosition, childPosition));
  }

  private static final String getCategoryId(Cursor cursor) {
    return cursor.getString(cursor.getColumnIndex(Categories.ID));
  }

  private static final String getFeedId(Cursor cursor) {
    return cursor.getString(cursor.getColumnIndex(Feeds.ID));
  }

  public FeedlyNavigationAdapter(Context context, final BackgroundQueryHandler queryHandler) {
    //The constructor does not take a Cursor - avoiding querying the db on the main thread.
    super(context, null,
          R.layout.fragment_navigation_list_group, GROUP, new int[]{android.R.id.text1},
          android.R.layout.simple_list_item_1, CHILD, new int[]{android.R.id.text1});

    mQueryHandler = queryHandler;

    mQueryHandler.addContentChangeListener(Categories.CONTENT_URI, new ContentChangeListener() {
      @Override
      public void onChange() { startQueryGroup(); }
    });

    tokenGroup = mQueryHandler.addQueryListener(this);
    tokenChild = mQueryHandler.addQueryListener(this);
  }


  private final BackgroundQueryHandler mQueryHandler;

  private final int tokenGroup, tokenChild;

  private static final String[] GROUP = new String[]{Categories.LABEL, Categories.ID};

  private static final String[] CHILD = new String[]{Feeds.TITLE, Feeds.ID};
}