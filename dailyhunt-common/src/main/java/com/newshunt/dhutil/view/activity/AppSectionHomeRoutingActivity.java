/*
 * Copyright (c) 2016 Newshunt. All rights reserved.
 */
package com.newshunt.dhutil.view.activity;

import android.os.Bundle;

import com.newshunt.dataentity.analytics.referrer.PageReferrer;
import com.newshunt.common.helper.common.Constants;
import com.newshunt.common.helper.common.Logger;
import com.newshunt.dataentity.common.model.entity.AppSection;
import com.newshunt.common.view.customview.NHBaseActivity;
import com.newshunt.common.view.view.UniqueIdHelper;
import com.newshunt.dhutil.R;
import com.newshunt.dhutil.helper.appsection.AppSectionsProvider;
import com.newshunt.deeplink.navigator.CommonNavigator;
import com.newshunt.dataentity.dhutil.model.entity.appsection.AppSectionsResponse;

/**
 * @author santhosh.kc
 */
public class AppSectionHomeRoutingActivity extends NHBaseActivity {

  private static final String ACTIVITY_ID = "ACTIVITY_ID";

  private int activityId;
  private PageReferrer pageReferrer;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    if (savedInstanceState != null) {
      activityId = savedInstanceState.getInt(ACTIVITY_ID);
    } else {
      activityId = UniqueIdHelper.getInstance().generateUniqueId();
    }

    setContentView(R.layout.activity_app_section_home_router);

    if (getIntent() != null) {
      pageReferrer =
          (PageReferrer) getIntent().getSerializableExtra(Constants.BUNDLE_ACTIVITY_REFERRER);
    }
  }

  @Override
  public void onResume() {
    super.onResume();
    AppSectionsProvider.INSTANCE.getAppSectionsObserver()
        .observe(this, this::startAppSectionRouting);
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    try {
      super.onSaveInstanceState(outState);
      outState.putInt(ACTIVITY_ID, activityId);
    } catch (Exception e) {
      Logger.caughtException(e);
    }
  }

  private void startAppSectionRouting(AppSectionsResponse appSectionsResponse) {
    if (getIntent() == null) {
      finish();
      return;
    }

    String appSectionString = getIntent().getStringExtra(Constants.APP_SECTION);
    AppSection appSection = AppSection.fromName(appSectionString);

    CommonNavigator.launchSectionHome(this, appSection, pageReferrer, false);
    finish();
  }
}
