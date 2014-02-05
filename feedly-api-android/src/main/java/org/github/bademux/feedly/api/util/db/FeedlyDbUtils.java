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

package org.github.bademux.feedly.api.util.db;

import com.google.api.client.json.GenericJson;
import com.google.api.client.util.GenericData;

import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.net.Uri;

import org.github.bademux.feedly.api.model.Category;
import org.github.bademux.feedly.api.model.Feed;
import org.github.bademux.feedly.api.model.Subscription;
import org.github.bademux.feedly.api.provider.FeedlyContract;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.content.ContentProviderOperation.Builder;
import static org.github.bademux.feedly.api.provider.FeedlyContract.Categories;
import static org.github.bademux.feedly.api.provider.FeedlyContract.Feeds;
import static org.github.bademux.feedly.api.provider.FeedlyContract.FeedsCategories;

public final class FeedlyDbUtils {

  public static Collection<ContentProviderOperation> prepareInsertOperations(
      Collection<Subscription> subscriptions) {
    //approx. size: subscriptions + categories + mappings
    Map<String, ContentProviderOperation> uniqueOps = new HashMap<>(subscriptions.size() * 3);
    for (Subscription subscription : subscriptions) {
      uniqueOps.put(subscription.getId(), buildInsert(Feeds.CONTENT_URI, subscription).build());
      List<Category> categories = subscription.getCategories();
      if (categories != null && !categories.isEmpty()) {
        for (Category category : categories) {
          uniqueOps.put(category.getId(), buildInsert(Categories.CONTENT_URI, category).build());
          uniqueOps.put(subscription.getId() + category.getId(),
                        buildInsert(FeedsCategories.CONTENT_URI,
                                    "feed_id", subscription, "category_id", category)
                            //hints commiting after this operation
                            .withYieldAllowed(true).build());
        }
      }
    }
    return uniqueOps.values();
  }

  public static <T extends GenericJson> Builder buildInsert(final Uri uri, final T genericData) {
    Builder builder = ContentProviderOperation.newInsert(uri);
    for (String fieldName : genericData.getClassInfo().getNames()) {
      Object value = genericData.get(fieldName);
      if (isAllowed(value)) {
        builder.withValue(fieldName, value);
      }
    }
    return builder;
  }

  public static <T extends Map<String, ?>> Builder buildInsert(final Uri uri,
                                                               final String column_name1,
                                                               final T entity1,
                                                               final String column_name2,
                                                               final T entity2) {
    return ContentProviderOperation.newInsert(uri)
        .withValue(column_name1, entity1.get("id")).withValue(column_name2, entity2.get("id"));
  }

  public static boolean isAllowed(Object value) {
    return value == null || value instanceof String || value instanceof Byte
           || value instanceof Short || value instanceof Integer || value instanceof Long
           || value instanceof Float || value instanceof Double || value instanceof Boolean
           || value instanceof byte[];
  }

  public static ContentValues convert(final GenericData genericData) {
    Collection<String> fieldNames = genericData.getClassInfo().getNames();
    ContentValues contentValues = new ContentValues(fieldNames.size());
    for (String fieldName : fieldNames) {
      Object value = genericData.get(fieldName);
      if (value == null) {
        contentValues.putNull(fieldName);
      } else if (value instanceof String) {
        contentValues.put(fieldName, (String) value);
      } else if (value instanceof Byte) {
        contentValues.put(fieldName, (Byte) value);
      } else if (value instanceof Short) {
        contentValues.put(fieldName, (Short) value);
      } else if (value instanceof Integer) {
        contentValues.put(fieldName, (Integer) value);
      } else if (value instanceof Long) {
        contentValues.put(fieldName, (Long) value);
      } else if (value instanceof Float) {
        contentValues.put(fieldName, (Float) value);
      } else if (value instanceof Double) {
        contentValues.put(fieldName, (Double) value);
      } else if (value instanceof Boolean) {
        contentValues.put(fieldName, (Boolean) value);
      } else if (value instanceof byte[]) {
        contentValues.put(fieldName, (byte[]) value);
      } else {
        throw new IllegalArgumentException("Unknown type: " + value.getClass().getName()
                                           + " for GenericData: " + genericData);
      }
    }
    return contentValues;
  }

  public static void execute(SQLiteDatabase db, Collection<SQLiteStatement> stms) {
    db.beginTransaction();
    try {
      for (SQLiteStatement stm : stms) {
        stm.execute();
      }
      db.setTransactionSuccessful();
    } finally {
      db.endTransaction();
    }
  }

  /**
   * @return list of prepared stetements - nullsafe
   */
  public static Collection<SQLiteStatement> prepareInserts(SQLiteDatabase db,
                                                           Collection<Subscription> subscriptions) {
    //approx: subscriptions + categories + mappings
    List<SQLiteStatement> stms = new ArrayList<>(subscriptions.size() * 3);
    for (Subscription subscription : subscriptions) {
      stms.add(createFeedStatement(db, subscription));
      List<Category> categories = subscription.getCategories();
      if (categories != null && !categories.isEmpty()) {
        for (Category category : categories) {
          stms.add(createCategoryStatement(db, category));
          stms.add(createFeedsCategoriesStatement(db, subscription, category));
        }
      }
    }
    return stms;
  }

