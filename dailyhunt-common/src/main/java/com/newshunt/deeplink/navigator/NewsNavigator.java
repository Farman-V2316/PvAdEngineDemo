/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.deeplink.navigator;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.newshunt.common.helper.appconfig.AppConfig;
import com.newshunt.common.helper.common.ApplicationStatus;
import com.newshunt.common.helper.common.Constants;
import com.newshunt.common.helper.common.JsonUtils;
import com.newshunt.common.helper.preference.AppUserPreferenceUtils;
import com.newshunt.dataentity.analytics.entity.NhAnalyticsUserAction;
import com.newshunt.dataentity.analytics.referrer.NhGenericReferrer;
import com.newshunt.dataentity.analytics.referrer.PageReferrer;
import com.newshunt.dataentity.common.follow.entity.FollowNavigationType;
import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.dataentity.common.model.entity.AppSection;
import com.newshunt.dataentity.common.model.entity.UserAppSection;
import com.newshunt.dataentity.dhutil.model.entity.launch.AppSectionLaunchResult;
import com.newshunt.dataentity.news.analytics.FollowTabLandingInfoEvent;
import com.newshunt.dataentity.news.model.entity.PageType;
import com.newshunt.dataentity.notification.BaseInfo;
import com.newshunt.dataentity.notification.LangSelectionNavModel;
import com.newshunt.dataentity.notification.LocalNavModel;
import com.newshunt.dataentity.notification.NavigationType;
import com.newshunt.dataentity.notification.NewsNavModel;
import com.newshunt.dhutil.R;
import com.newshunt.dhutil.helper.appsection.AppSectionsProvider;
import com.newshunt.dhutil.helper.preference.UserPreferenceUtil;
import com.newshunt.news.analytics.NhAnalyticsAppState;
import com.newshunt.news.util.NewsConstants;

import org.jetbrains.annotations.NotNull;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

/**
 * Helper class for navigation across the screens.
 *
 * @author vishal.bharati
 */
public class NewsNavigator {

  /**
   * Function which navigate to home screen and clears all the activities and fragments on stack.
   *
   * @param activity - calling activity
   * @author vishal.bharati
   */
  public static void navigateToHeadlines(Activity activity) {
    if (activity == null) {
      return;
    }

    forceSetLastVisitedAsHomeTab();
    navigateToHomeOnLastExitedTab(activity, null);
  }

  /**
   * Function to force set the last visited tab as home tab and save it to preference, so that
   * when news home is launched, it will launch to home tab
   */
  public static void forceSetLastVisitedAsHomeTab() {
    UserAppSection prevNewsAppSection = AppSectionsProvider.INSTANCE
        .getAnyUserAppSectionOfType(AppSection.NEWS);

    if (prevNewsAppSection != null) {
      //forcing the app section to land on headlines overriding the last exited tab
      prevNewsAppSection = new UserAppSection.Builder().from(prevNewsAppSection).entityKey
          (Constants.EMPTY_STRING).build();
      AppSectionsProvider.INSTANCE.updateAppSectionInfo(prevNewsAppSection);
    }
  }

  /**
   * Function to launch news home on last exited tab.
   *
   * @param activity     - calling activity
   * @param pageReferrer
   */
  public static void navigateToHomeOnLastExitedTab(Activity activity,
                                                   PageReferrer pageReferrer) {
    if (activity == null) {
      return;
    }

    Intent intent = getIntentForNewsHome();
    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
    UserAppSection prevNewsAppSection = AppSectionsProvider.INSTANCE
        .getAnyUserAppSectionOfType(AppSection.NEWS);
    if (prevNewsAppSection != null) {
      intent.putExtra(Constants.APP_SECTION_ID, prevNewsAppSection.getId());
      intent.putExtra(Constants.APP_SECTION_LAUNCH_ENTITY, prevNewsAppSection
          .getAppSectionEntityKey());
    }
    if (pageReferrer != null) {
      intent.putExtra(NewsConstants.BUNDLE_ACTIVITY_REFERRER, pageReferrer);
    }
    AppSectionLaunchResult result =
        CommonNavigator.launchHomeIntent(activity, AppSection.NEWS, intent);
    if (result == null || !result.isLaunched()) {
      return;
    }
    AppUserPreferenceUtils.setAppSectionSelected(result.getAppSection());
    activity.finish();
  }

