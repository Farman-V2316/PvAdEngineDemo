package com.newshunt.news.util

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.newshunt.common.helper.common.BusProvider
import com.newshunt.common.helper.common.Logger
import com.newshunt.dataentity.analytics.entity.NhAnalyticsEvent
import com.squareup.otto.Subscribe

/*
* Helper class to avoid duplicate event getting triggered for each update on card.
* */
class EventDedupHelper(private val params: Map<String, String?>) : LifecycleObserver {
    private val eventTracker: MutableSet<EventKey> = mutableSetOf()

    fun reset() {
        eventTracker.clear()
    }

    fun fireEvent(eventKey: EventKey, trigger: Runnable) {
        if (eventTracker.contains(eventKey)) {
            return
        }
        eventTracker.add(eventKey)
        trigger.run()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun onPause() {
        Logger.i(LOG_TAG, "Deregister event dedup helper")
        BusProvider.getRestBusInstance().unregister(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume() {
        Logger.i(LOG_TAG, "Register event dedup helper")
        BusProvider.getRestBusInstance().register(this)
    }

    @Subscribe
    fun onResetEvent(resetEvent: ResetDedupHelperEvent) {
        if (params == resetEvent.params) {
            Logger.i(LOG_TAG, "Resetting event dedup helper $resetEvent")
            reset()
        }
    }

}

data class EventKey(
        val eventName: NhAnalyticsEvent,
        val params: Map<String, Any?>
)

data class ResetDedupHelperEvent(val params: Map<String, String>)

private const val LOG_TAG = "EventDedupHelper"