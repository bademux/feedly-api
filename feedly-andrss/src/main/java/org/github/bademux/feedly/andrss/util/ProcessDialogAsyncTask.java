package org.github.bademux.feedly.andrss.util;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import org.github.bademux.feedly.andrss.R;

public abstract class ProcessDialogAsyncTask<Params, Result>
    extends AsyncTask<Params, Void, Result> {

  private final ProgressDialog dialog;

  public ProcessDialogAsyncTask(final Context context) {
    dialog = new ProgressDialog(context);
    dialog.setMessage(context.getString(R.string.msg_loading));
  }

  @Override
  protected void onPreExecute() {dialog.show(); }

  @Override
  protected void onPostExecute(Result result) { dialog.dismiss(); }
}