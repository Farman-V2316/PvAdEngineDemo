/*
 * Copyright (c) 2016 Newshunt. All rights reserved.
 */
package com.newshunt.deeplink;

import android.net.Uri;

import com.newshunt.common.helper.common.Constants;
import com.newshunt.common.helper.common.JsonUtils;
import com.newshunt.common.helper.common.LanguageUtils;
import com.newshunt.common.helper.common.Logger;
import com.newshunt.common.helper.common.UrlUtil;
import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.dataentity.notification.AdjunctLangModel;
import com.newshunt.dataentity.notification.AdsNavModel;
import com.newshunt.dataentity.notification.AppSectionModel;
import com.newshunt.dataentity.notification.BaseInfo;
import com.newshunt.dataentity.notification.BaseModel;
import com.newshunt.dataentity.notification.ContactsRecoNavModel;
import com.newshunt.dataentity.notification.CreatePostNavModel;
import com.newshunt.dataentity.notification.DeeplinkModel;
import com.newshunt.dataentity.notification.ExploreNavModel;
import com.newshunt.dataentity.notification.FollowNavModel;
import com.newshunt.dataentity.notification.GroupNavModel;
import com.newshunt.dataentity.notification.LangSelectionNavModel;
import com.newshunt.dataentity.notification.LiveTVNavModel;
import com.newshunt.dataentity.notification.LocalNavModel;
import com.newshunt.dataentity.notification.NavigationModel;
import com.newshunt.dataentity.notification.NavigationType;
import com.newshunt.dataentity.notification.NewsNavModel;
import com.newshunt.dataentity.notification.NotificationInboxModel;
import com.newshunt.dataentity.notification.NotificationSectionType;
import com.newshunt.dataentity.notification.NotificationSettingsModel;
import com.newshunt.dataentity.notification.PermissionNavModel;
import com.newshunt.dataentity.notification.ProfileNavModel;
import com.newshunt.dataentity.notification.SSONavModel;
import com.newshunt.dataentity.notification.SearchNavModel;
import com.newshunt.dataentity.notification.SettingsAutoScrollModel;
import com.newshunt.dataentity.notification.SettingsModel;
import com.newshunt.dataentity.notification.SocialCommentsModel;
import com.newshunt.dataentity.notification.TVNavModel;
import com.newshunt.dataentity.notification.WebNavModel;
import com.newshunt.dhutil.R;
import com.newshunt.dhutil.helper.common.DailyhuntConstants;
import com.newshunt.helper.NotificationUniqueIdGenerator;

import java.util.Set;


/**
 * An Utility class for deeplink handling
 *
 * @author santhosh.kc
 */
public class DeeplinkUtils {

  /**
   * Helper function to check if the given deeplink url is of short url type
   *
   * @param deepLink - deeplink url to check
   * @return - true if short url else false
   */
  public static boolean isShortUrl(String deepLink) {
    final String deeplinkBaseUrl = UrlUtil.getBaseUrl(deepLink);
    if (CommonUtils.isEmpty(deeplinkBaseUrl)) {
      return false;
    }
    return deeplinkBaseUrl.contains(CommonUtils.getString(R.string.host_url_short_com)) ||
        deeplinkBaseUrl.contains(CommonUtils.getString(R.string.host_url_short_net) + "/yourls") ||
        deeplinkBaseUrl.contains(CommonUtils.getString(R.string.host_url_short_in)) ||
        deeplinkBaseUrl.contains(CommonUtils.getString(R.string.host_url_short_buzz)) ||
        deeplinkBaseUrl.contains(CommonUtils.getString(R.string.host_url_short_vh_dhunt_in)) ||
        deeplinkBaseUrl.contains(CommonUtils.getString(R.string.host_url_short_dhunt_in)) ||
        deeplinkBaseUrl.contains(CommonUtils.getString(R.string.host_url_short_stage_dhunt_in)) ||
        deeplinkBaseUrl.contains(CommonUtils.getString(R.string.host_url_short_stage));
  }

  public static boolean isValidHost(String deeplink) {
    Uri deepLinkUri;
    try {
      deepLinkUri = Uri.parse(deeplink);
    } catch (Exception e) {
      Logger.caughtException(e);
      return false;
    }
    return deepLinkUri != null && !CommonUtils.isEmpty(deepLinkUri.getHost());
  }

