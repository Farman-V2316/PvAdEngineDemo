package com.newshunt.news.model.usecase

import android.os.Bundle
import com.google.gson.reflect.TypeToken
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.JsonUtils
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.common.model.NewsPageMode
import com.newshunt.dataentity.common.model.entity.CommunicationEventsResponse
import com.newshunt.dataentity.common.model.entity.model.ApiResponse
import com.newshunt.dataentity.common.model.entity.model.MultiValueResponse
import com.newshunt.dataentity.common.pages.DefaulPagesEntity
import com.newshunt.dataentity.common.pages.PageEntity
import com.newshunt.dataentity.common.pages.PageResponse
import com.newshunt.dataentity.common.pages.PageSection
import com.newshunt.dataentity.common.pages.PageSyncEntity
import com.newshunt.dataentity.common.pages.S_PageEntity
import com.newshunt.dataentity.dhutil.model.entity.version.VersionEntity
import com.newshunt.dhutil.bundleOf
import com.newshunt.dhutil.helper.preference.AppStatePreference
import com.newshunt.dhutil.model.versionedapi.VersionedApiHelper
import com.newshunt.news.model.repo.PageSyncRepo
import com.newshunt.news.model.service.CommunicationEventServiceImpl
import com.newshunt.news.model.service.CommunicationEventsService
import com.newshunt.news.model.sqlite.SocialDB
import com.newshunt.news.util.NewsConstants
import io.reactivex.Observable
import javax.inject.Inject

class StoreHomePagesUsecase(val pagesSyncRepo: PageSyncRepo) : Usecase<String, PageResponse> {
  override fun invoke(p1: String): Observable<PageResponse> {
    return pagesSyncRepo.performPageSync().map {
      return@map it }
  }
}

class MediatorHomePageUsecase : Usecase<String, List<PageEntity>> {
  override fun invoke(p1: String): Observable<List<PageEntity>> {
    val p = SocialDB.instance().pageEntityDao().getPageEntities(p1)
    return p.map { l ->
      l.map { it.pageEntity }
    }
  }
}

class GetHomePageUsecase : Usecase<String, List<PageEntity>> {
  override fun invoke(p1: String): Observable<List<PageEntity>> {
    return Observable.fromCallable {
      SocialDB.instance().pageEntityDao().getAllPages(p1)?.map { it.pageEntity }
    }
  }
}

class GetPageByIdOrDefaultUsecase @Inject constructor() : Usecase<Bundle, PageEntity?> {
  override fun invoke(p1: Bundle): Observable<PageEntity?> {
    val entityId = p1.getString(B_ENTITY_ID) ?: UNDEFINED
    val section = p1.getString(B_SECTION) ?: PageSection.NEWS.section

    return Observable.fromCallable {
      SocialDB.instance().pageEntityDao().getPageOrHome(section, entityId)?.pageEntity
    }
  }

  companion object {
    fun bundle(section: String?, entityId: String? = null): Bundle {
      return bundleOf(B_SECTION to section, B_ENTITY_ID to entityId)
    }

    const val B_ENTITY_ID = "entityId"
    const val B_SECTION = "section"
    const val UNDEFINED = "undefined"
  }
}

class DefaultHomePageUsecase : Usecase<Any, Any> {

  override fun invoke(p1: Any): Observable<Any> {
    return Observable.fromCallable {
      val preloadPages = PreferenceManager.getPreference(AppStatePreference.PRELOAD_PAGES, Constants.EMPTY_STRING)
      if (CommonUtils.isEmpty(preloadPages)) {
        return@fromCallable
      }

      val type = object : TypeToken<MultiValueResponse<DefaulPagesEntity>>() {}.type
      val defaultPageList: MultiValueResponse<DefaulPagesEntity>? = JsonUtils.fromJson(preloadPages,type)
      if (defaultPageList == null) {
        return@fromCallable
      } else {
        defaultPageList.rows.forEach {
          val section = it.section
          val pageList = SocialDB.instance().pageEntityDao().getAllPages(section)
          if (CommonUtils.isEmpty(pageList)) {
            SocialDB.instance().pageEntityDao().insReplace(it.pages.map { page -> S_PageEntity(pageEntity = page, section = section) })
          }
        }
      }
    }
  }
}

class ReorderPageUsecase : BundleUsecase<Any> {

  override fun invoke(p1: Bundle): Observable<Any> {
    val section = p1.getString(BUNDLE_SECTION) ?: PageSection.NEWS.section
    val list = p1.getSerializable(BUNDLE_LIST) as List<PageEntity>

    return Observable.fromCallable {
      SocialDB.instance().pageEntityDao().insReplace(list.map {
        S_PageEntity(pageEntity = it, section = section)
      })
      SocialDB.instance().pageSyncEntityDao().insReplace(list.map {
        PageSyncEntity(id = it.id, viewOrder = it.viewOrder,
            mode = NewsPageMode.ADDED.mode,
            entityType = it.entityType,
            section = section, isServerDetermined = it.isServerDetermined)
      })
    }
  }

  companion object {
    const val BUNDLE_LIST = "bundle_list"
    const val BUNDLE_SECTION = "bundle_section"
  }
}

class RemovePageUsecase : BundleUsecase<Any> {

  override fun invoke(p1: Bundle): Observable<Any> {
    return Observable.fromCallable {
      val pageEntity = p1.getSerializable(BUNDLE_PAGE) as? PageEntity
      val section  = p1.getString(BUNDLE_SECTION) ?: PageSection.NEWS.section
      pageEntity?.let {
        SocialDB.instance().pageEntityDao().deletePage(it.id)
        SocialDB.instance().pageSyncEntityDao().insReplace(PageSyncEntity(id = it.id, viewOrder = it.viewOrder,
            mode = NewsPageMode.DELETED.mode, entityType = it.entityType, section = section,
            isServerDetermined = it.isServerDetermined))
      }

    }
  }

  companion object {
    const val BUNDLE_PAGE = "bundle_page"
    const val BUNDLE_SECTION = "bundle_section"
  }
}

class CommunicationEventUsecase : Usecase<Any, CommunicationEventsResponse> {
  override fun invoke(p1: Any): Observable<CommunicationEventsResponse> {
    return Observable.fromCallable {
      val versionedApiHelper = VersionedApiHelper<ApiResponse<CommunicationEventsResponse>>()
      val type = object : TypeToken<ApiResponse<CommunicationEventsResponse>>() {}.type
      val result = versionedApiHelper.getLocalEntity(entityType = VersionEntity.COMMUNICATION_EVENTS.name, classOfT = type)
      if (result?.data == null) {
        throw Exception(Constants.NOT_FOUND_IN_CACHE)
      }

      result.data
    }.onErrorResumeNext { t : Throwable  ->
      val communicationEventService : CommunicationEventsService = CommunicationEventServiceImpl()
      communicationEventService.communicationEvents
    }.onErrorReturn {
      CommunicationEventsResponse("")
    }
  }
}

class AddPageUsecase : BundleUsecase<Any> {
  override fun invoke(p1: Bundle): Observable<Any> {
    return Observable.fromCallable {
      val pageEntity = p1.getSerializable(NewsConstants.NEWS_PAGE_ENTITY) as? PageEntity
      val section = p1.getString(NewsConstants.DH_SECTION) ?: PageSection.NEWS.section
      pageEntity?.let {
        SocialDB.instance().pageSyncEntityDao().insReplace(
            PageSyncEntity(it.id, it.entityType, 1001, NewsPageMode.ADDED.mode, section, it.isServerDetermined))
      }
    }
  }
}