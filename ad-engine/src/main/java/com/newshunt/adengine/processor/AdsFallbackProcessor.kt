/*
 * Copyright (c) 2020 Newshunt. All rights reserved.
 */
package com.newshunt.adengine.processor

import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.newshunt.adengine.client.HttpClientManager
import com.newshunt.adengine.model.AdReadyHandler
import com.newshunt.adengine.model.entity.AdsFallbackEntity
import com.newshunt.adengine.model.entity.BaseAdEntity
import com.newshunt.adengine.model.entity.BaseDisplayAdEntity
import com.newshunt.adengine.model.entity.MultipleAdEntity
import com.newshunt.adengine.model.entity.version.AdPosition
import com.newshunt.adengine.model.entity.version.AdRequest
import com.newshunt.adengine.processor.AdProcessorFactory.getAdProcessor
import com.newshunt.adengine.util.AdLogger
import com.newshunt.adengine.util.AdsUtil
import com.newshunt.adengine.view.BackupAdsCache
import com.newshunt.common.helper.common.Logger
import com.newshunt.dhutil.helper.common.DailyhuntConstants
import com.newshunt.sdk.network.Priority
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.ExecutorService

/**
 * Processes ads from a AdsFallbackEntity as per fallback model.
 *
 * @author raunak.yadav
 */
class AdsFallbackProcessor(private val adsFallbackEntity: AdsFallbackEntity,
                           private val adReadyHandler: AdReadyHandler,
                           private val backupAdsCache: BackupAdsCache?,
                           private val responseExecutor: ExecutorService,
                           private val isPersistedAd: Boolean = false) : BaseAdProcessor, AdReadyHandler {

    private val handlerThread: HandlerThread = HandlerThread(BG_HANDLER_TAG)
    private val bgHandler: Handler
    private var index = 0
    private var adRequest: AdRequest? = null

    init {
        handlerThread.start()
        bgHandler = Handler(handlerThread.looper)
    }

    override fun processAdContent(adRequest: AdRequest?) {
        this.adRequest = adRequest
        val baseAdEntity = adsFallbackEntity.baseAdEntities[index]
        fetchAd(this, baseAdEntity)
    }

    /**
     * Needs to run on a background thread as network operations are performed.
     */
    private fun fetchAd(adReadyHandler: AdReadyHandler, baseAdEntity: BaseAdEntity) {

        // Direct ads in carousel, clubbed as MultipleAdEntity, can return immediately.
        // ContentAds should proceed as they need another hop.
        if (baseAdEntity is MultipleAdEntity) {
            if (baseAdEntity.baseDisplayAdEntities.isNotEmpty() && !baseAdEntity.baseDisplayAdEntities[0].isContentEnabledAd()) {
                onReady(baseAdEntity)
                return
            }
        }
        bgHandler.post {
            try {
                baseAdEntity.campaignId?.let {
                    //Add the last Ad's campaign id is logged to Crashlytics. Helps to debug Field crashes
                    if (it.isNotBlank()) {
                        FirebaseCrashlytics.getInstance().setCustomKey(DailyhuntConstants.CRASHLYTICS_KEY_NEW_AD_PROCESSING, it)
                    }
                }
            } catch (e: Exception) {
                Logger.caughtException(e)
            }
            adRequest?.let {
                getAdProcessor(baseAdEntity, adReadyHandler).processAdContent(it)
            }
        }
    }

    override fun onReady(baseAdEntity: BaseAdEntity?) {
        responseExecutor.execute {
            var processingBackupAd = false
            if (baseAdEntity != null) {
                AdLogger.d(LOG_TAG, "Sending " + baseAdEntity.type + " ad : " + baseAdEntity.uniqueAdIdentifier)

                if (!isPersistedAd && baseAdEntity.adPosition != AdPosition.SPLASH_DEFAULT) {
                    AdsUtil.hitAdRespondedUrl(baseAdEntity)
                }
                adReadyHandler.onReady(baseAdEntity)
                processingBackupAd = baseAdEntity is BaseDisplayAdEntity && baseAdEntity.needsBackupAds ?: false
                        && backupAdsCache != null && backupAdsCache.getBackupAd(adRequest) == null
                if (!processingBackupAd) {
                    adRequest = null
                    handlerThread.quit()
                    return@execute
                }
                AdLogger.d(LOG_TAG, "Processing backup ad for adId = " + baseAdEntity.uniqueAdIdentifier)
            }
            if (adRequest == null) {
                AdLogger.d(LOG_TAG, "Activity null. Response already returned. Aborting further " +
                        "fallback.")
                handlerThread.quit()
                return@execute
            }
            index++
            if (index >= adsFallbackEntity.baseAdEntities.size) {
                AdLogger.w(LOG_TAG, "no ad returned for adGroupId : " + adsFallbackEntity.adGroupId)
                adReadyHandler.onReady(null)
                adRequest = null
                handlerThread.quit()
                return@execute
            }
            val adEntity = adsFallbackEntity.baseAdEntities[index]
            adEntity.isBackUpAd = processingBackupAd
            if (!adEntity.isEvergreenAd) {
                hitRequestUrl(adEntity.requestUrl)
            }
            AdLogger.d(LOG_TAG, "processing fallback")
            fetchAd(this, adEntity)
        }
    }

    private fun hitRequestUrl(url: String?) {
        url ?: return
        try {
            val call = HttpClientManager.newRequestCall(url, Priority.PRIORITY_NORMAL) ?: return
            call.enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    AdLogger.e(LOG_TAG, "hitRequestUrl failed $url")
                }

                override fun onResponse(call: Call, response: Response) {
                    AdLogger.v(LOG_TAG, "hitRequestUrl SUCCESS $url")
                    response.close()
                }
            })
        } catch (e: Exception) {
            AdLogger.e(LOG_TAG, e.toString())
        }
    }

    companion object {
        private const val LOG_TAG = "AdsFallbackProcessor"
        private const val BG_HANDLER_TAG = "Ads_task_Handler"
    }
}