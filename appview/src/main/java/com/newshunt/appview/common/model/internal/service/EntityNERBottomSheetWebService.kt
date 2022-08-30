/*
 * Copyright (c) 2022 Newshunt. All rights reserved.
 */
package com.newshunt.appview.common.model.internal.service

import com.newshunt.appview.common.model.internal.rest.EntityNERBottomSheetWebAPI
import com.newshunt.dataentity.common.model.EntityNERBottomSheetWebResponse
import io.reactivex.Observable
import javax.inject.Inject
import javax.inject.Named

/**
 * Service and it's implementation class for bottomsheet description webview for NERs and TPV profiles.
 * <p>
 * Created by aman.roy on 05/30/2022.
 */

interface EntityNERBottomSheetWebService {
    fun getNERWebBottomSheetForEntity(url:String) : Observable<EntityNERBottomSheetWebResponse?>
}

class EntityNERBottomSheetWebServiceImpl @Inject constructor(@Named("entityNERBottomSheetWebAPI") val api: EntityNERBottomSheetWebAPI) :EntityNERBottomSheetWebService {
    override fun getNERWebBottomSheetForEntity(url: String): Observable<EntityNERBottomSheetWebResponse?> {
        return api.getNERWebBottomSheetForEntity(url).map { it.data }
    }
}