package com.newshunt.notification.model.service

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.common.model.entity.model.MultiValueResponse
import com.newshunt.dataentity.dhutil.model.entity.version.VersionDbEntity
import com.newshunt.dhutil.helper.interceptor.VersionedApiInterceptor
import com.newshunt.dhutil.helper.preference.UserPreferenceUtil
import com.newshunt.dhutil.helper.retrofit.RestAdapterProvider
import com.newshunt.dataentity.dhutil.model.entity.version.VersionEntity
import com.newshunt.dataentity.dhutil.model.entity.version.VersionedApiEntity
import com.newshunt.dhutil.model.versionedapi.VersionedApiHelper
import com.newshunt.dataentity.notification.asset.OptInEntity
import com.newshunt.notification.model.internal.rest.StreamAPI
import com.newshunt.notification.model.manager.StickyNotificationsManager
import com.newshunt.sdk.network.Priority
import io.reactivex.Observable

object ServerNotificationServiceImpl {

  private val versionApiEntity = VersionedApiEntity(VersionEntity.SERVER_OPT_IN)
  private val versionedApiHelper = VersionedApiHelper<MultiValueResponse<OptInEntity>>()

  fun fetchNotificationFromServer(version : String) : Observable<MultiValueResponse<OptInEntity>> {
    val serverApi  = RestAdapterProvider.getRestAdapter(Priority.PRIORITY_HIGH, null,
        VersionedApiInterceptor({ json: String -> this.validate(json) }))
        .create(StreamAPI::class.java)
    return serverApi.getServerNotifications(UserPreferenceUtil.getUserNavigationLanguage(), version)
  }

  fun validate(json: String) : String {
    if (CommonUtils.isEmpty(json)) {
      return Constants.EMPTY_STRING
    }

    try {
      val type = object : TypeToken<MultiValueResponse<OptInEntity>>() {}.type
      val response = Gson().fromJson<MultiValueResponse<OptInEntity>>(json, type)
      return if (response == null) {
        Constants.EMPTY_STRING
      } else {
        Logger.d(StickyNotificationsManager.TAG, "Received opt ins from server: ")
        val versionDbEntity = VersionDbEntity(entityType = versionApiEntity.entityType, version = response.version,
            langCode = UserPreferenceUtil.getUserLanguages(), data = json.toByteArray())
        versionedApiHelper.insertVersionDbEntity(versionDbEntity)
        StickyNotificationsManager.serverOptInNotifications(response.rows)
        response.version
      }
    } catch (e: Exception) {
      Logger.caughtException(e)
    }
    return Constants.EMPTY_STRING

  }
}