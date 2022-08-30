/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.adengine.view.helper

import com.newshunt.adengine.ClearAdsDataUsecase
import com.newshunt.adengine.FetchAdSpecUsecase
import com.newshunt.adengine.InsertAdInfoUsecase
import com.newshunt.adengine.model.entity.BaseAdEntity
import com.newshunt.adengine.util.AdLogger
import com.newshunt.news.model.usecase.toMediator2
import javax.inject.Inject
import javax.inject.Named

/**
 * This is a pgi ad helper class to insert ads in db
 *
 * @author Mukesh Yadav
 */
private const val LOG_TAG = "PgiAdHelper"

class PgiAdHelper @Inject constructor(@Named("adDbHelper") private val adDbHelper: AdDBHelper,
                                      insertAdInfoUsecase: InsertAdInfoUsecase,
                                      clearAdsUsecase: ClearAdsDataUsecase,
                                      private val fetchAdSpec: FetchAdSpecUsecase) {

    private val insertAdInfoUsecaseMediator = insertAdInfoUsecase.toMediator2()
    private val removeAdInforUsecaseMediator = clearAdsUsecase.toMediator2()
    val adSpec = fetchAdSpec.data()

    fun tryInsertAd(baseAdEntity: BaseAdEntity?, adIndex: Int) {
        AdLogger.i(LOG_TAG, "insert pgi ad id : ${baseAdEntity?.uniqueAdIdentifier}")
        baseAdEntity ?: return
        insertAdInfoUsecaseMediator.execute(InsertAdInfoUsecase.bundle(baseAdEntity,
                adDbHelper.getItemIdBeforeIndex(adIndex), System.currentTimeMillis()))
    }

    fun fetchAdSpec(entityId: String) {
        fetchAdSpec.execute(listOf(entityId))
    }

    fun destroy() {
        insertAdInfoUsecaseMediator.dispose()
        fetchAdSpec.dispose()
    }

    fun removeAd(baseAdEntity: BaseAdEntity?, reported: Boolean = false) {
        AdLogger.i(LOG_TAG, "removing pgi ad id : ${baseAdEntity?.uniqueAdIdentifier}")
        baseAdEntity ?: return
        removeAdInforUsecaseMediator.execute(ClearAdsDataUsecase.bundle(baseAdEntity.uniqueAdIdentifier, reported))
    }
}