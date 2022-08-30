/*
 * Copyright (c) 2017 Newshunt. All rights reserved.
 */

package com.newshunt.dataentity.news.model.entity;

import com.newshunt.dataentity.news.model.entity.server.asset.UIType;
import com.newshunt.dataentity.news.model.entity.server.asset.UIType;

/**
 * Represents different cards to be displayed.
 *
 * @author shreyas.desai
 */
public enum DisplayCardType {
  STORY(1, "story", false, UIType.NORMAL),
  SOURCE(2, "source"),
  TOPIC(3, "topic"),
  ASTROLOGY(4, "astrology", false),
  /**
   * For gallery having >= Constants.GALLERY_CARD_TILE_3_COUNT
   */
  GALLERY(5, "album", null, UIType.TILE_3),
  TICKER(6, "ticker"),
  VIDEO(7, "video", null, UIType.NORMAL),
  ALBUM_PHOTO(8, "album-photo"),
  APP_DOWNLOAD_AD(9, "appDownload"),
  BANNER_AD(12, "native-banner"),
  IMAGE_LINK_AD(13, "imgLink"),
  EXTERNAL_SDK_AD(14, "external-sdk"),
  FEATURED_STORY(15, "featuredStory", false),
  MRAID_ZIP(16, "mraid-zip"),
  MRAID_EXTERNAL(17, "mraid-external"),
  PGI_ZIP(18, "pgi-zip"),
  PGI_EXTERNAL(19, "pgi-external"),
  PGI_ARTICLE_AD(20, "pgi-native-article"),
  STORY_URDU(21, "urduStory", false, UIType.NORMAL),
  RECENT_NEWSPAPERS(22, "recent-newspapers"),
  //has extra icon in the card.
  STORY_DOWNLOAD(23, "story", true, UIType.NORMAL),
  FEATURED_STORY_DOWNLOAD(24, "featuredStory", true),
  STORY_URDU_DOWNLOAD(25, "urduStory", true, UIType.NORMAL),
  VIDEO_URDU(26, "urduVideo", null, UIType.NORMAL),
  ASTROLOGY_DOWNLOAD(27, ASTROLOGY.getName(), true),
  /**
   * Urdu version of GALLERY
   */
  GALLERY_URDU(28, GALLERY.getName() + "_urdu", null, UIType.TILE_3),

  /**
   * used in the case where it is single photo not belonging to a gallery
   */
  PHOTO(29, "photo"),
  /**
   * card used to display related stories
   */
  RELATED_STORIES(30, "related_stories"),

  // seperators
  STORIES_SEPARATOR(31, "stories_separator"),
  //viewholders for native ads.
  NATIVE_AD(32, "native_default_ad"),
  NATIVE_HIGH_AD(33, "native_high_template_ad"),
  EXTERNAL_NATIVE_PGI(34, "external_sdk_native_pgi"),
  NATIVE_DFP_AD(35, "native_dfp_ad"),
  NATIVE_DFP_HIGH_AD(36, "native_dfp_high_template_ad"),
  NATIVE_DFP_APP_INSTALL_AD(37, "native_dfp_app_install_ad"),
  NATIVE_DFP_APP_INSTALL_HIGH_AD(38, "native_dfp_app_install_high_template_ad"),

