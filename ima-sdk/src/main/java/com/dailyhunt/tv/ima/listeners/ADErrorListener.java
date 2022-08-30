package com.dailyhunt.tv.ima.listeners;

import com.dailyhunt.tv.ima.IMALogger;
import com.dailyhunt.tv.ima.protocol.ContentPlayerProtocol;
import com.dailyhunt.tv.ima.service.ContentStateProvider;
import com.google.ads.interactivemedia.v3.api.AdErrorEvent;

/**
 * ADError Listener for ADManager of IMA SDK
 *
 * @author ranjith
 */

public class ADErrorListener implements AdErrorEvent.AdErrorListener {

  private final String TAG = ADErrorListener.class.getSimpleName();

  private final ContentStateProvider serviceProvider;
  private final ContentPlayerProtocol contentPlayProtocol;
  private final boolean contentVideoExists;

  public ADErrorListener(ContentStateProvider serviceProvider,
                         ContentPlayerProtocol contentPlayProtocol, boolean contentVideoExists) {
    this.serviceProvider = serviceProvider;
    this.contentPlayProtocol = contentPlayProtocol;
    this.contentVideoExists = contentVideoExists;
  }

  @Override
  public void onAdError(AdErrorEvent event) {
    IMALogger.d(TAG, "AD-ERROR : " + event.getError());
    if (serviceProvider != null && serviceProvider.getADListener() != null) {
      serviceProvider.getADListener().onAdError();
    }
    if (contentVideoExists) {
      contentPlayProtocol.getAdProtocol().setAdVisibility(false);
      contentPlayProtocol.getVideoProtocol().resumeVideoRequested(true);
    }
    contentPlayProtocol.showOrHideIntermediateProgress(false);
  }
}
