package com.newshunt.dataentity.social.entity

import androidx.room.Entity

/**
 * Created by karthik.r on 2020-02-18.
 */
@Entity(tableName = "localdelete", primaryKeys = ["postId"])
data class LocalDelete (val postId: String, val ts: Long)
