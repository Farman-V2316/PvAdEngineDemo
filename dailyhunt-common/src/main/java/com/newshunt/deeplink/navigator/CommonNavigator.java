package com.newshunt.deeplink.navigator;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.reflect.TypeToken;
import com.newshunt.app.helper.DHGameProvider;
import com.newshunt.common.helper.appconfig.AppConfig;
import com.newshunt.common.helper.common.Constants;
import com.newshunt.common.helper.common.DHConstants;
import com.newshunt.common.helper.common.JsonUtils;
import com.newshunt.common.helper.common.LaunchSearch;
import com.newshunt.common.helper.common.Logger;
import com.newshunt.common.helper.preference.AppUserPreferenceUtils;
import com.newshunt.common.helper.preference.PreferenceManager;
import com.newshunt.dataentity.analytics.referrer.NHGenericReferrerSource;
import com.newshunt.dataentity.analytics.referrer.NhGenericReferrer;
import com.newshunt.dataentity.analytics.referrer.PageReferrer;
import com.newshunt.dataentity.analytics.section.NhAnalyticsEventSection;
import com.newshunt.dataentity.common.asset.CommonAsset;
import com.newshunt.dataentity.common.asset.CreatePostUiMode;
import com.newshunt.dataentity.common.follow.entity.FollowSnackBarEntity;
import com.newshunt.dataentity.common.follow.entity.FollowSnackBarInfo;
import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.dataentity.common.model.entity.AppSection;
import com.newshunt.dataentity.common.model.entity.SearchRequestType;
import com.newshunt.dataentity.common.model.entity.UserAppSection;
import com.newshunt.dataentity.common.pages.PageEntity;
import com.newshunt.dataentity.dhutil.model.entity.launch.AppSectionLaunchParameters;
import com.newshunt.dataentity.dhutil.model.entity.launch.AppSectionLaunchResult;
import com.newshunt.dataentity.model.entity.GroupBaseInfo;
import com.newshunt.dataentity.model.entity.GroupInfo;
import com.newshunt.dataentity.model.entity.GroupPojosKt;
import com.newshunt.dataentity.model.entity.LoginType;
import com.newshunt.dataentity.model.entity.ProfilePojosKt;
import com.newshunt.dataentity.model.entity.ProfileTabType;
import com.newshunt.dataentity.model.entity.ReviewItem;
import com.newshunt.dataentity.model.entity.UserBaseProfile;
import com.newshunt.dataentity.news.analytics.FollowReferrerSource;
import com.newshunt.dataentity.news.analytics.NewsReferrerSource;
import com.newshunt.dataentity.news.analytics.ProfileReferrerSource;
import com.newshunt.dataentity.notification.AdjunctLangModel;
import com.newshunt.dataentity.notification.BaseModel;
import com.newshunt.dataentity.notification.FollowModel;
import com.newshunt.dataentity.notification.FollowNavModel;
import com.newshunt.dataentity.notification.SearchNavModel;
import com.newshunt.dataentity.search.SearchPayloadContext;
import com.newshunt.dataentity.search.SearchSuggestionItem;
import com.newshunt.dataentity.sso.model.entity.AccountLinkType;
import com.newshunt.deeplink.DeeplinkUtils;
import com.newshunt.deeplink.Deeplinker;
import com.newshunt.dhutil.R;
import com.newshunt.dhutil.helper.appsection.AppSectionsProvider;
import com.newshunt.dhutil.helper.launch.CampaignAcquisitionHelper;
import com.newshunt.dhutil.helper.preference.AppStatePreference;
import com.newshunt.dhutil.model.SettingsSection;
import com.newshunt.news.util.NewsConstants;
import com.newshunt.sso.SignInUIModes;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.newshunt.common.helper.common.Constants.INTENT_ACTIONS_LAUNCH_CREATE_POST;

/**
 * Helps in navigating between different verticals like news, buzz, live tv.
 *
 * @author maruti.borker
 */
public class CommonNavigator {

  private static final String TAG = "CommonNavigator";

  private static int NO_INTENT_FLAG = -1;

  /**
   * Utility function to check if referrer is through deeplink
   *
   * @param referrer - referrer to check
   * @return - true if it is through deeplink
   */
  public static boolean isDeeplinkReferrer(PageReferrer referrer) {
    return referrer != null && referrer.getReferrerSource() == NHGenericReferrerSource.DEEPLINK;
  }

  public static boolean shouldDeeplinkNavigateToHome(String referrerRaw) {
    Type type = new TypeToken<HashMap<String, String>>() {}.getType();
    Map<String, String> referrerMap = JsonUtils.fromJson(referrerRaw, type);
    return referrerMap != null && referrerMap.containsKey(Constants.REFERRER_RAW_SS);
  }

  /**
   * Utility function to check if referrer is through notifcation either through notification
   * tray or notification inbox
   *
   * @param referrer - referrer to check
   * @return - true if it is through notifications
   */
  public static boolean isFromNotifications(PageReferrer referrer) {
    return isFromNotificationTray(referrer) || isFromNotificationInbox(referrer);
  }

  /**
   * Utility function to check if referrer is through notification inbox
   *
   * @param referrer - referrer to check
   * @return - true if it is from notification inbox
   */
  public static boolean isFromNotificationInbox(PageReferrer referrer) {
    return referrer != null && referrer.getReferrerSource() == NHGenericReferrerSource
        .NOTIFICATION_INBOX_VIEW;
  }

  /**
   * Utility function to check if referrer is through notification tray
   *
   * @param referrer - referrer to check
   * @return - true if it is from notification tray
   */
  public static boolean isFromNotificationTray(PageReferrer referrer) {
    return referrer != null && referrer.getReferrerSource() == NHGenericReferrerSource
        .NOTIFICATION_TRAY;
  }

  public static boolean isFromFollowHome(PageReferrer referrer) {
    return referrer != null && referrer.getReferrerSource() == FollowReferrerSource
        .FOLLOW_HOME_VIEW;
  }

  public static boolean isFromDeeplink(PageReferrer referrer) {
    return referrer != null && referrer.getReferrerSource() == NHGenericReferrerSource.DEEPLINK;
  }

  public static boolean isFromSearch(PageReferrer referrer) {
    return referrer != null && referrer.getReferrerSource() == NewsReferrerSource.SEARCH_HOME_VIEW;
  }

  public static boolean isFromNewsDetail(PageReferrer referrer) {
    return referrer != null && referrer.getReferrerSource() == NewsReferrerSource.NEWS_DETAIL_VIEW;
  }

  public static boolean isProfileReferrer(PageReferrer referrer) {
    return referrer != null &&
        referrer.getReferrerSource() == ProfileReferrerSource.PROFILE_HOME_VIEW;
  }

  private static void fillIntentParameters(Intent intent, String sectionId, String entityKey,
                                           int flags, PageReferrer pageReferrer) {
    if (intent == null) {
      return;
    }

    if (flags != NO_INTENT_FLAG) {
      intent.addFlags(flags);
    }

    if (!CommonUtils.isEmpty(sectionId)) {
      intent.putExtra(Constants.APP_SECTION_ID, sectionId);
    }

    if (!CommonUtils.isEmpty(entityKey)) {
      intent.putExtra(Constants.APP_SECTION_LAUNCH_ENTITY, entityKey);
    }

    if (pageReferrer != null) {
      intent.putExtra(Constants.BUNDLE_ACTIVITY_REFERRER, pageReferrer);
    }
  }

  public static boolean launchNewsHome(Context activity, boolean skipAnimation, String
      sectionId, String entityKey) {
    return launchNewsHome(activity, skipAnimation, sectionId, entityKey, null, false);
  }

  public static boolean launchNewsHome(Context activity, boolean skipAnimation, String
      sectionId, String entityKey, PageReferrer pageReferrer) {
    return launchNewsHome(activity, skipAnimation, sectionId, entityKey, pageReferrer, false);
  }

  public static boolean launchNewsHome(Context activity, boolean skipAnimation, String
      sectionId, String entityKey, PageReferrer pageReferrer, boolean needClearTask) {
    Intent newsHomeIntent =
        getNewsHomeIntent(activity, skipAnimation, sectionId, entityKey, pageReferrer,
            needClearTask);

    try {
      activity.startActivity(newsHomeIntent);
      int count = AppUserPreferenceUtils.getNewsLaunchCount();
      AppUserPreferenceUtils.saveNewsLaunchCount(++count);
    } catch (ActivityNotFoundException e) {
      return false;
    }
    return true;
  }

