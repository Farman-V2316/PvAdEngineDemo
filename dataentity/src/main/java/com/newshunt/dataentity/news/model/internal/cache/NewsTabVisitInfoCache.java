/*
 * Copyright (c) 2017 Newshunt. All rights reserved.
 */
package com.newshunt.dataentity.news.model.internal.cache;

import android.util.LruCache;

import java.io.Serializable;

/**
 * @author santhosh.kc on 11/21/17.
 */
public class NewsTabVisitInfoCache {

  private static int DEFAULT_MAX_SIZE = 50;

  private static boolean sessionAlive = false;

  private InternalCache cache;

  private static NewsTabVisitInfoCache sInstance;

  private NewsTabVisitInfoCache() {
    cache = new InternalCache(DEFAULT_MAX_SIZE);
  }

  public static void init() {
    sessionAlive = true;
  }

  public static NewsTabVisitInfoCache getInstance() {
    if (sInstance == null) {
      synchronized (NewsTabVisitInfoCache.class) {
        if (sInstance == null) {
          sInstance = new NewsTabVisitInfoCache();
        }
      }
    }
    return sInstance;
  }

  public void put(String key, long lastVisitedTime) {
    if (!sessionAlive || key == null) {
      return;
    }
    CacheValue value = new CacheValue();
    value.setLastVisitedTime(lastVisitedTime);
    cache.put(key, value);
  }

  public CacheValue get(String key) {
    return cache.get(key);
  }

  public void clearCache() {
    cache.evictAll();
  }

  public void terminate() {
    clearCache();
    sessionAlive = false;
  }

  private static class InternalCache extends LruCache<String, CacheValue> {

    public InternalCache(int maxSize) {
      super(maxSize);
    }
  }

  public static class CacheValue implements Serializable {
    private static final long serialVersionUID = -2673682908014445896L;
    private long lastVisitedTime;

    public long getLastVisitedTime() {
      return lastVisitedTime;
    }

    public void setLastVisitedTime(long lastVisitedTime) {
      this.lastVisitedTime = lastVisitedTime;
    }
  }
}
