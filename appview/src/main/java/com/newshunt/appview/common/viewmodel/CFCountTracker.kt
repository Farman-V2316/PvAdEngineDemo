/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.appview.common.viewmodel

import androidx.lifecycle.Lifecycle
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger
import com.newshunt.dhutil.editValueAt

/**
 * - Tracks no. of CardsFragment alive and connected to a particular fetchinfo
 * - Not a lifecycle observer as it needs more information. see [event]
 * - Intended usage : One instance per application; so cleanup not required
 * - This should be in-memory rather than DB because, if process gets killed, DB would have inconsistent state.
 *
 * @author satosh.dhanyamraju
 */
class CFCountTracker {
    private val LOG_TAG = "CFCountTracker"
    private val map = hashMapOf<String, Int>()

    fun event(state: Lifecycle.State, entityId: String, location: String, section: String) {
/*
         Except in home, Each instance of cardsfragment gets it own fetchinfo because, `location`
         would be different. So, we need this logic on fragments hosted by Home (they have hard coded
         location 'list'; and hence might share fetchinfo
*/
        if (location != Constants.FETCH_LOCATION_LIST) return

        val key = key(entityId, location, section)

        when (state) {
            Lifecycle.State.CREATED -> map.editValueAt(key) {
                it?.inc() ?: 1
            }
            Lifecycle.State.DESTROYED -> map.editValueAt(key) {
                it?.dec()?.coerceAtLeast(0)
            }

        }
        Logger.d(LOG_TAG, "event: $state, $map")
    }


    fun count(entityId: String, location: String, section: String) = map.get(key(entityId, location, section))

    private fun key(entityId: String, location: String, section: String) = "$entityId-$location-$section"

    companion object {
        val INST = CFCountTracker()
    }
}