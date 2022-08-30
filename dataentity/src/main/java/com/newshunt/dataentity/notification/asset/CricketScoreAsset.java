/*
 *
 *  * Copyright (c) 2017 Newshunt. All rights reserved.
 *
 */

package com.newshunt.dataentity.notification.asset;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * @author shrikant on 27/08/17.
 */

public class CricketScoreAsset implements Serializable {
  @SerializedName("r")
  private Integer runs;

  @SerializedName("o")
  private Float overs;

  @SerializedName("w")
  private Integer wickets;

  @SerializedName("d")
  private Integer declared;

  @SerializedName("bt")
  private Integer batting;

  public Integer getRuns() {
    return runs == null ? 0 : runs;
  }

  public void setRuns(Integer runs) {
    this.runs = runs;
  }

  public Float getOvers() {
    return overs == null ? 0f : overs;
  }

  public void setOvers(Float overs) {
    this.overs = overs;
  }

  public Integer getWickets() {
    return wickets == null ? 0 : wickets;
  }

  public void setWickets(Integer wickets) {
    this.wickets = wickets;
  }

  public boolean isDeclared() {
    return declared != null && declared == 1;
  }

  public boolean isBatting() {
    return batting != null && batting == 1;
  }


}
