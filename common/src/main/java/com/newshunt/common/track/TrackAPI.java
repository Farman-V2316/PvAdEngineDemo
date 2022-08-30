/**
 * Copyright (c) 2017 Newshunt. All rights reserved.
 */
package com.newshunt.common.track;


import io.reactivex.Completable;
import retrofit2.http.GET;
import retrofit2.http.Url;

/**
 * @author shrikant.agrawal
 */
public interface TrackAPI {

  @GET()
  Completable sendTrack(@Url String url);
}
