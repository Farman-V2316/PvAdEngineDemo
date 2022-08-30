package com.newshunt.dataentity

/**
 * Created by karthik.r on 2019-08-26.
 */

/**
 * Function to chronologically compare the time represented by the Long Values
 */
fun Long?.chronologicallyCompareTo(other: Long?): Int {
    this ?: return 1
    other ?: return 1

    return when {
        this == other -> 0
        this > other -> -1 //because it is chronological compare
        else -> 1
    }
}