  public static boolean isDHDeeplink(String deepLink) {

    if (CommonUtils.isEmpty(deepLink) || !isValidHost(deepLink)) {
      return false;
    }
    if (isShortUrl(deepLink)) {
      return true;
    }

    return deepLink.contains(CommonUtils.getString(R.string.host_url_dailyhunt)) ||
        deepLink.contains(CommonUtils.getString(R.string.host_url_qa_dailyhunt)) ||
        deepLink.contains(CommonUtils.getString(R.string.host_url_web)) ||
        deepLink.contains(CommonUtils.getString(R.string.host_url_webqa)) ||
        deepLink.contains(CommonUtils.getString(R.string.profile_dailyhunt_in)) ||
        deepLink.contains(CommonUtils.getString(R.string.group_dailyhunt_in));

  }

  /**
   * Utility function to extract DeeplinkModel into navigation model by checking if it is
   * deeplinkable and extracting details into navigationmodel
   *
   * @param deeplinkModel   - deeplink model to extract from
   * @param navigationModel - navigation model to extract into
   * @return - true if deeplink model can be extracted into NavigationModel else false
   */
  public static boolean extractDeeplinkModelIntoNavigationModel(DeeplinkModel deeplinkModel,
                                                                NavigationModel navigationModel) {
    if (deeplinkModel == null || navigationModel == null) {
      return false;
    }

    if (isDeeplinkable(navigationModel.getsType())) {
      navigationModel.setSectionType(NotificationSectionType.APP);
    } else if (deeplinkModel.isFallbackToHomeOnFailure()) {
      navigationModel.setsType(String.valueOf(NavigationType.TYPE_OPEN_APP.getIndex()));
    } else {
      return false;
    }
    //no need to set notification unique id, as NavigationModel are not added to DB
    extractDeeplinkBaseInfo(deeplinkModel.getBaseInfo(), navigationModel);
    return true;
  }

  /**
   * Utility function to extract DeeplinkModel into NewsNavModel by checking if it is deeplinkable
   * and extracting details into newsnavmodel
   *
   * @param deeplinkModel - deeplinkModel to extract from
   * @param newsNavModel  - newsNavModel to extract into
   * @return - true if deeplink model can be extracted into NewsNavModel else false
   */
  public static boolean extractDeeplinkModelIntoNewsNavModel(DeeplinkModel deeplinkModel,
                                                             NewsNavModel newsNavModel) {
    if (newsNavModel == null || deeplinkModel == null) {
      return false;
    }
    newsNavModel.setPopupDisplayType(deeplinkModel.getPopupDisplayType());
    newsNavModel.setAdjunct(deeplinkModel.isAdjunct());
    BaseInfo baseInfo = newsNavModel.getBaseInfo();
    if (baseInfo == null) {
      baseInfo = new BaseInfo();
      newsNavModel.setBaseInfo(baseInfo);
    }
    if (isDeeplinkable(newsNavModel.getsType())) {
      baseInfo.setSectionType(NotificationSectionType.NEWS);
    } else if (deeplinkModel.isFallbackToHomeOnFailure()) {
      newsNavModel.setsType(String.valueOf(NavigationType.TYPE_OPEN_NEWS_HOME.getIndex()));
      baseInfo.setSectionType(NotificationSectionType.NEWS);
    } else {
      return false;
    }

    //Notification unique id
    baseInfo.setUniqueId(NotificationUniqueIdGenerator.generateUniqueIdForNews(newsNavModel));
    extractDeeplinkBaseInfo(deeplinkModel.getBaseInfo(), baseInfo);
    return true;
  }

  public static boolean extractDeeplinkModelIntoSocialCommentsModel(DeeplinkModel deeplinkModel,
                                                                    SocialCommentsModel
                                                                        socialCommentsModel) {
    if (socialCommentsModel == null || deeplinkModel == null) {
      return false;
    }

    BaseInfo baseInfo = socialCommentsModel.getBaseInfo();
    if (baseInfo == null) {
      baseInfo = new BaseInfo();
      socialCommentsModel.setBaseInfo(baseInfo);
    }
    if (isDeeplinkable(socialCommentsModel.getsType())) {
      baseInfo.setSectionType(NotificationSectionType.SOCIAL_SECTION);
    } else if (deeplinkModel.isFallbackToHomeOnFailure()) {
      socialCommentsModel.setsType(String.valueOf(NavigationType.TYPE_OPEN_NEWS_HOME.getIndex()));
      baseInfo.setSectionType(NotificationSectionType.SOCIAL_SECTION);
    } else {
      return false;
    }

    //Notification unique id
    baseInfo.setUniqueId(
        NotificationUniqueIdGenerator.generateUniqueIdForSocialCommentsModel(socialCommentsModel));
    extractDeeplinkBaseInfo(deeplinkModel.getBaseInfo(), baseInfo);

    return true;
  }

