/*
 * Copyright (c) 2020 Newshunt. All rights reserved.
 */
package com.newshunt.dhutil.helper

import android.os.Bundle
import com.newshunt.common.helper.preference.GenericAppStatePreference
import com.newshunt.dataentity.model.entity.Action
import com.newshunt.dataentity.model.entity.ActionableNotiPayload

/**
 * Created by karthik.r on 03/07/20.
 */
abstract class NotificationActionExecutionService {
    abstract fun handleActionableNotification(data: Bundle, payloadPram: ActionableNotiPayload?)

    abstract fun checkAndExecuteAction(action: Action)
    abstract fun executePendingAction(preference: GenericAppStatePreference)
}

object NotificationActionExecutionServiceImpl {

    var instane: NotificationActionExecutionService? = null

    fun getInstance() : NotificationActionExecutionService? {
        return instane
    }

    fun setInstance(instanceParam: NotificationActionExecutionService) {
        this.instane = instanceParam
    }
}