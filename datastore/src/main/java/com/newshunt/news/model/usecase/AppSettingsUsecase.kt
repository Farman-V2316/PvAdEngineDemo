package com.newshunt.news.model.usecase

import com.newshunt.dhutil.helper.appsection.AppSectionsProvider
import com.newshunt.dhutil.model.versionedapi.VersionedApiHelper
import com.newshunt.news.model.daos.MenuDao
import com.newshunt.news.model.sqlite.SocialDB
import io.reactivex.Observable

class ResetVersionApiUsescase(val menuDao : MenuDao = SocialDB.instance().menuDao()) : Usecase<Any, Any> {
  override fun invoke(p1: Any): Observable<Any> {
    return Observable.fromCallable {
      VersionedApiHelper.resetAllApiVersion()
      AppSectionsProvider.resetLocalVersion()
      SocialDB.instance().pageableTopicsDao().cleanUpPageableTopics()
      SocialDB.instance().entityInfoDao().cleanUpEntityInfo()
      menuDao.resetVersion()
    }
  }
}