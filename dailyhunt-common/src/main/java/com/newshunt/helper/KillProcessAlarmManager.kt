/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */

package com.newshunt.helper

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.newshunt.common.helper.common.ApplicationStatus
import com.newshunt.common.helper.common.Logger
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.common.helper.preference.GenericAppStatePreference
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.dhutil.model.internal.service.MultiProcessConfigServiceImpl
import com.newshunt.receiver.KillProcessAlarmReceiver
import kotlin.math.max

/**
 * Contains functions to schedule/cancel 'kill process' alarm
 * @author anshul.jain
 */

class KillProcessAlarmManager {

    companion object {
        private const val LOG_TAG = "KillProcessAlarmManager"
        private const val requestCode = 1000
        private val alarmManager = CommonUtils.getApplication().getSystemService(Context.ALARM_SERVICE) as AlarmManager

        @JvmStatic
        fun onDoubleBackToExit() {
            Logger.d(LOG_TAG, "isMultiProcessModeEnabled: " + MultiProcessConfigServiceImpl.isMultiProcessModeEnabled())
            if (MultiProcessConfigServiceImpl.isMultiProcessModeEnabled()) {
                killMainProcess()
            }
        }

        @JvmStatic
        fun onFirstActivityVisible() {
            killAlarm()
        }

        @JvmStatic
        fun onAppProcessInvokedInBackground() {
            scheduleAlarmOnBackgroundWakeUp()
        }

        @JvmStatic
        fun onLastActivityStopped() {
            scheduleAlarm(MultiProcessConfigServiceImpl.getKillProcessFGDuration());
        }

        private fun scheduleAlarm(durationInSeconds: Int) {
            if (!canModifyAlarms()) return
            Logger.d(LOG_TAG, "scheduleAlarm: $durationInSeconds")
            val time = System.currentTimeMillis() + durationInSeconds * 1000
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, time, getPendingIntent(0))
            PreferenceManager.savePreference(GenericAppStatePreference
                    .KILL_PROCESS_SCHEDULED_ALARM_TIME, time)
        }

        private fun scheduleAlarmOnBackgroundWakeUp() {
            if (!canModifyAlarms()) return
            val scheduledTime = PreferenceManager.getPreference(GenericAppStatePreference
                    .KILL_PROCESS_SCHEDULED_ALARM_TIME, 0L)
            val pendingAlarmInSeconds = (scheduledTime - System.currentTimeMillis()) / 1000
            val durationInSeconds = max(pendingAlarmInSeconds.toInt(),
                    MultiProcessConfigServiceImpl.getKillProcessBGDuration())
            Logger.d(LOG_TAG, "scheduleAlarm: $durationInSeconds")
            scheduleAlarm(durationInSeconds)
        }

        private fun killAlarm() {
            getPendingIntent(PendingIntent.FLAG_NO_CREATE)?.run {
                alarmManager.cancel(this)
                Logger.d(LOG_TAG, "killAlarm: ")
            }
        }

        private fun killMainProcess() {
            Logger.d(LOG_TAG, "Killing the main process")
            val intent = Intent(CommonUtils.getApplication(), KillProcessAlarmReceiver::class.java)
            CommonUtils.getApplication().sendBroadcast(intent)
        }

        private fun canModifyAlarms() = when {
            !MultiProcessConfigServiceImpl.isMultiProcessModeEnabled() -> {
                Logger.d(LOG_TAG, "scheduleAlarmOnBackgroundWakeUp: disabled")
                false
            }
            ApplicationStatus.getVisibleActiviesCount() > 0 -> {
                Logger.d(LOG_TAG, "scheduleAlarmOnBackgroundWakeUp: activity visible")
                false
            }
            else -> true
        }

        private fun getPendingIntent(flags: Int): PendingIntent? {
            val intent = Intent(CommonUtils.getApplication(), KillProcessAlarmReceiver::class.java)
            return PendingIntent.getBroadcast(CommonUtils.getApplication(), requestCode,
                    intent, flags)
        }
    }
}