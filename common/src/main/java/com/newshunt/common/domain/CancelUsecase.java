/**
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */
package com.newshunt.common.domain;

/**
 * Used by BasePresenter to cancel NetworkSDK requests
 * @author satosh.dhanyamraju
 */
public interface CancelUsecase {
  boolean cancel(Object tag);
}
