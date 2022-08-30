/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */
package com.newshunt.sso.model.internal.restadapter;

import com.newshunt.common.model.retrofit.RestAdapters;
import com.newshunt.sdk.network.Priority;

import retrofit2.Retrofit;

/**
 * @author arun.babu
 */
public class SSORestAdapter {

  //TODO: Retrofit update(unnecessary)
  public static Retrofit getSSORestAdapter(String ssoUrl, Priority priority, Object tag) {
    return RestAdapters.getBuilder(ssoUrl, false, priority, tag).build();
  }
}
