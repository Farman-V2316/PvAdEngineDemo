package com.dailyhunt.tv.players.analytics.interfaces;

/**
 * Created by Jayanth on 09/05/18.
 */
public interface VideoPlayerCallBacks {
  void onPlay(long currentPos);
  void onPause(long currentPos);
  void onResume();
  void onError(long currentPos);
  void seekTo();
  void bufferingStart();
  void bufferingStop();
  void end(long duration);
  void currentTime(long time);
}
