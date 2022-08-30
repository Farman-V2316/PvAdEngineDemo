/*
 * Copyright (c) 2020 Newshunt. All rights reserved.
 */
package com.newshunt.news.model.helper

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.os.Parcelable
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.newshunt.analytics.client.AnalyticsClient
import com.newshunt.analytics.entity.NhAnalyticsAppEvent
import com.newshunt.common.helper.common.AndroidUtils
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.JsonUtils
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.preference.GenericAppStatePreference
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.dataentity.analytics.entity.NhAnalyticsAppEventParam
import com.newshunt.dataentity.analytics.entity.NhAnalyticsEventParam
import com.newshunt.dataentity.analytics.section.NhAnalyticsEventSection
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.model.entity.Action
import com.newshunt.dataentity.model.entity.ActionDef
import com.newshunt.dataentity.model.entity.ActionableNotiPayload
import com.newshunt.dataentity.model.entity.SchedulingCondition
import com.newshunt.dataentity.notification.util.NotificationConstants
import com.newshunt.dhutil.helper.NotificationActionExecutionService
import com.newshunt.news.model.receiver.BootCompletedActionableReceiver
import com.newshunt.notification.analytics.NhNotificationAnalyticsUtility
import com.newshunt.notification.model.entity.NotificationFilterType
import com.newshunt.notification.model.entity.NotificationInvalidType
import com.newshunt.notification.sqlite.NotificationDB
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.io.Serializable

/**
 * Framework to execute actions specified by actionable payload.
 *
 * Created by karthik.r on 26/06/20.
 */
object NotificationActionExecutionHelper : NotificationActionExecutionService() {

    private val TAG = "NotiActionExeHelper"

    override fun handleActionableNotification(data: Bundle, payloadPram: ActionableNotiPayload?) {
        val payload = (if (payloadPram == null) {
            val actionablePayloadStr = data.getString(NotificationConstants.MESSAGE)
            if (CommonUtils.isEmpty(actionablePayloadStr)) {
                // Do not handle actions from pull framework
                null
            }
            else {
                val type = object : TypeToken<ActionableNotiPayload>() {}.type
                val payloadFramed: ActionableNotiPayload = Gson().fromJson(actionablePayloadStr, type) as ActionableNotiPayload
                payloadFramed
            }
        }
        else {
            payloadPram
        }) ?: return notficationDiscarded()

        val map = HashMap<NhAnalyticsEventParam, Any>()
        val actionIds = ArrayList<String>()
        payload.actions?.forEach {
            it.actionId?.let { it1 -> actionIds.add(it1) }
        }

        map[NhAnalyticsAppEventParam.LIST_ACTION_ID] = actionIds.toString()
        AnalyticsClient.logDynamic(NhAnalyticsAppEvent.NOTIFICATION_DELIVERED,
                NhAnalyticsEventSection.NOTIFICATION, map, null, false)

        val actionsMap: HashMap<String, ActionDef> = HashMap()
        payload.actionDefs?.forEach {
            if (it.actionId != null) {
                actionsMap[it.actionId!!] = it
            }
        }

        payload.actions?.forEach {
            expandedPayload(it, actionsMap)
        }

        val actions = payload.actions
        actions?.forEach { action ->
            checkAndExecuteAction(action)
        }
    }

    private fun notficationDiscarded() {
        NhNotificationAnalyticsUtility.deployNotificationFilteredEvent(
                NotificationFilterType.INVALID, NotificationInvalidType.PARSING_ACTION_FAILED.type)
    }

    override fun executePendingAction(preference: GenericAppStatePreference) {
        val actionStr = PreferenceManager.getPreference(preference, Constants.EMPTY_STRING)
        PreferenceManager.remove(preference)
        if (!CommonUtils.isEmpty(actionStr)) {
            val type = object : TypeToken<Action?>() {}.type
            val action = JsonUtils.fromJson<Action>(actionStr, type)
            action?.let { checkAndExecuteAction(it) }
        }
    }

