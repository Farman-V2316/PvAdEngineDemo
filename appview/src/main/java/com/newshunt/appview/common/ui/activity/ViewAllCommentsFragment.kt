/*
* Copyright (c) 2020 Newshunt. All rights reserved.
*/
package com.newshunt.appview.common.ui.activity

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.databinding.ObservableField
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.newshunt.adengine.view.helper.AdDBHelper
import com.newshunt.appview.BR
import com.newshunt.appview.R
import com.newshunt.appview.common.di.CardsModule
import com.newshunt.appview.common.ui.helper.CardsBindUtils
import com.newshunt.appview.common.viewmodel.CardsViewModel
import com.newshunt.appview.common.viewmodel.ViewAllCommentsViewModel
import com.newshunt.appview.databinding.*
import com.newshunt.common.helper.common.AndroidUtils
import com.newshunt.common.helper.common.BaseErrorBuilder
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.preference.GenericAppStatePreference
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.common.view.customview.GenericCustomSnackBar
import com.newshunt.common.view.customview.NhWebView
import com.newshunt.common.view.customview.fontview.NHTextView
import com.newshunt.common.view.dbgCode
import com.newshunt.common.view.view.BaseSupportFragment
import com.newshunt.dataentity.analytics.entity.NhAnalyticsUserAction
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.common.asset.AssetType2
import com.newshunt.dataentity.common.asset.CommonAsset
import com.newshunt.dataentity.common.asset.CreatePostUiMode
import com.newshunt.dataentity.common.asset.PostEntity
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.common.model.entity.BaseError
import com.newshunt.dataentity.common.pages.PageSection
import com.newshunt.dataentity.common.pages.ReportEntity
import com.newshunt.dataentity.news.analytics.NewsReferrer
import com.newshunt.dataentity.notification.SocialCommentsModel
import com.newshunt.dataentity.social.entity.AllLevelCards
import com.newshunt.dataentity.social.entity.DetailCard
import com.newshunt.dataentity.social.entity.ReplyCount
import com.newshunt.deeplink.navigator.CommonNavigator
import com.newshunt.deeplink.navigator.NewsNavigator
import com.newshunt.deeplink.navigator.NhBrowserNavigator
import com.newshunt.dhutil.bundleOf
import com.newshunt.dhutil.helper.AppSettingsProvider
import com.newshunt.dhutil.helper.common.DailyhuntConstants
import com.newshunt.news.di.DaggerViewAllCommentsComponent
import com.newshunt.news.di.ViewAllCommentsModule
import com.newshunt.news.model.sqlite.SocialDB
import com.newshunt.news.util.NewsConstants
import com.newshunt.news.view.fragment.PostActions
import com.newshunt.sdk.network.connection.ConnectionSpeed
import com.newshunt.sdk.network.connection.ConnectionSpeedEvent
import com.newshunt.sso.SSO
import com.newshunt.sso.model.entity.LoginMode
import com.newshunt.sso.model.entity.SSOLoginSourceType
import java.util.Collections
import javax.inject.Inject

/**
 * Fragment to list all the comments for a post/article
 *
 * Created by karthik.r on 2020-02-11.
 */
class ViewAllCommentsFragment : BaseSupportFragment(), AdDBHelper, PostActions {

    companion object {

        private const val LOG_TAG = "ViewAllCommFrg"

        @JvmStatic
        fun newInstance(intent: Intent): ViewAllCommentsFragment {
            val fragment = ViewAllCommentsFragment()
            fragment.arguments = intent.extras
            return fragment
        }
    }

