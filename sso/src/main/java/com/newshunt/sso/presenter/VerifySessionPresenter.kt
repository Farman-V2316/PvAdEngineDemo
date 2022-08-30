/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.sso.presenter

import com.newshunt.common.helper.common.BusProvider
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.info.MigrationStatusProvider
import com.newshunt.common.presenter.BasePresenter
import com.newshunt.dhutil.helper.retrofit.NewsBaseUrlContainer
import com.newshunt.sso.model.entity.LoginResponse
import com.newshunt.sso.model.entity.SSOResult
import com.newshunt.sso.model.internal.service.VerifySessionServiceImpl
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

/**
 * @author anshul.jain
 * A presenter for verifying the session.
 */
class VerifySessionPresenter : BasePresenter() {

    private val verifySerice = VerifySessionServiceImpl()
    private val TAG = "VerifySessionPresenter"


    override fun start() {

    }

    override fun stop() {

    }

    fun verifySession() {
        val disposable = verifySerice.verifySession(NewsBaseUrlContainer
                .getUserServiceSecuredBaseUrl())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ resp ->
                    val userLoginResponse = resp.data
                    userLoginResponse ?: return@subscribe
                    val loginResponse = LoginResponse(SSOResult.SUCCESS, userLoginResponse)
                    BusProvider.getUIBusInstance().post(loginResponse)
                    if (userLoginResponse.userMigrationCompleted == true) {
                        MigrationStatusProvider.updateMigrationStatus(null)
                    }

                }, { throwable ->
                    Logger.d(TAG, "Inside error " + throwable.message)
                })
        addDisposable(disposable)
    }
}