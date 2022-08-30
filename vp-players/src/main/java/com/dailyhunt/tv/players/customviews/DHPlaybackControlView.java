/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */
package com.dailyhunt.tv.players.customviews;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.dailyhunt.tv.players.R;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlayerLibraryInfo;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.ui.PlaybackControlView;
import com.google.android.exoplayer2.ui.TimeBar;
import com.google.android.exoplayer2.util.Assertions;
import com.newshunt.common.helper.common.Logger;
import com.newshunt.common.view.customview.fontview.NHTextView;
import com.newshunt.helper.player.PlayerControlHelper;

import java.util.Arrays;
import java.util.Formatter;
import java.util.Locale;

import androidx.annotation.Nullable;


public class DHPlaybackControlView extends FrameLayout {

  /**
   * @deprecated
   */
  @Deprecated
  public static final PlaybackControlView.ControlDispatcher DEFAULT_CONTROL_DISPATCHER;

  static {
    ExoPlayerLibraryInfo.registerModule("goog.exo.ui");
    DEFAULT_CONTROL_DISPATCHER = new DefaultControlDispatcher();
  }

  private final static String TAG = "DHPlaybackControlView";
  private ComponentListener componentListener;
  private final View muteButton;
  private final NHTextView durationView;
  private final NHTextView liveView;
  private final TimeBar timeBar;
  private final StringBuilder formatBuilder;
  private final Formatter formatter;
  private final Timeline.Period period;
  private final Timeline.Window window;
  private final Drawable repeatOffButtonDrawable;
  private final Drawable repeatOneButtonDrawable;
  private final Drawable repeatAllButtonDrawable;
  private final String repeatOffButtonContentDescription;
  private final String repeatOneButtonContentDescription;
  private final String repeatAllButtonContentDescription;
  private final Runnable updateProgressAction;
  private SimpleExoPlayer player;
  private com.google.android.exoplayer2.ControlDispatcher controlDispatcher;
  private ControlStateListener controlStateListener;
  private boolean isAttachedToWindow;
  private boolean showMultiWindowTimeBar;
  private boolean multiWindowTimeBar;
  private boolean scrubbing;
  private int rewindMs;
  private int fastForwardMs;
  private int showTimeoutMs;
  private int repeatToggleModes;
  private boolean showShuffleButton;
  private long hideAtMs;
  private long[] adGroupTimesMs;
  private boolean[] playedAdGroups;
  private long[] extraAdGroupTimesMs;
  private boolean[] extraPlayedAdGroups;
  private boolean isLive;
  public boolean isPauseByHaptickFeedBack = false;
  private boolean isHideControl;

  public DHPlaybackControlView(Context context) {
    this(context, (AttributeSet) null);
  }

  public DHPlaybackControlView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public DHPlaybackControlView(Context context, AttributeSet attrs, int defStyleAttr) {
    this(context, attrs, defStyleAttr, attrs);
  }

  public boolean isPauseByHaptickFeedBack() {
    return isPauseByHaptickFeedBack;
  }

