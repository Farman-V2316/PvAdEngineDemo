/*
* Copyright (c) 2019 Newshunt. All rights reserved.
*/
package com.newshunt.adengine.view.helper

import `in`.dailyhunt.money.contentContext.ContentContext
import android.app.Activity
import androidx.lifecycle.LifecycleOwner
import com.newshunt.adengine.FetchAdSpecUsecase
import com.newshunt.adengine.domain.controller.GetAdUsecaseController
import com.newshunt.adengine.model.entity.AdFCLimitReachedEvent
import com.newshunt.adengine.model.entity.AdViewedEvent
import com.newshunt.adengine.model.entity.BaseAdEntity
import com.newshunt.adengine.model.entity.BaseDisplayAdEntity
import com.newshunt.adengine.model.entity.NativeAdContainer
import com.newshunt.adengine.model.entity.version.AdPosition
import com.newshunt.adengine.model.entity.version.AdRequest
import com.newshunt.adengine.model.entity.version.AmazonSdkPayload
import com.newshunt.adengine.util.AdFrequencyStats
import com.newshunt.adengine.util.AdLogger
import com.newshunt.adengine.util.AdStatisticsHelper
import com.newshunt.adengine.util.AdsUtil
import com.newshunt.common.helper.common.BusProvider
import com.newshunt.common.helper.common.Constants
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.common.asset.CommonAsset
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.common.pages.PageEntity
import com.newshunt.dataentity.common.pages.PageSection
import com.newshunt.dataentity.dhutil.model.entity.upgrade.LangInfo
import com.newshunt.dataentity.news.model.entity.DetailCardType
import com.newshunt.dataentity.social.entity.AdSpec
import com.newshunt.dataentity.social.entity.Position
import com.newshunt.dataentity.social.entity.ZoneConfig
import com.newshunt.dhutil.analytics.originalMessage
import com.newshunt.dhutil.helper.AdsUpgradeInfoProvider
import com.newshunt.news.model.usecase.MediatorUsecase
import com.squareup.otto.Bus
import com.squareup.otto.Subscribe
import org.jetbrains.annotations.NotNull
import javax.inject.Inject
import javax.inject.Named

/**
 * Helper to manage fetch,insert and display of ads in a Post.
 *
 * @author raunak.yadav
 */
