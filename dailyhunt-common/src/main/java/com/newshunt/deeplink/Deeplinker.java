/*
 * Copyright (c) 2016 Newshunt. All rights reserved.
 */
package com.newshunt.deeplink;

import android.os.AsyncTask;

import com.newshunt.common.helper.common.BusProvider;
import com.newshunt.common.helper.common.Constants;
import com.newshunt.common.helper.common.UrlUtil;
import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.dataentity.notification.AdjunctLangModel;
import com.newshunt.dataentity.notification.AdsNavModel;
import com.newshunt.dataentity.notification.AppSectionModel;
import com.newshunt.dataentity.notification.BaseInfo;
import com.newshunt.dataentity.notification.BaseModel;
import com.newshunt.dataentity.notification.BaseModelType;
import com.newshunt.dataentity.notification.ContactsRecoNavModel;
import com.newshunt.dataentity.notification.CreatePostNavModel;
import com.newshunt.dataentity.notification.DeeplinkModel;
import com.newshunt.dataentity.notification.DeeplinkResponse;
import com.newshunt.dataentity.notification.ExploreNavModel;
import com.newshunt.dataentity.notification.FollowNavModel;
import com.newshunt.dataentity.notification.GroupNavModel;
import com.newshunt.dataentity.notification.LangSelectionNavModel;
import com.newshunt.dataentity.notification.LiveTVNavModel;
import com.newshunt.dataentity.notification.LocalNavModel;
import com.newshunt.dataentity.notification.NavigationModel;
import com.newshunt.dataentity.notification.NewsNavModel;
import com.newshunt.dataentity.notification.NotificationInboxModel;
import com.newshunt.dataentity.notification.NotificationSettingsModel;
import com.newshunt.dataentity.notification.PermissionNavModel;
import com.newshunt.dataentity.notification.ProfileNavModel;
import com.newshunt.dataentity.notification.SSONavModel;
import com.newshunt.dataentity.notification.SearchNavModel;
import com.newshunt.dataentity.notification.SettingsModel;
import com.newshunt.dataentity.notification.SettingsAutoScrollModel;
import com.newshunt.dataentity.notification.SocialCommentsModel;
import com.newshunt.dataentity.notification.TVNavModel;
import com.newshunt.dataentity.notification.WebNavModel;
import com.newshunt.deeplink.navigator.NewsNavigator;
import com.newshunt.dhutil.R;

import java.util.Locale;
import java.util.Map;

/**
 * Helper class to get {@link BaseModel} from DeeplinkUrl
 *
 * @author santhosh.kc
 */
public class Deeplinker {

  public static void startDeeplinking(int uniqueRequestId, DeeplinkModel deeplinkModel) {
    new DeeplinkServiceTask(uniqueRequestId, deeplinkModel).executeOnExecutor(
        AsyncTask.SERIAL_EXECUTOR);
  }

  private static class DeeplinkServiceTask extends AsyncTask<Void, Void, BaseModel> {
    private int uniqueRequestId;
    private DeeplinkModel deeplinkModel;

    public DeeplinkServiceTask(int uniqueRequestId, DeeplinkModel deeplinkModel) {
      this.uniqueRequestId = uniqueRequestId;
      this.deeplinkModel = deeplinkModel;
    }

    @Override
    protected BaseModel doInBackground(Void... voids) {
      if (deeplinkModel == null || CommonUtils.isEmpty(deeplinkModel.getDeeplinkUrl())) {
        return null;
      }
      String reDirectedUrl = DeeplinkUrlReDirectionHelper.getReDirectedUrl(deeplinkModel
          .getDeeplinkUrl(), uniqueRequestId);

      if (CommonUtils.isEmpty(reDirectedUrl)) {
        return null;
      }

      return parseDeeplinkModel(reDirectedUrl, deeplinkModel);
    }

