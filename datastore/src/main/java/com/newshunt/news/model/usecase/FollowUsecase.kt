package com.newshunt.news.model.usecase

import android.net.Uri
import android.os.Bundle
import com.newshunt.common.helper.common.BusProvider
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.preference.AppUserPreferenceUtils
import com.newshunt.common.helper.preference.GenericAppStatePreference
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.dataentity.common.model.entity.BaseError
import com.newshunt.dataentity.common.model.entity.ListNoContentException
import com.newshunt.dataentity.common.pages.ActionableEntity
import com.newshunt.dataentity.common.pages.FollowSyncResponse
import com.newshunt.dataentity.common.pages.SourceFollowBlockEntity
import com.newshunt.dataentity.common.pages.UserFollowEntity
import com.newshunt.dataentity.model.entity.ContactLiteSyncDone
import com.newshunt.dataentity.social.entity.FetchInfoEntity
import com.newshunt.dhutil.bundleOf
import com.newshunt.dhutil.helper.preference.UserPreferenceUtil
import com.newshunt.dhutil.helper.retrofit.NewsBaseUrlContainer
import com.newshunt.news.model.daos.FetchDao
import com.newshunt.news.model.daos.UserFollowDao
import com.newshunt.news.model.repo.FollowRepo
import com.newshunt.news.model.sqlite.SocialDB
import io.reactivex.Observable
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers
import java.net.HttpURLConnection
import javax.inject.Inject
import javax.inject.Named

class GetAllFollowUsecase : BundleUsecase<FollowSyncResponse> {

  companion object {
    const val BUNDLE_URL = "url"
    const val BUNDLE_IS_FIRST_PAGE = "isFirstPage"
  }


  override fun invoke(p1: Bundle): Observable<FollowSyncResponse> {
    val url = p1.getString(BUNDLE_URL) ?: Constants.EMPTY_STRING
    val isFirstPage = p1.getBoolean(BUNDLE_IS_FIRST_PAGE)
    return FollowRepo(SocialDB.instance().followEntityDao()).getFollowAndBlocks(url, isFirstPage).map {
      PreferenceManager.savePreference(GenericAppStatePreference
          .FOLLOW_SYNC_LAST_SUCCESSFUL_TIME, System.currentTimeMillis())
      it
    }
  }
}

private const val QUERY_PARAM_SYNC = "sync"
class GetFirstPageFollowForUserUsecase
@Inject constructor(@Named("entityId") private val entityId: String,
                    @Named("location") private val location: String,
                    @Named("section") private val section: String,
                    @Named("fetchUsecase") private val fetchUsecase: BundleUsecase<NLResponseWrapper>,
                    private val fetchDao: FetchDao,
                    private val userFollowDao: UserFollowDao): BundleUsecase<NLResponseWrapper> {
  override fun invoke(p1: Bundle): Observable<NLResponseWrapper> {
    return Observable.fromCallable {
      fetchDao.insIgnore(FetchInfoEntity(entityId, location, section = section))
    }.flatMap {
      var feedPage = fetchDao.lookupPage(entityId, section)!!
      //If the FP request is for contact lite sync, need to add a query param sync based on whether or not contact lite sync was done previously
      if (feedPage.id == Constants.IMPORT_FOLLOW_PAGE_ID) {
        val uri = Uri.parse(feedPage.contentUrl)
        if (uri.getQueryParameter(QUERY_PARAM_SYNC) == null) {
          val newUrl = uri.buildUpon()
                  .appendQueryParameter(QUERY_PARAM_SYNC, (!AppUserPreferenceUtils.isContactLiteSyncDone()).toString())
                  .build()
                  .toString()
          feedPage = feedPage.copy(contentUrl = newUrl)
        }
      }
      fetchUsecase.invoke(FetchCardListFromUrlUsecase.bundle(feedPage))
          .map {
            val nlResp = it.nlResp
            val userFollows = nlResp.rows.filterIsInstance<ActionableEntity>()
            val userFollowList = userFollows.map { entity -> UserFollowEntity(entityId, entity) }
            val fetchInfoEntity = FetchInfoEntity(entityId, location, npUrlOf1stResponse = nlResp.nextPageUrl,
                nextPageUrl = nlResp.nextPageUrl, currentPageNum = nlResp.pageNumber, section = section)
            userFollowDao.replaceFirstPage(fetchDao, fetchInfoEntity, userFollowList, it.reqUrl)
            //Mark contact lite sync done here!
            if (feedPage.id == Constants.IMPORT_FOLLOW_PAGE_ID && !AppUserPreferenceUtils
                            .isContactLiteSyncDone()) {
              AppUserPreferenceUtils.setContactLiteSyncDone()
              BusProvider.postOnUIBus(ContactLiteSyncDone())
            }
            it
          }.doOnError {
            if (it is ListNoContentException) {
              // Since the first page is null, empty the list
              userFollowDao.cleanUpUserFollowTable(entityId)
              fetchDao.deleteFetchInfo(entityId)
            }
          }
    }
  }
}

