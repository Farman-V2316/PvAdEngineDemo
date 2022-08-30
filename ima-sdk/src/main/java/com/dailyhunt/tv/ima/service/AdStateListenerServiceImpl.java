package com.dailyhunt.tv.ima.service;

import com.dailyhunt.tv.ima.IMALogger;
import com.dailyhunt.tv.ima.callback.AdPlayerCallBack;
import com.dailyhunt.tv.ima.entity.model.ContentAdType;
import com.dailyhunt.tv.ima.entity.state.AdState;
import com.google.ads.interactivemedia.v3.api.Ad;

/**
 * Service Impl Holding the Corresponding AdState.
 * Implements the AdStateListenerService , and updates the adState MemoryVariable
 *
 * @author ranjith
 */
public class AdStateListenerServiceImpl implements AdStateListenerService {

  private final String TAG = AdStateListenerServiceImpl.class.getSimpleName();
  private ContentAdType contentAdType;
  private AdPlayerCallBack callBack;

  private AdState adState;

  public AdStateListenerServiceImpl(AdPlayerCallBack callBack, ContentAdType contentAdType) {

    this.callBack = callBack;
    this.contentAdType = contentAdType;

    this.adState = AdState.AD_UNKNOWN;
  }

  @Override
  public void onAdLoaded(Ad ad) {
    IMALogger.d(TAG, "ON AD Loaded");
    updateAdState(AdState.AD_LOADED);

    callBack.onAdStateChanged(ad, AdState.AD_LOADED, contentAdType);
  }

  @Override
  public void onAdPlayStart(Ad ad, boolean companionAdLoaded) {
    IMALogger.d(TAG, "ON AD Play Started");
    updateAdState(AdState.AD_PLAY_STARTED);

    callBack.onAdStateChanged(ad, AdState.AD_PLAY_STARTED, contentAdType, companionAdLoaded);
  }


  @Override
  public void onAdPlayEnded(Ad ad) {
    IMALogger.d(TAG, "ON Ad Play Ended");
    updateAdState(AdState.AD_PLAY_ENDED);

    callBack.onAdStateChanged(ad, AdState.AD_PLAY_ENDED, contentAdType);
  }

  @Override
  public void onAdResumed(Ad ad) {
    IMALogger.d(TAG, "ON Ad Resumed");
    updateAdState(AdState.AD_RESUMED);
    callBack.onAdStateChanged(ad, AdState.AD_RESUMED, contentAdType);
  }

  @Override
  public void onAdPaused(Ad ad) {
    IMALogger.d(TAG, "ON Ad Paused");
    updateAdState(AdState.AD_PAUSED);
    callBack.onAdStateChanged(ad, AdState.AD_PAUSED, contentAdType);
  }

  @Override
  public void onAdTapped(Ad ad) {
    IMALogger.d(TAG, "ON Ad Tapped");
    callBack.onAdStateChanged(ad, AdState.AD_TAPPED, contentAdType);
  }

  @Override
  public void onAdClicked(Ad ad) {
    IMALogger.d(TAG, "On Ad Clicked");
    callBack.onAdStateChanged(ad, AdState.AD_CLICKED, contentAdType);
  }

  @Override
  public void onCompanionAdClicked(Ad ad) {
    IMALogger.d(TAG, "On Companion Ad Clicked");
    callBack.onAdStateChanged(ad, AdState.AD_COMPANION_CLICKED, contentAdType);
  }

  @Override
  public void onAdError() {
    IMALogger.d(TAG, "On Ad Error");
    updateAdState(AdState.AD_ERROR);

    callBack.onAdStateChanged(null, AdState.AD_ERROR, contentAdType);
  }

  @Override
  public void onAllAdsCompleted() {
    IMALogger.d(TAG, "On All Ads Completed");
    updateAdState(AdState.ALL_ADS_COMPLETE);
    callBack.onAdStateChanged(null, AdState.ALL_ADS_COMPLETE, contentAdType);
  }

  @Override
  public AdState getAdState() {
    return adState;
  }

  private synchronized void updateAdState(AdState state) {
    this.adState = state;
  }
}
