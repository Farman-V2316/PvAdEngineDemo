package com.newshunt.news.model.apis

import com.newshunt.dataentity.common.asset.AllLocationResponse
import com.newshunt.dataentity.common.model.entity.model.ApiResponse
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * @author priya.gupta
 */
interface LocationApi {

    @GET("/api/v2/entity/sync/page/locations")
    fun getAllLocation(@Query("section") section: String?,
                         @Query("appLanguage") appLanguage: String?,
                         @Query("langCode") languages: String?,
                         @Query("version") version: String?): Observable<ApiResponse<AllLocationResponse>>
}