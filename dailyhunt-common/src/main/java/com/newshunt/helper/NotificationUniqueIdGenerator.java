/*
 * Copyright (c) 2016 Newshunt. All rights reserved.
 */
package com.newshunt.helper;

import com.newshunt.common.helper.common.Constants;
import com.newshunt.common.helper.common.DataUtil;
import com.newshunt.common.helper.common.Logger;
import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.dataentity.notification.AdjunctLangStickyNavModel;
import com.newshunt.dataentity.notification.AdsNavModel;
import com.newshunt.dataentity.notification.InAppNotificationModel;
import com.newshunt.dataentity.notification.LiveTVNavModel;
import com.newshunt.dataentity.notification.NavigationModel;
import com.newshunt.dataentity.notification.NavigationType;
import com.newshunt.dataentity.notification.NewsNavModel;
import com.newshunt.dataentity.notification.ProfileNavModel;
import com.newshunt.dataentity.notification.SearchNavModel;
import com.newshunt.dataentity.notification.SocialCommentsModel;
import com.newshunt.dataentity.notification.TVNavModel;
import com.newshunt.dataentity.notification.WebNavModel;
import com.newshunt.dataentity.notification.ExploreNavModel;
import com.newshunt.dataentity.notification.FollowNavModel;
import com.newshunt.dataentity.notification.GroupNavModel;
import com.newshunt.dataentity.notification.util.NotificationConstants;

import androidx.annotation.NonNull;

/**
 * Helper class to generate unique notification ids based on the notification payloads
 *
 * @author santhosh.kc
 */
public class NotificationUniqueIdGenerator {

  //-----------------------------
  // News
  //-----------------------------

  /**
   * Helper function to get notification unique id for news based on navigationModel input
   * (Notification version V1)
   *
   * @param navigationModel - notification V1 payload
   * @return - returns the unique id generated
   */
  public static int generateUniqueIdForNews(NavigationModel navigationModel) {
    if (navigationModel == null) {
      return NotificationConstants.NOTIFICATION_SECTION_NEWS_DEFAULT_ID.hashCode();
    }

    if (navigationModel.getBaseInfo() != null && !DataUtil.isEmpty(navigationModel.getBaseInfo().getDedupeKey())) {
      return navigationModel.getBaseInfo().getDedupeKey().hashCode();
    }

    //for old V1 models, we use only newsId as the uniqueId, without any combination, so we set
    // that and return..
    if (!CommonUtils.isEmpty(navigationModel.getNewsId())) {
      int uniqueId = (int) System.currentTimeMillis();
      try {
        uniqueId = Integer.parseInt(navigationModel.getNewsId());
      } catch (Exception e) {
        if (!DataUtil.isEmpty(navigationModel.getNewsId())) {
          uniqueId = navigationModel.getNewsId().hashCode();
        }
        Logger.caughtException(e);
      }
      return uniqueId;
    }

    StringBuilder uniqueStrBuf = new StringBuilder();
    if (navigationModel.getSectionType() != null) {
      uniqueStrBuf.append(navigationModel.getSectionType());
    }
    if (!CommonUtils.isEmpty(navigationModel.getsType())) {
      uniqueStrBuf.append(navigationModel.getsType());
    }
    if (!CommonUtils.isEmpty(navigationModel.getLanguage())) {
      uniqueStrBuf.append(navigationModel.getLanguage());
    }
    if (!CommonUtils.isEmpty(navigationModel.getNpKey())) {
      uniqueStrBuf.append(navigationModel.getNpKey());
    }
    if (!CommonUtils.isEmpty(navigationModel.getCtKey())) {
      uniqueStrBuf.append(navigationModel.getCtKey());
    }

    if (uniqueStrBuf.length() == 0) {
      return NotificationConstants.NOTIFICATION_SECTION_NEWS_DEFAULT_ID.hashCode();
    }
    return uniqueStrBuf.toString().hashCode();
  }

