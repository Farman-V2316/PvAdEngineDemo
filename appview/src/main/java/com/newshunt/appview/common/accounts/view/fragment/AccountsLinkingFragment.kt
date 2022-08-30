/*
 * Copyright (c) 2020 Newshunt. All rights reserved.
 */
package com.newshunt.appview.common.accounts.view.fragment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.newshunt.appview.R
import com.newshunt.appview.common.accounts.AccountsAnalyticsHelper
import com.newshunt.appview.common.accounts.view.AccountsModule
import com.newshunt.appview.common.accounts.view.DaggerAccountsComponent
import com.newshunt.appview.common.accounts.view.adapters.AccountsLinkAdapter
import com.newshunt.appview.common.accounts.viewmodel.AccountsLinkingVMFactory
import com.newshunt.appview.common.accounts.viewmodel.AccountsLinkingViewModel
import com.newshunt.appview.common.ui.helper.ErrorHelperUtils
import com.newshunt.appview.databinding.AccountLinkingPlaceholderBinding
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.font.FontHelper
import com.newshunt.common.helper.info.ClientInfoHelper
import com.newshunt.common.view.view.BaseSupportFragment
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.common.model.entity.BaseError
import com.newshunt.dataentity.model.entity.AuthType
import com.newshunt.dataentity.model.entity.LoginType
import com.newshunt.dataentity.sso.model.entity.AccountLinkingResult
import com.newshunt.dataentity.sso.model.entity.AvailableAccounts
import com.newshunt.dataentity.sso.model.entity.DHAccount
import com.newshunt.dataentity.sso.model.entity.LoginPayload
import com.newshunt.sso.SSO
import com.newshunt.sso.helper.CustomHashGenerator
import com.newshunt.sso.helper.social.FacebookHelper
import com.newshunt.sso.helper.social.GoogleSignInHelper
import com.newshunt.sso.model.entity.SSOResult
import com.newshunt.sso.model.entity.UserExplicit
import com.newshunt.sso.view.fragment.TrueCallerVerificationDialogActivity
import javax.inject.Inject

/**
 * A fragment which takes an account type as parameter, performs login, fetches the associated
 * accounts which can be linked and finally performs selection of a final primary account
 *
 * @author srikanth on 06/11/2020
 */
private const val LOG_TAG = "AccountLinkingFragment"

class AccountsLinkingFragment : BaseSupportFragment(), GoogleSignInHelper.LoginCallback, FacebookHelper.Callback, View.OnClickListener {
    @Inject
    lateinit var accountsLinkingVMFactory: AccountsLinkingVMFactory
    private lateinit var accountsLinkingViewModel: AccountsLinkingViewModel

    private var linkSpecificAccount: LoginType? = null
    private lateinit var viewBinding: AccountLinkingPlaceholderBinding
    private var googleSignInHelper: GoogleSignInHelper? = null
    private var facebookHelper: FacebookHelper? = null
    private var referrer: PageReferrer? = null

    override fun onCreate(savedState: Bundle?) {
        super.onCreate(savedState)
        DaggerAccountsComponent
                .builder()
                .accountsModule(AccountsModule())
                .build()
                .inject(this)

        arguments?.let { bundle ->
            linkSpecificAccount = bundle.getSerializable(Constants.BUNDLE_LINK_SPECIFIC_ACCOUNT) as? LoginType?
            referrer = bundle.getSerializable(Constants.REFERRER) as? PageReferrer?
        }
        //This bundle variable is mandatory without which we can not initiate SDK login
        if (linkSpecificAccount == LoginType.NONE || linkSpecificAccount == LoginType.GUEST || linkSpecificAccount == null) {
            getAccountLinkingResultFlow()?.onAccountLinkingFailed()
            return
        }
        accountsLinkingViewModel = ViewModelProviders.of(this, accountsLinkingVMFactory).get(AccountsLinkingViewModel::class.java)
        performSDKLogin()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewBinding = DataBindingUtil.inflate(inflater, R.layout.account_linking_placeholder, container, false)
        return viewBinding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        observeLiveData()
    }

    override fun onGoogleLoginFailed() {
        Logger.e(LOG_TAG, "onGoogleLoginFailed")
        onSDKLoginFailed()
    }

    override fun onGoogleLoginSuccess(token: String?, userId: String?) {
        Logger.d(LOG_TAG, "onGoogleLoginSuccess")
        val loginPayload = LoginPayload(CustomHashGenerator.getHash(ClientInfoHelper.getClientId())!!,
                AuthType.GOOGLE.name, token, UserExplicit.YES.value)
        fetchLinkedAccounts(loginPayload)
    }

