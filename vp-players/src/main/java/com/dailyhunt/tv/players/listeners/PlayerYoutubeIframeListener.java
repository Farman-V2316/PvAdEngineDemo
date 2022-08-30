package com.dailyhunt.tv.players.listeners;

/**
 * Created by Jayanth on 09/05/18.
 */
public interface PlayerYoutubeIframeListener {

  void onYIFramePlayerReady();

  void onYIFramePlayerError();

  void onYIFramePlayerComplete();

  boolean isFragmentAdded();

}
