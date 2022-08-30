/*
* Copyright (c) 2022 Newshunt. All rights reserved.
*/
package com.newshunt.adengine.util

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.newshunt.adengine.PersistAdUsecase
import com.newshunt.adengine.RemovePersistedAdUsecase
import com.newshunt.adengine.client.NativeAdInventoryManager
import com.newshunt.adengine.interceptor.AdEtagHeaderInterceptor
import com.newshunt.adengine.model.entity.AdCustomDeserializer
import com.newshunt.adengine.model.entity.BaseDisplayAdEntity
import com.newshunt.adengine.usecase.EvergreenAdsApi
import com.newshunt.adengine.usecase.FetchEvergreenAdsUsecase
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.common.model.retrofit.RestAdapterContainer
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.dhutil.model.entity.adupgrade.EvergreenAdsConfig
import com.newshunt.dhutil.helper.AdsUpgradeInfoProvider
import com.newshunt.dhutil.helper.preference.AdsPreference
import com.newshunt.dhutil.helper.preference.UserPreferenceUtil
import com.newshunt.news.model.sqlite.SocialDB
import com.newshunt.news.model.usecase.toMediator2
import com.newshunt.sdk.network.Priority

/**
 * Helper methods for getting evergreen ads config
 *
 * @author raunak.yadav
 */
object EvergreenAdsHelper {
    private const val TAG = "EvergreenAdsHelper"

    private fun getEvergreenAdsApi(): EvergreenAdsApi? {
        val url = AdsUpgradeInfoProvider.getInstance().adsUpgradeInfo?.evergreenAds?.endpoint
            ?: return null
        AdLogger.d(TAG, "Evergreen Ads url : $url")
        return RestAdapterContainer.getInstance()
            .getDynamicRestAdapterRx(CommonUtils.formatBaseUrlForRetrofit(url),
                Priority.PRIORITY_NORMAL, null, gson(), AdEtagHeaderInterceptor())
            .create(EvergreenAdsApi::class.java)
    }

    private fun gson(): Gson {
        return GsonBuilder().registerTypeAdapter(BaseDisplayAdEntity::class.java,
            AdCustomDeserializer()).create()
    }

    fun getEvergreenAdsUsecase(): FetchEvergreenAdsUsecase? {
        return getEvergreenAdsApi()?.let { api ->
            FetchEvergreenAdsUsecase(api, PersistAdUsecase(SocialDB.instance().adsDao()),
                RemovePersistedAdUsecase(SocialDB.instance().adsDao()).toMediator2())
        }
    }

    fun areAdsEnabled(): Boolean {
        return AdsUpgradeInfoProvider.getInstance().adsUpgradeInfo?.evergreenAds?.enabled ?: false
    }

    fun persistSyncTime(syncTs: Long) {
        PreferenceManager.savePreference(AdsPreference.AD_EVERGREEN_FETCH_LAST_TS, syncTs)
    }

    fun clearPersistedETag() {
        PreferenceManager.remove(AdsPreference.AD_EVERGREEN_API_E_TAG)
    }

    fun clearAllEvergreenData() {
        AdLogger.d(TAG, "Clear all data related to Evergreen ads.")
        EvergreenSplashUtil.clear()
        clearPersistedETag()
        NativeAdInventoryManager.getEvergreenCacheInstance()?.let {
            it.clearInventory()
            it.deletePersistedAds()
        }
    }

    fun canHitEvergreenApi(): Boolean {
        // Do not hit before language is selected.
        if (UserPreferenceUtil.getUserPrimaryLanguage().isNullOrBlank()) {
            AdLogger.d(TAG, "User language not selected. Can't hit api")
            return false
        }

        //Do not hit if minimum delay has not passed yet.
        val delay = AdsUpgradeInfoProvider.getInstance().adsUpgradeInfo?.evergreenAds?.apiFetchDelay
            ?: return false
        val lastFetchTs =
            PreferenceManager.getPreference(AdsPreference.AD_EVERGREEN_FETCH_LAST_TS, 0L)
        return CommonUtils.isTimeExpired(lastFetchTs, delay * 1000)
    }

    fun getTimeoutValue(evergreenAdsConfig: EvergreenAdsConfig? = null): Long? {
        return evergreenAdsConfig?.let { config ->
            val isRegularUser = config.isRegUser ?: true
            return config.substituteTimeout?.let {
                if (AdStatisticsHelper.adStatistics.totalSeenAds == 0) {
                    if (isRegularUser) it.regularUser?.firstImpressionMS else it.thinUser?.firstImpressionMS
                } else {
                    if (isRegularUser) it.regularUser?.impressionMS else it.thinUser?.impressionMS
                }
            }
        }
    }

    fun getRetryDelay(): Long? {
        val delay = AdsUpgradeInfoProvider.getInstance().adsUpgradeInfo?.evergreenAds?.retryOnFailureAfterTimeInSec ?: -1L
        return if (delay < 0) null else delay
    }
}