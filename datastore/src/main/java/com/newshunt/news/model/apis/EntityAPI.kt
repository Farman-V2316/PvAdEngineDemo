package com.newshunt.news.model.apis

import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.info.ClientInfoHelper
import com.newshunt.dataentity.common.model.entity.model.ApiResponse
import com.newshunt.dataentity.common.pages.EntityInfoResponse
import com.newshunt.dataentity.common.pages.PageResponse
import com.newshunt.dataentity.common.pages.PageSyncBody
import com.newshunt.dhutil.helper.preference.UserPreferenceUtil
import io.reactivex.Observable
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface EntityAPI {

  @GET("/api/v2/entity/sync/users/{clientId}")
  fun getHomePages(@Path("clientId") clientId: String = ClientInfoHelper.getClientId(),
                   @Query("langCode") langCode: String = UserPreferenceUtil.getUserLanguages(),
                   @Query("appLanguage") appLanguage: String = UserPreferenceUtil.getUserNavigationLanguage(),
                   @Query("section") section:String,
                   @Query("version") version: String) : Observable<ApiResponse<PageResponse>>

  @POST("/api/v2/entity/sync/users/{clientId}")
  fun postHomePages(@Body pageSyncBody: PageSyncBody,
                    @Path("clientId") clientId: String = ClientInfoHelper.getClientId(),
                    @Query("langCode") langCode: String = UserPreferenceUtil.getUserLanguages(),
                    @Query("appLanguage") appLanguage: String= UserPreferenceUtil.getUserNavigationLanguage(),
                    @Query("section") section: String,
                    @Query("version") version: String) : Observable<ApiResponse<PageResponse>>

  @GET("/api/v2/entity/sync/page/tags")
  fun getPageableTopics(@Query("langCode") langCode: String = UserPreferenceUtil.getUserLanguages(),
                        @Query("appLanguage") appLanguage: String = UserPreferenceUtil.getUserNavigationLanguage(),
                        @Query("version") version: String,
                        @Query("section") section: String)
      : Observable<ApiResponse<PageResponse>>

  @GET("/api/v2/entity/sync/id/{pageId}")
  fun getEntityInfo(@Path("pageId") pageId : String,
                    @Query("type") entityType: String,
                    @Query("langCode") langCode: String,
                    @Query("appLanguage") appLanguage: String= UserPreferenceUtil.getUserNavigationLanguage(),
                    @Query("version") version: String = Constants.EMPTY_STRING,
                    @Query("section") section: String) : Observable<ApiResponse<EntityInfoResponse>>
}