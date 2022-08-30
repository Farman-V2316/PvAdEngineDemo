/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */

package com.newshunt.news.helper

/**
 * @author amit.chaudhary
 */

import androidx.core.util.Pair
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.news.analytics.NewsReferrer
import com.newshunt.dhutil.helper.preference.AppStatePreference
import com.newshunt.news.util.NewsApp
import io.reactivex.Observable

class MenuHelper {
    companion object {

        @JvmStatic
        fun showDislike(): Boolean {
            val dislikeContentAvailable = PreferenceManager.getPreference(AppStatePreference
                    .DISLIKE_CONTENT_AVAILABLE, false)

            return dislikeContentAvailable
        }

    }
}

fun dislikedIds() : Observable<List<Pair<String,String>>> =
        Observable.fromCallable { NewsApp.getNewsAppComponent().dislikeService().allDisliked() }
                .map {
                    val list: ArrayList<Pair<String, String>> = ArrayList()
                    it.mapTo(list, { entity -> Pair(entity.value.itemId, entity.value.groupType) })
                    list
                }