  public static Intent getNewsHomeIntent(Context activity, boolean skipAnimation, String
      sectionId, String entityKey, PageReferrer pageReferrer, boolean needClearTask) {
    Intent newsHomeIntent = new Intent();
    newsHomeIntent.setAction(Constants.NEWS_HOME_ACTION);
    newsHomeIntent.setPackage(CommonUtils.getApplication().getPackageName());

    int flags = Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP;
    if (needClearTask) {
      flags |= Intent.FLAG_ACTIVITY_CLEAR_TASK;
    }
    if (skipAnimation) {
      flags |= Intent.FLAG_ACTIVITY_NO_ANIMATION;
    }

    fillIntentParameters(newsHomeIntent, sectionId, entityKey, flags, pageReferrer);
    return newsHomeIntent;
  }

  public static Intent getPostCreationIntent(String postId, CreatePostUiMode mode,
                                             SearchSuggestionItem suggestionItem,
                                             PageReferrer pageReferrer) {
    return getPostCreationIntent(postId, mode, suggestionItem, pageReferrer, null, null, null,
        null, null);
  }

  public static Intent getPostCreationIntent(String postId, CreatePostUiMode mode,
                                             SearchSuggestionItem suggestionItem,
                                             PageReferrer pageReferrer,
                                             Serializable localInfo) {
    return getPostCreationIntent(postId, mode, suggestionItem, pageReferrer,
        localInfo, null, null, null, null);
  }

  public static Intent getPostCreationIntent(String postId, CreatePostUiMode mode,
                                             SearchSuggestionItem suggestionItem,
                                             PageReferrer pageReferrer,
                                             Serializable localInfo,
                                             String sourceId,
                                             String sourceType,
                                             String parentPostId) {
    return getPostCreationIntent(postId, mode, suggestionItem, pageReferrer,
        localInfo, sourceId, sourceType, parentPostId, null);

  }

  public static Intent getContactsRecoIntent(PageReferrer pageReferrer, BaseModel baseModel) {
    Intent intent = new Intent(Constants.IMPORT_CONTACTS_ACTIONS);
    intent.putExtra(Constants.BUNDLE_ACTIVITY_REFERRER, pageReferrer);
    intent.putExtra(Constants.BUNDLE_CONTACT_RECO_MODEL, baseModel);
    intent.setPackage(AppConfig.getInstance().getPackageName());
    return intent;
  }

  public static Intent getPostCreationIntent(String postId, CreatePostUiMode mode,
                                             SearchSuggestionItem suggestionItem,
                                             PageReferrer pageReferrer,
                                             Serializable localInfo,
                                             String sourceId,
                                             String sourceType,
                                             String parentPostId, GroupInfo groupInfo) {
    Intent addCommentIntent = new Intent();
    addCommentIntent.setAction(INTENT_ACTIONS_LAUNCH_CREATE_POST);
    addCommentIntent.putExtra(Constants.BUNDLE_POST_ID, postId);
    addCommentIntent.putExtra(Constants.BUNDLE_PARENT_ID,
        (parentPostId == null) ? postId : parentPostId);
    addCommentIntent.putExtra(Constants.BUNDLE_SOURCE_ID, sourceId);
    addCommentIntent.putExtra(Constants.BUNDLE_SOURCE_TYPE, sourceType);
    addCommentIntent.putExtra(Constants.BUNDLE_MODE, mode);
    addCommentIntent.putExtra(Constants.BUNDLE_CREATE_POST_TAG_DATA, suggestionItem);
    addCommentIntent.putExtra(Constants.BUNDLE_CREATE_POST_NEXT_CARD_ID_FOR_LOCAL_CARD, localInfo);
    addCommentIntent.putExtra(Constants.BUNDLE_ACTIVITY_REFERRER, pageReferrer);
    addCommentIntent.putExtra(Constants.BUNDLE_GROUP_INFO, groupInfo);
    addCommentIntent.setPackage(CommonUtils.getApplication().getPackageName());
    return addCommentIntent;
  }

  public static Intent getIntentForOnboardingScreenForAdjunctLang(
      @NotNull AdjunctLangModel adjunctLangModel, String adjunctLang,
      Boolean tickClicked,String langFlow) {
    Intent onBoardingActivityIntent = new Intent(Constants.ONBOARDING_ACTIVITY_OPEN_ACTION);
    onBoardingActivityIntent.setPackage(AppConfig.getInstance().getPackageName());
    if(adjunctLang != null) {
      onBoardingActivityIntent.putExtra(Constants.ADJUNCT_LANG_FROM_TICK_CROSS, adjunctLang);
    }
    if(tickClicked != null) {
      onBoardingActivityIntent.putExtra(Constants.ADJUNCT_LANG_TICK_CLICKED, tickClicked);
      if(!tickClicked) {
        onBoardingActivityIntent.putExtra(Constants.BUNDLE_LAUNCHED_FROM_SETTINGS,true);
        onBoardingActivityIntent.putExtra(Constants.IS_FROM_ADJUNCT_CROSS,true);
      }
    } else {
      onBoardingActivityIntent.putExtra(Constants.BUNDLE_LAUNCHED_FROM_SETTINGS,true);
    }
    if(langFlow != null) {
      onBoardingActivityIntent.putExtra(Constants.ADJUNCT_LANG_FLOW, langFlow);
    }
    return onBoardingActivityIntent;
  }

  public static Intent getIntentForAppSection(String sectionId){
    UserAppSection prevAppSection = getPreviousUserAppSection();
    Intent  intent = getNewsHomeIntent(CommonUtils.getApplication(), false, prevAppSection.getId(), prevAppSection
            .getAppSectionEntityKey(), null, false);
    intent.putExtra(Constants.SECTION_ID,sectionId);
    return intent;
  }

  public static Intent getIntentForSettingsSection(String section) {
    SettingsSection settingsSection = SettingsSection.fromName(section);
    Intent intent = new Intent(DHConstants.SETTINGS_OPEN_ACTION);
    if(settingsSection != null) {
      switch (settingsSection) {
        case NOTIFICATION:
          intent = new Intent(DHConstants.OPEN_NOTIFICATION_ACTIVITY);
          break;
        case APP_LANGUAGE:
          intent = new Intent(DHConstants.OPEN_APP_LANGUAGE);
          break;
        case PREFERRED_LOCATION:
          intent = new Intent(DHConstants.OPEN_LOCATION_SELECTION);
          break;
        case BLOCKED_SOURCES: {
          intent = new Intent(DHConstants.OPEN_FOLLOW_ENTITIES_SCREEN);
          FollowNavModel followNavModel = new FollowNavModel(null, null, null, null,
                  FollowModel.BLOCKED);
          intent.putExtra(NewsConstants.BUNDLE_OPEN_FOLLOWED_ENTITY, followNavModel);
          intent.putExtra(Constants.BUNDLE_FOLLOW_MODEL, FollowModel.BLOCKED.name());
        }
          break;
        case FEEDBACK:
          intent = new Intent(Constants.FEEDBACK_OPEN);
          break;
        default:
          break;
      }
    }
    intent.setPackage(CommonUtils.getApplication().getPackageName());
    return intent;
  }

  public static Intent getIntentForNotificationInbox() {
    Intent notificationInboxIntent = new Intent();
    notificationInboxIntent.setPackage(CommonUtils.getApplication().getPackageName());
    notificationInboxIntent.setFlags(
            Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
    notificationInboxIntent.setAction(Constants.NOTIFICATION_INBOX);
    return notificationInboxIntent;
  }
  public static Intent getLocalBottombarSectionIntent(PageReferrer pageReferrer) {
    Intent getLocalBottombarSectionIntent = new Intent();
    getLocalBottombarSectionIntent.setAction(DHConstants.OPEN_LOCAL_SCREEN);
    getLocalBottombarSectionIntent.putExtra(Constants.BUNDLE_ACTIVITY_REFERRER, pageReferrer);
    getLocalBottombarSectionIntent.setPackage(CommonUtils.getApplication().getPackageName());
    return getLocalBottombarSectionIntent;
  }

  public static Intent getAddReplyIntent(String postId, String commentId,
                                         SearchSuggestionItem sourceHandle, CreatePostUiMode mode,
                                         PageReferrer pageReferrer, String sourceId,
                                         String sourceType) {
    Intent addCommentIntent = new Intent();
    addCommentIntent.setAction(INTENT_ACTIONS_LAUNCH_CREATE_POST);
    addCommentIntent.putExtra(Constants.BUNDLE_POST_ID, postId);
    addCommentIntent.putExtra(Constants.BUNDLE_PARENT_ID, commentId);
    addCommentIntent.putExtra(Constants.BUNDLE_SOURCE_ID, sourceId);
    addCommentIntent.putExtra(Constants.BUNDLE_SOURCE_TYPE, sourceType);
    addCommentIntent.putExtra(Constants.BUNDLE_MODE, mode);
    addCommentIntent.putExtra(Constants.BUNDLE_CREATE_POST_TAG_DATA, sourceHandle);
    addCommentIntent.putExtra(Constants.BUNDLE_ACTIVITY_REFERRER, pageReferrer);
    addCommentIntent.setPackage(CommonUtils.getApplication().getPackageName());
    return addCommentIntent;
  }

  public static void launchNewsHome(Context context, PageReferrer pageReferrer) {
    launchNewsHome(context, pageReferrer, false);
  }

  public static void launchNewsHome(Context context, PageReferrer pageReferrer,
                                    boolean needClearTask) {
    UserAppSection prevNewsAppSection =
        AppSectionsProvider.INSTANCE.getAnyUserAppSectionOfType(AppSection.NEWS);
    if (prevNewsAppSection != null) {
      CommonNavigator.launchNewsHome(context, true, prevNewsAppSection.getId(),
          prevNewsAppSection.getAppSectionEntityKey(), pageReferrer, needClearTask);
    }
  }

  public static void launchSplash(Context context) {
    Intent intent = new Intent();
    intent.setAction(Constants.SPLASH_ACTION);
    intent.setPackage(CommonUtils.getApplication().getPackageName());
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
    intent.putExtra(Constants.BUNDLE_SPLASH_RELAUNCH, true);
    context.startActivity(intent);
  }

  public static void launchSplashWithClearTask(Context context) {
    Intent intent = new Intent();
    intent.setAction(Constants.SPLASH_ACTION);
    intent.setPackage(CommonUtils.getApplication().getPackageName());
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP |
        Intent.FLAG_ACTIVITY_CLEAR_TASK);
    intent.putExtra(Constants.BUNDLE_SPLASH_RELAUNCH, true);
    context.startActivity(intent);
  }