    @Override
    public void onPostExecute(BaseModel baseModel) {
      String deeplinkUrl = Constants.EMPTY_STRING;
      if (deeplinkModel != null) {
        deeplinkUrl = deeplinkModel.getDeeplinkUrl();
        if (baseModel != null) {
          baseModel.setFullSync(deeplinkModel.isFullSync());
          baseModel.setFilterValue(deeplinkModel.getFilterValue());
          if(deeplinkModel.getBaseInfo() != null && baseModel.getBaseInfo() != null){
            baseModel.getBaseInfo().setDisableLangFilter(deeplinkModel.getBaseInfo().isDisableLangFilter());
            baseModel.getBaseInfo().setLanguage(deeplinkModel.getBaseInfo().getLanguage());
          }
        }
      }

      DeeplinkResponse deeplinkResponse =
          new DeeplinkResponse(uniqueRequestId, baseModel, deeplinkUrl, deeplinkModel);
      BusProvider.getUIBusInstance().post(deeplinkResponse);
    }
  }

  public static BaseModel parseDeeplinkModel(String reDirectedUrl, DeeplinkModel deeplinkModel) {
    BaseModel baseModel = DeepLinkParser.parseUrl(reDirectedUrl);
    if (baseModel == null) {
      return null;
    } else if (BaseModelType.BOOKS_MODEL.equals(baseModel.getBaseModelType())) {
      if (baseModel.getBaseInfo() != null && deeplinkModel.getBaseInfo() != null) {
        baseModel.getBaseInfo().setDedupeKey(deeplinkModel.getBaseInfo().getDedupeKey());
      }
      return baseModel;
    }

    if (baseModel.getBaseInfo() == null) {
      baseModel.setBaseInfo(new BaseInfo());
    }

    baseModel.getBaseInfo().setDedupeKey(deeplinkModel.getBaseInfo().getDedupeKey());

    String queryUrl = UrlUtil.getQueryUrl(reDirectedUrl);
    if (!CommonUtils.isEmpty(queryUrl)) {
      Map<String, String> reDirectedUrlParams;
      reDirectedUrlParams = UrlUtil.urlRequestParamToMap(queryUrl);
      baseModel.getBaseInfo().setUrlParamsMap(reDirectedUrlParams);
      baseModel.getBaseInfo().setQueryParams(reDirectedUrlParams);
    }

    return extractDeeplinkModelIntoBaseModel(deeplinkModel, baseModel) ? baseModel : null;
  }

