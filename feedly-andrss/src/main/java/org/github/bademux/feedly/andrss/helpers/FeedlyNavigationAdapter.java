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
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.SimpleCursorTreeAdapter;
import android.widget.TextView;

import org.github.bademux.feedly.andrss.R;
import org.github.bademux.feedly.api.util.db.BackgroundQueryHandler;

import static android.view.View.OnClickListener;
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
  public View newGroupView(Context context, Cursor cursor, boolean isExpanded, ViewGroup parent) {
    final View view = super.newGroupView(context, cursor, isExpanded, parent);
    TextView textView = (TextView) view.findViewById(R.id.navigation_list_group_indicator);
    //set ExpandableListView for later use
    textView.setTag(parent);
    textView.setOnClickListener(ON_CLICK_LISTENER);
    return view;
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
          R.layout.fragment_navigation_list_group, GROUP,
          new int[]{R.id.navigation_list_group_title},
          R.layout.fragment_navigation_list_item, CHILD,
          new int[]{R.id.navigation_list_item_favicon, R.id.navigation_list_item_title});
    setViewBinder(viewBinder);
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

  private final ViewBinder viewBinder = new ViewBinder() {
    @Override
    public boolean setViewValue(final View view, final Cursor cursor, final int columnIndex) {
      switch (view.getId()) {
        case R.id.navigation_list_item_favicon:
          if (View.GONE == view.getVisibility()) {
            byte[] img = cursor.getBlob(columnIndex);
            ((ImageView) view).setImageBitmap(BitmapFactory.decodeByteArray(img, 0, img.length));
            view.setVisibility(View.VISIBLE);
          }
          return true;
        default:
      }
      return false;
    }
  };

  private static final String[] GROUP = new String[]{Categories.LABEL, Categories.ID};

  private static final String[] CHILD = new String[]{Feeds.FAVICON, Feeds.TITLE, Feeds.ID};

  private static final OnClickListener ON_CLICK_LISTENER = new OnClickListener() {
    @Override
    public void onClick(final View v) {
      final ExpandableListView listView = (ExpandableListView) v.getTag();
      final View listItem = (View) v.getParent();
      //calculate group position
      long pos = listView.getExpandableListPosition(listView.getPositionForView(listItem));
      final int positionGroup = ExpandableListView.getPackedPositionGroup(pos);

      if (listView.isGroupExpanded(positionGroup)) {
        listView.collapseGroup(positionGroup);
      } else {
        listView.expandGroup(positionGroup);
      }
    }
  };
}