  //Special cards
  STORY_HERO(42, "story_hero", null, UIType.HERO),//Breaking story lite
  STORY_HERO_URDU(43, "story_hero_urdu", null, UIType.HERO),//Breaking story urdu
  VIDEO_HERO(44, "video_hero", null, UIType.HERO),
  VIDEO_HERO_URDU(45, "video_hero_urdu", null, UIType.HERO),
  GRID_5_GALLERY(46, "grid_5_gallery", null, UIType.GRID_5),//grid gallery
  GRID_5_GALLERY_URDU(47, "grid_5_gallery_urdu", null, UIType.GRID_5),//grid gallery urdu
  ASTROCARD(48, "astrocard"),
  ASTROCARD_URDU(49, "astrocard_urdu"),
  BANNER(50, "BANNER", null, UIType.BANNER),
  GIF(51, "gif", null, UIType.NORMAL),
  GIF_URDU(52, "gif_urdu", null, UIType.NORMAL),
  GIF_HERO(53, "gif_hero", null, UIType.HERO),
  GIF_HERO_URDU(54, "gif_hero_urdu", null, UIType.HERO),
  STORY_RECT(55, "story_rect_image", false, UIType.NORMAL_RECT),
  STORY_URDU_RECT(56, "urduStory_rect_image", false, UIType.NORMAL_RECT),
  QUESTION_2_CHOICES(57, "question_2_choices", false),
  ASSOCIATION_VIDEO(58, "buzz_association_video", false),
  VH_SMALL(59, "vh_small", null, UIType.VH_SMALL),
  VH_BIG(60, "vh_big", null, UIType.BIG),
  VH_NORMAL(61, "vh_normal"),
  VH_DETAIL_TEXT(62, "vh_detail_text"),
  VH_FIT_BACKGROUND(63, "vh_fit_background", null, UIType.VH_FIT_BACKGROUND),
  IMA_VIDEO_AD(64, "ima_video_ad"),
  COMMENTS_SECTION(65, "comments_section"),
  QUESTION_MULTI_CHOICES_GRID(66, "GRID"),
  QUESTION_MULTI_CHOICES_TAGS(67, "TAGS"),
  QUESTION_MULTI_CHOICES_CAROUSEL1(68, "CAROUSEL_1", null, UIType.CAROUSEL_1),
  QUESTION_MULTI_CHOICES_CAROUSEL1_URDU(69, "CAROUSEL_1_URDU", null, UIType.CAROUSEL_1),
  QUESTION_MULTI_CHOICES_CAROUSEL2(70, "CAROUSEL_2", null, UIType.CAROUSEL_2),
  QUESTION_MULTI_CHOICES_CAROUSEL2_URDU(71, "CAROUSEL_2_URDU", null, UIType.CAROUSEL_2),
  QUESTION_MULTI_CHOICES_LIST(72, "LIST", null, UIType.LIST),
  FEEDBACK_STORY(73, "feedback_story"),
  VIDEO_EXO_AUTOPLAY(74, "video_autoplay"),
  MULTIMEDIA_COLLECTION(75, "multimedia_collection", null, UIType.CAROUSEL_1),
  STORY_NO_IMAGE(76, "story_no_image", null, UIType.NORMAL),
  STORY_NO_IMAGE_URDU(77, "story_no_image_urdu", null, UIType.NORMAL),
  SEARCH_PHOTO_GRID(78, "search_photo_grid", null, UIType.NORMAL),
  VIDEO_WEB_AUTOPLAY(79, "video_autoplay"),
  POLL(80, "poll"),
  LANGUAGE_SELECTION_CARD(81, "language_selection_card"),
  HEADER_1(82, "header_1", null, UIType.HEADER_1),
  HEADER_2(83, "header_2", null, UIType.HEADER_2),
  HEADER_2_URDU(84, "header_2_urdu", null, UIType.HEADER_2),
  FOLLOWS_CAROUSEL_1(85, "FOLLOWS", null, UIType.CAROUSEL_1),
  FOLLOWS_CAROUSEL_1_URDU(86, "follows_carousel_1_urdu", null, UIType.CAROUSEL_1),
  FOLLOWS_CAROUSEL_2(87, "follows_carousel_2", null, UIType.CAROUSEL_2),
  FOLLOWS_CAROUSEL_2_URDU(88, "follows_carousel_2_urdu", null, UIType.CAROUSEL_2),
  FOLLOWS_CAROUSEL_3(89, "follows_carousel_3", null, UIType.CAROUSEL_3),
  FOLLOWS_CAROUSEL_3_URDU(90, "follows_carousel_3_urdu", null, UIType.CAROUSEL_3),
  FOLLOWS_CAROUSEL_4(91, "follows_carousel_4", null, UIType.CAROUSEL_4),
  FOLLOWS_CAROUSEL_4_URDU(92, "follows_carousel_4_urdu", null, UIType.CAROUSEL_4),
  FOLLOWS_GRID(93, "follows_grid", null, UIType.GRID),
  FOLLOWS_GRID_URDU(94, "follows_grid", null, UIType.GRID),
  FOLLOWS_GRID_2(95, "follows_grid_2", null, UIType.GRID_2),
  FOLLOWS_GRID_2_URDU(96, "follows_grid_2_urdu", null, UIType.GRID_2),
  FOLLOWS_CAROUSEL_ALL(97, "follows_carousel_all", null, UIType.CAROUSEL_5),
  COLLECTION_LIST(98, "collection", null, UIType.LIST),
  COLLECTION_LIST_URDU(99, "collection_urdu", null, UIType.LIST),
  QUESTION_MULTI_CHOICES_GRID2(100, "question_multi_choice_grid2", null, UIType.GRID_2),
  QUESTION_MULTI_CHOICES_CAROUSEL3(101, "question_multi_choice_carousel3", null, UIType.CAROUSEL_3),
  QUESTION_MULTI_CHOICES_CAROUSEL4(102, "question_multi_choice_carousel4", null,
      UIType.CAROUSEL_4),

