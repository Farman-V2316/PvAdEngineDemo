/*
* Copyright (c) 2017 Newshunt. All rights reserved.
*/
package com.dailyhunt.tv.players.customviews;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.VideoView;

import com.dailyhunt.tv.players.interfaces.PlayerDailyMotionView;
import com.dailyhunt.tv.players.listeners.PlayerDailyMotionPlayerListener;
import com.google.gson.Gson;
import com.newshunt.dataentity.common.helper.common.CommonUtils;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by Jayanth on 09/05/18.
 */
public class DMWebVideoView extends WebView {

  public static final String EVENT_APIREADY = "apiready";
  public static final String EVENT_TIMEUPDATE = "timeupdate";
  public static final String EVENT_DURATION_CHANGE = "durationchange";
  public static final String EVENT_PROGRESS = "progress";
  public static final String EVENT_WAITING = "waiting";
  public static final String EVENT_SEEKED = "seeked";
  public static final String EVENT_SEEKING = "seeking";
  public static final String EVENT_GESTURE_START = "gesture_start";
  public static final String EVENT_GESTURE_END = "gesture_end";
  public static final String EVENT_MENU_DID_SHOW = "menu_did_show";
  public static final String EVENT_MENU_DID_HIDE = "menu_did_hide";
  public static final String EVENT_VIDEO_START = "video_start";
  public static final String EVENT_VIDEO_END = "video_end";
  public static final String EVENT_AD_START = "ad_start";
  public static final String EVENT_AD_PAUSE = "ad_pause";
  public static final String EVENT_AD_END = "ad_end";
  public static final String EVENT_FULLSCREEN_TOGGLE_REQUESTED = "fullscreen_toggle_requested";
  public static final String EVENT_PLAY = "play";
  public static final String EVENT_PAUSE = "pause";
  public static final String EVENT_LOADEDMETADATA = "loadedmetadata";
  public static final String EVENT_PLAYING = "playing";
  public static final String EVENT_START = "start";
  public static final String EVENT_BUFFERING = "buffering";
  public static final String EVENT_END = "end";
  public static final String EVENT_CONTROLSCHANGE = "controlschange";
  public static final String EVENT_VOLUMECHANGE = "volumechange";
  public static final String EVENT_QUALITY = "qualitychange";

  public static final String COMMAND_NOTIFY_LIKECHANGED = "notifyLikeChanged";
  public static final String COMMAND_NOTIFY_WATCHLATERCHANGED = "notifyWatchLaterChanged";
  public static final String COMMAND_LOAD = "load";
  public static final String COMMAND_MUTE = "mute";
  public static final String COMMAND_CONTROLS = "controls";
  public static final String COMMAND_QUALITY = "quality";
  public static final String COMMAND_SUBTITLE = "subtitle";
  public static final String COMMAND_TOGGLE_CONTROLS = "toggle-controls";
  public static final String COMMAND_TOGGLE_PLAY = "toggle-play";
  private boolean hasMetadata;
  private Runnable controlsCommandRunnable;
  private Runnable muteCommandRunnable;
  private Runnable loadCommandRunnable;

  private boolean videoPaused = false;
  private boolean apiReady;
  private long controlsLastTime;
  private long muteLastTime;
  private long loadLastTime;
  private ArrayList<DMWebVideoView.Command> commandList = new ArrayList<>();
  private int systemUIVisibilityFlag;

  static class Command {
    public String methodName;
    public Object[] params;
  }

  public class Error {

    public String code;
    public String title;
    public String message;

    public Error(String c, String t, String m) {
      code = c;
      title = t;
      message = m;
    }
  }

  private WebSettings webSettings;
  private WebChromeClient chromeClient;
  private VideoView customVideoView;
  private WebChromeClient.CustomViewCallback viewCallback;
  public static String DEFAULT_PLAYER_URL = "http://www.dailymotion.com/embed/video/";
  private String baseUrl = DEFAULT_PLAYER_URL;
  private final String extraUA = "; DailymotionEmbedSDK 1.0";
  private FrameLayout videoLayout;
  private boolean isFullscreen = false;
  private ViewGroup rootLayout;
  private boolean autoPlay = true;
  private String extraParameters;
  private String videoId;
  private Gson gson;
  public boolean autoplay = false;
  public double currentTime = 0;
  public double duration = 0;
  public Object error = null;
  public boolean ended = false;
  public boolean fullscreen = false;
  public String quality = "";
  public String subtitle = "";

  private static final String DM_AUTH_KEY = "&syndication=273733";