  public static boolean extractDeeplinkModeIntoWebNavModel(DeeplinkModel deeplinkModel,
                                                           WebNavModel webNavModel) {
    if (deeplinkModel == null || webNavModel == null) {
      return false;
    }
    BaseInfo baseInfo = webNavModel.getBaseInfo();
    if (baseInfo == null) {
      baseInfo = new BaseInfo();
      webNavModel.setBaseInfo(baseInfo);
    }
    int uniqueId = NotificationUniqueIdGenerator.generateUniqueIdForWebModel(webNavModel);
    baseInfo.setUniqueId(uniqueId);

    baseInfo.setSectionType(NotificationSectionType.WEB);
    webNavModel.setsType(String.valueOf(NavigationType.TYPE_OPEN_WEBPAGE.getIndex()));
    extractDeeplinkBaseInfo(deeplinkModel.getBaseInfo(), baseInfo);
    return true;
  }

  /**
   * Utility function to extract DeeplinkModel into tvNavModel by checking if it is deeplinkable
   * and extracting details into tvNavModel
   *
   * @param deeplinkModel - deeplinkModel to extract from
   * @param tvNavModel    - tvNavModel to extract into
   * @return - true if deeplink model can be extracted into tvNavModel else false
   */
  public static boolean extractDeeplinkModelIntoTVNavModel(DeeplinkModel deeplinkModel,
                                                           TVNavModel tvNavModel) {
    if (tvNavModel == null || deeplinkModel == null) {
      return false;
    }

    BaseInfo baseInfo = tvNavModel.getBaseInfo();
    if (baseInfo == null) {
      baseInfo = new BaseInfo();
      tvNavModel.setBaseInfo(baseInfo);
    }
    if (isDeeplinkable(tvNavModel.getsType())) {
      baseInfo.setSectionType(NotificationSectionType.TV);
    } else if (deeplinkModel.isFallbackToHomeOnFailure()) {
      tvNavModel.setsType(String.valueOf(NavigationType.TYPE_TV_OPEN_TO_SECTION.getIndex()));
      baseInfo.setSectionType(NotificationSectionType.TV);
    } else {
      return false;
    }

    //Notification unique id
    baseInfo.setUniqueId(NotificationUniqueIdGenerator.generateUniqueIdForBuzz(tvNavModel));
    extractDeeplinkBaseInfo(deeplinkModel.getBaseInfo(), baseInfo);
    return true;
  }

  public static boolean extractDeeplinkModelIntoLiveTVNavModel(DeeplinkModel deeplinkModel,
                                                               LiveTVNavModel liveTvNavModel) {
    if (liveTvNavModel == null || deeplinkModel == null) {
      return false;
    }

    BaseInfo baseInfo = liveTvNavModel.getBaseInfo();
    if (baseInfo == null) {
      baseInfo = new BaseInfo();
      liveTvNavModel.setBaseInfo(baseInfo);
    }
    if (isDeeplinkable(liveTvNavModel.getsType())) {
      baseInfo.setSectionType(NotificationSectionType.LIVETV);
    } else if (deeplinkModel.isFallbackToHomeOnFailure()) {
      liveTvNavModel.setsType(String.valueOf(NavigationType.TYPE_OPEN_LIVETV_SECTION.getIndex()));
      baseInfo.setSectionType(NotificationSectionType.LIVETV);
    } else {
      return false;
    }

    //Notification unique id
    baseInfo.setUniqueId(NotificationUniqueIdGenerator.generateUniqueIdForLiveTv(liveTvNavModel));
    extractDeeplinkBaseInfo(deeplinkModel.getBaseInfo(), baseInfo);
    return true;
  }

