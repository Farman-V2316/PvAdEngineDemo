package com.dailyhunt.tv.ima;

import android.content.Context;

import com.dailyhunt.tv.ima.entity.model.ContentData;
import com.dailyhunt.tv.ima.exo.ImaAdsLoader;
import com.dailyhunt.tv.ima.helper.ImaUtils;
import com.dailyhunt.tv.ima.listeners.ADErrorListener;
import com.dailyhunt.tv.ima.listeners.ADEventListener;
import com.dailyhunt.tv.ima.protocol.ContentPlayerProtocol;
import com.dailyhunt.tv.ima.service.ContentStateProvider;
import com.google.ads.interactivemedia.v3.api.Ad;
import com.google.ads.interactivemedia.v3.api.AdDisplayContainer;
import com.google.ads.interactivemedia.v3.api.AdErrorEvent;
import com.google.ads.interactivemedia.v3.api.AdsLoader;
import com.google.ads.interactivemedia.v3.api.AdsManager;
import com.google.ads.interactivemedia.v3.api.AdsRenderingSettings;
import com.google.ads.interactivemedia.v3.api.AdsRequest;
import com.google.ads.interactivemedia.v3.api.player.ContentProgressProvider;
import com.google.ads.interactivemedia.v3.api.player.VideoProgressUpdate;
import com.newshunt.common.helper.common.Logger;
import com.newshunt.dataentity.common.helper.common.CommonUtils;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Controller for playing Video and Ads
 *
 * @author ranjith
 */

public class ContentPlayerController {

  private final static String TAG = ContentPlayerController.class.getSimpleName();

  private Context context;
  private TVIMASDK tvImaSDK;

  private AdsLoader adsLoader;
  private AdsManager adsManager;
  private AdsRequest adRequest;
  private AdDisplayContainer adDisplayContainer;

  // Content Player protocol + State ..
  private ContentStateProvider stateProvider;
  private ContentPlayerProtocol contentProtocol;

  // Admanager Listener's ..
  private ADEventListener adEventListener;
  private ADErrorListener adErrorListener;

  //AdsLoader listeners
  private AdsLoader.AdsLoadedListener adLoadEventListener;
  private AdErrorEvent.AdErrorListener adLoadErrorListener;

  private final ScheduledExecutorService cancelRequestExecutorService =
      Executors.newScheduledThreadPool(1);
  private ScheduledFuture scheduledFuture;
  private final boolean contentVideoExists;
  private final boolean playOnLoad;
  private boolean showCompanion;
  private boolean adsInitFailed, adsManagerStarted;

  public ContentPlayerController(Context context, ContentPlayerProtocol protocol,
                                 ContentStateProvider stateProvider) {
    this(context, protocol, stateProvider, true, true, false);
  }

  public ContentPlayerController(Context context, ContentPlayerProtocol protocol,
                                 ContentStateProvider stateProvider, boolean contentVideoExists,
                                 boolean playOnLoad, boolean showCompanion) {
    IMALogger.d(TAG, "Constructor");
    this.context = context;
    this.contentProtocol = protocol;
    this.stateProvider = stateProvider;
    this.contentVideoExists = contentVideoExists;
    this.playOnLoad = playOnLoad;
    this.showCompanion = showCompanion;
  }

  private void initializeImaSDK() {
    tvImaSDK = TVIMASDK.getInstance();
    try {
      adDisplayContainer =
          contentProtocol.getAdProtocol().getAdDisplaycontainer(tvImaSDK.getImaSdkFactory(),showCompanion);
      adsLoader = tvImaSDK.getImaSdkFactory().createAdsLoader(context, tvImaSDK.getImaSettings(),
          adDisplayContainer);
      initializeADCallBacks();
    } catch (Exception ex) {
      // Handle crash : https://issuetracker.google.com/issues/37048374
      IMALogger.d(TAG, "Failed to init IMA AdsLoader " + ex.getMessage());
      adsInitFailed = true;
    }
  }

