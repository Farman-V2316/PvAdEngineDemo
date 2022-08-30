/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.dhutil.model.internal.rest

import com.newshunt.dataentity.common.model.entity.model.ApiResponse
import com.newshunt.dataentity.model.entity.GroupInviteConfig
import io.reactivex.Observable
import retrofit2.http.GET

/**
 * Versioned API implementation for fetching the invitation config
 * <p>
 * Created by srikanth.ramaswamy on 09/27/2019.
 */
interface InviteConfigAPI {
    @GET("api/v1/upgrade/dynamic/version?entity=INVITATION_CONFIG")
    fun getEventConfig() : Observable<ApiResponse<GroupInviteConfig>>
}