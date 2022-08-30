/*
 * Copyright (c) 2020 Newshunt. All rights reserved.
 */

package com.newshunt.news.model.usecase

import com.newshunt.news.model.daos.NudgeDao
import io.reactivex.Observable
import javax.inject.Inject

/**
 * Execute when tooltip is shown. Needed to update DB to disallow other nudges in the same session
 * @author satosh.dhanyamraju
 */
class MarkNudgeShownUsecase @Inject constructor(private val nudgeDao: NudgeDao): Usecase<Int, Boolean> {

    override fun invoke(p1: Int): Observable<Boolean> {
        return Observable.fromCallable {
            nudgeDao.markShown(p1)
            true
        }
    }
}