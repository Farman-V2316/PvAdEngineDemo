/*
 * Created by Rahul Ravindran at 12/9/19 10:32 PM
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.appview.common.postcreation.view.listener

import androidx.lifecycle.LifecycleOwner
import com.newshunt.news.view.view.BaseNewsMVPView

interface HashTagAndMentionView : BaseNewsMVPView {
    fun initSuggestions()
    fun lifeCycleOwner(): LifecycleOwner
    fun showSuggestionView(state: Boolean)
}