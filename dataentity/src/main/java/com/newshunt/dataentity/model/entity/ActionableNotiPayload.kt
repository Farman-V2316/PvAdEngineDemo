/*
 * Copyright (c) 2020 Newshunt. All rights reserved.
 */
package com.newshunt.dataentity.model.entity

import java.util.Collections

/**
 * Data classes related to Actionable Payload.
 *
 * Created by karthik.r on 26/06/20.
 */
data class ActionableNotiPayload(val actions: List<Action>? = Collections.singletonList(Action(pendingAction = Action(schedule = Schedule()))),
                                 val actionDefs: List<ActionDef>? = null)

data class ActionDef(val intentAction: String? = null,
                     val actionId: String? = null, // To Control number of times its executed
                     val intentType: String? = null,
                     val bundleParams: Map<String, String>? = null)

data class Action(val actionId: String? = null, // To Control number of times its executed
                  val deeplinkUrl: String? = null,
                  val pendingAction: Action? = null,
                  val actionConditions: ActionCondition? = ActionCondition(),
                  val schedule: Schedule? = Schedule(),
                  var actionDef: ActionDef? = null)

data class ActionCondition(val maxActionAllowed: Int = Integer.MAX_VALUE,
                           val maxRestrictionDuration: Long = Long.MAX_VALUE, // in MilliSeconds
                           val isActionForNotificationEnabled: Boolean = false,
                           val isActionForNotificationDisabled: Boolean = false,
                           val isActionForChannelEnabled: Boolean = false,
                           val isActionForChannelDisabled: Boolean = false,
                           val isDeviceLocked: Boolean = false,
                           val isDeviceUnLocked: Boolean = false,
                           val notificationChannelId: String? = null,
                           val manufacturerRestriction: List<String>? = null,
                           val apiLevelRestriction: List<Int>? = null)

data class Schedule(val schedulingCondition: SchedulingCondition? = SchedulingCondition.NOW,
                    val onFailure: Schedule? = null)

enum class SchedulingCondition {
    NOW, ON_NEXT_BOOT_COMPLETED, ON_NEXT_DEVICE_UNLOCK, ON_NEXT_LAUNCH,
    ON_NEXT_NOTIFICATION, ON_NEXT_STORY_PAGE, ON_NEXT_STORY_EXIT, ON_NEXT_COMMENT
}