  public DHPlaybackControlView(Context context, AttributeSet attrs, int defStyleAttr,
                               AttributeSet playbackAttrs) {
    super(context, attrs, defStyleAttr);
    this.updateProgressAction = new Runnable() {
      public void run() {
        updateProgress();
      }
    };
    int controllerLayoutId = R.layout.dh_playback_video_controls;
    this.rewindMs = 5000;
    this.fastForwardMs = 15000;
    this.showTimeoutMs = 6000;
    this.repeatToggleModes = 0;
    this.showShuffleButton = false;
    if (playbackAttrs != null) {
      TypedArray a = context.getTheme()
          .obtainStyledAttributes(playbackAttrs,
              com.google.android.exoplayer2.ui.R.styleable.PlayerControlView, 0, 0);

      try {
        this.rewindMs = a.getInt(
            com.google.android.exoplayer2.ui.R.styleable.PlayerControlView_rewind_increment,
            this.rewindMs);
        this.fastForwardMs = a.getInt(
            com.google.android.exoplayer2.ui.R.styleable.PlayerControlView_fastforward_increment,
            this.fastForwardMs);
        this.showTimeoutMs =
            a.getInt(com.google.android.exoplayer2.ui.R.styleable.PlayerControlView_show_timeout,
                this.showTimeoutMs);
        controllerLayoutId = R.layout.dh_playback_video_controls;
        this.repeatToggleModes = getRepeatToggleModes(a, this.repeatToggleModes);
        this.showShuffleButton = a.getBoolean(
            com.google.android.exoplayer2.ui.R.styleable.PlayerControlView_show_shuffle_button,
            this.showShuffleButton);
      } finally {
        a.recycle();
      }
    }

    this.period = new Timeline.Period();
    this.window = new Timeline.Window();
    this.formatBuilder = new StringBuilder();
    this.formatter = new Formatter(this.formatBuilder, Locale.getDefault());
    this.adGroupTimesMs = new long[0];
    this.playedAdGroups = new boolean[0];
    this.extraAdGroupTimesMs = new long[0];
    this.extraPlayedAdGroups = new boolean[0];
    this.componentListener = new ComponentListener();
    this.controlDispatcher = new DefaultControlDispatcher();
    LayoutInflater.from(context).inflate(controllerLayoutId, this);
    this.setDescendantFocusability(FOCUS_AFTER_DESCENDANTS);
    this.timeBar = (TimeBar) this.findViewById(R.id.dh_video_progress);
    this.durationView = this.findViewById(R.id.dhtv_video_duration);
    this.liveView = this.findViewById(R.id.dh_is_live);

    if (this.timeBar != null) {
      this.timeBar.addListener(this.componentListener);
    }

    this.muteButton = this.findViewById(R.id.dh_video_mute);

    if (this.muteButton != null) {
      this.muteButton.setOnClickListener(this.componentListener);
    }


    Resources resources = context.getResources();
    this.repeatOffButtonDrawable = resources.getDrawable(
        com.google.android.exoplayer2.ui.R.drawable.exo_controls_repeat_off);
    this.repeatOneButtonDrawable = resources.getDrawable(
        com.google.android.exoplayer2.ui.R.drawable.exo_controls_repeat_one);
    this.repeatAllButtonDrawable = resources.getDrawable(
        com.google.android.exoplayer2.ui.R.drawable.exo_controls_repeat_all);
    this.repeatOffButtonContentDescription = resources.getString(
        com.google.android.exoplayer2.ui.R.string.exo_controls_repeat_off_description);
    this.repeatOneButtonContentDescription = resources.getString(
        com.google.android.exoplayer2.ui.R.string.exo_controls_repeat_one_description);
    this.repeatAllButtonContentDescription = resources.getString(
        com.google.android.exoplayer2.ui.R.string.exo_controls_repeat_all_description);
  }

  public void show() {
    updateAll();
  }

  public void setLive(boolean live) {
    isLive = live;
    durationView.setVisibility(isLive ? View.GONE : View.VISIBLE);
    liveView.setVisibility(isLive ? View.VISIBLE : View.GONE);
  }

  public void setIsHideControl(boolean hideControl){
    isHideControl = hideControl;
  }

  private static int getRepeatToggleModes(TypedArray a, int repeatToggleModes) {
    return a.getInt(
        com.google.android.exoplayer2.ui.R.styleable.PlayerControlView_repeat_toggle_modes,
        repeatToggleModes);
  }

  @SuppressLint({"InlinedApi"})
  private static boolean isHandledMediaKey(int keyCode) {
    return keyCode == 90 || keyCode == 89 || keyCode == 85 || keyCode == 126 || keyCode == 127 ||
        keyCode == 87 || keyCode == 88;
  }

  private static boolean canShowMultiWindowTimeBar(Timeline timeline, Timeline.Window window) {
    if (timeline.getWindowCount() > 100) {
      return false;
    } else {
      int windowCount = timeline.getWindowCount();

      for (int i = 0; i < windowCount; ++i) {
        if (timeline.getWindow(i, window).durationUs == -9223372036854775807L) {
          return false;
        }
      }

      return true;
    }
  }

