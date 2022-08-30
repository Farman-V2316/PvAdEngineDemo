package com.newshunt.news.model.daos

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import com.newshunt.dataentity.social.entity.AdditionalContents
import com.newshunt.dataentity.social.entity.FetchDataEntity
import com.newshunt.dataentity.social.entity.LocalDelete

/**
 * Created by karthik.r on 2020-02-18.
 */
@Dao
abstract class LocalDeleteDao : BaseDao<LocalDelete> {

    @Query("select * from localdelete")
    abstract fun all(): LiveData<List<LocalDelete>>
}