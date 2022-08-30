/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.dataentity.social.entity

import com.newshunt.dataentity.common.asset.ArticleTimeSpentTrackEntity
import com.newshunt.dataentity.common.model.entity.AutoplayPlayerType
import com.newshunt.dataentity.news.model.entity.server.asset.VideoItem
import java.io.Serializable

/**
 * @author satosh.dhanyamraju
 */
class CardsPayload(
        val edition: String,
        val currentTab: RecentTabEntity?,
        val deviceWidth: Int,
        val deviceHeight: Int,
        val follows: List<P_Follow>,
        val connectionInfo: P_ConnInfo?,
        val autoplayPlayerTypes: List<AutoplayPlayerType>,
        val langs: String,
        val isUserSelectedLang:Boolean,
        val successfulPrevFeedLoadSessions:Int?,
        val languageCardShownCount: Int?,
        val clientTZ: String,
        val clientTS: String,
        val cid: String,
        val requestId: String,
        val recentArticlesV2: P_RecentArticles,
        val requestSessionContext: P_RequestSessionContext,
        val currentVideoSessionInfo: P_VideoSessionInfo,
        val recentTabs: Map<String, List<RecentTabEntity>> = emptyMap(),
        val viewMoreParams: Map<String, String> = mapOf(),
        val dislikesV2: List<DislikeEntity>,
        val userHasAnyFollows: Boolean,
        val enableSmallCards: Boolean,
        val isNotificationEnabled: Boolean,
        val localCookie: String?,
        val globalCookie: String?,
        val videosServedNotViewed: HashSet<String>?,
        val selectedLocation: String?,
        val recommendFollowBlockRequest:FollowBlockRequest?,
        val cardsDiscarded: List<String>,
        val cardsViewed : List<String>,
        val cssBatchId : String,
        val impressionsData : List<String>?
) {

    class P_Tab(
            val entityId: String,
            val entityType: String = "page",
            val section: String = "news",
            val ts : Long = 0
    )

    class P_ConnInfo(
            val type: String,
            val quality: String
    )

    class P_Follow(
            val id: String,
            val type: String,
            val subType: String? = null
    )


    data class P_VideoSessionInfo(val recentPlayed: List<VideoItem>?)

    class P_RequestSessionContext(
            val sessionRefreshCount: Int,
            val pullInfo: List<PullInfoEntity>
    )

    data class P_RecentArticles(
            val news: List<ArticleTimeSpentTrackEntity>? = null,
            val buzz: List<ArticleTimeSpentTrackEntity>? = null
    )

    data class FollowBlockRequest(var entityId:String?,
                                           var sourceName:String?,
                                           var entityType:String?,
                                           var entitySubType:String?,
                                           var action: String?) :Serializable
}