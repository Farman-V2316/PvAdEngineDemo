package com.dailyhunt.tv.exolibrary.listeners;

/**
 * Created by ketkigarg on 19/12/17.
 */

public interface VideoControlsSoundListener {
  void setAudioToMute();

  void setAudioToUnMute();

  boolean getAudioMuteState();
}
