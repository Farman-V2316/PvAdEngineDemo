/*
 * Copyright (c) 2016 Dailyhunt. All rights reserved.
 */

package com.newshunt.dataentity.common.model.entity.cachedapi;

/**
 * @author arun.babu
 */
public interface CachedApiCallbackAfterDataReceived<T extends com.newshunt.dataentity.common.model.entity.cachedapi.CachedApiData> {

  void onSuccess(T data);
}
