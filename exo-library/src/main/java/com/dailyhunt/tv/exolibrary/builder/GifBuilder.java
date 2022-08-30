package com.dailyhunt.tv.exolibrary.builder;

import android.content.Context;
import android.net.Uri;

import com.dailyhunt.tv.exolibrary.VideoQualitySettings;
import com.dailyhunt.tv.exolibrary.util.MediaSourceUtil;
import com.google.android.exoplayer2.source.LoopingMediaSource;
import com.google.android.exoplayer2.source.MediaSource;

/**
 * Created by rahul on 31/10/17.
 */

public class GifBuilder extends ExoplayerConfig.ExoPlayerConfigBuilder {
  private VideoQualitySettings qualitySettings;
  private Uri uri;
  private int loop_count = 0;
  private Context context;

  public GifBuilder(final Context context,
                    final VideoQualitySettings qualitySettings,
                    final Uri uri,
                    final  int loopCount) {
    this.qualitySettings = qualitySettings;
    this.uri = uri;
    this.loop_count = loopCount;
    this.context = context;
    init();
  }


  private void init() {
    buildControls();
    buildMediaSource();
    buildQualitySettings();
    buildStreamType();
    buildMuteMode();
  }


  @Override
  public void buildQualitySettings() {
      config.setQualitySettings(qualitySettings);
  }

  @Override
  public void buildControls() {
      config.setShowControls(false);
  }

  @Override
  public void buildMediaSource() {
    MediaSource source = MediaSourceUtil.getMappedSource(context, uri, false);
    config.setMediaSource(new LoopingMediaSource(source, loop_count));
  }

  @Override
  public void buildStreamType() {
    config.setLive(false);
  }

  @Override
  public void buildMuteMode() {
    config.setMuteMode(false);
  }

  @Override
  public void buildBufferSettings() {

  }
}
