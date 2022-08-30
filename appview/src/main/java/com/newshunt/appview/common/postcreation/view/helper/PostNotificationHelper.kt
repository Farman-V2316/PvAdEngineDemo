package com.newshunt.appview.common.postcreation.view.helper

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import com.bumptech.glide.request.transition.Transition
import com.newshunt.appview.R
import com.newshunt.common.helper.common.AndroidUtils
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.notification.AdjunctLangNavModel
import com.newshunt.dataentity.notification.AdjunctLangStickyNavModel
import com.newshunt.dataentity.notification.NotificationLayoutType
import com.newshunt.dataentity.notification.util.NotificationConstants
import com.newshunt.deeplink.navigator.CommonNavigator
import com.newshunt.dhutil.helper.preference.AppStatePreference
import com.newshunt.notification.helper.NotificationLogger
import com.newshunt.notification.model.entity.server.AdjunctLangBaseInfo
import com.newshunt.notification.model.entity.server.CreatePostBaseInfo
import com.newshunt.notification.sqlite.NotificationDB
import com.newshunt.notification.view.builder.NotificationBuilder
import com.newshunt.notification.view.builder.NotificationLayoutBuilder
import com.newshunt.sdk.network.image.Image
import com.newshunt.sdk.network.image.Image.ImageTarget
private const val LOG_TAG = "NotificationHelper"
object PostNotificationHelper {

    fun buildNotification(uniqueId: Int, prog: Int, status: CreatePostNotificationStatus,
                          cpId: Long, isImageAttached: Boolean = false) {
        val builder = NotificationLayoutBuilder(
                CommonUtils.getApplication().applicationContext,
                CreatePostBaseInfo(prog.toDouble(), cpId, uniqueId, isImageAttached).apply {
                    state = status.statusId()
                    uniMsg = status.message()
                    message = status.message()
                },
                null,
                null,
                NotificationLayoutType.NOTIFICATION_TYPE_CREATE_POST,
                101,

                false, null,
                PreferenceManager.getPreference(AppStatePreference.NOTIFICATION_FONT_SIZE, 0.0f)
        ).build(PreferenceManager.getPreference(AppStatePreference.IS_NOTIFICATION_UNGROUPING_ENABLED, false))
        getManager()?.notify(uniqueId, builder.build())
    }

    fun clearNotification(uniqueId: Int) {
        getManager()?.cancel(uniqueId)
    }

    private fun getManager(): NotificationManager? = CommonUtils.getApplication().getSystemService(
            Context.NOTIFICATION_SERVICE) as? NotificationManager


    enum class CreatePostNotificationStatus() {
        FAILURE {
            override fun statusId(): Int = NotificationConstants.POST_UPLOAD_STATUS_FAILED
            override fun message(): String = CommonUtils.getString(R.string.cp_error_message)
        },
        SUCCESS {
            override fun statusId(): Int = NotificationConstants.POST_UPLOAD_STATUS_SUCCESS
            override fun message(): String = CommonUtils.getString(R.string.cp_success_message)
        },
        PROGRESS {
            override fun statusId(): Int = NotificationConstants.POST_UPLOAD_PROGRESS
            override fun message(): String = CommonUtils.getString(R.string.cp_prog_message)
        };

        abstract fun statusId(): Int
        abstract fun message(): String
    }
}