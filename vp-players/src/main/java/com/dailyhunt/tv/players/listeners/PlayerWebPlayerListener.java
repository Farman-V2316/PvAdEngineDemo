package com.dailyhunt.tv.players.listeners;

/**
 * Created by Jayanth on 09/05/18.
 */

public interface PlayerWebPlayerListener {
  void onPlayerReady();

  void onPlayStart(long currentDuration);

  void onPlayerPause(long currentDuration);

  void onStartBuffering(long currentDuration);

  void onFinishBuffering(long currentDuration);

  void onFinishPlaying(long currentDuration);

  void onPlayerError(long currentDuration);

  void getCurrentpositon(long currentDuration);

  void onAdStarted(long currentDuration);

  void onAdPaused(long currentDuration);

  void onAdEnded(long currentDuration);

  void onAdSkipped(long currentDuration);

  boolean isViewInForeground();

  void handleFullScreen();

  void resetEventTimer(long currentDuration);

  void onDisplayClick();

  void onFirstQuartile();

  void onMidQuartile();

  void onThirdQuartile();

  boolean isVideoInNewsList();
}
