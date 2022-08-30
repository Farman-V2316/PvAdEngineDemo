package com.dailyhunt.tv.players.player;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;

import com.dailyhunt.tv.players.analytics.enums.PlayerVideoEndAction;
import com.dailyhunt.tv.players.analytics.enums.PlayerVideoStartAction;
import com.dailyhunt.tv.players.customviews.VideoEnabledWebView;
import com.dailyhunt.tv.players.interfaces.PlayerCallbacks;
import com.dailyhunt.tv.players.listeners.PlayerYoutubeIframeListener;
import com.dailyhunt.tv.players.managers.PlayerScriptManager;
import com.newshunt.analytics.helper.ReferrerProvider;
import com.newshunt.common.helper.common.Logger;
import com.newshunt.common.helper.common.ViewUtils;
import com.newshunt.dataentity.analytics.referrer.PageReferrer;
import com.newshunt.dataentity.analytics.section.NhAnalyticsEventSection;
import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.dataentity.news.model.entity.server.asset.PlayerAsset;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Jayanth on 09/05/18.
 */
public class YoutubeIframePlayer {
  private final VideoEnabledWebView tvVideoWebView;
  private final Context context;
  private String iFrameHtml;
  private final String DEFAULT_DOMAIN = "https://www.youtube.com";
  private final PlayerYoutubeIframeListener youtubeIframeListener;
  private boolean videoPaused;

  private enum IFrameStates {PLAYING, PAUSED, BUFFERING, ENDED, UNSTARTED}

  private IFrameStates currFrameState;
  private final long initialStartTime;
  private final Handler handler;
  private final PlayerAsset item;
  private static final String LOG_TAG = "YoutubeIframePlayer";

  public YoutubeIframePlayer(Context context, PlayerAsset item, VideoEnabledWebView tvVideoWebView,
                             PlayerYoutubeIframeListener youtubeIframeListener,
                             ReferrerProvider referrerProvider, PageReferrer referrer,
                             PlayerCallbacks playerCallbacks,
                             NhAnalyticsEventSection eventSection) {
    this.tvVideoWebView = tvVideoWebView;
    this.context = context;
    this.item = item;
    this.youtubeIframeListener = youtubeIframeListener;
    handler = new Handler(Looper.getMainLooper());
    init(item);

    initialStartTime = System.currentTimeMillis();

  }

  private void init(PlayerAsset item) {
    if (context == null) {
      return;
    }

    int width;
    int height;
    String yUrl = extractYouTubeVideoId(item.getVideoUrl());
    if (CommonUtils.isEmpty(yUrl)) {
      yUrl = item.getSourceVideoId();
    }
    width = CommonUtils.getDeviceScreenWidth();
    height = (CommonUtils.getDeviceScreenWidth() * 9) / 16;
    width = CommonUtils.getDpFromPixels(width, context);
    height = CommonUtils.getDpFromPixels(height, context);
    iFrameHtml = "<html><iframe id=\"dh-tv-iframe\" width=\"" + width + "\" " +
        "height=\"" + height + "\" src=\"https://www.youtube.com/embed/" + yUrl
        + "?autoplay=1&enablejsapi=1&fs=1\" " +
        "frameborder=\"0\"        style=\"margin:0;padding:0;\" allowfullscreen></iframe></html>";

    tvVideoWebView.setBackgroundColor(Color.BLACK);
  }

  private static String extractYouTubeVideoId(String videoUrl) {
    String videoId = null;
    Pattern pattern = Pattern.compile(
        ".*(?:youtu.be\\/|v\\/|u\\/\\w\\/|embed\\/|watch\\?v=)([^#\\&\\?]*).*");
    Matcher matcher = pattern.matcher(videoUrl);
    if (matcher.matches()) {
      videoId = matcher.group(1);
    }
    return CommonUtils.isEmpty(videoId) ? "#" : videoId;
  }

