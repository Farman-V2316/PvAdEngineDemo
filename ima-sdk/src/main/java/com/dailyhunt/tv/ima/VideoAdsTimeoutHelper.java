/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.dailyhunt.tv.ima;

import com.newshunt.common.helper.info.ConnectionInfoHelper;

/**
 * Provides function to get timeout for ad request based on connection type.
 *
 * @author neeraj.kumar
 */
public class VideoAdsTimeoutHelper {

  public static int getAdRequestTimeout() {
    String connectionType = ConnectionInfoHelper.getConnectionType();

    if (connectionType == null) {
      return 10;
    }

    if (connectionType.equalsIgnoreCase("w") || connectionType.equalsIgnoreCase("4G")) {
      return 10;

    } else if (connectionType.equalsIgnoreCase("3G") || connectionType.equalsIgnoreCase("3C")) {
      return 15;

    } else if (connectionType.equalsIgnoreCase("2G") || connectionType.equalsIgnoreCase("2C")) {
      return 20;
    }
    return 10;
  }
}
