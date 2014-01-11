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

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;


/**
 * Provide service class name by subclassing and configure with AndroidManifest.xml
 * <application ... >
 * <receiver android:name="my.subclass.of.StartupIntentReceiver">
 * <intent-filter>
 * <action android:name="android.intent.action.BOOT_COMPLETED"/>
 * </intent-filter>
 * </receiver>
 * ...
 * </application>
 */
public abstract class StartupIntentReceiver extends BroadcastReceiver {

  public StartupIntentReceiver(final Class<? extends Service> serviceClass) {
    this.serviceClass = serviceClass;
  }

  @Override
  public void onReceive(Context context, Intent intent) {
    if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
      if (Log.isLoggable(TAG, Log.DEBUG)) {
        Log.d(TAG, "Started on boot");
      }
      context.startService(new Intent(context, serviceClass));
    }
  }

  private final Class<? extends Service> serviceClass;

  private static final String TAG = "StartupIntentReceiver";
}