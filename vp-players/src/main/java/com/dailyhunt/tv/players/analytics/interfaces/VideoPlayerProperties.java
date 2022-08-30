package com.dailyhunt.tv.players.analytics.interfaces;


import com.dailyhunt.tv.players.analytics.enums.PlayerVideoEndAction;
import com.dailyhunt.tv.players.analytics.enums.PlayerVideoStartAction;

public interface VideoPlayerProperties {
  void setFullScreenMode(boolean state);
  void setVideoEndAction(PlayerVideoEndAction action);
  long playbackDuration();
}
