/**
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */
package com.newshunt.dhutil.model.sqlite

import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.google.gson.reflect.TypeToken
import com.newshunt.common.helper.common.JsonUtils
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.dhutil.model.entity.version.VersionDbEntity
import com.newshunt.dhutil.model.dao.VersionServiceDao
import java.util.ArrayList

/**
 * @author shrikant.agrawal
 * Database class for the version db
 */
@Database(entities = arrayOf(VersionDbEntity::class), version = 1)
abstract class VersionDatabase : RoomDatabase() {
    abstract fun versionServiceDao(): VersionServiceDao
}

// single instance to be used throughout the app
val versionDbInstance by lazy {
    Room.databaseBuilder(CommonUtils.getApplication(),
            VersionDatabase::class.java, "version.db")
            .addCallback(RoomCallback())
            .build()
}

class RoomCallback : RoomDatabase.Callback() {

    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        CommonUtils.runInBackground {loadPreloadedData()}
    }

    fun loadPreloadedData() {
        val data = CommonUtils.readFromAsset("preload.txt")
        if (data == null) return

        val type = object : TypeToken<ArrayList<VersionDbEntity>>() {}.getType()
        val versionDbEntityList = JsonUtils.fromJson<ArrayList<VersionDbEntity>>(data,type)
        if (CommonUtils.isEmpty(versionDbEntityList)) return
        versionDbEntityList?.forEach{
            versionDbInstance.versionServiceDao().insertVersionEntity(it)
        }
    }

}