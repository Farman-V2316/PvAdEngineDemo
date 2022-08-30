/*
 * Copyright (c) 2016 Newshunt. All rights reserved.
 */
package com.newshunt.deeplink;

import android.net.Uri;

import com.newshunt.common.helper.common.Constants;
import com.newshunt.common.helper.common.JsonUtils;
import com.newshunt.common.helper.common.Logger;
import com.newshunt.common.helper.common.UrlUtil;
import com.newshunt.common.helper.info.DebugHeaderProvider;
import com.newshunt.common.helper.info.DebugHeaderProviderKt;
import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.dataentity.notification.AdjunctLangModel;
import com.newshunt.dataentity.notification.AdsNavModel;
import com.newshunt.dataentity.notification.AppSectionModel;
import com.newshunt.dataentity.notification.BaseInfo;
import com.newshunt.dataentity.notification.BaseModel;
import com.newshunt.dataentity.notification.BooksNavModel;
import com.newshunt.dataentity.notification.ContactsRecoNavModel;
import com.newshunt.dataentity.notification.CreatePostNavModel;
import com.newshunt.dataentity.notification.ExploreNavModel;
import com.newshunt.dataentity.notification.FollowModel;
import com.newshunt.dataentity.notification.FollowNavModel;
import com.newshunt.dataentity.notification.GroupNavModel;
import com.newshunt.dataentity.notification.LangSelectionNavModel;
import com.newshunt.dataentity.notification.LiveTVNavModel;
import com.newshunt.dataentity.notification.LocalNavModel;
import com.newshunt.dataentity.notification.NavigationType;
import com.newshunt.dataentity.notification.NewsNavModel;
import com.newshunt.dataentity.notification.NotificationInboxModel;
import com.newshunt.dataentity.notification.NotificationSettingsModel;
import com.newshunt.dataentity.notification.PermissionNavModel;
import com.newshunt.dataentity.notification.ProfileNavModel;
import com.newshunt.dataentity.notification.SSONavModel;
import com.newshunt.dataentity.notification.SearchNavModel;
import com.newshunt.dataentity.notification.SettingsAutoScrollModel;
import com.newshunt.dataentity.notification.SettingsModel;
import com.newshunt.dataentity.notification.TVNavModel;
import com.newshunt.dataentity.notification.WebNavModel;
import com.newshunt.dataentity.notification.util.NotificationConstants;
import com.newshunt.dataentity.search.SearchPayloadContext;
import com.newshunt.dhutil.R;
import com.newshunt.dhutil.helper.common.DailyhuntConstants;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;
import java.util.Map;


/**
 * Helper class to parse a url
 *
 * @author maruti.borker
 */
public class DeepLinkParser {

  private static final String LANGUAGE = "l";
  private static final String COUNTRY = "c";
  private static final String NEWS_PAPER = "n";
  private static final String NEWS_PAPER_CATEGORY = "ncat";
  private static final String GALLERY_PHOTO_DEEPLINKED = "photos";

  private static final String SHARE = "share";
  private static final String SHARED_GALLERY_PHOTO = "pg";
  private static final String PARAM_SEPARATOR = "-";

  private static final String TESTPREP = "examprep";
  private static final String NEWS = "news";
  private static final String EXPLORE = "explore";
  private static final String FOLLOW = "follow";
  private static final String FOLLOWING = "following";
  private static final String FOLLOWERS = "followers";
  private static final String FEED = "feed";
  private static final String EXPLORE_PROMOTION_ID = "promotionId";
  private static final String BOOKS = "Ebooks";
  private static final String TV = "buzz";
  private static final String LIVE_TV = "livetv";
  private static final String WEB = "webItem";
  private static final String ADS = "ads";
  private static final String SSO = "sso";
  private static final String VIRAL = "viral";
  private static final String GROUP = "group";
  private static final String PROFILE = "profile";
  private static final String SEARCH = "search";
  private static final String PRESEARCH = "pre";
  private static final String CREATE_POST = "createpost";
  private static final String CONTACT_RECO = "contactsrecommendation";
  private static final String RUNTIME_PERMISSION = "permissions";
  public static final String LANG_SELECTION = "langSelection";
  public static final String LANG_UPDATE = "lang";
  public static final String LOCAL = "local";
  public static final String APP_SECTION = "appsection";
  public static final String SETTINGS = "settings";
  public static final String NOTIFICATION_INBOX = "notificationInbox";
  public static final String NOTIFICATION_SETTINGS = "notificationSettings";

  private static final String QUERY_EXPLORE_ID = "id";
  private static final String QUERY_EXPLORE_TYPE = "type";
  private static final String QUERY_EXPLORE_METHOD = "method";
  private static final String QUERY_EXPLORE_NAME = "name";
  private static final String USER_ID = "userId";

  public static BaseModel parseUrl(String deepLink) {
    if (deepLink == null || deepLink.isEmpty()) {
      return null;
    }

    if (!DeeplinkUtils.isValidHost(deepLink)) {
      return null;
    }

    if (DeeplinkUtils.isShortUrl(deepLink)) {
      return null;
    }

    if (deepLink.contains(CommonUtils.getString(R.string.host_url_dailyhunt)) ||
        deepLink.contains(CommonUtils.getString(R.string.host_url_qa_dailyhunt)) ||
        //will be removed after WAP enables '+' delimiter changes in m.dailyhunt.in
        deepLink.contains(CommonUtils.getString(R.string.host_url_web)) ||
        deepLink.contains(CommonUtils.getString(R.string.host_url_webqa)
        )) {
      return parseDeepLinkV2(deepLink);
    } else if (deepLink.contains(CommonUtils.getString(R.string.profile_dailyhunt_in))) {
      return parseProfileDomainDeeplink(deepLink);
    } else if (deepLink.contains(CommonUtils.getString(R.string.group_dailyhunt_in))) {
      return parseGroupDomainDeeplink(deepLink);
    } else {
      return parseDeeplinkV1(deepLink);
    }
  }

  private static String getEditionKeyFromCountry(String country) {
    if (Constants.COUNTRY_CODE_TO_EDITION.containsKey(country)) {
      return Constants.COUNTRY_CODE_TO_EDITION.get(country);
    }
    return country;
  }

