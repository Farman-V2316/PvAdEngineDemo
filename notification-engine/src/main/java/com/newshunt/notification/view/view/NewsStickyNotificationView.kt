package com.newshunt.notification.view.view

import android.app.PendingIntent
import android.content.Intent
import android.graphics.Bitmap
import android.util.TypedValue
import android.view.View
import android.widget.RemoteViews
import androidx.annotation.NonNull
import androidx.core.app.NotificationCompat
import com.newshunt.common.helper.common.AndroidUtils
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.dataentity.analytics.entity.NhAnalyticsEventParam
import com.newshunt.dataentity.analytics.referrer.NhGenericReferrer
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.notification.BaseModel
import com.newshunt.dataentity.notification.BaseModelType
import com.newshunt.dataentity.notification.NewsNavModel
import com.newshunt.dataentity.notification.NotificationDeliveryMechanism
import com.newshunt.dataentity.notification.StickyNavModel
import com.newshunt.dataentity.notification.asset.BaseDataStreamAsset
import com.newshunt.dataentity.notification.asset.BaseNotificationAsset
import com.newshunt.dataentity.notification.asset.DataStreamResponse
import com.newshunt.dataentity.notification.asset.NewsStickyDataStreamAsset
import com.newshunt.dataentity.notification.util.NotificationConstants
import com.newshunt.dhutil.helper.preference.AppStatePreference
import com.newshunt.news.analytics.NhAnalyticsNewsEventParam
import com.newshunt.notification.R
import com.newshunt.notification.analytics.NhAnalyticsNotificationEventParam
import com.newshunt.notification.helper.EMPTY_RESPONSE
import com.newshunt.notification.helper.NotificationActionAnalytics
import com.newshunt.notification.helper.NotificationUtils
import com.newshunt.notification.helper.getChannelId
import com.newshunt.notification.helper.logStickyActionEvent
import com.newshunt.notification.helper.logStickyExploreButtonClicks
import com.newshunt.notification.helper.logStickyUndisplayedEvent
import com.newshunt.notification.view.service.NewsStickyService
import com.newshunt.notification.view.service.StickyNotificationRefresher


