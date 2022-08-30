/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.news.model.daos

import androidx.room.Dao
import androidx.room.Query
import com.newshunt.dataentity.social.entity.RelatedList

/**
 * @author karthik.r
 */
@Dao
abstract class RelatedListDao : BaseDao<RelatedList> {

    @Query("DELETE FROM related_list WHERE postId = :postId")
    abstract fun deleteForPostId(postId: String)
}
