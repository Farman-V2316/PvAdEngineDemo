/*
 Copyright (c) 2022 Newshunt. All rights reserved.
 */

package com.bwutil.util

/*
 *  Copyright (c) 2015, Facebook, Inc.
 *  All rights reserved.
 *
 *  This source code is licensed under the BSD-style license found in the
 *  LICENSE file in the root directory of this source tree. An additional grant
 *  of patent rights can be found in the PATENTS file in the same directory.
 *
 */
/**
 * Moving average calculation taken from facebook ConnectionClass.
 *  @author satosh.dhanyamraju
 */
class ExponentialGeometricAverage(private val mDecayConstant: Double) {
    private val mCutover: Int
    var average = -1.0
        private set
    private var mCount = 0

    /**
     * Adds a new measurement to the moving average.
     * @param measurement - Bandwidth measurement in bits/ms to add to the moving average.
     */
    fun addMeasurement(measurement: Double) {
        val keepConstant = 1 - mDecayConstant
        if (mCount > mCutover) {
            average = Math.exp(keepConstant * Math.log(average) + mDecayConstant * Math.log(measurement))
        } else if (mCount > 0) {
            val retained = keepConstant * mCount / (mCount + 1.0)
            val newcomer = 1.0 - retained
            average = Math.exp(retained * Math.log(average) + newcomer * Math.log(measurement))
        } else {
            average = measurement
        }
        mCount++
    }

    /**
     * Reset the moving average.
     */
    fun reset() {
        average = -1.0
        mCount = 0
    }

    init {
        mCutover = if (mDecayConstant == 0.0) Int.MAX_VALUE else Math.ceil(1 / mDecayConstant).toInt()
    }
}