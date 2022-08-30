/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.adengine.client

import `in`.dailyhunt.money.adContextEvaluatorEngineNative.AdContextEvaluatorEngine
import `in`.dailyhunt.money.adContextEvaluatorEngineNative.AdContextLogger
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.newshunt.adengine.ReadPersistedAdUsecase
import com.newshunt.adengine.RemovePersistedAdUsecase
import com.newshunt.adengine.model.AdReadyHandler
import com.newshunt.adengine.model.entity.BaseAdEntity
import com.newshunt.adengine.model.entity.BaseDisplayAdEntity
import com.newshunt.adengine.model.entity.CurrentAdRequestInfo
import com.newshunt.adengine.model.entity.EmptyAd
import com.newshunt.adengine.model.entity.NativeAdContainer
import com.newshunt.adengine.model.entity.version.AdPosition
import com.newshunt.adengine.model.entity.version.AdRequest
import com.newshunt.adengine.processor.AdProcessorFactory
import com.newshunt.adengine.util.AdFrequencyStats
import com.newshunt.adengine.util.AdLogger
import com.newshunt.adengine.util.AdsUtil
import com.newshunt.adengine.util.SplashAdPersistenceHelper
import com.newshunt.adengine.view.AdEntityConsumer
import com.newshunt.adengine.view.BackupAdsCache
import com.newshunt.common.helper.common.AndroidUtils
import com.newshunt.common.helper.common.Logger
import com.newshunt.dataentity.ads.AdFCType
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.dhutil.model.entity.adupgrade.AdsUpgradeInfo
import com.newshunt.dhutil.trimToSize
import com.newshunt.news.model.usecase.MediatorUsecase
import com.newshunt.sdk.network.Priority
import com.squareup.otto.Bus
import io.reactivex.schedulers.Schedulers
import java.lang.ref.WeakReference
import java.util.Observable
import java.util.Observer
import java.util.Queue
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Implementation of an AdRepository which is responsible for caching the ads and hitting network
 * when necessary.
 *
 * Created by srikanth on 06/10/19.
 */
