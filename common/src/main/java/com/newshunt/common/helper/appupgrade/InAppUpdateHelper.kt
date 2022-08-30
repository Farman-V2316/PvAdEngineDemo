/*
 * Copyright (c) 2021 Newshunt. All rights reserved.
 */
package com.newshunt.common.helper.appupgrade

import android.app.Activity
import androidx.lifecycle.LiveData

/**
 * Pojos and interfaces related to in app updates to be defined in this class
 *
 * Created by srikanth.ramaswamy on 03/01/2021.
 */
const val USER_ACTION_TYPE_SKIP = "skip"
const val USER_ACTION_TYPE_UPGRADE = "upgrade"
const val USER_ACTION_TYPE_EXIT = "exit"

/**
 * Interface to interact with the implementation of in app updates
 */
interface InAppUpdateHelper {
    fun getInAppUpdateAvailability(): LiveData<InAppUpdateAvailability>
    fun checkInAppUpdate()
    fun startUpdate(activity: Activity, requestCode: Int, updateType: UpdateType)
    fun continueUpdate(activity: Activity, requestCode: Int)
    fun userCancelledUpdate()
}

/**
 * Singleton to inject the implementation of in app updates to lower layer modules
 */
object InAppUpdateHelperProvider {
    var inAppUpdateHelper: InAppUpdateHelper? = null
}

/**
 * Enum to communicate with the activities using Livedata
 */
enum class InAppUpdateAvailability {
    NO_UPDATE_AVAILABLE,
    FLEXIBLE_UPDATE_AVAILABLE,
    MANDATORY_UPDATE_AVAILABLE,
    UPDATE_IN_PROGRESS
}

/**
 * A wrapper enum to differentiate flexible and immediate update
 */
enum class UpdateType {
    FLEXIBLE_UPDATE,
    MANDATORY_UPDATE
}