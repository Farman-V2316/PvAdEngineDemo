/*
 * Copyright (c) 2020 Newshunt. All rights reserved.
 */
package com.newshunt.notification.helper

import android.content.Context
import android.content.Intent
import com.newshunt.common.helper.appconfig.AppConfig
import com.newshunt.common.helper.common.BusProvider
import com.newshunt.common.helper.common.Constants
import com.newshunt.dataentity.analytics.entity.NhAnalyticsUserAction
import com.newshunt.dataentity.analytics.referrer.NhGenericReferrer
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.common.asset.PostEntityLevel
import com.newshunt.dataentity.notification.AdsNavModel
import com.newshunt.dataentity.notification.BaseModel
import com.newshunt.dataentity.notification.BaseModelType
import com.newshunt.dataentity.notification.DeeplinkModel
import com.newshunt.dataentity.notification.ExploreNavModel
import com.newshunt.dataentity.notification.FollowNavModel
import com.newshunt.dataentity.notification.GroupNavModel
import com.newshunt.dataentity.notification.NavigationModel
import com.newshunt.dataentity.notification.NavigationType
import com.newshunt.dataentity.notification.NewsNavModel
import com.newshunt.dataentity.notification.ProfileNavModel
import com.newshunt.dataentity.notification.SearchNavModel
import com.newshunt.dataentity.notification.SocialCommentsModel
import com.newshunt.dataentity.notification.StickyNavModel
import com.newshunt.dataentity.notification.TVNavModel
import com.newshunt.dataentity.notification.WebNavModel
import com.newshunt.dataentity.notification.util.NotificationConstants
import com.newshunt.deeplink.navigator.AdsNavigator
import com.newshunt.deeplink.navigator.CommonNavigator
import com.newshunt.deeplink.navigator.ExploreSectionNavigator
import com.newshunt.deeplink.navigator.FollowSectionNavigator
import com.newshunt.deeplink.navigator.FollowSectionNavigator.Companion.isDeeplinkToFeed
import com.newshunt.deeplink.navigator.FollowingAllRoutingPresenter
import com.newshunt.deeplink.navigator.FollowingTabRoutingPresenter
import com.newshunt.deeplink.navigator.InboxNavigator
import com.newshunt.deeplink.navigator.NewsHomeRouter
import com.newshunt.deeplink.navigator.NewsNavigator
import com.newshunt.deeplink.navigator.NhBrowserNavigator
import com.newshunt.deeplink.navigator.ProfileNavigator.Companion.getTargetIntent
import com.newshunt.deeplink.navigator.SocialCommentsNavigator
import com.newshunt.deeplink.navigator.TvNavigationHelper.getTargetIntent
import com.newshunt.dhutil.helper.common.DailyhuntConstants
import com.newshunt.helper.NotificationUniqueIdGenerator
import com.newshunt.news.util.NewsConstants

/**
 * @author shrikant.agrawal
 */
object NotificationRouterHelper {

    @JvmStatic
    fun routeToModel(baseModel: BaseModel, context: Context,
                     callBack: NewsHomeRouter.Callback? = null,
                     followingAllRoutingPresenter: FollowingAllRoutingPresenter? = null,
                     followingTabRoutingPresenter: FollowingTabRoutingPresenter? = null,
                     pageReferrer: PageReferrer?): Intent? {
        return route(baseModel, context, callBack, followingAllRoutingPresenter, followingTabRoutingPresenter, pageReferrer)
    }

