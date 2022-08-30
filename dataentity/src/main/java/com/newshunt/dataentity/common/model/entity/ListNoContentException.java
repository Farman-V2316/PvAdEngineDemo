/*
* Copyright (c) 2017 Newshunt. All rights reserved.
*/
package com.newshunt.dataentity.common.model.entity;

import androidx.annotation.NonNull;

import java.io.IOException;

/**
 * Exception to handle empty data case with http code 200,204 in news list.
 *
 * @author raunak.yadav
 */
public class ListNoContentException extends IOException {
  private final BaseError error;

  public ListNoContentException(final @NonNull BaseError error) {
    this.error = error;
  }

  @NonNull
  public BaseError getError() {
    return error;
  }
}