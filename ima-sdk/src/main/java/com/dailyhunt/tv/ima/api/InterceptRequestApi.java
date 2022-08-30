package com.dailyhunt.tv.ima.api;

import com.newshunt.dataentity.common.model.entity.model.ApiResponse;

import io.reactivex.Single;
import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.Url;

/**
 * Created by ketkigarg on 31/01/18.
 */

public interface InterceptRequestApi {
  @GET()
  Single<ApiResponse<String>> hitDataUrl(@Url String path);
}
