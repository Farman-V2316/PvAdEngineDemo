/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */

package com.dailyhunt.tv.exolibrary.interceptors;

import com.dailyhunt.tv.exolibrary.R;
import com.newshunt.common.helper.common.BusProvider;
import com.newshunt.common.helper.common.Logger;
import com.newshunt.dataentity.common.helper.common.CommonUtils;

import java.io.IOException;
import java.net.HttpURLConnection;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class HttpNotFoundInterceptor implements Interceptor {
  public static final String TAG = HttpNotFoundInterceptor.class.getSimpleName();

  @Override
  public Response intercept(Chain chain) throws IOException {
    Request request = chain.request();
    Response response = chain.proceed(request);
    if (response.code() == HttpURLConnection.HTTP_NOT_FOUND) {
      Logger.d(TAG, "HTTP_NOT_FOUND firing event");
      BusProvider.getUIBusInstance()
          .post(new VideoEntityNotFoundEvent(HttpURLConnection.HTTP_NOT_FOUND,
              CommonUtils.getString(R.string.video_not_found)));
    }
    return response;
  }


  public class VideoEntityNotFoundEvent {
    private int code;
    private String message;

    public VideoEntityNotFoundEvent(int code, String message) {
      this.code = code;
      this.message = message;
    }

  }

}
