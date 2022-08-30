/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.dataentity.common.model.entity.language;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

/**
 * Represents a language detail.
 *
 * @author amarjit
 */
public class Language implements Serializable {

  private static final long serialVersionUID = 1562283797773228402L;

  /**
   * primary key uniquely identifies language
   */
  private long pk;

  /**
   * display name string for this language
   */
  private String name;

  /**
   * Two letter code for this language.
   */
  private String code;

  /**
   * lang index to connect to newspaper
   */
  private int langIndex;

  /**
   * Has list of enables or disables applicable for this language. Example: a
   * language applicable for news or books, etc.
   */
  private Set<String> enableList;

  /**
   * display order of this language in news section
   */
  private int newsDisplayOrder;

  /**
   * display order of this language in books section
   */
  private int booksDisplayOrder;

  /**
   * Unicode representation of language.
   */
  private String langUni;

  /**
   * Unicode symbol for language.
   */
  private String uniCode;

  /**
   * Client to use system-font or not (Y/N)
   */
  private String useSystemFont;

  /**
   * Indicates whether this language is primary or not. For an edition only one
   * language can be set as primary.
   */
  private boolean isPrimary;

  private String labelImageUrl;

  /**
   * Language unicode ranges for specific language
   */
  private List<LanguageUniRange> langUniCoderanges;

  public List<LanguageUniRange> getLangUniCoderanges() {
    return langUniCoderanges;
  }

  public void setLangUniCoderanges(List<LanguageUniRange> langUniCoderanges) {
    this.langUniCoderanges = langUniCoderanges;
  }

  public boolean isPrimary() {
    return isPrimary;
  }

  public void setPrimary(boolean isPrimary) {
    this.isPrimary = isPrimary;
  }

  public long getPk() {
    return pk;
  }

  public void setPk(long pk) {
    this.pk = pk;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public Set<String> getEnableList() {
    return enableList;
  }

  public void setEnableList(Set<String> enableList) {
    this.enableList = enableList;
  }

  public int getNewsDisplayOrder() {
    return newsDisplayOrder;
  }

  public void setNewsDisplayOrder(int newsDisplayOrder) {
    this.newsDisplayOrder = newsDisplayOrder;
  }

  public int getBooksDisplayOrder() {
    return booksDisplayOrder;
  }

  public void setBooksDisplayOrder(int booksDisplayOrder) {
    this.booksDisplayOrder = booksDisplayOrder;
  }

  public String getLangUni() {
    return langUni;
  }

  public void setLangUni(String langUni) {
    this.langUni = langUni;
  }

  public String getUseSystemFont() {
    return useSystemFont;
  }

  public void setUseSystemFont(String useSystemFont) {
    this.useSystemFont = useSystemFont;
  }

  public boolean useSystemFont() {
    return "y".equalsIgnoreCase(useSystemFont);
  }

  public int getLangIndex() {
    return langIndex;
  }

  public void setLangIndex(int langIndex) {
    this.langIndex = langIndex;
  }

  @Override
  public String toString() {
    return new StringBuilder().append(getClass()).append(" [").append(", name=").append(name)
        .append(", code=").append(code).append(", enableList=").append(enableList)
        .append(", newsDisplayOrder=").append(newsDisplayOrder).append(", booksDisplayOrder=")
        .append(booksDisplayOrder).append("]").toString();
  }

  public String getLabelImageUrl() {
    return labelImageUrl;
  }

  public void setLabelImageUrl(String labelImageUrl) {
    this.labelImageUrl = labelImageUrl;
  }

  public String getUniCode() {
    return uniCode;
  }

  public void setUniCode(String uniCode) {
    this.uniCode = uniCode;
  }
}
