/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.dhutil.helper.preference;


import com.newshunt.common.helper.preference.PreferenceType;
import com.newshunt.common.helper.preference.SavedPreference;

/**
 * Shared preferences
 *
 * @author arun.babu
 */
public enum UserDetailPreference implements SavedPreference {
  NAME("userName"),
  VERSION4_USER_ID("userVersion4UserId"),
  ID("userId"),
  LOGIN_TYPE("userLoginType"),
  PASSWORD("userPassword"),
  IS_UPGRADE_USER("nhIsUpgradeUser"),
  HEADLINES_STORY_VIEW_COUNT("nhHeadlinesStoryViewCount"),
  GOOGLE_AD_ID("nhGoogleAdId"),
  ON_BOARDING_COMPLETED("nhOnboardingCompleted"),
  LIKE_MAP("likeMap"),
  VIRAL_ITEM_VIEWS("viralItemView");

  private final String name;

  private UserDetailPreference(String name) {
    this.name = name;
  }

  @Override
  public PreferenceType getPreferenceType() {
    return PreferenceType.USER_DETAIL;
  }

  @Override
  public String getName() {
    return name;
  }
}
