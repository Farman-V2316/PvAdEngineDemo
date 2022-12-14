/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.dhutil

import com.newshunt.dataentity.common.asset.CommonAsset

/**
 * Used as a wrapper for data that is exposed via a LiveData that represents an event.
 * Taken from https://medium.com/androiddevelopers/livedata-with-snackbar-navigation-and-other-events-the-singleliveevent-case-ac2622673150
 *
 */
open class LiveEvent<out T>(private val content: T) {

    var hasBeenHandled = false
        private set // Allow external read but not write

    /**
     * Returns the content and prevents its use again.
     */
    fun getContentIfNotHandled(): T? {
        return if (hasBeenHandled) {
            null
        } else {
            hasBeenHandled = true
            content
        }
    }

    /**
     * Returns the content, even if it's already been handled.
     */
    fun peekContent(): T = content
}

/**
 * @author satosh.dhanyamraju
 */
class RepostLiveEvent(item: CommonAsset) : LiveEvent<CommonAsset>(item)