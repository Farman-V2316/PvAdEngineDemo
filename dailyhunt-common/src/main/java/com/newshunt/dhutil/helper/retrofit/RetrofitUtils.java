/**
 * Copyright (c) 2016 Newshunt. All rights reserved.
 */
package com.newshunt.dhutil.helper.retrofit;

import com.newshunt.dataentity.common.model.entity.BaseError;
import com.newshunt.dataentity.common.model.entity.cachedapi.CachedApiCallbackAfterDataReceived;
import com.newshunt.dataentity.common.model.entity.cachedapi.CachedApiData;
import com.newshunt.dataentity.common.model.entity.cachedapi.CachedApiServiceCallback;

/**
 * @author shrikant.agrawal
 */
public class RetrofitUtils {

  public static <T extends CachedApiData> CallbackWrapper<T> getRetrofitCallback
      (final CachedApiCallbackAfterDataReceived<T> callback,
       final CachedApiServiceCallback<T> cachedApiServiceCallback) {
    return new CallbackWrapper<T>() {
      @Override
      public void onSuccess(T response) {
        callback.onSuccess(response);
      }

      @Override
      public void onError(BaseError error) {
        cachedApiServiceCallback.failure(error);
      }
    };
  }
}
