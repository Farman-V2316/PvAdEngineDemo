/*
 * Copyright (c) 2016 Newshunt. All rights reserved.
 */
package com.dailyhunt.tv.helper;

/**
 * Created by vinod.bc on 6/20/2016.
 */
public class TVConstants {

  public static final String DEFAULT_RESOULTION_BUCKET_VALUE = "l";
  public static final String DEFAULT_QUALITY_BUCKET_VALUE = "h";
  public static final int TV_STATUS_BAR_HEIGHT = 60;

  public static final String KEY_NOTIFICATION_DATA = "notification_data";

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
