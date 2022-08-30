package com.newshunt.common.view.customview;

import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import androidx.core.view.ViewCompat;
import android.util.AttributeSet;
import android.webkit.CookieManager;
import android.webkit.WebView;

import com.newshunt.common.helper.appconfig.AppConfig;
import com.newshunt.common.helper.common.BaseErrorBuilder;
import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.common.helper.common.Constants;
import com.newshunt.common.helper.common.Logger;
import com.newshunt.common.helper.common.NHWebViewUtils;
import com.newshunt.common.helper.common.NhWebViewClient;
import com.newshunt.common.helper.listener.WebViewErrorCallback;
import com.newshunt.dataentity.common.model.entity.BaseError;
import com.newshunt.dhutil.R;
import com.newshunt.dhutil.helper.browser.NhWebChromeClient;

/**
 * Parent class for all webviews in app to
 * enable JavaScript and Dom storage. This also sets API cookies to webview calls.
 *
 * @author: bedprakash on 12/1/17.
 */

public class NhWebView extends WebView {

  private WebViewErrorCallback webViewErrorCallback;

  public NhWebView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init();
  }

  public NhWebView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public NhWebView(Context context) {
    super(context);
    init();
  }

  public WebViewErrorCallback getWebViewErrorCallback() {
    return webViewErrorCallback;
  }

  public void setWebViewErrorCallback(
      WebViewErrorCallback webViewErrorCallback) {
    this.webViewErrorCallback = webViewErrorCallback;
  }

  @Override
  protected void onDetachedFromWindow() {
    try {
      super.onDetachedFromWindow();
    } catch (Exception ex) {
      Logger.caughtException(ex);
    }
    webViewErrorCallback = null;
  }

  private void init() {
    // Keep text zoom at normal level i.e. 100 to avoid using device font zoom.
    getSettings().setTextZoom(100);

    // Default Settings for all
    getSettings().setJavaScriptEnabled(true);
    getSettings().setDomStorageEnabled(true);
    getSettings().setDatabaseEnabled(true);
    getSettings().setSupportMultipleWindows(true);

    setWebViewClient(new NhWebViewClient());
    setWebChromeClient(new NhWebChromeClient());

    // below lollipop - 3rd party cookies are enabled by default
    CookieManager.getInstance().setAcceptThirdPartyCookies(this, true);
  }

  @Override
  public void loadUrl(String url) {
    if (!CommonUtils.isNetworkAvailable(CommonUtils.getApplication()) && webViewErrorCallback != null) {
      BaseError baseError = BaseErrorBuilder.getBaseError(CommonUtils.getApplication().getString(com.newshunt.common.util.R.string
          .error_no_connection), Constants.ERROR_NO_INTERNET);
      webViewErrorCallback.onErrorReceived(baseError);
      return;
    }
    super.loadUrl(url);
  }

  @Override
  public void loadDataWithBaseURL(String baseUrl, String data, String mimeType, String encoding,
                                  String historyUrl) {
    if (CommonUtils.isEmpty(baseUrl)) {
      baseUrl = AppConfig.getInstance().getNewsAPIEndPoint();
    }
    super.loadDataWithBaseURL(baseUrl, data, mimeType, encoding, historyUrl);
  }

  public void webviewResume() {
    String script = NHWebViewUtils.formatScript("resume");
    NHWebViewUtils.callJavaScriptFunction(this, script);
  }

  public void webviewPaused() {
    String script = NHWebViewUtils.formatScript("pause");
    NHWebViewUtils.callJavaScriptFunction(this, script);
  }

  public void updateAudioCommentaryState(String json) {
    String script = NHWebViewUtils.formatScript("updateAudioCommentaryState", json);
    NHWebViewUtils.callJavaScriptFunction(this, script);
  }

  public void onScrollEvent(int scrollY) {
    String script = NHWebViewUtils.formatScript("onScroll", 0, scrollY);
    NHWebViewUtils.callJavaScriptFunctionWithReturnValue(this, script);
  }

  public void onScrollEnd() {
    NHWebViewUtils.callJavaScriptFunctionWithReturnValue(this, "onScrollEnd()");
  }

  public void jsRefresh() {
    NHWebViewUtils.callJavaScriptFunctionWithReturnValue(this, "reloadTriggerFromApp()");
  }

  public void updateWebviewOnScroll() {
    /*Fix for fabric crash - https://www.fabric.io/verse-innovation-pvt-ltd--bangalore/android/apps/com.eterno/issues/585182050aeb16625bfd584a
      http://stackoverflow.com/questions/12927617/null-pointer-exception-on-doing-webview-loadurl

      If the webview is destroyed() and then we try loading webview.loadUrl(), it will result in
      above crash. So webview is destroyed onDetachedFromWindow, so returning if view is not
      attached to window..
     */
    if (!ViewCompat.isAttachedToWindow(this)) {
      return;
    }
    Rect scrollBounds = new Rect();
    if (!this.getLocalVisibleRect(scrollBounds)) {
      return;
    }

    String topPosition = String.valueOf(scrollBounds.top);
    String bottomPosition = String.valueOf(scrollBounds.bottom);
    String totalVisibleWebViewHeight = String.valueOf(scrollBounds.height());
    String script = NHWebViewUtils.formatScript("onScroll", topPosition, bottomPosition,
        totalVisibleWebViewHeight);
    NHWebViewUtils.callJavaScriptFunctionWithReturnValue(this, script);
  }

}