  public static boolean extractDeeplinkModelIntoAdsNavModel(DeeplinkModel deeplinkModel,
                                                            AdsNavModel adsNavModel) {
    if (adsNavModel == null || deeplinkModel == null) {
      return false;
    }

    BaseInfo baseInfo = adsNavModel.getBaseInfo();
    if (baseInfo == null) {
      baseInfo = new BaseInfo();
      adsNavModel.setBaseInfo(baseInfo);
    }
    if (isDeeplinkable(adsNavModel.getsType())) {
      baseInfo.setSectionType(NotificationSectionType.ADS);
    } else if (deeplinkModel.isFallbackToHomeOnFailure()) {
      adsNavModel.setsType(String.valueOf(NavigationType.TYPE_OPEN_APP.getIndex()));
      baseInfo.setSectionType(NotificationSectionType.ADS);
    } else {
      return false;
    }

    //Notification unique id
    baseInfo.setUniqueId(NotificationUniqueIdGenerator.generateUniqueIdForAds(adsNavModel));
    extractDeeplinkBaseInfo(deeplinkModel.getBaseInfo(), baseInfo);
    return true;
  }

  public static boolean extractDeeplinkModelIntoProfileNavModel(DeeplinkModel deeplinkModel,
                                                                ProfileNavModel profileNavModel) {
    if (profileNavModel == null || deeplinkModel == null) {
      return false;
    }

    BaseInfo baseInfo = profileNavModel.getBaseInfo();
    if (baseInfo == null) {
      baseInfo = new BaseInfo();
      profileNavModel.setBaseInfo(baseInfo);
    }

    if (isDeeplinkable(profileNavModel.getsType())) {
      baseInfo.setSectionType(NotificationSectionType.PROFILE_SECTION);
    } else if (deeplinkModel.isFallbackToHomeOnFailure()) {
      profileNavModel.setsType(String.valueOf(NavigationType.TYPE_OPEN_APP.getIndex()));
      baseInfo.setSectionType(NotificationSectionType.APP);
    } else {
      return false;
    }

    baseInfo.setUniqueId(NotificationUniqueIdGenerator.generateUniqueIdForProfile(profileNavModel));
    extractDeeplinkBaseInfo(deeplinkModel.getBaseInfo(), baseInfo);
    return true;
  }

  public static boolean extractDeeplinkModelIntoGroupNavModel(DeeplinkModel deeplinkModel,
                                                                GroupNavModel groupNavModel) {
    if (groupNavModel == null || deeplinkModel == null) {
      return false;
    }

    BaseInfo baseInfo = groupNavModel.getBaseInfo();
    if (baseInfo == null) {
      baseInfo = new BaseInfo();
      groupNavModel.setBaseInfo(baseInfo);
    }

    if (isDeeplinkable(groupNavModel.getsType())) {
      baseInfo.setSectionType(NotificationSectionType.GROUP_SECTION);
    } else if (deeplinkModel.isFallbackToHomeOnFailure()) {
      groupNavModel.setsType(String.valueOf(NavigationType.TYPE_OPEN_APP.getIndex()));
      baseInfo.setSectionType(NotificationSectionType.APP);
    } else {
      return false;
    }

    baseInfo.setUniqueId(NotificationUniqueIdGenerator.generateUniqueIdForGroup(groupNavModel));
    extractDeeplinkBaseInfo(deeplinkModel.getBaseInfo(), baseInfo);
    return true;
  }

  public static boolean extractDeeplinkModeIntoSSONavModel(DeeplinkModel deeplinkModel,
                                                           SSONavModel ssoNavModel) {
    if (ssoNavModel == null || deeplinkModel == null) {
      return false;
    }

    BaseInfo baseInfo = ssoNavModel.getBaseInfo();
    if (baseInfo == null) {
      baseInfo = new BaseInfo();
      ssoNavModel.setBaseInfo(baseInfo);
    }

    if (isDeeplinkable(ssoNavModel.getsType())) {
      baseInfo.setSectionType(NotificationSectionType.SSO);
      ssoNavModel.setsType(String.valueOf(NavigationType.TYPE_OPEN_SSO.getIndex()));
    } else if (deeplinkModel.isFallbackToHomeOnFailure()) {
      ssoNavModel.setsType(String.valueOf(NavigationType.TYPE_OPEN_APP.getIndex()));
      baseInfo.setSectionType(NotificationSectionType.SSO);
    } else {
      return false;
    }

    //no need to set notification unique id, as NavigationModel are not added to DB
    extractDeeplinkBaseInfo(deeplinkModel.getBaseInfo(), baseInfo);
    return true;
  }

