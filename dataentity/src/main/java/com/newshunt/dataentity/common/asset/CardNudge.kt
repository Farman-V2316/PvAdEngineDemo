/*
 * Copyright (c) 2020 Newshunt. All rights reserved.
 */

package com.newshunt.dataentity.common.asset

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Table to store nudges and state
 * @author satosh.dhanyamraju
 */
@Entity(tableName = "card_nudge")
data class CardNudge(
        @PrimaryKey val id: Int,
        val level: PostEntityLevel? = null,
        val format: Format? = null,
        val subFormat: SubFormat? = null,
        val uiType2: UiType2? = null,
        val hasCommentsOrReposts: Boolean? = null,
        val type: String, // nudge_card
        val terminationType: String, // "share", "repost", "comment"
        val text : String,
        val tooltipDurationSec: Int,
        val maxAttempts: Int = 0,
        val sessionGroup : Int = -1, // only 1 nudges from a same group will be shown in the same session.
        @Embedded(prefix = "st_")
        val state: CardNudgeState? = null
)

/**
 * State. Will be preserved even if communication data changes
 * @author satosh.dhanyamraju
 */
data class CardNudgeState(
        val curAttempts: Int = 0,
        val active: Boolean = false, // for managing per session
        val terminated: Boolean = false // no longer valid
)

/**
 * To be created and passed from view layer for matchings nudges.
 * @author satosh.dhanyamraju
 */
data class CardInfo(
        val id: String,
        val level: PostEntityLevel,
        val format: Format,
        val subFormat: SubFormat,
        val uiType2: UiType2,
        val hasCommentsOrReposts: Boolean
)


enum class CardNudgeTerminateType {
    comment,
    repost,
    share
}