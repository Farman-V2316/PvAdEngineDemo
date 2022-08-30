/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */
package com.dailyhunt.tv.ima.player.exo;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import com.dailyhunt.tv.exolibrary.ui.CustomRenderersFactory;
import com.dailyhunt.tv.exolibrary.util.EventLogger;
import com.dailyhunt.tv.exolibrary.util.ExoUtils;
import com.dailyhunt.tv.exolibrary.util.MediaSourceUtil;
import com.dailyhunt.tv.ima.IMALogger;
import com.dailyhunt.tv.ima.player.CustomAdsPlayer;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.PlayerView;
import com.newshunt.common.helper.common.AndroidUtils;
import com.newshunt.common.helper.common.Logger;
import com.newshunt.dataentity.common.helper.common.CommonUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * An exoplayer that intercepts various methods and reports them back via a PlayerCallback.
 *
 * @author raunak.yadav
 */
public class AdsExoPlayer implements CustomAdsPlayer, Player.EventListener {

  private static final String TAG = "AdsExoPlayer";
  private static final int BUFFER_SEGMENT_SIZE = 64 * 1024;

  public AdsExoPlayer(PlayerView playerView) {
    this.playerView = playerView;
    new Handler(Looper.getMainLooper()).post(this::init);
  }

  private enum PlaybackState {
    STOPPED, PAUSED, PLAYING
  }

  private Context context;
  private PlaybackState mPlaybackState;
  private SimpleExoPlayer mediaPlayer;
  private boolean muteState = true;
  private PlayerView playerView;
  private EventLogger logger;
  private boolean qualifiedForImmersive = false;
  private boolean endLoopOnImmersive = false;
  private int immersiveTransitionSpan = -1;
  private final List<PlayerCallback> mVideoPlayerCallbacks = new ArrayList<PlayerCallback>(1);

  private  Runnable immersiveRunnable = () -> {
    if(mPlaybackState == PlaybackState.PLAYING) {
      for (PlayerCallback c: mVideoPlayerCallbacks) {
        c.triggerImmersive();
      }
    }
  };

  private void init() {
    mPlaybackState = PlaybackState.STOPPED;

    enablePlaybackControls();
    context = playerView.getContext();
    TrackSelection.Factory adaptivetrackFactory = new AdaptiveTrackSelection.Factory();
    DefaultTrackSelector trackSelector = new DefaultTrackSelector(adaptivetrackFactory);
    CustomRenderersFactory renderersFactory = new CustomRenderersFactory(context);
    mediaPlayer = new SimpleExoPlayer.Builder(context, renderersFactory)
            .setTrackSelector(trackSelector)
            .setBandwidthMeter(ExoUtils.getBANDWIDTH_METER())
            .build();
    setMuteState(muteState);
    playerView.setPlayer(mediaPlayer);
    mediaPlayer.addListener(this);
    //logger
    logger = new EventLogger(trackSelector);
    mediaPlayer.addListener(logger);
    mediaPlayer.setVideoFrameMetadataListener((presentationTimeUs, releaseTimeNs, format, mediaFormat) -> {
      long diff = (immersiveTransitionSpan * 1000L) - (presentationTimeUs /1000);
      if(mediaPlayer.getPlayWhenReady() && qualifiedForImmersive
              && !endLoopOnImmersive && immersiveTransitionSpan > -1 && diff == 0L) {
        endLoopOnImmersive = true;
        //call immersive mode after play of X seconds of the video
        AndroidUtils.getMainThreadHandler().removeCallbacks(immersiveRunnable);
        AndroidUtils.getMainThreadHandler().post( immersiveRunnable);
      } else {
        //re-shift the transition span if not gone into immersive because of miss span
        if(!endLoopOnImmersive && (diff < 0L && diff > -50L)) {
          immersiveTransitionSpan += immersiveTransitionSpan;
        }
      }
    });
  }

  // Methods implementing the CustomAdsPlayer interface.

  @Override
  public long getDuration() {
    return mPlaybackState == PlaybackState.STOPPED || mediaPlayer == null ? 0 :
        mediaPlayer.getDuration();
  }

  @Override
  public int getVolume() {
    if (mediaPlayer == null) {
      return 0;
    }

    Player.AudioComponent audioComponent = mediaPlayer.getAudioComponent();
    if (audioComponent != null) {
      return Math.round(audioComponent.getVolume() * 100);
    }

    // Check for a selected track using an audio renderer.
    TrackSelectionArray trackSelections = mediaPlayer.getCurrentTrackSelections();
    if (trackSelections == null) {
      return 0;
    }
    for (int i = 0; i < mediaPlayer.getRendererCount() && i < trackSelections.length; i++) {
      if (mediaPlayer.getRendererType(i) == C.TRACK_TYPE_AUDIO && trackSelections.get(i) != null) {
        return 100;
      }
    }
    return 0;
  }

