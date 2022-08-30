/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.dhutil.helper

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.OnLifecycleEvent
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.listener.PagerLifecycleObserver
import com.newshunt.dataentity.common.asset.PostEntity
import com.newshunt.dhutil.bundleOf
import com.newshunt.news.model.daos.FetchDao
import com.newshunt.news.model.usecase.MediatorUsecase
import com.newshunt.news.model.usecase.Result0
import com.newshunt.news.model.usecase.TickerRefreshUseCase
import com.newshunt.news.model.usecase.toMediator2
import javax.inject.Inject
import javax.inject.Named

/**
 * @author madhuri.pa
 */

class ActiveTickersUsecase @Inject constructor(  @Named("entityId") private val entityId: String,
                                                 @Named("location") private val location: String,
                                                 @Named("section") private val section: String,
                                                 private val fetchdao: FetchDao): MediatorUsecase<Bundle, List<PostEntity>> {
    lateinit var list : List<PostEntity>

    private val mediator = MediatorLiveData<Result0<List<PostEntity>>>()
    override fun execute(p1: Bundle): Boolean {
        mediator.addSource(fetchdao.tickersOf(entityId, location, section)) {
            if (!::list.isInitialized || list != it) {
                mediator.value = Result0.success(it)
                list = it
            }
        }
        return true
    }
    override fun data(): LiveData<Result0<List<PostEntity>>> = mediator
}

class TickerHelper3 @Inject constructor(tickerRefreshUseCase: TickerRefreshUseCase,
                                        activeTickersUsecase: ActiveTickersUsecase,
                                        lifecycleOwner: LifecycleOwner): PagerLifecycleObserver {

    private val handler = Handler(Looper.getMainLooper())
    private var resumed : Boolean? = null
    private var tickers : List<PostEntity>? = null
    private val TOKEN = "TickerHelper3#MSGS"
    private val tickerRefreshMediatorUsecase = tickerRefreshUseCase.toMediator2()

    init {
        with(activeTickersUsecase) {
            execute(Bundle())
            data().observe(lifecycleOwner, Observer {
                if (it.isSuccess) {
                    tickers = it.getOrNull()
                    Logger.d(LOG_TAG, "observe: got event")
                    reschedule()
                }
            })
        }
        lifecycleOwner.lifecycle.addObserver(this)
    }

    override fun setUserVisibleHint(isVisible: Boolean) {}

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume() {
        Logger.v(LOG_TAG, "onResume:")
        resumed = true
        reschedule()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun onPause() {
        Logger.v(LOG_TAG, "onPause:")
        resumed = false
        reschedule(true)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        tickerRefreshMediatorUsecase.dispose()
    }

    fun reschedule(justCancelAll: Boolean = false) {
        Logger.d(LOG_TAG, "rescheule cancel=$justCancelAll ${tickers?.size}tickers")
        handler.removeCallbacksAndMessages(TOKEN)
        if (justCancelAll) {
            return
        }
        if(isVisibleAndResumed().not()) {
            Logger.d(LOG_TAG, "not visible. ignore.")
            return
        }
        tickers?.forEach {
            val refreshTimeInMillis = it.i_tickerRefreshTime()?.times(1000)
            val nextRefreshTime = it.localLastTickerRefreshTime + (refreshTimeInMillis ?: 0)
            postAt(nextRefreshTime) {
                    tickerRefreshMediatorUsecase.execute(bundleOf(TickerRefreshUseCase.TICKER_URL to it.tickerUrl,
                            TickerRefreshUseCase.TICKER_ID to it.id))
            }
        }
    }

    private fun isVisibleAndResumed(): Boolean {
        return resumed == true
    }

    private  fun postAt(time: Long, f: () -> Unit) {
        if (SystemClock.uptimeMillis() > time) {
            Logger.d(LOG_TAG, "postAt: Running immediately")
            f()
        } else {
            Logger.d(LOG_TAG, "postAt: will run after ${time-SystemClock.uptimeMillis()} ms")
            handler.postAtTime(f, TOKEN, time)
        }
    }

    private val LOG_TAG
        get() = "TickerHelper3[${tickers?.getOrNull(0)?.id}#${tickers?.size}]"
}