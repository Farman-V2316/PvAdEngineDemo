package com.dailyhunt.tv.players.interfaces;

/**
 * Interface for definition of Gif Item View
 *
 * @author ranjith
 */

public interface PlayerVideoGifView extends PlayerViewDH {

  void requestFullScreen();

  void setVideoViewLoaded(boolean isAutoPlaying);

  void onVideoViewPlayComplete(int videoDuration);

  void onMediaPlayerError(int what);

  void showLoader();

  void hideLoader();

  void showOrHideSettingsImage(boolean show);

  boolean isViewVisible();
}
