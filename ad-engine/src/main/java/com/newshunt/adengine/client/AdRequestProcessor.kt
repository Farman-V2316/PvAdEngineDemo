/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.adengine.client

import com.coolfie_exo.download.ExoDownloadHelper
import com.newshunt.adengine.R
import com.newshunt.adengine.model.AdReadyHandler
import com.newshunt.adengine.model.entity.AdsFallbackEntity
import com.newshunt.adengine.model.entity.BaseAdEntity
import com.newshunt.adengine.model.entity.BaseDisplayAdEntity
import com.newshunt.adengine.model.entity.ExternalSdkAd
import com.newshunt.adengine.model.entity.version.AdContentType
import com.newshunt.adengine.model.entity.version.AdPosition
import com.newshunt.adengine.model.entity.version.AdRequest
import com.newshunt.adengine.model.entity.version.ExternalSdkAdType
import com.newshunt.adengine.processor.AdProcessorFactory
import com.newshunt.adengine.util.AdLogger
import com.newshunt.adengine.util.AdsUtil
import com.newshunt.adengine.util.AmazonBidUtilities
import com.newshunt.adengine.view.AdEntityConsumer
import com.newshunt.adengine.view.BackupAdsCache
import com.newshunt.common.helper.common.AndroidUtils
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger
import com.newshunt.dataentity.common.asset.AdCacheType
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dhutil.helper.AdsUpgradeInfoProvider
import com.newshunt.news.model.usecase.MediatorUsecase
import com.newshunt.sdk.network.Priority
import java.io.InputStream
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

/**
 * Processes ad request from the inventory manager. This is the brain of ad-engine.
 * It uses different component to prepare valid ads that can be sent to inventory.
 *
 * AdFetcher helps fetch ad. Different parsers help convert content in different
 * ad types. If it also does post processing of ad response if required.
 *
 * @author shreyas.desai
 */
