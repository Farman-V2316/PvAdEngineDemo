/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.notification.view.service

import android.app.Notification
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.os.Process
import androidx.core.app.NotificationCompat
import com.newshunt.common.helper.common.AndroidUtils
import com.newshunt.common.helper.common.BusProvider
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.notification.R
import com.newshunt.notification.helper.selectChannelId
import com.newshunt.dataentity.notification.BaseModel
import com.newshunt.notification.presenter.PullNotificationsPresenter
import com.newshunt.dataentity.notification.util.NotificationConstants
import com.newshunt.notification.helper.NotificationUtils

/***
* @author amit.chaudhary
 * when notification with type = trigger_pull comes this service will be invoked which runs in
 * foreground with sticky notification with provided message in baseinfo of notification.
 * @NotificationSocialPullService uses notificationPullPresenter to call pull request and handle
 * response.
* */
class NotificationSocialPullService : Service(), PullNotificationsPresenter.PullJobStateListener {
    private var pullNotificationPresenter: PullNotificationsPresenter? = null
    private var notification: Notification? = null
    private var serviceLooper: Looper? = null
    private var serviceHandler: ServiceHandler? = null
    override fun onPullJobComplete() {
        pullNotificationPresenter = null
        Logger.d(LOG_TAG, "pull job complete")
        stopSelf()
    }

    override fun onPullJobError() {
        pullNotificationPresenter = null
        Logger.d(LOG_TAG, "pull job failed")
        stopSelf()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        HandlerThread("ServiceStartArguments", Process.THREAD_PRIORITY_BACKGROUND).apply {
            start()

            // Get the HandlerThread's Looper and use it for our Handler
            serviceLooper = looper
            serviceHandler = ServiceHandler(looper)
        }
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val parseModel = intent?.getSerializableExtra(NotificationConstants.BUNDLE_NOTIFICATION)
                as? BaseModel ?: return START_NOT_STICKY
        serviceHandler?.obtainMessage()?.also { msg ->
            msg.arg1 = startId
            serviceHandler?.sendMessage(msg)
        }
        val msg = NotificationUtils.getNotificationContentText(parseModel.baseInfo)

        val channelId = selectChannelId(true, parseModel, this)
        val notificationBuilder = NotificationCompat.Builder(applicationContext,
                channelId)
                .setSmallIcon(R.mipmap.app_notification_icon)
                .setTicker(AndroidUtils.getTextFromHtml(msg))
                .setContentText(AndroidUtils.getRichTextFromHtml(msg))
                .setGroup(NotificationConstants.NOTIFICATION_DEFAULT_GROUP_NAME)
        notification = notificationBuilder.build()
        try {
            startForeground(104, notification)
            return START_STICKY
        } catch (exception: Exception) {
            Logger.caughtException(exception)
        }

        return START_NOT_STICKY
    }

    companion object {
        private const val LOG_TAG = "NotificationSocialPullService"

        @JvmStatic
        fun getIntent(context: Context, baseModel: BaseModel): Intent {
            val intent = Intent(context, NotificationSocialPullService::class.java)
            intent.putExtra(NotificationConstants.BUNDLE_NOTIFICATION, baseModel)
            return intent
        }
    }

    private fun startPull() {
        AndroidUtils.getMainThreadHandler().post {
            Logger.d(LOG_TAG, "Notification pull service started")
            pullNotificationPresenter = PullNotificationsPresenter(BusProvider.getUIBusInstance(),
                    this@NotificationSocialPullService)
            pullNotificationPresenter?.pullNotifications(null)
        }
    }


    private inner class ServiceHandler(looper: Looper) : Handler(looper) {

        override fun handleMessage(msg: Message) {
            try {
                startPull()
            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt()
            }
        }
    }

}