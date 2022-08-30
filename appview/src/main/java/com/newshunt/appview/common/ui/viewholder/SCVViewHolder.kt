/*
 * Copyright (c) 2020 Newshunt. All rights reserved.
 */
package com.newshunt.appview.common.ui.viewholder

import android.os.SystemClock
import android.text.TextUtils
import android.view.View
import com.newshunt.adengine.model.entity.ContentAdDelegate
import com.newshunt.analytics.entity.NhAnalyticsAppEvent
import com.newshunt.app.analytics.UiEventsPersistentHelper
import com.newshunt.appview.common.ui.adapter.UpdateableCardView
import com.newshunt.appview.common.ui.adapter.VideoPrefetchCallback
import com.newshunt.common.helper.common.BusProvider
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.dataentity.analytics.entity.AnalyticsParam
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.common.asset.CommonAsset
import com.newshunt.dataentity.common.asset.Format
import com.newshunt.dataentity.common.helper.common.SCVEvent
import com.newshunt.dataentity.social.entity.TopLevelCard
import com.newshunt.dataentity.viral.model.entity.UiEvent
import com.newshunt.dhutil.analytics.AnalyticsHelper2
import com.newshunt.dhutil.helper.preference.AppStatePreference
import com.newshunt.news.model.repo.CardSeenStatusRepo
import com.newshunt.viral.utils.visibility_utils.VisibilityAwareViewHolder

/**
 * @author shrikant.agrawal
 */
abstract class SCVViewHolder(
    open val view: View,
    open val uniqueScreenId: Int,
    open val section: String,
    open val referrer: PageReferrer?,
    open val prefetchListener: VideoPrefetchCallback? = null
) : CardsViewHolder(view),
    UpdateableCardView, VisibilityAwareViewHolder {

    companion object {
        const val LOG_TAG = "SCViewHolder"
        const val VIDEO_LOAD_TIME = "VIDEO_LOAD_TIME"
    }

    private val minViewVisibilityForTS: Int =
        PreferenceManager.getPreference(AppStatePreference.MIN_VIEW_VISIBILITY_FOR_TS, Constants.DEFAULT_MIN_VIEW_VISIBLE_FOR_TS)
    private val minViewVisibilityForSCV: Int =
        PreferenceManager.getPreference(AppStatePreference.MIN_VIEW_VISIBILITY_FOR_SCV, Constants.DEFAULT_MIN_VIEW_VISIBLE_FOR_SCV)
    private val minScreenVisibilityForTS:Float =
        PreferenceManager.getPreference(AppStatePreference.MIN_SCREEN_VISIBILITY_FOR_TS, Constants.DEFAULT_MIN_SCREEN_VISIBLE_FOR_TS)
    private val minScreenVisibilityForSCV:Float =
        PreferenceManager.getPreference(AppStatePreference.MIN_SCREEN_VISIBILITY_FOR_SCV, Constants.DEFAULT_MIN_SCREEN_VISIBLE_FOR_SCV)

    private var visiblePercentage = 0
    private var screenVisiblePc = 0f
    private var isTracking = false
    protected var isSCVFired = false
    private var startTime = 0L
    var analyticsItem: CommonAsset? = null
    var cardPosition: Int = 0
    var contentAdDelegate: ContentAdDelegate? = null
    protected var videoLoadTime: Long = 0L

    override fun onInVisible() {
        // do nothing handled in the lifecycle event
    }

    override fun onUserEnteredFragment(viewVisibilityPercentage: Int, percentageOfScreen: Float) {
        // do nothing handled in the lifecycle event
    }

    override fun onUserLeftFragment() {
        // do nothing handled in the lifecycle event
    }

    override fun onVisible(viewVisibilityPercentage: Int, percentageOfScreen: Float) {
        visiblePercentage = viewVisibilityPercentage
        screenVisiblePc = percentageOfScreen
        contentAdDelegate?.onCardView(adAdapterPosition = adapterPosition)
        checkForSCV()
        checkForTimespent(false)
    }

    private fun checkForSCV() {
        if (visiblePercentage >= minViewVisibilityForSCV || screenVisiblePc >= minScreenVisibilityForSCV) {
            if (isSCVFired) {
                Logger.d(LOG_TAG,  "SCVFired for cardPosition $cardPosition")
                return
            } else {
                Logger.d(LOG_TAG,  "Firing SCV for cardPosition $cardPosition")
                postEvent(0L)
                isSCVFired = true
            }
            prefetchListener?.onCardVisibility(cardPosition, null)
        }

    }

    private fun checkForTimespent(checkForSCV: Boolean) {
        if (visiblePercentage >= minViewVisibilityForTS || screenVisiblePc >= minScreenVisibilityForTS) {
            startTimespent()
        } else {
            if (checkForSCV) {
                checkForSCV()
            }
            stopTimespent()
        }
    }

    private fun startTimespent() {

        if (isTracking) {
            return
        }
        startTime = SystemClock.elapsedRealtime()
        isTracking = true
    }

    private fun stopTimespent() {
        if (!isTracking) {
            return
        }
        isTracking = false
        val timespentEvent = SystemClock.elapsedRealtime() - startTime
        if (timespentEvent < Constants.MINIMUM_TIME_SPENT_FOR_PV) {
            return
        }
        postEvent(timespentEvent)
    }

    fun postEvent(timeSpent: Long)  {
        analyticsItem?.let {
            val dynamicMap: MutableMap<String, String> = if (it.i_experiments() != null) {
                val map = mutableMapOf<String, String>()
                map.putAll(it.i_experiments()!!)
                map
            } else mutableMapOf()
            val nhParams = AnalyticsHelper2.getParamsForCardSeenEvent(it, mutableMapOf(),
                    cardPosition,null, null, timeSpent, true, false, referrer)
            val loadTime = getVideoLoadTime()
            if(!TextUtils.isEmpty(loadTime)) {
                nhParams[AnalyticsParam.INITIAL_LOAD_TIME.getName()] = loadTime
            }
            val bufferTime = getVideoBufferTime()
            if(!TextUtils.isEmpty(bufferTime)) {
                nhParams[AnalyticsParam.BUFFER_TIME_MS.getName()] = bufferTime
            }
            val uiEvent = UiEvent(eventId = it.i_id(),
                uid = uniqueScreenId.toString(),
                event = NhAnalyticsAppEvent.STORY_CARD_VIEW.name,
                section = section,
                nhParams = nhParams,
                dynamicParams = dynamicMap)
            UiEventsPersistentHelper.postEvent(uiEvent)
            CardSeenStatusRepo.DEFAULT.markSeen(it.i_id())
            val postEntity = it.rootPostEntity()
            BusProvider.getUIBusInstance().post(SCVEvent(postEntity))
        }
    }

    fun onResumeCb() {
        checkForTimespent(true)
        UiEventsPersistentHelper.onActivityResumed(uniqueScreenId)
    }

    fun onStopCb() {
        stopTimespent()
        UiEventsPersistentHelper.onActivityPaused(uniqueScreenId)
    }

    fun onDestroyCb() {
        UiEventsPersistentHelper.onActivityDestroyed(uniqueScreenId)
    }

    open fun getVideoBufferTime(): String {
        return ""
    }

    open fun getVideoLoadTime(): String {
        return ""
    }
}