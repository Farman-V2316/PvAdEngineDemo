/*
 *
 *  * Copyright (c) 2017 Newshunt. All rights reserved.
 *
 */

package com.newshunt.notification.view.builder


import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Build
import android.text.Html
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.bumptech.glide.request.transition.Transition
import com.newshunt.common.helper.common.AndroidUtils
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.notification.NotificationLayoutType
import com.newshunt.dataentity.notification.StickyNavModel
import com.newshunt.dataentity.notification.asset.GenericDataStreamAsset
import com.newshunt.dataentity.notification.asset.GenericEntity
import com.newshunt.dataentity.notification.asset.GenericNotificationAsset
import com.newshunt.dhutil.helper.preference.AppStatePreference
import com.newshunt.notification.R
import com.newshunt.notification.helper.createDefaultChannel
import com.newshunt.notification.helper.getChannelId
import com.newshunt.sdk.network.image.Image
import java.util.*

/**
 * Created by priya on 10/04/19.
 */


class GenericStickyNotificationBuilder(private val context: Context, private val stickyNavModel: StickyNavModel<*, *>?,
                                       private val layoutType: NotificationLayoutType,
                                       private val targetIntent: PendingIntent, private val refreshIntent: PendingIntent,
                                       private val dismissedIntent: PendingIntent, private val brandingCount: Int) {

    internal var cache = HashMap<String, Bitmap>()
    internal var brandingcache = HashMap<String, Bitmap>()


    fun setBitmapCache(hashmap: HashMap<String, Bitmap>) {
        this.cache = hashmap
    }

    fun setBrandingCache(hashmap: HashMap<String, Bitmap>) {
        this.brandingcache = hashmap
    }


    fun build(buildHeadsUpNotification: Boolean, state: String?): Notification? {
        createDefaultChannel()

        when (layoutType) {

            NotificationLayoutType.NOTIFICATION_TYPE_STICKY_GENERIC -> return buildNotificationLayoutOfTypeGenericSticky(buildHeadsUpNotification,
                    state)
        }

        return null
    }

    private fun buildNotificationLayoutOfTypeGenericSticky(enableHeadsUpNotification: Boolean, state: String?): Notification? {
        Log.d(TAG, "inside buildNotificationLayoutOfTypeGenericSticky:")
        val genericNotificationAsset = stickyNavModel?.getBaseNotificationAsset() as GenericNotificationAsset
        var genericDataStreamAsset: GenericDataStreamAsset? = null


        if (stickyNavModel == null || stickyNavModel
                        .baseInfo == null || stickyNavModel.getBaseNotificationAsset() !is GenericNotificationAsset) {
            Logger.d(TAG, "stickyNavModel is null :? " + (stickyNavModel == null))
            if (stickyNavModel != null) {
                Logger.d(TAG,
                        "stickyNavModel.baseInfo is null? : " + (stickyNavModel.baseInfo == null))
                Logger.d(TAG,
                        "stickymodel.notif asset is instanceof generic notif asset?: " +
                                (stickyNavModel.getBaseNotificationAsset() is GenericNotificationAsset))
            }
            return null
        }


        if (stickyNavModel.getBaseStreamAsset() != null)
            genericDataStreamAsset = stickyNavModel.getBaseStreamAsset() as GenericDataStreamAsset


        val expandedView = RemoteViews(context.packageName, R.layout
                .generic_sticky_expanded_layout)


        val remoteViews = RemoteViews(context.packageName, R.layout.generic_sticky_small_layout)

        setRemoteViews(remoteViews, genericNotificationAsset, genericDataStreamAsset, state, true)

        setRemoteViews(expandedView, genericNotificationAsset, genericDataStreamAsset, state, false)
        val channelId : String? = getChannelId(stickyNavModel.channelId)
        if(channelId == null){
            Logger.d(TAG, "channel Id is null exiting")
            return null
        }
        val builder = NotificationCompat.Builder(context, channelId)
        builder.setSmallIcon(R.mipmap.app_notification_icon)
        builder.setContentIntent(targetIntent)
        builder.setDeleteIntent(dismissedIntent)
        builder.setCustomBigContentView(expandedView)
        builder.setCustomContentView(remoteViews)
        builder.setOnlyAlertOnce(true)
        builder.setOngoing(true)
        //application of unique vs server obtained groupId will be controlled by static config
        if (!CommonUtils.isEmpty(stickyNavModel.baseInfo.group)) {
            builder.setGroup(stickyNavModel.baseInfo.group)
        }else if(!PreferenceManager.getPreference(AppStatePreference.UNIQUE_NOTIFICATION_GROUP_DISABLED, false)){
            builder.setGroup("${stickyNavModel?.baseInfo?.uniqueId?:""}_${System.currentTimeMillis()}")
        }
        builder.priority = Notification.PRIORITY_LOW

        if (enableHeadsUpNotification) {
            builder.setVibrate(LongArray(0))
        }

        return builder.build()

    }

    private fun setRemoteViews(remoteViews: RemoteViews, genericNotificationAsset: GenericNotificationAsset, genericDataStreamAsset: GenericDataStreamAsset?, state: String?, collapsed: Boolean) {

        var state = state
        var parties: List<GenericEntity>
        var title: String
        var header: GenericEntity

        var dataAsset = genericDataStreamAsset;

        if (dataAsset == null) {
            dataAsset = GenericDataStreamAsset(autoRefreshInterval = genericNotificationAsset.autoRefreshInterval
                    ?: 1000,
                    features = genericNotificationAsset?.f,
                    values = genericNotificationAsset.values)
        }

        parties = dataAsset.values.entities
        title = dataAsset.values.title
        header = dataAsset.values.header



        if (dataAsset.isLive()) {
            remoteViews.setViewVisibility(R.id.refresh_btn, View.VISIBLE)

        } else {
            remoteViews.setViewVisibility(R.id.refresh_btn, View.GONE)
        }

        if (CommonUtils.isEmpty(state)) {

            remoteViews.setInt(R.id.refresh_btn, "setBackgroundResource", R.drawable.ic_refresh)
        } else {
            remoteViews.setInt(R.id.refresh_btn, "setBackgroundResource", R.drawable.ic_updating)
        }


        if (CommonUtils.isEmpty(state)) {
            state = dataAsset.values.status
        }

        remoteViews.setTextViewText(R.id.matchStateTv, state)


        if (!CommonUtils.isEmpty(title)) {
            val charSequenceTitle = Html.fromHtml(title)
            if (charSequenceTitle != null) {
                remoteViews.setViewVisibility(R.id.titleTv, View.VISIBLE)
                remoteViews.setTextViewText(R.id.titleTv, charSequenceTitle)
            } else {
                remoteViews.setViewVisibility(R.id.titleTv, View.INVISIBLE)
            }
        } else {
            remoteViews.setViewVisibility(R.id.titleTv, View.INVISIBLE)
        }

        if (!CommonUtils.isEmpty(genericNotificationAsset.getBranding()) && brandingcache[genericNotificationAsset.getBranding().get(brandingCount)] != null)
            remoteViews.setImageViewBitmap(R.id.brandingImage, brandingcache[genericNotificationAsset.getBranding().get(brandingCount)])

        val length = parties.size

        remoteViews.removeAllViews(R.id.counting_board)
        remoteViews.removeAllViews(R.id.row1)
        remoteViews.removeAllViews(R.id.row2)
        remoteViews.removeAllViews(R.id.row3)

        if (collapsed) {

            for (i in 0 until length) {
                var bitmap: Bitmap? = cache.get(parties[i].icon)
                val entity = RemoteViews(context.packageName, R.layout.remoteview_entity_small)
                if (bitmap != null) {
                    entity.setImageViewBitmap(R.id.EntityImage, AndroidUtils.getRoundedBitmap(bitmap, CommonUtils.getDimension(R.dimen.notification_flag_width),
                            CommonUtils.getDimension(R.dimen.notification_flag_width),
                            CommonUtils.getDimension(R.dimen.notification_round_flag_radius)))
                } else {
                    setBitmapImage(parties[i].icon)
                }
                entity.setTextViewText(R.id.entityName, parties[i].name)
                entity.setTextViewText(R.id.collapseValue, parties[i].collapsedData)
                remoteViews.addView(R.id.counting_board, entity)
            }

        } else {

            val entity = RemoteViews(context.packageName, R.layout.remoteview_entity_expanded)
            entity.setTextViewText(R.id.entityName, header.name)
            entity.setViewVisibility(R.id.EntityImage, View.INVISIBLE)

            remoteViews.addView(R.id.counting_board, entity)

            if (!TextUtils.isEmpty(header.row1Data)) {
                val textView = RemoteViews(context.packageName, R.layout.remote_text_view)
                textView.setTextViewText(R.id.rowEntity, header.row1Data)
                remoteViews.addView(R.id.row1, textView)
            }
            if (!TextUtils.isEmpty(header.row1Data)) {
                val textView = RemoteViews(context.packageName, R.layout.remote_text_view)
                textView.setTextViewText(R.id.rowEntity, header.row2Data)
                remoteViews.addView(R.id.row2, textView)
            }
            if (!TextUtils.isEmpty(header.row3Data)) {
                val textView = RemoteViews(context.packageName, R.layout.remote_text_view)
                textView.setTextViewText(R.id.rowEntity, header.row3Data)
                remoteViews.addView(R.id.row3, textView)
            }

            for (i in 0 until length) {
                val entity = RemoteViews(context.packageName, R.layout.remoteview_entity_expanded)
                entity.setTextViewText(R.id.entityName, parties[i].name)
                var bitmap: Bitmap? = cache.get(parties[i].icon)
                if (bitmap != null) {
                    entity.setImageViewBitmap(R.id.EntityImage, bitmap)
                } else {
                    setBitmapImage(parties[i].icon)
                }

                remoteViews.addView(R.id.counting_board, entity)

                if (!TextUtils.isEmpty(parties[i].row1Data)) {
                    val textView = RemoteViews(context.packageName, R.layout.remote_text_view)
                    textView.setTextViewText(R.id.rowEntity, parties[i].row1Data)
                    remoteViews.addView(R.id.row1, textView)
                }
                if (!TextUtils.isEmpty(parties[i].row2Data)) {
                    val textView = RemoteViews(context.packageName, R.layout.remote_text_view)
                    textView.setTextViewText(R.id.rowEntity, parties[i].row2Data)
                    remoteViews.addView(R.id.row2, textView)
                }
                if (!TextUtils.isEmpty(parties[i].row3Data)) {
                    val textView = RemoteViews(context.packageName, R.layout.remote_text_view)
                    textView.setTextViewText(R.id.rowEntity, parties[i].row3Data)
                    remoteViews.addView(R.id.row3, textView)
                }
            }
        }


        remoteViews.setOnClickPendingIntent(R.id.refresh_btn, refreshIntent)
        remoteViews.setOnClickPendingIntent(R.id.close_btn, dismissedIntent)

    }

    private fun setBitmapImage(icon: String) {

        com.newshunt.sdk.network.image.Image.load(icon, true)
                .into(object : Image.ImageTarget() {

                    override fun onLoadFailed(errorDrawable: Drawable?) {
                        super.onLoadFailed(errorDrawable)
                        Logger.e(TAG, "Failure while downloading image  ")

                    }

                    override fun onResourceReady(bitmap: Any, transition: Transition<*>?) {

                        if (bitmap !is Bitmap) {
                            return
                        }
                        cache.put(icon, bitmap)

                        Logger.d(TAG, "onSuccess")

                    }
                })

    }

    companion object {
        private val TAG = "StickyNotifications"
    }


}
