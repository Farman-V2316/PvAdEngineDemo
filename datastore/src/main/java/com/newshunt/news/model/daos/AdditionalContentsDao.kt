/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.news.model.daos

import androidx.room.Dao
import androidx.room.Query
import com.newshunt.dataentity.social.entity.AdditionalContents
import io.reactivex.Observable

/**
 * @author Karthik.r
 */
@Dao
abstract class AdditionalContentsDao : BaseDao<AdditionalContents>