  /**
   * Utility function to check if headlines should be launched from any other activity in news
   *
   * @param activity             - activity calling this function
   * @param referrer             - referrer to calling activity
   * @param systemBackKeyPressed - true if to navigate to headlines on system back key pressed
   * @return - true if should navigate to headlines else false
   */
  public static boolean shouldNavigateToHome(FragmentActivity activity, PageReferrer referrer,
                                             boolean systemBackKeyPressed, String pageReferrerGeneric) {
    if (activity == null || referrer == null ||
        (!CommonNavigator.shouldDeeplinkNavigateToHome(pageReferrerGeneric) && systemBackKeyPressed &&
                CommonNavigator.isDeeplinkReferrer(referrer)) ||
        CommonNavigator.launchSplashIfFirstLaunch(activity, isActivityTaskRoot(activity))) {
      return false;
    }
    return (isActivityTaskRoot(activity)) ||
        CommonNavigator.isDeeplinkReferrer(referrer) ||
        CommonNavigator.isFromNotificationTray(referrer);
  }

  public static boolean shouldNavigateToHome(FragmentActivity activity, PageReferrer referrer,
                                             boolean systemBackKeyPressed) {
    return shouldNavigateToHome(activity,referrer,systemBackKeyPressed,null);
  }

  public static boolean isActivityTaskRoot(@NonNull FragmentActivity activity) {
    return activity.isTaskRoot() &&
        activity.getSupportFragmentManager().getBackStackEntryCount() <= 0;
  }

  public static Intent goToNewsHome(PageReferrer navReferrer) {
    if (CommonNavigator.isFromNotificationTray(navReferrer)) {
      return CommonNavigator.getSectionHomeRouterLaunchIntent(AppSection.NEWS, navReferrer);
    }
    if (!AppSectionsProvider.INSTANCE.isSectionAvailable(AppSection.NEWS)) {
      return CommonNavigator.getLastSectionHomeLaunchIntent(CommonUtils.getApplication(),
          navReferrer);
    }
    Intent intent = getIntentForNewsHome();
    intent.putExtra(Constants.HOME_INTENT, true);
    UserAppSection prevNewsAppSection = AppSectionsProvider.INSTANCE
        .getAnyUserAppSectionOfType(AppSection.NEWS);
    if (prevNewsAppSection != null) {
      intent.putExtra(Constants.APP_SECTION_ID, prevNewsAppSection.getId());
    }
    intent.putExtra(NewsConstants.INTENT_NEWS_HOME_TAB, CommonUtils.getString(com.newshunt.common.util.R.string.headlines));
    PageReferrer pageReferrer = new PageReferrer(navReferrer.getReferrer(), null);
    intent.putExtra(NewsConstants.BUNDLE_ACTIVITY_REFERRER, pageReferrer);
    return intent;
  }

  private static Intent getIntentForNewsHome() {
    Intent intent = new Intent(Constants.NEWS_HOME_ACTION);
    intent.setPackage(CommonUtils.getApplication().getPackageName());
    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
    return intent;
  }

