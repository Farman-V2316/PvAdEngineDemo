/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.adengine.view.helper

import android.app.Activity
import android.view.LayoutInflater
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleOwner
import com.newshunt.adengine.BR
import com.newshunt.adengine.R
import com.newshunt.adengine.client.AmazonAdFetcher
import com.newshunt.adengine.client.AsyncAdImpressionReporter
import com.newshunt.adengine.databinding.ExternalAdContainerBinding
import com.newshunt.adengine.databinding.LayoutHtmlFullPageAdBinding
import com.newshunt.adengine.domain.controller.GetAdUsecaseController
import com.newshunt.adengine.listeners.ReportAdsMenuListener
import com.newshunt.adengine.model.entity.BaseAdEntity
import com.newshunt.adengine.model.entity.BaseDisplayAdEntity
import com.newshunt.adengine.model.entity.ContentAd
import com.newshunt.adengine.model.entity.EmptyAd
import com.newshunt.adengine.model.entity.ExternalSdkAd
import com.newshunt.adengine.model.entity.NativeAdContainer
import com.newshunt.adengine.model.entity.NativeAdHtml
import com.newshunt.adengine.model.entity.NativePgiAdAsset
import com.newshunt.adengine.model.entity.PgiArticleAd
import com.newshunt.adengine.model.entity.version.AdPosition
import com.newshunt.adengine.model.entity.version.AdRequest
import com.newshunt.adengine.model.entity.version.AmazonSdkPayload
import com.newshunt.adengine.util.AdLogger
import com.newshunt.adengine.util.AdsUtil
import com.newshunt.adengine.view.viewholder.ExternalSdkViewHolder
import com.newshunt.adengine.view.viewholder.NativeAdHtmlViewHolder
import com.newshunt.common.helper.common.BusProvider
import com.newshunt.common.helper.common.Logger
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.common.asset.Format
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.dhutil.model.entity.adupgrade.ApsInfo
import com.newshunt.dataentity.dhutil.model.entity.adupgrade.PgiAdsConfig
import com.newshunt.dataentity.social.entity.AdSpec
import com.newshunt.dhutil.helper.AdsUpgradeInfoProvider
import com.squareup.otto.Bus
import com.squareup.otto.Subscribe

/**
 * Requests for pgi ads, manages page swipe count and shows pgi ads
 * according to minPageSwipeCount value.
 *
 * @author Mukesh Yadav
 */
private const val LOG_TAG = "PgiAdHandler"
private const val MAX_PAGE_SWIPE_COUNT = 15
private const val DEFAULT_PAGE_SWIPE_COUNT = 12
private const val DEFAULT_FIRST_SWIPE_COUNT = 12
private const val DEFAULT_REQUEST_SWIPE_COUNT = 0
private const val DEFAULT_NO_FILL_RETRY_SWIPE_COUNT = 7

object PgiAdHandler {

    private var requestSent: Boolean = false
    private var initialised: Boolean = false
    private var uniqueRequestId: Int = 0
    private var baseAdEntity: BaseAdEntity? = null
    private var pageReferrer: PageReferrer? = null
    private var pgiAdHandlerInfo: PgiAdHandlerInfo? = null
    private val uiBus: Bus = BusProvider.getUIBusInstance()
    private var registered: Boolean = false
    private var getAdUsecaseController: GetAdUsecaseController? = null
    private var isFirstPgiAd = true
    private var pageSwipeCountToShowAd: Int = 0
    private var requestSwipeCount: Int = 0
    private var minThresHoldSwipeCount: Int = 0
    private var maxPageSwipeCount = MAX_PAGE_SWIPE_COUNT
    private var pgiNoFillRetrySwipeCount: Int = 0
    private var swipesAfterRequest: Int = 0
    private var nofillOccurred: Boolean = false
    private var swipeCounter: SwipeCounter? = null
    private var isAdBlockedForEntity: Boolean = false