  public void resetPlayAndMuteButton() {
    muteButton.setSelected(PlayerControlHelper.isDetailMuteMode());
  }

  public void setDuration(String duration) {
    if (durationView != null) {
      durationView.setText(duration);
    }
  }

  public SimpleExoPlayer getPlayer() {
    return this.player;
  }

  public void setPlayer(SimpleExoPlayer player) {
    if (this.player != player) {
      if (this.player != null) {
        this.player.removeListener(this.componentListener);
      }

      this.player = player;
      if (player != null) {
        player.removeListener(this.componentListener);
        Logger.d(TAG, "this.componentListener : " + this.componentListener);
        if (this.componentListener != null) {
          Logger.d(TAG, "Adding componentListener");
          player.addListener(this.componentListener);
        }
      }

      if(this.isAttachedToWindow) {
        //Update the mute state to player
        muteAudio(PlayerControlHelper.isDetailMuteMode());
      }

      this.updateAll();
    }
  }

  public void setShowMultiWindowTimeBar(boolean showMultiWindowTimeBar) {
    this.showMultiWindowTimeBar = showMultiWindowTimeBar;
    this.updateTimeBarMode();
  }

  public void setExtraAdGroupMarkers(@Nullable long[] extraAdGroupTimesMs,
                                     @Nullable boolean[] extraPlayedAdGroups) {
    if (extraAdGroupTimesMs == null) {
      this.extraAdGroupTimesMs = new long[0];
      this.extraPlayedAdGroups = new boolean[0];
    } else {
      Assertions.checkArgument(extraAdGroupTimesMs.length == extraPlayedAdGroups.length);
      this.extraAdGroupTimesMs = extraAdGroupTimesMs;
      this.extraPlayedAdGroups = extraPlayedAdGroups;
    }

    this.updateProgress();
  }

  public void setControlStateListener(ControlStateListener listener) {
    controlStateListener = listener;
  }

  public void volumeButtonUnmuteTriggered() {
    updatePlayPauseButton();
  }

  public void setControlDispatcher(
      @Nullable com.google.android.exoplayer2.ControlDispatcher controlDispatcher) {
    this.controlDispatcher =
        (com.google.android.exoplayer2.ControlDispatcher) (controlDispatcher == null ?
            new com.google.android.exoplayer2.DefaultControlDispatcher() : controlDispatcher);
  }

  public void setRewindIncrementMs(int rewindMs) {
    this.rewindMs = rewindMs;
    this.updateNavigation();
  }

  public void setFastForwardIncrementMs(int fastForwardMs) {
    this.fastForwardMs = fastForwardMs;
    this.updateNavigation();
  }

  public int getShowTimeoutMs() {
    return this.showTimeoutMs;
  }

  public void setShowTimeoutMs(int showTimeoutMs) {
    this.showTimeoutMs = showTimeoutMs;
  }

  public boolean getShowShuffleButton() {
    return this.showShuffleButton;
  }

  public void setShowShuffleButton(boolean showShuffleButton) {
    this.showShuffleButton = showShuffleButton;
  }

  public boolean isVisible() {
    return this.getVisibility() == VISIBLE;
  }

  private void updateAll() {
    this.updatePlayPauseButton();
    this.updateNavigation();
    this.updateProgress();
    this.updateLiveStatus();
  }

  private void updateLiveStatus() {
    if (this.isVisible() && this.isAttachedToWindow) {
      if (durationView != null && liveView != null) {
        durationView.setVisibility(isLive ? View.GONE : View.VISIBLE);
        liveView.setVisibility(isLive ? View.VISIBLE : View.GONE);
      }
    }
    requestLayout();
  }

  private void updatePlayPauseButton() {
    if (this.isVisible() && this.isAttachedToWindow) {
      if (this.player != null) {
        if (controlStateListener != null) {
          controlStateListener.setPlayButtonState(!player.getPlayWhenReady());
        }
        muteButton.setSelected(PlayerControlHelper.isDetailMuteMode());
        muteAudio(muteButton.isSelected());
      }
    }
  }

