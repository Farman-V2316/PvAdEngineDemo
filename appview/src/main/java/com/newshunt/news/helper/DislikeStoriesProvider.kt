/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.news.helper

import androidx.core.util.Pair
import androidx.lifecycle.LiveData
import com.newshunt.news.util.NewsApp
import io.reactivex.Observable

/**
 * @author santhosh.kc
 */
interface DislikeStoriesProvider {

    fun getDislikedItems(): List<Pair<String, String>>?

    fun getDislikedItemsLiveData(): LiveData<List<Pair<String, String>>>?

    fun getDislikedItemsObservable(): Observable<List<Pair<String, String>>>?

}

fun getDislikeStoriesProvider(pageTypeStr: String?): DislikeStoriesProvider {
    return NormalDislikedStoriesProvider()
}

class NormalDislikedStoriesProvider : DislikeStoriesProvider {
    override fun getDislikedItems(): List<Pair<String, String>>? {
        return NewsApp.getNewsAppComponent().dislikeService().allDisliked().map {
            Pair(it.value.itemId, it.value.groupType)
        }
    }

    override fun getDislikedItemsLiveData(): LiveData<List<Pair<String, String>>>? {
        return null
    }

    override fun getDislikedItemsObservable(): Observable<List<Pair<String, String>>>? {
        return dislikedIds()
    }

}