  private static boolean extractDeeplinkModelIntoBaseModel(DeeplinkModel deeplinkModel, BaseModel
      baseModel) {
    boolean extracted = false;
    if (deeplinkModel != null && deeplinkModel.getBaseInfo() != null && baseModel != null &&
        baseModel.getBaseInfo() != null) {
      baseModel.getBaseInfo().setV4DisplayTime(deeplinkModel.getBaseInfo().getV4DisplayTime());
      baseModel.getBaseInfo()
          .setV4IsInternetRequired(deeplinkModel.getBaseInfo().isV4IsInternetRequired());
      baseModel.getBaseInfo().setV4SwipeUrl(deeplinkModel.getBaseInfo().getV4SwipeUrl());
      baseModel.getBaseInfo().setV4BackUrl(deeplinkModel.getBaseInfo().getV4BackUrl());
      baseModel.getBaseInfo()
          .setV4SwipePageLogic(deeplinkModel.getBaseInfo().getV4SwipePageLogic());
      baseModel.getBaseInfo()
          .setV4SwipePageLogicId(deeplinkModel.getBaseInfo().getV4SwipePageLogicId());
      baseModel.getBaseInfo().setDeliveryType(deeplinkModel.getBaseInfo().getDeliveryType());
      baseModel.getBaseInfo().setGroupType(deeplinkModel.getBaseInfo().getGroupType());
      baseModel.getBaseInfo().setPlacement(deeplinkModel.getBaseInfo().getPlacement());
      baseModel.getBaseInfo().setIconUrls(deeplinkModel.getBaseInfo().getIconUrls());
      baseModel.getBaseInfo().setType(deeplinkModel.getBaseInfo().getType());
      baseModel.getBaseInfo().setImp(deeplinkModel.getBaseInfo().getImp());
      baseModel.getBaseInfo().setGroupable(deeplinkModel.getBaseInfo().getGroupable());
      baseModel.getBaseInfo().setTags(deeplinkModel.getBaseInfo().getTags());
      baseModel.getBaseInfo().setSourceId(deeplinkModel.getBaseInfo().getSourceId());
      baseModel.getBaseInfo().setIgnoreSourceBlock(deeplinkModel.getBaseInfo().getIgnoreSourceBlock());
      baseModel.setStickyItemType(deeplinkModel.getStickyItemType());
      baseModel.setDisableEvents(deeplinkModel.isLoggingNotificationEventsDisabled());
    }

    BaseModelType baseModelType = baseModel.getBaseModelType();
    if (baseModelType != null) {
      switch (baseModelType) {
        case NEWS_MODEL:
          extracted = extractDeeplinkModelIntoNews(deeplinkModel, (NewsNavModel) baseModel);
          break;
        case TV_MODEL:
          extracted = extractDeeplinkModelIntoTV(deeplinkModel, (TVNavModel) baseModel);
          break;
        case LIVETV_MODEL:
          extracted = extractDeeplinkModelIntoLiveTV(deeplinkModel, (LiveTVNavModel) baseModel);
          break;
        case NAVIGATION_MODEL:
          extracted = extractDeeplinkModelIntoNavigationModel(deeplinkModel,
              (NavigationModel) baseModel);
          break;
        case ADS_MODEL:
          extracted = extractDeeplinkModelIntoAds(deeplinkModel, (AdsNavModel) baseModel);
          break;
        case WEB_MODEL:
          extracted = extractDeeplinkModelIntoWeb(deeplinkModel, (WebNavModel) baseModel);
          break;
        case SSO_MODEL:
          extracted = extractDeeplinkModelIntoSSO(deeplinkModel, (SSONavModel) baseModel);
          break;
        case SOCIAL_COMMENTS_MODEL:
          extracted = extractDeeplinkModelIntoSocialComments(deeplinkModel,
              (SocialCommentsModel) baseModel);
          break;
        case EXPLORE_MODEL:
          extracted = extractDeeplinkModelIntoExplore(deeplinkModel, (ExploreNavModel) baseModel);
          break;
        case FOLLOW_MODEL:
          extracted = extractDeeplinkModelIntoFollow(deeplinkModel, (FollowNavModel) baseModel);
          break;
        case PROFILE_MODEL:
          extracted = extractDeeplinkModelIntoProfile(deeplinkModel, (ProfileNavModel) baseModel);
          break;
        case GROUP_MODEL:
          extracted = extractDeeplinkModelIntoGroup(deeplinkModel, (GroupNavModel) baseModel);
          break;
        case SEARCH_MODEL:
          extracted = extractDeeplinkModelIntoSearch(deeplinkModel, (SearchNavModel) baseModel);
          break;
        case CREATE_POST_MODEL:
          extracted = extractDeeplinkModelIntoCreatePost(deeplinkModel, (CreatePostNavModel) baseModel);
          break;
        case CONTACTS_RECO_MODEL:
          extracted = extractDeeplinkModelIntoContactsReco(deeplinkModel, (ContactsRecoNavModel) baseModel);
          break;
        case RUNTIME_PERMISSIONS:
          extracted = extractDeeplinkModelIntoRuntimePermission(deeplinkModel, (PermissionNavModel) baseModel);
          break;
        case LANG_SELECTION:
          extracted = extractDeeplinkModelIntoLangeSelection(deeplinkModel, (LangSelectionNavModel) baseModel);
          break;
        case ADJUNCT_LANG_MODEL:
          extracted = extractDeeplinkModelIntoAdjunctLangModel(deeplinkModel, (AdjunctLangModel) baseModel);
          break;
        case APP_SECTION_MODEL:
          extracted = extractDeeplinkModelIntoAppSectionModel(deeplinkModel, (AppSectionModel) baseModel);
          break;
        case SETTINGS:
          extracted = extractDeeplinkModelIntoSettingsModel(deeplinkModel, (SettingsModel) baseModel);
          break;
        case SETTINGS_AUTOSCROLL:
          extracted = extractDeeplinkModelIntoSettingsAutoScrollModel(deeplinkModel, (SettingsAutoScrollModel) baseModel);
          break;
        case NOTIFICATION_INBOX:
          extracted = extractDeeplinkModelIntoNotificationInboxModel(deeplinkModel, (NotificationInboxModel) baseModel);
          break;
        case NOTIFICATION_SETTINGS:
          extracted = extractDeeplinkModelIntoNotificationSettingsModel(deeplinkModel, (NotificationSettingsModel) baseModel);
          break;
        case LOCAL_MODEL:
          extracted = extractDeeplinkModelIntoLocalModel(deeplinkModel, (LocalNavModel) baseModel);
      }
    }
    return extracted;
  }