    val nativePgiAdAsset: NativePgiAdAsset?
        get() {
            if (isSwipeablePgiAd && canShowPgiAd()) {
                isFirstPgiAd = false
                return NativePgiAdAsset(baseAdEntity?.uniqueAdIdentifier!!,
                        null, null, Format.AD, baseAdEntity)
            }
            return null
        }

    private val isPopupHTMLPgiAd: Boolean
        get() =
            baseAdEntity is NativeAdHtml && PgiAdsConfig.HTMLPgiDisplayType.POPUP == (baseAdEntity as NativeAdHtml).interstitialDisplayType

    private val isSwipeableHTMLPgiAd: Boolean
        get() =
            baseAdEntity is NativeAdHtml && PgiAdsConfig.HTMLPgiDisplayType.POPUP != (baseAdEntity as NativeAdHtml).interstitialDisplayType

    private val isSwipeablePgiAd: Boolean
        get() = baseAdEntity is PgiArticleAd || baseAdEntity is ContentAd || AdsUtil.isExternalSdkNativePgiAd(baseAdEntity) ||
                isSwipeableHTMLPgiAd

    init {
        initialize()
    }

    fun initialize() {
        if (!registered) {
            uiBus.register((this))
            registered = true
        }
        if (swipeCounter == null) {
            swipeCounter = SwipeCounter()
        }
    }

    //To be called once when we enter detail mode.
    fun initPgiAdHandler(uniqueRequestId: Int, activity: Activity?,
                         pageReferrer: PageReferrer? = null,
                         pgiAdHandlerInfo: PgiAdHandlerInfo) {
        AdLogger.v(LOG_TAG, "Got adSpec for pgi ${pgiAdHandlerInfo.adSpec}")
        initialize()
        handleEntityChange(this.pgiAdHandlerInfo, pgiAdHandlerInfo, activity)
        this.pageReferrer = pageReferrer
        this.pgiAdHandlerInfo = pgiAdHandlerInfo
        if (initialised) {
            return
        }

        initialised = true
        this.uniqueRequestId = uniqueRequestId
        requestSwipeCount = AdsUtil.getRequestSwipeCountFromHandshake(DEFAULT_REQUEST_SWIPE_COUNT)

        val adsUpgradeInfo = AdsUpgradeInfoProvider.getInstance().adsUpgradeInfo
        pgiNoFillRetrySwipeCount = adsUpgradeInfo?.pgiNoFillRetrySwipeCount ?: 0
        if (pgiNoFillRetrySwipeCount <= 0) {
            pgiNoFillRetrySwipeCount = DEFAULT_NO_FILL_RETRY_SWIPE_COUNT
        }
        requestPgiAds(1, AdPosition.PGI, activity)
    }

    fun reset(activity: Activity? = null, destroyed: Boolean = false) {
        baseAdEntity = null
        requestSent = false
        nofillOccurred = false
        swipesAfterRequest = 0
        pageSwipeCountToShowAd = 0
        if (!destroyed) {
            swipeCounter?.resetSwipeCount()
            requestPgiAds(1, AdPosition.PGI, activity)
        }
    }

    /**
     * If ad is unusable due to FC limit reached, discard it and request a new one.
     */
    fun discardFCExhaustedAd(adEntity: BaseAdEntity, activity: Activity?) {
        if (baseAdEntity?.i_adId() == adEntity.i_adId()) {
            baseAdEntity = null
            requestPgiAds(1, AdPosition.PGI, activity)
            return
        }
    }

    private fun isAdInvalid(adEntity: BaseAdEntity?): Boolean {
        return adEntity == null || AdsUtil.isFCLimitReachedForAd(adEntity, uniqueRequestId, false)
    }

    /*
    * Needs to be called at 0th position, it just update the PGI count but not show PGI ads at 0th position
    * */
    fun updateSwipeCount() {

        swipeCounter?.incrementSwipeCount()
        AdLogger.d(LOG_TAG, "PGI: swipe count : ${swipeCounter?.swipeCount} " +
                "Required Ad Offset : $pageSwipeCountToShowAd ")
    }

