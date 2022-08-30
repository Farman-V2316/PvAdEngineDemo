/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */
package com.newshunt.deeplink.navigator

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.newshunt.app.helper.PageUtil
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.DHConstants
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.common.helper.preference.AppUserPreferenceUtils
import com.newshunt.dataentity.common.model.entity.AppSection
import com.newshunt.dataentity.common.follow.entity.FollowNavigationType
import com.newshunt.dataentity.common.pages.PageEntity
import com.newshunt.dataentity.news.analytics.FollowTabLandingInfoEvent
import com.newshunt.dhutil.helper.appsection.AppSectionsProvider
import com.newshunt.dataentity.news.model.entity.PageType
import com.newshunt.news.util.NewsConstants
import com.newshunt.dataentity.notification.NavigationType
import com.newshunt.dataentity.notification.ExploreNavModel
import com.newshunt.dataentity.notification.FollowNavModel

/**
 * @author santhosh.kc
 */
class ExploreSectionNavigator {

    companion object {

        @JvmStatic
        fun getTargetIntent(context: Context, exploreNavModel: ExploreNavModel?, pageReferrer:
        PageReferrer): Intent? {
            exploreNavModel ?: return null
            val navigationType = NavigationType.fromIndex(
                    Integer.parseInt(exploreNavModel.getsType()))

            navigationType ?: return null

            return when (navigationType) {
                NavigationType.TYPE_OPEN_EXPLORE_ENTITY ->
                    goToSeeAllOfEntity(context, exploreNavModel, pageReferrer)
                else -> null
            }
        }

        private fun goToSeeAllOfEntity(context: Context, exploreNavModel: ExploreNavModel?,
                                       pageReferrer: PageReferrer): Intent? {
            exploreNavModel ?: return null
            val intent = Intent(NewsConstants.INTENT_ACTION_SEE_ALL_ENTITY)
            intent.setPackage(CommonUtils.getApplication().packageName)
            intent.putExtra(NewsConstants.BUNDLE_ACTIVITY_REFERRER, pageReferrer)
            intent.putExtra(Constants.BUNDLE_EXPLORE_NAV_MODEL, exploreNavModel)
            intent.putExtra(Constants.BUNDLE_NAVIGATION_TYPE, NavigationType
                    .TYPE_OPEN_EXPLORE_ENTITY.name)

            if (CommonNavigator.isFromNotificationTray(pageReferrer)) {
                intent.putExtra(Constants.V4BACKURL, exploreNavModel.baseInfo?.v4BackUrl)
            }
            return intent
        }
    }

}

