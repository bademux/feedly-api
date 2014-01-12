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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.github.bademux.feedly.api.service.ServiceManager;

public class FeedlyBroadcastReceiver extends BroadcastReceiver {

  @Override
  public void onReceive(final Context context, final Intent intent) {
    if (intent != null && intent.getAction() != null) {
      new FeedlyServiceManager(context).process(intent);
    }
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
      return (status.isBatteryOk || status.isPowerConnected) && status.isNetworkAvailable;
    }
  }
}