/*
 * Copyright (c) 2016 Newshunt. All rights reserved.
 */
package com.dailyhunt.tv.players.listeners;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;

/**
 * Listener to CustomYoutubeFragment for any 'State' and 'Error' Callbacks
 *
 * Created by Jayanth on 09/05/18.
 */
public interface PlayerCustomYoutubeListener {

  void onAdStarted();

  void onVideoStarted();

  void releaseTouchHandler();

  void onVideoEnded();

  void onError(YouTubePlayer.ErrorReason errorReason);

  void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer,
                               boolean wasRestored);

  void onInitializationFailure(YouTubePlayer.Provider provider,
                               YouTubeInitializationResult youTubeInitializationResult);

  void onFullscreen(boolean isFullscreen);

  Boolean isViewInForeground();


}
