/*
 * Copyright (c) 2016 Newshunt. All rights reserved.
 */
package com.newshunt.news.di;

import com.newshunt.news.model.internal.service.MenuService;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Component for singletons in news-app module
 * @author satosh.dhanyamraju
 */
@Component(modules = {NewsAppModule.class, AppModule.class, MenuModule.class})
@Singleton
public interface NewsAppComponent extends AppComponent {

  MenuService dislikeService();

}