  private static boolean extractDeeplinkModelIntoLangeSelection(DeeplinkModel deeplinkModel, LangSelectionNavModel baseModel) {
    return DeeplinkUtils.extractDeeplinkModelIntoLangeSelection(deeplinkModel, baseModel);

  }

  private static boolean extractDeeplinkModelIntoAdjunctLangModel(DeeplinkModel deeplinkModel,
                                                                  AdjunctLangModel baseModel) {
    return DeeplinkUtils.extractDeeplinkModelIntoAdjunctLang(deeplinkModel, baseModel);
  }

  private static boolean extractDeeplinkModelIntoAppSectionModel(DeeplinkModel deeplinkModel,
                                                                  AppSectionModel baseModel) {
    return DeeplinkUtils.extractDeeplinkModelIntoAppSection(deeplinkModel, baseModel);
  }

  private static boolean extractDeeplinkModelIntoSettingsModel(DeeplinkModel deeplinkModel,
                                                                 SettingsModel baseModel) {
    return DeeplinkUtils.extractDeeplinkModelIntoSettings(deeplinkModel, baseModel);
  }

  private static boolean extractDeeplinkModelIntoSettingsAutoScrollModel(DeeplinkModel deeplinkModel,
                                                               SettingsAutoScrollModel baseModel) {
    return DeeplinkUtils.extractDeeplinkModelIntoSettingsAutoScroll(deeplinkModel, baseModel);
  }

  private static boolean extractDeeplinkModelIntoNotificationInboxModel(DeeplinkModel deeplinkModel,
                                                                        NotificationInboxModel baseModel) {
    return DeeplinkUtils.extractDeeplinkModelIntoNotificationInbox(deeplinkModel, baseModel);
  }

  private static boolean extractDeeplinkModelIntoNotificationSettingsModel(DeeplinkModel deeplinkModel,
                                                                        NotificationSettingsModel baseModel) {
    return DeeplinkUtils.extractDeeplinkModelIntoNotificationSettings(deeplinkModel, baseModel);
  }

  private static boolean extractDeeplinkModelIntoLocalModel(DeeplinkModel deeplinkModel,
                                                     LocalNavModel baseModel) {
    return DeeplinkUtils.extractDeeplinkModelIntoLocal(deeplinkModel, baseModel);
  }

  private static boolean extractDeeplinkModelIntoRuntimePermission(DeeplinkModel deeplinkModel,
                                                                   PermissionNavModel baseModel) {
    return DeeplinkUtils.extractDeeplinkModelIntoRuntimePermission(deeplinkModel, baseModel);
  }

  private static boolean extractDeeplinkModelIntoSearch(DeeplinkModel deeplinkModel,
                                                        SearchNavModel searchNavModel) {
    return DeeplinkUtils.extractDeeplinkModeIntoSearchNavModel(deeplinkModel, searchNavModel);
  }

  private static boolean extractDeeplinkModelIntoSSO(DeeplinkModel deeplinkModel,
                                              SSONavModel ssoNavModel) {
    return DeeplinkUtils.extractDeeplinkModeIntoSSONavModel(deeplinkModel, ssoNavModel);
  }

  private static boolean extractDeeplinkModelIntoNews(DeeplinkModel deeplinkModel, NewsNavModel
      newsNavModel) {
    if (!DeeplinkUtils.extractDeeplinkModelIntoNewsNavModel(deeplinkModel, newsNavModel)) {
      return false;
    }

    if (NewsNavigator.needsLanguageCodeSetting(newsNavModel)) {
      setLanguageCode(newsNavModel);
    }
    if(deeplinkModel.isAdjunct() && deeplinkModel.getBaseInfo().getLanguage()!=null) {
      newsNavModel.setLanguage(deeplinkModel.getBaseInfo().getLanguage().toLowerCase(Locale.ROOT));
      newsNavModel.setAdjunct(deeplinkModel.isAdjunct());
      newsNavModel.setPopupDisplayType(deeplinkModel.getPopupDisplayType());
    }
    return true;
  }

