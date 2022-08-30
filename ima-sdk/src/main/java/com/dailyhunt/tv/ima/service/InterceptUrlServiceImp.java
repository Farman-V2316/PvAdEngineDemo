package com.dailyhunt.tv.ima.service;

import com.dailyhunt.tv.ima.api.InterceptRequestApi;
import com.newshunt.common.helper.appconfig.AppConfig;
import com.newshunt.dataentity.common.model.entity.model.ApiResponse;
import com.newshunt.common.model.retrofit.RestAdapterContainer;
import com.newshunt.sdk.network.Priority;

import io.reactivex.Single;
import retrofit2.Retrofit;

/**
 * Created by ketkigarg on 31/01/18.
 */

public class InterceptUrlServiceImp {
  private String url;
  private Object tag;

  public InterceptUrlServiceImp(Object tag, String url) {
    this.url = url;
    this.tag = tag;
  }

  public Single<ApiResponse<String>> hitDataUrl() {
    return getRetrofit().create(InterceptRequestApi.class).hitDataUrl(url);
  }

  private Retrofit getRetrofit() {
    return RestAdapterContainer.getInstance()
        .getDynamicRestAdapterRx(AppConfig.getInstance().getTVBaseUrl(), Priority.PRIORITY_HIGH,
            tag);
  }
}
