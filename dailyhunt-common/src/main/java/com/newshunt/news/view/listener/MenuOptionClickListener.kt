/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */

package com.newshunt.news.view.listener

import com.newshunt.dataentity.news.view.entity.MenuOpts


/**
 * Implemented by cardsfragment (that which interacts with SimpleCardListView
 * @author amit.chaudhary
 */
interface MenuOptionClickListener {
    fun onFullScreenOptionClick(menuOption: MenuOpts, fragmentId: Int)
    fun onInlineOptionsSubmit(menuOption: MenuOpts?, position: Int, fragmentId: Int)
    fun hideInlineDislikeCard(position: Int, menuOption: MenuOpts, fragmentId: Int)
}


/**
 * Implemented by activities holding CardsFragment
 * @author satosh.dhanyamraju
 */
interface MenuListenerProvider {
    fun menuOptionClickListener(): MenuOptionClickListener?
}