  public static BaseModel parseDeeplinkV1(final String deepLink) {
    Uri deepLinkUri;
    try {
      deepLinkUri = Uri.parse(deepLink);
    } catch (Exception e) {
      return null;
    }
    String deepLinkHost = deepLinkUri.getHost();
    String firstPathSegment = Constants.EMPTY_STRING;
    String lastPathSegment = deepLinkUri.getLastPathSegment();
    String penulitmatePathSegment = Constants.EMPTY_STRING;


    if (!deepLinkUri.getPathSegments().isEmpty()) {
      firstPathSegment = deepLinkUri.getPathSegments().get(0);
    }

    if (deepLinkUri.getPathSegments().size() > 1) {
      penulitmatePathSegment = deepLinkUri.getPathSegments().get(
          deepLinkUri.getPathSegments().size() - 2);
    }

    BaseModel baseModel = null;
    BaseInfo baseInfo = new BaseInfo();

    if (deepLinkHost.equals(CommonUtils.getString(R.string.host_url_ebooks_com)) ||
        deepLinkHost.equals(CommonUtils.getString(R.string.host_url_books_in)) ||
        deepLinkHost.equals(CommonUtils.getString(R.string.host_url_ebooks_in)) ||
        deepLinkHost.equals(CommonUtils.getString(R.string.host_url_qa_books_dhunt_in))) {
      BooksNavModel navigationModel = new BooksNavModel();
      navigationModel.setBaseInfo(baseInfo);
      return navigationModel;
    } else if (deepLinkHost.equals(CommonUtils.getString(R.string.host_url_m_newshunt_com)) ||
        deepLinkHost.equals(CommonUtils.getString(R.string.host_url_news_net))) {

      NewsNavModel newsNavModel = new NewsNavModel();
      newsNavModel.setBaseInfo(baseInfo);
      if (lastPathSegment != null && lastPathSegment.contains(PARAM_SEPARATOR)) {
        // the last path segment will be in format of
        // c-<editonKey>-l-<language>-n-<newspaperKey>-ncat-<categoryKey>
        String[] lastPathSegmentTokens = lastPathSegment.split(PARAM_SEPARATOR);
        String childId = null;
        for (int i = 0; i < lastPathSegmentTokens.length; i += 2) {
          switch (lastPathSegmentTokens[i]) {
            case COUNTRY:
              newsNavModel.setEdition(getEditionKeyFromCountry(lastPathSegmentTokens[i + 1]));
              continue;
            case LANGUAGE:
              newsNavModel.setLanguage(lastPathSegmentTokens[i + 1]);
              continue;
            case NEWS_PAPER:
              newsNavModel.setNpKey(lastPathSegmentTokens[i + 1]);
              newsNavModel.setsType(
                  Constants.EMPTY_STRING + NavigationType.TYPE_OPEN_NEWS_LIST.getIndex());
              continue;
            case NEWS_PAPER_CATEGORY:
              newsNavModel.setCtKey(lastPathSegmentTokens[i + 1]);
              newsNavModel.setsType(
                  Constants.EMPTY_STRING + NavigationType.TYPE_OPEN_NEWS_LIST_CATEGORY.getIndex());
              continue;
            case GALLERY_PHOTO_DEEPLINKED:
              childId = lastPathSegmentTokens[i + 1];
              continue;
          }
        }
        // if its a story page then the penultimatePathSegment will have a underscore with id
        if (penulitmatePathSegment.contains("_")) {
          String newsId = penulitmatePathSegment.substring(
              penulitmatePathSegment.lastIndexOf("_") + 1);
          newsNavModel.setsType(
              Constants.EMPTY_STRING + NavigationType.TYPE_OPEN_NEWSITEM.getIndex());
          if (childId != null) {
            fillNewsIdList(newsNavModel, childId, newsId);
          } else {
            fillNewsIdList(newsNavModel, newsId);
          }
        }
      } else {
        newsNavModel.setsType(
            Constants.EMPTY_STRING + NavigationType.TYPE_OPEN_NEWS_HOME.getIndex());
      }
      BaseModel socialModel =
          DeeplinkUtils.getSocialCommentsModel(deepLink);
      baseModel = socialModel == null ? newsNavModel : socialModel;

    } else if (deepLinkHost.equals(CommonUtils.getString(R.string.host_url_dhtv_in))) {
      String GROUP = Constants.TV_BUNDLE_ASSET_GROUP;
      TVNavModel tvNavModel = new TVNavModel();
      tvNavModel.setBaseInfo(baseInfo);
      if (null == deepLinkUri.getPathSegments() || deepLinkUri.getPathSegments().size() <= 1) {
        // Open to section
        tvNavModel.setsType(
            Constants.EMPTY_STRING + NavigationType.TYPE_TV_OPEN_TO_SECTION.getIndex());
      } else {
        firstPathSegment = deepLinkUri.getPathSegments().get(1);
        lastPathSegment = deepLinkUri.getLastPathSegment();
        String unitId;
        String groupKey;
        String groupName = Constants.EMPTY_STRING;
        if (firstPathSegment.equals(Constants.TV_BUNDLE_ASSET_ITEM_TYPE_GIF) || firstPathSegment
            .equals(Constants.TV_BUNDLE_ASSET_ITEM_TYPE_IMAGE) || firstPathSegment.equals(Constants
            .TV_BUNDLE_ASSET_ITEM_TYPE_VIDEO)) {
          unitId = lastPathSegment.substring(lastPathSegment.lastIndexOf("-") + 1,
              lastPathSegment.length());

          tvNavModel.setUnitId(unitId);
          tvNavModel.setsType("" + NavigationType.TYPE_TV_OPEN_TO_DETAIL.getIndex());
        } else if (firstPathSegment.equals(GROUP)) {
          groupKey = lastPathSegment.substring(lastPathSegment.lastIndexOf("-") + 1,
              lastPathSegment.length());
          tvNavModel.setsType("" + NavigationType.TYPE_TV_OPEN_TO_GROUP_TAB.getIndex());
          tvNavModel.setGroupId(groupKey);
          if (lastPathSegment.contains("-")) {
            groupName = lastPathSegment.substring(0, lastPathSegment.indexOf("-"));
            tvNavModel.setGroupTitle(groupName);
          }
        }
      }

      BaseModel socialModel =
          DeeplinkUtils.getSocialCommentsModel(deepLink);
      baseModel = socialModel == null ? tvNavModel : socialModel;
    } else if (SHARE.equals(firstPathSegment) && (
        deepLinkHost.equals(CommonUtils.getString(R.string.host_url_newshunt_com)) ||
            deepLinkHost.equals(CommonUtils.getString(R.string.host_url_newshunt_net)
            ))) {
      NewsNavModel newsNavModel = new NewsNavModel();
      newsNavModel.setBaseInfo(baseInfo);
      newsNavModel.setsType(Constants.EMPTY_STRING + NavigationType.TYPE_OPEN_NEWSITEM.getIndex());
      //share url format of gallery photo is host/share/pg/childId/parentId
      if (isGalleryPhotoShared(deepLinkUri)) {
        fillNewsIdList(newsNavModel, penulitmatePathSegment, lastPathSegment);
      } else {
        fillNewsIdList(newsNavModel, lastPathSegment);
      }

      baseModel = newsNavModel;
    }

    if (baseModel != null) {
      DebugHeaderProvider.INSTANCE.setPartnerRef(
          deepLinkUri.getQueryParameter(DebugHeaderProviderKt.PARTNER_REF_PARAM));
    }

    return baseModel;
  }


  public static BaseModel parseDeepLinkV2(final String deepLink) {
    String firstPathSegment = Constants.EMPTY_STRING;
    Uri deepLinkUri;

    try {
      deepLinkUri = Uri.parse(deepLink);
    } catch (Exception e) {
      return null;
    }

    if (!deepLinkUri.getPathSegments().isEmpty()) {
      firstPathSegment = deepLinkUri.getPathSegments().get(0);
    }

    if (CommonUtils.isEmpty(firstPathSegment)) {
      return null;
    }

    BaseModel baseModel = null;
    switch (firstPathSegment) {
      case NEWS:
        baseModel = parseNewsDeepLinkV2(deepLink);
        break;
      case VIRAL:
        baseModel = parseViralDeepLinkV2(deepLink);
        break;
      case BOOKS:
        return new BooksNavModel();
      case TV:
        baseModel = parseTVDeepLinkV2(deepLink);
        break;
      case LIVE_TV:
        baseModel = parseLiveTVDeepLinkV2(deepLink);
        break;
      case ADS:
        baseModel = parseAdsDeepLinkV2(deepLink);
        break;
      case WEB:
        baseModel = parseWebItemDeepLinkV2(deepLink);
        break;
      case SSO:
        baseModel = parseSSODeepLinkV2(deepLink);
        break;
      case EXPLORE:
        baseModel = parseExploreModel(deepLink);
        break;
      case FOLLOW:
        baseModel = parseFollowModel(deepLink);
        break;
      case GROUP:
        baseModel = parseGroupModel(deepLink);
        break;
      case PROFILE:
        baseModel = parseProfileDeeplink(deepLink);
        break;
      case SEARCH:
        baseModel = parseSearchModel(deepLink);
        break;
      case CREATE_POST:
        baseModel = parseCreatePostModel(deepLink);
        break;
      case CONTACT_RECO:
        baseModel = parseContactRecommendation(deepLink);
        break;
      case RUNTIME_PERMISSION:
        baseModel = parseRuntimePermissionDL(deepLink);
        break;
      case LANG_SELECTION:
        baseModel = parseLangSelectionDL(deepLink);
        break;
      case LANG_UPDATE:
        baseModel = parseAdjunctLangDL(deepLink);
        break;
      case NOTIFICATION_INBOX:
        baseModel = parseNotificationInboxDL(deepLink);
        break;
      case NOTIFICATION_SETTINGS:
        baseModel = parseNotificationDeviceSettingsDL(deepLink);
        break;
      case SETTINGS:
        baseModel = parseSettingsDL(deepLink);
        break;
      case APP_SECTION:
        baseModel = parseAppSectionDL(deepLink);
        break;
      case LOCAL:
        baseModel = parseLocalPostModel(deepLink);
      default:
        break;

    }

    if(deepLink.contains(Constants.SETTING_AUTOSCROLL_PATH)) {
      baseModel = parseSettingsAutoScrollDL(deepLink);
    }

    if (baseModel != null) {
      DebugHeaderProvider.INSTANCE.setPartnerRef(
          deepLinkUri.getQueryParameter(DebugHeaderProviderKt.PARTNER_REF_PARAM));
    }
    BaseModel socialCommentsModel = DeeplinkUtils.getSocialCommentsModel(deepLink);
    return socialCommentsModel == null ? baseModel : socialCommentsModel;
  }

