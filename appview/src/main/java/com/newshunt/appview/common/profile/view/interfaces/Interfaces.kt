/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.appview.common.profile.view.interfaces

import android.content.Intent

/**
 * All view specific interfaces to be defined here
 * <p>
 * Created by srikanth.ramaswamy on 05/28/2019.
 */

interface ProfileFlow {
    fun launchActivity(intent: Intent)
    fun launchNewsHome()
}
