package com.dailyhunt.tv.players.utils;

import com.dailyhunt.tv.players.interfaces.PlayerAnalyticCallbacks;
import com.newshunt.common.helper.appconfig.AppConfig;
import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.common.helper.common.Constants;
import com.newshunt.common.helper.common.Logger;
import com.newshunt.common.helper.info.ClientInfoHelper;
import com.newshunt.common.helper.info.ConnectionInfoHelper;
import com.newshunt.dhutil.helper.preference.UserPreferenceUtil;
import com.newshunt.dataentity.news.model.entity.server.asset.ExoPlayerAsset;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.concurrent.TimeUnit;

/**
 * Url utils for adding custom parameters to the AD Url
 *
 * @author ranjith
 */

public class PlayerAdUrlUtils {

  private static final String CUSTOM_PARAM_KEY = "&cust_params=";

  private static final String TV_CHANNEL_KEY = "ContentChannelKey";
  private static final String TV_CATEGORY_KEY = "ContentCategory";
  private static final String TV_LANGUAGE_KEY = "Contentlanguage";
  private static final String TV_DURATION_KEY = "ContentDuration";
  private static final String TV_SOURCE_KEY = "ContentSourceKey";
  private static final String TV_SIZE_KEY = "ContentSize";
  private static final String TV_TITLE_KEY = "ContentTitle";
  private static final String TV_KEYWORDS_KEY = "ContentKeywords";
  private static final String TV_APP_VERSION = "Appversion";
  private static final String TV_USER_SELECTED_LANGUAGES = "UserLanguages";
  private static final String TV_CLIENT_ID = "ClientId";
  private static final String TV_CONNECTION_TYPE = "ConnectionType";

  private static final String AD_CMSID = "cmsid";
  private static final String AD_VID = "vid";
  private static final String AD_DESCRIPTION_URL = "description_url";


  private static final long TV_DURATION_B1 = 1L;
  private static final long TV_DURATION_B2 = 5L;
  private static final long TV_DURATION_B3 = 15L;
  private static final long TV_DURATION_B4 = Long.MAX_VALUE;


  private static int durationBucketIndex(ExoPlayerAsset item) {
    if (item.getDurationLong() < 0) {
      return 0;
    }

    long minutes = TimeUnit.MILLISECONDS.toMinutes(item.getDurationLong());

    if (minutes >= 0L && minutes <= TV_DURATION_B1) {
      return 0;
    } else if (minutes > TV_DURATION_B1 && minutes <= TV_DURATION_B2) {
      return 1;
    } else if (minutes > TV_DURATION_B2 && minutes <= TV_DURATION_B3) {
      return 2;
    } else {
      return 3;
    }
  }