  /**
   * Helper function to generate unique id for news based on NewsNavModel for notification
   * version V2 and V3
   *
   * @param newsNavModel - input newsnavModel
   * @return - generated unique id
   */
  public static int generateUniqueIdForNews(NewsNavModel newsNavModel) {
    if (newsNavModel == null) {
      return NotificationConstants.NOTIFICATION_SECTION_NEWS_DEFAULT_ID.hashCode();
    }

    if (newsNavModel.getBaseInfo() != null && !DataUtil.isEmpty(newsNavModel.getBaseInfo().getDedupeKey())) {
      return newsNavModel.getBaseInfo().getDedupeKey().hashCode();
    }

    NavigationType navigationType = NavigationType.fromIndex(
        Integer.parseInt(newsNavModel.getsType()));
    if (navigationType == null) {
      return (int) System.currentTimeMillis();
    }
    StringBuilder uniqueStrBuf = new StringBuilder();
    switch (navigationType) {
      case TYPE_OPEN_NEWSITEM:
        int uniqueId;
        String newsId = CommonUtils.isEmpty(newsNavModel.getParentNewsId()) ? newsNavModel.getNewsId() : newsNavModel.getParentNewsId();
        try {
          uniqueId = Integer.parseInt(newsId);
        } catch (Exception e) {
          Logger.caughtException(e);
          uniqueId = newsId.hashCode();
        }
        return uniqueId;
      case TYPE_OPEN_TOPIC:
        if (!CommonUtils.isEmpty(newsNavModel.getTopicKey())) {
          uniqueStrBuf.append(newsNavModel.getTopicKey());
        }
        if (!CommonUtils.isEmpty(newsNavModel.getSubTopicKey())) {
          uniqueStrBuf.append(newsNavModel.getSubTopicKey());
        }
        break;
      case TYPE_OPEN_LOCATION:
        if (!CommonUtils.isEmpty(newsNavModel.getLocationKey())) {
          uniqueStrBuf.append(newsNavModel.getLocationKey());
        }
        if (!CommonUtils.isEmpty(newsNavModel.getSubLocationKey())) {
          uniqueStrBuf.append(newsNavModel.getSubLocationKey());
        }
        break;
      case TYPE_OPEN_NEWS_LIST:
      case TYPE_OPEN_NEWS_LIST_CATEGORY:
        if (!CommonUtils.isEmpty(newsNavModel.getNpKey())) {
          uniqueStrBuf.append(newsNavModel.getNpKey());
        }
        if (!CommonUtils.isEmpty(newsNavModel.getCtKey())) {
          uniqueStrBuf.append(newsNavModel.getCtKey());
        }
        break;
      case TYPE_OPEN_VIRAL_ITEM:
        if (!CommonUtils.isEmpty(newsNavModel.getViralId())) {
          uniqueStrBuf.append(newsNavModel.getViralId());
        } else if (!CommonUtils.isEmpty(newsNavModel.getNewsId())) {
          uniqueStrBuf.append(newsNavModel.getNewsId());
        }
        break;
      default:
        uniqueStrBuf.append(NotificationConstants.NOTIFICATION_SECTION_NEWS_DEFAULT_ID);
    }

    return uniqueStrBuf.toString().hashCode();
  }

  public static int generateUniqueIdForSocialCommentsModel(SocialCommentsModel
                                                               socialCommentsModel) {

    if (socialCommentsModel.getBaseInfo() != null && socialCommentsModel.getBaseInfo().getDedupeKey() != null) {
      return socialCommentsModel.getBaseInfo().getDedupeKey().hashCode();
    }

    return socialCommentsModel.getUniqueId();
  }

  /**
   * Helper function to generate unique id for notifications landing on Nhbrowser based on
   * WebNavModel
   *
   * @param webNavModel - input WebNavModel
   * @return - generated unique id
   */
  public static int generateUniqueIdForWebModel(WebNavModel webNavModel) {

    String uniqueNotificationId = NotificationConstants.NOTIFICATION_SECTION_NHBROWSER_DEFAULT_ID;
    if (webNavModel.getBaseInfo() != null && !DataUtil.isEmpty(webNavModel.getBaseInfo().getDedupeKey())) {
      return webNavModel.getBaseInfo().getDedupeKey().hashCode();
    }

    if (webNavModel != null && !CommonUtils.isEmpty(webNavModel.getUrl())) {
      uniqueNotificationId = webNavModel.getUrl();
    }
    return uniqueNotificationId.hashCode();
  }

