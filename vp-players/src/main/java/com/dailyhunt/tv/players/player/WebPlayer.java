package com.dailyhunt.tv.players.player;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.dailyhunt.tv.exolibrary.listeners.VideoTimeListener;
import com.dailyhunt.tv.players.constants.PlayerContants;
import com.dailyhunt.tv.players.interfaces.WebPlayerView;
import com.dailyhunt.tv.players.listeners.PlayerWebPlayerListener;
import com.dailyhunt.tv.players.managers.PlayerScriptManager;
import com.dailyhunt.tv.players.presenters.WebPlayerScriptPresenter;
import com.dailyhunt.tv.players.utils.PlayerUtils;
import com.google.android.exoplayer2.util.Util;
import com.newshunt.common.helper.common.BusProvider;
import com.newshunt.common.helper.common.Constants;
import com.newshunt.common.helper.common.Logger;
import com.newshunt.common.helper.common.ViewUtils;
import com.newshunt.common.helper.info.ClientInfoHelper;
import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.dataentity.dhutil.model.entity.players.PlayerUnifiedWebPlayer;
import com.newshunt.dataentity.news.model.entity.server.asset.PlayerAsset;
import com.newshunt.dataentity.news.model.entity.server.asset.PlayerType;
import com.newshunt.dhutil.helper.PlayerDataProvider;
import com.newshunt.helper.player.PlayerControlHelper;

import java.util.Formatter;
import java.util.Locale;

/**
 * Created by santoshkulkarni on 15/05/17.
 */

public class WebPlayer implements WebPlayerView {

  private final String TAG = WebPlayer.class.getSimpleName();
  private final WebView tvVideoWebView;
  private final Context context;
  private final Handler handler;
  private final PlayerAsset playerAsset;
  private final String DEFAULT_DOMAIN = "http://google.com";
  private WebPlayerScriptPresenter presenter;
  private PlayerUnifiedWebPlayer player = new PlayerUnifiedWebPlayer();
  private boolean isPlayerDataExist = false;
  private boolean isFullScreenCallback = false;
  private boolean videoPausedBeforeInitialize;
  private boolean isVideoReady;
  private boolean isVideoComplete;
  private boolean isAdStarted;
  private boolean isVideoStarted;
  private boolean isVideoError;
  private final TVWebPlayerInterface playerInterface;
  private boolean muteMode;
  private boolean nonAutoPlayClicked = false;
  private boolean isAutoplayVideo = false;
  //Used in case of NexG - as NexG Player does handle tap to resume on error
  private boolean showThumbnailOnError;
  private final boolean hasCompleteURL;
  private VideoTimeListener videoTimeListener;
  private final StringBuilder formatBuilder;
  private final Formatter formatter;
  private boolean showRemainingTime;

  private PlayerWebPlayerListener playerListener;

  public WebPlayer(Context context, PlayerAsset playerAsset, WebView tvVideoWebView,
                   PlayerWebPlayerListener playerListener, boolean hasCompleteURL, boolean
                       muteMode, boolean nonAutoPlayClicked) {
    this(context, playerAsset, tvVideoWebView, playerListener, hasCompleteURL,
        muteMode, nonAutoPlayClicked, false);
  }

  public WebPlayer(Context context, PlayerAsset playerAsset, WebView tvVideoWebView,
                   PlayerWebPlayerListener playerListener, boolean hasCompleteURL,
                   boolean muteMode, boolean nonAutoPlayClicked, boolean isAutoplayVideo) {
    this.tvVideoWebView = tvVideoWebView;
    this.context = context;
    handler = new Handler(Looper.getMainLooper());
    this.playerAsset = playerAsset;
    this.playerListener = playerListener;
    this.muteMode = muteMode;
    this.nonAutoPlayClicked = nonAutoPlayClicked;
    this.hasCompleteURL = hasCompleteURL;
    init();
    playerInterface = new TVWebPlayerInterface();
    player.setPlayerKey(playerAsset.getSourceInfo().getPlayerKey());
    presenter = new WebPlayerScriptPresenter(this, BusProvider.getUIBusInstance(), player);
    this.isAutoplayVideo = isAutoplayVideo;
    formatBuilder = new StringBuilder();
    formatter = new Formatter(formatBuilder, Locale.ENGLISH);
  }