  private static boolean extractDeeplinkModelIntoLiveTV(DeeplinkModel deeplinkModel, LiveTVNavModel
      tvNavModel) {
    return DeeplinkUtils.extractDeeplinkModelIntoLiveTVNavModel(deeplinkModel, tvNavModel);
  }

  private static boolean extractDeeplinkModelIntoSocialComments(DeeplinkModel deeplinkModel,
                                                         SocialCommentsModel socialCommentsModel) {
    return DeeplinkUtils.extractDeeplinkModelIntoSocialCommentsModel(deeplinkModel,
        socialCommentsModel);
  }

  private static boolean extractDeeplinkModelIntoTV(DeeplinkModel deeplinkModel, TVNavModel
      tvNavModel) {
    return DeeplinkUtils.extractDeeplinkModelIntoTVNavModel(deeplinkModel, tvNavModel);
  }

  private static boolean extractDeeplinkModelIntoWeb(DeeplinkModel deeplinkModel, WebNavModel
      webNavModel) {
    return DeeplinkUtils.extractDeeplinkModeIntoWebNavModel(deeplinkModel, webNavModel);
  }

  private static boolean extractDeeplinkModelIntoExplore(DeeplinkModel deeplinkModel, ExploreNavModel
      exploreNavModel) {
    return DeeplinkUtils.extractDeeplinkModelIntoExploreModel(deeplinkModel, exploreNavModel);
  }

  private static boolean extractDeeplinkModelIntoFollow(DeeplinkModel deeplinkModel, FollowNavModel
      followNavModel) {
    return DeeplinkUtils.extractDeeplinkModelIntoFollowModel(deeplinkModel,followNavModel);
  }

  private static boolean extractDeeplinkModelIntoNavigationModel(DeeplinkModel deeplinkModel,
                                                          NavigationModel navigationModel) {
    return DeeplinkUtils.extractDeeplinkModelIntoNavigationModel(deeplinkModel, navigationModel);
  }

  private static boolean extractDeeplinkModelIntoAds(DeeplinkModel deeplinkModel, AdsNavModel
      adsNavModel) {
    return DeeplinkUtils.extractDeeplinkModelIntoAdsNavModel(deeplinkModel, adsNavModel);
  }

  private static boolean extractDeeplinkModelIntoProfile(DeeplinkModel deeplinkModel,
                                                  ProfileNavModel profileNavModel) {
    return DeeplinkUtils.extractDeeplinkModelIntoProfileNavModel(deeplinkModel, profileNavModel);
  }

  private static boolean extractDeeplinkModelIntoGroup(DeeplinkModel deeplinkModel,
                                                  GroupNavModel groupNavModel) {
    return DeeplinkUtils.extractDeeplinkModelIntoGroupNavModel(deeplinkModel, groupNavModel);
  }

  private static boolean extractDeeplinkModelIntoCreatePost(DeeplinkModel deeplinkModel,
                                                            CreatePostNavModel cpNavModel) {
    return DeeplinkUtils.extractDeeplinkModelIntoCreatePostModel(deeplinkModel, cpNavModel);
  }

  private static boolean extractDeeplinkModelIntoContactsReco(DeeplinkModel deeplinkModel,
                                                            ContactsRecoNavModel contactsRecoNavModel) {
    return DeeplinkUtils.extractDeeplinkModelIntoContactsRecoModel(deeplinkModel, contactsRecoNavModel);
  }

  private static void setLanguageCode(BaseModel baseModel) {
    if (baseModel == null || baseModel.getBaseInfo() == null) {
      return;
    }
    BaseInfo baseInfo = baseModel.getBaseInfo();
    String langCode = CommonUtils.getLangCode(baseInfo.getLanguage(), R.array.language_list);
    baseInfo.setLanguageCode(langCode);
  }
}