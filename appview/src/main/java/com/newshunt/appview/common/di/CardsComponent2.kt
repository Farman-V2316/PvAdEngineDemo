/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.appview.common.di

import com.newshunt.appview.common.CardsFragment
import com.newshunt.news.di.scopes.PerFragment
import dagger.Component

/**
 * @author satosh.dhanyamraju
 */
@PerFragment
@Component(modules = [CardsModule::class])
interface CardsComponent2 {
    fun inject(cardsComponent: CardsFragment)
}