    /**
     * Function for opening the relevant activity based on the Base Model type.
     *
     * @param baseModel
     */
    private fun route(baseModel: BaseModel, context: Context, callBack: NewsHomeRouter.Callback?,
                      followingAllRoutingPresenter: FollowingAllRoutingPresenter?,
                      followingTabRoutingPresenter: FollowingTabRoutingPresenter?, pageReferrer: PageReferrer?): Intent? {
        return when (baseModel.baseModelType) {
            BaseModelType.NEWS_MODEL -> routeToNews(baseModel as NewsNavModel, context, callBack, pageReferrer)
            BaseModelType.TV_MODEL -> routeToTV(baseModel as TVNavModel, context, pageReferrer)
            BaseModelType.ADS_MODEL -> routeToAds(baseModel as AdsNavModel, context)
            BaseModelType.WEB_MODEL -> routeToNhBrowser(baseModel as WebNavModel, context, pageReferrer)
            BaseModelType.NAVIGATION_MODEL -> routeToDefault(baseModel as NavigationModel, context)
            BaseModelType.STICKY_MODEL -> routeStickyNotification(baseModel as StickyNavModel<*, *>, context)
            BaseModelType.EXPLORE_MODEL -> routeToExploreSection(baseModel as ExploreNavModel, context, pageReferrer)
            BaseModelType.FOLLOW_MODEL -> routeToFollowSection(baseModel as FollowNavModel, context,
                followingAllRoutingPresenter, followingTabRoutingPresenter, pageReferrer)
            BaseModelType.PROFILE_MODEL -> routeToProfileSection(baseModel as ProfileNavModel, context, pageReferrer)
            BaseModelType.DEEPLINK_MODEL -> routeToDeeplink(baseModel as DeeplinkModel, context, pageReferrer)
            BaseModelType.SOCIAL_COMMENTS_MODEL -> routeToSocialComment(baseModel as SocialCommentsModel, context, pageReferrer)
            BaseModelType.SEARCH_MODEL -> routeToSearch(baseModel as SearchNavModel, context,pageReferrer)
            BaseModelType.GROUP_MODEL -> routeToGroup(baseModel as GroupNavModel, context,pageReferrer)
            else -> null
        }
    }

    private fun routeToNews(newsNavModel: NewsNavModel, context: Context, callBack: NewsHomeRouter.Callback?, pageReferrer: PageReferrer?) : Intent? {
        val referrerId = if (newsNavModel.baseInfo != null) newsNavModel.baseInfo.id else Constants.EMPTY_STRING
         val referrer = pageReferrer?:PageReferrer(NhGenericReferrer.NOTIFICATION, referrerId)
        BusProvider.getRestBusInstance().post(NotificationDismissedEvent(newsNavModel.baseInfo.uniqueId, true))
        return if (NewsNavigator.needsNewsHomeRouting(newsNavModel)) {
            startNewsHomeRouting(newsNavModel, referrer, context, callBack)
            null
        } else {
            NewsNavigator.getNotificationTargetIntent(context, newsNavModel, pageReferrer)
        }
    }

    private fun startNewsHomeRouting(newsNavModel: NewsNavModel, pageReferrer: PageReferrer,
                                     context: Context, callBack: NewsHomeRouter.Callback?) {
        val newsHomeRouter = NewsHomeRouter(context, newsNavModel, pageReferrer)
        newsHomeRouter.setCallback(callBack)
        newsHomeRouter.startRouting()
    }

    private fun routeToTV(tvNavModel: TVNavModel, context: Context, pageReferrer: PageReferrer?): Intent? {
        val referrerId = if (tvNavModel.baseInfo != null) tvNavModel.baseInfo.id else Constants.EMPTY_STRING
        val referrer = pageReferrer?:PageReferrer(NhGenericReferrer.NOTIFICATION, referrerId)
        BusProvider.getRestBusInstance().post(NotificationDismissedEvent(tvNavModel.baseInfo.uniqueId, true))
        return getTargetIntent(tvNavModel, referrer)
    }

    private fun routeToAds(adsNavModel: AdsNavModel, context: Context): Intent? {
        return AdsNavigator.goToAdsRoutingActivity(adsNavModel)
    }

    private fun routeToDeeplink(deeplinkModel: DeeplinkModel, context: Context, pageReferrer: PageReferrer?): Intent? {
        val referrerId = if (deeplinkModel.baseInfo != null) deeplinkModel.baseInfo.id else Constants.EMPTY_STRING
        val referrer = pageReferrer?:PageReferrer(NhGenericReferrer.NOTIFICATION, referrerId)
        val intent = CommonNavigator.getDeepLinkLauncherIntent(
            deeplinkModel.deeplinkUrl, false, referrer)
        intent?.putExtra(NotificationConstants.NOTIFICATION_MESSAGE_ID, deeplinkModel.baseInfo)
        return intent
    }

