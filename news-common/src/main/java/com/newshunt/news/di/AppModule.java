/*
 * Copyright (c) 2017 Newshunt. All rights reserved.
 */
package com.newshunt.news.di;

import android.content.Context;
import android.os.Bundle;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.newshunt.common.helper.common.BusProvider;
import com.newshunt.common.helper.common.Constants;
import com.newshunt.common.helper.common.Logger;
import com.newshunt.common.helper.preference.PreferenceManager;
import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.dhutil.helper.preference.AppStatePreference;
import com.newshunt.news.di.qualifiers.AdBus;
import com.newshunt.news.di.qualifiers.AppContext;
import com.newshunt.news.di.qualifiers.RestBus;
import com.newshunt.news.di.qualifiers.UiBus;
import com.newshunt.news.helper.DislikeStoryHelper;
import com.newshunt.news.model.sqlite.SocialDB;
import com.newshunt.news.model.usecase.BundleUsecase;
import com.newshunt.news.model.usecase.CleanupDBOnAppEventUsecase;
import com.newshunt.news.model.usecase.MediatorUsecase;
import com.newshunt.news.model.usecase.MediatorUsecaseKt;
import com.squareup.otto.Bus;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import kotlin.Unit;

/**
 * Provides singletons
 * @author satosh.dhanyamraju
 */
@Module
public class AppModule {
  private Context appContext;
  // TODO(satosh.dhanyamraju): dependency. remove getInstance from provided classes

  public AppModule(Context appContext) {
    this.appContext = appContext.getApplicationContext();
  }

  @Provides @Singleton @AppContext Context context() {
    return appContext;
  }

  @Provides @Singleton @UiBus
  Bus uiBus() {
    // TODO(satosh.dhanyamraju): dependency. remove singleton / static constructs
    return BusProvider.getUIBusInstance();
  }

  @Provides @Singleton @RestBus
  Bus restBus() {
    return BusProvider.getUIBusInstance();
  }

  @Provides @Singleton @AdBus
  Bus adBus() {
    return BusProvider.getUIBusInstance();
  }

  @Provides
  @Singleton
  DislikeStoryHelper cardEventListener(@UiBus Bus bus) {
    String dislikedIds = PreferenceManager.getPreference(AppStatePreference
        .DISLIKED_STORY_IDS, Constants.EMPTY_STRING);
    Type type = new TypeToken<Map<DislikeStoryHelper.MapKey, DislikeStoryHelper.MapValue>>() {
    }.getType();
    Logger.d("CardEvent", "cardEventListener: " + dislikedIds);
    Map<DislikeStoryHelper.MapKey, DislikeStoryHelper.MapValue> defaultMap = new HashMap<>();
    Map<DislikeStoryHelper.MapKey, DislikeStoryHelper.MapValue> dislikeMap = CommonUtils.isEmpty
        (dislikedIds) ? defaultMap : new Gson().fromJson(dislikedIds, type);
    return new DislikeStoryHelper(dislikeMap);
  }

  @Provides
  @Singleton
  SocialDB appDB() {
    return SocialDB.instance();
  }

  @Provides
  @Singleton
  @Named("cleanupDbOnAppEventUc")
  MediatorUsecase<Bundle, Unit> cleanupDBOnAppEvent(SocialDB socialDB) {
    return MediatorUsecaseKt.toMediator2(new CleanupDBOnAppEventUsecase(socialDB));
  }

  @Provides
  @Singleton
  @Named("cleanupDbOnAppEventBundleUc")
  BundleUsecase<Unit> cleanupDBOnAppEventBundleUseCase(SocialDB socialDB) {
    return new CleanupDBOnAppEventUsecase(socialDB);
  }
}