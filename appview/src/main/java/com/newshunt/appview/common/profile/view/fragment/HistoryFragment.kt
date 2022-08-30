/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.appview.common.profile.view.fragment

import android.content.Context
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.newshunt.appview.R
import com.newshunt.appview.common.profile.DaggerHistoryComponent
import com.newshunt.appview.common.profile.HistoryModule
import com.newshunt.appview.common.profile.helper.analytics.logHistoryListViewEvent
import com.newshunt.appview.common.profile.view.ProfileClearDialog
import com.newshunt.appview.common.profile.view.adapter.HistoryAdapter
import com.newshunt.appview.common.profile.view.interfaces.ProfileFlow
import com.newshunt.appview.common.profile.viewmodel.HistoryViewModel
import com.newshunt.appview.common.profile.viewmodel.HistoryViewModelFactory
import com.newshunt.appview.databinding.HistoryFragmentBinding
import com.newshunt.common.view.customview.CommonMessageDialog
import com.newshunt.common.view.customview.CommonMessageDialogOptions
import com.newshunt.common.view.customview.CommonMessageEvents
import com.newshunt.common.view.view.BaseSupportFragment
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.model.entity.DISABLED_FILTER_ALPHA
import com.newshunt.dataentity.model.entity.ENABLED_FILTER_ALPHA
import com.newshunt.dataentity.model.entity.LoginType
import com.newshunt.dataentity.news.analytics.ProfileReferrer
import com.newshunt.deeplink.navigator.SSONavigator
import com.newshunt.dhutil.commons.listener.ReferrerProviderlistener
import com.newshunt.news.model.sqlite.SocialDB
import com.newshunt.news.view.ListEditInterface
import com.newshunt.profile.FragmentCommunicationsViewModel
import com.newshunt.sso.SSO
import com.newshunt.sso.analytics.SSOReferrer
import kotlinx.android.synthetic.main.list_signin_overlay.view.signinView
import kotlinx.android.synthetic.main.list_signin_overlay.view.signinWidget
import kotlinx.android.synthetic.main.profile_tab_edit_layout.view.clearAll_layout
import kotlinx.android.synthetic.main.profile_tab_edit_layout.view.history_clear_all
import kotlinx.android.synthetic.main.profile_tab_edit_layout.view.history_delete_done
import kotlinx.android.synthetic.main.profile_tab_edit_layout.view.interaction_delete
import javax.inject.Inject

/**
 * A fragment to show list of stories from history
 * <p>
 * Created by srikanth.ramaswamy on 04/17/2019.
 */
private const val DELAY_SIGNIN_PROMPT = 1000L

class HistoryFragment : BaseSupportFragment(), ListEditInterface, View.OnClickListener, View.OnKeyListener {
    private lateinit var historyViewModel: HistoryViewModel
    private lateinit var historyAdapter: HistoryAdapter
    private lateinit var historyLayoutManager: LinearLayoutManager
    private lateinit var viewBinding: HistoryFragmentBinding
    private var historyViewState: HistoryViewState = HistoryViewState.HISTORY_STATE_VIEW
    private var referrerProviderlistener : ReferrerProviderlistener? = null
    private var storyListViewEventFired = false
    private var totalHistoryCount = 0
    private var filteredHistoryCount = 0
    private var isSocialLogin = SSO.getInstance().isLoggedIn(false)

