/*
* Copyright (c) 2018 Newshunt. All rights reserved.
*/
package com.dailyhunt.tv.ima.playerholder;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.dailyhunt.tv.exolibrary.VideoQualitySettings;
import com.dailyhunt.tv.ima.callback.VideoPlayerCallBack;
import com.dailyhunt.tv.ima.exo.ImaAdsLoader;
import com.dailyhunt.tv.ima.protocol.VideoPlayerProtocol;
import com.dailyhunt.tv.ima.service.ContentStateProvider;

/**
 * Dummy Video Player holder in case only ad needs be shown.
 *
 * @author raunak.yadav
 */
public class DefaultVideoPlayerHolder extends FrameLayout implements VideoPlayerProtocol {
  public DefaultVideoPlayerHolder(
      @NonNull Context context) {
    super(context);
  }

  public DefaultVideoPlayerHolder(@NonNull Context context,
                                  @Nullable AttributeSet attrs) {
    super(context, attrs);
  }

  public DefaultVideoPlayerHolder(@NonNull Context context, @Nullable AttributeSet attrs,
                                  int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  @Override
  public void initialize() {

  }

  @Override
  public void setInputData(String dataUrl, boolean isEnableQualitySetting,
                           VideoQualitySettings settings, boolean startPlaying, boolean isLive,
                           boolean muteMode, boolean addClientInfo, boolean applyBufferSettings,
                           ImaAdsLoader adsLoader) {

  }

  @Override
  public void resumeVideoRequested(boolean adEvent) {

  }

  @Override
  public void pauseVideoRequested(boolean adEvent) {

  }

  @Override
  public int getVideoCurrentPosition() {
    return 0;
  }

  @Override
  public int getVideoDuration() {
    return 0;
  }

  @Override
  public void setVideoPlayerCallBack(VideoPlayerCallBack callBack) {

  }

  @Override
  public void setContentStateProvider(ContentStateProvider stateProvider) {

  }

  @Override
  public void releasePlayer() {

  }

  @Override
  public void showReplayButton() {

  }

  @Override
  public boolean isVideoComplete() {
    return true;
  }

}