    private fun routeToSocialComment(commentsModel: SocialCommentsModel, context: Context, pageReferrer: PageReferrer?): Intent? {
        val referrerId = if (commentsModel.baseInfo != null) commentsModel.baseInfo.id else Constants.EMPTY_STRING
        val referrer = pageReferrer?:PageReferrer(NhGenericReferrer.NOTIFICATION, referrerId)
        val intent = SocialCommentsNavigator.getViewAllCommentsIntent(
            context, commentsModel, referrer)
        intent?.putExtra(NotificationConstants.NOTIFICATION_MESSAGE_ID, commentsModel.baseInfo)
        return intent
    }

    private fun routeToSearch(searchNavModel: SearchNavModel, context: Context, pageReferrer: PageReferrer?): Intent? {
        val referrerId = if (searchNavModel.baseInfo != null) searchNavModel.baseInfo.id else Constants.EMPTY_STRING
        val referrer = pageReferrer?:PageReferrer(NhGenericReferrer.NOTIFICATION, referrerId)
        val intent = CommonNavigator.getSearchIntent(
            searchNavModel, referrer)
        intent?.putExtra(NotificationConstants.NOTIFICATION_MESSAGE_ID, searchNavModel.baseInfo)
        return intent
    }

    private fun routeToExploreSection(exploreNavModel: ExploreNavModel, context: Context, pageReferrer: PageReferrer?): Intent? {
        val referrerId = if (exploreNavModel.baseInfo != null) exploreNavModel.baseInfo.id else Constants.EMPTY_STRING
        val referrer = pageReferrer?:PageReferrer(NhGenericReferrer.NOTIFICATION, referrerId)
        return ExploreSectionNavigator.getTargetIntent(context, exploreNavModel, referrer)
    }

    private fun routeToFollowSection(followNavModel: FollowNavModel, context: Context,
                                     followingAllRoutingPresenter: FollowingAllRoutingPresenter?,
                                     followingTabRoutingPresenter: FollowingTabRoutingPresenter?,
                                     pageReferrer: PageReferrer?): Intent? {
        val referrerId = if (followNavModel.baseInfo != null) followNavModel.baseInfo.id else Constants.EMPTY_STRING
        val referrer = pageReferrer?:PageReferrer(NhGenericReferrer.NOTIFICATION, referrerId)
        val navigationType = NavigationType.fromIndex(followNavModel.getsType().toInt())
        return if (followingAllRoutingPresenter != null && navigationType == NavigationType.TYPE_OPEN_FOLLOWING) {
            followingAllRoutingPresenter.startRouting(followNavModel, referrer)
            null
        } else if (followingTabRoutingPresenter!= null && isDeeplinkToFeed(followNavModel)) {
            followingTabRoutingPresenter.startRouting(followNavModel, referrer)
            null
        } else {
            return FollowSectionNavigator.getTargetIntent(context, followNavModel, referrer)
        }
    }

    private fun routeToProfileSection(profileNavModel: ProfileNavModel?, context: Context, pageReferrer: PageReferrer?): Intent? {
        if (profileNavModel == null) {
            return null
        }
        val referrer = pageReferrer?:PageReferrer(NhGenericReferrer.NOTIFICATION, null)
        return getTargetIntent(profileNavModel, referrer, null)
    }

    private fun routeToGroup(groupNavModel: GroupNavModel, context: Context, pageReferrer: PageReferrer?): Intent? {
        val referrerId = if (groupNavModel.baseInfo != null) groupNavModel.baseInfo.id else Constants.EMPTY_STRING
        val referrer = pageReferrer?:PageReferrer(NhGenericReferrer.NOTIFICATION, referrerId)
        val navigationType = NavigationType.fromIndex(Integer.parseInt(groupNavModel.getsType()))
        return if (navigationType == NavigationType.TYPE_OPEN_SOCIAL_GROUP) {
            CommonNavigator.getGroupDetailIntent(groupNavModel.groupId, groupNavModel.handle, referrer)
        } else if (navigationType == NavigationType.TYPE_OPEN_SOCIAL_GROUP_APPROVAL) {
            CommonNavigator.getGroupApprovalIntent(groupNavModel.subType, referrer)
        } else if (navigationType == NavigationType.TYPE_OPEN_SOCIAL_GROUP_CREATE) {
            CommonNavigator.getGroupEditorIntent(pageReferrer, null)
        } else if (navigationType == NavigationType.TYPE_OPEN_SOCIAL_GROUP_INVITES) {
            CommonNavigator.getGroupInvitationIntent(groupNavModel.groupId, referrer)
        } else {
            null
        }
    }


