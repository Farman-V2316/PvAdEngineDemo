/*
* Copyright (c) 2020 Newshunt. All rights reserved.
*/
package com.newshunt.adengine.processor

import com.newshunt.adengine.model.AdReadyHandler
import com.newshunt.adengine.model.entity.BaseAdEntity
import com.newshunt.adengine.model.entity.BaseDisplayAdEntity
import com.newshunt.adengine.model.entity.MultipleAdEntity
import com.newshunt.adengine.model.entity.version.AdContentType
import com.newshunt.adengine.model.entity.version.AdRequest
import com.newshunt.adengine.util.AdLogger
import com.newshunt.common.helper.common.Constants
import com.newshunt.dataentity.common.asset.PostEntity
import com.newshunt.news.model.usecase.FetchContentForAdUsecase
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

private const val TAG = "ContentAdProcessor"

/**
 * Processes Ads with content Id (to be fetched from News BE)
 *
 * @author raunak.yadav
 */
class ContentAdProcessor(private val baseAdEntity: BaseAdEntity,
                         private val adReadyHandler: AdReadyHandler) : BaseAdProcessor {

    override fun processAdContent(adRequest: AdRequest?) {
        AdLogger.d(TAG, "Request content for Ad : ${baseAdEntity.uniqueAdIdentifier}")
        val postIds = HashMap<String, BaseDisplayAdEntity>()

        val payload: Any? = when (baseAdEntity) {
            is BaseDisplayAdEntity -> {
                val id = baseAdEntity.content?.id
                if (id.isNullOrBlank()) {
                    onAdFailed("Content Id absent")
                    return
                }
                postIds[id] = baseAdEntity
                baseAdEntity.extras
            }
            is MultipleAdEntity -> {
                val finalAds = baseAdEntity.baseDisplayAdEntities.toMutableList()
                val iterator = finalAds.iterator()
                while (iterator.hasNext()) {
                    val ad = iterator.next()
                    if (ad.content?.id.isNullOrEmpty()) {
                        iterator.remove()
                    } else {
                        postIds[ad.content!!.id!!] = ad
                    }
                }
                baseAdEntity.baseDisplayAdEntities = finalAds
                if (finalAds.isEmpty()) {
                    onAdFailed("Content Id absent")
                    return
                }
                finalAds[0].extras
            }
            else -> null
        }
        //TODO(raunak) : Check if any post is in disliked table and filter it out.

        if (payload == null) {
            onAdFailed("Payload absent in AdResponse")
            return
        }
        fetchContent(postIds, payload)
    }

    private fun fetchContent(postIds: HashMap<String, BaseDisplayAdEntity>, payload: Any) {
        FetchContentForAdUsecase(if (baseAdEntity.type == AdContentType.CONTENT_AD) null else Constants.ADS)
            .invoke(payload)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                handleContent(it, postIds)
                postIds.clear()
            }, {
                onAdFailed("No content served for Ids : ${postIds.keys} \n ${it?.message}")
                postIds.clear()
            })
    }

    private fun handleContent(posts: List<PostEntity>,
                              requestedIds: HashMap<String, BaseDisplayAdEntity>) {
        if (posts.isNullOrEmpty()) {
            onAdFailed("No content served for Ids : ${requestedIds.keys}")
            return
        }
        val asset = posts[0]
        if (baseAdEntity is BaseDisplayAdEntity) {
            if (baseAdEntity.content?.id != asset.i_id()) {
                onAdFailed("Mismatching content served for Id : ${baseAdEntity.content?.id}")
                return
            }
        } else if (baseAdEntity is MultipleAdEntity) {
            if (!asset.i_collectionItems().isNullOrEmpty()) {
                asset.i_collectionItems()?.forEach {
                    requestedIds.remove(it.i_id())
                }
            }
            filterOutEmptyAds(requestedIds, baseAdEntity.baseDisplayAdEntities)
            if (baseAdEntity.baseDisplayAdEntities.isEmpty()) {
                onAdFailed("Mismatching content served for Ids : ${requestedIds.keys}")
                return
            }
        }
        baseAdEntity.contentAsset = asset
        AdLogger.d(TAG, "Content retrieved successfully for Ad : ${baseAdEntity.uniqueAdIdentifier}")
        adReadyHandler.onReady(baseAdEntity)
    }

    private fun onAdFailed(errorMsg: String) {
        AdLogger.e(TAG, "Fail : $errorMsg")
        adReadyHandler.onReady(null)
    }

    private fun filterOutEmptyAds(noFillIds: HashMap<String, BaseDisplayAdEntity>, ads: MutableList<BaseDisplayAdEntity>) {
        val iterator = ads.iterator()
        while (iterator.hasNext()) {
            val ad = iterator.next()
            if (noFillIds.containsKey(ad.content?.id)) {
                iterator.remove()
            }
        }
    }

}