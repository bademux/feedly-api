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

import org.github.bademux.feedly.api.service.ServiceManager;

import static android.content.Context.DOWNLOAD_SERVICE;

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
          String uri = c.getString(c.getColumnIndex(DownloadManager.COLUMN_URI));
          String filename = c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME));
          String mime = c.getString(c.getColumnIndex(DownloadManager.COLUMN_MEDIA_TYPE));
          context.startService(createIntentDlComplete(context, uri, filename, mime));
        }
      }
    } else {
      new FeedlyServiceManager(context).process(intent);
    }
  }

  private static Intent createIntentDlComplete(final Context context, final String url,
                                               final String filename, final String mime) {
    Intent intent = new Intent(FeedlyCacheService.ACTION_DOWNLOAD_COMPLETED, null,
                               context, FeedlyCacheService.class);
    intent.putExtra(FeedlyCacheService.EXTRA_URL, url);
    intent.putExtra(FeedlyCacheService.EXTRA_FILENAME, filename);
    intent.putExtra(FeedlyCacheService.EXTRA_MIME, mime);
    return intent;
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