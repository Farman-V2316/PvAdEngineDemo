/*
* Copyright (c) 2019 Newshunt. All rights reserved.
*/
package com.newshunt.adengine.handshake.usecase

import com.newshunt.adengine.AdEngineGateway
import com.newshunt.adengine.handshake.network.AdsConfigApi
import com.newshunt.adengine.util.AdCampaignsSyncHelper
import com.newshunt.adengine.util.AdCampaignsSyncManager
import com.newshunt.adengine.util.EvergreenAdsFetcher
import com.newshunt.adengine.util.EvergreenAdsHelper
import com.newshunt.adengine.util.OMSdkHelper
import com.newshunt.common.helper.common.ApiResponseUtils
import com.newshunt.common.helper.common.BusProvider
import com.newshunt.common.helper.common.JsonUtils
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.common.PasswordEncryption
import com.newshunt.common.helper.common.PasswordEncryptionUtil
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.dhutil.model.entity.adupgrade.AdsUpgradeInfo
import com.newshunt.dataentity.dhutil.model.entity.status.CurrentAdProfile
import com.newshunt.dhutil.helper.AdsUpgradeInfoProvider
import com.newshunt.dhutil.helper.preference.AdsPreference
import com.newshunt.news.model.usecase.Usecase
import io.reactivex.Observable

/**
 * Provides ability to perform periodic handshake with the ads server.
 * Currently invoked only on success of App handshake. So adding no scheduling.
 *
 * @author raunak.yadav
 */
class AdsHandshakeUsecase (private val adsApi: AdsConfigApi): Usecase<CurrentAdProfile, AdsUpgradeInfo?> {
    private val TAG = "AdsHandshakeUsecase"

    override fun invoke(currentAdProfile: CurrentAdProfile): Observable<AdsUpgradeInfo?> {
        //TODO: PANDA: for testing, don't need to post data to API since it's a mock API
        var forTesting = true
        if (forTesting) {
            return adsApi.performTestAdsHandshake()
                .map {
                    ApiResponseUtils.throwErrorIfDataNull(it)
                    processHandshake(it.data)
                    it.data
                }
        } else {
            return adsApi.performAdsHandshake(currentAdProfile)
                .map {
                    ApiResponseUtils.throwErrorIfDataNull(it)
                    processHandshake(it.data)
                    it.data
                }
        }
    }

    private fun processHandshake(adsUpgradeInfo: AdsUpgradeInfo) {
        val adsUpgradeInfoJson = JsonUtils.toJson(adsUpgradeInfo)
        if (CommonUtils.isEmpty(adsUpgradeInfoJson)) {
            Logger.d(TAG, "Failure in parsing response to json. Return")
            // PANDA: new
//            HandshakeScheduler.onHandshakeError()
            return
        }

        val oldInfo = AdsUpgradeInfoProvider.getInstance().adsUpgradeInfo
        AdsUpgradeInfoProvider.getInstance().adsUpgradeInfo = adsUpgradeInfo

        if (oldInfo == null || oldInfo.isDataCollectionEnabled != adsUpgradeInfo.isDataCollectionEnabled) {
            // If data collection state has changed, post an event.
            //PANDA: removed
//            BusProvider.postOnUIBus(DataCollectionStateChangeEvent(adsUpgradeInfo.isDataCollectionEnabled))
//            DeviceDataUtil.toggleReceiverState(adsUpgradeInfo.isDataCollectionEnabled)
        }

        adsUpgradeInfo.encryption?.let {
            val isValidKey = PasswordEncryption.validateAndPersistForAds(it.key, it.version)
            Logger.d(TAG, "onNext: encryption: isValid: $isValidKey")
            if (!isValidKey) {
                PasswordEncryptionUtil.logger?.logEvent("adsUpgradeInfo-invalid-key-received",
                        it.key, it.version)
            }
        }

        if (adsUpgradeInfo.omSdkConfig?.isEnabled == false) {
            OMSdkHelper.enableOMSdk(false)
            PreferenceManager.remove(AdsPreference.OMID_SERVICE_JS)
        } else if (oldInfo?.omSdkConfig == null || oldInfo.omSdkConfig != adsUpgradeInfo.omSdkConfig) {
            // If omSdk config has changed, download the new script.
            OMSdkHelper.enableOMSdk(true)
            OMSdkHelper.fetchJS(adsUpgradeInfo.omSdkConfig)
        }

        adsUpgradeInfo.amazonSDK?.let {
            AdEngineGateway.initializeAmazonSDK()
        }

        PreferenceManager.savePreference(AdsPreference.ADS_HANDSHAKE_RESPONSE_JSON, adsUpgradeInfoJson)
        PreferenceManager.savePreference(AdsPreference.ADS_CONFIG_VERSION, adsUpgradeInfo.version)
        PreferenceManager.savePreference(AdsPreference.CARD_P0_REFRESH_ENABLED, adsUpgradeInfo.isCardP0Refresh)
        PreferenceManager.savePreference(AdsPreference.CARD_P1_NO_FILL_RETRY_DISTANCE, adsUpgradeInfo
                .cardP1NoFillRetryDistance)
        PreferenceManager.savePreference(AdsPreference.AD_ZIPPED_HTML_CACHE_COUNT, adsUpgradeInfo.zippedHtmlAdCacheCount)

        adsUpgradeInfo.buzzAd?.let {
            PreferenceManager.savePreference(AdsPreference.VIDEO_AD_DISTANCE, it.adDistance)
            PreferenceManager.savePreference(AdsPreference.VIDEO_INITIAL_AD_OFFSET, it.adInitialOffset)
        }

        adsUpgradeInfo.immersiveView?.let {
            PreferenceManager.savePreference(AdsPreference.IMMERSIVE_VIEW_TRANSITION_SPAN, it.immersiveTransitionSpan)
            PreferenceManager.savePreference(AdsPreference.IMMERSIVE_VIEW_DISTANCE, it.immersiveViewDistance)
            PreferenceManager.savePreference(AdsPreference.IMMERSIVE_VIEW_REFRESH_TIME, it.companionRefreshTime)
        }

        if (adsUpgradeInfo.evergreenAds?.enabled != true) {
            EvergreenAdsHelper.clearAllEvergreenData()
        }

        // Trigger FC campaign fetch.
        AdCampaignsSyncHelper.getFetchCampaignUsecase()?.let {
            AdCampaignsSyncManager.syncAdCampaigns(it, inForeground = CommonUtils.isInFg)
        }
        // Trigger Evergreen ads fetch.
        EvergreenAdsHelper.getEvergreenAdsUsecase()?.let {
            EvergreenAdsFetcher.fetchEgAds(it)
        }
        BusProvider.postOnUIBus(adsUpgradeInfo)

        // PANDA: new
//        HandshakeScheduler.handshakeCompleted()
    }
}
