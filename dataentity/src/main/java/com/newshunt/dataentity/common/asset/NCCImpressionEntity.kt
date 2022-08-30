/*
 * Copyright (c) 2022 Newshunt. All rights reserved.
 */
package com.newshunt.dataentity.common.asset

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.newshunt.dataentity.social.entity.TABLE_NCC_IMPRESSION

/*
* author Mukesh Yadav
* */
@Entity(tableName = TABLE_NCC_IMPRESSION)
data class NCCImpression(
    @PrimaryKey
    var cardId: String,
    val data: String?,
    var status: NCCStatus = NCCStatus.NOT_SYNCED
)

enum class NCCStatus {
    NOT_SYNCED,
    SYNCED,
    SYNCING
}