/*
* Copyright (c) 2016 Newshunt. All rights reserved.
*/
package com.newshunt.dhutil.view.service;

import android.content.ComponentName;
import androidx.browser.customtabs.CustomTabsClient;
import androidx.browser.customtabs.CustomTabsServiceConnection;

import java.lang.ref.WeakReference;

/**
 * Implementation for the CustomTabsServiceConnection that avoids leaking the
 * ServiceConnectionCallback
 *
 * File copied from
 * https://github.com/GoogleChrome/custom-tabs-client/blob/master/shared/src/main/java/org/chromium/customtabsclient/shared/ServiceConnection.java
 *
 * @author neeraj.kumar
 */
public class ServiceConnection extends CustomTabsServiceConnection {
  // A weak reference to the ServiceConnectionCallback to avoid leaking it.
  private WeakReference<ServiceConnectionCallback> mConnectionCallback;

  public ServiceConnection(ServiceConnectionCallback connectionCallback) {
    mConnectionCallback = new WeakReference<>(connectionCallback);
  }

  @Override
  public void onCustomTabsServiceConnected(ComponentName name, CustomTabsClient client) {
    ServiceConnectionCallback connectionCallback = mConnectionCallback.get();
    if (connectionCallback != null) {
      connectionCallback.onServiceConnected(client);
    }
  }

  @Override
  public void onServiceDisconnected(ComponentName name) {
    ServiceConnectionCallback connectionCallback = mConnectionCallback.get();
    if (connectionCallback != null) {
      connectionCallback.onServiceDisconnected();
    }
  }

  /**
   * Callback for events when connecting and disconnecting from Custom Tabs Service.
   *
   * @author neeraj.kumar
   */
  public interface ServiceConnectionCallback {
    /**
     * Called when the service is connected.
     *
     * @param client a CustomTabsClient
     */
    void onServiceConnected(CustomTabsClient client);

    /**
     * Called when the service is disconnected.
     */
    void onServiceDisconnected();
  }
}
