package com.newshunt.news.model.usecase

import androidx.paging.DataSource

/**
 * @see androidx.paging.ListDataSource<T>
 * */
class ListDataSourceFactory<T>(private val list: List<T?>) : DataSource.Factory<Int, T?>() {
    override fun create(): DataSource<Int, T?> {
        return ListDataSource(list)
    }
}