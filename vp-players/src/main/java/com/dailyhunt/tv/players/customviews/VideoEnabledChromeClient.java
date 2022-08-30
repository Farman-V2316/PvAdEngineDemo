package com.dailyhunt.tv.players.customviews;

import android.app.Activity;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.widget.FrameLayout;
import android.widget.VideoView;

import com.dailyhunt.tv.players.R;


/**
 * This class serves as a WebChromeClient to be set to a WebView, allowing it to play video.
 * Video will play differently depending on target API level (in-line, fullscreen, or both).
 * <p/>
 * It has been tested with the following video classes:
 * - android.widget.VideoView (typically API level <11)
 * - android.webkit.HTML5VideoFullScreen$VideoSurfaceView/VideoTextureView (typically API level 11-18)
 * - com.android.org.chromium.content.browser.ContentVideoView$VideoSurfaceView (typically API level 19+)
 * <p/>
 * Important notes:
 * - For API level 11+, android:hardwareAccelerated="true" must be set in the application manifest.
 * - The invoking activity must call VideoEnabledWebChromeClient's onBackPressed() inside of its own onBackPressed().
 * - Tested in Android API levels 8-19. Only tested on http://m.youtube.com.
 *
 * @author Cristian Perez (http://cpr.name)
 */
public class VideoEnabledChromeClient extends WebChromeClient
    implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener,
    MediaPlayer.OnErrorListener {
  private static Bitmap mDefaultVideoPoster;
  private VideoEnabledWebView webView;
  private boolean isVideoFullscreen;
  private CustomViewCallback videoViewCallback;
  private VideoView mCustomVideoView;
  private FrameLayout mVideoLayout;
  private ViewGroup mRootLayout;
  private ToggledFullscreenCallback toggledFullscreenCallback;
  private int systemUIVisibilityFlag;

  /**
   * Never use this constructor alone.
   * This constructor allows this class to be defined as an inline inner class in which the user can override methods
   */
  @SuppressWarnings("unused")
  public VideoEnabledChromeClient() {
  }

  @SuppressWarnings("unused")
  public VideoEnabledChromeClient(VideoEnabledWebView webView, FrameLayout rootView) {
    this.webView = webView;
    this.mRootLayout = rootView;
    this.isVideoFullscreen = false;
  }

  /**
   * Indicates if the video is being displayed using a custom view (typically full-screen)
   *
   * @return true it the video is being displayed using a custom view (typically full-screen)
   */
  public boolean isVideoFullscreen() {
    return isVideoFullscreen;
  }

  /**
   * Set a callback that will be fired when the video starts or finishes displaying using a custom view (typically full-screen)
   *
   * @param callback A VideoEnabledWebChromeClient.ToggledFullscreenCallback callback
   */
  @SuppressWarnings("unused")
  public void setOnToggledFullscreen(ToggledFullscreenCallback callback) {
    this.toggledFullscreenCallback = callback;
  }

  @Override
  public void onShowCustomView(View view, CustomViewCallback callback) {
    super.onShowCustomView(view, callback);

    if (mRootLayout == null) {
      return;
    }

    isVideoFullscreen = true;
    videoViewCallback = callback;

    if (view instanceof FrameLayout) {
      FrameLayout frame = (FrameLayout) view;
      if (frame.getFocusedChild() instanceof VideoView) {//We are in 2.3
        VideoView video = (VideoView) frame.getFocusedChild();
        frame.removeView(video);

        setupVideoLayout(video);

        mCustomVideoView = video;

      } else {//Handle 4.x

        setupVideoLayout(view);

      }

      // Notify full-screen change
      if (toggledFullscreenCallback != null) {
        toggledFullscreenCallback.toggledFullscreen(true, null);
      }
    }
  }

  @Override
  @SuppressWarnings("deprecation")
  public void onShowCustomView(View view, int requestedOrientation,
                               CustomViewCallback callback) // Available in API level 14+, deprecated in API level 18+
  {
    onShowCustomView(view, callback);
  }

  @Override
  public void onHideCustomView() {
    closeFullscreenView();
  }

  @Override
  public View getVideoLoadingProgressView() // Video will start loading
  {
    FrameLayout frameLayout = new FrameLayout(webView.getContext());
    frameLayout.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
        FrameLayout.LayoutParams.MATCH_PARENT));
    return frameLayout;
  }

  @Override
  public void onPrepared(
      MediaPlayer mp) // Video will start playing, only called in the case of android.widget.VideoView (typically API level <11)
  {
  }

  @Override
  public void onCompletion(
      MediaPlayer mp) // Video finished playing, only called in the case of android.widget.VideoView (typically API level <11)
  {
    //Leave video in fullscreen mode
  }

  @Override
  public boolean onError(MediaPlayer mp, int what,
                         int extra) // Error while playing video, only called in the case of android.widget.VideoView (typically API level <11)
  {
    return false; // By returning false, onCompletion() will be called
  }

  /**
   * Notifies the class that the back key has been pressed by the user.
   * This must be called from the Activity's onBackPressed(), and if it returns false, the activity itself should handle it. Otherwise don't do anything.
   *
   * @return Returns true if the event was handled, and false if was not (video view is not visible)
   */
  @SuppressWarnings("unused")
  public boolean onBackPressed() {
    if (isVideoFullscreen) {
      closeFullscreenView();
      return true;
    } else {
      return false;
    }
  }

  public void resetPlayer() {
    closeFullscreenView();
  }

  @Override
  public Bitmap getDefaultVideoPoster() {
    if (mDefaultVideoPoster == null) {
      mDefaultVideoPoster = Bitmap.createBitmap(10, 10, Bitmap.Config.RGB_565);
    }
    return mDefaultVideoPoster;
  }

  private void closeFullscreenView() {
    if (isVideoFullscreen) {
      // Notify full-screen change
      if (toggledFullscreenCallback != null) {
        toggledFullscreenCallback.toggledFullscreen(false, null);
      }

      if (mCustomVideoView != null) {
        mCustomVideoView.stopPlayback();
      }
      mRootLayout.removeView(mVideoLayout);
      videoViewCallback.onCustomViewHidden();
      isVideoFullscreen = false;
      webView.requestFocus();
    }
  }

  private void setupVideoLayout(View video) {

    /**
     * As we don't want the touch events to be processed by the underlying WebView, we do not set the WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE flag
     * But then we have to handle directly back press in our View to exit fullscreen.
     * Otherwise the back button will be handled by the topmost Window, id-est the player controller
     */
    mVideoLayout = new FrameLayout(webView.getContext());

    mVideoLayout.setBackgroundResource(R.color.black_color);
    mVideoLayout.addView(video);
    ViewGroup.LayoutParams lp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
        FrameLayout.LayoutParams.MATCH_PARENT);
    mRootLayout.addView(mVideoLayout, lp);
    mRootLayout.requestFocus();
    isVideoFullscreen = true;
  }

  // This snippet hides the system bars.
  private void hideSystemUI() {
    if (mRootLayout == null) {
      return;
    }
    systemUIVisibilityFlag = mRootLayout.getSystemUiVisibility();
    mRootLayout.setSystemUiVisibility(
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
    View decorView = ((Activity) webView.getContext()).getWindow().getDecorView();
    decorView.setSystemUiVisibility(systemUIVisibilityFlag);

  }

  public interface ToggledFullscreenCallback {
    void toggledFullscreen(boolean fullscreen, View videoView);
  }
}