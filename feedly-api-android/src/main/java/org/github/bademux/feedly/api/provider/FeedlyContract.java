/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
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
  }

  protected interface CategoriesColumns {

    public static final String ID = "id";
    public static final String LABEL = "label";
  }
}