    private var pageReferrer: PageReferrer? = null
    private var referrerRaw: String? = null
    private var currentPageReferrer: PageReferrer? = null
    private var commentParams: Map<String, String>? = null
    private var heading: String? = null
    private var title: String? = null
    private var subtitle: String? = null
    private var canInsertDummyPost = false
    private lateinit var section: String
    private lateinit var location: String
    private var socialCommentsModel: SocialCommentsModel? = null
    private var postId: String? = null
    private var parentId: String? = null
    private lateinit var discussionList: RecyclerView
    private lateinit var adapter: DiscussionAdapter
    private lateinit var commentBarHolder: LinearLayout
    private var commentDataBinding: ViewDataBinding? = null
    private var lastKnownMyDiscussionCount: Int = 0
    private var lastKnownMyDiscussionRepliesCount: List<ReplyCount>? = null
    private var v4BackUrl: String? = null
    private var backPressedHandled = false
    private var uiPaused = false
    private lateinit var linLayoutManager: LinearLayoutManager
    private var isRepost = false

    @Inject
    lateinit var viewModelF: ViewAllCommentsViewModel.Factory
    private lateinit var vm: ViewAllCommentsViewModel

    @Inject
    lateinit var cardsViewModelF: CardsViewModel.Factory
    private lateinit var cvm: CardsViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view = inflater.inflate(R.layout.activity_view_all_comments, container, false)
        val bundle = arguments
        if (bundle != null) {
            pageReferrer = bundle.getSerializable(Constants.BUNDLE_ACTIVITY_REFERRER) as? PageReferrer
            referrerRaw = bundle.getString(Constants.REFERRER_RAW)
            section = bundle.getString(NewsConstants.DH_SECTION, PageSection.NEWS.section)
            location = bundle.getString(Constants.LOCATION)
                    ?: Constants.FETCH_LOCATION_DETAIL
            val showCommentOnly = bundle.getBoolean(Constants.BUNDLE_IS_COMMENT_ONLY, false)
            v4BackUrl = arguments?.getString(Constants.V4BACKURL)
            val socialCommentsModel = bundle.getSerializable(Constants.BUNDLE_COMMENTS_MODEL) as? SocialCommentsModel
            if (socialCommentsModel != null) {
                canInsertDummyPost = socialCommentsModel.commentParams != null
                this.socialCommentsModel = socialCommentsModel
                heading = socialCommentsModel.title1
                if (CommonUtils.isEmpty(heading)) {
                    heading = socialCommentsModel.title2
                } else {
                    title = socialCommentsModel.title2
                }
                if (CommonUtils.isEmpty(heading)) {
                    heading = socialCommentsModel.title3
                } else {
                    subtitle = socialCommentsModel.title3
                }

                commentParams = socialCommentsModel.commentParams
                postId = commentParams?.get(Constants.BUNDLE_ID)
                if (socialCommentsModel.contentMeta != null && CommonUtils.isEmpty(heading)) {
                    heading = socialCommentsModel.contentMeta.title
                }
                if (heading != null) {
                    activity?.title = heading
                }
            } else {
                postId = bundle.getSerializable(Constants.BUNDLE_POST_ID) as? String
                parentId = bundle.getString(Constants.BUNDLE_PARENT_ID)
                heading = bundle.getSerializable(Constants.BUNDLE_ACTIVITY_TITLE) as? String
            }

            if (postId == null) {
                activity?.finish()
                return view
            }

            currentPageReferrer = PageReferrer(NewsReferrer.COMMENT_DETAIL, postId)
            discussionList = view.findViewById(R.id.discussion_list)
            commentBarHolder = view.findViewById(R.id.comments_bar_holder)
            linLayoutManager = LinearLayoutManager(activity)
            discussionList.layoutManager = linLayoutManager


            DaggerViewAllCommentsComponent.builder().viewAllCommentsModule(ViewAllCommentsModule(
                    CommonUtils.getApplication(), postId!!, section, this, pageReferrer
            )).cardsModule(CardsModule(
                    CommonUtils.getApplication(),
                    SocialDB.instance(),
                    postId!!,
                    postId!!,
                    null,
                    location,
                    adDbHelper = this,
                    supportAds = false,
                    lifecycleOwner = this,
                    section = section,
                    searchQuery = null,
                    performLogin = ::performLogin
            )).build().inject(this)

            vm = ViewModelProviders.of(this, viewModelF)[ViewAllCommentsViewModel::class.java]
            cvm = ViewModelProviders.of(this, cardsViewModelF)[CardsViewModel::class.java]
            cvm.setCurrentPageReferrer(currentPageReferrer
                    ?: PageReferrer(NewsReferrer.COMMENT_DETAIL),
                    currentPageReferrer ?: PageReferrer(NewsReferrer.COMMENT_LIST), null)
            if (bundle != null) {
                vm.isAllFilter = bundle.getBoolean("isAllFilter", false)
                vm.isCommentsFilter = bundle.getBoolean("isCommentsFilter", false)
                vm.isRepostFilter = bundle.getBoolean("isRepostFilter", false)
            }
            if (!vm.isAllFilter)
                if (vm.isCommentsFilter)
                    vm.discussionMode = ObservableField(CreatePostUiMode.COMMENT)
                else if (vm.isRepostFilter)
                    vm.discussionMode = ObservableField(CreatePostUiMode.REPOST)
            isRepost = bundle.getString("preSelectFilter", null) == AssetType2.REPOST.name
            if (isRepost) {
                vm.discussionMode = ObservableField(CreatePostUiMode.REPOST)
            }
            vm.setShowCommentFilter(showCommentOnly)
            vm.readDiscussionsForViewCommentsUsecase.data().observe(viewLifecycleOwner, Observer {
                vm.readDiscussionsForViewCommentsUsecaseResponse(it)
                adapter.dataSetChange()
            })
            AndroidUtils.connectionSpeedLiveData.observe(viewLifecycleOwner, Observer {
                if (it.connectionSpeed != ConnectionSpeed.NO_CONNECTION) {
                    if (adapter.items.size == 0) {
                        vm.createDummyPost(heading ?: "", canInsertDummyPost)
                        vm.fetchCommentFirstPage()
                    }
                }
            })

            vm.cleanAndreadNetworkCommentsUsecase.data().observe(viewLifecycleOwner, Observer {
                vm.readNetworkCommentsUsecaseResponse(it)
                adapter.dataSetChange()
            })

            vm.clickDelegate = cvm.cardClickDelegate
            vm.insertDummyPost.observe(this, Observer {
                adapter.dummyPost = it.getOrNull()?.pe
                val fetchId = it.getOrNull()?.fetchId
                vm.isDummyPost = it.getOrNull()?.isDummy ?: true
                if (fetchId != null) {
                    val uniqueId = it.getOrNull()?.pe?.getUniqueId(fetchId)
                    vm.fetchDetailCard(uniqueId)
                    registerDetailCard()
                }
                if (adapter.dummyPost != null) {
                    adapter.dataSetChange()
                    vm.fetchCommentFirstPage()
                }
            })
            vm.deletePostData.observe(viewLifecycleOwner, Observer {
                if (it.isSuccess && it.getOrNull() == true) {
                    showPostDeletedSnackbar()
                }
            })

            vm.readNetworkCommentsUsecase.data().observe(viewLifecycleOwner, Observer {
                vm.readNetworkCommentsUsecaseResponse(it)
                if (it.isSuccess) {
                    if (it.getOrNull()?.second == Integer(0)) {
                        adapter.isEmptyList = adapter.items.isEmpty()
                        adapter.error = null
                        adapter.dataSetChange()
                    }
                } else {
                    if (it.exceptionOrNull() is BaseError) {
                        adapter.error = it.exceptionOrNull() as BaseError
                    } else {
                        adapter.error = BaseErrorBuilder.getBaseError(it.exceptionOrNull(), null,
                                null, null)
                    }

                    Logger.e(LOG_TAG, "Error fetching discussions", it.exceptionOrNull())
                }
            })

            registerDetailCard()
            vm.mydiscussions.observe(viewLifecycleOwner, Observer {
                if (lastKnownMyDiscussionCount != it) {
                    lastKnownMyDiscussionCount = it
                    loadDiscussionFirstPage()
                }
            })
            vm.mydiscussionsRepliesCount.observe(viewLifecycleOwner, Observer {
                if (lastKnownMyDiscussionRepliesCount != it) {
                    compareAndOpenComment(lastKnownMyDiscussionRepliesCount, it)
                    lastKnownMyDiscussionRepliesCount = it
                    loadDiscussionFirstPage()
                    adapter.updateReplyCount(lastKnownMyDiscussionRepliesCount)
                }
            })

            vm.reportCommentTriggered.observe(viewLifecycleOwner, Observer {
                if (it == true) {
                    showReportL2Page()
                    vm.reportCommentTriggered.postValue(false)
                }
            })

            vm.createDummyPost(heading ?: "", canInsertDummyPost)
            adapter = DiscussionAdapter(vm, cvm, this, this, title, subtitle, viewLifecycleOwner)
            discussionList.adapter = adapter
        } else {
            activity?.finish()
        }

