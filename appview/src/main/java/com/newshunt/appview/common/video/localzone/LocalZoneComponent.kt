/*
 * Copyright (c) 2020 Newshunt. All rights reserved.
 */

package com.newshunt.appview.common.video.localzone

import com.newshunt.appview.common.di.CardsModule
import com.newshunt.appview.common.video.base.BaseVerticalVideoFragment
import com.newshunt.news.di.scopes.PerFragment
import dagger.Component

@PerFragment
@Component(modules = [CardsModule::class, LocalZoneModule::class])
interface LocalZoneComponent {
    fun inject(localZoneFragment: LocalZoneFragment)
}

@PerFragment
@Component(modules = [CardsModule::class, LocalZoneModule::class])
interface LocalVideoComponent {
    fun inject(localVideoFragment: BaseVerticalVideoFragment)
}