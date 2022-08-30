/*
 * Copyright (c) 2020 Newshunt. All rights reserved.
 */

package com.newshunt.news.model.usecase

import com.newshunt.dataentity.common.asset.CardInfo
import com.newshunt.dataentity.common.asset.CardNudge
import com.newshunt.news.model.daos.NudgeDao
import io.reactivex.Observable
import javax.inject.Inject

/**
 * Execute whenever cards-data changes
 * @author satosh.dhanyamraju
 */
class ReadNudgesUsecase @Inject constructor(private val nudgeDao: NudgeDao): Usecase<List<CardInfo>, Map<String, CardNudge?>>{
    override fun invoke(p1: List<CardInfo>): Observable<Map<String, CardNudge?>> {
        return Observable.fromCallable {
            nudgeDao.readCardNudges(p1).toMap().mapKeys { it.key.id }
        }
    }
}