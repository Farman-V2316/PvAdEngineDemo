/**
 * Copyright (c) 2016 Newshunt. All rights reserved.
 */
package com.newshunt.dataentity.news.model.entity.server.asset;

import java.io.Serializable;

/**
 * @author shrikant.agrawal
 */
public class ShareParam implements Serializable {

  private String shareTitle;

  private String shareDescription;

  private String sourceName;

  public String getShareTitle() {
    return shareTitle;
  }

  public void setShareTitle(String shareTitle) {
    this.shareTitle = shareTitle;
  }

  public String getShareDescription() {
    return shareDescription;
  }

  public void setShareDescription(String shareDescription) {
    this.shareDescription = shareDescription;
  }

  public String getSourceName() {
    return sourceName;
  }

  public void setSourceName(String sourceName) {
    this.sourceName = sourceName;
  }
}
