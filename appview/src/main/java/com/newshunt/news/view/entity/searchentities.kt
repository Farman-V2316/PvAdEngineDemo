/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */

package com.newshunt.news.view.entity

import java.io.Serializable

/**
 * @author satosh.dhanymaraju
 */

/**
 * To be passed to CardsPresenter, based on which it can create different payloads
 */
enum class NewsListPayloadType {
    PAYLOAD_FORYOU, PAYLOAD_SEARCH
}



data class SearchProps(
        val payloadType: NewsListPayloadType = NewsListPayloadType.PAYLOAD_FORYOU,
        val searchQuery: String,
        val searchParams: Map<String, String>? = null,
        val context: String = "",
        val useGrid: Boolean,
        val searchRequestId: String
): Serializable {
    /**
     * To be used by cardspresenter to create request payload
     * And by CardsFragment to disable swipeRefersh and ads
     */
    fun isSearchPayload() = (payloadType == NewsListPayloadType.PAYLOAD_SEARCH)

    companion object {
        @JvmStatic
        val DEFAULT = SearchProps(NewsListPayloadType.PAYLOAD_FORYOU, "", null, "", false, "")
    }
}