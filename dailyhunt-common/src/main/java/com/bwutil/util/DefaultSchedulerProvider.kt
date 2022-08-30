/*
 Copyright (c) 2022 Newshunt. All rights reserved.
 */

package com.bwutil.util

import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

/**
 * Default implementation uses rx schedulers
 * @author satosh.dhanyamraju
 */
object DefaultSchedulerProvider : SchedulerProvider {
    override fun io(): Scheduler = Schedulers.io()
    override fun ui(): Scheduler = AndroidSchedulers.mainThread()
}