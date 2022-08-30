package com.dailyhunt.tv.players.interfaces;

/**
 * Interface for definition of Exo Item View
 *
 * @author ranjith
 */

public interface PlayerExoView extends PlayerViewDH {

  void requestFullScreen();

  void onVideoViewPlayComplete(int videoDuration);

}
