/*
* Copyright (c) 2016 Newshunt. All rights reserved.
*/
package com.newshunt.dataentity.notification;

import java.io.Serializable;

/**
 * @author neeraj.kumar
 */
public class AdsNavModel extends BaseModel implements Serializable {
  private static final long serialVersionUID = -3129964307467456390L;

  @Override
  public BaseModelType getBaseModelType() {
    return BaseModelType.ADS_MODEL;
  }
}