    override fun onGoogleLoginError(ssoResult: SSOResult?) {
        Logger.e(LOG_TAG, "onGoogleLoginError")
        onSDKLoginFailed()
    }

    override fun onGoogleLoginCancelled() {
        Logger.e(LOG_TAG, "onGoogleLoginCancelled")
        onSDKLoginFailed()
    }

    override fun onFacebookLoginError() {
        Logger.e(LOG_TAG, "onFacebookLoginError")
        onSDKLoginFailed()
    }

    override fun onFacebookLoginFailed(errorMessage: String?) {
        Logger.e(LOG_TAG, "onFacebookLoginFailed")
        onSDKLoginFailed()
    }

    override fun onFacebookLoginCancelled() {
        Logger.e(LOG_TAG, "onFacebookLoginCancelled")
        onSDKLoginFailed()
    }

    override fun onFacebookLogin(token: String?, userId: String?) {
        Logger.d(LOG_TAG, "onFacebookLogin success")
        val loginPayload = LoginPayload(CustomHashGenerator.getHash(ClientInfoHelper.getClientId())!!,
                AuthType.FACEBOOK.name, token, UserExplicit.YES.value)
        fetchLinkedAccounts(loginPayload)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Logger.d(LOG_TAG, "onActivityResult of fragment with requestCode : $requestCode and resultCode: $resultCode")
        when (requestCode) {
            Constants.REQ_CODE_LOGIN_RESULT -> {
                handleTCLoginResult(data, resultCode)
            }
            Constants.REQ_CODE_GOOGLE -> {
                handleGoogleSignInResult(data)
            }
            Constants.REQ_CODE_TRUECALLER -> {
            }
            else -> {
                //FB doesn't provide a specific requestCode. So we need to pass the requestCode
                // to the Facebook SDK to check whether it handled the requestCode or not.
                handleFBLoginResult(requestCode, resultCode, data)
            }
        }
    }

    override fun onClick(view: View?) {
        view ?: return
        if (view.id == R.id.done_btn) {
            selectPrimaryAccount()
        }
    }

    private fun getAccountLinkingResultFlow(): AccountLinkingResultFlow? {
        return activity as? AccountLinkingResultFlow?
    }

    private fun performSDKLogin() {
        when (linkSpecificAccount) {
            LoginType.FACEBOOK -> {
                facebookLogin()
            }
            LoginType.MOBILE -> {
                tcLogin()
            }
            LoginType.GOOGLE -> {
                googleLogin()
            }
            else -> {
            }
        }
    }

    private fun googleLogin() {
        if (googleSignInHelper == null) {
            googleSignInHelper = GoogleSignInHelper(this)
        }
        //If google play services is not up to date, we can cancel this login and move on!
        if (!GoogleSignInHelper.arePlayServicesAvailable(activity)) {
            Logger.e(LOG_TAG, "Google play services not available, cant google login!")
            onSDKLoginFailed(CommonUtils.getString(R.string.play_services_dialog_message))
            return
        }
        googleSignInHelper?.login()
    }

    private fun facebookLogin() {
        if (facebookHelper == null) {
            facebookHelper = FacebookHelper(this)
        }
        facebookHelper?.login(this)
    }

    private fun tcLogin() {
        val intent = Intent(activity, TrueCallerVerificationDialogActivity::class.java)
        val enableOneTouchLogin = arguments?.getBoolean(Constants.BUNDLE_ENABLE_ONE_TOUCH_LOGIN, true)
                ?: true
        intent.putExtra(Constants.BUNDLE_ENABLE_ONE_TOUCH_LOGIN, enableOneTouchLogin)
        startActivityForResult(intent, Constants.REQ_CODE_LOGIN_RESULT)
    }

    private fun handleGoogleSignInResult(data: Intent?) {
        //Check if the response is success.
        googleSignInHelper?.let {
            it.handleSignInResult(data)
        } ?: onGoogleLoginError(SSOResult.UNEXPECTED_ERROR)
    }

