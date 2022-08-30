/*
 * Copyright (c) 2017 Newshunt. All rights reserved.
 */
package com.newshunt.common.helper.cachedapi;

import com.newshunt.common.domain.Usecase;

import io.reactivex.Observable;

/**
 * @author satosh.dhanyamraju
 */
public interface ReadFromCacheUsecase<T> extends Usecase {
  Observable<T> get(String url);
}