  public static boolean isFirstLaunch() {
    return PreferenceManager.getPreference(AppStatePreference.APP_FIRST_LAUNCH, true);
  }

  /**
   * Opens splash activity if its not the first launch. Also returns the status whether its a
   * first launch or not.
   */
  public static boolean launchSplashIfFirstLaunch(Context context) {
    return launchSplashIfFirstLaunch(context, true);
  }

  /**
   * Opens splash activity if it is not a first launch and the task has no other activities.
   * Returns - if the Splash activity was launched.
   */
  public static boolean launchSplashIfFirstLaunch(Context context, boolean isTaskRoot) {
    boolean isFirstLaunch = isFirstLaunch();
    if (isFirstLaunch && isTaskRoot) {
      CommonNavigator.launchSplash(context);
      return true;
    }
    return false;
  }

  public static boolean launchTVHome(Context activity, boolean skipAnimation, String sectionId,
                                     String entityKey) {
    return launchTVHome(activity, skipAnimation, sectionId, entityKey, null);
  }

  public static boolean launchTVHome(Context activity, boolean skipAnimation, String sectionId,
                                     String entityKey, PageReferrer pageReferrer) {
    Intent tvHomeIntent =
        getTVHomeIntent(activity, skipAnimation, sectionId, entityKey, pageReferrer);
    try {
      activity.startActivity(tvHomeIntent);
    } catch (ActivityNotFoundException e) {
      return false;
    }
    return true;
  }

  public static Intent getTVHomeIntent(Context activity, boolean skipAnimation, String
      sectionId, String entityKey, PageReferrer pageReferrer) {
    Intent tvHomeIntent = new Intent();
    tvHomeIntent.setPackage(CommonUtils.getApplication().getPackageName());
//    tvHomeIntent.setAction(Constants.TV_HOME_ACTION);
    tvHomeIntent.setAction(Constants.NEWS_HOME_ACTION);

    int flags = Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP;
    if (skipAnimation) {
      flags |= Intent.FLAG_ACTIVITY_NO_ANIMATION;
    }
    fillIntentParameters(tvHomeIntent, sectionId, entityKey, flags, pageReferrer);
    return tvHomeIntent;
  }

  public static boolean launchNotificationInbox(Context activity, boolean skipAnimation) {
    return launchNotificationInbox(activity, skipAnimation, null);
  }

