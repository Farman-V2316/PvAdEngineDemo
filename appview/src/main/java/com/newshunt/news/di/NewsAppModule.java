/*
 * Copyright (c) 2016 Newshunt. All rights reserved.
 */
package com.newshunt.news.di;

import android.util.Pair;

import com.google.gson.reflect.TypeToken;
import com.newshunt.common.helper.common.AndroidUtils;
import com.newshunt.dhutil.helper.preference.AppStatePreference;
import com.newshunt.news.model.internal.service.NewsAppJsProviderServiceImpl;
import com.newshunt.news.model.service.NewsAppJSProviderService;

import java.util.HashMap;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.reactivex.Observable;
import io.reactivex.functions.Consumer;

/**
 * Provides singletons used in newsapp module
 *
 * @author satosh.dhanyamraju
 */
@Module
public class NewsAppModule {
  private final Pair<Consumer<Pair<String, Long>>, Observable<HashMap<String, Long>>> tab1stNwPgTrack;

  public NewsAppModule(){
    final HashMap<String, Long> map = new HashMap<>();
    tab1stNwPgTrack = AndroidUtils.buildAutoSaveMapForPref(map,
        AppStatePreference.NEWSHOME_TAB_LAST_ACCESS_TIME,
        new TypeToken<HashMap<String, Long>>(){}.getType());
  }

  @Provides
  @Singleton
  NewsAppJSProviderService newsAppJSProviderService() {
    return new NewsAppJsProviderServiceImpl();
  }

}
