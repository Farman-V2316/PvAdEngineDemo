/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.appview.common.profile.viewmodel

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModel
import com.newshunt.appview.R
import com.newshunt.appview.common.profile.helper.mapFilterToTimeLimit
import com.newshunt.appview.common.profile.model.usecase.CountFilteredHistoryUsecase
import com.newshunt.appview.common.profile.model.usecase.CountHistoryUsecase
import com.newshunt.appview.common.profile.model.usecase.QueryHistoryForDisplayUsecase
import com.newshunt.appview.common.ui.helper.NavigationEvent
import com.newshunt.appview.common.ui.helper.NavigationHelper
import com.newshunt.appview.common.viewmodel.ClickHandlingViewModel
import com.newshunt.common.helper.appconfig.AppConfig
import com.newshunt.common.helper.common.Constants
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.common.asset.CommonAsset
import com.newshunt.dataentity.model.entity.TimeFilter
import com.newshunt.dataentity.news.analytics.ProfileReferrer
import com.newshunt.dhutil.analytics.AnalyticsHelper2
import com.newshunt.news.model.usecase.MediatorUsecase
import javax.inject.Inject
import javax.inject.Named

/**
 * A MVVM ViewModel implementation to handle History usecases
 * <p>
 * Created by srikanth.ramaswamy on 04/17/2019.
 */
class HistoryViewModel @Inject constructor(private val queryHistoryMediatorUC: QueryHistoryForDisplayUsecase,
                                           @Named("markHistoryDeletedMediatorUC")
                                           private val markHistoryDeletedMediatorUC: MediatorUsecase<String, Unit>,
                                           @Named("deleteHistoryMediatorUC")
                                           private val deleteHistoryMediatorUC: MediatorUsecase<Unit, Unit>,
                                           @Named("undoDeleteHistoryMediatorUC")
                                           private val undoDeleteHistoryMediatorUC: MediatorUsecase<Unit, Unit>,
                                           @Named("clearHistoryMediatorUC")
                                           private val clearHistoryMediatorUC: MediatorUsecase<Unit, Unit>,
                                           private val countHistoryMediatorUC: CountHistoryUsecase,
                                           private val countFilteredHistoryMediatorUC: CountFilteredHistoryUsecase) :
        ViewModel(),
        ClickHandlingViewModel {

    private var historyTimeFilter = TimeFilter.NINETY_DAYS
    val historyLiveData = queryHistoryMediatorUC.data()
    val totalHistoryCountLiveData = countHistoryMediatorUC.data()
    val filteredHistoryCountLiveData = countFilteredHistoryMediatorUC.data()

    init {
        countHistoryMediatorUC.execute(Unit)
    }

    var isSocialLogin = false
    set(value) {
        field = value
        setupTimeFilter()
    }

    fun queryHistory() {
        val sinceTime = mapFilterToTimeLimit(historyTimeFilter)
        queryHistoryMediatorUC.execute(sinceTime)
        countFilteredHistoryMediatorUC.execute(sinceTime)
    }

    fun clearHistory() {
        clearHistoryMediatorUC.execute(Unit)
    }

    fun commitEditSession() {
        deleteHistoryMediatorUC.execute(Unit)
    }

    fun undoEdits() {
        undoDeleteHistoryMediatorUC.execute(Unit)
    }

    override fun onViewClick(view: View, item: Any) {
        onViewClick(view, item, null)
    }

    override fun onViewClick(view: View, item: Any, args: Bundle?) {
        val position = args?.getInt(Constants.STORY_POSITION) ?: -1
        (item as? CommonAsset)?.let {
            when (view.id) {
                R.id.interaction_rootview -> {
                    openDetail(it, position)
                }
                R.id.user_interaction_delete -> {
                    markHistoryDeletedMediatorUC.execute(it.i_id())
                }
                else -> {
                }
            }
        }
    }

    private fun openDetail(item: CommonAsset, itemPosition: Int) {
        AnalyticsHelper2.logStoryCardClickEvent(
                item,
                PageReferrer(ProfileReferrer.HISTORY),
                itemPosition,
                null,
                null)
        Intent(Constants.NEWS_DETAIL_ACTION).apply {
            putExtra(Constants.STORY_ID, item.i_id())
            putExtra(Constants.BUNDLE_IS_FROM_HISTORY, true)
            putExtra(Constants.BUNDLE_HISTORY_SINCE_TIME, mapFilterToTimeLimit(historyTimeFilter))

            setPackage(AppConfig.getInstance()!!.packageName)
            NavigationHelper.navigationLiveData.postValue(NavigationEvent(this, callback
            = null))
        }
    }

    private fun setupTimeFilter() {
        historyTimeFilter = if (!isSocialLogin) {
            TimeFilter.SEVEN_DAYS
        } else {
            TimeFilter.NINETY_DAYS
        }
    }
}