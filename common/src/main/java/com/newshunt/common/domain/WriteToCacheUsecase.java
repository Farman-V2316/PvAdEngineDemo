/*
 * Copyright (c) 2017 Newshunt. All rights reserved.
 */
package com.newshunt.common.domain;

import java.io.IOException;

import io.reactivex.Completable;
import io.reactivex.Observable;

/**
 * @author satosh.dhanyamraju
 */
public interface WriteToCacheUsecase<T> extends Usecase {
  Observable<Boolean> put(String url, T t, boolean writeIfFull);

  Observable<Boolean> writeToCache(String url, String json) throws IOException;

  Observable<Boolean> put(String url, T t);

  Completable remove(String url);
}
