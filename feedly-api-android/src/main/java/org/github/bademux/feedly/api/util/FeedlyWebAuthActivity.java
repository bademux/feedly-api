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

package org.github.bademux.feedly.api.util;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class FeedlyWebAuthActivity extends Activity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    //setup fullscreen
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                         WindowManager.LayoutParams.FLAG_FULLSCREEN);

    mProgressDialog = new ProgressDialog(this);

    WebView webView = createWebView(this);
    setContentView(webView);
    webView.loadUrl(getIntent().getData().getSchemeSpecificPart());
  }

  private WebView createWebView(final Context context) {
    WebView webView = new WebView(context);
    webView.setWebViewClient(createWebViewClient());
    webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
    webView.setVisibility(View.VISIBLE);
    webView.getSettings().setJavaScriptEnabled(true);
    webView.getSettings().setLoadWithOverviewMode(true);
    webView.getSettings().setSupportZoom(true);
    webView.getSettings().setBuiltInZoomControls(false);
    return webView;
  }

  private WebViewClient createWebViewClient() {
    return new WebViewClient() {
      @Override
      public void onPageStarted(WebView view, String url, Bitmap fav) { mProgressDialog.show(); }

      @Override
      public void onPageFinished(WebView view, String url) { mProgressDialog.dismiss(); }

      @Override
      public boolean shouldOverrideUrlLoading(WebView view, String url) {
        mProgressDialog.dismiss();
        if (url.startsWith(REDIRECT_URI_LOCAL) || url.startsWith(REDIRECT_URN)) {
          setResult(Activity.RESULT_OK, FeedlyWebAuthActivity.this.fillWithReturnValue(url));
          finish();
          return true;
        }
        return false;
      }
    };
  }

  protected Intent fillWithReturnValue(final String responseUrl) {
    return getIntent().putExtra(RESPONSE_URL_TAG, responseUrl);
  }

  /**
   * Util Method - helps to prepare auth intent
   *
   * @param target     - activity that handles response
   * @param requestUrl - auth data
   */
  public static void startActivityForResult(final Activity target, final String requestUrl) {
    Uri data = Uri.fromParts("feedlyauth", requestUrl, null);
    Intent intend = new Intent(ACTION_FEEDLY_AUTH, data, target, FeedlyWebAuthActivity.class);
    target.startActivityForResult(intend, REQUEST_CODE);
  }

  /**
   * Util Method - helps to extract Access code from response intent
   *
   * @return access code
   */
  public static String getResponceUrl(final Intent responseIntent) {
    return responseIntent.getStringExtra(RESPONSE_URL_TAG);
  }

  private ProgressDialog mProgressDialog;

  public static final String ACTION_FEEDLY_AUTH = "org.github.bademux.feedly.api.util.FEEDLY_AUTH";

  public final static int REQUEST_CODE = 0;

  public final static String RESPONSE_URL_TAG = "ACCESS_CODE";

  public static final String REDIRECT_URI_LOCAL = "http://localhost";

  public static final String REDIRECT_URN = "urn:ietf:wg:oauth:2.0:oob";
}
