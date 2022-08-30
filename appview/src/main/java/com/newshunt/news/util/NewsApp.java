/*
 * Copyright (c) 2016 Newshunt. All rights reserved.
 */
package com.newshunt.news.util;

import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.news.di.AppComponent;
import com.newshunt.news.di.AppModule;
import com.newshunt.news.di.DaggerFeedInboxAppComponent;
import com.newshunt.news.di.DaggerNewsAppComponent;
import com.newshunt.news.di.FeedInboxAppComponent;
import com.newshunt.news.di.FeedInboxAppModule;
import com.newshunt.news.di.NewsAppComponent;
import com.newshunt.news.di.NewsAppModule;

/**
 * For storing singleton dagger components throughout the app (workaround for not able to
 * access NewshuntAppController)
 * @author satosh.dhanyamraju
 */
public class NewsApp {
  private static AppComponent appComponent;
  private static NewsAppComponent newsAppComponent;
  private static FeedInboxAppComponent feedInboxAppComponent;

  public static AppComponent appComponent() {
    return appComponent;
  }

  public static void setAppComponent(AppComponent appComponent) {
    NewsApp.appComponent = appComponent;
  }

  public static NewsAppComponent getNewsAppComponent() {
    if (newsAppComponent == null) {
      newsAppComponent = DaggerNewsAppComponent.builder()
          .appModule(new AppModule(CommonUtils.getApplication()))
          .newsAppModule(new NewsAppModule())
          .build();
    }
    newsAppComponent.dislikeService().migrateForUpgradeCases();
    return newsAppComponent;
  }

  public static FeedInboxAppComponent feedAppComponent() {
    if (feedInboxAppComponent == null) {
      feedInboxAppComponent = DaggerFeedInboxAppComponent.builder().feedInboxAppModule(new
          FeedInboxAppModule()).build();
    }
    return feedInboxAppComponent;
  }

}
