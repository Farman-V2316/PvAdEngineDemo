/*
 * Copyright (c) 2022 Newshunt. All rights reserved.
 */
package com.newshunt.news.model.usecase

import android.os.Bundle
import com.newshunt.common.helper.common.Logger
import com.newshunt.news.model.daos.FetchDao
import com.newshunt.news.model.repo.CardSeenStatusRepo
import io.reactivex.Observable
import javax.inject.Inject
import javax.inject.Named
/**
 * For executing DB operations on fragment destroy
 *
 * @author satosh.dhanyamraju
 * */
class CFDestroyUsecase @Inject constructor(@Named("entityId") private val entityId: String,
                                           @Named("location") private val location: String,
                                           @Named("section") private val section: String,
                                           private val fetchDao: FetchDao,
                                           private val cssRepo: CardSeenStatusRepo) : Usecase<Bundle, Boolean> {
    private val LOG_TAG = "CFDestroyUsecase"
    override fun invoke(bundle: Bundle): Observable<Boolean> {
        return Observable.fromCallable {
            try {
                val fetchId = fetchDao.fetchInfo(entityId, location, section)?.fetchInfoId
                if (fetchId == null) {
                    Logger.d(LOG_TAG, "no fetch_info - ($entityId, $location,$section)")
                    return@fromCallable false
                }
                val b1 = cssRepo.markDiscardedFromFetchId(fetchId)
                Logger.d(LOG_TAG, "done=$b1 - ($entityId, $location,$section); fetchId=$fetchId")
                true
            } catch (e: Throwable) {
                Logger.e(LOG_TAG, "exception - ($entityId, $location,$section)", e)
                false
            }
        }
    }
}
