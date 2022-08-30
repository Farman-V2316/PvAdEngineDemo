package com.newshunt.common.model.apis

import com.newshunt.dataentity.common.asset.LikeAsset
import com.newshunt.dataentity.common.model.entity.model.ApiResponse
import com.newshunt.dataentity.common.model.entity.model.LikeMultiValueResponse
import com.newshunt.dataentity.common.model.entity.model.MultiValueResponse
import com.newshunt.dataentity.social.entity.InteractionPayload
import io.reactivex.Observable
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Url

interface InteractionAPI {

    @POST("entity/interactions/likes/bulk/operations")
    fun postLikes(@Body body: InteractionPayload): Observable<ApiResponse<Any>>

    @POST("entity/interactions/share/add")
    fun postShares(@Body body: InteractionPayload): Observable<ApiResponse<Any>>

    @GET("entity/interactions/likes/all")
    fun getLikes(): Observable<ApiResponse<MultiValueResponse<InteractionPayload.InteractionPayloadItem>>>

    @GET("entity/interactions/likes/entity/paged")
    fun getLikesForPost(@Query("namespace") namespace: String,
                                 @Query("entityId") entityId: String,
                                 @Query("type") type: String,
                                 @Query("start") start: Int,
                                 @Query("count") count: Int,
                                 @Query("filterAction") likeType: String?):
            Observable<ApiResponse<LikeMultiValueResponse>>

    @GET()
    fun getNextLikesForPost(@Url url: String):
            Observable<ApiResponse<MultiValueResponse<LikeAsset>>>


}
