/*
 * Copyright (c) 2022 Newshunt. All rights reserved.
 */
package com.newshunt.appview.common.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.core.app.SharedElementCallback
import androidx.fragment.app.Fragment
import com.google.android.material.transition.MaterialContainerTransform
import com.newshunt.appview.R
import com.newshunt.appview.common.ui.helper.NavigationHelper
import com.newshunt.common.helper.common.Logger
import com.newshunt.dhutil.helper.theme.ThemeUtils

/**
 * Helper delegate class to implement fragment transition from list to detail scenarios
 * <p>
 * Created by srikanth.ramaswamy on 07/28/2022
 */
class TransitionParentDelegate(private val hostFragment: Fragment,
                               private val fragmentName: String): TransitionParent {

    override fun prepareSharedElementTransition(animatedView: View) {
        hostFragment.sharedElementEnterTransition = MaterialContainerTransform().apply {
            scrimColor = ThemeUtils.getThemeColorByAttribute(hostFragment.context, R.attr.default_background)
        }
        hostFragment.setEnterSharedElementCallback(
            object : SharedElementCallback() {
                override fun onMapSharedElements(names: List<String>, sharedElements: MutableMap<String, View>) {
                    if (names.isEmpty()) return
                    Logger.d(NavigationHelper.FRAGMENT_TRANSITION_TAG, "$fragmentName Adding Child fragment view to animation name: ${names[0]}")
                    sharedElements[names[0]] = animatedView
                }
            })
    }

    override fun postponeEnterTransition(savedInstanceState: Bundle?, arguments: Bundle?) {
        if (savedInstanceState == null && arguments?.getBoolean(NavigationHelper.FRAGMENT_TRANSITION_NEEDED, false) == true) {
            Logger.d(NavigationHelper.FRAGMENT_TRANSITION_TAG, "$fragmentName Postponing enter transition")
            hostFragment.postponeEnterTransition()
        }
    }

    override fun onPageSwipe(currentItemId: String, landingStoryId: String) {
        if (currentItemId != landingStoryId && hostFragment.sharedElementEnterTransition != null && hostFragment.sharedElementReturnTransition != null) {
            Logger.d(NavigationHelper.FRAGMENT_TRANSITION_TAG, "$fragmentName landingStoryId: $landingStoryId, id of current item $currentItemId, disable return transition")
            //Disable the return transition here so the fragment transaction's pop animation kicks in.
            hostFragment.sharedElementReturnTransition = null
        }
    }
}