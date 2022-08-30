/*
 *
 *  * Copyright (c) 2021 Newshunt. All rights reserved.
 *  
 */

package com.newshunt.news.view.helper

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
/**
*
* @author satosh.dhanyamraju
*/
interface PostDetailActionbarInterface {
    fun setActionMoreVisible(isVisible: Boolean)
    fun setActionDisclaimerVisible(b: Boolean)
    fun hideActionMoreView()
    fun updateToolbarVisibility(v: Int)
    fun showWithAnimation()
    fun hideWithAnimation()
    fun initActionBar(parent: View, parentFragment: Fragment?, activity: Activity?, menuClickListener: Toolbar.OnMenuItemClickListener, inflater: LayoutInflater)
    fun isOnTheTop() : Boolean
    fun setTransparentToolbar(alpha:Float)
    fun setToolbar()
}