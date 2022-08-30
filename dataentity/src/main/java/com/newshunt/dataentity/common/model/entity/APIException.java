package com.newshunt.dataentity.common.model.entity;

import androidx.annotation.NonNull;

/**
 * API Exception class wrapping BaseError
 *
 * @author: bedprakash on 15/12/16.
 */

public class APIException extends RuntimeException {
  private final BaseError error;

  public APIException(final @NonNull BaseError error) {
    this.error = error;
  }


  @NonNull
  public BaseError getError() {
    return error;
  }

  public int getErrorCode() {
    return error.getStatusAsInt();
  }
}
