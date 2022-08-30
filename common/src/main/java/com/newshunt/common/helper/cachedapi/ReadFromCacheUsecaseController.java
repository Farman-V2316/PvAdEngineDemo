/*
 * Copyright (c) 2017 Newshunt. All rights reserved.
 */
package com.newshunt.common.helper.cachedapi;

import com.google.gson.Gson;
import com.newshunt.common.helper.common.Logger;
import com.newshunt.dataentity.common.model.entity.cachedapi.CachedApiData;
import com.newshunt.dataentity.common.model.entity.cachedapi.CachedApiResponseSource;

import java.io.IOException;
import java.lang.reflect.Type;

import io.reactivex.Observable;

/**
 * @author satosh.dhanyamraju
 */
public class ReadFromCacheUsecaseController<T extends CachedApiData> implements ReadFromCacheUsecase<T> {
  private static final Gson GSON = new Gson();
  private final CachedApiCacheRx cache;
  private final Type typeOfT;

  public ReadFromCacheUsecaseController(CachedApiCacheRx cache, Type typeOfT) {
    this.cache = cache;
    this.typeOfT = typeOfT;
  }

  @Override
  public Observable<T> get(String url) {
    return cache.get(CachedApiCacheRx.urlToKey(url)).map(this::decompress);
  }

  private T decompress(byte[] d) {
    try {
      String json = null;
      json = CacheCompressUtils.decompress(d);
      T data = GSON.fromJson(json, typeOfT);
      data.setCachedApiResponseSource(CachedApiResponseSource.DISK_CACHE);
      return data;
    } catch (IOException e) {
      Logger.caughtException(e);
    }
    return null;
  }

  @Override
  public void execute() {

  }
}
