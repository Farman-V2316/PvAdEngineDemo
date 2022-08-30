/*
 * Copyright (c) 2020 Newshunt. All rights reserved.
 */
package com.newshunt.appview.common.ui.helper

import android.content.Intent
import android.os.Bundle
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.share.ShareUi
import com.newshunt.common.model.usecase.ShareUsecase
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.common.asset.CommonAsset
import com.newshunt.dataentity.common.asset.CreatePostUiMode
import com.newshunt.dataentity.common.asset.LocalInfo
import com.newshunt.dataentity.news.analytics.NewsReferrer
import com.newshunt.deeplink.navigator.CommonNavigator
import com.newshunt.dhutil.analytics.AnalyticsHelper2
import com.newshunt.news.helper.toMinimizedCommonAsset
import com.newshunt.news.model.usecase.MediatorUsecase
import java.io.Serializable

/**
 * @author shrikant.agrawal
 */
enum class NotificationUiType {
    AUTO_SHARE,
    AUTO_REPOST
}
object NotificationCtaUiHelper {

    @JvmStatic
    fun getNotificationUiType(bundle: Bundle): NotificationUiType? {
        return when {
            bundle.getBoolean(Constants.BUNDLE_AUTO_SHARE_FROM_NOTIFICATION, false) -> {
                NotificationUiType.AUTO_SHARE
            }
            bundle.getBoolean(Constants.BUNDLE_AUTO_REPOST_FROM_NOTIFICATION, false) -> {
                NotificationUiType.AUTO_REPOST
            }
            else -> {
                null
            }
        }
    }

    @JvmStatic
    fun handleNotificationCtaType(notificationUiType: NotificationUiType, card: CommonAsset?,
                                  shareUsecase: MediatorUsecase<Bundle, Boolean>?,
                                  entityId: String, location: String, section: String) {
        if (card == null) {
            return
        }

        when(notificationUiType) {
            NotificationUiType.AUTO_SHARE -> {
                val intent = Intent()
                intent.action = Constants.SHARE_POST_ACTION
                intent.putExtra(Constants.BUNDLE_STORY, card.toMinimizedCommonAsset() as? Serializable)
                val event = NavigationEvent(intent = intent)
                NavigationHelper.navigationLiveData.postValue(event)
                shareUsecase?.execute(ShareUsecase.args(card.i_id(), "POST", parentId = card.i_parentPostId(), postSourceAsset = card.i_source(), sourceLang = card.i_langCode()))
                AnalyticsHelper2.logStorySharedEvent(
                    packageName = null,
                    shareUiParam = ShareUi.COMMENT_BAR_SHARE_ICON,
                    post = card,
                    referrer = PageReferrer(NewsReferrer.STORY_DETAIL, card.i_id()),
                    eventSection = AnalyticsHelper2.getSection(section),
                    groupInfo = null
                )
            }
            NotificationUiType.AUTO_REPOST -> {
                val local = LocalInfo(
                    pageId = entityId,
                    location = location,
                    section = section,
                    nextCardId = null,
                    isCreatedFromMyPosts = false,
                    creationDate = System.currentTimeMillis()
                )
                val repostIntent = CommonNavigator.getPostCreationIntent(
                    card.i_id(), CreatePostUiMode.REPOST, null,  PageReferrer(NewsReferrer.STORY_DETAIL, card.i_id()),
                    local, card.i_source()?.id, card.i_source()?.type, card.i_parentPostId())
                NavigationHelper.navigationLiveData.postValue(NavigationEvent(repostIntent))

            }
        }

    }


}