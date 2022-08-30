/*
 * Copyright (c) 2020 Newshunt. All rights reserved.
 */

package com.newshunt.dataentity.common.asset

import com.newshunt.dataentity.search.SearchSuggestionItem

/**
 * Visitor pattern for using same adapter to show autocomplete-suggestions and search-results
 * @author satosh.dhanyamraju
 */
val SEARCH_ITEMVIEW_TYPE_SUGGESTION : Int = 1
val SEARCH_ITEMVIEW_TYPE_SEARCH_RESULT = 2

interface SearchViewTypeSelector {
    fun viewType(item: SearchSuggestionItem) = SEARCH_ITEMVIEW_TYPE_SUGGESTION
    fun viewType(item: Locations) = SEARCH_ITEMVIEW_TYPE_SEARCH_RESULT
}
val DEFAULT_SEARCH_TYPE_SELECTOR = object : SearchViewTypeSelector {}

interface SearchUIVisitor {
    fun viewType(typeSelector : SearchViewTypeSelector) : Int
}