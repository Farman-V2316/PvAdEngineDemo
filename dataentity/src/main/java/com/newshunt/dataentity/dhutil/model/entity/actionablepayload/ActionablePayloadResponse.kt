/*
 * Copyright (c) 2020 Newshunt. All rights reserved.
 */
package com.newshunt.dataentity.dhutil.model.entity.actionablepayload

import com.newshunt.common.helper.common.Constants
import com.newshunt.dataentity.model.entity.ActionableNotiPayload

/**
 * Created by karthik.r on 03/07/20.
 */
data class ActionablePayloadResponse(val version: String,
                                     val configuration: ActionableNotiPayload = ActionableNotiPayload()) {
    constructor() : this(Constants.EMPTY_STRING, ActionableNotiPayload())
}