        initActionBar(view)
        return view
    }

    private fun getSorted(data: List<AllLevelCards>): List<AllLevelCards> {
        Collections.sort(data) { o1, o2 ->
            o2.postEntity.i_publishTime()?.compareTo(o1.postEntity.i_publishTime() ?: 0) ?: 0
        }

        return data
    }

    private fun compareAndOpenComment(lastKnownReplyCount: List<ReplyCount>?, newReplyCount: List<ReplyCount>?) {
        if (lastKnownReplyCount != null && newReplyCount != null) {
            for (replyItem in newReplyCount) {
                if (!lastKnownReplyCount.contains(replyItem)) {
                    openDiscussionDetail(replyItem.parentId)
                    return
                }
            }
        }
    }

    private fun openDiscussionDetail(discussionId: String) {
        adapter.items.forEach {
            if (it.i_id() == discussionId && this.view != null) {
                vm.onViewClick(this.view!!, it, CardsBindUtils.bundle(Constants.BUNDLE_IN_DETAIL, true))
            }
        }
    }

    override fun onScrollToOtherPerspective() {
        // Do Nothing
    }

    override fun onFullPageLoaded() {
        // Do Nothing
    }

    override fun onRetryClicked(speedEvent: ConnectionSpeedEvent?, baseError: BaseError?) {

        if (speedEvent?.connectionSpeed == ConnectionSpeed.NO_CONNECTION) {
            var nwSettingIntent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
            activity?.startActivity(nwSettingIntent)
        } else {
            if (baseError.dbgCode().get().equals("BB04")) {
                handleBackPress()
            } else {
                vm.createDummyPost(heading ?: "", canInsertDummyPost)
                vm.fetchCommentFirstPage()
            }
        }

    }

    override fun shortContentLoadMore() {
        // Do Nothing
    }

    private fun showReportL2Page() {
        val reportUrl = PreferenceManager
                .getPreference(GenericAppStatePreference.REPORT_POST_URL, Constants.EMPTY_STRING)
        val fallback = if (reportUrl.isNullOrEmpty()) return else reportUrl
        val browserIntent = NhBrowserNavigator.getTargetIntent()
        browserIntent.putExtra(DailyhuntConstants.URL_STR, fallback)
        browserIntent.putExtra(DailyhuntConstants.USE_WIDE_VIEW_PORT, true)
        browserIntent.putExtra(DailyhuntConstants.CLEAR_HISTORY_ON_PAGE_LOAD, true)
        browserIntent.putExtra(Constants.VALIDATE_DEEPLINK, true)
        activity?.startActivity(browserIntent)
    }

    private fun loadDiscussionFirstPage() {
        vm.fetchCommentFirstPage()
    }

    private fun registerDetailCard() {
        vm.detailCard?.observe(viewLifecycleOwner, Observer {
            adapter.dummyPost = it
            if (commentDataBinding == null && it != null) {
                if (!isRepost && CardsBindUtils.canAllowComment(it)) {
                    inflateCommentsBar(it)
                }
            } else {
                commentDataBinding?.setVariable(BR.card, it)
                commentDataBinding?.executePendingBindings()
            }
        })

        vm.discussions?.observe(viewLifecycleOwner, Observer {
            adapter.items.clear()
            if (it != null) {
                val sortedList = getSorted(it)
                if (vm.discussionMode.get() == CreatePostUiMode.ALL) {
                    adapter.items.addAll(sortedList)
                } else {
                    val mode = vm.discussionMode.get()?.name
                    sortedList.forEach { card ->
                        if (mode == CreatePostUiMode.COMMENT.name && card.i_type() == AssetType2.COMMENT.name) {
                            adapter.items.add(card)
                        } else if (mode == CreatePostUiMode.REPOST.name && card.i_type() == AssetType2.REPOST.name) {
                            adapter.items.add(card)
                        }
                    }
                }
            }

            adapter.isEmptyList = !adapter.items.isNotEmpty()
            adapter.dataSetChange()
        })
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (hidden) {
            uiPaused = true
        } else {
            if (uiPaused) {
                //       vm.fetchCommentFirstPage()
            }
        }
    }

    private fun inflateCommentsBar(card: DetailCard) {
        commentDataBinding = DataBindingUtil.inflate<NewsDetailCommentBarBinding>(layoutInflater,
                R.layout.news_detail_comment_bar, commentBarHolder, false)

        if (commentBarHolder.childCount > 0) {
            commentBarHolder.removeAllViews()
        }

        commentBarHolder.addView(commentDataBinding?.root)
        commentDataBinding?.setVariable(BR.vm, cvm)
        commentDataBinding?.setVariable(BR.appSettingsProvider, AppSettingsProvider)
        commentDataBinding?.lifecycleOwner = viewLifecycleOwner
        commentDataBinding?.setVariable(BR.card, card)
        commentDataBinding?.setVariable(BR.v, true)
        commentDataBinding?.setVariable(BR.isComment, card.i_type() == AssetType2.COMMENT.name)
        commentDataBinding?.root?.findViewById<NHTextView>(R.id.edit_comment)?.setOnClickListener { v ->
            try {
                val intent = CommonNavigator.getPostCreationIntent(
                        postId,
                        CreatePostUiMode.COMMENT,
                        null,
                        currentPageReferrer,
                        null,
                        card.i_source()?.id,
                        card.i_source()?.type,
                        parentId)
                this.activity?.startActivityForResult(intent, 0)
            } catch (ex: Exception) {
                // Activity not found
            }

            false
        }
        commentDataBinding?.executePendingBindings()
    }


    private fun initActionBar(view: View?) {
        val toolbarBackButtonContainer: ImageView? = view?.findViewById(R.id.actionbar_back_black)
        toolbarBackButtonContainer?.setOnClickListener {
            if (!handleActionBarBackPress(false)) {
                activity?.onBackPressed()
            }
        }

        val disclaimerMenu: ImageView? = view?.findViewById(R.id.disclaimer_menu)
        disclaimerMenu?.setOnClickListener {
            if (activity != null) {
                showDisclaimer(activity!!)
            }
        }

        val actionbar_title: NHTextView? = view?.findViewById(R.id.actionbar_title)
        if (isRepost)
            actionbar_title?.text = CommonUtils.getString(R.string.repost_fragment_name)
        else
            actionbar_title?.text = CommonUtils.getString(R.string.comments_fragment_name)
    }

    private fun handleActionBarBackPress(isSystemBackPress: Boolean): Boolean {
        if (backPressedHandled) {
            backPressedHandled = false
            return true
        }
        if (!CommonUtils.isEmpty(v4BackUrl)) {
            val pageReferrer = PageReferrer(NewsReferrer.STORY_DETAIL, postId)
            pageReferrer.referrerAction = NhAnalyticsUserAction.BACK
            CommonNavigator.launchDeeplink(requireContext(), v4BackUrl, pageReferrer)
            backPressedHandled = true
            return true
        } else if (NewsNavigator.shouldNavigateToHome(activity, pageReferrer, isSystemBackPress, referrerRaw)) {
            val pageReferrer = PageReferrer(NewsReferrer.STORY_DETAIL, postId)
            pageReferrer.referrerAction = NhAnalyticsUserAction.BACK
            NewsNavigator.navigateToHomeOnLastExitedTab(activity, pageReferrer)
            backPressedHandled = true
            return true
        }

        return false
    }

    override fun handleBackPress(): Boolean {
        if (handleActionBarBackPress(true)) {
            return true
        }

        return false
    }

    override fun getTotalItems(): Int? {
        return 0
    }

    override fun getItemIdBeforeIndex(adPosition: Int): String? {
        return null
    }

    override fun getActivityContext(): Activity? {
        return activity
    }

    private fun showDisclaimer(fragmentActivity: androidx.fragment.app.FragmentActivity) {
        val dialogFragment = DisclaimerDialogFragment()
        val disclaimerUrl = PreferenceManager
                .getPreference(GenericAppStatePreference.DISCLAIMER_URL, Constants.EMPTY_STRING)
        val fallback = if (disclaimerUrl.isNullOrEmpty()) return else disclaimerUrl
        dialogFragment.arguments = bundleOf(Constants.BUNDLE_DISCLAIMER_URL to fallback)
        dialogFragment.show(fragmentActivity.supportFragmentManager, "disclaimer")
    }

    private fun performLogin(showToast: Boolean, toastMsgId: Int) {
        activity?.let {
            val sso = SSO.getInstance()
            sso.login(it as Activity, LoginMode.USER_EXPLICIT, SSOLoginSourceType.REVIEW)
        }
    }

    private fun showPostDeletedSnackbar() {
        val activity = activity ?: return
        GenericCustomSnackBar.showSnackBar(
                view = activity.window.decorView,
                context = activity,
                text = CommonUtils.getString(R.string.comment_deleted),
                duration = Snackbar.LENGTH_LONG,
                actionType = null,
                errorMessageClickedListener = null,
                action = null,
                customActionClickListener = null,
                bottomBarVisible = false
        ).show()
    }
}

