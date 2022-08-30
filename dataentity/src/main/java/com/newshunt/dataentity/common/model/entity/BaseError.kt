/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.dataentity.common.model.entity

import android.text.TextUtils
import com.newshunt.common.helper.common.Constants

/**
 * Base class for all news model errors
 *
 * @author madhuri.pa
 */
class BaseError : RuntimeException {

    var url: String? = null

    override val message: String?
        get() {
            return if (TextUtils.isEmpty(field) && originalError != null) {
                originalError!!.message
            } else field
        }

    val status: String?
    /**
     * used for logging & debugging purpose.
     */
    var originalError: Throwable? = null

    val statusAsInt: Int
        get() = Integer.parseInt(status!!)


    constructor(error: Throwable, message: String?, status: Int,
                url: String?) : this(error, message, status.toString(), url) {
    }

    @JvmOverloads
    constructor(error: Throwable,
                message: String?,
                status: String? = Constants.ERROR_UNEXPECTED,
                url: String? = null) {
        this.message = message;
        this.originalError = error
        if (status == null) {
            this.status = Constants.ERROR_UNEXPECTED
        } else {
            this.status = status
        }
        this.url = url
    }

    constructor(detailMessage: String) : this(Throwable(), detailMessage,
            Constants.ERROR_UNEXPECTED, null) {
    }

    fun equal(obj: Any): Boolean {

        return if (obj !is BaseError) {
            false
        } else equals(obj.message, message) &&
                equals(obj.status, status) &&
                equals(obj.url, url)
    }

    /**
     * Null-safe equivalent of `a.equals(b)`.
     *
     * @param a - object a
     * @param b - object b
     * @return - return true if equal else false
     */
    fun equals(a: Any?, b: Any?): Boolean {
        return if (a == null) b == null else b != null && a == b
    }

}

enum class ErrorTypes {
    BROWSER_SERVER, NOT_FOUND_IN_CACHE, BROWSER_GENERIC, API_STATUS_CODE_UNDEFINED,
    RESPONSE_ERROR_NULL, ERROR_CONNECTIVITY, VERSIONED_API_CORRUPTED,
    ONBOARDING_REQUEST
}

data class Extra(val type: ExtraListObjType, val extraTitle: String? = null)

enum class ExtraListObjType {
    FOOTER,
    HEADER,
    DATE_SEPARATOR,
    LOGIN_NUDGE,
    GUEST_USERS
}


