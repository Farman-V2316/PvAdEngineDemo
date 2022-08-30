package com.newshunt.news.model.apis

import com.newshunt.dataentity.common.model.entity.model.ApiResponse
import com.newshunt.dataentity.common.pages.FollowPayload
import com.newshunt.dataentity.common.pages.FollowSyncResponse
import com.newshunt.dhutil.helper.preference.UserPreferenceUtil
import io.reactivex.Observable
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Url

interface FollowAPI {

  @POST("/api/v1/entity/interactions/follow/bulk/operations")
  fun postFollows(@Body followPayload : FollowPayload) : Observable<Any>

  @GET
  fun getFollows(@Url url: String):
      Observable<ApiResponse<FollowSyncResponse>>

}