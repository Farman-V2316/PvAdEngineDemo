/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */
package com.dailyhunt.tv.ima.player.exo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.dailyhunt.tv.exolibrary.util.ExoUtils;
import com.dailyhunt.tv.ima.R;
import com.dailyhunt.tv.ima.player.CustomAdsPlayer;
import com.google.ads.interactivemedia.v3.api.AdsManager;
import com.google.ads.interactivemedia.v3.api.CompanionAdSlot;
import com.google.ads.interactivemedia.v3.api.player.ContentProgressProvider;
import com.google.ads.interactivemedia.v3.api.player.VideoAdPlayer;
import com.google.ads.interactivemedia.v3.api.player.VideoProgressUpdate;
import com.google.android.exoplayer2.ui.DefaultTimeBar;
import com.google.android.exoplayer2.ui.PlayerView;
import com.newshunt.common.helper.common.BusProvider;
import com.newshunt.common.helper.common.Constants;
import com.newshunt.common.helper.common.DataUtil;
import com.newshunt.common.helper.common.ViewUtils;
import com.newshunt.helper.player.PlaySettingsChangedEvent;
import com.newshunt.helper.player.PlayerControlHelper;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import androidx.constraintlayout.widget.ConstraintLayout;

/**
 * Video player that can play content video and ads.
 *
 * @author raunak.yadav
 */
public class VideoPlayerWithAdPlayback extends RelativeLayout implements View.OnClickListener {

  /**
   * Interface for alerting caller of video completion.
   */
  public interface OnContentCompleteListener {
    void onContentComplete();
  }

  public interface AdControlsListener {
    void onPlayTapped();

    void onPauseTapped();

    void onAdProgress(String timeLeft);

    default @Nullable View immersiveADview() { return null; }

    default void reinsertAdview(View view) {}

    default void onPlayerTapped(boolean defaultClick) {}
  }

  private AdsManager adsManager;
  // The wrapped video player.
  private CustomAdsPlayer mVideoPlayer;

  private ImageView thumbnail;
  private ImageView mMuteButton;
  private View playButton, pauseButton;
  private ImageView immersiveMuteButton;
  private int defaultCompanionTransitionTime = -1;
  private DefaultTimeBar timeBar;
  private TextView timeDisplay;
  private TextView learnMoreView;

  private ConstraintLayout nonImmersiveControlsContainer;
  private ConstraintLayout immersiveControlsContainer;
  private ViewFlipper companionContainer;

  // The SDK will render ad playback UI elements into this ViewGroup.
  private ViewGroup mAdUiContainer;

  // Used to track if the current video is an ad (as opposed to a content video).
  private boolean mIsAdDisplayed;

  private String mContentVideoUrl;

  // The saved position in the ad to resume if app is backgrounded during ad playback.
  private long mSavedAdPosition;

  // The saved position in the content to resume to after ad playback or if app is backgrounded
  // during content playback.
  private long mSavedContentPosition;

  private boolean isMute;

  private boolean followGlobalMute;
  private boolean canShowCustomCTA;

  private AdControlsListener adControlsListener;

  // Called when the content is completed.
  private OnContentCompleteListener mOnContentCompleteListener;

  // VideoAdPlayer interface implementation for the SDK to send ad play/pause type events.
  private VideoAdPlayer mVideoAdPlayer;

  // ContentProgressProvider interface implementation for the SDK to check content progress.
  private ContentProgressProvider mContentProgressProvider;

  private final List<VideoAdPlayer.VideoAdPlayerCallback> mAdCallbacks =
      new ArrayList<VideoAdPlayer.VideoAdPlayerCallback>(1);

  private final List<Pair<CompanionAdSlot, CompanionAdSlot.ClickListener>> companionAdSlots = new ArrayList();

  private CustomAdsPlayer.PlayerCallback playerCallback;

  private List<ImageView> muteButtonClickList  =new ArrayList<>();

  public VideoPlayerWithAdPlayback(Context context) {
    super(context);
  }

