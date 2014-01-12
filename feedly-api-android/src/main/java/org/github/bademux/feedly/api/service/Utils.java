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

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.BatteryManager;
import android.util.Log;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import static org.github.bademux.feedly.api.service.ServiceManager.Status;

public final class Utils {

  public static void store(final Context context, Status status) {
    ObjectOutputStream out = null;
    try {
      out = new ObjectOutputStream(context.openFileOutput(STATUS_TMP, Context.MODE_PRIVATE));
      out.writeObject(status);
    } catch (IOException e) {
      Log.e(ServiceManager.TAG, "can't write status file", e);
    } finally {
      if (out != null) {
        try {
          out.close();
        } catch (IOException e) {
          Log.e(ServiceManager.TAG, "", e);
        }
      }
    }
  }

  public static Status load(final Context context) {
    ObjectInput in = null;
    try {
      in = new ObjectInputStream(context.openFileInput(STATUS_TMP));
      return (Status) in.readObject();
    } catch (FileNotFoundException e) {
      //ignore and creare new one
    } catch (IOException e) {
      Log.e(ServiceManager.TAG, "can't read status file", e);
    } catch (ClassNotFoundException e) {
      Log.e(ServiceManager.TAG, "", e);
    } finally {
      if (in != null) {
        try {
          in.close();
        } catch (IOException e) {
          Log.e(ServiceManager.TAG, "", e);
        }
      }
    }

    return null;
  }

  public static void initAll(final Context context, final Status status) {
    initNetworkStatus(context, status);
    initBatteryStatus(context, status);
  }

  public static void initNetworkStatus(final Context context, final Status status) {
    Intent intent = context.registerReceiver(null,
                                             new IntentFilter(
                                                 ConnectivityManager.CONNECTIVITY_ACTION));
    if (intent == null) {
      return;
    }

    status.isNetworkAvailable =
        !intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
  }

  public static void initBatteryStatus(final Context context, final Status status) {
    Intent intent = context.registerReceiver(null,
                                             new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    if (intent == null) {
      return;
    }

    final int state = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
    status.isPowerConnected = state == BatteryManager.BATTERY_STATUS_CHARGING
                              || state == BatteryManager.BATTERY_STATUS_FULL;

    final int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
    final int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
    if (level > 0 && scale > 0) {
      status.isBatteryOk = (level / (float) scale) * 100 > BATTERY_LOW;
    }
  }

  public final static int BATTERY_LOW = 15;

  private static final String STATUS_TMP = "status.tmp";

  private Utils() {}
}
