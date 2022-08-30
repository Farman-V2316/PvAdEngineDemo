package com.dailyhunt.tv.exolibrary.builder;

import com.dailyhunt.tv.exolibrary.VideoQualitySettings;
import com.google.android.exoplayer2.source.MediaSource;

/**
 * Created by rahul on 31/10/17.
 */

public final class ExoplayerConfig {

  private VideoQualitySettings qualitySettings;
  private boolean showControls;
  private MediaSource mediaSource;
  private boolean isAutoPlay;
  private boolean isLive;
  private boolean muteMode;
  private boolean applyBufferSettings;

  public static abstract class ExoPlayerConfigBuilder {
    protected ExoplayerConfig config;

    public ExoPlayerConfigBuilder() {
      createNewConfig();
    }

    public ExoplayerConfig getConfig() {
      return config;
    }

    public void createNewConfig() {
      config = new ExoplayerConfig();
    }

    public abstract void buildQualitySettings();

    public abstract void buildControls();

    public abstract void buildMediaSource();

    public abstract void buildStreamType();

    public abstract void buildMuteMode();

    public abstract void buildBufferSettings();
  }

  public VideoQualitySettings getQualitySettings() {
    return qualitySettings;
  }

  public void setQualitySettings(VideoQualitySettings qualitySettings) {
    this.qualitySettings = qualitySettings;
  }

  public boolean isShowControls() {
    return showControls;
  }

  public void setShowControls(boolean showControls) {
    this.showControls = showControls;
  }

  public MediaSource getMediaSource() {
    return mediaSource;
  }

  public void setMediaSource(MediaSource mediaSource) {
    this.mediaSource = mediaSource;
  }

  public boolean isAutoPlay() {
    return isAutoPlay;
  }

  public void setAutoPlay(boolean autoPlay) {
    isAutoPlay = autoPlay;
  }

  public boolean isLive() {
    return isLive;
  }

  public void setLive(boolean live) {
    isLive = live;
  }

  public boolean isMuteMode() {
    return muteMode;
  }

  public void setMuteMode(boolean muteMode) {
    this.muteMode = muteMode;
  }


  public boolean isApplyBufferSettings() {
    return applyBufferSettings;
  }

  public void setApplyBufferSettings(boolean applyBufferSettings) {
    this.applyBufferSettings = applyBufferSettings;
  }
}
