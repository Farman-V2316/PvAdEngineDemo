/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.news.model.usecase

import android.os.Bundle
import com.newshunt.common.helper.common.Constants
import com.newshunt.dataentity.common.asset.Card
import com.newshunt.dataentity.common.asset.Format
import com.newshunt.dataentity.common.asset.PostEntity
import com.newshunt.dataentity.social.entity.FetchDataEntity
import com.newshunt.dataentity.social.entity.FetchInfoEntity
import com.newshunt.news.model.daos.FetchDao
import com.newshunt.news.model.daos.PostDao
import com.newshunt.news.model.sqlite.SocialDB
import io.reactivex.Observable
import javax.inject.Inject

/**
 * Usecase to open articles from other perspective view.
 * Create fetch info for more stories and open the details view.
 *
 * Created by karthik.r on 2020-04-11.
 */
class OpenOtherPerspectiveUsecase@Inject constructor(private val fetchDao: FetchDao,
                                                     private val postDao: PostDao) :
        BundleUsecase<String?> {

    override fun invoke(p1: Bundle): Observable<String?> {
        return Observable.fromCallable {
            var entityId = Constants.EMPTY_STRING
            val postId = p1.getString(Constants.BUNDLE_POST_ID)
            val adId = p1.getString(Constants.BUNDLE_AD_ID)
            val section = p1.getString(Constants.BUNDLE_LOCATION_ID) ?: Constants.EMPTY_STRING
            val useCollection = p1.getBoolean(Constants.BUNDLE_USE_COLLECTION, false)
            val collectionId = p1.getString(Constants.COLLECTION_ID,"")
            if (postId != null) {
                var card: Card? = null
                val cards = SocialDB.instance().postDao().getCardById(postId)
                cards.map { it.postEntity.i_collectionItems()}.firstOrNull()?.filter { it.i_id() == collectionId}?.firstOrNull()?.let {
                    card = (it as? PostEntity)?.toCard2()
                } ?:
                cards.forEach {
                    if (adId == it.i_adId()) {
                        card = it
                        return@forEach
                    }
                }
                card?.let { card ->
                    entityId = postId + System.currentTimeMillis()
                    val location = entityId
                    val fetchInfo = FetchInfoEntity(entityId, location, null, 0, section = section)
                    var index = 0
                    val fetchId = fetchDao.insertFetchInfoAndFetchData(fetchInfo, card, index, postDao)
                    val prefix = if (useCollection && card.i_adId() != null) {
                        card.i_adId().plus(Constants.UNDERSCORE_CHARACTER)
                    } else null
                    val childStories = if (useCollection) card.i_collectionItems() else card.i_moreStories()
                    val fetchDataChildren = ArrayList<FetchDataEntity>()
                    val cardChildren = ArrayList<Card>()
                    childStories?.forEach {
                        val childCard = it.rootPostEntity()?.let { post ->
                            post.toCard2(fetchId.toString(),
                                    useUniqueId = post.getUniqueId(fetchId.toString(), prefix = prefix),
                                    adId = card.i_adId())
                        }
                        if (childCard != null) {
                            index++
                            val childFetchData = FetchDataEntity(fetchId, 0, index,
                                    childCard.uniqueId, childCard.i_format() ?: Format.HTML,
                                    Constants.EMPTY_STRING)
                            fetchDataChildren.add(childFetchData)
                            cardChildren.add(childCard)
                        }
                    }
                    fetchDao.insertCollectionChildren(fetchDataChildren, cardChildren, postDao)
                }
            }

            entityId
        }
    }
}