package com.newshunt.dataentity.common.asset

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Relation
import com.newshunt.dataentity.common.pages.ActionableEntity
import com.newshunt.dataentity.common.pages.EntityInfoList
import com.newshunt.dataentity.common.pages.EntityInfoResponse
import com.newshunt.dataentity.common.pages.PageEntity
import com.newshunt.dataentity.social.entity.LocationsView
import java.io.Serializable


data class AllLocationResponse(val version: String = "0",
                               val rows: List<EntityInfoResponse>?) : Serializable


/**
 * Implements [AnyCard] not for json-parsing, but to fit-in search response.
 * [LocationIdParent] will be transformed into this
 * Used in output of Dao query (contains relation)
 * Not using [EntityInfoList] because its relation is defined in another table and there are additional fields
 */
data class Locations(@Embedded var parent: Location? = null,
                     @Relation(parentColumn = "id", entityColumn = "parentid", entity = LocationsView::class)
                     var kids: List<Location>? = emptyList(),
                     @Ignore var areChildrenVisible: Boolean = false) : Serializable, AnyCard, SearchUIVisitor {
    override fun viewType(typeSelector: SearchViewTypeSelector): Int {
        return typeSelector.viewType(this)
    }
}

@Entity(tableName = "locations", primaryKeys = ["id", "level", "isAlsoParent"])
data class Location(@Embedded val entity: PageEntity,
                    val parentid: String? = null,     // DB
                    var level: String = LocationEntityLevel.ALL_LOCATIONS.name, // DB
                    var isFollowed: Boolean = false,     //  DB>Calculated
                    var isAlsoParent: Boolean = false
) : Serializable {
    val id: String
        get() = entity.id
    val displayName: String?
        get() = entity.displayName

    val name: String?
        get() = entity.name

    val nameEnglish: String?
        get() = entity.nameEnglish

    val circleImageUrl: String?
        get() = entity.entityImageUrl

    val entityImageUrl: String?
        get() = entity.entityImageUrl

    val entityType: String
        get() = entity.entityType


    fun toActionableEntity(): ActionableEntity {
        return ActionableEntity(entityId = entity.id, entityType = entity.entityType, entitySubType = entity.subType,
                entityImageUrl = entity.entityImageUrl, iconUrl = entity.header?.logoUrl, displayName = entity.displayName,
                deeplinkUrl = entity.deeplinkUrl, nameEnglish = entity.nameEnglish)
    }
}


enum class LocationEntityLevel {
    ALL_LOCATIONS,
    RECOMMENDATION
}

class LocationIdParent(val id: String,
                 val items: List<LocationIdChild>) : AnyCard {
}
class LocationIdChild(val id: String)
