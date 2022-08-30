/*
 * Copyright (c) 2021 Newshunt. All rights reserved.
 */
package com.newshunt.appview.common.model.usecase

import com.newshunt.dataentity.common.model.AdjunctLangResponse
import com.newshunt.news.model.usecase.Usecase
import com.newshunt.onboarding.model.service.AdjunctLangService
import io.reactivex.Observable
/**
 * Adjunct language versioned usecase implementation which fetches static
 * config object from server using AdjunctLangServiceImpl.
 * @author aman.roy
 */
class AdjunctLanguageUsecase(val adjunctLangService:AdjunctLangService) : Usecase<Unit, AdjunctLangResponse> {
    override fun invoke(t:Unit): Observable<AdjunctLangResponse> {
        return adjunctLangService.getStoredAdjunctLang()
    }
}