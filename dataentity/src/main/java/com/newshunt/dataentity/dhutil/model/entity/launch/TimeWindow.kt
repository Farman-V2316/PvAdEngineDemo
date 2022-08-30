/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.dataentity.dhutil.model.entity.launch

/**
 * POJO to hold the time window for applaunch rules
 * <p>
 * Created by srikanth.ramaswamy on 02/06/2019.
 */
const val TIME_23_59_HOURS_MS = 86396400L
const val TIME_00_00_HOURS_MS = 0L

data class TimeWindow(var startTimeMs: Long = 0L,
                      var endTimeMs: Long = 0L,
                      val id: String? = null) {
    init {
        startTimeMs = restrictTimeLimits(startTimeMs)
        endTimeMs = restrictTimeLimits(endTimeMs)
    }

    /**
     * Make sure the the timestamps are within TIME_00_00_HOURS_MS and TIME_23_59_HOURS_MS
     */
    private fun restrictTimeLimits(time: Long): Long {
        return when {
            time < TIME_00_00_HOURS_MS -> TIME_00_00_HOURS_MS
            time > TIME_23_59_HOURS_MS -> TIME_23_59_HOURS_MS
            else -> time
        }
    }

    override fun toString(): String {
        return "{startTimeMs = ${millisToString(startTimeMs)}, endTimeMs = ${millisToString(endTimeMs)}, id: $id}"
    }

    private fun millisToString(millis: Long): String {
        val seconds = (millis / 1000).toInt() % 60
        val minutes = (millis / (1000 * 60) % 60)
        val hours = (millis / (1000 * 60 * 60) % 24)
        return "$hours:$minutes:$seconds"
    }
}