    private fun handleTCLoginResult(data: Intent?, resultCode: Int) {
        val isLoginSuccessful = data?.getBooleanExtra(Constants.BUNDLE_LOGIN_RESULT_SUCCESSFUL,
                false) ?: false
        val loginPayload = data?.getSerializableExtra(Constants.LOGIN_PAYLOAD) as? LoginPayload?

        if (resultCode == Activity.RESULT_CANCELED || !isLoginSuccessful || loginPayload == null) {
            Logger.e(LOG_TAG, "Truecaller login failed")
            onSDKLoginFailed()
            return
        }
        Logger.d(LOG_TAG, "Truecaller login sucess")
        fetchLinkedAccounts(loginPayload)
    }

    private fun handleFBLoginResult(requestCode: Int, resultCode: Int, data: Intent?) {
        facebookHelper?.callbackFromActivity(requestCode, resultCode, data)
    }

    /**
     * After SDK login, form the LoginPayload and hit the fetch API which returns various
     * accounts that are linked to this user
     */
    private fun fetchLinkedAccounts(payload: LoginPayload) {
        accountsLinkingViewModel.fetchLinkedAccounts(payload)
    }

    private fun showAccountLinkingUI(availableAccounts: AvailableAccounts) {
        if (!canResultBeDisplayed(availableAccounts)) {
            Logger.d(LOG_TAG, "No Accounts to link, we are done!")
            getAccountLinkingResultFlow()?.onAccountLinkingSuccess(AccountLinkingResult.NO_ACC_LINKED)
            return
        }
        viewBinding.availableAccounts = availableAccounts
        viewBinding.accountsLinkuiParent.availableAccounts = availableAccounts
        //Show Google/FB/TC accounts linking
        availableAccounts.dhAccounts?.let { socialAccounts ->
            if (socialAccounts.isNotEmpty()) {
                setupAccountsRecyclerView(viewBinding.accountsLinkuiParent.innerContainerInstance1
                        .rvChooseAccount, socialAccounts, false)
            }
        }

        //Show conflicting mobile numbers linking
        availableAccounts.conflictAccounts?.mobile?.let { mobileAccounts ->
            if (mobileAccounts.isNotEmpty()) {
                setupAccountsRecyclerView(viewBinding.accountsLinkuiParent
                        .innerContainerInstance2.rvChooseAccount, mobileAccounts, true)
            }
        }
        viewBinding.accountsLinkuiParent.doneBtn.setOnClickListener(this)
        viewBinding.executePendingBindings()
    }

    private fun setupAccountsRecyclerView(recyclerView: RecyclerView, dhAccounts: List<DHAccount>, isMobileAccounts: Boolean) {
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.adapter = AccountsLinkAdapter(dhAccounts, isMobileAccounts)
        recyclerView.setHasFixedSize(true)
    }

    private fun canResultBeDisplayed(availableAccounts: AvailableAccounts): Boolean {
        return !(availableAccounts.dhAccounts.isNullOrEmpty() &&
                availableAccounts.conflictAccounts?.mobile.isNullOrEmpty())
    }

    private fun selectPrimaryAccount() {
        val accountRecyclerView = viewBinding.accountsLinkuiParent.innerContainerInstance1.rvChooseAccount
        val conflictRecylerView = viewBinding.accountsLinkuiParent.innerContainerInstance2.rvChooseAccount
        var selectedAccount: DHAccount? = null
        var unselectedAccount: DHAccount? = null
        var selectedConflictAccount: DHAccount? = null
        var unselectedConflictAccount: DHAccount? = null
        (accountRecyclerView.adapter as? AccountsLinkAdapter?)?.let { adapter ->
            selectedAccount = adapter.getSelectedAccount()
            unselectedAccount = adapter.getUnSelectedAccount()
        }
        (conflictRecylerView.adapter as? AccountsLinkAdapter?)?.let { adapter ->
            selectedConflictAccount = adapter.getSelectedAccount()
            unselectedConflictAccount = adapter.getUnSelectedAccount()
        }
        if (selectedAccount == null && selectedConflictAccount == null) {
            Logger.d(LOG_TAG, "User did not select any account, we are done!")
            getAccountLinkingResultFlow()?.onAccountLinkingSuccess(AccountLinkingResult.NO_ACC_LINKED)
            return
        }
        AccountsAnalyticsHelper.logPrimaryAccountSelected(referrer,
                SSO.getInstance().userDetails?.userID,
                selectedAccount?.userId,
                selectedAccount?.linkedAccounts)
        //Pass on the selected and defunct accounts to BE API to switch accounts accordingly
        accountsLinkingViewModel.selectPrimaryAccount(selectedAccount,
                unselectedAccount,
                selectedConflictAccount,
                unselectedConflictAccount)

        Logger.d(LOG_TAG, "First account selected: ${selectedAccount?.handle}, Second account selected: " + "${selectedConflictAccount?.handle}")
    }

