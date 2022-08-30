/*
 * Created by Rahul Ravindran at 27/9/19 7:14 PM
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.news.model.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import com.newshunt.dataentity.search.RecentSearchEntity

/**
 * @author shrikant.agarwal
 */
@Dao
interface SearchServiceDao: BaseDao<RecentSearchEntity>{

    @Query("SELECT * from recent_search where search LIKE :searchParam ORDER BY time DESC LIMIT 12")
    fun getSearchItems(searchParam: String) : List<RecentSearchEntity>

    @Query("DELETE FROM recent_search")
    fun deleteAll()

    @Delete
    fun deleteEntity(recentSearchEntity: RecentSearchEntity)
}