    override fun checkAndExecuteAction(action: Action) {
        if (isActionApplicable(action)) {
            // Device locked. Open the app, based on requirement.
            val intent = getIntentForAction(action)
            if (intent != null) {
                if (action.schedule?.schedulingCondition == SchedulingCondition.NOW && action.actionId != null) {
                    if (action.actionDef?.bundleParams?.contains(Constants.REPLACE_NOTI) == true) {
                        if (Looper.getMainLooper() == Looper.myLooper()) {
                            // Replace in IO thread and notify main thread
                            Observable.fromCallable {
                                val modifiedIntent = getReplacedIntent(intent)
                                modifiedIntent
                            }.doOnError {
                                // At least show without replacement
                                CommonUtils.getApplication().startActivity(intent)
                                markActionExecutionTime(action.actionId!!)
                            }.subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe {
                                        CommonUtils.getApplication().startActivity(it)
                                        markActionExecutionTime(action.actionId!!)
                                    }
                        }
                        else {
                            // Replace in current thread and proceed
                            val modifiedIntent = getReplacedIntent(intent)
                            CommonUtils.getApplication().startActivity(modifiedIntent)
                            markActionExecutionTime(action.actionId!!)
                        }
                    }
                    else {
                        CommonUtils.getApplication().startActivity(intent)
                        markActionExecutionTime(action.actionId!!)
                    }
                }

                if (action.schedule?.schedulingCondition == SchedulingCondition.ON_NEXT_DEVICE_UNLOCK && action.actionId != null) {
                    CommonUtils.getApplication().registerReceiver(object : BroadcastReceiver() {
                        override fun onReceive(context: Context?, userIntent: Intent?) {
                            CommonUtils.getApplication().startActivity(intent)
                            markActionExecutionTime(action.actionId!!)
                        }
                    }, IntentFilter((Intent.ACTION_USER_PRESENT)))
                }

                if (action.schedule?.schedulingCondition == SchedulingCondition.ON_NEXT_BOOT_COMPLETED) {
                    val actionNow = action.copy(schedule = action.schedule?.copy(SchedulingCondition.NOW))
                    val actionStr = Gson().toJson(actionNow)
                    PreferenceManager.savePreference(
                            GenericAppStatePreference.BOOT_COMPLETED_ACTION, actionStr)
                    val pm = CommonUtils.getApplication().getPackageManager()
                    val comp = ComponentName(CommonUtils.getApplication(),
                            BootCompletedActionableReceiver::class.java)
                    pm.setComponentEnabledSetting(comp,
                            PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
                }

                if (action.schedule?.schedulingCondition == SchedulingCondition.ON_NEXT_LAUNCH) {
                    val actionNow = action.copy(schedule = action.schedule?.copy(SchedulingCondition.NOW))
                    val actionStr = Gson().toJson(actionNow)
                    PreferenceManager.savePreference(
                            GenericAppStatePreference.APP_LAUNCH_ACTION, actionStr)
                }

                if (action.schedule?.schedulingCondition == SchedulingCondition.ON_NEXT_NOTIFICATION) {
                    val actionNow = action.copy(schedule = action.schedule?.copy(SchedulingCondition.NOW))
                    val actionStr = Gson().toJson(actionNow)
                    PreferenceManager.savePreference(
                            GenericAppStatePreference.NEXT_NOTIFICATION_ACTION, actionStr)
                }

                if (action.schedule?.schedulingCondition == SchedulingCondition.ON_NEXT_STORY_PAGE) {
                    val actionNow = action.copy(schedule = action.schedule?.copy(SchedulingCondition.NOW))
                    val actionStr = Gson().toJson(actionNow)
                    PreferenceManager.savePreference(
                            GenericAppStatePreference.NEXT_STORY_OPEN, actionStr)
                }

                if (action.schedule?.schedulingCondition == SchedulingCondition.ON_NEXT_STORY_EXIT) {
                    val actionNow = action.copy(schedule = action.schedule?.copy(SchedulingCondition.NOW))
                    val actionStr = Gson().toJson(actionNow)
                    PreferenceManager.savePreference(
                            GenericAppStatePreference.NEXT_STORY_EXIT, actionStr)
                }

                if (action.schedule?.schedulingCondition == SchedulingCondition.ON_NEXT_COMMENT) {
                    val actionNow = action.copy(schedule = action.schedule?.copy(SchedulingCondition.NOW))
                    val actionStr = Gson().toJson(actionNow)
                    PreferenceManager.savePreference(
                            GenericAppStatePreference.NEXT_COMMENT_CREATION, actionStr)
                }
            }
        } else {
            Logger.e(TAG, "Action Not Applicable")
        }
    }

    private fun getReplacedIntent(intent: Intent): Intent? {
        try {
            val topNotifications =
                NotificationDB.instance().getNotificationDao().getNonDeferredNotifications(false)
            if (topNotifications?.isNotEmpty() == true) {
                // Replace first item

                if (intent.extras?.containsKey(Constants.DH_IMG1) == true) {
                    var imageLink = topNotifications[0]?.baseInfo?.imageLink
                    if (CommonUtils.isEmpty(imageLink)) {
                        imageLink = topNotifications[0]?.baseInfo?.imageLinkV2
                    }
                    if (CommonUtils.isEmpty(imageLink)) {
                        imageLink = topNotifications[0]?.baseInfo?.bigImageLink
                    }
                    if (CommonUtils.isEmpty(imageLink)) {
                        imageLink = topNotifications[0]?.baseInfo?.bigImageLinkV2
                    }
                    if (CommonUtils.isEmpty(imageLink)) {
                        imageLink = topNotifications[0]?.baseInfo?.inboxImageLink
                    }

                    if (!CommonUtils.isEmpty(imageLink)) {
                        intent.putExtra(Constants.DH_IMG1, imageLink)
                    }
                }

                if (intent.extras?.containsKey(Constants.DH_M1) == true) {
                    var title = topNotifications[0]?.baseInfo?.message
                    if(title == null){
                        title = topNotifications[0]?.baseInfo?.uniMsg
                    }
                    if (title != null) {
                        intent.putExtra(Constants.DH_M1, title)
                    }
                }
            }

            if (topNotifications?.size?:0 > 1) {
                // Replace second item
                if (intent.extras?.containsKey(Constants.DH_IMG2) == true) {
                    var imageLink = topNotifications?.get(1)?.baseInfo?.imageLink
                    if (CommonUtils.isEmpty(imageLink)) {
                        imageLink = topNotifications?.get(1)?.baseInfo?.imageLinkV2
                    }
                    if (CommonUtils.isEmpty(imageLink)) {
                        imageLink = topNotifications?.get(1)?.baseInfo?.bigImageLink
                    }
                    if (CommonUtils.isEmpty(imageLink)) {
                        imageLink = topNotifications?.get(1)?.baseInfo?.bigImageLinkV2
                    }
                    if (CommonUtils.isEmpty(imageLink)) {
                        imageLink = topNotifications?.get(1)?.baseInfo?.inboxImageLink
                    }

                    if (!CommonUtils.isEmpty(imageLink)) {
                        intent.putExtra(Constants.DH_IMG2, imageLink)
                    }
                }

                if (intent.extras?.containsKey(Constants.DH_M2) == true) {
                    var title = topNotifications?.get(1)?.baseInfo?.message
                    if(title == null){
                        title = topNotifications?.get(1)?.baseInfo?.uniMsg
                    }
                    if (title != null) {
                        intent.putExtra(Constants.DH_M2, title)
                    }
                }
            }

            if (topNotifications?.size?:0 > 2) {
                // Replace third item
                if (intent.extras?.containsKey(Constants.DH_IMG3) == true) {
                    var imageLink = topNotifications?.get(2)?.baseInfo?.imageLink
                    if (CommonUtils.isEmpty(imageLink)) {
                        imageLink = topNotifications?.get(2)?.baseInfo?.imageLinkV2
                    }
                    if (CommonUtils.isEmpty(imageLink)) {
                        imageLink = topNotifications?.get(2)?.baseInfo?.bigImageLink
                    }
                    if (CommonUtils.isEmpty(imageLink)) {
                        imageLink = topNotifications?.get(2)?.baseInfo?.bigImageLinkV2
                    }
                    if (CommonUtils.isEmpty(imageLink)) {
                        imageLink = topNotifications?.get(2)?.baseInfo?.inboxImageLink
                    }

                    if (!CommonUtils.isEmpty(imageLink)) {
                        intent.putExtra(Constants.DH_IMG3, imageLink)
                    }
                }

                if (intent.extras?.containsKey(Constants.DH_M3) == true) {
                    var title = topNotifications?.get(2)?.baseInfo?.message
                    if (title == null){
                        title = topNotifications?.get(2)?.baseInfo?.uniMsg
                    }
                    if (title != null) {
                        intent.putExtra(Constants.DH_M3, title)
                    }
                }
            }
        } catch (ex: Exception) {
            // Do nothing.
            Logger.w(TAG, "Error replacing placeholders", ex)
        }

        return intent
    }

    private fun expandedPayload(action: Action, actionsMap: HashMap<String, ActionDef>) {
        action.actionDef = actionsMap[action.actionId]
        if (action.pendingAction != null) {
            expandedPayload(action.pendingAction!!, actionsMap)
        }
    }

    private fun getIntentForAction(action: Action?): Intent? {
        if (action?.actionDef?.intentAction == null && action?.deeplinkUrl == null) {
            return null
        }

        val intent = when {
            action.deeplinkUrl == null -> {
                val tempIntent = Intent(action.actionDef?.intentAction)
                tempIntent.putExtra(Constants.ACTION_ID, action.actionId)
                tempIntent
            }
            action.actionDef?.intentAction == null -> {
                val tempIntent = Intent(Intent.ACTION_VIEW, Uri.parse(action.deeplinkUrl))
                tempIntent.putExtra(Constants.ACTION_ID, action.actionId)
                tempIntent
            }
            else -> {
                val tempIntent = Intent(action.actionDef?.intentAction, Uri.parse(action.deeplinkUrl))
                tempIntent.putExtra(Constants.ACTION_ID, action.actionId)
                tempIntent
            }
        }

        if (action.actionDef?.intentType != null) {
            intent.setType(action.actionDef?.intentType)
        }

        val bundleParams = action.actionDef?.bundleParams
        bundleParams?.keys?.forEach {
            val value = bundleParams.get(it)
            val valueObj: Any = getBundleValue(value)
            if (valueObj is String) {
                intent.putExtra(it, valueObj)
            } else if (valueObj is Int) {
                intent.putExtra(it, valueObj)
            } else if (valueObj is Long) {
                intent.putExtra(it, valueObj)
            } else if (valueObj is Short) {
                intent.putExtra(it, valueObj)
            } else if (valueObj is Byte) {
                intent.putExtra(it, valueObj)
            } else if (valueObj is Float) {
                intent.putExtra(it, valueObj)
            } else if (valueObj is Double) {
                intent.putExtra(it, valueObj)
            } else if (valueObj is Char) {
                intent.putExtra(it, valueObj)
            } else if (valueObj is Boolean) {
                intent.putExtra(it, valueObj)
            } else if (valueObj is Serializable) {
                intent.putExtra(it, valueObj)
            } else if (valueObj is Parcelable) {
                intent.putExtra(it, valueObj)
            }
        }

        val pendingIntent = getIntentForAction(action.pendingAction)
        if (pendingIntent != null) {
            intent.putExtra(Constants.BUNDLE_EXTRA, pendingIntent)
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return intent
    }

    private fun getBundleValue(value: String?): Any {
        return value ?: ""
    }

    private fun isExecutionLimitAvailable(action: Action): Boolean {
        if (action.actionConditions?.maxActionAllowed == null || action.actionConditions?.maxActionAllowed == Integer.MAX_VALUE) {
            return true
        }

        if (action.actionConditions?.maxRestrictionDuration == null || action.actionConditions?.maxRestrictionDuration == Long.MAX_VALUE) {
            return true
        }

        val actionExecTime = PreferenceManager.getPreference(GenericAppStatePreference.ACTIONABLE_ITEM_EXECUTION_TIME,
                Constants.EMPTY_STRING)
        val type = object : TypeToken<List<Pair<String, Long>>>() {}.type

        val actionTimesPair: MutableList<Pair<String, Long>> = if (CommonUtils.isEmpty(actionExecTime)) {
            ArrayList()
        } else {
            Gson().fromJson(actionExecTime, type) as MutableList<Pair<String, Long>>
        }

        var includedCount = 0
        actionTimesPair.forEach {
            if (it.first == action.actionId && System.currentTimeMillis() - it.second < action.actionConditions!!.maxRestrictionDuration) {
                includedCount++
            }
        }

        return includedCount < action.actionConditions!!.maxActionAllowed
    }

    public fun markActionExecutionTime(actionId: String) {
        var actionExecTime = PreferenceManager.getPreference(GenericAppStatePreference.ACTIONABLE_ITEM_EXECUTION_TIME,
                Constants.EMPTY_STRING)
        val type = object : TypeToken<List<Pair<String, Long>>>() {}.type

        val actionTimesPair: MutableList<Pair<String, Long>>
        if (CommonUtils.isEmpty(actionExecTime)) {
            actionTimesPair = ArrayList()
        } else {
            actionTimesPair = Gson().fromJson(actionExecTime, type) as MutableList<Pair<String, Long>>
        }

        actionTimesPair.add(Pair(actionId, System.currentTimeMillis()))
        actionExecTime = Gson().toJson(actionTimesPair)
        PreferenceManager.savePreference(GenericAppStatePreference.ACTIONABLE_ITEM_EXECUTION_TIME,
                actionExecTime)
    }

    private fun isActionApplicable(action: Action): Boolean {
        val blockedIds : HashSet<String> =
                PreferenceManager.getPreference(GenericAppStatePreference.BLOCKED_ACTION_IDS, HashSet<String>())
        if (blockedIds.contains(action.actionId)) {
            // Action is already blocked

            // Do not handle actions from pull framework
            NhNotificationAnalyticsUtility.deployNotificationFilteredEvent(
                    NotificationFilterType.ACTION_RELEVANCE,
                    NotificationInvalidType.PUSH_ACTION.type)
            return false
        }

        if (action.actionConditions == null) {
            Log.e(TAG, "actionConditions " + action.schedule?.schedulingCondition)
            // No condition specified. So applicable for all.
            return true;
        }

        if (!isExecutionLimitAvailable(action)) {
            // Limit crossed for given duration
            NhNotificationAnalyticsUtility.deployNotificationFilteredEvent(
                    NotificationFilterType.ACTION_RELEVANCE,
                    NotificationInvalidType.RELEVANCE_LIMIT.type)
            return false
        }

        if (action.actionConditions?.manufacturerRestriction?.isNotEmpty() == true) {
            // Manufacturer restrictions applicable. So match manufacturer.
            if (action.actionConditions?.manufacturerRestriction?.contains(Build.MANUFACTURER?.toLowerCase()) == false) {
                Log.e(TAG, "manufacturerRestriction " + action.actionConditions?.manufacturerRestriction)
                NhNotificationAnalyticsUtility.deployNotificationFilteredEvent(
                        NotificationFilterType.ACTION_RELEVANCE,
                        NotificationInvalidType.RELEVANCE_MANUFACTURER.type)
                return false
            }
        }

        if (action.actionConditions?.apiLevelRestriction?.isNotEmpty() == true) {
            // API Level restrictions applicable. So match API Level.
            if (action.actionConditions?.apiLevelRestriction?.contains(Build.VERSION.SDK_INT) == false) {
                Log.e(TAG, "apiLevelRestriction " + action.actionConditions?.apiLevelRestriction)
                NhNotificationAnalyticsUtility.deployNotificationFilteredEvent(
                        NotificationFilterType.ACTION_RELEVANCE,
                        NotificationInvalidType.RELEVANCE_API_LEVEL.type)
                return false
            }
        }

        if (action.actionConditions?.isDeviceLocked == true && !AndroidUtils.isDeviceLocked()) {
            // Needs to be mandatorily locked. So rejecting.
            NhNotificationAnalyticsUtility.deployNotificationFilteredEvent(
                    NotificationFilterType.ACTION_RELEVANCE,
                    NotificationInvalidType.RELEVANCE_DEVICE_LOCKED.type)
            return false
        }

        if (action.actionConditions?.isDeviceUnLocked == true && AndroidUtils.isDeviceLocked()) {
            // Needs to be mandatorily unlocked. So rejecting.
            NhNotificationAnalyticsUtility.deployNotificationFilteredEvent(
                    NotificationFilterType.ACTION_RELEVANCE,
                    NotificationInvalidType.RELEVANCE_DEVICE_UNLOCKED.type)
            return false
        }

        val isNotificationEnabled =
                NotificationManagerCompat.from(CommonUtils.getApplication()).areNotificationsEnabled()
        if (action.actionConditions?.isActionForNotificationEnabled == true && !isNotificationEnabled) {
            NhNotificationAnalyticsUtility.deployNotificationFilteredEvent(
                    NotificationFilterType.ACTION_RELEVANCE,
                    NotificationInvalidType.RELEVANCE_NOTIFICATION_ENABLED.type)
            return false
        }

        if (action.actionConditions?.isActionForNotificationDisabled == true && isNotificationEnabled) {
            NhNotificationAnalyticsUtility.deployNotificationFilteredEvent(
                    NotificationFilterType.ACTION_RELEVANCE,
                    NotificationInvalidType.RELEVANCE_NOTIFICATION_DISABLED.type)
            return false
        }

        if (action.actionConditions?.isActionForChannelEnabled == true &&
                !isChannelEnabled(action.actionConditions?.notificationChannelId)) {
            NhNotificationAnalyticsUtility.deployNotificationFilteredEvent(
                    NotificationFilterType.ACTION_RELEVANCE,
                    NotificationInvalidType.RELEVANCE_CHANNEL_ENABLED.type)
            return false
        }

        if (action.actionConditions?.isActionForChannelDisabled == true &&
                isChannelEnabled(action.actionConditions?.notificationChannelId)) {
            NhNotificationAnalyticsUtility.deployNotificationFilteredEvent(
                    NotificationFilterType.ACTION_RELEVANCE,
                    NotificationInvalidType.RELEVANCE_CHANNEL_DISABLED.type)
            return false
        }

        // None of the conditions met, As good as no one reject it.
        return true
    }

    private fun isChannelEnabled(channelId: String?): Boolean {
        return if (!CommonUtils.isEmpty(channelId)) {
            val manager: NotificationManager = CommonUtils.getApplication()
                .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel: NotificationChannel? = manager.getNotificationChannel(channelId)
            channel != null && channel.getImportance() !== NotificationManager.IMPORTANCE_NONE
        } else false
    }

}