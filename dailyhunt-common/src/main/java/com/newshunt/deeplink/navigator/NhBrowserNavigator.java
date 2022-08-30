/*
 * Copyright (c) 2017 Newshunt. All rights reserved.
 */
package com.newshunt.deeplink.navigator;

import android.content.Intent;

import com.newshunt.dataentity.analytics.referrer.PageReferrer;
import com.newshunt.common.helper.common.Constants;
import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.dhutil.helper.common.DailyhuntConstants;

/**
 * Created by anshul on 15/03/17.
 * A navigator for getting intent for NhBrowser Activity.
 */

public class NhBrowserNavigator {
  public static Intent getTargetIntent(PageReferrer pageReferrer) {
    Intent intent = new Intent();
    intent.setAction(DailyhuntConstants.NH_BROWSER_ACTION);
    intent.setPackage(CommonUtils.getApplication().getPackageName());
    intent.putExtra(Constants.BUNDLE_ACTIVITY_REFERRER, pageReferrer);
    return intent;
  }

  public static Intent getTargetIntent() {
    Intent intent = new Intent();
    intent.setAction(DailyhuntConstants.NH_BROWSER_ACTION);
    intent.setPackage(CommonUtils.getApplication().getPackageName());
    return intent;
  }
}