  public static boolean launchNotificationInbox(Context activity, boolean skipAnimation,
                                                @Nullable PageReferrer pageReferrer) {
    Intent notificationInboxIntent = new Intent();
    notificationInboxIntent.setPackage(CommonUtils.getApplication().getPackageName());
    notificationInboxIntent.putExtra(Constants.BUNDLE_ACTIVITY_REFERRER, pageReferrer);
    notificationInboxIntent.setFlags(
        Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
    notificationInboxIntent.setAction(Constants.NOTIFICATION_INBOX);
    if (skipAnimation) {
      notificationInboxIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
    }
    try {
      activity.startActivity(notificationInboxIntent);
    } catch (ActivityNotFoundException e) {
      return false;
    }
    return true;
  }

  public static FollowSnackBarEntity readFollowSnackBarEntityFromPreferences() {
    String strValue = PreferenceManager.getPreference(AppStatePreference.FOLLOW_SNACKBAR_INFO,
        Constants.EMPTY_STRING);

    if (CommonUtils.isEmpty(strValue)) {
      return null;
    }

    return JsonUtils.fromJson(strValue, new TypeToken<FollowSnackBarEntity>() {
    }.getType());
  }

  public static boolean launchFollowingFeed(Context context, PageReferrer pageReferrer) {
    String deeplink =
        FollowSnackBarInfo.Companion.getInstance(readFollowSnackBarEntityFromPreferences())
            .getSnackBarDeeplink();

    if (CommonUtils.isEmpty(deeplink)) {
      Logger.d(TAG, "Launching follow feed failed, as follow feed deeplink url from registration" +
          "or handshake is empty");
      return false;
    }

    CommonNavigator.launchDeeplink(context, deeplink, pageReferrer);
    return true;
  }

  public static boolean launchLiveTVHome(Context activity, boolean skipAnimation, String sectionId,
                                         String entityKey) {
    return launchLiveTVSection(activity, skipAnimation, sectionId, entityKey, null);
  }

  public static boolean launchLiveTVSection(Context context, boolean skipAnimation, String
      sectionId, String appSectionEntityKey, PageReferrer pageReferrer) {
    Intent liveHomeIntent =
        getLiveTVSectionIntent(context, skipAnimation, sectionId, appSectionEntityKey,
            pageReferrer);
    try {
      context.startActivity(liveHomeIntent);
    } catch (ActivityNotFoundException e) {
      return false;
    }
    return true;
  }

  public static Intent getLiveTVSectionIntent(Context activity, boolean skipAnimation, String
      sectionId, String appSectionEntityKey, PageReferrer pageReferrer) {
    Intent liveHomeIntent = new Intent();
    liveHomeIntent.setPackage(CommonUtils.getApplication().getPackageName());
    liveHomeIntent.setAction(Constants.LIVE_HOME_ACTION);

    int flags = Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP;
    if (skipAnimation) {
      flags |= Intent.FLAG_ACTIVITY_NO_ANIMATION;
    }
    fillIntentParameters(liveHomeIntent, sectionId, appSectionEntityKey, flags, pageReferrer);
    return liveHomeIntent;
  }

  public static boolean launchWebSection(Context activity, boolean skipAnimation,
                                         String sectionId, String appSectionEntityKey,
                                         String contentUrl, PageReferrer pageReferrer, AppSection sectionType) {
    Intent webHomeIntent = getWebHomeIntent(activity, skipAnimation, sectionId,
        appSectionEntityKey, contentUrl, pageReferrer, sectionType);
    try {
      activity.startActivity(webHomeIntent);
    } catch (ActivityNotFoundException e) {
      return false;
    }
    return true;
  }

  private static Intent getWebHomeIntent(Context activity, boolean skipAnimation, String
          sectionId, String appSectionEntityKey, String contentUrl, PageReferrer pageReferrer,
                                         AppSection sectionType) {
    Intent webHomeIntent = new Intent();
    webHomeIntent.setPackage(CommonUtils.getApplication().getPackageName());
    webHomeIntent.setAction(Constants.WEB_HOME_ACTION);

    int flags = Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP;
    if (skipAnimation) {
      flags |= Intent.FLAG_ACTIVITY_NO_ANIMATION;
    }
    fillIntentParameters(webHomeIntent, sectionId, appSectionEntityKey, flags, pageReferrer);
    if (!CommonUtils.isEmpty(contentUrl)) {
      webHomeIntent.putExtra(Constants.WEB_CONTENT_URL, contentUrl);
    }
    if (sectionType != null) {
      webHomeIntent.putExtra(Constants.WEB_SECTION_TYPE, sectionType.getName());
    }
    return webHomeIntent;
  }

  public static boolean launchSavedArticles(Context activity, boolean skipAnimation) {
    Intent savedArticlesIntent = new Intent();
    savedArticlesIntent.setPackage(CommonUtils.getApplication().getPackageName());
    savedArticlesIntent.setAction(Constants.SAVED_ARTICLES);
    try {
      activity.startActivity(savedArticlesIntent);
    } catch (ActivityNotFoundException e) {
      return false;
    }
    return true;
  }

  public static boolean launchFollowHome(Context context, boolean skipAnimation, String
      sectionId, String appSectionEntityKey, PageReferrer pageReferrer) {
    Intent followHomeIntent = getFollowHomeIntent(context, skipAnimation, sectionId,
        appSectionEntityKey, pageReferrer);
    try {
      context.startActivity(followHomeIntent);
    } catch (ActivityNotFoundException e) {
      return false;
    }
    return true;
  }

  public static Intent getFollowHomeIntent(Context context, boolean skipAnimation, String
      sectionId, String appSectionEntityKey, PageReferrer pageReferrer) {
    Intent followIntent = new Intent(Constants.INTENT_ACTIONS_LAUNCH_FOLLOW_HOME);
    followIntent.setPackage(CommonUtils.getApplication().getPackageName());

    int flags = Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP;
    if (skipAnimation) {
      flags |= Intent.FLAG_ACTIVITY_NO_ANIMATION;
    }
    fillIntentParameters(followIntent, sectionId, appSectionEntityKey, flags, pageReferrer);
    return followIntent;
  }

  public static boolean launchDHTVHome(Context context, boolean skipAnimation, String
      sectionId, String appSectionEntityKey, PageReferrer pageReferrer) {
    Intent dhtvHomeIntent = getDHTVHomeIntent(context, skipAnimation, sectionId,
        appSectionEntityKey, pageReferrer);
    try {
      context.startActivity(dhtvHomeIntent);
    } catch (ActivityNotFoundException e) {
      return false;
    }
    return true;
  }

  public static Intent getDHTVHomeIntent(Context context, boolean skipAnimation, String
      sectionId, String appSectionEntityKey, PageReferrer pageReferrer) {
    Intent dhtvIntent = new Intent(Constants.INTENT_ACTIONS_LAUNCH_DHTV_HOME);
    dhtvIntent.setPackage(CommonUtils.getApplication().getPackageName());

    int flags = Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP;
    if (skipAnimation) {
      flags |= Intent.FLAG_ACTIVITY_NO_ANIMATION;
    }
    fillIntentParameters(dhtvIntent, sectionId, appSectionEntityKey, flags, pageReferrer);
    return dhtvIntent;
  }

  public static boolean launchLocoSection(Context context, boolean skipAnimation, String
      sectionId, String appSectionEntityKey, PageReferrer pageReferrer) {
    try {
      DHGameProvider.INSTANCE.launch(pageReferrer);
    } catch (ActivityNotFoundException e) {
      return false;
    }
    return true;
  }

  /*public static Intent getLocoHomeIntent(boolean skipAnimation, String
      sectionId, String appSectionEntityKey, PageReferrer pageReferrer) {
    Intent locoIntent = new Intent(Constants.INTENT_ACTIONS_LAUNCH_DHTV_HOME);
    locoIntent.setPackage(CommonUtils.getApplication().getPackageName());

    int flags = Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP;
    if (skipAnimation) {
      flags |= Intent.FLAG_ACTIVITY_NO_ANIMATION;
    }
    fillIntentParameters(locoIntent, sectionId, appSectionEntityKey, flags, pageReferrer);
    return locoIntent;
  }*/

  public static void launchDeeplink(Context context, String deeplinkUrl,
                                    PageReferrer pageReferrer) {
    launchDeeplink(context, deeplinkUrl, false, pageReferrer);
  }

  public static void launchDeeplink(Context context, String deeplinkUrl,
                                    boolean needDoubleBackExitViaDeeplink,
                                    PageReferrer pageReferrer) {
    launchDeeplink(context, deeplinkUrl, needDoubleBackExitViaDeeplink, pageReferrer, false, null);
  }

  public static void launchDeeplink(Context context, String deeplinkUrl,
                                    boolean needDoubleBackExitViaDeeplink,
                                    PageReferrer pageReferrer, boolean finishCurrentActivity,
                                    PageEntity pageEntity) {
    launchDeeplink(context, deeplinkUrl, needDoubleBackExitViaDeeplink, pageReferrer,
        finishCurrentActivity, false, pageEntity);
  }

  public static void launchDeeplink(Context context, String deeplinkUrl,
                                    boolean needDoubleBackExitViaDeeplink,
                                    PageReferrer pageReferrer, boolean finishCurrentActivity,
                                    boolean skipHomeRouting, PageEntity pageEntity) {
    launchDeeplink(context, deeplinkUrl, needDoubleBackExitViaDeeplink, pageReferrer,
        finishCurrentActivity, skipHomeRouting, pageEntity, null);
  }

  public static void launchDeeplink(Context context, String deeplinkUrl,
                                    boolean needDoubleBackExitViaDeeplink,
                                    PageReferrer pageReferrer, boolean finishCurrentActivity,
                                    boolean skipHomeRouting, PageEntity pageEntity,
                                    Map<String, Object> extraParams) {
    if (context == null || CommonUtils.isEmpty(deeplinkUrl)) {
      return;
    }

    context.startActivity(getDeepLinkLauncherIntent(deeplinkUrl,
        needDoubleBackExitViaDeeplink, pageReferrer, skipHomeRouting, pageEntity, extraParams));
    if (finishCurrentActivity && (context instanceof Activity)) {
      ((Activity) context).finish();
    }
  }

  public static void launchInternalDeeplink(Context context,
                                            String deeplinkUrl,
                                            PageReferrer referrer,
                                            boolean skipHomeRouting,
                                            @NonNull NavigatorCallback navigatorCallback) {
    launchInternalDeeplink(context, deeplinkUrl, referrer, skipHomeRouting, navigatorCallback, null,
        null);
  }

  public static void launchInternalDeeplink(Context context,
                                            String deeplinkUrl,
                                            PageReferrer referrer,
                                            boolean skipHomeRouting,
                                            @NonNull NavigatorCallback navigatorCallback,
                                            @Nullable Bundle extraBundleParams) {
    launchInternalDeeplink(context, deeplinkUrl, referrer, skipHomeRouting, navigatorCallback,
        null, extraBundleParams);
  }

  public static void launchInternalDeeplink(Context context,
                                            String deeplinkUrl,
                                            PageReferrer referrer,
                                            boolean skipHomeRouting,
                                            @NonNull NavigatorCallback navigatorCallback,
                                            @Nullable String section) {
    launchInternalDeeplink(context, deeplinkUrl, referrer, skipHomeRouting, navigatorCallback,
        section, null);
  }


  public static void launchInternalDeeplink(Context context,
                                            String deeplinkUrl,
                                            PageReferrer referrer,
                                            boolean skipHomeRouting,
                                            @NonNull NavigatorCallback navigatorCallback,
                                            @Nullable String section,
                                            @Nullable Bundle extraBundleParams) {
    if (DeeplinkUtils.isShortUrl(deeplinkUrl)) {
      launchDeeplink(context, deeplinkUrl, false, referrer, false, skipHomeRouting, null);
    } else {
      Intent intent = getInternalDeeplinkIntent(context, deeplinkUrl, referrer, skipHomeRouting,
          navigatorCallback, section, extraBundleParams);
      if (intent != null) {
        context.startActivity(intent);
      }
    }
  }

  public static Intent getInternalDeeplinkIntent(Context context,
                                                 String deeplinkUrl,
                                                 PageReferrer referrer,
                                                 boolean skipHomeRouting,
                                                 @NonNull NavigatorCallback navigatorCallback,
                                                 @Nullable String section,
                                                 @Nullable Bundle extraBundleParams) {
    BaseModel model = Deeplinker.parseDeeplinkModel(deeplinkUrl,
        DeeplinkUtils.createDeeplinkModel(deeplinkUrl, null));
    if (model == null) {
      return null;
    }
    Intent intent = DeeplinkNavigator.INSTANCE.navigate(model, skipHomeRouting, context, referrer,
        navigatorCallback);
    if (intent == null) {
      return null;
    }
    if (section != null) {
      intent.putExtra(NewsConstants.DH_SECTION, section);
    }
    intent.setPackage(AppConfig.getInstance().getPackageName());
    intent.putExtra(Constants.IS_INTERNAL_DEEPLINK, true);
    if (extraBundleParams != null) {
      intent.putExtras(extraBundleParams);
    }
    return intent;
  }

  public static boolean isNonSwipeableStoryDetailDeeplinkIntent(Intent intent) {
    if (intent == null || intent.getExtras() == null) {
      return false;
    }

    Bundle bundle = intent.getExtras();
    Map<String, Object> deeplinkExtraParams =
        (Map<String, Object>) bundle.getSerializable(Constants.BUNDLE_DEEPLINK_EXTRA_PARAMS);
    return !CommonUtils.isEmpty(deeplinkExtraParams) &&
        deeplinkExtraParams.get(Constants.BUNDLE_NEWS_DETAIL_NON_SWIPEABLE) ==
            Boolean.valueOf(true);
  }

  public static Intent getDeepLinkLauncherIntent(String deeplinkUrl,
                                                 boolean needDoubleBackExitViaDeeplink,
                                                 PageReferrer pageReferrer) {
    return getDeepLinkLauncherIntent(deeplinkUrl, needDoubleBackExitViaDeeplink, pageReferrer,
        false, null, null);
  }

  public static Intent getDeepLinkLauncherIntent(String deeplinkUrl,
                                                 boolean needDoubleBackExitViaDeeplink,
                                                 PageReferrer pageReferrer, boolean skipHomeRouting,
                                                 PageEntity pageEntity, Map<String, Object> extraParams) {
    Intent deeplinkIntent = new Intent();
    deeplinkIntent.setPackage(CommonUtils.getApplication().getPackageName());
    deeplinkIntent.setAction(Constants.DEEP_LINK_ACTION);
    deeplinkIntent.putExtra(Constants.DEEP_LINK_URL, deeplinkUrl);
    deeplinkIntent.putExtra(Constants.DEEP_LINK_DOUBLE_BACK_EXIT, needDoubleBackExitViaDeeplink);
    deeplinkIntent.putExtra(Constants.DEEP_LINK_SKIP_HOME_ROUTING, skipHomeRouting);
    deeplinkIntent.putExtra(Constants.BACK_URL_REFERRER, pageReferrer);
    if (extraParams != null) {
      deeplinkIntent.putExtra(Constants.BUNDLE_DEEPLINK_EXTRA_PARAMS, (Serializable) extraParams);
    }
    if (pageReferrer != null && pageReferrer.getReferrer() != null &&
        !NhGenericReferrer.NOTIFICATION.getReferrerName().
            equals(pageReferrer.getReferrer().getReferrerName())) {
      deeplinkIntent.putExtra(Constants.IS_INTERNAL_DEEPLINK, true);
    }
    if (pageEntity != null) {
      deeplinkIntent.putExtra(NewsConstants.NEWS_PAGE_ENTITY, pageEntity);
    }
    return deeplinkIntent;
  }

  /**
   * Helper function to get AppSectionHomeRouterActivity which will decide at run time which
   * section home to launch if desired home is not available. This will be used during click on
   * notification tray
   *
   * @param appSectionToRoute - {@link AppSection} desired
   * @param pageReferrer      - pageReferrer
   * @return - Intent to launch AppSectionHomeRouterActivity
   */
  public static Intent getSectionHomeRouterLaunchIntent(AppSection appSectionToRoute,
                                                        PageReferrer pageReferrer) {
    Intent sectionHomeRouterIntent = new Intent();
    sectionHomeRouterIntent.setPackage(CommonUtils.getApplication().getPackageName());
    sectionHomeRouterIntent.setAction(Constants.APP_SECTION_HOME_ROUTER_OPEN);
    sectionHomeRouterIntent.putExtra(Constants.APP_SECTION, appSectionToRoute.name());
    if (pageReferrer != null) {
      sectionHomeRouterIntent.putExtra(Constants.BUNDLE_ACTIVITY_REFERRER, pageReferrer);
    }
    sectionHomeRouterIntent.addCategory(Intent.CATEGORY_DEFAULT);
    return sectionHomeRouterIntent;
  }

  /**
   * Helper function to get LastSectionHomeIntent to intent of last used section. If the last used
   * section is not available in server bottom bar configuration, then it will return intent of
   * any first section available
   *
   * @param context      - calling context
   * @param pageReferrer - page Referrer
   * @return - intent of last section home or any available intent
   */
  public static Intent getLastSectionHomeLaunchIntent(Context context, PageReferrer pageReferrer) {
    if (context == null) {
      return null;
    }
    UserAppSection prevAppSection = getPreviousUserAppSection();

    Intent intent = null;
    switch (prevAppSection.getType()) {
      case TV:
        intent = getTVHomeIntent(context, false, prevAppSection.getId(), prevAppSection
            .getAppSectionEntityKey(), pageReferrer);
        break;
      case NEWS:
        intent = getNewsHomeIntent(context, false, prevAppSection.getId(), prevAppSection
            .getAppSectionEntityKey(), pageReferrer, false);
        break;
      case WEB:
      case SEARCH:
        intent = getWebHomeIntent(context, false, prevAppSection.getId(), prevAppSection
            .getAppSectionEntityKey(), prevAppSection.getContentUrl(), pageReferrer, prevAppSection.getType());
        break;
      case FOLLOW:
        intent = getFollowHomeIntent(context, false, prevAppSection.getId(),
            prevAppSection.getAppSectionEntityKey(), pageReferrer);
        break;
    }
    if (intent != null) {
      intent.putExtra(Constants.BUNDLE_ACTIVITY_REFERRER, pageReferrer);
    }
    return intent;
  }

  /**
   * Helper function to launch desired {@link AppSection} home intent. This function also takes
   * care of handling launching previous section home if available else any available home, if
   * desired home is not present
   *
   * @param context    - calling context
   * @param appSection - {@link AppSection} desired
   * @param intent     - intent to fire desired app section
   * @return - {@link AppSectionLaunchResult} result of launched section
   */
  public static AppSectionLaunchResult launchHomeIntent(Context context, AppSection appSection,
                                                        Intent intent) {
    if (intent == null || appSection == null) {
      return null;
    }
    AppSectionsProvider appSectionsProvider = AppSectionsProvider.INSTANCE;
    //If preferred section not available, it will launch any section available
    UserAppSection userAppSection = appSectionsProvider.getAnyUserAppSectionOfType(appSection);
    if (userAppSection == null) {
      return navigateToLastAppSection(context);
    }
    try {
      intent.putExtra(Constants.APP_SECTION_ID, userAppSection.getId());
      intent.putExtra(Constants.APP_SECTION_LAUNCH_ENTITY, userAppSection.getAppSectionEntityKey());
      context.startActivity(intent);
    } catch (ActivityNotFoundException e) {
      return null;
    }
    return new AppSectionLaunchResult(userAppSection, true);
  }

  /**
   * Helper function to launch default.
   * It will first attempt to launch any section of type {@link AppSection}.NEWS if available else
   * it will launch any available {@link AppSection}
   *
   * @param context             - calling context
   * @param preferredAppSection - preferred App Section to launch
   * @return - {@link AppSectionLaunchResult} if success else null
   */
  public static AppSectionLaunchResult launchSectionHome(Context context,
                                                         AppSection preferredAppSection,
                                                         PageReferrer pageReferrer,
                                                         boolean skipAnimation) {
    if (context == null || preferredAppSection == null) {
      return null;
    }
    AppSectionsProvider appSectionsProvider = AppSectionsProvider.INSTANCE;
    //If preferred section not available, it will launch any section available
    UserAppSection userAppSection = appSectionsProvider.getAnyUserAppSectionOfType
        (preferredAppSection);
    if (userAppSection == null) {
      return navigateToLastAppSection(context, pageReferrer, skipAnimation);
    }

    return launchDefinedSectionHome(
        new AppSectionLaunchParameters.Builder().setUserAppSection(userAppSection).setSkipAnimation(skipAnimation)
            .setPageReferrer(pageReferrer).build());
  }

  public static AppSectionLaunchResult navigateToLastAppSection(Context activity) {
    return navigateToLastAppSection(activity, null, false);
  }

  public static AppSectionLaunchResult navigateToLastAppSection(Context activity,
                                                                PageReferrer pageReferrer,
                                                                boolean skipAnimation) {
    UserAppSection prevAppSection = getPreviousUserAppSection();
    return launchDefinedSectionHome(new AppSectionLaunchParameters.Builder().setUserAppSection
        (prevAppSection).setSkipAnimation(skipAnimation).setPageReferrer(pageReferrer).build());
  }

  /**
   * Default rule to launch app.
   * <p>
   * Prev Section                 Next Section
   * ------------                 ------------
   * NEWS                         NEWS
   * TV                           TV(if available in bottom bar) else NEWS
   * WEB                          WEB
   * LIVE TV                      LIVE TV (if available in bottom bar) else NEWS
   * DAILY_TV                     DAILY_TV
   * FOLLOW                       FOLLOW
   * LOCO                         NEWS
   *
   * @param activity     - calling context
   * @param pageReferrer - pageReferrer
   * @return - returns {@link AppSectionLaunchResult}
   */
  public static AppSectionLaunchResult defaultRuleLaunchSection(Context activity,
                                                                PageReferrer pageReferrer) {
    boolean success;
    AppSectionsProvider appSectionsProvider = AppSectionsProvider.INSTANCE;
    UserAppSection prevAppSection = getPreviousUserAppSection();

    UserAppSection userAppSectionLaunched = prevAppSection;
    switch (prevAppSection.getType()) {
      case NOTIFICATIONINBOX:
        success = CommonNavigator.launchNotificationInbox(activity, false, pageReferrer);
        break;
      case WEB:
      case SEARCH:
        success = CommonNavigator.launchWebSection(activity, false, prevAppSection.getId(),
            Constants.EMPTY_STRING, prevAppSection.getContentUrl(), pageReferrer, prevAppSection.getType());
        break;
      case TV:
        success = CommonNavigator.launchTVHome(activity, false, prevAppSection.getId(),
            Constants.EMPTY_STRING, pageReferrer);
        break;
      case FOLLOW:
        success = CommonNavigator.launchFollowHome(activity, false, prevAppSection.getId(),
            prevAppSection.getAppSectionEntityKey(), pageReferrer);
        break;
      case NEWS:
      default:
        if (prevAppSection.getType() != AppSection.NEWS) {
          prevAppSection = appSectionsProvider.getAnyUserAppSectionOfType(AppSection.NEWS);
          if (prevAppSection == null) {
            prevAppSection = new UserAppSection.Builder().section(AppSection.NEWS).build();
          }
        }
        success = CommonNavigator.launchNewsHome(activity, false, prevAppSection.getId(),
            //To make it to land on home tab if available else first non source tab in news home
            Constants.EMPTY_STRING, pageReferrer);
        userAppSectionLaunched = prevAppSection;
        break;
    }
    return new AppSectionLaunchResult(userAppSectionLaunched, success);
  }

  /**
   * Helper function to launch defined AppSection mentioned in
   * {@link AppSectionLaunchParameters}, there is no fallback logic, if {@link AppSection} and
   * {@link AppSection}.getId() is not available
   *
   * @param parameters - launch {@link AppSectionLaunchParameters}
   * @return - {@link AppSectionLaunchResult} if success, else null
   */
  public static AppSectionLaunchResult launchDefinedSectionHome(
      AppSectionLaunchParameters parameters) {
    if (parameters == null || parameters.getUserAppSection() == null || parameters
        .getUserAppSection().getType() == null) {
      return null;
    }

    UserAppSection appSectionToLaunch = parameters.getUserAppSection();
    boolean skipAnimation = parameters.isSkipAnimation();
    boolean success = false;
    switch (appSectionToLaunch.getType()) {
      case WEB:
      case SEARCH:
        success = launchWebSection(CommonUtils.getApplication(), skipAnimation, appSectionToLaunch
            .getId(), appSectionToLaunch.getAppSectionEntityKey(), appSectionToLaunch
            .getContentUrl(), parameters.getPageReferrer(), appSectionToLaunch.getType());
        break;
      case NEWS:
        success = CommonNavigator.launchNewsHome(CommonUtils.getApplication(), skipAnimation,
            appSectionToLaunch.getId(), appSectionToLaunch.getAppSectionEntityKey(),
            parameters.getPageReferrer());
        break;
      case TV:
        success = CommonNavigator.launchTVHome(CommonUtils.getApplication(), skipAnimation,
            appSectionToLaunch.getId(), appSectionToLaunch.getAppSectionEntityKey(),
            parameters.getPageReferrer());
        break;
      case FOLLOW:
        success = CommonNavigator.launchFollowHome(CommonUtils.getApplication(), skipAnimation,
            appSectionToLaunch.getId(), appSectionToLaunch.getAppSectionEntityKey(), parameters
                .getPageReferrer());
        break;
      case NOTIFICATIONINBOX:
        success = CommonNavigator.launchNotificationInbox(CommonUtils.getApplication(), false,
            parameters.getPageReferrer());
        break;
    }

    if (success && appSectionToLaunch.getType() != AppSection.NOTIFICATIONINBOX) {
      AppUserPreferenceUtils.setAppSectionSelected(appSectionToLaunch);
    }
    return new AppSectionLaunchResult(appSectionToLaunch, success);
  }

  private static boolean isPreviousAppSectionAvailable(UserAppSection prevAppSection) {
    AppSectionsProvider appSectionsProvider = AppSectionsProvider.INSTANCE;

    return prevAppSection != null && appSectionsProvider.isSectionAvailable(prevAppSection.getId());
  }

  private static boolean isPreviousAppSectionTypeAvailable(UserAppSection prevAppSection) {
    AppSectionsProvider appSectionsProvider = AppSectionsProvider.INSTANCE;

    return prevAppSection != null &&
        appSectionsProvider.isSectionAvailable(prevAppSection.getType());
  }

  public static UserAppSection getPreviousUserAppSection() {
    AppSectionsProvider appSectionsProvider = AppSectionsProvider.INSTANCE;
    UserAppSection prevAppSection = AppUserPreferenceUtils.getPreviousAppSection();

    if (!isPreviousAppSectionAvailable(prevAppSection)) {
      prevAppSection = isPreviousAppSectionTypeAvailable(prevAppSection) ?
          appSectionsProvider.getAnyUserAppSectionOfType(prevAppSection.getType()) :
          appSectionsProvider.getAnySection();
    }
    return prevAppSection;
  }

  public static Intent getExamPrepAppIntent() {
    try {
      return CommonUtils.getApplication().getPackageManager().
          getLaunchIntentForPackage(Constants.EP_APP_PACKAGE_NAME);
    } catch (Exception e) {
      Logger.caughtException(e);
    }
    return null;
  }

  public static void launchDeferredDeeplinkActivity(final Context context, final String deeplinkUrl,
                                                    final PageReferrer pageReferrer,
                                                    final String deeplinkKey) {
    if (context == null || CommonUtils.isEmpty(deeplinkUrl)) {
      return;
    }

    Intent deeplinkIntent = getDeepLinkLauncherIntent(deeplinkUrl, false, pageReferrer);
    deeplinkIntent.setAction(Constants.DEFERRED_DEEP_LINK_ACTION);
    if (!CommonUtils.isEmpty(deeplinkKey)) {
      deeplinkIntent.putExtra(deeplinkKey, deeplinkUrl);
    }
    context.startActivity(deeplinkIntent);
  }


  /**
   * Launches SearchActivity with pre-search screen.
   */
  public static void launchSearch(Context context,
                                  LaunchSearch launchSearch,
                                  SearchRequestType searchRequestType) {
    Intent intent = new Intent(Constants.INTENT_ACTION_LAUNCH_SEARCH);
    intent.setPackage(AppConfig.getInstance().getPackageName());
    intent.putExtra(Constants.BUNDLE_SEARCH_CONTEXT, launchSearch.getSearchContext());
    intent.putExtra(Constants.BUNDLE_SEARCH_CONTEXT_PAYLOAD,
        launchSearch.getSearchPayloadContext());
    intent.putExtra(Constants.BUNDLE_SEARCH_HINT, launchSearch.getSearchHint());
    intent.putExtra(Constants.BUNDLE_SEARCH_REQUEST_TYPE, searchRequestType);
    if (launchSearch.getReferrer() != null && launchSearch.getReferrer() instanceof PageReferrer) {
      intent.putExtra(Constants.BUNDLE_ACTIVITY_REFERRER,
          (PageReferrer) launchSearch.getReferrer());
    }
    context.startActivity(intent);
  }

  /**
   * Launches SearchActivty and displays result of the query
   *
   * @param query search query
   */
  public static void searchForQuery(Context context, String query, PageReferrer referrer,
                                    String searchContext,
                                    SearchPayloadContext searchPayloadContext) {
    Intent intent = new Intent(Constants.INTENT_ACTION_LAUNCH_SEARCH);
    String packageName =
        AppConfig.getInstance() != null ? AppConfig.getInstance().getPackageName() : null;
    if (packageName != null) {
      intent.setPackage(packageName);
    }
    intent.putExtra(Constants.BUNDLE_SEARCH_QUERY, query);
    if (referrer != null) {
      intent.putExtra(Constants.BUNDLE_ACTIVITY_REFERRER, referrer);
    }
    intent.putExtra(Constants.BUNDLE_SEARCH_CONTEXT, searchContext);
    intent.putExtra(Constants.BUNDLE_SEARCH_CONTEXT_PAYLOAD, searchPayloadContext);

    context.startActivity(intent);
  }

  /**
   * Read the default utm source of the APK and decide which App section to launch. For Coolfie
   * acquisitions, we check if the utm source begins with "dhcoolfie", launch coolfie home. Else,
   * launch News home
   *
   * @return Appsection to launch
   */
  public static AppSection getLandingAppSection() {

    AppSection targetSection = AppSection.fromName(
        CampaignAcquisitionHelper.fetchAcquisitionTypeFromCampaign(
            CampaignAcquisitionHelper.readCampaignAcquisitionParams()));
    return targetSection.isLandingSupported() ? targetSection : AppSection.NEWS;
  }

  public static void launchMyProfileActivity(@NonNull final Context context,
                                             final boolean isLoggedIn,
                                             final PageReferrer pageReferrer,
                                             final NavigatorCallback navigatorCallback) {
    Intent intent = null;
    boolean redirect = false;
    if (!isLoggedIn) {
      /**
       * If user is not logged in to social accounts, redirect him to sign in screen before
       * skipping to profile for X times, configured by handshake
       */
      int signInSkipCount =
          PreferenceManager.getPreference(AppStatePreference.SIGNIN_SKIP_COUNTER, 0);
      int maxSkipCount =
          PreferenceManager.getPreference(AppStatePreference.SIGNIN_BEFORE_PROFILE_LAUNCH_COUNT,
              -1);
      if (signInSkipCount < maxSkipCount) {
        //PendingIntent to Launch Profile after login success
        PendingIntent profilePendingIntent = PendingIntent.getActivity(context,
            Constants.REQ_CODE_PROFILE,
            getProfileHomeIntent(null, pageReferrer, null),
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        //On sign in success, On Skip/back-> launch profile
        intent = getSignInIntentForProfileRedirection(profilePendingIntent,
            profilePendingIntent,
            pageReferrer);

        redirect = true;
      }
    }
    if (!redirect) {
      intent = getProfileHomeIntent(null, pageReferrer, null);
    }
    if (intent != null) {
      context.startActivity(intent);
    }
  }

  public static void launchProfileActivity(@NonNull final Context context,
                                           @Nullable final UserBaseProfile basicInfo,
                                           PageReferrer referrer) {
    Intent intent = getProfileHomeIntent(basicInfo, referrer, null);
    if (intent == null) {
      return;
    }
    context.startActivity(intent);
  }

  public static Intent getProfileHomeIntent(@Nullable final UserBaseProfile basicInfo,
                                            PageReferrer referrer,
                                            @Nullable final ProfileTabType preferredProfileTabType) {
    return getProfileHomeIntent(basicInfo,referrer,preferredProfileTabType,null);
  }

  public static Intent getProfileHomeIntent(@Nullable final UserBaseProfile basicInfo,
                                            PageReferrer referrer,
                                            @Nullable final ProfileTabType preferredProfileTabType,String defaultTabId) {
    Intent intent = new Intent(DHConstants.PROFILE_OPEN_ACTION);
    intent.setPackage(AppConfig.getInstance().getPackageName());
    intent.putExtra(ProfilePojosKt.PROFILE_USER_DATA_KEY, basicInfo);
    intent.putExtra(ProfilePojosKt.BUNDLE_PROFILE_PREFERRED_TAB_TYPE, preferredProfileTabType);
    intent.putExtra(Constants.BUNDLE_ACTIVITY_REFERRER, referrer);
    if(!CommonUtils.isEmpty(defaultTabId)) {
      intent.putExtra(ProfilePojosKt.BUNDLE_DEFAULT_PREFERRED_TAB_ID,defaultTabId);
    }
    return intent;
  }

  public static Intent getProfileMyPostsIntent(PageReferrer referrer) {
    return getProfileHomeIntent(null, referrer, ProfileTabType.FPV_POSTS);
  }


  private static Intent getSignInIntentForProfileRedirection(PendingIntent successPendingIntent,
                                                             PendingIntent skipPendingIntent,
                                                             PageReferrer pageReferrer) {
    Intent intent = SSONavigator.getIntentForSignIn();
    intent.putExtra(Constants.BUNDLE_ACTIVITY_REFERRER, pageReferrer);
    intent.putExtra(Constants.BUNDLE_SIGN_ON_UI_MODE,
        SignInUIModes.SIGN_IN_WITH_SKIP_BUTTON.toString());
    intent.putExtra(Constants.BUNDLE_SIGN_IN_CUSTOM_HEADER,
        CommonUtils.getString(com.newshunt.common.util.R.string.sign_up_header_text_default));
    intent.putExtra(Constants.BUNDLE_COUNT_SKIP_CLICK, true);
    intent.putExtra(Constants.BUNDLE_SIGNIN_SUCCESS_PENDING_INTENT, successPendingIntent);
    intent.putExtra(Constants.BUNDLE_SIGNIN_SKIP_PENDING_INTENT, skipPendingIntent);
    intent.putExtra(Constants.BUNDLE_LINK_ACCOUNTS_POST_LOGIN, true);
    return intent;
  }

  public static void launchSettingsActivity(final Context context) {
    final Intent intent = new Intent(DHConstants.SETTINGS_OPEN_ACTION);
    intent.setPackage(AppConfig.getInstance().getPackageName());
    context.startActivity(intent);
  }

  public static void launchGroupDetailActivity(final Context context,
                                               @Nullable final GroupBaseInfo groupBaseInfo,
                                               PageReferrer referrer,
                                               @Nullable PageReferrer providedReferrer) {
    final Intent intent = new Intent(DHConstants.GROUP_DETAIL_OPEN_ACTION);
    intent.setPackage(AppConfig.getInstance().getPackageName());
    intent.putExtra(GroupPojosKt.GROUP_INFO_KEY, groupBaseInfo);
    intent.putExtra(Constants.BUNDLE_ACTIVITY_REFERRER, referrer);
    intent.putExtra(Constants.BUNDLE_ACTIVITY_REFERRER_FLOW, providedReferrer);
    context.startActivity(intent);
  }

  public static Intent getGroupDetailIntent(String groupId, String handle,
                                            PageReferrer pageReferrer) {
    GroupBaseInfo groupBaseInfo = new GroupBaseInfo();
    groupBaseInfo.setId(groupId);
    groupBaseInfo.setHandle(handle);
    final Intent intent = new Intent(DHConstants.GROUP_DETAIL_OPEN_ACTION);
    intent.setPackage(AppConfig.getInstance().getPackageName());
    intent.putExtra(GroupPojosKt.GROUP_INFO_KEY, groupBaseInfo);
    intent.putExtra(Constants.BUNDLE_ACTIVITY_REFERRER, pageReferrer);
    return intent;
  }

  public static Intent getSearchIntent(SearchNavModel searchNavModel, PageReferrer pageReferrer) {
    Intent intent = new Intent(Constants.INTENT_ACTION_LAUNCH_SEARCH);
    String packageName = null;
    intent.setPackage(AppConfig.getInstance().getPackageName());
    intent.putExtra(Constants.BUNDLE_SEARCH_QUERY, searchNavModel.getQuery());
    intent.putExtra(Constants.BUNDLE_SEARCH_MODEL, searchNavModel);
    intent.putExtra(Constants.BUNDLE_SEARCH_CONTEXT, searchNavModel.getContext());
    intent.putExtra(Constants.BUNDLE_SEARCH_CONTEXT_PAYLOAD, searchNavModel.getPayload());
    intent.putExtra(Constants.BUNDLE_ACTIVITY_REFERRER, pageReferrer);
    return intent;
  }


  public static void launchEditGroupActivity(final Context context,
                                             @Nullable final GroupBaseInfo groupBaseInfo,
                                             final boolean isLoggedIn,
                                             @Nullable PageReferrer referrer) {
    //TODO : pass appropriate referrer
    Intent intent = null;
    boolean redirect = false;
    if (!isLoggedIn) {
      /**
       * If user is not logged in to social accounts, redirect him to sign in screen before
       * going to Create/Edit Group activity
       */
      PendingIntent pendingIntent = PendingIntent.getActivity(context,
          Constants.REQ_CODE_GROUP,
          getGroupEditorIntent(null, groupBaseInfo),
          PendingIntent.FLAG_UPDATE_CURRENT);
      intent = getSignInIntentForCreateGroupRedirection(pendingIntent, referrer);
      redirect = true;
    }
    if (!redirect) {
      intent = getGroupEditorIntent(referrer, groupBaseInfo);
    }
    if (intent != null) {
      context.startActivity(intent);
    }
  }

  public static Intent getGroupEditorIntent(PageReferrer referrer,
                                            @Nullable final GroupBaseInfo groupBaseInfo) {
    Intent intent = new Intent(DHConstants.GROUP_CREATE_EDIT_ACTION);
    intent.setPackage(AppConfig.getInstance().getPackageName());
    intent.putExtra(GroupPojosKt.GROUP_INFO_KEY, groupBaseInfo);
    intent.putExtra(Constants.BUNDLE_ACTIVITY_REFERRER, referrer);
    return intent;
  }

  private static Intent getSignInIntentForCreateGroupRedirection(PendingIntent pendingIntent,
                                                                 PageReferrer pageReferrer) {
    Intent intent = SSONavigator.getIntentForSignIn();
    intent.putExtra(Constants.BUNDLE_ACTIVITY_REFERRER, pageReferrer);
    intent.putExtra(Constants.BUNDLE_SIGNIN_SUCCESS_PENDING_INTENT, pendingIntent);
    return intent;
  }

  public static void launchGroupInvitationActivity(final Context context,
                                                   @NonNull GroupInfo groupInfo,
                                                   @Nullable PageReferrer referrer) {
    final Intent intent = getGroupInvitationIntent(groupInfo, referrer);
    context.startActivity(intent);
  }

  public static Intent getGroupInvitationIntent(@NonNull GroupInfo groupInfo,
                                                @Nullable PageReferrer referrer) {
    final Intent intent = new Intent(DHConstants.GROUP_INVITATION_ACTION);
    intent.setPackage(AppConfig.getInstance().getPackageName());
    intent.putExtra(GroupPojosKt.GROUP_INFO_KEY, groupInfo);
    intent.putExtra(Constants.BUNDLE_ACTIVITY_REFERRER, referrer);
    return intent;
  }

  public static Intent getGroupInvitationIntent(@NonNull String groupId,
                                                @Nullable PageReferrer referrer) {
    final Intent intent = new Intent(DHConstants.GROUP_INVITATION_ACTION);
    intent.setPackage(AppConfig.getInstance().getPackageName());
    intent.putExtra(GroupPojosKt.GROUP_ID_KEY, groupId);
    intent.putExtra(Constants.BUNDLE_ACTIVITY_REFERRER, referrer);
    return intent;
  }

  public static void launchApprovalsActivity(final Context context,
                                             @Nullable ReviewItem preferredTabType,
                                             PageReferrer referrer) {
    final Intent intent = new Intent(DHConstants.APPROVALS_ACTION);
    intent.setPackage(AppConfig.getInstance().getPackageName());
    intent.putExtra(GroupPojosKt.BUNDLE_APPROVAL_PREFERRED_TAB_TYPE, preferredTabType);
    intent.putExtra(Constants.BUNDLE_ACTIVITY_REFERRER, referrer);
    context.startActivity(intent);
  }

  public static Intent getGroupApprovalIntent(String landingTab, PageReferrer referrer) {
    final Intent intent = new Intent(DHConstants.APPROVALS_ACTION);
    intent.setPackage(AppConfig.getInstance().getPackageName());
    intent.putExtra(GroupPojosKt.BUNDLE_APPROVAL_PREFERRED_TAB_TYPE, ReviewItem.from(landingTab));
    intent.putExtra(Constants.BUNDLE_ACTIVITY_REFERRER, referrer);
    return intent;
  }

  public static void launchGroupMemberActivity(final Context context,
                                               final @NonNull GroupInfo groupInfo,
                                               final PageReferrer referrer) {
    final Intent memberListIntent = new Intent(DHConstants.GROUP_MEMBERS_ACTION);
    memberListIntent.setPackage(AppConfig.getInstance().getPackageName());
    memberListIntent.putExtra(GroupPojosKt.GROUP_INFO_KEY, groupInfo);
    memberListIntent.putExtra(Constants.BUNDLE_ACTIVITY_REFERRER, referrer);
    context.startActivity(memberListIntent);
  }

  public static void launchMyProfileAfterLoginAndImportContacts(Context context,
                                                                PageReferrer pageReferrer,
                                                                NavigatorCallback navigatorCallback) {
    //PendingIntent to Launch Profile after import contacts and follow or on pressing back/skip
    PendingIntent profilePendingIntent = PendingIntent.getActivity(context,
        Constants.REQ_CODE_PROFILE,
        getProfileHomeIntent(null, pageReferrer, null),
        PendingIntent.FLAG_UPDATE_CURRENT);
    Bundle extraBundle = new Bundle();
    extraBundle.putParcelable(Constants.BUNDLE_IMPORT_CONTACTS_PENDING_INTENT,
        profilePendingIntent);

    Intent internalDeeplinkIntent = getInternalDeeplinkIntent(context,
        Constants.CONTACTS_RECOMENDATION_DEEPLINK,
        pageReferrer,
        true,
        navigatorCallback,
        NhAnalyticsEventSection.APP.getEventSection(),
        extraBundle);

    //Pending intent to launch import contacts after signin
    PendingIntent deeplinkPendingIntent = PendingIntent.getActivity(context,
        Constants.REQ_CODE_IMPORT_CONTACTS,
        internalDeeplinkIntent,
        PendingIntent.FLAG_UPDATE_CURRENT);

    Intent intent = SSONavigator.getIntentForSignIn();
    intent.putExtra(Constants.BUNDLE_ACTIVITY_REFERRER, pageReferrer);
    intent.putExtra(Constants.BUNDLE_SIGN_IN_CUSTOM_HEADER,
        CommonUtils.getString(com.newshunt.common.util.R.string.sign_up_header_text_default));
    intent.putExtra(Constants.BUNDLE_SIGNIN_SUCCESS_PENDING_INTENT, deeplinkPendingIntent);
    intent.putExtra(Constants.BUNDLE_REFERRER_VIEW_IS_FVP, true);
    intent.putExtra(Constants.BUNDLE_LINK_ACCOUNTS_POST_LOGIN, true);
    context.startActivity(intent);
  }

  public static void loginAndImportContacts(Context context,
                                            PageReferrer pageReferrer,
                                            NavigatorCallback navigatorCallback) {
    Intent intent = SSONavigator.getIntentForSignIn();
    intent.putExtra(Constants.BUNDLE_ACTIVITY_REFERRER, pageReferrer);
    intent.putExtra(Constants.BUNDLE_SIGN_IN_CUSTOM_HEADER,
        CommonUtils.getString(com.newshunt.common.util.R.string.sign_up_header_text_default));
    intent.putExtra(Constants.BUNDLE_REFERRER_VIEW_IS_FVP, true);
    intent.putExtra(Constants.BUNDLE_LINK_ACCOUNTS_POST_LOGIN, true);
    context.startActivity(intent);
  }

  public static Intent getAccountLinkingActivityIntent(
      @Nullable List<AccountLinkType> linkedAccounts,
      @Nullable LoginType linkAccountType,
      @Nullable Boolean enableOneTouchLogin,
      @Nullable PendingIntent nextStepPendingIntent,
      @Nullable PageReferrer referrer) {
    Intent intent = new Intent(DHConstants.INTENT_ACTION_ACCOUNT_LINK);
    intent.putExtra(Constants.BUNDLE_LINKED_ACCOUNT_TYPES, (Serializable) linkedAccounts);
    if (linkAccountType != null) {
      intent.putExtra(Constants.BUNDLE_LINK_SPECIFIC_ACCOUNT, linkAccountType.getValue());
    }
    intent.putExtra(Constants.BUNDLE_ENABLE_ONE_TOUCH_LOGIN, enableOneTouchLogin);
    intent.putExtra(Constants.BUNDLE_SIGNIN_SUCCESS_PENDING_INTENT, nextStepPendingIntent);
    intent.putExtra(Constants.REFERRER, referrer);
    return intent;
  }

  public static void launchAccountLinkActivity(Context context,
                                               @Nullable List<AccountLinkType> linkedAccounts,
                                               @Nullable LoginType linkAccountType,
                                               @Nullable Boolean enableOneTouchLogin,
                                               @Nullable PendingIntent nextStepPendingIntent,
                                               @Nullable PageReferrer referrer) {
    context.startActivity(getAccountLinkingActivityIntent(linkedAccounts,
        linkAccountType,
        enableOneTouchLogin,
        nextStepPendingIntent,
        referrer));
  }

  public static void openLocationSelection(Context context, boolean hideNextButton, boolean isFromLocalZone) {
    Intent selectLocationActivityIntent = new Intent(DHConstants.OPEN_LOCATION_SELECTION);
    selectLocationActivityIntent.setPackage(AppConfig.getInstance().getPackageName());
    selectLocationActivityIntent.addCategory(Intent.CATEGORY_DEFAULT);
    selectLocationActivityIntent.putExtra(Constants.BUNDLE_LAUNCHED_FROM_SETTINGS, hideNextButton);
    selectLocationActivityIntent.putExtra(Constants.BUNDLE_IS_LOCAL_ZONE, isFromLocalZone);
    context.startActivity(selectLocationActivityIntent);
  }

  public static void openNotificationActivity(Context context) {
    Intent notificationActivityIntent = new Intent(DHConstants.OPEN_NOTIFICATION_ACTIVITY);
    notificationActivityIntent.setPackage(AppConfig.getInstance().getPackageName());
    context.startActivity(notificationActivityIntent);
  }

  public static Intent openDeviceNotificationSettingsScreen(Context context){
    Intent intent = new Intent();
    intent.setAction(Constants.NOTIFICATION_SETTINGS_DEEPLINK_ACTION);
    intent.putExtra(Constants.APP_PACKAGE, context.getPackageName());
    intent.putExtra(Constants.APP_UID, context.getApplicationInfo().uid);
    intent.putExtra(Constants.EXTRA_APP_PACKAGE,context.getPackageName());
    intent.putExtra(Constants.DEVICE_SETTINGS,true);
    return intent;
  }

}

