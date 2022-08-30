/*
 * Copyright (c) 2022 Newshunt. All rights reserved.
 */
package com.newshunt.appview.common.utils

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import com.newshunt.common.helper.common.AndroidUtils
import com.newshunt.common.helper.common.Constants
import com.newshunt.dataentity.notification.InAppNotificationModel
import com.newshunt.dataentity.notification.InAppTemplateResponse
import com.newshunt.news.model.usecase.Usecase
import com.newshunt.news.model.usecase.toMediator2
import com.newshunt.notification.analytics.NotificationActionAnalyticsHelper
import com.newshunt.notification.model.entity.InAppNotificationEntity
import com.newshunt.notification.model.internal.dao.InAppNotificationDao
import com.newshunt.notification.model.internal.dao.NotificationDao
import com.newshunt.notification.model.service.NotificationTemplateService
import com.newshunt.notification.sqlite.NotificationDB
import io.reactivex.Observable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * Created by kajal.kumari on 26/04/22.
 */

object InAppNotificationUtils {

    val inAppNotificationLiveData by lazy {
        MutableLiveData<InAppNotificationModel?>()
    }

    enum class InAppState {
        CAN_SHOW, CANNOT_SHOW, COMMUNICATION_API_PRIORITIZED, OTHER_IN_APP_SHOWN
    }
    var inAppNotificationState = InAppState.CANNOT_SHOW

    fun handleInAppNotification() {
        val inAppHandleUsecase = InAppHandleUsecase().toMediator2()
        inAppHandleUsecase.execute(Unit)
    }

    fun handleInAppNotShown() {
        val inAppNotShownUsecase = InAppNotShownUsecase().toMediator2()
        inAppNotShownUsecase.execute(Unit)
    }

    fun markShownInAppNotificationStatus(id:String,status: String) {
        GlobalScope.launch(Dispatchers.IO) {
            val inAppNotificationDao: InAppNotificationDao = NotificationDB.instance().getInAppNotificationDao()
            inAppNotificationDao.markInAppNotificationStatus(id, status)
        }
    }

    fun parseSectionId(appSectionDeeplink:String?) : String? {
        appSectionDeeplink ?: return null
        return Uri.parse(appSectionDeeplink).pathSegments[3]
    }
}
class InAppHandleUsecase( private val inAppNotificationDao: InAppNotificationDao = NotificationDB.instance().getInAppNotificationDao()): Usecase<Unit,Unit> {

    override fun invoke(p1: Unit): Observable<Unit> {
        return Observable.fromCallable {
            val inAppNotificationModel = inAppNotificationDao.getExpiredInAppNotifications(System.currentTimeMillis())
            inAppNotificationModel?.let {
                it.forEach {
                    if (it != null) {
                        inAppNotificationDao.markInAppNotificationStatus(it.baseInfo.id, Constants.EXPIRED)
                        NotificationActionAnalyticsHelper.logInAppNotificationNotDisplayedEvent(it,Constants.NO_USER_SESSION)
                    }
                }
            }
        }.map {
            //sort by priority and get the top most notification
            val inAppNotificationBaseModel = inAppNotificationDao.getHighestPriorityInAppNotificationsToBeShown()
            if (!inAppNotificationBaseModel.isNullOrEmpty()) {
                var delay = inAppNotificationBaseModel[0]?.baseInfo?.inAppInfo?.startTimeMs?.minus(System.currentTimeMillis())
                if (delay != null) {
                    if(delay < 0) delay = 0
                    AndroidUtils.getMainThreadHandler().postDelayed({
                        InAppNotificationUtils.inAppNotificationLiveData.postValue(inAppNotificationBaseModel[0])
                    }, delay)
                }
            }
        }
    }

}


class InAppNotShownUsecase( private val inAppNotificationDao: InAppNotificationDao = NotificationDB.instance().getInAppNotificationDao()): Usecase<Unit,Unit> {

    override fun invoke(p1: Unit): Observable<Unit> {
        return Observable.fromCallable {
            val inAppNotificationModel = inAppNotificationDao.getExpiredInAppNotifications(System.currentTimeMillis())
            inAppNotificationModel?.let {
                it.forEach {
                    if (it != null) {
                        inAppNotificationDao.markInAppNotificationStatus(it.baseInfo.id, Constants.EXPIRED)
                        NotificationActionAnalyticsHelper.logInAppNotificationNotDisplayedEvent(it,Constants.NO_USER_SESSION)
                    }
                }
            }
        }.map {
            val inAppNotificationBaseModel = inAppNotificationDao.getHighestPriorityInAppNotificationsToBeShown()
            if(!inAppNotificationBaseModel.isNullOrEmpty()) {
                if (InAppNotificationUtils.inAppNotificationState == InAppNotificationUtils.InAppState.COMMUNICATION_API_PRIORITIZED) {
                    NotificationActionAnalyticsHelper.logInAppNotificationNotDisplayedEvent(inAppNotificationBaseModel[0]!!, Constants.COMMUNICATION_API_PRIORITIZED)
                } else {
                    NotificationActionAnalyticsHelper.logInAppNotificationNotDisplayedEvent(inAppNotificationBaseModel[0]!!, Constants.OTHER_IN_APP_PRIORITIZED)
                }
            }
        }
    }

}

class TemplateUsecase(val notificationTemplateService: NotificationTemplateService) : Usecase<Unit, InAppTemplateResponse> {
    override fun invoke(t:Unit): Observable<InAppTemplateResponse> {
        return notificationTemplateService.getStoredTemplates()
    }
}