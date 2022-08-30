/*
 * Copyright (c) 2017 Newshunt. All rights reserved.
 */
package com.newshunt.news.helper;

import com.newshunt.common.helper.common.UrlUtil;
import com.newshunt.dhutil.helper.preference.UserPreferenceUtil;
import com.newshunt.dhutil.helper.retrofit.NewsBaseUrlContainer;
import com.newshunt.helper.ImageUrlReplacer;
import com.newshunt.news.util.NewsConstants;

import java.util.HashMap;
import java.util.Map;

import androidx.core.util.Pair;

/**
 * An Utility class for News Detail
 *
 * @author santhosh.kc
 */
public class NewsDetailUtil {

  public static String getCdnContentUrl(String v4SwipeUrl) {
    String baseUrl = NewsBaseUrlContainer.getApplicationRelativeUrl();
    String contentUrl = UrlUtil.appendPath(baseUrl, v4SwipeUrl);
    Map<String, String> paramsMap = new HashMap<>();
    paramsMap.put(NewsConstants.QUERY_PARAMETER_LANGUAGE_CODE,
        UserPreferenceUtil.getUserLanguages());
    paramsMap.put(NewsConstants.QUERY_PARAMETER_EDITION,
        UserPreferenceUtil.getUserEdition());
    paramsMap.put(NewsConstants.QUERY_PARAMETER_APP_LANGUAGE,
        UserPreferenceUtil.getUserNavigationLanguage());
    return UrlUtil.getUrlWithQueryParamns(contentUrl, paramsMap);
  }


  public static String modifyImageTagsInHTMLContent(String html) {
    String replacementMacro;

    Pair<Integer, Integer> newsDetailDimension = NewsListCardLayoutUtil
        .getNewsDetailMastHeadImageDimension();
    replacementMacro = ImageUrlReplacer.getDimensionString(newsDetailDimension.first) +
        ImageUrlReplacer.RESOLUTION_CHARACTER + ImageUrlReplacer.getDimensionString
        (newsDetailDimension.second);

    return ImageUrlReplacer.replaceHTMLWithImageMacro(html, replacementMacro);
  }

}
