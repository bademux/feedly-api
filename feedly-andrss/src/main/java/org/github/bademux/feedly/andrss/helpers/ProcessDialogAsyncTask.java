/*
 * Copyright 2013 Bademus.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

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

package org.github.bademux.feedly.andrss.helpers;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.widget.Toast;

import org.github.bademux.feedly.andrss.R;

public abstract class ProcessDialogAsyncTask  extends AsyncTask<Void, Void, Void> {

  public ProcessDialogAsyncTask(final Activity activity) {
    this.activity = activity;
    mProgressDialog = new ProgressDialog(activity);
    mProgressDialog.setTitle(activity.getText(R.string.app_name));
    mProgressDialog.setMessage(activity.getString(R.string.msg_loading));
  }

  @Override
  protected Void doInBackground(final Void... params) {
    doInBackground();
    return null;
  }

  protected void doInBackground() {}

  @Override
  protected void onPreExecute() { mProgressDialog.show(); }

  @Override
  protected void onPostExecute(Void result) {
    onPostExecute();
    super.onPostExecute(result);
    mProgressDialog.dismiss();
  }

  protected void onPostExecute() {}

  protected void toast(final String msg) {
    activity.runOnUiThread(new Runnable() {
      @Override
      public void run() {
        Toast.makeText(activity, msg, Toast.LENGTH_LONG).show();
      }
    });
  }

  protected void toast(final int resId) {
    activity.runOnUiThread(new Runnable() {
      @Override
      public void run() {
        Toast.makeText(activity, resId, Toast.LENGTH_LONG).show();
      }
    });
  }

  private final Activity activity;

  private final ProgressDialog mProgressDialog;
}