  //---------------------------------------------------
  //Buzz
  //---------------------------------------------------

  /**
   * Helper function to generate unique id for buzz based on tvnavModel for notification
   * version V3
   *
   * @param tvNavModel - input tvNavModel
   * @return - generated unique id
   */
  public static int generateUniqueIdForBuzz(TVNavModel tvNavModel) {
    if (tvNavModel == null) {
      return NotificationConstants.NOTIFICATION_SECTION_BUZZ_DEFAULT_ID.hashCode();
    }

    if (tvNavModel.getBaseInfo() != null && !DataUtil.isEmpty(tvNavModel.getBaseInfo().getDedupeKey())) {
      return tvNavModel.getBaseInfo().getDedupeKey().hashCode();
    }

    if (!CommonUtils.isEmpty(tvNavModel.getUnitId())) {
      int uniqueId = (int) System.currentTimeMillis();
      try {
        uniqueId = Integer.parseInt(tvNavModel.getUnitId());
      } catch (Exception e) {
        if (!DataUtil.isEmpty(tvNavModel.getUnitId())) {
          uniqueId = tvNavModel.getUnitId().hashCode();
        }
        Logger.caughtException(e);
      }
      return uniqueId;
    }

    StringBuilder uniqueStrBuf = new StringBuilder();
    if (tvNavModel.getSectionType() != null) {
      uniqueStrBuf.append(tvNavModel.getSectionType());
    }
    if (!CommonUtils.isEmpty(tvNavModel.getCollectionId())) {
      uniqueStrBuf.append(tvNavModel.getCollectionId());
    }
    if (!CommonUtils.isEmpty(tvNavModel.getGroupId())) {
      uniqueStrBuf.append(tvNavModel.getGroupId());
    }
    if (!CommonUtils.isEmpty(tvNavModel.getSubGroupId())) {
      uniqueStrBuf.append(tvNavModel.getSubGroupId());
    }

    if (uniqueStrBuf.length() == 0) {
      return NotificationConstants.NOTIFICATION_SECTION_BUZZ_DEFAULT_ID.hashCode();
    }
    return uniqueStrBuf.toString().hashCode();
  }

  public static int generateUniqueIdForLiveTv(LiveTVNavModel tvNavModel) {
    if (tvNavModel == null) {
      return NotificationConstants.NOTIFICATION_SECTION_LIVETV_DEFAULT_ID.hashCode();
    }

    if (tvNavModel.getBaseInfo() != null && !DataUtil.isEmpty(tvNavModel.getBaseInfo().getDedupeKey())) {
      return tvNavModel.getBaseInfo().getDedupeKey().hashCode();
    }

    if (!CommonUtils.isEmpty(tvNavModel.getUnitId())) {
      int uniqueId = (int) System.currentTimeMillis();
      try {
        uniqueId = Integer.parseInt(tvNavModel.getUnitId());
      } catch (Exception e) {
        if (!DataUtil.isEmpty(tvNavModel.getUnitId())) {
          uniqueId = tvNavModel.getUnitId().hashCode();
        }
        Logger.caughtException(e);
      }
      return uniqueId;
    }

    StringBuilder uniqueStrBuf = new StringBuilder();
    if (tvNavModel.getSectionType() != null) {
      uniqueStrBuf.append(tvNavModel.getSectionType());
    }
    if (!CommonUtils.isEmpty(tvNavModel.getCollectionId())) {
      uniqueStrBuf.append(tvNavModel.getCollectionId());
    }
    if (!CommonUtils.isEmpty(tvNavModel.getGroupId())) {
      uniqueStrBuf.append(tvNavModel.getGroupId());
    }
    if (!CommonUtils.isEmpty(tvNavModel.getSubGroupId())) {
      uniqueStrBuf.append(tvNavModel.getSubGroupId());
    }

    if (uniqueStrBuf.length() == 0) {
      return NotificationConstants.NOTIFICATION_SECTION_LIVETV_DEFAULT_ID.hashCode();
    }
    return uniqueStrBuf.toString().hashCode();
  }

