package com.newshunt.notification.model.internal.rest

import com.newshunt.dataentity.common.model.entity.model.ApiResponse
import com.newshunt.dataentity.common.model.entity.model.MultiValueResponse
import com.newshunt.dataentity.notification.asset.BaseNotificationAsset
import com.newshunt.dataentity.notification.asset.CricketDataStreamAsset
import com.newshunt.dataentity.notification.asset.GenericDataStreamAsset
import com.newshunt.dataentity.notification.asset.NewsStickyDataStreamAsset
import com.newshunt.dataentity.notification.asset.OptInEntity
import com.newshunt.dataentity.notification.asset.NewsStickyOptInEntity
import io.reactivex.Observable
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url

interface StreamAPI {

  @GET
  fun getStreamData(@Url url: String): Observable<Response<ApiResponse<CricketDataStreamAsset>>>

  @GET
  fun getGenericNotificationStreamData(@Url url: String) : Observable<Response<ApiResponse<GenericDataStreamAsset>>>

  @GET
  fun getMetaData(@Url url: String): Observable<Response<ApiResponse<Any>>>

  @GET
  fun getGenericNotificationData(@Url url: String) : Observable<ApiResponse<BaseNotificationAsset>>

  @GET("/api/v2/upgrade/dynamic/version?entity=EVENT_OPTIN")
  fun getServerNotifications(@Query("appLanguage") appLanguage: String,
                             @Query("version") version : String) : Observable<MultiValueResponse<OptInEntity>>

  @GET
  fun getNewsStickyItemsData(@Url url: String): Observable<Response<ApiResponse<NewsStickyDataStreamAsset>>>

  @GET("/api/v2/upgrade/dynamic/version?entity=NEWS_STICKY_OPTIN")
  fun getNewsStickyOptInConfig(@Query("version") version : String?) : Observable<ApiResponse<MultiValueResponse<NewsStickyOptInEntity>>>
}
