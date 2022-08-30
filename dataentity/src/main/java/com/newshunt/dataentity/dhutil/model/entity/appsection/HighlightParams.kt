/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */

package com.newshunt.dataentity.dhutil.model.entity.appsection

import java.io.Serializable

/**
 * Created by karthik.r on 14/11/18.
 */
class HighlightParams : Serializable {
    var color: String? = null
    var colorNight: String? = null
    var rippleCount: Int = 10 // Default value
    var rippleDuration: Long = 1000 // Default value per cycle
}