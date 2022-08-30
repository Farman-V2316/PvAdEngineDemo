/*
 * Copyright (c) 2017 Newshunt. All rights reserved.
 */
package com.newshunt.dataentity.common.model.entity;

/**
 * Created by anshul on 10/05/17.
 * A class for getting and interpreting various errors from the WebView.
 */

public enum NhWebViewErrorType {

  NETWORK_ERROR_TOAST("network_error_toast"),
  SERVER_ERROR_TOAST("server_error_toast"),
  GENERIC_ERROR_TOAST("generic_error_toast"),
  NO_CONTENT_ERROR_TOAST("noContent_error_toast"),
  NETWORK_ERROR_FULLSCREEN("network_error_fullscreen"),
  SERVER_ERROR_FULLSCREEN("server_error_fullscreen"),
  GENERIC_ERROR_FULLSCREEN("generic_error_fullscreen"),
  NO_CONTENT_ERROR_FULLSCREEN("noContent_error_fullscreen");

  private String error;

  NhWebViewErrorType(String error) {
    this.error = error;
  }

  public String getError() {
    return error;
  }

  public static  NhWebViewErrorType getErrorType(String errorType) {
    for (NhWebViewErrorType error : NhWebViewErrorType.values()) {
      if (error.getError().equals(errorType)) {
        return error;
      }
    }
    return null;
  }
}
