/*
* Copyright (c) 2020 Newshunt. All rights reserved.
*/
package com.newshunt.adengine.util

import com.newshunt.adengine.interceptor.AdCampaignModifiedTsHeaderInterceptor
import com.newshunt.adengine.usecase.CampaignApi
import com.newshunt.adengine.usecase.FetchAdCampaignsUsecase
import com.newshunt.adengine.usecase.UpdateAdCampaignsUsecase
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.common.model.retrofit.RestAdapterContainer
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dhutil.helper.AdsUpgradeInfoProvider
import com.newshunt.dhutil.helper.preference.AdsPreference
import com.newshunt.news.model.sqlite.SocialDB
import com.newshunt.sdk.network.Priority
import java.util.concurrent.TimeUnit

/**
 * Helper methods for syncing ad campaigns info.
 *
 * @author raunak.yadav
 */
object AdCampaignsSyncHelper {
    private const val TAG = "AdCampaignsSyncHelper"

    private fun getCampaignSyncApi(): CampaignApi? {
        val syncUrl = AdsUpgradeInfoProvider.getInstance().adsUpgradeInfo?.fetchCampaignDataUrl
                ?: return null
        AdLogger.d(TAG, "Ad campaign sync url : $syncUrl")
        return RestAdapterContainer.getInstance().getDynamicRestAdapterRx(CommonUtils.formatBaseUrlForRetrofit(syncUrl),
                Priority.PRIORITY_NORMAL, null, AdCampaignModifiedTsHeaderInterceptor()).create(CampaignApi::class.java)
    }

    fun getFetchCampaignUsecase(): FetchAdCampaignsUsecase? {
        return getCampaignSyncApi()?.let { api ->
            FetchAdCampaignsUsecase(api, UpdateAdCampaignsUsecase(SocialDB.instance().adFrequencyCapDao()))
        }
    }

    fun isCampaignSyncEnabled(inForeground: Boolean): Boolean {
        return AdsUpgradeInfoProvider.getInstance().adsUpgradeInfo?.let {
            !it.fetchCampaignDataUrl.isNullOrBlank() &&
                    (if (inForeground) it.campaignSyncFgIntervalInMinutes else it.campaignSyncBgIntervalInMinutes) ?: -1L > 0
        } ?: false
    }

    fun getSyncWindowInMillis(inForeground: Boolean): Long {
        return AdsUpgradeInfoProvider.getInstance().adsUpgradeInfo?.let {
             (if (inForeground) it.campaignSyncFgIntervalInMinutes else it.campaignSyncBgIntervalInMinutes)?.let { mins ->
                TimeUnit.MINUTES.toMillis(mins)
            }
        } ?: -1L
    }

    fun getRemainingTimeToSync(inForeground: Boolean): Long {
        val lastTs = PreferenceManager.getPreference(AdsPreference.AD_CAMPAIGN_FETCH_LAST_TS, 0L)
        if (lastTs == 0L) return 0L

        val syncWindow = getSyncWindowInMillis(inForeground)
        if (syncWindow < 0) return -1L

        val time = lastTs + syncWindow - System.currentTimeMillis()
        return if (time < 0) 0L else time
    }

    fun hasSyncWindowCompleted(inForeground: Boolean): Boolean {
        val lastTs = PreferenceManager.getPreference(AdsPreference.AD_CAMPAIGN_FETCH_LAST_TS, 0L)
        val syncWindow = getSyncWindowInMillis(inForeground)
        return syncWindow > 0 && (lastTs == 0L || CommonUtils.isTimeExpired(lastTs, syncWindow))
    }

    fun persistSyncTime(syncTs: Long) {
        PreferenceManager.savePreference(AdsPreference.AD_CAMPAIGN_FETCH_LAST_TS, syncTs)
    }
}