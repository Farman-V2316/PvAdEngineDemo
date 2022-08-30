/*
 * Copyright (c) 2020 Newshunt. All rights reserved.
 */
package com.newshunt.appview.common.viewmodel

import android.app.Application
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.databinding.ObservableField
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.newshunt.appview.R
import com.newshunt.appview.common.ui.activity.CreateDummyPostPojo
import com.newshunt.common.helper.common.AndroidUtils
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.common.asset.CreatePostUiMode
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.social.entity.AllLevelCards
import com.newshunt.dataentity.social.entity.DetailCard
import com.newshunt.dataentity.social.entity.LikeType
import com.newshunt.deeplink.DeeplinkUtils
import com.newshunt.deeplink.navigator.CommonNavigator
import com.newshunt.dhutil.bundleOf
import com.newshunt.news.di.ClearAndReadNetworkCommentsUsecase
import com.newshunt.news.di.CreateDummyPostUsecase
import com.newshunt.news.di.ReadDiscussionsForViewCommentsUsecase
import com.newshunt.news.di.ReadNetworkCommentsUsecase
import com.newshunt.news.model.sqlite.SocialDB
import com.newshunt.news.model.usecase.MediatorUsecase
import com.newshunt.news.model.usecase.Result0
import com.newshunt.news.model.usecase.toMediator2
import com.newshunt.news.viewmodel.EmojiClickHandlingViewModel
import com.newshunt.news.view.present.DeleteCommentUsecase
import com.newshunt.news.view.present.ReportCommentUsecase
import com.newshunt.news.viewmodel.CommonDetailsViewModel
import javax.inject.Inject
import javax.inject.Named

/**
 * Created by karthik.r on 2020-02-12.
 */
private const val TAG = "ViewAllComVM"

