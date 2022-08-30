/*
 * Copyright (c) 2016 Newshunt. All rights reserved.
 */
package com.newshunt.dhutil.helper.appsection;

import com.newshunt.common.helper.preference.PreferenceManager;
import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.common.helper.common.Logger;
import com.newshunt.dataentity.common.model.entity.AppSection;
import com.newshunt.dhutil.helper.preference.AppStatePreference;
import com.newshunt.dhutil.helper.preference.UserPreferenceUtil;
import com.newshunt.dataentity.dhutil.model.entity.appsection.AppSectionInfo;
import com.newshunt.dataentity.dhutil.model.entity.appsection.AppSectionsResponse;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * @author santhosh.kc
 */
public class AppSectionsFilter {

  private static final String TAG = "AppSectionsFilter";

  //Declare the mandatory sections for the filter
  private static final int MANDATORY_SECTIONS =
      AppSection.NEWS.getTypeNumber() | AppSection.TV.getTypeNumber() |
          AppSection.FOLLOW.getTypeNumber();
  private static final int MANDATORY_SECTIONS_NOREG =
      AppSection.NEWS.getTypeNumber() | AppSection.TV.getTypeNumber();

  public static boolean filterAndValidateResponse(AppSectionsResponse appSectionsResponse) {
    if (appSectionsResponse == null || CommonUtils.isEmpty(appSectionsResponse.getSections())) {
      return false;
    }
    filterSections(appSectionsResponse);
    return validateSections(appSectionsResponse);
  }

  private static void filterSections(AppSectionsResponse appSectionsResponse) {
    if (appSectionsResponse == null || CommonUtils.isEmpty(appSectionsResponse.getSections())) {
      return;
    }

    List<AppSectionInfo> appSectionInfos = appSectionsResponse.getSections();

    for (int i = appSectionInfos.size() - 1; i >= 0; i--) {
      if (!isValidAppSection(appSectionInfos.get(i))) {
        appSectionInfos.remove(i);
      }
    }
  }

  private static boolean isValidAppSection(AppSectionInfo appSectionInfo) {
    return appSectionInfo != null && appSectionInfo.getType() != null &&
        appSectionInfo.getType() != AppSection.NOTIFICATIONINBOX &&
        !CommonUtils.isEmpty(appSectionInfo.getId());
  }

  public static boolean filterResponseOnLanguage(AppSectionsResponse appSectionsResponse,
                                                 String userLanguages) {

    Logger.d(TAG, "Applying Language filter - entry");
    if (appSectionsResponse == null || CommonUtils.isEmpty(appSectionsResponse.getSections())) {
      Logger.d(TAG,"appSectionsResponse is null or sections are empty, so returning");
      return false;
    }

    Set<String> presentIds = new HashSet<>();

    List<AppSectionInfo> appSectionInfos = appSectionsResponse.getSections();
    Iterator<AppSectionInfo> iterator = appSectionInfos.iterator();
    List<AppSectionInfo> filteredList = new ArrayList<>();

    while(iterator.hasNext()) {
      AppSectionInfo next = iterator.next();
      if (!isSectionLanguageValid(next, userLanguages) || presentIds.contains(next.getId())) {
        Logger.d(TAG,"Removing app section with id: " + next.getId() + " and type: " + next
            .getType().getName() + " as section is not valid for userLanguages: " + userLanguages
            + " or app section id is already present..");
        iterator.remove();
      } else {
        presentIds.add(next.getId());
        filteredList.add(next);
        Logger.d(TAG,"Adding app section with id: " + next.getId() + " and type: " + next
            .getType().getName() + " as section is valid for userLanguages: " + userLanguages);
      }
    }

    appSectionsResponse.setSections(filteredList);
    Logger.d(TAG, "Applying Language filter - exit");
    return !filteredList.isEmpty();
  }

  private static boolean isSectionLanguageValid(AppSectionInfo appSectionInfo, String appLanguage) {
    if (CommonUtils.isEmpty(appSectionInfo.getLangfilter())) {
      return true;
    }

    if (CommonUtils.isEmpty(appLanguage)) {
      return false;
    }

    String languages = UserPreferenceUtil.getUserLanguages();
    String[] languagesList = languages.split(",");
    if (languagesList != null && languagesList.length > 0) {
      for (String language : languagesList) {
        if (appSectionInfo.getLangfilter().contains(language)) {
          return true;
        }
      }
    }

    return false;
  }

