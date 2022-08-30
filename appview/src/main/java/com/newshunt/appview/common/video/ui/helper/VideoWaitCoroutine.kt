package com.newshunt.appview.common.video.ui.helper

import androidx.lifecycle.*
import com.newshunt.common.helper.common.Logger
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

/**
 * Coroutine context that automatically is cancelled when UI is destroyed
 */
class VideoWaitCoroutine(val lifecycleOwner: LifecycleOwner, val position: Int) :
        CoroutineScope, LifecycleObserver {

    private var job: Job = Job()
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main

    companion object {
        val TAG: String = "VideoWaitScope"
    }

    val jobComplete = MutableLiveData<Boolean>()

    init {
        lifecycleOwner.lifecycle.addObserver(this)
    }

    val timesUp = MutableLiveData<Boolean>()

    fun runDelay(delayTime: Long) {
        Logger.d(TAG, "$position - runDelay $delayTime")
        launch(Dispatchers.Main) {
            delay(delayTime)
            Logger.d(TAG, "$position - runJon After ${job.isActive}")
            if (job.isActive) {
                jobComplete.value = true
            }
        }
    }

    fun cancelJob() {
        Logger.d(TAG, "$position - Job cancelled")
        job.cancel()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun onPause() {
        Logger.d(TAG, "$position - ON_PAUSE")
        cancelJob()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onStop() {
        Logger.d(TAG, "$position - ON_STOP")
        cancelJob()
    }
}