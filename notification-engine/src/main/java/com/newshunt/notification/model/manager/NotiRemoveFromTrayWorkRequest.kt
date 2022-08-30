package com.newshunt.notification.model.manager

import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.workDataOf
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.notification.util.NotificationConstants
import com.newshunt.notification.helper.getNotificationRemoveFromTrayJobTag
import com.newshunt.notification.model.service.NotiRemoveFromTrayJobService
import java.util.concurrent.TimeUnit

class NotiRemoveFromTrayWorkRequest(private val notificationTrayId: Int, private val id: String, private
val stickyType: String, private val nextScheduledDuration: Int) {
    private val tolerance = 60 // 1 minute
    private var jobTag: String? = null

    @Throws(Exception::class)
    fun create(): OneTimeWorkRequest? {
        if (!CommonUtils.isEmpty(id) && !CommonUtils.isEmpty(stickyType)) {
            jobTag = getNotificationRemoveFromTrayJobTag(id, stickyType)
        }
        jobTag?.let {
            val workData = workDataOf(
                    NotificationConstants.STICKY_NOTIFICATION_REMOVE_FROM_TRAY_JOB_KEY to jobTag,
                    NotificationConstants.INTENT_EXTRA_STICKY_ID to id,
                    NotificationConstants.INTENT_EXTRA_STICKY_TYPE to stickyType,
                    NotificationConstants.INTENT_STICKY_NOTIFICATION_TRAY_ID to notificationTrayId)
            return OneTimeWorkRequestBuilder<NotiRemoveFromTrayJobService>()
                    .setInitialDelay(nextScheduledDuration.toLong(), TimeUnit.SECONDS)
                    .setInputData(workData)
                    .addTag(it)
                    .build()
        }
        return null
    }

    override fun toString(): String {
        return "Notification Remove Job Created with tag [ " + jobTag +
                "] scheduled after [" + " " + nextScheduledDuration + " ] seconds"
    }
}