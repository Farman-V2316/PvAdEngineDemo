/*
* Copyright (c) 2020 Newshunt. All rights reserved.
*/
package com.newshunt.adengine

import com.amazon.device.ads.AdRegistration
import com.amazon.device.ads.DTBAdNetwork
import com.amazon.device.ads.DTBAdNetworkInfo
import com.amazon.device.ads.MRAIDPolicy
import com.newshunt.adengine.client.NativeAdInventoryManager
import com.newshunt.adengine.handshake.helper.AdsVersionApiHelper
import com.newshunt.adengine.usecase.FetchAllAdFcDataUsecase
import com.newshunt.adengine.util.AdCampaignsSyncHelper
import com.newshunt.adengine.util.AdCampaignsSyncManager
import com.newshunt.adengine.util.AdConstants
import com.newshunt.adengine.util.AdFrequencyStats
import com.newshunt.adengine.util.AdLogger
import com.newshunt.adengine.util.OMSdkHelper
import com.newshunt.common.helper.common.Logger
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dhutil.helper.AdsUpgradeInfoProvider
import com.newshunt.news.model.sqlite.SocialDB
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

/**
 * Initializer code for ad-engine
 *
 * @author raunak.yadav
 */
class AdEngineGateway {

    companion object {

        @JvmStatic
        fun initialize() {

            // Fetch Frequency Cap data for ad campaigns.
            populateAdFCData()

            //initialize inventories of early zones to trigger fetch of persisted ads from DB.
            val adsUpgradeInfo = AdsUpgradeInfoProvider.getInstance().adsUpgradeInfo
            if (adsUpgradeInfo?.evergreenAds?.enabled == true) {
                NativeAdInventoryManager.getEvergreenCacheInstance()?.readPersistedAds()
            }
            NativeAdInventoryManager.getSplashInstance()
            NativeAdInventoryManager.getP0Instance()
            NativeAdInventoryManager.getStoryInstance()

        }

        @JvmStatic
        fun lazyInitialize(){
            OMSdkHelper.init()

            // Trigger FC campaign fetch.
            AdCampaignsSyncHelper.getFetchCampaignUsecase()?.let {
                AdCampaignsSyncManager.syncAdCampaigns(it, inForeground = CommonUtils.isInFg)
            }
            val adsUpgradeInfo = AdsUpgradeInfoProvider.getInstance().adsUpgradeInfo
            adsUpgradeInfo?.amazonSDK?.let {
                initializeAmazonSDK()
            }
            /**
             * Steps to enable amazon test ads in charles :
             * Select Tools > Rewrite
             * On the rewrite settings window, check "Enable rewrite"
             * Select "Add"
             * Input : `/e/msdk/ads` in Path column in location window
             * In rewrite window, select type as body
             * Add `isDTBMobile` in Match value part
             * Add `isDTBMobile": "true", "isTest` as value in Replace part
             * select ok
             */
            // PANDA: new added
            AdsVersionApiHelper.performHandshake()

        }

        private fun populateAdFCData(): Disposable {
            return FetchAllAdFcDataUsecase(SocialDB.instance().adFrequencyCapDao()).invoke(Unit)
                    .subscribeOn(Schedulers.io())
                    .subscribe({
                        AdFrequencyStats.loadFCData(it)
                        AdLogger.d("AdCampaignsFetch", "Ads FC data loaded from DB")
                    }, {
                        AdLogger.d("AdCampaignsFetch", "Ads FC data load failed. ${it.message}")
                    })
        }

        fun initializeAmazonSDK() {
            if (!AdRegistration.isInitialized()) {
                AdRegistration.getInstance(AdConstants.APP_KEY, CommonUtils.getApplication().applicationContext)
                AdRegistration.enableLogging(Logger.loggerEnabled()) // Enables APS debug logging
                AdRegistration.setMRAIDSupportedVersions(arrayOf("1.0", "2.0", "3.0"))
                AdRegistration.setMRAIDPolicy(MRAIDPolicy.CUSTOM)
                AdRegistration.setAdNetworkInfo(DTBAdNetworkInfo(DTBAdNetwork.OTHER))
            }
        }
    }
}