package com.newshunt.dataentity.news.model.entity.server.asset;

import java.io.Serializable;

/**
 * Created by santosh.kumar on 7/17/2016.
 */
public class ContentScale implements Serializable{

  private int height;

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

  private int width;

}
