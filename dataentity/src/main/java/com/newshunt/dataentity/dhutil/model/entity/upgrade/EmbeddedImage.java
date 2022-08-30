/**
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */
package com.newshunt.dataentity.dhutil.model.entity.upgrade;

import java.io.Serializable;

/**
 * @author satosh.dhanyamraju
 */
public class EmbeddedImage implements Serializable {
  private static final long serialVersionUID = 2499407836107942133L;
  private String macro;
  private Value value;

  public String getMacro() {
    return macro;
  }

  public void setMacro(String macro) {
    this.macro = macro;
  }

  public Value getValue() {
    return value;
  }

  public void setValue(Value value) {
    this.value = value;
  }
}
