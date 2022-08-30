/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.common.model.interceptor;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.newshunt.common.helper.common.Constants;
import com.newshunt.common.helper.common.Logger;
import com.newshunt.dataentity.common.model.entity.BaseError;
import com.newshunt.dataentity.common.model.entity.ErrorSettableResponse;
/**
 * Generic error handler to remove duplicate code in retrofit error cases
 *
 * @author maruti.borker
 */
public class GenericRestFailureHandler {

  public static @Nullable ErrorSettableResponse handleRestFailure(@NonNull ErrorSettableResponse response,
                                          @NonNull BaseError error) {
    if (Constants.ERROR_HTTP_NOT_MODIFIED.equals(error.getStatus())) {
      return null;
    }
    Logger.e("Retrofit", "[DEBUG] rest call failed with msg - " + error.getMessage());
    response.setError(error);
    return response;
  }
}