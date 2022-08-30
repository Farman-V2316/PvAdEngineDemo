/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.appview.common.di

import com.newshunt.appview.common.ui.fragment.MenuFragment
import com.newshunt.news.model.usecase.FetchAndInsertMenuOptionsUsecase
import dagger.Component

/**
 * @author amit.chaudhary
 */
@Component(modules = [MenuModule2::class])
interface MenuComponent2 {
    fun inject(menuFragment: MenuFragment)

    fun fetchAndInsertUsecase() : FetchAndInsertMenuOptionsUsecase
}