/**
 * For showing disclaimer dialog in newsdetail screen
 *
 * @author satosh.dhanyamraju
 */
class DisclaimerDialogFragment : androidx.fragment.app.DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val url = arguments?.getString(Constants.BUNDLE_DISCLAIMER_URL)
        val view: View = LayoutInflater.from(activity).inflate(R.layout.layout_disclaimer, null, false)
        with(view) {
            findViewById<ImageView>(R.id.imageView).setOnClickListener {
                dismiss()
            }
            findViewById<NhWebView>(R.id.webview).also {
                url?.let { url ->
                    it.loadUrl(url)
                }
            }
        }
        val builder = AlertDialog.Builder(view.context).apply {
            setView(view)
        }
        return builder.create()
    }
}

enum class ViewAllViewType {
    DISCUSSION, PARENT, EMPTY, PROGRESS, ERROR
}

class DiscussionAdapter(val vm: ViewAllCommentsViewModel, val cvm: CardsViewModel, private val fragment: BaseSupportFragment,
                        private val listener: PostActions, val title: String?, val subTitle:
                        String?, private val lifecycleOwner: LifecycleOwner) :
        RecyclerView.Adapter<DiscussionVH>() {

    var dummyPost: CommonAsset? = null
    var isEmptyList: Boolean? = null
    val items = ArrayList<CommonAsset>()
    val oldItems = ArrayList<Any>()
    val newItems = ArrayList<Any>()
    var replyCount: List<ReplyCount>? = null
    var error: BaseError? = null

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiscussionVH {
        val layoutInflater = LayoutInflater.from(parent.context)
        when (viewType) {
            ViewAllViewType.PARENT.ordinal -> {
                val viewDataBinding: ViewDataBinding =
                        DataBindingUtil.inflate<ViewAllParentVhBinding>(layoutInflater,
                                R.layout.view_all_parent_vh, parent, false)
                viewDataBinding.setVariable(BR.vm, vm)
                viewDataBinding.setVariable(BR.isAllFilter, vm.isAllFilter)
                viewDataBinding.setVariable(BR.isCommentsFilter, vm.isCommentsFilter)
                viewDataBinding.setVariable(BR.isRepostFilter, vm.isRepostFilter)
                return DiscussionVH(viewDataBinding, lifecycleOwner)
            }
            ViewAllViewType.EMPTY.ordinal -> {
                val viewDataBinding: ViewDataBinding =
                        DataBindingUtil.inflate<ViewAllEmptyVhBinding>(layoutInflater,
                                R.layout.view_all_empty_vh, parent, false)
                return EmptyVH(viewDataBinding, lifecycleOwner)

            }
            ViewAllViewType.PROGRESS.ordinal -> {
                val viewDataBinding: ViewDataBinding =
                        DataBindingUtil.inflate<NewsDetailDiscussionLoaderVhBinding>(layoutInflater,
                                R.layout.news_detail_discussion_loader_vh, parent, false)
                return DiscussionVH(viewDataBinding, lifecycleOwner)
            }
            ViewAllViewType.ERROR.ordinal -> {
                val viewDataBinding: ViewDataBinding =
                        DataBindingUtil.inflate<ViewAllErrorVhBinding>(layoutInflater,
                                R.layout.view_all_error_vh, parent, false)
                viewDataBinding.setVariable(BR.baseError, error)
                viewDataBinding.also {
                    it.lifecycleOwner = fragment
                }
                return DiscussionVH(viewDataBinding, lifecycleOwner)
            }
            else -> {
                val viewDataBinding: ViewDataBinding =
                        DataBindingUtil.inflate<ViewAllCommentsVh2Binding>(layoutInflater,
                                R.layout.view_all_comments_vh_2, parent, false)
                viewDataBinding.setVariable(BR.vm, vm)
                return DiscussionVH(viewDataBinding, lifecycleOwner)
            }
        }
    }

    fun dataSetChange() {
        newItems.clear()
        if (dummyPost != null) {
            newItems.add(ViewAllViewType.PARENT.name)
        }
        newItems.addAll(items)

        if (items.isEmpty() && vm.discussionFetchRunning.get() == false && error == null) {
            newItems.add(ViewAllViewType.EMPTY.name)
        } else if (items.isEmpty() && vm.discussionFetchRunning.get() == true && error == null) {
            newItems.add(ViewAllViewType.PROGRESS.name)
        } else if (items.isEmpty() && error != null) {
            newItems.add(ViewAllViewType.ERROR.name)
        }

        val diffCallback = CommentsAdapterDiffUtilCallback(oldItems, newItems)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        oldItems.clear()
        oldItems.addAll(newItems)
        diffResult.dispatchUpdatesTo(this)
    }

    fun updateReplyCount(replyCount: List<ReplyCount>?) {
        this.replyCount = replyCount
        dataSetChange()
    }

    override fun getItemCount(): Int {
        return newItems.size
    }

    private fun dummyPostSize(): Int {
        return (if (dummyPost == null) 0 else 1)
    }

    private fun emptySize(): Int {
        return if (items.size == 0) 1 else 0
    }

    override fun onBindViewHolder(holder: DiscussionVH, position: Int) {
        if (position >= itemCount - 4) {
            vm.fetchNextPage()
        }
        if (dummyPost != null && position == 0) {
            holder.bindView(dummyPost!!, dummyPost, 0, error, title, subTitle, listener,
                    vm, cvm)
        } else if (isEmptyList == false) {
            val discussion = items[position - dummyPostSize()]
            val discussionId = discussion.i_id()
            val replyTs = discussion.i_counts()?.COMMENTS?.ts ?: 0
            var replyCountValue = 0
            replyCount?.forEach {
                if (it.parentId == discussionId && it.creationDate > replyTs) {
                    replyCountValue++
                }
            }

            holder.bindView(discussion, dummyPost, replyCountValue, error, title, subTitle,
                    listener, vm, cvm)
        } else if (holder is EmptyVH) {
            holder.bindView(vm)
        } else {
            holder.bindView(null, null, 0, error, title, subTitle, listener,
                    vm, cvm)
        }
    }

    override fun getItemViewType(position: Int): Int {
        when (newItems[position]) {
            ViewAllViewType.PARENT.name -> return ViewAllViewType.PARENT.ordinal
            ViewAllViewType.EMPTY.name -> return ViewAllViewType.EMPTY.ordinal
            ViewAllViewType.PROGRESS.name -> return ViewAllViewType.PROGRESS.ordinal
            ViewAllViewType.ERROR.name -> return ViewAllViewType.ERROR.ordinal
        }

        return ViewAllViewType.DISCUSSION.ordinal
    }

    override fun getItemId(position: Int): Long {
        return if (dummyPost != null && position == 0) {
            ViewAllViewType.PARENT.ordinal.toLong()
        } else if (items.isEmpty() && vm.discussionFetchRunning.get() == false && error == null) {
            ViewAllViewType.EMPTY.ordinal.toLong()
        } else if (items.isEmpty() && vm.discussionFetchRunning.get() == true && error == null) {
            ViewAllViewType.PROGRESS.ordinal.toLong()
        } else if (items.isEmpty() && error != null) {
            ViewAllViewType.ERROR.ordinal.toLong()
        } else {
            items[position - dummyPostSize()].i_id().hashCode().toLong()
        }
    }


}

