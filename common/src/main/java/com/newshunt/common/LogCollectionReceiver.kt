/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */
package com.newshunt.common

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.newshunt.common.helper.LogCollectionUtils
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.service.LogCollectionService

/**
 * Receiver to start the Log collection service
 * <p>
 * Created by srikanth.ramaswamy on 09/21/2018.
 */
private const val LOG_TAG = "LogCollectionReceiver"
class LogCollectionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (LogCollectionUtils.ACTION_LOG_COLLECTION == intent?.action && context != null) {
            Logger.d(LOG_TAG, "onReceive, queueing LogCollectionService")
            LogCollectionService.startLogCollectionService(context)
        }
    }
}