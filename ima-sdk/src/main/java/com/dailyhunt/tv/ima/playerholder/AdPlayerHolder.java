package com.dailyhunt.tv.ima.playerholder;

import android.content.Context;
import android.content.res.TypedArray;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;

import com.dailyhunt.tv.ima.IMALogger;
import com.dailyhunt.tv.ima.R;
import com.dailyhunt.tv.ima.helper.ImaUtils;
import com.dailyhunt.tv.ima.player.exo.VideoPlayerWithAdPlayback;
import com.dailyhunt.tv.ima.protocol.AdPlayerProtocol;
import com.google.ads.interactivemedia.v3.api.AdDisplayContainer;
import com.google.ads.interactivemedia.v3.api.AdsManager;
import com.google.ads.interactivemedia.v3.api.AdsRequest;
import com.google.ads.interactivemedia.v3.api.CompanionAdSlot;
import com.google.ads.interactivemedia.v3.api.ImaSdkFactory;
import com.newshunt.common.helper.common.Constants;
import com.newshunt.common.helper.common.Logger;
import com.newshunt.common.view.customview.NHRoundedFrameLayout;
import com.newshunt.common.view.view.DetachableWebView;
import com.newshunt.dataentity.common.helper.common.CommonUtils;

import java.util.ArrayList;
import java.util.List;


/**
 * Ad Player Holder,  that is responsible for playing the AD.
 * <p>
 * AD is rendered with help of IMA SDK + DFP/VAST Complaint Server
 *
 * @author ranjith
 */

public class AdPlayerHolder extends FrameLayout implements AdPlayerProtocol, DetachableWebView {

  private static final float AD_REQUEST_TIMEOUT = 8000.0f;
  private final static String TAG = AdPlayerHolder.class.getSimpleName();
  private String adTagUrl;
  private final boolean useCustomPlayer;
  private VideoPlayerWithAdPlayback mVideoPlayerWithAdPlayback;

  public AdPlayerHolder(Context context) {
    this(context, null);
  }

  public AdPlayerHolder(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public AdPlayerHolder(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    final TypedArray array =
        context.obtainStyledAttributes(attrs, R.styleable.AdPlayerHolder);
    useCustomPlayer = array.getBoolean(R.styleable.AdPlayerHolder_useCustomPlayerForIMA, false);
    array.recycle();
  }

  @Override
  public void initialize() {
    if (useCustomPlayer) {
      mVideoPlayerWithAdPlayback = findViewById(R.id.videoPlayerWithAdPlayback);
    }
  }

  @Override
  public void setAdsManager(AdsManager adsManager) {
    if (mVideoPlayerWithAdPlayback != null) {
      mVideoPlayerWithAdPlayback.setAdsManager(adsManager);
    }
  }

  @Override
  public void setInputData(String adUrl) {
    IMALogger.d(TAG, "AD : " + adUrl);
    this.adTagUrl = adUrl;
  }

  @Override
  public void savePosition() {
    if (mVideoPlayerWithAdPlayback != null) {
      mVideoPlayerWithAdPlayback.savePosition();
    }
  }

  @Override
  public void restorePosition() {
    if (mVideoPlayerWithAdPlayback != null) {
      mVideoPlayerWithAdPlayback.restorePosition();
    }
  }


  @Override
  public AdsRequest buildAdRequest(ImaSdkFactory mSdkFactory) {
    IMALogger.d(TAG, "AD : Build ad req");
    if (CommonUtils.isEmpty(adTagUrl)) {
      return null;
    }

    IMALogger.d(TAG, "AD : Build ad req 1");

    //Create Ad Request ..
    AdsRequest request = mSdkFactory.createAdsRequest();
    request.setAdTagUrl(adTagUrl);
    request.setVastLoadTimeout(AD_REQUEST_TIMEOUT);
    if (useCustomPlayer) {
      request.setContentProgressProvider(mVideoPlayerWithAdPlayback.getContentProgressProvider());
    }
    return request;
  }

  @Override
  public AdDisplayContainer getAdDisplaycontainer(ImaSdkFactory mSdkFactory, boolean showCompanion) {
    //Create AD UI Container..
    AdDisplayContainer adDisplayContainer = mSdkFactory.createAdDisplayContainer();
    if (useCustomPlayer) {
      adDisplayContainer.setPlayer(mVideoPlayerWithAdPlayback.getVideoAdPlayer());

      //Register obstruction views for OM tracking.
      for (View view : mVideoPlayerWithAdPlayback.getVideoControlsOverlay()) {
        adDisplayContainer.registerVideoControlsOverlay(view);
      }
    }

    //set companion view as well
    if(showCompanion) {
      try {
        Uri uri = Uri.parse(adTagUrl);
        String size = uri.getQueryParameter("ciu_szs");
        List<Pair<Integer, Integer>> sizes = ImaUtils.getCompanionSize(size);
        List<CompanionAdSlot> adSlots = new ArrayList();
        if (sizes != null && !sizes.isEmpty()) {
          for (Pair<Integer, Integer> s : sizes) {
            CompanionAdSlot compAdSlots = mSdkFactory.createCompanionAdSlot();
            NHRoundedFrameLayout companionHolder = createCompanionView();
            compAdSlots.setContainer(companionHolder);
            compAdSlots.setSize(s.first, s.second);
            adSlots.add(compAdSlots);
            companionHolder.setTag(s.first.toString() + Constants.SIZE_TOKEN + s.second.toString());
            if(mVideoPlayerWithAdPlayback != null) {
              mVideoPlayerWithAdPlayback.addCompanion(companionHolder, compAdSlots);
            }
          }
        }
        adDisplayContainer.setCompanionSlots(adSlots);
      } catch (Exception e) {
        Logger.caughtException(e);
        e.printStackTrace();
      }

    }
    adDisplayContainer.setAdContainer(
        useCustomPlayer ? mVideoPlayerWithAdPlayback.getAdUiContainer() : this);

    return adDisplayContainer;
  }

  private NHRoundedFrameLayout createCompanionView(){
      NHRoundedFrameLayout companionView = new NHRoundedFrameLayout(getContext());
      companionView.setCornerRadius((int) getResources().getDimension(R.dimen.immersive_view_companion_border_radius));
      FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
               FrameLayout.LayoutParams.WRAP_CONTENT);
      params.gravity = Gravity.CENTER;
      companionView.setLayoutParams(params);
      return companionView;
  }

  @Override
  public void releasePlayer() {
    if (mVideoPlayerWithAdPlayback != null) {
      mVideoPlayerWithAdPlayback.releasePlayer();
    }
  }

  @Override
  public void setAdVisibility(boolean visible) {
    if (visible) {
      setVisibility(VISIBLE);
    } else {
      setVisibility(GONE);
    }
  }

  @Override
  public void setQualifiesImmersive(boolean state) {
      if(mVideoPlayerWithAdPlayback != null) mVideoPlayerWithAdPlayback.setQualifiesImmersive(state);
  }

  @Override
  public void setImmersiveSpan(int span) {
    if(mVideoPlayerWithAdPlayback != null) mVideoPlayerWithAdPlayback.setImmersiveSpan(span);
  }

  @Override
  public void setCompanionRefreshTime(int refreshTime) {
    if(mVideoPlayerWithAdPlayback != null) mVideoPlayerWithAdPlayback.setDefaultCompanionTransitionTime(refreshTime);
  }
}
