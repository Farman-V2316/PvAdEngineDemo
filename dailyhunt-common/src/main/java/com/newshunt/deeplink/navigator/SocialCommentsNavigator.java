/*
 * Copyright (c) 2017 Newshunt. All rights reserved.
 */
package com.newshunt.deeplink.navigator;

import android.content.Context;
import android.content.Intent;
import com.newshunt.common.helper.appconfig.AppConfig;
import com.newshunt.common.helper.common.Constants;
import com.newshunt.dataentity.analytics.referrer.PageReferrer;
import com.newshunt.dataentity.notification.SocialCommentsModel;

/**
 * @author santhosh.kc
 * <p>
 * TODO(santosh.D) to convert into kotlin
 */
public class SocialCommentsNavigator {

  public static Intent getViewAllCommentsIntent(Context context,
                                                SocialCommentsModel socialCommentsModel,
                                                PageReferrer pageReferrer) {
    Intent intent = new Intent(Constants.ALL_COMMENTS_ACTION);
    intent.putExtra(Constants.BUNDLE_COMMENTS_MODEL, socialCommentsModel);
    intent.putExtra(Constants.BUNDLE_ACTIVITY_REFERRER, pageReferrer);
    intent.setPackage(AppConfig.getInstance().getPackageName());
    return intent;
  }
}
