package com.dailyhunt.tv.players.interfaces;

import com.dailyhunt.tv.players.customviews.CompanionAdView;

import org.jetbrains.annotations.Nullable;

import androidx.lifecycle.LifecycleOwner;

/**
 * Created by vinod on 02/11/16.
 */

public interface PlayerCallbacks extends PlayerAnalyticCallbacks {

  void toggleUIForFullScreen(boolean isFullScreen);

  boolean isVideoInNewsList();

  @Nullable
  default CompanionAdView getCompanionAdView() {
    return null;
  }

  default boolean isAnyActivePlayer() {
    return false;
  }

  default void canShowUpNextVideoCard() {
  }

  default void onVideoResumed() {
  }

  default void onVideoInDetail() {
  }

  LifecycleOwner getLifeCycleOwner();

  Boolean isViewInForeground();

  default void onRenderedFirstFrame() {}
}
