/*
 * Copyright (c) 2016 Dailyhunt. All rights reserved.
 */

package com.newshunt.dataentity.common.model.entity.cachedapi;

/**
 * @author arun.babu
 */
public class CachedApiEntity {
  private CacheType cacheType = CacheType.NO_CACHE;
  private String key;

  public CachedApiEntity() {
  }

  public void setCacheType(CacheType cacheType) {
    this.cacheType = cacheType;
  }

  public CacheType getCacheType() {
    return cacheType;
  }

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }
}
