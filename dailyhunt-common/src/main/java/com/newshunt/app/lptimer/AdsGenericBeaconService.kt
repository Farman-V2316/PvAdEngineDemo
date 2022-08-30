/*
* Copyright (c) 2021 Newshunt. All rights reserved.
*/
package com.newshunt.app.lptimer

/**
 * @author Mukesh Yadav
 */
interface AdsGenericBeaconService {
    fun updateAdsGenericBeacon(requestUrl: String, playload: Map<String, String>)
}

enum class TimeSpentOnLPExitReason {
    APP_KILL,
    BACK_NAVIGATION,
    THRESHOLD_TIME
}