open class DiscussionVH(val viewDataBinding: ViewDataBinding, lifecycleOwner: LifecycleOwner) : RecyclerView
.ViewHolder
(viewDataBinding.root), LifecycleObserver {

    init {
        lifecycleOwner.lifecycle.addObserver(this)
    }

    open fun bindView(card: CommonAsset?, parentCard: CommonAsset?, replyCount: Int,
                      error: BaseError?, title: String?, subtitle: String?, listener: PostActions,
                      vm: ViewAllCommentsViewModel, cvm: CardsViewModel) {
        viewDataBinding.setVariable(BR.discussion, card)
        viewDataBinding.setVariable(BR.card, parentCard)
        viewDataBinding.setVariable(BR.replyCount, replyCount)
        viewDataBinding.setVariable(BR.baseError, error)
        viewDataBinding.setVariable(BR.title, title)
        viewDataBinding.setVariable(BR.subtitle, subtitle)
        viewDataBinding.setVariable(BR.listener, listener)
        viewDataBinding.setVariable(BR.vm, vm)
        viewDataBinding.setVariable(BR.cvm, cvm)
        viewDataBinding.executePendingBindings()
    }
}

class EmptyVH(viewDataBinding: ViewDataBinding, lifecycleOwner: LifecycleOwner) : DiscussionVH
(viewDataBinding, lifecycleOwner) {

    fun bindView(vm: ViewAllCommentsViewModel) {
        itemView.findViewById<NHTextView>(R.id.empty_discussion).text = vm.getEmptyDiscussionText()
    }
}

