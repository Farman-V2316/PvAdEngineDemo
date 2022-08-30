/*
 * Copyright (c) 2017 Newshunt. All rights reserved.
 */
package com.newshunt.dataentity.news.model.entity.server.asset;

import java.io.Serializable;

/**
 * Card Tag Label Info to be displayed on the news card
 *
 * @author santhosh.kc
 */
public class CardLabel implements Serializable {

  private static final long serialVersionUID = -5023120575674028754L;

  private CardLabelType type;

  private String text;

  private CardLabelBGType bgType;

  private String bgColor;

  private String fgColor;

  //in seconds
  private Long ttl;

  public CardLabelType getType() {
    return type;
  }

  public void setType(CardLabelType type) {
    this.type = type;
  }

  public String getText() {
    return text.toUpperCase();
  }

  public void setText(String text) {
    this.text = text;
  }

  public CardLabelBGType getBgType() {
    return bgType;
  }

  public void setBgType(CardLabelBGType bgType) {
    this.bgType = bgType;
  }

  public String getBgColor() {
    return bgColor;
  }

  public void setBgColor(String bgColor) {
    this.bgColor = bgColor;
  }

  public String getFgColor() {
    return fgColor;
  }

  public void setFgColor(String fgColor) {
    this.fgColor = fgColor;
  }

  public Long getTtl() {
    return ttl;
  }

  public void setTtl(Long ttl) {
    this.ttl = ttl;
  }
}
