/*
 Copyright (c) 2022 Newshunt. All rights reserved.
 */

package com.bwutil.util

import io.reactivex.Scheduler

/**
 * Abstraction over Rx schedulers, to enable swapping them for unit tests
 * @author satosh.dhanyamraju
 */
interface SchedulerProvider {
    fun io(): Scheduler
    fun ui(): Scheduler
}