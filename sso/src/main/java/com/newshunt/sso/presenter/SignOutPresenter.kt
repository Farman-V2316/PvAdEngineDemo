/*
 * Copyright (c) 2016 Newshunt. All rights reserved.
 */
package com.newshunt.sso.presenter


import com.newshunt.common.helper.common.Logger
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.common.helper.info.MigrationStatusProvider
import com.newshunt.common.presenter.BasePresenter
import com.newshunt.dataentity.common.model.entity.BaseError
import com.newshunt.dataentity.model.entity.LoginType
import com.newshunt.dataentity.sso.model.entity.UserLoginResponse
import com.newshunt.common.track.ApiResponseOperator
import com.newshunt.dhutil.helper.retrofit.NewsBaseUrlContainer
import com.newshunt.sso.R
import com.newshunt.sso.SSO
import com.newshunt.sso.helper.social.GoogleSignInHelper
import com.newshunt.sso.model.entity.SSOResult
import com.newshunt.sso.model.internal.service.LogoutService
import com.newshunt.sso.model.internal.service.LogoutServiceImpl
import com.newshunt.sso.view.view.SignOutView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

/**
 * Presenter for Signing out from social accounts.
 *
 * @author anshul.jain on 3/14/2016.
 */
class SignOutPresenter : BasePresenter(),
        GoogleSignInHelper.LogoutCallback {

    private val logoutService: LogoutService
    private var signOutView: SignOutView? = null
    private var loginType: LoginType? = null
    private var userLoginResponse: UserLoginResponse? = null
    private val publisher = SSO.getInstance().publisher

    init {
        this.logoutService = LogoutServiceImpl()
    }

    override fun start() {}

    override fun stop() {}

    fun logout(loginType: LoginType, signOutView: SignOutView) {
        this.loginType = loginType
        this.signOutView = signOutView
        val disposable = this.logoutService.logout(NewsBaseUrlContainer.getUserServiceSecuredBaseUrl())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ userLoginResponse ->
                    this.userLoginResponse = userLoginResponse
                    logoutFromClient(loginType)
                    signOutView.onLogoutSuccess()
                    if (userLoginResponse.userMigrationCompleted == true) {
                        MigrationStatusProvider.updateMigrationStatus(null)
                    }
                }, { throwable ->
                    onUserlogoutError(ApiResponseOperator.getError(throwable))
                    signOutView.onLogoutFailed()
                })
        addDisposable(disposable)
    }

    private fun onUserlogoutError(baseError: BaseError?) {
        if (baseError == null) {
            return
        }

        val ssolResult = if (CommonUtils.equals(CommonUtils.getString(R.string.error_no_connection), baseError
                        .message)) {
            SSOResult.NETWORK_ERROR
        } else {
            SSOResult.UNEXPECTED_ERROR
        }
        publisher.postLogoutResult(ssolResult)

        val toastMsg = if (CommonUtils.equals(CommonUtils.getString(R.string.error_no_connection), baseError
                        .message)) {
            CommonUtils.getString(R.string.error_no_connection)
        } else {
            CommonUtils.getString(R.string.unexpected_error_message)
        }
        signOutView?.showToast(toastMsg)
    }

    private fun logoutFromClient(loginType: LoginType) {

        if (!CommonUtils.isNetworkAvailable(CommonUtils.getApplication())) {
            return
        }
        /*Lets try to logout of google first. Incase we had some account linked to it, we got to
        logout. This is irrespective of what is the current login type*/
        googleLogoutFromClient()
        publishLogoutSuccessResults()
    }

    private fun publishLogoutSuccessResults() {
        publisher.postLogoutResult(SSOResult.SUCCESS, userLoginResponse)
    }

    private fun googleLogoutFromClient() {
        try {
            val signInHelper = GoogleSignInHelper(this, signOutView!!.viewContext)
            signInHelper.logout()
        } catch (e: Exception) {
            Logger.caughtException(e)
        }
    }

    override fun onGoogleLogoutFromClientSuccess() {
    }

    override fun onGoogleLogoutFromClientFailure() {
    }

    companion object {
        private const val LOG_TAG = "SignOutPresenter"
    }
}
