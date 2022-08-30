package com.newshunt.deeplink.navigator

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.DHConstants
import com.newshunt.common.helper.common.DeeplinkHelper
import com.newshunt.dataentity.analytics.referrer.NHGenericReferrerSource
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.dhutil.model.entity.BrowserType
import com.newshunt.dataentity.model.entity.LoginType
import com.newshunt.dataentity.notification.*
import com.newshunt.dhutil.analytics.RunTimeReferrer
import com.newshunt.dhutil.helper.browser.NHBrowserUtil
import com.newshunt.dhutil.helper.common.DailyhuntConstants
import java.net.URLDecoder

object DeeplinkNavigator {

    fun navigate(baseModel: BaseModel,
                 skipHomeRouting: Boolean,
                 context: Context,
                 pageReferrer: PageReferrer,
                 navigatorCallback: NavigatorCallback): Intent? {
        return when (baseModel.baseModelType) {
            BaseModelType.NEWS_MODEL -> {
                onDeeplinkedToNews(baseModel as NewsNavModel, skipHomeRouting, context, pageReferrer, navigatorCallback)
            }
            BaseModelType.TV_MODEL -> {
                onDeeplinkedToTV(baseModel as TVNavModel, pageReferrer, skipHomeRouting, navigatorCallback)
            }
            BaseModelType.ADS_MODEL -> {
                onDeeplinkedToAds(baseModel as AdsNavModel)
            }
            BaseModelType.WEB_MODEL -> {
                onDeepLinkedToWeb(baseModel as WebNavModel, pageReferrer)
            }
            BaseModelType.SSO_MODEL -> {
                onDeepLinkedToSSO(baseModel as SSONavModel, pageReferrer, context, navigatorCallback)
            }
            BaseModelType.SOCIAL_COMMENTS_MODEL -> {
                onDeeplinkedToSocialComments(baseModel as SocialCommentsModel, context, pageReferrer)
            }
            BaseModelType.EXPLORE_MODEL -> {
                onDeeplinkedToExplore(baseModel as ExploreNavModel, context, pageReferrer)
            }
            BaseModelType.FOLLOW_MODEL -> {
                onDeeplinkedToFollow(baseModel as FollowNavModel, context, pageReferrer, navigatorCallback)
            }
            BaseModelType.PROFILE_MODEL -> {
                onDeeplinkedToProfile(baseModel as ProfileNavModel, pageReferrer, navigatorCallback)
            }
            BaseModelType.GROUP_MODEL -> {
                onDeeplinkToGroup(baseModel as GroupNavModel, pageReferrer)
            }
            BaseModelType.SEARCH_MODEL -> {
                onDeeplinkedToSearch(baseModel as SearchNavModel, pageReferrer)
            }
            BaseModelType.CREATE_POST_MODEL -> {
                onDeeplinkToCreatePost(baseModel as CreatePostNavModel, pageReferrer)
            }
            BaseModelType.CONTACTS_RECO_MODEL -> {
                onDeeplinkToContactsReco(baseModel as ContactsRecoNavModel, pageReferrer)
            }
            BaseModelType.RUNTIME_PERMISSIONS -> {
                onDeeplinkToRuntimePermission(baseModel as PermissionNavModel, pageReferrer)
            }
            BaseModelType.LANG_SELECTION -> {
                onDeeplinkToLangSelection(baseModel as LangSelectionNavModel, pageReferrer)
            }
            BaseModelType.ADJUNCT_LANG_MODEL -> {
                onDeeplinkToAdjunctLang(baseModel as AdjunctLangModel)
            }
            BaseModelType.APP_SECTION_MODEL -> {
                onDeeplinkToAppSection(baseModel as AppSectionModel)
            }
            BaseModelType.SETTINGS -> {
                onDeeplinkToSettings(baseModel as SettingsModel)
            }
            BaseModelType.NOTIFICATION_INBOX -> {
                onDeeplinkToNotificationInbox(baseModel as NotificationInboxModel)
            }
            BaseModelType.SETTINGS_AUTOSCROLL -> {
                onDeeplinkToSettingsAutoScroll(baseModel as SettingsAutoScrollModel)
            }
            BaseModelType.NOTIFICATION_SETTINGS -> {
                onDeeplinkToNotificationSettings(baseModel as NotificationSettingsModel, context)
            }
            BaseModelType.LOCAL_MODEL -> {
                onDeeplinkToLocal(baseModel as LocalNavModel, pageReferrer)
            }
            else -> {
                null
            }
        }
    }

