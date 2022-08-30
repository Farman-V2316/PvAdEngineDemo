/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.news.model.daos

import androidx.room.Insert
import androidx.room.OnConflictStrategy

/**
 * @author satosh.dhanyamraju
 */
interface BaseDao<T> {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insReplace(vararg t: T)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insReplace(t: List<T>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
     fun insIgnore(vararg t: T) : List<Long>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insIgnore(t: List<T>)
}