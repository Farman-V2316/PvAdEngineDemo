package com.newshunt.common.helper.common;

import android.annotation.TargetApi;
import android.os.Build;
import androidx.annotation.RequiresApi;
import okhttp3.HttpUrl;

import android.os.Bundle;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.google.gson.reflect.TypeToken;
import com.newshunt.dataentity.model.entity.ActionableNotiPayload;
import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.common.helper.font.FontType;
import com.newshunt.dhutil.R;
import com.newshunt.dhutil.helper.NotificationActionExecutionServiceImpl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;

/**
 * This implementation of {@link WebViewClient} saves the cookies after page gets loaded to persist
 * accross app launches.
 *
 * @author: bedprakash on 30/12/16.
 */

public class NhWebViewClient extends WebViewClient {

  @Override
  public final void onPageFinished(WebView view, String url) {
    onPageLoaded(view, url);
  }

  public void onPageLoaded(WebView view, String url) {
  }

  @SuppressWarnings("deprecation")
  @Override
  public boolean shouldOverrideUrlLoading(WebView view, String url) {

    if (url.startsWith(("http://" + CommonUtils.getString(R.string.host_url_dailyhunt) + "/logs/"))) {
      String actionPayload = HttpUrl.parse(url).queryParameter("actionParam");
      String actionJson = URLDecoder.decode(actionPayload);
      ActionableNotiPayload payload = JsonUtils.fromJson(actionJson,
          new TypeToken<ActionableNotiPayload>() {}.getType());
      NotificationActionExecutionServiceImpl.INSTANCE.getInstance().handleActionableNotification(Bundle.EMPTY, payload);
      return true;
    }

    return NHWebViewUtils.shouldOverrideUrlLoading(view, url, false,null);
  }

  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
    if (request == null || request.getUrl() == null) {
      return super.shouldOverrideUrlLoading(view, request);
    }
    String url = request.getUrl().toString();
    return shouldOverrideUrlLoading(view, url);
  }

  @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
  @Override
  public WebResourceResponse shouldInterceptRequest(final WebView view,
                                                    WebResourceRequest webResourceRequest) {
    if (webResourceRequest == null || webResourceRequest.getUrl() == null) {
      return super.shouldInterceptRequest(view, webResourceRequest);
    }
    return shouldInterceptRequest(view, webResourceRequest.getUrl().toString());
  }

  @SuppressWarnings("deprecation")
  @Override
  public WebResourceResponse shouldInterceptRequest(final WebView view, String url) {
    if (url == null) {
      return super.shouldInterceptRequest(view, url);
    }
    String fileName = null;
    if (url.contains(FontType.NEWSHUNT_REGULAR.getFilename())) {
      fileName = FontType.NEWSHUNT_REGULAR.getFilename();
    } else if (url.contains(FontType.NEWSHUNT_BOLD.getFilename())) {
      fileName = FontType.NEWSHUNT_BOLD.getFilename();
    } else {
      return super.shouldInterceptRequest(view, url);
    }

    InputStream data;
    try {
      data = CommonUtils.getApplication().getAssets().open(fileName);
    } catch (IOException e) {
      Logger.caughtException(e);
      return super.shouldInterceptRequest(view, url);
    }

    if (data == null) {
      return super.shouldInterceptRequest(view, url);
    }
    return new WebResourceResponse(Constants.HTML_MIME_TYPE, Constants.TEXT_ENCODING_UTF_8, data);

  }
}
