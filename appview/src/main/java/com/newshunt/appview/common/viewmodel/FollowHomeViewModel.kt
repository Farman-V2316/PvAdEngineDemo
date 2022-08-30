package com.newshunt.appview.common.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.newshunt.appview.common.model.usecase.InsertFollowFeedPageUsecase
import com.newshunt.dataentity.common.pages.PageSection
import com.newshunt.dhutil.bundleOf
import com.newshunt.dhutil.helper.retrofit.NewsBaseUrlContainer
import com.newshunt.news.model.usecase.toMediator2
import com.newshunt.news.util.NewsConstants

class FollowHomeViewModel: ViewModel() {

  init {
    InsertFollowFeedPageUsecase().toMediator2().execute(bundleOf(
        InsertFollowFeedPageUsecase.BUNDLE_ID to "follow",
        InsertFollowFeedPageUsecase.BUNDLE_CONTENT_URL to getUrl(),
        InsertFollowFeedPageUsecase.BUNDLE_CONTENT_POST to "POST",
        NewsConstants.DH_SECTION to PageSection.FOLLOW.section
    ))
  }


  private fun getUrl() : String {
   return Uri.Builder().encodedPath(NewsBaseUrlContainer.getApplicationUrl())
        .appendEncodedPath("api/v2/star/section/explore").toString()
  }

}