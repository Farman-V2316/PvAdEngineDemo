/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.dataentity.social.entity

import androidx.room.Entity

/**
 * To store recent tabs (only to be sent in payload)
 * @author satosh.dhanyamraju
 */
@Entity(tableName = RecentTabEntity.TABLE, primaryKeys = ["entityId", "entityType", "section"])
class RecentTabEntity(
        val entityId: String,
        val entityType: String,
        val section: String,
        val ts : Long // will be updated everytime accessed. Insert replace. This also would unique, so it will be used for deletion.
){
    companion object {
        const val TABLE = "recent_tabs"
    }
}