  private static BaseModel parseExploreModel(String deeplink) {
    String queryUrl = UrlUtil.getQueryUrl(deeplink);
    try {
      queryUrl = URLDecoder.decode(queryUrl, NotificationConstants.ENCODING);
    } catch (UnsupportedEncodingException e) {
      Logger.caughtException(e);
      return null;
    }
    if (CommonUtils.isEmpty(queryUrl)) {
      return null;
    }

    Map<String, String> urlRequestParamToMap = UrlUtil.urlRequestParamToMap(queryUrl);
    String id = urlRequestParamToMap.get(QUERY_EXPLORE_ID);
    String type = urlRequestParamToMap.get(QUERY_EXPLORE_TYPE);
    String method = urlRequestParamToMap.get(QUERY_EXPLORE_METHOD);
    String name = urlRequestParamToMap.get(QUERY_EXPLORE_NAME);

    if (CommonUtils.isEmpty(id) || CommonUtils.isEmpty(type) || CommonUtils.isEmpty(name)) {
      return null;
    }
    ExploreNavModel exploreNavModel = new ExploreNavModel(id, type, name, method);
    exploreNavModel.setsType(
        Constants.EMPTY_STRING + NavigationType.TYPE_OPEN_EXPLORE_ENTITY.getIndex());
    return exploreNavModel;
  }

  private static BaseModel parseFollowModel(String deepLink) {
    Uri deepLinkUri;
    try {
      deepLinkUri = Uri.parse(deepLink);
    } catch (Exception e) {
      return null;
    }

    String tab = null;
    String subTab = null;
    String promotionId = null;
    String userId = null;
    FollowModel followModel = null;

    List<String> pathSegments = deepLinkUri.getPathSegments();
    int pathsegmentsSize = pathSegments == null ? 0 : pathSegments.size();

    String sType = Constants.EMPTY_STRING + NavigationType.TYPE_OPEN_FOLLOW_HOME.getIndex();
    if (pathsegmentsSize > 1) {
      String navigation = pathSegments.get(1).trim();
      String subNavigation = pathsegmentsSize == 2 ? null : pathSegments.get(2).trim();

      switch (navigation) {
        case EXPLORE:
          if (subNavigation != null) {
            tab = EXPLORE;
            subTab = subNavigation;
            sType = Constants.EMPTY_STRING + NavigationType.TYPE_OPEN_EXPLORE_VIEW_TAB.getIndex();
            promotionId = deepLinkUri.getQueryParameter(EXPLORE_PROMOTION_ID);
          }
          break;
        case FOLLOWING:
          tab = FOLLOWING;
          subTab = subNavigation;
          sType = Constants.EMPTY_STRING + NavigationType.TYPE_OPEN_FOLLOWING.getIndex();
          userId = deepLinkUri.getQueryParameter(USER_ID);
          followModel = FollowModel.FOLLOWING;
          break;
        case FOLLOWERS:
          sType = Constants.EMPTY_STRING + NavigationType.TYPE_OPEN_FOLLOWERS.getIndex();
          userId = deepLinkUri.getQueryParameter(USER_ID);
          followModel = FollowModel.FOLLOWERS;
          break;
        case FEED:
          tab = FEED;
          sType = Constants.EMPTY_STRING + NavigationType.TYPE_OPEN_FOLLOWING_FEED.getIndex();
          break;
        default:
          break;
      }
    }

    FollowNavModel followNavModel =
        new FollowNavModel(tab, subTab, promotionId, userId, followModel);
    followNavModel.setsType(sType);

    return followNavModel;
  }

  private static BaseModel parseGroupModel(String deepLink) {
    Uri deepLinkUri;
    try {
      deepLinkUri = Uri.parse(deepLink);
    } catch (Exception e) {
      return null;
    }
    BaseInfo baseInfo = new BaseInfo();
    final String VIEW = "view";
    final String APPROVALS = "approvals";
    final String CREATE = "create";
    final String GROUP_ID = "groupId";
    final String INVITES = "invites";

    List<String> pathSegments = deepLinkUri.getPathSegments();
    int pathsegmentsSize = pathSegments == null ? 0 : pathSegments.size();

    String sType = Constants.EMPTY_STRING + NavigationType.TYPE_OPEN_SOCIAL_GROUP.getIndex();
    GroupNavModel groupNavModel = new GroupNavModel();
    if (pathsegmentsSize > 1) {
      String navigation = pathSegments.get(1).trim();
      String subNavigation = pathsegmentsSize == 2 ? null : pathSegments.get(2).trim();

      switch (navigation) {
        case VIEW: {
          String groupId = deepLinkUri.getQueryParameter(GROUP_ID);
          if (groupId == null) {
            return null;
          }
          groupNavModel.setGroupId(groupId);
          baseInfo.setType(VIEW);
          break;
        }
        case APPROVALS: {
          sType =
              Constants.EMPTY_STRING + NavigationType.TYPE_OPEN_SOCIAL_GROUP_APPROVAL.getIndex();
          if (subNavigation != null) {
            baseInfo.setType(subNavigation);
            groupNavModel.setSubType(subNavigation);
          }
          break;
        }
        case CREATE: {
          sType = Constants.EMPTY_STRING + NavigationType.TYPE_OPEN_SOCIAL_GROUP_CREATE.getIndex();
          break;
        }
        case INVITES: {
          String groupId = deepLinkUri.getQueryParameter(GROUP_ID);
          if (groupId == null) {
            return null;
          }
          groupNavModel.setGroupId(groupId);
          sType = Constants.EMPTY_STRING + NavigationType.TYPE_OPEN_SOCIAL_GROUP_INVITES.getIndex();
          break;
        }
      }
    }
    groupNavModel.setBaseInfo(baseInfo);
    groupNavModel.setsType(sType);

    return groupNavModel;
  }