  private static Intent goToNewsDetail(NewsNavModel newsNavModel, Context context,
                                       PageReferrer navReferrer) {
    // For instrumentation
    NhAnalyticsAppState.getInstance().setSourceId(newsNavModel.getNewsId());

    Intent intent = new Intent(Constants.NEWS_DETAIL_ACTION);
    intent.putExtra(Constants.STORY_ID, newsNavModel.getNewsId());

    if (!CommonUtils.isEmpty(newsNavModel.getViralId())) {
      intent.putExtra(NewsConstants.VIRAL_ARTICLE_ID, newsNavModel.getViralId());
    }

    if (!CommonUtils.isEmpty(newsNavModel.getParentNewsId())) {
      intent.putExtra(NewsConstants.PARENT_STORY_ID, newsNavModel.getParentNewsId());
      intent.setAction(Constants.GALLERY_PHOTO_ACTION);
    }

    // For news share analytics
    if (!CommonUtils.isEmpty(newsNavModel.getBaseInfo().getUrlParamsMap())) {
      navReferrer.setReferrer(NhGenericReferrer.ORGANIC_SOCIAL);
      intent.putExtra(Constants.REFERRER_RAW,
          JsonUtils.toJson(newsNavModel.getBaseInfo().getUrlParamsMap()));
    }

    setNotificationBackAndSwipeUrls(intent, navReferrer, newsNavModel);

    if (!CommonUtils.isEmpty(newsNavModel.getBaseInfo().getV4SwipePageLogic())) {
      intent.putExtra(NewsConstants.BUNDLE_NEXT_PAGE_LOGIC,
          newsNavModel.getBaseInfo().getV4SwipePageLogic());
    }

    if (!CommonUtils.isEmpty(newsNavModel.getBaseInfo().getV4SwipePageLogicId())) {
      intent.putExtra(NewsConstants.BUNDLE_NEXT_PAGE_LOGIC_ID,
          newsNavModel.getBaseInfo().getV4SwipePageLogicId());
    }

    if (!ApplicationStatus.isRunning()) {
      NhAnalyticsAppState.getInstance().setSourceId(newsNavModel.getNewsId());
    }
    NhAnalyticsAppState.getInstance()
        .setEventAttributionId(newsNavModel.getNewsId())
        .setReferrerId(newsNavModel.getNewsId());

    setAnalyticsIntentExtras(intent, navReferrer, newsNavModel);

    intent.putExtra(NewsConstants.BUNDLE_NOTF_DONOT_AUTO_FETCH_SWIPEURL,
        newsNavModel.getBaseInfo().isDoNotAutoFetchSwipeUrl());

    intent.putExtra(NewsConstants.IS_ADJUNCT_LANG_NEWS,newsNavModel.isAdjunct());
    intent.putExtra(NewsConstants.ADJUNCT_POPUP_DISPLAY_TYPE,newsNavModel.getPopupDisplayType());
    intent.putExtra(NewsConstants.ADJUNCT_LANGUAGE,newsNavModel.getLanguage());

    return intent;
  }


  private static Intent goToNewsPaperActivity(NewsNavModel newsNavModel, Context context,
                                              PageReferrer navReferrer) {
    Intent intent = new Intent(Constants.ENTITY_OPEN_ACTION);
    String newspaperKey = newsNavModel.getNpKey();
    String categoryKey = newsNavModel.getCtKey();
    intent.putExtra(NewsConstants.ENTITY_KEY, newspaperKey);
    intent.putExtra(NewsConstants.ENTITY_TYPE, newsNavModel.getEntityType());
    if (categoryKey != null) {
      intent.putExtra(NewsConstants.SUB_ENTITY_KEY, categoryKey);
    }
    setAnalyticsIntentExtras(intent, navReferrer, newsNavModel);
    setNotificationBackAndSwipeUrls(intent, navReferrer, newsNavModel);
    setDeeplinkUrls(intent, newsNavModel);
    return intent;
  }

  private static Intent goToViralTopic(NewsNavModel newsNavModel, PageReferrer pageReferrer) {
    Intent targetIntent = new Intent(NewsConstants.INTENT_ACTION_LAUNCH_NEWS_HOME_ROUTER);
    targetIntent.setPackage(CommonUtils.getApplication().getPackageName());
    targetIntent.addCategory(Intent.CATEGORY_DEFAULT);
    targetIntent.putExtra(NewsConstants.ENTITY_KEY, newsNavModel.getTopicKey());
    targetIntent.putExtra(NewsConstants.ENTITY_TYPE, newsNavModel.getEntityType());
    if (!CommonUtils.isEmpty(newsNavModel.getSubTopicKey())) {
      targetIntent.putExtra(NewsConstants.SUB_ENTITY_KEY, newsNavModel.getSubTopicKey());
    }
    setLanguageAndEditionIntentExtras(targetIntent, newsNavModel.getBaseInfo());
    setAnalyticsIntentExtras(targetIntent, pageReferrer, newsNavModel);
    setNotificationBackAndSwipeUrls(targetIntent, pageReferrer, newsNavModel);
    setDeeplinkUrls(targetIntent, newsNavModel);
    return targetIntent;
  }

