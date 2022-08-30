/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.sso.model.service

import com.newshunt.common.model.retrofit.RestAdapterContainer
import com.newshunt.sdk.network.Priority
import com.newshunt.sso.model.internal.rest.FetchUserProfilesAPI
import com.newshunt.sso.model.internal.rest.FetchUserProfilesResponse
import io.reactivex.Observable

/**
 * @author anshul.jain
 */

class FetchUserProfilesServiceImp : FetchUserProfilesService {

    override fun getUserProfiles(baseUrl: String): Observable<FetchUserProfilesResponse> {
        val fetchUserProfiles = RestAdapterContainer.getInstance()
                .getRestAdapter(baseUrl, Priority.PRIORITY_HIGHEST, this)
                .create(FetchUserProfilesAPI::class.java)
        return fetchUserProfiles.getUserProfiles().map { it.data }
    }

}

interface FetchUserProfilesService {

    fun getUserProfiles(baseUrl: String): Observable<FetchUserProfilesResponse>
}