    /**
     * Extacts deeplinkUrl parama - intent, json-deserialises it, and fills its entries in the bundle
     * Currently, used for passing 'enableNudge' in discovery section.
     * But `intent` can contain other custem flags.
     * The fragments/activities, look for the flags they want.
     *
     */
    @JvmStatic
    @JvmOverloads
    fun extractDeeplinkIntentParamToIntent(intent: Intent?, baseModel: BaseModel? = null, intentParamString: String? = null) {
        if (intent == null || (baseModel == null && intentParamString == null)) return
        val intentParams = intentParamString
                ?: baseModel?.baseInfo?.queryParams?.get("intent")?.let { URLDecoder.decode(it) }
        if (intentParams.isNullOrEmpty()) return
        // copy each entry in the map to intent bundle
        kotlin.runCatching {
            val token = object : TypeToken<HashMap<String, Any>>() {}.type
            val map = Gson().fromJson<HashMap<String, Any>>(intentParams, token)
            map.forEach { entry ->
                val value = entry.value
                (value as? String)?.let {
                    intent.putExtra(entry.key, it)
                }
                (value as? Double)?.let {
                    intent.putExtra(entry.key, it)
                }
                (value as? Boolean)?.let {
                    intent.putExtra(entry.key, it)
                }
            }
        }
    }

    private fun onDeeplinkedToNews(newsNavModel: NewsNavModel, skipHomeRouting: Boolean,
                                   context: Context, pageReferrer: PageReferrer,
                                   navigatorCallback: NavigatorCallback): Intent? {
        val needsNewsHomeRouting = !skipHomeRouting && NewsNavigator.needsNewsHomeRouting(newsNavModel)
        return if (!needsNewsHomeRouting) {
            NewsNavigator.getDeeplinkTargetIntent(newsNavModel, context, pageReferrer)
        } else {
            navigatorCallback.startNewsHomeRouting(newsNavModel)
            RoutingIntent()
        }
    }

    private fun onDeeplinkedToTV(tvNavModel: TVNavModel, pageReferrer: PageReferrer, skipHomeRouting: Boolean,
                                 navigatorCallback: NavigatorCallback): Intent? {
        return if (!skipHomeRouting && TvNavigationHelper.isHomeRoutable(tvNavModel)) {
            navigatorCallback.startTvHomeRouting(tvNavModel)
            RoutingIntent()
        } else {
            TvNavigationHelper.getTargetIntent(tvNavModel, pageReferrer)
        }
    }

    private fun onDeeplinkedToAds(adsNavModel: AdsNavModel): Intent? {
        return AdsNavigator.goToAdsRoutingActivity(adsNavModel)
    }

    private fun onDeepLinkedToWeb(webNavModel: WebNavModel, pageReferrer: PageReferrer): Intent? {
        val targetIntent = NhBrowserNavigator.getTargetIntent(pageReferrer)
        targetIntent.putExtra(DailyhuntConstants.BUNDLE_WEB_NAV_MODEL, webNavModel)
        return targetIntent
    }

    private fun onDeeplinkedToSearch(searchNavModel: SearchNavModel, pageReferrer: PageReferrer): Intent? {
        return CommonNavigator.getSearchIntent(searchNavModel, pageReferrer)
    }

    private fun onDeepLinkedToSSO(ssoNavModel: SSONavModel, pageReferrer: PageReferrer, context: Context, navigatorCallback: NavigatorCallback): Intent? {

        var targetIntent: Intent? = null
        if (navigatorCallback.isLoggedIn()) {
            val url = ssoNavModel.url
            if (CommonUtils.isEmpty(url)) {
                targetIntent = CommonNavigator.getProfileHomeIntent(null, pageReferrer, null)
            } else {
                if (DeeplinkHelper.isInternalDeeplinkUrl(url)) {
                    CommonNavigator.launchDeeplink(context, url, null)
                } else {
                    val browserType = BrowserType.fromName(ssoNavModel.browserType)
                    NHBrowserUtil.handleBrowserSelection(context as Activity, url,
                            browserType, null, ssoNavModel.isUseWideViewPort, ssoNavModel
                            .isClearHistoryOnPageLoad)
                }
            }
        } else {
            val loginType = LoginType.fromValue(ssoNavModel.loginType)
            val browserType = BrowserType.fromName(ssoNavModel.browserType)
            targetIntent = SSONavigator.getIntentForSignIn(loginType, false, loginType !== LoginType.NONE, ssoNavModel.url, browserType, ssoNavModel.isUseWideViewPort,
                    ssoNavModel.isClearHistoryOnPageLoad, pageReferrer)
        }
        return targetIntent
    }