  QUESTION_MULTI_CHOICES_GRID2_URDU(103, "question_multi_choice_grid2_urdu", null, UIType.GRID_2),
  QUESTION_MULTI_CHOICES_CAROUSEL3_URDU(104, "question_multi_choice_carousel3_urdu", null,
      UIType.CAROUSEL_3),
  QUESTION_MULTI_CHOICES_CAROUSEL4_URDU(105, "question_multi_choice_carousel4_urdu", null,
      UIType.CAROUSEL_4),
  QUESTION_MULTI_CHOICES_GRID_URDU(106, "question_multi_choice_grid_urdu", null, UIType.GRID),
  WEB(107, "web"),
  MULTIMEDIA_COLLECTION_NEWS(108, "multimedia_collection_news", null, UIType.CAROUSEL_1),
  FOLLOWS_CAROUSEL_ALL_URDU(109, "follow_carousel_all_urdu", null, UIType.CAROUSEL_5),

  SAVED_CAROUSEL(112, "carousel_6"),
  SAVED_CAROUSEL_LIST_ITEM(113, "saved_carousel_list_item"),
  ACTIVITY(114, "activity", null, UIType.NORMAL),
  ACTIVITY_URDU(115, "activity_urdu", null, UIType.NORMAL),
  LIST_GROUP_HEADER(116, "list_group_header", null, UIType.NORMAL),
  SAVED_CAROUSEL_VIDEO_ITEM(117, "saved_carousel_video_item"),
  STORY_SMALL(118, "small", false, UIType.SMALL),
  SAVED_VIDEO_NORMAL(119, "normal", false, UIType.SAVED),
  LOGIN_CARD(120, "normal", false, UIType.NORMAL),


  GALLERY_2(121, "gallery_2"),
  GALLERY_3(122, "gallery_3"),
  GALLERY_4(123, "gallery_4"),
  GALLERY_5(124, "gallery_5"),
  POLL_RESULT(125,"poll_result"),
  VIRAL(126, "viral");


  private int index;
  private String name;
  private Boolean hasDownloadIcon;
  private UIType uiType;

  DisplayCardType(int index, String name) {
    this(index, name, null, null);
  }

  DisplayCardType(int index, String name, Boolean hasDownloadIcon) {
    this(index, name, hasDownloadIcon, null);
  }

  DisplayCardType(int index, String name, Boolean hasDownloadIcon, UIType uiType) {
    this.index = index;
    this.name = name;
    this.hasDownloadIcon = hasDownloadIcon;
    this.uiType = uiType;
  }

  public static DisplayCardType fromName(String name) {
    for (DisplayCardType cardType : DisplayCardType.values()) {
      if (DisplayCardType.FEATURED_STORY.getName().equalsIgnoreCase(name)) {
        return DisplayCardType.STORY;
      } else if (DisplayCardType.FEATURED_STORY_DOWNLOAD.getName().equalsIgnoreCase(name)) {
        return DisplayCardType.STORY_DOWNLOAD;
      } else if (cardType.name.equalsIgnoreCase(name)) {
        return cardType;
      }
    }
    return null;
  }

  /**
   * returns cardtype matching all params.
   */
  public static DisplayCardType thatMatches(String name, Boolean hasDownloadIcon) {
    for (DisplayCardType cardType : DisplayCardType.values()) {
      if (cardType.name.equalsIgnoreCase(name)
          && ((cardType.hasDownloadIcon == null) ||
          cardType.hasDownloadIcon.equals(hasDownloadIcon))) {
        return cardType;
      }
    }
    return null;
  }

  public static DisplayCardType fromIndex(int index) {
    for (DisplayCardType cardType : DisplayCardType.values()) {
      if (DisplayCardType.FEATURED_STORY.index == index) {
        return DisplayCardType.STORY;
      } else if (DisplayCardType.FEATURED_STORY_DOWNLOAD.index == index) {
        return DisplayCardType.STORY_DOWNLOAD;
      } else if (cardType.index == index) {
        return cardType;
      }
    }
    return null;
  }

  public int getIndex() {
    return index;
  }

  public String getName() {
    return name;
  }

  public UIType getUiType() {
    return uiType;
  }
}
