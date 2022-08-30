/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.dataentity.common.model.entity;

import com.newshunt.dataentity.common.model.entity.language.Language;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Represents the edition having names for different languages.
 *
 * @author amarjit
 */
public class Edition implements Serializable {

  private static final long serialVersionUID = 8084096100732786517L;

  /**
   * pk of this edition
   */
  private Long pk;
  /**
   * key for this edition
   */
  private String key;
  /**
   * family key of edition
   */
  private String familyKey;
  /**
   * display name string for this edition
   */
  private String name;
  /**
   * which all languages are available for this edition
   */
  private Long languageMask;
  /**
   * unique bit mask for this edition
   */
  private Long editionMask;
  /**
   * Group List version
   */
  private Long groupListVersion;
  /**
   * Topic list version
   */
  private Long topicsListVersion;
  /**
   * "<Edition>": "<name represented in the specific language>"
   */
  private Map<Long, String> editionMapping;

  /**
   * Supported languages for this edition
   */
  private List<Language> languages;

  private String primaryLanguage;

  /**
   * Flag Url for edition
   */
  private String flagUrl;

  /**
   * Country code
   */
  private String countryCode;

  public static long getSerialversionuid() {
    return serialVersionUID;
  }

  public List<Language> getLanguages() {
    return languages;
  }

  public void setLanguages(List<Language> languages) {
    this.languages = languages;
  }

  public Long getPk() {
    return pk;
  }

  public void setPk(Long pk) {
    this.pk = pk;
  }

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public String getFamilyKey() {
    return familyKey;
  }

  public void setFamilyKey(String familyKey) {
    this.familyKey = familyKey;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Long getLanguageMask() {
    return languageMask;
  }

  public void setLanguageMask(Long languageMask) {
    this.languageMask = languageMask;
  }

  public Long getEditionMask() {
    return editionMask;
  }

  public void setEditionMask(Long editionMask) {
    this.editionMask = editionMask;
  }

  public Long getGroupListVersion() {
    return groupListVersion;
  }

  public void setGroupListVersion(Long groupListVersion) {
    this.groupListVersion = groupListVersion;
  }

  public Long getTopicsListVersion() {
    return topicsListVersion;
  }

  public void setTopicsListVersion(Long topicsListVersion) {
    this.topicsListVersion = topicsListVersion;
  }

  public Map<Long, String> getEditionMapping() {
    return editionMapping;
  }

  public void setEditionMapping(Map<Long, String> editionMapping) {
    this.editionMapping = editionMapping;
  }

  public String getFlagUrl() {
    return flagUrl;
  }

  public void setFlagUrl(String flagUrl) {
    this.flagUrl = flagUrl;
  }

  public String getPrimaryLanguage() {
    return primaryLanguage;
  }

  public void setPrimaryLanguage(String primaryLanguage) {
    this.primaryLanguage = primaryLanguage;
  }

  public String getCountryCode() {
    return countryCode;
  }

  public void setCountryCode(String countryCode) {
    this.countryCode = countryCode;
  }


  @Override
  public String toString() {
    return new StringBuilder().append(getClass()).append(" [").append("key=").append(key)
        .append(", familyKey=").append(familyKey).append(", name=").append(name)
        .append(", languageMask=").append(languageMask).append(", editionMask=")
        .append(editionMask).append(", groupListVersion=").append(groupListVersion)
        .append(", countryCode=").append(countryCode)
        .append(", topicsListVersion=").append(topicsListVersion).append(", languageMapping=")
        .append(editionMapping).append("]").toString();
  }

}