  public static int generateUniqueIdForAds(AdsNavModel adsNavModel) {
    if (adsNavModel == null) {
      return NotificationConstants.NOTIFICATION_SECTION_ADS_DEFAULT_ID.hashCode();
    }

    if (adsNavModel.getBaseInfo() != null && !DataUtil.isEmpty(adsNavModel.getBaseInfo().getDedupeKey())) {
      return adsNavModel.getBaseInfo().getDedupeKey().hashCode();
    }

    NavigationType navigationType = NavigationType.fromIndex(
        Integer.parseInt(adsNavModel.getsType()));
    if (navigationType == null) {
      return (int) System.currentTimeMillis();
    }
    return NotificationConstants.NOTIFICATION_SECTION_ADS_DEFAULT_ID.hashCode();
  }

  public static int generateUniqueIdForExplore(ExploreNavModel exploreNavModel) {
    if (exploreNavModel == null) {
      return NotificationConstants.NOTIFICATION_SECTION_EXPLORE_DEFAULT_ID.hashCode();
    }

    if (exploreNavModel.getBaseInfo() != null && !DataUtil.isEmpty(exploreNavModel.getBaseInfo().getDedupeKey())) {
      return exploreNavModel.getBaseInfo().getDedupeKey().hashCode();
    }

    NavigationType navigationType = NavigationType.fromIndex(
        Integer.parseInt(exploreNavModel.getsType()));
    if (navigationType == null) {
      return (int) System.currentTimeMillis();
    }
    switch (navigationType) {
      case TYPE_OPEN_EXPLORE_ENTITY:
        StringBuilder sbr = new StringBuilder();
        if (!CommonUtils.isEmpty(exploreNavModel.getType())) {
          sbr.append(exploreNavModel.getType());
        }
        if (!CommonUtils.isEmpty(exploreNavModel.getId())) {
          sbr.append(exploreNavModel.getId());
        }
        return sbr.toString().hashCode();
      default:
        return NotificationConstants.NOTIFICATION_SECTION_EXPLORE_DEFAULT_ID.hashCode();
    }
  }

  public static int generateUniqueIdForFollow(FollowNavModel followNavModel) {
    if (followNavModel == null) {
      return NotificationConstants.NOTIFICATION_SECTION_FOLLOW_DEFAULT_ID.hashCode();
    }

    if (followNavModel.getBaseInfo() != null && !DataUtil.isEmpty(followNavModel.getBaseInfo().getDedupeKey())) {
      return followNavModel.getBaseInfo().getDedupeKey().hashCode();
    }

    NavigationType navigationType = NavigationType.fromIndex(
        Integer.parseInt(followNavModel.getsType()));
    if (navigationType == null) {
      return (int) System.currentTimeMillis();
    }
    switch (navigationType) {
      case TYPE_OPEN_FOLLOWING:
      case TYPE_OPEN_FOLLOWING_FEED:
      case TYPE_OPEN_EXPLORE_VIEW_TAB:
        StringBuilder sbr = new StringBuilder();
        if (!CommonUtils.isEmpty(followNavModel.getTabType())) {
          sbr.append(followNavModel.getTabType());
        }
        if (!CommonUtils.isEmpty(followNavModel.getSubTabType())) {
          sbr.append(followNavModel.getSubTabType());
        }
        return sbr.toString().hashCode();
      default:
        return NotificationConstants.NOTIFICATION_SECTION_FOLLOW_DEFAULT_ID.hashCode();
    }
  }

