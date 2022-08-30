/*
 * Copyright (c) 2020 Newshunt. All rights reserved.
 */

package com.newshunt.appview.common.video.relatedvideo

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.newshunt.common.helper.common.UrlUtil
import com.newshunt.dataentity.common.pages.FollowSyncEntity
import com.newshunt.dataentity.social.entity.FeedPage
import com.newshunt.dataentity.social.entity.GeneralFeed
import com.newshunt.dhutil.distinctUntilChanged
import com.newshunt.dhutil.helper.preference.UserPreferenceUtil
import com.newshunt.news.model.sqlite.SocialDB
import com.newshunt.news.model.usecase.CurrentPageInfoUsecase
import com.newshunt.news.model.usecase.MediatorUsecase
import com.newshunt.news.model.usecase.onlyData
import com.newshunt.news.util.NewsConstants
import javax.inject.Inject

/**
 * Handles CRUD operations on GeneralFeed Table
 */
class RelatedVideoVM @Inject constructor(app: Application,
                                         private val feedPage: FeedPage,
                                         private val setUpRelatedVideoUsecase: MediatorUsecase<List<GeneralFeed>, List<String>>,
                                         private val readRelatedVideoFeedUsecase: CurrentPageInfoUsecase) : AndroidViewModel(app) {
    init {
        readRelatedVideoFeedUsecase.execute(Unit)
    }
    val curRelatedInfo: LiveData<FeedPage?> = readRelatedVideoFeedUsecase.onlyData()
            .distinctUntilChanged()
    // inserts to generalFeed
    fun start(relatedUrl: String) = execUc(url = relatedUrl)

    private fun execUc(url: String) {
        val curPage = curRelatedInfo.value ?: feedPage
        val gnrlFeed = GeneralFeed(curPage.id, url
                ?: curPage.contentUrl, curPage.contentRequestMethod, curPage.section)
        setUpRelatedVideoUsecase.execute(listOf(gnrlFeed))
    }

    class Factory @Inject constructor(app: Application)
        : ViewModelProvider.AndroidViewModelFactory(app) {
        @Inject
        lateinit var relatedVideoVM: RelatedVideoVM
        override fun <T : ViewModel?> create(modelClass: Class<T>): T = relatedVideoVM as T
    }
}