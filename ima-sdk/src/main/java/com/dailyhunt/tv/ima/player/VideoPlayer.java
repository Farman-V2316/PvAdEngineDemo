package com.dailyhunt.tv.ima.player;

import com.dailyhunt.tv.exolibrary.VideoQualitySettings;
import com.dailyhunt.tv.ima.callback.VideoPlayerCallBack;
import com.dailyhunt.tv.ima.exo.ImaAdsLoader;
import com.dailyhunt.tv.ima.service.ContentStateProvider;

/**
 * Video Player call backs required
 *
 * @author ranjith
 */

public interface VideoPlayer {

  /**
   * Initialize with input data
   *
   * @param dataUrl         -- url
   * @param qualitySettings -- quality settings
   * @param startPlaying    -- whether we should start playing immediately or not
   */
  void setInputData(String dataUrl, boolean isEnableQualitySetting,
                    VideoQualitySettings qualitySettings, boolean startPlaying, boolean isLive,
                    boolean muteMode, boolean addClientInfo, boolean applyBufferSettings,
                    ImaAdsLoader adsLoader);

  /**
   * Resume Video as requested from IMA SDK / Fragment
   *
   * @param isAdEvent -- whether given call is due to ima sdk or not
   */
  void resumeVideoReq(boolean isAdEvent);

  /**
   * Pause Video as requested from IMA SDK /Fragment
   *
   * @param isAdEvent -- whether given call is due to ima sdk or not
   */
  void pauseVideoReq(boolean isAdEvent);


    /**
     * Pause Video with release for optimisation
     *
     * @param isAdEvent -- whether given call is due to ima sdk or not
     */
    default void pauseVideoWithReleaseReq(boolean isAdEvent) {
    }


  /**
   * Get the current Video Position
   *
   * @return -- current video position
   */
  int getVideoCurDuration();

  /**
   * Get the total Video Duration
   *
   * @return -- total video Duration
   */
  int getVideoDuration();

  /**
   * Call back to Fragment from Video View ..
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
   * Call for releasing the Player
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
