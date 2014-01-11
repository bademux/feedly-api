/*
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

package org.github.bademux.feedly.service;

import android.app.IntentService;
import android.content.ContentProviderOperation;
import android.content.Intent;
import android.util.Log;

import org.github.bademux.feedly.andrss.R;
import org.github.bademux.feedly.api.model.Subscription;
import org.github.bademux.feedly.api.provider.FeedlyContract;
import org.github.bademux.feedly.api.service.CacheServiceManager;
import org.github.bademux.feedly.api.util.FeedlyUtil;
import org.github.bademux.feedly.api.util.db.QueryHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Boolean.TRUE;
import static org.github.bademux.feedly.api.service.CacheServiceManager.Configurator;
import static org.github.bademux.feedly.api.util.db.FeedlyDbUtils.prepareInsertOperations;

/**
 * Feedly background service service - fetches new data database. Runned in separete process.
 * Service started on boot by the {@link org.github.bademux.feedly.api.service.StartupIntentReceiver}
 * Service woken by various events {@link org.github.bademux.feedly.api.service.CacheServiceManager}
 */
public class FeedlyCacheService extends IntentService {

  public final static int DEFAULT_PERIOD = 15;

  @Override
  public void onCreate() {
    super.onCreate();
    Log.i(TAG, "Service created");

    try {
      mFeedlyUtil = new FeedlyUtil(this, getString(R.string.client_id),
                                   getString(R.string.client_secret));
    } catch (IOException e) {
      Log.e(TAG, "Something goes wrong", e);
      throw new IllegalStateException(e);
    }

    mQueryHandler = new QueryHandler(getContentResolver());

    // register actions
    mManager = new CacheServiceManager(this, mConfigurator);
  }

  @Override
  protected void onHandleIntent(final Intent intent) {
    Log.i(TAG, "onStartCommand " + intent.getAction());
    if (CacheServiceManager.ACTION_REFRESH.equals(intent.getAction())) {
      Log.i(TAG, "startPooling - " + intent.getAction());
//      startPooling();
      return;
    }
  }

  /** Pools data from feedly servers and stores it in database */
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

  private final Configurator mConfigurator = new CacheServiceManager.Configurator() {
    @Override
    public int interval() { return TRUE.equals(isCharging) ? DEFAULT_PERIOD * 2 : DEFAULT_PERIOD; }

    //TODO: implement
    @Override
    public boolean shouldRefresh() { return TRUE.equals(isCharged) && TRUE.equals(isConnected); }
  };

  private volatile FeedlyUtil mFeedlyUtil;

  private volatile QueryHandler mQueryHandler;

  private volatile CacheServiceManager mManager;

  private static final String TAG = "FeedlyCacheService";
}