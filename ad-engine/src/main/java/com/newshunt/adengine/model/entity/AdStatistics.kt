/*
* Copyright (c) 2019 Newshunt. All rights reserved.
*/
package com.newshunt.adengine.model.entity

import java.io.Serializable
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Keep stats about todays ads to be sent in adRequest.
 *
 * @author raunak.yadav
 */
data class AdStatistics(@Transient private var daysFirstTimestamp: Long,
                   var totalSessions: Int,
                   var totalAdSessions: Int,
                   var totalSeenAds: Int,
                   var mastheadAdCount: Int) : Serializable {

    @Transient
    var currentSessionId: String? = null
    @Transient
    var adsSeenInCurrentSession: Boolean = false

    var sessionSource: String? = null

    fun isExpired(): Boolean {
        val cal = Calendar.getInstance()
        if (cal.timeInMillis - daysFirstTimestamp >= TimeUnit.HOURS.toMillis(24)) {
            return true
        }
        val today = Calendar.getInstance()
        today.timeInMillis = daysFirstTimestamp

        return cal.get(Calendar.DATE) != today.get(Calendar.DATE)
    }

    /**
     * Reset Ad Stats.
     * To be done when queried on a different date
     */
    fun reset() {
        daysFirstTimestamp = System.currentTimeMillis()

        currentSessionId = null
        sessionSource = null
        adsSeenInCurrentSession = false

        totalSessions = 0
        totalAdSessions = 0
        totalSeenAds = 0
        mastheadAdCount = 0
    }
}
