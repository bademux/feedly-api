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

package org.github.bademux.feedly.api.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.util.Log;

import java.io.Serializable;

/**
 * TODO: complete doc
 * Manages reaction on various events, trying to be as silent as possible.
 * Service woken by:
 * - connectivity event
 * - timer event (occurs when connectivity is present)
 * ...
 */
public abstract class ServiceManager {

  public boolean process(final Intent intent) {
    Log.e(TAG, "Broadcast received: " + intent.getAction());

    switch (intent.getAction()) {
      case Intent.ACTION_BATTERY_OKAY:
        mStatus.isBatteryOk = true; break;
      case Intent.ACTION_BATTERY_LOW:
        mStatus.isBatteryOk = false; break;
      case Intent.ACTION_POWER_CONNECTED:
        mStatus.isPowerConnected = true; break;
      case Intent.ACTION_POWER_DISCONNECTED:
        mStatus.isPowerConnected = false; break;
      case Intent.ACTION_AIRPLANE_MODE_CHANGED:
        mStatus.isNetworkAvailable = intent.getBooleanExtra("state", false);
        break;
      case ConnectivityManager.CONNECTIVITY_ACTION:
        mStatus.isNetworkAvailable =
            intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
        break;
      case Intent.ACTION_BOOT_COMPLETED: case ACTION_INIT: Utils.initAll(mContext, mStatus); break;
      default: return false;
    }

    Utils.store(mContext, mStatus);

    Log.e(TAG, "schedule: " + mStatus);

    if (shouldScheduleInternal(mStatus)) {
      schedule();
    } else {
      cancelScheduled();
    }
    return true;
  }

  public void schedule() {
    mAlarmManager.setInexactRepeating(AlarmManager.RTC, System.currentTimeMillis(),
                                      interval(mStatus) * 1000,
                                      createPendingIntent());
  }

  public boolean cancelScheduled() {
    PendingIntent pendingIntent = getPendingIntent();
    if (pendingIntent == null) {
      return false;
    }
    mAlarmManager.cancel(pendingIntent);
    pendingIntent.cancel();
    return true;
  }

  public boolean isScheduled() { return null != getPendingIntent(); }

  protected Intent createIntent() {
    Intent intent = new Intent(mContext, mClazz);
    intent.setAction(ACTION_REFRESH);
    return intent;
  }

  protected PendingIntent createPendingIntent() {
    Intent intent = createIntent();
    return PendingIntent.getService(mContext, REQUEST_CODE, intent,
                                    PendingIntent.FLAG_UPDATE_CURRENT);
  }

  protected PendingIntent getPendingIntent() {
    return PendingIntent.getService(mContext, REQUEST_CODE, createIntent(),
                                    PendingIntent.FLAG_NO_CREATE);
  }

  protected Status createStatus() {
    Status status = new Status();
    Utils.initAll(mContext, mStatus);
    return status;
  }

  private final boolean shouldScheduleInternal(final Status status) {
    return status.interval != 0 && shouldSchedule(status);
  }

  /**
   * Whether it should schedule service
   *
   * @return true if service should be (re)scheduled
   */
  public abstract boolean shouldSchedule(final Status status);

  /**
   * Can be overrode to define interval dynamically
   *
   * @return interval in seconds
   */
  public int interval(final Status status) { return status.interval; }

  public ServiceManager(final Context context, final Class<? extends Service> clazz) {
    this.mContext = context;
    this.mClazz = clazz;
    Status status = Utils.load(mContext);
    this.mStatus = status == null ? createStatus() : status;
    mAlarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
  }

  private final Status mStatus;

  private final Context mContext;

  private final Class<? extends Service> mClazz;

  private final AlarmManager mAlarmManager;

  private static final int REQUEST_CODE = 1858946154;

  public final static String ACTION_REFRESH = "org.github.bademux.feedly.api.service.Refresh";

  public static final String ACTION_INIT = "org.github.bademux.feedly.api.service.ACTION_INIT";

  static final String TAG = "ServiceManager";

  protected static final class Status implements Serializable {

    public String toString() {
      StringBuffer sb = new StringBuffer("Status:\n")
          .append("interval:           ").append(interval).append("\n")
          .append("isBatteryOk:        ").append(isBatteryOk).append("\n")
          .append("isPowerConnected:   ").append(isPowerConnected).append("\n")
          .append("isNetworkAvailable: ").append(isNetworkAvailable).append("\n")
          .append("isStorageOk:        ").append(isStorageOk);
      return sb.toString();
    }

    private Status() {}

    public boolean isStorageOk;
    public boolean isNetworkAvailable;
    public boolean isBatteryOk;
    public boolean isPowerConnected;
    public int interval = DEFAULT_INTERVAL;

    static final long serialVersionUID = 44212L;

    public final static int DEFAULT_INTERVAL = 30;
  }
}