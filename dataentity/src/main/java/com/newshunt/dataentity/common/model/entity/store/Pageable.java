/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.dataentity.common.model.entity.store;

import java.io.Serializable;
import java.util.Arrays;

public class Pageable implements Serializable {

  private static final long serialVersionUID = -7764918620114469643L;

  /**
   * This values determines the value from which the page scroll started. This
   * helps in solving pagination issue when content is ever growing, so that the
   * user doesn't see the same items in consecutive pages.
   */
  private String pageScrollStart;

  /**
   * This is a hack, to remove duplicates around page boundaries
   */
  private String ignoreValues;

  private int pageNumber;
  private int pageSize;
  private Sort[] sortFields = {};

  public Pageable() {
    pageNumber = 0;
    pageSize = 10;
  }

  public Pageable(int pageNumber, int pageSize, Sort... sortFields) {
    super();
    this.pageNumber = pageNumber;
    this.pageSize = pageSize;
    this.sortFields = sortFields;
  }

  public int getOffset() {
    return pageSize * pageNumber;
  }

  public int getEnd() {
    return getOffset() + pageSize - 1;
  }

  public int getPageNumber() {
    return pageNumber;
  }

  public void setPageNumber(int pageNumber) {
    this.pageNumber = pageNumber;
  }

  public int getPageSize() {
    return pageSize;
  }

  public void setPageSize(int pageSize) {
    this.pageSize = pageSize;
  }

  public Sort[] getSortFields() {
    return sortFields;
  }

  public void setSortFields(Sort... sortFields) {
    this.sortFields = sortFields;
  }

  public String getPageScrollStart() {
    return pageScrollStart;
  }

  public void setPageScrollStart(String pageScrollStart) {
    this.pageScrollStart = pageScrollStart;
  }

  public String getIgnoreValues() {
    return ignoreValues;
  }

  public void setIgnoreValues(String ignoreValues) {
    this.ignoreValues = ignoreValues;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("Pageable [pageScrollStart=").append(pageScrollStart).append(", ignoreValues=")
        .append(ignoreValues).append(", pageNumber=").append(pageNumber).append(", pageSize=")
        .append(pageSize).append(", sortFields=").append(Arrays.toString(sortFields)).append("]");
    return builder.toString();
  }

}
