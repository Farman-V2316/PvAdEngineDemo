/*
 * Copyright (c) 2020 Newshunt. All rights reserved.
 */
package com.newshunt.appview.common.accounts.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.newshunt.appview.R
import com.newshunt.appview.common.accounts.AccountsAnalyticsHelper
import com.newshunt.appview.databinding.AccountLinkTypesBinding
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.view.view.BaseSupportFragment
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.model.entity.AuthType
import com.newshunt.dataentity.model.entity.LoginType
import com.newshunt.dataentity.sso.model.entity.AccountLinkType
import java.io.Serializable

/**
 * A simple fragment showing available account linking options.
 * Handles selection of one of the types and a cancel action
 *
 * @author srikanth on 06/11/2020
 */
private const val LOG_TAG = "AccountTypesFragment"
class AccountTypesFragment : BaseSupportFragment(), View.OnClickListener {
    private val availableLinkTypes = mutableListOf(LoginType.GOOGLE,
            LoginType.MOBILE,
            LoginType.FACEBOOK)
    private lateinit var viewBinding: AccountLinkTypesBinding
    private var referrer: PageReferrer? = null

    override fun onCreate(savedState: Bundle?) {
        super.onCreate(savedState)
        arguments?.let { bundle ->
            //Filter out already linked from the available list. So only unlinked ones show up
            (bundle.getSerializable(Constants.BUNDLE_LINKED_ACCOUNT_TYPES) as? List<*>?)?.let { bundleList ->
                bundleList.forEach { item ->
                    if (item is AccountLinkType) {
                        item.loginType?.let { type ->
                            availableLinkTypes.remove(type)
                        }
                    }
                }
            }
            referrer = bundle.getSerializable(Constants.REFERRER) as? PageReferrer?
            if(availableLinkTypes.isEmpty()) {
                Logger.d(LOG_TAG, "No option to show, closing the dialog")
                getAccountLinkingFlow()?.onLinkingTypeSelectionCancelled()
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewBinding = DataBindingUtil.inflate(inflater, R.layout.account_link_types, container, false)
        setupView()
        return viewBinding.root
    }

    override fun onClick(view: View?) {
        view ?: return
        val viewIdMap = mapOf(R.id.connect_fb to LoginType.FACEBOOK,
                R.id.connect_google to LoginType.GOOGLE,
                R.id.connect_mobile to LoginType.MOBILE)
        when (view.id) {
            R.id.cross_icon -> {
                getAccountLinkingFlow()?.onLinkingTypeSelectionCancelled()
                return
            }
            else -> {
                viewIdMap[view.id]?.let { type ->
                    AccountsAnalyticsHelper.logAccountOptionSelectedEvent(referrer, AuthType.getAuthTypeFromLoginType(type)?.name)
                    getAccountLinkingFlow()?.tryLinkingAccount(type)
                }
            }
        }
    }

    private fun setupView() {
        val linkTypeViewMap = mapOf<LoginType, View>(
                LoginType.FACEBOOK to viewBinding.connectFb,
                LoginType.GOOGLE to viewBinding.connectGoogle,
                LoginType.MOBILE to viewBinding.connectMobile
        )
        availableLinkTypes.forEach { type ->
            linkTypeViewMap[type]?.visibility = View.VISIBLE
            linkTypeViewMap[type]?.setOnClickListener(this)
        }
        AccountsAnalyticsHelper.logAccountOptionsDisplayed(referrer)
        viewBinding.crossIcon.setOnClickListener(this)
    }

    private fun getAccountLinkingFlow(): AccountLinkingFlow? {
        return activity as? AccountLinkingFlow?
    }

    companion object {
        fun newInstance(accountLinkTypes: Serializable,
                        referrer: PageReferrer?): AccountTypesFragment {
            return AccountTypesFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(Constants.BUNDLE_LINKED_ACCOUNT_TYPES, accountLinkTypes)
                    putSerializable(Constants.REFERRER, referrer)
                }
            }
        }
    }
}

interface AccountLinkingFlow {
    fun onLinkingTypeSelectionCancelled()
    fun tryLinkingAccount(accountType: LoginType)
}