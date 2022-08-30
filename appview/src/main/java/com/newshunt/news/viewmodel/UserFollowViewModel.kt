package com.newshunt.news.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.newshunt.appview.common.model.usecase.InsertFollowFeedPageUsecase
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger
import com.newshunt.dhutil.bundleOf
import com.newshunt.dhutil.helper.retrofit.NewsBaseUrlContainer
import com.newshunt.news.model.usecase.CleanupUserFollowUsecase
import com.newshunt.news.model.usecase.toMediator2
import com.newshunt.dataentity.notification.FollowModel
import com.newshunt.news.util.NewsConstants

class UserFollowViewModel : ViewModel() {

  companion object {
    const val FOLLOWING_PATH = "api/v2/follow/filter"
    const val FOLLOWER_PATH ="api/v2/follow/followers"
    const val BLOCK_PATH = "api/v2/follow/block"
  }

  val insertedPages = mutableListOf<String>()

  fun insertPage(userId: String, filter: String, followModel: FollowModel, section: String) {
    insertedPages.add(userId+"_"+filter)
    InsertFollowFeedPageUsecase().toMediator2().execute(bundleOf(InsertFollowFeedPageUsecase.BUNDLE_ID to (userId+"_"+filter),
        InsertFollowFeedPageUsecase.BUNDLE_CONTENT_URL to getUrl(userId, filter, followModel),
        InsertFollowFeedPageUsecase.BUNDLE_CONTENT_POST to "GET",
        NewsConstants.DH_SECTION to section))
  }

  private fun getUrl(userId: String, filter: String, followModel: FollowModel) : String {
    val path = when(followModel) {
      FollowModel.FOLLOWERS -> FOLLOWER_PATH
      FollowModel.BLOCKED -> BLOCK_PATH
      else -> FOLLOWING_PATH
    }
    val uri = Uri.Builder().encodedPath(NewsBaseUrlContainer.getApplicationUrl())
        .appendEncodedPath(path)
        .appendQueryParameter(Constants.QUERY_USERID , userId)
    if (followModel == FollowModel.FOLLOWING) {
      uri.appendQueryParameter(Constants.QUERY_FILTER, filter)
    }
    return uri.toString()
  }

  override fun onCleared() {
    super.onCleared()
    insertedPages.forEach {
      CleanupUserFollowUsecase().toMediator2().execute(it)
    }
  }
}