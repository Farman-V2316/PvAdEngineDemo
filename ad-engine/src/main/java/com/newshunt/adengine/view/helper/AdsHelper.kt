/*
* Copyright (c) 2019 Newshunt. All rights reserved.
*/
package com.newshunt.adengine.view.helper

import `in`.dailyhunt.money.contentContext.ContentContext
import android.app.Activity
import android.os.Bundle
import androidx.lifecycle.LifecycleOwner
import com.newshunt.adengine.ClearAdsDataUsecase
import com.newshunt.adengine.FetchAdSpecUsecase
import com.newshunt.adengine.InsertAdInfoUsecase
import com.newshunt.adengine.ReplaceAdInfoUsecase
import com.newshunt.adengine.domain.controller.GetAdUsecaseController
import com.newshunt.adengine.model.entity.AdFCLimitReachedEvent
import com.newshunt.adengine.model.entity.AdViewedEvent
import com.newshunt.adengine.model.entity.BaseAdEntity
import com.newshunt.adengine.model.entity.NativeAdContainer
import com.newshunt.adengine.model.entity.NoFillOrErrorAd
import com.newshunt.adengine.model.entity.version.AdPosition
import com.newshunt.adengine.model.entity.version.AdRequest
import com.newshunt.adengine.model.entity.version.AmazonSdkPayload
import com.newshunt.adengine.util.AdFrequencyStats
import com.newshunt.adengine.util.AdLogger
import com.newshunt.adengine.util.AdsUtil
import com.newshunt.common.helper.common.BusProvider
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.analytics.section.NhAnalyticsEventSection
import com.newshunt.dataentity.common.asset.CommonAsset
import com.newshunt.dataentity.common.asset.Format
import com.newshunt.dataentity.common.pages.PageEntity
import com.newshunt.dataentity.common.pages.PageSection
import com.newshunt.dataentity.dhutil.model.entity.adupgrade.AdsUpgradeInfo
import com.newshunt.dataentity.dhutil.model.entity.upgrade.LangInfo
import com.newshunt.dataentity.social.entity.AdInsertFailReason
import com.newshunt.dataentity.social.entity.AdInsertResult
import com.newshunt.dataentity.social.entity.AdSpec
import com.newshunt.dhutil.helper.AdsUpgradeInfoProvider
import com.newshunt.dhutil.helper.preference.AdsPreference
import com.newshunt.dhutil.helper.preference.AppStatePreference
import com.newshunt.helper.TickerAvailability
import com.newshunt.news.model.usecase.MediatorUsecase
import com.newshunt.news.model.usecase.NLResp
import com.newshunt.news.model.usecase.toMediator2
import com.squareup.otto.Subscribe
import java.util.Observable
import java.util.Observer
import javax.inject.Inject
import javax.inject.Named

/**
 * Requests and manages ads to be shown in List.
 * @author raunak.yadav
 */
