/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.dataentity.dhutil.model.entity.version;

import com.newshunt.dataentity.common.model.entity.store.Pageable;

import java.util.Date;

/**
 * Holds all different attributes of a versionedAPI
 *
 * @author maruti.borker
 */
public class VersionedApiEntity extends Pageable {
  private Long id;
  private String entityType;
  private String grandParentType;
  private String grandParentId;
  private String parentType;
  private String parentId;
  private String version;
  private String languageCode;
  private String edition;
  private Date lastUpdated;

  public VersionedApiEntity() {
    super();
  }

  public VersionedApiEntity(VersionEntity versionEntity) {
    this();
    VersionEntity grandParentEntity = versionEntity.getGrandParent();
    VersionEntity parentEntity = versionEntity.getParent();
    this.setEntityType(versionEntity.name());
    if (grandParentEntity != null) {
      this.setGrandParentType(grandParentEntity.name());
    }
    if (parentEntity != null) {
      this.setParentType(parentEntity.name());
    }
  }

  public String getLanguageCode() {
    return languageCode;
  }

  public void setLanguageCode(String languageCode) {
    this.languageCode = languageCode;
  }

  public String getEdition() {
    return edition;
  }

  public void setEdition(String edition) {
    this.edition = edition;
  }

  public Date getLastUpdated() {
    return lastUpdated;
  }

  public void setLastUpdated(Date lastUpdated) {
    this.lastUpdated = lastUpdated;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getEntityType() {
    return entityType;
  }

  public void setEntityType(String entityType) {
    this.entityType = entityType;
  }

  public String getGrandParentType() {
    return grandParentType;
  }

  public void setGrandParentType(String grandParentType) {
    this.grandParentType = grandParentType;
  }

  public String getGrandParentId() {
    return grandParentId;
  }

  public void setGrandParentId(String grandParentId) {
    this.grandParentId = grandParentId;
  }

  public String getParentType() {
    return parentType;
  }

  public void setParentType(String parentType) {
    this.parentType = parentType;
  }

  public String getParentId() {
    return parentId;
  }

  public void setParentId(String parentId) {
    this.parentId = parentId;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public String getNonNullVersion() {
    return version == null ? "" : version;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof VersionedApiEntity)) {
      return false;
    }

    VersionedApiEntity that = (VersionedApiEntity) o;

    if (edition != null ? !edition.equals(that.edition) : that.edition != null) {
      return false;
    }
    if (languageCode != null ? !languageCode.equals(that.languageCode) :
        that.languageCode != null) {
      return false;
    }
    if (getEntityType() != null ? !getEntityType().equals(that.getEntityType()) :
        that.getEntityType() != null) {
      return false;
    }
    if (getGrandParentType() != null ? !getGrandParentType().equals(that.getGrandParentType()) :
        that.getGrandParentType() != null) {
      return false;
    }
    if (getParentType() != null ? !getParentType().equals(that.getParentType()) :
        that.getParentType() != null) {
      return false;
    }
    if (getGrandParentId() != null ? !getGrandParentId().equals(that.getGrandParentId()) :
        that.getGrandParentId() != null) {
      return false;
    }

    if (getParentId() != null ? !getParentId().equals(that.getParentId()) :
        that.getParentId() != null) {
      return false;
    }

    if (getPageNumber() != that.getPageNumber()) {
      return false;
    }

    return getPageSize() == that.getPageSize();

  }

  @Override
  public int hashCode() {
    int result = languageCode != null ? languageCode.hashCode() : 0;
    result = 31 * result + (edition != null ? edition.hashCode() : 0);
    result = 31 * result + (getEntityType() != null ? getEntityType().hashCode() : 0);
    result = 31 * result + (getGrandParentType() != null ? getGrandParentType().hashCode() : 0);
    result = 31 * result + (getParentType() != null ? getParentType().hashCode() : 0);
    result = 31 * result + (getGrandParentId() != null ? getGrandParentId().hashCode() : 0);
    result = 31 * result + (getParentId() != null ? getParentId().hashCode() : 0);
    return result;
  }
}