  private static boolean validateSections(AppSectionsResponse appSectionsResponse) {
    if (appSectionsResponse == null || CommonUtils.isEmpty(appSectionsResponse.getSections())) {
      return false;
    }
    int sectionsSoFar = 0;
    int mandatorySections = 0;
    List<AppSectionInfo> appSectionInfos = appSectionsResponse.getSections();
    Boolean isRegistered = PreferenceManager.getPreference(AppStatePreference.IS_APP_REGISTERED,
        false);

    for (AppSectionInfo appSectionInfo : appSectionInfos) {
      switch (appSectionInfo.getType()) {
        case NEWS:
          //will not add duplicate News Tab
          if (!isValidNewsSection(sectionsSoFar, appSectionInfo)) {
            return false;
          }
          break;
        case TV:
          //will not add duplicate TV Tab
          if (!isValidTVSection(sectionsSoFar, appSectionInfo)) {
            return false;
          }
          break;
        case WEB:
        case SEARCH:
          //can add another web tab
          if (!isValidWebSection(appSectionInfo)) {
            return false;
          }
          break;
        case FOLLOW:
          //will not add duplicate Follow Tab
          if (!isValidFollowSection(sectionsSoFar, appSectionInfo)) {
            return false;
          }
          break;
        case DEEPLINK:
          //will not add duplicate Follow Tab
          if (!isValidDeeplinkSection(sectionsSoFar, appSectionInfo)) {
            return false;
          }
          break;
        default:
          return false;
      }

      if (isMandatorySection(appSectionInfo.getType(),isRegistered)) {
        mandatorySections = addToSections(mandatorySections, appSectionInfo.getType().getTypeNumber());
      }
      sectionsSoFar = addToSections(sectionsSoFar, appSectionInfo.getType().getTypeNumber());
    }
    if(!isRegistered){
      return mandatorySections == MANDATORY_SECTIONS_NOREG;
    }

    return mandatorySections == MANDATORY_SECTIONS;
  }

  private static int addToSections(int sectionsSoFar, int typeNumber) {
    return sectionsSoFar | typeNumber;
  }

  private static boolean isMandatorySection(AppSection appSection, Boolean isRegistered) {
    if(!isRegistered){
      return appSection != null &&
          (appSection.getTypeNumber() & MANDATORY_SECTIONS_NOREG) == appSection.getTypeNumber();
    }
    return appSection != null &&
        (appSection.getTypeNumber() & MANDATORY_SECTIONS) == appSection.getTypeNumber();
  }

  private static boolean isValidNewsSection(int sectionsSoFar, AppSectionInfo newsAppSectionInfo) {
    return !sectionAlreadyExists(sectionsSoFar, AppSection.NEWS) && isAppSectionTitleInfoValid
        (newsAppSectionInfo) && isAppSectionIconsValid(newsAppSectionInfo);
  }

  private static boolean isValidTVSection(int sectionsSoFar, AppSectionInfo tvAppSectionInfo) {
    return !sectionAlreadyExists(sectionsSoFar, AppSection.TV) && isAppSectionTitleInfoValid
        (tvAppSectionInfo) && isAppSectionIconsValid(tvAppSectionInfo);
  }


  private static boolean isValidNotificationInboxSection(int sectionsSoFar, AppSectionInfo
      notifAppSectionInfo) {
    return !sectionAlreadyExists(sectionsSoFar, AppSection.NOTIFICATIONINBOX) &&
        isAppSectionTitleInfoValid
            (notifAppSectionInfo) && isAppSectionIconsValid(notifAppSectionInfo);
  }

  private static boolean isValidWebSection(AppSectionInfo webAppSectionInfo) {
    return isAppSectionTitleInfoValid(webAppSectionInfo)
        && isAppSectionIconsValid(webAppSectionInfo) && isValidContentUrl(webAppSectionInfo);
  }

  private static boolean isValidFollowSection(int sectionsSoFar, AppSectionInfo
      followAppSectionInfo) {
    return !sectionAlreadyExists(sectionsSoFar, AppSection.FOLLOW) && isAppSectionTitleInfoValid
        (followAppSectionInfo) && isAppSectionIconsValid(followAppSectionInfo);
  }

  private static boolean isValidDeeplinkSection(int sectionsSoFar, AppSectionInfo
      deeplinkAppSectionInfo) {
    return isAppSectionTitleInfoValid(deeplinkAppSectionInfo)
        && isAppSectionIconsValid(deeplinkAppSectionInfo) && isValidDeeplinkUrl(deeplinkAppSectionInfo);
  }

  private static boolean sectionAlreadyExists(int existingSections, AppSection appSection) {
    return appSection != null &&
        (existingSections & appSection.getTypeNumber()) == appSection.getTypeNumber();
  }

  private static boolean isAppSectionTitleInfoValid(AppSectionInfo appSectionInfo) {
    return appSectionInfo != null && !CommonUtils.isEmpty(appSectionInfo.getId()) && !CommonUtils.isEmpty
        (appSectionInfo.getTitle());
  }

  private static boolean isAppSectionIconsValid(AppSectionInfo appSectionInfo) {
    return appSectionInfo != null && !CommonUtils.isEmpty(appSectionInfo.getActiveIconUrl()) &&
        !CommonUtils.isEmpty(appSectionInfo.getInactiveIconUrl()) &&
        !CommonUtils.isEmpty(appSectionInfo.getActiveIconUrlNight()) &&
        !CommonUtils.isEmpty(appSectionInfo.getInactiveIconUrlNight());
  }

  private static boolean isValidContentUrl(AppSectionInfo appSectionInfo) {
    return appSectionInfo != null && !CommonUtils.isEmpty(appSectionInfo.getContentUrl());
  }
  private static boolean isValidDeeplinkUrl(AppSectionInfo appSectionInfo) {
    return appSectionInfo != null && !CommonUtils.isEmpty(appSectionInfo.getDeeplinkUrl());
  }
}
