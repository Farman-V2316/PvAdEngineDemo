/*
 * Copyright (c) 2022 Newshunt. All rights reserved.
 */
package com.newshunt.onboarding.view.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.newshunt.common.view.view.BaseFragment
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dhutil.R
import com.newshunt.dhutil.databinding.DhLoaderFragmentBinding

/**
 * Fragment implementation to show a loader to cover up any ongoing api sync
 * Created by srikanth.r on 05/30/22.
 */
const val DH_LOADER_FRAGMENT = "DHLoaderFragment"
class DHLoaderFragment: BaseFragment() {
    private lateinit var viewBinding: DhLoaderFragmentBinding

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        viewBinding = DataBindingUtil.inflate(inflater, R.layout.dh_loader_fragment, null, false)
        val drawable = CommonUtils.getResourceIdFromAttribute(activity, R.attr.home_loader_anim)
        Glide.with(viewBinding.homeLoaderAnim)
            .load(drawable)
            .into(viewBinding.homeLoaderAnim)
        return viewBinding.root
    }

    companion object {
        fun newInstance(intent: Intent?): DHLoaderFragment {
            return DHLoaderFragment().apply {
                arguments = intent?.extras ?: Bundle()
            }
        }
    }
}