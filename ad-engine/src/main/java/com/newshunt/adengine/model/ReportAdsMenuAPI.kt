package com.newshunt.adengine.model

import com.newshunt.dataentity.dhutil.model.entity.adupgrade.ReportAdsMenuPostBodyEntity
import io.reactivex.Observable
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Url

interface ReportAdsMenuAPI {
    @POST
    fun postReportAdsMenu(@Url requestUrl: String, @Body postBody: ReportAdsMenuPostBodyEntity):
            Observable<Response<Any>>
}