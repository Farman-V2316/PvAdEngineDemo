package com.newshunt.common.model.interceptor;

import com.newshunt.common.helper.common.Constants;
import com.newshunt.common.helper.common.Logger;
import com.newshunt.dataentity.common.model.entity.model.NoConnectivityException;
import com.newshunt.sdk.network.NetworkSDK;
import com.newshunt.sdk.network.internal.NetworkSDKLogger;
import com.newshunt.sdk.network.internal.NetworkSDKUtils;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * @author: bedprakash on 20/10/16.
 */

public class InternetConnectionInterceptor implements Interceptor {

  private static final String LOG_TAG = NetworkSDKLogger.NETWORKSDK_LOG_TAG + "_" +
      InternetConnectionInterceptor.class.getSimpleName();

  @Override
  public Response intercept(Chain chain) throws IOException {
    Request request = chain.request();
    if (!NetworkSDKUtils.isNetworkAvailable(NetworkSDK.getContext())) {
      Logger.e(LOG_TAG, String.format(
          "recieved Response " + Constants.ERROR_NO_INTERNET + " for %s No Connectivity ",
          request.url()));
      throw new NoConnectivityException("No Connectivity");
    }
    return chain.proceed(request);

  }
}
