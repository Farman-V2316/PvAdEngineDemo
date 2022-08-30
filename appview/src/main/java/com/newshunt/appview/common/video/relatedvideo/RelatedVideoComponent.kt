/*
 * Copyright (c) 2020 Newshunt. All rights reserved.
 */

package com.newshunt.appview.common.video.relatedvideo

import com.newshunt.appview.common.di.CardsModule
import com.newshunt.appview.common.video.base.BaseVerticalVideoFragment
import com.newshunt.news.di.scopes.PerFragment
import dagger.Component

@PerFragment
@Component(modules = [CardsModule::class, RelatedVideoModule::class])
interface RelatedVideoComponent {
    fun inject(relatedVideoFragment: RelatedVideoFragment)
}

@PerFragment
@Component(modules = [CardsModule::class, RelatedVideoModule::class])
interface LocalVideoComponent {
    fun inject(localVideoFragment: BaseVerticalVideoFragment)
}