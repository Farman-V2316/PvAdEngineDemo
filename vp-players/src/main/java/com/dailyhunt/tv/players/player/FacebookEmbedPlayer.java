package com.dailyhunt.tv.players.player;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.dailyhunt.tv.players.listeners.PlayerFacebookPlayerListener;
import com.dailyhunt.tv.players.managers.PlayerScriptManager;
import com.dailyhunt.tv.players.utils.PlayerUtils;
import com.newshunt.common.helper.common.Logger;
import com.newshunt.dataentity.news.model.entity.server.asset.PlayerAsset;

/**
 * Created by santoshkulkarni on 03/09/16.
 */
public class FacebookEmbedPlayer {

  private final String DEFAULT_DOMAIN = "https://www.facebook.com";
  public String iFrameHtml;
  private WebView tvVideoWebView;
  private Context context;
  private PlayerFacebookPlayerListener facebookPlayerListener;
  private String videoUrl;
  private Handler handler;
  private PlayerAsset item;
  private boolean isVideoStarted;
  private boolean isPaused;
  private boolean videoPausedBeforeInitialize;
  private boolean videoComplete;
  private boolean nonAutoPlayClicked = false;

  public FacebookEmbedPlayer(Context context, PlayerAsset playerAsset, WebView tvVideoWebView,
                             PlayerFacebookPlayerListener facebookPlayerListener,
                             boolean nonAutoPlayClicked) {
    this.tvVideoWebView = tvVideoWebView;
    this.context = context;
    this.facebookPlayerListener = facebookPlayerListener;
    this.nonAutoPlayClicked = nonAutoPlayClicked;
    this.handler = new Handler(Looper.getMainLooper());
    this.videoUrl = playerAsset.getVideoUrl();
    this.item = playerAsset;

    Logger.d("FBVIDEO", "URL :: " + videoUrl);
    buildHtml();
    init();
  }

  private void buildHtml() {
//    TVContentScale scale = null;
//    if (null != item.getImageUrl() && item.getImageUrl().getWidth() >= 0) {
//      scale = TVImageUtil.getScale(CommonUtils.getApplication(),
//          item.getImageUrl().getWidth(), item.getImageUrl().getHeight());
//    } else {
//      scale = new TVContentScale();
//      scale.setWidth(TVUtils.getScreenWidth());
//    }

    String BEGIN = "<html>\n" +
        "<head>\n" +
        "  <title>Your Website Title</title>\n" +
        "</head>\n" +
        "<body>\n" +
        "<div id=\"fb-root\"></div>";

    String JS = PlayerScriptManager.getInstance().getFBJavaScriptStr();

    String END = "  <!-- Your embedded video player code -->\n" +
        "  <div  \n" +
        "    class=\"fb-video\" \n" +
        "    data-href=" + "\"" + videoUrl + "\"\n" +
        "    data-allowfullscreen=\"true\"></div>\n" +
        "    style=\"border:none;overflow:hidden\"\n" +
        "    scrolling=\"no\" frameborder=\"0\" allowTransparency=\"true\"" +
        "\n" +
        "</body>\n" +
        "</html>";
    iFrameHtml = BEGIN + JS + END;

  }


  private void init() {
    if (context == null) {
      return;
    }

    tvVideoWebView.setBackgroundColor(Color.BLACK);
  }


