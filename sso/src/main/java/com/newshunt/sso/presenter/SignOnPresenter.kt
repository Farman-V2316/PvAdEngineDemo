/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.sso.presenter

import com.newshunt.common.helper.common.BusProvider
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.info.MigrationStatusProvider
import com.newshunt.common.presenter.BasePresenter
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.common.model.entity.BaseError
import com.newshunt.dataentity.model.entity.LoginType
import com.newshunt.dataentity.sso.model.entity.LoginPayload
import com.newshunt.dataentity.sso.model.entity.UserLoginResponse
import com.newshunt.common.track.ApiResponseOperator
import com.newshunt.dhutil.helper.retrofit.NewsBaseUrlContainer
import com.newshunt.sso.R
import com.newshunt.sso.SSO
import com.newshunt.sso.model.entity.*
import com.newshunt.sso.model.internal.service.LoginServiceImpl
import com.newshunt.sso.view.view.SignOnView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

/**
 * Presenter for  SignOn feature
 *
 * @author anshul.jain
 */
class SignOnPresenter(private var signOnView: SignOnView? = null, loginType: LoginType? = null,
                      private var retryLogin: Boolean = false, private val uniqueId: Int? = null,
                      private var autoLogin: Boolean = false) : BasePresenter() {

    private var inputLoginType: LoginType? = loginType
    private val loginService = LoginServiceImpl()
    private val TAG = "SignOnPresenter"
    private var loginPayload: LoginPayload? = null

    override fun start() {
        if (retryLogin || autoLogin) {
            login(inputLoginType)
        } else {
            signOnView?.showLoadingProgress(false, null)
            signOnView?.showSignOnView(true)
        }
    }

    override fun stop() {

    }

    override fun destroy(): Boolean {
        signOnView = null
        return super.destroy()
    }

    fun login(loginType: LoginType?) {
        if (CommonUtils.isNetworkAvailable(CommonUtils.getApplication())) {
            when (loginType) {
                LoginType.FACEBOOK, LoginType.GOOGLE, LoginType.MOBILE -> socialLogin(loginType)
                else -> {
                    signOnView?.showLoadingProgress(false, null)
                    signOnView?.showSignOnView(true)
                }
            }
        } else {
            signOnView?.showToast(CommonUtils.getApplication().getString(com.newshunt.common.util.R.string.no_connection_error))
        }
    }


    fun socialLogin(loginPayload: LoginPayload, loginType: LoginType) {
        this.loginPayload = loginPayload
        this.inputLoginType = loginType
        SSO.getInstance().setSSOLoginSourceTracker(SSOLoginSourceType.SIGN_IN_PAGE, LoginMode.USER_EXPLICIT)
        this.loginService.login(loginPayload, NewsBaseUrlContainer.getUserServiceSecuredBaseUrl())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ userLoginResponse ->
                    val name = userLoginResponse.name
                    if (!retryLogin) {
                        signOnView?.showLoginSuccessMessage(name)
                    }
                    signOnView?.onLoginSuccessful(userLoginResponse.name, loginType, userLoginResponse)

                    if (signOnView?.isAccountLinkingFlow() == true && userLoginResponse?.linkedAccounts.isNullOrEmpty().not()) {
                        //Need to inform the SSO module that account linking is in progress
                        postLoginResponse(SSOResult.SUCCESS,
                                userLoginResponse = userLoginResponse,
                                isAccountLinkingInProgress = true)
                    } else {
                        postLoginResponse(SSOResult.SUCCESS, userLoginResponse = userLoginResponse)
                    }
                    if (userLoginResponse.userMigrationCompleted == true) {
                        MigrationStatusProvider.updateMigrationStatus(null)
                    }
                }, { throwable ->
                    Logger.d(TAG, "Inside error " + throwable.message)
                    handleError(loginType, ApiResponseOperator.getError(throwable))
                })
    }

    private fun handleError(loginType: LoginType, baseError: BaseError) {
        if (CommonUtils.equals(baseError.message, CommonUtils.getString(com.newshunt.common.util.R.string.error_no_connection))) {
            signOnView?.showToast(CommonUtils.getApplication().getString(com.newshunt.common.util.R.string.no_connection_error))
            signOnView?.showLoadingProgress(false, null)
            signOnView?.onLoginFailed(loginType, SSOResult.NETWORK_ERROR)
            postLoginResponse(SSOResult.NETWORK_ERROR, userLoginResponse = SSO.getLoginResponse())
            return
        }

        if (LoginType.NONE === inputLoginType) {
            signOnView?.showToast(baseError.message)
            signOnView?.showLoadingProgress(false, null)
            signOnView?.showSignOnView(true)
            return
        }

        when (SSOError.fromValue(baseError.status)) {
            SSOError.CODE_AUTH_FAILED -> {
                signOnView?.showToast(baseError.message)
                postLoginResponse(SSOResult.LOGIN_INVALID, userLoginResponse = SSO.getLoginResponse())
            }

            else -> postLoginResponse(SSOResult.UNEXPECTED_ERROR, userLoginResponse = SSO.getLoginResponse())
        }

        signOnView?.showUnexpectedError()
        signOnView?.onLoginFailed(loginType, SSOResult.UNEXPECTED_ERROR)
    }


    @JvmOverloads
    fun postLoginResponse(ssoResult: SSOResult,
                          userLoginResponse: UserLoginResponse? = null,
                          isAccountLinkingInProgress: Boolean = false) {
        val loginResponse = LoginResponse(ssoResult, userLoginResponse, isAccountLinkingInProgress)
        BusProvider.getUIBusInstance().post(loginResponse)
    }

    fun onHelperLoginError(loginType: LoginType, ssoResult: SSOResult) {
        if (autoLogin) {
            if (ssoResult == SSOResult.CANCELLED) {
                signOnView?.onLoginFailed(loginType, ssoResult)
            } else {
                signOnView?.showLoadingProgress(false, null)
                signOnView?.showSignOnView(true)
            }
            return
        }
        signOnView?.onLoginFailed(loginType, ssoResult)
    }

    fun onClientLoginError(loginType: LoginType, ssoResult: SSOResult) {

        var errorMessage = ""
        val res = signOnView?.viewContext?.resources ?: CommonUtils.getApplication().resources
        if (ssoResult == SSOResult.NETWORK_ERROR) {
            errorMessage = res.getString(
                com.newshunt.common.util.R.string
                    .no_connection_error)
        } else if (ssoResult == SSOResult.UNEXPECTED_ERROR) {
            errorMessage = res.getString(com.newshunt.common.util.R.string.unexpected_error_message)
        } else {
            return
        }
        signOnView?.showToast(errorMessage)
        signOnView?.showLoadingProgress(false, null)
        signOnView?.showSignOnView(true)
        signOnView?.onLoginFailed(loginType, ssoResult)
    }

    private fun socialLogin(loginType: LoginType) {
        signOnView?.apply {
            showSignOnView(false)
            showLoadingProgress(true, CommonUtils.getString(com.newshunt.common.util.R.string.please_wait))
            when (loginType) {
                LoginType.FACEBOOK -> facebookLogin()
                LoginType.GOOGLE -> googleLogin()
                LoginType.MOBILE -> phoneNumberLogin()
                else -> {
                }
            }
        }

    }
}