    private fun observeLiveData() {
        accountsLinkingViewModel.accountsLinkLiveData.observe(viewLifecycleOwner, Observer { result ->
            if (result.isSuccess) {
                val uiResponseWrapper = result.getOrNull()
                uiResponseWrapper?.response?.let {
                    //We might have some accounts to show on the UI
                    showAccountLinkingUI(it)
                    return@Observer
                }
                var accLinkingResult = AccountLinkingResult.SAME_ACC_LINKED
                //If no accounts to show to user, BE might decide to show some message instead
                if (uiResponseWrapper?.response == null && uiResponseWrapper?.message.isNullOrEmpty().not()) {
                    Logger.d(LOG_TAG, "Nothing to link, message: $uiResponseWrapper?.message")
                    activity?.let { fragmentActivity ->
                        view?.let { rootView ->
                            val message = uiResponseWrapper?.message ?: CommonUtils.getString(R.string.error_generic)
                            FontHelper.showCustomFontToast(fragmentActivity, message, Toast.LENGTH_LONG)
                        }
                    }
                    //If API failed, logout of google.
                    googleSignInHelper?.logout()
                    accLinkingResult = AccountLinkingResult.NO_ACC_LINKED
                }
                //In this case, we don't have any UI to show, end the process
                getAccountLinkingResultFlow()?.onAccountLinkingSuccess(accLinkingResult)
            } else {
                //If API failed, logout of google.
                googleSignInHelper?.logout()
                showError(result.exceptionOrNull())
            }
        })

        accountsLinkingViewModel.primaryAccountLiveData.observe(viewLifecycleOwner, Observer { result ->
            if (result.isSuccess) {
                val response = result.getOrNull()
                if (response?.response == null) {
                    //User selected the currently logged in account. Nothing much to do
                    getAccountLinkingResultFlow()?.onAccountLinkingSuccess(AccountLinkingResult.SAME_ACC_LINKED)
                } else {
                    //User selected a different account. Need to switch sessions
                    getAccountLinkingResultFlow()?.onAccountLinkingSuccess(AccountLinkingResult.DIFFERENT_ACC_LINKED)
                }
            } else {
                //API failed, show a snackbar and stay on screen.
                ErrorHelperUtils.showErrorSnackbar(result.exceptionOrNull(), viewBinding.root)
            }
        })

        //Show an overlay progress while the primary account switch is going on.
        accountsLinkingViewModel.primaryAccountStatusLiveData.observe(viewLifecycleOwner, Observer {
            viewBinding.overlayProgressLayer.visibility = if (it) {
                View.VISIBLE
            } else {
                View.GONE
            }
        })

        //Handle retry from full screen error
        accountsLinkingViewModel.retryLiveData.observe(viewLifecycleOwner, Observer {
            if (it) {
                Logger.d(LOG_TAG, "Need to retry SDK login")
                hideError()
                performSDKLogin()
            }
        })
    }

    private fun showError(throwable: Throwable?) {
        if (throwable is BaseError) {
            Logger.d(LOG_TAG, "Showing error for ${throwable.message}")
            viewBinding.errorParent.vm = accountsLinkingViewModel
            viewBinding.errorParent.baseError = throwable
            viewBinding.errorParent.root.visibility = View.VISIBLE
        }
    }

    private fun hideError() {
        viewBinding.errorParent.root.visibility = View.GONE
    }

    private fun onSDKLoginFailed(message: String = CommonUtils.getString(R.string.error_generic)) {
        activity?.let {
            FontHelper.showCustomFontToast(it, message, Toast.LENGTH_LONG)
        }
        getAccountLinkingResultFlow()?.onSDKLoginFailed()
    }

    companion object {
        fun newInstance(loginType: LoginType,
                        enableOneTouchLogin: Boolean,
                        referrer: PageReferrer?): AccountsLinkingFragment {
            return AccountsLinkingFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(Constants.BUNDLE_LINK_SPECIFIC_ACCOUNT, loginType)
                    putBoolean(Constants.BUNDLE_ENABLE_ONE_TOUCH_LOGIN, enableOneTouchLogin)
                    putSerializable(Constants.REFERRER, referrer)
                }
            }
        }
    }
}

interface AccountLinkingResultFlow {
    fun onAccountLinkingFailed()
    fun onSDKLoginFailed()
    fun onAccountLinkingSuccess(result: AccountLinkingResult)
}