  public static boolean extractDeeplinkModelIntoExploreModel(DeeplinkModel deeplinkModel,
                                                             ExploreNavModel exploreNavModel) {
    if (exploreNavModel == null || deeplinkModel == null) {
      return false;
    }

    BaseInfo baseInfo = exploreNavModel.getBaseInfo();
    if (baseInfo == null) {
      baseInfo = new BaseInfo();
      exploreNavModel.setBaseInfo(baseInfo);
    }

    if (isDeeplinkable(exploreNavModel.getsType())) {
      baseInfo.setSectionType(NotificationSectionType.EXPLORE_SECTION);
    } else if (deeplinkModel.isFallbackToHomeOnFailure()) {
      exploreNavModel.setsType(String.valueOf(NavigationType.TYPE_OPEN_APP.getIndex()));
      baseInfo.setSectionType(NotificationSectionType.EXPLORE_SECTION);
    } else {
      return false;
    }

    baseInfo.setUniqueId(NotificationUniqueIdGenerator.generateUniqueIdForExplore(exploreNavModel));
    extractDeeplinkBaseInfo(deeplinkModel.getBaseInfo(), baseInfo);
    return true;
  }

  public static boolean extractDeeplinkModelIntoFollowModel(DeeplinkModel deeplinkModel,
                                                            FollowNavModel followNavModel) {
    if (deeplinkModel == null || followNavModel == null) {
      return false;
    }

    BaseInfo baseInfo = followNavModel.getBaseInfo();
    if (baseInfo == null) {
      baseInfo = new BaseInfo();
      followNavModel.setBaseInfo(baseInfo);
    }

    if (isDeeplinkable(followNavModel.getsType())) {
      baseInfo.setSectionType(NotificationSectionType.FOLLOW_SECTION);
    } else if (deeplinkModel.isFallbackToHomeOnFailure()) {
      followNavModel.setsType(String.valueOf(NavigationType.TYPE_OPEN_FOLLOW_HOME.getIndex()));
      baseInfo.setSectionType(NotificationSectionType.FOLLOW_SECTION);
    } else {
      return false;
    }

    baseInfo.setUniqueId(NotificationUniqueIdGenerator.generateUniqueIdForFollow(followNavModel));
    extractDeeplinkBaseInfo(deeplinkModel.getBaseInfo(), baseInfo);
    return true;
  }

  public static boolean extractDeeplinkModelIntoCreatePostModel(DeeplinkModel deeplinkModel,
                                                          CreatePostNavModel cpNavModel) {
    if (deeplinkModel == null || cpNavModel == null) {
      return false;
    }

    BaseInfo baseInfo = cpNavModel.getBaseInfo();
    if (baseInfo == null) {
      baseInfo = new BaseInfo();
      cpNavModel.setBaseInfo(baseInfo);
    }
    baseInfo.setSectionType(NotificationSectionType.APP);

    extractDeeplinkBaseInfo(deeplinkModel.getBaseInfo(), baseInfo);
    return true;
  }

  public static boolean extractDeeplinkModelIntoContactsRecoModel(DeeplinkModel deeplinkModel,
                                                                ContactsRecoNavModel contactsRecoNavModel) {
    if (deeplinkModel == null || contactsRecoNavModel == null) {
      return false;
    }

    BaseInfo baseInfo = contactsRecoNavModel.getBaseInfo();
    if (baseInfo == null) {
      baseInfo = new BaseInfo();
      contactsRecoNavModel.setBaseInfo(baseInfo);
    }
    baseInfo.setSectionType(NotificationSectionType.APP);

    extractDeeplinkBaseInfo(deeplinkModel.getBaseInfo(), baseInfo);
    return true;
  }

