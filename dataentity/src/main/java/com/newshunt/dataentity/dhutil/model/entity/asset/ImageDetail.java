/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */
package com.newshunt.dataentity.dhutil.model.entity.asset;

import java.io.Serializable;

/**
 * Represents an image detail object.
 *
 * @author amarjit
 */
public class ImageDetail implements Serializable {

  private static final long serialVersionUID = 7715517221289302009L;

  private String url;
  private float width;
  private float height;
  transient private int orientation = 0;
  transient private String resolution = "";
  private String format;

  public ImageDetail() {
  }

  public ImageDetail(String url, float width, float height) {
    this.url = url;
    this.width = width;
    this.height = height;
  }

  public ImageDetail(String url) {
    this.url = url;
  }

    public ImageDetail(String url, float width, float height, int orientation, String res, String format) {
        this.url = url;
        this.width = width;
        this.height = height;
        this.orientation = orientation;
        this.resolution = res;
        this.format = format;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public float getWidth() {
    return width;
  }

  public void setWidth(float width) {
    this.width = width;
  }

  public float getHeight() {
    return height;
  }

  public void setHeight(float height) {
    this.height = height;
  }


    public int getOrientation() {
        return orientation;
    }

    public void setOrientation(int orientation) {
        this.orientation = orientation;
    }

    public String getResolution() {
        return resolution;
    }

    public void setResolution(String resolution) {
        this.resolution = resolution;
    }

    @Override
  public String toString() {
    return new StringBuilder().append(getClass())
            .append("[")
        .append("url=").append(url)
        .append(", width=").append(width)
        .append(", height=").append(height)
            .append(", orientaion=").append(orientation)
            .append(", resolution=").append(resolution)
        .append("]").toString();
  }

}
