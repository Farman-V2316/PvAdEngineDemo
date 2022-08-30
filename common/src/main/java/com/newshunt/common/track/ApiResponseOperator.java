/*
 * Copyright (c) 2017 Newshunt. All rights reserved.
 */
package com.newshunt.common.track;

import com.newshunt.common.helper.common.ApiResponseUtils;
import com.newshunt.dataentity.common.model.entity.APIException;
import com.newshunt.dataentity.common.model.entity.BaseError;

import io.reactivex.ObservableOperator;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import retrofit2.Response;

/**
 * Operator to convert throwables from retrofit to BaseError.
 * To be used in all rx based apis with lift().
 *
 * @author: bedprakash.rout on 16/12/16.
 */
public class ApiResponseOperator<T> implements ObservableOperator<T, Response<T>> {

  public static BaseError getError(Throwable t) {
    return ApiResponseUtils.Companion.getError(t);
  }

  @Override
  public Observer<? super Response<T>> apply(Observer<? super T> observer) throws Exception {
    return new ApiResponseObserver<>(observer);
  }

  private class ApiResponseObserver<R> implements Observer<Response<R>> {

    private Observer<? super R> observer;

    public ApiResponseObserver(Observer<? super R> observer) {
      this.observer = observer;
    }

    @Override
    public void onSubscribe(Disposable d) {
      observer.onSubscribe(d);
    }

    @Override
    public void onNext(Response<R> value) {
      if (value != null && value.isSuccessful()) {
        observer.onNext(value.body());
        DailyhuntUtils.fireTrackRequestForApi(value);
      } else {
        BaseError error = ApiResponseUtils.Companion.getError(value);
        onError(new APIException(error));
      }
    }

    @Override
    public void onError(Throwable e) {
      observer.onError(e);
    }

    @Override
    public void onComplete() {
      observer.onComplete();
    }
  }
}