  private static void extractDeeplinkBaseInfo(BaseInfo deeplinkBaseInfo, BaseInfo
      baseModelBaseInfo) {
    if (deeplinkBaseInfo == null || baseModelBaseInfo == null) {
      return;
    }

    //Notification id
    baseModelBaseInfo.setId(deeplinkBaseInfo.getId());
    //Setting notification layout type
    baseModelBaseInfo.setLayoutType(deeplinkBaseInfo.getLayoutType());
    //Notification Timestamp
    baseModelBaseInfo.setTimeStamp(deeplinkBaseInfo.getTimeStamp());
    //Notification Message
    baseModelBaseInfo.setMessage(deeplinkBaseInfo.getMessage());
    //Notification Uni message
    baseModelBaseInfo.setUniMsg(deeplinkBaseInfo.getUniMsg());
    //Notification Expiry time
    baseModelBaseInfo.setExpiryTime(deeplinkBaseInfo.getExpiryTime());
    //Notification BigImageLinkv2
    baseModelBaseInfo.setBigImageLinkV2(deeplinkBaseInfo.getBigImageLinkV2());
    //Notification ImageLinkV2
    baseModelBaseInfo.setImageLinkV2(deeplinkBaseInfo.getImageLinkV2());
    //Notification BigText
    baseModelBaseInfo.setBigText(deeplinkBaseInfo.getBigText());
    //Notification priority
    baseModelBaseInfo.setPriority(deeplinkBaseInfo.getPriority());
    //Notification isUrdu for remote view layout (NotificationLayout Builder)
    baseModelBaseInfo.setUrdu(LanguageUtils.isUrdu(deeplinkBaseInfo.getLanguage()));
    //Set experiment params.
    baseModelBaseInfo.setExperimentParams(deeplinkBaseInfo.getExperimentParams());
    baseModelBaseInfo.setDoNotAutoFetchSwipeUrl(deeplinkBaseInfo.isDoNotAutoFetchSwipeUrl());
    //Notification channel
    baseModelBaseInfo.setChannelId(deeplinkBaseInfo.getChannelId());
    //Notification channel group
    baseModelBaseInfo.setChannelGroupId(deeplinkBaseInfo.getChannelGroupId());
    //Filter type for notification
    baseModelBaseInfo.setFilterType(deeplinkBaseInfo.getFilterType());
    //notification type
    baseModelBaseInfo.setNotifType(deeplinkBaseInfo.getNotifType());
    baseModelBaseInfo.setNotifSubType(deeplinkBaseInfo.getNotifSubType());

    baseModelBaseInfo.setInboxImageLink(deeplinkBaseInfo.getInboxImageLink());
  }

  private static void extractDeeplinkBaseInfo(BaseInfo deeplinkBaseInfo, NavigationModel
      navigationModel) {
    if (deeplinkBaseInfo == null || navigationModel == null) {
      return;
    }

    //Notification id
    navigationModel.setId(deeplinkBaseInfo.getId());
    //Setting notification layout type
    navigationModel.setLayoutType(deeplinkBaseInfo.getLayoutType());
    //Notification Timestamp
    navigationModel.setTimeStamp(deeplinkBaseInfo.getTimeStamp());
    //Notification Message
    navigationModel.setMsg(deeplinkBaseInfo.getMessage());
    //Notification Uni message
    navigationModel.setUniMsg(deeplinkBaseInfo.getUniMsg());
    //Notification Expiry time
    navigationModel.setExpiryTime(deeplinkBaseInfo.getExpiryTime());
    //Notification BigImageLinkv2
    navigationModel.setBigImageLinkV2(deeplinkBaseInfo.getBigImageLinkV2());
    //Notification ImageLinkV2
    navigationModel.setImageLinkV2(deeplinkBaseInfo.getImageLinkV2());
    //Notification BigText
    navigationModel.setBigText(deeplinkBaseInfo.getBigText());
    //Notification priority
    navigationModel.setPriority(deeplinkBaseInfo.getPriority());
    //Notification isUrdu for remote view layout (NotificationLayout Builder)
    navigationModel.setUrdu(LanguageUtils.isUrdu(deeplinkBaseInfo.getLanguage()));
    navigationModel.setDoNotAutoFetchSwipeUrl(deeplinkBaseInfo.isDoNotAutoFetchSwipeUrl());
    //Notification channel
    navigationModel.setChannelId(deeplinkBaseInfo.getChannelId());
    //Notification channel group
    navigationModel.setChannelGroupId(deeplinkBaseInfo.getChannelGroupId());
    //filter
    navigationModel.setFilterType(deeplinkBaseInfo.getFilterType());
    navigationModel.setNotifType(deeplinkBaseInfo.getNotifType());
    navigationModel.setNotifSubType(deeplinkBaseInfo.getNotifSubType());
  }

  /*
   * Utility function to check if deeplink parsed NavigationType is something that will land at
   * destination. If sType is NavigationType.TYPE_OPEN_DEFAULT means, deeplink parser did not
   * understand the deeplink url and cannot determine the open type
   */
  private static boolean isDeeplinkable(String sType) {
    if (CommonUtils.isEmpty(sType)) {
      return false;
    }

    NavigationType navigationType = NavigationType.fromIndex(
        Integer.parseInt(sType));
    if (navigationType == null) {
      return false;
    }

    return navigationType != NavigationType.TYPE_OPEN_DEFAULT;
  }

