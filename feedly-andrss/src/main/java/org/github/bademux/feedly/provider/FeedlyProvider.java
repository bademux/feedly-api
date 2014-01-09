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


package org.github.bademux.feedly.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;

import org.github.bademux.feedly.api.provider.FeedlyContract;
import org.github.bademux.feedly.api.util.db.FeedlyDbUtils;

import java.util.ArrayList;
import java.util.List;

import static java.lang.String.valueOf;
import static org.github.bademux.feedly.api.provider.FeedlyContract.Categories;
import static org.github.bademux.feedly.api.provider.FeedlyContract.Feeds;
import static org.github.bademux.feedly.api.provider.FeedlyContract.FeedsByCategory;
import static org.github.bademux.feedly.api.provider.FeedlyContract.FeedsCategories;

public class FeedlyProvider extends ContentProvider {

  private static final String PROVIDER_URI = "org.github.bademux.feedly.provider.FeedlyProvider";

  private static final UriMatcher sUriMatcher = new UriMatcher(Code.AUTHORITY);

  static {
    sUriMatcher.addURI(FeedlyContract.AUTHORITY, Feeds.TBL_NAME + "/#", Code.FEED);
    sUriMatcher.addURI(FeedlyContract.AUTHORITY, Feeds.TBL_NAME, Code.FEEDS);
    sUriMatcher.addURI(FeedlyContract.AUTHORITY,
                       FeedsByCategory.TBL_NAME + "/*", Code.FEEDS_BY_CATEGORY);
    sUriMatcher.addURI(FeedlyContract.AUTHORITY, Categories.TBL_NAME + "/#", Code.CATEGORY);
    sUriMatcher.addURI(FeedlyContract.AUTHORITY, Categories.TBL_NAME, Code.CATEGORIES);
  }