class NewsStickyNotificationView(val stickyNavModel: StickyNavModel<BaseNotificationAsset, BaseDataStreamAsset>, @NonNull
val refresher: StickyNotificationRefresher, var callback: NewsStickyService? = null): StickyNotificationView {
    val REQ_CODE_SETTINGS = 3001
    val REQ_CODE_NEXT = 3002
    val REQ_CODE_PREV = 3003
    val REQ_CODE_CLICK = 3004
    val TAG = "NewsStickyServiceNotificationView"
    var indicatorPositionNetCorrection = -1


    override fun buildNotification(isUpdate: Boolean, enableHeadsUpNotificatio: Boolean, state: String?) {
    }

    override fun handleStreamDataSuccess(dataStreamResponse: DataStreamResponse?) {
        Logger.d(TAG, "data stream success enter")
        try{
            if (dataStreamResponse == null || (dataStreamResponse?.baseStreamAsset as? NewsStickyDataStreamAsset) == null) {
                logStickyUndisplayedEvent(stickyNavModel = stickyNavModel, reason = EMPTY_RESPONSE)
                return
            }

            callback?.handleStreamResponse(dataStreamResponse)
        }catch (ex: Exception){

        }
    }

    override fun handleStreamDataError(dataStreamResponse: DataStreamResponse?) {
        callback?.handleStreamError(dataStreamResponse)
    }

    override fun handleAction(action: String, intent: Intent) {
        action?.let {
            when(action){
                NotificationConstants.INTENT_ACTION_GO_TO_PREV_ITEM -> {
                    val ind = intent.getIntExtra(NotificationConstants.INTENT_EXTRA_NEWS_STICKY_ITEM_INDEX, -1)
                    val analyticsInd = intent.getIntExtra(NotificationConstants.INTENT_EXTRA_NEWS_STICKY_ANALYTICS_ITEM_INDEX, -1)
                    var id = intent.getStringExtra(NotificationConstants.INTENT_EXTRA_ITEM_ID)
                    id = id ?: Constants.EMPTY_STRING
                    val notificationId = intent.getStringExtra(NotificationConstants.INTENT_EXTRA_NOT_ID)?:null
                    val referrer = PageReferrer(NhGenericReferrer.NOTIFICATION, id)
                    val map = mutableMapOf<NhAnalyticsEventParam, Any>()
                    map.put(NhAnalyticsNotificationEventParam.ITEM_INDEX, analyticsInd)
                    map.put(NhAnalyticsNewsEventParam.TYPE, "prev")
                    logStickyExploreButtonClicks(map, referrer)
                    callback?.handlePrevNextClick(ind, false, notificationId)
                }
                NotificationConstants.INTENT_ACTION_GO_TO_NEXT_ITEM -> {
                    val ind = intent.getIntExtra(NotificationConstants.INTENT_EXTRA_NEWS_STICKY_ITEM_INDEX, -1)
                    val analyticsInd = intent.getIntExtra(NotificationConstants.INTENT_EXTRA_NEWS_STICKY_ANALYTICS_ITEM_INDEX, -1)
                    var id = intent.getStringExtra(NotificationConstants.INTENT_EXTRA_ITEM_ID)
                    id = id ?: Constants.EMPTY_STRING
                    val notificationId = intent.getStringExtra(NotificationConstants.INTENT_EXTRA_NOT_ID)?:null
                    val referrer = PageReferrer(NhGenericReferrer.NOTIFICATION, id)
                    val map = mutableMapOf<NhAnalyticsEventParam, Any>()
                    map.put(NhAnalyticsNotificationEventParam.ITEM_INDEX, analyticsInd)
                    map.put(NhAnalyticsNewsEventParam.TYPE, "next")
                    logStickyExploreButtonClicks(map, referrer)
                    callback?.handlePrevNextClick(ind, true, notificationId)
                }
                NotificationConstants.INTENT_STICKY_NOTIFICATION_CANCEL_ONGOING -> {
                    callback?.onCancelNewsSticky(intent.getBooleanExtra(NotificationConstants.INTENT_EXTRA_FROM_NEWS_STICKY, false),
                            intent.getBooleanExtra(NotificationConstants.INTENT_EXTRA_FROM_INBOX, false))
                }
                NotificationConstants.INTENT_ACTION_NEWS_STICKY_ITEM_CLICK -> {
                    val ind = intent.getIntExtra(NotificationConstants.INTENT_EXTRA_NEWS_STICKY_ITEM_INDEX, -1)
                    if (ind >= 0) {
                        try{
                            logStickyActionEvent(null, stickyNavModel, NotificationDeliveryMechanism.PULL, (getBaseModelInfoFromIntent(intent) as? NewsNavModel), NotificationActionAnalytics.CLICK.action)
                        }catch(ex: java.lang.Exception){
                            Logger.caughtException(ex)
                        }
                        callback?.handleItemClicked(ind)

                    } else {
                        Logger.d(TAG, "Wrong index for item click")
                    }
                }
                NotificationConstants.INTENT_ACTION_NEWS_STICKY_GO_TO_SETTINGS ->{
                    try{
                        logStickyActionEvent(null, stickyNavModel, NotificationDeliveryMechanism.PULL, (getBaseModelInfoFromIntent(intent) as? NewsNavModel), NotificationActionAnalytics.CROSS_DELETE.action)
                    }catch(ex: java.lang.Exception){
                        Logger.caughtException(ex)
                    }
                }
                NotificationConstants.INTENT_ACTION_NOTIFICATION_CLEAR_ALL -> {
                    callback?.onNotificationClearAll()
                }
                NotificationConstants.INTENT_ACTION_NOTIFICATION_RECEIVED ->{
                    val id = intent.getIntExtra(NotificationConstants.INTENT_EXTRA_ITEM_ID, -1)
                    if(id != -1){
                        callback?.onNormalNotificationReceived(id)
                    }else{
                        Logger.d(TAG, "Wrong id received")
                    }
                }
                else -> {

                }
            }
        }
    }

    override fun setServiceCallbackAsNull() {
        callback = null
    }

    override fun handleAudioChanged(intent: Intent?) {
        //Not required for news sticky
    }

    public fun buildNotification(items: List<BaseModel>?, itemBitmaps: List<Bitmap?>?, isUpdate: Boolean, enableHeadsUpNotification: Boolean, channelId: String?, priority: Int?, indicatorPositionCorrection: Int?, useIndicatorCorrection: Boolean){

        try{
            val collapsedContentView = RemoteViews(CommonUtils.getApplication().packageName, R.layout.news_sticky_remote_layout)
            val expandedContentView = RemoteViews(CommonUtils.getApplication().packageName, R.layout.news_sticky_expanded_remote_layout)
            val expandedRootView = RemoteViews(CommonUtils.getApplication().packageName, R.layout.news_sticky_expanded_container_layout)
            val collapsedRootView = RemoteViews(CommonUtils.getApplication().packageName, R.layout.news_sticky_collapsed_container_layout)
            collapsedContentView.removeAllViews(R.id.news_sticky_viewflipper)
            expandedContentView.removeAllViews(R.id.news_sticky_expanded_viewflipper)

            if(items != null){
                if(items.size>0){
                    if(useIndicatorCorrection){
                        if(indicatorPositionNetCorrection < 0){
                            indicatorPositionNetCorrection = indicatorPositionCorrection?:0
                        }
                        else if(indicatorPositionCorrection?:0 < 0) {
                            //Do not update in this case, -ve value means we have to maintain the previous correctionValue
                        }else {
                            indicatorPositionNetCorrection = (indicatorPositionNetCorrection + (indicatorPositionCorrection?:0)) % (items.size)
                        }
                    }else{
                        indicatorPositionNetCorrection = -1
                    }
                }
                for(i in 0 until items.size){
                    val notificationFontSize = PreferenceManager.getPreference(AppStatePreference.NOTIFICATION_FONT_SIZE, 0.0f);
                    val item = items.get(i)
                    var itemBitmap : Bitmap? = null
                    if(itemBitmaps != null && itemBitmaps.size == items.size){
                        itemBitmap = itemBitmaps.get(i)
                    }
                    val collapsedChildView = RemoteViews(CommonUtils.getApplication().packageName, R.layout.sticky_news_item_remote_layout)
                    val expandedChildView = RemoteViews(CommonUtils.getApplication().packageName, R.layout.sticky_news_item_expanded_remote_layout)
                    item?.baseInfo?.let{
                        val title = AndroidUtils.getRichTextFromHtml(NotificationUtils.getNotificationContentText(item.baseInfo))
                        collapsedChildView.setTextViewText(R.id.news_sticky_notification_text, title)
                        expandedChildView.setTextViewText(R.id.news_sticky_notification_text, title)
                        setFontSize(collapsedChildView, R.id.news_sticky_notification_text, notificationFontSize)
                        setFontSize(expandedChildView, R.id.news_sticky_notification_text, notificationFontSize)
                    }

                    //Indicator drawing
                    for(y in 0 until items.size){
                        val correctedI = ((if(indicatorPositionNetCorrection < 0) 0 else indicatorPositionNetCorrection) + i)%(items.size)
                        if(y == correctedI){
                            expandedChildView.addView(R.id.item_position_indicator, RemoteViews(CommonUtils.getApplication().packageName, R.layout.filled_circle_indicator_item))
                        }else{
                            expandedChildView.addView(R.id.item_position_indicator, RemoteViews(CommonUtils.getApplication().packageName, R.layout.empty_circle_indicator_item))
                        }
                    }

                    val clickIntent = NotificationUtils.getNotificationRouterIntent(item)
                    clickIntent.putExtra(NotificationConstants.INTENT_EXTRA_NEWS_STICKY_ITEM_INDEX, i)
                    clickIntent.putExtra(NotificationConstants.INTENT_EXTRA_FROM_NEWS_STICKY, true)
                    clickIntent.putExtra(NotificationConstants.INTENT_EXTRA_ITEM_ID, item.baseInfo?.id ?: Constants.EMPTY_STRING)
                    val pendingClickIntent = PendingIntent.getActivity(CommonUtils.getApplication(), (REQ_CODE_CLICK + i * 100), clickIntent, PendingIntent.FLAG_CANCEL_CURRENT)
                    collapsedChildView.setOnClickPendingIntent(R.id.news_sticky_root, pendingClickIntent)
                    expandedChildView.setOnClickPendingIntent(R.id.news_sticky_expanded_root, pendingClickIntent)

                    val settingsIntent = NotificationUtils.getSettingsIntent()
                    settingsIntent.putExtra(NotificationConstants.INTENT_EXTRA_FROM_NEWS_STICKY, true)
                    addBaseModelExtrasToIntent(settingsIntent, item)
                    val pendingSettingsIntent = PendingIntent.getActivity(CommonUtils.getApplication(), (REQ_CODE_SETTINGS + i * 100), settingsIntent, PendingIntent.FLAG_CANCEL_CURRENT)
                    collapsedChildView.setOnClickPendingIntent(R.id.news_sticky_cross_btn, pendingSettingsIntent)
                    expandedChildView.setOnClickPendingIntent(R.id.news_sticky_expanded_cross_btn, pendingSettingsIntent)
                    collapsedChildView.setImageViewResource(R.id.news_sticky_cross_btn, R.drawable.ic_cross_news_sticky)
                    expandedChildView.setImageViewResource(R.id.news_sticky_expanded_cross_btn, R.drawable.ic_cross_news_sticky)

                    val prevIntent = Intent(NotificationConstants.INTENT_ACTION_GO_TO_PREV_ITEM)
                    prevIntent.`package` = CommonUtils.getApplication().packageName
                    prevIntent.putExtra(NotificationConstants.INTENT_EXTRA_NEWS_STICKY_ITEM_INDEX, i)
                    prevIntent.putExtra(NotificationConstants.INTENT_EXTRA_ITEM_ID, item.itemId?:item.baseInfo?.id?: Constants.EMPTY_STRING)
                    prevIntent.putExtra(NotificationConstants.INTENT_EXTRA_NEWS_STICKY_ANALYTICS_ITEM_INDEX,
                            (((if(indicatorPositionNetCorrection < 0) 0 else indicatorPositionNetCorrection) + i)%(items.size)) + 1)
                    prevIntent.putExtra(NotificationConstants.INTENT_EXTRA_NOT_ID, item.baseInfo?.uniqueId?.toString()?:Constants.EMPTY_STRING)
                    val pendingPrevItemIntent = PendingIntent.getBroadcast(CommonUtils.getApplication(), (REQ_CODE_PREV + i * 100), prevIntent, PendingIntent.FLAG_CANCEL_CURRENT)
                    expandedChildView.setOnClickPendingIntent(R.id.news_sticky_prev_btn, pendingPrevItemIntent)
                    expandedChildView.setImageViewResource(R.id.news_sticky_prev_btn, R.drawable.ic_prev_news_sticky)

                    val nextIntent = Intent(NotificationConstants.INTENT_ACTION_GO_TO_NEXT_ITEM)
                    nextIntent.putExtra(NotificationConstants.INTENT_EXTRA_NEWS_STICKY_ITEM_INDEX, i)
                    nextIntent.putExtra(NotificationConstants.INTENT_EXTRA_ITEM_ID, item.itemId?:item.baseInfo?.id?: Constants.EMPTY_STRING)
                    nextIntent.putExtra(NotificationConstants.INTENT_EXTRA_NEWS_STICKY_ANALYTICS_ITEM_INDEX,
                            (((if(indicatorPositionNetCorrection < 0) 0 else indicatorPositionNetCorrection) + i)%(items.size) + 1))
                    nextIntent.putExtra(NotificationConstants.INTENT_EXTRA_NOT_ID, item.baseInfo?.uniqueId?.toString()?:Constants.EMPTY_STRING)
                    val pendingNextItemIntent = PendingIntent.getBroadcast(CommonUtils.getApplication(), (REQ_CODE_NEXT + i * 100), nextIntent, PendingIntent.FLAG_CANCEL_CURRENT)
                    collapsedChildView.setOnClickPendingIntent(R.id.news_sticky_next_btn, pendingNextItemIntent)
                    expandedChildView.setOnClickPendingIntent(R.id.news_sticky_next_btn, pendingNextItemIntent)
                    collapsedChildView.setImageViewResource(R.id.news_sticky_next_btn, R.drawable.ic_next_news_sticky)
                    expandedChildView.setImageViewResource(R.id.news_sticky_next_btn, R.drawable.ic_next_news_sticky)
                    nextIntent.`package` = CommonUtils.getApplication().packageName


                    if(itemBitmap != null){
                        collapsedChildView.setImageViewBitmap(R.id.news_sticky_notify_image, AndroidUtils.getRoundedBitmap(itemBitmap, CommonUtils.getDimension(R.dimen.image_size), CommonUtils.getDimension(R.dimen.image_size), CommonUtils.getDimension(R.dimen.news_sticky_image_radius)))
                        expandedChildView.setImageViewBitmap(R.id.news_sticky_notify_image, AndroidUtils.getRoundedBitmap(itemBitmap, CommonUtils.getDimension(R.dimen.image_size), CommonUtils.getDimension(R.dimen.image_size), CommonUtils.getDimension(R.dimen.news_sticky_image_radius)))
                        collapsedChildView.setViewVisibility(R.id.news_sticky_notify_default_image, View.GONE)
                        collapsedChildView.setViewVisibility(R.id.news_sticky_notify_image, View.VISIBLE)
                        expandedChildView.setViewVisibility(R.id.news_sticky_notify_default_image, View.GONE)
                        expandedChildView.setViewVisibility(R.id.news_sticky_notify_image, View.VISIBLE)
                    }else{
                        collapsedChildView.setViewVisibility(R.id.news_sticky_notify_default_image, View.VISIBLE)
                        collapsedChildView.setViewVisibility(R.id.news_sticky_notify_image, View.GONE)
                        collapsedChildView.setViewVisibility(R.id.news_sticky_notification_logo, View.GONE)
                        expandedChildView.setViewVisibility(R.id.news_sticky_notify_default_image, View.VISIBLE)
                        expandedChildView.setViewVisibility(R.id.news_sticky_notify_image, View.GONE)
                        expandedChildView.setViewVisibility(R.id.news_sticky_notification_logo, View.GONE)
                    }

                    expandedChildView.setTextViewText(R.id.sticky_news_expanded_description, AndroidUtils.getTextFromHtml(item.description))
                    collapsedContentView.addView(R.id.news_sticky_viewflipper, collapsedChildView)
                    expandedContentView.addView(R.id.news_sticky_expanded_viewflipper, expandedChildView)


                }
                val flipInterval = PreferenceManager.getPreference(AppStatePreference.NEWS_STICKY_AUTO_SCROLL_TIME, 5000)
                expandedRootView.removeAllViews(R.id.news_sticky_expanded_container_layout)
                collapsedRootView.removeAllViews(R.id.news_sticky_collapsed_container_layout)
                expandedRootView.addView(R.id.news_sticky_expanded_container_layout, expandedContentView)
                collapsedRootView.addView(R.id.news_sticky_collapsed_container_layout, collapsedContentView)
                expandedContentView.setInt(R.id.news_sticky_expanded_viewflipper, "setFlipInterval", flipInterval)
                collapsedContentView.setInt(R.id.news_sticky_viewflipper, "setFlipInterval", flipInterval)
            }

            var channel = getChannelId(stickyNavModel.channelId)
            if(!channelId.isNullOrEmpty()){
                channel = getChannelId(channelId)
            }
            var updatedPriority = stickyNavModel.baseInfo.priority
            if(priority != null){
                updatedPriority = priority
            }
            if (channel == null) {
                Logger.d(TAG, "channel Id is null exiting")
                return;
            }
            val builder = NotificationCompat.Builder(CommonUtils.getApplication(),
                    channel)
            builder.setSmallIcon(R.mipmap.app_notification_icon)
            builder.setCustomBigContentView(expandedRootView)
            builder.setOngoing(true)
            builder.setCustomContentView(collapsedRootView)

            builder.priority = updatedPriority

            if(items == null){
                builder.setOnlyAlertOnce(false)
            }else{
                builder.setOnlyAlertOnce(true)
            }
            //application of unique groupId will be controlled by static config
            if(!PreferenceManager.getPreference(AppStatePreference.UNIQUE_NOTIFICATION_GROUP_DISABLED, false)){
                builder.setGroup("${NotificationConstants.NEWS_STICKY_NOTIF_ID}_${System.currentTimeMillis()}")
            }
            val notification = builder.build()
            Logger.d(TAG, "Number of sticky items to be shown is ${items?.size?:0}")
            callback?.addNotificationToTray(NotificationConstants.NEWS_STICKY_NOTIF_ID, notification, isUpdate)
        }catch(ex: Exception){
            Logger.caughtException(ex)
        }

    }

    private fun setFontSize(remoteViews: RemoteViews, textViewId: Int, fontSize: Float){
        var notificationFontSize = fontSize
        if(fontSize <= 0){
            notificationFontSize = CommonUtils.getDimension(R.dimen.default_notification_text_size).toFloat()
        }

        remoteViews.setTextViewTextSize(textViewId, TypedValue.COMPLEX_UNIT_SP, notificationFontSize)
    }

    private fun addBaseModelExtrasToIntent(intent: Intent, notificationModel: BaseModel): Intent{
        intent.putExtra(Constants.NOTIFICATION_BASE_MODEL,
                BaseModelType.convertModelToString(notificationModel))
        if (notificationModel is StickyNavModel<*, *>) {
            intent.putExtra(Constants.NOTIFICATION_BASE_MODEL_STICKY_TYPE,
                    (notificationModel as StickyNavModel<*, *>).stickyType)
        }
        if (notificationModel.getBaseModelType() != null) {
            intent.putExtra(Constants.NOTIFICATION_BASE_MODEL_TYPE,
                    notificationModel.getBaseModelType().name)
        }
        return intent
    }

    private fun getBaseModelInfoFromIntent(intent: Intent): BaseModel?{
        try{
            val modelString = intent.getStringExtra(Constants.NOTIFICATION_BASE_MODEL)
            val modelTypeString = intent.getStringExtra(Constants.NOTIFICATION_BASE_MODEL_TYPE)
            val stickyTypeString = intent.getStringExtra(Constants.NOTIFICATION_BASE_MODEL_STICKY_TYPE)
            val baseModel = BaseModelType.convertStringToBaseModel(modelString, BaseModelType.getValue(modelTypeString), stickyTypeString)
            return baseModel
        }catch(ex: Exception){
            Logger.caughtException(ex)
        }
        return null
    }

}