/*
* Copyright (c) 2020 Newshunt. All rights reserved.
*/
package com.newshunt.adengine.usecase

import com.newshunt.adengine.util.AdLogger
import com.newshunt.dataentity.ads.AdFrequencyCapEntity
import com.newshunt.news.model.daos.AdFrequencyCapDao
import com.newshunt.news.model.usecase.Usecase
import io.reactivex.Observable

/**
 * Persist frequency cap data for ad campaigns
 *
 * @author raunak.yadav
 */
class InsertAdFcDataUsecase constructor(private val adsDao: AdFrequencyCapDao) : Usecase<List<AdFrequencyCapEntity>, Boolean> {

    override fun invoke(p1: List<AdFrequencyCapEntity>): Observable<Boolean> {
        return Observable.fromCallable {
            AdLogger.v(TAG, "Save ad campaign $p1")
            adsDao.insReplace(p1)
            return@fromCallable true
        }
    }
}

/**
 * Fetch previously persisted data for frequency capped campaigns.
 *
 * @author raunak.yadav
 */
class FetchAllAdFcDataUsecase constructor(private val adsDao: AdFrequencyCapDao)
    : Usecase<Unit, List<AdFrequencyCapEntity>> {

    override fun invoke(p1: Unit): Observable<List<AdFrequencyCapEntity>> {
        return Observable.fromCallable {
            AdLogger.v(TAG, "Fetching Frequency Cap data for ads")
            adsDao.fetchAll()
        }
    }
}

/**
 * Remove frequency cap data for expired campaigns.
 *
 * @author raunak.yadav
 */
class RemoveAdFcDataUsecase constructor(private val adsDao: AdFrequencyCapDao) : Usecase<List<AdFrequencyCapEntity>, Boolean> {

    override fun invoke(fcEntities: List<AdFrequencyCapEntity>): Observable<Boolean> {
        return Observable.fromCallable {
            AdLogger.v(TAG, "Deleting FC data for : $fcEntities")
            fcEntities.forEach {
                adsDao.delete(it)
            }
            true
        }
    }
}
private const val TAG = "AdCampaignsSync"