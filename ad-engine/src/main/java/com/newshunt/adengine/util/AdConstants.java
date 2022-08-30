/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.adengine.util;

/**
 * Constants to be used in ads.
 *
 * @author heena.arora
 */
public class AdConstants {
  public static final String DFP_AD = "DFP";
  public static final String FB_AD = "FB";
  public static final String AMAZON_AD = "Amazon";

  //Max ad cache count for good speed network
  public static final int MAX_AD_CACHE_COUNT_GOOD = 4;
  //Max ad cache count for average speed network
  public static final int MAX_AD_CACHE_COUNT_AVERAGE = 2;
  //Max ad cache count for slow speed network
  public static final int MAX_AD_CACHE_COUNT_SLOW = 1;
  public static final int DEFAULT_AD_CACHE_COUNT = 1;

  public static final String DEFAULT_CATEGORY = "common";
  public static final String DEFAULT_NPKEY = "common";

  public static final int DEFAULT_AD_SIZE = 100;
  public static final float ASPECT_RATIO_WIDE_ADS_DEFAULT = 1.91f;
  public static final float ASPECT_RATIO_NATIVE_WIDE = 1.5f;
  public static final float ASPECT_RATIO_VIDEO_MIN = 1.5f;
  public static final float ASPECT_RATIO_PGI_MIN = 0.75f;
  public static final int MIN_VISIBILITY_AUTOPLAY_VIDEO = 20;

  //Ads timeout default (in seconds).
  public static final int ADS_SDK_TIMEOUT_GOOD = 3;
  public static final int ADS_SDK_TIMEOUT_AVERAGE = 6;
  public static final int ADS_SDK_TIMEOUT_SLOW = 12;
  public static final long ADS_DEFAULT_CACHE_TTL = 24 * 60 * 60; //seconds

  public static final String AD_REQ_EXCLUDE_BANNERS = "excludeBanners";
  public static final String AD_REQ_FB_TOKEN = "fbBidderToken";
  public static final String AD_REQ_CONTEXT = "contentContext";
  public static final String AD_REQ_PARENT_CONTEXT = "parentContentContext";
  public static final String AD_REQ_AD_EXTRA = "adExtras";
  public static final String AD_REQ_STATS = "adStatistics";
  public static final String AD_REQ_PERMISSION = "uses-permission";
  public static final String AD_REQ_FCAP = "fcap";
  public static final String AD_REQ_AMAZON_PAYLOAD = "amazonSdkPayload";
  public static final String AD_REQ_LANG_INFO = "langInfo";

  // Dfp custom native ad assets
  public static final String AD_ASSET_HEADLINE = "Headline";
  public static final String AD_ASSET_BODY = "Body";
  public static final String AD_ASSET_CALL_TO_ACTION = "CallToAction";
  public static final String AD_ASSET_ICON = "IconUrl";
  public static final String AD_ASSET_MAIN_IMAGE = "MainImage";
  public static final String AD_ASSET_ADVERTISER = "Advertiser";
  public static final String AD_ASSET_CLICK_URL = "ClickUrl";

  // OM sdk
  public static final String OM_PARTNER_NAME = "Versein";
  public static final String OMID_DEFAULT_JS_VERSION = "1.3.0";
  public static final int OMID_WEBVIEW_DESTROY_DELAY = 1000;


  //AD states from a web ad player
  public static final String AD_LOADED = "ad_loaded";
  public static final String AD_STARTED = "ad_started";
  public static final String AD_RESUMED = "ad_resumed";
  public static final String AD_PAUSED = "ad_paused";
  public static final String AD_PAUSE_BY_TAP = "ad_pausebyTap";
  public static final String AD_CLICK = "ad_click";
  public static final String AD_ENDED = "ad_ended";
  public static final String ALL_ADS_COMPLETE = "ad_all_ended";
  public static final String AD_ERROR = "ad_error";
  public static final String AD_INVALID = "undefined";

  //javascript constants
  public static final String SCRIPT_PLAY = "play();";
  public static final String SCRIPT_PAUSE = "pause();";
  public static final String SCRIPT_MUTE = "setMute(%s);";
  public static final String SCRIPT_AD_IN_VIEW = "onAdInView();";
  public static final String SCRIPT_AD_OUT_VIEW = "onAdOutOfView();";

  //Instream Ads
  public static final String NEWS_GROUP = "NewsGroup";
  public static final String DHTV_GROUP =  "DHTVGroup";
  public static final String BUZZ_GROUP =  "BuzzGroup";
  public static final String COMPANION_SIZES_QUERY_PARAM = "ciu_szs";

  public static final char AD_TOKEN = '-';
  public static final int AD_NEGATIVE_DEFAULT = -1;
  public static final int AD_BANNER_TITLE_DEFAULT_MAXLINES = 1;

  //immersive beacon keys
  public static final String NON_IMMERSIVE_CLICK = "nonImmersiveViewClick";
  public static final String IMMERSIVE_VIEW_ENTER= "immersiveViewEnter";
  public static final String IMMERSIVE_VIEW_CLICK = "immersiveViewClick";
  public static final String IMMERSIVE_VIEW_EXIT = "immersiveViewExit";


  //common bundle keys
  public static final String B_AD_ENTITY = "baseAdEntity";
  public static final String B_AD_POS = "baseADPos";

  public static final String AD_GENERIC_VIEW = "generic_click";

  public static final String AD_CAMPAIGN_PULL_WORK_TAG = "adCampaignsFetch";
  public static final String AD_PROXY_FETCH_ID = "proxy";
  public static final String AD_ACTION_QUERY_PARAM = "action";
  public static final String AD_DESELECT_QUERY_PARAM = "deselect";
  public static final String TRUE = "true";

  public static final int INVALID_FC = -1;

  //Amazon SDK
  public static final String APP_KEY = "cb207faa-ab64-4533-91bf-890c3c201959";
  public static final int DEFAULT_AMAZON_ADS_TTL = 600;
  public static final String AMAZON_HOST = "amzn_h";
  public static final String AMAZON_PRICEPOINTS = "amznslots";
  public static final String AMAZON_BID_ID = "amzn_b";
  public static final String AMAZON_HASHED_BIDDER_ID = "amznp";
  public static final String AMAZON_DATA_CENTER = "dc";

  //Error Screenshot
  public static final String SCREENSHOT_DIR = "errorImages";
  public static final String ERROR_REQ_BODY_ERROR_CODE = "errorCode";
  public static final String ERROR_REQ_BODY_ERROR_MESSAGE = "errorMessage";
  public static final String ERROR_REQ_BODY_URL = "url";
  public static final String ERROR_REQ_BODY_PLAYER_ERROR_CODE = "playerErrorCode";
  public static final String ERROR_REQ_BODY_PLAYER_ERROR_MESSAGE = "playerErrorMessage";
  public static final String ERROR_REQ_BODY_PLAYER_ERROR_TYPE = "playerErrorType";
  public static final Long SCREENSHOT_CAPTURE_DELAY = 2000L;

  //instream ads pod simplification
  public static final String PRE_ROLL_POD = "start";
  public static final String MID_ROLL_POD = "mid";
  public static final String POST_ROLL_POD = "end";


  // sdk ads reusing external ad container removes non-dh child views inside container. check for this tag before removing any child
  public static final String DH_VIEW_TAG = "dh_view_tag";

  //Evergreen ads
  public static final String DEFAULT_ADTAG = "DEFAULT";
  public static final int SPLASH_DEFAULT_SHOW_COUNT = 3;
  public static final int SPLASH_DEFAULT_SPAN = 2;
}
