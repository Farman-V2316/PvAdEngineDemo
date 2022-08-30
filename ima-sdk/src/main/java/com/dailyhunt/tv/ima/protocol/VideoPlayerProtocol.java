package com.dailyhunt.tv.ima.protocol;

import com.dailyhunt.tv.exolibrary.VideoQualitySettings;
import com.dailyhunt.tv.ima.callback.VideoPlayerCallBack;
import com.dailyhunt.tv.ima.exo.ImaAdsLoader;
import com.dailyhunt.tv.ima.service.ContentStateProvider;

/**
 * Interaction Protocol between the ContentPlayer View and Video Player View..
 *
 * @author ranjith
 */

public interface VideoPlayerProtocol {

  /**
   * Initialize the Video Player
   */
  void initialize();

  /**
   * Helper method to set Input data to Video Player ..
   */
  void setInputData(String dataUrl, boolean isEnableQualitySetting, VideoQualitySettings settings,
                    boolean startPlaying, boolean isLive, boolean muteMode, boolean addClientInfo,
                    boolean applyBufferSettings, ImaAdsLoader adsLoader);

  /**
   * Resume Video as requested from IMA SDK /Detail View Holder ..
   *
   * @param adEvent -- whether this is adEvent or not
   */
  void resumeVideoRequested(boolean adEvent);

  /**
   * Pause Video as requested from IMA SDK / Detail View Holder ..
   *
   * @param adEvent -- whether this is adEvent or not
   */
  void pauseVideoRequested(boolean adEvent);


  /**
   * Pause Video with release for optimisation.
   *
   * @param adEvent -- whether given call is due to ima sdk or not
   */
  default void pauseVideoWithRelease(boolean adEvent) {
  }

  /**
   * Get the current Video Position
   *
   * @return -- current video position
   */
  int getVideoCurrentPosition();

  /**
   * Get the total Video Duration
   *
   * @return -- total video Duration
   */
  int getVideoDuration();

  /**
   * Call back to set to the Video Player view
   *
   * @param callBack -- call back
   */
  void setVideoPlayerCallBack(VideoPlayerCallBack callBack);

  /**
   * Method to set Content State Provider
   *
   * @param stateProvider -- state provider
   */
  void setContentStateProvider(ContentStateProvider stateProvider);

  /**
   * PLayer release call back
   */
  void releasePlayer();

  /**
   * Call to player view to show replay icon
   */
  void showReplayButton();

  /**
   * Call to player state video complete state
   */
  boolean isVideoComplete();

}