    private fun onDeeplinkedToSocialComments(socialCommentsModel: SocialCommentsModel,
                                             context: Context,
                                             pageReferrer: PageReferrer): Intent? {
        return SocialCommentsNavigator.getViewAllCommentsIntent(context, socialCommentsModel, pageReferrer)
    }

    private fun onDeeplinkedToExplore(exploreNavModel: ExploreNavModel, context: Context, pageReferrer: PageReferrer): Intent? {
        return ExploreSectionNavigator.getTargetIntent(context, exploreNavModel, pageReferrer)
    }

    private fun onDeeplinkedToFollow(followNavModel: FollowNavModel, context: Context, pageReferrer: PageReferrer,
                                     navigatorCallback: NavigatorCallback): Intent? {
        val navigationType = NavigationType.fromIndex(Integer.parseInt(followNavModel.getsType()))

        return if (navigationType == NavigationType.TYPE_OPEN_FOLLOWING || navigationType == NavigationType.TYPE_OPEN_FOLLOWERS) {
            FollowSectionNavigator.getFollowedAllIntent(context, pageReferrer, followNavModel)
        } else if (FollowSectionNavigator.isDeeplinkToFeed(followNavModel)) {
            navigatorCallback.onDeeplinkedToFollowingTab(followNavModel)
            RoutingIntent()
        } else {
            FollowSectionNavigator.getTargetIntent(context, followNavModel, pageReferrer)
        }
    }

    private fun onDeeplinkedToProfile(profileNavModel: ProfileNavModel, pageReferrer: PageReferrer, navigatorCallback: NavigatorCallback): Intent? {
        return ProfileNavigator.getTargetIntent(profileNavModel, pageReferrer, navigatorCallback.getUserId())
    }

    private fun onDeeplinkToGroup(groupNavModel: GroupNavModel, pageReferrer: PageReferrer): Intent? {
        val navigationType = NavigationType.fromIndex(Integer.parseInt(groupNavModel.getsType()))
        return if (navigationType == NavigationType.TYPE_OPEN_SOCIAL_GROUP) {
            CommonNavigator.getGroupDetailIntent(groupNavModel.groupId, groupNavModel.handle, pageReferrer)
        } else if (navigationType == NavigationType.TYPE_OPEN_SOCIAL_GROUP_APPROVAL) {
            CommonNavigator.getGroupApprovalIntent(groupNavModel.subType, pageReferrer)
        } else if (navigationType == NavigationType.TYPE_OPEN_SOCIAL_GROUP_CREATE) {
            CommonNavigator.getGroupEditorIntent(pageReferrer, null)
        } else if (navigationType == NavigationType.TYPE_OPEN_SOCIAL_GROUP_INVITES) {
            CommonNavigator.getGroupInvitationIntent(groupNavModel.groupId, pageReferrer)
        } else {
            null
        }
    }

    private fun onDeeplinkToCreatePost(cpNavModel: CreatePostNavModel, pageReferrer: PageReferrer): Intent? {
        val navigationType = NavigationType.fromIndex(Integer.parseInt(cpNavModel.getsType()))
        if (navigationType == NavigationType.TYPE_OPEN_CREATE_POST) {
            return CommonNavigator.getPostCreationIntent(null, null, null,
                    pageReferrer)
        }
        return null
    }

    private fun onDeeplinkToContactsReco(contactsRecoNavModel: ContactsRecoNavModel, pageReferrer: PageReferrer): Intent? {
        val navigationType = NavigationType.fromIndex(Integer.parseInt(contactsRecoNavModel.getsType()))
        if (navigationType == NavigationType.TYPE_OPEN_CONTACTS_RECO) {
            return CommonNavigator.getContactsRecoIntent(pageReferrer, contactsRecoNavModel)
        }
        return null
    }

    private fun onDeeplinkToRuntimePermission(permissionNavModel: PermissionNavModel?, pageReferrer: PageReferrer?): Intent? {
        return PermissionNavigator.getIntentPermissionActivity(permissionNavModel, pageReferrer)
    }

    private fun onDeeplinkToLangSelection(langSelectionModel: LangSelectionNavModel, pageReferrer: PageReferrer): Intent? {
        val navigationType = NavigationType.fromIndex(Integer.parseInt(langSelectionModel.getsType()))
        return if (navigationType == NavigationType.TYPE_OPEN_LANG_SELECTION) {
            val targetReferrer =
                    if (langSelectionModel.referrer != null) /*if present in deeplink, prefer it*/
                        PageReferrer(RunTimeReferrer(langSelectionModel.referrer, NHGenericReferrerSource.DEEPLINK), langSelectionModel.referrerId)
                    else pageReferrer
            NewsNavigator.getIntentForOnboardingScreen(langSelectionModel, targetReferrer)
        } else null
    }

