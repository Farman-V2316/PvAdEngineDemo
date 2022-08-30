/*
* Copyright (c) 2019 Newshunt. All rights reserved.
*/
package com.newshunt.dataentity.dhutil.model

import com.newshunt.dataentity.common.model.entity.model.ApiResponse
import com.newshunt.dataentity.model.entity.ApprovalTabsInfo
import io.reactivex.Observable
import retrofit2.http.GET

/**
 * @author raunak.yadav
 */
interface ApprovalTabsAPI {

    @GET("api/v2/upgrade/dynamic/version?entity=GROUP_APPROVAL")
    fun getApprovalTabs(): Observable<ApiResponse<ApprovalTabsInfo>>

}