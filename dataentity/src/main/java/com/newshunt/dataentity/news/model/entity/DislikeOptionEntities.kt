/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.dataentity.news.model.entity

import com.newshunt.dataentity.news.model.entity.server.asset.L1L2Mapping
import java.io.Serializable

/**
 * Contains entities for dislike UI
 *
 * @author satosh.dhanymaraju
 */


enum class MenuL1ClickAction {
    POST,
    NA
}

/*
Depends on
- where it is shown : news/detail/buzz
- type of card : news/video
- external state : isFollowing?/ inInPL?/ isSaved?

 */
enum class MenuL1Filter {
    // if news-card -> ok-block-source
    CAN_BLOCK,
    // if news-card & !following -> use-source
    CAN_FOLLOW,
    // if news-card & following -> use-source
    CAN_UNFOLLOW,
    // if shown-in-news-detail & saveable-card & !saved -> ok
    CAN_SAVE,
    // if showing in news-detail & saved -> ok
    CAN_UNSAVE,
    // if showing & blocked -> true
    CAN_UNBLOCK,
    // the default
    NA
}

enum class MenuL2Action {
    BLOCK,
    NA
}

enum class MenuL1PostClkAction {
    INLINE_L2,
    FULLSCREEN_L2,
    BLOCK_NA,
    UNBLOCK_NA,
    BLOCK_FULLSCREEN_L2,
    BLOCK_INLINE_L2,
    BROWSER,
    BROWSER_SOURCE,
    FOLLOW,
    UNFOLLOW,
    SAVE,
    UNSAVE,
    SHARE,
    CHANGE_FONT,
    ADD_COMMENT,
    DOWNLOAD_VIDEO, // applicable for only video card which can be downloaded
    ENABLE_NSFW_FILTER, // applicable only for viral cards
    BROWSE_BY_SOURCE, // For news cards; opens newspaper activity
    ENABLE_AUTOPLAY, // enable auto play video flag -> change to AUTO_PLAY_ALWAYS
    DISABLE_AUTOPLAY, // disable auto play video flag -> change to AUTO_PLAY_DISABLED

    DELETE_POST, /*To delete post accessible only for (group owner or admin) or (creator of post)*/
    REPORT_POST,
    NA; // should be used as default, if not present is API response

    fun isFullScreenL2() = when (this) {
        FULLSCREEN_L2, BLOCK_FULLSCREEN_L2 -> true
        else -> false
    }

    fun isInline() = when (this) {
        INLINE_L2, BLOCK_INLINE_L2 -> true
        else -> false
    }

    fun isBlock() = when (this) {
        BLOCK_NA, BLOCK_FULLSCREEN_L2, BLOCK_INLINE_L2 -> true
        else -> false
    }

    fun isUnBlock() = when (this) {
        UNBLOCK_NA -> true
        else -> false
    }
}

data class MenuL1Meta(
        val id: String,
        val icon: String,
        val nIcon: String,
        val clickAction: MenuL1ClickAction?, // will be null if not understood
        val hideCard: Boolean,
        val postAction: MenuL1PostClkAction?, // will be null if not understood
        val title: String?,
        val browserUrl: String?,
        val isDislike: Boolean?, // TODO (satosh.dhanyamraju): nullable?
        val filter: MenuL1Filter?,
        val isDislikeL2: Boolean?) : Serializable {

    companion object {
        fun dummy() = MenuL1Meta("id", "icon", "nIcon", MenuL1ClickAction.NA
                , true, null, "text", null, null, null, null)
    }
}

data class MenuL2Meta(
        val id: String,
        val title: String,
        val actionMenu: MenuL2Action? // TODO (satosh.dhanyamraju): make it MenuL2Action
) : Serializable

/**
 * should be type of ver-api
 *
 */
data class MenuMasterOpts(
    val l1Title: String,
    val l1SubTitle: String,
    val l2Title: String,
    val l2SubTitle: String,
    val version: String,
    val l2Icon: String,
    val l2nIcon: String,
    val l2SendButtonText: String,
    val postUrl: String,
    val masterOptionsL1: Map<String, MenuL1Meta>,
    val masterOptionsL2: Map<String, MenuL2Meta>,
    val listL1OptionsForAssetType: Map<String, List<L1L2Mapping>>, // "L1_HIDE_NEWS": "L2_NOTINT, L2_VULGAR, L2_OTHER"
    val detailL1OptionsForAssetType: Map<String, List<L1L2Mapping>>)


/**
 * Common entity to be posted to different APIs
 */
data class MenuEntity(
        val itemId: String,
        val groupId: String? = null,
        val groupType: String = "",
        val npId: Long = -1, //   for older cached stories
        val source: String? = null,
        val displayLocation: DisplayLocation = DisplayLocation.CARD_EXTERNAL,
        val createdAt: Long = -1,
        val uiType: String = "NA"/* default "NA" for older cached stories*/,
        val options: List<String> = emptyList(),
        val optionsL2: List<String> = emptyList(),
        val eventParam: String? = null,
        val isPerSession: Boolean = false) {

    val uniqueId: String
        get() = "$itemId + $groupId"

    fun minimal(): MenuEntity = MenuEntity(itemId, groupId)
}

enum class DisplayLocation {
    DETAIL,
    CARD_EXTERNAL,
    CARD_INLINE
}

/**
 * Will be thrown by consumer of versioned entity when it finds a problem with the entry.
 * Typically, it will trigger a re-fetch of the content.
 *
 */
class UnexpectedDataFormatException(msg: String? = "") : Throwable(msg)