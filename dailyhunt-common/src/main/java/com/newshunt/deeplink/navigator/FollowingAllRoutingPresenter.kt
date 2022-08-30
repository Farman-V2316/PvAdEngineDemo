/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */
package com.newshunt.deeplink.navigator

import android.content.Context
import android.content.Intent
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.common.presenter.BasePresenter
import com.newshunt.common.view.view.BaseMVPView
import com.newshunt.dataentity.common.pages.PageEntity
import com.newshunt.dataentity.common.pages.PageSection
import com.newshunt.news.model.usecase.GetHomePageUsecase
import com.newshunt.dataentity.notification.FollowNavModel
import com.squareup.otto.Bus
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

/**
 * @author santhosh.kc
 */
private const val LOG_TAG = "FollowingAllRoutingPresenter"

interface FollowingAllRoutingView : BaseMVPView {

    fun onFollowingAllRouted(intent: Intent?, followNavModel: FollowNavModel)
}

class FollowingAllRoutingPresenter(val uiBus: Bus, val uniqueRequestId: Int,
                                   val followingAllRoutingView: FollowingAllRoutingView)
    : BasePresenter() {
    override fun start() {

    }

    override fun stop() {

    }

    fun startRouting(followNavModel: FollowNavModel, pageReferrer: PageReferrer) {

        val intent = FollowSectionNavigator.getFollowedAllIntent(followingAllRoutingView.viewContext,
                pageReferrer, followNavModel)
        if (intent == null) {
            followingAllRoutingView.onFollowingAllRouted(FollowSectionNavigator
                    .goToFollowHome(pageReferrer), followNavModel)
        } else {
            followingAllRoutingView.onFollowingAllRouted(intent, followNavModel)
        }
    }
}

interface FollowingTabRoutingView {
    fun onFollowingTabRoutingSuccess(intent: Intent?, followNavModel: FollowNavModel)

    fun onFollowingTabRoutingFailure(followNavModel: FollowNavModel)
}

class FollowingTabRoutingPresenter(val uniqueRequestId: Int, val context: Context,
                                   private val followingTabRoutingView: FollowingTabRoutingView)
    : BasePresenter() {
    override fun start() {

    }

    override fun stop() {

    }

    fun startRouting(followNavModel: FollowNavModel, pageReferrer: PageReferrer?) {
        val usecase = GetHomePageUsecase()
        usecase.invoke(PageSection.NEWS.section)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext { handleNewsPageResponseForFollowingTab(it, followNavModel, pageReferrer) }
                .subscribe()
    }

    private fun handleNewsPageResponseForFollowingTab(pageList: List<PageEntity>,
                                                      followNavModel: FollowNavModel,
                                                      pageReferrer: PageReferrer?) {
        val intent
                = FollowSectionNavigator.getFollowingTabIntentFromNewsPageResponse(pageList, pageReferrer)
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            followingTabRoutingView.onFollowingTabRoutingSuccess(intent, followNavModel)
        }
        followingTabRoutingView.onFollowingTabRoutingFailure(followNavModel)
    }
}