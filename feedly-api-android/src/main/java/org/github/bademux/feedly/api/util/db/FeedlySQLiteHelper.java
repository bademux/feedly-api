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


import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class FeedlySQLiteHelper extends SQLiteOpenHelper {

  @Override
  public void onConfigure(SQLiteDatabase db) { db.setForeignKeyConstraintsEnabled(true); }

  @Override
  public void onCreate(SQLiteDatabase db) {
    BufferedReader reader = null;
    try {
      // Open the resource
      InputStream is = context.getResources().openRawResource(dbResId);
      reader = new BufferedReader(new InputStreamReader(is));
      String stm;
      //read all
      db.beginTransaction();
      try {
        while ((stm = reader.readLine()) != null) {
          if (!stm.trim().isEmpty()) {
            db.execSQL(stm);
          }
        }
        db.setTransactionSuccessful();
      } finally {
        db.endTransaction();
      }
    } catch (IOException e) {
      Log.wtf(TAG, "Can't create database", e);
    } finally {
      if (reader != null) {
        try { reader.close(); } catch (IOException e) {}
      }
    }
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    cleanAll(db);
    db.execSQL("VACUUM");
    onCreate(db);
  }

  @Override
  public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    onUpgrade(db, oldVersion, newVersion);
  }

  protected void cleanAll(SQLiteDatabase db) {
    Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);
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
    //clean
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

  public FeedlySQLiteHelper(Context context, int dbResId) {
    super(context, DATABASE_NAME, null, DATABASE_VERSION);
    this.context = context;
    this.dbResId = dbResId;
  }

  private final Context context;

  private final int dbResId;

  private static final String DATABASE_NAME = "feedly_cache.db";

  private static final int DATABASE_VERSION = 1;

  private static final String TAG = FeedlySQLiteHelper.class.getName();
}
