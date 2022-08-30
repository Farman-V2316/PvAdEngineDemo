/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */

package com.newshunt.common.helper.listener

import androidx.lifecycle.LifecycleObserver

/**
 * LifecycleObserver + setUserVisiblityHint callbacks
 * To be used by fragments that will be shown in a pager
 * @author satosh.dhanymaraju
 */

interface PagerLifecycleObserver: LifecycleObserver {
    fun setUserVisibleHint(isVisible: Boolean)

    fun updateStory(story : Any?) {}
}
