/*
 Copyright (c) 2022 Newshunt. All rights reserved.
 */

package com.bwutil.entity

import androidx.room.DatabaseView
import androidx.room.Embedded

/**
 * This intermediate construct is used to feed input to ExponentialGeometricAverage
 * @author satosh.dhanyamraju
 */
@DatabaseView(viewName = "view_hourly_mean", value = "SELECT ts, day, hour, networkName, avg(bitrate) bitrate  FROM bw_bitrates GROUP by day, hour, networkName ORDER by ts")
class BwHourlyMean (
    @Embedded val bw: BwBitrates
)