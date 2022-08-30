/*
 * Copyright (c) 2016 Newshunt. All rights reserved.
 */

package com.newshunt.news.helper;

/**
 * News explore button type class
 *
 * @author  shashikiran.nr on 7/4/2016.
 */
public enum NewsExploreButtonType {

  ADD("add"),
  GEAR("gear"),
  EXPLORE("explore"),
  LOCAL("local"),
  TOOL_TIP("tooltip"),
  BOTTOMBAR_REFRESH("bottombar_1_Refresh"),
  TAB_REFRESH("tabrefresh"),
  GO_TO_TOP("gototop"),
  MORE_STORY_FOOTER("morestoryfooter"),
  TAP_TO_REFRESH("taptorefresh"),
  SNACKBAR("snackbar"),
  VIRAL_TOPIC("viral_topic"),
  CARD_HIDE("card_hide"),
  CREATE_GROUP("create_group"),
  JOIN_GROUP("join_group"),
  APPROVAL_CARD("approval_card"),
  OTHER_PERSPECTIVE("otherPerspectiveStories"),
  APPROVE("approve"),
  DECLINE("decline"),
  GROUP_INVITE_SEARCH("group_invite_search"),
  GROUP_INVITE_ALLOW("group_invite_allow"),
  DROPDOWN_MENU("dropdown_menu"),
  OTHER_LOCATION("other_location"),
  ADD_MORE("add_more"),
  PLUS_SECTION_LOCATIONS("plus_section_locations");


  private String buttonType;

  NewsExploreButtonType(String type) {
    this.buttonType = type;
  }

  public static NewsExploreButtonType fromName(String type) {
    for (NewsExploreButtonType newsExploreButtonType : NewsExploreButtonType.values()) {
      if (newsExploreButtonType.buttonType.equalsIgnoreCase(type)) {
        return newsExploreButtonType;
      }
    }
    return null;
  }

  public String getButtonType() {
    return buttonType;
  }
}