  private static BaseModel parseSearchModel(String deepLink) {
    if (deepLink == null) {
      return null;
    }

    Uri uri = Uri.parse(deepLink);

    if (uri == null) {
      return null;

    }
    List<String> pathSegments = uri.getPathSegments();
    boolean isPresearch =
        pathSegments.size() == 2 && CommonUtils.equals(pathSegments.get(1), PRESEARCH);
    String query = uri.getQueryParameter("query");
    String hint = uri.getQueryParameter("hint");
    String payload = uri.getQueryParameter("contextMap");
    String context = uri.getQueryParameter("context");
    SearchNavModel searchNavModel =
        new SearchNavModel(query,
            JsonUtils.fromJson(payload, SearchPayloadContext.class),
            hint,
            context,
            isPresearch);
    BaseInfo baseInfo = new BaseInfo();
    baseInfo.setUniqueId(deepLink.hashCode());
    searchNavModel.setBaseInfo(baseInfo);
    searchNavModel.setsType(
        Constants.EMPTY_STRING + NavigationType.TYPE_OPEN_SEARCH_ITEM.getIndex());
    return searchNavModel;
  }

  private static BaseModel parseCreatePostModel(String deepLink) {
    if (deepLink == null) {
      return null;
    }

    Uri uri = Uri.parse(deepLink);

    if (uri == null) {
      return null;

    }
    CreatePostNavModel cpNavModel = new CreatePostNavModel(deepLink);
    BaseInfo baseInfo = new BaseInfo();
    baseInfo.setUniqueId(deepLink.hashCode());
    cpNavModel.setBaseInfo(baseInfo);
    cpNavModel.setsType(Constants.EMPTY_STRING + NavigationType.TYPE_OPEN_CREATE_POST.getIndex());

    return cpNavModel;
  }

  private static BaseModel parseLocalPostModel(String deepLink) {
    if (deepLink == null) {
      return null;
    }

    Uri uri = Uri.parse(deepLink);

    if (uri == null) {
      return null;

    }
    LocalNavModel localNavModel = new LocalNavModel(deepLink);
    BaseInfo baseInfo = new BaseInfo();
    baseInfo.setUniqueId(deepLink.hashCode());
    baseInfo.setDeeplink(deepLink);
    localNavModel.setBaseInfo(baseInfo);
    localNavModel.setsType(
        Constants.EMPTY_STRING + NavigationType.TYPE_OPEN_LOCAL_SECTION.getIndex());

    return localNavModel;
  }

  private static BaseModel parseContactRecommendation(String deepLink) {
    if (deepLink == null) {
      return null;
    }

    Uri uri = Uri.parse(deepLink);

    if (uri == null) {
      return null;

    }
    ContactsRecoNavModel cpNavModel = new ContactsRecoNavModel(deepLink);
    BaseInfo baseInfo = new BaseInfo();
    baseInfo.setUniqueId(deepLink.hashCode());
    cpNavModel.setBaseInfo(baseInfo);
    cpNavModel.setsType(Constants.EMPTY_STRING + NavigationType.TYPE_OPEN_CONTACTS_RECO.getIndex());

    return cpNavModel;
  }

  private static BaseModel parseSSODeepLinkV2(String deepLink) {
    String queryUrl = UrlUtil.getQueryUrl(deepLink);
    if (CommonUtils.isEmpty(queryUrl)) {
      return parseSSOModelWithoutQueryParams(deepLink);
    }

    Map<String, String> map = UrlUtil.urlRequestParamToMap(queryUrl);
    if (CommonUtils.isEmpty(map.get(DailyhuntConstants.BUNDLE_SSO_NAV_MODEL))) {
      return parseSSOModelWithoutQueryParams(deepLink);
    }

    try {
      String value = map.get(DailyhuntConstants.BUNDLE_SSO_NAV_MODEL);
      value = URLDecoder.decode(value, Constants.TEXT_ENCODING_UTF_8);
      return JsonUtils.fromJson(value, SSONavModel.class);
    } catch (Exception e) {
      Logger.caughtException(e);
    }
    return parseSSOModelWithoutQueryParams(deepLink);
  }

  private static BaseModel parseSSOModelWithoutQueryParams(String deepLink) {
    Uri deepLinkUri;
    try {
      deepLinkUri = Uri.parse(deepLink);
    } catch (Exception e) {
      return null;
    }

    int pathSegmentSize = deepLinkUri.getPathSegments().size();
    SSONavModel ssoNavModel = new SSONavModel();
    if (pathSegmentSize == 1) {
      ssoNavModel.setsType(Constants.EMPTY_STRING + NavigationType.TYPE_OPEN_SSO.getIndex());
    } else {
      ssoNavModel.setsType(Constants.EMPTY_STRING + NavigationType.TYPE_OPEN_DEFAULT.getIndex());
    }
    return ssoNavModel;
  }

  // Ex.
  // http://m.dailyhunt.in/web/webItem?webModel={"id":"12345","disableActionBarMenu":false,
  // "finishOnBackPress":false,"actionBarTitleTextColor":"#000000","actionBarBackgroundColor":
  // "#ffffff","title":"Tarot Card Reading","url":"https%3A%2F%2Fwww.google.co.in%2F%3Fsearch
  // %3Danshul%26gws_rd%3Dcr%26ei%3DAOO_WKy7MYuGvQS4v4WABw"}
  public static BaseModel parseWebItemDeepLinkV2(final String deepLinkUrl) {
    String queryUrl = UrlUtil.getQueryUrl(deepLinkUrl);
    if (CommonUtils.isEmpty(queryUrl)) {
      return null;
    }
    Map<String, String> map = UrlUtil.urlRequestParamToMap(queryUrl);
    if (map == null || CommonUtils.isEmpty(map.get(DailyhuntConstants.BUNDLE_WEB_NAV_MODEL))) {
      return null;
    }

    try {
      String value = map.get(DailyhuntConstants.BUNDLE_WEB_NAV_MODEL);
      value = URLDecoder.decode(value, Constants.TEXT_ENCODING_UTF_8);
      return JsonUtils.fromJson(value, WebNavModel.class);
    } catch (Exception e) {
      Logger.caughtException(e);
    }
    return null;
  }


  public static BaseModel parseViralDeepLinkV2(final String deepLink) {
    NewsNavModel viralNavModel = new NewsNavModel();
    BaseInfo baseInfo = new BaseInfo();
    viralNavModel.setBaseInfo(baseInfo);
    try {
      Uri deepLinkUri = Uri.parse(deepLink);
      final String TYPE_TOPIC = "topics";
      final String TYPE_VIRAL_DETAIL = "viralid";
      final List<String> pathSegments = deepLinkUri.getPathSegments();
      final int pathSegmentSize = pathSegments.size();
      //we are deep linking news url if the pathSegmentSize is equal to 1 or greater than or
      // equal to 4
      //Ex: http://m.dailyhunt.in/  -- No Deep link
      //Ex: http://m.dailyhunt.in/news/<Country>/<Language> -- No Deep link

      //Ex: http://m.dailyhunt.in/news or http://m.dailyhunt.in/news/
      if (pathSegmentSize == 1) {
        viralNavModel.setsType(
            Constants.EMPTY_STRING + NavigationType.TYPE_OPEN_NEWS_HOME.getIndex());
      }

      //Extracting language and edition
      if (pathSegmentSize > 2) {
        String edition = deepLinkUri.getPathSegments().get(1);
        baseInfo.setEdition(edition);
        String language = deepLinkUri.getPathSegments().get(2);
        baseInfo.setLanguage(language);
      }

      for (int i = 3; i < pathSegmentSize; i++) {
        DeepLinkPathSegmentParser deepLinkPathSegmentParser = new DeepLinkPathSegmentParser();
        deepLinkPathSegmentParser.parsePathData(deepLinkUri.getPathSegments().get(i));

        switch (deepLinkPathSegmentParser.getKey()) {
          case TYPE_TOPIC:
            viralNavModel.setsType(
                Constants.EMPTY_STRING + NavigationType.TYPE_OPEN_VIRAL_TOPIC.getIndex());
            String topicKey = deepLinkPathSegmentParser.getValue();
            viralNavModel.setTopicKey(topicKey);
            break;
          case TYPE_VIRAL_DETAIL:
            viralNavModel.setsType(
                Constants.EMPTY_STRING + NavigationType.TYPE_OPEN_VIRAL_ITEM.getIndex());
            String viralItemId = deepLinkPathSegmentParser.getValue();
            viralNavModel.setViralId(viralItemId);
            viralNavModel.setNewsId(viralItemId);
            String viralItemLanguage = deepLinkPathSegmentParser.getLanguage();
            viralNavModel.setLanguage(viralItemLanguage);
            break;
          default:
            viralNavModel.setsType(
                Constants.EMPTY_STRING + NavigationType.TYPE_OPEN_TOPIC.getIndex());
        }
      }
    } catch (Exception e) {
      // In case any exception just open to home page
      viralNavModel.setsType(
          Constants.EMPTY_STRING + NavigationType.TYPE_OPEN_DEFAULT.getIndex());
    }
    return viralNavModel;
  }