  public VideoPlayerWithAdPlayback(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public VideoPlayerWithAdPlayback(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
  }

  @Override
  protected void onFinishInflate() {
    super.onFinishInflate();
    init();
  }

  private void init() {
    mIsAdDisplayed = false;
    View parent = getRootView();
    mAdUiContainer = parent.findViewById(R.id.adUiContainer);

    initialisePlayer(parent);

    thumbnail = parent.findViewById(R.id.video_thumbnail);
    playButton = findViewById(R.id.playBtn);
    pauseButton = findViewById(R.id.pauseBtn);
    learnMoreView = parent.findViewById(R.id.learn_more);

    immersiveControlsContainer  = parent.findViewById(R.id.immersive_view_control_container);
    nonImmersiveControlsContainer = parent.findViewById(R.id.non_immersive_controls_container);
    companionContainer = parent.findViewById(R.id.companion_ad_container);
    companionContainer.setAutoStart(false);

    immersiveMuteButton = parent.findViewById(R.id.ad_immersive_mute_button);
    timeBar = parent.findViewById(R.id.ad_immersive_video_timebar);
    timeBar.setEnabled(false); //disable scrubbing in timebar
    timeDisplay = parent.findViewById(R.id.ad_immersive_video_time);

    thumbnail.setOnClickListener(this);
    playButton.setOnClickListener(this);
    pauseButton.setOnClickListener(this);
    mMuteButton = parent.findViewById(R.id.muteButton);
    muteButtonClickList.add(mMuteButton);
    muteButtonClickList.add(immersiveMuteButton);
    mMuteButton.setImageResource(isMute ? R.drawable.ic_mute : R.drawable.ic_unmute);
    immersiveMuteButton.setImageResource(isMute ? R.drawable.ic_mute : R.drawable.ic_unmute);

    OnClickListener muteClickListener = view -> {
      toggleMuteState(true);
      mMuteButton.setImageResource(isMute ? R.drawable.ic_mute : R.drawable.ic_unmute);
      immersiveMuteButton.setImageResource(isMute ? R.drawable.ic_mute : R.drawable.ic_unmute);
    };

    for (ImageView button: muteButtonClickList) {
        button.setOnClickListener(muteClickListener);
    }

    // Define VideoAdPlayer connector.
    mVideoAdPlayer = new VideoAdPlayer() {

      @Override
      public int getVolume() {
        return mVideoPlayer.getVolume();
      }

      @Override
      public void playAd() {
        if (mIsAdDisplayed) {
          mVideoPlayer.resume();
        } else {
          mIsAdDisplayed = true;
          mVideoPlayer.play();
        }
      }

      @Override
      public void loadAd(String url) {
        mMuteButton.setVisibility(View.VISIBLE);
        togglePlayPauseState(false);
        mIsAdDisplayed = false;
        mVideoPlayer.setVideoPath(url);
      }

      @Override
      public void stopAd() {
        mVideoPlayer.stopPlayback();
        mMuteButton.setVisibility(View.GONE);
      }

      @Override
      public void pauseAd() {
        mVideoPlayer.pause();
      }

      @Override
      public void resumeAd() {
        playAd();
      }

      @Override
      public void addCallback(VideoAdPlayerCallback videoAdPlayerCallback) {
        mAdCallbacks.add(videoAdPlayerCallback);
      }

      @Override
      public void removeCallback(VideoAdPlayerCallback videoAdPlayerCallback) {
        mAdCallbacks.remove(videoAdPlayerCallback);
      }

      @Override
      public VideoProgressUpdate getAdProgress() {
        if (!mIsAdDisplayed || mVideoPlayer.getDuration() <= 0) {
          return VideoProgressUpdate.VIDEO_TIME_NOT_READY;
        }
        //updating default timebar for immersive mode here
        long timeLeft = mVideoPlayer.getDuration() - mVideoPlayer.getCurrentPosition();
        timeDisplay.setText(ExoUtils.stringForTime(timeLeft));
        timeBar.setDuration(mVideoPlayer.getDuration());
        timeBar.setPosition(mVideoPlayer.getCurrentPosition());

        adControlsListener.onAdProgress(ExoUtils.stringForTime(timeLeft));

        return new VideoProgressUpdate(mVideoPlayer.getCurrentPosition(),
            mVideoPlayer.getDuration());
      }
    };

    mContentProgressProvider = () -> {
      if (mIsAdDisplayed || mVideoPlayer.getDuration() <= 0) {
        return VideoProgressUpdate.VIDEO_TIME_NOT_READY;
      }
      return new VideoProgressUpdate(mVideoPlayer.getCurrentPosition(),
          mVideoPlayer.getDuration());
    };
  }

  public void setDefaultCompanionTransitionTime(int span){
    this.defaultCompanionTransitionTime = span * 1000;
  }

  public void startFlipping() {
    companionContainer.startFlipping();
    companionContainer.setFlipInterval(defaultCompanionTransitionTime);
  }

  public void showImmersiveView(Boolean state) {
    nonImmersiveControlsContainer.setVisibility(state ? GONE: VISIBLE);
    immersiveControlsContainer.setVisibility(state? VISIBLE : GONE);
    setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
  }

  public void initialisePlayer(View parent) {
    isMute = PlayerControlHelper.INSTANCE.isListMuteMode();

    final PlayerView playerView = parent.findViewById(R.id.videoPlayer);
    playerView.setUseController(false);
    playerView.findViewById(R.id.exo_shutter).setBackgroundColor(Color.TRANSPARENT);
    mVideoPlayer = new AdsExoPlayer(playerView);
    mVideoPlayer.setMuteState(isMute);

    // Set player callbacks for delegating major video events.
    playerCallback = new CustomAdsPlayer.PlayerCallback() {
      @Override
      public void onPlay() {
        togglePlayPauseState(true);
        learnMoreView.setVisibility(canShowCustomCTA ? View.VISIBLE : View.GONE);
        if (mIsAdDisplayed) {
          for (VideoAdPlayer.VideoAdPlayerCallback callback : mAdCallbacks) {
            callback.onPlay();
          }
        }
      }

      @Override
      public void onVolumeChanged(int i) {
        if (mIsAdDisplayed) {
          for (VideoAdPlayer.VideoAdPlayerCallback callback : mAdCallbacks) {
            callback.onVolumeChanged(i);
          }
        }
      }

      @Override
      public void onPause() {
        if (mIsAdDisplayed) {
          togglePlayPauseState(false);
          for (VideoAdPlayer.VideoAdPlayerCallback callback : mAdCallbacks) {
            callback.onPause();
          }
        }
      }

      @Override
      public void onResume() {
        togglePlayPauseState(true);
        if (mIsAdDisplayed) {
          for (VideoAdPlayer.VideoAdPlayerCallback callback : mAdCallbacks) {
            callback.onResume();
          }
        }
      }

      @Override
      public void onError() {
        hideControls();
        if (mIsAdDisplayed) {
          for (VideoAdPlayer.VideoAdPlayerCallback callback : mAdCallbacks) {
            callback.onError();
          }
        }
      }

      @Override
      public void triggerImmersive() {
          if(adControlsListener != null) adControlsListener.immersiveADview();
      }

      @Override
      public void onCompleted() {
        TextureView textureView = (TextureView) playerView.getVideoSurfaceView();
        Bitmap bitmap = textureView.getBitmap();
        thumbnail.setImageBitmap(bitmap);
        thumbnail.setVisibility(View.VISIBLE);
        playerView.setVisibility(View.GONE);
        hideControls();

        if (mIsAdDisplayed) {
          for (VideoAdPlayer.VideoAdPlayerCallback callback : mAdCallbacks) {
            callback.onEnded();
          }
        } else {
          // Alert an external listener that our content video is complete.
          if (mOnContentCompleteListener != null) {
            mOnContentCompleteListener.onContentComplete();
          }
        }
      }


    };
    mVideoPlayer.addPlayerCallback(playerCallback);
    playerView.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        if(playerCallback != null) playerCallback.triggerImmersive();
      }
    });
  }

  /**
   * Set a listener to be triggered when the content (non-ad) video completes.
   */
  public void setOnContentCompleteListener(OnContentCompleteListener listener) {
    mOnContentCompleteListener = listener;
  }

  /**
   * Save the playback progress state of the currently playing video. This is called when content
   * is paused to prepare for ad playback or when app is backgrounded.
   */
  public void savePosition() {
    if (mIsAdDisplayed) {
      mSavedAdPosition = mVideoPlayer.getCurrentPosition();
    } else {
      mSavedContentPosition = mVideoPlayer.getCurrentPosition();
    }
  }

  /**
   * Restore the currently loaded video to its previously saved playback progress state. This is
   * called when content is resumed after ad playback or when focus has returned to the app.
   */
  public void restorePosition() {
    if (mIsAdDisplayed) {
      mVideoPlayer.seekTo(mSavedAdPosition);
    } else {
      mVideoPlayer.seekTo(mSavedContentPosition);
    }
  }

  /**
   * Pauses the content video.
   */
  public void pause() {
    mVideoPlayer.pause();
  }

  /**
   * Plays the content video.
   */
  public void play() {
    mVideoPlayer.play();
  }

  /**
   * Returns the UI element for rendering video ad elements.
   */
  public ViewGroup getAdUiContainer() {
    return mAdUiContainer;
  }

  public void addCompanion(ViewGroup companionView, CompanionAdSlot adSlot) {
      companionContainer.addView(companionView);
      companionAdSlots.add(new Pair(adSlot, null));
  }

  //keep pair of companionAdSlot and Listener. So that we need not create again
  public List<Pair<CompanionAdSlot,  CompanionAdSlot.ClickListener>> getCompanionSlots() {
    return companionAdSlots;
  }
  /**
   * Views considered as obstruction by OM sdk.
   *
   * @return
   */
  public List<View> getVideoControlsOverlay() {
    List<View> obstructions = new ArrayList<>();
    obstructions.add(playButton);
    obstructions.add(pauseButton);
    obstructions.add(mMuteButton);
    obstructions.add(learnMoreView);
    obstructions.add(nonImmersiveControlsContainer);
    obstructions.add(immersiveMuteButton);
    return obstructions;
  }

  /**
   * Returns an implementation of the SDK's VideoAdPlayer interface.
   */
  public VideoAdPlayer getVideoAdPlayer() {
    return mVideoAdPlayer;
  }


  /**
   * Release the player as soon as its use is over.
   * May cause an exoplayer internal crash if multiple players are streaming and the device does
   * not support that many parallel mediaCodecs.
   */
  public void releasePlayer() {
    if (mVideoPlayer == null) {
      return;
    }
    mVideoPlayer.removePlayerCallback(playerCallback);
    mVideoPlayer.release();
    adsManager = null;
    mContentProgressProvider = null;
  }

  /**
   * Returns if an ad is displayed.
   */
  public boolean getIsAdDisplayed() {
    return mIsAdDisplayed;
  }

  public ContentProgressProvider getContentProgressProvider() {
    return mContentProgressProvider;
  }

  /**
   * Set initial mute/unmute state for player.
   *
   * @param mute isMute
   */
  public void setStartMuted(boolean mute) {
    if (isMute == mute) {
      return;
    }
    toggleMuteState(false);
  }

  /**
   * Change player mute/unmute state.
   * Do not update if player does not follow global mute switch.
   *
   * @param mute isMute
   */
  public void setMuteState(boolean mute) {
    if (!followGlobalMute || isMute == mute) {
      return;
    }
    toggleMuteState(false);
  }

  public void toggleForImmersiveMode(boolean state) {
    if(mVideoPlayer != null) mVideoPlayer.setMuteState(state);
    isMute = state;
    immersiveMuteButton.setImageResource(state ? R.drawable.ic_mute : R.drawable.ic_unmute);
  }

  private void toggleMuteState(boolean userAction) {
    if (followGlobalMute) {
      isMute = userAction ? PlayerControlHelper.INSTANCE.toggleMute() :
          PlayerControlHelper.isListMuteMode();
    } else {
      isMute = !isMute;
    }

    mMuteButton.setImageResource(isMute ? R.drawable.ic_mute : R.drawable.ic_unmute);

    if (mVideoPlayer != null) {
      mVideoPlayer.setMuteState(isMute);
    }
    if (userAction) {
      BusProvider.getUIBusInstance().post(new PlaySettingsChangedEvent(isMute, Constants.EMPTY_STRING));
    }
  }

  public void setFollowGlobalMute(boolean followGlobalMute) {
    this.followGlobalMute = followGlobalMute;
  }

  public void setAdsManager(AdsManager adsManager) {
    this.adsManager = adsManager;
  }

  public void setAdControlsListener(AdControlsListener adControlsListener) {
    this.adControlsListener = adControlsListener;
  }

  public void setQualifiesImmersive(boolean state) {
    mVideoPlayer.setQualifiedForImmersive(state);
  }

  public void setImmersiveSpan(int span) {
    mVideoPlayer.setImmersiveSpan(span);
  }

  @Override
  public void onClick(View v) {
    if (v.equals(learnMoreView) || v.equals(thumbnail)) {
      if (adControlsListener != null) {
        adControlsListener.onPlayerTapped(true);
      }
    } else if (v.equals(nonImmersiveControlsContainer)) {
      if (adControlsListener != null) {
        adControlsListener.onPlayerTapped(false);
      }
    }
    if (adsManager == null) {
      return;
    }
    if (v.equals(playButton)) {
      if (mIsAdDisplayed) {
        adsManager.resume();
      } else {
        adsManager.start();
      }
      if (adControlsListener != null) {
        adControlsListener.onPlayTapped();
      }
    } else if (v.equals(pauseButton)) {
      adsManager.pause();
      if (adControlsListener != null) {
        adControlsListener.onPauseTapped();
      }
    }
  }

  /**
   * Whether custom CTA view is to be shown.
   * If not, full video will serve as CTA.
   *
   * @param show    value
   * @param ctaText Text for Cta button
   * @param color cta text color
   */
  public void shouldShowCustomCTA(boolean show, String ctaText, String color) {
    canShowCustomCTA = show;
    if (show && !DataUtil.isEmpty(ctaText)) {
      learnMoreView.setText(ctaText);
      learnMoreView.setTextColor(ViewUtils.getColor(color,
          getResources().getColor(R.color.learn_more_ima_text_color, getContext().getTheme())));
      learnMoreView.setOnClickListener(this);
    }
    nonImmersiveControlsContainer.setOnClickListener(this);
  }

  private void togglePlayPauseState(boolean isPlaying) {
    playButton.setVisibility(isPlaying ? View.GONE : View.VISIBLE);
    pauseButton.setVisibility(isPlaying ? View.VISIBLE : View.GONE);
  }

  private void hideControls() {
    nonImmersiveControlsContainer.setVisibility(View.GONE);
    learnMoreView.setVisibility(View.GONE);
    pauseButton.setVisibility(View.GONE);
  }
}