  private void updateNavigation() {
    if (this.isVisible() && this.isAttachedToWindow) {
      Timeline timeline = this.player != null ? this.player.getCurrentTimeline() : null;
      boolean haveNonEmptyTimeline = timeline != null && !timeline.isEmpty();
      boolean isSeekable = false;
      boolean enablePrevious = false;
      boolean enableNext = false;
      if (haveNonEmptyTimeline && !this.player.isPlayingAd()) {
        int windowIndex = this.player.getCurrentWindowIndex();
        timeline.getWindow(windowIndex, this.window);
        isSeekable = this.window.isSeekable;
        enablePrevious =
            isSeekable || !this.window.isDynamic || this.player.getPreviousWindowIndex() != -1;
        enableNext = this.window.isDynamic || this.player.getNextWindowIndex() != -1;
      }

      if (this.timeBar != null) {
        this.timeBar.setEnabled(isLive ? false : isSeekable);
      }

    }
  }

  private void updateTimeBarMode() {
    if (this.player != null) {
      this.multiWindowTimeBar = this.showMultiWindowTimeBar &&
          canShowMultiWindowTimeBar(this.player.getCurrentTimeline(), this.window);
    }
  }

  private void updateProgress() {
    if (this.isVisible() && this.isAttachedToWindow) {
      long position = 0L;
      long bufferedPosition = 0L;
      long duration = 0L;
      if (this.player != null) {
        long currentWindowTimeBarOffsetUs = 0L;
        long durationUs = 0L;
        int adGroupCount = 0;
        Timeline timeline = this.player.getCurrentTimeline();
        int extraAdGroupCount;
        int totalAdGroupCount;
        if (!timeline.isEmpty()) {
          extraAdGroupCount = this.player.getCurrentWindowIndex();
          totalAdGroupCount = this.multiWindowTimeBar ? 0 : extraAdGroupCount;
          int lastWindowIndex =
              this.multiWindowTimeBar ? timeline.getWindowCount() - 1 : extraAdGroupCount;

          for (int i = totalAdGroupCount; i <= lastWindowIndex; ++i) {
            if (i == extraAdGroupCount) {
              currentWindowTimeBarOffsetUs = durationUs;
            }

            timeline.getWindow(i, this.window);
            if (this.window.durationUs == -9223372036854775807L) {
              Assertions.checkState(!this.multiWindowTimeBar);
              break;
            }

            for (int j = this.window.firstPeriodIndex; j <= this.window.lastPeriodIndex; ++j) {
              timeline.getPeriod(j, this.period);
              int periodAdGroupCount = this.period.getAdGroupCount();

              for (int adGroupIndex = 0; adGroupIndex < periodAdGroupCount; ++adGroupIndex) {
                long adGroupTimeInPeriodUs = this.period.getAdGroupTimeUs(adGroupIndex);
                if (adGroupTimeInPeriodUs == -9223372036854775808L) {
                  if (this.period.durationUs == -9223372036854775807L) {
                    continue;
                  }

                  adGroupTimeInPeriodUs = this.period.durationUs;
                }

                long adGroupTimeInWindowUs =
                    adGroupTimeInPeriodUs + this.period.getPositionInWindowUs();
                if (adGroupTimeInWindowUs >= 0L &&
                    adGroupTimeInWindowUs <= this.window.durationUs) {
                  if (adGroupCount == this.adGroupTimesMs.length) {
                    int newLength =
                        this.adGroupTimesMs.length == 0 ? 1 : this.adGroupTimesMs.length * 2;
                    this.adGroupTimesMs = Arrays.copyOf(this.adGroupTimesMs, newLength);
                    this.playedAdGroups = Arrays.copyOf(this.playedAdGroups, newLength);
                  }

                  this.adGroupTimesMs[adGroupCount] = C.usToMs(durationUs + adGroupTimeInWindowUs);
                  this.playedAdGroups[adGroupCount] = this.period.hasPlayedAdGroup(adGroupIndex);
                  ++adGroupCount;
                }
              }
            }

            durationUs += this.window.durationUs;
          }
        }

        duration = C.usToMs(durationUs);
        position = C.usToMs(currentWindowTimeBarOffsetUs);
        bufferedPosition = position;
        if (this.player.isPlayingAd()) {
          position += this.player.getContentPosition();
          bufferedPosition = position;
        } else {
          position += this.player.getCurrentPosition();
          bufferedPosition += this.player.getBufferedPosition();
        }

        if (this.timeBar != null) {
          extraAdGroupCount = this.extraAdGroupTimesMs.length;
          totalAdGroupCount = adGroupCount + extraAdGroupCount;
          if (totalAdGroupCount > this.adGroupTimesMs.length) {
            this.adGroupTimesMs = Arrays.copyOf(this.adGroupTimesMs, totalAdGroupCount);
            this.playedAdGroups = Arrays.copyOf(this.playedAdGroups, totalAdGroupCount);
          }

          System.arraycopy(this.extraAdGroupTimesMs, 0, this.adGroupTimesMs, adGroupCount,
              extraAdGroupCount);
          System.arraycopy(this.extraPlayedAdGroups, 0, this.playedAdGroups, adGroupCount,
              extraAdGroupCount);
          this.timeBar.setAdGroupTimesMs(this.adGroupTimesMs, this.playedAdGroups,
              totalAdGroupCount);
        }
      }


      if (this.timeBar != null && !isLive) {
        this.timeBar.setPosition(position);
        this.timeBar.setBufferedPosition(bufferedPosition);
        this.timeBar.setDuration(duration);
      } else {
        this.timeBar.setPosition(Integer.MAX_VALUE);
        this.timeBar.setBufferedPosition(Integer.MAX_VALUE);
        this.timeBar.setDuration(Integer.MAX_VALUE);
      }

      if (!isLive) {
        durationView.setText(stringForTime(duration, position));
      }

      this.removeCallbacks(this.updateProgressAction);
      int playbackState = this.player == null ? 1 : this.player.getPlaybackState();
      if (playbackState != 1 && playbackState != 4) {
        long delayMs;
        if (this.player.getPlayWhenReady() && playbackState == 3) {
          float playbackSpeed = this.player.getPlaybackParameters().speed;
          if (playbackSpeed <= 0.1F) {
            delayMs = 1000L;
          } else if (playbackSpeed <= 5.0F) {
            long mediaTimeUpdatePeriodMs =
                (long) (1000 / Math.max(1, Math.round(1.0F / playbackSpeed)));
            long mediaTimeDelayMs = mediaTimeUpdatePeriodMs - position % mediaTimeUpdatePeriodMs;
            if (mediaTimeDelayMs < mediaTimeUpdatePeriodMs / 5L) {
              mediaTimeDelayMs += mediaTimeUpdatePeriodMs;
            }

            delayMs = playbackSpeed == 1.0F ? mediaTimeDelayMs :
                (long) ((float) mediaTimeDelayMs / playbackSpeed);
          } else {
            delayMs = 200L;
          }
        } else {
          delayMs = 1000L;
        }

        this.postDelayed(this.updateProgressAction, delayMs);
      }

    }
  }

