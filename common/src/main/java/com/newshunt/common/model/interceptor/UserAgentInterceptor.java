package com.newshunt.common.model.interceptor;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * @author: bedprakash on 4/10/16.
 */

public class UserAgentInterceptor implements Interceptor {

  @Override
  public Response intercept(Chain chain) throws IOException {
    return addUserAgent(chain);
  }

  protected Response addUserAgent(Chain chain) throws IOException {
    // Customize the request
    Request request = chain.request().newBuilder()
        .header("User-Agent", System.getProperty("http.agent"))
        .build();

    return chain.proceed(request);
  }
}