    private val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                if (historyLayoutManager.findLastCompletelyVisibleItemPosition() >= historyAdapter.itemCount - 1) {
                    onEndOfHistory()
                }
            }
        }
    }

    @Inject
    lateinit var historyViewModelF: HistoryViewModelFactory

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        DaggerHistoryComponent
                .builder()
                .historyModule(HistoryModule(SocialDB.instance()))
                .build()
                .inject(this)
        historyViewModel = ViewModelProviders.of(this, historyViewModelF).get(HistoryViewModel::class.java)
        historyViewModel.isSocialLogin = isSocialLogin
        viewBinding = DataBindingUtil.inflate(inflater, R.layout.history_fragment, container, false)

        historyLayoutManager = LinearLayoutManager(activity as Context)
        with(viewBinding.historyList) {
            layoutManager = historyLayoutManager
            historyAdapter = HistoryAdapter(activity as Context,
                    this@HistoryFragment,
                    referrerProviderListener = referrerProviderlistener,
                    clickHandlingViewModel = historyViewModel)
            adapter = historyAdapter
            addOnScrollListener(scrollListener)
        }
        viewBinding.historyEditLayout.interaction_delete.setOnClickListener(this)
        viewBinding.historyEditLayout.history_clear_all.setOnClickListener(this)
        viewBinding.historyEditLayout.history_delete_done.setOnClickListener(this)
        viewBinding.historyHomeBtn.setOnClickListener(this)

        viewBinding.historyContentView.signinWidget.setOnClickListener(this)
        switchToViewMode()
        viewBinding.root.isFocusableInTouchMode = true
        viewBinding.root.requestFocus()
        viewBinding.root.setOnKeyListener(this)
        return viewBinding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        observeHistory()
    }

    override fun onStart() {
        super.onStart()
        queryHistoryIfPossible(userVisibleHint)
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        queryHistoryIfPossible(isVisibleToUser)
    }

    override fun onStop() {
        if (historyViewState == HistoryViewState.HISTORY_STATE_EDIT) {
            discardEdits()
        }
        super.onStop()
    }

    override fun onDestroy() {
        viewBinding.historyList.clearOnScrollListeners()
        viewBinding.historyContentView.removeCallbacks(null)
        super.onDestroy()
    }

    override fun isInEditMode(): Boolean {
        return historyViewState == HistoryViewState.HISTORY_STATE_EDIT
    }

    fun onEndOfHistory() {
        //If user is not logged in: Show the signin view only if total history count > 7 day count shown
        if (!isSocialLogin && totalHistoryCount > filteredHistoryCount) {
            viewBinding.historyContentView.signinView.visibility = View.VISIBLE
        }
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.interaction_delete -> {
                switchToEditMode()
            }
            R.id.history_clear_all -> {
                fragmentManager?.let {
                    val commonMessageDialogOptions = CommonMessageDialogOptions(
                        uniqueScreenId,
                        CommonUtils.getString(R.string.clear_all_history),
                        CommonUtils.getString(R.string.clear_all_msg),
                        CommonUtils.getString(R.string.clear_all),
                        CommonUtils.getString(R.string.cancel_text),
                        drawable = CommonUtils.getDrawable(CommonUtils.getResourceIdFromAttribute(activity,R.attr.profile_dialog_delete_icon)
                    ))

                    ProfileClearDialog.newInstance(commonMessageDialogOptions).show(it, "CommonMessageDialog")
                }
            }
            R.id.history_delete_done -> {
                switchToViewMode()
                historyViewModel.commitEditSession()
            }
            R.id.history_home_btn -> {
                launchNewsHome()
            }
            R.id.signinWidget -> {
                activity?.let {
                    SSONavigator.launchSignInActivity(it, LoginType.NONE, PageReferrer
                    (SSOReferrer.SIGNIN_NUDGE))
                }
            }
        }
    }

    override fun onKey(view: View?, keyCode: Int, keyEvent: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_BACK -> {
                if (historyViewState == HistoryViewState.HISTORY_STATE_EDIT) {
                    discardEdits()
                    return true
                }
            }
        }
        return false
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity?.let {
            ViewModelProviders.of(it).get(FragmentCommunicationsViewModel::class.java)
                    .fragmentCommunicationLiveData.observe(this, Observer {
                if (it.hostId != uniqueScreenId) {
                    return@Observer
                }
                when (it.anyEnum) {
                    is CommonMessageEvents -> {
                        if (it.anyEnum == CommonMessageEvents.POSITIVE_CLICK) {
                            historyViewModel.clearHistory()
                            switchToViewMode()
                        }
                    }
                }
            })
        }
        referrerProviderlistener = context as? ReferrerProviderlistener
    }

    private fun switchToEditMode() {
        historyViewState = HistoryViewState.HISTORY_STATE_EDIT
        viewBinding.historyEditLayout.interaction_delete.isEnabled = false
        viewBinding.historyEditLayout.interaction_delete.alpha = DISABLED_FILTER_ALPHA
        viewBinding.historyEditLayout.clearAll_layout.visibility = View.VISIBLE
        historyAdapter.notifyDataSetChanged()
    }

    private fun switchToViewMode() {
        historyViewState = HistoryViewState.HISTORY_STATE_VIEW
        viewBinding.historyEditLayout.interaction_delete.isEnabled = true
        viewBinding.historyEditLayout.interaction_delete.alpha = ENABLED_FILTER_ALPHA
        viewBinding.historyEditLayout.clearAll_layout.visibility = View.GONE
        historyAdapter.notifyDataSetChanged()
    }


    private fun discardEdits() {
        switchToViewMode()
        historyViewModel.undoEdits()
    }

    private fun launchNewsHome() {
        activity ?: return
        (activity as ProfileFlow).launchNewsHome()
    }

    private fun hideShimmer() {
        viewBinding.root.findViewById<View>(R.id.history_shimmer_container)?.let {
            (viewBinding.root as? ViewGroup?)?.removeView(it)
        }
    }

    private fun queryHistoryIfPossible(isVisibleToUser: Boolean) {
        if (!::historyViewModel.isInitialized) {
            return
        }
        if (isVisibleToUser) {
            historyViewModel.queryHistory()
        }
    }

    private fun observeHistory() {
        historyViewModel.historyLiveData.observe(this, Observer {
            if (it.isSuccess) {
                val list = it.getOrNull()
                list?.also { items ->
                    hideShimmer()
                    historyAdapter.items = items
                    if (!CommonUtils.isEmpty(historyAdapter.items)
                            && historyViewState != HistoryViewState.HISTORY_STATE_EDIT
                            && !storyListViewEventFired) {
                        logHistoryListViewEvent(referrerProviderlistener?.providedReferrer
                                ?: PageReferrer(ProfileReferrer.HISTORY), referrerProviderlistener)
                        storyListViewEventFired = true
                    }
                    if (items.isNotEmpty()) {
                        viewBinding.historyContentView.visibility = View.VISIBLE
                        viewBinding.historyEmptyContainer.visibility = View.GONE
                        viewBinding.historyContentView.postDelayed({
                            if (historyLayoutManager.findLastCompletelyVisibleItemPosition() >= historyAdapter.itemCount - 1) {
                                onEndOfHistory()
                            }
                        }, DELAY_SIGNIN_PROMPT)
                    } else if (historyViewState != HistoryViewState.HISTORY_STATE_EDIT) {
                            viewBinding.historyContentView.visibility = View.GONE
                            viewBinding.historyEmptyContainer.visibility = View.VISIBLE
                        }
                }
            }
        })
        historyViewModel.totalHistoryCountLiveData.observe(this, Observer {
            if (it.isSuccess) {
                totalHistoryCount = it.getOrDefault(0)
            }
        })

        historyViewModel.filteredHistoryCountLiveData.observe(this, Observer {
            if(it.isSuccess) {
                filteredHistoryCount = it.getOrDefault(0)
            }
        })
    }
}

enum class HistoryViewState {
    HISTORY_STATE_VIEW,
    HISTORY_STATE_EDIT
}