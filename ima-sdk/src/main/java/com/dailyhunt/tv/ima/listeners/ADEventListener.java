package com.dailyhunt.tv.ima.listeners;

import com.dailyhunt.tv.ima.IMALogger;
import com.dailyhunt.tv.ima.protocol.ContentPlayerProtocol;
import com.dailyhunt.tv.ima.service.ContentStateProvider;
import com.google.ads.interactivemedia.v3.api.Ad;
import com.google.ads.interactivemedia.v3.api.AdEvent;
import com.google.ads.interactivemedia.v3.api.AdsManager;

import java.util.Map;

/**
 * ADEvent Listener for ADManager of IMA SDK
 *
 * @author ranjith
 */

public class ADEventListener implements AdEvent.AdEventListener {

  private final static String TAG = "ADEventListener";

  private final ContentStateProvider serviceProvider;
  private final ContentPlayerProtocol contentPlayProtocol;
  private final AdsManager adsManager;
  private final boolean contentVideoExists;
  private boolean isAdLoaded;
  private boolean playOnLoad;

  public ADEventListener(ContentStateProvider serviceProvider, AdsManager adsManager,
                         ContentPlayerProtocol contentPlayProtocol, boolean contentVideoExists,
                         boolean playOnLoad) {
    this.adsManager = adsManager;
    this.serviceProvider = serviceProvider;
    this.contentPlayProtocol = contentPlayProtocol;
    this.contentVideoExists = contentVideoExists;
    this.playOnLoad = playOnLoad;
  }

  @Override
  public void onAdEvent(AdEvent adEvent) {
    if (adEvent.getType() == AdEvent.AdEventType.LOG) {
      for (Map.Entry<String, String> entry : adEvent.getAdData().entrySet()) {
        IMALogger.d(TAG, entry.getKey() + " : " + entry.getValue());
      }
      return;
    }
    Ad ad = adEvent.getAd();
    if (adEvent.getType() == AdEvent.AdEventType.AD_PROGRESS) {
      IMALogger.v(TAG, "AD " + adEvent.getType() + " -- " + ad);
      return;
    }
    IMALogger.d(TAG, "AD " + adEvent.getType() + " -- " + ad);

    switch (adEvent.getType()) {
      case LOADED:
        serviceProvider.getADListener().onAdLoaded(ad);
        if (playOnLoad) {
          adsManager.start();
        }
        break;
      case RESUMED:
        serviceProvider.getADListener().onAdResumed(ad);
        break;
      case PAUSED:
        serviceProvider.getADListener().onAdPaused(ad);
        break;
      case TAPPED:
        contentPlayProtocol.getAdProtocol().savePosition();
        serviceProvider.getADListener().onAdTapped(ad);
        break;
      case CLICKED:
        contentPlayProtocol.getAdProtocol().savePosition();
        serviceProvider.getADListener().onAdClicked(ad);
        break;
      case SKIPPED:
        serviceProvider.getADListener().onAdPlayEnded(ad);
        break;
      case STARTED:
        isAdLoaded = true;
        contentPlayProtocol.showOrHideIntermediateProgress(false);
        serviceProvider.getADListener().onAdPlayStart(ad, false);
        break;
      case COMPLETED:
        serviceProvider.getADListener().onAdPlayEnded(ad);
        break;
      case CONTENT_PAUSE_REQUESTED:
        contentPlayProtocol.getVideoProtocol().pauseVideoRequested(true);
        contentPlayProtocol.showOrHideIntermediateProgress(true);
        contentPlayProtocol.getAdProtocol().setAdVisibility(true);
        break;
      case CONTENT_RESUME_REQUESTED:
        if (contentVideoExists) {
          contentPlayProtocol.getAdProtocol().setAdVisibility(false);
          contentPlayProtocol.getVideoProtocol().resumeVideoRequested(true);
        } else if (!isAdLoaded) {
          // To fix blank space bug.
          // This may occur in case of non-fatal errors in IMA sdk when an ad cannot be
          // loaded. If none of the ad succeeds in loading, call error method.
          serviceProvider.getADListener().onAdError();
        }
        contentPlayProtocol.showOrHideIntermediateProgress(false);
        break;
      case ALL_ADS_COMPLETED:
        serviceProvider.getADListener().onAllAdsCompleted();
        if (adsManager != null) {
          adsManager.destroy();
        }
        contentPlayProtocol.getAdProtocol().releasePlayer();
        break;
      default:
        break;
    }
  }
}
