/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.news.model.repo

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.JsonUtils
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.preference.GenericAppStatePreference
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.common.model.retrofit.RestAdapterContainer
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.common.model.NewsPageMode
import com.newshunt.dataentity.common.model.entity.model.ApiResponse
import com.newshunt.dataentity.common.pages.PageResponse
import com.newshunt.dataentity.common.pages.PageSyncBody
import com.newshunt.dataentity.common.pages.PageSyncList
import com.newshunt.dataentity.common.pages.S_PageEntity
import com.newshunt.dataentity.dhutil.model.entity.version.VersionDbEntity
import com.newshunt.dataentity.dhutil.model.entity.version.VersionEntity
import com.newshunt.dhutil.helper.interceptor.VersionedApiInterceptor
import com.newshunt.dhutil.helper.preference.UserPreferenceUtil
import com.newshunt.dhutil.helper.retrofit.NewsBaseUrlContainer
import com.newshunt.dhutil.model.versionedapi.VersionedApiHelper
import com.newshunt.news.analytics.NhAnalyticsAppState
import com.newshunt.news.model.apis.EntityAPI
import com.newshunt.news.model.sqlite.SocialDB
import com.newshunt.sdk.network.Priority
import io.reactivex.Observable

class PageSyncRepo(val section: String) {

  fun performPageSync() : Observable<PageResponse> {
    return Observable.fromCallable {
      return@fromCallable SocialDB.instance().pageSyncEntityDao().getAllPageSyncEntity(section)
    }.flatMap { it ->
        val version = VersionedApiHelper.getLocalVersion(entityType = VersionEntity.PAGE_ENTITY.name, parentId = section)
      if (CommonUtils.isEmpty(it)) {
        // perform the get sync
        RestAdapterContainer.getInstance().getRestAdapter(NewsBaseUrlContainer.getApplicationUrl(),
            Priority.PRIORITY_HIGHEST, this, VersionedApiInterceptor({ json -> validate(json) }))
            .create(EntityAPI::class.java).getHomePages(
            section = section,
            version = version
        ).map {
          Logger.d("HomePage", "Got the result from the server")
          updateDb(it, section)
          return@map it.data
        }
      } else {
        val pageSyncList = PageSyncList(
            SocialDB.instance().pageSyncEntityDao().getPageSyncEntities(NewsPageMode.ADDED.mode, section),
            SocialDB.instance().pageSyncEntityDao().getPageSyncEntities(NewsPageMode.MODIFIED.mode, section),
            SocialDB.instance().pageSyncEntityDao().getPageSyncEntities(NewsPageMode.DELETED.mode, section))
        RestAdapterContainer.getInstance().getRestAdapter(NewsBaseUrlContainer.getApplicationUrl(),
            Priority.PRIORITY_HIGHEST, this, VersionedApiInterceptor({ json -> validate(json) }))
            .create(EntityAPI::class.java).postHomePages(
            pageSyncBody = PageSyncBody(pageSyncList),
            section = section,
            version = version).map {
          updateDb(it, section)
          return@map it.data
        }
      }
    }
  }

  private fun updateDb(apiResponse: ApiResponse<PageResponse>, section: String) {
    Logger.d("HomePage" , "Storing the server response in the db")
    SocialDB.instance().pageEntityDao().updateAfterPageSync(apiResponse.data.rows.map {
      return@map S_PageEntity(pageEntity = it, section = section)
    }, section)
    SocialDB.instance().pageSyncEntityDao().deleteAllPageSyncEntity(section)

    //saving experiment params
    if (apiResponse.experimentGlobal != null) {
      PreferenceManager.savePreference(GenericAppStatePreference.GLOBAL_EXPERIMENT_PARAMS,
          JsonUtils.toJson(apiResponse.experimentGlobal))
      NhAnalyticsAppState.getInstance().updateGlobalExperimentParams()
    }
    if (PreferenceManager.getPreference(GenericAppStatePreference.SOCIAL_PAGESYNC_UPGRADE, false)) {
      PreferenceManager.getPreference(GenericAppStatePreference.SOCIAL_PAGESYNC_UPGRADE, false)
    }
  }

  private fun validate(json: String): String {
    if (CommonUtils.isEmpty(json)) return Constants.EMPTY_STRING

    try {
      val type = object : TypeToken<ApiResponse<PageResponse>>() {}.type
      val apiResponse = Gson().fromJson<ApiResponse<PageResponse>>(json, type)
      val versionedApiHelper = VersionedApiHelper<ApiResponse<PageResponse>>()
      return if (apiResponse != null && apiResponse.data != null && !CommonUtils.isEmpty(apiResponse.data.rows)) {
        val versionDbEntity = VersionDbEntity(entityType = VersionEntity.PAGE_ENTITY.name, version = apiResponse.data.version,
            langCode = UserPreferenceUtil.getUserLanguages(), data = json.toByteArray() , parentId = section)
        versionedApiHelper.insertVersionDbEntity(versionDbEntity)
        apiResponse.data.version
      } else {
        Constants.EMPTY_STRING
      }
    } catch (e: Exception) {
      Logger.caughtException(e)
    }

    return Constants.EMPTY_STRING
  }
}