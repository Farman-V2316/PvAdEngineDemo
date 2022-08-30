package com.newshunt.appview.common.postcreation.view.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.newshunt.appview.common.postcreation.view.helper.PostNotificationHelper
import com.newshunt.appview.common.postcreation.view.service.UploadJobService
import com.newshunt.common.helper.common.Logger
import com.newshunt.dataentity.notification.util.NotificationConstants

private const val TAG = "CreatePostReceiver"
class CreatePostReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent) {
        Logger.d(TAG, "onReceive")
        val notificationId: Int =
            intent.getIntExtra(NotificationConstants.CREATE_POST_NOTIFICATION_ID, 0)
        val cpId: Long = intent.getLongExtra(NotificationConstants.CREATE_POST_ID, -1)
        when (intent.action) {
            NotificationConstants.CREATE_POST_ACTION_RETRY -> {
                PostNotificationHelper.clearNotification(notificationId)
                UploadJobService.retry(cpId)
            }
        }
    }

}