data class CreateDummyPostPojo(val pe: PostEntity?, val fetchId: Long, val isDummy: Boolean)


class CommentsAdapterDiffUtilCallback(private val mOldItemList: List<Any>,
                                      private val mNewItemList: List<Any>) : DiffUtil.Callback() {

    override fun getOldListSize(): Int {
        return mOldItemList.size
    }

    override fun getNewListSize(): Int {
        return mNewItemList.size
    }

    private fun isComment(widget: Any): Boolean {
        return widget is CommonAsset
    }

    private fun isParent(widget: Any): Boolean {
        return ViewAllViewType.PARENT.name.equals(widget)
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val postItemOld = mOldItemList[oldItemPosition]
        val postItemNew = mNewItemList[newItemPosition]
        return postItemOld.equals(postItemNew)
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val postItemOld = mOldItemList[oldItemPosition]
        val postItemNew = mNewItemList[newItemPosition]

        if (isComment(postItemNew) && isComment(postItemOld)) {
            val oldDiscussion = postItemOld as CommonAsset
            val newDiscussion = postItemNew as CommonAsset
            return oldDiscussion.i_id().equals(newDiscussion.i_id())
        }
        if (isParent(postItemNew) && isParent(postItemOld)) {
            return false
        }

        return postItemOld.equals(postItemNew)
    }

}