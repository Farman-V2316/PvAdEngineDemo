/*
 * Copyright (c) 2020  Newshunt. All rights reserved.
 */

package com.newshunt.dataentity.social.entity
import java.io.Serializable

/**
 * created by mukesh.yadav on 18/06/20
 */
data class AdInsertResult(val uniqueAdIdentifier: String,
                          val isInserted: Boolean,
                          val prevPostId: String?,
                          val failReason: AdInsertFailReason) : Serializable

enum class AdInsertFailReason {
    UNKNOWN_FETCH_ID,
    FEED_DATA_CHANGED,
    QUERY_FAILED,
    NONE
}