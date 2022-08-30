/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */
package com.newshunt.dataentity.common.model.entity.model;


import com.newshunt.common.helper.common.Constants;

/**
 * Handling the error from the retrofit
 *
 * @author bheemesh on 7/20/2015.
 */
public enum StatusError {
  NETWORK_ERROR(Constants.ERROR_NO_INTERNET),
  CONVERSION_ERROR(Constants.ERROR_CONVERSION),
  HTTP_ERROR(Constants.ERROR_HTTP_NOT_FOUND),
  UNEXPECTED_ERROR(Constants.ERROR_UNEXPECTED),
  NO_CONTENT_ERROR(Constants.ERROR_HTTP_NO_CONTENT);

  private String name;

  StatusError(String name) {
    this.name = name;
  }

  public static StatusError fromName(String name) {
    for (StatusError type : StatusError.values()) {
      if (type.name.equalsIgnoreCase(name)) {
        return type;
      }
    }
    return UNEXPECTED_ERROR;
  }

  public String getName() {
    return name;
  }

}