    private fun routeStickyNotification(stickyNavModel: StickyNavModel<*, *>?, context: Context): Intent? {
        if (stickyNavModel == null || stickyNavModel.baseInfo == null) {
            return null
        }
        val pageReferrer = PageReferrer(NhGenericReferrer.NOTIFICATION, stickyNavModel.baseInfo.id, null, NhAnalyticsUserAction.CLICK)
        val targetIntent = CommonNavigator.getDeepLinkLauncherIntent(
            stickyNavModel.deeplinkUrl, false, pageReferrer)
        targetIntent.putExtra(Constants.FLAG_STICKY_NOTIFICATION_LANDING, true)
        return targetIntent
    }

    private fun routeToDefault(navModel: NavigationModel, context: Context): Intent? {
        return InboxNavigator.getTargetIntent(navModel, null)
    }

    private fun routeToNhBrowser(webNavModel: WebNavModel, context: Context, pageReferrer: PageReferrer?): Intent? {
        val referrerId = if (webNavModel.baseInfo != null) webNavModel.baseInfo.id else Constants.EMPTY_STRING
        val referrer = pageReferrer?:PageReferrer(NhGenericReferrer.NOTIFICATION, referrerId)
        val targetIntent = NhBrowserNavigator.getTargetIntent(referrer)
        targetIntent.putExtra(DailyhuntConstants.BUNDLE_WEB_NAV_MODEL, webNavModel)
        val notificationId = NotificationUniqueIdGenerator.generateUniqueIdForWebModel(webNavModel).toString()
        targetIntent.putExtra(Constants.BUNDLE_NOTIFICATION_UNIQUE_ID, notificationId)
        return targetIntent
    }

    @JvmStatic
    fun getIntentForReply(targetIntent: Intent) : Intent {
        val socialCommentsModel = targetIntent.getSerializableExtra(Constants.BUNDLE_COMMENTS_MODEL) as? SocialCommentsModel
        if (socialCommentsModel == null || socialCommentsModel.commentParams == null) {
            return targetIntent
        } else {
            val cid = socialCommentsModel.commentParams?.get(Constants.BUNDLE_CID)
            return if (cid == null) {
                targetIntent
            } else {
                val intent = Intent()
                intent.action = Constants.NEWS_DETAIL_ACTION
                intent.putExtra(Constants.STORY_ID, cid)
                intent.setPackage(AppConfig.getInstance()!!.packageName)
                intent.putExtra(NewsConstants.POST_ENTITY_LEVEL, PostEntityLevel.DISCUSSION.name)
                intent.putExtra(Constants.BUNDLE_NEWS_DETAIL_NON_SWIPEABLE, true)
                intent
            }
        }
    }

    @JvmStatic
    fun getIntentForShare(targetIntent: Intent) : Intent {
        val socialCommentsModel = targetIntent.getSerializableExtra(Constants.BUNDLE_COMMENTS_MODEL) as? SocialCommentsModel
        if (socialCommentsModel == null || socialCommentsModel.commentParams == null) {
            return targetIntent
        } else {
            val id = socialCommentsModel.commentParams?.get(Constants.BUNDLE_ID)
            return if (id == null) {
                targetIntent
            } else {
                val intent = Intent()
                intent.action = Constants.NEWS_DETAIL_ACTION
                intent.putExtra(Constants.STORY_ID, id)
                intent.setPackage(AppConfig.getInstance()!!.packageName)
                intent
            }
        }
    }
}
