/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.news.model.usecase

import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.paging.EitherList
import androidx.paging.PagedList
import androidx.paging.toLiveData
import com.newshunt.common.helper.common.Logger
import com.newshunt.dataentity.common.asset.Format
import com.newshunt.dataentity.common.asset.SavedCard
import com.newshunt.news.model.daos.FetchDao
import com.newshunt.news.model.utils.InvalidCardsLogger
import javax.inject.Inject
import javax.inject.Named

/**
 * Variation of ReadCardsUsecase which limits the number of items in its output list to a
 * configurable value. We also check collection items and limit them also to the same
 * configurable value.
 * <p>
 * Created by srikanth.ramaswamy on 12/04/2019
 */
private const val LOG_TAG = "ReadLimitedCardsUsecase"

class ReadLimitedCardsUsecase @Inject constructor(@Named("entityId") private val entityId: String,
                                                  @Named("location") private val location: String,
                                                  @Named("section") private val section: String,
                                                  private val listDao: FetchDao,
                                                  @Named("cardsLimit")
                                                  private val cardsLimit: Int = Integer.MAX_VALUE,
                                                  private val invalidCardsLogger: InvalidCardsLogger)
    : MediatorUsecase<Bundle, EitherList<Any>> {

    private val _data = MediatorLiveData<Result0<EitherList<Any>>>()
    private lateinit var cardsFromServer: EitherList<Any>

    override fun execute(t: Bundle): Boolean {
        _data.addSource(listDao.readBookmarkedItems(entityId, location, section, cardsLimit).map { savedCard: SavedCard ->
            if (ReadCardsUsecase.filterCards(savedCard, invalidCardsLogger)) {
                if (savedCard.i_format() == Format.POST_COLLECTION) {
                    val colAsset = savedCard.rootPostEntity()?.collectionAsset
                    val limitedItems = colAsset?.collectionItem?.take(cardsLimit)
                    Logger.d(LOG_TAG, "Original Collection size: ${colAsset?.collectionItem?.size}," + " after applying $cardsLimit, size = ${limitedItems?.size}")
                    colAsset?.collectionItem = limitedItems
                }
                savedCard
            } else {
                null
            }
        }.toLiveData(readCardPageConfig())) { cards: PagedList<SavedCard?> ->
            cardsFromServer =  EitherList(pagedList = cards as PagedList<Any?>)
            sendEvent()
        }
        return true
    }

    private fun sendEvent() {
        _data.value = Result0.success(cardsFromServer)
    }

    override fun data(): LiveData<Result0<EitherList<Any>>> {
        return _data
    }

    data class FormatWithCount(val format: String, val count : Int)
}