  private void initializeADCallBacks() {
    adLoadErrorListener = adErrorEvent -> {
      IMALogger.d(TAG, "ON AD ERROR, on LOAD " + adErrorEvent.getError());
      if (contentVideoExists) {
        resumeVideo();
      } else {
        //If video is not present, need to handle error case.
        stateProvider.getADListener().onAdError();
      }
    };
    adsLoader.addAdErrorListener(adLoadErrorListener);

    adLoadEventListener = adsManagerLoadedEvent -> {
      IMALogger.d(TAG, " onAdsManagerLoaded ");
      cancelScheduledExecutorService();
      adsManagerStarted = false;
      adsManager = adsManagerLoadedEvent.getAdsManager();
      contentProtocol.getAdProtocol().setAdsManager(adsManager);
      //Create AdEvent and AdError Listener's ..
      adEventListener =
          new ADEventListener(stateProvider, adsManager, contentProtocol, contentVideoExists,
              playOnLoad);
      adErrorListener = new ADErrorListener(stateProvider, contentProtocol, contentVideoExists);

      adsManager.addAdErrorListener(adErrorListener);
      adsManager.addAdEventListener(adEventListener);

      AdsRenderingSettings renderSettings =
          tvImaSDK.getImaSdkFactory().createAdsRenderingSettings();
      renderSettings.setEnablePreloading(true);
      renderSettings.setDisableUi(true);
      int maxBitRate = ImaUtils.getMaxBitrateFromHandshake();
      if (maxBitRate > 0) {
        renderSettings.setBitrateKbps(maxBitRate);
      }
      // Initialize the Ad Manager
      adsManager.init(renderSettings);
    };
    adsLoader.addAdsLoadedListener(adLoadEventListener);
  }

  /**
   * Request media.
   *
   * @param contentData ad & video url
   * @return if ad request was successful.
   */
  public boolean requestForContent(ContentData contentData) {
    IMALogger.d(TAG, "Request for content SDK");
    contentProtocol.getAdProtocol().setInputData(contentData.getAdUrl());
    if (stateProvider != null && stateProvider.getADListener() != null) {
      initializeImaSDK();
    }
    if (adsInitFailed) {
      if (!contentVideoExists) {
        return false;
      }
    } else if (!CommonUtils.isEmpty(contentData.getAdUrl())) {
      //Request the AD..
      IMALogger.d(TAG, "Request for content SDK 1");
      adRequest = contentProtocol.getAdProtocol().buildAdRequest(tvImaSDK.getImaSdkFactory());
    }
    contentProtocol.getVideoProtocol()
        .setInputData(contentData.getVideoUrl(), contentData.isEnableQualitySettings(),
            contentData.getVideoQualitySettings(), false, contentData.isLive(),
            contentData.isMuteMode(), contentData.isAddClientInfo(),
            contentData.isApplyBufferSettings(), null);

    //Content Progress Update ..
    ContentProgressProvider mContentProgressProvider = new ContentProgressProvider() {
      @Override
      public VideoProgressUpdate getContentProgress() {
        if (contentProtocol.getVideoProtocol().getVideoDuration() <= 0) {
          return VideoProgressUpdate.VIDEO_TIME_NOT_READY;
        }

        return new VideoProgressUpdate(
            contentProtocol.getVideoProtocol().getVideoCurrentPosition(),
            contentProtocol.getVideoProtocol().getVideoDuration());
      }
    };

    if (adsLoader != null && adRequest != null) {
      IMALogger.d(TAG, "Request for IMA sdk ads");
      if (adRequest.getContentProgressProvider() == null) {
        adRequest.setContentProgressProvider(mContentProgressProvider);
      }
      adsLoader.requestAds(adRequest);
      int timeout = VideoAdsTimeoutHelper.getAdRequestTimeout();
      setScheduledFuture(timeout);
    } else {
      IMALogger.d(TAG, "No ad request. Resuming video");
      contentProtocol.getAdProtocol().setAdVisibility(false);
      contentProtocol.getVideoProtocol().resumeVideoRequested(false);
    }
    return !adsInitFailed;
  }

  /**
   * To play the ad in exoplayer using IMA extension.
   */
  public void requestForContentWithAdsLoader(ContentData contentData, ImaAdsLoader imaAdsLoader,
                                             boolean isAdPlaying) {
    IMALogger.d(TAG, "Request Content+Ad with AdsLoader " + imaAdsLoader);
    if (!isAdPlaying) {
      contentProtocol.getAdProtocol().setAdVisibility(false);
    }
    contentProtocol.getVideoProtocol()
        .setInputData(contentData.getVideoUrl(), contentData.isEnableQualitySettings(),
            contentData.getVideoQualitySettings(), false, contentData.isLive(),
            contentData.isMuteMode(), contentData.isAddClientInfo(),
            contentData.isApplyBufferSettings(), imaAdsLoader);
  }

