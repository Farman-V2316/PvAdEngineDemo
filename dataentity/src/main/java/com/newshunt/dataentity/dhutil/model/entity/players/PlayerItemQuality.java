package com.newshunt.dataentity.dhutil.model.entity.players;

import java.io.Serializable;
import java.util.Comparator;

/**
 * @author rohit
 */
public class PlayerItemQuality implements Serializable, Comparator {

  private int networkType;
  private String requestParam;
  private String displayString;

  private int qualityIndex;

  public int getNetworkType() {
    return networkType;
  }

  public void setNetworkType(int networkType) {
    this.networkType = networkType;
  }

  public String getRequestParam() {
    return requestParam;
  }

  public void setRequestParam(String requestParam) {
    this.requestParam = requestParam;
  }

  public String getDisplayString() {
    return displayString;
  }

  public void setDisplayString(String displayString) {
    this.displayString = displayString;
  }

  public int getQualityIndex() {
    return qualityIndex;
  }

  public void setQualityIndex(int qualityIndex) {
    this.qualityIndex = qualityIndex;
  }

  @Override
  public int compare(Object lhs, Object rhs) {
    if (((PlayerItemQuality) lhs).getDisplayString().equalsIgnoreCase("low") && ((PlayerItemQuality) rhs)
        .getDisplayString().equalsIgnoreCase("high")) {
      return -1;
    } else if (((PlayerItemQuality) lhs).getDisplayString().equalsIgnoreCase("medium") &&
        ((PlayerItemQuality)
            rhs)
            .getDisplayString().equalsIgnoreCase("high")) {
      return -1;
    } else if (((PlayerItemQuality) lhs).getDisplayString().equalsIgnoreCase("low") && ((PlayerItemQuality)
        rhs)
        .getDisplayString().equalsIgnoreCase("medium")) {
      return -1;
    } else if (((PlayerItemQuality) lhs).getDisplayString().equalsIgnoreCase("low") &&
        ((PlayerItemQuality) rhs)
            .getDisplayString().equalsIgnoreCase("low")) {
      return 0;
    } else if (((PlayerItemQuality) lhs).getDisplayString().equalsIgnoreCase("medium") &&
        ((PlayerItemQuality) rhs)
            .getDisplayString().equalsIgnoreCase("medium")) {
      return 0;
    } else if (((PlayerItemQuality) lhs).getDisplayString().equalsIgnoreCase("high") && ((PlayerItemQuality)
        rhs)
        .getDisplayString().equalsIgnoreCase("high")) {
      return 0;
    }
    return 1;

  }

  public void setDefaultQualityParameters() {
    networkType = 0;
    requestParam = "high";
    displayString = "h";
    qualityIndex = 30;

  }
}


