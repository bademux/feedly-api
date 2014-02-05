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

package org.github.bademux.feedly.api.provider;

import android.net.Uri;

public final class FeedlyContract {

  /** The authority for the contacts provider */
  public static final String AUTHORITY = "org.github.bademux.feedly.api";
  /** A content:// style uri to the authority for the contacts provider */
  public static final Uri AUTHORITY_URI = Uri.parse("content://" + AUTHORITY);

  /**  Class that represents a Feed list */
  public static final class Feeds implements FeedsColumns {

    public static final String TBL_NAME = "feeds";

    public static final Uri CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, TBL_NAME);

    /** This utility class cannot be instantiated */
    private Feeds() {}
  }

  /** Class that represents a Category list */
  public static final class Categories implements CategoriesColumns {

    public static final String TBL_NAME = "categories";

    public static final Uri CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, TBL_NAME);

    /** This utility class cannot be instantiated */
    private Categories() {}
  }

  /** Class that represents a FeedsCategories list - used only internaly */
  public static final class FeedsCategories implements FeedsCategoriesColumns {

    public static final String TBL_NAME = "feeds_categories";

    public static final Uri CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, TBL_NAME);

    /** This utility class cannot be instantiated */
    private FeedsCategories() {}
  }

  /** Class that represents a FeedsByCategory list - sql View*/
  public static final class FeedsByCategory implements FeedsColumns, FeedsCategoriesColumns {

    public static final String TBL_NAME = "feeds_by_category";

    public static final Uri CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, TBL_NAME);

    /** This utility class cannot be instantiated */
    private FeedsByCategory() {}
  }


  protected interface FeedsCategoriesColumns {

    public static final String FEED_ID = "feed_id", CATEGORY_ID = "category_id";
  }

  protected interface FeedsColumns {

    public static final String ID = "id";
    public static final String TITLE = "title";
    public static final String SORTID = "sortid";
    public static final String UPDATED = "updated";
    public static final String WEBSITE = "website";
    public static final String VELOCITY = "velocity";
    public static final String STATE = "state";
  }

  protected interface CategoriesColumns {

    public static final String ID = "id";
    public static final String LABEL = "label";
  }
}
