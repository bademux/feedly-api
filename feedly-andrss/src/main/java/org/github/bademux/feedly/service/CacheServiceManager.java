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
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.util.Log;

/**
 * TODO: complete doc
 * Manages reaction on various events, trying to be as silent as possible.
 * Service woken by:
 * - connectivity event
 * - timer event (occurs when connectivity is present)
 * ...
 */
public class CacheServiceManager {

  public final static int DEFAULT_PERIOD = 15;

  protected void onBroadcastReceive(final Intent intent) {
    Log.i(TAG, "Broadcast received: " + intent.getAction());

    switch (intent.getAction()) {
      case Intent.ACTION_SHUTDOWN: solver.isShutdownling = true; break;
      case Intent.ACTION_DEVICE_STORAGE_OK: solver.isStorageOk = true; break;
      case Intent.ACTION_DEVICE_STORAGE_LOW: solver.isStorageOk = false; break;
      case Intent.ACTION_POWER_CONNECTED: solver.isCharging = true; break;
      case Intent.ACTION_POWER_DISCONNECTED: solver.isCharging = false; break;
      case Intent.ACTION_BATTERY_CHANGED:
//        int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
//        int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
//        float battPct = level/(float)scale;
//        solver.isCharged =
        break;
      case Intent.ACTION_AIRPLANE_MODE_CHANGED:
        solver.isConnected = intent.getExtras().getBoolean("state", false);
        break;
      case ConnectivityManager.CONNECTIVITY_ACTION:
        solver.isConnected = intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY,
                                                    false);
        break;
      default: return;
    }

    if (solver.shouldRefresh()) {
      schedule(DEFAULT_PERIOD);
    } else {
      cancelScheduled();
    }
  }

  /**
   * Starts service every ~period second, service will not wake up the system
   *
   * @param period in seconds
   */
  protected void schedule(int period) {
    mAlarmManager.setInexactRepeating(AlarmManager.RTC, System.currentTimeMillis(),
                                      period * 1000, mPendingIntent);
  }

  protected void cancelScheduled() {
    mAlarmManager.cancel(mPendingIntent);
  }

  public void unregister() { context.unregisterReceiver(mBroadcastReceiver); }

  public CacheServiceManager(final Service context, String action) {
    this.context = context;

    context.registerReceiver(mBroadcastReceiver, INTENT_FILTER);

    mAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

    Intent intent = new Intent(context, context.getClass());
    intent.setAction(action);
    mPendingIntent = PendingIntent.getService(context, 0, intent, 0);
  }

  private volatile PendingIntent mPendingIntent;

  private final Context context;

  private final AlarmManager mAlarmManager;

  private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(final Context context, final Intent intent) {
      if (intent != null && intent.getAction() == null) {
        onBroadcastReceive(intent);
      }
    }
  };

  private static final IntentFilter INTENT_FILTER = new IntentFilter() {{
    addAction(Intent.ACTION_SHUTDOWN);
    addAction(Intent.ACTION_POWER_CONNECTED);
    addAction(Intent.ACTION_POWER_DISCONNECTED);
    addAction(Intent.ACTION_BATTERY_CHANGED);
    addAction(Intent.ACTION_DEVICE_STORAGE_OK);
    addAction(Intent.ACTION_DEVICE_STORAGE_LOW);
    addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);
    addAction(ConnectivityManager.CONNECTIVITY_ACTION);
  }};

  private static final String TAG = "CacheServiceManager";

  private final Solver solver = new Solver();

  protected class Solver {

    Boolean isShutdownling;
    Boolean isCharged;
    Boolean isCharging;
    Boolean isConnected;
    Boolean isStorageOk;

    boolean shouldRefresh() {
      //TODO: implement
      return Boolean.TRUE.equals(isCharged) && Boolean.TRUE.equals(isConnected);
    }
  }
}