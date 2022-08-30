/*
 * Copyright (c) 2020 Newshunt. All rights reserved.
 */

package com.newshunt.notification.helper

import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.preference.GenericAppStatePreference
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dhutil.helper.common.DailyhuntConstants

/**
 * Contains functions to configure the behaviour of [DummyNotificationForegroundService]
 *
 * @author satosh.dhanyamraju
 */
private const val LOG_TAG = "FGServiceUtil"
private fun config() = PreferenceManager.getPreference(GenericAppStatePreference.NOTF_FG_SERVICE_FLAGS, Constants.DEFAULT_NOTIFICATION_FG_FLAGS)
private const val F1_STOP_FG_ONLY = 0b1
private const val F2_USE_START_SERVICE_WHEN_IN_FG = 0b10
private const val F3_HANDLE_DEFAULT_CASE_SHOW_DUMMY_NOTIF = 0b100
private const val F4_CALL_START_FG_FROM_ON_CREATE = 0b1000
private const val F5_STICKY_DUMMY_NOTI_AND_START_FG_FROM_ON_CREATE = 0b10000
private const val F6_DISABLE_PREFETCH_NOTI_AND_START_FG_FROM_ON_CREATE = 0b100000
private const val F7_DISABLE_PREFETCH_NOTI_AND_START_FG_BEFORE_STOP = 0b1000000
private fun Int.queryBitmask(mask: Int) = and(mask) == mask


fun isStopFGOnly() = config().queryBitmask(F1_STOP_FG_ONLY)
fun useStartServWhenInFg() = config().queryBitmask(F2_USE_START_SERVICE_WHEN_IN_FG)
fun useStartServiceOnly() = useStartServWhenInFg() && CommonUtils.isInFg
fun handleDefaultCase() = config().queryBitmask(F3_HANDLE_DEFAULT_CASE_SHOW_DUMMY_NOTIF)
fun callStartFgFromOnCreate() = config().queryBitmask(F4_CALL_START_FG_FROM_ON_CREATE)
//return false when queryBitmask returns true
fun callStartFgFromOnCreateAndShowDummyNotiForSticky() = !(config().queryBitmask(F5_STICKY_DUMMY_NOTI_AND_START_FG_FROM_ON_CREATE))
fun callStartFgFromOnCreateAndShowNotiForPrefetch() = !(config().queryBitmask(F6_DISABLE_PREFETCH_NOTI_AND_START_FG_FROM_ON_CREATE))
fun callStartFgAndShowNotiBeforeStoppingForPrefetch() = !(config().queryBitmask(F7_DISABLE_PREFETCH_NOTI_AND_START_FG_BEFORE_STOP))

fun stopSelfDelay(): Long {
    return PreferenceManager.getPreference(
            GenericAppStatePreference.NOTF_FG_SERVICE_STOP_DELAY, 2000L)
}

fun disablePostingDummyNotification(): Boolean {
    return PreferenceManager.getPreference(
            GenericAppStatePreference.DISABLE_POSTING_DUMMY_NOTIFICATION, false)
}
fun setCrashlyticsProps() {
    val props = "" +
            "ntfForegroundServiceFlags=${config()}," +
            "disablePostingDummyNotification=${disablePostingDummyNotification()}," +
            "ntfForegroundServiceDuration=${PreferenceManager.getPreference(GenericAppStatePreference.NOTF_FG_SERVICE_DURATION, 1000)}," +
            "ntfForegroundServiceStopDelay=${stopSelfDelay()}"
    FirebaseCrashlytics.getInstance().setCustomKey(DailyhuntConstants.CRASHLYTICS_KEY_NOTF_FG_CONFIG, props)
    Logger.d(LOG_TAG, "setCrashlyticsProps $props")
}