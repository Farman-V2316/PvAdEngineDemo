/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.dhutil.model.service;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;

import com.newshunt.common.helper.common.BusProvider;
import com.newshunt.common.helper.common.Constants;
import com.newshunt.common.helper.common.Logger;
import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.dataentity.common.model.entity.model.ApiResponse;
import com.newshunt.dataentity.common.model.entity.model.NoConnectivityException;
import com.newshunt.dataentity.common.model.entity.model.Status;
import com.newshunt.dataentity.common.model.entity.model.StatusError;
import com.newshunt.common.model.retrofit.RestAdapterContainer;
import com.newshunt.dhutil.R;
import com.newshunt.common.track.DailyhuntUtils;
import com.newshunt.sdk.network.Priority;
import com.newshunt.sdk.network.internal.NetworkSDKLogger;

import java.lang.annotation.Annotation;
import java.net.UnknownHostException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Converter;
import retrofit2.Response;

/**
 * Helper around retrofit to handle NoConnectivity cases
 *
 * @author arun.babu
 */
public abstract class BaseService<RESPONSE> {

  private static final String LOG_TAG =
      NetworkSDKLogger.NETWORKSDK_LOG_TAG + "_" + BaseService.class.getSimpleName();
  private static ConnectivityManager manager;
  private int uniqueId;

  private final Callback<ApiResponse<RESPONSE>> callback = new Callback<ApiResponse<RESPONSE>>() {
    @Override
    public void onResponse(Call<ApiResponse<RESPONSE>> call,
                           Response<ApiResponse<RESPONSE>> apiResponse) {
      if (apiResponse.isSuccessful()) {
        storeInDBorCache(apiResponse.body().getData(), apiResponse, uniqueId);
        handleResponse(apiResponse.body().getData(), apiResponse, uniqueId);
        DailyhuntUtils.fireTrackRequestForApi(apiResponse);
      } else {
        handleError(apiResponse, uniqueId);
      }
    }

    @Override
    public void onFailure(Call<ApiResponse<RESPONSE>> call, Throwable t) {
      String code = String.valueOf(Constants.ERROR_UNEXPECTED);
      String message = CommonUtils.getString(R.string.unexpected_error_message);
      String description = Constants.EMPTY_STRING;
      if (t instanceof UnknownHostException) {
        code = String.valueOf(Constants.ERROR_NO_INTERNET);
        message = CommonUtils.getString(R.string.error_no_connection);
      }

      handleError(new Status(code, message), uniqueId);
    }
  };

  private boolean isConnected(ConnectivityManager manager) {
    NetworkInfo activeNetwork = manager.getActiveNetworkInfo();
    return activeNetwork != null && activeNetwork.isConnected();
  }

  private void handleError(Response error, int uniqueId) {

    String code = Constants.ERROR_UNEXPECTED;
    String message = CommonUtils.getString(R.string.unexpected_error_message);

    //Worst case scenario where error itself null or its kind is null.
    if (error == null) {
      handleError(new Status(code, message), uniqueId);
      return;
    }
    // TODO:(Retrofit update) finalize on this.(Make default retrofit object)
    Converter<ResponseBody, ApiResponse> converter =
        RestAdapterContainer.getInstance()
            .getRestAdapter("http://demo.com/", Priority.PRIORITY_LOW, null)
            .responseBodyConverter(ApiResponse.class, new Annotation[0]);
    ApiResponse response;
    try {
      response = converter.convert(error.errorBody());
    } catch (Exception e) {
      response = null;
    }
    // From server got response with some error messages
    if (response != null && response.getStatus() != null) {
      Status status = response.getStatus();
      code = status.getCode();
      message = status.getMessage();
      handleError(new Status(code, message, StatusError.HTTP_ERROR), uniqueId);
      return;
    }

    // For debug purpose we enable the logger.
    if (error.errorBody() != null && !CommonUtils.isEmpty(error.message())) {
      Logger.d(LOG_TAG, "ERROR " + error.message());
    }
    // Connection manager or Server error handling
    code = Constants.EMPTY_STRING;
    handleError(new Status(code, message, StatusError.UNEXPECTED_ERROR), uniqueId);
  }

  public final void request(final int uniqueId) {
    final RESPONSE response = getFromDBorCache();
    if (response != null) {
      Handler handler = new Handler(Looper.getMainLooper());
      handler.post(new Runnable() {
        @Override
        public void run() {
          Response<RESPONSE> retrofitResponse = Response.success(response);
          handleResponse(response, Response.success(response), uniqueId);
        }
      });
    }

    this.uniqueId = uniqueId;
    if (null == manager) {
      manager = (ConnectivityManager) CommonUtils.getApplication()
          .getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    if (!isConnected(manager)) {
      final String reason = CommonUtils.getString(R.string.no_connection_error);
      Handler handler = new Handler(Looper.getMainLooper());
      final NoConnectivityException noConnectivityException = new NoConnectivityException(reason);
      handler.post(new Runnable() {
        @Override
        public void run() {
          BusProvider.getUIBusInstance().post(noConnectivityException);
        }
      });
      handler.post(new Runnable() {
        @Override
        public void run() {

          handleError(new Status(Constants.ERROR_NO_INTERNET, Constants.EMPTY_STRING,
              StatusError.NETWORK_ERROR), uniqueId);
        }
      });
    } else {
      execute(callback);
    }
  }

  protected RESPONSE getFromDBorCache() {
    return null;
  }

  protected void storeInDBorCache(RESPONSE response, Response retrofitResponse, int uniqueId) {
  }

  protected abstract void handleResponse(RESPONSE response, Response retrofitResponse,
                                         int uniqueId);

  protected abstract void handleError(Status status, int uniqueId);

  protected abstract void execute(Callback<ApiResponse<RESPONSE>> callback);
}
