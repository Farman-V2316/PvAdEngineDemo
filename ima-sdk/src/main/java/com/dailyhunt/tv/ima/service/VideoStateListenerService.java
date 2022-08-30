package com.dailyhunt.tv.ima.service;

import com.dailyhunt.tv.ima.entity.state.VideoState;

/**
 * Listener from the Video Player when the Video state is changed
 *
 * @author ranjith
 */

public interface VideoStateListenerService {

  /**
   * Call when Video is un known state
   */
  void onVideoUnknownState();

  /**
   * Call when video prepare in progress
   */
  void onVideoPrepareInProgress();

  /**
   * Call when video is prepared
   */
  void onVideoPrepared();

  /**
   * Call when video is playing
   */
  void onVideoPlaying();

  /**
   * Call when video is playing
   */
  void onVideoPaused();

  /**
   * Call when video is play is completed
   */
  void onVideoPlayComplete();

  /**
   * Call when video error occur
   */
  void onVideoError();

  /**
   * Call when video quality change
   */

  void onVideoQualityChange();

  /**
   * Method to return current Video State
   *
   * @return -- Video State
   */
  VideoState getVideoState();
}
