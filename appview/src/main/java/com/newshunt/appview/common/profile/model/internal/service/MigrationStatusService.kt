/*
 * Copyright (c) 2020 Newshunt. All rights reserved.
 */
package com.newshunt.appview.common.profile.model.internal.service

import com.newshunt.appview.common.profile.model.internal.rest.MigrationStatusAPI
import com.newshunt.common.helper.common.ApiResponseUtils
import com.newshunt.dataentity.model.entity.UserMigrationStatusResponse
import io.reactivex.Observable
import javax.inject.Inject

/**
 * Service implementation for Migration status API
 *
 * Created by srikanth.ramaswamy on 01/02/2020
 */
interface MigrationStatusService {
    fun checkMigrationState(): Observable<UserMigrationStatusResponse>
}

class MigrationStatusServiceImpl @Inject constructor(private val migrationStatusAPI: MigrationStatusAPI): MigrationStatusService {
    override fun checkMigrationState(): Observable<UserMigrationStatusResponse> {
        return migrationStatusAPI.checkMigrationState()
                .map {
                    ApiResponseUtils.throwErrorIfDataNull(it)
                    it.data
                }
    }
}