  private String stringForTime(long duration, long positionMs) {
    if (positionMs == C.TIME_UNSET) {
      positionMs = 0;
    }
    long totalSeconds = (positionMs + 500) / 1000;
    if(totalSeconds < 0) {
      totalSeconds = 0;
    }
    long seconds = totalSeconds % 60;
    long minutes = (totalSeconds / 60) % 60;
    long hours = totalSeconds / 3600;
    formatBuilder.setLength(0);
    String positionStr = hours > 0 ? formatter.format("%d:%02d:%02d", hours, minutes, seconds).toString()
            : formatter.format("%d:%02d", minutes, seconds).toString();

    long totalDurationSeconds = (duration + 500) / 1000;
    if(totalDurationSeconds < 0) {
      totalDurationSeconds = 0;
    }
    long durationSeconds = totalDurationSeconds % 60;
    long durationMinutes = (totalDurationSeconds / 60) % 60;
    long durationHours = totalDurationSeconds / 3600;
    formatBuilder.setLength(0);
    String durationStr = durationHours > 0 ? formatter.format("%d:%02d:%02d", durationHours, durationMinutes, durationSeconds).toString()
            : formatter.format("%d:%02d", durationMinutes, durationSeconds).toString();
    return positionStr + " / " + durationStr;
  }

