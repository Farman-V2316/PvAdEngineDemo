/*
 * Created by Rahul Ravindran at 27/9/19 7:11 PM
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.news.model.sqlite

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.search.RecentSearchEntity
import com.newshunt.dataentity.search.SearchSuggestionType
import com.newshunt.news.model.daos.SearchServiceDao

@Database(entities = [RecentSearchEntity::class],
        version = 1)
@TypeConverters(SearchDBTypeConv::class)
abstract class SearchDatabase : RoomDatabase() {
    abstract fun searchServiceDao(): SearchServiceDao


    companion object {
        fun instance(context: Context = CommonUtils.getApplication()): SearchDatabase{
            return Room.databaseBuilder(context, SearchDatabase::class.java, "search.db")
                    .build()
        }

    }

}

class SearchDBTypeConv {

    @TypeConverter
    fun toSSType(type: String): SearchSuggestionType? = SearchSuggestionType.findSSType(type)

    @TypeConverter
    fun ssTypeString(type: SearchSuggestionType) = type.type
}