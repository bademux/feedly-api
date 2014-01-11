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
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.net.Uri;

import org.github.bademux.feedly.api.model.Category;
import org.github.bademux.feedly.api.model.Feed;
import org.github.bademux.feedly.api.model.Subscription;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static android.content.ContentProviderOperation.Builder;
import static org.github.bademux.feedly.api.provider.FeedlyContract.Categories;
import static org.github.bademux.feedly.api.provider.FeedlyContract.Feeds;
import static org.github.bademux.feedly.api.provider.FeedlyContract.FeedsCategories;

public final class FeedlyDbUtils {

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

  public static ArrayList<ContentProviderOperation> prepareInsertOperations(
      Collection<Subscription> subscriptions) {
    //approx: subscriptions + categories + mappings
    int sz = subscriptions.size() * 3;
    ArrayList<ContentProviderOperation> contentValues = new ArrayList<ContentProviderOperation>(sz);
    for (Subscription subscription : subscriptions) {
      contentValues.add(buildInsert(Feeds.CONTENT_URI, subscription).build());
      List<Category> categories = subscription.getCategories();
      if (categories != null && !categories.isEmpty()) {
        for (Category category : categories) {
          contentValues.add(buildInsert(Categories.CONTENT_URI, category).build());
          //it is possible to commit after this operation
          contentValues.add(buildInsert(FeedsCategories.CONTENT_URI, subscription, category)
                                .withYieldAllowed(true).build());
        }
      }
    }
    return contentValues;
  }

  public static <T extends GenericJson> Builder buildInsert(final Uri uri, final T genericData) {
    Builder builder = ContentProviderOperation.newInsert(uri);
    for (String fieldName : genericData.getClassInfo().getNames()) {
      builder.withValue(fieldName, genericData.get(fieldName));
    }
    return builder;
  }

  public static <T extends GenericJson> Builder buildInsert(final Uri uri, final T idJsonEntity1,
                                                            final T idJsonEntity2) {
    return ContentProviderOperation.newInsert(uri)
        .withValue("id", idJsonEntity1.get("id")).withValue("id", idJsonEntity2.get("id"));
  }

  public static Collection<ContentValues> prepareContentValues(
      final Collection<Subscription> subscriptions) {
    //approx: subscriptions + categories + mappings
    List<ContentValues> contentValues = new ArrayList<ContentValues>(subscriptions.size() * 3);
    for (Subscription subscription : subscriptions) {
      contentValues.add(convertFrom(subscription));
      List<Category> categories = subscription.getCategories();
      if (categories != null && !categories.isEmpty()) {
        for (Category category : categories) {
          contentValues.add(convertFrom(category));
          ContentValues feedsCategories = new ContentValues(2);
          feedsCategories.put(subscription.getId(), category.getId());
        }
      }
    }
    return contentValues;
  }

  public static ContentValues convertFrom(final GenericData genericData) {
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

  private final static String INSERT_FEEDS =
      "INSERT OR REPLACE INTO feeds (id,title,sortid,updated,website) VALUES (?,?,?,?,?)";
  private final static String INSERT_CATEGORIES =
      "INSERT OR REPLACE INTO categories (id,label) VALUES (?,?)";
  private final static String INSERT_FEEDS_CATEGORIES =
      "INSERT OR IGNORE INTO feeds_categories (feed_id,category_id) VALUES (?,?)";

  private FeedlyDbUtils() {}
}
