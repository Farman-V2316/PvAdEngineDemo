/*
* Copyright (c) 2019 Newshunt. All rights reserved.
*/
package com.newshunt.adengine.view.helper

import androidx.lifecycle.LifecycleOwner
import com.newshunt.adengine.FetchAdSpecUsecase
import com.newshunt.adengine.instream.IAdCacheManager
import com.newshunt.adengine.util.AdLogger
import com.newshunt.dataentity.common.asset.CommonAsset
import com.newshunt.dataentity.common.pages.PageEntity
import com.newshunt.dataentity.social.entity.AdSpec
import com.newshunt.dhutil.analytics.originalMessage
import com.newshunt.news.model.usecase.MediatorUsecase
import javax.inject.Inject
import javax.inject.Named

typealias AdspecAndSourceId = Pair<AdSpec?, String?>

/**
 * Helper to manage fetch parentAdSpec
 *
 * @author vinod
 */
class FetchAdsSpec(private val postList: List<AdspecAndSourceId>?,
                   private val parentEntity: PageEntity?,
                   lifecycleOwner: LifecycleOwner,
                   private val fetchAdSpecUsecase: MediatorUsecase<List<String?>, Map<String, AdSpec>>) {

    class Factory @Inject constructor(@Named("pageEntity") private val pageEntity: PageEntity? = null,
                                      private val lifecycleOwner: LifecycleOwner,
                                      private val fetchAdSpecUsecase: FetchAdSpecUsecase) {

        fun fetchParentContext() {
            FetchAdsSpec(null, pageEntity, lifecycleOwner, fetchAdSpecUsecase)
        }

        fun fetch(postList: List<CommonAsset>) {
            FetchAdsSpec(postList.map { it.i_adSpec() to it.i_source()?.id }, pageEntity,
                    lifecycleOwner,
                    fetchAdSpecUsecase)
        }

        fun fetch(post: CommonAsset) {
            FetchAdsSpec(List(1) {
                post.i_adSpec() to post.i_source()?.id
            }, pageEntity, lifecycleOwner, fetchAdSpecUsecase)
        }
    }

    private val parentAdSpecLiveData = fetchAdSpecUsecase.data()

    init {
        val adSpecIds = mutableListOf<String?>()
        adSpecIds.add(parentEntity?.id)

        postList?.forEach { post ->
            // If post adspec is missing, get it from source/cat if present in handshake response.
            if (post.first == null) {
                AdLogger.e(TAG, "Post Video AdSpec not present. Fallback to source")
                adSpecIds.add(post.second)
            }
        }

        parentAdSpecLiveData.observe(lifecycleOwner, androidx.lifecycle.Observer {
            if (it.isFailure) {
                AdLogger.e(TAG, "${parentEntity?.id} AdSpec fetch failed : ${it.exceptionOrNull()?.originalMessage()}")
            } else {
                it.getOrNull()?.let { adSpecs ->
                    val parentAdSpec = adSpecs[parentEntity?.id]
                    AdLogger.v(TAG, "${parentEntity?.id} > Received parent AdSpec $parentAdSpec")
                    IAdCacheManager.addToAdSpecList(adSpecs)
                }
            }
        })

        fetchAdSpecUsecase.execute(adSpecIds)
    }

}

private const val TAG = "FetchAdsSpec"

