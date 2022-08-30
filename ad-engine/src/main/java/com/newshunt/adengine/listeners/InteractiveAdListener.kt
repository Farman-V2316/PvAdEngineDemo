/*
* Copyright (c) 2019 Newshunt. All rights reserved.
*/
package com.newshunt.adengine.listeners

/**
 * For listening events from Interactive ads.
 * Parent view might need to know that it is covered
 * or not.
 *
 * @author raunak.yadav
 */
interface InteractiveAdListener {

    fun onInteractiveAdExpanded()

    fun onInteractiveAdCollapsed()
}