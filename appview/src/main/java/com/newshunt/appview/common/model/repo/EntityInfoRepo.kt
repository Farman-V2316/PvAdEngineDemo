package com.newshunt.appview.common.model.repo

import com.newshunt.appview.R
import com.newshunt.news.model.apis.EntityAPI
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.common.model.entity.ListNoContentException
import com.newshunt.common.model.retrofit.RestAdapterContainer
import com.newshunt.common.view.DbgCode
import com.newshunt.dataentity.common.model.entity.BaseError
import com.newshunt.dhutil.helper.interceptor.NewsListErrorResponseInterceptor
import com.newshunt.dhutil.helper.retrofit.NewsBaseUrlContainer
import com.newshunt.news.model.sqlite.SocialDB
import com.newshunt.sdk.network.Priority
import io.reactivex.Observable
import java.net.HttpURLConnection

class EntityInfoRepo(val section: String) {

  fun getInfoFromServer(id: String, entityType: String, langCode: String) : Observable<Any> {

    return RestAdapterContainer.getInstance().getRestAdapter(NewsBaseUrlContainer.getApplicationUrl(),
          Priority.PRIORITY_HIGHEST,this,
          NewsListErrorResponseInterceptor()).create(EntityAPI::class.java).getEntityInfo(
                           pageId = id, entityType = entityType, section = section, langCode = langCode)
          .map { response ->
            if (response.data == null) {
              throw ListNoContentException(BaseError(DbgCode.DbgHttpCode(HttpURLConnection.HTTP_NO_CONTENT),
                  CommonUtils.getString(R.string.no_content_found)))
            } else {
              response.data?.let { info ->
                   SocialDB.instance().entityInfoDao().insertEntityKids(info, section)
              }
            }
            Any()
      }.doOnError {
        if (it is ListNoContentException) {
          SocialDB.instance().entityInfoDao().deleteItem(id)
          SocialDB.instance().entityInfoDao().deleteChilds(id)
        }
    }
  }
}