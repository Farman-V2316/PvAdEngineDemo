package com.newshunt.news.helper

import android.webkit.JavascriptInterface
import com.newshunt.appview.common.ui.helper.NavigationEvent
import com.newshunt.appview.common.ui.helper.NavigationEventPublisher
import com.newshunt.appview.common.ui.helper.NavigationHelper
import com.newshunt.common.helper.common.Logger
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.common.asset.CreatePostUiMode
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.deeplink.navigator.CommonNavigator
import javax.inject.Inject

class RepostWebItemJsInterfaceClickHandler @Inject constructor() : RepostWebItemJsInterface, NavigationEventPublisher {

    var pageReferrer: PageReferrer? = null
    private var navigationEventTarget = 0L

    companion object {
        private const val LOG_TAG = "NHJsInterfaceWithRepostHandling"
    }

    @JavascriptInterface
    override fun onRepostClicked(postId: String) {
        Logger.d(LOG_TAG, "On repost button click")
        try {
            if(!CommonUtils.isEmpty(postId)){
                val repostIntent = CommonNavigator.getPostCreationIntent(
                        postId, CreatePostUiMode.REPOST, null, pageReferrer,
                        null, null, null, null)
                NavigationHelper.navigationLiveData.postValue(NavigationEvent(repostIntent, targetId = navigationEventTarget))
            }
        } catch (e: Exception) {
            Logger.caughtException(e)
        }
    }
    override fun setTargetId(uniqueId: Long) {
        navigationEventTarget = uniqueId
    }
}