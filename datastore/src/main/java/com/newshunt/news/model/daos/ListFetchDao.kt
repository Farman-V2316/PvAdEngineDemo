/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.news.model.daos

import androidx.paging.DataSource

/**
 * @author raunak.yadav
 */
interface ListFetchDao<T> {
    fun itemsMatching(pageId: String, location: String, section: String): DataSource.Factory<Int, T>
}