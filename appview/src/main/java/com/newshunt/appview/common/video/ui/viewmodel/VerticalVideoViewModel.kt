/*
 * Copyright (c) 2020 Newshunt. All rights reserved.
 */
package com.newshunt.appview.common.video.ui.viewmodel

import android.app.Application
import android.os.Bundle
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.newshunt.appview.common.video.ui.helper.RelatedVideoHelper
import com.newshunt.dataentity.common.asset.CommonAsset
import com.newshunt.news.model.usecase.MediatorUsecase
import com.newshunt.news.model.usecase.NLResponseWrapper
import javax.inject.Inject
import javax.inject.Named

class VerticalVideoViewModel(context: Application,
                             private val entityId: String,
                             private val postId: String,
                             private val fpUsecase: MediatorUsecase<Bundle, NLResponseWrapper>,
                             private val relatedVideoHelper: RelatedVideoHelper)
    : AndroidViewModel(context) {

    val TEST_URL = "http://api-news.dailyhunt.in/api/v2/posts/feed/HASHTAG/91581308b67fdfbcd24028a0c513bc37?langCode=en&edition=india&section=tv&feedConfigKey=BUZZ_FY&appLanguage=en"
    val test_url_qa ="http://qa-news.newshunt.com/api/v2/posts/feed/HASHTAG/91581308b67fdfbcd24028a0c513bc37?entityTitle=For+You&langCode=en&edition=india&section=tv&feedConfigKey=BUZZ_FY&appLanguage=en"
    val test = "https://5f2171f0daa42f0016665b24.mockapi.io/related"

    var relatedCardList : LiveData<RelatedVideoHelper.RelatedCards> = relatedVideoHelper.relatedCardsLiveData

    val videoListLiveData = fpUsecase.data()

    fun requestRelatedVideos(relatedUrl : String) {
        if(!relatedUrl?.isNullOrEmpty()) {
            relatedVideoHelper.getRelated(relatedUrl, postId)
        } else {
            relatedVideoHelper.getRelated(TEST_URL, postId)
        }
    }


    fun requestLocalVideos() {

    }

    fun updateNLFCAsset(commonAsset: CommonAsset?, position: Int, landingStoryId: String? = null) {
        relatedVideoHelper.asset = commonAsset?.rootPostEntity()
        relatedVideoHelper.position = position
        relatedVideoHelper.parentId = landingStoryId ?: commonAsset?.i_id()
    }


    class Factory @Inject constructor(private val app: Application,
                                      @Named("entityId") val entityId: String,
                                      @Named("postId") val postId: String,
                                      @Named("fpUsecase")
                                      private val fpUsecase: MediatorUsecase<Bundle, NLResponseWrapper>,
                                      private val relatedVideoHelper: RelatedVideoHelper
    ) : ViewModelProvider
    .AndroidViewModelFactory(app) {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return VerticalVideoViewModel(
                    app,
                    entityId,
                    postId,
                    fpUsecase,
                    relatedVideoHelper
            ) as T
        }
    }

}