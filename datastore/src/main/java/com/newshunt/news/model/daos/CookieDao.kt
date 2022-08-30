package com.newshunt.news.model.daos

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import com.newshunt.dataentity.common.asset.CookieEntity


@Dao
abstract class CookieDao : BaseDao<CookieEntity> {

    @Query("SELECT cookie FROM cookie_table WHERE location = :location")
    abstract fun getLocalCookie(location:String): String?

    @Query("SELECT cookie FROM cookie_table WHERE location ='global'")
    abstract fun getGlobalCookie(): String?

}