class GetNextPageFollowUserUsecase
@Inject constructor(@Named("entityId") val entityId: String,
                    @Named("location") val location: String,
                    @Named("section") private val section: String,
                    @Named("fetchUsecase") val fetchUsecase: BundleUsecase<NLResponseWrapper>,
                    private val fetchDao: FetchDao,
                    private val userFollowDao: UserFollowDao) : BundleUsecase<NLResp> {
  override fun invoke(p1: Bundle): Observable<NLResp> {
    return Observable.fromCallable {
      val fetchInfo = fetchDao.fetchInfo(entityId, location, section) ?: run {
        Logger.e(FollowUtils.LOG_TAG, "couldn't read fetchdao from db: $entityId, $location")
        return@fromCallable p1
      }
      val npUrl = fetchInfo.nextPageUrl ?: run {
        Logger.d(FollowUtils.LOG_TAG, "nextPageUrl in null. Pagination terminated?")
        return@fromCallable p1
      }
      val lookupPage = fetchDao.lookupPage(fetchInfo.entityId, section)
      val pageEntity = lookupPage?.copy(
          contentUrl = npUrl
      ) ?: run {
        Logger.e(FollowUtils.LOG_TAG, "entity is null")
        return@fromCallable p1
      }
      FetchCardListFromUrlUsecase.bundle(pageEntity, p1, lookupPage?.contentUrl)
    }.flatMap {
      val url = it.getString("url")
      if (url != null) fetchUsecase(it)
      else Observable.empty()
    }.map {
      val nlResp = it.nlResp
      val fetchEntity = FetchInfoEntity(entityId, location, nlResp.nextPageUrl,
              nlResp.pageNumber, section = section)
      val userFollows = nlResp.rows.filterIsInstance<ActionableEntity>()
      val userFollowList = userFollows.map { entity -> UserFollowEntity(entityId, entity) }
      userFollowDao.appendNextPage(fetchDao, fetchEntity, userFollowList, it.reqUrl)
      nlResp
    }.doOnError {
      // callback will be invoked in non-UI thread, so we can call dao methods directly.
      if (it is ListNoContentException && (it.error as? BaseError)?.statusAsInt == HttpURLConnection.HTTP_NO_CONTENT) {
        Logger.d(FollowUtils.LOG_TAG, "can make it null ${Thread.currentThread().name}")
        fetchDao.paginationTerminated(entityId, location, section)
      }
    }
  }
}

class CleanupUserFollowUsecase : Usecase<String, Any> {

  override fun invoke(p1: String): Observable<Any> {
    return Observable.fromCallable {
      SocialDB.instance().userFollowDao().cleanUpUserFollowTable(p1)
      SocialDB.instance().fetchDao().deleteFetchInfo(p1)
    }
  }
}

object FollowUtils {

  const val LOG_TAG = "FollowUsecase"

  @JvmStatic
  fun getAllFollows(userId: String) {
    val lastSuccessfulFollowSync = PreferenceManager.getPreference(GenericAppStatePreference
        .FOLLOW_SYNC_LAST_SUCCESSFUL_TIME, 0L)
    val minimumGapForFollowSync = PreferenceManager.getPreference(GenericAppStatePreference
        .FOLLOW_SYNC_MINIMUM_GAP, 0L)

    if (System.currentTimeMillis() - lastSuccessfulFollowSync < minimumGapForFollowSync) {
      return
    }

    callFollowSync(getUrl(userId), getDisposableObserver(), true)
  }

  fun isSameFollowBlockObject(obj1:SourceFollowBlockEntity?,obj2:SourceFollowBlockEntity?):Boolean {
    obj1 ?: return false
    obj2 ?: return false
    return obj1.sourceId == obj2.sourceId  && obj1.pageViewCount == obj2.pageViewCount &&
            obj1.shareCount == obj2.shareCount && obj1.reportCount == obj2.reportCount &&
            obj1.showLessCount == obj2.showLessCount
  }

  private fun getDisposableObserver() : DisposableObserver<FollowSyncResponse>{
    return object:DisposableObserver<FollowSyncResponse>() {

      override fun onNext(t: FollowSyncResponse) {
        Logger.d("FOLLOW_SYNC" , "Received the response with npUrl ${t?.nextPageUrl}")
        t?.nextPageUrl?.let {
          callFollowSync(it, getDisposableObserver(), false)
        }
      }

      override fun onError(e: Throwable) {
        Logger.d("FOLLOW_SYNC" , "onError Received")
        dispose()
      }

      override fun onComplete() {
        dispose()
      }

    }
  }

  private fun callFollowSync(url: String, disposableObserver: DisposableObserver<FollowSyncResponse>, isFirstPage: Boolean) {
    val observable = GetAllFollowUsecase().invoke(bundleOf(GetAllFollowUsecase.BUNDLE_URL to url,
        GetAllFollowUsecase.BUNDLE_IS_FIRST_PAGE to isFirstPage))
    observable.subscribeOn(Schedulers.io()).subscribeWith(disposableObserver)
  }

  private fun getUrl(userId: String): String {
    return Uri.Builder().encodedPath(NewsBaseUrlContainer.getApplicationUrl())
        .appendEncodedPath("api/v2/follow/all")
        .appendQueryParameter("userId", userId)
        .appendQueryParameter(Constants.URL_QUERY_LANG_CODE, UserPreferenceUtil.getUserLanguages())
        .appendQueryParameter(Constants.URL_QUERY_APP_LANG, UserPreferenceUtil.getUserNavigationLanguage())
        .toString()
  }
}