    fun updatePageInfoAndSwipeCount(activity: Activity?, reportAdsMenuListener: ReportAdsMenuListener? = null, lifecycleOwner: LifecycleOwner?) {

        swipeCounter?.incrementSwipeCount()
        AdLogger.d(LOG_TAG, "PGI: swipe count : ${swipeCounter?.swipeCount} " +
                "Required Ad Offset : $pageSwipeCountToShowAd ")

        if (isAdBlockedForEntity) {
            return
        }

        if (isAdInvalid(baseAdEntity)) {
            baseAdEntity = null
            swipesAfterRequest++
            requestPgiAds(1, AdPosition.PGI, activity)
            return
        }

        handlePgiAdDisplay(activity, baseAdEntity as? BaseDisplayAdEntity?, reportAdsMenuListener, lifecycleOwner)
    }

    private fun handlePgiAdDisplay(
        activity: Activity?, baseDisplayAdEntity:
        BaseDisplayAdEntity?, reportAdsMenuListener: ReportAdsMenuListener?, lifecycleOwner: LifecycleOwner?
    ) {
        if (!isCompletelyDownloaded(baseDisplayAdEntity)) {
            reset(activity)
            return
        }

        if (swipeCounter?.sessionSwipeCount ?: 0 > maxPageSwipeCount) {

            // When ever we see swipe count reached more than MaxPageswipeCount, we call reset method,
            // which will again request an Ad from native ad inventory manager and reset swipe count,
            // Ad inventory manager will return the same ad if it was not seen. So If we
            // have Pgi Article Ad, we will call its notifyObserver() method before calling reset, So that
            // it will be removed from Native Ad Inventory.
            if (baseAdEntity is PgiArticleAd) {
                baseAdEntity?.notifyObservers()
            }

            reset(activity)
        } else if (canShowPgiAd()) {
            showPgiAd(activity, reportAdsMenuListener, lifecycleOwner)
            isFirstPgiAd = false
        }
    }

    private fun isCompletelyDownloaded(baseDisplayAdEntity: BaseDisplayAdEntity?): Boolean {
        if (baseDisplayAdEntity !is NativeAdHtml) {
            return true
        }
        if (baseDisplayAdEntity.coolAd?.zipped == false) {
            return true
        }
        return baseDisplayAdEntity.coolAd?.content?.mainFile != null

    }

    private fun canRequestPgiAd(): Boolean {
        return if (requestSent || pgiAdHandlerInfo == null) {
            false
        } else swipeCounter?.swipeCount ?: 0 >= requestSwipeCount && !nofillOccurred || nofillOccurred && swipesAfterRequest >= pgiNoFillRetrySwipeCount
    }

    private fun requestPgiAds(numOfAds: Int, adPosition: AdPosition, activity: Activity?) {
        if (activity == null || baseAdEntity != null || !canRequestPgiAd()) {
            return
        }

        val adRequest = getAdRequest(numOfAds, adPosition, activity)
        if (adRequest == null) {
            requestSent = false
            return
        }

        requestSent = true
        swipesAfterRequest = 0
        if (getAdUsecaseController == null) {
            getAdUsecaseController = GetAdUsecaseController(
                    uiBus, uniqueRequestId)
        }

        getAdUsecaseController?.requestAds(adRequest,
                AdsUpgradeInfoProvider.getInstance().adsUpgradeInfo)
    }

    fun destroy() {
        reset(destroyed = true)
        swipeCounter = null
        if (registered) {
            uiBus.unregister(this)
            registered = false
        }
        getAdUsecaseController?.destroy()
        getAdUsecaseController = null
    }

