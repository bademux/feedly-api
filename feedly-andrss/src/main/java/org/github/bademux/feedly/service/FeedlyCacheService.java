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
import android.content.ContentResolver;
import android.content.Intent;
import android.util.Log;

import org.github.bademux.feedly.andrss.R;
import org.github.bademux.feedly.api.model.EntriesResponse;
import org.github.bademux.feedly.api.model.Subscription;
import org.github.bademux.feedly.api.service.Feedly;
import org.github.bademux.feedly.api.service.ServiceManager;
import org.github.bademux.feedly.api.util.FeedlyUtil;

import java.io.IOException;
import java.util.List;

import static org.github.bademux.feedly.api.model.Category.ALL;
import static org.github.bademux.feedly.api.util.db.FeedlyDbUtils.processEntries;
import static org.github.bademux.feedly.api.util.db.FeedlyDbUtils.processSubscriptions;

public class FeedlyCacheService extends IntentService {

  @Override
  public void onCreate() {
    Log.i(TAG, "Service created");
    try {
      mFeedlyUtil = new FeedlyUtil(this, getString(R.string.client_id),
                                   getString(R.string.client_secret));
    } catch (IOException e) {
      Log.e(TAG, "Something goes wrong", e);
      throw new IllegalStateException(e);
    }

    super.onCreate();
  }

  @Override
  protected void onHandleIntent(final Intent intent) {
    Log.i(TAG, "onStartCommand " + intent.getAction());
    ContentResolver contentResolver = getContentResolver();
    final String action = intent.getAction();
    try {
      switch (action) {
        case ACTION_FETCH_SUBSCRIPTION:
          List<Subscription> subscriptions = mFeedlyUtil.service().subscriptions().list().execute();
          if (subscriptions != null) {
            processSubscriptions(contentResolver, subscriptions);
          }
          break;
        case ACTION_FETCH_ENTRIES:
          Feedly service = mFeedlyUtil.service();
          Feedly.Streams.Contents request = service.streams().contents(service.newCategory(ALL));
          EntriesResponse result = request.execute();
          if (result != null) {
            processEntries(contentResolver, result.items());
          }
          break;
        case ACTION_DOWNLOAD_DATA:
          break;
        case ServiceManager.ACTION_REFRESH:
        default:
      }
    } catch (Exception e) {
      Log.e(TAG, "error while pooling", e);
    }
  }


  public FeedlyCacheService() { super(TAG); }

  private volatile FeedlyUtil mFeedlyUtil;

  public final static String ACTION_FETCH_SUBSCRIPTION =
      "org.github.bademux.feedly.api.service.Subscription";

  public final static String ACTION_FETCH_ENTRIES = "org.github.bademux.feedly.api.service.Entries";
  private final static String ACTION_DOWNLOAD_DATA =
      "org.github.bademux.feedly.api.service.DOWNLOAD";

  static final String TAG = "FeedlyCacheService";
}