  public static int generateUniqueIdForProfile(ProfileNavModel profileNavModel) {
    if (profileNavModel == null) {
      return NotificationConstants.NOTIFICATION_SECTION_PROFILE_DEFAULT_ID.hashCode();
    }

    if (profileNavModel.getBaseInfo() != null && !DataUtil.isEmpty(profileNavModel.getBaseInfo().getDedupeKey())) {
      return profileNavModel.getBaseInfo().getDedupeKey().hashCode();
    }

    NavigationType navigationType = NavigationType.fromIndex(
        Integer.parseInt(profileNavModel.getsType()));
    if (navigationType == null) {
      return NotificationConstants.NOTIFICATION_SECTION_PROFILE_DEFAULT_ID.hashCode();
    }

    switch (navigationType) {
      case TYPE_OPEN_PROFILE:
        @NonNull
        String landingTab = CommonUtils.isEmpty(profileNavModel.getTabType()) ? Constants.EMPTY_STRING :
            profileNavModel.getTabType();
        String userId = !CommonUtils.isEmpty(profileNavModel.getUserId())? profileNavModel.getUserId()
            : profileNavModel.getUserHandle();
        return CommonUtils.isEmpty(userId)?
            NotificationConstants.NOTIFICATION_SECTION_PROFILE_DEFAULT_ID.hashCode() :
            (userId + landingTab).hashCode();
      default:
        return NotificationConstants.NOTIFICATION_SECTION_FOLLOW_DEFAULT_ID.hashCode();
    }
  }

  public static int generateUniqueIdForGroup(GroupNavModel groupNavModel) {
    if (groupNavModel == null) {
      return NotificationConstants.NOTIFICATION_SECTION_GROUP_DEFAULT_ID.hashCode();
    }

    if (groupNavModel.getBaseInfo() != null && !DataUtil.isEmpty(groupNavModel.getBaseInfo().getDedupeKey())) {
      return groupNavModel.getBaseInfo().getDedupeKey().hashCode();
    }

    NavigationType navigationType = NavigationType.fromIndex(
        Integer.parseInt(groupNavModel.getsType()));
    if (navigationType == null) {
      return NotificationConstants.NOTIFICATION_SECTION_GROUP_DEFAULT_ID.hashCode();
    }

    switch (navigationType) {
      case TYPE_OPEN_SOCIAL_GROUP:
        return NotificationConstants.NOTIFICATION_SECTION_GROUP_VIEW.hashCode();
      case TYPE_OPEN_SOCIAL_GROUP_CREATE:
        return NotificationConstants.NOTIFICATION_SECTION_GROUP_CREATE.hashCode();
      case TYPE_OPEN_SOCIAL_GROUP_APPROVAL: {
        String subTab = groupNavModel.getBaseInfo().getType();
        return (NotificationConstants.NOTIFICATION_SECTION_GROUP_APPROVAL + subTab).hashCode();
      }
    }
    return NotificationConstants.NOTIFICATION_SECTION_GROUP_DEFAULT_ID.hashCode();
  }

  public static int generateUniqueIdForSearch(SearchNavModel searchNavModel) {
    if (searchNavModel.getBaseInfo() != null && !DataUtil.isEmpty(searchNavModel.getBaseInfo().getDedupeKey())) {
      return searchNavModel.getBaseInfo().getDedupeKey().hashCode();
    }

    return searchNavModel.toString().hashCode();
  }

  public static int generateUniqueIdForAdjunctSticky(AdjunctLangStickyNavModel adjunctLangStickyNavModel) {
    return Constants.ADJUNCT_LANG_DEFAULT_NOTIFICATION_ID;
  }

  public static int generateUniqueIdForInApp(InAppNotificationModel inAppNotificationModel) {
    if (inAppNotificationModel == null) {
      return NotificationConstants.IN_APP_NOTIFICATION_DEFAULT_ID.hashCode();
    }

    if (inAppNotificationModel.getBaseInfo() != null && !DataUtil.isEmpty(inAppNotificationModel.getBaseInfo().getDedupeKey())) {
      return inAppNotificationModel.getBaseInfo().getDedupeKey().hashCode();
    }
    return NotificationConstants.IN_APP_NOTIFICATION_DEFAULT_ID.hashCode();
  }
}
