/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.deeplink;

import com.newshunt.common.helper.common.Constants;

/**
 * @author nayana.hs on 12/2/2015.
 *         <p/>
 *         parse the m.dailyhunt.in deeplink pathsegment and extracts the key and Value pair
 *         Ex: https://m.dailyhunt.in/ebooks/hindi/sports-games-books-spogms
 *         pathsegment is "sports-games-books-spogms"
 *         key is books and Value is spogms
 *         <p/>
 *         Also, this class can parse the m.dailyhunt.in deeplink pathsegment and extract the key
 *         and value pair at any position from the last
 *         <p/>
 *         eg: http://m.dailyhunt.in/news/india/english/ifairer-epaper-ifairer/blockbuster-alert-popular-tetris-game-inspires-movie-newsid-54013374-photos-{childid}
 *         if positionFromLast is 0, the key is photos and value is {childid}
 *         if positionFromLast is 1, the key is newsid and value is 54013374
 */
public class DeepLinkPathSegmentParser {
  private String key = Constants.EMPTY_STRING;
  private String value = Constants.EMPTY_STRING;
  private String language = Constants.EMPTY_STRING;
  private static final String PARAM_SEPARATOR = "-";

  public String getValue() {
    return value;
  }

  public String getKey() {
    return key;
  }

  public void parsePathData(String deepLinkPath) {
    parsePathData(deepLinkPath, 0);
  }

  /**
   * Parses the key and value pair from position from last of the link
   * if positionFromLast is 0, then it sets last key value pair,
   * if positionFromLast is 1, then it sets key value pair of last but one
   * if positionFromLast is 2, then it sets key value pair of last but two
   * and so on..
   * <p/>
   * incase the positionFromLast is greater than total number of key value pairs, then key and
   * value pair will not be set
   *
   * @param deepLinkPath     - to be parsed
   * @param positionFromLast - position from Last
   */
  public void parsePathData(String deepLinkPath, int positionFromLast) {
    String[] pathSegmentTokens;

    if (deepLinkPath == null || deepLinkPath.isEmpty()) {
      return;
    } else {
      pathSegmentTokens = deepLinkPath.split(PARAM_SEPARATOR);
    }

    int languagePositionFromLast = 2 * positionFromLast + 3;
    int keyPositionFromLast = 2 * positionFromLast + 2;
    int valuePositionFromLast = 2 * positionFromLast + 1;
    if (pathSegmentTokens.length >= 3 && keyPositionFromLast < pathSegmentTokens.length) {
      key = pathSegmentTokens[pathSegmentTokens.length - keyPositionFromLast];
      value = pathSegmentTokens[pathSegmentTokens.length - valuePositionFromLast];
      language = pathSegmentTokens[pathSegmentTokens.length - languagePositionFromLast];
    }
  }

  public String getLanguage() {
    return language;
  }
}
