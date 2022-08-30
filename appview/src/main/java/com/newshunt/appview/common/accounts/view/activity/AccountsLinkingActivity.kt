/*
 * Copyright (c) 2020 Newshunt. All rights reserved.
 */
package com.newshunt.appview.common.accounts.view.activity

import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.newshunt.appview.R
import com.newshunt.appview.common.accounts.view.fragment.AccountLinkingFlow
import com.newshunt.appview.common.accounts.view.fragment.AccountLinkingResultFlow
import com.newshunt.appview.common.accounts.view.fragment.AccountTypesFragment
import com.newshunt.appview.common.accounts.view.fragment.AccountsLinkingFragment
import com.newshunt.appview.databinding.AccountsLinkingActivityBinding
import com.newshunt.common.helper.common.BusProvider
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.view.customview.NHBaseActivity
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.model.entity.LoginType
import com.newshunt.dataentity.sso.model.entity.AccountLinkingResult
import com.newshunt.dhutil.helper.theme.ThemeUtils
import java.io.Serializable

private const val TAG_ACCOUNT_TYPES_FRAGMENT = "TAG_ACCOUNT_TYPES_FRAGMENT"
private const val TAG_ACCOUNT_LINK_FRAGMENT = "TAG_ACCOUNT_LINK_FRAGMENT"
private const val LOG_TAG = "AccountLinkActivity"

/**
 * Activity to allow linking DH accounts into the currently logged in account
 *
 * @author srikanth on 06/11/2020
 */
class AccountsLinkingActivity : NHBaseActivity(), AccountLinkingFlow, AccountLinkingResultFlow {
    private var alreadyLinkedAccounts: List<Any>? = null
    private var linkSpecificAccount: LoginType? = null
    private lateinit var viewBinding: AccountsLinkingActivityBinding
    private var nextFlowPendingIntent: PendingIntent? = null
    private var referrer: PageReferrer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(ThemeUtils.preferredTheme.themeId)
        super.onCreate(savedInstanceState)
        viewBinding = DataBindingUtil.setContentView(this, R.layout.accounts_linking_activity)
        readBundle()
        setupUI()
    }

    override fun onLinkingTypeSelectionCancelled() {
        Logger.e(LOG_TAG, "onLinkingTypeSelectionCancelled")
        setResultAndFinish(false, AccountLinkingResult.NO_ACC_LINKED)
    }

    override fun tryLinkingAccount(accountType: LoginType) {
        linkSpecificAccount = accountType
        Logger.d(LOG_TAG, "Lets link $accountType for this user")
        addAccountLinkingFragment()
    }

    override fun onBackPressed() {
        Logger.e(LOG_TAG, "onBackPressed, User cancelled account linking")
        setResultAndFinish(false, AccountLinkingResult.NO_ACC_LINKED)
    }

    override fun onAccountLinkingFailed() {
        Logger.e(LOG_TAG, "onAccountLinkingFailed")
        setResultAndFinish(false, AccountLinkingResult.NO_ACC_LINKED)
    }

    override fun onAccountLinkingSuccess(result: AccountLinkingResult) {
        Logger.d(LOG_TAG, "onAccountLinkingSuccess, result=$result")
        setResultAndFinish(true, result)
    }

    override fun onSDKLoginFailed() {
        Logger.d(LOG_TAG, "onSDKLoginFailed, show the right UI again!")
        setupUI(true)
    }

    private fun readBundle() {
        intent?.let { incomingIntent ->
            alreadyLinkedAccounts = incomingIntent.getSerializableExtra(Constants.BUNDLE_LINKED_ACCOUNT_TYPES) as? List<Any>?
            linkSpecificAccount = LoginType.fromValue(incomingIntent.getStringExtra(Constants.BUNDLE_LINK_SPECIFIC_ACCOUNT))
            nextFlowPendingIntent = incomingIntent.getParcelableExtra(Constants.BUNDLE_SIGNIN_SUCCESS_PENDING_INTENT)
            referrer = incomingIntent.getSerializableExtra(Constants.REFERRER) as? PageReferrer?
        }
    }

    private fun setupUI(retry: Boolean = false) {
        if (alreadyLinkedAccounts.isNullOrEmpty() && (linkSpecificAccount == LoginType.NONE ||
                        linkSpecificAccount == LoginType.GUEST || linkSpecificAccount == null)) {
            Logger.e(LOG_TAG, "Can not proceed, nothing to link")
            setResultAndFinish(true, AccountLinkingResult.NO_ACC_LINKED)
            return
        }
        if (alreadyLinkedAccounts.isNullOrEmpty().not()) {
            Logger.d(LOG_TAG, "Adding account types fragment")
            addAccountTypesFragment()
            return
        }
        if (!retry) {
            addAccountLinkingFragment()
        } else {
            setResultAndFinish(false, AccountLinkingResult.NO_ACC_LINKED)
        }
    }

    private fun addAccountTypesFragment() {
        val fragment = AccountTypesFragment.newInstance(alreadyLinkedAccounts as Serializable, referrer)
        supportFragmentManager.beginTransaction()
                .replace(R.id.accounts_linking_parent, fragment, TAG_ACCOUNT_TYPES_FRAGMENT)
                .commit()
    }

    private fun addAccountLinkingFragment() {
        linkSpecificAccount?.let {
            val enableOneTouchLogin = intent?.getBooleanExtra(Constants.BUNDLE_ENABLE_ONE_TOUCH_LOGIN, true)
                    ?: true
            val fragment = AccountsLinkingFragment.newInstance(it, enableOneTouchLogin, referrer)
            supportFragmentManager.beginTransaction()
                    .replace(R.id.accounts_linking_parent, fragment, TAG_ACCOUNT_LINK_FRAGMENT)
                    .commit()
        }
    }

    /**
     * All flows to close this activity come from here.
     */
    private fun setResultAndFinish(isSuccess: Boolean, result: AccountLinkingResult) {
        if (!isSuccess) {
            setResult(Activity.RESULT_CANCELED)
        } else {
            Intent().apply {
                putExtra(Constants.BUNDLE_ACCOUNT_LINKING_RESULT, result)
                setResult(Activity.RESULT_OK, this)
            }
        }
        /**
         * SSO Presenter needs to know account linking is done so it saves the session and posts
         * the results
         */
        BusProvider.getUIBusInstance().post(result)
        finish()
        nextFlowPendingIntent?.send()
    }
}