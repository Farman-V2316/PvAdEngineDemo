/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.dataentity.common.model.entity;

import com.newshunt.dataentity.common.model.entity.store.Pageable;
import com.newshunt.dataentity.common.model.entity.store.Pageable;

/**
 * Base Class for all responses.
 * Some data responses will have a uniqueRequestId which will help in differentiating  between
 * subscribers in an eventBus.
 *
 * @author maruti.borker
 */
public class BaseDataResponse extends Pageable implements ErrorSettableResponse {
  private static final long serialVersionUID = 429413295969830794L;

  private int uniqueRequestId = -1;
  private BaseError error = null;

  public BaseDataResponse() {
  }

  public BaseDataResponse(int uniqueRequestId) {
    this.uniqueRequestId = uniqueRequestId;
  }

  public int getUniqueRequestId() {
    return uniqueRequestId;
  }

  public void setUniqueRequestId(int uniqueRequestId) {
    this.uniqueRequestId = uniqueRequestId;
  }

  public BaseError getError() {
    return error;
  }

  public void setError(BaseError error) {
    this.error = error;
  }
}
