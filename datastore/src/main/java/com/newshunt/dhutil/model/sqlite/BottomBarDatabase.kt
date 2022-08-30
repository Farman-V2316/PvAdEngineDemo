/*
* Copyright (c) 2018 Newshunt. All rights reserved.
*/
package com.newshunt.dhutil.model.sqlite

import androidx.lifecycle.LiveData
import androidx.room.*

import com.newshunt.dataentity.common.helper.common.CommonUtils

/**
 * @author santhosh.kc
 */

private const val PRIMARY_KEY_FOR_RESPONSE = "bottom_bar_primary_key"
private const val BOTTOM_BAR_RESPONSE_TABLE = "bottom_bar_response_table"

@Entity(tableName = BOTTOM_BAR_RESPONSE_TABLE)
data class BottomBarEntity(@PrimaryKey @ColumnInfo(name = "pk") val pk: String = PRIMARY_KEY_FOR_RESPONSE,
                           @ColumnInfo(name = "time") val timeStamp: Long = 0L,
                           @ColumnInfo(name = "version") var version: String,
                           @ColumnInfo(name = "json") var json: String = "")

@Database(entities = [BottomBarEntity::class], version = 1)
abstract class BottomBarDatabase : RoomDatabase() {
    abstract fun bottomBarDao(): BottomBarDao
}

@Dao
interface BottomBarDao {

    @Query("SELECT * from BOTTOM_BAR_RESPONSE_TABLE")
    fun getResponseData() : List<BottomBarEntity>

    @Query("SELECT * from BOTTOM_BAR_RESPONSE_TABLE")
    fun readResponse(): LiveData<List<BottomBarEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun writeResponse(bottomBarEntity: BottomBarEntity)

    @Update(onConflict = OnConflictStrategy.REPLACE)//as default onConflictStrategy is ABORT
    fun update(vararg bottomBarEntity: BottomBarEntity)
}

// single instance to be used throughout the app
val BottomBarDbInstance by lazy {
    Room.databaseBuilder(CommonUtils.getApplication(),
            BottomBarDatabase::class.java, "bottom_bar.db").build()
}