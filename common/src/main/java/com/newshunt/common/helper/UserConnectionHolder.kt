/*
 Copyright (c) 2022 Newshunt. All rights reserved.
 */
package com.newshunt.common.helper

import com.newshunt.common.helper.common.Constants

/**
 * Class to keep track of user connection
 * @author satosh.dhanyamraju
 */
object UserConnectionHolder {
    var exoEstimatedSpeed: Double = 0.0
        private set
    var networkEstimatedSpeed: Double = 0.0
        private set
    var estimatedSpeed: Double = 0.0
        private set
    var userConnectionQuality = Constants.EMPTY_STRING
        private set
    var userConnectionType = Constants.EMPTY_STRING
        private set
    var exoSlidingPercentileMaxWeight: Int = 2000

    fun updateUserConnectionValues(exoEstimatedSpeed: Double,
                                   networkEstimatedSpeed: Double,
                                   estimatedSpeed: Double,
                                   userConnectionQuality: String,
                                   userConnectionType: String) {
        this.exoEstimatedSpeed = exoEstimatedSpeed
        this.networkEstimatedSpeed = networkEstimatedSpeed
        this.estimatedSpeed = estimatedSpeed
        this.userConnectionQuality = userConnectionQuality
        this.userConnectionType = userConnectionType
    }
}