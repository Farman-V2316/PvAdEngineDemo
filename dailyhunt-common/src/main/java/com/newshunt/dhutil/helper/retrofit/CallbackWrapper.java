/*
 * Copyright (c) 2017 Newshunt. All rights reserved.
 */
package com.newshunt.dhutil.helper.retrofit;

import android.content.Context;

import com.newshunt.common.helper.common.BaseErrorBuilder;
import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.common.helper.common.Constants;
import com.newshunt.common.helper.common.Logger;
import com.newshunt.dataentity.common.model.entity.BaseError;
import com.newshunt.dataentity.common.model.entity.ErrorTypes;
import com.newshunt.dataentity.common.model.entity.ListNoContentException;
import com.newshunt.dataentity.common.model.entity.model.BaseErrorReportingResponse;
import com.newshunt.dataentity.common.model.entity.model.NoConnectivityException;
import com.newshunt.dhutil.R;
import com.newshunt.common.track.DailyhuntUtils;
import com.newshunt.common.view.DbgCode;

import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.newshunt.sdk.network.internal.NetworkSDKLogger.NETWORKSDK_LOG_TAG;

/**
 * Wrapper class to parse and handle errors from retrofit.
 *
 * @author: bedprakash on 12/10/16.
 */

public abstract class CallbackWrapper<T> implements Callback<T> {

  private static final String LOG_TAG = NETWORKSDK_LOG_TAG + "_" +
      CallbackWrapper.class.getSimpleName();

  @Override
  public void onResponse(Call<T> call, Response<T> response) {
    if (response.isSuccessful()) {
      // setting url to be used to report 200 code with 0 rows.
      if (response.body() instanceof BaseErrorReportingResponse) {
        ((BaseErrorReportingResponse) response.body()).setUrl(call.request().url().toString());
      }
      onSuccess(response.body());
    } else {
      BaseError error = getError(call, response);
      onError(error);
    }
    DailyhuntUtils.fireTrackRequestForApi(response);
  }

  @Override
  public void onFailure(Call<T> call, Throwable t) {
    Logger.e(LOG_TAG, "Connectivity issues");
    BaseError error = getError(t);
    onError(error);
  }

  public abstract void onSuccess(T response);

  public abstract void onError(BaseError error);

  public static BaseError getError(Call call, Response response) {
    BaseError error;
    int statusCode = response.code();
    ResponseBody errorBody = response.errorBody();
    String url = call.request().url().toString();
    Context context = CommonUtils.getApplication();
    switch (statusCode) {
      case HttpURLConnection.HTTP_OK:
        //code 200 but body is null.
      case HttpURLConnection.HTTP_NO_CONTENT:
      case HttpURLConnection.HTTP_NOT_FOUND: {
        Logger.e(LOG_TAG, statusCode + " response");
        error = new BaseError(
            new DbgCode.DbgHttpCode(statusCode),
            CommonUtils.getString(R.string.no_content_found),statusCode, url);
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
        error = new BaseError(new DbgCode.DbgHttpCode(statusCode),
            CommonUtils.getString(R.string.error_server_issue),statusCode,
            null);
        break;
      }
      default: {
        Logger.e(LOG_TAG, "Request " + call.request().url() + " failed with " + errorBody);
        error =
            BaseErrorBuilder.getBaseError(ErrorTypes.API_STATUS_CODE_UNDEFINED,
                context.getString(R.string.error_generic));
      }
    }
    //Code 200, 204 is considered success by okHttp, so errorBody will be null.
    if (errorBody != null) {
      errorBody.close();
    }
    return error;
  }

  public static BaseError getError(Throwable t) {
    BaseError error;
    if (t instanceof SocketTimeoutException) {
      error = new BaseError(t, CommonUtils.getApplication().getString(R.string.error_connectivity),null,
          null);
    } else if (t instanceof NoConnectivityException) {
      error = new BaseError(t, CommonUtils.getApplication().getString(R.string.error_no_connection),null
          ,null);
    } else if (t instanceof UnknownHostException) {
      if (CommonUtils.isNetworkAvailable(CommonUtils.getApplication())) {
        error = new BaseError(t, CommonUtils.getApplication().getString(R.string.error_connectivity),
            null,null);
      } else {
        error = new BaseError(t, CommonUtils.getApplication().getString(R.string.error_no_connection),
            null,null);
      }
    } else if (t instanceof ListNoContentException) {
      error = ((ListNoContentException) t).getError();
    } else {
      error = new BaseError(t, CommonUtils.getApplication().getString(R.string.error_generic),null,null);
    }
    return error;
  }

}
