package com.dailyhunt.tv.players.player;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.dailyhunt.tv.exolibrary.VideoQualitySettings;
import com.dailyhunt.tv.ima.IMALogger;
import com.dailyhunt.tv.ima.R;
import com.dailyhunt.tv.ima.callback.VideoPlayerCallBack;
import com.dailyhunt.tv.ima.exo.ImaAdsLoader;
import com.dailyhunt.tv.ima.player.VideoPlayer;
import com.dailyhunt.tv.ima.protocol.VideoPlayerProtocol;
import com.dailyhunt.tv.ima.service.ContentStateProvider;

/**
 * Video Player Holder,  that is responsible for playing the Video .
 *
 * @author ranjith
 */

/*
public class VideoPlayerHolder extends FrameLayout implements VideoPlayerProtocol {

  //private final static String TAG = VideoPlayerHolder.class.getSimpleName();

  public VideoPlayer videoPlayer;

  public VideoPlayerHolder(Context context) {
    super(context);
  }

  public VideoPlayerHolder(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public VideoPlayerHolder(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  @Override
  public void initialize() {
    //IMALogger.d(TAG, "initialize");
    videoPlayer = (ExoPlayerView) findViewById(R.id.video_player_view);
  }

  @Override
  public void setInputData(String dataUrl, boolean isEnableQualitySetting,
                           VideoQualitySettings settings, boolean startPlaying, boolean isLive,
                           boolean muteMode, boolean addClientInfo, boolean applyBufferSettings,
                           ImaAdsLoader adsLoader) {
    //IMALogger.d(TAG, "setInputData");
    videoPlayer.setInputData(dataUrl, isEnableQualitySetting, settings, startPlaying, isLive,
        muteMode, addClientInfo, applyBufferSettings, adsLoader);
  }

  @Override
  public void resumeVideoRequested(boolean isAdEvent) {
    //IMALogger.d(TAG, "resumeVideoReq " + isAdEvent);
    videoPlayer.resumeVideoReq(isAdEvent);
  }

  @Override
  public void pauseVideoRequested(boolean isAdEvent) {
    //IMALogger.d(TAG, "pauseVideoReq " + isAdEvent);
    videoPlayer.pauseVideoReq(isAdEvent);
  }

  @Override
  public void pauseVideoWithRelease(boolean adEvent) {
    IMALogger.d(TAG, "pauseVideoReqWithRelease" + adEvent);
    videoPlayer.pauseVideoWithReleaseReq(adEvent);
  }

  @Override
  public int getVideoCurrentPosition() {
    return videoPlayer.getVideoCurDuration();
  }

  @Override
  public int getVideoDuration() {
    return videoPlayer.getVideoDuration();
  }

  @Override
  public void setVideoPlayerCallBack(VideoPlayerCallBack callBack) {
    videoPlayer.setVideoPlayerCallBack(callBack);
  }

  @Override
  public void setContentStateProvider(ContentStateProvider stateProvider) {
    videoPlayer.setContentStateProvider(stateProvider);
  }

  @Override
  public void releasePlayer() {
    //IMALogger.d(TAG, "releasePlayer");
    videoPlayer.releasePlayer();
  }

  @Override
  public void showReplayButton() {
    //IMALogger.d(TAG, "showReplayButton");
    videoPlayer.showReplayButton();
  }

  @Override
  public boolean isVideoComplete() {
    //IMALogger.d(TAG, "isVideoComplete");
    return videoPlayer.isVideoComplete();
  }

}
*/
