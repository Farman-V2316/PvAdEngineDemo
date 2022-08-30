/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.appview.common.di

import com.newshunt.appview.common.ui.activity.HomeActivity
import dagger.Component

/**
 * Dagger component for home activity
 *
 * @author satosh.dhanyamraju
 *
 */
@Component(modules = [FollowSnackbarModule::class])
interface HomeComponent {
    fun inject(home: HomeActivity)
}