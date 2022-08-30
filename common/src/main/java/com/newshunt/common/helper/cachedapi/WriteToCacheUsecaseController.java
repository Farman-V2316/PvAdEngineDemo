/*
 * Copyright (c) 2017 Newshunt. All rights reserved.
 */
package com.newshunt.common.helper.cachedapi;

import com.google.gson.Gson;
import com.newshunt.common.domain.WriteToCacheUsecase;

import java.io.IOException;

import io.reactivex.Completable;
import io.reactivex.Observable;

/**
 * @author satosh.dhanyamraju
 */
public class WriteToCacheUsecaseController<T> implements WriteToCacheUsecase<T> {
  private static final Gson GSON = new Gson();
  private final CachedApiCacheRx cache;

  public WriteToCacheUsecaseController(CachedApiCacheRx cache) {
    this.cache = cache;
  }

  @Override
  public Observable<Boolean> put(String url, T t, boolean writeIfFull) {
    if (!writeIfFull && cache.isFull()) {
      return Observable.just(false);
    }

    try {
      String json = GSON.toJson(t);
      return writeToCache(url, json);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return Observable.just(false);
  }

  @Override
  public Observable<Boolean> writeToCache(String url, String json) throws IOException {
    byte[] zippedData = CacheCompressUtils.compress(json);
    return cache.addOrUpdate(CachedApiCacheRx.urlToKey(url), zippedData);
  }

  public Observable<Boolean> put(String url, T t) {
    return put(url, t, true);
  }

  public Completable remove(String url) {
    return cache.remove(CachedApiCacheRx.urlToKey(url));
  }

  @Override
  public void execute() {

  }
}
