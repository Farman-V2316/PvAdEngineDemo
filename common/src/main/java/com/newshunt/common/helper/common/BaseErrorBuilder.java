package com.newshunt.common.helper.common;

import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.common.util.R;
import com.newshunt.dataentity.common.model.entity.BaseError;
import com.newshunt.dataentity.common.model.entity.ErrorTypes;
import com.newshunt.dataentity.common.model.entity.model.NoConnectivityException;
import com.newshunt.common.view.DbgCode;

import androidx.annotation.Nullable;

/**
 * Created by karthik.r on 2019-08-23.
 */
public class BaseErrorBuilder {

  public static BaseError getBaseError(ErrorTypes errorTypes, String message, String status) {
    if (message == null) {
      message = CommonUtils.getString(R.string.error_generic);
    }
    return new BaseError(getThrowableFromErrorType(errorTypes), message, status);
  }

  public static BaseError getBaseError(ErrorTypes errorTypes, String message) {
    return getBaseError(errorTypes, message, null);
  }

  public static BaseError getBaseError(ErrorTypes errorTypes) {
    return getBaseError(errorTypes, null, null);
  }

  public static BaseError getBaseError(Throwable error,
                                       @Nullable String message,
                                       @Nullable String status,
                                       @Nullable String url) {
    if (error == null) {
      error = new Throwable();
    }

    if (error instanceof NoConnectivityException) {
      message = CommonUtils.getString(R.string.error_no_connection);
    }

    if (message == null) {
      message = CommonUtils.getString(R.string.error_generic);
    }

    if (status == null) {
      status = Constants.ERROR_UNEXPECTED;
    }

    return new BaseError(error, message, status, url);
  }

  public static BaseError getBaseError(String message,
                                       int status) {
    return new BaseError(getThrowableException(Integer.toString(status)), message,
        Integer.toString(status),
        null);
  }

  public static BaseError getBaseError(String message,
                                       String status) {
    return new BaseError(getThrowableException(status), message, status, null);
  }


  public static BaseError getBaseError(String message,
                                       int status, String url) {
    return new BaseError(getThrowableException(Integer.toString(status)), message,
        Integer.toString(status),
        url);
  }

  public static BaseError getBaseError(String message,
                                       String status, String url) {
    return new BaseError(getThrowableException(status), message, status, url);
  }

  // for places where we dont have originalError, passing enum which will return Throwable
  // with the error code
  private static Throwable getThrowableFromErrorType(ErrorTypes errorTypes) {
    if (errorTypes == ErrorTypes.API_STATUS_CODE_UNDEFINED) {
      return new DbgCode.DbgApiInvalidStatusCode();
    }
    if (errorTypes == ErrorTypes.BROWSER_GENERIC) {
      return new DbgCode.DbgBroswerGeneric();
    }
    if (errorTypes == ErrorTypes.BROWSER_SERVER) {
      return new DbgCode.DbgBroswerServer();
    }
    if (errorTypes == ErrorTypes.ERROR_CONNECTIVITY) {
      return new DbgCode.DbgErrorConnectivity();
    }
    if (errorTypes == ErrorTypes.NOT_FOUND_IN_CACHE) {
      return new DbgCode.DbgNotFoundInCache();
    }
    if (errorTypes == ErrorTypes.ONBOARDING_REQUEST) {
      return new DbgCode.DbgOnBoardingRequest();
    }
    if (errorTypes == ErrorTypes.RESPONSE_ERROR_NULL) {
      return new DbgCode.DbgResponseErrorNull();
    }
    if (errorTypes == ErrorTypes.VERSIONED_API_CORRUPTED) {
      return new DbgCode.DbgVersionedApiCorrupt();
    }

    return new Throwable();
  }

  private static Exception getThrowableException(String status) {
    if (status.equals(Constants.ERROR_NO_INTERNET) ||
        status.equals(CommonUtils.getString(R
            .string.no_connection_error)) ||
        status.equals(CommonUtils.getString(R
            .string.error_no_connection)) ||
        status.equals(CommonUtils.getString(R.string.error_connectivity))) {
      return new NoConnectivityException(CommonUtils.getString(R.string.error_no_connection));
    }

    return new IllegalStateException();
  }

}