class PostAdsHelper(private val post: CommonAsset?,
                    private val uniqueRequestId: Int,
                    private val parentEntity: PageEntity?,
                    private var adConsumer: AdConsumer?,
                    private val section: String?,
                    lifecycleOwner: LifecycleOwner,
                    private val referrer: PageReferrer?,
                    private val fetchAdSpecUsecase: MediatorUsecase<List<String?>, Map<String, AdSpec>>) {

    class Factory @Inject constructor(@Named("pageEntity") private val pageEntity: PageEntity? = null,
                                      @Named("adConsumer") private val adConsumer: AdConsumer?,
                                      @Named("section") private val section: String?,
                                      private val lifecycleOwner: LifecycleOwner,
                                      private val fetchAdSpecUsecase: FetchAdSpecUsecase) {

        fun create(post: CommonAsset, uniqueRequestId: Int, referrer: PageReferrer?): PostAdsHelper {
            return PostAdsHelper(post, uniqueRequestId, pageEntity, adConsumer, section,
                    lifecycleOwner, referrer, fetchAdSpecUsecase)
        }
    }

    private var destroyed: Boolean = false
    private val allowedZones = mutableSetOf(AdPosition.STORY.value,
            AdPosition.SUPPLEMENT.value, AdPosition.DHTV_MASTHEAD.value)
    private val supplementSubSlots = mutableListOf<String>()
    private val uiBus: Bus = BusProvider.getUIBusInstance()
    private var getAdUsecaseController: GetAdUsecaseController = GetAdUsecaseController(uiBus, uniqueRequestId)
    private val adRequestStatus = mutableMapOf<AdPosition, Status>()
    private var registered = false
    private var parentAdSpec: AdSpec? = null
    private var postAdSpec: AdSpec? = post?.i_adSpec()
    private var adSpecFetchDone = false
    private val adSpecLiveData = fetchAdSpecUsecase.data()
    private var adsZoneConfigMap = mutableMapOf<String, ZoneConfig?>()
    private val unseenSupplementAds = HashSet<String>()
    private val shiftedAdPositions = mutableMapOf<String, Position>()

    enum class Status {
        NONE, READY, IN_PROGRESS, COMPLETE, REFILL
    }

    init {
        AdsUpgradeInfoProvider.getInstance().adsUpgradeInfo?.supplementAdConfig?.tagOrder?.let {
            if (it.isNotEmpty()) {
                supplementSubSlots.addAll(it)
            }
        }
        val adSpecIds = mutableListOf<String?>()
        adSpecIds.add(parentEntity?.id)

        // If post adspec is missing, get it from source/cat if present in handshake response.
        if (post?.i_adSpec() == null) {
            AdLogger.e(TAG, "Post adSpec not present. Fallback to source/cat")
            adSpecIds.add(post?.i_source()?.catId)
            adSpecIds.add(post?.i_source()?.id)
        }

        adSpecLiveData.observe(lifecycleOwner, androidx.lifecycle.Observer {
            adSpecFetchDone = true
            if (it.isFailure) {
                AdLogger.e(TAG, "AdSpec fetch failed : ${it.exceptionOrNull()?.originalMessage()}")
            } else {
                it.getOrNull()?.let { adSpecs ->
                    parentAdSpec = adSpecs[parentEntity?.id]
                    AdLogger.v(TAG, "Received parent AdSpec $parentAdSpec")
                    postAdSpec = post?.i_adSpec()?: adSpecs[post?.i_source()?.catId]
                            ?: adSpecs[post?.i_source()?.id]
                }
            }
            onPostAdSpecReceived(postAdSpec)
            triggerPendingRequests()
        })
        fetchAdSpecUsecase.execute(adSpecIds)

        if (!registered) {
            uiBus.register(this)
            registered = true
        }
    }

    private fun onPostAdSpecReceived(adSpec: AdSpec?) {
        AdLogger.v(TAG, "Using adSpec for post : $adSpec ")
        adSpec?.adZones?.toHide?.forEach { zoneConfig ->
            zoneConfig.zone?.let { zone ->
                zoneConfig.showIf?.let {
                    adsZoneConfigMap.put(zone, zoneConfig)
                }
            }
        }
        AdsUtil.filterBlockedZones(adSpec, allowedZones, post?.i_id(), TAG, supplementSubSlots)
        if (supplementSubSlots.isEmpty()) {
            allowedZones.remove(AdPosition.SUPPLEMENT.value)
        }
    }

    fun getAdConfigByAdZone(position: String) : ZoneConfig? {
        return adsZoneConfigMap[position]
    }
    fun start() {
        //requestAds(activity, AdPosition.MASTHEAD)
        requestAds(AdPosition.STORY)
    }

    fun startDHTVMastHead() {
        requestAds(AdPosition.DHTV_MASTHEAD)
    }

    fun stop() {
        if (registered) {
            uiBus.unregister(this)
            registered = false
        }
    }

    /**
     * Place admarkers for different zones as per the adSpec of Post.
     */
    fun addAdStubsInPost(elements: MutableList<DetailCardType>): List<String> {
        postAdSpec?.adZones?.let {
            it.adZonePositions?.forEach { item ->
                if (!allowedZones.contains(item.zone)) {
                    return@forEach
                }
                shiftAdStub(elements, item.zone, item.position)
            }
        }

        // If some ad was already shifted as per config from handshake, place it appropriately.
        for ((zone, position) in shiftedAdPositions) {
            shiftAdStub(elements, zone, position)
        }

        // Substitute supplement slot with subslots. Now convert list to strings as subslots are
        // not constant enums.
        return if (elements.contains(DetailCardType.SUPPLEMENT)) {
            val index = elements.indexOf(DetailCardType.SUPPLEMENT)
            elements.removeAt(index)
            addSupplementSubSlots(index, elements.map { it.name }.toMutableList())
        } else {
            elements.map { it.name }
        }
    }

    private fun shiftAdStub(elements: MutableList<DetailCardType>,
                            zone: String?, position: Position?) {
        val zoneEnum = toCardEnum(zone) ?: return

        // Get the anchor element
        val anchor = try {
            DetailCardType.valueOf(position?.element?.uppercase() ?: return)
        } catch (ex: Exception) {
            AdLogger.d(TAG, "${position?.element} : No/Invalid Anchor element in config.")
            return
        }

        if (!elements.contains(anchor) && !(anchor == DetailCardType.IMAGE && elements.contains(DetailCardType.IMAGE_DYNAMIC))) {
            AdLogger.d(TAG, " $zoneEnum: Anchor($anchor) absent in post.")
            return
        }
        elements.remove(zoneEnum)

        val isPosBelow = position.relativePos?.equals("Below", ignoreCase = true) == true

        //In case of Image/s, insert before the 1st image or after the last image.
        var anchorIndex = if (anchor == DetailCardType.IMAGE) {
            if (isPosBelow) {
                elements.map { type ->
                    type.adGroup
                }.lastIndexOf(anchor.adGroup)
            } else {
                elements.map { type ->
                    type.adGroup
                }.indexOf(anchor.adGroup)
            }
        } else {
            elements.indexOf(anchor)
        }

        if (isPosBelow) {
            anchorIndex += 1
        }

        when (zone) {
            AdPosition.MASTHEAD.value,
            AdPosition.STORY.value,
            AdPosition.SUPPLEMENT.value -> {
                elements.add(anchorIndex, zoneEnum)
            }
            else -> {
                AdLogger.d(TAG, "Unhandled zone present in adSpec $zone")
            }
        }
    }

    /**
     * 1st ad of the day must be shifted as per handshake config.
     */
    fun shiftAdForThinUser(currentOrder: List<String>, ad: BaseAdEntity?): List<String>? {
        // Shifting applicable only to Storypage as of now.
        if (ad?.adPosition != AdPosition.STORY) {
            return null
        }

        // Apply shifting to only 1st ad of the day.
        if (AdStatisticsHelper.adStatistics.totalSeenAds > 0) {
            return null
        }

        val config = AdsUpgradeInfoProvider.getInstance().adsUpgradeInfo?.storyPageAdConfig
            ?: return null
        val position = config.position ?: return null

        // If ad is regular, check if it needs shifting.
        if (!ad.isEvergreenAd && config.enableShiftRegularAd != true) {
            return null
        }

        val elements = currentOrder.mapNotNull {
            try {
                DetailCardType.valueOf(it)
            } catch (ex: Exception) {
                null
            }
        }.toMutableList()

        val zone = ad.adPosition?.value ?: return null
        if (ad is BaseDisplayAdEntity) {
            ad.showOnlyImage = true
        }
        ad.displayPosition = position

        shiftAdStub(elements, zone, position)
        shiftedAdPositions[zone] = position
        AdLogger.d(TAG, "Shifting [${ad.adPosition}][${ad.uniqueAdIdentifier}] to $position")
        return elements.map { it.name }
    }

    fun toCardEnum(zone: String?): DetailCardType? {
        return when (zone) {
            AdPosition.MASTHEAD.value -> DetailCardType.MASTHEAD
            AdPosition.STORY.value -> DetailCardType.STORYPAGE
            AdPosition.SUPPLEMENT.value -> DetailCardType.SUPPLEMENT
            else -> null
        }
    }

    private fun addSupplementSubSlots(index: Int, elements: MutableList<String>): List<String> {
        if (index == -1 || supplementSubSlots.isNullOrEmpty()) {
            return elements
        }
        var localIndex = index
        supplementSubSlots.forEach { tag ->
            elements.add(localIndex, AdsUtil.getAdSlotName(tag ,AdPosition.SUPPLEMENT))
            localIndex += 1
        }
        return elements
    }

    /**
     * Fetches local ads(sync) and sends n/w request(async), if needed.
     */
    fun requestAds(adPosition: AdPosition, isRefill: Boolean = false) {
        if (!canRequestAdFor(adPosition, isRefill)) {
            return
        }
        if (!adSpecFetchDone) {
            AdLogger.d(TAG, "Parent context not fetched yet." +
                    "Queueing request for $adPosition id :$uniqueRequestId")
            adRequestStatus[adPosition] = Status.READY
            return
        }
        AdLogger.d(TAG, "request Ads $adPosition $uniqueRequestId, refill : $isRefill")
        adRequestStatus[adPosition] = if (isRefill) Status.REFILL else Status.IN_PROGRESS
        val adRequest = getAdRequest(adPosition)
        val adContainer = getAdUsecaseController.requestAds(adRequest,
                AdsUpgradeInfoProvider.getInstance().adsUpgradeInfo, false)
        if (adContainer == null) {
            allowedZones.remove(adPosition.value)
            adRequestStatus[adPosition] = Status.COMPLETE

            if (adPosition == AdPosition.MASTHEAD && section != PageSection.TV.section
                    && adConsumer?.isFragmentVisible() == true) {
                //Send event so that other fragments may prefetch ad now.
                BusProvider.getUIBusInstance().post(AdViewedEvent(Constants.EMPTY_STRING, uniqueRequestId,
                        null, adPosition))
            }
        } else {
            processAds(adContainer)
        }
    }

    //To be requested after detail zones.
    private fun requestExitSplash() {
        ExitSplashAdCommunication.requestExitSplash("post_${post?.i_id()}")
    }

    private fun triggerPendingRequests() {
        adRequestStatus.entries.forEach {
            if (it.value == Status.READY) {
                requestAds(it.key)
            }
        }
    }

    private fun canRequestAdFor(adPosition: AdPosition, isRefill: Boolean): Boolean {
        adConsumer ?: return false
        if (adRequestStatus[adPosition] == Status.IN_PROGRESS ||
                adRequestStatus[adPosition] == Status.COMPLETE) {
            AdLogger.d(TAG, "Abort. Duplicate request for $adPosition id :$uniqueRequestId")
            return false
        }
        if (!allowedZones.contains(adPosition.value)) {
            AdLogger.d(TAG, "Zone ${adPosition.value} is blocked")
            return false
        }
        // If view has the ad already, do not request again.
        return isRefill || adConsumer?.getAdsMap()?.containsKey(adPosition.value) != true
    }

    /**
     * Request backupAd for an adPosition.
     * Call should return immediately as we are fetching an existing ad.
     *
     * @param adPosition adPosition
     * @return backup ad, if available
     */
    fun requestBackupAd(adPosition: AdPosition): BaseAdEntity? {
        if (destroyed) {
            return null
        }
        return getAdUsecaseController.getInventoryInstance(adPosition)?.getBackupAd(getAdRequest(adPosition))
    }

    private fun getAdRequest(adPosition: AdPosition): AdRequest {
        val contextMap = mutableMapOf<String, ContentContext>()
        val parentContextMap = mutableMapOf<String, ContentContext>()
        var numOfAds = 1
        if (adPosition == AdPosition.SUPPLEMENT) {
            supplementSubSlots.forEach {
                val key = AdsUtil.getAdSlotName(it, adPosition)
                AdsUtil.getContentContextFor(postAdSpec, key)?.let {
                    contextMap[key] = it
                }
                AdsUtil.getContentContextFor(parentAdSpec, key)?.let {
                    parentContextMap[key] = it
                }
            }
            numOfAds = supplementSubSlots.size
        } else {
            AdsUtil.getContentContextFor(postAdSpec, adPosition.value)?.let {
                contextMap[adPosition.value] = it
            }
            AdsUtil.getContentContextFor(parentAdSpec, adPosition.value)?.let {
                parentContextMap[adPosition.value] = it
            }
        }

        val requestBodyList = AdsUtil.getAmazonRequestBody(adPosition)
        AdsUtil.makeAmazonAdRequest(adPosition)

        return AdRequest(adPosition,
                numOfAds,
                entityId = parentEntity?.id,
                entityType = parentEntity?.entityType,
                entitySubType = parentEntity?.subType,
                postId = post?.i_id(),
                sourceId = post?.i_source()?.id,
                sourceCatId = post?.i_source()?.catId,
                sourceType = post?.i_source()?.type,
                contentContextMap = contextMap,
                parentContextMap = parentContextMap,
                pageReferrer = referrer,
                referrerId = referrer?.id,
                localRequestedAdTags = if (adPosition == AdPosition.SUPPLEMENT) ArrayList(supplementSubSlots) else null,
                section = section,
                amazonSdkPayload = AmazonSdkPayload(requestBody = requestBodyList)
        )
    }

    @Subscribe
    fun setAdResponse(nativeAdContainer: NativeAdContainer) {
        if (nativeAdContainer.uniqueRequestId != uniqueRequestId) {
            if (adRequestStatus[nativeAdContainer.adPosition] == Status.IN_PROGRESS)
                adRequestStatus[nativeAdContainer.adPosition] = Status.NONE
            return
        }
        adRequestStatus[nativeAdContainer.adPosition] = Status.COMPLETE
        if (adRequestStatus[nativeAdContainer.adPosition] != Status.REFILL) {
            processAds(nativeAdContainer)
        }
    }

    private fun processAds(nativeAdContainer: NativeAdContainer) {
        if (CommonUtils.isEmpty(nativeAdContainer.baseAdEntities) ||
                !allowedZones.contains(nativeAdContainer.adPosition.value)) {
            return
        }
        when (val adPos = nativeAdContainer.adPosition) {
            AdPosition.STORY,
            AdPosition.MASTHEAD,
            AdPosition.DHTV_MASTHEAD -> nativeAdContainer.baseAdEntities?.get(0)?.let { ad ->
                if (ad.campaignId?.let { AdsUtil.isFCLimitReachedForAd(ad, uniqueRequestId) } != true) {
                    adConsumer?.insertAd(ad, adPos.value)
                    AdLogger.d(TAG, "[${ad.adPosition}]Ad Inserted : $ad")
                }
            }
            AdPosition.SUPPLEMENT -> {
                nativeAdContainer.baseAdEntities?.forEach { ad ->
                    if (ad.campaignId?.let { AdsUtil.isFCLimitReachedForAd(ad, uniqueRequestId) } != true) {
                        unseenSupplementAds.add(ad.uniqueAdIdentifier)
                        adConsumer?.insertAd(ad, AdsUtil.getAdSlotName(ad.adTag, adPos))
                        AdLogger.d(TAG, "[${ad.adPosition}][${ad.adTag}]Ad Inserted : $ad")
                    }
                }
            }
            else -> AdLogger.d(TAG, "Post does not support zone : $adPos")
        }
        adRequestStatus[nativeAdContainer.adPosition] = Status.COMPLETE
        requestExitSplash()
    }

    /**
     * If an ad has been replaced, mark it as shown to allow removing from cache.
     *
     * @param oldAd replaced ad
     */
    fun onAdReplaced(oldAd: BaseAdEntity, newAd: BaseAdEntity) {
        oldAd.isShown = true
        oldAd.notifyObservers()
        AdsUtil.destroyAd(oldAd, uniqueRequestId)
    }

    fun destroy() {
        if (destroyed) {
            return
        }
        destroyed = true
        AdFrequencyStats.onViewDestroyed(uniqueRequestId)
        adConsumer?.getAdsMap()?.values?.forEach {
            AdsUtil.destroyAd(it, uniqueRequestId)
        }
        fetchAdSpecUsecase.dispose()
        adConsumer = null
    }

    /**
     * Ad Viewed Event for different zones :
     *  Case 1 : Ad seen in same fragment
     *           MASTHEAD -> Do not refill, Next fragment will pre-fetch.
     *           STORY -> send refill request.
     *           SUPPLEMENT -> refill only when all ads seen.
     *  Case 2 : Ad seen in adjacent fragment
     *           - Remove ad is added here too.
     *           - Prefetch Masthead if not inserted.
     */
    @Subscribe
    fun onAdViewedEvent(adViewedEvent: AdViewedEvent) {
        val adPosition = adViewedEvent.adPosition
        unseenSupplementAds.remove(adViewedEvent.adId)
        if (uniqueRequestId == adViewedEvent.viewedParentId) {
            // Refill cache for the zone, if allowed.
            if (adPosition == AdPosition.STORY ||
                    (adPosition == AdPosition.SUPPLEMENT && unseenSupplementAds.isEmpty())) {
                AdLogger.d(TAG, "Sending refill request for : $adPosition")
                adRequestStatus[adPosition] = Status.NONE
                requestAds(adPosition, true)
            }
            return
        }

        // Ad viewed in some other fragment.
        if (adViewedEvent.parentIds?.contains(uniqueRequestId) == true) {
            AdLogger.e(TAG, "$adPosition Ad: ${adViewedEvent.adId} already " +
                    "consumed elsewhere. Remove from $uniqueRequestId")
            val adsMap = adConsumer?.getAdsMap()
            if (adsMap.isNullOrEmpty()) {
                return
            }
            // Remove ad, if inserted here too.
            val key = when (adPosition) {
                AdPosition.SUPPLEMENT -> AdsUtil.getAdSlotName(adViewedEvent.adTag ,adPosition)
                else -> adPosition.value
            }
            if (adsMap.containsKey(key) && adsMap[key]?.uniqueAdIdentifier == adViewedEvent.adId) {
                adConsumer?.removeSeenAd(key)
                adRequestStatus[adPosition] = Status.NONE
            }
        }
    }

    @Subscribe
    fun onAdFCLimitReachedEvent(event: AdFCLimitReachedEvent) {
        val adsToRemove = mutableListOf<String>()
        val adsMap = adConsumer?.getAdsMap() ?: return
        for ((adSlot, ad) in adsMap) {
            if (!ad.isShown) {
                val capId = AdsUtil.getCapId(ad, event.type)
                if (capId == event.capId) {
                    AdLogger.d(TAG, "FC limit exhausted for campaign : ${event.capId}. " +
                            "Removing ${ad.adPosition} : ${ad.uniqueAdIdentifier} from uid:  $uniqueRequestId")
                    adsToRemove.add(adSlot)
                    ad.adPosition?.let { adRequestStatus[it] = Status.NONE }
                }
            }
        }
        adsToRemove.forEach { adSlot ->
            adConsumer?.removeSeenAd(adSlot)
        }
    }

    @Subscribe
    fun onLangInfoChanged(langInfo: LangInfo) {
        AdLogger.d(TAG, "LangInfo changed. Remove inserted ads")
        val adsToRemove = mutableListOf<String>()
        adConsumer?.getAdsMap()?.forEach { entry ->
            adsToRemove.add(entry.key)
            entry.value.adPosition?.let { adRequestStatus[it] = Status.NONE }
        }
        adsToRemove.forEach { adSlot ->
            adConsumer?.removeSeenAd(adSlot)
        }
    }

    fun resetAdRequestStatus( adPosition: AdPosition) {
        adRequestStatus[adPosition] = Status.NONE
    }


    // Information about ads required to remove
    fun handleRemovalReportedAdsPost(@NotNull reportedAdEntity: BaseDisplayAdEntity,
                                     reportedAdParentUniqueAdIdIfCarousal: String?){
        if(adConsumer != null){
            val adsMap = adConsumer?.getAdsMap()
            // Remove ad, if inserted here too.
            val key = when (reportedAdEntity.adPosition) {
                AdPosition.SUPPLEMENT -> AdsUtil.getAdSlotName(reportedAdEntity.adTag, AdPosition.SUPPLEMENT)
                else -> reportedAdEntity.adPosition?.value
            }

            if (adsMap != null && adsMap.containsKey(key)) {
                if(adsMap[key]?.uniqueAdIdentifier ==
                        reportedAdEntity.uniqueAdIdentifier){
                    adConsumer?.removeSeenAd(key!!)
                } else {
                    if(!CommonUtils.isEmpty(reportedAdParentUniqueAdIdIfCarousal)){
                        adConsumer?.removeSeenAd(key!!)
                    }
                }
            }
        }
    }
}

private const val TAG = "PostAdsHelper"

interface AdConsumer {
    fun insertAd(baseAdEntity: BaseAdEntity, adPositionWithTag: String)
    fun removeSeenAd(adSlot: String)
    fun getActivityContext(): Activity?
    fun isFragmentVisible(): Boolean
    fun getAdsMap(): Map<String, BaseAdEntity>?
}