  @Override
  public void play() {
    if (mediaPlayer == null) {
      return;
    }
    mediaPlayer.setPlayWhenReady(true);
    for (PlayerCallback callback : mVideoPlayerCallbacks) {
      callback.onPlay();
    }
    mPlaybackState = PlaybackState.PLAYING;
  }

  @Override
  public void resume() {
    if (mediaPlayer == null) {
      return;
    }
    mediaPlayer.setPlayWhenReady(true);
    for (PlayerCallback callback : mVideoPlayerCallbacks) {
      callback.onResume();
    }
    mPlaybackState = PlaybackState.PLAYING;
  }

  @Override
  public long getCurrentPosition() {
    return mediaPlayer == null ? 0 : mediaPlayer.getCurrentPosition();
  }

  @Override
  public void seekTo(long videoPosition) {
    if (mediaPlayer != null) {
      mediaPlayer.seekTo(videoPosition);
    }
  }

  @Override
  public void pause() {
    if (mediaPlayer == null) {
      return;
    }
    mediaPlayer.setPlayWhenReady(false);
    mPlaybackState = PlaybackState.PAUSED;
    for (PlayerCallback callback : mVideoPlayerCallbacks) {
      callback.onPause();
    }
  }

  @Override
  public void stopPlayback() {
    if (mPlaybackState == PlaybackState.STOPPED || mediaPlayer == null) {
      return;
    }
    mediaPlayer.stop();
    mPlaybackState = PlaybackState.STOPPED;
  }

  @Override
  public void setQualifiedForImmersive(Boolean state) {
      this.qualifiedForImmersive = state;
  }

  @Override
  public void setImmersiveSpan(int duration) {
      this.immersiveTransitionSpan = duration;
  }

  @Override
  public void disablePlaybackControls() {
    playerView.hideController();
  }

  @Override
  public void enablePlaybackControls() {
    playerView.showController();
  }

  @Override
  public void setVideoPath(String videoUrl) {
    if (mediaPlayer == null) {
      return;
    }
    Logger.d(TAG," video url for IMA: "+videoUrl);
    // This is the MediaSource representing the content media (i.e. not the ad).
    Uri videoUri = Uri.parse(CommonUtils.isEmpty(videoUrl) ? "http://" : videoUrl);
    MediaSource contentMediaSource = MediaSourceUtil.getMappedSource(context, videoUri, false);
    mediaPlayer.prepare(contentMediaSource);
  }

  @Override
  public void addPlayerCallback(PlayerCallback callback) {
    mVideoPlayerCallbacks.add(callback);
  }

  @Override
  public void removePlayerCallback(PlayerCallback callback) {
    mVideoPlayerCallbacks.remove(callback);
  }



  @Override
  public void setMuteState(boolean muteState) {
    this.muteState = muteState;
    if (mediaPlayer != null) {
      float newVolume = muteState ? 0.0f : 1.0f;
      mediaPlayer.setVolume(newVolume);

      for (PlayerCallback callback : mVideoPlayerCallbacks) {
        callback.onVolumeChanged((int) newVolume * 100);
      }
    }
  }

  @Override
  public void release() {
    IMALogger.d(TAG, "Release player");
    if (mediaPlayer != null) {
      mediaPlayer.removeListener(this);
      mediaPlayer.removeListener(logger);
      mediaPlayer.release();
      mediaPlayer = null;

      playerView.setPlayer(null);
    }
    context = null;
    logger = null;
    endLoopOnImmersive = false;
  }

  // Player.EventListener implementation

  @Override
  public void onTimelineChanged(Timeline timeline, Object manifest,
                                @Player.TimelineChangeReason int reason) {
    //Not required.
  }

  @Override
  public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
    //Not required.
  }

  @Override
  public void onLoadingChanged(boolean isLoading) {
    //Not required.
  }

  @Override
  public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
    if (mediaPlayer == null) {
      return;
    }
    switch (playbackState) {
      case Player.STATE_ENDED:
        if (mPlaybackState == PlaybackState.STOPPED) {
          return;
        }
        mPlaybackState = PlaybackState.STOPPED;
        //oncomplete
        if (mediaPlayer.getPlayWhenReady()) {
          for (PlayerCallback callback : mVideoPlayerCallbacks) {
            callback.onCompleted();
          }
        }
        break;
    }
  }

  @Override
  public void onRepeatModeChanged(int repeatMode) {
    //Not required.
  }

  @Override
  public void onPlayerError(ExoPlaybackException error) {
    mPlaybackState = PlaybackState.STOPPED;
    for (PlayerCallback callback : mVideoPlayerCallbacks) {
      callback.onError();
    }
  }

  @Override
  public void onPositionDiscontinuity(int reason) {

  }

  @Override
  public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
    //Not required.
  }

  @Override
  public void onSeekProcessed() {

  }

  @Override
  public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {

  }
}