  public void setPlayerListener(PlayerWebPlayerListener playerListener) {
    this.playerListener = playerListener;
  }

  public void setVideoTimeListener(VideoTimeListener videoTimeListener) {
    this.videoTimeListener = videoTimeListener;
  }

  private String buildHtml(String playerHtml) {
    if (CommonUtils.isEmpty(playerHtml)) {
      return playerHtml;
    }

    if (playerHtml.contains(PlayerContants.DH_WEB_PLAYER_FIND_FULLSCREEN_FUNC)) {
      isFullScreenCallback = true;
    }

    return getQualifiedData(playerHtml);
  }

  private String getQualifiedData(String playerData) {
    int width = CommonUtils.getDpFromPixels(CommonUtils.getDeviceScreenWidth(), context);
    int height = CommonUtils.getDpFromPixels((CommonUtils.getDeviceScreenWidth() * 9) / 16, context);
    if (!CommonUtils.isEmpty(playerAsset.getSourceVideoId())) {
      playerData = playerData.replace("DH_PLAYER_VIDEO_ID", playerAsset.getSourceVideoId());
    }

    Logger.d("VideoUrl", "" + playerAsset.getSourceVideoId());
    playerData = playerData.replace("DH_PLAYER_WIDTH", "" + width);
    playerData = playerData.replace("DH_PLAYER_HEIGHT", "" + height);
    playerData = playerData.replace("DH_CLIENT_ID", ClientInfoHelper.getClientId());
    playerData = playerData.replace("DH_APP_NAME", PlayerUtils.getAppName());
    playerData = playerData.replace("DH_APP_VERSION", ClientInfoHelper.getAppVersion());

    if (!CommonUtils.isEmpty(playerAsset.getReplaceableParams())) {
      for (String key : playerAsset.getReplaceableParams().keySet()) {
        String value = playerAsset.getReplaceableParams().get(key);
        if (!CommonUtils.isEmpty(value)) {
          playerData = playerData.replace(key, value);
        }
      }
    }

    return playerData;
  }

  private void init() {
    if (context == null) {
      return;
    }
    tvVideoWebView.setBackgroundColor(Color.BLACK);
    CookieManager.getInstance().setAcceptCookie(true);
    CookieManager.getInstance().setAcceptThirdPartyCookies(tvVideoWebView, true);
  }

  private void fetchPlayerDetail() {
    PlayerUnifiedWebPlayer tmpPlayer =
        PlayerScriptManager.getInstance().getPlayerData(playerAsset.getSourceInfo().getPlayerKey());

    if (null != tmpPlayer && !CommonUtils.isEmpty(tmpPlayer.getData())) {
      Logger.d("DH_WEB_PLAYER", "getPlayerKey = " +
          playerAsset.getSourceInfo().getPlayerKey() + "::PlayerDataExist");
      player = tmpPlayer;
      isPlayerDataExist = true;
    } else {
      Logger.d("DH_WEB_PLAYER", "getPlayerKey = " +
          playerAsset.getSourceInfo().getSourceName() + "::PlayerDataNull");
      if (null == presenter) {
        player.setPlayerKey(playerAsset.getSourceInfo().getPlayerKey());
        presenter = new WebPlayerScriptPresenter(this, BusProvider.getUIBusInstance(), player);
      }
      presenter.start();
    }
  }


