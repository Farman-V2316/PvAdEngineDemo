/*
* Copyright (c) 2016 Newshunt. All rights reserved.
*/
package com.newshunt.dhutil.view.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

/**
 * Empty service used by the custom tab to bind to, raising the application's importance.
 *
 * Chrome will be bound to this service, there by binding itself to our app which is in
 * foreground. This keeps Chrome app alive in BG.
 *
 * More: https://chromium.googlesource.com/external/github.com/GoogleChrome/custom-tabs-client/+/380a1c31040671699f8ccb81830b5c75c80327ec/README.md
 * @author neeraj.kumar
 */
public class KeepAliveService extends Service {
  private static final Binder sBinder = new Binder();

  @Override
  public IBinder onBind(Intent intent) {
    return sBinder;
  }
}