class AdRequestProcessor(private val adEntityConsumer: AdEntityConsumer,
                         private val adRequestListener: AdRequestListener,
                         private val backupAdsCache: BackupAdsCache?,
                         private val responseExecutor: ExecutorService,
                         private val adRequest: AdRequest,
                         private val uniqueRequestId: Int,
                         private val priority: Priority,
                         private val persistAdUsecase: MediatorUsecase<List<AdsFallbackEntity>, Boolean>)
    : AdFetcher.AdRequester, AdReadyHandler {

    private val adFetcher: AdFetcher = AdFetcher(this)
    private var executeDefaultSdkOrder = AtomicBoolean(false)
    private var pendingAdsProcessingCount = AtomicInteger(0)
    private var numberOfRetries: Int = 0

    private fun fetchAdsFromServer() {
        val excludeBannerList = StringBuilder()
        val excludeBannerIds = adEntityConsumer.excludeBannerIds
        if (!CommonUtils.isEmpty(excludeBannerIds)) {
            for (id in excludeBannerIds) {
                excludeBannerList.append(id).append(Constants.SEMICOLON)
            }
        }

        AdLogger.d(LOG_TAG, "fetchAdsFromServer for request with id :$uniqueRequestId")
        try {
            adFetcher.run(adRequest, excludeBannerList.toString(), uniqueRequestId, priority)
        } catch (e: Exception) {
            AdLogger.e("AdError", "Error fetching ad ${e.message}")
        }
    }

    private fun clearPendingRequests() {
        adFetcher.clearPendingRequests()
    }

    /**
     * For cases when Ads server is down, we directly hit default sdk adUnitIds.
     */
    private fun requestAdsInDefaultSdkOrder(adRequest: AdRequest) {
        executeDefaultSdkOrder.set(true)
        val adsReceived = parseDefaultSdkResponse(adRequest.zoneType)
        processReceivedAds(adsReceived, adRequest)
    }

    override fun onAdReceivedFromServer(data: String?, adRequest: AdRequest, uniqueRequestId: Int) {
        if (data != null) {
            val adEntities = AdProcessorHelper.parseJson(data)
            if (!CommonUtils.isEmpty(adEntities)) {
                AdLogger.d(LOG_TAG, "Total ads received :${adEntities.size} for id :$uniqueRequestId")

                if (AdPosition.SPLASH == adRequest.zoneType ||
                        AdPosition.SPLASH_DEFAULT == adRequest.zoneType) {
                    val iterator = adEntities.iterator()
                    while (iterator.hasNext()) {
                        val adEntity = iterator.next()
                        // Remove if ad is invalid for splash zone.
                        if (!isValidSplashAd(adEntity)) {
                            iterator.remove()
                            continue
                        }
                    }
                }

                adRequest.amazonSdkPayload?.requestBody?.let { body ->
                    if (body.size > 0) {
                        if (adRequest.zoneType == AdPosition.SUPPLEMENT) {
                            val supplementSubSlots = mutableListOf<String>()
                            AdsUpgradeInfoProvider.getInstance().adsUpgradeInfo?.supplementAdConfig?.tagOrder?.let {
                                if (it.isNotEmpty()) {
                                    supplementSubSlots.addAll(it)
                                }
                                supplementSubSlots.forEach { tag ->
                                    AmazonBidUtilities.removeSlots(AdsUtil.getAdSlotName(tag, AdPosition.SUPPLEMENT))
                                }
                            }
                        } else {
                            AmazonBidUtilities.removeSlots(adRequest.zoneType.toString())
                        }
                    }
                }
            }
            processReceivedAds(adEntities, adRequest)
        } else {
            onAdRequestFailedAtServer(adRequest, uniqueRequestId)
        }
    }

    override fun onAdRequestFailedAtServer(adRequest: AdRequest, uniqueRequestId: Int) {
        if (!adRequestListener.isServerDown) {
            adRequestListener.isServerDown = true
            clearPendingRequests()

            if (numberOfRetries < 2) {
                numberOfRetries++
                processReceivedAds(null, adRequest)
                startServerDownTimer(RETRY_TIME)
            } else {
                numberOfRetries = 0
                requestAdsInDefaultSdkOrder(adRequest)
            }
        } else {
            adRequestListener.onRequestComplete(uniqueRequestId)
        }
    }

    override fun onAdRequestError(errorMessage: String?, uniqueRequestId: Int) {
        AdLogger.d(LOG_TAG,
                "Adrequest error for id : $uniqueRequestId. Message $errorMessage")
        adRequestListener.onRequestComplete(uniqueRequestId)
    }

    private fun startServerDownTimer(delay: Long) {
        AndroidUtils.getMainThreadHandler().postDelayed({
            adRequestListener.isServerDown = false
            executeDefaultSdkOrder.set(false)
        }, delay * 1000)
    }

    private fun processReceivedAds(adsReceived: List<BaseDisplayAdEntity>?, adRequest: AdRequest) {
        if (CommonUtils.isEmpty(adsReceived)) {
            adRequestListener.onRequestComplete(uniqueRequestId)
            return
        }

        val adClassifier = AdClassifier(adsReceived)

        AdLogger.d(LOG_TAG, "Number of clubbed ads to be processed : ${adClassifier.clubbedAds.size}")

        if (pendingAdsProcessingCount.addAndGet(adClassifier.clubbedAds.size) == 0) {
            adRequestListener.onRequestComplete(uniqueRequestId)
            return
        }
        processGroupedAds(adClassifier.clubbedAds, adRequest)
    }

    private fun prefetchVideoAds(baseAdEntity: BaseAdEntity) {
        var mediaItem = AdsUtil.getMediaItemFromAds(baseAdEntity)
        ExoDownloadHelper.downloadStream(mediaItem)
    }

    private fun processGroupedAds(groupedAds: List<AdsFallbackEntity>, adRequest: AdRequest) {
        for (adsFallbackEntity in groupedAds) {

            persistAdsIfNeeded(adsFallbackEntity)
            val processor = AdProcessorFactory.getAdProcessor(adsFallbackEntity, this, backupAdsCache, responseExecutor)
            processor.processAdContent(adRequest)
        }
    }

    private fun persistAdsIfNeeded(adsFallbackEntity: AdsFallbackEntity) {
        val ad = adsFallbackEntity.baseAdEntities[0]
        if (ad.adContextRules?.cacheType == AdCacheType.PERSIST) {
            persistAdUsecase.execute(listOf(adsFallbackEntity))
        }
    }

    private fun parseDefaultSdkResponse(adPosition: AdPosition): List<BaseDisplayAdEntity> {
        val inputStream: InputStream? = when (adPosition) {
            AdPosition.LIST_AD -> CommonUtils.getApplication().resources.openRawResource(
                    R.raw.default_sdk_order_cardp1)
            AdPosition.PGI -> CommonUtils.getApplication().resources.openRawResource(
                    R.raw.default_sdk_order_pgi)
            AdPosition.STORY -> CommonUtils.getApplication().resources.openRawResource(
                    R.raw.default_sdk_order_story)
            else -> null
        }
        return if (inputStream == null) {
            ArrayList()
        } else AdProcessorHelper.parseJson(AdProcessorHelper.readInputStream(inputStream))
    }

    fun requestAdsFromServer(executor: ExecutorService) {
        if (adRequest.numOfAds > 0) {
            // Setting google ad id requires running in separate thread.
            executor.execute {
                try {
                    if (executeDefaultSdkOrder.get()) {
                        requestAdsInDefaultSdkOrder(adRequest)
                    } else {
                        fetchAdsFromServer()
                    }
                } catch (e: Exception) {
                    Logger.caughtException(e)
                    adRequestListener.onRequestComplete(uniqueRequestId)
                }
            }
        } else {
            adRequestListener.onRequestComplete(uniqueRequestId)
        }
    }
/*

    fun isServerDown(): Boolean {
        return isServerDown && !executeDefaultSdkOrder
    }
*/

    override fun onReady(baseAdEntity: BaseAdEntity?) {
        if (executeDefaultSdkOrder.get()) {
            startServerDownTimer(DEFAULT_SDK_ORDER_RETRY_TIME)
        }

        AdLogger.w(LOG_TAG, "Response after processing : $baseAdEntity")
        pendingAdsProcessingCount.decrementAndGet()
        AdLogger.d(LOG_TAG, "Number of remaining ads to be processed : $pendingAdsProcessingCount")

        if (baseAdEntity != null) {
            AdLogger.d(LOG_TAG, "Processed ad with type = " + baseAdEntity.type!!)
            adEntityConsumer.consumeNextSet(listOf(baseAdEntity), true)
        } else {
            AdLogger.d(LOG_TAG, "Ad processed returned null")
        }

        if(baseAdEntity is ExternalSdkAd && baseAdEntity.external?.data == ExternalSdkAdType.IMA_SDK.adType) {
            prefetchVideoAds(baseAdEntity)
        }

        if (pendingAdsProcessingCount.get() <= 0) {
            pendingAdsProcessingCount.set(0)
            adRequestListener.onRequestComplete(uniqueRequestId)
        }
    }

    /**
     * currently valid ad types for splash ad are mraid-zip and pgi-zip
     *
     * @param baseAdEntity
     * @return true if valid. false otherwise
     */
    private fun isValidSplashAd(baseAdEntity: BaseAdEntity?): Boolean {
        return when (baseAdEntity?.adPosition) {
            AdPosition.SPLASH_DEFAULT -> {
                when (baseAdEntity.type) {
                    AdContentType.PGI_ARTICLE_AD,
                    AdContentType.EMPTY_AD -> true
                    else -> false
                }
            }
            AdPosition.SPLASH -> {
                when (baseAdEntity.type) {
                    AdContentType.EMPTY_AD,
                    AdContentType.MRAID_ZIP,
                    AdContentType.PGI_ZIP -> true
                    else -> false
                }
            }
            else -> false
        }
    }

    companion object {
        private const val RETRY_TIME: Long = 10
        private const val DEFAULT_SDK_ORDER_RETRY_TIME: Long = 60
        private const val LOG_TAG = "AdRequestProcessor"
    }
}

interface AdRequestListener {
    var isServerDown: Boolean
    fun onRequestComplete(uniqueRequestId: Int)
}