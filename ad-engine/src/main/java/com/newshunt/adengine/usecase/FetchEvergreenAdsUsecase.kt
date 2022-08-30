/*
* Copyright (c) 2022 Newshunt. All rights reserved.
*/
package com.newshunt.adengine.usecase

import android.os.Bundle
import com.newshunt.adengine.PersistAdUsecase
import com.newshunt.adengine.RemovePersistedAdUsecase
import com.newshunt.adengine.client.AdClassifier
import com.newshunt.adengine.client.NativeAdInventoryManager
import com.newshunt.adengine.model.entity.AdResponse
import com.newshunt.adengine.model.entity.version.AdPosition
import com.newshunt.adengine.util.AdFrequencyStats
import com.newshunt.adengine.util.EvergreenSplashUtil
import com.newshunt.common.helper.common.Logger
import com.newshunt.dhutil.helper.AdsUpgradeInfoProvider
import com.newshunt.news.model.usecase.MediatorUsecase
import com.newshunt.news.model.usecase.Usecase
import io.reactivex.Observable
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Url
import java.lang.IllegalArgumentException
import java.net.HttpURLConnection

interface EvergreenAdsApi {
    @GET
    fun getAds(@Url url: String): Observable<Response<AdResponse>>
}

/**
 * Hit Evergreen Ads API
 * 200 -> Remove old data and persist new ads
 * 304 -> Keep using old data
 * Other -> Failure : Do not remove old data but signal for retry after a delay.
 *
 * @author raunak.yadav
 */
class FetchEvergreenAdsUsecase(private val api: EvergreenAdsApi,
                               private val persistAdUsecase: PersistAdUsecase,
                               private val removePersistedAdUsecase: MediatorUsecase<Bundle, Boolean>) :
    Usecase<String, Boolean> {

    override fun invoke(url: String): Observable<Boolean> {

        return api.getAds(url).flatMap { response ->
            Logger.d(TAG, "fetching evergreen ads code : ${response.code()}")
            if (response.isSuccessful) {
                //Remove previous ads from DB.
                removePersistedAdUsecase.execute(RemovePersistedAdUsecase.bundle(adPosition = AdPosition.EVERGREEN))
                EvergreenSplashUtil.clear()

                val adResponse = response.body()
                if (!adResponse.ads.isNullOrEmpty()) {
                    val adEntities = adResponse.ads
                    Logger.d(TAG, "fetching evergreen ads success : ${adEntities?.size}")
                    adEntities?.forEach {
                        it.adPosition = AdPosition.EVERGREEN
                        AdFrequencyStats.updateAndPersistFCDataFrom(it)
                    }
                    val adsToPersist = AdClassifier(adEntities).clubbedAds
                    return@flatMap persistAdUsecase.invoke(adsToPersist).map { result ->
                        Logger.d(TAG, "fetching evergreen ads success : $result")
                        if (result) {
                            val adsToProcess = AdsUpgradeInfoProvider.getInstance().adsUpgradeInfo?.evergreenAds?.noOfAdsToProcess
                            NativeAdInventoryManager.getEvergreenCacheInstance()?.let {
                                it.clearInventory()
                                it.readPersistedAds(adsToProcess)
                            }
                        }
                        true
                    }
                }
                Logger.d(TAG, "EG Ads response 200 but no/null ads")
                return@flatMap Observable.just(true)
            } else if (response.code() == HttpURLConnection.HTTP_NOT_MODIFIED) {
                Logger.d(TAG, "EG ads : 304 Not modified")
                return@flatMap Observable.just(true)
            }
            Observable.error(IllegalArgumentException("Eg ads Response failure " + "${response.code()}"))
        }
    }
}

private const val TAG = "EvergreenAds"