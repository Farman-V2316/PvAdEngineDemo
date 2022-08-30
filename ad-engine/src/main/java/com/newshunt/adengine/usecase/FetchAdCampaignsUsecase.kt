/*
* Copyright (c) 2020 Newshunt. All rights reserved.
*/
package com.newshunt.adengine.usecase

import com.newshunt.common.helper.common.Logger
import com.newshunt.dataentity.common.model.entity.model.ApiResponse
import com.newshunt.dataentity.model.entity.AdCampaignsInfo
import com.newshunt.news.model.daos.AdFrequencyCapDao
import com.newshunt.news.model.usecase.Usecase
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Url

interface CampaignApi {
    @GET
    fun getCampaigns(@Url syncUrl: String): Observable<ApiResponse<AdCampaignsInfo>>
}

/**
 * @author raunak.yadav
 */
class FetchAdCampaignsUsecase(private val api: CampaignApi,
                              private val updateAdCampaignsUsecase: UpdateAdCampaignsUsecase) :
        Usecase<String, ApiResponse<AdCampaignsInfo>> {

    override fun invoke(syncUrl: String): Observable<ApiResponse<AdCampaignsInfo>> {

        return api.getCampaigns(syncUrl).flatMap { apiResponse ->
            if (apiResponse.data != null) {
                Logger.d("AdCampaignsFetchUsecase", "fetching campaigns success")
                updateAdCampaignsUsecase.invoke(apiResponse.data).map {
                    apiResponse
                }
            } else {
                Logger.d("AdCampaignsFetchUsecase", "error fetching campaigns")
                Observable.just(apiResponse)
            }
        }
    }
}

/**
 * Refresh the DB entries with network response for FC.
 *
 * @author raunak.yadav
 */
class UpdateAdCampaignsUsecase(private val adsDao: AdFrequencyCapDao) : Usecase<AdCampaignsInfo, Unit> {

    override fun invoke(p1: AdCampaignsInfo): Observable<Unit> {
        return Observable.fromCallable {
            adsDao.updateCampaignInfo(p1)
        }
    }
}