/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.deeplink.navigator;

import android.content.Intent;

import com.newshunt.dataentity.notification.AdsNavModel;
import com.newshunt.dataentity.notification.NavigationType;
import com.newshunt.deeplink.navigator.NewsNavigator;

/**
 * Helper class for navigation across the screens.
 *
 * @author neeraj.kumar
 */
public class AdsNavigator {

  public static Intent goToAdsRoutingActivity(AdsNavModel adsNavModel) {
    if (adsNavModel == null) {
      return null;
    }

    NavigationType navigationType = NavigationType.fromIndex(
        Integer.parseInt(adsNavModel.getsType()));
    if (navigationType == null) {
      return null;
    }
    return NewsNavigator.getClearTaskIntentForNewsHome();
  }
}
