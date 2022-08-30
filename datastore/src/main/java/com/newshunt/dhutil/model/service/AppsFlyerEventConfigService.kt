/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */

package com.newshunt.dhutil.model.service

import com.newshunt.dataentity.dhutil.model.entity.appsflyer.AppsFlyerEventsConfigResponse

import io.reactivex.Observable

/**
 * Versioned API interface for fetching AppsFlyer Events config
 * <p>
 * Created by srikanth.ramaswamy on 09/17/2018.
 */
interface AppsFlyerEventConfigService {
    fun getEventConfig() : Observable<AppsFlyerEventsConfigResponse>
    fun getEventConfigLocal()
    fun resetVersion()
}
