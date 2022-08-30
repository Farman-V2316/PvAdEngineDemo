/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.appview.common.di

import android.os.Bundle
import com.newshunt.dataentity.common.pages.FollowSyncEntity
import com.newshunt.news.model.sqlite.SocialDB
import com.newshunt.news.model.usecase.GetLatestFollowUsecase
import com.newshunt.news.model.usecase.MediatorUsecase
import dagger.Module
import dagger.Provides
import javax.inject.Named

/**
 * It is unscoped because it might be used with components with different scopes (activity/fragment)
 *
 * @author satosh.dhanyamraju
 */
@Module
class FollowSnackbarModule {

    @Provides
    @Named("getFollowsUc")
    fun followsUc(): MediatorUsecase<Bundle, List<FollowSyncEntity>> {
        return GetLatestFollowUsecase(SocialDB.instance().followEntityDao())
    }

    @Provides
    fun cpDao() = SocialDB.instance().cpDao()

}