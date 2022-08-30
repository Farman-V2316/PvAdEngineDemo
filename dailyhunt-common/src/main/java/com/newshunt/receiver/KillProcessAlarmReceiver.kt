package com.newshunt.receiver

import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Process
import com.newshunt.common.helper.appconfig.AppConfig

/**
 * This receiver is designed to run in a separate process(P2) and to kill main process(P1). <br/>
 *
 * Constraints that led to above design:
 * 1. App should consume less memory, when in background
 *    - Cannot trim memory to this number, with all the Ad sdks.
 * 2. Some process should be running, to be able to receive notifications.
 *
 * P2 is light-weight ; as it satisfies 30MB limit, it can keep running, and
 * notifications will be delivered to P1.
 *
 * @author satosh.dhanyamraju
 *
 */
class KillProcessAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        mainPid(context)?.let { Process.killProcess(it) }
    }
}

/**
 * Returns true if caller process is not main process.
 */
fun isTrimProcess(appContext: Context) =
        Process.myPid() ==
                mainPid(appContext, "${AppConfig.getInstance().packageName}$TRIM_PROCESS_NAME")

/**
 * Process name suffix. Should match android:process of receiver definition in manifest.
 */
private const val TRIM_PROCESS_NAME = ":notifier"


/**
 * Returns process id of the running process. <br/>
 * Process-name, if not passed,  will return pid of the main process (AppConfig.packageName)
 */
private fun mainPid(applicationContext: Context, processName: String? = null): Int? {
    val mainProcessName = processName ?: AppConfig.getInstance().packageName ?: return null
    return (applicationContext.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager)
            ?.runningAppProcesses?.find { it.processName == mainProcessName }?.pid
}
