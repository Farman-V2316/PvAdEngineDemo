/*
 *  Copyright (c) 2021 Newshunt. All rights reserved.
 */
package com.newshunt.notification.view.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.newshunt.common.helper.common.BusProvider
import com.newshunt.common.helper.common.Logger
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.notification.util.NotificationConstants
import com.newshunt.notification.helper.NotificationServiceProvider
import com.newshunt.notification.sqlite.NotificationDB
import kotlin.Exception

/**
 * News Sticky foreground service implementation
 *
 * Created by atul.anand on 24/10/21.
 */
class NewsStickyHelperService : Service() {

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.action?.let{ action ->
            when(action){
                NotificationConstants.INTENT_ACTION_UPDATE_NOTIFICATION_TRAY -> {
                    try{
                        CommonUtils.runInBackground(object: Runnable{
                            override fun run() {
                                NotificationServiceProvider.getNotificationService()?.updateNotificationsInTray(true)
                            }

                        })

                    }catch(ex: Exception){
                        Logger.caughtException(ex)
                    }

                }
                else ->{}
            }
        }

        return START_NOT_STICKY
    }
}