    private fun onDeeplinkToAdjunctLang(adjunctLangModel: AdjunctLangModel): Intent? {
        val navigationType = NavigationType.fromIndex(Integer.parseInt(adjunctLangModel.getsType()))
        if (navigationType == NavigationType.TYPE_HANDLE_ADJUNCT_LANG) {
            var adjunctLang:String ?= null
            var tickClicked:Boolean ?= null
            var langFlow:String ?= null
            adjunctLangModel.addLang?.let {
                adjunctLang = it
                tickClicked = true
            }

            adjunctLangModel.removeLang?.let {
                adjunctLang = it
                tickClicked = false
            }

            adjunctLangModel.langFlow?.let {
                langFlow = it
            }
            return CommonNavigator.getIntentForOnboardingScreenForAdjunctLang(adjunctLangModel,
              adjunctLang, tickClicked, langFlow)
        }
        else return null
    }

    private fun onDeeplinkToAppSection(appSectionModel: AppSectionModel): Intent? {
        val navigationType = NavigationType.fromIndex(Integer.parseInt(appSectionModel.getsType()))
        if(navigationType == NavigationType.TYPE_HANDLE_APP_SECTION) {
            val sectionId = appSectionModel.appSectionId
            if(!sectionId.isNullOrEmpty()) {
                return CommonNavigator.getIntentForAppSection(sectionId)
            }
        }
        return null
    }

    private fun onDeeplinkToSettings(settingsModel: SettingsModel): Intent? {
        val navigationType = NavigationType.fromIndex(Integer.parseInt(settingsModel.getsType()))
        if(navigationType == NavigationType.TYPE_SETTINGS) {
            val intent = CommonNavigator.getIntentForSettingsSection(settingsModel.settingsSection)
            return intent
        }
        return null
    }

    private fun onDeeplinkToNotificationInbox(notificationInboxModel: NotificationInboxModel): Intent? {
        val navigationType = NavigationType.fromIndex(Integer.parseInt(notificationInboxModel.getsType()))
        if(navigationType == NavigationType.TYPE_NOTIFICATION_INBOX) {
            return CommonNavigator.getIntentForNotificationInbox()
        }
        return null
    }

    private fun onDeeplinkToNotificationSettings(notificationSettingsModel: NotificationSettingsModel, context: Context): Intent? {
        val navigationType = NavigationType.fromIndex(Integer.parseInt(notificationSettingsModel.getsType()))
        if(navigationType == NavigationType.TYPE_NOTIFICATION_SETTINGS) {
            return CommonNavigator.openDeviceNotificationSettingsScreen(context)
        }
        return null
    }

    private fun onDeeplinkToSettingsAutoScroll(settingsAutoScrollModel: SettingsAutoScrollModel): Intent? {
        val navigationType = NavigationType.fromIndex(Integer.parseInt(settingsAutoScrollModel.getsType()))
        if(navigationType == NavigationType.TYPE_SETTINGS_AUTOSCROLL) {
            val intent = Intent(DHConstants.SETTINGS_OPEN_ACTION)
            intent.setPackage(CommonUtils.getApplication().packageName)
            intent.putExtra(Constants.SETTINGS_SCROLL_TO,settingsAutoScrollModel.scrollToPosition)
            return intent
        }
        return null
    }

    private fun onDeeplinkToLocal(localNavModel: LocalNavModel, pageReferrer:
    PageReferrer): Intent? {
        val onBoardingActivityIntent = NewsNavigator.getIntentForOnboardingScreenIfFirstLaunchLocal(localNavModel)
        if (onBoardingActivityIntent != null) {
            return onBoardingActivityIntent
        }
        val navigationType = NavigationType.fromIndex(Integer.parseInt(localNavModel.getsType()))
		if (navigationType == NavigationType.TYPE_OPEN_LOCAL_SECTION) {
            return CommonNavigator.getLocalBottombarSectionIntent(pageReferrer)
        }
        return null
    }
}

interface NavigatorCallback {
    fun isLoggedIn(): Boolean
    fun onDeeplinkedToFollowingTab(followNavModel: FollowNavModel)
    fun startNewsHomeRouting(newsNavModel: NewsNavModel)
    fun startTvHomeRouting(tvNavModel: TVNavModel)
    fun getUserId(): String?

}

class RoutingIntent : Intent() {

}