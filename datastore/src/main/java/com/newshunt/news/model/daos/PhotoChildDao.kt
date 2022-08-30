/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.news.model.daos

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import com.newshunt.dataentity.social.entity.PhotoChild
import io.reactivex.Observable

/**
 * @author satosh.dhanyamraju
 */
@Dao
abstract class PhotoChildDao : BaseDao<PhotoChild> {
    @Query("SELECT * FROM photochild WHERE postId = :postId ORDER BY viewOrder ASC")
    abstract fun getPhotosForPost(postId: String): LiveData<List<PhotoChild>>
}