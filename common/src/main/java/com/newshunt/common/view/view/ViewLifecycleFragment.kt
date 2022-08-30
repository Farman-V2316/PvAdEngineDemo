/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.common.view.view

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleRegistry
import com.newshunt.common.helper.common.Logger

/**
 * @author shrikant.agrawal
 */
open class ViewLifecycleFragment: Fragment() {

	override fun onHiddenChanged(hidden: Boolean) {
		super.onHiddenChanged(hidden)
		if (view == null) return

		if (viewLifecycleOwner.lifecycle is LifecycleRegistry) {
			if (hidden) {
				(viewLifecycleOwner.lifecycle as LifecycleRegistry).handleLifecycleEvent(Lifecycle.Event.ON_STOP)
			} else {
				(viewLifecycleOwner.lifecycle as LifecycleRegistry).handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
			}
		}
	}

	override fun setUserVisibleHint(isVisibleToUser: Boolean) {
		super.setUserVisibleHint(isVisibleToUser)
		if (view == null) return

		if (viewLifecycleOwner.lifecycle is LifecycleRegistry) {
			if (isVisibleToUser) {
				(viewLifecycleOwner.lifecycle as LifecycleRegistry).handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
			} else {
				(viewLifecycleOwner.lifecycle as LifecycleRegistry).handleLifecycleEvent(Lifecycle.Event.ON_STOP)
			}
		}
	}

	fun afterResume() {
		if (!userVisibleHint || isHidden) {
			(viewLifecycleOwner.lifecycle as LifecycleRegistry).handleLifecycleEvent(Lifecycle.Event.ON_STOP)
		}
	}


}


class FragmentCallback: FragmentManager.FragmentLifecycleCallbacks() {

	override fun onFragmentResumed(fm: FragmentManager, f: Fragment) {
		super.onFragmentResumed(fm, f)
		Logger.d("ViewLifecycleFragment" , "onFragmentResumed")
		if (f is ViewLifecycleFragment) {
			f.afterResume()
		}
	}

}