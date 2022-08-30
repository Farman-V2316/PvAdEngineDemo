/*
 *
 *  * Copyright (c) 2017 Newshunt. All rights reserved.
 *
 */

package com.newshunt.notification.helper;

import com.newshunt.common.helper.common.Constants;
import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.dhutil.helper.retrofit.NewsBaseUrlContainer;

/**
 * Created by anshul on 30/08/17.
 */

public class NotificationUrlUtil {

  /**
   * This method is used to return complete url. If url starts with http or https, then return
   * it,else form the url and return it.
   *
   * @param url
   * @return
   */
  public static String getCompleteUrl(String url) {
    if (CommonUtils.isEmpty(url) || url.startsWith(Constants.URL_HTTP_FORMAT) || url.startsWith
        (Constants.URL_HTTPS_FORMAT)) {
      return url;
    }

    String applicationUrl = NewsBaseUrlContainer.getApplicationRelativeUrl();
    if (!applicationUrl.endsWith(Constants.FORWARD_SLASH) && !url.startsWith(Constants
        .FORWARD_SLASH)) {
      applicationUrl = applicationUrl + Constants.FORWARD_SLASH;
    }
    return applicationUrl + url;
  }
}
