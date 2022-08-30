/*
 * Copyright (c) 2016 Dailyhunt. All rights reserved.
 */

package com.newshunt.dataentity.common.model.entity.cachedapi;


import com.newshunt.dataentity.common.model.entity.BaseError;

/**
 * @author arun.babu
 */
public interface CachedApiServiceCallback<T extends CachedApiData> {

  /*
   * Send Data to usecase / presenter
   */
  void sendData(T t, CachedApiResponseSource responseSource);

  void notInCache();

  /**
   * Unsuccessful HTTP response due to network failure, non-2XX status code, or unexpected
   * exception.
   */
  void failure(BaseError error);
}
