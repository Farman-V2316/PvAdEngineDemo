/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */

package com.newshunt.common.model.entity.cachedapi;

import androidx.annotation.NonNull;

import com.newshunt.dataentity.common.model.entity.BaseError;
import com.newshunt.dataentity.common.model.entity.cachedapi.CachedApiResponseSource;
import com.newshunt.dataentity.common.model.entity.cachedapi.CachedApiServiceCallback;
import com.newshunt.common.helper.common.AndroidUtils;
import com.newshunt.common.helper.common.Constants;
import com.newshunt.dataentity.common.model.entity.model.ApiResponse;
import com.newshunt.dataentity.common.model.entity.model.Status;

import io.reactivex.subjects.PublishSubject;

/**
 * Adapter class for converting CachedApi callbacks to Observable stream. <br/>
 * Can be passed wherever CachedApiServiceCallback is needed.   It provides a subject which can be subscribed.
 *
 * @author satosh.dhanymaraju
 */

public class CachedApiCallbackRxAdapter<T extends ApiResponse, U>
    implements CachedApiServiceCallback<T> {
  public final PublishSubject<ApiResponse<U>> subject = PublishSubject.create();

  @Override
  public void sendData(T t, CachedApiResponseSource responseSource) {
    AndroidUtils.getMainThreadHandler().post(() -> {
      if (t == null) {
        ApiResponse tApiResponse = notFound(responseSource);
        subject.onNext(tApiResponse); // error signal.
      } else {
        t.setCachedApiResponseSource(responseSource);
        subject.onNext(t);
      }
    });
  }

  @Override
  public void notInCache() {
    AndroidUtils.getMainThreadHandler().post(() -> {
      ApiResponse tApiResponse = notFound(CachedApiResponseSource.DISK_CACHE);
      subject.onNext(tApiResponse);
    });
  }

  @NonNull
  private ApiResponse notFound(CachedApiResponseSource source) {
    ApiResponse tApiResponse = new ApiResponse<>(null);
    tApiResponse.setCachedApiResponseSource(source);
    Status status = new Status(Constants.ERROR_HTTP_NOT_FOUND, Constants.NOT_FOUND_IN_CACHE);
    tApiResponse.setStatus(status);
    return tApiResponse;
  }

  @Override
  public void failure(BaseError error) {
    AndroidUtils.getMainThreadHandler().post(() -> {
      ApiResponse tApiResponse = new ApiResponse<U>(null);
      tApiResponse.setCachedApiResponseSource(CachedApiResponseSource.DISK_CACHE);
      Status status = new Status(error.getStatus(), error.getMessage());
      tApiResponse.setStatus(status);
      subject.onNext(tApiResponse);
    });
  }
}