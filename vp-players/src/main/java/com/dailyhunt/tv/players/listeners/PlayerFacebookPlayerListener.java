package com.dailyhunt.tv.players.listeners;

/**
 * Created by Jayanth on 09/05/18.
 */
public interface PlayerFacebookPlayerListener {

  void onPlayerReady();

  void onPlayStart(long currentDuration);

  void onPlayerPause(long currentDuration);

  void onStartBuffering(long currentDuration);

  void onFinishBuffering(long currentDuration);

  void onFinishPlaying(long currentDuration);

  void onPlayerError(long currentDuration);

  void getCurrentpositon(long currentDuration);

  boolean isFragmentAdded();
}
