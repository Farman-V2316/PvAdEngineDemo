/*
* Copyright (c) 2019 Newshunt. All rights reserved.
*/
package com.newshunt.adengine.client

import com.newshunt.adengine.PersistAdUsecase
import com.newshunt.adengine.model.entity.version.AdPosition
import com.newshunt.adengine.model.entity.version.AdRequest
import com.newshunt.adengine.util.AdLogger
import com.newshunt.adengine.view.AdEntityConsumer
import com.newshunt.adengine.view.BackupAdsCache
import com.newshunt.common.helper.common.Constants
import com.newshunt.dhutil.helper.AdsUpgradeInfoProvider
import com.newshunt.news.model.sqlite.SocialDB
import com.newshunt.news.model.usecase.toMediator2
import com.newshunt.sdk.network.Priority
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Manager per zone to cap outgoing network requests.
 *
 * @author raunak.yadav
 */
class AdRequestManager(private val adPosition: AdPosition,
                       private val adEntityConsumer: AdEntityConsumer,
                       private val backupAdsCache: BackupAdsCache,
                       private val responseExecutor: ExecutorService) : AdRequestListener {

    override var isServerDown: Boolean = false
    var maxParallelRequests = AdsUpgradeInfoProvider.getInstance().adsUpgradeInfo?.maxParallelAdRequestCount
            ?: 2
    private val executor = Executors.newFixedThreadPool(maxParallelRequests)
    private val requestPool = ConcurrentHashMap<Int, AdRequest>()

    private fun canTriggerNwFetch(): Boolean {
        return requestPool.size < maxParallelRequests
    }

    fun triggerNwFetch(adRequest: AdRequest, uniqueRequestId: Int, priority: Priority): Boolean {
        if (!canTriggerNwFetch()) {
            AdLogger.e(LOG_TAG, "Cannot trigger n/w request id : $uniqueRequestId. Will wait")
            return false
        }
        if (requestPool.containsKey(uniqueRequestId)) {
            AdLogger.e(LOG_TAG, "Request for $adPosition::$uniqueRequestId already in progress.")
            requestPool[uniqueRequestId] = adRequest
            return true
        }
        requestPool[uniqueRequestId] = adRequest
        AdLogger.e(LOG_TAG, "$adPosition Request pool size : ${requestPool.size}")

        val excludeBannerList = StringBuilder()
        adEntityConsumer.excludeBannerIds.forEach {
            excludeBannerList.append(it).append(Constants.SEMICOLON)
        }

        AdRequestProcessor(adEntityConsumer, this, backupAdsCache, responseExecutor, adRequest,
                uniqueRequestId, priority, PersistAdUsecase(SocialDB.instance().adsDao()).toMediator2())
                .requestAdsFromServer(executor)
        return true
    }

    override fun onRequestComplete(uniqueRequestId: Int) {
        AdLogger.e(LOG_TAG, "Adrequest for $adPosition complete. id : $uniqueRequestId")
        requestPool.remove(uniqueRequestId)
        responseExecutor.execute {
            adEntityConsumer.doneProcessingRequest()
        }
    }
}

private const val LOG_TAG = "AdRequestManager"