  private PlayerDailyMotionPlayerListener playerListener;

  public DMWebVideoView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    init();
  }

  public DMWebVideoView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public DMWebVideoView(Context context) {
    super(context);
    init();
  }

  public void setExtraParameters(String extraParameters) {
    this.extraParameters = extraParameters;
  }

  private void init() {
    Object mJavascriptBridge = new JavascriptBridge();
    addJavascriptInterface(mJavascriptBridge, "dmpNativeBridge");
    setExtraParameters(DM_AUTH_KEY);
    setBackgroundColor(0xff000000);
    mHandler = new Handler();
    gson = new Gson();
    webSettings = getSettings();
    webSettings.setJavaScriptEnabled(true);
    webSettings.setPluginState(WebSettings.PluginState.ON);
    webSettings.setUserAgentString(webSettings.getUserAgentString() + extraUA);
    setWebChromeClient(chromeClient);
    webSettings.setMediaPlaybackRequiresUserGesture(false);

    chromeClient = new WebChromeClient() {

      /**
       * The view to be displayed while the fullscreen VideoView is buffering
       *
       * @return the progress view
       */
      @Override
      public View getVideoLoadingProgressView() {
        ProgressBar pb = new ProgressBar(getContext());
        pb.setIndeterminate(true);
        return pb;
      }

      @Override
      public void onShowCustomView(View view, CustomViewCallback callback) {
        super.onShowCustomView(view, callback);
        hideSystemUI();
        if (playerListener != null && playerListener instanceof PlayerDailyMotionView) {
          ((PlayerDailyMotionView) playerListener).setFullScreenMode(true);
        }
        //  ((Activity) getContext()).setVolumeControlStream(AudioManager.STREAM_MUSIC);
        isFullscreen = true;
        viewCallback = callback;
        if (view instanceof FrameLayout) {
          FrameLayout frame = (FrameLayout) view;
          if (frame.getFocusedChild() instanceof VideoView) {//We are in 2.3
            VideoView video = (VideoView) frame.getFocusedChild();
            frame.removeView(video);

            setupVideoLayout(video);

            customVideoView = video;
            customVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

              @Override
              public void onCompletion(MediaPlayer mediaPlayer) {
                //hideVideoView();
              }
            });


          } else {//Handle 4.x
            setupVideoLayout(view);
          }
        }
      }

      @Override
      public Bitmap getDefaultVideoPoster() {
        int colors[] = new int[1];
        colors[0] = Color.TRANSPARENT;
        Bitmap bm = Bitmap.createBitmap(colors, 0, 1, 1, 1, Bitmap.Config.ARGB_8888);
        return bm;
      }

      @Override
      public void onShowCustomView(View view, int requestedOrientation,
                                   CustomViewCallback callback) {
        // Only available in API level 14+
        onShowCustomView(view, callback);
      }

      @Override
      public void onHideCustomView() {
        hideVideoView();
      }

    };
    setWebChromeClient(chromeClient);
  }

  private void callPlayerMethod(String method) {
    loadUrl("javascript:player.api(\"" + method + "\")");
  }

  private void callPlayerMethod(String method, String param) {
    loadUrl("javascript:player.api(\"" + method + "\", \"" + param + "\")");
  }

  public void setVideoId(String videoId) {
    this.videoId = videoId;
  }

  public void setPlayerListener(PlayerDailyMotionPlayerListener playerListener) {
    this.playerListener = playerListener;
  }

  public void load(String playerUrl) {
    if (rootLayout == null) {
      //The topmost layout of the window where the actual VideoView will be added to
      rootLayout = (FrameLayout) ((Activity) getContext()).getWindow().getDecorView();
    }

    String finalUrl = null;
    if (!CommonUtils.isEmpty(playerUrl) && playerUrl.contains(baseUrl)) {
      playerUrl = playerUrl.replace("api=location","api=nativeBridge");
      finalUrl = playerUrl;
    } else {
      //DataUrl is null From server, Constructing the "FinalUrl" at client
      finalUrl =
          baseUrl + videoId + "?app=" + getContext().getPackageName() + "&api=nativeBridge";
      if (extraParameters != null && !extraParameters.equals("")) {
        finalUrl += "&" + extraParameters;
      }
    }

    loadUrl(finalUrl, new HashMap<String, String>());
  }

  public void hideVideoView() {
    if (isFullscreen()) {
      if (playerListener != null && playerListener instanceof PlayerDailyMotionView) {
        ((PlayerDailyMotionView) playerListener).setFullScreenMode(false);
      }
      showSystemUI();
      if (customVideoView != null) {
        customVideoView.stopPlayback();
      }
      rootLayout.removeView(videoLayout);
      viewCallback.onCustomViewHidden();
      //((Activity) getContext()).setVolumeControlStream(AudioManager.USE_DEFAULT_STREAM_TYPE);
      isFullscreen = false;
      requestFocus();
    }
  }

  private void setupVideoLayout(View video) {
    /**
     * As we don't want the touch events to be processed by the underlying WebView, we do not set the WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE flag
     * But then we have to handle directly back press in our View to exit fullscreen.
     * Otherwise the back button will be handled by the topmost Window, id-est the player controller
     */
    videoLayout = new FrameLayout(getContext()) {
    };

    videoLayout.setBackgroundResource(android.R.color.black);
    videoLayout.addView(video);
    ViewGroup.LayoutParams lp =
        new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    rootLayout.addView(videoLayout, lp);
    rootLayout.requestFocus();
    isFullscreen = true;
  }

  public boolean isFullscreen() {
    return isFullscreen;
  }

  public void handleBackPress() {
    if (isFullscreen()) {
      hideVideoView();
    } else {
      loadUrl("");//Hack to stop video
    }
  }

  public void setAutoPlay(boolean autoPlay) {
    this.autoPlay = autoPlay;
  }

  public void pauseDM() {
    videoPaused = true;
    if (apiReady) {
      callPlayerMethod("pause");
    }
  }

  private Handler mHandler;

  private class JavascriptBridge {
    @JavascriptInterface
    public void triggerEvent(final String e) {
      mHandler.post(new Runnable() {
        @Override
        public void run() {
          handleEvent(e);
        }
      });
    }
  }

  private void handleEvent(String e) {
    e = URLDecoder.decode(e);
    String p[] = e.split("&");
    HashMap<String, String> map = new HashMap<>();

    for (String s : p) {
      String s2[] = s.split("=");
      if (s2.length == 1) {
        map.put(s2[0], null);
      } else if (s2.length == 2) {
        map.put(s2[0], s2[1]);
      } else {
      }
    }

    String event = map.get("event");
    if (event == null) {
      return;
    }

    if (!event.equals("timeupdate")) {
    }

    switch (event) {
      case EVENT_APIREADY: {
        if (autoPlay && !videoPaused) {
          callPlayerMethod("play");
        } else if (playerListener != null) {
          callPlayerMethod("pause");
          playerListener.hideLoader();
        }
        apiReady = true;
        break;
      }
      case EVENT_START: {
        videoPaused = false;
        ended = false;
        if (null != playerListener) {
          playerListener.hideLoader();
        }
        mHandler.removeCallbacks(loadCommandRunnable);
        loadCommandRunnable = null;
        break;
      }
      case EVENT_VIDEO_START:
        playerListener.onVideoStarted();
        break;
      case EVENT_END: {
        ended = true;
        playerListener.playNextVideo();
        break;
      }
      case EVENT_WAITING:
        playerListener.onStartBuffering();
        break;
      case EVENT_PROGRESS: {
        currentTime = Float.parseFloat(map.get("time"));
        break;
      }
      case EVENT_TIMEUPDATE: {
        currentTime = Float.parseFloat(map.get("time"));
        break;
      }
      case EVENT_DURATION_CHANGE: {
        duration = Float.parseFloat(map.get("duration"));
        break;
      }
      case EVENT_GESTURE_START:
      case EVENT_MENU_DID_SHOW: {
        break;
      }
      case EVENT_GESTURE_END:
      case EVENT_MENU_DID_HIDE: {
        break;
      }
      case EVENT_PLAYING:
        videoPaused = false;
        playerListener.onFinishBuffering();
        break;
      case EVENT_VIDEO_END: {
        videoPaused = true;
        playerListener.onPlayComplete();
        break;
      }
      case EVENT_PLAY: {
        videoPaused = false;
        playerListener.onVideoResume();
        break;
      }
      case EVENT_PAUSE: {
        playerListener.onPlayerPaused();
        videoPaused = true;
        break;
      }
      case EVENT_CONTROLSCHANGE: {
        mHandler.removeCallbacks(controlsCommandRunnable);
        controlsCommandRunnable = null;
        break;
      }
      case EVENT_VOLUMECHANGE: {
        mHandler.removeCallbacks(muteCommandRunnable);
        muteCommandRunnable = null;
        break;

      }
      case EVENT_LOADEDMETADATA: {
        hasMetadata = true;
        break;
      }
      case EVENT_QUALITY: {
        quality = map.get("quality");
        break;
      }
      case EVENT_SEEKED: {
        playerListener.onStartBuffering();
        currentTime = Float.parseFloat(map.get("time"));
        break;
      }
      case EVENT_SEEKING: {
        break;
      }
      case EVENT_AD_END:
        videoPaused = true;
        playerListener.onAdEnd();
        break;
      case EVENT_AD_START:
        videoPaused = false;
        playerListener.onAdStart();
        break;
      case EVENT_AD_PAUSE:
        videoPaused = true;
        playerListener.onAdPaused();
        break;
      case EVENT_BUFFERING:
        playerListener.onStartBuffering();
        break;
      case EVENT_FULLSCREEN_TOGGLE_REQUESTED: {
        break;
      }
    }
    tick();
  }

  private void sendCommand(DMWebVideoView.Command command) {
    switch (command.methodName) {
      case COMMAND_MUTE:
        callPlayerMethod((Boolean) command.params[0] ? "mute" : "unmute");
        break;
      case COMMAND_CONTROLS:
        callPlayerMethod("api", "controls", (Boolean) command.params[0] ? "true" : "false");
        break;
      case COMMAND_QUALITY:
        callPlayerMethod("api", "quality", command.params[0]);
        break;
      case COMMAND_SUBTITLE:
        callPlayerMethod("api", "subtitle", command.params[0]);
        break;
      case COMMAND_TOGGLE_CONTROLS:
        callPlayerMethod("api", "toggle-controls", command.params);
        break;
      case COMMAND_TOGGLE_PLAY:
        callPlayerMethod("api", "toggle-play", command.params);
        break;
      default:
        callPlayerMethod(command.methodName, command.params);
        break;
    }
  }

  private void tick() {

    if (!apiReady) {
      return;
    }

    Iterator<DMWebVideoView.Command> iterator = commandList.iterator();
    while (iterator.hasNext()) {
      final DMWebVideoView.Command command = iterator.next();
      switch (command.methodName) {
        case COMMAND_NOTIFY_LIKECHANGED:
          if (!hasMetadata) {
            continue;
          }
          break;
        case COMMAND_NOTIFY_WATCHLATERCHANGED:
          if (!hasMetadata) {
            continue;
          }
          break;
        case COMMAND_MUTE:
          if (System.currentTimeMillis() - muteLastTime < 1000) {
            continue;
          }
          muteLastTime = System.currentTimeMillis();
          break;
        case COMMAND_LOAD:
          if (System.currentTimeMillis() - loadLastTime < 1000) {
            continue;
          }
          loadLastTime = System.currentTimeMillis();
          break;
        case COMMAND_CONTROLS:
          if (System.currentTimeMillis() - controlsLastTime < 1000) {
            continue;
          }
          controlsLastTime = System.currentTimeMillis();
          break;
      }

      iterator.remove();
      sendCommand(command);
    }
  }

  public void callPlayerMethod(String method, Object... params) {
    StringBuilder builder = new StringBuilder();
    builder.append("javascript:player.");
    builder.append(method);
    builder.append('(');
    int count = 0;
    for (Object o : params) {
      count++;
      if (o instanceof String) {
        builder.append("'" + o + "'");
      } else if (o instanceof Number) {
        builder.append(o.toString());
      } else if (o instanceof Boolean) {
        builder.append(o.toString());
      } else {
        builder.append("JSON.parse('" + gson.toJson(o) + "')");
      }
      if (count < params.length) {
        builder.append(",");
      }
    }
    builder.append(')');
    String js = builder.toString();

    loadUrl(js);
  }

  public void setSubtitle(String language_code) {
    callPlayerMethod("subtitle", language_code);
  }

  // This snippet hides the system bars.
  private void hideSystemUI() {
    View decorView = ((Activity) getContext()).getWindow().getDecorView();
    systemUIVisibilityFlag = decorView.getSystemUiVisibility();
    decorView.setSystemUiVisibility(
        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
            | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
            | View.SYSTEM_UI_FLAG_IMMERSIVE);
  }

  // This snippet shows the system bars. It does this by removing all the flags
// except for the ones that make the content appear under the system bars.
  private void showSystemUI() {
    View decorView = ((Activity) getContext()).getWindow().getDecorView();
    decorView.setSystemUiVisibility(systemUIVisibilityFlag);
  }

  @SuppressWarnings("unused")
  public boolean onBackPressed() {
    if (isFullscreen()) {
      hideVideoView();
      return true;
    } else {
      return false;
    }
  }

  public boolean isVideoComplete() {
    return ended;
  }

}