/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */
package com.newshunt.dhutil.commons.listener;

/**
 * Created by karthik.r on 23/05/18.
 */
public interface VideoPlayerProvider {
  Object getVideoPlayerWrapper();

  void setVideoPlayerWrapper(Object player);

  default void handleVideoBack(Object videoAnalyticsHelper) {
  }

  default Object getVideoAnalyticsHelper() {
    return null;
  }

  void onAutoPlayCardClick();

  void loadThumbnailForBackup();

  boolean getNLFCRequestStatus();

  boolean isAutoImmersiveMode();

  void setImmersiveModeAsConsumed();

}
