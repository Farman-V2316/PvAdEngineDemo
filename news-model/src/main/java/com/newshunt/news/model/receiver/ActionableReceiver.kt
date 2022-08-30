/*
 * Copyright (c) 2020 Newshunt. All rights reserved.
 */
package com.newshunt.news.model.receiver

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.preference.GenericAppStatePreference
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.model.entity.Action
import com.newshunt.dataentity.model.entity.ActionableNotiPayload
import com.newshunt.dataentity.model.entity.SchedulingCondition
import com.newshunt.news.model.helper.NotificationActionExecutionHelper

/**
 * Created by karthik.r on 28/06/20.
 */
abstract class ActionableReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        // Do not persist or post actionable notifications
        NotificationActionExecutionHelper.handleActionableNotification(Bundle.EMPTY, ActionableNotiPayload())
    }

    abstract fun getReceiverAction() : SchedulingCondition
}

class BootCompletedActionableReceiver: ActionableReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
        val pm = CommonUtils.getApplication().getPackageManager();
        pm.setComponentEnabledSetting(ComponentName(CommonUtils.getApplication(),
                BootCompletedActionableReceiver::class.java),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP)

        val actionStr : String  = PreferenceManager.getPreference(GenericAppStatePreference
                .BOOT_COMPLETED_ACTION, Constants.EMPTY_STRING)
        PreferenceManager.remove(GenericAppStatePreference.BOOT_COMPLETED_ACTION)
        if (!CommonUtils.isEmpty(actionStr)) {
            val type = object : TypeToken<Action>() {}.type
            val action = Gson().fromJson(actionStr, type) as Action?
            if (action != null) {
                NotificationActionExecutionHelper.checkAndExecuteAction(action)
            }
        }
    }

    override fun getReceiverAction(): SchedulingCondition {
        return SchedulingCondition.ON_NEXT_BOOT_COMPLETED
    }
}