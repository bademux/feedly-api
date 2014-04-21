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

package org.github.bademux.feedly.service;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;

import org.github.bademux.feedly.api.model.Feed;
import org.github.bademux.feedly.api.service.ServiceManager;

import static android.app.DownloadManager.COLUMN_DESCRIPTION;
import static android.app.DownloadManager.COLUMN_LOCAL_FILENAME;
import static android.app.DownloadManager.COLUMN_LOCAL_URI;
import static android.app.DownloadManager.COLUMN_MEDIA_TYPE;
import static android.app.DownloadManager.COLUMN_URI;
import static android.content.Context.DOWNLOAD_SERVICE;
import static org.github.bademux.feedly.service.FeedlyCacheService.ACTION_DOWNLOAD_COMPLETED;
import static org.github.bademux.feedly.service.FeedlyCacheService.ACTION_DOWNLOAD_COMPLETED_FAVICON;

public class FeedlyBroadcastReceiver extends BroadcastReceiver {

  @Override
  public void onReceive(final Context context, final Intent intent) {
    if (intent == null) {
      return;
    }
    String action = intent.getAction();
    if (action == null) {
      return;
    }
    if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
      long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
      if (downloadId == -1) {
        return;
      }
      DownloadManager dm = (DownloadManager) context.getSystemService(DOWNLOAD_SERVICE);
      Cursor c = dm.query(new DownloadManager.Query().setFilterById(downloadId));
      if (c.moveToFirst()) {
        int status = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
        if (status == DownloadManager.STATUS_SUCCESSFUL) {

          String desc = c.getString(c.getColumnIndex(COLUMN_DESCRIPTION));
          boolean type = desc != null && desc.startsWith(Feed.PREFIX);
          String actionDl = type ? ACTION_DOWNLOAD_COMPLETED_FAVICON : ACTION_DOWNLOAD_COMPLETED;

          Intent intentDl = new Intent(actionDl, null, context, FeedlyCacheService.class);
          intentDl.putExtras(createBundle(c.getString(c.getColumnIndex(COLUMN_URI)),
                                          c.getString(c.getColumnIndex(COLUMN_LOCAL_FILENAME)),
                                          c.getString(c.getColumnIndex(COLUMN_MEDIA_TYPE)),
                                          c.getString(c.getColumnIndex(COLUMN_LOCAL_URI)), desc));
          context.startService(intentDl);
        }
      }
    } else {
      new FeedlyServiceManager(context).process(intent);
    }
  }

  private static Bundle createBundle(final String url, final String filename, final String mime,
                                     final String localUri, final String feedId) {
    Bundle bundle = new Bundle(5);
    bundle.putString(FeedlyCacheService.EXTRA_URL, url);
    bundle.putString(FeedlyCacheService.EXTRA_FILENAME, filename);
    bundle.putString(FeedlyCacheService.EXTRA_MIME, mime);
    bundle.putString(FeedlyCacheService.EXTRA_LOCAL_URI, localUri);
    bundle.putString(FeedlyCacheService.EXTRA_FEED_ID, feedId);
    return bundle;
  }

  public class FeedlyServiceManager extends ServiceManager {

    public FeedlyServiceManager(final Context context) {
      super(context, FeedlyCacheService.class);
    }

    @Override
    public int interval(final Status status) {
//      int interval = isPowerConnected ? DEFAULT_PERIOD * 2 : DEFAULT_PERIOD;
//      if (isNetworkAvailable && mConnectivityManager != null) {
//        int networkType = mConnectivityManager.getActiveNetworkInfo().getType();
//        if (networkType == ConnectivityManager.TYPE_MOBILE) {
//          interval = interval / 2;
//        }
//      }
      return status.interval;
    }

    @Override
    public boolean shouldSchedule(final Status status) {
      //return (status.isBatteryOk || status.isPowerConnected) && status.isNetworkAvailable;
      return false;
    }
  }
}