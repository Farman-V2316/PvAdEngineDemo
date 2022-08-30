/*
* Copyright (c) 2017 Newshunt. All rights reserved.
*/
package com.newshunt.dhutil.helper.interceptor;

import androidx.annotation.NonNull;

import com.newshunt.common.helper.common.ApiResponseUtils;
import com.newshunt.news.model.repo.CardSeenStatusRepo;

import java.io.IOException;
import java.net.HttpURLConnection;

import okhttp3.Interceptor;
import okhttp3.Response;

/**
 * Interceptor to validate server response for 204 code and empty success cases.
 *
 * @author raunak.yadav
 */
public class NewsListErrorResponseInterceptor implements Interceptor {

  @Override
  public Response intercept(@NonNull Chain chain) throws IOException {
    String url = chain.request().url().toString();
    Response response = null;
    try {
      response = chain.proceed(chain.request());

      if (isSuccessfulButEmptyResponse(response)) {
        throw ApiResponseUtils.composeListNoContentError(url, response.code());
      }
    } finally {
      CardSeenStatusRepo.getDEFAULT().onAPIResponse(response, chain.request());
    }
    return response;
  }

  protected boolean isSuccessfulButEmptyResponse(Response response) throws IOException {
    return response.isSuccessful() && (response.code() == HttpURLConnection.HTTP_NO_CONTENT ||
        response.peekBody(1L).string().isEmpty());
  }
}