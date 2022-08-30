/*
 * Copyright (c) 2022 Newshunt. All rights reserved.
 */
package com.newshunt.appview.common.model.usecase

import com.newshunt.appview.common.model.internal.service.EntityNERBottomSheetWebService
import com.newshunt.dataentity.common.model.EntityNERBottomSheetWebResponse
import com.newshunt.news.model.usecase.Usecase
import io.reactivex.Observable
import javax.inject.Inject
import javax.inject.Named

/**
 * Usecase for bottomsheet description webview for NERs and TPV profiles.
 * <p>
 * Created by aman.roy on 05/30/2022.
 */

class EntityNERBottomSheetWebUsecase @Inject constructor(@Named("entityNERBottomSheetWebService")
                                                         val nerBSService: EntityNERBottomSheetWebService) : Usecase<String, EntityNERBottomSheetWebResponse?> {
    override fun invoke(p1: String): Observable<EntityNERBottomSheetWebResponse?> {
       return nerBSService.getNERWebBottomSheetForEntity(p1)
    }
}