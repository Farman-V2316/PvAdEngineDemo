/*
 * Copyright (c) 2020 Newshunt. All rights reserved.
 */

package com.newshunt.appview.common.video.localzone

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.newshunt.common.helper.appconfig.AppConfig
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.UrlUtil
import com.newshunt.common.helper.preference.PreferenceManager
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
class LocalZoneVM @Inject constructor(app: Application,
                                      private val feedPage: FeedPage,
                                      private val setUpLocalZoneUsecase: MediatorUsecase<List<GeneralFeed>, List<String>>,
                                      private val readLocalZoneGeneralFeedUsecase: CurrentPageInfoUsecase) : AndroidViewModel(app) {
    init {
        readLocalZoneGeneralFeedUsecase.execute(Unit)
    }
    val curLocalZoneInfo: LiveData<FeedPage?> = readLocalZoneGeneralFeedUsecase.onlyData().distinctUntilChanged()
    // inserts to generalFeed
    fun start(location: String) = execUc(location = location)

    // inserts to generalFeed
    fun changeLocation(location: String) {
        return execUc(buildUrlDefaultParams(feedPage.contentUrl), location)
    }

    private fun execUc(url: String? = null, location: String? = null) {
        PreferenceManager.saveString(NewsConstants.LOCAL_SELECTED_LOCATION_KEY, location)
        val curPage = curLocalZoneInfo.value ?: feedPage
        val feedUrl = url ?: AppConfig.getInstance().localZoneUrl
        val gnrlFeed = GeneralFeed(curPage.id, buildUrlDefaultParams(feedUrl)
                ?: curPage.contentUrl, curPage.contentRequestMethod, curPage.section)
        setUpLocalZoneUsecase.execute(listOf(gnrlFeed))
    }

    private fun buildUrlDefaultParams(url: String): String {
        return UrlUtil.getUrlWithQueryParamns(url,
                hashMapOf(NewsConstants.ENTITY_TITLE to "Local+Zone",
                        NewsConstants.EDITION_CODE to UserPreferenceUtil.getUserEdition()));
    }

    fun getLatestFollowedLocationData(): LiveData<List<FollowSyncEntity>> {
        return SocialDB.instance().followEntityDao().getLatestFollowedLocation()
    }

    fun getFollowedLocationsFIFOData(): LiveData<List<FollowSyncEntity>> {
        return SocialDB.instance().followEntityDao().getFollowedLocationsFIFO()
    }

    class Factory @Inject constructor(app: Application)
        : ViewModelProvider.AndroidViewModelFactory(app) {
        @Inject
        lateinit var localZoneVM: LocalZoneVM
        override fun <T : ViewModel?> create(modelClass: Class<T>): T = localZoneVM as T
    }
}