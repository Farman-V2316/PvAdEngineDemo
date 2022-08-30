/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.eterno

import android.content.ContentResolver
import com.newshunt.common.model.retrofit.RestAdapterContainer
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dhutil.helper.retrofit.NewsBaseUrlContainer
import com.newshunt.news.model.apis.ContactSyncAPI
import com.newshunt.news.model.service.ContactSyncService
import com.newshunt.news.model.service.ContactSyncServiceImpl
import com.newshunt.sdk.network.Priority
import dagger.Component
import dagger.Module
import dagger.Provides
import javax.inject.Named

/**
 * A common Job intent service which can be used to sync stuff from different B.E services
 * <p>
 * Created by srikanth.ramaswamy on 10/17/2019.
 */

@Component(modules = [ContactSyncModule::class])
interface BGSyncServiceComponent {
    fun inject(component: BGSyncService)
}

@Module
class ContactSyncModule(private val bucketSize: Int) {

    @Provides
    fun api(): ContactSyncAPI {
        val contactsBaseUrl = CommonUtils.formatBaseUrlForRetrofit(NewsBaseUrlContainer.getContactSyncBaseUrl())
        return RestAdapterContainer.getInstance()
                .getRestAdapter(contactsBaseUrl, Priority.PRIORITY_NORMAL, null)
                .create(ContactSyncAPI::class.java)
    }

    @Provides
    fun service(service: ContactSyncServiceImpl): ContactSyncService {
        return service
    }

    @Provides
    fun contentResolver(): ContentResolver {
        return CommonUtils.getApplication().contentResolver
    }

    @Provides
    @Named("payloadBucketSize")
    fun payloadBucketSize(): Int {
        return bucketSize
    }
}