  private static void setLanguageAndEditionIntentExtras(Intent intent, BaseInfo baseInfo) {
    if (intent == null || baseInfo == null) {
      return;
    }

    String language = baseInfo.getLanguage();
    if (!CommonUtils.isEmpty(language)) {
      intent.putExtra(NewsConstants.LANGUAGE_FROM_DEEPLINK_URL, language);
    }

    String langCode = baseInfo.getLanguageCode();
    if (!CommonUtils.isEmpty(langCode)) {
      intent.putExtra(NewsConstants.LANGUAGE_CODE_FROM_DEEPLINK_URL, langCode);
    }

    String edition = baseInfo.getEdition();
    if (!CommonUtils.isEmpty(baseInfo.getEdition())) {
      intent.putExtra(NewsConstants.EDITION_FROM_DEEPLINK_URL, edition);
    }
  }

  private static void setAnalyticsIntentExtras(Intent intent, PageReferrer pageReferrer,
                                               NewsNavModel newsNavModel) {
    if (intent == null || pageReferrer == null || newsNavModel == null) {
      return;
    }
    intent.putExtra(NewsConstants.BUNDLE_ACTIVITY_REFERRER, pageReferrer);
    NavigationType navigationType = NavigationType.fromIndex(
        Integer.parseInt(newsNavModel.getsType()));
    //no need of null check for navigation type as this evaluation would have been done by caller
    // of this method
    intent.putExtra(Constants.BUNDLE_NAVIGATION_TYPE, navigationType.name());
  }

  static void setDeeplinkUrls(Intent intent, NewsNavModel newsNavModel) {
    if (intent == null || newsNavModel == null) {
      return;
    }

    if (!CommonUtils.isEmpty(newsNavModel.getBaseInfo().getDeeplink())) {
      intent.putExtra(Constants.SELECTED_DEEP_LINK_URL, newsNavModel.getBaseInfo().getDeeplink());
    }
  }

  private static void setNotificationBackAndSwipeUrls(Intent intent, PageReferrer pageReferrer,
                                                      NewsNavModel newsNavModel) {
    if (intent == null || newsNavModel == null ||
        !CommonNavigator.isFromNotificationTray(pageReferrer)) {
      return;
    }

    // For notification back and swipe action
    if (!CommonUtils.isEmpty(newsNavModel.getBaseInfo().getV4BackUrl())) {
      intent.putExtra(Constants.V4BACKURL, newsNavModel.getBaseInfo().getV4BackUrl());
      intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    }
    if (!CommonUtils.isEmpty(newsNavModel.getBaseInfo().getV4SwipeUrl())) {
      intent.putExtra(Constants.V4SWIPEURL, newsNavModel.getBaseInfo().getV4SwipeUrl());
    }
  }

  public static Intent getDeeplinkTargetIntent(NewsNavModel newsNavModel, Context context,
                                               PageReferrer pageReferrer) {
    return getNotificationAndDeeplinkTargetIntent(context, newsNavModel, pageReferrer, false);
  }

  private static Intent getIntentForFollowExploreTab(Context context, PageReferrer pageReferrer) {
    Intent intent =
        CommonNavigator.getFollowHomeIntent(context, true, null, null, pageReferrer);
    if (intent != null) {
      FollowTabLandingInfoEvent event = new FollowTabLandingInfoEvent(PageType.EXPLORE,
          FollowNavigationType.SOURCE, pageReferrer);
      intent.putExtra(Constants.BUNDLE_FOLLOW_TAB_LANDING_INFO, event);
    }
    return intent;
  }

