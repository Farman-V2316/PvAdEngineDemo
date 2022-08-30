/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.dhutil

import android.app.ActivityManager
import android.content.Context
import android.os.Process
import com.newshunt.common.helper.appconfig.AppConfig

/**
 * @author santhosh.kc
 */

const val STICKY_PROCESS = ":stickyProcess"

/**
 * Returns true if process is sticky process.
 */
fun isStickyProcess(appContext: Context) =
        Process.myPid() ==
                mainPid(appContext, "${AppConfig.getInstance().packageName}$STICKY_PROCESS")

fun isMainProcess(appContext: Context) =
        Process.myPid() == mainPid(appContext, AppConfig.getInstance().packageName)

/**
 * Returns process id of the running process. <br/>
 * Process-name, if not passed,  will return pid of the main process (AppConfig.packageName)
 */
private fun mainPid(applicationContext: Context, processName: String? = null): Int? {
    val mainProcessName = processName ?: AppConfig.getInstance().packageName ?: return null
    return (applicationContext.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager)
            ?.runningAppProcesses?.find { it.processName == mainProcessName }?.pid
}

fun getProcessName(applicationContext: Context): String? {
    val pId = Process.myPid()
    return (applicationContext.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager)
            ?.runningAppProcesses?.find { it.pid == pId }?.processName
}

fun isMissingSplitProcess(processName: String?): Boolean {
    val missingSplitsProcessName = "${AppConfig.getInstance().packageName}:playcore_missing_splits_activity"
    return processName == missingSplitsProcessName
}