package com.newshunt.notification.helper

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.core.app.NotificationManagerCompat
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.info.ClientInfoHelper
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.common.model.retrofit.RestAdapterContainer
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.common.model.entity.model.ApiResponse
import com.newshunt.dataentity.notification.NotificationDeliveryMechanism
import com.newshunt.dataentity.notification.util.NotificationConstants
import com.newshunt.dhutil.helper.AppSettingsProvider
import com.newshunt.dhutil.helper.preference.AppStatePreference
import com.newshunt.dhutil.helper.retrofit.NewsBaseUrlContainer
import com.newshunt.notification.model.entity.server.NotificationSyncResponse
import com.newshunt.notification.model.internal.rest.NotificationSyncAPI
import com.newshunt.notification.sqlite.NotificationDB
import com.newshunt.sdk.network.Priority
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable

/**
 * Created by karthik.r on 2019-11-08.
 */
const val NOTIFICATION_FILTER_SOCIAL = 1
const val NOTIFICATION_FILTER_CONTENT = 2
const val NOTIFICATION_FILTER_ALL = 3

class NotificationSyncHelper {

    companion object {

        var syncRunning: Boolean = false
        var fetchRequestRunning: Boolean = false

        fun fetchNextPage(filter: String) {
            if (fetchRequestRunning) {
                return
            }
            fetchRequestRunning = true
            val bottomMarker : String? = PreferenceManager.getPreference(AppStatePreference
                    .FIRST_MARKER, Constants.EMPTY_STRING)
            if (!CommonUtils.isEmpty(bottomMarker)) {
                fetch(NotificationConstants.DIRECTION_DOWN, bottomMarker, true, filter, false)
            }
        }

        fun fetchPreviousPage(filter: String) {
            if (fetchRequestRunning) {
                return
            }
            fetchRequestRunning = true
            val topMarker = PreferenceManager.getPreference<String>(AppStatePreference.LAST_MARKER, Constants.EMPTY_STRING)
            if (!CommonUtils.isEmpty(topMarker)) {
                fetch(NotificationConstants.DIRECTION_UP, topMarker, true, filter, false)
            }
        }

        fun fetchFirstPage(updateUI: Boolean = false, filter: String?) {
            fetch(null, null, updateUI, filter, true, true)
        }

        fun fetch(direction: String?, pageMarker: String?, updateUI: Boolean,
                  filterValue: String?, clearOldNotification: Boolean = false,
                  isFirstPage: Boolean = false) {
            val api = RestAdapterContainer.getInstance().getDynamicRestAdapterRx(CommonUtils
                    .formatBaseUrlForRetrofit(NewsBaseUrlContainer.getFullSyncUrl()),
                    Priority.PRIORITY_HIGHEST, "").create(NotificationSyncAPI::class.java)

            var clientId = ClientInfoHelper.getClientId()
            val notificationEnabled = NotificationManagerCompat.from(CommonUtils.getApplication()).areNotificationsEnabled()
            val fullSyncPageSize: Int? = if (isFirstPage) null else NotificationConstants.DEFAULT_PAGE_SIZE
            val notiFullSyncUrl = NewsBaseUrlContainer.getFullSyncUrl()
            val filter = if (filterValue != null && filterValue.equals(Constants.NOTIFICATION_FILTER_TYPE_ALL)) {
                null
            } else {
                filterValue
            }
            api.syncNotifications(notiFullSyncUrl,
                    clientId, notificationEnabled, filter, direction, fullSyncPageSize, pageMarker)
                    .subscribeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            object : Observer<ApiResponse<NotificationSyncResponse>> {
                                override fun onSubscribe(d: Disposable) {

                                }

                                override fun onNext(
                                        notificationSyncResponseApiResponse: ApiResponse<NotificationSyncResponse>) {
                                    syncRunning = true
                                    if (direction == null ) {
                                        val lastMarker = notificationSyncResponseApiResponse.data?.last_marker
                                                ?: Constants.EMPTY_STRING
                                        PreferenceManager.savePreference(AppStatePreference.LAST_MARKER, lastMarker)
                                        val firstMarker = notificationSyncResponseApiResponse.data?.first_marker
                                                ?: Constants.EMPTY_STRING
                                        PreferenceManager.savePreference(AppStatePreference.FIRST_MARKER, firstMarker)
                                    } else if (NotificationConstants.DIRECTION_UP == direction) {
                                        // Save top marker
                                        val lastMarker = notificationSyncResponseApiResponse.data?.last_marker
                                                ?: Constants.EMPTY_STRING
                                        PreferenceManager.savePreference(AppStatePreference.LAST_MARKER, lastMarker)
                                    } else {
                                        // Save bottom marker
                                        val firstMarker = notificationSyncResponseApiResponse.data?.first_marker
                                                ?: Constants.EMPTY_STRING
                                        PreferenceManager.savePreference(AppStatePreference.FIRST_MARKER, firstMarker)
                                    }

                                    if (notificationSyncResponseApiResponse.data?.notifications != null) {
                                        insertNotifications(notificationSyncResponseApiResponse.data.notifications)
                                    }

                                    syncRunning = false

                                    if (updateUI){
                                        loadContentInUI()
                                    }
                                }

                                override fun onError(e: Throwable) {
                                    syncRunning = false
                                    if (updateUI){
                                        loadContentInUI()
                                    }
                                }

                                override fun onComplete() {
                                    syncRunning = false
                                    fetchRequestRunning = false
                                }
                            })
        }

        fun loadContentInUI() {
            val handler = Handler(Looper.getMainLooper())
            handler.post { AppSettingsProvider.getNotificationLiveData().postValue(true) }
        }

        private fun insertNotifications(pullNotifications: JsonArray?) {
            if (pullNotifications == null) {
                return
            }

            for (i in 0 until pullNotifications.size()) {
                try {
                    val jsonElement = pullNotifications.get(i) ?: continue
                    val notificationDataJson = jsonElement.asJsonObject ?: continue
                    val notificationJson = notificationDataJson.get(NotificationConstants.NOTIFICATION_DATA_FIELD) as JsonObject
                    val bundle = Bundle()
                    notificationJson.keySet().forEach {
                        bundle.putString(it, notificationJson.get(it).asString)
                    }

                    // saves notification with synced = true.
                    NotificationHandler.handleNotificationData(NotificationDeliveryMechanism.PULL, bundle,
                            true,
                            NOTIFICATION_FILTER_ALL)
                } catch (e: Exception) {
                    Logger.caughtException(e)
                }

            }
        }

        public fun getFilterValue(filter: String?): Int {
            filter ?: return NOTIFICATION_FILTER_ALL
            if (filter.equals("CONTENT")) {
                return NOTIFICATION_FILTER_CONTENT
            }
            if (filter.equals("SOCIAL")) {
                return NOTIFICATION_FILTER_SOCIAL
            }
            return NOTIFICATION_FILTER_ALL
        }
    }
}