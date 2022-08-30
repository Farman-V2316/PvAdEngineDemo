/*
 * Copyright (c) 2020 Newshunt. All rights reserved.
 */

package com.newshunt.news.model.usecase

import com.newshunt.common.helper.common.Logger
import com.newshunt.dataentity.common.asset.CardNudgeTerminateType
import com.newshunt.news.model.daos.NudgeDao
import io.reactivex.Observable
import javax.inject.Inject

/**
 * Triggered when any action occurs that might terminate a card-nudge.
 *
 * @author satosh.dhanyamraju
 */
class TerminateCardNudgeUsecase @Inject constructor(private val nudgeDao: NudgeDao) : Usecase<CardNudgeTerminateType, Boolean> {
    private val LOG_TAG: String = "TerminateCardNudgeUC"
    override fun invoke(p1: CardNudgeTerminateType): Observable<Boolean> {
        return Observable.fromCallable {
            Logger.d(LOG_TAG, "invoke: $p1")
            val rows = nudgeDao.terminate(p1.name)
            Logger.d(LOG_TAG, "invoke: updated $rows rows")
            true
        }
    }
}