  public void setUpWebView() {
    try {
      tvVideoWebView.getSettings().setPluginState(WebSettings.PluginState.ON);
      tvVideoWebView.getSettings().setJavaScriptEnabled(true);
      tvVideoWebView.setHorizontalScrollBarEnabled(false);
      tvVideoWebView.getSettings().setDisplayZoomControls(false);
      tvVideoWebView.setVerticalScrollBarEnabled(false);
      tvVideoWebView.getSettings().setMediaPlaybackRequiresUserGesture(false);
      tvVideoWebView.addJavascriptInterface(new FBWebAppInterface(), "FBWebAppInterface");
      tvVideoWebView.loadDataWithBaseURL(DEFAULT_DOMAIN, iFrameHtml, "text/html", null, null);
      tvVideoWebView.setOnTouchListener(new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
          return (event.getAction() == MotionEvent.ACTION_MOVE);
        }
      });
      Logger.d("FBVIDEO", "HTML :: loadDataWithBaseURL");
    } catch (Exception e) {
      e.printStackTrace();
      Logger.d("FBVIDEO", "HTML ::EXCEPTION  " + e.getMessage());
    }

  }

  private void startPlay() {
    handler.post(new Runnable() {
      @Override
      public void run() {
        try {

          String startPlay = "startPlay();";
          tvVideoWebView.evaluateJavascript(startPlay, null);
        } catch (Exception e) {

        }

      }
    });
  }


  public void closeFullscreen() {
    handler.post(new Runnable() {
      @Override
      public void run() {
        try {
          String fullscreen = "closeFullScreen();";
          tvVideoWebView.evaluateJavascript(fullscreen, null);
        } catch (Exception e) {

        }

      }
    });


  }

  public void getCurrentPosition() {
    //Posting through handler not needed here
    if (tvVideoWebView == null) {
      return;
    }
    try {
      String getCurrentPosition = "getCurrentPosition();";
      tvVideoWebView.evaluateJavascript(getCurrentPosition, null);
    } catch (Exception e) {
    }
  }


  public void pausePlay() {
    videoPausedBeforeInitialize = true;
    handler.post(new Runnable() {
      @Override
      public void run() {
        try {
          String pausePlay = "pausePlay();";
          tvVideoWebView.evaluateJavascript(pausePlay, null);
        } catch (Exception e) {

        }

      }
    });

  }

  public boolean isVideoPlayStarted() {
    return isVideoStarted;
  }

  public boolean isPaused() {
    return isPaused;
  }

  public boolean isVideoComplete() {
    return videoComplete;
  }


  public class FBWebAppInterface {

    @JavascriptInterface
    public void onReady(String log) {
      Logger.d("FBVIDEO", "onReady :: " + log);
      handler.post(new Runnable() {
        @Override
        public void run() {
          if (!facebookPlayerListener.isFragmentAdded()) {
            return;
          }
          facebookPlayerListener.onPlayerReady();
          tvVideoWebView.requestFocus();
          if (!videoPausedBeforeInitialize || nonAutoPlayClicked) {
            startPlay();
          }
        }
      });
    }

    @JavascriptInterface
    public void onPlayStart(final long currentDuration) {
      Logger.d("FBVIDEO", "onPlayStart :: currentDuration :: " + currentDuration);
      videoComplete = false;
      isVideoStarted = true;
      isPaused = false;
      handler.post(new Runnable() {
        @Override
        public void run() {
          facebookPlayerListener.onPlayStart(currentDuration);
        }
      });
    }

    @JavascriptInterface
    public void onPause(final long currentDuration) {
      Logger.d("FBVIDEO", "onPause :: currentDuration :: " + currentDuration);
      isPaused = true;
      handler.post(new Runnable() {
        @Override
        public void run() {
          facebookPlayerListener.onPlayerPause(currentDuration);
        }
      });
    }

    @JavascriptInterface
    public void onStartBuffering(final long currentDuration) {
      Logger.d("FBVIDEO", "onStartBuffering :: currentDuration :: " + currentDuration);
      videoComplete = false;
      isPaused = false;
      handler.post(new Runnable() {
        @Override
        public void run() {
          facebookPlayerListener.onStartBuffering(currentDuration);
        }
      });
    }

    @JavascriptInterface
    public void onFinishBuffering(final long currentDuration) {
      Logger.d("FBVIDEO", "onFinishBuffering :: currentDuration :: " + currentDuration);
      isPaused = false;
      handler.post(new Runnable() {
        @Override
        public void run() {
          facebookPlayerListener.onFinishBuffering(currentDuration);
        }
      });
    }

    @JavascriptInterface
    public void onError(final long currentDuration) {
      Logger.d("FBVIDEO", "onError :: currentDuration :: " + currentDuration);
      isPaused = false;
      handler.post(new Runnable() {
        @Override
        public void run() {
          facebookPlayerListener.onPlayerError(currentDuration);
        }
      });
    }

    @JavascriptInterface
    public void onFinishPlaying(final long currentDuration) {
      Logger.d("FBVIDEO", "onFinishPlaying :: currentDuration :: " + currentDuration);
      videoComplete = true;
      isPaused = false;
      handler.post(new Runnable() {
        @Override
        public void run() {
          closeFullscreen();
          facebookPlayerListener.onFinishPlaying(currentDuration);
        }
      });
    }

    @JavascriptInterface
    public void log(String log) {
      Logger.d("FBVIDEO", "log :: " + log);
    }


    @JavascriptInterface
    public void getPlayerCurrPosition(final long currentDuration) {
      Logger.d("FBVIDEO", "getPlayerCurrPosition :: currentDuration :: " + currentDuration);
      handler.post(new Runnable() {
        @Override
        public void run() {
          facebookPlayerListener.getCurrentpositon(currentDuration);
        }
      });
    }
  }

}