  public void onContentCompleteCallBack() {
    if (adsLoader != null) {
      adsLoader.contentComplete();
    }
  }

  public void showReplayButton() {
    if (contentProtocol != null) {
      contentProtocol.getVideoProtocol().showReplayButton();
    }
  }

  public void resetOldAdsLoaderPlusManager() {
    IMALogger.d(TAG, "Reset Old Ads Loader PlusManager");
    if (adsLoader != null) {
      adsLoader.removeAdErrorListener(adLoadErrorListener);
      adsLoader.removeAdsLoadedListener(adLoadEventListener);
      adLoadErrorListener = null;
      adLoadEventListener = null;
      adsLoader = null;
    }
    if (adDisplayContainer != null) {
      adDisplayContainer.unregisterAllVideoControlsOverlays();
    }
    if (adsManager != null) {
      adsManager.destroy();
      adsManager.removeAdErrorListener(adErrorListener);
      adsManager.removeAdEventListener(adEventListener);
      adsManager = null;
    }
    if (adRequest != null) {
      adRequest.setContentProgressProvider(null);
      adRequest = null;
    }
    cancelScheduledExecutorService();
  }

  public void startPlayingAd() {
    // Calling adsManager.start() multiple times causes inconsistent video behavior. Avoid.
    if (adsManager != null) {
      if (!adsManagerStarted) {
        IMALogger.d(TAG, "Start Ad Manager");
        adsManager.start();
        adsManagerStarted = true;
      } else {
        resumeAdManager();
      }
    }
  }

  public void resumeAdManager() {
    if (!contentVideoExists) {
      contentProtocol.getAdProtocol().restorePosition();
    }
    if (adsManager != null) {
      IMALogger.d(TAG, "Resume Ad Manager");
      adsManager.resume();
    }
  }

  public void pauseAdManager() {
    if (!contentVideoExists) {
      contentProtocol.getAdProtocol().savePosition();
    }
    if (adsManager != null) {
      IMALogger.d(TAG, "Pause Ad Manager");
      adsManager.pause();
    }
  }

  public boolean requestClick() {
    if (adsManager != null) {
      Ad ad = adsManager.getCurrentAd();
      if (ad != null && ad.isUiDisabled()) {
        IMALogger.d(TAG, "Click Ad Manager");
        adsManager.clicked();
        return true;
      }
    }
    return false;
  }

  public void showOrHideProgress(boolean show) {
    contentProtocol.showOrHideIntermediateProgress(show);
  }

  private void setScheduledFuture(final int timeOut) {
    scheduledFuture = cancelRequestExecutorService.schedule(new Runnable() {
      @Override
      public void run() {
        IMALogger.d(TAG, "exo player ad timeout");
        resumeVideo();
      }
    }, timeOut, TimeUnit.SECONDS);
  }

  private void cancelScheduledExecutorService() {
    if (scheduledFuture != null) {
      scheduledFuture.cancel(true);
      IMALogger.d(TAG, "exo player ad timeout timer cancelled");
    }
    cancelRequestExecutorService.shutdown();
  }

  private void resumeVideo() {
    if (!contentVideoExists) {
      return;
    }
    try {
      if (stateProvider != null && stateProvider.getADListener() != null) {
        stateProvider.getADListener().onAdError();
      }
      contentProtocol.getAdProtocol().setAdVisibility(false);
      contentProtocol.getVideoProtocol().resumeVideoRequested(true);
      contentProtocol.showOrHideIntermediateProgress(false);
      cancelScheduledExecutorService();
    } catch (Exception e) {
      Logger.caughtException(e);
    }
  }

  public void destroy() {
    resetOldAdsLoaderPlusManager();
    contentProtocol.getAdProtocol().releasePlayer();
    if (!contentVideoExists) {
      contentProtocol.getVideoProtocol().releasePlayer();
    }
  }
}