//todo mukesh need to handle states for request and processing to make code predictable
class AdsHelper(private val adDbHelper: AdDBHelper,
                private val requestAds: Boolean,
                private val entityId: String,
                private val sourceId: String?,
                private val sourceType: String?,
                private val pageEntity: PageEntity?,
                private val uniqueRequestId: Int,
                private val section: String?,
                private val referrer: PageReferrer?,
                lifecycleOwner: LifecycleOwner,
                private val insertAdInfoUsecase: MediatorUsecase<Bundle, AdInsertResult>,
                private val clearAdsDataUsecase: MediatorUsecase<Bundle, Unit>,
                private val fetchAdSpecUsecase: MediatorUsecase<List<String?>, Map<String, AdSpec>>,
                private val replaceAdInfoUsecase: MediatorUsecase<Bundle, Long>) {

    class Factory @Inject constructor(@Named("adDbHelper") private val adDbHelper: AdDBHelper,
                                      @Named("supportAds") private val requestAds: Boolean,
                                      @Named("entityId") private val entityId: String,
                                      @Named("sourceId") private val sourceId: String?,
                                      @Named("sourceType") private val sourceType: String?,
                                      @Named("pageEntity") private val pageEntity: PageEntity?,
                                      @Named("section") private val section: String?,
                                      private val lifecycleOwner: LifecycleOwner,
                                      private val insertAdUsecase: InsertAdInfoUsecase,
                                      private val clearAdsUsecase: ClearAdsDataUsecase,
                                      private val fetchAdSpecUsecase: FetchAdSpecUsecase,
                                      private val replaceAdUsecase: ReplaceAdInfoUsecase) {

        fun create(uniqueRequestId: Int, referrer: PageReferrer?): AdsHelper {
            return AdsHelper(adDbHelper, requestAds, entityId, sourceId, sourceType,
                    pageEntity, uniqueRequestId, section, referrer, lifecycleOwner,
                    insertAdUsecase.toMediator2(), clearAdsUsecase.toMediator2(),
                    fetchAdSpecUsecase, replaceAdUsecase.toMediator2())
        }
    }

    private val uiBus = BusProvider.getUIBusInstance()
    private val zonesSupported = setOf(AdPosition.P0.value, AdPosition.PP1.value, AdPosition.CARD_P1.value)
    private var allowedZones = mutableSetOf<String>()

    /**
     * this holds pp1 ads
     * */
    private val localPP1Ads = mutableMapOf<String, BaseAdEntity>()
    val insertedPP1IdList = mutableSetOf<String>()

    /*
    * key value pair to keep tag order with status, if ad processed for particular tag mark it true else false
    * */
    private val pp1TagOrders = LinkedHashMap<String, Boolean>()
    private var isPP1ResponseAwaited = true

    //use to delete Observer
    private val refillPP1Ads = mutableSetOf<BaseAdEntity>()
    private var uniqueAdIdentifierTriedLast: String? = null
    private var usedPrevPostId: String? = null

    private var availableAds: MutableList<BaseAdEntity> = ArrayList()
    private var replacedAds: MutableList<BaseAdEntity>? = null
    private var cardP0ResponseAwaited: Boolean = false
    private var cardP1ResponseAwaited: Boolean = false
    private var detailPrefetchRequestSent: Boolean = false
    private var waitForNextAdRequest: Boolean = false
    private var isP0AdInserted: Boolean = false
    var isPP1AdsInserted = false
    private var isP1AdInserted: Boolean = false
    //this id is used to track if particular  pp1 is being inserted in DB, failed to insert(then retry).
    private var processingTag : String? = null
    private var currentAdData: Pair<Int, BaseAdEntity?> = INVALID_AD_DATA
    private var prevAdData: Pair<Int, BaseAdEntity?> = INVALID_AD_DATA
    private var currentRequestPosition: Int = 0
    private var getAdUsecase: GetAdUsecaseController = GetAdUsecaseController(uiBus, uniqueRequestId)
    private val adSpecLiveData = fetchAdSpecUsecase.data()
    private val adInsertLiveData = insertAdInfoUsecase.data()

    private var destroyed = false

    // this holds p0 ad
    private var premiumBaseAdEntity: BaseAdEntity? = null
    private var tickerAvailability: TickerAvailability? = TickerAvailability.UNKNOWN
    private var busRegistered: Boolean = false
    private var p1AdRequestAlreadyMadeInThisSession = false
    private var cardP1RetryDistance: Int = 0
    private var adsUpgradeInfo: AdsUpgradeInfo? = null
    private var unseenAdIds: MutableSet<String> = HashSet()
    private var adSpec: AdSpec? = null
    private var processingAdId : String? = null
    private val isHome: Boolean = entityId == PreferenceManager.getPreference(AppStatePreference.ID_OF_FORYOU_PAGE,
            Constants.EMPTY_STRING)

    /**
     * Required to re-inflate all seen ads when list updates from DB.
     */
    private var allAds = ArrayList<String>()
    private var nextAdInsertPosition = -1
    private var refillAdRequstMade: Boolean = false

    private val refillAdObserver = object : Observer {
        override fun update(o: Observable, arg: Any?) {
            if (o is BaseAdEntity && o.isShown) {
                o.deleteObserver(this)
                if (destroyed) {
                    return
                }
                o.adPosition?.let {
                    refillAd(it)
                }
            }
        }
    }

    init {
        adSpecLiveData.observe(lifecycleOwner, androidx.lifecycle.Observer {
            AdLogger.d(LOG_TAG, "Adspec received for id : $entityId")
            updateAdSpecData(it.getOrNull()?.get(entityId))
            if (adDbHelper.isViewVisible()) {
                requestP0Ad()
                requestPP1Ad()
            }
        })
        adInsertLiveData.observe(lifecycleOwner, androidx.lifecycle.Observer {
            if (it.isSuccess) {
                it.getOrNull()?.let { res ->
                    if (res.isInserted) {
                        if (uniqueAdIdentifierTriedLast == res.uniqueAdIdentifier) {
                            processingTag?.let {
                                AdLogger.d(LOG_TAG, "processed tag ${it}")
                                markPP1TagProcessed(it)
                            }
                            processingTag = null
                            usedPrevPostId = res.prevPostId
                        }
                    } else {
                        //Ad insertion failed
                        AdLogger.e(LOG_TAG, "Failed to insert ad in DB : ${res.uniqueAdIdentifier} " +
                                "Reason : ${res.failReason}")
                        unseenAdIds.remove(res.uniqueAdIdentifier)
                        val ad = AdBinderRepo.getAdById(res.uniqueAdIdentifier)
                        if (res.failReason != AdInsertFailReason.NONE) {
                            val map = hashMapOf(
                                    "error_type" to "Ad Insertion failed",
                                    "error_reason" to res.failReason.name,
                                    "adPosition" to ad?.adPosition?.value,
                                    "adIndex" to currentAdData.toString(),
                                    "adId" to res.uniqueAdIdentifier
                            )
                            //TODO: PANDA removed
//                            AnalyticsClient.logDynamic(NhAnalyticsDevEvent.DEV_CUSTOM_ERROR,
//                                    NhAnalyticsEventSection.APP, null, map, false)
                        }

                        when (ad?.adPosition) {
                            AdPosition.P0 -> {
                                isP0AdInserted = false
                                prevAdData = INVALID_AD_DATA
                                currentAdData = INVALID_AD_DATA
                            }
                            AdPosition.PP1 -> {
                                processingTag = null
                                currentAdData = prevAdData
                            }
                            AdPosition.CARD_P1 -> {
                                currentAdData = prevAdData
                            }
                            else -> {
                            }
                        }
                    }
                } ?: run {
                    handleDBFailure()
                }
            } else {
                handleDBFailure()
            }
        })
    }

    private fun handleDBFailure() {
        processingTag?.let {
            currentAdData = prevAdData
            processingTag = null
        }
    }

    fun start() {
        if (busRegistered) {
            return
        }
        AdLogger.d(LOG_TAG, "Adshelper Start : $uniqueRequestId")
        uiBus.register(this)
        busRegistered = true
        cardP0ResponseAwaited = false
        cardP1ResponseAwaited = false
        isPP1ResponseAwaited = false

        initPP1TagOrder()
        fetchAdSpecFromDb()

        adsUpgradeInfo = AdsUpgradeInfoProvider.getInstance().adsUpgradeInfo
        cardP1RetryDistance = PreferenceManager.getPreference(AdsPreference.CARD_P1_NO_FILL_RETRY_DISTANCE,
                DEFAULT_CARD_SWIPE_WAIT_COUNT)
        if (cardP1RetryDistance <= 0) {
            cardP1RetryDistance = DEFAULT_CARD_SWIPE_WAIT_COUNT
        }
    }

    private fun initPP1TagOrder() {
        AdsUpgradeInfoProvider.getInstance().adsUpgradeInfo?.cardPP1AdsConfig?.tagOrder?.let { tagOrder ->
            if (tagOrder.isNotEmpty()) {
                tagOrder.forEach {
                    pp1TagOrders[it] = false
                }
            }
        }
    }

    fun stop() {
        if (busRegistered) {
            busRegistered = false
            uiBus.unregister(this)
        }
        getAdUsecase.destroy()
    }

    private fun resetPrevAdPosition() {
        prevAdData = INVALID_AD_DATA
        currentAdData = INVALID_AD_DATA

        // reset currentRequestPosition So that we can make new ad requests from the top of the list.
        currentRequestPosition = 0
        nextAdInsertPosition = -1

    }

    private fun purgeAdsFromDb() {
        clearAdsDataUsecase.execute(Bundle())
    }

    fun removeAdFromDb(adId: String?, reported: Boolean = false) {
        adId ?: return
        clearAdsDataUsecase.execute(ClearAdsDataUsecase.bundle(adId, reported))
    }

    private fun fetchAdSpecFromDb() {
        fetchAdSpecUsecase.execute(listOf(entityId))
    }

    /**
     * When list has new FP data from server :
     *  - Update Ticker availability
     *  - Reset state
     */
    fun onFPResponse(fpData: NLResp, visible: Boolean) {
        AdLogger.d(LOG_TAG, "onFPResponse $uniqueRequestId, ${fpData.isFromNetwork}")
        if (fpData.rows.isEmpty()) {
            return
        }
        reset()
        setTickerAvailability(fpData.rows)
        if (visible) {
            fpData.adSpec?.let {
                updateAdSpecData(it)
            }
            requestP0Ad()
            requestPP1Ad()
        }
    }

    private fun updateAdSpecData(adSpec: AdSpec?) {
        adSpec ?: return
        this.adSpec = adSpec
        allowedZones.addAll(zonesSupported)
        AdLogger.v(LOG_TAG, "updateAdSpecData id : $entityId, AdSpec : $adSpec")
        val pp1Slots = mutableListOf<String>()
        if (localPP1Ads.isEmpty()) {
            AdsUpgradeInfoProvider.getInstance().adsUpgradeInfo?.cardPP1AdsConfig?.tagOrder?.let { tagOrder ->
                if (tagOrder.isNotEmpty()) {
                    pp1Slots.addAll(tagOrder)
                }
            }
            AdsUtil.filterBlockedZones(adSpec, allowedZones, entityId, LOG_TAG, pp1Slots)
            pp1Slots.forEach {
                pp1TagOrders[it] = false
            }
            if (pp1TagOrders.isEmpty()) {
                allowedZones.remove(AdPosition.PP1.value)
            }
        }
    }

    fun setTickerAvailability(data: List<Any?>?) {
        if (data.isNullOrEmpty() || tickerAvailability != TickerAvailability.UNKNOWN) {
            return
        }
        data.forEach {
            if (it is CommonAsset && it.i_format() == Format.TICKER) {
                tickerAvailability = TickerAvailability.AVAILABLE
                return
            }
        }
        tickerAvailability = TickerAvailability.UNAVAILABLE
    }

    private fun canRequestAd(lastVisibleItem: Int): Boolean {
        return !isPP1ResponseAwaited && !cardP1ResponseAwaited &&
                (!waitForNextAdRequest && currentAdData.first <= lastVisibleItem ||
                        currentRequestPosition + cardP1RetryDistance < lastVisibleItem)
    }

    private fun requestP0Ad() {
        if (!allowedZones.contains(AdPosition.P0.value) || premiumBaseAdEntity != null ||
                cardP0ResponseAwaited) {
            return
        }

        if (requestAds && requestAds(AdPosition.P0)) {
            AdLogger.d(LOG_TAG, "P0 ad request made")
            cardP0ResponseAwaited = true
        }
    }

    private fun requestPP1Ad() {
        if (!allowedZones.contains(AdPosition.PP1.value) || isPP1ResponseAwaited || localPP1Ads.isNotEmpty() || pp1TagOrders.isEmpty()) {
            return
        }
        if (requestAds && requestAds(AdPosition.PP1)) {
            isPP1ResponseAwaited = true
            AdLogger.d(LOG_TAG, "PP1 ad request made")
        }
    }

    private fun requestAds(adPosition: AdPosition): Boolean {
        val adRequest = getAdRequest(adPosition)
        getAdUsecase.requestAds(adRequest)
        return true
    }

    private fun getAdRequest(adPosition: AdPosition): AdRequest {
        val contextMap = mutableMapOf<String, ContentContext>()
        var numOfAds = 1
        // add contentContext per pp1 tag/slot
        if (AdPosition.PP1 == adPosition) {
            pp1TagOrders.forEach { tag ->
                val key = AdsUtil.getAdSlotName(tag.key, adPosition)
                AdsUtil.getContentContextFor(adSpec, key)?.let {
                    contextMap[key] = it
                }
            }
            numOfAds = pp1TagOrders.size
        } else {
            AdsUtil.getContentContextFor(adSpec, adPosition.value)?.let {
                contextMap[adPosition.value] = it
            }
        }

        val requestBodyList = AdsUtil.getAmazonRequestBody(adPosition)
        AdsUtil.makeAmazonAdRequest(adPosition)

        return AdRequest(
            adPosition,
            numOfAds = numOfAds,
            entityId = entityId,
            entityType = pageEntity?.entityType,
            entitySubType = pageEntity?.subType,
            sourceId = sourceId,
            //if sourceId is present, entity is a sourceCat
            sourceCatId = if (sourceId.isNullOrBlank()) null else entityId,
            sourceType = sourceType,
            contentContextMap = contextMap,
            localRequestedAdTags = if (adPosition == AdPosition.PP1) ArrayList(pp1TagOrders.keys) else null,
            pageReferrer = referrer,
            referrerId = referrer?.id,
            isHome = isHome,
            section = section,
            skipCacheMatching = adPosition == AdPosition.PP1,
            amazonSdkPayload = AmazonSdkPayload(requestBody = requestBodyList)
        )
    }

    /**
     * Request backupAd for an adPosition.
     * Call should return immediately as we are fetching any existing ad only.
     *
     * @param adPosition adPosition
     * @return backup ad, if available
     */

    fun requestBackupAd(adPosition: AdPosition): BaseAdEntity? {
        if (destroyed) {
            return null
        }
        AdLogger.d(LOG_TAG, "Requesting backup ad for : $adPosition")
        return getAdUsecase.getInventoryInstance(adPosition)?.getBackupAd(getAdRequest(adPosition))
    }

    /**
     * Insert P0 ad in appropriate position after confirming availability of ticker information
     * and presence of ticker.
     * If a P1 ad is inserted already due to previously empty P0 ad, do not insert P0 ad until list
     * is refreshed.
     */
    fun insertP0AdinList(totalItemCount: Int?, refill : Boolean = true) {
        if (totalItemCount == null || totalItemCount <= 0) {
            // adapter diffing or no data yet.
            return
        }
        // Avoid duplicate, Avoid empty ad and delay till ticker availability is confirmed
        if (isP0AdInserted) {
            return
        }
        if (TickerAvailability.UNKNOWN == tickerAvailability) {
            AdLogger.d(LOG_TAG, "Can't insert P0 : Ticker availability Unknown.")
            return
        }
        if (isPP1AdsInserted || isP1AdInserted) {
            AdLogger.d(LOG_TAG, "Can't insert P0 : P1/PP1 Ad inserted.")
            return
        }

        if (premiumBaseAdEntity == null || premiumBaseAdEntity is NoFillOrErrorAd) {
            AdLogger.d(LOG_TAG, "Can't insert P0 : Ad not available")
            return
        }

        /* Ad insertion steps :
           1. Insert in adapter
           2. Insert in DB
           #1 and #2 are executed one after the other. If #1 succeeds and ad is viewed but #2 fails,
         isP0AdInserted flag is reset.
           After this state, P0 ad insertion keeps failing as ad is already shown (even though for split-second).
           Better to allow insertion of same ad, then request a new one as that might trigger many P0 ad requests.
         - This issue will not occur when insertion failed due to new FPResponse as reset will be called.

        if (premiumBaseAdEntity!!.isShown) {
            AdLogger.d(LOG_TAG, "Can't insert P0 : Already viewed ad.")
            return
        }*/

        var adPosition = if (TickerAvailability.AVAILABLE == tickerAvailability) {
            AdsUtil.getIntValue(premiumBaseAdEntity?.positionWithTicker, DEFAULT_P0_AD_POSITION_WITH_TICKER)
        } else {
            AdsUtil.getIntValue(premiumBaseAdEntity?.cardPosition, DEFAULT_P0_AD_POSITION)
        }

        if (adPosition < 0) {
            adPosition = 0
        }

        if (adPosition <= totalItemCount) {
            val success = tryInsertAd(premiumBaseAdEntity, adDbHelper.getItemIdBeforeIndex(adPosition),
                    adPosition) { premiumBaseAdEntity = null }
            if (success) {
                if (refill) {
                    premiumBaseAdEntity?.addObserver(refillAdObserver)
                }
                AdLogger.d(LOG_TAG, "P0 ad inserted for position : $adPosition")
                isP0AdInserted = true
                prevAdData = currentAdData
                currentAdData = Pair(adPosition, premiumBaseAdEntity)
            }
        }
    }

    /**
     * Do not insert PP1 ads if P0 is not processed
     * */
    fun tryinsertPP1Ads(totalItemCount: Int?) {
        // pp1 ads are already inserted
        if (isPP1AdsInserted) {
            return
        }
        if (totalItemCount == null || totalItemCount <= 0) {
            AdLogger.d(LOG_TAG, "adapter diffing or no data yet")
            return
        }
        if (!allowedZones.contains(AdPosition.PP1.value) || cardP0ResponseAwaited ||
                !isP0AdInserted && premiumBaseAdEntity != null && premiumBaseAdEntity !is NoFillOrErrorAd) {
            AdLogger.d(LOG_TAG, "Can't insert PP1. P0 Insertion: awaited-$cardP0ResponseAwaited, " +
                    "inserted :$isP0AdInserted")
            return
        }
        if (isP1AdInserted) {
            pp1TagOrders.clear()
            AdLogger.d(LOG_TAG, "Can't insert PP1 now : P1 is already inserted")
            return
        }
        loop@ for (tag in pp1TagOrders.keys) {

            if (pp1TagOrders[tag] == true) {
                AdLogger.v(LOG_TAG, "Continuing as $tag tag has been already processed/inserted")
                continue@loop
            }

            if (processingTag != null) {
                AdLogger.d(LOG_TAG, "waiting for last ad to get insert in DB")
                break@loop
            }
            /*
            * This check to make sure ads distancing is correct in the list.
            * As adInsertLiveData get DB callback tha ad gets inserted immediately,
            * but there is delay in getting DB callback to the list (mediatorCardsLiveData).
            * */
            if (processingAdId != null && !insertedPP1IdList.contains(processingAdId)) {
                AdLogger.d(LOG_TAG, "Can't next ad : prev ads is not yet inserted in list")
                return
            }

            /**
             * Insert PP1 tags in order given in handshake
             * read more https://docs.google.com/document/d/1hmnU58te0M4fT-X5dm4fZu048C8UmCyPRZwXkxhKY9Q/edit?ts=5ee11451#heading=h.viktgpd1zv5u
             */
            val pp1Ad = localPP1Ads[tag]
            if (pp1Ad == null && isPP1ResponseAwaited) {
                break@loop
            } else if (pp1Ad == null || pp1Ad is NoFillOrErrorAd || pp1Ad.isShown) {
                AdLogger.d(LOG_TAG, "Can't insert PP1 : $pp1Ad ad.")
                markPP1TagProcessed(tag)
                continue@loop
            }
            val adPosition = getAdPosition(pp1Ad)
            if (adPosition <= totalItemCount) {
                processingTag = tag
                val success = tryInsertAd(pp1Ad, adDbHelper.getItemIdBeforeIndex(adPosition), adPosition) {
                    localPP1Ads.remove(tag)
                    markPP1TagProcessed(tag)
                }
                if (success) {
                    nextAdInsertPosition = -1
                    prevAdData = currentAdData
                    currentAdData = Pair(adPosition, pp1Ad)
                    AdLogger.d(LOG_TAG, "PP1 ad inserted for position : $adPosition with tag ${pp1Ad.adTag}")
                } else {
                    processingTag = null
                }
                break@loop
            }
        }
    }

    fun getProcessingAdId() : String?{
        return processingAdId
    }

    /**
     * Do not insert P1 ad if a PP1 ad request is in progress or PP1 ads are ready but not
     * inserted yet.Returns -1 if ad cannot be inserted or not available.
     */
    fun tryinsertP1Ad(visibleItemCount: Int, firstVisibleItem: Int, totalItemCount: Int) {
        if (!allowedZones.contains(AdPosition.CARD_P1.value)
                || cardP0ResponseAwaited || !isP0AdInserted && premiumBaseAdEntity != null && premiumBaseAdEntity !is NoFillOrErrorAd
                || isPP1ResponseAwaited || !isPP1AdsInserted) {
            return
        }
        if (availableAds.isNotEmpty()) {
            // Remove displayed ads
            val iterator = availableAds.iterator()
            while (iterator.hasNext()) {
                val ad = iterator.next()
                if (ad.isShown) {
                    iterator.remove()
                    unseenAdIds.remove(ad.uniqueAdIdentifier)
                }
            }
        }

        var lastVisibleItem = firstVisibleItem + visibleItemCount - 1
        AdLogger.d(LOG_TAG, "first: $firstVisibleItem, last: $lastVisibleItem, visible: $visibleItemCount")
        if (availableAds.isEmpty()) {
            if (canRequestAd(lastVisibleItem)) {
                if (requestAds && requestAds(AdPosition.CARD_P1)) {
                    cardP1ResponseAwaited = true
                    AdLogger.d(LOG_TAG, "Card P1 ad request made")
                    p1AdRequestAlreadyMadeInThisSession = true
                    currentRequestPosition = lastVisibleItem
                }
            }
            nextAdInsertPosition = -1
            return
        }

        var retryInsertAd = false
        val baseAdEntity = availableAds[0]
        if (unseenAdIds.contains(baseAdEntity.uniqueAdIdentifier)) {
            val adPos = if (nextAdInsertPosition == -1) currentAdData.first else nextAdInsertPosition
            if (firstVisibleItem > adPos + AD_INSERT_RETRY_BUFFER) {
                AdLogger.e(LOG_TAG, "No chance for (${baseAdEntity.uniqueAdIdentifier})." +
                        "currentAd : $adPos, firstItem : $firstVisibleItem.  Will " +
                        "remove and re-insert")
                removeAdFromDb(baseAdEntity.uniqueAdIdentifier)
                retryInsertAd = true
            } else {
                AdLogger.d(LOG_TAG, "Not trying to insert ad again. Previous ad not shown yet.")
                return
            }
        }
        val adPosition = if (retryInsertAd) lastVisibleItem + AD_INSERT_BUFFER else getAdPosition(baseAdEntity)
        // In case if we have footer visible and items not loaded yet, last visible item
        // will be more than total items in the memory
        if (lastVisibleItem >= totalItemCount) {
            lastVisibleItem = totalItemCount - 1
        }

        if (adPosition > lastVisibleItem && adPosition <= lastVisibleItem + AD_INSERT_BUFFER &&
                adPosition <= totalItemCount) {
            val success = tryInsertAd(baseAdEntity, adDbHelper.getItemIdBeforeIndex(adPosition), adPosition)
                { availableAds.remove(baseAdEntity) }
            if (success) {
                prevAdData = currentAdData
                currentAdData = Pair(adPosition, baseAdEntity)
                isP1AdInserted = true
                nextAdInsertPosition = -1
                unseenAdIds.add(baseAdEntity.uniqueAdIdentifier)
                AdLogger.d(LOG_TAG, "P1 ad inserted at position when adPosition($adPosition) >= " +
                        "lastVisibleItem: $lastVisibleItem")
            }
        } else if (adPosition <= lastVisibleItem && lastVisibleItem + AD_INSERT_BUFFER <= totalItemCount) {
            val position = lastVisibleItem + AD_INSERT_BUFFER
            val success = tryInsertAd(baseAdEntity, adDbHelper.getItemIdBeforeIndex(position), position)
                { availableAds.remove(baseAdEntity) }
            if (success) {
                prevAdData = currentAdData
                currentAdData = Pair(position, baseAdEntity)
                isP1AdInserted = true
                nextAdInsertPosition = -1
                unseenAdIds.add(baseAdEntity.uniqueAdIdentifier)
                AdLogger.d(LOG_TAG, "P1 ad inserted at position when adPosition($position) < " +
                        "lastVisibleItem($lastVisibleItem)")
            }
        }
    }

    private fun markPP1TagProcessed(tag: String) {
        pp1TagOrders[tag] = true
        //check if all tags are inserted successfully
        if (!pp1TagOrders.containsValue(false)) {
            isPP1AdsInserted = true
        }
    }

    @Subscribe
    fun setAdResponse(nativeAdContainer: NativeAdContainer) {
        //Ads helper deals with only P0, Card-p1, Card-PP1 ads in news list.
        if (nativeAdContainer.adPosition != AdPosition.P0 &&
                nativeAdContainer.adPosition != AdPosition.CARD_P1 &&
                nativeAdContainer.adPosition != AdPosition.PP1) {
            return
        }
        if (destroyed) {
            return
        }

        if (nativeAdContainer.uniqueRequestId == INVALID_REQUEST_ID) {
            return
        }

        when (nativeAdContainer.adPosition) {
            AdPosition.P0 -> cardP0ResponseAwaited = false
            AdPosition.CARD_P1 -> cardP1ResponseAwaited = false
            else -> {
            }
        }

        if (nativeAdContainer.uniqueRequestId != uniqueRequestId) {
            return
        }
        AdLogger.d(LOG_TAG, "[${nativeAdContainer.adPosition}]Ad response received : $uniqueRequestId")
        if (nativeAdContainer.baseAdEntities != null) {
            nativeAdContainer.baseAdEntities?.forEach { baseAdEntity ->
                if (baseAdEntity.isShown) {
                    return@forEach
                }

                allAds.add(baseAdEntity.uniqueAdIdentifier)
                when (baseAdEntity.adPosition) {
                    AdPosition.P0 -> {
                        premiumBaseAdEntity = baseAdEntity
                        insertP0AdinList(adDbHelper.getTotalItems())
                    }
                    AdPosition.PP1 -> {
                        baseAdEntity.adTag?.let {
                            localPP1Ads[it] = baseAdEntity
                        }
                        if (nativeAdContainer.doneRequestProcessing && pp1TagOrders.size == localPP1Ads.size) {
                            isPP1ResponseAwaited = false
                        }
                        tryinsertPP1Ads(adDbHelper.getTotalItems())
                    }
                    else -> availableAds.add(baseAdEntity)
                }
            }
            waitForNextAdRequest = false
        } else {
            AdLogger.d(LOG_TAG, "[${nativeAdContainer.adPosition}] Empty response")
            // Empty response has come we should wait for some time
            waitForNextAdRequest = true

            //If the card-p1 ad request is not made yet and for card-p0 ad we got null response, in
            // this specific case we do not want to wait for DEFAULT_CARD_SWIPE_WAIT_COUNT scroll time
            // to make card-p1 ad request.
            if (!p1AdRequestAlreadyMadeInThisSession) {
                waitForNextAdRequest = false
            }

            //On failure case we don't want to request again for p0 ad, So assigning it a empty ad.
            if (premiumBaseAdEntity == null) {
                premiumBaseAdEntity = NoFillOrErrorAd()
            }
            if (nativeAdContainer.adPosition == AdPosition.PP1 && nativeAdContainer.doneRequestProcessing) {
                isPP1ResponseAwaited = false
                tryinsertPP1Ads(adDbHelper.getTotalItems())
            }
        }
        if (nativeAdContainer.adPosition == AdPosition.P0) {
            requestStoryPageAdsFromHome()
            requestExitSplash()
        }

    }

    @Subscribe
    fun onAdViewed(event: AdViewedEvent) {
        // Content ad viewed in detail page should not remove ad from the same entity's feed.
        if (event.viewedParentId == uniqueRequestId || event.entityId == entityId) {
            AdLogger.d(LOG_TAG, "Adviewed event in parent $uniqueRequestId")
            return
        }

        AdLogger.d(LOG_TAG, "ADVIEWED event received $uniqueRequestId")
        // Viewed elsewhere
        if (event.parentIds?.contains(uniqueRequestId) == true) {
            AdLogger.d(LOG_TAG, "Removing ad ${event.adId} from $uniqueRequestId")
            //Remove ad from DB for this list.
            removeAdFromDb(event.adId)
            onAdRemoved(event.adId, event.adPosition)
        }
    }

    @Subscribe
    fun onLangInfoChanged(langInfo: LangInfo) {
        reset(false)
    }

    @Subscribe
    fun onAdFCLimitReachedEvent(event: AdFCLimitReachedEvent) {
        val adsToRemove = mutableListOf<BaseAdEntity>()
        allAds.forEach { adId ->
            AdBinderRepo.getAdById(adId)?.let { ad ->
                if (!ad.isShown) {
                    val capId = AdsUtil.getCapId(ad, event.type)
                    if (capId == event.capId) {
                        AdLogger.d(LOG_TAG, "FC limit reached. [${ad.adPosition}] Removing " +
                                "${ad.uniqueAdIdentifier} from uid:  $uniqueRequestId")
                        adsToRemove.add(ad)
                    }
                }
            }
        }
        adsToRemove.forEach { ad ->
            removeAdFromDb(ad.uniqueAdIdentifier)
            onAdRemoved(event.adId, ad.adPosition!!)
        }
    }

    /**
     * An inserted but not seen ad has been removed.
     * Its position will now become eligible for re-insertion.
     */
    private fun onAdRemoved(removedAdId: String, adPosition: AdPosition) {
        AdLogger.d(LOG_TAG, "onAdRemoved id : $removedAdId , zone : $adPosition")
        when (adPosition) {
            AdPosition.P0 -> {
                //For P0, we reset all fields for fresh ads.
                premiumBaseAdEntity?.deleteObserver(refillAdObserver)
                premiumBaseAdEntity = null
                isP0AdInserted = false
                resetPrevAdPosition()
                requestP0Ad()
            }
            AdPosition.PP1 -> {
                // Do nothing, do not refill as pp1 ads doesn't support caching due to pp1_c1 ad support.(BE limitation)
            }
            else -> {
                //Reset to just the secondLast ad.
                nextAdInsertPosition = -1
                currentAdData = prevAdData
                unseenAdIds.remove(removedAdId)
            }
        }
        allAds.remove(removedAdId)
    }

    private fun refillAd(adPosition: AdPosition) {
        val prefetchEnabled = AdsUtil.isPrefetchEnabled(adPosition, adsUpgradeInfo)
        if (refillAdRequstMade || !prefetchEnabled) {
            return
        }
        //Sending unique request id in negative so response will be ignored.
        val prefetchAdRequestUseCaseController = GetAdUsecaseController(uiBus, INVALID_REQUEST_ID)
        val refillP0Request = getAdRequest(adPosition)
        prefetchAdRequestUseCaseController.requestAds(refillP0Request)
        refillAdRequstMade = true
        prefetchAdRequestUseCaseController.destroy()
    }

    //To be requested after P0 response if this is Home tab.
    private fun requestStoryPageAdsFromHome() {
        if (isHome && section != PageSection.TV.section && requestAds && !detailPrefetchRequestSent) {
            AdLogger.d(LOG_TAG, "Story request made from home")
            detailPrefetchRequestSent = true
            requestAds(AdPosition.STORY)
        }
    }

    //To be requested after P0 response.
    private fun requestExitSplash() {
        ExitSplashAdCommunication.requestExitSplash("feed_$entityId")
    }

    private fun getAdPosition(baseAdEntity: BaseAdEntity): Int {
        if (nextAdInsertPosition != -1) {
            return nextAdInsertPosition
        }

        val defaultDistance = if (baseAdEntity.adPosition == AdPosition.PP1) DEFAULT_PP1_AD_POSITION else 7
        val distance = AdsUtil.getIntValue(if (currentAdData.second?.isLargeAd == true) baseAdEntity.largeAdDistance else baseAdEntity.minAdDistance,
                defaultDistance)

        val desiredPosition = if (baseAdEntity.adPosition == AdPosition.PP1) {
            AdsUtil.getIntValue(baseAdEntity.cardPosition, DEFAULT_PP1_AD_POSITION)
        } else {
            AdsUtil.getIntValue(baseAdEntity.cardPosition, 7)
        }

        var position = if (currentAdData.first == -1) {
            desiredPosition
        } else {
            currentAdData.first + distance
        }
        if (position < 0) {
            position = 0
        }

        if (distance > 0) {
            nextAdInsertPosition = position
            AdLogger.e(LOG_TAG, "Next ad pos calculated $nextAdInsertPosition")
        }
        return position
    }

    fun destroy() {
        if (destroyed) {
            return
        }
        destroyed = true
        reset(viewDestroyed = true)
        fetchAdSpecUsecase.dispose()
        insertAdInfoUsecase.dispose()
        clearAdsDataUsecase.dispose()
        replaceAdInfoUsecase.dispose()
    }

    private fun reset(viewDestroyed: Boolean = false) {
        AdLogger.d(LOG_TAG, "Resetting ad data")
        destroyAds(viewDestroyed)
        isP0AdInserted = false
        isP1AdInserted = false
        refillAdRequstMade = false
        tickerAvailability = TickerAvailability.UNKNOWN
        premiumBaseAdEntity?.deleteObserver(refillAdObserver)
        premiumBaseAdEntity = null
        //pp1 reset
        isPP1AdsInserted = false
        pp1TagOrders.clear()
        localPP1Ads.clear()
        insertedPP1IdList.clear()
        isPP1ResponseAwaited = false
        processingTag = null
        usedPrevPostId = null
        processingAdId = null
        //remove pp1 refill observer
        refillPP1Ads.forEach {
            it.deleteObserver(refillAdObserver)
        }
        refillPP1Ads.clear()
        resetPrevAdPosition()
    }

    private fun destroyAds(viewDestroyed: Boolean) {
        purgeAdsFromDb()
        //Destroy ads
        AdBinderRepo.destroyAds(allAds, uniqueRequestId, viewDestroyed)
        AdFrequencyStats.onViewDestroyed(uniqueRequestId)
        replacedAds?.forEach {
            AdsUtil.destroyAd(it, uniqueRequestId)
        }
        availableAds.clear()
        unseenAdIds.clear()
        allAds.clear()
        replacedAds?.clear()
    }

    /**
     * If an ad has been replaced, mark it as shown to allow removing from cache.
     *
     * @param oldAd replaced ad
     */
    fun onAdReplaced(oldAd: BaseAdEntity, newAd: BaseAdEntity) {
        AdLogger.d(LOG_TAG, "onAdReplaced. old : ${oldAd.uniqueAdIdentifier} , new : ${newAd.uniqueAdIdentifier}")
        oldAd.isShown = true
        oldAd.notifyObservers()

        //Replace in mem cache and db.
        AdBinderRepo.add(newAd)
        newAd.parentIds.add(uniqueRequestId)
        replaceAdInfoUsecase.execute(ReplaceAdInfoUsecase.bundle(newAd, oldAd.uniqueAdIdentifier))

        if (replacedAds == null) {
            replacedAds = ArrayList()
        }
        replacedAds?.add(oldAd)
    }

    private fun tryInsertAd(baseAdEntity: BaseAdEntity?, prevPostId: String?,
                            adapterPos: Int, onAdInvalid: () -> Unit): Boolean {
        AdLogger.d(LOG_TAG, "tryInsertAd ${baseAdEntity?.adPosition}, prevPostId : $prevPostId, ad id : ${baseAdEntity?.i_adId()}")
        baseAdEntity ?: return false
        if (prevPostId == null && adapterPos != 0 || prevPostId != null && prevPostId == usedPrevPostId) {
            AdLogger.d(LOG_TAG, "tryInsertAd Aborted.")
            return false
        }
        val adPosCheckTs = System.currentTimeMillis()
        if (AdsUtil.isFCLimitReachedForAd(baseAdEntity, uniqueRequestId)) {
            AdLogger.d(LOG_TAG, "tryInsertAd Aborted. FC limit exhausted already.")
            onAdInvalid()
            allAds.remove(baseAdEntity.uniqueAdIdentifier)
            return false
        }
        if (adDbHelper.insertAdInList(baseAdEntity, adapterPos)) {
            AdLogger.d(LOG_TAG, "tryInsertAd in DB.")
            processingAdId = baseAdEntity.i_id()
            uniqueAdIdentifierTriedLast = baseAdEntity.uniqueAdIdentifier
            AdBinderRepo.add(baseAdEntity)
            insertAdInfoUsecase.execute(InsertAdInfoUsecase.bundle(baseAdEntity, prevPostId, adPosCheckTs, adapterPos))

            AdFrequencyStats.onAdInsertedInView(baseAdEntity, uniqueRequestId)
            return true
        }
        return false
    }

    companion object {

        private const val DEFAULT_P0_AD_POSITION = 3
        private const val DEFAULT_P0_AD_POSITION_WITH_TICKER = 3
        private const val DEFAULT_PP1_AD_POSITION = 3
        private const val DEFAULT_CARD_SWIPE_WAIT_COUNT = 7
        private const val AD_INSERT_BUFFER = 1
        private const val AD_INSERT_RETRY_BUFFER = 3
        private const val INVALID_REQUEST_ID = -999
        private val INVALID_AD_DATA = Pair(-1, null)
    }
}

private const val LOG_TAG = "AdsHelper"

interface AdDBHelper {
    fun isViewVisible(): Boolean = false
    fun getTotalItems(): Int?
    fun getItemIdBeforeIndex(adPosition: Int): String?
    fun insertAdInList(baseAdEntity: BaseAdEntity, adapterPos: Int): Boolean {
        return false
    }

    fun getActivityContext(): Activity?
}
