/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.common.helper.appupgrade;

import android.os.AsyncTask;

/**
 * Abstract class for all upgrade tasks
 *
 * @author nayana.hs on 7/13/2015.
 */
public abstract class UpgradeTask extends AsyncTask<Void, Void, Boolean> {

  private final AppUpgrade appUpgrade;

  protected UpgradeTask(AppUpgrade appUpgrade) {
    this.appUpgrade = appUpgrade;
  }

  protected abstract boolean doInBackground();

  @Override
  protected final Boolean doInBackground(Void... voids) {
    try {
      return doInBackground();
    } catch (Exception e) {
      return false;
    }
  }

  @Override
  protected final void onPostExecute(Boolean result) {
    appUpgrade.onTaskCompleted(this, result);
  }
}
