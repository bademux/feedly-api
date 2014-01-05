/*
 * Copyright 2013 Bademus
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

package org.github.bademux.feedly.api.util.db;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import org.github.bademux.feedly.api.model.Category;
import org.github.bademux.feedly.api.model.Feed;
import org.github.bademux.feedly.api.model.Subscription;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class FeedlyDbUtils {

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
