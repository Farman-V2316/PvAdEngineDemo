/*
 * Copyright (c) 2016 Dailyhunt. All rights reserved.
 */

package com.newshunt.common.helper.cachedapi;

import com.newshunt.dataentity.common.model.entity.cachedapi.CachedApiCallbackAfterDataReceived;
import com.newshunt.dataentity.common.model.entity.cachedapi.CachedApiData;
import com.newshunt.dataentity.common.model.entity.cachedapi.CachedApiResponseSource;

/**
 * @author arun.babu
 */
public interface CachedApiService<T extends CachedApiData> {

  void getData(CachedApiCallbackAfterDataReceived<T> callback);

  void sendData(T data, CachedApiResponseSource responseSource);

  void notInCache();
}
