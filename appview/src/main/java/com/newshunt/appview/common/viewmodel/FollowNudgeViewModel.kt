/*
 * Copyright (c) 2020 Newshunt. All rights reserved.
 */

package com.newshunt.appview.common.viewmodel

import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.common.model.entity.EventsInfo
import com.newshunt.deeplink.navigator.CommonNavigator
import com.newshunt.dhutil.helper.preference.AppStatePreference
import com.newshunt.news.model.usecase.CommunicationEventUsecase
import com.newshunt.news.model.usecase.onlyData
import com.newshunt.news.model.usecase.toMediator2
import java.util.concurrent.TimeUnit

/**
 * Detail pages having follow button will use it to fetch and show nudges
 *
 * @author satosh.dhanyamraju
 *
 */
class FollowNudgeViewModel public constructor(): ViewModel() {
    private val events = with(CommunicationEventUsecase().toMediator2()) {
        execute("")
        onlyData()
    }

    private val curTime
        get() = System.currentTimeMillis()

    fun nudges(referer: PageReferrer, followed: Boolean, bundle: Bundle?): LiveData<List<EventsInfo>> {
        val mediator = MediatorLiveData<List<EventsInfo>>()
        mediator.addSource(events) {
            val events = it?.events?.filter { info ->
                if (info.activity?.type == NUDGE_DETAIL) {
                    val lastNudgeShown = PreferenceManager.getPreference(AppStatePreference.FOLLOW_NUDGE_LAST_SHOWN, 0L)
                    val minGap = info.precondition?.get("gapSec")?.toLong()?.let { sec ->
                        TimeUnit.SECONDS.toMillis(sec)
                    } ?: -1
                    val minTimePassed = (curTime - lastNudgeShown > minGap &&
                            !PreferenceManager.getPreference(AppStatePreference.NUDGE_SHOWN_IN_CURRENT_LAUNCH, false))
                    val sectionMatched = bundle?.getBoolean(ENABLE_NUDGES, false) == true
                    val followStatematched = info.precondition?.get("followed") == followed.toString()
                    minTimePassed && sectionMatched && followStatematched
                } else false
            }
            if (mediator.value != events) mediator.value = events
        }
        return mediator
    }

    fun nudgeShown(nudgeId: Int) {
        PreferenceManager.savePreference(AppStatePreference.FOLLOW_NUDGE_LAST_SHOWN, curTime)
        PreferenceManager.savePreference(AppStatePreference.NUDGE_SHOWN_IN_CURRENT_LAUNCH, true)
    }

    private fun sectionOf(referer: PageReferrer, isDeeplink: Boolean): String {
        // TODO(satosh.dhanyamraju): handle discovery
        return if ((CommonNavigator.isFromDeeplink(referer)) || isDeeplink) "discovery" else "app"
    }

    companion object {
        const val NUDGE_DETAIL = "nudge_detail"
        private const val ENABLE_NUDGES = "enableNudges"
    }
}