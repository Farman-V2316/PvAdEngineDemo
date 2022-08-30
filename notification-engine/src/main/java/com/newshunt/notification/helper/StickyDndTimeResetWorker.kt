/*
 *  Copyright (c) 2022 Newshunt. All rights reserved.
 */
package com.newshunt.notification.helper

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.dataentity.notification.util.NotificationConstants
import com.newshunt.dhutil.helper.preference.AppStatePreference
import com.newshunt.notification.model.manager.StickyNotificationsManager
import java.lang.Exception
import java.util.concurrent.TimeUnit

/**
 * Worker to clear dndTime time for news-sticky, can be extended to other stickies in future with some modifications
 *
 * Created by atul.anand on 20/04/22.
 */
const val WORKER_ID = "StickyDndTimeResetWorker_WorkerId"
class StickyDndTimeResetWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    override fun doWork(): Result {
        try{
            Logger.d(WORKER_ID, "started work")
            StickyNotificationsManager.userOptedIn(NotificationConstants.NEWS_STICKY_OPTIN_ID, NotificationConstants.STICKY_NEWS_TYPE)
            Logger.d(WORKER_ID, "finished work")
        }catch(ex: Exception){
            Logger.caughtException(ex)
        }

        return Result.success()
    }
}

object StickyDndTimeResetHelper {
    fun scheduleStickyDndTimeResetWork(dndEndTime: Long){
        Logger.d(WORKER_ID, "scheduling work")
        val delay = dndEndTime - System.currentTimeMillis()
        val workRequestBuilder = OneTimeWorkRequestBuilder<StickyDndTimeResetWorker>().addTag(WORKER_ID)

        if(delay > 0){
            workRequestBuilder.setInitialDelay(delay, TimeUnit.MILLISECONDS)
        }

        val workRequest = workRequestBuilder.build()
        DHWorkManager.beginUniqueWork(workRequest, WORKER_ID, ExistingWorkPolicy.REPLACE)
        Logger.d(WORKER_ID, "scheduled work")
    }

    fun cancelDndTimeResetWork(){
        Logger.d(WORKER_ID, "cancelling work")
        DHWorkManager.cancelUniqueWork(WORKER_ID)
    }
}