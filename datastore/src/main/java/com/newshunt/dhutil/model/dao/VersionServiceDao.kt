/**
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */
package com.newshunt.dhutil.model.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.newshunt.common.helper.common.Constants
import com.newshunt.dataentity.dhutil.model.entity.version.VersionDbEntity

/**
 * @author shrikant.agrawal
 * Service Dao to handle the version db operations
 */
@Dao
interface VersionServiceDao {

    @Query("SELECT * from version_data WHERE entity_type = :entityType AND parent_type = :parentType AND parent_id = :parentId")
    fun getVersionEntity(entityType:String, parentType: String = Constants.EMPTY_STRING, parentId: String = Constants.EMPTY_STRING) : VersionDbEntity

    @Query("SELECT version from version_data WHERE entity_type = :entityType AND parent_type = :parentType AND parent_id = :parentId")
    fun getStoredVersion(entityType:String, parentType: String = Constants.EMPTY_STRING, parentId: String = Constants.EMPTY_STRING) : String

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertVersionEntity(versionDbEntity: VersionDbEntity): Long

    @Query("UPDATE version_data SET version='0'")
    fun resetVersionForAll()

    @Query("DELETE from version_data WHERE entity_type = :entityType AND parent_type = :parentType AND parent_id = :parentId")
    fun deleteVersionEntity(entityType: String, parentType: String = Constants.EMPTY_STRING, parentId: String= Constants.EMPTY_STRING)

    @Query("UPDATE version_data SET version='0' WHERE entity_type = :entityType AND parent_type = :parentType AND parent_id = :parentId")
    fun resetVersionForEntity(entityType: String, parentType: String = Constants.EMPTY_STRING, parentId: String= Constants.EMPTY_STRING)

    @Query("DELETE from version_data WHERE entity_type IN (:list)")
    fun removeVersionEntities(list: List<String>)

    @Transaction
    fun insertAfterVersionValidation(validator: ((String?) -> Boolean)? = { true },
                                     versionDbEntity: VersionDbEntity): Long {
        return if(validator?.invoke(getStoredVersion(versionDbEntity.entityType)) == true) {
            insertVersionEntity(versionDbEntity)
        } else {
            0
        }
    }
}