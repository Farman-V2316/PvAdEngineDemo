/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */
package com.newshunt.common.helper.common

import com.newshunt.common.util.R
import com.newshunt.common.view.DbgCode
import com.newshunt.common.view.isNotFoundInCacheError
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.common.model.entity.BaseError
import com.newshunt.dataentity.common.model.entity.ErrorTypes
import com.newshunt.dataentity.common.model.entity.ListNoContentException
import com.newshunt.dataentity.common.model.entity.model.ApiResponse
import com.newshunt.dataentity.common.model.entity.model.NoConnectivityException
import com.newshunt.sdk.network.internal.NetworkSDKLogger.NETWORKSDK_LOG_TAG
import io.reactivex.exceptions.CompositeException
import okhttp3.Response
import okhttp3.ResponseBody
import retrofit2.adapter.rxjava2.HttpException
import java.io.IOException
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.nio.charset.Charset

/**
 * @author santhosh.kc
 */
class ApiResponseUtils {

    companion object {
        private const val LOG_TAG = NETWORKSDK_LOG_TAG.plus("_ApiResponseOperator")
        @JvmStatic
        fun extractRawResponse(response: Response?) : String? {
            response ?: return Constants.EMPTY_STRING
            val responseBody = response.body() ?: return Constants.EMPTY_STRING

            val contentLength = responseBody.contentLength()
            val source = responseBody.source()
            source.request(java.lang.Long.MAX_VALUE) // Buffer the entire body.
            val buffer = source.buffer()

            return if (contentLength != 0L) {
                buffer.clone().readString(Charset.forName(Constants.TEXT_ENCODING_UTF_8))
            } else {
                Constants.EMPTY_STRING
            }
        }

        /**
         * This method checks for data == null and throws exception.
         */
        @JvmStatic
        fun throwErrorIfDataNull(response: ApiResponse<*>) {
            if (response.data == null) {
                val error = BaseError(DbgCode.DbgHttpCode(HttpURLConnection.HTTP_NO_CONTENT), CommonUtils.getString(R.string.no_content_found), response.code, response.url)
                throw ListNoContentException(error)
            }
        }

        /**
         * This method checks for data == null and throws exception.
         */
        @JvmStatic
        fun throwErrorIfResponseNull(response: retrofit2.Response<*>) {
            if (response.body() == null) {
                val error = BaseError(DbgCode.DbgHttpCode(HttpURLConnection.HTTP_NO_CONTENT),
                        CommonUtils.getString(R.string.no_content_found), response.code(),
                        response.raw().request().url().toString())
                throw ListNoContentException(error)
            }
        }

        @JvmStatic
        @JvmOverloads
        fun composeListNoContentError(url: String? = null, code: Int = HttpURLConnection.HTTP_NO_CONTENT): ListNoContentException {
            val error = BaseError(DbgCode.DbgHttpCode(code),
                    CommonUtils.getString(R.string.no_content_found),
                    code,
                    url)
            return ListNoContentException(error)
        }

        fun getError(response: retrofit2.Response<*>?): BaseError {
            val error: BaseError

            var statusCode = -1
            var errorBody: ResponseBody? = null
            if (response != null) {
                statusCode = response.code()
                errorBody = response.errorBody()
            }
            val context = CommonUtils.getApplication()
            when (statusCode) {
                HttpURLConnection.HTTP_NOT_FOUND -> {
                    Logger.e(LOG_TAG, "404 response")
                    error = BaseError(DbgCode.DbgHttpCode(statusCode), CommonUtils.getString(R.string.no_content_found), HttpURLConnection.HTTP_NOT_FOUND, null)
                }
                HttpURLConnection.HTTP_NOT_MODIFIED -> {
                    Logger.e(LOG_TAG, "Cached response no error")
                    error = BaseErrorBuilder.getBaseError(Constants.HTTP_304_NOT_MODIFIED, HttpURLConnection.HTTP_NOT_MODIFIED)
                }

                HttpURLConnection.HTTP_INTERNAL_ERROR,
                HttpURLConnection.HTTP_GATEWAY_TIMEOUT,
                HttpURLConnection.HTTP_BAD_GATEWAY,
                HttpURLConnection.HTTP_NOT_IMPLEMENTED,
                HttpURLConnection.HTTP_UNAVAILABLE,
                HttpURLConnection.HTTP_UNAUTHORIZED,
                HttpURLConnection.HTTP_BAD_REQUEST,
                HttpURLConnection.HTTP_VERSION -> {
                    Logger.e(LOG_TAG, "Server Error $statusCode")
                    error = BaseError(DbgCode.DbgHttpCode(statusCode),
                            context.getString(R.string.error_server_issue), statusCode, null)
                }
                else -> {
                    try {
                        if (errorBody != null) {
                            val body = errorBody.string()
                            Logger.e(LOG_TAG, "Request failed with $body")
                        }
                    } catch (e: IOException) {
                        Logger.caughtException(e)
                    }

                    error = BaseErrorBuilder.getBaseError(
                            ErrorTypes.API_STATUS_CODE_UNDEFINED, context.getString(R.string.error_generic))
                }
            }
            errorBody?.close()
            return error
        }

        fun getError(t: Throwable): BaseError {
            val error: BaseError
            if (t is BaseError) {
                return t
            }
            if (t is SocketTimeoutException) {
                error = BaseError(t, CommonUtils.getApplication().getString(R.string.error_connectivity), null, null)
            } else if (t is NoConnectivityException) {
                error = BaseError(t, CommonUtils.getApplication().getString(R.string.error_no_connection), null, null)
            } else if (t is UnknownHostException) {
                if (CommonUtils.isNetworkAvailable(CommonUtils.getApplication())) {
                    error = BaseError(t, CommonUtils.getApplication().getString(R.string.error_connectivity), null, null)
                } else {
                    error = BaseError(t, CommonUtils.getApplication().getString(R.string.error_no_connection), null, null)
                }
            } else if (t is HttpException) {
                return getError(t.response())
            } else if (t is ListNoContentException) {
                error = t.error
            } else if (t is CompositeException) {
                val exceptions = t.exceptions.filterNot {
                    (it as? BaseError).isNotFoundInCacheError()
                }
                if (exceptions.isEmpty()) {
                    error = BaseError(t, CommonUtils.getApplication().getString(R.string.error_generic), null, null)
                }
                else {
                    return getError(exceptions.first())
                }
            } else {
                error = BaseError(t, CommonUtils.getApplication().getString(R.string.error_generic), null, null)
            }
            return error
        }
    }
}