  private void setButtonEnabled(boolean enabled, View view) {
    if (view != null) {
      view.setEnabled(enabled);
      view.setAlpha(enabled ? 1.0F : 0.3F);
      view.setVisibility(VISIBLE);
    }
  }

  private void previous() {
    Timeline timeline = this.player.getCurrentTimeline();
    if (!timeline.isEmpty()) {
      int windowIndex = this.player.getCurrentWindowIndex();
      timeline.getWindow(windowIndex, this.window);
      int previousWindowIndex = this.player.getPreviousWindowIndex();
      if (previousWindowIndex == -1 || this.player.getCurrentPosition() > 3000L &&
          (!this.window.isDynamic || this.window.isSeekable)) {
        this.seekTo(0L);
      } else {
        this.seekTo(previousWindowIndex, -9223372036854775807L);
      }

    }
  }

  private void next() {
    Timeline timeline = this.player.getCurrentTimeline();
    if (!timeline.isEmpty()) {
      int windowIndex = this.player.getCurrentWindowIndex();
      int nextWindowIndex = this.player.getNextWindowIndex();
      if (nextWindowIndex != -1) {
        this.seekTo(nextWindowIndex, -9223372036854775807L);
      } else if (timeline.getWindow(windowIndex, this.window, false).isDynamic) {
        this.seekTo(windowIndex, -9223372036854775807L);
      }

    }
  }

  private void rewind() {
    if (this.rewindMs > 0) {
      this.seekTo(Math.max(this.player.getCurrentPosition() - (long) this.rewindMs, 0L));
    }
  }

  private void fastForward() {
    if (this.fastForwardMs > 0) {
      long durationMs = this.player.getDuration();
      long seekPositionMs = this.player.getCurrentPosition() + (long) this.fastForwardMs;
      if (durationMs != -9223372036854775807L) {
        seekPositionMs = Math.min(seekPositionMs, durationMs);
      }

      this.seekTo(seekPositionMs);
    }
  }

  private void seekTo(long positionMs) {
    this.seekTo(this.player.getCurrentWindowIndex(), positionMs);
  }

  private void seekTo(int windowIndex, long positionMs) {
    boolean dispatched =
        this.controlDispatcher.dispatchSeekTo(this.player, windowIndex, positionMs);
    if (!dispatched) {
      this.updateProgress();
    }

  }

  private void seekToTimeBarPosition(long positionMs) {
    Timeline timeline = this.player.getCurrentTimeline();
    int windowIndex;
    if (this.multiWindowTimeBar && !timeline.isEmpty()) {
      int windowCount = timeline.getWindowCount();
      windowIndex = 0;

      while (true) {
        long windowDurationMs = timeline.getWindow(windowIndex, this.window).getDurationMs();
        if (positionMs < windowDurationMs) {
          break;
        }

        if (windowIndex == windowCount - 1) {
          positionMs = windowDurationMs;
          break;
        }

        positionMs -= windowDurationMs;
        ++windowIndex;
      }
    } else {
      windowIndex = this.player.getCurrentWindowIndex();
    }

    this.seekTo(windowIndex, positionMs);
  }

  public void onAttachedToWindow() {
    super.onAttachedToWindow();
    Logger.d(TAG, "onAttachedToWindow");
    this.isAttachedToWindow = true;
    this.updateAll();
  }

