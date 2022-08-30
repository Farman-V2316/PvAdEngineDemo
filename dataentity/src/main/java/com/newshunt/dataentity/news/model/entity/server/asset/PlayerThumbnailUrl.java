package com.newshunt.dataentity.news.model.entity.server.asset;

import java.io.Serializable;

public class PlayerThumbnailUrl implements Serializable {

  private static final long serialVersionUID = 564180272512598201L;

  private String url;
  private int width;
  private int height;

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public int getWidth() {
    return width;
  }

  public void setWidth(int width) {
    this.width = width;
  }

  public int getHeight() {
    return height;
  }

  public void setHeight(int height) {
    this.height = height;
  }
}
