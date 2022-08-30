/*
 * Created by Rahul Ravindran at 27/9/19 7:11 PM
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.dataentity.search



/**
 * To be used as payload for search requests
 *
 * @author satosh.dhanyamraju
 */
data class SearchPayload(private val query: String? = null,
                         private val type: String? = null,
                         private val servedPaginatedItems: List<String>? = null,
                         private val searchParams: Map<String, String> = mapOf(),
                         private val appUserData: SearchAppUserData? = null
)

data class SearchAppUserData(private val currentTab: SearchTab? = null,
                             private val campaign: String = "",
                             private val clientTS: String = "",
                             private val clientTZ: String = "",
                             private val context: String = "",
                             private val searchRequestId: String = "",
                             private val autoplayPlayerTypes: List<*>? = emptyList<Any>(),
                             private val contextMap: SearchPayloadContext? = null)
//TvAppProvider
//.getInstance().tvAppInterface.autoplayPlayerTypes)

data class SearchTab(private val pageType: String?,
                     private val entityId: String?)