class ViewAllCommentsViewModel(context: Application,
                               lifecycleOwner: LifecycleOwner,
                               val postId: String,
                               section: String,
                               pageReferrer: PageReferrer?,
                               deleteCommentUsecase: MediatorUsecase<Bundle, Boolean>,
                               val reportCommentUsecase: MediatorUsecase<Bundle, String?>,
                               val cleanAndreadNetworkCommentsUsecase: MediatorUsecase<Bundle, Pair<String, Integer>>,
                               val readNetworkCommentsUsecase: MediatorUsecase<Bundle, Pair<String, Integer>>,
                               val readDiscussionsForViewCommentsUsecase: MediatorUsecase<Bundle, String>,
                               private val createDummyPostUsecase: MediatorUsecase<Bundle, CreateDummyPostPojo>)
    : CommonDetailsViewModel(context, section, postId, null, deleteCommentUsecase,
        reportCommentUsecase), ClickHandlingViewModel, EmojiClickHandlingViewModel {
    private var discussionNextPageUrl: String? = null
    var discussionFetchRunning: ObservableField<Boolean> = ObservableField(true)
    var clickDelegate: ClickDelegate? = null
    var latestUniqueId: String? = null
    val insertDummyPost = createDummyPostUsecase.data()
    var isDummyPost: Boolean = true
    var showCommentOnly: Boolean = false
    val mydiscussions = SocialDB.instance().cpDao().getByParent(postId)
    val mydiscussionsRepliesCount = SocialDB.instance().cpDao().getReplyCount(postId)
    var detailCard: LiveData<DetailCard?>? = null
    var discussions: LiveData<List<AllLevelCards>>? = null
    val reportComment = MediatorLiveData<Result0<String?>>()
    val conLiveData2 = AndroidUtils.connectionSpeedLiveData
    var discussionMode: ObservableField<CreatePostUiMode> = ObservableField(CreatePostUiMode.ALL)
    var isAllFilter = true
    var isCommentsFilter = true
    var isRepostFilter = true

    init {
        reportComment.addSource(reportCommentUsecase.data()) {
            reportComment.value = it
        }
    }

    fun fetchDetailCard(uniqueId: String?) {
        if (latestUniqueId == null) {
            latestUniqueId = uniqueId
        }

        if (latestUniqueId == null) {
            return
        }

        detailCard = SocialDB.instance().fetchDao().detailCardLiveByUniqueId(latestUniqueId!!)
        discussions = SocialDB.instance().fetchDao().discussionsForPost(latestUniqueId!!)
    }

    fun setShowCommentFilter(showCommentOnly: Boolean) {
        this.showCommentOnly = showCommentOnly
        if (showCommentOnly) {
            discussionMode.set(CreatePostUiMode.COMMENT)
        }
    }

    fun canShowFilter(): Boolean {
        if (showCommentOnly) {
            return false
        }

        return !isDummyPost
    }

    fun fetchCommentFirstPage() {
        if (insertDummyPost.value != null) {
            readNetworkCommentsUsecase.execute(bundleOf(Constants.BUNDLE_FILTER to discussionMode.get()))
        }
    }

    fun createDummyPost(title: String, canInsertDummyPost: Boolean) {
        createDummyPostUsecase.execute(bundleOf(Constants.BUNDLE_ACTIVITY_TITLE to title,
                Constants.BUNDLE_IS_WEB_ITEM to canInsertDummyPost))
    }

    override fun onViewClick(view: View, item: Any) {
        clickDelegate?.onViewClick(view, item)
    }

    override fun onViewClick(view: View) {
        clickDelegate?.onViewClick(view)
    }

    override fun onViewClick(view: View, item: Any, args: Bundle?) {
        clickDelegate?.onViewClick(view, item, args)
    }

    override fun onEmojiClick(view: View, item: Any, parentItem: Any?, likeType: LikeType,
                              isComment: Boolean?, commentType: String?) {
        clickDelegate?.onEmojiClick(view, item, parentItem, likeType, isComment, commentType)
    }

    override fun onInternalUrlClick(view: View, url: String) {
        super.onInternalUrlClick(view, url)
        Logger.d(TAG, "launching deeplink $url")
        if (DeeplinkUtils.isDHDeeplink(url)) {
            CommonNavigator.launchDeeplink(view.context, url, pageReferrer)
        } else if (DeeplinkUtils.isValidHost(url)) {
            AndroidUtils.launchExternalLink(view.context, url)
        }
    }

    fun getEmptyDiscussionText(): String {
        val card = detailCard?.value
        if (card?.i_allowComments() != false) {
            if (discussionMode.get() == CreatePostUiMode.ALL) {
                CommonUtils.getString(R.string.first_person_start_conversation)
            } else if (discussionMode.get() == CreatePostUiMode.COMMENT) {
                CommonUtils.getString(R.string.first_person_start_conversation)
            } else if (discussionMode.get() == CreatePostUiMode.REPOST) {
                return CommonUtils.getString(R.string.empty_repost)
            }
        } else {
            CommonUtils.getString(R.string.comments_disabled)
        }

        return CommonUtils.getString(R.string.first_person_start_conversation)
    }

    fun onDiscussionFilterClick(view: View, mode: CreatePostUiMode?) {
        val newMode: CreatePostUiMode = mode ?: CreatePostUiMode.ALL
        if (discussionMode.get() == newMode) {
            // Already is selected mode
            return
        }

        discussionMode.set(newMode)
        discussionFetchRunning.set(true)
        if (insertDummyPost.value != null) {
            cleanAndreadNetworkCommentsUsecase.execute(bundleOf(Constants.BUNDLE_FILTER to discussionMode.get()))
        }
    }

    fun fetchNextPage() {
        if (discussionFetchRunning.get() == true || TextUtils.isEmpty(discussionNextPageUrl)) {
            return
        }

        discussionFetchRunning.set(true)
        readDiscussionsForViewCommentsUsecase.execute(
                bundleOf(Constants.BUNDLE_CONTENT_URL to discussionNextPageUrl))
    }

    fun readNetworkCommentsUsecaseResponse(result: Result0<Pair<String, Integer>>) {
        discussionNextPageUrl = result.getOrNull()?.first
        fetchDetailCard(latestUniqueId)
        discussionFetchRunning.set(false)
    }

    fun readDiscussionsForViewCommentsUsecaseResponse(result: Result0<String>) {
        discussionNextPageUrl = result.getOrNull()
        discussionFetchRunning.set(false)
    }

    class Factory @Inject constructor(private val app: Application,
                                      private val lifecycleOwner: LifecycleOwner,
                                      @Named("postId") private val postId: String,
                                      @Named("section") private val section: String,
                                      @Named("pageReferrer") private val pageReferrer: PageReferrer?,
                                      private val deleteCommentUsecase: DeleteCommentUsecase,
                                      private val reportCommentUsecase: ReportCommentUsecase,
                                      private val clearReadNetworkCommentsUsecase: ClearAndReadNetworkCommentsUsecase,
                                      private val readNetworkCommentsUsecase: ReadNetworkCommentsUsecase,
                                      private val readDiscussionsForViewCommentsUsecase: ReadDiscussionsForViewCommentsUsecase,
                                      private val createDummyPostUsecase: CreateDummyPostUsecase) :
            ViewModelProvider.AndroidViewModelFactory(app) {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return ViewAllCommentsViewModel(app, lifecycleOwner, postId, section, pageReferrer,
                    deleteCommentUsecase.toMediator2(),
                    reportCommentUsecase.toMediator2(),
                    clearReadNetworkCommentsUsecase.toMediator2(),
                    readNetworkCommentsUsecase.toMediator2(),
                    readDiscussionsForViewCommentsUsecase.toMediator2(),
                    createDummyPostUsecase.toMediator2()) as T
        }
    }

}