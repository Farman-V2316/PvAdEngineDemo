/*
 * Copyright (c) 2020 Newshunt. All rights reserved.
 */

package com.newshunt.news.model.daos

import androidx.annotation.VisibleForTesting
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.newshunt.common.helper.common.Logger
import com.newshunt.dataentity.common.asset.CardInfo
import com.newshunt.dataentity.common.asset.CardNudge
import com.newshunt.dataentity.common.asset.CardNudgeState
import com.newshunt.dataentity.common.asset.Format
import com.newshunt.dataentity.common.asset.PostEntityLevel
import com.newshunt.dataentity.common.asset.SubFormat
import com.newshunt.dataentity.common.asset.UiType2
import com.newshunt.dataentity.common.model.entity.EventsInfo

/**
 * Does not extend BaseDao : No direct insertion, because it will lose state
 * @author satosh.dhanyamraju
 */

// TODO(satosh.dhanyamraju): log why some nudges are not shown.

//  Even after verAPI change (they keep the state), if you really want to reset the state, change id

@Dao
abstract class NudgeDao {
    private val LOG_TAG: String = "NudgeDao"
    @Query("SELECT * FROM card_nudge WHERE id = :id")
    abstract fun lookup(id: Int): CardNudge?

    @VisibleForTesting
    @Query("SELECT * FROM card_nudge")
    internal abstract fun all(): List<CardNudge>

    @Query("SELECT * FROM card_nudge WHERE st_active = 1 AND st_terminated = 0 AND (level is NULL OR level = :level) AND (format IS NULL OR format = :format) AND (subFormat IS NULL OR subFormat = :subFormat) AND (uiType2 IS NULL OR uiType2 = :uiType2) AND (hasCommentsOrReposts IS NULL OR hasCommentsOrReposts = :hasCommentOrReposts) LIMIT 1")
    abstract fun matching(level: PostEntityLevel, format: Format, subFormat: SubFormat, uiType2: UiType2, hasCommentOrReposts: Boolean) : CardNudge?

    fun readCardNudges(cardInfos: List<CardInfo>): List<Pair<CardInfo, CardNudge?>> {
        return cardInfos.map {
            it to matching(it.level, it.format, it.subFormat, it.uiType2, it.hasCommentsOrReposts)
        }
    }

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    internal abstract fun insertIgnore(nudge: CardNudge)

    @Update
    abstract fun update(nudge: CardNudge)

    @Query("UPDATE card_nudge SET st_active = 0 WHERE sessionGroup = (SELECT sessionGroup FROM card_nudge WHERE id = :id)")
    internal abstract fun markAllInTheSameGroupInactive(id: Int)

    @Query("UPDATE card_nudge SET st_active = 1 WHERE st_terminated = 0")
    internal abstract fun markAllActive()

    @Query("UPDATE card_nudge SET st_curAttempts = st_curAttempts + 1 WHERE id = :id")
    internal abstract fun incAttempts(id: Int)

    @Query("UPDATE card_nudge SET st_terminated = 1 WHERE st_curAttempts = maxAttempts")
    internal abstract fun terminateIfMaxAttemptsReached()

    @Query("UPDATE card_nudge SET st_terminated = 1 WHERE terminationType = :type")
    abstract fun terminate(type: String) : Int

    @Query("DELETE FROM card_nudge WHERE id NOT IN (:ids)")
    abstract fun deleteRowsMatching(ids: List<Int>)

    @Transaction
    open fun markShown(id: Int) {
        Logger.d(LOG_TAG, "markShown: $id")
        markAllInTheSameGroupInactive(id)
        incAttempts(id)
        terminateIfMaxAttemptsReached()
    }

    @Transaction
    open fun updateFrom(c: List<EventsInfo>) {
        Logger.d(LOG_TAG, "updateFrom: ${c.size}")
        /* IMPL
        1. filter by type only card events
        2. ids not there in newslist : DELETE
        3. for every newlistitem,
            a) if there in DB, keep the state and update rest
            b) else, insIgnore

         */
        val cardNudges = c.filter {
            it.activity?.type == "nudge_card"
        }
        deleteRowsMatching(cardNudges.map{it.id})
        cardNudges.forEach {evt ->
            val storedNudge = lookup(evt.id)
            val curNudge = CardNudge(evt.id,
                    evt.precondition?.get("level")?.let { PostEntityLevel.valueOf(it) },
                    evt.precondition?.get("format")?.let { Format.valueOf(it) },
                    evt.precondition?.get("subFormat")?.let { SubFormat.valueOf(it) },
                    evt.precondition?.get("uiType")?.let { UiType2.valueOf(it) },
                    evt.precondition?.get("hasCommentsOrReposts")?.let { it == "true" },
                    evt.activity?.type?:"",
                    evt.precondition?.get("terminationType")?:"",
                    evt.activity?.attributes?.get("text")?:"",
                    evt.activity?.attributes?.get("tooltipDurationSec")?.toInt() ?:10,
                    evt.precondition?.get("maxAttempts")?.toInt()?:1,
                    evt.precondition?.get("sessionGroup")?.toInt()?: -1,
                    CardNudgeState(active = true)
                    )
            if (storedNudge != null) {
                val updatedNudge = curNudge.copy(state = storedNudge.state)
                update(updatedNudge)
            } else {
                insertIgnore(curNudge)
            }
        }
    }

}