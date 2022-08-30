/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.dataentity.notification

import java.io.Serializable

/***
 * @author amit.chaudhary
 * */
data class SocialContentMeta(val title: String?,
                             val deepLinkUrl: String?,
                             val commentParams: Map<String, String>?) : Serializable {
    override fun toString(): String {
        return "SocialContentMeta(deepLinkUrl=$deepLinkUrl, commentParams=$commentParams)"
    }
}