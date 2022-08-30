/*
 * Copyright (c) 2016 Newshunt. All rights reserved.
 */
package com.newshunt.common.helper.cachedapi;

import com.newshunt.common.helper.common.BaseErrorBuilder;
import com.newshunt.common.helper.common.Constants;
import com.newshunt.common.helper.common.Logger;
import com.newshunt.dataentity.common.model.entity.ErrorTypes;

import java.util.concurrent.Callable;

import io.reactivex.Completable;
import io.reactivex.Observable;

/**
 * @author satosh.dhanyamraju
 */
public class CachedApiCacheRx {
  private static final String LOG_TAG = "CachedApiCacheRx";
  private CachedApiCache cachedApiCache;

  public CachedApiCacheRx(CachedApiCache cachedApiCache) {
    this.cachedApiCache = cachedApiCache;
  }

  public Observable<Boolean> addOrUpdate(final String key, final byte[] data) {
    Logger.d(LOG_TAG, "addOrUpdate: " + key);
    return Observable.fromCallable(() -> {
      cachedApiCache.addOrUpdate(key, data);
      return true;
    });
  }

  public Observable<byte[]> get(final String key) {
    Logger.d(LOG_TAG, "get: " + key);
    return Observable.fromCallable(() -> {
      try {
        byte[] bytes;
        try {
          bytes = cachedApiCache.get(key);
        } catch (Exception e) {
          Logger.e(LOG_TAG, "call: " + e.getMessage());
          bytes = null;
        }
        if (bytes == null) {
          throw new Exception(Constants.NOT_FOUND_IN_CACHE);
        }
        return bytes;
      } catch (Exception e) {
        throw  BaseErrorBuilder.getBaseError(ErrorTypes.NOT_FOUND_IN_CACHE,
                Constants.NOT_FOUND_IN_CACHE);
      }
    });
  }

  public Completable remove(final String key) {
    return Completable.fromCallable(new Callable<Void>() {
      @Override
      public Void call() throws Exception {
        cachedApiCache.remove(key);
        return null;
      }
    });
  }

  public Observable<Boolean> clear() {
    return Observable.fromCallable(new Callable<Boolean>() {
      @Override
      public Boolean call() throws Exception {
        cachedApiCache.clear();
        return true;
      }
    });
  }

  public long size() {
    return cachedApiCache.size();
  }

  public boolean isFull() {
    return cachedApiCache.isFull();
  }

  public static String urlToKey(String url) {
    CacheApiKeyBuilder cacheApiKeyBuilder = new CacheApiKeyBuilder();
    cacheApiKeyBuilder.addParams("url", url);
    return cacheApiKeyBuilder.build();
  }

}
