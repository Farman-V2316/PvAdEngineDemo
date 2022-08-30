/*
* Copyright (c) 2020 Newshunt. All rights reserved.
*/
package com.newshunt.dataentity.ads

import java.io.Serializable

/**
 * @author raunak.yadav
 */
data class FcCounter(
        /**
         * Total impressions for a campaign in a given time slot.
         */
        var actual: Int,
        /**
         * Map of view's uniqueId to the inserted but unseen ad in that view.
         */
        @Transient
        var soft: HashMap<Int, Int> = HashMap(),

        var softCounter: Int = 0) : Serializable {

    fun reset() {
        actual = 0
    }

    fun incSoft(uniqueRequestId: Int) {
        if (soft.containsKey(uniqueRequestId)) {
            soft[uniqueRequestId]?.let {
                soft[uniqueRequestId] = it + 1
            }
        } else {
            soft[uniqueRequestId] = 1
        }
    }

    fun decSoft(uniqueRequestId: Int) {
        soft[uniqueRequestId]?.let {
            if (it > 0) {
                soft[uniqueRequestId] = it - 1
            } else {
                soft.remove(uniqueRequestId)
            }
        }
    }
}