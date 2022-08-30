/*
* Copyright (c) 2020 Newshunt. All rights reserved.
*/
package com.newshunt.adengine.util

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.newshunt.common.helper.common.Logger
import com.newshunt.dataentity.common.helper.common.CommonUtils

/**
 * Worker to trigger ad campaigns api sync.
 *
 * @author raunak.yadav
 */
class AdCampaignsSyncWorker(context: Context, private val workerParameters: WorkerParameters)
    : Worker(context, workerParameters) {

    override fun doWork(): Result {
        AdLogger.d("AdCampaignsSyncWorker", "Ad campaigns pull work invoked")

        try {
            AdCampaignsSyncHelper.getFetchCampaignUsecase()?.let {
                AdCampaignsSyncManager.syncAdCampaigns(it, inForeground = CommonUtils.isInFg)
            }
        } catch (e: Exception) {
            Logger.caughtException(e)
        }
        return Result.success()
    }
}