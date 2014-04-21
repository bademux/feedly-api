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

import com.google.api.client.util.IOUtils;

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
import org.github.bademux.feedly.api.model.Entry;
import org.github.bademux.feedly.api.model.Feed;
import org.github.bademux.feedly.api.model.Stream;
import org.github.bademux.feedly.api.model.Subscription;
import org.github.bademux.feedly.api.service.Feedly;
import org.github.bademux.feedly.api.service.ServiceManager;
import org.github.bademux.feedly.api.service.Utils;
import org.github.bademux.feedly.api.util.FeedlyUtil;
import org.github.bademux.feedly.api.util.db.FeedlyDbUtils;
import org.github.bademux.feedly.provider.FeedlyCacheProvider;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static android.app.DownloadManager.Request;
import static com.google.api.client.repackaged.com.google.common.base.Strings.isNullOrEmpty;
import static org.github.bademux.feedly.api.model.Category.ALL;
import static org.github.bademux.feedly.api.provider.FeedlyContract.Entries;
import static org.github.bademux.feedly.api.provider.FeedlyContract.Feeds;
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
      case ACTION_FETCH_ENTRIES:
        String streamId = intent.getStringExtra(EXTRA_STREAM_ID);

        fetchEntries(contentResolver, streamId);

      case ACTION_DOWNLOAD: downloadFiles(contentResolver); downloadFavicon(contentResolver); break;
      case ACTION_DOWNLOAD_COMPLETED: completeDownload(intent, contentResolver); break;
      case ACTION_DOWNLOAD_COMPLETED_FAVICON: completeDownloadFav(intent, contentResolver); break;
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

  protected void fetchEntries(final ContentResolver contentResolver, final String streamId) {

    Feedly service = mFeedlyUtil.service();
    Stream stream = isNullOrEmpty(streamId) ?
                    service.newCategory(ALL) :
                    new Stream() {
                      @Override
                      public String getId() { return streamId; }
                    };

    Feedly.Streams.Contents request = service.streams().contents(stream);

    //check local db
    Cursor cursor = contentResolver.query(Entries.CONTENT_URI,
                                          new String[]{"MAX(" + Entries.CRAWLED + ")"},
                                          null, null, null, null);
    if (cursor.moveToFirst()) { //fetch latest
      request.setNewerThan(cursor.getLong(1));
    }
    cursor.close();

    processEntries(contentResolver, execute(request));
  }

  private Collection<Entry> execute(final Feedly.Streams.Contents request) {
    final Collection<Entry> entries = new ArrayList<Entry>();
    do {
      //TODO: for dev purposes
      if (entries.size() > 20) { break; }

      try {
        EntriesResponse result = request.execute();
        request.setContinuation(result.getContinuation());
        if (result != null && !result.isEmpty()) {
          entries.addAll(result.items());
        }
      } catch (IOException e) {
        Log.e(TAG, "unknown action", e);
        request.setContinuation(null);
      }
    } while (!isNullOrEmpty(request.getContinuation()));
    return entries;
  }

  /**
   * Get all files that were not cached
   */
  private void downloadFavicon(final ContentResolver contentResolver) {
    Uri notcachedUri = Feeds.CONTENT_URI.buildUpon()
                                        .appendPath(FeedlyCacheProvider.FEEDS_EMPTY_FAVICON)
                                        .build();
    Cursor c = contentResolver.query(notcachedUri, null, null, null, null);
    if (c.moveToFirst()) {
      DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
      do {
        String feedId = c.getString(c.getColumnIndex(Feeds.ID));
        String website = c.getString(c.getColumnIndex(Feeds.WEBSITE));
        //see org.github.bademux.feedly.api.model.Feed#getUrl() for more details
        String url = website == null ? feedId.substring(Feed.PREFIX.length() + 1) : website;
        Uri uri = Uri.parse(FeedlyDbUtils.FAVICON_TPL + Uri.parse(url).getHost());

        dm.enqueue(new Request(uri)
                       //.setDestinationUri(Uri.fromFile(getCacheDir()))
                       .setVisibleInDownloadsUi(false).setDescription(feedId)
                       .setNotificationVisibility(Request.VISIBILITY_HIDDEN));
      } while (c.moveToNext());
    }
  }

  /**
   * update favicons
   */
  private void downloadFiles(final ContentResolver contentResolver) {
    Uri notcachedUri = Files.CONTENT_URI.buildUpon()
                                        .appendPath(FeedlyCacheProvider.FILES_NOT_CACHED).build();
    Cursor c = contentResolver.query(notcachedUri, null, null, null, null);
    if (c.moveToFirst()) {
      DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
      File cacheDir = Utils.getCacheDir(getApplicationContext());

      do {
        Uri srcUri = Uri.parse(c.getString(c.getColumnIndex(Files.URL)));
        String fileId = String.valueOf(c.getLong(c.getColumnIndex(Files.ID)));
        dm.enqueue(new Request(srcUri).setDestinationUri(Uri.fromFile(new File(cacheDir, fileId)))
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

  protected void completeDownloadFav(final Intent intent, final ContentResolver contentResolver) {
    Uri faviconUri = Uri.parse(intent.getStringExtra(FeedlyCacheService.EXTRA_LOCAL_URI));
    String feedId = intent.getStringExtra(FeedlyCacheService.EXTRA_FEED_ID);
    try {
      ContentValues values = new ContentValues(1);
      values.put(Feeds.FAVICON, readAll(contentResolver, faviconUri));
      contentResolver.update(Feeds.CONTENT_URI, values, Feeds.ID + "=?", new String[]{feedId});
    } catch (IOException e) {
      Log.e(TAG, "Can't copy", e);
    }
  }

  protected static byte[] readAll(final ContentResolver contentResolver, final Uri src)
      throws IOException {
    ByteArrayOutputStream stream = new ByteArrayOutputStream();
    try {
      IOUtils.copy(contentResolver.openInputStream(src), stream, true);
      return stream.toByteArray();
    } finally {
      if (stream != null) {
        stream.close();
      }
    }
  }


  public FeedlyCacheService() { super(TAG); }

  private volatile FeedlyUtil mFeedlyUtil;

  public final static String ACTION_FETCH_SUBSCRIPTION =
      "org.github.bademux.feedly.api.service.Subscription";

  public final static String ACTION_FETCH_ENTRIES = "org.github.bademux.feedly.api.service.Entries";

  public final static String EXTRA_STREAM_ID =
      "org.github.bademux.feedly.api.service.EXTRA_STREAM_ID";

  protected final static String ACTION_DOWNLOAD = "org.github.bademux.feedly.api.service.Download";

  protected final static String ACTION_DOWNLOAD_COMPLETED_FAVICON =
      "org.github.bademux.feedly.api.service.DownloadCompletedFavicon";

  protected final static String ACTION_DOWNLOAD_COMPLETED =
      "org.github.bademux.feedly.api.service.DownloadCompleted";

  protected final static String EXTRA_URL = "org.github.bademux.feedly.api.service.EXTRA_URL";

  protected final static String EXTRA_MIME = "org.github.bademux.feedly.api.service.EXTRA_MIME";

  protected static final String EXTRA_FILENAME =
      "org.github.bademux.feedly.api.service.EXTRA_FILENAME";

  protected final static String EXTRA_LOCAL_URI =
      "org.github.bademux.feedly.api.service.EXTRA_LOCAL_URI";

  protected final static String EXTRA_FEED_ID =
      "org.github.bademux.feedly.api.service.EXTRA_FEED_ID";

  static final String TAG = "FeedlyCacheService";
}