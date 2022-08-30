/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.analytics.helper;

import com.newshunt.common.helper.common.Logger;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

/**
 * Decodes referrer parameters.
 *
 * @author shreyas.desai
 */
public class ReferrerDecoder {

  public static Map<String, String> getCampaignParams(String referrer) {
    String decodedString = getDecodedString(referrer);

    // Check double encoded string
    if (!decodedString.contains("&")) {
      decodedString = getDecodedString(decodedString);
    }

    Map<String, String> map = new HashMap<>();
    String[] parameters = decodedString.split("&");
    for (String param : parameters) {
      int idx = param.indexOf("=");
      if (idx == -1 || idx > param.length()) {
        continue;
      }
      try {
        map.put(URLDecoder.decode(param.substring(0, idx), "UTF-8"),
            URLDecoder.decode(param.substring(idx + 1), "UTF-8"));
      } catch (UnsupportedEncodingException e) {
        Logger.caughtException(e);
      } catch (IllegalArgumentException e) {
        Logger.caughtException(e);
      }
    }
    return map;
  }

  private static String getDecodedString(String referrer) {
    String decodedString = referrer;
    try {
      decodedString = URLDecoder.decode(referrer, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      Logger.caughtException(e);
    } catch (IllegalArgumentException e) {
      Logger.caughtException(e);
    }
    return decodedString;
  }

}