    private fun getAdRequest(numOfAds: Int, adPosition: AdPosition, activity: Activity?): AdRequest? {
        val contentContext =
            AdsUtil.getContentContextFor(pgiAdHandlerInfo?.adSpec, adPosition.value)

        val requestBodyList = AdsUtil.getAmazonRequestBody(adPosition)

        val apsInfo: ApsInfo? = AdsUpgradeInfoProvider.getInstance()?.adsUpgradeInfo?.amazonSDK

        apsInfo?.let {
            val interstitialInfo = apsInfo.interstitial?.sizes
            // traverse through each size list item
            interstitialInfo?.forEach { item ->
                val zoneInfo = item.data
                // traverse through each adPosition inside size list item
                zoneInfo?.forEach { zoneItem ->
                    val slotInfo = zoneItem.slotInfo
                    val position = zoneItem.adPosition
                    if (position == adPosition.value) {
                        // traverse through each slots present for given adPosition
                        slotInfo?.forEach { slotItem ->
                            val slotUUID = slotItem.slotAdUnitId
                            slotUUID.let { slotId ->
                                AmazonAdFetcher().fetchInterstitialAd(slotId, position)
                            }
                        }
                    }
                }
            }
        }
        return AdRequest(
            adPosition, numOfAds,
            entityId = pgiAdHandlerInfo?.entityId,
            entityType = pgiAdHandlerInfo?.entityType,
            entitySubType = pgiAdHandlerInfo?.entitySubType,
            sourceId = pgiAdHandlerInfo?.sourceId,
            sourceCatId = if (pgiAdHandlerInfo?.sourceId.isNullOrBlank()) null else pgiAdHandlerInfo?.entityId,
            sourceType = pgiAdHandlerInfo?.sourceType,
            contentContextMap = contentContext?.let { mapOf(adPosition.value to it) },
            section = pgiAdHandlerInfo?.section,
            amazonSdkPayload = AmazonSdkPayload(requestBody = requestBodyList),
            activity = activity
        )
    }

    @Subscribe
    fun setAdResponse(nativeAdContainer: NativeAdContainer) {
        if (nativeAdContainer.uniqueRequestId != uniqueRequestId) {
            return
        }
        requestSent = false
        if (CommonUtils.isEmpty(nativeAdContainer.baseAdEntities)) {
            nofillOccurred = true
            return
        }

        nofillOccurred = false
        val baseAdEntity = nativeAdContainer.baseAdEntities?.get(0)
        if (baseAdEntity !is BaseDisplayAdEntity || baseAdEntity.isShown) {
            Logger.e(LOG_TAG, "Discarding PGI ad response")
            return
        }
        val baseDisplayAdEntity = baseAdEntity
        if (baseDisplayAdEntity.adPosition == AdPosition.PGI) {
            this.baseAdEntity = baseDisplayAdEntity
            if (this.baseAdEntity !is EmptyAd) {
                AdBinderRepo.add(baseAdEntity)
            }
            AdsUtil.saveMinSessionsToPersistSwipeCount(baseDisplayAdEntity.sessionCount ?: 0)
            initConfigFromAd(baseDisplayAdEntity)
        }
    }

    private fun initConfigFromAd(baseDisplayAdEntity: BaseDisplayAdEntity) {
        if (isFirstPgiAd) {
            val initialSwipeCountToShowAd = baseDisplayAdEntity.firstAdSwipeCount ?: 0
            pageSwipeCountToShowAd = if (initialSwipeCountToShowAd <= 0)
                DEFAULT_FIRST_SWIPE_COUNT
            else
                initialSwipeCountToShowAd
        } else {
            pageSwipeCountToShowAd = baseDisplayAdEntity.pageSwipeCount?: 0
            pageSwipeCountToShowAd = if (pageSwipeCountToShowAd <= 0)
                DEFAULT_PAGE_SWIPE_COUNT
            else
                pageSwipeCountToShowAd
        }

        // minimum threshold swipe count value should not be greater than default pageswipecount to
        // show ad
        minThresHoldSwipeCount = baseDisplayAdEntity.minThresholdSwipeCount ?: 0
        if (minThresHoldSwipeCount > pageSwipeCountToShowAd) {
            minThresHoldSwipeCount = pageSwipeCountToShowAd
        }

        maxPageSwipeCount = baseDisplayAdEntity.maxPageSwipeCount ?: 0
        if (maxPageSwipeCount <= 0) {
            maxPageSwipeCount = MAX_PAGE_SWIPE_COUNT
        }

        // requestSwipeCount value should be taken from current ad or handshake. Previous value is
        // not to be used.
        requestSwipeCount = AdsUtil.getRequestSwipeCountFromAd(baseDisplayAdEntity, -1)
        if (requestSwipeCount > pageSwipeCountToShowAd) {
            requestSwipeCount = pageSwipeCountToShowAd
        } else if (requestSwipeCount < 0) {
            requestSwipeCount = AdsUtil.getRequestSwipeCountFromHandshake(DEFAULT_REQUEST_SWIPE_COUNT)
        }
    }

