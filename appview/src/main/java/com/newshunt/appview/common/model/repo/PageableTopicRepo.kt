package com.newshunt.appview.common.model.repo

import androidx.lifecycle.LiveData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.newshunt.news.model.apis.EntityAPI
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger
import com.newshunt.dataentity.common.model.NewsPageMode
import com.newshunt.dataentity.common.model.entity.model.ApiResponse
import com.newshunt.common.model.retrofit.RestAdapterContainer
import com.newshunt.dataentity.common.pages.AddPageEntity
import com.newshunt.dataentity.common.pages.PageEntity
import com.newshunt.dataentity.common.pages.PageResponse
import com.newshunt.dataentity.common.pages.PageSyncEntity
import com.newshunt.dataentity.common.pages.PageableTopicsEntity
import com.newshunt.dataentity.common.pages.S_PageEntity
import com.newshunt.dataentity.common.pages.TopicsEntity
import com.newshunt.dataentity.dhutil.model.entity.version.VersionDbEntity
import com.newshunt.dhutil.helper.interceptor.VersionedApiInterceptor
import com.newshunt.dhutil.helper.preference.UserPreferenceUtil
import com.newshunt.dataentity.dhutil.model.entity.version.VersionEntity
import com.newshunt.dhutil.model.versionedapi.VersionedApiHelper
import com.newshunt.dataentity.news.model.entity.server.navigation.NavigationTree
import com.newshunt.dhutil.helper.interceptor.NewsListErrorResponseInterceptor
import com.newshunt.dhutil.helper.retrofit.NewsBaseUrlContainer
import com.newshunt.news.model.sqlite.SocialDB
import com.newshunt.sdk.network.Priority
import io.reactivex.Observable

class PageableTopicRepo(val section: String) {

  fun getPageableTopicsFromServer() : Observable<PageResponse> {

    return Observable.fromCallable {
      return@fromCallable VersionedApiHelper.getLocalVersion(entityType = VersionEntity.PAGEABLE_TOPICS.name,
          parentId = section)?: Constants.EMPTY_STRING
    }.flatMap { version ->
          RestAdapterContainer.getInstance().getRestAdapter(NewsBaseUrlContainer.getApplicationUrl(),
              Priority.PRIORITY_HIGHEST, this, VersionedApiInterceptor({ json -> validate(json) }),
                  NewsListErrorResponseInterceptor()).create(EntityAPI::class.java).getPageableTopics(version = version, section =
          section).map { main ->
            main.data.rows?.let { list ->
              SocialDB.instance().pageableTopicsDao().replaceTopics(list.map { TopicsEntity(it, section)}, section)
            }
            return@map main.data
          }
        }
  }

  fun getPageableTopics() : LiveData<List<PageableTopicsEntity>> {
    return SocialDB.instance().pageableTopicsDao().getPageableTopics(section)
  }

  private fun validate(json: String) : String {
    if (CommonUtils.isEmpty(json)) return Constants.EMPTY_STRING
    try {
      val type = object : TypeToken<ApiResponse<PageResponse>>() {}.type
      val apiResponse = Gson().fromJson<ApiResponse<PageResponse>>(json, type)
      return if (apiResponse == null || apiResponse.data == null) {
        Constants.EMPTY_STRING
      } else {
        val versionDbEntity = VersionDbEntity(entityType = VersionEntity.PAGEABLE_TOPICS.name, data = json.toByteArray(),
            langCode = UserPreferenceUtil.getUserLanguages(), version = apiResponse.data.version, parentId = section)
        VersionedApiHelper.insertVersionEntity(versionDbEntity)
        apiResponse.data.version
      }
    } catch (e: Exception) {
      Logger.caughtException(e)
    }

    return Constants.EMPTY_STRING
  }

  fun addOrRemovePages(pageableTopicsEntity: PageableTopicsEntity, isAdded: Boolean, section: String) : Observable<Any> {
    return Observable.fromCallable {
      if (isAdded) {
        SocialDB.instance().pageEntityDao().insReplace(S_PageEntity(
            pageEntity = pageableTopicsEntity.pageEntity.copy(viewOrder = 1001),
            section = section))
      } else {
        SocialDB.instance().pageEntityDao().deletePage(pageableTopicsEntity.pageEntity.id)
      }
      val mode  = if (isAdded) NewsPageMode.ADDED.mode else NewsPageMode.DELETED.mode
      SocialDB.instance().pageSyncEntityDao().insReplace(
          PageSyncEntity(pageableTopicsEntity.pageEntity.id, pageableTopicsEntity.pageEntity.entityType,
              1001, mode , section, pageableTopicsEntity.pageEntity.isServerDetermined))
      SocialDB.instance().addPageDao().toggleAddPage(AddPageEntity(pageableTopicsEntity.pageEntity.id, mode,
          pageableTopicsEntity.pageEntity.displayName?:Constants.EMPTY_STRING,
          entityType = pageableTopicsEntity.pageEntity.entityType))
    }
  }

}