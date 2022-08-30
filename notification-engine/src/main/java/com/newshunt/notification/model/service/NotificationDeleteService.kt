package com.newshunt.notification.model.service

import com.newshunt.common.helper.common.Logger
import com.newshunt.common.model.retrofit.RestAdapterContainer
import com.newshunt.dhutil.helper.retrofit.NewsBaseUrlContainer
import com.newshunt.notification.model.entity.DeleteNotificationPayload
import com.newshunt.notification.model.entity.NotificationId
import com.newshunt.notification.model.internal.rest.NotificationDeleteApi
import com.newshunt.notification.sqlite.NotificationDB
import com.newshunt.sdk.network.Priority
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers

class NotificationDeleteService {
    fun deleteNotifications(notifications: List<NotificationId>, cid: String)
            : Observable<Boolean> {
        if (notifications.isEmpty()) {
            return Observable.just(false)
        }
        val baseUrl = NewsBaseUrlContainer.getFullSyncUrl()
        val deleteApi = RestAdapterContainer.getInstance().getRestAdapter(baseUrl, Priority
                .PRIORITY_HIGH, null).create(NotificationDeleteApi::class.java)
        Logger.d(LOG_TAG, "Deleting notification")
        return deleteApi.deleteNotifications(DeleteNotificationPayload(cid, notifications), cid)
                .map {
                    val deleted = it.isSuccessful && it.code() == 200
                    deleted
                }.map {
                    val notificationDao = NotificationDB.instance().getNotificationDao()
                    if (!it) {
                        notificationDao.insertNotificationToDelete(notifications)
                    }
                    it
                }
    }

    fun syncDeleteNotification(cid: String) {
        val baseUrl = NewsBaseUrlContainer.getFullSyncUrl()
        val deleteApi = RestAdapterContainer.getInstance().getRestAdapter(baseUrl, Priority
                .PRIORITY_HIGH, null).create(NotificationDeleteApi::class.java)
        Logger.d(LOG_TAG, "Syncing delete notification")
        val notificationDao = NotificationDB.instance().getNotificationDao()
        val disposable = Observable.fromCallable {
            notificationDao.deleteAllDeleteNotificationSynced()
            notificationDao.getNotificationToDelete()
        }.flatMap {
            if (it.isEmpty()) {
                Observable.just(false)
            } else {
                deleteApi.deleteNotifications(DeleteNotificationPayload(cid, it as List<NotificationId>), cid)
                        .map {
                            val deleted = it.isSuccessful && it.code() == 200
                            deleted
                        }
            }
        }.map {
            if (it) {
                notificationDao.markAllNotificationSynced()
            }
            it
        }.subscribeOn(Schedulers.io()).observeOn(Schedulers.io()).subscribe({
            Logger.d(LOG_TAG, "Notification delete synced $it")
        }, {
            Logger.caughtException(it)
        })
    }

    companion object {
        private const val LOG_TAG = "NotificationDeleteService"
    }
}