  public static BaseModel parseNewsDeepLinkV2(final String deepLink) {

    NewsNavModel newsNavModel = new NewsNavModel();
    BaseInfo baseInfo = new BaseInfo();
    baseInfo.setDeeplink(deepLink);
    newsNavModel.setBaseInfo(baseInfo);
    try {
      Uri deepLinkUri = Uri.parse(deepLink);
      final String TYPE_NEWSPAPER = "epaper";
      final String TYPE_CATEGORY = "updates";
      final String TYPE_STORY = "newsid";
      final String TYPE_GALLERY_PHOTO = "photos";
      final String TYPE_TOPIC = "topics";
      final String TYPE_SUB_TOPIC = "subtopics";
      final String TYPE_LOCATION = "location";
      final String TYPE_LOCATION_ALL = "locations";
      final String TYPE_NEWSPAPERS_ALL = "newspapers";
      final String TYPE_SUB_LOCATION = "city";
      final String TYPE_SIMILAR_STORIES = "morestories";
      final String TYPE_GROUPS_ALL = "groups";
      final List<String> pathSegments = deepLinkUri.getPathSegments();
      final int pathSegmentSize = pathSegments.size();
      //we are deep linking news url if the pathSegmentSize is equal to 1 or greater than or
      // equal to 4
      //Ex: http://m.dailyhunt.in/  -- No Deep link
      //Ex: http://m.dailyhunt.in/news/<Country>/<Language> -- No Deep link

      //Ex: http://m.dailyhunt.in/news or http://m.dailyhunt.in/news/
      if (pathSegmentSize == 1) {
        newsNavModel.setsType(
            Constants.EMPTY_STRING + NavigationType.TYPE_OPEN_NEWS_HOME.getIndex());
      }

      //Extracting language and edition
      if (pathSegmentSize > 2) {
        String edition = deepLinkUri.getPathSegments().get(1);
        baseInfo.setEdition(edition);
        String language = deepLinkUri.getPathSegments().get(2);
        baseInfo.setLanguage(language);
      }

      if (pathSegmentSize == 5 && pathSegments.get(3).equals(TYPE_SIMILAR_STORIES)) {
        String[] grpInfo = pathSegments.get(4).split("-");
        newsNavModel.setsType(String.valueOf(NavigationType.TYPE_OPEN_SIMILAR_STORIES.getIndex()));
        newsNavModel.setGroupId(grpInfo[1]);
        return newsNavModel;
      }

      for (int i = 3; i < pathSegmentSize; i++) {
        DeepLinkPathSegmentParser deepLinkPathSegmentParser = new DeepLinkPathSegmentParser();
        deepLinkPathSegmentParser.parsePathData(deepLinkUri.getPathSegments().get(i));

        switch (deepLinkPathSegmentParser.getKey()) {
          case TYPE_STORY:
            //open to Story Page
            newsNavModel.setsType(
                Constants.EMPTY_STRING + NavigationType.TYPE_OPEN_NEWSITEM.getIndex());
            newsNavModel.setNewsId(deepLinkPathSegmentParser.getValue());
            break;

          case TYPE_NEWSPAPER:
            //open to newspaper
            newsNavModel.setsType(
                Constants.EMPTY_STRING + NavigationType.TYPE_OPEN_NEWS_LIST.getIndex());
            newsNavModel.setEntityType(TYPE_NEWSPAPER);
            newsNavModel.setNpKey(deepLinkPathSegmentParser.getValue());
            break;

          case TYPE_CATEGORY:
            //open to category in a newspaper
            if (CommonUtils.isEmpty(newsNavModel.getNpKey())) {
              //No NP key, but category key present, throw exception and catch it and
              // set to TYPE_OPEN_DEFAULT
              throw new Exception();
            }
            newsNavModel.setsType(Constants.EMPTY_STRING + NavigationType
                .TYPE_OPEN_NEWS_LIST_CATEGORY.getIndex());
            newsNavModel.setCtKey(deepLinkPathSegmentParser.getValue());
            break;

          case TYPE_GALLERY_PHOTO:
            newsNavModel.setsType(
                Constants.EMPTY_STRING + NavigationType.TYPE_OPEN_NEWSITEM.getIndex());
            String childId = deepLinkPathSegmentParser.getValue();
            //parsing parent article id
            deepLinkPathSegmentParser.parsePathData(deepLinkUri.getPathSegments().get(i), 1);
            String parentId = deepLinkPathSegmentParser.getValue();
            if (parentId == null) {
              fillNewsIdList(newsNavModel, childId);
            } else {
              fillNewsIdList(newsNavModel, childId, parentId);
            }
            break;

          case TYPE_TOPIC:
            newsNavModel.setsType(
                Constants.EMPTY_STRING + NavigationType.TYPE_OPEN_TOPIC.getIndex());
            newsNavModel.setEntityType(TYPE_TOPIC);
            String topicKey = deepLinkPathSegmentParser.getValue();
            newsNavModel.setTopicKey(topicKey);
            break;

          case TYPE_SUB_TOPIC:
            if (CommonUtils.isEmpty(newsNavModel.getTopicKey())) {
              //No topic key, but subtopic key present, throw exception and catch it and set to
              // TYPE_OPEN_DEFAULT
              throw new Exception();
            }
            newsNavModel.setsType(
                Constants.EMPTY_STRING + NavigationType.TYPE_OPEN_TOPIC.getIndex());
            String subTopicKey = deepLinkPathSegmentParser.getValue();
            newsNavModel.setSubTopicKey(subTopicKey);
            break;

          case TYPE_LOCATION:
            newsNavModel.setsType(
                Constants.EMPTY_STRING + NavigationType.TYPE_OPEN_LOCATION.getIndex());
            newsNavModel.setEntityType(TYPE_LOCATION);
            String locationKey = deepLinkPathSegmentParser.getValue();
            newsNavModel.setLocationKey(locationKey);
            break;

          case TYPE_SUB_LOCATION:
            if (CommonUtils.isEmpty(newsNavModel.getLocationKey())) {
              //No Location key, but subLocation key present, throw exception and catch it and
              // set to TYPE_OPEN_DEFAULT
              throw new Exception();
            }
            newsNavModel.setsType(
                Constants.EMPTY_STRING + NavigationType.TYPE_OPEN_LOCATION.getIndex());
            String city = deepLinkPathSegmentParser.getValue();
            newsNavModel.setSubLocationKey(city);
            break;

          case TYPE_GROUPS_ALL:
            String groupKey = deepLinkPathSegmentParser.getValue();
            newsNavModel.setsType(
                Constants.EMPTY_STRING + NavigationType.TYPE_OPEN_NP_GROUP_LIST.getIndex());
            newsNavModel.setGroupKey(groupKey);
            break;

          default:
            String key = deepLinkUri.getPathSegments().get(i);
            if (TYPE_TOPIC.equals(key)) {
              newsNavModel.setsType(
                  Constants.EMPTY_STRING + NavigationType.TYPE_OPEN_TOPIC_LIST.getIndex());
            } else if (TYPE_LOCATION_ALL.equals(key)) {
              newsNavModel.setsType(
                  Constants.EMPTY_STRING + NavigationType.TYPE_OPEN_LOCATION_LIST.getIndex());
            } else if (TYPE_NEWSPAPERS_ALL.equals(key)) {
              newsNavModel.setsType(
                  Constants.EMPTY_STRING + NavigationType.TYPE_OPEN_FOLLOW_EXPLORE_TAB.getIndex());
            } else {
              newsNavModel.setsType(
                  Constants.EMPTY_STRING + NavigationType.TYPE_OPEN_DEFAULT.getIndex());
            }
        }
      }
    } catch (Exception e) {
      // In case any exception just open to home page
      newsNavModel.setsType(
          Constants.EMPTY_STRING + NavigationType.TYPE_OPEN_DEFAULT.getIndex());
    }

    return newsNavModel;
  }