  /**
   * Add / replace cmsid,vid and description_url
   *
   * @param item
   * @param adUrl
   * @return
   */
  public static String replaceAdparams(ExoPlayerAsset item, String adUrl) {
    if (item == null) {
      return adUrl;
    }
    StringBuilder customParams = new StringBuilder();
    try {
      String cmsId = Constants.BIT_AND + AD_CMSID + Constants.EQUAL_CHAR;
      String vid = Constants.BIT_AND + AD_VID + Constants.EQUAL_CHAR;
      String description_url = Constants.BIT_AND + AD_DESCRIPTION_URL + Constants.EQUAL_CHAR;
      String description_url_Value = "[description_url]";
      String decodedUrl = adUrl;
      if (!CommonUtils.isEmpty(item.getAdcmsId())) {
        if (!decodedUrl.contains(cmsId)) {
          customParams.append(Constants.BIT_AND);
          customParams.append(AD_CMSID);
          customParams.append(Constants.EQUAL_CHAR);
          customParams.append(URLEncoder.encode(item.getAdcmsId(), "UTF-8"));
        }
      }
      if (!CommonUtils.isEmpty(item.getAdvId())) {
        if (!decodedUrl.contains(vid)) {
          customParams.append(Constants.BIT_AND);
          customParams.append(AD_VID);
          customParams.append(Constants.EQUAL_CHAR);
          customParams.append(URLEncoder.encode(item.getAdvId(), "UTF-8"));
        }
      }
      if (!CommonUtils.isEmpty(item.getAdDescriptionUrl())) {
        if (!decodedUrl.contains(description_url)) {
          customParams.append(Constants.BIT_AND);
          customParams.append(AD_DESCRIPTION_URL);
          customParams.append(Constants.EQUAL_CHAR);
          customParams.append(URLEncoder.encode(item.getAdDescriptionUrl(), "UTF-8"));
        } else if (decodedUrl.contains(description_url_Value)) {
          decodedUrl = decodedUrl.replace(description_url_Value,
              URLEncoder.encode(item.getAdDescriptionUrl(), "UTF-8"));
        }
      }
      return decodedUrl + customParams.toString();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return adUrl;
  }


  public static String buildTargetAdUrl(ExoPlayerAsset item, String baseAdUrl, String keyMapping,
                                        PlayerAnalyticCallbacks playerAnalyticCallbacks) {
    if (CommonUtils.isEmpty(keyMapping)) {
      return baseAdUrl;
    }

    try {
      String decodedMapping = URLDecoder.decode(keyMapping, "UTF-8");
      String keys[] = decodedMapping.split(";");

      StringBuilder customParams = new StringBuilder();
      for (String key : keys) {

        //Special handling for Duration , rest is pattern matching ..
        if (key.startsWith(TV_DURATION_KEY)) {
          String keyValue = key.substring(TV_DURATION_KEY.length() + 1);
          String[] values = keyValue.split(Constants.COMMA_CHARACTER);

          customParams.append(TV_DURATION_KEY);
          customParams.append(Constants.EQUAL_CHAR);
          customParams.append(values[durationBucketIndex(item)]);
          customParams.append(Constants.BIT_AND);
          continue;
        }

        //Rest Cases for keys ...
        switch (key) {
          case TV_LANGUAGE_KEY:
            if (playerAnalyticCallbacks != null) {
              customParams.append(TV_LANGUAGE_KEY);
              customParams.append(Constants.EQUAL_CHAR);

              if (playerAnalyticCallbacks.getLanguageKey() != null) {
                customParams.append(playerAnalyticCallbacks.getLanguageKey());
              }

              customParams.append(Constants.BIT_AND);
            }
            break;
          case TV_SOURCE_KEY:
            customParams.append(TV_SOURCE_KEY);
            customParams.append(Constants.EQUAL_CHAR);
            if (item.getSourceInfo() != null) {
              customParams.append(item.getSourceInfo().getSourceName());
            }
            customParams.append(Constants.BIT_AND);
            break;
          case TV_APP_VERSION:
            customParams.append(TV_APP_VERSION);
            customParams.append(Constants.EQUAL_CHAR);
            customParams.append(AppConfig.getInstance().getAppVersion());
            customParams.append(Constants.BIT_AND);
            break;
          case TV_USER_SELECTED_LANGUAGES:
            customParams.append(TV_USER_SELECTED_LANGUAGES);
            customParams.append(Constants.EQUAL_CHAR);
            String userLanguages = UserPreferenceUtil.getUserLanguages();
            if (!CommonUtils.isEmpty(userLanguages)) {
              userLanguages = userLanguages.replaceAll(Constants.COMMA_CHARACTER, Constants
                  .UNDERSCORE_CHARACTER);
            }
            customParams.append(userLanguages);
            customParams.append(Constants.BIT_AND);
            break;
          case TV_CLIENT_ID:
            customParams.append(TV_CLIENT_ID);
            customParams.append(Constants.EQUAL_CHAR);
            customParams.append(ClientInfoHelper.getClientId());
            customParams.append(Constants.BIT_AND);
            break;
          case TV_CONNECTION_TYPE:
            customParams.append(TV_CONNECTION_TYPE);
            customParams.append(Constants.EQUAL_CHAR);
            customParams.append(ConnectionInfoHelper.getConnectionType());
            customParams.append(Constants.BIT_AND);
            break;
          case TV_SIZE_KEY:
          case TV_TITLE_KEY:
          case TV_KEYWORDS_KEY:
          default:
            break;
        }
      }

      String encodedParams = URLEncoder.encode(customParams.toString(), "UTF-8");
      if (CommonUtils.isEmpty(customParams.toString())) {
        return baseAdUrl;
      }

      return baseAdUrl + CUSTOM_PARAM_KEY + encodedParams;
    } catch (Exception ex) {
      Logger.caughtException(ex);
      return baseAdUrl;
    }
  }
}
