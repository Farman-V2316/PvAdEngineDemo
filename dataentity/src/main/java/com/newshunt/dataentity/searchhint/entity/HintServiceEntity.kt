/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */

package com.newshunt.dataentity.searchhint.entity

/**
 * @author madhuri.pa
 *
 */

// Will be parsed and as a string. Sent to BE
enum class SearchContext {
    NewsHomeForYou, // ForYou tab
    NewsHomeHeadlines, // headlines tab
    NewsHomeViral, // viral tab
    NewsHomeOther, // for all other tabs in news-home
    BuzzHome, // Buzz section
    FollowSources, // Follow section > see all sources
    FollowTopics, // Follow section > see all topics
    FollowHome, // Every where else, in follow section
    Global // For notification inbox and all other places.
}


// activity containing the searchbar
enum class SearchLocation {
    NewsHome,
    BuzzHome,
    FollowSources,
    FollowTopics,
    FollowHome,
    Global,
    PeapleSearch,
    MemberSearch,
    LocationSearch
}


data class HintServiceEntity(
        val version:String,
        // language to pojo mapping
        val hint: Map<String, Map<String, List<SearchHint>>>?
)

data class SearchHint(
        val displayText: String,
        val key : String,
        val pageType: String,
        val pageId : String
)