  public static BaseModel getSocialCommentsModel(String deeplink) {
    SocialCommentsModel socialCommentsModel = new SocialCommentsModel();
    Uri deeplinkUri = Uri.parse(deeplink);
    Set<String> queryParams = deeplinkUri.getQueryParameterNames();
    String json;
    String navigationType;
    if (queryParams.contains(DailyhuntConstants.DEEPLINK_ALL_COMMENTS_QUERY_PARAMS)) {
      json = deeplinkUri.getQueryParameter(DailyhuntConstants.DEEPLINK_ALL_COMMENTS_QUERY_PARAMS);
      navigationType =
              Constants.EMPTY_STRING + NavigationType.TYPE_OPEN_ALL_SOCIAL_COMMENTS.getIndex();
    } else {
      return null;
    }

    if (!CommonUtils.isEmpty(json)) {
      socialCommentsModel = JsonUtils.fromJson(json, SocialCommentsModel.class);
      if (socialCommentsModel == null) {
        return null;
      }
      socialCommentsModel.setsType(navigationType);
      if (socialCommentsModel.getBaseInfo() == null) {
        socialCommentsModel.setBaseInfo(new BaseInfo());
      }
    }
    return socialCommentsModel;
  }

  public static boolean extractDeeplinkModeIntoSearchNavModel(DeeplinkModel deeplinkModel,
                                                              SearchNavModel searchNavModel) {
    if (searchNavModel == null || deeplinkModel == null) {
      return false;
    }

    BaseInfo baseInfo = searchNavModel.getBaseInfo();
    if (baseInfo == null) {
      baseInfo = new BaseInfo();
      searchNavModel.setBaseInfo(baseInfo);
    }

    if (isDeeplinkable(searchNavModel.getsType())) {
      baseInfo.setSectionType(NotificationSectionType.SEARCH_SECTION);
    } else if (deeplinkModel.isFallbackToHomeOnFailure()) {
      deeplinkModel.setsType(String.valueOf(NavigationType.TYPE_OPEN_NEWS_HOME.getIndex()));
      baseInfo.setSectionType(NotificationSectionType.SEARCH_SECTION);
    } else {
      return false;
    }

    baseInfo.setUniqueId(NotificationUniqueIdGenerator.generateUniqueIdForSearch(searchNavModel));
    extractDeeplinkBaseInfo(deeplinkModel.getBaseInfo(), baseInfo);
    return true;
  }

  public static DeeplinkModel createDeeplinkModel(String shortUrl, BaseInfo baseInfo) {
    DeeplinkModel deeplinkModel = new DeeplinkModel();
    if (baseInfo == null) {
      baseInfo = new BaseInfo();
    }
    deeplinkModel.setBaseInfo(baseInfo);
    deeplinkModel.setFallbackToHomeOnFailure(true);
    deeplinkModel.setDeeplinkUrl(shortUrl);
    return deeplinkModel;
  }

  public static boolean extractDeeplinkModelIntoRuntimePermission(DeeplinkModel deeplinkModel,
                                                                  PermissionNavModel permissionNavModel) {
    if (null == deeplinkModel || null == permissionNavModel) {
      return false;
    }
    BaseInfo baseInfo = permissionNavModel.getBaseInfo();
    if (null == baseInfo) {
      baseInfo = new BaseInfo();
      permissionNavModel.setBaseInfo(baseInfo);
    }
    if (isDeeplinkable(permissionNavModel.getsType())) {
      baseInfo.setSectionType(NotificationSectionType.PERMISSIONS);
      permissionNavModel.setsType(String.valueOf(NavigationType.TYPE_OPEN_PERMISSION.getIndex()));
    } else if (deeplinkModel.isFallbackToHomeOnFailure()) {
      baseInfo.setSectionType(NotificationSectionType.PERMISSIONS);
      permissionNavModel.setsType(String.valueOf(NavigationType.TYPE_OPEN_APP.getIndex()));
    } else {
      return false;
    }
    extractDeeplinkBaseInfo(deeplinkModel.getBaseInfo(), baseInfo);
    return true;
  }

  public static boolean extractDeeplinkModelIntoLangeSelection(DeeplinkModel deeplinkModel, LangSelectionNavModel baseModel) {
      if (deeplinkModel == null || baseModel == null) {
        return false;
      }

      BaseInfo baseInfo = baseModel.getBaseInfo();
      if (baseInfo == null) {
        baseInfo = new BaseInfo();
        baseModel.setBaseInfo(baseInfo);
      }
      baseInfo.setSectionType(NotificationSectionType.APP);

      extractDeeplinkBaseInfo(deeplinkModel.getBaseInfo(), baseInfo);
      return true;
  }

