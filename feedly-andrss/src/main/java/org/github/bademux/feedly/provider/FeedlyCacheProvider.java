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

package org.github.bademux.feedly.provider;

import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;

import org.github.bademux.feedly.api.util.db.FeedlyDbUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import static android.database.sqlite.SQLiteDatabase.CONFLICT_IGNORE;
import static org.github.bademux.feedly.api.provider.FeedlyContract.AUTHORITY;
import static org.github.bademux.feedly.api.provider.FeedlyContract.Categories;
import static org.github.bademux.feedly.api.provider.FeedlyContract.Entries;
import static org.github.bademux.feedly.api.provider.FeedlyContract.EntriesByCategory;
import static org.github.bademux.feedly.api.provider.FeedlyContract.EntriesByTag;
import static org.github.bademux.feedly.api.provider.FeedlyContract.EntriesTags;
import static org.github.bademux.feedly.api.provider.FeedlyContract.Feeds;
import static org.github.bademux.feedly.api.provider.FeedlyContract.FeedsByCategory;
import static org.github.bademux.feedly.api.provider.FeedlyContract.FeedsCategories;
import static org.github.bademux.feedly.api.provider.FeedlyContract.Files;
import static org.github.bademux.feedly.api.provider.FeedlyContract.Tags;
import static org.github.bademux.feedly.api.util.db.FeedlyDbUtils.merge;

public class FeedlyCacheProvider extends ContentProvider {

  private static final UriMatcher URI_MATCHER = new UriMatcher(Code.AUTHORITY);

  static {
    URI_MATCHER.addURI(AUTHORITY, Entries.TBL_NAME + "/#", Code.ENTRY);
    URI_MATCHER.addURI(AUTHORITY, Entries.TBL_NAME, Code.ENTRIES);
    URI_MATCHER.addURI(AUTHORITY, EntriesByTag.TBL_NAME + "/*", Code.ENTRIES_BY_TAG);
    URI_MATCHER.addURI(AUTHORITY, EntriesByCategory.TBL_NAME + "/*", Code.ENTRIES_BY_CATEGORY);
    URI_MATCHER.addURI(AUTHORITY, Feeds.TBL_NAME + "/#", Code.FEED);
    URI_MATCHER.addURI(AUTHORITY, Feeds.TBL_NAME, Code.FEEDS);
    URI_MATCHER.addURI(AUTHORITY, FeedsByCategory.TBL_NAME + "/*", Code.FEEDS_BY_CATEGORY);
    URI_MATCHER.addURI(AUTHORITY, Categories.TBL_NAME + "/#", Code.CATEGORY);
    URI_MATCHER.addURI(AUTHORITY, Categories.TBL_NAME, Code.CATEGORIES);
    URI_MATCHER.addURI(AUTHORITY, FeedsCategories.TBL_NAME, Code.FEEDS_CATEGORIES);
    URI_MATCHER.addURI(AUTHORITY, EntriesTags.TBL_NAME, Code.ENTRIES_TAGS);
    URI_MATCHER.addURI(AUTHORITY, Tags.TBL_NAME + "/#", Code.TAG);
    URI_MATCHER.addURI(AUTHORITY, Tags.TBL_NAME, Code.TAGS);
    URI_MATCHER.addURI(AUTHORITY, Files.TBL_NAME, Code.FILES);
  }

