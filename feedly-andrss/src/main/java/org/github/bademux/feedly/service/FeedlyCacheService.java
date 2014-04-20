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

import android.app.DownloadManager;
import android.app.IntentService;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import org.github.bademux.feedly.andrss.R;
import org.github.bademux.feedly.api.model.EntriesResponse;
import org.github.bademux.feedly.api.model.Subscription;
import org.github.bademux.feedly.api.service.Feedly;
import org.github.bademux.feedly.api.service.ServiceManager;
import org.github.bademux.feedly.api.util.FeedlyUtil;
import org.github.bademux.feedly.provider.FeedlyCacheProvider;

import java.io.IOException;
import java.util.List;

import static android.app.DownloadManager.Request;
import static org.github.bademux.feedly.api.model.Category.ALL;
import static org.github.bademux.feedly.api.provider.FeedlyContract.Files;
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
    try {
      handleIntent(intent, getContentResolver());
    } catch (Exception e) {
      Log.e(TAG, "error while pooling", e);
    }
  }

  private void handleIntent(final Intent intent, final ContentResolver contentResolver)
      throws IOException {
    final String action = intent.getAction();
    if (action == null) {
      return;
    }
    switch (action) {
      case ACTION_FETCH_SUBSCRIPTION: fetchSubscriptions(contentResolver);
      case ServiceManager.ACTION_REFRESH:
      case ACTION_FETCH_ENTRIES: fetchEntries(contentResolver);
      case ACTION_DOWNLOAD: download(contentResolver); break;
      case ACTION_DOWNLOAD_COMPLETED: completeDownload(intent, contentResolver); break;
      default:
        Log.d(TAG, "unknown action" + action);
    }
  }

  protected void fetchSubscriptions(final ContentResolver contentResolver) throws IOException {
    List<Subscription> subscriptions = mFeedlyUtil.service().subscriptions().list().execute();
    if (subscriptions != null) {
      processSubscriptions(contentResolver, subscriptions);
    }
  }

  protected void fetchEntries(final ContentResolver contentResolver) throws IOException {
    Feedly service = mFeedlyUtil.service();
    Feedly.Streams.Contents request = service.streams().contents(service.newCategory(ALL));
    EntriesResponse result = request.execute();
    if (result != null) {
      processEntries(contentResolver, result.items());
    }
  }

  protected void download(final ContentResolver contentResolver) {
    //Get all files that were not cached
    Uri notcachedUri = Files.CONTENT_URI.buildUpon()
                                        .appendPath(FeedlyCacheProvider.NOTCACHED).build();
    Cursor c = contentResolver.query(notcachedUri, null, null, null, null);
    if (c.moveToFirst()) {
      DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
      String cacheDir = "file://" + FeedlyCacheProvider.getCacheDir(getApplicationContext()) + '/';
      do {
        Uri srcUri = Uri.parse(c.getString(c.getColumnIndex(Files.URL)));
        String fileId = String.valueOf(c.getLong(c.getColumnIndex(Files.ID)));
        dm.enqueue(new Request(srcUri).setDestinationUri(Uri.parse(cacheDir + fileId))
                                      .setVisibleInDownloadsUi(false)
                                      .setNotificationVisibility(Request.VISIBILITY_HIDDEN));
      } while (c.moveToNext());
    }
  }

  protected void completeDownload(final Intent intent, final ContentResolver contentResolver) {
    String url = intent.getStringExtra(FeedlyCacheService.EXTRA_URL);
    ContentValues values = new ContentValues(2);
    values.put(Files.FILENAME, intent.getStringExtra(FeedlyCacheService.EXTRA_FILENAME));
    values.put(Files.MIME, intent.getStringExtra(FeedlyCacheService.EXTRA_MIME));
    Uri updateUri = Files.CONTENT_URI.buildUpon().appendPath(url).build();
    contentResolver.update(updateUri, values, null, null);
  }

  public FeedlyCacheService() { super(TAG); }

  private volatile FeedlyUtil mFeedlyUtil;

  public final static String ACTION_FETCH_SUBSCRIPTION =
      "org.github.bademux.feedly.api.service.Subscription";

  public final static String ACTION_FETCH_ENTRIES = "org.github.bademux.feedly.api.service.Entries";

  protected final static String ACTION_DOWNLOAD = "org.github.bademux.feedly.api.service.Download";

  protected final static String ACTION_DOWNLOAD_COMPLETED =
      "org.github.bademux.feedly.api.service.DownloadCompleted";

  protected final static String EXTRA_URL = "org.github.bademux.feedly.api.service.EXTRA_URL";

  protected final static String EXTRA_MIME = "org.github.bademux.feedly.api.service.EXTRA_MIME";

  protected static final String EXTRA_FILENAME =
      "org.github.bademux.feedly.api.service.EXTRA_FILENAME";

  static final String TAG = "FeedlyCacheService";
}