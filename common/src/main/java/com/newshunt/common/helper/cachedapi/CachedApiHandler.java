/*
 * Copyright (c) 2016 Dailyhunt. All rights reserved.
 */

package com.newshunt.common.helper.cachedapi;

import android.os.Handler;
import android.os.Looper;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.newshunt.common.helper.common.AndroidUtils;
import com.newshunt.common.helper.common.BusProvider;
import com.newshunt.common.helper.common.Logger;
import com.newshunt.common.helper.common.TrackEvent;
import com.newshunt.dataentity.common.asset.PostEntity;
import com.newshunt.dataentity.common.model.entity.cachedapi.CachedApiCallbackAfterDataReceived;
import com.newshunt.dataentity.common.model.entity.cachedapi.CachedApiData;
import com.newshunt.dataentity.common.model.entity.cachedapi.CachedApiEntity;
import com.newshunt.dataentity.common.model.entity.cachedapi.CachedApiResponseSource;
import com.newshunt.dataentity.common.model.entity.model.ApiResponse;

import java.lang.reflect.Type;
import java.util.concurrent.Executor;

/**
 * @author arun.babu
 */
public class CachedApiHandler<T extends CachedApiData>
    implements CachedApiCallbackAfterDataReceived<T> {

  private static final String TAG = CachedApiHandler.class.getName();
  private static final Gson GSON = new Gson();
  private static final Handler HANDLER = new Handler(Looper.getMainLooper());
  private static final Executor CACHE_EXECUTOR =
      AndroidUtils.newSingleThreadExecutor("CachedExecutor");
  private static final CachedApiCache CACHED_API_CACHE = CachedApiCache.getInstance();

  private final CachedApiEntity cachedApiEntity;
  private final CachedApiService<T> cachedApiService;
  private final Type typeOfT;

  private T data;

  public CachedApiHandler(CachedApiEntity cachedApiEntity, CachedApiService cachedApiService,
                          Type typeOfT) {
    this.cachedApiEntity = cachedApiEntity;
    this.cachedApiService = cachedApiService;
    this.typeOfT = typeOfT;
  }

  public void getCachedApiData() {
    if (null == cachedApiEntity || null == typeOfT || null == cachedApiService ||
        null == CACHE_EXECUTOR) {
      return;
    }

    Logger.i(TAG, String.format("getCachedApiData %s %s", cachedApiEntity.getKey(),
        cachedApiEntity.getCacheType()));

    CACHE_EXECUTOR.execute(new GetDataTask());
  }

  @Override
  public void onSuccess(T data) {
    if (null == data) {
      sendData(null, CachedApiResponseSource.NETWORK);
      return;
    }
    this.data = data;
    data.setCachedApiResponseSource(CachedApiResponseSource.NETWORK);
    CACHE_EXECUTOR.execute(new SetDataTask());
  }

  private boolean sendDataFromCache() {
    try {
      byte[] zippedData = CACHED_API_CACHE.get(cachedApiEntity.getKey());
      String json = CacheCompressUtils.decompress(zippedData);
      T data = GSON.fromJson(json, typeOfT);
      if (data != null) {
        data.setCachedApiResponseSource(CachedApiResponseSource.DISK_CACHE);
        sendData(data, CachedApiResponseSource.DISK_CACHE);
        BusProvider.getRestBusInstance().post(new TrackEvent(data, true));
        return true;
      }
    } catch (Exception e) {
      Logger.caughtException(e);
    }
    return false;
  }

  public T getDataFromCache() {
    try {
      byte[] zippedData = CACHED_API_CACHE.get(cachedApiEntity.getKey());
      String json = CacheCompressUtils.decompress(zippedData);
      T data = GSON.fromJson(json, typeOfT);
      if (data != null) {
        data.setCachedApiResponseSource(CachedApiResponseSource.DISK_CACHE);
        return data;
      }
    } catch (Exception e) {
      Logger.caughtException(e);
    }
    return null;
  }

  private void sendData(@Nullable final T data, final CachedApiResponseSource responseSource) {
    cachedApiService.sendData(data, responseSource);
  }

  private void notInCache() {
    HANDLER.post(cachedApiService::notInCache);
  }

  private void updateCacheAndSendData() {
    if (null == data || null == cachedApiEntity.getKey()) {
      return;
    }

    try {
      String json = GSON.toJson(data);
      byte[] zippedData = CacheCompressUtils.compress(json);
      CACHED_API_CACHE.addOrUpdate(cachedApiEntity.getKey(), zippedData);
    } catch (Exception e) {
      Logger.caughtException(e);
    } finally {
      sendData(data, CachedApiResponseSource.NETWORK);
    }
  }

  private class GetDataTask implements Runnable {

    @Override
    public void run() {
      switch (cachedApiEntity.getCacheType()) {
        case NO_CACHE:
          cachedApiService.getData(CachedApiHandler.this);
          break;

        case USE_CACHE_AND_UPDATE:
          if (!sendDataFromCache()) {
            notInCache();
          }
          cachedApiService.getData(CachedApiHandler.this);
          break;

        case USE_NETWORK_IF_NO_CACHE:
          boolean cacheDataSent = sendDataFromCache();
          if (!cacheDataSent) {
            cachedApiService.getData(CachedApiHandler.this);
          }
          break;

        case NO_NETWORK:
          if (!sendDataFromCache()) {
            notInCache();
          }
          break;
        case IGNORE_CACHE_AND_UPDATE:
          notInCache();
          cachedApiService.getData(CachedApiHandler.this);
          break;
      }
    }
  }

  private class SetDataTask implements Runnable {

    @Override
    public void run() {
      updateCacheAndSendData();
    }
  }
}