/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.common.helper.appupgrade;

import android.os.Handler;
import android.os.Looper;

/**
 * abstract class for App Upgrade implementations
 *
 * @author arun.babu
 */
public abstract class AppUpgrade {

  private static final Handler HANDLER = new Handler(Looper.getMainLooper());
  private final Callback callback;

  public AppUpgrade(Callback callback) {
    this.callback = callback;
  }

  public abstract void start();

  protected abstract void onTaskCompleted(UpgradeTask upgradeTask, boolean result);

  protected final void postCompletion() {
    if (callback != null) {
      HANDLER.post(new Runnable() {
        @Override
        public void run() {
          callback.onAppUpgradeDone();
        }
      });
    }
  }

  public interface Callback {
    void onAppUpgradeDone();
  }
}
