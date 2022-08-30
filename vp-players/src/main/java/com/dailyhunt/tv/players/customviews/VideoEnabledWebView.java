package com.dailyhunt.tv.players.customviews;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import com.dailyhunt.tv.players.utils.PlayerUtils;
import com.newshunt.common.view.view.DetachableWebView;

import java.util.Map;

/**
 * This class serves as a WebView to be used in conjunction with a VideoEnabledWebChromeClient.
 * It makes possible:
 * - To detect the HTML5 video ended event so that the VideoEnabledWebChromeClient can exit full-screen.
 * <p/>
 * Important notes:
 * - Javascript is enabled by default and must not be disabled with getSettings().setJavaScriptEnabled(false).
 * - setWebChromeClient() must be called before any loadData(), loadDataWithBaseURL() or loadUrl() method.
 *
 * @author Cristian Perez (http://cpr.name)
 */
public class VideoEnabledWebView extends WebView implements DetachableWebView {
  private VideoEnabledChromeClient videoEnabledWebChromeClient;
  private boolean addedJavascriptInterface;
  private boolean isAutoplayVideo;

  @SuppressWarnings("unused")
  public VideoEnabledWebView(Context context) {
    super(context);
    addedJavascriptInterface = false;
  }

  @SuppressWarnings("unused")
  public VideoEnabledWebView(Context context, AttributeSet attrs) {
    super(context, attrs);
    addedJavascriptInterface = false;
  }

  @SuppressWarnings("unused")
  public VideoEnabledWebView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    addedJavascriptInterface = false;
  }

  /**
   * Indicates if the video is being displayed using a custom view (typically full-screen)
   *
   * @return true it the video is being displayed using a custom view (typically full-screen)
   */
  @SuppressWarnings("unused")
  public boolean isVideoFullscreen() {
    return videoEnabledWebChromeClient != null && videoEnabledWebChromeClient.isVideoFullscreen();
  }

  /**
   * Pass only a VideoEnabledWebChromeClient instance.
   */
  @Override
  @SuppressLint("SetJavaScriptEnabled")
  public void setWebChromeClient(WebChromeClient client) {
    getSettings().setJavaScriptEnabled(true);

    if (client instanceof VideoEnabledChromeClient) {
      this.videoEnabledWebChromeClient = (VideoEnabledChromeClient) client;
    }

    super.setWebChromeClient(client);
  }

  @Override
  public void loadData(String data, String mimeType, String encoding) {
    addJavascriptInterface();
    super.loadData(data, mimeType, encoding);
  }

  @Override
  public void loadDataWithBaseURL(String baseUrl, String data, String mimeType, String encoding,
                                  String historyUrl) {
    addJavascriptInterface();
    super.loadDataWithBaseURL(baseUrl, data, mimeType, encoding, historyUrl);
  }

  @Override
  public void loadUrl(String url) {
    try {
      addJavascriptInterface();
      super.loadUrl(url);
    } catch (Exception e) {

    }

  }

  @Override
  public void loadUrl(String url, Map<String, String> additionalHttpHeaders) {
    addJavascriptInterface();
    super.loadUrl(url, additionalHttpHeaders);
  }

  private void addJavascriptInterface() {
    if (!addedJavascriptInterface) {
      // Add javascript interface to be called when the video ends (must be done before page load)
      //noinspection all
      addJavascriptInterface(new JavascriptInterface(),
          "_VideoEnabledWebView"); // Must match Javascript interface name of VideoEnabledWebChromeClient

      addedJavascriptInterface = true;
    }
  }

  public void onBackPressed() {
    if (null != videoEnabledWebChromeClient) {
      videoEnabledWebChromeClient.onBackPressed();
    }
  }

  public void resetPlayer() {
    if (null != videoEnabledWebChromeClient) {
      videoEnabledWebChromeClient.resetPlayer();
    }
  }

  @Override
  protected void onAttachedToWindow() {
    super.onAttachedToWindow();
  }

  @Override
  public void onDetachedFromWindow() {
    if (isAutoplayVideo) {
      return;
    }
    try {
      this.setVisibility(View.GONE);
      PlayerUtils.resetWebViewState(this);
      super.onDetachedFromWindow();
    } catch (IllegalArgumentException e) {
      // // e.printStackTrace();
    }
  }

  public class JavascriptInterface {
    @android.webkit.JavascriptInterface
    @SuppressWarnings("unused")
    public void notifyVideoEnd() // Must match Javascript interface method of VideoEnabledWebChromeClient
    {
      //Do Nothing on VideoEnd
    }
  }

  public void setAutoplayVideo(boolean autoplayVideo) {
    isAutoplayVideo = autoplayVideo;
  }

}