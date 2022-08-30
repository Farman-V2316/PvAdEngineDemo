/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.news.model.usecase

import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Transformations
import androidx.paging.EitherList
import com.newshunt.common.helper.common.Constants
import com.newshunt.dataentity.common.model.entity.Extra
import com.newshunt.dataentity.common.model.entity.ExtraListObjType
import com.newshunt.dataentity.model.entity.HISTORY_DATE_PATTERN
import com.newshunt.news.model.daos.FetchDao
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject
import javax.inject.Named

/**
 * Variation of ReadCardsUsecase which takes care of adding dates for user interaction responses
 * <p>
 * Created by srikanth.ramaswamy on 12/04/2019
 */
private val dateFormat = SimpleDateFormat(HISTORY_DATE_PATTERN, Locale.ENGLISH)

class ReadResponseCardsUsecase<T : Any> @Inject constructor(@Named("entityId") private val entityId: String,
                                                            @Named("location") private val location: String,
                                                            @Named("section") private val section: String,
                                                            private val fetchDao: FetchDao) : MediatorUsecase<Bundle, EitherList<Any>> {

    override fun execute(t: Bundle): Boolean {
        val resultTransformation = Transformations.map(fetchDao.itemsMatchingLiveList(entityId, location, section)) {
            val adapterList = ArrayList<Any?>()
            it?.let { responsesList ->
                var runningDate = Constants.EMPTY_STRING
                responsesList.forEach { response ->
                    if (response.i_userInteractionAsset() != null) {
                        val dateString = dateFormat.format(response.i_userInteractionAsset()?.activityTime)
                        if (runningDate != dateString) {
                            adapterList.add(Extra(ExtraListObjType.DATE_SEPARATOR, dateString))
                            runningDate = dateString
                        }
                        adapterList.add(response)
                    }
                }
            }
            adapterList
        }
        _data.addSource(resultTransformation) {
            _data.value = Result0.success(EitherList(simpleList = it))
        }
        return true
    }

    private val _data = MediatorLiveData<Result0<EitherList<Any>>>()

    override fun data(): LiveData<Result0<EitherList<Any>>> {
        return _data
    }
}

