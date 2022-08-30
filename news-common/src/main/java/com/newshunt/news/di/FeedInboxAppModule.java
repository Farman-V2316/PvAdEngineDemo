/*
 * Copyright (c) 2016 Newshunt. All rights reserved.
 */
package com.newshunt.news.di;

import com.newshunt.common.helper.cachedapi.CachedApiCache;
import com.newshunt.news.util.NewsConstants;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Provides singletons
 * @author satosh.dhanyamraju
 */

@Module
public class FeedInboxAppModule {
  @Provides
  @Singleton
  @Named("feed")
  public CachedApiCache cachedApiCache() {
    return new CachedApiCache(NewsConstants.HTTP_FEED_CACHE_DIR);
  }
}