  public static BaseModel parseTVDeepLinkV2(final String deepLink) {

    Uri deepLinkUri;

    try {
      deepLinkUri = Uri.parse(deepLink);
    } catch (Exception e) {
      return null;
    }
    String firstPathSegment = Constants.EMPTY_STRING;
    String lastPathSegment = Constants.EMPTY_STRING;
    String GROUP = Constants.TV_BUNDLE_ASSET_GROUP;
    final String VIDEOS_TAB = "VDO";
    final String PLAYLIST_TAB = "PL";
    final String CATEGORY_TAB = "CAT";
    final String SHOWS_TAB = "SHOW";
    final String DHTV_HANDLER = "handler=dailyTV";
    final String DHTV_DAILY_TV_DISP_TEXT = "name";
    final String DHTV_DAILY_TV_CHANNEL_ID = "dailytvId";
    final String DHTV_DAILY_TV_TAG_ID = "dailytvTagId";
    final String DHTV_DAILY_TV_SPL_ID = "dailytvSplId";


    TVNavModel tvNavModel = new TVNavModel();
    tvNavModel.setBaseInfo(new BaseInfo());
    if (null == deepLinkUri.getPathSegments() || deepLinkUri.getPathSegments().size() <= 1) {
      // Open to section
      tvNavModel.setsType(
          Constants.EMPTY_STRING + NavigationType.TYPE_TV_OPEN_TO_SECTION.getIndex());
      if (deepLink.contains(DHTV_HANDLER)) {
        tvNavModel.setsType(
            Constants.EMPTY_STRING + NavigationType.TYPE_DH_TV_OPEN_TO_SECTION.getIndex());
      }
    } else {
      firstPathSegment = deepLinkUri.getPathSegments().get(1);
      lastPathSegment = deepLinkUri.getLastPathSegment();
      String unitId = null;
      String tagId = null;
      String splId = null;
      String channelId = null;
      String displayText = Constants.EMPTY_STRING;
      String groupKey;
      String groupName;
      String[] penulitmatePathSegmentTokens = null;
      String penulitmatePathSegment;
      if (firstPathSegment.equals(Constants.TV_BUNDLE_ASSET_ITEM_TYPE_GIF) || firstPathSegment
          .equals(Constants.TV_BUNDLE_ASSET_ITEM_TYPE_IMAGE) || firstPathSegment.equals(Constants
          .TV_BUNDLE_ASSET_ITEM_TYPE_VIDEO)) {
        unitId = lastPathSegment.substring(lastPathSegment.lastIndexOf("-") + 1,
            lastPathSegment.length());

        tvNavModel.setUnitId(unitId);
        tvNavModel.setsType(
            Constants.EMPTY_STRING + NavigationType.TYPE_TV_OPEN_TO_DETAIL.getIndex());
        if (deepLink.contains(DHTV_HANDLER)) {
          tvNavModel.setsType(
              Constants.EMPTY_STRING + NavigationType.TYPE_DH_TV_OPEN_TO_DETAIL.getIndex());
        }
      } else if (firstPathSegment.equals(GROUP)) {
        groupKey = lastPathSegment.substring(lastPathSegment.lastIndexOf("-") + 1,
            lastPathSegment.length());
        tvNavModel.setsType(
            Constants.EMPTY_STRING + NavigationType.TYPE_TV_OPEN_TO_GROUP_TAB.getIndex());
        tvNavModel.setGroupId(groupKey);
        if (lastPathSegment.contains("-")) {
          groupName = lastPathSegment.substring(0, lastPathSegment.indexOf("-"));
          tvNavModel.setGroupTitle(groupName);
        }
        tvNavModel.setModelType(GROUP);
      } else if (firstPathSegment.equals(Constants.TV_BUNDLE_CHANNEL) || firstPathSegment.equals
          (Constants.TV_BUNDLE_TAG)) {
        if (deepLink.contains(DHTV_HANDLER)) {

          String queryUrl = UrlUtil.getQueryUrl(deepLink);
          try {
            queryUrl = URLDecoder.decode(queryUrl, NotificationConstants.ENCODING);
          } catch (UnsupportedEncodingException e) {
            Logger.caughtException(e);
          }
          if (!CommonUtils.isEmpty(queryUrl)) {
            Map<String, String> urlRequestParamToMap = UrlUtil.urlRequestParamToMap(queryUrl);

            displayText = urlRequestParamToMap.get(DHTV_DAILY_TV_DISP_TEXT);

            tvNavModel.setDisplayText(displayText);
            for (Map.Entry<String, String> entry : urlRequestParamToMap.entrySet()) {
              if (entry.getKey().equals(DHTV_DAILY_TV_CHANNEL_ID)) {
                unitId = urlRequestParamToMap.get(DHTV_DAILY_TV_CHANNEL_ID);
                tvNavModel.setsType(
                    Constants.EMPTY_STRING + NavigationType.TYPE_DH_TV_OPEN_TO_CHANNEL.getIndex());
              } else if (entry.getKey().equals(DHTV_DAILY_TV_SPL_ID)) {
                unitId = urlRequestParamToMap.get(DHTV_DAILY_TV_SPL_ID);
                tvNavModel.setsType(
                    Constants.EMPTY_STRING + NavigationType.TYPE_DH_TV_OPEN_TO_SPL.getIndex());
              } else if (entry.getKey().equals(DHTV_DAILY_TV_TAG_ID)) {
                unitId = urlRequestParamToMap.get(DHTV_DAILY_TV_TAG_ID);
                tvNavModel.setsType(
                    Constants.EMPTY_STRING + NavigationType.TYPE_DH_TV_OPEN_TO_TAG.getIndex());
              }
            }
          }

        } else {
          tvNavModel.setsType(Integer.toString(NavigationType.TYPE_TV_OPEN_TO_CHANNEL.getIndex()));

          if (lastPathSegment.endsWith(VIDEOS_TAB) || lastPathSegment.endsWith(PLAYLIST_TAB) ||
              lastPathSegment.endsWith(CATEGORY_TAB) || lastPathSegment.endsWith(SHOWS_TAB)) {
            if (deepLinkUri.getPathSegments().size() > 3) {
              penulitmatePathSegment =
                  deepLinkUri.getPathSegments().get(deepLinkUri.getPathSegments().size() - 2);
              penulitmatePathSegmentTokens = penulitmatePathSegment.split(PARAM_SEPARATOR);
              if (!CommonUtils.isEmpty(penulitmatePathSegmentTokens) &&
                  penulitmatePathSegmentTokens.length >= 2) {
                unitId = penulitmatePathSegmentTokens[penulitmatePathSegmentTokens.length - 1];
                tvNavModel.setTvItemLanguage
                    (penulitmatePathSegmentTokens[penulitmatePathSegmentTokens.length - 2]);
              }
              tvNavModel.setGroupId(lastPathSegment.substring(lastPathSegment.lastIndexOf("-") + 1,
                  lastPathSegment.length()));
            }
          } else if (lastPathSegment.contains("-")) {
            unitId = lastPathSegment.substring(0, lastPathSegment.indexOf("-"));
            tvNavModel.setUnitId(unitId);
          }
        }
        tvNavModel.setModelType(Constants.TV_BUNDLE_CHANNEL);
      } else if (firstPathSegment.equals(Constants.TV_BUNDLE_PLAYLIST)) {
        unitId = lastPathSegment.substring(lastPathSegment.lastIndexOf("-") + 1,
            lastPathSegment.length());
        tvNavModel.setsType(Integer.toString(NavigationType.TYPE_TV_OPEN_TO_PLAYLIST.getIndex()));
        tvNavModel.setUnitId(unitId);
        if (lastPathSegment.contains("-")) {
          groupName = lastPathSegment.substring(0, lastPathSegment.indexOf("-"));
          tvNavModel.setGroupTitle(groupName);
        }
      } else if (firstPathSegment.equals(Constants.TV_BUNDLE_SHOW)) {
        tvNavModel.setsType(Integer.toString(NavigationType.TYPE_TV_OPEN_TO_SHOW.getIndex()));
        if (deepLinkUri.getPathSegments().size() > 3) {
          penulitmatePathSegment =
              deepLinkUri.getPathSegments().get(deepLinkUri.getPathSegments().size() - 1);
          penulitmatePathSegmentTokens = penulitmatePathSegment.split(PARAM_SEPARATOR);
          if (!CommonUtils.isEmpty(penulitmatePathSegmentTokens) &&
              penulitmatePathSegmentTokens.length >= 2) {
            unitId = penulitmatePathSegmentTokens[penulitmatePathSegmentTokens.length - 1];
            tvNavModel.setTvItemLanguage
                (penulitmatePathSegmentTokens[penulitmatePathSegmentTokens.length - 2]);
            if (penulitmatePathSegmentTokens.length > 3) {
              tvNavModel.setGroupId(
                  penulitmatePathSegmentTokens[penulitmatePathSegmentTokens.length - 4]);
            }
          }
        } else if (lastPathSegment != null && lastPathSegment.contains("-")) {
          unitId = lastPathSegment.substring(0, lastPathSegment.indexOf("-"));
          tvNavModel.setUnitId(unitId);
        }
      }
    }
    return tvNavModel;
  }

