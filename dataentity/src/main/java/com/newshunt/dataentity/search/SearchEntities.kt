/*
 * Created by Rahul Ravindran at 27/9/19 7:11 PM
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.dataentity.search

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose
import com.newshunt.dataentity.common.asset.AnyCard
import com.newshunt.dataentity.common.asset.CreatePostUiMode
import com.newshunt.dataentity.common.asset.SearchUIVisitor
import com.newshunt.dataentity.common.asset.SearchViewTypeSelector
import com.newshunt.dataentity.common.model.entity.BaseError
import com.newshunt.dataentity.common.model.entity.SearchRequestType
import com.newshunt.dataentity.common.model.entity.model.MultiValueResponse
import com.newshunt.dataentity.social.entity.SearchPage
import java.io.Serializable
import java.util.TimeZone

/**
 * File containing all pojos for search
 * @author shrikant.agarwal
 */

@Entity(tableName = "recent_search")
data class RecentSearchEntity(
        @PrimaryKey @ColumnInfo(name="search") val  query: String,
        @ColumnInfo(name = "time") val timeStamp : Long = System.currentTimeMillis(),
        @ColumnInfo(name = "json") val json : String = "") : Serializable

enum class SearchSuggestionType(val type:String): Serializable {
    RECENT("recent"),
    RECENT_HEADER("recent_header"),
    TRENDING("trending"),
    TRENDING_HEADER("trending_header"),
    HASHTAG("hashtag"),
    HASHTAG_UNIFIED("hashtag_unified"),
    HANDLE_UNIFIED("handle_unified"),
    HANDLE("handle"),
    SUGGESTION("autocomp_keywords");

    companion object {
        fun findSSType(type:String): SearchSuggestionType? = values().find { it.type == type }
    }
}

data class SearchSuggestionItem(@Expose val id: String = "",
                                @Expose val suggestion: String = "",
                                @Expose val searchParams: Map<String, String>? = null,
                                @Expose val deeplinkUrl: String = "",
                                val iconUrl: String = "",
                                val iconNightMode: String = "",
                                @Expose val uiType: String? = null,
                                var suggestionType: SearchSuggestionType = SearchSuggestionType.SUGGESTION,
                                val creatorType:String = "",
                                val ts: Long = 0L,
                                val searchContext: String = "",
                                val requestId: String = "",
                                var isEndItem : Boolean = false,
                                @Expose var typeName: String = "",
                                @Expose val groupType: String = "",
                                @Expose val subType: String = "",
                                @Expose val userId: String = "",
                                @Expose val itemId : String = "",
                                @Expose val followersCount: String? = "",
                                @Expose val imageUrl:String? = "",
                                @Expose val entityType:String? = "",
                                @Expose val name:String? = "",
                                @Expose val searchPayloadContext: SearchPayloadContext? = null,
                                @Expose val experiment: Map<String, String> = mapOf()): Serializable, SearchUIVisitor {

    @JvmOverloads
    fun toSearchPayload(searhRequestType: SearchRequestType? = null): SearchPayload {
        val userData = SearchAppUserData(null,
                "",
                System.currentTimeMillis().toString(),
                TimeZone.getDefault().getDisplayName(true, TimeZone.SHORT),
                searchContext,
                requestId,
                contextMap = searchPayloadContext)
        // for location, we need to pass type. Not required for others
        val ltype = if(searhRequestType == SearchRequestType.LOCATION) "LOCATION" else null
        return SearchPayload(
                suggestion,
                ltype,
                emptyList(),
                searchParams ?: emptyMap(),
                userData)
    }

    override fun viewType(typeSelector: SearchViewTypeSelector): Int {
        return typeSelector.viewType(this)
    }
}

data class SuggestionResponse<T>(val maxRecentSize: Int = 3,
                              val maxListSize: Int = 12,
                              val version: String = "0",
                              var rows: T? = null)

data class CreatePostTrendingResponse(val HASHTAG: List<SearchSuggestionItem>, val HANDLE: List<SearchSuggestionItem>)


data class SearchUiEntity<T>(val query: String = "",
                             val response: T,
                             val responseTs: Long = System.currentTimeMillis())

typealias SuggestionUiResponse = SearchUiEntity<SuggestionResponse<List<SearchSuggestionItem>>>
typealias SugResponse = SuggestionResponse<List<SearchSuggestionItem>>
typealias SearchUiResponse = SearchUiEntity<AggrMultivalueResponse>
class Aggrs(val values: List<SearchPage> = listOf())
class AggrMultivalueResponse(val aggrs: Aggrs? = Aggrs(),
                             val emptySearchTitle: String? = "",
                             val emptySearchSubTitle: String? = "",
                             val correctedSearchKeyword: String? = "",
                             val correctedSearchParams: Map<String, String>? = emptyMap(),
                             val error : BaseError? = null) : MultiValueResponse<AnyCard>()

typealias SearchRequest = SearchSuggestionItem
typealias SearchQuery = SearchSuggestionItem

data class UserData(val campaign: String,
                    val clientTS: Long,
                    val clientTZ: String,
                    val context: String,
                    val cid: String,
                    val deviceWidth: Int,
                    val deviceHeight: Int,
                    val cookieInfo: Map<String, String>,
                    val searchRequestId: String = "",
                    var postText:String = "",
                    val contextMap: SearchPayloadContext? = null)

data class SuggestionPayload(var query: String? = null,
                             val appUserData: UserData? = null,
                             val type: String = "") : Serializable

data class SearchPayloadContext(val garbage: String? = null,
                                val section: String? = null,
                                val entityType: String? = null,
                                val entityId: String? = null,
                                val postId: String? = null,
                                val parentPostId: String? = null,
                                val groupId: String? = null,
                                val action: String? = null) : Serializable



// Search payload context
const val SEARCH_ACTION_TYPE = "SEARCH_ACTION_TYPE"
enum class SearchActionType(val typeName:String) {
    UNIFIED("unified"),
    POST("post"),
    COMMENT("comment"),
    RE_POST("repost"),
    COMMENT_REPLY("comment_reply"),
    GROUP_PARTICIPANT_SEARCH("group_participant_search"),
    GROUP_ADD_PARTICIPANT("group_add_participant"),
    LOCATION("location");

    companion object {

        fun getsearchActionType(mode: CreatePostUiMode): SearchActionType {
            return when (mode) {
                CreatePostUiMode.COMMENT -> SearchActionType.COMMENT
                CreatePostUiMode.POST -> SearchActionType.POST
                CreatePostUiMode.REPOST-> SearchActionType.RE_POST
                CreatePostUiMode.REPLY-> SearchActionType.COMMENT_REPLY
                else -> SearchActionType.POST
            }
        }
    }
}