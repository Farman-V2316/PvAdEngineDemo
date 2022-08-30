/*
 * Copyright (c) 2016 Newshunt. All rights reserved.
 */
package com.newshunt.news.di;

import android.content.Context;
import android.os.Bundle;

import com.newshunt.news.di.qualifiers.AdBus;
import com.newshunt.news.di.qualifiers.AppContext;
import com.newshunt.news.di.qualifiers.RestBus;
import com.newshunt.news.di.qualifiers.UiBus;
import com.newshunt.news.helper.DislikeStoryHelper;
import com.newshunt.news.model.usecase.BundleUsecase;
import com.newshunt.news.model.usecase.MediatorUsecase;
import com.squareup.otto.Bus;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Component;
import kotlin.Unit;

/**
 * Application level dagger component
 * @author satosh.dhanyamraju
 */
@Singleton
@Component(modules = AppModule.class)
public interface AppComponent {

  @AppContext
  Context context();
  @UiBus
  Bus uiBus();

  @RestBus
  Bus restBus();

  @AdBus
  Bus adBus();

  DislikeStoryHelper cardEventListener();

  @Named("cleanupDbOnAppEventUc")
  MediatorUsecase<Bundle, Unit> cleanupDBOnAppEvent();

  @Named("cleanupDbOnAppEventBundleUc")
  BundleUsecase<Unit> cleanupDBOnAppEventBundleUseCase();

}