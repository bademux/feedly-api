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

package org.github.bademux.feedly.api.util;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.github.bademux.feedly.api.oauth2.FeedlyOAuthConstants;

public class FeedlyWebAuthActivity extends Activity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    //setup fullscreen
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

    mProgressDialog = new ProgressDialog(this);
//    mProgressDialog.setTitle(getText(android.R.string.app_name));
//    mProgressDialog.setMessage(getText(android.R.string.msg_loading));

    WebView webView = createWebView(this);
    setContentView(webView);
    webView.loadUrl(getIntent().getStringExtra(REQUEST_URL_TAG));
  }

  private WebView createWebView(Context context) {
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
        if (url.startsWith(FeedlyOAuthConstants.REDIRECT_URI_LOCAL)
            || url.startsWith(FeedlyOAuthConstants.REDIRECT_URN)) {
          FeedlyWebAuthActivity.this.finish(url);
          return true;
        }
        return false;
      }
    };
  }

  protected void finish(String responseUrl) {
    Intent intent = getIntent();
    intent.putExtra(RESPONSE_URL_TAG, responseUrl);
    setResult(Activity.RESULT_OK, intent);
    finish();
  }

  public static void startActivityForResult(Activity target, String requestUrl, String state) {
    Intent intend = new Intent(target, FeedlyWebAuthActivity.class);
    intend.putExtra(REQUEST_URL_TAG, requestUrl);
    intend.putExtra(STATE_TAG, state);
    target.startActivityForResult(intend, FeedlyWebAuthActivity.REQUEST_CODE);
  }

  public static void startActivityForResult(Activity target, String requestUrl) {
    startActivityForResult(target, requestUrl, null);
  }

  public static String getResponceUrl(Intent responseIntent) {
    return responseIntent.getStringExtra(RESPONSE_URL_TAG);
  }

  private ProgressDialog mProgressDialog;

  public final static int REQUEST_CODE = 0;

  public final static String STATE_TAG = "STATE";

  public final static String RESPONSE_URL_TAG = "ACCESS_CODE";

  public final static String REQUEST_URL_TAG = "REQUEST_URL";
}
