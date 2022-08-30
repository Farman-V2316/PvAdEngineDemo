package com.newshunt.appview.common.model.usecase

import android.os.Bundle
import com.newshunt.common.helper.common.Constants
import com.newshunt.dataentity.common.pages.PageSection
import com.newshunt.dataentity.social.entity.GeneralFeed
import com.newshunt.news.model.usecase.BundleUsecase
import com.newshunt.news.model.sqlite.SocialDB
import com.newshunt.news.util.NewsConstants
import io.reactivex.Observable

class InsertFollowFeedPageUsecase : BundleUsecase<Any> {

  override fun invoke(p1: Bundle): Observable<Any> {
    return Observable.fromCallable {
      val id = p1.getString(BUNDLE_ID)?:Constants.EMPTY_STRING
      val contentUrl = p1.getString(BUNDLE_CONTENT_URL)?:Constants.EMPTY_STRING
      val contentRequestMethod = p1.getString(BUNDLE_CONTENT_POST)?:Constants.EMPTY_STRING
      val section = p1.getString(NewsConstants.DH_SECTION) ?: Constants.GROUP_SECTION
      SocialDB.instance().groupDao().insReplace(GeneralFeed(id, contentUrl, contentRequestMethod, section))
    }

  }

  companion object {
    const val BUNDLE_ID = "BUNDLE_ID"
    const val BUNDLE_CONTENT_URL = "BUNDLE_CONTENT_URL"
    const val BUNDLE_CONTENT_POST = "BUNDLE_CONTENT_POST"
  }
}