/*
 * Copyright (c) 2021 Newshunt. All rights reserved.
 */
package com.newshunt.notification.helper

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import com.bumptech.glide.request.transition.Transition
import com.newshunt.common.helper.common.AndroidUtils
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.notification.AdjunctLangStickyNavModel
import com.newshunt.dataentity.notification.BaseModelType
import com.newshunt.dataentity.notification.NotificationLayoutType
import com.newshunt.dataentity.notification.util.NotificationConstants
import com.newshunt.dhutil.helper.preference.AppStatePreference
import com.newshunt.notification.model.entity.server.AdjunctLangBaseInfo
import com.newshunt.notification.sqlite.NotificationDB
import com.newshunt.notification.view.builder.NotificationBuilder
import com.newshunt.notification.view.builder.NotificationLayoutBuilder
import com.newshunt.sdk.network.image.Image

private const val LOG_TAG = "AdjunctLangNotificationHelper"

/**
 * Adjunct Notification Helper class which exposes function to post adjunct sticky notification.
 * This class also handles to remove sticky notification in case adjunct lang is selected/blacklisted from
 * any other UI component like (sticky banner, ad banner, multi lang card or detail page popup).
 * This class also handles to remove sticky notification in case of No Notification opted from settings.
 * @author aman.roy
 */
object AdjunctLangNotificationHelper {

    @SuppressLint("LaunchActivityFromNotification")
    @JvmStatic
    private fun buildAdjunctNotification(adjunctLangBaseInfo: AdjunctLangBaseInfo, adjunctLangStickyNavModel: AdjunctLangStickyNavModel, image: Bitmap?) {
        val intent = Intent()
        intent.putExtra(Constants.NOTIFICATION_BASE_MODEL,BaseModelType.convertModelToString(adjunctLangStickyNavModel))
        val builder = NotificationLayoutBuilder(
            CommonUtils.getApplication().applicationContext,
            adjunctLangBaseInfo,
            image,
            null,
            NotificationLayoutType.NOTIFICATION_TYPE_ADJUNCT_STICKY,
            adjunctLangBaseInfo.uniqueId,
            false,intent,
            PreferenceManager.getPreference(AppStatePreference.NOTIFICATION_FONT_SIZE, 0.0f)
        ).build(PreferenceManager.getPreference(AppStatePreference.IS_NOTIFICATION_UNGROUPING_ENABLED, false))

        val pendingIntent = PendingIntent.getActivity(
            CommonUtils.getApplication().applicationContext, System.currentTimeMillis().toInt(),
            getAdjunctPendingIntent(adjunctLangBaseInfo.uniqueId,adjunctLangBaseInfo.notificationClickDeeplinkUrl,adjunctLangStickyNavModel),
            PendingIntent.FLAG_CANCEL_CURRENT)

        builder.setContentIntent(pendingIntent)
        builder.setAutoCancel(true)
        getManager()?.notify(adjunctLangBaseInfo.uniqueId,builder.build())

    }
    @JvmStatic
    fun buildAdjunctNotification(adjunctLangBaseInfo: AdjunctLangBaseInfo, adjunctLangStickyNavModel: AdjunctLangStickyNavModel) {
        NotificationBuilder.HANDLER.post {
            Image.load(adjunctLangBaseInfo.adjunctNotiUrl, true).into(object : Image.ImageTarget() {
                override fun onResourceReady(
                    bitmap: Any,
                    transition: Transition<*>?
                ) {
                    if (bitmap !is Bitmap) {
                        return
                    }
                    val notificationImage = AndroidUtils.getRoundedBitmap(
                        bitmap,
                        CommonUtils.getDimension(com.newshunt.notification.R.dimen.image_size),
                        CommonUtils.getDimension(com.newshunt.notification.R.dimen.image_size),
                        CommonUtils.getDimension(com.newshunt.notification.R.dimen.image_radius)
                    )
                    buildAdjunctNotification(adjunctLangBaseInfo,adjunctLangStickyNavModel,notificationImage)
                }

                override fun onLoadFailed(errorDrawable: Drawable?) {
                    Logger.d(LOG_TAG,"Image url downloading failed")
                    buildAdjunctNotification(adjunctLangBaseInfo,adjunctLangStickyNavModel,null)
                }
            })
        }
    }

    /* Function to clear adjunct sticky notification in case of language selection*/
    fun clearAdjunctLangNotificationPostLanguageSelection(langs:List<String>) {
        CommonUtils.runInBackground {
            val notificationDao = NotificationDB.instance().getNotificationDao()
            val adjunctLangNotifications = notificationDao.getAdjunctStickyNavModel()
            val ids = adjunctLangNotifications.filter { langs.contains(it.baseInfo.language) }.map {
                clearNotification(it.baseInfo.uniqueId)
                it.baseInfo.uniqueId.toString()
            }
            notificationDao.deleteAdjunctStickyNotifications(ids)
        }
    }

    /* Function for removing all adjunct sticky notification in case when no notification from tray is clicked*/
    fun clearAdjunctLangNotificationForNoNotification() {
        CommonUtils.runInBackground {
            val notificationDao = NotificationDB.instance().getNotificationDao()
            val adjunctLangNotifications = notificationDao.getAdjunctStickyNavModel()
            val ids = adjunctLangNotifications.map {
                clearNotification(it.baseInfo.uniqueId)
                it.baseInfo.uniqueId.toString()
            }
            notificationDao.deleteAdjunctStickyNotifications(ids)
        }
    }

    private fun clearNotification(uniqueId: Int) {
        getManager()?.cancel(uniqueId)
    }

    private fun getAdjunctPendingIntent(notiId: Int, deeplinkUrl: String,adjunctLangStickyNavModel:AdjunctLangStickyNavModel): Intent {
        val adjunctIntent = NotificationUtils.getNotificationRouterIntent(adjunctLangStickyNavModel)
        adjunctIntent.putExtra(NotificationConstants.ADJUNCT_NOTI_ID, notiId)
        adjunctIntent.putExtra(NotificationConstants.ADJUNCT_CTA_DEEPLINK_URL, deeplinkUrl)
        return adjunctIntent
    }


    private fun getManager(): NotificationManager? = CommonUtils.getApplication().getSystemService(
        Context.NOTIFICATION_SERVICE) as? NotificationManager
}