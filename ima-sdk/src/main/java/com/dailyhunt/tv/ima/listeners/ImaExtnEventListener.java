/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */
package com.dailyhunt.tv.ima.listeners;

import com.dailyhunt.tv.ima.IMALogger;
import com.dailyhunt.tv.ima.protocol.ContentPlayerProtocol;
import com.dailyhunt.tv.ima.service.ContentStateProvider;
import com.google.ads.interactivemedia.v3.api.Ad;
import com.google.ads.interactivemedia.v3.api.AdEvent;
import com.google.ads.interactivemedia.v3.api.CompanionAdSlot;

import java.util.List;
import java.util.Map;

/**
 * AdEventListener for Buzz videos that use IMA extension in exo.
 *
 * @author raunak.yadav
 */
public class ImaExtnEventListener implements ImaAdsListener {

  private final static String TAG = "ImaExtnEventListener";

  private final ContentStateProvider serviceProvider;
  private final ContentPlayerProtocol contentPlayProtocol;
  private List<CompanionAdSlot> companionAdSlots;
  private CompanionAdSlot.ClickListener companionListener;

  public ImaExtnEventListener(ContentStateProvider serviceProvider,
                              ContentPlayerProtocol contentPlayProtocol) {
    this.serviceProvider = serviceProvider;
    this.contentPlayProtocol = contentPlayProtocol;
  }

  @Override
  public void setCompanionAdSlots(List<CompanionAdSlot> companionAdSlots) {
    this.companionAdSlots = companionAdSlots;
  }

  @Override
  public void onAdEvent(AdEvent adEvent) {
    if (adEvent.getType() == AdEvent.AdEventType.LOG) {
      for (Map.Entry<String, String> entry : adEvent.getAdData().entrySet()) {
        IMALogger.d(TAG, entry.getKey() + " : " + entry.getValue());
        if ("type".equals(entry.getKey()) && "adLoadError".equals(entry.getValue())) {
          serviceProvider.getADListener().onAdError();
        }
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
        //Remove previous companion ad content & listeners.
        if (companionAdSlots != null) {
          companionAdSlots.get(0).getContainer().removeAllViews();
          for (CompanionAdSlot slot : companionAdSlots) {
            slot.removeClickListener(companionListener);
          }
        }
        serviceProvider.getADListener().onAdLoaded(ad);
        break;
      case RESUMED:
        serviceProvider.getADListener().onAdResumed(ad);
        break;
      case PAUSED:
        serviceProvider.getADListener().onAdPaused(ad);
        break;
      case TAPPED:
        serviceProvider.getADListener().onAdTapped(ad);
        break;
      case CLICKED:
        serviceProvider.getADListener().onAdClicked(ad);
        break;
      case SKIPPED:
        serviceProvider.getADListener().onAdPlayEnded(ad);
        break;
      case STARTED:
        contentPlayProtocol.showOrHideIntermediateProgress(false);
        boolean companionAdLoaded = false;
        if (companionAdSlots != null) {
          for (CompanionAdSlot slot : companionAdSlots) {
            if (slot != null && slot.isFilled()) {
              companionAdLoaded = true;
              if (companionListener == null) {
                companionListener = () -> serviceProvider.getADListener().onCompanionAdClicked(ad);
              }
              slot.addClickListener(companionListener);
              break;
            }
          }
        }
        serviceProvider.getADListener().onAdPlayStart(ad, companionAdLoaded);
        break;
      case COMPLETED:
        serviceProvider.getADListener().onAdPlayEnded(ad);
        break;
      case ALL_ADS_COMPLETED:
        serviceProvider.getADListener().onAllAdsCompleted();
        break;
      default:
        break;
    }
  }
}
