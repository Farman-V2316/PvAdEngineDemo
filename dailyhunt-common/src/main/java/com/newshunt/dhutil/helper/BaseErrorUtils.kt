/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */

package com.newshunt.dhutil.helper

import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.common.model.entity.BaseError
import com.newshunt.common.track.ApiResponseOperator
import com.newshunt.dhutil.R
import io.reactivex.exceptions.CompositeException

/**
 * @author santhosh.kc
 */
class BaseErrorUtils {

    companion object {

        @JvmStatic
        fun getErrorMessage(throwable: Throwable) : String {
            return if(throwable is CompositeException) getCompositeErrorMessage(throwable) else
                getCommonErrorMessage(throwable)
        }

        @JvmStatic
        fun getCompositeErrorMessage(throwable: CompositeException) : String{
            val exceptions = throwable.exceptions
            var hasNetworkException = false
            var messageForUser : String? = null
            for (exception in exceptions) {
                Logger.caughtException(exception)
                if (exception is BaseError && Constants.NOT_FOUND_IN_CACHE != exception.message) {
                    messageForUser = exception.message
                    hasNetworkException = true
                    break
                }
            }
            if (!hasNetworkException) {
                // unknown error
                val t = exceptions[0]
                messageForUser = t.message
            }
            return messageForUser ?: CommonUtils.getString(com.newshunt.common.util.R.string.error_generic)
        }

        @JvmStatic
        fun getCommonErrorMessage(throwable: Throwable) : String{
            val error: BaseError = throwable as? BaseError ?: ApiResponseOperator.getError(throwable)
            return error.message ?: CommonUtils.getString(com.newshunt.common.util.R.string.error_generic)
        }

        /**
         * Helper method to extract the Exception from a composite exception. This method ignores
         * the NOT_FOUND_IN_CACHE exception and returns the other exceptions to the caller.
         */
        @JvmStatic
        fun extractNetworkError(throwable: Throwable): Throwable {
            if (throwable !is CompositeException) {
                return throwable
            }

            throwable.exceptions?.forEach {
                if (it is BaseError && Constants.NOT_FOUND_IN_CACHE != it.message) {
                    return it
                } else if(it !is BaseError){
                    return it
                }
            }

            return throwable
        }
    }
}