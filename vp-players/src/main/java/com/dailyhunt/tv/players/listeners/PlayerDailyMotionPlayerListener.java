/*
* Copyright (c) 2017 Newshunt. All rights reserved.
*/
package com.dailyhunt.tv.players.listeners;

/**
 * Created by umesh.isran on 1/9/17.
 */

public interface PlayerDailyMotionPlayerListener {
  void onVideoResume();

  void onVideoStarted();

  void onPlayComplete();

  void playNextVideo();

  void onPlayerPaused();

  void onStartBuffering();

  void onFinishBuffering();

  void onFullScreenToggle(boolean fullScreen);

  void onAdStart();

  void onAdResume();

  /**
   * Callback used for Ad Paused and Ad Complete
   */
  void onAdPaused();

  void onAdEnd();

  void hideLoader();

}
