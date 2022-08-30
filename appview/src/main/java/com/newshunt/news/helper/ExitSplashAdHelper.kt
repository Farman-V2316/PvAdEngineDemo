/*
 * Copyright (c) 2022 Newshunt. All rights reserved.
 */
package com.newshunt.news.helper

import android.content.Intent
import com.newshunt.adengine.client.AsyncAdImpressionReporter
import com.newshunt.adengine.client.NativeAdInventoryManager
import com.newshunt.adengine.domain.controller.GetAdUsecaseController
import com.newshunt.adengine.model.entity.BaseAdEntity
import com.newshunt.adengine.model.entity.EmptyAd
import com.newshunt.adengine.model.entity.NativeAdContainer
import com.newshunt.adengine.model.entity.version.AdPosition
import com.newshunt.adengine.model.entity.version.AdRequest
import com.newshunt.adengine.model.entity.version.AdUIType
import com.newshunt.adengine.model.entity.version.AmazonSdkPayload
import com.newshunt.adengine.util.AdLogger
import com.newshunt.adengine.util.AdsUtil
import com.newshunt.adengine.view.helper.ExitSplashAdCommunication
import com.newshunt.appview.common.ui.helper.NavigationEvent
import com.newshunt.appview.common.ui.helper.NavigationHelper
import com.newshunt.common.helper.common.BusProvider
import com.newshunt.common.helper.common.Constants
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.common.asset.AdDisplayType
import com.newshunt.dhutil.helper.AdsUpgradeInfoProvider
import com.squareup.otto.Subscribe

/**
 * @author raunak.yadav
 */
class ExitSplashAdHelper(private val referrer: PageReferrer?) {

    private val uiBus = BusProvider.getUIBusInstance()
    private var busRegistered: Boolean = false
    private var requestInProgress: Boolean = false
    private var getAdUsecase: GetAdUsecaseController = GetAdUsecaseController(uiBus, uniqueRequestId)
    private var adEntity: BaseAdEntity? = null

    fun start() {
        if (busRegistered) {
            return
        }
        AdLogger.d(TAG, "Start : $uniqueRequestId")
        uiBus.register(this)
        busRegistered = true
    }

    fun requestAd() {
        if (requestInProgress) {
            AdLogger.d(TAG, "Exit-splash Ad request in progress. Return")
            return
        }
        if (AdsUpgradeInfoProvider.getInstance().adsUpgradeInfo?.splashExitConfig?.isEnabled != true) {
            AdLogger.d(TAG, "Exit-splash Ad is disabled. Return")
            return
        }
        if (adEntity?.isShown == false) {
            AdLogger.d(TAG, "Exit-splash Ad Present. Return")
            return
        }
        AdLogger.d(TAG, "exit-splash request made")
        requestInProgress = true
        val adRequest = getAdRequest(AdPosition.EXIT_SPLASH, referrer)
        getAdUsecase.requestAds(adRequest)
        ExitSplashAdCommunication.exitAdRequested = true
        return
    }

    private fun getAdRequest(adPosition: AdPosition, referrer: PageReferrer?): AdRequest {
        val requestBodyList = AdsUtil.getAmazonRequestBody(adPosition)
        AdsUtil.makeAmazonAdRequest(adPosition)

        return AdRequest(
            adPosition,
            numOfAds = 1,
            pageReferrer = referrer,
            referrerId = referrer?.id,
            amazonSdkPayload = AmazonSdkPayload(requestBody = requestBodyList)
        )
    }

    @Subscribe
    fun setAdResponse(nativeAdContainer: NativeAdContainer) {
        if (nativeAdContainer.uniqueRequestId != uniqueRequestId ||
            nativeAdContainer.adPosition != AdPosition.EXIT_SPLASH) {
            return
        }
        requestInProgress = false
        if (nativeAdContainer.baseAdEntities.isNullOrEmpty()) {
            return
        }

        nativeAdContainer.baseAdEntities?.get(0)?.let { ad ->
            if (AdsUtil.isValidExitSplashAd(ad)) {
                AdLogger.d(TAG,
                    "[${nativeAdContainer.adPosition}]Ad response received : ${ad.type} " +
                            "Evergreen : ${ad.isEvergreenAd}")
                ad.parentIds.add(uniqueRequestId)
                adEntity = ad
            } else {
                AdLogger.d(TAG,
                    "[${nativeAdContainer.adPosition}] Invalid Ad response received : ${ad.type} " +
                            "Evergreen : ${ad.isEvergreenAd}")
            }
        }
    }

    fun stop() {
        AdLogger.d(TAG, "on stop: $uniqueRequestId")
        if (busRegistered) {
            busRegistered = false
            uiBus.unregister(this)
        }
        getAdUsecase.destroy()
    }

    fun canRenderExitSplashAd(): BaseAdEntity? {
        if (adEntity == null || adEntity?.isEvergreenAd == true) {
            // Some ads that came after timeout might be available in store.
            NativeAdInventoryManager.getExitSplashInstance()?.getAd(AdRequest(AdPosition
                .EXIT_SPLASH))?.let { ad ->
                ad.parentIds.add(uniqueRequestId)
                adEntity = ad
                AdLogger.d(TAG, "canRenderExitSplashAd Found ad : ${adEntity?.type}")
            }
        }
        AdLogger.d(TAG, "canRenderExitSplashAd ${adEntity?.type} Evergreen : ${adEntity?.isEvergreenAd}")
        return adEntity?.let { ad ->
            if (AdsUpgradeInfoProvider.getInstance().adsUpgradeInfo?.splashExitConfig?.isEnabled != true) {
                AdLogger.d(TAG, "Exit-splash Ad is disabled. Return")
                adEntity = null
                return null
            }
            when (AdsUtil.getCardTypeForAds(ad)) {
                -1 -> return null
                AdDisplayType.EMPTY_AD.index -> {
                    AdLogger.d(TAG, "Empty ad received ${ad.isEvergreenAd}")
                    if (ad is EmptyAd) {
                        with(AsyncAdImpressionReporter(ad)) {
                            onAdInflated()
                            onCardView()
                        }
                    }
                    return null
                }
            }

            // Move to full screen mode and setup a blur bg.
            val isBlurNeeded = ad.displayType == AdUIType.MINI_SCREEN && !AdsUtil.isExternalPopUpAd(ad)
            val adIntent = Intent(Constants.EXIT_SPLASH_AD_ACTION)
            adIntent.putExtra(Constants.BUNDLE_AD_EXTRA, ad)
            adIntent.putExtra(Constants.BUNDLE_AD_UI_WITH_BLUR, isBlurNeeded)
            adIntent.putExtra(Constants.BUNDLE_ACTIVITY_REFERRER, referrer)
            NavigationHelper.navigationLiveData.postValue(NavigationEvent(adIntent))
            return@let adEntity
        }
    }

    fun destroy() {
        adEntity = null
    }

    companion object {
        private const val TAG = "ExitSplashAdHelper"
        private const val uniqueRequestId = 1999
    }
}