package com.dailyhunt.tv.ima.service;

import com.dailyhunt.tv.ima.IMALogger;
import com.dailyhunt.tv.ima.entity.state.VideoState;

/**
 * Service Impl Holding the Corresponding VideoState.
 * Implements the VideoStateListenerService , and updates the videoState MemoryVariable
 *
 * @author ranjith
 */

public class VideoStateListenerServiceImpl implements VideoStateListenerService {

  private final String TAG = VideoStateListenerServiceImpl.class.getSimpleName();
  private VideoState videoState;

  public VideoStateListenerServiceImpl() {
    this.videoState = VideoState.VIDEO_UNKNOWN;
  }

  @Override
  public void onVideoUnknownState() {
    IMALogger.d(TAG, "ON Unknown state");
    updateVideoState(VideoState.VIDEO_UNKNOWN);
  }

  @Override
  public void onVideoPrepareInProgress() {
    IMALogger.d(TAG, "onVideoPrepareInProgress");
    updateVideoState(VideoState.VIDEO_PREPARE_IN_PROGRESS);
  }

  @Override
  public void onVideoPrepared() {
    IMALogger.d(TAG, "OnVideoPrepared");
    updateVideoState(VideoState.VIDEO_PREPARED);
  }

  @Override
  public void onVideoPlaying() {
    IMALogger.d(TAG, "onVideoPlaying");
    updateVideoState(VideoState.VIDEO_PLAYING);
  }

  @Override
  public void onVideoPaused() {
    IMALogger.d(TAG, "onVideoPaused");
    updateVideoState(VideoState.VIDEO_PAUSED);
  }

  @Override
  public void onVideoPlayComplete() {
    IMALogger.d(TAG, "onVideoCompleted");
    updateVideoState(VideoState.VIDEO_COMPLETE);
  }

  @Override
  public void onVideoError() {
    IMALogger.d(TAG, "onVideoError");
    updateVideoState(VideoState.VIDEO_ERROR);
  }

  @Override
  public void onVideoQualityChange() {
    IMALogger.d(TAG, "onVideoQualityChange");
    updateVideoState(VideoState.VIDEO_QUALITY_CHANGE);
  }

  @Override
  public VideoState getVideoState() {
    return videoState;
  }

  private synchronized void updateVideoState(VideoState state) {
    this.videoState = state;
  }
}
