/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.dataentity.common.model.entity.language;

import java.io.Serializable;

/**
 * Represents language's uni-code ranges
 *
 * @author shanmugam.c
 */
public class LanguageUniRange implements Serializable {

  private static final long serialVersionUID = -6683339896916647813L;

  private long pk;

  /**
   * language code
   */
  private String langCode;

  /**
   * Unicode's starting range
   */
  private String unicodeStartRange;

  /**
   * Unicode's ending range
   */
  private String unicodeEndRange;

  public long getPk() {
    return pk;
  }

  public void setPk(long pk) {
    this.pk = pk;
  }

  public String getLangCode() {
    return langCode;
  }

  public void setLangCode(String langCode) {
    this.langCode = langCode;
  }

  public String getUnicodeStartRange() {
    return unicodeStartRange;
  }

  public void setUnicodeStartRange(String unicodeStartRange) {
    this.unicodeStartRange = unicodeStartRange;
  }

  public String getUnicodeEndRange() {
    return unicodeEndRange;
  }

  public void setUnicodeEndRange(String unicodeEndRange) {
    this.unicodeEndRange = unicodeEndRange;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("LanguageUniRange [pk=").append(pk).append(", langCode=").append(langCode)
        .append(", unicodeStartRange=").append(unicodeStartRange).append(", unicodeEndRange=")
        .append(unicodeEndRange).append("]");
    return builder.toString();
  }

}