  public static boolean extractDeeplinkModelIntoAdjunctLang(DeeplinkModel deeplinkModel,
                                                  AdjunctLangModel baseModel) {
    if (deeplinkModel == null || baseModel == null) {
      return false;
    }

    BaseInfo baseInfo = baseModel.getBaseInfo();
    if (baseInfo == null) {
      baseInfo = new BaseInfo();
      baseModel.setBaseInfo(baseInfo);
    }
    baseInfo.setSectionType(NotificationSectionType.APP);

    extractDeeplinkBaseInfo(deeplinkModel.getBaseInfo(), baseInfo);
    return true;
  }

  public static boolean extractDeeplinkModelIntoSettings(DeeplinkModel deeplinkModel,
                                                           SettingsModel baseModel) {
    if (deeplinkModel == null || baseModel == null) {
      return false;
    }

    BaseInfo baseInfo = baseModel.getBaseInfo();
    if (baseInfo == null) {
      baseInfo = new BaseInfo();
      baseModel.setBaseInfo(baseInfo);
    }
    baseInfo.setSectionType(NotificationSectionType.APP);

    extractDeeplinkBaseInfo(deeplinkModel.getBaseInfo(), baseInfo);
    return true;
  }

  public static boolean extractDeeplinkModelIntoNotificationInbox(DeeplinkModel deeplinkModel,
                                                                   NotificationInboxModel baseModel) {
    if (deeplinkModel == null || baseModel == null) {
      return false;
    }

    BaseInfo baseInfo = baseModel.getBaseInfo();
    if (baseInfo == null) {
      baseInfo = new BaseInfo();
      baseModel.setBaseInfo(baseInfo);
    }
    baseInfo.setSectionType(NotificationSectionType.APP);

    extractDeeplinkBaseInfo(deeplinkModel.getBaseInfo(), baseInfo);
    return true;
  }

  public static boolean extractDeeplinkModelIntoNotificationSettings(DeeplinkModel deeplinkModel,
                                                                  NotificationSettingsModel baseModel) {
    if (deeplinkModel == null || baseModel == null) {
      return false;
    }

    BaseInfo baseInfo = baseModel.getBaseInfo();
    if (baseInfo == null) {
      baseInfo = new BaseInfo();
      baseModel.setBaseInfo(baseInfo);
    }
    baseInfo.setSectionType(NotificationSectionType.APP);

    extractDeeplinkBaseInfo(deeplinkModel.getBaseInfo(), baseInfo);
    return true;
  }

  public static boolean extractDeeplinkModelIntoAppSection(DeeplinkModel deeplinkModel,
                                                            AppSectionModel baseModel) {
    if (deeplinkModel == null || baseModel == null) {
      return false;
    }

    BaseInfo baseInfo = baseModel.getBaseInfo();
    if (baseInfo == null) {
      baseInfo = new BaseInfo();
      baseModel.setBaseInfo(baseInfo);
    }
    baseInfo.setSectionType(NotificationSectionType.APP);

    extractDeeplinkBaseInfo(deeplinkModel.getBaseInfo(), baseInfo);
    return true;
  }

    public static boolean extractDeeplinkModelIntoSettingsAutoScroll(DeeplinkModel deeplinkModel,
                                                         SettingsAutoScrollModel baseModel) {
    if (deeplinkModel == null || baseModel == null) {
      return false;
    }

    BaseInfo baseInfo = baseModel.getBaseInfo();
    if (baseInfo == null) {
      baseInfo = new BaseInfo();
      baseModel.setBaseInfo(baseInfo);
    }
    baseInfo.setSectionType(NotificationSectionType.APP);

    extractDeeplinkBaseInfo(deeplinkModel.getBaseInfo(), baseInfo);
    return true;
  }

  public static boolean extractDeeplinkModelIntoLocal(DeeplinkModel deeplinkModel,
                                                   LocalNavModel baseModel) {
      if (deeplinkModel == null || baseModel == null) {
        return false;
      }

      BaseInfo baseInfo = baseModel.getBaseInfo();
      if (baseInfo == null) {
        baseInfo = new BaseInfo();
        baseModel.setBaseInfo(baseInfo);
      }
      baseInfo.setSectionType(NotificationSectionType.LOCAL_SECTION);

      extractDeeplinkBaseInfo(deeplinkModel.getBaseInfo(), baseInfo);
      return true;
  }
}