class FollowSectionNavigator {
    companion object {

        @JvmStatic
        fun isDeeplinkToFeed(followNavModel: FollowNavModel): Boolean {
            return CommonUtils.equalsIgnoreCase(followNavModel.tabType, PageType.FEED.deeplinkValue)
        }

        @JvmStatic
        fun getTargetIntent(context: Context, followNavModel: FollowNavModel?,
                            pageReferrer: PageReferrer?): Intent? {
            followNavModel ?: return null

            val navigationType = NavigationType.fromIndex(
                    Integer.parseInt(followNavModel.getsType()))

            navigationType ?: return null

            return when (navigationType) {
                NavigationType.TYPE_OPEN_FOLLOW_HOME ->
                    goToFollowHome(pageReferrer)
                NavigationType.TYPE_OPEN_EXPLORE_VIEW_TAB ->
                    NewsNavigator.goToNewsHome(pageReferrer)
                else -> null
            }
        }

        fun goToFollowHome(pageReferrer: PageReferrer?): Intent? {
            if (!AppSectionsProvider.isSectionAvailable(AppSection.FOLLOW)) {
                return null
            }
            val followIntent = Intent(Constants.INTENT_ACTIONS_LAUNCH_FOLLOW_HOME)
            followIntent.setPackage(CommonUtils.getApplication().packageName)
            followIntent.putExtra(NewsConstants.BUNDLE_ACTIVITY_REFERRER, pageReferrer)
            followIntent.putExtra(Constants.BUNDLE_NAVIGATION_TYPE, NavigationType
                    .TYPE_OPEN_FOLLOW_HOME.name)
            followIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            return followIntent
        }

        fun navigateToUnifiedExplore(context: Context?, pageReferrer: PageReferrer?) {

            val followTabLandingInfoEvent = FollowTabLandingInfoEvent(PageType.EXPLORE,
                    pageReferrer = pageReferrer)

            val followIntent = Intent(Constants.INTENT_ACTION_LAUNCH_FOLLOW_EXPLORE)
            followIntent.setPackage(CommonUtils.getApplication().packageName)
            followIntent.putExtra(NewsConstants.BUNDLE_ACTIVITY_REFERRER, pageReferrer)

            followIntent.putExtra(Constants.BUNDLE_FOLLOW_TAB_LANDING_INFO,
                    followTabLandingInfoEvent)

            context?.startActivity(followIntent)
        }

        @JvmOverloads
        fun navigateToFollowHome(context: Context?, pageReferrer: PageReferrer?,
                                 clearTaskOnLaunch: Boolean = true) {
            context ?: return
            val followIntent = goToFollowHome(pageReferrer)
                ?: return
            if (clearTaskOnLaunch) {
                followIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            }
            val result =
                    CommonNavigator.launchHomeIntent(context, AppSection.FOLLOW, followIntent)
            if (result == null || !result.isLaunched) {
                return
            }
            AppUserPreferenceUtils.setAppSectionSelected(result.appSection)
        }

        private fun goToFollowExploreViewTab(followNavModel: FollowNavModel?,
                                             pageReferrer: PageReferrer?): Intent? {
            followNavModel ?: return null

            val followTabLandingInfoEvent: FollowTabLandingInfoEvent? = when {
                CommonUtils.equals(followNavModel.tabType, PageType.EXPLORE.deeplinkValue) -> {
                    val navigationType = if (!CommonUtils.isEmpty(followNavModel.subTabType))
                        FollowNavigationType.fromDeeplinkValue(followNavModel.subTabType)
                    else null
                    FollowTabLandingInfoEvent(PageType.EXPLORE, navigationType, pageReferrer,
                            followNavModel.promotionId)
                }
                else -> null
            }

            val followIntent = Intent(Constants.INTENT_ACTION_LAUNCH_FOLLOW_EXPLORE)
            followIntent.setPackage(CommonUtils.getApplication().packageName)
            followIntent.putExtra(NewsConstants.BUNDLE_ACTIVITY_REFERRER, pageReferrer)
            followIntent.putExtra(Constants.BUNDLE_NAVIGATION_TYPE,
                    NavigationType.TYPE_OPEN_EXPLORE_VIEW_TAB.name)
            if (followTabLandingInfoEvent != null) {
                followIntent.putExtra(Constants.BUNDLE_FOLLOW_TAB_LANDING_INFO,
                        followTabLandingInfoEvent)
            }
            if (CommonNavigator.isFromNotificationTray(pageReferrer)) {
                followIntent.putExtra(Constants.V4BACKURL, followNavModel.baseInfo?.v4BackUrl)
            }
            followIntent.putExtra(NewsConstants.BUNDLE_ACTIVITY_REFERRER, pageReferrer)
            return followIntent
        }

        @JvmStatic
        fun getFollowedAllIntent(context: Context, pageReferrer: PageReferrer?,
                                 followNavModel: FollowNavModel?):
                Intent? {

            val followIntent = Intent(DHConstants.OPEN_FOLLOW_ENTITIES_SCREEN)
            followIntent.setPackage(CommonUtils.getApplication().packageName)
            followIntent.putExtra(NewsConstants.BUNDLE_ACTIVITY_REFERRER, pageReferrer)
            followIntent.putExtra(NewsConstants.BUNDLE_OPEN_FOLLOWED_ENTITY, followNavModel)
            if (CommonNavigator.isFromNotificationTray(pageReferrer)) {
                followIntent.putExtra(Constants.V4BACKURL, followNavModel?.baseInfo?.v4BackUrl)
            }
            return followIntent
        }

        @JvmStatic
        fun getFollowingTabIntentFromNewsPageResponse(pageList: List<PageEntity>,
                                                      pageReferrer: PageReferrer?): Intent? {
            for (page in pageList) {
                if (PageUtil.isFollowingTabEntity(page)) {
                    val intent = Intent(Constants.NEWS_HOME_ACTION)
                    intent.setPackage(CommonUtils.getApplication().packageName)
                    val bundle = Bundle()
                    bundle.putSerializable(NewsConstants.BUNDLE_NEWSPAGE, page)
                    intent.putExtra(NewsConstants.EXTRA_PAGE_ADDED, bundle)
                    val prevNewsAppSection = AppSectionsProvider
                            .getAnyUserAppSectionOfType(AppSection.NEWS)
                    if (prevNewsAppSection != null) {
                        intent.putExtra(Constants.APP_SECTION_ID, prevNewsAppSection.id)
                    }
                    pageReferrer?.let { intent.putExtra(NewsConstants.BUNDLE_ACTIVITY_REFERRER, it) }
                    return intent
                }
            }
            return null
        }
    }
}