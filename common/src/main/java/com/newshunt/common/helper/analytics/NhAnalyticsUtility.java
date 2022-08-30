/**
 * Copyright (c) 2016 Newshunt. All rights reserved.
 */
package com.newshunt.common.helper.analytics;

/**
 * @author shrikant.agrawal
 */
public class NhAnalyticsUtility {

  public enum ErrorResponseCode {
    CONTENT_ERROR(1),
    SERVER_ERROR(2),
    NO_INTERNET(3),
    NETWORK_ERROR(4);

    int value;

    ErrorResponseCode(int value) {
      this.value = value;
    }

    public int getValue() {
      return value;
    }
  }

  public enum ErrorViewType {
    FULLSCREEN("fullscreen"),
    FULLSCREEN_EXPLORED("fullscreen_explored"),
    FULL_PAGE("full_page"),
    HALF_PAGE("half_page"),
    SNACKBAR("snackbar");

    String viewType;

    ErrorViewType(String viewType) {
      this.viewType = viewType;
    }

    public String getViewType() {
      return viewType;
    }
  }

  public enum ErrorPageType {
    STORY_DETAIL("story_detail"),
    STORY_LIST("story_list"),
    BOOK_HOME("book_home"),
    BOOK_TAB("book_tab"),
    BOOK_DETAIL("book_detail"),
    TV_LIST("tv_list"),
    LOCATION_SELECTION("location_selection");

    String pageType;

    ErrorPageType(String pageType) {
      this.pageType = pageType;
    }

    public String getPageType() {
      return pageType;
    }
  }
}
