package com.newshunt.news.model.daos

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.newshunt.dataentity.common.asset.Location
import com.newshunt.dataentity.common.asset.LocationEntityLevel
import com.newshunt.dataentity.common.asset.LocationIdParent
import com.newshunt.dataentity.common.asset.Locations
import com.newshunt.dataentity.common.pages.EntityInfoResponse
import io.reactivex.Observable


@Dao
abstract class LocationEntityDao : BaseDao<Location> {

    @Query("SELECT * FROM locations WHERE level = :level")
    abstract fun getLocation(level: LocationEntityLevel): LiveData<List<Location>>

    @Query("SELECT * FROM locations WHERE id IN (SELECT id FROM follow)")
    abstract fun getFollowedLocations(): List<Location>

    @Query("""
    SELECT l.* ,
    CASE WHEN fl.entityId IS NULL THEN 0 ELSE 1 END isFollowed
    FROM locations l
    LEFT JOIN follow fl ON fl.entityId = l.id AND fl.`action`='FOLLOW'
    WHERE level = :level AND parentid =:parentId
    ORDER BY viewOrder
  """)
    abstract fun getKidLocations(level: LocationEntityLevel, parentId: String): LiveData<List<Location>>

    @Query("SELECT * FROM locations_view WHERE parentid IS NULL AND level ='ALL_LOCATIONS'")
    abstract fun getLocationsNested() : LiveData<List<Locations>>

    @Query("SELECT * FROM locations_view WHERE parentid IS NULL AND level ='ALL_LOCATIONS'")
    abstract fun getLocationsNestedObs() : Observable<List<Locations>>

    //todo prevent ondelete cascade fetchinfo
    @Query("DELETE FROM locations ")
    abstract fun deleteAllLocations()

    @Query("DELETE FROM locations WHERE id=:id")
    abstract fun deleteLocation(id: String)

    @Query("DELETE FROM locations WHERE level=:level")
    abstract fun deleteAllLocationLevel(level: String)

    @Query("""
    SELECT l.* ,
    CASE
               WHEN fl.entityId IS NULL THEN 0
               ELSE (CASE
                         WHEN fl.`action`='FOLLOW' THEN 1
                         ELSE 0
                     END)
           END isFollowed
    FROM locations l
    LEFT JOIN follow fl ON fl.entityId = l.id 
   WHERE level =:level
    
  """)
    abstract fun getLocationsRecommended(level: String): LiveData<List<Location>>

    @Transaction
    open fun replaceLocations(list: List<EntityInfoResponse>) {
        deleteAllLocationLevel(LocationEntityLevel.ALL_LOCATIONS.name)
        list.forEach { location ->
            val parent = Location(location.parent)
            parent?.let { insIgnore(parent.copy(level = LocationEntityLevel.ALL_LOCATIONS.name,
                    parentid = null)) }
            val kids = location.kids
            kids?.forEach {
                insIgnore(Location(it).copy(level = LocationEntityLevel.ALL_LOCATIONS.name, parentid = parent?.id, isAlsoParent = it.id == parent.id))
            }
        }
    }

    @Query("SELECT * FROM locations_view WHERE id = :id AND (parentid = :parentId OR parentid IS NULL)")
    abstract fun lookup(id: String, parentId: String? = null) : Location?

    @Transaction
    open fun readFromIds(l: List<LocationIdParent>): List<Locations> {
        return l.mapNotNull {parent->
            val parentLoc = lookup(parent.id, null)
            if(parentLoc == null) null
            else {
                val kids = parent.items.mapNotNull { kid -> lookup(kid.id, parent.id) }
                Locations(parentLoc, kids)
            }
        }
    }


    @Transaction
    open fun replaceRecommendations(list: List<Location>) {
        deleteAllLocationLevel(LocationEntityLevel.RECOMMENDATION.name)
        list.forEach { location ->
            insIgnore(location.copy(level = LocationEntityLevel.RECOMMENDATION.name))

        }
    }
}
