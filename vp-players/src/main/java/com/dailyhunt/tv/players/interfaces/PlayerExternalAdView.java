package com.dailyhunt.tv.players.interfaces;

/**
 * Used to destroy external sdk ads when not needed.
 *
 * @author neeraj.kumar
 */
public interface PlayerExternalAdView {

  int getVideoCardVisibleRatio();

  void startVideoPlayback();

  void onItemNotInFocus();

  void onStop();

  void onPause();

  void removeMask();

  void increaseMask();

  void decreaseMask();

  void detachView();

  void destroy();
}