  /** {@inheritDoc} */
  @Override
  public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                      String sortOrder) {
    Log.i(TAG, "Querying database: " + uri);

    final SQLiteDatabase db = mHelper.getReadableDatabase();
    assert db != null;
    switch (URI_MATCHER.match(uri)) {
      case Code.ENTRY:
        return db.query(Entries.TBL_NAME, projection, selection, selectionArgs, null, null, null);
      case Code.ENTRIES:
        return db.query(Entries.TBL_NAME, merge(projection, "rowid as _id"),
                        selection, selectionArgs, null, null, sortOrder);
      case Code.ENTRIES_BY_TAG:
        return db.query(EntriesByTag.TBL_NAME,
                        merge(projection, "rowid as _id", EntriesByTag.TAG_ID),
                        EntriesByTag.TAG_ID + "=?",
                        new String[]{uri.getLastPathSegment()}, null, null, null);
      case Code.ENTRIES_BY_CATEGORY:
        return db.query(EntriesByCategory.TBL_NAME,
                        merge(projection, "rowid as _id", EntriesByCategory.CATEGORY_ID),
                        EntriesByCategory.CATEGORY_ID + "=?",
                        new String[]{uri.getLastPathSegment()}, null, null, null);
      case Code.FEED:
        return db.query(Feeds.TBL_NAME, projection, selection, selectionArgs, null, null, null);
      case Code.FEEDS:
        return db.query(Feeds.TBL_NAME, merge(projection, "rowid as _id"),
                        selection, selectionArgs, null, null, sortOrder);
      case Code.FEEDS_BY_CATEGORY:
        return db.query(FeedsByCategory.TBL_NAME,
                        merge(projection, "rowid as _id", FeedsByCategory.CATEGORY_ID),
                        FeedsByCategory.CATEGORY_ID + "=?",
                        new String[]{uri.getLastPathSegment()}, null, null, null);
      case Code.CATEGORY:
        return db.query(Categories.TBL_NAME, projection, selection, selectionArgs,
                        null, null, null);
      case Code.CATEGORIES:
        return db.query(Categories.TBL_NAME, merge(projection, "rowid as _id"),
                        selection, selectionArgs, null, null, sortOrder);
      case Code.TAG:
        return db.query(Tags.TBL_NAME, projection, selection, selectionArgs,
                        null, null, null);
      case Code.TAGS:
        return db.query(Tags.TBL_NAME, merge(projection, "rowid as _id"),
                        selection, selectionArgs, null, null, sortOrder);
      case Code.FILES:
        return db.query(Files.TBL_NAME, projection, Files.URL + "=?",
                        new String[]{uri.getLastPathSegment()}, null, null, null);
      case Code.AUTHORITY: return null;
      default:
        throw new UnsupportedOperationException("Unsupported Uri " + uri);
    }
  }

  /** {@inheritDoc} */
  @Override
  public Uri insert(final Uri uri, final ContentValues values) {
    Log.i(TAG, "Inserting into database: " + uri);
    long rowId = insert(mHelper.getWritableDatabase(), URI_MATCHER.match(uri), values);
    return Uri.withAppendedPath(uri, String.valueOf(rowId));
  }

  protected long insert(final SQLiteDatabase db, final int uriCode, final ContentValues values) {
    switch (uriCode) {
      case Code.ENTRIES:
        return db.replace(Entries.TBL_NAME, null, values);
      case Code.FEEDS:
        return db.replace(Feeds.TBL_NAME, null, values);
      case Code.CATEGORIES:
        return db.replace(Categories.TBL_NAME, null, values);
      case Code.FEEDS_CATEGORIES:
        return db.insertWithOnConflict(FeedsCategories.TBL_NAME, null, values, CONFLICT_IGNORE);
      case Code.TAGS:
        return db.replace(Tags.TBL_NAME, null, values);
      case Code.ENTRIES_TAGS:
        return db.insertWithOnConflict(EntriesTags.TBL_NAME, null, values, CONFLICT_IGNORE);
      case Code.FILES:
        return db.replace(Files.TBL_NAME, null, values);
      case UriMatcher.NO_MATCH:
        throw new UnsupportedOperationException("Unmatched Uri");
      default:
        throw new UnsupportedOperationException("Unsupported Uri code: " + uriCode);
    }
  }

  /** {@inheritDoc} */
  @Override
  public int delete(final Uri uri, final String selection, final String[] selectionArgs) {
    Log.i(TAG, "Deleting from database");
    final SQLiteDatabase db = mHelper.getWritableDatabase();
    assert db != null;
    switch (URI_MATCHER.match(uri)) {
      case Code.ENTRIES:
        return db.delete(Entries.TBL_NAME, selection, selectionArgs);
      case Code.FEEDS:
        return db.delete(Feeds.TBL_NAME, selection, selectionArgs);
      case Code.CATEGORIES:
        return db.delete(Categories.TBL_NAME, selection, selectionArgs);
      case Code.FEEDS_CATEGORIES:
        return db.delete(FeedsCategories.TBL_NAME, selection, selectionArgs);
      case Code.TAGS:
        return db.delete(Tags.TBL_NAME, selection, selectionArgs);
      case Code.ENTRIES_TAGS:
        return db.delete(EntriesTags.TBL_NAME, selection, selectionArgs);
      default:
        throw new UnsupportedOperationException("Unsupported Uri " + uri);
    }
  }

  /** {@inheritDoc} */
  @Override
  public int update(final Uri uri, final ContentValues values,
                    final String selection, final String[] selectionArgs) {
    Log.i(TAG, "Updating data in database");
    final SQLiteDatabase db = mHelper.getWritableDatabase();
    assert db != null;
    switch (URI_MATCHER.match(uri)) {
      case Code.ENTRIES:
        return db.update(Entries.TBL_NAME, values, selection, selectionArgs);
      case Code.FEEDS:
        return db.update(Feeds.TBL_NAME, values, selection, selectionArgs);
      case Code.CATEGORIES:
        return db.update(Categories.TBL_NAME, values, selection, selectionArgs);
      case Code.FILES:
        return db.update(Files.TBL_NAME, values, Files.URL + "=?",
                         new String[]{uri.getLastPathSegment()});
      default:
        throw new UnsupportedOperationException("Unsupported Uri " + uri);
    }
  }

  /**
   * With notification
   * {@inheritDoc}
   */
  @Override
  public ContentProviderResult[] applyBatch(ArrayList<ContentProviderOperation> operations)
      throws OperationApplicationException {

    final Set<Uri> uris = new HashSet<>(operations.size());
    for (ContentProviderOperation operation : operations) {
      uris.add(operation.getUri());
    }

    final ContentProviderResult[] results = super.applyBatch(operations);
    if (results != null && results.length > 0) {
      for (Uri uri : uris) { notify(uri); }
    }
    return results;
  }

  /**
   * With notification
   * {@inheritDoc}
   */
  @Override
  public int bulkInsert(Uri uri, ContentValues[] values) {
    int numValues = super.bulkInsert(uri, values);
    if (numValues > 0) {
      notify(uri);
    }
    return numValues;
  }

  protected final void notify(final Uri uri) {
    final ContentResolver resolver = mResolver.get();
    if (resolver != null) { resolver.notifyChange(uri, null); }
  }

  /** {@inheritDoc} */
  @Override
  public String getType(final Uri uri) { return null; }

  /** {@inheritDoc} */
  @Override
  public boolean onCreate() {
    mHelper = new DatabaseHelper(getContext());
    mResolver = new WeakReference<>(getContext().getContentResolver());
    return true;
  }

  private DatabaseHelper mHelper;

  private WeakReference<ContentResolver> mResolver;

  private static final String TAG = "FeedlyCacheProvider";

  private static class DatabaseHelper extends SQLiteOpenHelper {

    /**
     * Enables sql relationship
     * {@inheritDoc}
     */
    @Override
    public void onConfigure(final SQLiteDatabase db) { db.setForeignKeyConstraintsEnabled(true); }

    /** {@inheritDoc} */
    @Override
    public void onCreate(final SQLiteDatabase db) {
      Log.i(TAG, "Creating database");
      FeedlyDbUtils.create(db);
    }

    /** {@inheritDoc} */
    @Override
    public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
      Log.i(TAG, "Upgrading database; wiping app data");
      FeedlyDbUtils.dropAll(db);
      onCreate(db);
    }

    @Override
    public void onDowngrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
      onUpgrade(db, oldVersion, newVersion);
    }


    public DatabaseHelper(final Context context) { super(context, DB_NAME, null, VERSION); }

    private static final String DB_NAME = "feedly_cache.db";

    private static final int VERSION = 1;

    static final String TAG = "DatabaseHelper";
  }

  private interface Code {

    static final int AUTHORITY = 0;
    static final int FEEDS = 100, FEED = 101, FEEDS_BY_CATEGORY = 102;
    static final int CATEGORIES = 200, CATEGORY = 201;
    static final int FEEDS_CATEGORIES = 300, ENTRIES_TAGS = 301;
    static final int TAGS = 400, TAG = 401;
    static final int ENTRIES = 500, ENTRY = 501, ENTRIES_BY_TAG = 502, ENTRIES_BY_CATEGORY = 503;
    static final int FILES = 600;
  }
}