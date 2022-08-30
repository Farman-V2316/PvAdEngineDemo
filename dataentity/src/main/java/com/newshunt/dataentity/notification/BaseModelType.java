/*
 * Copyright (c) 2017 Newshunt. All rights reserved.
 */
package com.newshunt.dataentity.notification;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.dataentity.notification.asset.CricketDataStreamAsset;
import com.newshunt.dataentity.notification.asset.CricketNotificationAsset;
import com.newshunt.dataentity.notification.asset.GenericDataStreamAsset;
import com.newshunt.dataentity.notification.asset.GenericNotificationAsset;

import java.lang.reflect.Type;

import javax.annotation.Nullable;

/**
 * Created by anshul on 6/3/17.
 * An enum for telling the type of BaseModel.
 */

public enum BaseModelType {
  NEWS_MODEL,
  BOOKS_MODEL,
  TV_MODEL,
  LIVETV_MODEL,
  DEEPLINK_MODEL,
  ADS_MODEL,
  NAVIGATION_MODEL,
  WEB_MODEL,
  SSO_MODEL,
  STICKY_MODEL,
  SOCIAL_COMMENTS_MODEL,
  EXPLORE_MODEL,
  FOLLOW_MODEL,
  SEARCH_MODEL,
  PROFILE_MODEL,
  GROUP_MODEL,
  CREATE_POST_MODEL,
  CONTACTS_RECO_MODEL,
  RUNTIME_PERMISSIONS,
  LANG_SELECTION,
  ADJUNCT_LANG_MODEL,
  APP_SECTION_MODEL,
  ADJUNCT_MESSAGE,
  LOCAL_MODEL,
  SILENT_MODEL,
  SETTINGS,
  SETTINGS_AUTOSCROLL,
  IN_APP,
  NOTIFICATION_INBOX,
  NOTIFICATION_SETTINGS;


  @Nullable
  public static String convertModelToString(@Nullable BaseModel baseModel) {

    if (baseModel == null) {
      return null;
    }
    Gson gson = new Gson();
    if (baseModel.getBaseModelType() == null) {
      return gson.toJson(baseModel, BaseModel.class);
    }

    switch (baseModel.getBaseModelType()) {
      case NEWS_MODEL:
        return gson.toJson(baseModel, NewsNavModel.class);
      case BOOKS_MODEL:
        return gson.toJson(baseModel, BooksNavModel.class);
      case TV_MODEL:
        return gson.toJson(baseModel, TVNavModel.class);
      case LIVETV_MODEL:
        return gson.toJson(baseModel, LiveTVNavModel.class);
      case DEEPLINK_MODEL:
        return gson.toJson(baseModel, DeeplinkModel.class);
      case ADS_MODEL:
        return gson.toJson(baseModel, AdsNavModel.class);
      case NAVIGATION_MODEL:
        return gson.toJson(baseModel, NavigationModel.class);
      case WEB_MODEL:
        return gson.toJson(baseModel, WebNavModel.class);
      case SSO_MODEL:
        return gson.toJson(baseModel, SSONavModel.class);
      case STICKY_MODEL:
        Type type = getType(((StickyNavModel) baseModel).getStickyType());
        if (type != null) {
          return gson.toJson(baseModel, type);
        }
      case SOCIAL_COMMENTS_MODEL:
        return gson.toJson(baseModel, SocialCommentsModel.class);
      case EXPLORE_MODEL:
        return gson.toJson(baseModel, ExploreNavModel.class);
      case FOLLOW_MODEL:
        return gson.toJson(baseModel, FollowNavModel.class);
      case PROFILE_MODEL:
        return gson.toJson(baseModel, ProfileNavModel.class);
      case SEARCH_MODEL:
        return gson.toJson(baseModel, SearchNavModel.class);
      case CREATE_POST_MODEL:
        return gson.toJson(baseModel, CreatePostNavModel.class);
      case RUNTIME_PERMISSIONS:
        return gson.toJson(baseModel, PermissionNavModel.class);
      case GROUP_MODEL:
        return gson.toJson(baseModel, GroupNavModel.class);
      case ADJUNCT_MESSAGE:
        return gson.toJson(baseModel,AdjunctLangStickyNavModel.class);
    }

    return gson.toJson(baseModel, BaseModel.class);
  }

  @Nullable
  public static BaseModel convertStringToBaseModel(@Nullable String baseModelString,
                                                   BaseModelType baseModelType,
                                                   String stickyType) {
    if (CommonUtils.isEmpty(baseModelString)) {
      return null;
    }

    Gson gson = new Gson();

    if (baseModelType == null) {
      return gson.fromJson(baseModelString, BaseModel.class);
    }

    switch (baseModelType) {
      case NEWS_MODEL:
        return gson.fromJson(baseModelString, NewsNavModel.class);
      case BOOKS_MODEL:
        return gson.fromJson(baseModelString, BooksNavModel.class);
      case TV_MODEL:
        return gson.fromJson(baseModelString, TVNavModel.class);
      case LIVETV_MODEL:
        return gson.fromJson(baseModelString, LiveTVNavModel.class);
      case DEEPLINK_MODEL:
        return gson.fromJson(baseModelString, DeeplinkModel.class);
      case ADS_MODEL:
        return gson.fromJson(baseModelString, AdsNavModel.class);
      case NAVIGATION_MODEL:
        return gson.fromJson(baseModelString, NavigationModel.class);
      case WEB_MODEL:
        return gson.fromJson(baseModelString, WebNavModel.class);
      case SSO_MODEL:
        return gson.fromJson(baseModelString, SSONavModel.class);
      case STICKY_MODEL:
        Type type = getType(stickyType);
        return gson.fromJson(baseModelString, type);
      case SOCIAL_COMMENTS_MODEL:
        return gson.fromJson(baseModelString, SocialCommentsModel.class);
      case EXPLORE_MODEL:
        return gson.fromJson(baseModelString, ExploreNavModel.class);
      case FOLLOW_MODEL:
        return gson.fromJson(baseModelString, FollowNavModel.class);
      case PROFILE_MODEL:
        return gson.fromJson(baseModelString, ProfileNavModel.class);
      case SEARCH_MODEL:
        return gson.fromJson(baseModelString, SearchNavModel.class);
      case CREATE_POST_MODEL:
        return gson.fromJson(baseModelString, CreatePostNavModel.class);
      case RUNTIME_PERMISSIONS:
        return gson.fromJson(baseModelString, PermissionNavModel.class);
      case GROUP_MODEL:
        return gson.fromJson(baseModelString, GroupNavModel.class);
      case ADJUNCT_MESSAGE:
        return gson.fromJson(baseModelString,AdjunctLangStickyNavModel.class);
    }
    return gson.fromJson(baseModelString, BaseModel.class);
  }

  @Nullable
  public static BaseModelType getValue(String value) {
    for (BaseModelType baseModelType : BaseModelType.values()) {
      if (CommonUtils.equals(baseModelType.name(), value)) {
        return baseModelType;
      }
    }
    return null;
  }

  public static Type getType(String stickyType) {
    StickyNavModelType stickyNavModelType = StickyNavModelType.from(stickyType);
    if (stickyNavModelType == StickyNavModelType.CRICKET) {
      return new TypeToken<StickyNavModel<CricketNotificationAsset,
          CricketDataStreamAsset>>() {
      }.getType();
    } else if (stickyNavModelType == StickyNavModelType.GENERIC) {
      return new TypeToken<StickyNavModel<GenericNotificationAsset,
          GenericDataStreamAsset>>() {
      }.getType();
    }
    return null;
  }
}
