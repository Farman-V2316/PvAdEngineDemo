/*
 * Copyright (c) 2017 Dailyhunt. All rights reserved.
 */

package com.newshunt.news.helper;

import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.sdk.network.connection.ConnectionManager;

import java.io.IOException;
import java.util.Map;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Interceptor to add additional image quality headers for image requests made through picasso
 *
 * @author karthik.r
 */
public class ImageQualityInterceptor implements Interceptor {

  private static final String IMAGE_QUALITY_HEADER = "imageq";

  private final Map<String, String> imageQualityMap;

  public ImageQualityInterceptor(Map<String, String> imageQualityMap) {
    this.imageQualityMap = imageQualityMap;
  }

  @Override
  public Response intercept(Chain chain) throws IOException {
    Request.Builder builder = chain.request().newBuilder();
    if (imageQualityMap != null) {
      String imageQualityValue =
          imageQualityMap.get(
              ConnectionManager.getInstance()
                  .getCurrentConnectionSpeed(CommonUtils.getApplication())
                  .name());
      if (imageQualityValue != null) {
        builder.removeHeader(IMAGE_QUALITY_HEADER);
        builder.addHeader(IMAGE_QUALITY_HEADER, imageQualityValue);
      }
    }

    Request request = builder.build();
    return chain.proceed(request);
  }
}