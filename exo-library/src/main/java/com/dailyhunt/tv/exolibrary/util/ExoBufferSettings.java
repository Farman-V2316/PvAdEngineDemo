/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */
package com.dailyhunt.tv.exolibrary.util;

import static com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection.DEFAULT_MAX_DURATION_FOR_QUALITY_DECREASE_MS;
import static com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection.DEFAULT_MIN_DURATION_FOR_QUALITY_INCREASE_MS;

/**
 * Created by umesh.isran on 11/06/2018.
 */


public class ExoBufferSettings {
  private static int bufferMinSize;
  private static int bufferMaxSize;
  private static int hlsMinTimeForSwitchUpMs = DEFAULT_MIN_DURATION_FOR_QUALITY_INCREASE_MS;
  private static int hlsMaxTimeForSwitchDownMs = DEFAULT_MAX_DURATION_FOR_QUALITY_DECREASE_MS;
  private static int bufferSegmentSize;
  private static int initialBufferMs;
  private static int playbackDurationAfterRebuffer;
  private static boolean shouldUseDefault;

    public static void setBufferSettings(int bufferMinSize,
                                         int bufferMaxSize,
                                         int hlsMinTimeForSwitchUpMs,
                                         int hlsMaxTimeForSwitchDownMs,
                                         int bufferSegmentSize,
                                         int initialBufferMs,
                                         int playbackDurationAfterRebuffer,
                                         boolean shouldUseDefault) { // extra fields made server config. but default served are from exo
      ExoBufferSettings.bufferMinSize = bufferMinSize;
      ExoBufferSettings.bufferMaxSize = bufferMaxSize;
      ExoBufferSettings.hlsMinTimeForSwitchUpMs = hlsMinTimeForSwitchUpMs;
      ExoBufferSettings.hlsMaxTimeForSwitchDownMs = hlsMaxTimeForSwitchDownMs;
      ExoBufferSettings.bufferSegmentSize = bufferSegmentSize;
      ExoBufferSettings.initialBufferMs = initialBufferMs;
      ExoBufferSettings.playbackDurationAfterRebuffer = playbackDurationAfterRebuffer;
      ExoBufferSettings.shouldUseDefault = shouldUseDefault;
  }

  public static int getBufferMinSize() {
    return bufferMinSize;
  }

  public static int getBufferMaxSize() {
    return bufferMaxSize;
  }


    public static int getHlsMinTimeForSwitchUpMs() {
        return hlsMinTimeForSwitchUpMs;
    }

    public static int getHlsMaxTimeForSwitchDownMs() {
        return hlsMaxTimeForSwitchDownMs;
    }

    public static int getBufferSegmentSize() {
        return bufferSegmentSize;
    }

    public static int getInitialBufferMs() {
        return initialBufferMs;
    }

  public static boolean isShouldUseDefault() {
    return shouldUseDefault;
  }

  public static int getPlaybackDurationAfterRebuffer() {
    return playbackDurationAfterRebuffer;
  }

  public static void setPlaybackDurationAfterRebuffer(int playbackDurationAfterRebuffer) {
    ExoBufferSettings.playbackDurationAfterRebuffer = playbackDurationAfterRebuffer;
  }
}