  @Nullable
  private static Intent getIntentForOnboardingScreenIfFirstLaunch(NewsNavModel newsNavModel) {
    if (CommonNavigator.isFirstLaunch() && newsNavModel.getBaseInfo() != null) {
      Intent onBoardingActivityIntent =
          new Intent(Constants.ONBOARDING_ACTIVITY_OPEN_ACTION);
      onBoardingActivityIntent.putExtra(NewsConstants.BUNDLE_LAUNCH_DEEPLINK,
          newsNavModel.getBaseInfo().getDeeplink());
      onBoardingActivityIntent.setFlags(
          Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
      onBoardingActivityIntent.setPackage(AppConfig.getInstance().getPackageName());
      return onBoardingActivityIntent;
    }
    return null;
  }


   static Intent getIntentForOnboardingScreenIfFirstLaunchLocal(LocalNavModel newsNavModel) {

    if (CommonNavigator.isFirstLaunch() && newsNavModel.getBaseInfo() != null) {
      Intent onBoardingActivityIntent =
          new Intent(Constants.ONBOARDING_ACTIVITY_OPEN_ACTION);
      onBoardingActivityIntent.putExtra(NewsConstants.BUNDLE_LAUNCH_DEEPLINK,
          newsNavModel.getBaseInfo().getDeeplink());
      onBoardingActivityIntent.setFlags(
          Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
      onBoardingActivityIntent.setPackage(AppConfig.getInstance().getPackageName());
      return onBoardingActivityIntent;
    }
    return null;
  }

  public static Intent getIntentForOnboardingScreen(
      @NotNull LangSelectionNavModel langeSelectionNavModel, @NotNull PageReferrer pageReferrer) {
    Intent onBoardingActivityIntent = new Intent(Constants.ONBOARDING_ACTIVITY_OPEN_ACTION);
    onBoardingActivityIntent.setPackage(AppConfig.getInstance().getPackageName());
    onBoardingActivityIntent.putExtra(Constants.BUNDLE_LAUNCHED_FROM_SETTINGS, true);
    onBoardingActivityIntent.putExtra(Constants.BUNDLE_ACTIVITY_REFERRER, pageReferrer);
    return onBoardingActivityIntent;
  }

  private static Intent getAddPageActivityIntent(NewsNavModel newsNavModel, boolean
      isFromChangeLocation) {
    Intent onBoardingActivityIntent = getIntentForOnboardingScreenIfFirstLaunch(newsNavModel);
    if (onBoardingActivityIntent != null) {
      return onBoardingActivityIntent;
    }

    Intent intent = new Intent(NewsConstants.INTENT_ACTION_LAUNCH_ADD_PAGE);
    if (isFromChangeLocation) {
      Bundle locationChangeBundle = new Bundle();
      locationChangeBundle.putString(NewsConstants.ADD_PAGE_ACTIVITY_OPEN_PAGE,
          PageType.LOCATION.name());
      intent.putExtras(locationChangeBundle);
    }
    intent.setPackage(AppConfig.getInstance().getPackageName());
    return intent;
  }

  public static Intent getNotificationTargetIntent(Context context, NewsNavModel navigationModel,
                                                   PageReferrer pageReferrer) {
    return getNotificationAndDeeplinkTargetIntent(context, navigationModel, pageReferrer, true);
  }

  private static Intent getNotificationAndDeeplinkTargetIntent(Context context,
                                                               NewsNavModel navigationModel,
                                                               PageReferrer pageReferrer,
                                                               boolean isFromNotification) {

    if (navigationModel == null) {
      return null;
    }
    NavigationType navigationType = NavigationType.fromIndex(
        Integer.parseInt(navigationModel.getsType()));
    if (navigationType == null) {
      return null;
    }

    if (pageReferrer == null && isFromNotification) {
      pageReferrer = new PageReferrer(NhGenericReferrer.NOTIFICATION,
          navigationModel.getBaseInfo().getId(), null, NhAnalyticsUserAction.CLICK);
    }
    Intent targetIntent;
    switch (navigationType) {
      case TYPE_OPEN_NEWSITEM:
      case TYPE_OPEN_VIRAL_ITEM:
        targetIntent = goToNewsDetail(navigationModel, context, pageReferrer);
        break;
      case TYPE_OPEN_NEWS_LIST:
      case TYPE_OPEN_NEWS_LIST_CATEGORY:
        targetIntent = goToNewsPaperActivity(navigationModel, context, pageReferrer);
        break;
      case TYPE_OPEN_TOPIC:
        targetIntent =
            EntityPreviewUtils.getTopicPreviewIntent(context, navigationModel, pageReferrer);
        if (isFromNotification) {
          targetIntent.addCategory(Intent.CATEGORY_DEFAULT);
        }
        break;
      case TYPE_OPEN_VIRAL_TOPIC:
        targetIntent = goToViralTopic(navigationModel, pageReferrer);
        targetIntent.addCategory(Intent.CATEGORY_DEFAULT);
        break;
      case TYPE_OPEN_LOCATION:
        targetIntent =
            EntityPreviewUtils.getLocationPreviewIntent(context, navigationModel, pageReferrer);
        if (isFromNotification) {
          targetIntent.addCategory(Intent.CATEGORY_DEFAULT);
        }
        break;

      case TYPE_OPEN_SIMILAR_STORIES:
        return goToSimilarStories(navigationModel, context, pageReferrer);

      case TYPE_OPEN_FOLLOW_EXPLORE_TAB:
        return getIntentForFollowExploreTab(context, pageReferrer);
      case TYPE_OPEN_TOPIC_LIST:
        return getAddPageActivityIntent(navigationModel, false);
      case TYPE_OPEN_LOCATION_LIST:
        return getAddPageActivityIntent(navigationModel, true);
      case TYPE_OPEN_NEWS_HOME:
        return goToNewsHome(pageReferrer);

      default:
        if (isFromNotification) {
          targetIntent = goToNewsHome(pageReferrer);
          targetIntent.addCategory(Intent.CATEGORY_DEFAULT);
          targetIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        } else {
          targetIntent = null;
        }
    }

    if (isFromNotification && targetIntent != null) {
      targetIntent.putExtra(Constants.BUNDLE_NOTIFICATION_UNIQUE_ID, navigationModel.getBaseInfo()
          .getUniqueId());
    }

    targetIntent.setPackage(AppConfig.getInstance().getPackageName());
    return targetIntent;
  }

  private static Intent goToSimilarStories(@NonNull NewsNavModel navigationModel, @NonNull
      Context context, @NonNull PageReferrer pageReferrer) {
    return getIntentForSimilarStories(context, pageReferrer, navigationModel.getGroupId(),
        navigationModel.getNewsId());
  }

  private static Intent getIntentForSimilarStories(@NonNull Context context,
                                                   @NonNull PageReferrer pageReferrer,
                                                   @NonNull String groupid,
                                                   @NonNull String storyId) {

    Intent intent = new Intent(Constants.SIMILAR_STORIES_OPEN_ACTION);
    intent.putExtra(NewsConstants.BUNDLE_GROUP_ID, groupid);
    intent.putExtra(NewsConstants.BUNDLE_ACTIVITY_REFERRER, pageReferrer);
    intent.putExtra(Constants.STORY_ID, storyId);
    return intent;
  }

  public static boolean needsNewsHomeRouting(NewsNavModel newsNavModel) {
    if (newsNavModel == null) {
      return false;
    }

    NavigationType navigationType = NavigationType.fromIndex(
        Integer.parseInt(newsNavModel.getsType()));
    if (navigationType == null) {
      return false;
    }

    return navigationType == NavigationType.TYPE_OPEN_TOPIC || navigationType ==
        NavigationType.TYPE_OPEN_LOCATION || navigationType == NavigationType.
        TYPE_OPEN_VIRAL_TOPIC;
  }

  public static boolean needsLanguageCodeSetting(NewsNavModel newsNavModel) {
    if (newsNavModel == null) {
      return false;
    }

    NavigationType navigationType = NavigationType.fromIndex(
        Integer.parseInt(newsNavModel.getsType()));
    if (navigationType == null) {
      return false;
    }

    if (navigationType != NavigationType.TYPE_OPEN_TOPIC && navigationType !=
        NavigationType.TYPE_OPEN_LOCATION &&
        navigationType != NavigationType.TYPE_OPEN_VIRAL_TOPIC) {
      return false;
    }
    return CommonUtils.isEmpty(UserPreferenceUtil.getUserLanguages());
  }

  /**
   * An event class to close the activity for launching news home, because news home should be at
   * the root of the task. This class looks empty, as there is need to close all activities for
   * news home and no need of setting uniqueRequestid.
   */
  public static class CloseForNewsHomeEvent {

  }

  public static Intent getClearTaskIntentForNewsHome() {
    Intent intent = new Intent(Constants.NEWS_HOME_ACTION);
    intent.setPackage(CommonUtils.getApplication().getPackageName());
    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
    return intent;
  }
}
