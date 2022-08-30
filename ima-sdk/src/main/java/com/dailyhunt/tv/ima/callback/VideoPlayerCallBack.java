package com.dailyhunt.tv.ima.callback;

import com.google.android.exoplayer2.ExoPlaybackException;

/**
 * Call backs from the Video Player back till the place where the video is played
 * Ideally this is implemented by activity / fragment
 *
 * @author ranjith
 */

public interface VideoPlayerCallBack {

  /**
   * Show/Remove Thumbnail ..
   */
  void showOrRemoveThumbnail(boolean show);

  /**
   * On Video is completed/Error
   */
  void onContentCompleteOrError(boolean isComplete, int duration);

  /**
   * On Full Screen View click
   */
  void onFullScreenClick();

  /**
   * Call back to show video Settings UI ..
   */
  void showVideoSettingsUI();
  /*
  * Calback for video started
  * */
  void onVideoStarted();

  void onVideoError(int exceptiontype);


  void onVideoResumed();

  /**
   * Callback for video paused
   */
  void onVideoPaused();

  void onPlayerReady();

  /**
   * Callback to know video to be cached
   */
  boolean isCacheVideo();

  /**
   * Callback to know video controller to be hide or not
   */
  boolean isHideController();

  /**
   * Used in case of Autoplay - as we pause the video after load - until it comes in focus
   * @return
   */
  void onVideoPausedOnCaching();

  /**
   * Callback on Seek start
   */
  void onSeekStart();

  /**
   * Callback on Seek complete
   */
  void onSeekComplete();

  /**
   * Callback on video restart
   */
  void onVideoReplay();

  /**
   * Callback for Exoplayer position.
   *
   * @param position content position
   */
  default void onTimeUpdate(long position) {
  }

  default boolean isMediaPlayerVisible() { return false; }

  default void updatePlayerController(boolean isMute) {}

  void logVideoError(ExoPlaybackException exception);
}
