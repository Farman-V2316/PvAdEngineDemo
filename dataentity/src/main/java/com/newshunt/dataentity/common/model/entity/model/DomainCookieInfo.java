/*
 *  Copyright (c) 2017 Newshunt. All rights reserved.
 */

package com.newshunt.dataentity.common.model.entity.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * @author: bedprakash on 29/09/17.
 */

public class DomainCookieInfo implements Serializable {

  private static final long serialVersionUID = 5453436315688812153L;

  @SerializedName("dm")
  private String domain;
  private String url;
  @SerializedName("lrt")
  private long lastRemovedTime;

  public String getDomain() {
    return domain;
  }
  public void setDomain(String domain) {
    this.domain = domain;
  }
  public String getUrl() {
    return url;
  }
  public void setUrl(String url) {
    this.url = url;
  }
  public Long getLastRemovedTime() {
    return lastRemovedTime;
  }
  public void setLastRemovedTime(Long lastRemovedTime) {
    this.lastRemovedTime = lastRemovedTime;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("DomainCookieInfo [domain=").append(domain).append(", url=").append(url)
        .append(", lastRemovedTime=").append(lastRemovedTime).append("]");
    return builder.toString();
  }

}