    private fun canShowPgiAd(): Boolean {
        return swipeCounter?.let {
            it.swipeCount >= pageSwipeCountToShowAd && it.sessionSwipeCount >= minThresHoldSwipeCount
        } ?: false
    }

    private fun showPgiAd(activity: Activity?, reportAdsMenuListener: ReportAdsMenuListener?, lifecycleOwner: LifecycleOwner?) {
        if (isSwipeablePgiAd) {
            return
        }

        baseAdEntity?.let {
            it.notifyObservers()
            val viewHolder = if (isPopupHTMLPgiAd) {
                val viewDataBinding = DataBindingUtil.inflate<LayoutHtmlFullPageAdBinding>(LayoutInflater.from(activity),
                        R.layout.layout_html_full_page_ad, null, false).apply {
                    setVariable(BR.adReportListener, reportAdsMenuListener)
                }
                val nativeAdHtmlViewHolder = NativeAdHtmlViewHolder(viewDataBinding)
                activity?.let { activity ->
                    nativeAdHtmlViewHolder.updateView(activity, it)
                }
                nativeAdHtmlViewHolder
            } else if (baseAdEntity is ExternalSdkAd) {
                val viewDataBinding = DataBindingUtil.inflate<ExternalAdContainerBinding>(LayoutInflater.from(activity),
                        R.layout.external_ad_container, null, false)
                val externalSdkViewHolder = ExternalSdkViewHolder(viewDataBinding, uniqueRequestId, lifecycleOwner)
                activity?.let { activity ->
                    externalSdkViewHolder.updateView(activity, it)
                }
                externalSdkViewHolder
            } else null
            if (it is EmptyAd) {
                with(AsyncAdImpressionReporter(it)) {
                    onAdInflated()
                    onCardView()
                }
            }
            viewHolder?.onCardView(it)
            reset(activity)
        }
    }

    private fun handleEntityChange(oldInfo: PgiAdHandlerInfo?,
                                   newInfo: PgiAdHandlerInfo?, activity: Activity?) {
        if (oldInfo == null || newInfo == null) {
            return
        }
        if (!oldInfo.entityId.equals(newInfo.entityId, true) ||
                !CommonUtils.equalsIgnoreCase(oldInfo.entityType, newInfo.entityType)) {
            //Clear cached ad make requestSent = false, So that next ad request can be successfully
            // routed to ad engine and matched against contexts.
            baseAdEntity = null
            requestSent = false
            isAdBlockedForEntity = isAdBlocked(newInfo)
        }
        requestPgiAds(1, AdPosition.PGI, activity)
    }

    private fun isAdBlocked(info: PgiAdHandlerInfo): Boolean {
        val allowedZones = mutableSetOf(AdPosition.PGI.value)
        AdsUtil.filterBlockedZones(info.adSpec, allowedZones, info.entityId, LOG_TAG)
        return allowedZones.isEmpty()
    }

    data class PgiAdHandlerInfo(val section: String?, val entityId: String?,
                                val entitySubType: String?, val entityType: String?,
                                val sourceId: String? = null, val sourceType: String? = null,
                                val adSpec: AdSpec? = null)
}
