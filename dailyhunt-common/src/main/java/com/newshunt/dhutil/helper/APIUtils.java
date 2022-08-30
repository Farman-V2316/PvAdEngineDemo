/*
 * Copyright (c) 2017 Newshunt. All rights reserved.
 */
package com.newshunt.dhutil.helper;

import com.newshunt.common.helper.common.BaseErrorBuilder;
import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.common.helper.common.Constants;
import com.newshunt.common.helper.common.Logger;
import com.newshunt.dataentity.common.model.entity.BaseError;
import com.newshunt.dataentity.common.model.entity.ErrorTypes;
import com.newshunt.dataentity.common.model.entity.ListNoContentException;
import com.newshunt.dataentity.common.model.entity.model.NoConnectivityException;
import com.newshunt.common.view.DbgCode;
import com.newshunt.dhutil.R;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;

import androidx.annotation.NonNull;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import retrofit2.Response;

import static com.newshunt.sdk.network.internal.NetworkSDKLogger.NETWORKSDK_LOG_TAG;

/**
 * Utility class to host methods related to API requests
 *
 * @author: bedprakash.rout on 28/08/17.
 */

public class APIUtils {
  private static final String LOG_TAG =
      NETWORKSDK_LOG_TAG + "_" + APIUtils.class.getSimpleName();

  @NonNull
  public static BaseError getError(Response response) {
    final BaseError error;

    int statusCode = -1;
    ResponseBody errorBody = null;
    if (response != null) {
      statusCode = response.code();
      errorBody = response.errorBody();
    }
    switch (statusCode) {
      case HttpURLConnection.HTTP_NOT_FOUND: {
        Logger.e(LOG_TAG, "404 response");
        error =
            new BaseError(new DbgCode.DbgHttpCode(statusCode),
                CommonUtils.getString(R.string.no_content_found),
                HttpURLConnection
                    .HTTP_NOT_FOUND, null);
        break;
      }
      case HttpURLConnection.HTTP_NOT_MODIFIED: {
        Logger.e(LOG_TAG, "Cached response no error");
        error = BaseErrorBuilder.getBaseError(Constants.HTTP_304_NOT_MODIFIED, HttpURLConnection
            .HTTP_NOT_MODIFIED);
        break;
      }
      case HttpURLConnection.HTTP_INTERNAL_ERROR:
      case HttpURLConnection.HTTP_GATEWAY_TIMEOUT:
      case HttpURLConnection.HTTP_BAD_GATEWAY:
      case HttpURLConnection.HTTP_NOT_IMPLEMENTED:
      case HttpURLConnection.HTTP_UNAVAILABLE:
      case HttpURLConnection.HTTP_VERSION: {
        Logger.e(LOG_TAG, "Server Error " + statusCode);
        error =
            new BaseError(new DbgCode.DbgHttpCode(statusCode),
                CommonUtils.getString(R.string.error_server_issue), statusCode,
                null);
        break;
      }
      default: {
        try {
          if (errorBody != null) {
            String errorBodyValue = errorBody.string();
            Logger.e(LOG_TAG, "Request failed with " + errorBodyValue);
          }
        } catch (IOException e) {
          Logger.caughtException(e);
        }

        error =
            BaseErrorBuilder.getBaseError(ErrorTypes.API_STATUS_CODE_UNDEFINED,
                CommonUtils.getString(R.string.error_generic));
      }
    }
    if (errorBody != null) {
      errorBody.close();
    }
    return error;
  }

  public static BaseError getError(Throwable t) {
    if (t instanceof BaseError) {
      return (BaseError) t;
    }

    final BaseError error;
    if (t instanceof SocketTimeoutException) {
      error = new BaseError(
          t, CommonUtils.getApplication().getString(R.string.error_connectivity),
          null, null);
    } else if (t instanceof NoConnectivityException) {
      error = new BaseError(
          t, CommonUtils.getApplication().getString(R.string.error_no_connection),
          null, null);
    } else if (t instanceof UnknownHostException) {
      if (CommonUtils.isNetworkAvailable(CommonUtils.getApplication())) {
        error = new BaseError(
            t, CommonUtils.getApplication().getString(R.string.error_connectivity),
            null, null);
      } else {
        error = new BaseError(
            t, CommonUtils.getApplication().getString(R.string.error_no_connection),
            null, null);
      }
    } else if (t instanceof retrofit2.HttpException) {
      BaseError httpError = getError(((retrofit2.HttpException) t).response());
      httpError.setOriginalError(t);
      return httpError;
    } else if (t instanceof ListNoContentException) {
      error = ((ListNoContentException) t).getError();
    } else {
      error = new BaseError(
          t, CommonUtils.getApplication().getString(R.string.error_generic), null,
          null);
    }
    return error;
  }


  public static String getResponseAsString(@NonNull okhttp3.Response response) throws IOException {
    ResponseBody responseBody = response.body();
    if (responseBody == null) {
      return null;
    }
    long contentLength = responseBody.contentLength();
    BufferedSource source = responseBody.source();
    source.request(Long.MAX_VALUE); // Buffer the entire body.
    Buffer buffer = source.buffer();

    if (contentLength == 0) {
      return null;
    }
    return buffer.clone().readString(Charset.forName(Constants.TEXT_ENCODING_UTF_8));
  }

  public static String getGenericErrorMessage() {
    return CommonUtils.isNetworkAvailable(CommonUtils.getApplication()) ?
        CommonUtils.getApplication().getString(R.string.error_generic) :
        CommonUtils.getApplication().getString(R.string.error_no_connection);
  }

}
