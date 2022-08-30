/*
* Copyright (c) 2019 Newshunt. All rights reserved.
*/
package com.newshunt.dataentity.social.entity

import `in`.dailyhunt.money.contentContext.ContentContext
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.io.Serializable

/**
 * @author raunak.yadav
 */

data class Position(val relativePos: String? = null,
                    val element: String? = null) : Serializable

data class ZoneConfig(val zone: String? = null,
                      val position: Position? = null,
                      val subSlots: List<String>? = null,
                      val showIf: ShowIf?) : Serializable

data class AdZones(val toHide: List<ZoneConfig>? = null,
                   val adZonePositions: List<ZoneConfig>? = null) : Serializable

data class ShowIf(val operand: String? = null, val rules: List<AdRule>? = null) : Serializable

data class AdRule(val element: String? = null, val on: String? = null) : Serializable

/**
 * Content should have this meta to decide display and position of ads
 */
@Entity(tableName = "ad_spec",
        indices = [Index(value = ["entityId", "section"], unique = true)])
data class AdSpecEntity(
        @PrimaryKey(autoGenerate = true) var id: Int = 0,
        val entityId: String,
        val adSpec: AdSpec,
        val type: String? = "entity",  //or post
        val entryTs: Long = System.currentTimeMillis(),
        val inHandshake: Boolean = false,
        val section: String = SECTION_ANY) {
    companion object {
        const val SECTION_ANY: String = "any"
    }
}

data class AdSpec(val adZones: AdZones? = null,
                  val contentContexts: Map<String, ContentContext>? = null) : Serializable