  public static BaseModel parseLiveTVDeepLinkV2(final String deepLink) {

    Uri deepLinkUri;

    try {
      deepLinkUri = Uri.parse(deepLink);
    } catch (Exception e) {
      return null;
    }
    String secondPathSegment = Constants.EMPTY_STRING;
    String lastPathSegment = Constants.EMPTY_STRING;
    LiveTVNavModel liveTVNavModel = new LiveTVNavModel();
    liveTVNavModel.setBaseInfo(new BaseInfo());
    if (null == deepLinkUri.getPathSegments() || deepLinkUri.getPathSegments().size() <= 1) {
      // Open to section
      liveTVNavModel.setsType(
          Constants.EMPTY_STRING + NavigationType.TYPE_OPEN_LIVETV_SECTION.getIndex());
    } else {
      secondPathSegment = deepLinkUri.getPathSegments().get(1);
      lastPathSegment = deepLinkUri.getLastPathSegment();

      if (secondPathSegment.equals(GROUP)) {
        String groupKey = lastPathSegment.substring(lastPathSegment.lastIndexOf("-") + 1,
            lastPathSegment.length());
        if (lastPathSegment.contains("-")) {
          String groupName = lastPathSegment.substring(0, lastPathSegment.indexOf("-"));
          liveTVNavModel.setGroupTitle(groupName);
        }
        liveTVNavModel.setGroupId(groupKey);
        liveTVNavModel.setsType(
            Constants.EMPTY_STRING + NavigationType.TYPE_OPEN_LIVETV_GROUP_TAB.getIndex());
      } else if (secondPathSegment.equals(Constants.TV_BUNDLE_ASSET_ITEM_TYPE_VIDEO)) {
        String[] parts = lastPathSegment.split("-");

        if (!CommonUtils.isEmpty(parts)) {
          String groupKey = null;
          String unitId = null;
          String itemLanguage = null;
          if (parts.length > 2) {
            groupKey = parts[parts.length - 3];
            itemLanguage = parts[parts.length - 2];
            unitId = parts[parts.length - 1];
          } else if (parts.length > 1) {
            itemLanguage = parts[parts.length - 2];
            unitId = parts[parts.length - 1];
          } else if (parts.length > 0) {
            unitId = parts[parts.length - 1];
          }
          liveTVNavModel.setUnitId(unitId);
          liveTVNavModel.setGroupId(groupKey);
          liveTVNavModel.setTvItemLanguage(itemLanguage);
        }
        liveTVNavModel.setsType(
            Constants.EMPTY_STRING + NavigationType.TYPE_OPEN_LIVETV_ITEM.getIndex());
      } else {
        liveTVNavModel.setGroupTitle(Constants.TV_BUNDLE_LIVE_TV);
        liveTVNavModel.setsType(
            Constants.EMPTY_STRING + NavigationType.TYPE_OPEN_LIVETV_SECTION.getIndex());
      }
    }
    return liveTVNavModel;
  }


  public static BaseModel parseAdsDeepLinkV2(final String deepLink) {
    Uri deepLinkUri;
    try {
      deepLinkUri = Uri.parse(deepLink);
    } catch (Exception e) {
      return null;
    }
    AdsNavModel adsNavModel = new AdsNavModel();
    adsNavModel.setsType(
        Constants.EMPTY_STRING + NavigationType.TYPE_OPEN_DEFAULT.getIndex());
    return adsNavModel;
  }

  /*
   *  share url format of gallery photo is
   *  host/share/pg/childId/parentId - so size of pathsegments must be 4
   */
  private static boolean isGalleryPhotoShared(Uri uri) {
    boolean shared = false;
    List<String> pathSegments = uri.getPathSegments();
    if (pathSegments != null && pathSegments.size() == 4 &&
        SHARED_GALLERY_PHOTO.equalsIgnoreCase(pathSegments.get(1))) {
      shared = true;
    }
    return shared;
  }

  private static void fillNewsIdList(NewsNavModel newsNavModel, String... newsIds) {
    if (newsNavModel == null || newsIds == null || newsIds.length == 0) {
      return;
    }
    newsNavModel.setNewsId(newsIds[0]);
    if (newsIds.length > 1) {
      newsNavModel.setParentNewsId(newsIds[1]);
    }
  }

  private static GroupNavModel parseGroupDomainDeeplink(String deeplink) {
    final int PROFILE_DOMAIN_DEEPLINK_SEGMENTS = 1;
    final int USER_HANDLE_URL_SEGMENT_POSITION = 0;
    if (CommonUtils.isEmpty(deeplink)) {
      return null;
    }
    final Uri uri = Uri.parse(deeplink);
    if (uri == null || uri.getPathSegments().size() < PROFILE_DOMAIN_DEEPLINK_SEGMENTS) {
      return null;
    }

    final GroupNavModel groupNavModel = new GroupNavModel();
    final BaseInfo baseInfo = new BaseInfo();
    groupNavModel.setBaseInfo(baseInfo);
    groupNavModel.setsType(
        Constants.EMPTY_STRING + NavigationType.TYPE_OPEN_SOCIAL_GROUP.getIndex());
    groupNavModel.setHandle(uri.getPathSegments().get(USER_HANDLE_URL_SEGMENT_POSITION));
    return groupNavModel;
  }

