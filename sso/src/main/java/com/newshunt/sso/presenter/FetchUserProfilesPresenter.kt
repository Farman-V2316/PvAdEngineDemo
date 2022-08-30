/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.sso.presenter

import com.newshunt.common.presenter.BasePresenter
import com.newshunt.dhutil.helper.retrofit.NewsBaseUrlContainer
import com.newshunt.sso.model.internal.rest.FetchUserProfilesResponse
import com.newshunt.sso.model.service.FetchUserProfilesServiceImp
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

/**
 * @author anshul.jain
 */
class FetchUserProfilesPresenter(val view: FetchUserProfilesView) : BasePresenter() {

    val service = FetchUserProfilesServiceImp()

    override fun start() {
        val disposable = service.getUserProfiles(NewsBaseUrlContainer.getUserServiceBaseUrl())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    view.showResponseForFetchProfileList(it)
                }, {
                    view.fetchProfileListError()
                })
        addDisposable(disposable)
    }

    override fun stop() {
        destroy()
    }

}

interface FetchUserProfilesView {

    fun showResponseForFetchProfileList(response: FetchUserProfilesResponse)

    fun fetchProfileListError()
}