/*
* Copyright (c) 2020 Newshunt. All rights reserved.
*/
package com.newshunt.dataentity.ads

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Ads persisted to be used in next session.
 */
@Entity(tableName = "persisted_ads",
        indices = [Index(value = ["adId"], unique = true)])
data class PersistedAdEntity(@PrimaryKey(autoGenerate = true) var id: Int = 0,
                             val adId: String,
                             val adGroupId: String,
                             val campaignId: String,
                             val adPosition: String,
                             val adContentType: String,
                             val adJson: String)