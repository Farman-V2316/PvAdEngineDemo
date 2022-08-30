/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.news.model.usecase

import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.paging.Config
import androidx.paging.EitherList
import androidx.paging.PagedList
import androidx.paging.toLiveData
import com.newshunt.common.helper.common.AndroidUtils
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger
import com.newshunt.dataentity.common.asset.CommonAsset
import com.newshunt.dataentity.common.asset.Format
import com.newshunt.dataentity.common.asset.SubFormat
import com.newshunt.dataentity.common.asset.UiType2
import com.newshunt.news.model.daos.ListFetchDao
import com.newshunt.news.model.utils.InvalidCardsLogger
import java.util.concurrent.Executor
import javax.inject.Inject
import javax.inject.Named

/**
 * @author satosh.dhanyamraju
 */
class ReadCardsUsecase<T : Any>
@Inject constructor(@Named("entityId") private val entityId: String,
                    @Named("location") private val location: String,
                    @Named("section") private val section: String,
                    private val listDao: ListFetchDao<T>,
                    private val invalidCardsLogger: InvalidCardsLogger) :
        MediatorUsecase<Bundle, EitherList<Any>> {

    private lateinit var prevList: EitherList<Any>
    private val _data = MediatorLiveData<Result0<EitherList<Any>>>()
    private lateinit var cardsFromServer: EitherList<Any>

    override fun execute(t: Bundle): Boolean {
        _data.addSource(listDao.itemsMatching(entityId, location, section).map {
            if (filterCards(it, invalidCardsLogger)) {
                it
            } else {
                null
            }
        }.toLiveData(readCardPageConfig())) { cards: PagedList<T?> ->
            cardsFromServer = EitherList(pagedList = cards as PagedList<Any?>)
            sendEvent()
        }
        return true
    }

    private fun sendEvent() {

        val serverCards = if (::cardsFromServer.isInitialized) cardsFromServer else return

        val combinedList = serverCards
        if (!::prevList.isInitialized || prevList != combinedList) {  // distinct until changed
            _data.value = Result0.success(combinedList)
        }
        prevList = combinedList
    }

    override fun data(): LiveData<Result0<EitherList<Any>>> = _data

    companion object {
        private const val TAG = "ReadCardsUsecase"
        fun filterCards(item: Any, invalidCardsLogger: InvalidCardsLogger): Boolean {
            if (item is CommonAsset) {
                if (item.i_format() == Format.COLLECTION && item.i_subFormat() == SubFormat.ENTITY) {
                    val allow = !item.i_entityCollection().isNullOrEmpty()
                    if (!allow) Logger.e(TAG, "filterCards: Rejecting ${item.i_id()} - empty children")
                    return allow
                }
                if (item.i_format() == Format.POST_COLLECTION && item.i_uiType() != UiType2.CAROUSEL_6) {
                    val allow = !item.i_collectionItems().isNullOrEmpty()
                    if(!allow)
                        Logger.e(TAG, "filterCards: Rejecting ${item.i_id()} - empty children")
                    return allow
                }
                if(item.i_id() == Constants.INVALID_POSTENTITY_ID) {
                    Logger.e(TAG, "filterCards: Rejecting 1 card - deserialization failed")
                    if(AndroidUtils.devEventsEnabled())
                        invalidCardsLogger.log("deserialization failed", "card filtered.")
                    return false
                }
            }
            return true
        }
    }
}


fun readCardPageConfig(): PagedList.Config {
    return Config(pageSize = 20,
            prefetchDistance = 10,
            maxSize = 80,
            enablePlaceholders = true)
}

fun <T> getPagedListFromList(list: List<T>): PagedList<T?> {
    val pagedListBuilder = PagedList.Builder(ListDataSource(list), readCardPageConfig())
    val executor = Executor {
        it.run()
    }
    pagedListBuilder.setFetchExecutor(executor)
    pagedListBuilder.setNotifyExecutor(executor)
    return pagedListBuilder.build()
}