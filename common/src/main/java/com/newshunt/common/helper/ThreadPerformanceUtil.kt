package com.newshunt.common.helper

import android.os.StrictMode
import com.newshunt.common.util.BuildConfig

/*
* Class for logging thread performance leaks appearing across application
* filter in adb logcat with "? E/StrictMode"
*/
class ThreadPerformanceUtil{

    fun init() {
        enableStrictMode(BuildConfig.DEBUG)
    }

    fun enableStrictMode(state:Boolean) {
        if (state) {
            StrictMode.setThreadPolicy(buildThreadPolicy())
            StrictMode.setVmPolicy(buildDefaultVMPolicy())
        }
    }
    // UI thread policy for detecting leaks for both network and disk reads
    private fun buildThreadPolicy(): StrictMode.ThreadPolicy = StrictMode.ThreadPolicy.Builder()
            .detectAll()
            .build()


    /*
    * VM policy for detecting
    * 1. Activity leaks
    * 2. SQL lite object leaks
    * 3. Object registration and closable leaks
    * */
    private fun buildDefaultVMPolicy() : StrictMode.VmPolicy = StrictMode.VmPolicy.Builder()
            .detectLeakedSqlLiteObjects()
            .detectActivityLeaks()
            .detectLeakedClosableObjects()
            .detectLeakedRegistrationObjects()
            .penaltyLog()  // keeping just logs and not expecting to kill VM
            .build()
}