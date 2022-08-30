/*
 * Copyright (c) 2016 Newshunt. All rights reserved.
 */
package com.newshunt.deeplink.navigator;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.newshunt.common.helper.appconfig.AppConfig;
import com.newshunt.dataentity.analytics.referrer.PageReferrer;
import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.common.helper.common.Constants;
import com.newshunt.dataentity.common.model.entity.AppSection;
import com.newshunt.dataentity.common.model.entity.UserAppSection;
import com.newshunt.dataentity.common.pages.PageEntity;
import com.newshunt.dhutil.helper.appsection.AppSectionsProvider;
import com.newshunt.deeplink.navigator.EntityPreviewUtils;
import com.newshunt.deeplink.navigator.NewsHomeRouterInput;
import com.newshunt.dataentity.news.model.entity.PageType;
import com.newshunt.news.util.NewsConstants;
import com.newshunt.dataentity.notification.NavigationType;
import com.newshunt.dataentity.notification.NewsNavModel;

import java.util.List;

/**
 * A Helper class
 * 1. to build router input info built from input intent.
 * 2. to get routing target intent to entity preview or news home
 *
 * @author santhosh.kc
 */
public class NewsHomeRouterHelper {

  /**
   * An Helper to getRouterInput from input bundle
   *
   * @param bundle - input bundle
   * @return - NewsHomeRouterInput
   */
  public static NewsHomeRouterInput getRouterInputFrom(Bundle bundle) {
    if (bundle == null) {
      return null;
    }

    String entityKey = (String) bundle.get(NewsConstants.TOPIC_KEY);
    String subEntityKey = (String) bundle.get(NewsConstants.SUB_TOPIC_KEY);
    if (CommonUtils.isEmpty(entityKey)) {
      entityKey = (String) bundle.get(NewsConstants.LOCATION_KEY);
      subEntityKey = (String) bundle.get(NewsConstants.SUB_LOCATION_KEY);
    }

    if (CommonUtils.isEmpty(entityKey)) {
      return null;
    }

    PageReferrer pageReferrer = (PageReferrer) bundle.get(NewsConstants.BUNDLE_ACTIVITY_REFERRER);
    String sType = bundle.getString(Constants.BUNDLE_NAVIGATION_TYPE);
    int uniqueId = bundle.getInt(Constants.BUNDLE_NOTIFICATION_UNIQUE_ID);
    String language = bundle.getString(NewsConstants.LANGUAGE_FROM_DEEPLINK_URL);
    String languageCode = bundle.getString(NewsConstants.LANGUAGE_CODE_FROM_DEEPLINK_URL);
    String edition = bundle.getString(NewsConstants.EDITION_FROM_DEEPLINK_URL);
    String notificationBackUrl = bundle.getString(Constants.V4BACKURL);
    NavigationType navigationType = NavigationType.fromString(sType);
    PageType pageType = EntityPreviewUtils.getPageTypeFromNavigationType(navigationType,
        subEntityKey);
    if (pageType == null) {
      return null;
    }
    return new NewsHomeRouterInput.Builder().setEntityKey(entityKey).setSubEntityKey
        (subEntityKey).setPageType(pageType).setNavigationType(navigationType).setPageReferrer
        (pageReferrer).setLanguage(language).setLangCode(languageCode).setEdition(edition)
        .setUniqueId(uniqueId).setNotificationBackUrl(notificationBackUrl).build();
  }

  /**
   * An helper function to getRouterInput from input NewsNavModel
   *
   * @param newsNavModel - input NewNavModel
   * @param pageReferrer - pageReferrer
   * @return - NewsHomeRouterInput
   */
  public static NewsHomeRouterInput getRouterInputFrom(NewsNavModel newsNavModel,
                                                       PageReferrer pageReferrer) {
    return EntityPreviewUtils.getRouterInputFrom(newsNavModel,pageReferrer);
  }

  /**
   * Helper method to get the routing intent to the entity preview or news home depending on if
   * the user has already added that entity to the news home tab
   *
   * @param context             - context
   * @param newsHomeRouterInput - router input having the entity keys and pagetype info
   * @param pageList  - list of already added entities by user
   * @return - routing intent to the entity preview or news home
   */
  public static Intent getRoutingIntent(Context context, NewsHomeRouterInput newsHomeRouterInput,
                                        List<PageEntity> pageList) {

    if (newsHomeRouterInput == null) {
      return null;
    }

    switch (newsHomeRouterInput.getPageType()) {
      case TOPIC:
      case SUB_TOPIC:
        return getTopicTargetIntent(context, newsHomeRouterInput, pageList);
      case LOCATION:
      case SUB_LOCATION:
        return getLocationTargetIntent(context, newsHomeRouterInput, pageList);
      case VIRAL:
        return getViralTopicTargetIntent(context, newsHomeRouterInput, pageList);
      default:
        break;
    }

    return null;
  }

