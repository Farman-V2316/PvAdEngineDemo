/*
 * Copyright (c) 2016 Dailyhunt. All rights reserved.
 */

package com.newshunt.dataentity.common.model.entity.cachedapi;

/**
 * @author arun.babu
 */
public enum CacheType {
  NO_NETWORK,
  USE_NETWORK_IF_NO_CACHE,
  USE_CACHE_AND_UPDATE,
  NO_CACHE,
  IGNORE_CACHE_AND_UPDATE,
  DELAYED_CACHE_AND_NETWORK /*delay cache response while waiting for network. If network error,
   use cache immediately */
}