  /** {@inheritDoc} */
  @Override
  public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                      String sortOrder) {
    Log.i(TAG, "Querying database");
    projection = FeedlyDbUtils.merge(projection, "rowid as _id");

    final SQLiteDatabase db = mHelper.getReadableDatabase();
    switch (sUriMatcher.match(uri)) {
      case Code.FEED:
        return db.query(Feeds.TBL_NAME, projection, selection, selectionArgs, null, null, null);
      case Code.FEEDS:
        return db.query(Feeds.TBL_NAME, projection, null, null, null, null, sortOrder);
      case Code.FEEDS_BY_CATEGORY:
        projection = FeedlyDbUtils.merge(projection, FeedsByCategory.CATEGORY_ID);
        return db.query(FeedsByCategory.TBL_NAME, projection, FeedsByCategory.CATEGORY_ID + "=?",
                        new String[]{uri.getLastPathSegment()}, null, null, null);
      case Code.CATEGORY:
        return db.query(Categories.TBL_NAME, projection, selection, selectionArgs,
                        null, null, null);
      case Code.CATEGORIES:
        return db.query(Categories.TBL_NAME, projection, null, null, null, null, sortOrder);
      case Code.AUTHORITY: return null;
      default:
        throw new UnsupportedOperationException("Unsupported Uri " + uri);
    }
  }

  /** {@inheritDoc} */
  @Override
  public Uri insert(final Uri uri, final ContentValues values) {
    Log.i(TAG, "Inserting into database");
    final SQLiteDatabase db = mHelper.getWritableDatabase();
    switch (sUriMatcher.match(uri)) {
      case Code.FEEDS:
        return Uri.withAppendedPath(uri, valueOf(db.insert(Feeds.TBL_NAME, null, values)));
      case Code.CATEGORIES:
        return Uri.withAppendedPath(uri, valueOf(db.insert(Categories.TBL_NAME, null, values)));
      default:
        throw new UnsupportedOperationException("Unsupported Uri " + uri);
    }
  }

  /** {@inheritDoc} */
  @Override
  public int delete(final Uri uri, final String selection, final String[] selectionArgs) {
    Log.i(TAG, "Deleting from database");
    final SQLiteDatabase db = mHelper.getWritableDatabase();
    switch (sUriMatcher.match(uri)) {
      case Code.FEEDS:
        return db.delete(Feeds.TBL_NAME, selection, selectionArgs);
      case Code.CATEGORIES:
        return db.delete(Categories.TBL_NAME, selection, selectionArgs);
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
    switch (sUriMatcher.match(uri)) {
      case Code.FEEDS:
        return db.update(Feeds.TBL_NAME, values, selection, selectionArgs);
      case Code.CATEGORIES:
        return db.update(Categories.TBL_NAME, values, selection, selectionArgs);
      default:
        throw new UnsupportedOperationException("Unsupported Uri " + uri);
    }
  }

  /** {@inheritDoc} */
  @Override
  public String getType(final Uri uri) { return null; }

  /** {@inheritDoc} */
  @Override
  public boolean onCreate() {mHelper = new DatabaseHelper(getContext()); return true;}

  private DatabaseHelper mHelper;

  private static final String TAG = "FeedlyProvider";

  private static class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "feedly_cache.db";

    private static final int VERSION = 1;

    /**
     * Enables sql relarions
     * {@inheritDoc}
     */
    @Override
    public void onConfigure(SQLiteDatabase db) { db.setForeignKeyConstraintsEnabled(true); }

    /** {@inheritDoc} */
    @Override
    public void onCreate(SQLiteDatabase db) {
      Log.i(TAG, "Creating database");
      db.execSQL("CREATE TABLE IF NOT EXISTS " + Feeds.TBL_NAME + "("
                 + Feeds.ID + " TEXT PRIMARY KEY,"
                 + Feeds.TITLE + " TEXT NOT NULL,"
                 + Feeds.SORTID + " TEXT,"
                 + Feeds.UPDATED + " INTEGER DEFAULT 0,"
                 + Feeds.WEBSITE + " TEXT)");
      db.execSQL("CREATE INDEX idx_" + Feeds.TBL_NAME + "_" + Feeds.TITLE
                 + " ON " + Feeds.TBL_NAME + "(" + Feeds.TITLE + ")");
      db.execSQL("CREATE INDEX idx_" + Feeds.TBL_NAME + "_" + Feeds.UPDATED
                 + " ON " + Feeds.TBL_NAME + "(" + Feeds.UPDATED + ")");
      db.execSQL("CREATE INDEX idx_" + Feeds.TBL_NAME + "_" + Feeds.WEBSITE
                 + " ON " + Feeds.TBL_NAME + "(" + Feeds.WEBSITE + ")");

      db.execSQL("CREATE TABLE IF NOT EXISTS " + Categories.TBL_NAME + "("
                 + Categories.ID + " TEXT PRIMARY KEY,"
                 + Categories.LABEL + " TEXT)");
      db.execSQL("CREATE INDEX idx_" + Categories.TBL_NAME + "_" + Categories.LABEL
                 + " ON " + Categories.TBL_NAME + "(" + Categories.LABEL + ")");

      db.execSQL("CREATE TABLE IF NOT EXISTS " + FeedsCategories.TBL_NAME + "("
                 + FeedsCategories.FEED_ID + " TEXT,"
                 + FeedsCategories.CATEGORY_ID + " TEXT,"
                 + "PRIMARY KEY(" + FeedsCategories.FEED_ID + "," + FeedsCategories.CATEGORY_ID
                 + "),"
                 + "FOREIGN KEY(" + FeedsCategories.FEED_ID + ") REFERENCES "
                 + Feeds.TBL_NAME + "(" + Feeds.ID + ") ON UPDATE CASCADE ON DELETE CASCADE,"
                 + "FOREIGN KEY(" + FeedsCategories.CATEGORY_ID + ") REFERENCES "
                 + Categories.TBL_NAME + "(" + Categories.ID
                 + ") ON UPDATE CASCADE ON DELETE CASCADE,"
                 + "UNIQUE(" + FeedsCategories.FEED_ID + ") ON CONFLICT REPLACE,"
                 + "UNIQUE(" + FeedsCategories.CATEGORY_ID + ") ON CONFLICT REPLACE)");

      db.execSQL("CREATE VIEW " + FeedsByCategory.TBL_NAME + " AS "
                 + "SELECT * FROM " + Feeds.TBL_NAME + " INNER JOIN " + FeedsCategories.TBL_NAME
                 + " ON " + Feeds.TBL_NAME + "." + Feeds.ID + "="
                 + FeedsCategories.TBL_NAME + "." + FeedsCategories.FEED_ID);

      //tmpdata
      db.execSQL("INSERT INTO categories (id, label) VALUES('category/1', 'my category')");
      db.execSQL("INSERT INTO feeds (id, title) VALUES('feed/1', 'my feed')");
      db.execSQL(
          "INSERT INTO feeds_categories (feed_id, category_id) VALUES('feed/1', 'category/1')");
    }

    /** {@inheritDoc} */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
      Log.i(TAG, "Upgrading database; wiping app data");
      clear(db);
      onCreate(db);
      db.execSQL("VACUUM");
    }

    protected void clear(SQLiteDatabase db) {
      Cursor c = db.rawQuery("SELECT TBL_NAME FROM sqlite_master WHERE type='table'", null);
      if (!c.moveToFirst()) {
        return;
      }

      List<String> tables = new ArrayList<>();
      while (!c.isAfterLast()) {
        String tableName = c.getString(0);
        if (!tableName.equals("android_metadata")) {
          tables.add(tableName);
        }
        c.moveToNext();
      }
      //cleanup
      db.beginTransaction();
      try {
        for (String tableName : tables) {
          db.delete(tableName, null, null);
//        db.execSQL("DROP TABLE IF EXISTS '" + tableName + '\'');
        }
        db.setTransactionSuccessful();
      } finally {
        db.endTransaction();
      }
    }

    /** {@inheritDoc} */
    public DatabaseHelper(Context context) { super(context, DB_NAME, null, VERSION); }

    private static final String TAG = "DatabaseHelper";
  }

  private interface Code {

    static final int AUTHORITY = 0;
    static final int FEEDS = 100, FEED = 101, FEEDS_BY_CATEGORY = 102;
    static final int CATEGORIES = 200, CATEGORY = 201;
  }
}