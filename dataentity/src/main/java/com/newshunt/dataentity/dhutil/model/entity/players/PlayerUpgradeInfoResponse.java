package com.newshunt.dataentity.dhutil.model.entity.players;


import com.newshunt.dataentity.common.model.entity.BaseError;

import java.io.Serializable;

/**
 * Created by vinod.bc on 7/8/2016.
 */
public class PlayerUpgradeInfoResponse implements Serializable {
  private static final long serialVersionUID = -3363044957364528303L;
  private PlayerUpgradeInfo data;
  private BaseError baseError;

  public PlayerUpgradeInfo getData() {
    return data;
  }

  public void setData(PlayerUpgradeInfo data) {
    this.data = data;
  }

  public BaseError getBaseError() {
    return baseError;
  }

  public void setBaseError(BaseError baseError) {
    this.baseError = baseError;
  }
}
