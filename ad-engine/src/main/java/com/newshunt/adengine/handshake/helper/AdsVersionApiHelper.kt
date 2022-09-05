/*
* Copyright (c) 2019 Newshunt. All rights reserved.
*/
package com.newshunt.adengine.handshake.helper

import com.newshunt.adengine.handshake.network.AdsConfigApi
import com.newshunt.adengine.handshake.usecase.AdsHandshakeUsecase
import com.newshunt.adengine.util.AdLogger
import com.newshunt.common.model.retrofit.RestAdapterContainer
import com.newshunt.helper.CurrentAdProfileBuilder
import com.newshunt.sdk.network.Priority
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

/**
 * @author raunak.yadav
 */
object AdsVersionApiHelper {

    fun getAdsConfigApi(): AdsConfigApi {
        return RestAdapterContainer.getInstance()
                .getRestAdapter("https://qa-money.newshunt.com", Priority.PRIORITY_LOW, null)
                .create(AdsConfigApi::class.java)
    }

    fun performHandshake(): Disposable? {
        return AdsHandshakeUsecase(getAdsConfigApi()).invoke(CurrentAdProfileBuilder.getCurrentAdProfile())
                .subscribeOn(Schedulers.io())
                .subscribe({
                    AdLogger.d(TAG, "Ads Handshake success")
                }, {
                    AdLogger.d(TAG, "Ads handshake failed. ${it.message}")
                })
    }

}

private const val TAG = "AdsVersionApiHelper"