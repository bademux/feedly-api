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

import android.app.IntentService;
import android.content.ContentProviderOperation;
import android.content.Intent;
import android.util.Log;

import org.github.bademux.feedly.andrss.R;
import org.github.bademux.feedly.api.model.Subscription;
import org.github.bademux.feedly.api.provider.FeedlyContract;
import org.github.bademux.feedly.api.util.FeedlyUtil;
import org.github.bademux.feedly.api.util.db.QueryHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.github.bademux.feedly.api.util.db.FeedlyDbUtils.prepareInsertOperations;

/**
 * Feedly background service service - fetches new data database. Runned in separete process.
 * Service started on boot by the {@link StartupIntentReceiver}
 * Service woken by various events {@link CacheServiceManager}
 */
public class FeedlyCacheService extends IntentService {

  public final static String ACTION_REFRESH = "Refresh";

  @Override
  public void onCreate() {
    super.onCreate();
    Log.i(TAG, "Service created");

    try {
      mFeedlyUtil = new FeedlyUtil(this, getString(R.string.client_id),
                                   getString(R.string.client_secret));
    } catch (IOException e) {
      Log.e(TAG, "Something goes wrong", e);
      return;
    }

    mQueryHandler = new QueryHandler(getContentResolver());

    // register actions
    mManager = new CacheServiceManager(this, ACTION_REFRESH);
  }

  @Override
  protected void onHandleIntent(final Intent intent) {
    Log.i(TAG, "onStartCommand " + intent.getAction());
    if (ACTION_REFRESH.equals(intent.getAction())) {
//      startPooling();
      return;
    }
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

  @Override
  public void onDestroy() {
    super.onDestroy();
    mManager.unregister();
  }

  public FeedlyCacheService() { super(TAG); }

  private volatile FeedlyUtil mFeedlyUtil;

  private volatile QueryHandler mQueryHandler;

  private volatile CacheServiceManager mManager;

  private static final String TAG = "FeedlyCacheService";
}