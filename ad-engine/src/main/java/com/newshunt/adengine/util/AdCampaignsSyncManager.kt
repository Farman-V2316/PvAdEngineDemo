/*
* Copyright (c) 2020 Newshunt. All rights reserved.
*/
package com.newshunt.adengine.util

import com.newshunt.adengine.usecase.FetchAdCampaignsUsecase
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dhutil.helper.AdsUpgradeInfoProvider
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Trigger pull of ad campaigns configs for FCaps.
 *
 * @author raunak.yadav
 */
object AdCampaignsSyncManager {

    private val workScheduler = AdCampaignsSyncScheduler(AdConstants.AD_CAMPAIGN_PULL_WORK_TAG)
     private var syncInProgress: AtomicBoolean = AtomicBoolean(false)

    @JvmStatic
    fun syncAdCampaigns(fetchUsecase: FetchAdCampaignsUsecase,
                        inForeground: Boolean): Disposable? {
        if (syncInProgress.get()) {
            return null
        }
        syncInProgress.set(true)
        //Tell the framework explicitly that the work for this work cycle has completed and there is
        // no need to reschedule on failure
        workScheduler.cancelScheduledWork()

        if (!AdCampaignsSyncHelper.isCampaignSyncEnabled(inForeground)) {
            AdLogger.d(TAG, "Ads campaign pull disabled. Fg : $inForeground")
            syncInProgress.set(false)
            return null
        }
        if (!AdCampaignsSyncHelper.hasSyncWindowCompleted(inForeground)) {
            AdLogger.d(TAG, "Ads campaign pull window not expired yet. Fg : $inForeground")
            val syncWindow = AdCampaignsSyncHelper.getRemainingTimeToSync(inForeground)
            if (syncWindow >= 0) {
                workScheduler.scheduleWork(syncWindow, true)
            }
            syncInProgress.set(false)
            return null
        }
        AdLogger.d(TAG, "Triggering Ads campaigns sync. Fg : $inForeground")
        val syncUrl = AdsUpgradeInfoProvider.getInstance().adsUpgradeInfo?.fetchCampaignDataUrl
        if (syncUrl.isNullOrBlank()) {
            syncInProgress.set(false)
            return null
        }
        return fetchUsecase.invoke(syncUrl)
                .subscribeOn(Schedulers.io())
                .subscribe({
                    AdLogger.d(TAG, "Ads campaign data n/w fetch success")
                    onSyncComplete(true)
                }, {
                    AdLogger.d(TAG, "Ads campaign data n/w fetch failed. ${it.message}")
                    onSyncComplete(false)
                })
    }

    private fun onSyncComplete(success: Boolean) {
        syncInProgress.set(false)
        if (success) {
            AdCampaignsSyncHelper.persistSyncTime(System.currentTimeMillis())
        }
        val syncWindow = AdCampaignsSyncHelper.getSyncWindowInMillis(CommonUtils.isInFg)
        workScheduler.scheduleWork(syncWindow, true)
    }
}

private const val TAG = "AdCampaignsSyncManager"