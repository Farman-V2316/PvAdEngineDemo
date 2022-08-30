/*
 * Copyright (c) 2016 Newshunt. All rights reserved.
 */
package com.newshunt.news.di;

import com.newshunt.common.helper.cachedapi.CachedApiCache;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Component;

/**
 * Application level dagger component
 * @author satosh.dhanyamraju
 */
// TODO(satosh.dhanyamraju): move to existing singletons
@Singleton
@Component(modules = FeedInboxAppModule.class)
public interface FeedInboxAppComponent {
 @Named("feed")
  @Singleton
  CachedApiCache feedCache();
}