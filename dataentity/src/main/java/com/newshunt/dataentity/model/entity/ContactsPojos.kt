/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.dataentity.model.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import com.newshunt.common.helper.common.Constants.CONTACT_SYNC_FREQ_DEFAULT
import com.newshunt.common.helper.common.Constants.CONTACT_SYNC_PAYLOAD_BUCKET_SIZE_DEFAULT
import com.newshunt.dataentity.social.entity.CardsPayload

/**
 * All contact sync related POJOs to be defined here
 * <p>
 * Created by srikanth.ramaswamy on 10/04/2019.
 */
//Intentionally naming the table cs, do not want to reveal the purpose and what it contains
const val CONTACTS_TABLE_NAME = "cs"
const val CONTACTS_ID = "_id"
const val CONTACT_VERSION = "version"
const val BUNDLE_CS_FULL_NEEDED = "bundle_cs_full_needed"

@Entity(tableName = CONTACTS_TABLE_NAME, primaryKeys = [CONTACTS_ID])
data class ContactEntity(@ColumnInfo(name = CONTACTS_ID) val _id: String,
                         @Ignore val payload: String? = null,
                         @ColumnInfo(name = CONTACT_VERSION) @Transient val version: Int) {
    constructor(_id: String, version: Int) : this(_id, null, version)
}

data class ContactsSyncPayload(val additions: MutableList<ContactEntity>,
                               val updations: MutableList<ContactEntity>,
                               val deletions: MutableList<String>) {
    fun size(): Int {
        return additions.size + updations.size + deletions.size
    }

    fun isEmpty(): Boolean = (size() <= 0)
}

/**
 * All contact sync related configurations expected from the config API need to be added in this
 * class
 */
data class CSConfig(val enabled: Boolean = true,
                    val frequencyTimeMS: Long = CONTACT_SYNC_FREQ_DEFAULT,
                    val bucketSizeNew: Int = CONTACT_SYNC_PAYLOAD_BUCKET_SIZE_DEFAULT)

/**
 * POST body for the Lite contact sync API
 * 'payload' field is an encrypted and gzipped string. Other lists are plain text json
 */
data class ContactsSyncLitePayload(val payload: String,
                                   val recentFollows: List<CardsPayload.P_Follow>? = null,
                                   val recentUnFollows: List<CardsPayload.P_Follow>? = null,
                                   val recentBlocks: List<CardsPayload.P_Follow>? = null)

/**
 * Each contact posted in the lite contact sync API
 * Has name, deduped (using a Set) list of phone numbers and emails
 */
data class ContactLiteItem(val name: String?,
                           val phones: HashSet<String>?,
                           val emails: HashSet<String>?) {
    override fun toString(): String {
        return "ContactLiteItem(name=$name, phone=$phones, emails=$emails)"
    }
}

/**
 * An event to indicate contact lite sync done. Used to throw bus event
 */
class ContactLiteSyncDone

/**
 * Exception telling server needs a full contact sync.
 */
class ContactSyncResetException constructor(msg: String): Exception(msg)

/**
 * Payload for each contact id. Used for full contact sync
 */
data class ContactFullItem(var phones: MutableSet<String>? = null,
                           var emails: MutableSet<String>? = null,
                           var nickname: MutableSet<String>? = null,
                           var grpMembership: MutableSet<String>? = null,
                           var org: MutableSet<String>? = null,
                           var relations: MutableSet<String>? = null,
                           var address: MutableSet<String>? = null,
                           var website: MutableSet<String>? = null,
                           var photoExists: MutableSet<Boolean>? = null,
                           var name: MutableSet<String>? = null,
                           var events: MutableSet<ContactProviderEvent>? = null,
                           var favourite: MutableSet<Int>? = null) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ContactFullItem

        if (phones != other.phones) return false
        if (emails != other.emails) return false
        if (nickname != other.nickname) return false
        if (grpMembership != other.grpMembership) return false
        if (org != other.org) return false
        if (relations != other.relations) return false
        if (address != other.address) return false
        if (website != other.website) return false
        if (photoExists != other.photoExists) return false
        if (name != other.name) return false
        if (events != other.events) return false
        if (favourite != other.favourite) return false

        return true
    }

    override fun hashCode(): Int {
        var result = phones?.hashCode() ?: 0
        result = 31 * result + (emails?.hashCode() ?: 0)
        result = 31 * result + (nickname?.hashCode() ?: 0)
        result = 31 * result + (grpMembership?.hashCode() ?: 0)
        result = 31 * result + (org?.hashCode() ?: 0)
        result = 31 * result + (relations?.hashCode() ?: 0)
        result = 31 * result + (address?.hashCode() ?: 0)
        result = 31 * result + (website?.hashCode() ?: 0)
        result = 31 * result + (photoExists?.hashCode() ?: 0)
        result = 31 * result + (name?.hashCode() ?: 0)
        result = 31 * result + (events?.hashCode() ?: 0)
        result = 31 * result + (favourite?.hashCode() ?: 0)
        return result
    }
}

/**
 * POJO representing contacts provider's event.
 */
data class ContactProviderEvent(val date: String? = null,
                                val type: String? = null)