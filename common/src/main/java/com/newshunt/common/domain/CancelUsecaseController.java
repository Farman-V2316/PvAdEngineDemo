/**
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */
package com.newshunt.common.domain;

import com.newshunt.sdk.network.NetworkSDK;

/**
 * Used by BasePresenter to cancel NetworkSDK requests
 * @author satosh.dhanyamraju
 */
public class CancelUsecaseController implements CancelUsecase {
  @Override
  public boolean cancel(Object tag) {
    return NetworkSDK.cancel(tag);
  }
}
