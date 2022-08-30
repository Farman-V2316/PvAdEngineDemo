/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */

package com.newshunt.dataentity.dhutil.model.entity.adupgrade

import com.newshunt.common.helper.common.Constants

/**
 * POJO for SelfServiceConfig configuration received in Ads Handshake
 *
 * Created by srikanth.ramaswamy on 06/02/2018.
 */
data class SelfServiceConfig(val ssUrl: String,
                             val ssEnabled: Boolean,
                             val ssTitle: String,
                             val useInternalBrowser: String,
                             val useWideViewPort: Boolean?,
                             val clearHistoryOnPageLoad: Boolean?) {
    constructor() : this(Constants.EMPTY_STRING, false, Constants.EMPTY_STRING,
            Constants.EMPTY_STRING, true, true)
}