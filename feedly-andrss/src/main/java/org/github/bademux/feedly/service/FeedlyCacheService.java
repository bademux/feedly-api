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

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.IBinder;
import android.util.Log;

import org.github.bademux.feedly.andrss.R;
import org.github.bademux.feedly.api.model.Subscription;
import org.github.bademux.feedly.api.provider.FeedlyContract;
import org.github.bademux.feedly.api.util.FeedlyUtil;
import org.github.bademux.feedly.api.util.db.QueryHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static org.github.bademux.feedly.api.util.db.FeedlyDbUtils.prepareInsertOperations;

public class FeedlyCacheService extends Service {

  private final static String ACTION_ALARM = "FeedlyAlarm";
  private final static int DEFAULT_PERIOD = 30;

  @Override
  public void onCreate() {
    Log.i(TAG, "Service created");

    try {
      mFeedlyUtil = new FeedlyUtil(this, getString(R.string.client_id),
                                   getString(R.string.client_secret));
    } catch (IOException e) {
      Log.e(TAG, "Service created");
      return;
    }

    mQueryHandler = new QueryHandler(getContentResolver());

    // register actions
    registerReceiver(myBroadcastReceiver, new IntentFilter() {{
      addAction(Intent.ACTION_POWER_CONNECTED);
      addAction(Intent.ACTION_POWER_DISCONNECTED);
      addAction(Intent.ACTION_BATTERY_OKAY);
      addAction(Intent.ACTION_BATTERY_LOW);
      addAction(Intent.ACTION_BATTERY_CHANGED);
      addAction(Intent.ACTION_DEVICE_STORAGE_OK);
      addAction(Intent.ACTION_DEVICE_STORAGE_LOW);
      addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);
      addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);
      addAction(ConnectivityManager.CONNECTIVITY_ACTION);
    }});

    mAlarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

    schedule(DEFAULT_PERIOD);
  }

  /**
   * Starts service every ~period second, service will not wake up the system
   *
   * @param period in seconds
   */
  public void schedule(int period) {
    Intent intent = new Intent(this, FeedlyCacheService.class);
    intent.setAction(ACTION_ALARM);
    PendingIntent mPendingIntent = PendingIntent.getService(this, 0, intent,
                                                            PendingIntent.FLAG_UPDATE_CURRENT);
    long now = Calendar.getInstance().getTimeInMillis();
    mAlarmManager.setInexactRepeating(AlarmManager.RTC, now, period * 1000, mPendingIntent);
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    Log.i(TAG, "onStartCommand " + intent.getAction() + " flags:" + flags + " startId:" + startId);
    if(ALARM_SERVICE.equals(intent.getAction())){
//      startPooling();
    }
    return START_STICKY; // If we get killed, after returning from here, restart
  }

  /**
   * Pools data from feedly servers and stores it in database
   */
  protected void startPooling() throws Exception {
    Log.i(TAG, "onStartCommand");
    List<Subscription> subscriptions = mFeedlyUtil.service().subscriptions().list().execute();
    ArrayList<ContentProviderOperation> operations = prepareInsertOperations(subscriptions);
    getContentResolver().applyBatch(FeedlyContract.AUTHORITY, operations);
  }

  public void onBroadcastReceive(Intent intent) {
    Log.i(TAG, "Broadcast received: " + intent.getAction());

//    switch (intent.getAction()) {
//      case Intent.ACTION_POWER_CONNECTED: schedule(DEFAULT_PERIOD * 2); break;
//      case Intent.ACTION_POWER_DISCONNECTED: schedule(DEFAULT_PERIOD); break;
//      case Intent.ACTION_BATTERY_OKAY: schedule(DEFAULT_PERIOD); break;
//      // case Intent.ACTION_BATTERY_LOW: mPendingIntent.cancel(); break;
//      case ConnectivityManager.CONNECTIVITY_ACTION:
//        if (intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false)) {
//          schedule(DEFAULT_PERIOD);
//        } else {
//          //mPendingIntent.cancel();
//        }
//        break;
//    }
  }

  private FeedlyUtil mFeedlyUtil;

  private AlarmManager mAlarmManager;

  private QueryHandler mQueryHandler;

  private final BroadcastReceiver myBroadcastReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(final Context context, final Intent intent) {onBroadcastReceive(intent);}
  };

  @Override
  public void onDestroy() { unregisterReceiver(myBroadcastReceiver); }

  @Override
  public IBinder onBind(Intent intent) { return null; }

  private static final String TAG = "FeedlyCacheService";
}