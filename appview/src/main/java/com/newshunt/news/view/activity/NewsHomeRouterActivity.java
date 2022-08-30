/*
 * Copyright (c) 2016 Newshunt. All rights reserved.
 */
package com.newshunt.news.view.activity;

import android.content.Intent;
import android.os.Bundle;

import com.newshunt.appview.R;
import com.newshunt.deeplink.navigator.NewsNavigator;
import com.newshunt.deeplink.navigator.NewsHomeRouter;
import com.newshunt.dataentity.notification.BaseModel;

/**
 * A Helper activity to route location or Topic for old versions of notification(V1 and V2) and
 * old command Ads.
 * <p/>
 * This activity will be removed once notification stops support older versions V1 and V2 and Ads
 * stops sending old commands Ads (nhcommand://opentopic:..)
 *
 * @author santhosh.kc
 */
public class NewsHomeRouterActivity extends NewsBaseActivity implements NewsHomeRouter.Callback {

  private NewsHomeRouter newsHomeRouter;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_news_home_router);
    Bundle bundle = getIntent().getExtras();
    createRouter(bundle);
  }

  private void createRouter(Bundle bundle) {
    if (bundle == null) {
      return;
    }

    newsHomeRouter = new NewsHomeRouter(this,bundle);
    newsHomeRouter.setCallback(this);
  }

  @Override
  public void onStart() {
    super.onStart();
    if (newsHomeRouter != null) {
      newsHomeRouter.startRouting();
    }
  }

  @Override
  public void onRoutingSuccess(Intent routedIntent, BaseModel baseModel) {
    if (routedIntent != null) {
      startActivity(routedIntent);
      finish();
    } else {
      onRoutingFailure();
    }
  }

  @Override
  public void onRoutingFailure() {
    NewsNavigator.navigateToHeadlines(this);
    finish();
  }
}
