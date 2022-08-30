/*
* Copyright (c) 2016 Newshunt. All rights reserved.
*/
package com.newshunt.dhutil.analytics;

/**
 * @author raunak.yadav
 */
public enum ExploreButtonType {
  MASTHEAD("masthead"),
  BOTTOMBAR("bottombar"),
  THREE_DOTS("three_dots"),
  NSFW_ALLOWED("nsfw_allowed"),
  MUTE("mute"),
  UNMUTE("unmute"),
  CARD_MENU_ADD_COMMENT("card_menu_add_comment");

  private final String type;

  ExploreButtonType(String type) {
    this.type = type;
  }

  public String getType() {
    return type;
  }
}