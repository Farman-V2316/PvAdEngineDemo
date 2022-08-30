/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.dataentity.common.model.entity.store;

import java.io.Serializable;

public class Sort implements Serializable {

  private static final long serialVersionUID = -3427696888371783192L;

  private String property;
  private boolean ascending;

  public Sort(String property, boolean ascending) {
    super();
    this.property = property;
    this.ascending = ascending;
  }

  public static Sort sortBy(String property) {
    return new Sort(property, true);
  }

  public static Sort sortByDesc(String property) {
    return new Sort(property, false);
  }

  public String getProperty() {
    return property;
  }

  public boolean isAscending() {
    return ascending;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("Sort [property=").append(property).append(", ascending=").append(ascending)
        .append("]");
    return builder.toString();
  }

}