internal open class AdRepository(private val adBus: Bus,
                                 private var cacheSize: Int,
                                 private var cacheThreshold: Int,
                                 val adPosition: AdPosition,
                                 private val excludedBannerProvider: ExcludedBannerProvider,
                                 private val readPersistedAdUsecase: ReadPersistedAdUsecase,
                                 private val removePersistedAdUsecase: MediatorUsecase<Bundle, Boolean>,
                                 readPersistedAdsOnInit: Boolean = true)
    : AdEntityConsumer, BackupAdsCache, AdReadyHandler {
    protected var uniqueRequestId: Int = 0
    private val logTag = "AdRepository_$adPosition"
    /**
     * All ad responses will be received on this single thread to avoid concurrency issues.
     */
    private val responseExecutor = AndroidUtils.newSingleThreadExecutor(logTag)
    private val adRequestManager = AdRequestManager(adPosition, this, this, responseExecutor)
    private val adQueue = ConcurrentLinkedQueue<BaseAdEntity>()
    private val backupAdQueue = ConcurrentLinkedQueue<BaseAdEntity>()
    private var currentlyServedRequest: CurrentAdRequestInfo? = null
    private var allAdsProcessed = AtomicBoolean(false)

    override val excludeBannerIds: List<String>
        get() = excludedBannerProvider.excludeBannerIds

    override fun consumeNextSet(baseAdEntities: List<BaseAdEntity>?, fromNetwork: Boolean) {
        if (baseAdEntities.isNullOrEmpty()) {
            AdLogger.w(logTag, "consumeNextSet returned 0 ads")
            serveCurrentRequest()
            return
        }

        AdLogger.w(logTag, "consumeNextSet returned ads: ${baseAdEntities.size}")

        // Received concurrent modification exception couple of times.
        synchronized(this) {
            val currentTime = System.currentTimeMillis()
            for (baseAdEntity in baseAdEntities) {
                //Discard invalid ads
                if (adPosition != baseAdEntity.adPosition) {
                    continue
                }

                if (baseAdEntity is EmptyAd && dropEmptyAd(baseAdEntity)) {
                    continue
                }

                AdLogger.d(logTag, "[$adPosition] Sending ad with type = ${baseAdEntity.type} " +
                        "with id= ${baseAdEntity.aduid}")

                val isAdValid = ActionUrlOadestCheck.checkOadestValue(baseAdEntity)
                if (!isAdValid) {
                    AdLogger.e(logTag, "Oadest check failed for click url. ${baseAdEntity.uniqueAdIdentifier}")
                    baseAdEntity.adGroupId?.let { removePersistedAdUsecase.execute(RemovePersistedAdUsecase.bundle(adGroupId = it)) }
                    continue
                }

                if (fromNetwork) {
                    //update impFcap data from this ad, if changed.
                    AdFrequencyStats.updateAndPersistFCDataFrom(baseAdEntity)
                } else if (!baseAdEntity.isEvergreenAd &&
                    baseAdEntity.campaignId?.let { AdsUtil.isFCLimitReachedForAd(baseAdEntity, uniqueRequestId) } == true) {
                   // Evergreen ads are not removed from db on FC exhaustion as they may live beyond the reset time
                    AdLogger.e(logTag, "FC validation failed for ${baseAdEntity.uniqueAdIdentifier}")
                    baseAdEntity.adGroupId?.let {
                        removePersistedAdUsecase.execute(RemovePersistedAdUsecase.bundle(adGroupId = it))
                    }
                    continue
                }

                if (handleForSplashAd(baseAdEntity)) {
                    return
                }
                baseAdEntity.receiveTime = currentTime
                if (baseAdEntity.isBackUpAd) {
                    backupAdQueue.add(baseAdEntity)
                } else {
                    adQueue.add(baseAdEntity)
                }
                excludedBannerProvider.updateExcludeBannerList(baseAdEntity)
            }
            //Check if queues need to be trimmed
            sortAndTrimAdQueue(adQueue, cacheSize)
            sortAndTrimAdQueue(backupAdQueue, cacheSize)
        }
        serveCurrentRequest()
    }

    /**
     * Returns whether processing needs to be stopped, if Splash ad.
     */
    open fun handleForSplashAd(baseAdEntity: BaseAdEntity): Boolean {
        // In case of splash ad response just save it in preferences and do not add to queue, This
        // ad will be shown in next launch
        if (AdPosition.SPLASH == baseAdEntity.adPosition) {
            CommonUtils.runInBackground {
                SplashAdPersistenceHelper.writeObjectToPersistentStore(baseAdEntity as BaseDisplayAdEntity)
            }
            currentlyServedRequest = null
            return true
        } else if (AdPosition.SPLASH_DEFAULT == baseAdEntity.adPosition) {
            //No need to add to cache for this zone
            currentlyServedRequest = null
            return true
        }
        return false
    }

    override fun doneProcessingRequest() {
        AdLogger.d(logTag, "doneProcessingRequest id : $uniqueRequestId")
        currentlyServedRequest ?: return

        currentlyServedRequest?.let {

            //Request served only empty ads or retries exceeded.
            if (it.adRequest.numOfAds == 0 || it.adRequest.retryCount >= RETRY_THRESHOLD) {
                AndroidUtils.getMainThreadHandler().post {
                    it.let {
                        val nativeAdContainer = NativeAdContainer(it.uniqueRequestId, adPosition, null, true)
                        adBus.post(nativeAdContainer)
                    }
                }
                currentlyServedRequest = null
                AdLogger.d(logTag, "no pending request, retry count = ${it.adRequest.retryCount}, numOfAds=${it.adRequest.numOfAds}")
                return
            }

            AdLogger.d(logTag, "Number of ads in queue : ${adQueue.size}")

            val adsTofillCache = cacheSize - adQueue.size
            val adsToRequest = if (it.adRequest.numOfAds > adsTofillCache) it.adRequest.numOfAds else adsTofillCache

            if (adsToRequest > 0 || adQueue.size <= cacheThreshold) {
                AdLogger.d(logTag, "Current cache is below threshold. Need to fetch more, retryCount: ${it.adRequest.retryCount}")
                if (it.nwRequestInitiated) {
                    // Retry count to be incremented only if this adrequest was able to send a n/w
                    // request earlier.
                    it.adRequest.retryCount += 1
                }

                val serverAdRequest = it.adRequest.copy(numOfAds = adsToRequest)
                requestAdsFromServer(serverAdRequest, uniqueRequestId, Priority.PRIORITY_NORMAL)
            } else {
                currentlyServedRequest = null
            }
        }
    }

    override fun getBackupAd(adRequest: AdRequest?): BaseAdEntity? {
        adRequest ?: return null
        if (CommonUtils.isEmpty(backupAdQueue)) {
            return null
        }
        val ads = getServeableAds(adRequest, backupAdQueue)
        return if (ads.isNotEmpty()) {
            ads[0]
        } else null
    }

    fun getAd(adRequest: AdRequest?): BaseAdEntity? {
        adRequest ?: return null
        if (CommonUtils.isEmpty(adQueue)) {
            return null
        }
        val ads = getServeableAds(adRequest, adQueue)
        return if (ads.isNotEmpty()) {
            ads[0]
        } else null
    }

    /**
     * To clear all ads from FC exhausted campaigns.
     */
    fun clearFCExhaustedAds(capId: String, fcType: AdFCType) {
        clearFCExhaustedAds(adQueue, capId, fcType)
        clearFCExhaustedAds(backupAdQueue, capId, fcType)

    }

    private fun clearFCExhaustedAds(queue: Queue<BaseAdEntity>, capId: String, fcType: AdFCType) {
        val iterator = queue.iterator()
        while (iterator.hasNext()) {
            val ad = iterator.next()
            val remove = when (fcType) {
                AdFCType.BANNER -> ad is BaseDisplayAdEntity && ad.bannerId == capId
                AdFCType.CAMPAIGN -> ad.campaignId == capId
            }
            if (remove) {
                iterator.remove()
            }
        }
    }

    /**
     * To clear all ads from inventory safely
     */
    open fun clearInventory(clearOnlyUnAttachedAds: Boolean) {
        clearAdQueue(adQueue, clearOnlyUnAttachedAds)
        clearAdQueue(backupAdQueue, clearOnlyUnAttachedAds)
        allAdsProcessed.set(false)
    }

    /**
     * To clear all persisted ads for an adPosition safely
     */
    fun deletePersistedAds() {
        removePersistedAdUsecase.execute(RemovePersistedAdUsecase.bundle(adPosition = adPosition))
    }

    /**
     * Returns the cached ads immediately and posts on bus, if marked so.
     * TODO(raunak.yadav) : To remove posting cached ads on bus when all zones support this.
     * For now only Masthead does.
     * If no/low cache, will also send out a network request.
     *
     * @return null - if request discarded.
     * NativeAdContainer with available cached ads.
     */
    open fun requestAds(adRequest: AdRequest, uniqueRequestId: Int,
                   priority: Priority,
                   adsUpgradeInfo: AdsUpgradeInfo?,
                   postLocalAdsAsync: Boolean): NativeAdContainer? {
        check(adRequest.zoneType == adPosition) { "AdRequest zone does not match AdInventory zone" }

        AdLogger.w(logTag, "Initiating request for ad of type = $adPosition with id =$uniqueRequestId of count ${adRequest.numOfAds}")

        excludedBannerProvider.updateExcludeBannerListOnContextChange(adRequest)

        adsUpgradeInfo?.maxParallelAdRequestCount?.let {
            adRequestManager.maxParallelRequests = it
        }
        this.uniqueRequestId = uniqueRequestId
        cacheSize = AdsUtil.getSavedCacheCount(adPosition, adsUpgradeInfo)
        cacheThreshold = cacheSize - 1

        //if ad request is for Splash ad, show ad from saved preferences and then continue with
        // request from server
        if (AdPosition.SPLASH == adPosition) {
            CommonUtils.runInBackground {
                getBaseAdEntityFromPersistenceStore(adRequest.copy())
            }
        }
        //no caching required for PP1
        if (AdPosition.PP1 == adPosition){
            adQueue.clear()
        }
        /*  This call updates the final AdUrl with the requiredAdTags field. Currently, only
        Supplement and pp1 zone needs to send that data to the BE.*/
        AdsUtil.updateRequiredAdTagsFromCache(adRequest, cacheSize)
        val adContainer = if (adRequest.skipCacheMatching) NativeAdContainer(uniqueRequestId, adPosition)
        else
            serveLocalAds(adRequest, uniqueRequestId, postLocalAdsAsync)

        var serveAbleAds = 0
        if (!CommonUtils.isEmpty(adContainer.baseAdEntities)) {
            serveAbleAds = adContainer.baseAdEntities?.size?:0
        }
        val remainingAds = adRequest.numOfAds - serveAbleAds
        if (remainingAds > 0 || adQueue.size <= cacheThreshold) {
            //Ads not available and n/w request need to be made
            AdLogger.w(logTag, "Local ads were not enough asking for more...")
            val pendingAdRequest = adRequest.copy(numOfAds = remainingAds)
            currentlyServedRequest = CurrentAdRequestInfo(uniqueRequestId, pendingAdRequest)

            val adsTofillCache = cacheSize - adQueue.size
            val adsToRequest = if (remainingAds > adsTofillCache) remainingAds else adsTofillCache
            AdLogger.d(logTag, "Remaining number of ads = $remainingAds")
            val serverAdRequest = adRequest.copy(numOfAds = adsToRequest)

            requestAdsFromServer(serverAdRequest, uniqueRequestId, priority)
        } else if (remainingAds == 0) {
            AdLogger.w(logTag, "Request Served: currentlyServedRequest making it null remainingAds == 0")
            currentlyServedRequest = null
        }
        return adContainer
    }

    open fun update(observable: Observable?) {
        val baseAdEntity = observable as? BaseAdEntity? ?: return

        if (removeDisplayedAd(baseAdEntity, adQueue)) {
            return
        }
        // Check if ad exists in backupQueue.
        removeDisplayedAd(baseAdEntity, backupAdQueue)
    }

    override fun onReady(baseAdEntity: BaseAdEntity?) {
        baseAdEntity ?: return
        if (AdPosition.SPLASH == baseAdEntity.adPosition) {
            AdLogger.d("Splash Ad", "Send splash Ad")
            val splashAdList = ArrayList<BaseAdEntity>()
            splashAdList.add(baseAdEntity)
            sendAds(splashAdList, uniqueRequestId, true)
        } else {
            AdLogger.d(logTag, "Processed ad with type = " + baseAdEntity.type!!)
            consumeNextSet(listOf(baseAdEntity), false)
        }
    }

    private fun clearAdQueue(queue: Queue<BaseAdEntity>, clearOnlyUnAttachedAds: Boolean) {
        for (adEntity in queue) {
            //If this ad has a parent and we need to clear only unattached ads, skip this ad
            if (!CommonUtils.isEmpty(adEntity.parentIds) && clearOnlyUnAttachedAds) {
                continue
            }

            adEntity.isShown = true
            // only seen ads can be removed from destroyAds
            adEntity.parentIds.clear()
            // do not have any reference to parentId now so clearing parentId
            AdsUtil.destroyAd(adEntity, INVALID_UI_COMPONENT_ID)
        }
        queue.clear()
    }

    /**
     * Get saved splash ad response from persistence store and show.
     */
    private fun getBaseAdEntityFromPersistenceStore(adRequest: AdRequest?) {
        if (adRequest == null) {
            AdLogger.d(logTag, "activity null")
            return
        }
        SplashAdPersistenceHelper.readObjectFromPersistentStore()?.let { splashAd ->
            if (SplashAdPersistenceHelper.isValidSplashAdToServe(splashAd)) {
                val baseAdProcessor = AdProcessorFactory.getAdProcessorToProcessPartially(splashAd, this)
                baseAdProcessor.processAdContent(adRequest)
            }
        }
    }

    protected fun serveLocalAds(adRequest: AdRequest, uniqueRequestId: Int,
                              postOnBus: Boolean): NativeAdContainer {
        val adsToSend = getServeableAds(adRequest, adQueue)
        AdLogger.w(logTag, "Sending number of ads = ${adsToSend.size} to request with id = $uniqueRequestId")
        return sendAds(adsToSend, uniqueRequestId, postOnBus)
    }

    open fun getServeableAds(adRequest: AdRequest, queue: ConcurrentLinkedQueue<BaseAdEntity>):
            List<BaseAdEntity> {
        val uniqueAds = mutableListOf<BaseAdEntity>()
        val defaultCacheTTL = AdsUtil.getCacheTTL(adPosition)

        val iterator = queue.iterator()
        while (iterator.hasNext()) {
            val baseAdEntity = iterator.next()
            // Remove dead ad from the list
            if (baseAdEntity.isShown || handleAdIfExpired(baseAdEntity, defaultCacheTTL)) {
                iterator.remove()
                continue
            }
            var contextKey = adPosition.value
            //Check supplement tag match
            val adTag = baseAdEntity.adTag
            if (!CommonUtils.isEmpty(adRequest.localRequestedAdTags)) {
                if (adRequest.localRequestedAdTags?.contains(adTag) == true) {
                    contextKey = AdsUtil.getAdSlotName(adTag, adPosition)
                } else {
                    continue
                }
            }
            if (baseAdEntity.fcData != null &&
                    baseAdEntity.campaignId?.let { AdsUtil.isFCLimitReachedForAd(baseAdEntity, uniqueRequestId) } == true) {
                AdLogger.e(logTag, "FC validation failed for ${baseAdEntity.uniqueAdIdentifier}")
                iterator.remove()
                continue
            }
            if (!adRequest.skipCacheMatching) {
                //check context match
                val logger: AdContextLogger? = if (Logger.loggerEnabled()) AdContextLogger() else null
                logger?.appendToLog("For Context Key: $contextKey")
                val contextMatch = try {
                    AdContextEvaluatorEngine.evaluate(adRequest.contentContextMap?.get(contextKey),
                            adRequest.parentContextMap?.get(contextKey),
                            baseAdEntity.adContextRules?.ruleGroup, logger)
                } catch (ex: Exception) {
                    Logger.caughtException(ex)
                    ex.printStackTrace()
                    false
                }
                if (!contextMatch) {
                    AdLogger.e(logTag, "Context does not match for ${baseAdEntity.uniqueAdIdentifier}")
                    AdLogger.v(logTag, "Context mismatch reason : ${logger?.log}")
                    continue
                }
                AdLogger.v(logTag, logger?.log)
                AdLogger.d(logTag, "Context matched for ${baseAdEntity.uniqueAdIdentifier}")
            }
            adTag?.let { tag ->
                removeAdTag(adRequest, tag)
            }

            uniqueAds.add(baseAdEntity)
            if (uniqueAds.size == adRequest.numOfAds) {
                break
            }
        }
        //remove all negative or zero values.
        removeInvalidTags(adRequest.requiredAdtags)

        return uniqueAds
    }

    @Synchronized
    private fun removeAdTag(adRequest: AdRequest, tag: String) {
        adRequest.localRequestedAdTags?.remove(tag)
        adRequest.requiredAdtags?.let {
            it.put(tag, (it[tag] ?: 0) - 1)
        }
    }

    private fun removeInvalidTags(tagMap: ConcurrentHashMap<String, Int>?) {
        tagMap?.let {
            val mapIterator = it.entries.iterator()
            while (mapIterator.hasNext()) {
                if (mapIterator.next().value <= 0) {
                    mapIterator.remove()
                }
            }
        }
    }

    /**
     * @param adRequest - This adrequest may have different numOfAds then adRequest sent from
     * view as it accounts for cache refill too.
     * e.g. View requested for 1 ad but cacheSize was 3, so 3 ads will be finally requested. Do not
     * overwrite 'currentlyServedRequest.adRequest' with this adRequest.
     *
     */
    private fun requestAdsFromServer(adRequest: AdRequest?, uniqueRequestId: Int, priority: Priority) {
        adRequest ?: return

        // Ad server is down. Don't request more
        if (adRequestManager.isServerDown) {
            AdLogger.d(logTag, "Server temporarily down. Aborting request with id : $uniqueRequestId")
            currentlyServedRequest = null
            val handler = Handler(Looper.getMainLooper())
            handler.post {
                val nativeAdContainer = NativeAdContainer(uniqueRequestId, adPosition, null, true)
                adBus.post(nativeAdContainer)
            }
            return
        }
        val success = adRequestManager.triggerNwFetch(adRequest, uniqueRequestId, priority)
        currentlyServedRequest?.nwRequestInitiated = success
    }

    open fun sendAds(baseAdEntities: List<BaseAdEntity>?,
                     uniqueRequestId: Int, postOnBus: Boolean): NativeAdContainer {
        val container = NativeAdContainer(uniqueRequestId, adPosition)

        if (baseAdEntities.isNullOrEmpty()) {
            return container
        }
        baseAdEntities.forEach {
            it.addObserver(AdObserver(this))
        }
        container.baseAdEntities = baseAdEntities
        if (postOnBus) {
            AndroidUtils.getMainThreadHandler().post { adBus.post(container) }
        }
        return container
    }

    private fun serveCurrentRequest() {
        if (currentlyServedRequest == null) {
            AdLogger.d(logTag, "currently served request is null")
            return
        }
        currentlyServedRequest?.let {

            // Check if adRequest has no pending ad left because of empty ad send null to server
            if (it.adRequest.numOfAds == 0) {
                return
            }
            val adContainer = serveLocalAds(it.adRequest, it.uniqueRequestId, false)
            var serveableAds = 0
            if (!CommonUtils.isEmpty(adContainer.baseAdEntities)) {
                serveableAds = adContainer.baseAdEntities?.size ?: 0
            }
            if (it.adRequest.numOfAds - serveableAds > 0) {
                it.adRequest.numOfAds -= serveableAds
            } else {
                AdLogger.d(logTag, "Done processing: $uniqueRequestId")
                currentlyServedRequest = null
                adContainer.doneRequestProcessing = true
            }
            AndroidUtils.getMainThreadHandler().post { adBus.post(adContainer) }
        }
    }

    private fun removeDisplayedAd(adEntity: BaseAdEntity, adQueue: Queue<BaseAdEntity>): Boolean {
        AdLogger.v(logTag, "Trying to remove ad from sentAds ${adEntity.uniqueAdIdentifier}")
        adEntity.adGroupId?.let {
            removePersistedAdUsecase.execute(RemovePersistedAdUsecase.bundle(adGroupId = it))
        }
        if (CommonUtils.isEmpty(adQueue)) {
            return false
        }
        val success = adQueue.remove(adEntity)
        if (success) {
            AdLogger.d(logTag, "Removing viewed ad : ${adEntity.uniqueAdIdentifier}")
        }
        return success
    }

    /**
     * Purges the expired and least priority ads if adQueue grows to larger than given size.
     * Sorts remaining ads as per selctionPriority.
     */
    private fun sortAndTrimAdQueue(queue: Queue<BaseAdEntity>, size: Int) {
        if (size <= 0) {
            queue.clear()
            return
        }
        val defaultTTL = AdsUtil.getCacheTTL(adPosition)
        val ads = mutableListOf<BaseAdEntity>()
        queue.forEach {
            // mark and skip expired ads.
            if (handleAdIfExpired(it, defaultTTL)) {
                return@forEach
            }
            ads.add(it)
        }
        ads.sortWith(Comparator { ad1, ad2 ->
            val adPriority1 = ad1.adContextRules?.selectionPriority ?: 0
            val adPriority2 = ad2.adContextRules?.selectionPriority ?: 0
            if (adPriority1 == adPriority2) {
                //Newer ad to have higher precedence over old ad of same priority
                (ad2.receiveTime - ad1.receiveTime).toInt()
            } else {
                adPriority2 - adPriority1
            }
        })
        if (size <= 0) {
            queue.clear()
            return
        }
        queue.clear()
        queue.addAll(ads.trimToSize(size))
    }

    private fun handleAdIfExpired(baseAdEntity: BaseAdEntity, defaultTTL: Long): Boolean {
        val ttl = baseAdEntity.adContextRules?.ttl ?: defaultTTL
        if (ttl <= 0) {
            return false
        }
        val adReceiveTime = baseAdEntity.receiveTime
        if (adReceiveTime > 0L && CommonUtils.isTimeExpired(adReceiveTime, ttl * 1000)) {
            baseAdEntity.isShown = true
            baseAdEntity.parentIds.clear()
            AdsUtil.destroyAd(baseAdEntity, INVALID_UI_COMPONENT_ID)
            AdLogger.e(logTag, "Ad expired ${baseAdEntity.adPosition}, ${baseAdEntity.aduid}.")
            baseAdEntity.adGroupId?.let { removePersistedAdUsecase.execute(RemovePersistedAdUsecase.bundle(adGroupId = it)) }
            return true
        }
        return false
    }

    private fun dropEmptyAd(baseAdEntity: BaseAdEntity): Boolean {
        if (baseAdEntity !is EmptyAd) return false
        if (!AdsUtil.zonesWhichDropEmptyAds.contains(baseAdEntity.adPosition)) {
            return false
        }
        AdLogger.w(logTag, "found empty ad: ${baseAdEntity.aduid}")
        // Need to decrease pending ad count even if ad is empty so that we can serve subsequent requests.
        currentlyServedRequest?.adRequest?.let {
            it.numOfAds -= 1
            baseAdEntity.adTag?.let { tag ->
                removeAdTag(it, tag)
                removeInvalidTags(it.requiredAdtags)
            }
        }
        if (baseAdEntity.adPosition != AdPosition.SPLASH_DEFAULT) {
            val asyncImpressionReporter = AsyncAdImpressionReporter(baseAdEntity)
            if (baseAdEntity.isEvergreenAd) {
                asyncImpressionReporter.hitTrackerUrl(baseAdEntity.requestUrl)
            }
            baseAdEntity.beaconUrl?.let {
               asyncImpressionReporter.onCardView()
            }
        }
        return true
    }

    private val readInProgress = AtomicBoolean(false)

    fun readAndProcessPersistedAds(count: Int? = null) {
        if (allAdsProcessed.get() || readInProgress.get()) return

        readInProgress.set(true)
        val disposable = readPersistedAdUsecase.invoke(adPosition)
            .observeOn(Schedulers.io())
            .subscribeOn(Schedulers.io())
            .subscribe({ adGroups ->
                AdLogger.d(logTag, "Persisted ads fetched $adPosition : ${adGroups.size}")

                if (adGroups.isEmpty()) {
                    readInProgress.set(false)
                    allAdsProcessed.set(true)
                    return@subscribe
                }
                //Get ads that are not yet processed.
                val processedAdIds = adQueue.map { it.adGroupId }
                val toProcessAdGroups = adGroups.mapNotNull {
                    if (processedAdIds.contains(it.adGroupId)) {
                        null
                    } else it
                }
                if (toProcessAdGroups.isNullOrEmpty()) {
                    readInProgress.set(false)
                    allAdsProcessed.set(true)
                    return@subscribe
                }

                //number of ads to be processed.
                val toProcess = count?.let { Math.min(adGroups.size, count) } ?: toProcessAdGroups.size
                for (i in 0 until toProcess) {
                    val processor = AdProcessorFactory.getAdProcessor(toProcessAdGroups[i], this,
                        this, responseExecutor, isPersistedAd = true)
                    processor.processAdContent(AdRequest(adPosition))
                    AdLogger.v(logTag, "Persisted ads processed : $i")
                }
                //Flag to mark that all ads are now read, processed and added to queue.
                if (toProcess == toProcessAdGroups.size) {
                    allAdsProcessed.set(true)
                }
                readInProgress.set(false)
            }, {
                AdLogger.d(logTag, "Persisted ads fetch failed: ${it.message}")
                readInProgress.set(false)
            })
    }

    init {
        if (readPersistedAdsOnInit) {
            readAndProcessPersistedAds()
        }
    }
}

private const val RETRY_THRESHOLD = 1
private const val INVALID_UI_COMPONENT_ID = -999

internal class AdObserver(repo: AdRepository) : Observer {
    private val weakRepository = WeakReference(repo)

    override fun update(observable: Observable?, obj: Any?) {
        val repository = weakRepository.get() ?: return
        repository.update(observable)
    }
}

interface ExcludedBannerProvider {
    val excludeBannerIds: List<String>
    fun updateExcludeBannerList(adEntity: BaseAdEntity)
    fun updateExcludeBannerListOnContextChange(request: AdRequest)
}
