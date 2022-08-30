/*
 * Copyright (c) 2020 Newshunt. All rights reserved.
 */
package com.newshunt.common.helper.common

import com.squareup.otto.Bus
import com.squareup.otto.ThreadEnforcer

/**
 * Wrapper over the Otto Bus class to catch Exceptions thrown in register and unregister methods
 * <p>
 * Created by srikanth.ramaswamy on 06/01/2020.
 */
class DHBus : Bus {
    constructor() : super()
    constructor(enforcer: ThreadEnforcer) : super(enforcer)

    override fun register(`object`: Any?) {
        try {
            super.register(`object`)
        } catch (ex: IllegalArgumentException) {
            Logger.caughtException(ex)
        }
    }

    override fun unregister(`object`: Any?) {
        try {
            super.unregister(`object`)
        } catch (ex: IllegalArgumentException) {
            Logger.caughtException(ex)
        }
    }
}