  public void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    Logger.d(TAG, "onDetachedFromWindow");
    this.isAttachedToWindow = false;
  }

  public void removeListener() {
    this.removeCallbacks(this.updateProgressAction);
    controlStateListener = null;

    if (timeBar != null) {
      timeBar.removeListener(this.componentListener);
    }

    if (player != null) {
      player.removeListener(this.componentListener);
    }

    componentListener = null;
  }

  public void setRotationAngle(float angle) {
    if(timeBar != null && timeBar instanceof CustomVideoTimeBar) {
      CustomVideoTimeBar customVideoTimeBar = (CustomVideoTimeBar) timeBar;
      customVideoTimeBar.setRotationAngle(angle);
    }
  }

  public boolean dispatchKeyEvent(KeyEvent event) {
    return this.dispatchMediaKeyEvent(event) || super.dispatchKeyEvent(event);
  }

  public boolean dispatchMediaKeyEvent(KeyEvent event) {
    int keyCode = event.getKeyCode();
    if (this.player != null && isHandledMediaKey(keyCode)) {
      if (event.getAction() == 0) {
        if (keyCode == 90) {
          this.fastForward();
        } else if (keyCode == 89) {
          this.rewind();
        } else if (event.getRepeatCount() == 0) {
          switch (keyCode) {
            case 85:
              this.controlDispatcher.dispatchSetPlayWhenReady(this.player,
                  !this.player.getPlayWhenReady());
              break;
            case 87:
              this.next();
              break;
            case 88:
              this.previous();
              break;
            case 126:
              this.controlDispatcher.dispatchSetPlayWhenReady(this.player, true);
              break;
            case 127:
              this.controlDispatcher.dispatchSetPlayWhenReady(this.player, false);
          }
        }
      }

      return true;
    } else {
      return false;
    }
  }

  private void muteAudio(boolean state) {
    //Mute if controls are Hidden
    if (isHideControl) {
      state = true;
    }
    if (player != null) {
      player.setVolume(state ? 0.0f : 1.0f);
    }
  }

  public interface ControlStateListener {
    void setPlayButtonState(boolean isSelected);

    void muteModeChanged(boolean isMuteMode);
  }

  /**
   * @deprecated
   */
  @Deprecated
  public interface ControlDispatcher extends com.google.android.exoplayer2.ControlDispatcher {
  }

  private static final class DefaultControlDispatcher
      extends com.google.android.exoplayer2.DefaultControlDispatcher
      implements PlaybackControlView.ControlDispatcher {
    private DefaultControlDispatcher() {
    }
  }

  private final class ComponentListener extends Player.DefaultEventListener
      implements TimeBar.OnScrubListener, OnClickListener {
    private ComponentListener() {
    }

    public void onScrubStart(TimeBar timeBar, long position) {
      scrubbing = true;
    }

    public void onScrubMove(TimeBar timeBar, long position) {

    }

    public void onScrubStop(TimeBar timeBar, long position, boolean canceled) {
      scrubbing = false;
      if (!canceled && player != null) {
        seekToTimeBarPosition(position);
      }
    }

    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
      updatePlayPauseButton();
      updateProgress();
    }

    public void onRepeatModeChanged(int repeatMode) {
      updateNavigation();
    }

    public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {
      updateNavigation();
    }

    public void onPositionDiscontinuity(int reason) {
      updateNavigation();
      updateProgress();
    }

    public void onTimelineChanged(Timeline timeline, Object manifest) {
      updateNavigation();
      updateTimeBarMode();
      updateProgress();
    }

    public void onClick(View view) {
      if (player != null) {
        if (muteButton == view) {
          if (muteButton.isSelected()) {
            muteButton.setSelected(false);
            PlayerControlHelper.setDetailMuteMode(false);
          } else {
            muteButton.setSelected(true);
            PlayerControlHelper.setDetailMuteMode(true);
          }
          muteAudio(muteButton.isSelected());
          if(controlStateListener != null) {
            controlStateListener.muteModeChanged(muteButton.isSelected());
          }
        }
      }
    }
  }
}
