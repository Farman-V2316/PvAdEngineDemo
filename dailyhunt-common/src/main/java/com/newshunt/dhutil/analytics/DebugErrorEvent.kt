/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */

package com.newshunt.dhutil.analytics

import com.newshunt.dataentity.common.model.entity.BaseError
import io.reactivex.exceptions.CompositeException

fun CompositeException.compositeMessage() =
        this.exceptions?.map { it.message }?.joinToString(separator = "!!!")

fun Throwable?.originalMessage(): String {
    this ?: return ""
    return when (this) {
    // if composite exception, make string of all its sub-exceptions messages
        is CompositeException -> (this as? CompositeException)?.compositeMessage()
        is BaseError ->
            // baseerror can hold a throwable which could be CompositeException
            when (this.originalError) {
                is CompositeException -> (this.originalError as? CompositeException)?.compositeMessage()
                else -> this.originalError?.message
            }
        else -> null
    } ?: message ?: ""
}
