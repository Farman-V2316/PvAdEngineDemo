/*
 * Copyright (c) 2016 Newshunt. All rights reserved.
 */
package com.dailyhunt.tv.players.utils;

/**
 * Created by vinod.bc on 6/20/2016.
 */
public class PlayerConstants {

  public static final String BUNDLE_ACTIVITY_REFERRER = "activityReferrer";
  public static final String TV_CURRENT_PAGEINFO = "tv_current_page_info";
  public static final String TV_CURRENT_ITEM_INDEX = "tv_current_item_index";
  public static final String CATEGORY = "category";
  public static final String GROUP = "group";
  public static final String TAG = "tag";
  public static final String TVASSET_ITEM = "ITEM";
  public static final String TVDETAILSCREEN_TYPE = "TVDETAILSCREEN_TYPE";
  public static final String EMPTY_STR = "";
  public static final String NEW_LINE = "\n";
  public static final String PLAYER_JSON_FILENAME = "player_handshake_default.json";
  public static final String DEFAULT_RESOULTION_BUCKET_VALUE = "l";
  public static final String BUNDLE_ADAPTER_POSITION = "adapter_position";
  public static final int TV_ITEM_INDEX_PASS = 1000;
  public static final String COMMENTS = " Comments  ";
  public static final String BUNDLE_TV_KEY = "tv_key";
  public static final String BUNDLE_TV_LANGUAGE = "tv_language";
  public static final int PREFETCH_ITEM_THRESHOLD = 3;
  public static final String PRE_ROLL =
      "https://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=/124319096/external/single_ad_samples&ciu_szs=300x250&impl=s&gdfp_req=1&env=vp&output=vast&unviewed_position_start=1&cust_params=deployment%3Ddevsite%26sample_ct%3Dlinear&correlator=";
  public static final String PRE_ROLL_SKIP =
      "https://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=/124319096/external/single_ad_samples&ciu_szs=300x250&impl=s&gdfp_req=1&env=vp&output=vast&unviewed_position_start=1&cust_params=deployment%3Ddevsite%26sample_ct%3Dskippablelinear&correlator=";
  public static final String POST_ROLL =
      "https://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=/124319096/external/ad_rule_samples&ciu_szs=300x250&ad_rule=1&impl=s&gdfp_req=1&env=vp&output=vmap&unviewed_position_start=1&cust_params=deployment%3Ddevsite%26sample_ar%3Dpostonly&cmsid=496&vid=short_onecue&correlator=";
  public static final String VMAP =
      "https://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=/124319096/external/ad_rule_samples&ciu_szs=300x250&ad_rule=1&impl=s&gdfp_req=1&env=vp&output=vmap&unviewed_position_start=1&cust_params=deployment%3Ddevsite%26sample_ar%3Dpremidpost&cmsid=496&vid=short_onecue&correlator=";
  public static final String ADSENSE =
      "http://googleads.g.doubleclick.net/pagead/ads?client=ca-video-afvtest&ad_type=video";
  public static final String TEST_DFP =
      "https://pubads.g.doubleclick.net/gampad/ads?sz=400x300|640x480&iu=/83414793/common_inline_vdo&impl=s&gdfp_req=1&env=vp&output=vast&unviewed_position_start=1&url=google.com&description_url=amazon.com&correlator=1492512909";
  public static final String TEST_ABP = "http://pubads.g.doubleclick" +
      ".net/gampad/ads?sz=400x300|640x480&iu=/2599136/Newshunt_English_VOD_Preroll&impl=s&gdfp_req=1&env=vp&output=vast&unviewed_position_start=1&url=[referrer_url]&description_url=http://abplive.in&correlator=[timestamp]&hl=en";
  public static final String TEST_ABP_MID = "http://cdn.abplive.in/videojs/js/newshunt.xml";
  public static final String TEST_IMA_VMAP = "https://pubads.g.doubleclick" +
      ".net/gampad/ads?sz=640x480&iu=/124319096/external/ad_rule_samples&ciu_szs=300x250&ad_rule=1&impl=s&gdfp_req=1&env=vp&output=vmap&unviewed_position_start=1&cust_params=deployment%3Ddevsite%26sample_ar%3Dpremidpost&cmsid=496&vid=short_onecue&correlator=";

  public static final String TEST_IMA_VMAP_1 = "https://pubads.g.doubleclick" +
      ".net/gampad/ads?sz=640x480&iu=/124319096/external/ad_rule_samples&ciu_szs=300x250&ad_rule=1&impl=s&gdfp_req=1&env=vp&output=vmap&unviewed_position_start=1&cust_params=deployment%3Ddevsite%26sample_ar%3Dpremidpostlongpod&cmsid=496&vid=short_tencue&correlator=";
}