  private static Intent getTopicTargetIntent(Context context, NewsHomeRouterInput
      newsHomeRouterInput, List<PageEntity> alreadyAddedTopics) {
    String targetTopicKey = newsHomeRouterInput.getEntityKey();
    String targetSubTopicKey = newsHomeRouterInput.getSubEntityKey();

    boolean isNewsSectionAvailable = isNewsSectionAvailable();

    //child wins, if deeplink to subtopic, then check the subtopic is already to ideate tab
    String targetKey = targetSubTopicKey != null ? targetSubTopicKey : targetTopicKey;

    PageEntity alreadyAddedTopic = getAlreadyAddedMatchingEntity(alreadyAddedTopics,
        targetKey);
    boolean launchNewsHome = alreadyAddedTopic != null && isNewsSectionAvailable;
    Intent intent = new Intent(launchNewsHome ? Constants.NEWS_HOME_ACTION : Constants.ENTITY_OPEN_ACTION);

    if (launchNewsHome) {
      setNewsHomeIntentExtras(intent, alreadyAddedTopic);
    } else {
      intent.putExtra(NewsConstants.SHOW_SELECT_TOPIC_BUTTON, isNewsSectionAvailable);
      intent.putExtra(NewsConstants.ENTITY_KEY, targetTopicKey);
      intent.putExtra(NewsConstants.ENTITY_TYPE, newsHomeRouterInput.getEntityType());
      if (!CommonUtils.isEmpty(targetSubTopicKey)) {
        intent.putExtra(NewsConstants.SUB_ENTITY_KEY, targetSubTopicKey);
      }
    }
    EntityPreviewUtils.buildIntentExtras(intent, newsHomeRouterInput);
    intent.setPackage(AppConfig.getInstance().getPackageName());
    return intent;
  }

  private static Intent getViralTopicTargetIntent(Context context, NewsHomeRouterInput
      newsHomeRouterInput, List<PageEntity> alreadyAddedTopics) {
    String targetTopicKey = newsHomeRouterInput.getEntityKey();
    String targetSubTopicKey = newsHomeRouterInput.getSubEntityKey();

    boolean isNewsSectionAvailable = isNewsSectionAvailable();

    //child wins, if deeplink to subtopic, then check the subtopic is already to ideate tab
    String targetKey = targetSubTopicKey != null ? targetSubTopicKey : targetTopicKey;

    PageEntity alreadyAddedTopic = getAlreadyAddedMatchingEntity(alreadyAddedTopics,
        targetKey);
    boolean launchNewsHome = alreadyAddedTopic != null && isNewsSectionAvailable;
    Intent intent = new Intent(launchNewsHome ? Constants.NEWS_HOME_ACTION : Constants.ENTITY_OPEN_ACTION);

    if (launchNewsHome) {
      setNewsHomeIntentExtras(intent, alreadyAddedTopic);
    } else {
      intent.putExtra(NewsConstants.ENTITY_KEY, targetTopicKey);
      intent.putExtra(NewsConstants.ENTITY_TYPE, newsHomeRouterInput.getEntityType());
    }
    EntityPreviewUtils.buildIntentExtras(intent, newsHomeRouterInput);
    return intent;
  }

  private static Intent getLocationTargetIntent(Context context, NewsHomeRouterInput
      newsHomeRouterInput, List<PageEntity> alreadyAddedLocations) {
    String targetLocationKey = newsHomeRouterInput.getEntityKey();
    String targetSubLocationKey = newsHomeRouterInput.getSubEntityKey();

    boolean isNewsSectionAvailable = isNewsSectionAvailable();
    String entityKeyToQueryDB = targetSubLocationKey != null ? targetSubLocationKey :
        targetLocationKey;
    PageEntity alreadyAddedLocation = getAlreadyAddedMatchingEntity(alreadyAddedLocations,
        entityKeyToQueryDB);
    boolean launchNewsHome = alreadyAddedLocation != null && isNewsSectionAvailable;
    Intent intent = new Intent(launchNewsHome ? Constants.NEWS_HOME_ACTION : Constants.ENTITY_OPEN_ACTION);

    if (launchNewsHome) {
      setNewsHomeIntentExtras(intent, alreadyAddedLocation);
    } else {
      intent.putExtra(NewsConstants.SHOW_SELECT_LOCATION_BUTTON, isNewsSectionAvailable);
      intent.putExtra(NewsConstants.ENTITY_KEY, targetLocationKey);
      intent.putExtra(NewsConstants.ENTITY_TYPE, newsHomeRouterInput.getEntityType());
      if (!CommonUtils.isEmpty(targetSubLocationKey)) {
        intent.putExtra(NewsConstants.SUB_ENTITY_KEY, targetSubLocationKey);
      }
    }
    EntityPreviewUtils.buildIntentExtras(intent, newsHomeRouterInput);
    return intent;
  }

  private static PageEntity getAlreadyAddedMatchingEntity(
      List<PageEntity> alreadyAddedEntities, String targetEntityKey) {
    if (CommonUtils.isEmpty(alreadyAddedEntities) || CommonUtils.isEmpty(targetEntityKey)) {
      return null;
    }
    PageEntity alreadyAddedMatchingEntity = null;
    for (PageEntity alreadyAddedEntity : alreadyAddedEntities) {
      if (CommonUtils.equals(alreadyAddedEntity.getId(), targetEntityKey)
          || CommonUtils.equals(alreadyAddedEntity.getLegacyKey(), targetEntityKey)) {
        alreadyAddedMatchingEntity = alreadyAddedEntity;
        break;
      }
    }
    return alreadyAddedMatchingEntity;
  }

  private static void setNewsHomeIntentExtras(Intent intent, PageEntity preferredTab) {
    if (intent == null || preferredTab == null) {
      return;
    }

    Bundle bundle = new Bundle();
    bundle.putSerializable(NewsConstants.BUNDLE_NEWSPAGE, preferredTab);
    intent.putExtra(NewsConstants.EXTRA_PAGE_ADDED, bundle);
    UserAppSection prevNewsAppSection = AppSectionsProvider.INSTANCE
        .getAnyUserAppSectionOfType(AppSection.NEWS);
    if (prevNewsAppSection != null) {
      intent.putExtra(Constants.APP_SECTION_ID, prevNewsAppSection.getId());
    }
    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
    // TODO post in UI thread
    //BusProvider.getUIBusInstance().post(new NewsNavigator.CloseForNewsHomeEvent());
  }

  private static boolean isNewsSectionAvailable() {
    return AppSectionsProvider.INSTANCE.isSectionAvailable(AppSection.NEWS);
  }
}
