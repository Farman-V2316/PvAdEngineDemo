package com.dailyhunt.tv.players.builders;

import android.content.Context;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;

import com.dailyhunt.tv.exolibrary.VideoQualitySettings;
import com.dailyhunt.tv.exolibrary.builder.ExoplayerConfig;
import com.dailyhunt.tv.exolibrary.util.MediaSourceUtil;
import com.dailyhunt.tv.ima.exo.AdsMediaSourceFactory;
import com.dailyhunt.tv.ima.exo.ImaAdsLoader;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ads.AdsLoader;
import com.google.android.exoplayer2.source.ads.AdsMediaSource;
import java.util.ArrayList;


/**
 * Created by rahul on 31/10/17.
 */

public class VideoBuilder extends ExoplayerConfig.ExoPlayerConfigBuilder {

  private VideoQualitySettings qualitySettings;
  private Uri uri;
  private Context context;
  private boolean live;
  private boolean muteMode;
  private boolean applyBufferSettings;
  private ImaAdsLoader adsLoader;
  private ViewGroup adViewGroup;

  public VideoBuilder(final Context context,
                      final VideoQualitySettings qualitySettings,
                      final Uri uri,
                      final boolean isAutoplay,
                      final boolean isLive,
                      final boolean muteMode,
                      final boolean applyBufferSettings,
                      final ImaAdsLoader adsLoader,
                      final ViewGroup adViewGroup) {
    super();
    this.qualitySettings = qualitySettings;
    this.uri = uri;
    this.context = context;
    this.live = isLive;
    this.muteMode = muteMode;
    this.applyBufferSettings = applyBufferSettings;
    this.adsLoader = adsLoader;
    this.adViewGroup = adViewGroup;
    getConfig().setAutoPlay(isAutoplay);
    init();
  }

  private void init() {
    buildControls();
    buildMediaSource();
    buildQualitySettings();
    buildStreamType();
    buildMuteMode();
    buildBufferSettings();
  }

  @Override
  public void buildMuteMode() {
    config.setMuteMode(muteMode);
  }

  @Override
  public void buildBufferSettings() {
    config.setApplyBufferSettings(applyBufferSettings);
  }

  @Override
  public void buildQualitySettings() {
    config.setQualitySettings(qualitySettings);
  }

  @Override
  public void buildControls() {
    config.setShowControls(true);
  }

  @Override
  public void buildMediaSource() {
    MediaSource source = MediaSourceUtil.getMappedSource(context, uri, live);
    if (adsLoader != null) {
      source = new AdsMediaSource(source, new AdsMediaSourceFactory(),
          adsLoader, new AdsLoader.AdViewProvider() {
        @Override
        public View[] getAdOverlayViews() {
          return new ArrayList<View>().toArray(new View[0]);
        }

        @Override
        public ViewGroup getAdViewGroup() {
          return adViewGroup;
        }
      });
    }
    config.setMediaSource(source);
  }

  @Override
  public void buildStreamType() {
    config.setLive(live);
  }


}
