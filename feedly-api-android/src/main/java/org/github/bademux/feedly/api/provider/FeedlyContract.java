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

  /** Class that represents a Feed list */
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

  /** Class that represents a Feed - Category mappings - used internaly */
  public static final class FeedsCategories implements FeedsCategoriesColumns {

    public static final String TBL_NAME = "feeds_categories";

    public static final Uri CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, TBL_NAME);

    /** This utility class cannot be instantiated */
    private FeedsCategories() {}
  }

  /** Class that represents a Entry - Tag mappings - used internaly */
  public static final class EntriesTags implements EntriesTagsColumns {

    public static final String TBL_NAME = "entries_tags";

    public static final Uri CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, TBL_NAME);

    /** This utility class cannot be instantiated */
    private EntriesTags() {}
  }

  /** Class that represents a FeedsByCategory list - sql View */
  public static final class FeedsByCategory implements FeedsColumns, FeedsCategoriesColumns {

    public static final String TBL_NAME = "feeds_by_category";

    public static final Uri CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, TBL_NAME);

    /** This utility class cannot be instantiated */
    private FeedsByCategory() {}
  }

  /** Class that represents a Entries */
  public static final class Tags implements TagsColumns {

    public static final String TBL_NAME = "tags";

    public static final Uri CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, TBL_NAME);

    /** This utility class cannot be instantiated */
    private Tags() {}
  }

  /** Class that represents a Entries */
  public static final class Entries implements EntriesColumns {

    public static final String TBL_NAME = "entries";

    public static final Uri CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, TBL_NAME);

    /** This utility class cannot be instantiated */
    private Entries() {}
  }

  /** Class that represents a EntriesByTag list - sql View */
  public static final class EntriesByTag implements EntriesColumns, EntriesTagsColumns {

    public static final String TBL_NAME = "entries_by_tag";

    public static final Uri CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, TBL_NAME);

    /** This utility class cannot be instantiated */
    private EntriesByTag() {}
  }

  /** Class that represents a EntriesByCategory list - sql View */
  public static final class EntriesByCategory implements EntriesColumns, FeedsCategoriesColumns {

    public static final String TBL_NAME = "entries_by_category";

    public static final Uri CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, TBL_NAME);

    /** This utility class cannot be instantiated */
    private EntriesByCategory() {}
  }

  /** Class that represents a Files */
  public static final class Files implements FilesColumns {

    public static final String TBL_NAME = "files";

    public static final Uri CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, TBL_NAME);

    /** This utility class cannot be instantiated */
    private Files() {}
  }


  /** Class that represents a EntriesByTag list - sql View */
  public static final class FilesByEntry implements FilesColumns, EntriesFilesColumns {

    public static final String TBL_NAME = "files_by_entry";

    public static final Uri CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, TBL_NAME);

    /** This utility class cannot be instantiated */
    private FilesByEntry() {}
  }


  /** Class that represents a Entry - Files mappings - used internaly */
  public static final class EntriesFiles implements EntriesFilesColumns {

    public static final String TBL_NAME = "entries_files";

    public static final Uri CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, TBL_NAME);

    /** This utility class cannot be instantiated */
    private EntriesFiles() {}
  }

  protected interface FeedsCategoriesColumns {

    public static final String FEED_ID = "feed_id", CATEGORY_ID = "category_id";
  }

  protected interface EntriesTagsColumns {

    public static final String ENTRY_ID = "entry_id", TAG_ID = "tag_id";
  }

  protected interface EntriesFilesColumns {

    public static final String ENTRY_ID = "entry_id", FILE_URL = "file_url";
  }

  protected interface FeedsColumns {

    public static final String ID = "id";
    public static final String TITLE = "title";
    public static final String SORTID = "sortid";
    public static final String UPDATED = "updated";
    public static final String WEBSITE = "website";
    public static final String VELOCITY = "velocity";
    public static final String STATE = "state";
    public static final String FAVICON = "favicon";
  }

  protected interface CategoriesColumns {

    public static final String ID = "id";
    public static final String LABEL = "label";
  }

  protected interface TagsColumns {

    public static final String ID = "id";
    public static final String LABEL = "label";
  }

  protected interface EntriesColumns {

    public static final String ID = "id";
    public static final String TITLE = "title";
    public static final String CONTENT = "content";
    public static final String CONTENT_DIRECTION = "content_direction";
    public static final String SUMMARY = "summary";
    public static final String SUMMARY_DIRECTION = "summary_direction";
    public static final String PUBLISHED = "published";
    public static final String UPDATED = "updated";
    public static final String AUTHOR = "author";
    public static final String CRAWLED = "crawled";
    public static final String RECRAWLED = "recrawled";
    public static final String UNREAD = "unread";
    public static final String KEYWORDS = "keywords";
    public static final String ENGAGEMENT = "engagement";
    public static final String ENGAGEMENTRATE = "engagementrate";
    public static final String FINGERPRINT = "fingerprint";
    public static final String ORIGINID = "originid";
    public static final String ORIGIN_STREAMID = "origin_streamid";
    public static final String ORIGIN_TITLE = "origin_title";
    public static final String VISUAL_URL = "visual_url";
    public static final String ENCLOSURE_MIMES = "enclosure_mimes";
  }

  protected interface FilesColumns {

    public static final String ID = "rowid";
    public static final String URL = "url";
    public static final String MIME = "mime";
    public static final String FILENAME = "filename";
    public static final String CREATED = "created";
  }
}
