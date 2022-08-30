/**
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */
package com.newshunt.dataentity.dhutil.model.entity.upgrade;

import java.io.Serializable;

/**
 * @author satosh.dhanyamraju
 */
public class Value implements Serializable {
  private static final long serialVersionUID = 4671646361656670393L;
  private String slow;
  private String fast;

  public String getSlow() {
    return slow;
  }

  public void setSlow(String slow) {
    this.slow = slow;
  }

  public String getFast() {
    return fast;
  }

  public void setFast(String fast) {
    this.fast = fast;
  }
}