  private static ProfileNavModel parseProfileDeeplink(String deeplink) {
    if (deeplink == null) {
      return null;
    }
    Uri uri = Uri.parse(deeplink);
    if (uri == null) {
      return null;
    }

    ProfileNavModel profileNavModel = new ProfileNavModel();
    BaseInfo baseInfo = new BaseInfo();
    profileNavModel.setBaseInfo(baseInfo);

    profileNavModel.setsType(Constants.EMPTY_STRING + NavigationType.TYPE_OPEN_PROFILE.getIndex());
    profileNavModel.setUserId(uri.getQueryParameter(Constants.QUERY_USERID));
    profileNavModel.setTabType(uri.getQueryParameter(Constants.QUERY_TAB_TYPE));
    profileNavModel.setDefaultTabId(uri.getQueryParameter(Constants.QUERY_DEFAULT_TAB_ID));

    return profileNavModel;
  }

  private static ProfileNavModel parseProfileDomainDeeplink(String deeplink) {
    if (deeplink == null) {
      return null;
    }

    Uri uri = Uri.parse(deeplink);
    if (uri == null) {
      return null;
    }

    if (uri.getPathSegments().size() < 1) {
      return null;
    }

    ProfileNavModel profileNavModel = new ProfileNavModel();
    BaseInfo baseInfo = new BaseInfo();
    profileNavModel.setBaseInfo(baseInfo);

    profileNavModel.setsType(Constants.EMPTY_STRING + NavigationType.TYPE_OPEN_PROFILE.getIndex());
    profileNavModel.setTabType(uri.getQueryParameter(Constants.QUERY_TAB_TYPE));
    profileNavModel.setUserHandle(uri.getPathSegments().get(0));

    return profileNavModel;
  }

  private static PermissionNavModel parseRuntimePermissionDL(String deepLink) {
    String queryUrl = UrlUtil.getQueryUrl(deepLink);
    Map<String, String> map = UrlUtil.urlRequestParamToMap(queryUrl);
    try {
      String value = map.get(DailyhuntConstants.PERMISSION_NAV_MODEL);
      value = URLDecoder.decode(value, Constants.TEXT_ENCODING_UTF_8);
      return JsonUtils.fromJson(value, PermissionNavModel.class);
    } catch (Exception e) {
      Logger.caughtException(e);
    }
    return null;
  }

  private static BaseModel parseLangSelectionDL(String deepLink) {
    if (deepLink == null) {
      return null;
    }

    Uri uri = Uri.parse(deepLink);

    if (uri == null) {
      return null;

    }
    LangSelectionNavModel langSelectionNavModel = new LangSelectionNavModel(deepLink);
    BaseInfo baseInfo = new BaseInfo();
    baseInfo.setUniqueId(deepLink.hashCode());
    langSelectionNavModel.setBaseInfo(baseInfo);

    langSelectionNavModel.setsType(
        Constants.EMPTY_STRING + NavigationType.TYPE_OPEN_LANG_SELECTION.getIndex());
    return langSelectionNavModel;
  }

  private static BaseModel parseAdjunctLangDL(String deepLink) {
    if (deepLink == null) {
      return null;
    }

    Uri uri = Uri.parse(deepLink);

    if (uri == null) {
      return null;

    }
    AdjunctLangModel adjunctLangModel = new AdjunctLangModel(deepLink);
    BaseInfo baseInfo = new BaseInfo();
    baseInfo.setUniqueId(deepLink.hashCode());
    adjunctLangModel.setBaseInfo(baseInfo);

    adjunctLangModel.setsType(
        Constants.EMPTY_STRING + NavigationType.TYPE_HANDLE_ADJUNCT_LANG.getIndex());
    return adjunctLangModel;
  }

  private static BaseModel parseAppSectionDL(String deepLink) {
    if (deepLink == null) {
      return null;
    }

    Uri uri = Uri.parse(deepLink);

    if (uri == null) {
      return null;

    }
    AppSectionModel appSectionModel = new AppSectionModel(deepLink);
    BaseInfo baseInfo = new BaseInfo();
    baseInfo.setUniqueId(deepLink.hashCode());
    appSectionModel.setBaseInfo(baseInfo);

    appSectionModel.setsType(
            Constants.EMPTY_STRING + NavigationType.TYPE_HANDLE_APP_SECTION.getIndex());

    List<String> pathSegments = uri.getPathSegments();
    if(pathSegments.size() == 4) {
      String sectionId = pathSegments.get(3);
      appSectionModel.setAppSectionId(sectionId);
    }
    return appSectionModel;
  }

  private static BaseModel parseSettingsDL(String deepLink) {
    if (deepLink == null) {
      return null;
    }

    Uri uri = Uri.parse(deepLink);

    if (uri == null) {
      return null;

    }
    SettingsModel settingsModel = new SettingsModel(deepLink);
    BaseInfo baseInfo = new BaseInfo();
    baseInfo.setUniqueId(deepLink.hashCode());
    settingsModel.setBaseInfo(baseInfo);

    settingsModel.setsType(
            Constants.EMPTY_STRING + NavigationType.TYPE_SETTINGS.getIndex());

    List<String> pathSegments = uri.getPathSegments();
    if(pathSegments.size() == 2) {
      String path = pathSegments.get(1);
      settingsModel.setSettingsSection(path);
    }
    return settingsModel;
  }

  private static BaseModel parseSettingsAutoScrollDL(String deepLink) {
    if (deepLink == null) {
      return null;
    }
    String path = deepLink.replace("settings#","");
    Uri uri = Uri.parse(path);

    if (uri == null) {
      return null;

    }

    List<String> pathSegments = uri.getPathSegments();
    SettingsAutoScrollModel settingsAutoScrollModel = new SettingsAutoScrollModel(deepLink);
    BaseInfo baseInfo = new BaseInfo();
    baseInfo.setUniqueId(deepLink.hashCode());
    settingsAutoScrollModel.setBaseInfo(baseInfo);

    settingsAutoScrollModel.setsType(
            Constants.EMPTY_STRING + NavigationType.TYPE_SETTINGS_AUTOSCROLL.getIndex());
    settingsAutoScrollModel.setScrollToPosition(pathSegments.get(0));
    return settingsAutoScrollModel;
  }

  private static BaseModel parseNotificationInboxDL(String deepLink) {
    if (deepLink == null) {
      return null;
    }

    Uri uri = Uri.parse(deepLink);

    if (uri == null) {
      return null;

    }
    NotificationInboxModel notificationInboxModel = new NotificationInboxModel(deepLink);
    BaseInfo baseInfo = new BaseInfo();
    baseInfo.setUniqueId(deepLink.hashCode());
    notificationInboxModel.setBaseInfo(baseInfo);

    notificationInboxModel.setsType(
            Constants.EMPTY_STRING + NavigationType.TYPE_NOTIFICATION_INBOX.getIndex());
    return notificationInboxModel;
  }

  private static BaseModel parseNotificationDeviceSettingsDL(String deepLink) {
    if (deepLink == null) {
      return null;
    }

    Uri uri = Uri.parse(deepLink);

    if (uri == null) {
      return null;

    }
    NotificationSettingsModel notificationSettingsModel = new NotificationSettingsModel(deepLink);
    BaseInfo baseInfo = new BaseInfo();
    baseInfo.setUniqueId(deepLink.hashCode());
    notificationSettingsModel.setBaseInfo(baseInfo);

    notificationSettingsModel.setsType(
            Constants.EMPTY_STRING + NavigationType.TYPE_NOTIFICATION_SETTINGS.getIndex());
    return notificationSettingsModel;
  }

}
