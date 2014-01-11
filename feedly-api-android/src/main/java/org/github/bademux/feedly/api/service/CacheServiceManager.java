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

package org.github.bademux.feedly.api.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.util.Log;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

/**
 * TODO: complete doc
 * Manages reaction on various events, trying to be as silent as possible.
 * Service woken by:
 * - connectivity event
 * - timer event (occurs when connectivity is present)
 * ...
 */
public class CacheServiceManager {

  public final static String ACTION_REFRESH = "Refresh";

  protected void onBroadcastReceive(final Intent intent) {
    Log.i(TAG, "Broadcast received: " + intent.getAction());

    switch (intent.getAction()) {
      case Intent.ACTION_SHUTDOWN: mConfigurator.isShutdowning = TRUE; break;
      case Intent.ACTION_DEVICE_STORAGE_OK: mConfigurator.isStorageOk = TRUE; break;
      case Intent.ACTION_DEVICE_STORAGE_LOW: mConfigurator.isStorageOk = FALSE; break;
      case Intent.ACTION_POWER_CONNECTED: mConfigurator.isCharging = TRUE; schedule(); break;
      case Intent.ACTION_POWER_DISCONNECTED: mConfigurator.isCharging = FALSE; break;
      case Intent.ACTION_BATTERY_CHANGED:
//        int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
//        int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
//        float battPct = level/(float)scale;
//        mConfigurator.isCharged =
        break;
      case Intent.ACTION_AIRPLANE_MODE_CHANGED:
        mConfigurator.isConnected = intent.getExtras().getBoolean("state", false);
        break;
      case ConnectivityManager.CONNECTIVITY_ACTION:
        mConfigurator.isConnected =
            intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
        break;
      default: return;
    }

    if (mConfigurator.shouldRefreshInternal()) {
      schedule();
    } else {
      cancelScheduled();
    }
  }

  /**
   * Starts service every ~interval second, service will not wake up the system
   */
  protected void schedule() {
    Log.i(TAG, "shouldRefresh");
    cancelScheduled();
    mAlarmManager.setInexactRepeating(AlarmManager.RTC, System.currentTimeMillis(),
                                      mConfigurator.interval() * 1000, mPendingIntent);
  }

  protected void cancelScheduled() {
    Log.i(TAG, "cancelScheduled");
    mAlarmManager.cancel(mPendingIntent);
  }

  public void unregister() { mContext.unregisterReceiver(mBroadcastReceiver); }

  public CacheServiceManager(final Service mContext, final Configurator mConfigurator) {
    this.mContext = mContext;
    this.mConfigurator = mConfigurator;

    mContext.registerReceiver(mBroadcastReceiver, INTENT_FILTER);

    mAlarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);

    Intent intent = new Intent(mContext, mContext.getClass());
    intent.setAction(ACTION_REFRESH);
    mPendingIntent = PendingIntent.getService(mContext, 0, intent, 0);
  }

  private final PendingIntent mPendingIntent;

  private final Context mContext;

  private final AlarmManager mAlarmManager;

  private final Configurator mConfigurator;

  private static final String TAG = "CacheServiceManager";

  private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(final Context context, final Intent intent) {
      if (intent != null && intent.getAction() != null) {
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

  public abstract static class Configurator {

    protected Boolean isShutdowning;
    protected Boolean isCharged;
    protected Boolean isCharging;
    protected Boolean isConnected;
    protected Boolean isStorageOk;
    private int interval;

    /**
     * if 0 is provided service will never scheduled to update.
     * Please override #interval() in this case
     */
    public Configurator(final int interval) { this.interval = interval; }

    /** The same as #Configurator(0) */
    public Configurator() { this(0); }

    /**
     * Can be overrode to define interval dynamically
     *
     * @return interval in seconds
     */
    public int interval() { return interval; }

    /**
     * Whether it should schedule service internal method
     *
     * @return true if service should be (re)scheduled
     */
    private boolean shouldRefreshInternal() { return interval != 0 && shouldRefresh(); }

    /**
     * Whether it should schedule service
     * Method is the extension point provided to change behaviour
     *
     * @return true if service should be (re)scheduled
     */
    public abstract boolean shouldRefresh();
  }
}