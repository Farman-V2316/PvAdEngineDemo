package com.dailyhunt.tv.players.service;

import android.content.*;

import com.dailyhunt.tv.players.api.*;
import com.dailyhunt.tv.players.model.entities.server.*;
import com.newshunt.common.model.retrofit.*;
import com.newshunt.dataentity.common.model.entity.*;
import com.newshunt.dataentity.common.model.entity.model.*;
import com.newshunt.dhutil.helper.retrofit.*;
import com.newshunt.sdk.network.*;

/**
 * Created by Jayanth on 09/05/18.
 */
public class PlayerErrorReportServiceImpl {

  private final Context context;
  private PlayerErrorReportAPI playerErrorReportAPI = null;

  public PlayerErrorReportServiceImpl(Context context) {
    this.context = context;
    this.playerErrorReportAPI = getErrorReportAPI(Priority.PRIORITY_HIGH);
  }

  private PlayerErrorReportAPI getErrorReportAPI(Priority priority) {
    return RestAdapterContainer.getInstance().getRestAdapter(
        NewsBaseUrlContainer.getApplicationUrl(), priority, null).create(PlayerErrorReportAPI.class);
  }

  public void reportVideoError(PlayerErrorInfo postBody) {
    playerErrorReportAPI.reportError(postBody).enqueue(getRetrofitCallback());
  }

  private CallbackWrapper<ApiResponse<Object>> getRetrofitCallback() {
    return new CallbackWrapper<ApiResponse<Object>>() {

      @Override
      public void onSuccess(ApiResponse<Object> response) {
        //ignore
      }

      @Override
      public void onError(BaseError error) {
        //ignore
      }
    };
  }


}
