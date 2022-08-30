/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */
package com.newshunt.common.helper.share;

import com.newshunt.common.helper.common.Constants;

/**
 * Helper class to add the relevant html tags and adding font tags.
 *
 * @author chetan.kumar
 */
public class HtmlContentHelper {

  /**
   * Method to get the shared description.
   *
   * @param description Description to share.
   * @return Description with html fonts.
   */
  public static String getShareDescription(String description) {
    if (description == null) {
      return Constants.EMPTY_STRING;
    }
    if (description.length() < Constants.SHARE_DESCRIPTION_SIZE) {
      return description;
    }
    int pIndex = description.indexOf("<p>", Constants.SHARE_DESCRIPTION_SIZE - 1);
    int brIndex = description.indexOf("<br>", Constants.SHARE_DESCRIPTION_SIZE - 1);
    if (pIndex == -1 && brIndex == -1) {
      return description;
    }
    if (pIndex != -1 && brIndex != -1) {
      pIndex = (pIndex < brIndex) ? pIndex : brIndex;
    } else {
      pIndex = (pIndex < 0) ? brIndex : pIndex;
    }
    return description.substring(0, pIndex - 1);
  }
}
