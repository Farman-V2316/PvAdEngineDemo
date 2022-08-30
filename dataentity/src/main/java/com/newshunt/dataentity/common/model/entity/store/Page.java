/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.dataentity.common.model.entity.store;


import java.io.Serializable;
import java.util.List;

public class Page<T> implements Serializable {

  private static final long serialVersionUID = -3122761105345543064L;

  private final List<T> content;
  private final Pageable pageable;
  private final int totalElements;

  public Page(List<T> content, int totalElements, Pageable pageable) {
    super();
    this.content = content;
    this.pageable = pageable;
    this.totalElements = totalElements;
  }

  public Page(List<T> content, Pageable pageable) {
    super();
    this.content = content;
    this.pageable = pageable;

    // -1 signifies we dont have totalElement info
    this.totalElements = -1;
  }

  public Pageable getPageable() {
    return pageable;
  }


  public List<T> getContent() {
    return content;
  }

  public int getNumber() {
    return pageable.getPageNumber();
  }

  public int getNumberOfElements() {
    return content.size();
  }

  public int getTotalElements() {
    return totalElements;
  }

  public int getTotalPages() {
    return totalElements / pageable.getPageSize();
  }

}
