/*
* Copyright (c) 2021 Newshunt. All rights reserved.
*/
package com.newshunt.app.lptimer

import com.newshunt.common.helper.common.Logger
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dhutil.helper.retrofit.RestAdapterProvider
import com.newshunt.sdk.network.Priority
import io.reactivex.schedulers.Schedulers

/**
 * @author Mukesh Yadav
 */
object AdsGenericBeaconServiceImpl : AdsGenericBeaconService {

    private const val TAG = "AdsGenericBeaconServiceImpl"
    private var adsBeaconAPI: AdsGenericBeaconAPI = RestAdapterProvider.getRestAdapter(
        Priority.PRIORITY_NORMAL,
        TAG, false
    ).create(AdsGenericBeaconAPI::class.java)

    fun triggerAdLPTimeSpentBeaconEvent(timeSpentInSeconds: String, assadLPTimeSpentBeaconUrlet: String?, exitReason: TimeSpentOnLPExitReason?) {
        assadLPTimeSpentBeaconUrlet?.let {
            val requestPayload: Map<String, String>
            requestPayload = HashMap()
            requestPayload["timeSpent"] = timeSpentInSeconds
            if (exitReason != null)
                requestPayload["exitReason"] = exitReason.toString()
            if (!CommonUtils.isEmpty(assadLPTimeSpentBeaconUrlet)) {
                updateAdsGenericBeacon(assadLPTimeSpentBeaconUrlet, requestPayload)
            }
        }

    }

    override fun updateAdsGenericBeacon(requestUrl: String, playload: Map<String, String>) {
        adsBeaconAPI.updateGenericAdItemEvent(requestUrl = requestUrl, params = playload)
            .subscribeOn(Schedulers.io())
            .subscribe({
                Logger.d(TAG, "LP time spent response success")
            }, {
                Logger.d(TAG, "LP time spent response Failure")
            })
    }
}