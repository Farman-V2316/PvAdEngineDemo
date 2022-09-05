/*
* Copyright (c) 2019 Newshunt. All rights reserved.
*/
package com.newshunt.adengine.handshake.network

import com.newshunt.dataentity.common.model.entity.model.ApiResponse
import com.newshunt.dataentity.dhutil.model.entity.adupgrade.AdsUpgradeInfo
import com.newshunt.dataentity.dhutil.model.entity.adupgrade.EntityContextMeta
import com.newshunt.dataentity.dhutil.model.entity.status.CurrentAdProfile
import io.reactivex.Observable
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Ads related apis.
 *
 * @author raunak.yadav
 */
interface AdsConfigApi {

    @POST("api/v1/handShake.php")
    fun performAdsHandshake(@Body currentAdProfile: CurrentAdProfile): Observable<ApiResponse<AdsUpgradeInfo>>

    @GET("publicVibe/v1/pvHandshake.json")
    fun performTestAdsHandshake(): Observable<ApiResponse<AdsUpgradeInfo>>

    @GET("api/v1/contentContextHandshake")
    fun adsContentContextHandshake(@Query("version") version: String): Observable<ApiResponse<EntityContextMeta>>

}