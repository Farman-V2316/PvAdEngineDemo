/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.socialfeatures.presenter

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import com.newshunt.news.util.AuthOrchestrator
import javax.inject.Inject

/**
 * Observers passed [lifecycleOwner] and calls start/stop on [AuthOrchestrator]
 */
class LifecycleAwareAuth @Inject constructor(
        lifecycleOwner: LifecycleOwner,
        val authOrchestrator: AuthOrchestrator) : LifecycleObserver {

    init {
        lifecycleOwner.lifecycle.addObserver(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun start() {
        authOrchestrator.start()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun destroy() {
        authOrchestrator.stop()
    }
}