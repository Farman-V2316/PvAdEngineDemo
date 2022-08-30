/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */
package com.newshunt.dataentity.dhutil.model.entity.multiprocess

import com.newshunt.common.helper.common.Constants

/**
 * Created by karthik.r on 11/12/18.
 */
data class MultiProcessConfigurationResponse(val version: String,
                                             val enableMultiProcessHandling: Boolean,
                                             val rows: List<MultiProcessConfig> = ArrayList()) {
    constructor() : this(Constants.EMPTY_STRING, false)
}

data class MultiProcessConfig(val manufacturer: String, val apiVersionStart: Int,
                              val apiVersionEnd: Int, val isEnabled: Boolean,
                              val killProcessBGDuration: Int, val killProcessFGDuration: Int) {
    constructor() : this(Constants.EMPTY_STRING, 0, 0, false, 0, 0)
}