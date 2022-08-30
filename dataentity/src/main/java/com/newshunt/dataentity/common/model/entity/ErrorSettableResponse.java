/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.dataentity.common.model.entity;

/**
 * Interface to expose the response object to set error only
 *
 * @author maruti.borker
 */
public interface ErrorSettableResponse {
  void setError(BaseError error);
}