  public void setUpWebView() {
    String jsAsset = null;
    try {
      jsAsset = PlayerScriptManager.getInstance().getYTJavaScriptStr();
      iFrameHtml = iFrameHtml.replace("<html>", "<html>" + jsAsset);
      tvVideoWebView.getSettings().setPluginState(WebSettings.PluginState.ON);
      tvVideoWebView.getSettings().setJavaScriptEnabled(true);
      tvVideoWebView.setHorizontalScrollBarEnabled(false);
      tvVideoWebView.setVerticalScrollBarEnabled(false);
      tvVideoWebView.getSettings().setDisplayZoomControls(false);
      tvVideoWebView.getSettings().setMediaPlaybackRequiresUserGesture(false);
      tvVideoWebView.addJavascriptInterface(new YTWebAppInterface(), "YTWebAppInterface");
      tvVideoWebView.getSettings().setMediaPlaybackRequiresUserGesture(false);
      tvVideoWebView.setOnTouchListener(new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
          return event.getAction() == MotionEvent.ACTION_MOVE;
        }
      });
      tvVideoWebView.loadDataWithBaseURL(DEFAULT_DOMAIN, iFrameHtml, "text/html", null, null);

    } catch (Exception e) {
      e.printStackTrace();
    }

  }


  public boolean isPaused() {
    return currFrameState == IFrameStates.PAUSED;
  }

  public boolean isVideoCompplete() {
    return currFrameState == IFrameStates.ENDED;
  }

  public void startPlay() {
    Logger.d("YTVIDEO", "startPlay");
    handler.post(new Runnable() {
      @Override
      public void run() {
        try {
          if (null == tvVideoWebView) {
            return;
          }
          String startPlay = "startPlay();";
          tvVideoWebView.evaluateJavascript(startPlay, null);
        } catch (Exception e) {

        }

      }
    });

  }

  public void pausePlay() {
    Logger.d("YTVIDEO", "pausePlay");
    videoPaused = true;
    handler.post(new Runnable() {
      @Override
      public void run() {
        try {
          if (null == tvVideoWebView) {
            return;
          }
          String pausePlay = "pausePlay();";
          tvVideoWebView.evaluateJavascript(pausePlay, null);
        } catch (Exception e) {

        }

      }
    });

  }

  public void setEndAction(PlayerVideoEndAction endAction) {

  }

  public void setStartAction(PlayerVideoStartAction startAction) {

  }

  // Call backs
  public class YTWebAppInterface {
    @JavascriptInterface
    public void onPlayerReady() {
      Logger.d("YTVIDEO", "onYIFramePlayerReady");
      handler.post(new Runnable() {
        @Override
        public void run() {
          if (!youtubeIframeListener.isFragmentAdded()) {
            return;
          }
          tvVideoWebView.requestFocus();
          if (!videoPaused) {
            startPlay();
          }
          if (youtubeIframeListener != null) {
            youtubeIframeListener.onYIFramePlayerReady();
          }
        }
      });
    }

    @JavascriptInterface
    public void onPlayerError() {
      Logger.d("YTVIDEO", "onYIFramePlayerError");
      handler.post(new Runnable() {
        @Override
        public void run() {
          if (youtubeIframeListener != null) {
            youtubeIframeListener.onYIFramePlayerError();
          }
        }
      });
    }

    @JavascriptInterface
    public void log(String log) {
      Logger.d("YTVIDEO", "log :: " + log);
    }

    @JavascriptInterface
    public void onPlayerStateChange(String event) {
      try {
        JSONObject jsonObject = new JSONObject(event);
        String state = jsonObject.getString("state");
        int time = jsonObject.getInt("currentTime");
        switch (state) {
          case "PLAYING":
            ViewUtils.setScreenAwakeLock(true, context, LOG_TAG);
            Logger.d("YTVIDEO", "PLAYING");
            currFrameState = IFrameStates.PLAYING;
            break;
          case "ENDED":
            Logger.d("YTVIDEO", "ENDED");
            ViewUtils.setScreenAwakeLock(false, context, LOG_TAG);
            currFrameState = IFrameStates.ENDED;
            handler.postDelayed(new Runnable() {
              @Override
              public void run() {
                if (youtubeIframeListener != null) {
                  youtubeIframeListener.onYIFramePlayerComplete();
                }
              }
            }, 200);
            break;
          case "PAUSED":
            ViewUtils.setScreenAwakeLock(false, context, LOG_TAG);
            Logger.d("YTVIDEO", "PAUSED");
            currFrameState = IFrameStates.PAUSED;
            break;
          case "BUFFERING":
            Logger.d("YTVIDEO", "BUFFERING");
            ViewUtils.setScreenAwakeLock(true, context, LOG_TAG);
            currFrameState = IFrameStates.BUFFERING;
            break;
          case "UNSTARTED":
            Logger.d("YTVIDEO", "UNSTARTED");
            currFrameState = IFrameStates.UNSTARTED;
            break;
        }
      } catch (JSONException e) {
        e.printStackTrace();
      }
    }
  }

}