  protected static SQLiteStatement createFeedStatement(SQLiteDatabase db,
                                                       Subscription subscription) {
    SQLiteStatement stm = db.compileStatement(INSERT_FEEDS);
    for (String key : subscription.keySet()) {
      switch (key) {
        case "id": stm.bindString(1, subscription.getId()); continue;
        case "title": stm.bindString(2, subscription.getTitle()); continue;
        case "sortid": stm.bindString(3, subscription.getSortid()); continue;
        case "updated": stm.bindLong(4, subscription.getUpdated()); continue;
        case "website": stm.bindString(5, subscription.getWebsite()); continue;
      }
    }
    return stm;
  }

  protected static SQLiteStatement createCategoryStatement(SQLiteDatabase db, Category category) {
    SQLiteStatement stm = db.compileStatement(INSERT_CATEGORIES);
    for (String key : category.keySet()) {
      switch (key) {
        case "id": stm.bindString(1, category.getId()); continue;
        case "label": stm.bindString(2, category.getLabel()); continue;
      }
    }
    return stm;
  }

  protected static SQLiteStatement createFeedsCategoriesStatement(SQLiteDatabase db,
                                                                  Feed feed, Category category) {
    SQLiteStatement stm = db.compileStatement(INSERT_FEEDS_CATEGORIES);
    stm.bindString(1, feed.getId());
    stm.bindString(2, category.getId());
    return stm;
  }

  public static void create(SQLiteDatabase db) {
    db.execSQL("CREATE TABLE IF NOT EXISTS " + Feeds.TBL_NAME + "("
               + Feeds.ID + " TEXT PRIMARY KEY,"
               + Feeds.TITLE + " TEXT NOT NULL,"
               + Feeds.SORTID + " TEXT,"
               + Feeds.UPDATED + " INTEGER DEFAULT 0,"
               + Feeds.WEBSITE + " TEXT,"
               + Feeds.VELOCITY + " DOUBLE,"
               + Feeds.STATE + " TEXT)");
    db.execSQL("CREATE INDEX idx_" + Feeds.TBL_NAME + "_" + Feeds.TITLE
               + " ON " + Feeds.TBL_NAME + "(" + Feeds.TITLE + ")");
    db.execSQL("CREATE INDEX idx_" + Feeds.TBL_NAME + "_" + Feeds.SORTID
               + " ON " + Feeds.TBL_NAME + "(" + Feeds.SORTID + ")");
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

    db.execSQL("CREATE VIEW " + FeedlyContract.FeedsByCategory.TBL_NAME + " AS "
               + "SELECT * FROM " + Feeds.TBL_NAME + " INNER JOIN " + FeedsCategories.TBL_NAME
               + " ON " + Feeds.TBL_NAME + "." + Feeds.ID + "="
               + FeedsCategories.TBL_NAME + "." + FeedsCategories.FEED_ID);
  }

  public static void dropAll(SQLiteDatabase db) {
    drop(db, "index", "name NOT LIKE 'sqlite_autoindex_%'");

    //TODO: delete dependant last
    //FeedlyDbUtils.drop(db, "table", "name != 'android_metadata'");
    db.execSQL("DROP TABLE 'feeds_categories'");
    db.execSQL("DROP TABLE 'categories'");
    db.execSQL("DROP TABLE 'feeds'");

    drop(db, "view", null);
  }

  public static void drop(SQLiteDatabase db, String type, String whereClause) {
    String sql = "SELECT name FROM sqlite_master WHERE type='" + type + '\'';
    if (whereClause != null) {
      sql += " AND " + whereClause;
    }

    Cursor c = db.rawQuery(sql, null);
    if (c.moveToFirst()) {
      while (!c.isAfterLast()) {
        db.execSQL("DROP " + type + " '" + c.getString(0) + '\'');
        c.moveToNext();
      }
    }
  }

  /**
   * Merges two String arrays, nullsafe
   *
   * @param strings1 - first array
   * @param strings2 - second array
   * @return merged
   */
  public static String[] merge(String[] strings1, String... strings2) {
    if (strings1 == null || strings2.length == 0) {
      if (strings2 == null || strings2.length == 0) {
        return null;
      }
      return strings2;
    }

    String[] tmpProjection = new String[strings1.length + strings2.length];
    System.arraycopy(strings2, 0, tmpProjection, 0, strings2.length);
    System.arraycopy(strings1, 0, tmpProjection, strings2.length, strings1.length);
    return tmpProjection;
  }

  private final static String INSERT_FEEDS =
      "INSERT OR REPLACE INTO feeds (id,title,sortid,updated,website) VALUES (?,?,?,?,?)";
  private final static String INSERT_CATEGORIES =
      "INSERT OR REPLACE INTO categories (id,label) VALUES (?,?)";
  private final static String INSERT_FEEDS_CATEGORIES =
      "INSERT OR IGNORE INTO feeds_categories (feed_id,category_id) VALUES (?,?)";

  private FeedlyDbUtils() {}
}
