/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.appview.common.profile.helper

import com.newshunt.appview.common.profile.model.internal.rest.MigrationStatusAPI
import com.newshunt.appview.common.profile.model.internal.service.MigrationStatusServiceImpl
import com.newshunt.common.helper.common.AndroidUtils
import com.newshunt.common.helper.common.ApplicationStatus
import com.newshunt.common.helper.common.BusProvider
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.info.MigrationStatusProvider
import com.newshunt.common.model.retrofit.RestAdapterContainer
import com.newshunt.dataentity.common.model.entity.DoubleBackExitEvent
import com.newshunt.dhutil.helper.interceptor.NewsListErrorResponseInterceptor
import com.newshunt.dhutil.helper.retrofit.NewsBaseUrlContainer
import com.newshunt.sdk.network.Priority
import com.newshunt.sso.model.helper.interceptor.HTTP401Interceptor
import com.squareup.otto.Subscribe
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

/**
 * A MigrationStatusHelper which does a periodic check on migration API
 * Created by srikanth.ramaswamy on 07/19/2019.
 */
private const val LOG_TAG = "MigrationStatusHelper"
object MigrationStatusHelper {
    private val scheduler by lazy {
        AndroidUtils.newScheduledThreadExecutor(1, LOG_TAG)
    }

    private val migrationStatusService by lazy {
        MigrationStatusServiceImpl(migrationStatusAPI())
    }

    private var scheduledFuture: ScheduledFuture<*>? = null
    private var isRegistered = false

    /**
     * Perform an API call to check migrationStatus and reschedule the calls after delay dictated
     * by the response. Stop scheduling once migration is done!
     */
    fun performMigrationStatusCheck() {
        MigrationStatusProvider.migrationState?.let {
            registerOnUIBus()
            migrationStatusService.checkMigrationState()
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.io())
                    .subscribe({
                        if (it.userMigrationCompleted == true) {
                            //Once migration is done, clear the status and no need to schedule any further
                            MigrationStatusProvider.updateMigrationStatus(null)
                            scheduledFuture = null
                        } else {
                            //Migration not done yet! Schedule after a delay of nextPingDelaySecs
                            scheduleNextMigrationJob(it.nextPingDelaySecs)
                        }
                    }, {})
        }
    }

    @Subscribe
    fun onDoubleBackToExit(exit: DoubleBackExitEvent) {
        //While exiting the app, cancel the scheduledFuture
        shutdown()
    }

    fun shutdown() {
        scheduledFuture?.let {
            it.cancel(false)
            Logger.d(LOG_TAG, "Cancelling migration checks")
            scheduledFuture = null
            unRegisterUIBus()
        }
    }

    private fun scheduleNextMigrationJob(delay: Long?) {
        //Schedule the migration status API only if app is in foreground.
        if (ApplicationStatus.getVisibleActiviesCount() > 0) {
            delay?.let {
                Logger.d(LOG_TAG, "Scheduling next migration check in $delay secs")
                scheduledFuture = scheduler.schedule(MigrationStatusJob(), delay, TimeUnit.SECONDS)
            }
        }
    }

    private fun registerOnUIBus() {
        AndroidUtils.getMainThreadHandler().post {
            BusProvider.getUIBusInstance().register(this)
            isRegistered = true
        }
    }

    private fun unRegisterUIBus() {
        AndroidUtils.getMainThreadHandler().post {
            try {
                if (isRegistered) {
                    BusProvider.getUIBusInstance().unregister(this)
                }
            } catch (ex: Exception) {
                Logger.caughtException(ex)
            }
        }
    }

    private fun migrationStatusAPI(): MigrationStatusAPI {
        val profileBaseUrl = NewsBaseUrlContainer.getUserServiceSecuredBaseUrl()
        return RestAdapterContainer.getInstance().getRestAdapter(profileBaseUrl,
                Priority.PRIORITY_LOW,
                null,
                NewsListErrorResponseInterceptor(),
                HTTP401Interceptor())
                .create(MigrationStatusAPI::class.java)
    }
}

class MigrationStatusJob: Runnable {
    override fun run() {
        MigrationStatusHelper.performMigrationStatusCheck()
    }
}