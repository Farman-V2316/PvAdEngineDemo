/*
 *  Copyright (c) 2021 Newshunt. All rights reserved.
 */
package androidx.paging

import android.os.Handler
import android.os.Looper
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.util.BuildConfig
import java.util.concurrent.Executor

/**
 * An Executor implementation which executes runnables on main thread and catches any exception
 * thrown. This is implemented to catch the IndexOutOfBoundException occuring in paged list v 2.x
 *
 * Reference: https://issuetracker.google.com/issues/135628748
 * Created by srikanth.r on 11/16/21.
 */
class ExecutorImpl : Executor {
    private val handler = Handler(Looper.getMainLooper())

    override fun execute(command: Runnable?) {
        handler.post {
            try {
                command?.run()
            } catch (ex: Exception) {
                if (BuildConfig.DEBUG) {
                    throw ex
                } else {
                    Logger.caughtException(ex)
                }
            }
        }
    }
}