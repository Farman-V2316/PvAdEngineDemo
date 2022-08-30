/*
 *
 *  * Copyright (c) 2017 Newshunt. All rights reserved.
 *
 */
package com.newshunt.dataentity.notification.asset;

import com.newshunt.dataentity.common.model.entity.BaseDataResponse;

/**
 * @author  shrikant on 27/08/17.
 */

public class DataStreamResponse extends BaseDataResponse {

  private BaseDataStreamAsset baseStreamAsset;

  public BaseDataStreamAsset getBaseStreamAsset() {
    return baseStreamAsset;
  }

  public void setBaseStreamAsset(BaseDataStreamAsset baseStreamAsset) {
    this.baseStreamAsset = baseStreamAsset;
  }
}
