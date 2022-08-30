/*
* Copyright (c) 2020 Newshunt. All rights reserved.
*/
package com.newshunt.adengine.util

import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.notification.helper.DHWorkManager
import java.util.concurrent.TimeUnit

/**
 * @author raunak.yadav
 */
class AdCampaignsSyncScheduler(private val workTag: String) {


    fun cancelScheduledWork() {
        if(CommonUtils.workManagerInitFailed) return
        DHWorkManager.cancelWork(workTag)
    }

    fun scheduleWork(nextScheduledDuration: Long, shouldReplaceExistingWork: Boolean) {
        if(CommonUtils.workManagerInitFailed) return
        AdLogger.d(TAG, "Schedule Ad campaign pull work after $nextScheduledDuration millis")
        DHWorkManager.beginUniqueWork(
                createWork(nextScheduledDuration), workTag, if (shouldReplaceExistingWork) ExistingWorkPolicy.REPLACE else ExistingWorkPolicy.KEEP)
    }

    private fun createWork(nextScheduledDuration: Long): OneTimeWorkRequest {
        val constraintsBuilder: Constraints.Builder = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)

        return OneTimeWorkRequest.Builder(AdCampaignsSyncWorker::class.java)
                .setInitialDelay(nextScheduledDuration, TimeUnit.MILLISECONDS)
                .setConstraints(constraintsBuilder.build())
                .addTag(workTag)
                .build()
    }
}

private const val TAG = "AdCampaignsSyncScheduler"