  public void setUpWebView() {
    try {
      if (!hasCompleteURL) {
        fetchPlayerDetail();
        if (!isPlayerDataExist) {
          return;
        }
      }

      tvVideoWebView.getSettings().setPluginState(WebSettings.PluginState.ON);
      tvVideoWebView.getSettings().setJavaScriptEnabled(true);
      tvVideoWebView.setHorizontalScrollBarEnabled(false);
      tvVideoWebView.getSettings().setDomStorageEnabled(true);
      tvVideoWebView.getSettings().setDisplayZoomControls(false);
      tvVideoWebView.setVerticalScrollBarEnabled(false);
      if(playerAsset.getSourceInfo() != null) {
        String userAgentString = PlayerDataProvider.getInstance().getUserAgentString(
                playerAsset.getSourceInfo().getPlayerKey());
        if(!CommonUtils.isEmpty(userAgentString)) {
          tvVideoWebView.getSettings().setUserAgentString(userAgentString);
          Logger.d(TAG, "Set User agent string : " + userAgentString);
        } else if(userAgentString == null && PlayerType.YOUTUBE.getName().equalsIgnoreCase(
                playerAsset.getSourceInfo().getPlayerKey())) {
          tvVideoWebView.getSettings().setUserAgentString(Constants.USER_AGENT_STRING);
          Logger.d(TAG, "Set default user agent string");
        } else {
          Logger.d(TAG, "Dont Set User agent string ");
        }
      }
      tvVideoWebView.getSettings().setMediaPlaybackRequiresUserGesture(false);
      tvVideoWebView.addJavascriptInterface(playerInterface, "WebPlayerInterface");

      if (hasCompleteURL) {
        String playerUrl = getQualifiedData(playerAsset.getVideoUrl());
        tvVideoWebView.loadUrl(playerUrl);
      } else {
        String embedHtml = buildHtml(player.getData());
        if (!CommonUtils.isEmpty(player.getSourceBaseUrl())) {
          tvVideoWebView.loadDataWithBaseURL(player.getSourceBaseUrl(), embedHtml, "text/html",
              null, null);
        } else {
          tvVideoWebView.loadDataWithBaseURL(DEFAULT_DOMAIN, embedHtml, "text/html", null, null);
        }
      }

      tvVideoWebView.setOnTouchListener(new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
          return event.getAction() == MotionEvent.ACTION_MOVE;
        }
      });
    } catch (Exception e) {
      Logger.caughtException(e);
    }

  }

  public void closeFullscreen() {
    handler.post(new Runnable() {
      @Override
      public void run() {
        try {
          String fullscreen = "m_fullScreen(false);";
          tvVideoWebView.evaluateJavascript(fullscreen, null);
        } catch (Exception e) {
          Logger.caughtException(e);
        }
      }
    });
  }

  private void getPlayerMuteState() {
    Logger.d(TAG, "getPlayerMuteState");
    if (!isAdStarted && !isVideoStarted) {
      videoPausedBeforeInitialize = true;
    }
    handler.post(new Runnable() {
      @Override
      public void run() {
        try {
          String pausePlay = "m_playerMuteState();";
          tvVideoWebView.evaluateJavascript(pausePlay, null);
        } catch (Exception e) {
          Logger.caughtException(e);
        }
      }
    });
  }

  public void pausePlay() {
    Logger.d(TAG, "pausePlay");
    getPlayerMuteState();
    if (!isAdStarted && !isVideoStarted) {
      videoPausedBeforeInitialize = true;
    }
    handler.postDelayed(new Runnable() {
      @Override
      public void run() {
        try {
          String pausePlay = "m_pauseVideo();";
          tvVideoWebView.evaluateJavascript(pausePlay, null);
        } catch (Exception e) {
          Logger.caughtException(e);
        }
      }
    }, 200);
  }

  public void startPlay() {
    Logger.d(TAG, "startPlay");
    if(playerListener != null && !playerListener.isViewInForeground()) {
      Logger.d(TAG, "isViewInForeground is false");
      return;
    }
    //reset the variable as video play was called
    videoPausedBeforeInitialize = false;
    handler.post(new Runnable() {
      @Override
      public void run() {
        try {
          String mute = "m_setMuteMode(" + muteMode + ");";
          tvVideoWebView.evaluateJavascript(mute, null);
        } catch (Exception e) {
          Logger.caughtException(e);
        }
        try {
          String play = "m_playVideo();";
          tvVideoWebView.evaluateJavascript(play, null);
        } catch (Exception e) {
          Logger.caughtException(e);
        }
      }
    });
  }

  public void setMuteMode(boolean muteMode) {
    this.muteMode = muteMode;
    handler.post(new Runnable() {
      @Override
      public void run() {
        try {
          String mute = "m_setMuteMode(" + muteMode + ");";
          tvVideoWebView.evaluateJavascript(mute, null);
        } catch (Exception e) {
          Logger.caughtException(e);
        }
      }
    });

  }

  public boolean getMuteMode() {
    return muteMode;
  }

  public void setControlState(boolean showControls) {
    handler.post(new Runnable() {
      @Override
      public void run() {
        try {
          String mute = "m_setControlState(" + showControls + ");";
          tvVideoWebView.evaluateJavascript(mute, null);
        } catch (Exception e) {
          Logger.caughtException(e);
        }
      }
    });
  }

  public void getShowRemainingTime() {
    handler.post(new Runnable() {
      @Override
      public void run() {
        try {
          String getShowRemainingTime = "m_getShowRemainingTime();";
          tvVideoWebView.evaluateJavascript(getShowRemainingTime, null);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    });
  }

  public boolean isVideoReady() {
    return isVideoReady;
  }

  public boolean isVideoError() {
    return isVideoError;
  }
  public boolean isVideoComplete() {
    return isVideoComplete;
  }

  @Override
  public void hideProgress() {
    if (null != presenter) {
      presenter.stop();
    }
  }

  @Override
  public void showError(String message) {
    if (null != presenter) {
      presenter.stop();
    }

  }

  @Override
  public void onPlayerScriptfetchSuccess() {
    if (null != presenter) {
      presenter.stop();
    }
    isPlayerDataExist = true;
    setUpWebView();
  }

  @Override
  public Context getViewContext() {
    return context;
  }

  public boolean isFullScreenCallback() {
    return isFullScreenCallback;
  }

  public boolean isShowThumbnailOnError() {
    return showThumbnailOnError;
  }

  public class TVWebPlayerInterface {

//State for Ad
    //adPlay, adTime, adPause, adPlay, adTime, adComplete, adSkipped, remove, fullscreen

    //States for Video
    //play, firstFrame, time, pause, beforePlay, play, providerFirstFrame, video_playing, seek,
    //seeked, providerFirstFrame, video_playing, complete, beforeComplete, playlistComplete, remove
    //bufferChange, video_playing
    private final String BUFFERING = "buffering";
    private final String FINISH_BUFFERING = "finish_buffering";
    private final String AD_STARTED = "ad_started";
    private final String AD_PLAYING = "ad_playing";
    private final String AD_PAUSED = "ad_paused";
    private final String AD_ENDED = "ad_ended";
    private final String AD_SKIPPED = "ad_skipped";
    private final String VIDEO_STARTED = "video_started";
    private final String VIDEO_PLAYING = "video_playing";
    private final String VIDEO_ENDED = "video_ended";
    private final String VIDEO_PAUSED = "video_paused";
    private final String SEEK = "seek";
    private final String SEEKED = "seeked";
    private final String ON_FULL_SCREEN_CLICK = "on_full_screen_click";
    private final String DISPLAY_CLICK = "displayClick";
    private final String TOUCH_START = "touchstart";
    private final String ERROR = "error";
    private final String REMOVED = "removed";
    private final String FIRST_QUARTILE = "first_quartile";
    private final String MID_QUARTILE = "mid_quartile";
    private final String THIRD_QUARTILE = "third_quartile";
    private final String VIDEO_TIMEUPDATE = "video_timeupdate";
    private final String MUTE = "mute";
    private final String Mute = "Mute";
    private final String UNMUTE = "unmute";
    private final String Unmute = "Unmute";
    private boolean showRemainingTime = false;

    @JavascriptInterface
    public void onReady() {
      Logger.d(TAG, "onReady");
      handler.post(new Runnable() {
        @Override
        public void run() {
          if (!playerListener.isViewInForeground()) {
            pausePlay();
            return;
          }
          playerListener.onPlayerReady();
          tvVideoWebView.requestFocus();
          if (!videoPausedBeforeInitialize || nonAutoPlayClicked) {
            startPlay();
          } else {
            pausePlay();
            videoPausedBeforeInitialize = false;
          }
        }
      });
      isVideoReady = true;
      getShowRemainingTime();
    }

    @JavascriptInterface
    public void showRemainingTime() {
      showRemainingTime = true;
      if (videoTimeListener != null) {
        videoTimeListener.showTimeLeft(showRemainingTime);
        videoTimeListener.onTimeUpdate(
            Util.getStringForTime(formatBuilder, formatter, playerAsset.getDurationLong()), 0L);
      }
    }

    @JavascriptInterface
    public void shouldShowThumbnailOnError() {
      showThumbnailOnError = true;
    }

    @JavascriptInterface
    public void setFullScreenFlag() {
      Logger.d(TAG, "setFullScreenFlag");
      isFullScreenCallback = true;
    }

    @JavascriptInterface
    public void onError(String errorMessage) {
      Logger.d(TAG, "onError :: errorMessage");
      handler.post(new Runnable() {
        @Override
        public void run() {
          playerListener.onPlayerError(0);
        }
      });
    }

    @JavascriptInterface
    public void getMuteMode(boolean muteMode) {
      Logger.d(TAG, "getMuteMode :: " + muteMode);
      if(playerListener != null && !playerListener.isVideoInNewsList()) {
        PlayerControlHelper.setDetailMuteMode(muteMode);
      }
    }

    @JavascriptInterface
    public void onMuteStateChanged(boolean isMuted) {
      muteMode = isMuted;
      Logger.d(TAG, "onMuteStateChanged :: " + muteMode);
      if(playerListener != null && !playerListener.isVideoInNewsList()) {
        PlayerControlHelper.setDetailMuteMode(muteMode);
      }
    }

    @JavascriptInterface
    public boolean isAutoplayVideo() {
      return isAutoplayVideo;
    }

    @JavascriptInterface
    public void log(String log) {
      Logger.d("DH_WEB_PLAYER", log);
    }

    @JavascriptInterface
    public void onPlayerStateChangeWithPlayerCurTime(String state, String time) {
      onPlayerStateChange(state, time);
    }

    @JavascriptInterface
    public void onPlayerStateChange(String state, String time) {
      try {
        float mTime = 0l;
        try {
          if (time != null && !CommonUtils.equals(time,"-1")) {
            mTime = Float.parseFloat(time);
          }
        } catch (Exception e) {
          Logger.caughtException(e);
        }
        final long playerCurTime = (long) mTime;
        Logger.d(TAG, "onPlayerStateChange :: " + state);
        switch (state) {
          case VIDEO_TIMEUPDATE:
            if (videoTimeListener != null) {
              videoTimeListener.onTimeUpdate(Util.getStringForTime(
                  formatBuilder, formatter, playerAsset.getDurationLong() - (playerCurTime * 1000)), playerCurTime*1000);
            }
            break;
          case BUFFERING:
            ViewUtils.setScreenAwakeLock(true, context, TAG);
            playerListener.onStartBuffering(playerCurTime);
            break;
          case FINISH_BUFFERING:
            playerListener.onFinishBuffering(playerCurTime);
            break;
          case AD_STARTED:
            ViewUtils.setScreenAwakeLock(true, context, TAG);
            if (!isAdStarted) {
              playerListener.onAdStarted(playerCurTime);
            } else {
              playerListener.resetEventTimer(playerCurTime);
            }
            isAdStarted = true;
            if (videoPausedBeforeInitialize || !playerListener.isViewInForeground()) {
              videoPausedBeforeInitialize = false;
              pausePlay();
            }
            break;
          case AD_PLAYING:
            ViewUtils.setScreenAwakeLock(true, context, TAG);
            break;
          case AD_PAUSED:
            ViewUtils.setScreenAwakeLock(false, context, TAG);
            playerListener.onAdPaused(playerCurTime);
            break;
          case AD_ENDED:
            ViewUtils.setScreenAwakeLock(false, context, TAG);
            playerListener.onAdEnded(playerCurTime);
            isAdStarted = false;
            break;
          case AD_SKIPPED:
            isAdStarted = false;
            playerListener.onAdSkipped(playerCurTime);
            break;
          case REMOVED:
            if (isAdStarted) {
              playerListener.onAdEnded(playerCurTime);
            } else {
              playerListener.resetEventTimer(playerCurTime);
            }
            break;
          case VIDEO_STARTED:
            ViewUtils.setScreenAwakeLock(true, context, TAG);
            isVideoStarted = true;
            isVideoComplete = false;
            playerListener.onPlayStart(playerCurTime);
            if (videoPausedBeforeInitialize || !playerListener.isViewInForeground()) {
              pausePlay();
              videoPausedBeforeInitialize = false;
            }
            break;
          case VIDEO_PLAYING:
            ViewUtils.setScreenAwakeLock(true, context, TAG);
            isVideoComplete = false;
            break;
          case VIDEO_ENDED:
            ViewUtils.setScreenAwakeLock(false, context, TAG);
            isVideoComplete = true;
            handler.post(new Runnable() {
              @Override
              public void run() {
                playerListener.onFinishPlaying(playerCurTime);
              }
            });
            break;
          case VIDEO_PAUSED:
            ViewUtils.setScreenAwakeLock(false, context, TAG);
            playerListener.onPlayerPause(playerCurTime);
            break;
          case SEEK:
            break;
          case SEEKED:
            break;
          case ON_FULL_SCREEN_CLICK:
            if (playerListener != null) {
              playerListener.handleFullScreen();
              if (isVideoComplete) {
                // ON_FULL_SCREEN_CLICK will be triggered immediately after Video_Complete
                playerListener.onFinishPlaying(playerCurTime);
              }
            }
            break;
          case TOUCH_START:
          case DISPLAY_CLICK: {
            if (playerListener != null) {
              playerListener.onDisplayClick();
            }
            break;
          }
          case ERROR:
            ViewUtils.setScreenAwakeLock(false, context, TAG);
            isVideoError = true;
            handler.post(new Runnable() {
              @Override
              public void run() {
                playerListener.onPlayerError(playerCurTime);
              }
            });
            break;
          case FIRST_QUARTILE:
            if (playerListener != null) {
              playerListener.onFirstQuartile();
            }
            break;
          case MID_QUARTILE:
            if (playerListener != null) {
              playerListener.onMidQuartile();
            }
            break;
          case THIRD_QUARTILE:
            if (playerListener != null) {
              playerListener.onThirdQuartile();
            }
            break;
          case Mute:
            muteMode = true;
            if(playerListener != null && !playerListener.isVideoInNewsList()) {
              PlayerControlHelper.setDetailMuteMode(muteMode);
            }
            break;
          case Unmute:
            muteMode = false;
            if(playerListener != null && !playerListener.isVideoInNewsList()) {
              PlayerControlHelper.setDetailMuteMode(muteMode);
            }
            break;
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
}
