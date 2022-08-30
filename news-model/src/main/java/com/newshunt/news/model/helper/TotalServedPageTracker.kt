/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */

package com.newshunt.news.model.helper

import com.newshunt.common.helper.common.BusProvider
import com.newshunt.dataentity.common.model.entity.DoubleBackExitEvent
import com.newshunt.dhutil.toPathOrSelf
import com.squareup.otto.Subscribe
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Interceptor that maintains count of every successful news-list response.
 * Should be used only by APIs that are requesting news-lists.
 * Counts are saved against URL path. Cleared on double-back exit, soft/hard reset.
 *
 * @author satosh.dhanymaraju
 */

object TotalServedPageTracker : Interceptor {
    private val requestCount = hashMapOf<String, Int>()

    init {
        BusProvider.getUIBusInstance().register(this)
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        if (response.isSuccessful) {
            val path = response.request().url().url().path?:""
            with(requestCount.getOrPut(path){ 0 }){
                requestCount[path] = inc()
            }
        }
        return response
    }

    @JvmStatic
    fun clear() = requestCount.clear()

    @Subscribe
    fun onAppExit(appExitEvent: DoubleBackExitEvent) = clear()

    @JvmStatic
    operator fun get(urlOrPath: String) = requestCount[urlOrPath.toPathOrSelf()] ?: 0
}