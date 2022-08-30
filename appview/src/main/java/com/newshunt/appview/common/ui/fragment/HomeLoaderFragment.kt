/*
 * Copyright (c) 2022 Newshunt. All rights reserved.
 */
package com.newshunt.appview.common.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import com.newshunt.appview.R
import com.newshunt.appview.common.ui.activity.FRAGMENT_TAG_HOME
import com.newshunt.appview.common.viewmodel.HomeViewModel
import com.newshunt.appview.common.viewmodel.HomeViewModelFactory
import com.newshunt.appview.databinding.HomeLoaderFragmentBinding
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.common.view.view.BaseFragment
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dhutil.helper.appsection.DefaultAppSectionsProvider
import com.newshunt.dhutil.helper.preference.AppStatePreference

/**
 * Fragment implementation to show a loader to cover up any ongoing api sync
 * Created by srikanth.r on 05/24/22.
 */
private const val MSG_DISMISS_LOADER_MIN_WAIT = 877
private const val MSG_DISMISS_LOADER_MAX_WAIT = 878
private const val LOG_TAG = "HomeLoaderFragment"
private const val BUNDLE_MAX_WAIT_MS = "bundle_max_wait_ms"
private const val BUNDLE_MIN_WAIT_MS = "bundle_min_wait_ms"
class HomeLoaderFragment: BaseFragment() {
    private lateinit var viewBinding: HomeLoaderFragmentBinding
    private var pageSyncResponseReceived = false

    private val handler = object: Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                MSG_DISMISS_LOADER_MAX_WAIT -> {
                    Logger.d(LOG_TAG, "Dismissing after waiting max time...")
                    dismiss()
                }
                MSG_DISMISS_LOADER_MIN_WAIT -> {
                    if (pageSyncResponseReceived) {
                        Logger.d(LOG_TAG, "Dismissing after min wait time, page sync already done")
                        dismiss()
                    } else {
                        Logger.d(LOG_TAG, "Min wait time elapsed but still waiting for page sync")
                    }
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        (activity?.supportFragmentManager?.findFragmentByTag(FRAGMENT_TAG_HOME) as? HomeFragment)?.let { homeFragment ->
            val currentSectionId = arguments?.getString(Constants.APP_SECTION_ID, DefaultAppSectionsProvider.DefaultAppSection.NEWS_SECTION.id) ?: DefaultAppSectionsProvider.DefaultAppSection.NEWS_SECTION.id
            val homeViewModel = ViewModelProviders.of(homeFragment, HomeViewModelFactory(section = currentSectionId)).get(HomeViewModel::class.java)
            homeViewModel.nwLiveData.observe(viewLifecycleOwner) {
                if (it.isSuccess) {
                    val data = it.getOrNull()
                    if (data != null) {
                        pageSyncResponseReceived = true
                        //If min wait time has elapsed, dismiss now, don't wait for max wait time
                        if (!handler.hasMessages(MSG_DISMISS_LOADER_MIN_WAIT)) {
                            Logger.d(LOG_TAG, "Page sync done, dismiss after min wait time")
                            dismiss()
                        } else {
                            Logger.d(LOG_TAG, "pageSyncResponseReceived, letting min wait message dismiss the loader")
                        }
                    }
                }
            }
        }
        viewBinding = DataBindingUtil.inflate(inflater, R.layout.home_loader_fragment, null, false)
        PreferenceManager.savePreference(AppStatePreference.HOME_LOADER_SHOWN, true)
        val minWaitTime = arguments?.getLong(BUNDLE_MIN_WAIT_MS) ?: Constants.HOME_LOADER_MIN_WAIT_TIME_MS
        val maxWaitTime = arguments?.getLong(BUNDLE_MAX_WAIT_MS) ?: Constants.HOME_LOADER_MAX_WAIT_TIME_MS
        Logger.d(LOG_TAG, "minWaitTime: $minWaitTime, maxWaitTime: $maxWaitTime")
        handler.sendMessageDelayed(Message.obtain(handler, MSG_DISMISS_LOADER_MAX_WAIT), maxWaitTime)
        handler.sendMessageDelayed(Message.obtain(handler, MSG_DISMISS_LOADER_MIN_WAIT), minWaitTime)

        val drawable = CommonUtils.getResourceIdFromAttribute(activity, R.attr.home_loader_anim)
        Glide.with(viewBinding.homeLoaderAnim)
            .load(drawable)
            .into(viewBinding.homeLoaderAnim)
        return viewBinding.root
    }

    override fun handleBackPress() = true

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }

    private fun dismiss() {
        handler.removeCallbacksAndMessages(null)
        activity?.supportFragmentManager?.popBackStack()
    }

    companion object {
        fun newInstance(intent: Intent): HomeLoaderFragment {
            return HomeLoaderFragment().apply {
                arguments = intent.extras ?: Bundle()
                arguments?.putLong(BUNDLE_MAX_WAIT_MS, PreferenceManager.getPreference(AppStatePreference.HOME_LOADER_MAX_WAIT_MS, Constants.HOME_LOADER_MAX_WAIT_TIME_MS))
                arguments?.putLong(BUNDLE_MIN_WAIT_MS, PreferenceManager.getPreference(AppStatePreference.HOME_LOADER_MIN_WAIT_MS, Constants.HOME_LOADER_MIN_WAIT_TIME_MS))
            }
        }
    }
}