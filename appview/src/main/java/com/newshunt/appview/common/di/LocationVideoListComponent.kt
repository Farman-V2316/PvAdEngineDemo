/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.appview.common.di

import com.newshunt.news.view.activity.LocationVideoListingActivity
import dagger.Component

/**
 * Dagger component for Location Video Listing activity
 *
 * @author Vinod. BC
 *
 */
@Component(modules = [FollowSnackbarModule::class])
interface LocationVideoListComponent {
    fun inject(locationActivity: LocationVideoListingActivity)
}