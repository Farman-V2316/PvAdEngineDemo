/*
 * Copyright (c) 2022 Newshunt. All rights reserved.
 */
package com.newshunt.appview.common.di

import android.app.Activity
import androidx.lifecycle.ViewModelProviders
import com.newshunt.appview.common.accounts.model.internal.rest.AccountAPI
import com.newshunt.appview.common.model.internal.rest.EntityNERBottomSheetWebAPI
import com.newshunt.appview.common.model.internal.service.EntityNERBottomSheetWebService
import com.newshunt.appview.common.model.internal.service.EntityNERBottomSheetWebServiceImpl
import com.newshunt.appview.common.model.usecase.EntityNERBottomSheetWebUsecase
import com.newshunt.appview.common.viewmodel.EntityInfoBottomSheetDescViewModel
import com.newshunt.common.model.retrofit.RestAdapterContainer
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.common.model.EntityNERBottomSheetWebResponse
import com.newshunt.dhutil.helper.retrofit.NewsBaseUrlContainer
import com.newshunt.navigation.viewmodel.AboutUsViewModel
import com.newshunt.news.model.usecase.MediatorUsecase
import com.newshunt.news.model.usecase.toMediator2
import com.newshunt.sdk.network.Priority
import com.newshunt.sso.model.helper.interceptor.HTTP401Interceptor
import dagger.Module
import dagger.Provides
import java.util.*
import javax.inject.Named

/**
 * Module class for bottomsheet description webview for NERs and TPV profiles.
 * <p>
 * Created by aman.roy on 05/30/2022.
 */

@Module
class EntityInfoBottomSheetModule {

    @Provides
    @Named("entityNERBottomSheetWebAPI")
    fun getEntityNERBottomSheetWebAPI(): EntityNERBottomSheetWebAPI {
        return RestAdapterContainer.getInstance().getRestAdapter(
            CommonUtils.formatBaseUrlForRetrofit(NewsBaseUrlContainer.getApplicationUrl()),
            Priority.PRIORITY_HIGHEST,
            null
        ).create(EntityNERBottomSheetWebAPI::class.java)
    }

    @Provides
    @Named("entityNERBottomSheetWebService")
    fun getEntityNERBottomSheetService(service: EntityNERBottomSheetWebServiceImpl):EntityNERBottomSheetWebService = service

    @Provides
    @Named("entityNERBottomSheetWebUsecase")
    fun getEntityNERBottomSheetWebUsecase(uc: EntityNERBottomSheetWebUsecase):MediatorUsecase<String,EntityNERBottomSheetWebResponse?> = uc.toMediator2()

}