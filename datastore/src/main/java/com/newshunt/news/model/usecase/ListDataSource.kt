package com.newshunt.news.model.usecase

import androidx.paging.PositionalDataSource

/**
 * @see androidx.paging.ListDataSource<T>
 * */
class ListDataSource<T>(list: List<T?>) : PositionalDataSource<T?>() {
    private val mList: List<T?>

    init {
        mList = java.util.ArrayList(list)
    }

    override fun loadInitial(params: LoadInitialParams,
                             callback: LoadInitialCallback<T?>) {
        val totalCount = mList.size

        val position = computeInitialLoadPosition(params, totalCount)
        val loadSize = computeInitialLoadSize(params, position, totalCount)

        val sublist = mList.subList(position, position + loadSize)
        callback.onResult(sublist, position, totalCount)
    }

    override fun loadRange(params: LoadRangeParams,
                           callback: LoadRangeCallback<T?>) {
        callback.onResult(mList.subList(params.startPosition,
                params.startPosition + params.loadSize))
    }
}