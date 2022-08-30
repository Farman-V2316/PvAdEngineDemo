package com.newshunt.appview.common.postcreation.view.activity

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.Spannable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.util.Patterns
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.core.view.updateLayoutParams
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.newshunt.appview.R
import com.newshunt.appview.common.postcreation.CreatePostModule
import com.newshunt.appview.common.postcreation.DaggerCreatePostViewComponent
import com.newshunt.appview.common.postcreation.addImageToGallery
import com.newshunt.appview.common.postcreation.analytics.helper.CreatePostAnalyticsHelper
import com.newshunt.appview.common.postcreation.createImageFile
import com.newshunt.appview.common.postcreation.view.adapter.GridBuilderImageAdapter
import com.newshunt.appview.common.postcreation.view.adapter.GridItemRemoveCallback
import com.newshunt.appview.common.postcreation.view.customview.CreatePostWalkThroughDialog
import com.newshunt.appview.common.postcreation.view.customview.GridSpacingItemDecoration
import com.newshunt.appview.common.postcreation.view.customview.NHCPMention
import com.newshunt.appview.common.postcreation.view.customview.OEmbedView
import com.newshunt.appview.common.postcreation.view.customview.PollView
import com.newshunt.appview.common.postcreation.view.customview.PollViewRemoveCallback
import com.newshunt.appview.common.postcreation.view.customview.PostCreationDiscardDialog
import com.newshunt.appview.common.postcreation.view.customview.PostCreationLocationView
import com.newshunt.appview.common.postcreation.view.customview.RepostView
import com.newshunt.appview.common.postcreation.view.customview.VIEW_TYPE
import com.newshunt.appview.common.postcreation.view.helper.PostConstants
import com.newshunt.appview.common.postcreation.view.helper.PostConstants.Companion.CAMERA_IMAGE_PATH
import com.newshunt.appview.common.postcreation.view.helper.PostConstants.Companion.CAMERA_REQUEST
import com.newshunt.appview.common.postcreation.view.helper.PostConstants.Companion.LOCATION_REQUEST
import com.newshunt.appview.common.postcreation.view.helper.PostConstants.Companion.PICK_IMAGE_MULTIPLE
import com.newshunt.appview.common.postcreation.view.helper.PostConstants.Companion.PRIVACY_STATE_REQUEST
import com.newshunt.appview.common.postcreation.view.helper.TextWatcherPattern
import com.newshunt.appview.common.postcreation.view.service.UploadJobService
import com.newshunt.appview.common.postcreation.viewmodel.CreatePostViewModel
import com.newshunt.appview.databinding.ActivityPostCreateBinding
import com.newshunt.common.helper.common.AndroidUtils
import com.newshunt.common.helper.common.BusProvider
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.font.FontHelper
import com.newshunt.common.helper.preference.AppUserPreferenceUtils
import com.newshunt.common.helper.preference.GenericAppStatePreference
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.common.view.customview.fontview.NHEditText
import com.newshunt.dataentity.analytics.entity.AnalyticsParam
import com.newshunt.dataentity.analytics.referrer.NHGenericReferrerSource
import com.newshunt.dataentity.analytics.referrer.NhGenericReferrer
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.analytics.section.NhAnalyticsEventSection
import com.newshunt.dataentity.common.asset.CreatePostUiMode
import com.newshunt.dataentity.common.asset.Format
import com.newshunt.dataentity.common.asset.PostCurrentPlace
import com.newshunt.dataentity.common.asset.PostMeta
import com.newshunt.dataentity.common.asset.PostPrivacy
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.common.model.entity.PermissionResult
import com.newshunt.dataentity.common.model.entity.SearchRequestType
import com.newshunt.dataentity.dhutil.model.entity.asset.ImageDetail
import com.newshunt.dataentity.model.entity.GroupInfo
import com.newshunt.dataentity.search.SearchActionType
import com.newshunt.dataentity.search.SearchPayloadContext
import com.newshunt.dataentity.search.SearchSuggestionItem
import com.newshunt.dataentity.search.SearchSuggestionType
import com.newshunt.dataentity.social.entity.CreatePostID
import com.newshunt.dataentity.social.entity.MAX_IMAGE_COUNT
import com.newshunt.deeplink.navigator.CommonNavigator
import com.newshunt.deeplink.navigator.NewsNavigator
import com.newshunt.dhutil.analytics.AnalyticsHelper
import com.newshunt.dhutil.analytics.AnalyticsHelper2
import com.newshunt.dhutil.bundleOf
import com.newshunt.dhutil.commons.listener.ReferrerProviderlistener
import com.newshunt.dhutil.dimensionFromAttribute
import com.newshunt.dhutil.getFromStream
import com.newshunt.dhutil.helper.behavior.BottomSheetLockBehaviour
import com.newshunt.dhutil.helper.common.DefaultRationaleProvider
import com.newshunt.dhutil.helper.preference.AppStatePreference
import com.newshunt.dhutil.helper.theme.ThemeUtils
import com.newshunt.dhutil.take1
import com.newshunt.dhutil.transaction
import com.newshunt.news.di.SearchModule
import com.newshunt.news.model.helper.NotificationActionExecutionHelper
import com.newshunt.news.model.sqlite.SearchDatabase
import com.newshunt.news.model.sqlite.SocialDB
import com.newshunt.news.model.usecase.CpImageInsertUseCase
import com.newshunt.news.model.usecase.toMediator2
import com.newshunt.news.util.NewsConstants
import com.newshunt.news.view.activity.NewsBaseActivity
import com.newshunt.news.view.fragment.PhotosModule
import com.newshunt.news.view.fragment.PresearchFragment
import com.newshunt.news.view.fragment.SuggestionListener
import com.newshunt.permissionhelper.Callbacks.PermissionRationaleProvider
import com.newshunt.permissionhelper.PermissionAdapter
import com.newshunt.permissionhelper.PermissionHelper
import com.newshunt.permissionhelper.utilities.Permission
import com.newshunt.sdk.network.NetworkSDK
import com.newshunt.sdk.network.internal.NetworkSDKUtils
import com.newshunt.sso.SSO
import com.newshunt.sso.model.entity.LoginMode
import com.newshunt.sso.model.entity.SSOLoginSourceType
import com.squareup.otto.Subscribe
import java.io.File
import java.io.IOException
import javax.inject.Inject


class CreatePostActivity : NewsBaseActivity(), CreatePostView, SuggestionListener,
        PollViewRemoveCallback, OEmbedView.OgRemoveCallback, ReferrerProviderlistener {
    private val TAG = CreatePostActivity::class.java.simpleName
    private var gridAdapter: GridBuilderImageAdapter? = null
    private lateinit var mBinding: ActivityPostCreateBinding
    @Inject
    lateinit var vf: CreatePostViewModel.Factory
    @Inject
    lateinit var imgusecase: CpImageInsertUseCase
    private lateinit var viewModel: CreatePostViewModel
    private lateinit var config: CreatePostUiConfig
    private var parentId: String = Constants.EMPTY_STRING
    private var parentPostId: String = Constants.EMPTY_STRING
    private var sourceId: String = Constants.EMPTY_STRING
    private var sourceType: String = Constants.EMPTY_STRING
    private var bsBehavior: BottomSheetBehavior<LinearLayout>? = null
    private var suggestionFrag: PresearchFragment? = null
    private var cameraImagePath: String? = null
    //With Poll, not allowed even once
    private var pMeta: PostMeta? = PostMeta(PostPrivacy.PUBLIC, allowComments = true)
    private var isOgViewAllowed = true
    //If poll repost, then only text allowed, disable everything else
    private var isPollRepost = false
    private var isRepost = false
    // For analytics
    private var referrer: PageReferrer? = null
    private var referrerRaw: String? = null
    private var referrerFlow: PageReferrer?= null
    private var currentPageReferrer: PageReferrer? = null
    private var isCameraHwAvailable = true
    private val bsvisibilityLiveData: MutableLiveData<Boolean> = MutableLiveData()
    private var isInternalDeeplink: Boolean = false
    private var isSystemBackKeyPressed = true
    private var isExternalShare = false
    private var isImageShare = false
    private var groupInfo: GroupInfo? = null

    companion object{
        const val EVENT_TYPE_VIEW = "post_cm_viewed"
    }

    override fun onCreate(bundle: Bundle?) {
        super.onCreate(bundle)
        mBinding = DataBindingUtil.setContentView(
                this,
                R.layout.activity_post_create
        )
        parentPostId = intent.getStringExtra(Constants.BUNDLE_POST_ID) ?: ""
        parentId = intent.getStringExtra(Constants.BUNDLE_PARENT_ID) ?: ""
        sourceId = intent.getStringExtra(Constants.BUNDLE_SOURCE_ID) ?: ""
        sourceType = intent.getStringExtra(Constants.BUNDLE_SOURCE_TYPE) ?: ""
        groupInfo = intent.getSerializableExtra(Constants.BUNDLE_GROUP_INFO) as? GroupInfo?

        setPageReferrerFromIntent()
        val isLocationEnable = PreferenceManager.getPreference(
                AppStatePreference.POST_CREATE_LOCATION_ENABLE, true)

        if (packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY).not()) {
            isCameraHwAvailable = false
        }

        config = UIConfigBuilder(
                intent.getSerializableExtra(Constants.BUNDLE_MODE) as? CreatePostUiMode ?: CreatePostUiMode.POST,
                isCameraHwAvailable, isLocationEnable
        ).build()

        //20.x Directly disabling the poll
        config.enablePoll = false

        if(config.mode == CreatePostUiMode.POST && !AppUserPreferenceUtils.getIsCreatePostWalkthroughShown()){
            // init the walkthrough dialog here.
            val cpWalkThroughDialog = CreatePostWalkThroughDialog.newInstance()
            cpWalkThroughDialog.show(this.supportFragmentManager, "CreatePostWalkThroughDialog")
            AppUserPreferenceUtils.setIsCreatePostWalkthroughShown(true)
            AnalyticsHelper2.logFeatureNudgeEvent(EVENT_TYPE_VIEW)
        }
        CreatePostAnalyticsHelper.logCreatePostHomeEvent(referrerFlow!!, config.mode)
        DaggerCreatePostViewComponent
                .builder()
                .createPostModule(CreatePostModule(postId = parentId, socialDb = SocialDB.instance()))
                .searchModule(SearchModule(Constants.SEARCH_CONTEXT_NEWSDETAIL, getString(R.string.recent_header_text),
                        getString(R.string.trending_header_text), SearchDatabase.instance()
                        , SearchRequestType.CREATE_POST,generateSearchContext()))
                .photosModule(PhotosModule(CommonUtils.getApplication(),
                        socialDB = SocialDB.instance(), postId = parentId))
                .build().inject(this)

        viewModel = ViewModelProviders.of(this, vf)[CreatePostViewModel::class.java]
        viewModel.attachView(this)
        viewModel.setCurrentPageReferrer(currentPageReferrer!!)
        viewModel.start(intent)
        mBinding.vm = viewModel
        mBinding.vi = this


        mBinding.config = config
        mBinding.lifecycleOwner = this
        mBinding.pm = PostMeta(PostPrivacy.PUBLIC, true)
        mBinding.executePendingBindings()

        initViews()

        //handle the repost intent
        if (config.mode == CreatePostUiMode.REPOST) {
            //Initially all options disable. once repost type is known, enable options based on type
            updateAttachmentButtons(enableGallery = false, enablePoll = false, isAllowed = false,
                    enablePostButton = false)
            if (parentId.isNotEmpty()) {
                val repostView = mBinding.cpAttachmentContainer.getViewOfType(VIEW_TYPE.REPOST_VIEW)
                        as? RepostView ?: kotlin.run {
                    //20.x If repost call for disabling the media upload.
                    isRepost = true
                    initRepostView()
                }
                repostView.showRepostLoader(false)
                viewModel.fetchRepostItemFromDB().take1().observe(this, Observer { it ->
                    if (it == null) {
                        viewModel.detailsAPIUsecase.status().observe(this, repostStatusObserver)
                        viewModel.fetchRepostItemFromNetwork().observe(this, Observer {
                            if (it != null) {
                                // If the reponse is not null form the networke.
                                repostView.setRepostData(it)
                                isPollRepost = it.i_format() == Format.POLL
                                viewModel.addRepostCard(it)
                                updateAttachmentButtons(enablePoll = false)
                            } else {
                                FontHelper.showCustomFontToast(this, "Repost preview failed! Go " +
                                        "ahead with repost. we'll link it for you" , Toast.LENGTH_SHORT)
                                repostView.hideView()
                                updateAttachmentButtons(enablePoll = false)
                            }
                        })
                    } else {
                        // if not draw the repost view from the DB reponse.
                        repostView.setRepostData(it)
                        isPollRepost = it.i_format() == Format.POLL
                        viewModel.addRepostCard(it)
                        updateAttachmentButtons(enablePoll = false)
                    }
                })
            }
        } else if (config.mode == CreatePostUiMode.COMMENT || config.mode == CreatePostUiMode.REPLY) {
            isOgViewAllowed = false
        }

        isImageShare = intent.getFromStream().isNullOrEmpty().not()

        //handle share intent
        addUrlMatcher()
        addTextChangeListener()
        mBinding.cpEditText.subscribe(this, viewModel.hashTagViewModel.getTagObserver())
        mBinding.cpEditText.addKeyBoardCallback(NHEditText.Callback {
             showSuggestionView(false)
        })
        mBinding.cpEditText.bsVisibility = bsvisibilityLiveData
        handleIntent(intent)

        viewModel.cpIdData.observe(this, Observer {
            //Not adding cpId check as it may be normal text as well instead of OG type
            mBinding.cpEditText.setText(intent)
        })

        viewModel.imgData.observe(this, Observer {
            updateImageGridView(it)
        })

        viewModel.privacyData.observe(this, Observer {
            mBinding.pm = it
            mBinding.executePendingBindings()
        })

        viewModel.locationData.observe(this, Observer {
            updateLocationView(it)
            PreferenceManager.savePreference(
                    AppStatePreference.POST_CREATE_DEFAULT_LOCATION, CommonUtils.GSON.toJson(it)
            )
        })

        // click listener rather than binding. To get data from different sources
        mBinding.toolBar.postCreationBtn.setOnClickListener {
            checkLoginAndPost()
        }

        viewModel.embedUseCase.status().observe(this, embedStatusObserver)

        viewModel.embedUseCase.data().observe(this, Observer {
            val oEmbedView = mBinding.cpAttachmentContainer.getViewOfType(VIEW_TYPE.OG_VIEW) as?
                    OEmbedView ?: kotlin.run{ initOembedView() }
            if(it.isFailure) {
                viewModel.embedUseCase.status().removeObserver(embedStatusObserver)
                mBinding.cpAttachmentContainer.removeView(oEmbedView)
                return@Observer
            }
            val response = it.getOrNull() ?: kotlin.run {
                oEmbedView.hideView()
                updateAttachmentButtons()
                return@Observer
            }
            oEmbedView.setOEmbedResponse(response)
        })

        mBinding.toolBar.actionbarBackButton.setOnClickListener {
            isSystemBackKeyPressed = false
            onBackPressed()
        }

        mBinding.cpBottomToolbarContainer.createPostActionLocation.visibility =
                if (isLocationEnable) View.VISIBLE else View.GONE
    }

    private val embedStatusObserver = Observer<Boolean>{
        val oEmbedView = mBinding.cpAttachmentContainer.getViewOfType(VIEW_TYPE.OG_VIEW) as?
                OEmbedView ?: kotlin.run{ initOembedView() }
        oEmbedView.setShowLoader(it)
    }

    private val repostStatusObserver = Observer<Boolean> {
        val repostView = mBinding.cpAttachmentContainer.getViewOfType(VIEW_TYPE.REPOST_VIEW) as?
                RepostView ?: kotlin.run {
            //20.x If repost call for disabling the media upload.
            isRepost = true
            initRepostView()
        }
        repostView.showRepostLoader(it)
    }

    override fun onStart() {
        super.onStart()
        requestEditFocus()
    }

    private fun initViews() {
        val text = CommonUtils.getString(
                R.string.post_create_hash_at_the_rate_hint,
                Constants.HASH_CHARACTER,
                Constants.AT_SYMBOL
        )
        val spannableString = SpannableString(text)
        val hashIndex = text.indexOf(Constants.HASH_CHARACTER)
        val atIndex = text.indexOf(Constants.AT_SYMBOL)
        val color = ThemeUtils.getThemeColorByAttribute(this, R.attr.cp_hash_at_text_color)
        spannableString.setSpan(
                ForegroundColorSpan(color), hashIndex, hashIndex + 1, Spannable
                .SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannableString.setSpan(
                ForegroundColorSpan(color), atIndex, atIndex + 1, Spannable
                .SPAN_EXCLUSIVE_EXCLUSIVE
        )
        mBinding.createPostTutText.text = spannableString

        mBinding.cpEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                mBinding.cpBottomToolbarContainer.createPostActionHash.visibility = View.VISIBLE
                mBinding.cpBottomToolbarContainer.createPostActionAt.visibility = View.VISIBLE
            } else {
                mBinding.cpBottomToolbarContainer.createPostActionHash.visibility = View.INVISIBLE
                mBinding.cpBottomToolbarContainer.createPostActionAt.visibility = View.INVISIBLE
            }
        }
    }

    private fun requestEditFocus() {
        mBinding.cpEditText.post {
            mBinding.cpEditText.requestFocus()
        }
    }

    private fun generateSearchContext() : SearchPayloadContext {
        intent.let {
            val entity = it.getStringExtra(AnalyticsParam.ENTITY_TYPE.name) ?: ""
            val entityId = it.getStringExtra(AnalyticsParam.ENTITY_ID.name) ?: ""
            val action = SearchActionType.getsearchActionType(config.mode)
            return SearchPayloadContext(
                    entityType = entity,
                    entityId = entityId,
                    action = action.typeName,
                    postId = parentId,
                    parentPostId = parentPostId)
        }
    }

    private fun handleIntent(intent: Intent?) {
        intent ?: return
        val action = intent.action
        action?.let {
            if (Intent.ACTION_SEND.equals(action, ignoreCase = true) ||
                Intent.ACTION_SEND_MULTIPLE.equals(action, ignoreCase = true)) {
                isExternalShare = true
            }
        }
        val handle = intent.getSerializableExtra(Constants.BUNDLE_CREATE_POST_TAG_DATA) as?
                SearchSuggestionItem
        handle?.also {
            when (handle.typeName) {
                SearchSuggestionType.HANDLE.type -> {
                    mBinding.cpEditText.setText(Constants.AT_SYMBOL)
                    suggestionClicked(
                            0, SearchSuggestionItem(
                            suggestion = if (handle.suggestion.trim().startsWith(Constants.AT_SYMBOL))
                                handle.suggestion.substring(1, handle.suggestion.length)
                            else handle.suggestion,
                            typeName = handle.typeName,
                            name = handle.name ?: handle.suggestion, itemId = handle.itemId
                    )
                    )
                }
                SearchSuggestionType.HASHTAG.type -> {
                    mBinding.cpEditText.setText(Constants.HASH_CHARACTER)
                    suggestionClicked(
                            0, SearchSuggestionItem(
                            suggestion = if (handle.suggestion.trim().startsWith(Constants.HASH_CHARACTER))
                                handle.suggestion.substring(1, handle.suggestion.length)
                            else handle.suggestion,
                            typeName = handle.typeName,
                            name = handle.name ?: handle.suggestion, itemId = handle.itemId
                    )
                    )
                }
            }
        }
    }

    private fun setPageReferrerFromIntent() {
        val bundle = intent.extras
        if (intent.action == Intent.ACTION_SEND) {
            referrerFlow = PageReferrer(NhGenericReferrer.CP_SOCIAL_LINK_SHARE)
        } else {
            if (bundle != null) {
                referrerFlow = bundle.get(NewsConstants.BUNDLE_ACTIVITY_REFERRER) as PageReferrer?
                if (referrerFlow != null) {
                    if (CommonNavigator.isFromNotificationTray(referrerFlow) ||
                            CommonNavigator.isDeeplinkReferrer(referrerFlow)) {
                        AnalyticsHelper.updateAppState(referrerFlow)
                    }
                }
                isInternalDeeplink = bundle.getBoolean(Constants.IS_INTERNAL_DEEPLINK, false)
            }
        }
        if (referrerFlow == null) {
            referrerFlow = PageReferrer(NhGenericReferrer.ORGANIC)
            referrerFlow?.referrerSource = NHGenericReferrerSource.CREATE_POST_VIEW
        }
        referrer = PageReferrer(referrerFlow)
        referrerRaw = bundle?.getString(Constants.REFERRER_RAW)
        currentPageReferrer = PageReferrer(NhGenericReferrer.CREATE_POST_HOME)
    }

    private fun updateImageGridView(imageList: Array<ImageDetail>) {
        if(imageList.isEmpty()) return
        if (mBinding.cpAttachmentContainer.getViewOfType(VIEW_TYPE.IMAGE_GRID) == null) {
            val imageRecyclerView = RecyclerView(this)
            imageRecyclerView.apply {
                layoutManager = GridLayoutManager(context, 3)
                gridAdapter = GridBuilderImageAdapter(viewModel.cpId, imgDao = imgusecase.toMediator2()) {
                    pickImageFromGallery()
                }
                gridAdapter?.updateList(*imageList)
                gridAdapter?.setGridItemRemoveCallback(object : GridItemRemoveCallback {
                    override fun onGridItemRemoved() {
                        updateImageAttachmentButton()
                    }
                })
                adapter = gridAdapter
            }
            imageRecyclerView.clipToPadding = false
            val padding =  resources.getDimensionPixelSize(R.dimen.poll_grid_item_spacing)
            imageRecyclerView.setPadding(padding, 0,padding,padding)
            imageRecyclerView.addItemDecoration(GridSpacingItemDecoration(this,
                    R.dimen.poll_grid_item_spacing))

            mBinding.cpAttachmentContainer.addViewOfType(VIEW_TYPE.IMAGE_GRID, imageRecyclerView)
        } else {
            gridAdapter?.updateList(*imageList)
        }
        updateImageAttachmentButton()
    }


    private fun updateLocationView(obj: PostCurrentPlace?) {
        val locationView =
                mBinding.cpAttachmentContainer.getViewOfType(VIEW_TYPE.LOCATION_VIEW) as?
                        PostCreationLocationView
        if (obj == null) {
            if (locationView != null) {
                mBinding.cpAttachmentContainer.removeView(locationView)
            }
            return
        }
        if (locationView != null) {
            locationView.setLocationResponse(obj)
        } else {
            val view = PostCreationLocationView(this)
            view.setLocationResponse(obj)
            view.setOnClickListener {
                startLocationActivity()
            }
            mBinding.cpAttachmentContainer.addViewOfType(VIEW_TYPE.LOCATION_VIEW, view)
        }
    }

    private fun initOembedView(): OEmbedView {
        val oEmbedView = OEmbedView(this)
        mBinding.cpAttachmentContainer.addViewOfType(VIEW_TYPE.OG_VIEW, oEmbedView)
        oEmbedView.setOgRemoveCallback(this)
        return oEmbedView
    }

    private fun initRepostView(): RepostView {
        var repostView = mBinding.cpAttachmentContainer.getViewOfType(VIEW_TYPE.REPOST_VIEW) as?
                RepostView
        if (repostView == null) {
            repostView = RepostView(this)
            mBinding.cpAttachmentContainer.addViewOfType(VIEW_TYPE.REPOST_VIEW, repostView)
        }
        return repostView
    }

//    @VisibleForTesting
//    internal fun initRepost(repostEntity: PostEntity) {
//        initRepostView(repostEntity)
//    }

    private fun checkLoginAndPost() {
        if (SSO.getInstance().isLoggedIn(false)) {
            publishPost()
        } else {
            observeLoginChanges()
            SSO.getInstance().login(this, LoginMode.USER_EXPLICIT, SSOLoginSourceType.CREATE_POST)
        }
    }

    override fun getPreFragmentManager(): FragmentManager = supportFragmentManager

    override fun initSuggestions() {
        if (bsBehavior == null) {
            bsBehavior = BottomSheetLockBehaviour.from(mBinding.bottomsheetContainer).let{
                        it.setPeekHeight(0)
                        it.isHideable = true
                        it.swipeEnabled = false
                        it
                    }
        }
        if (suggestionFrag == null) {
            suggestionFrag = PresearchFragment.newInstance(referrer = PageReferrer(),
                    showToolbar = false).apply {
                setCallback(this@CreatePostActivity)
                setSearchInterface(viewModel.hashTagViewModel)
            }
            getPreFragmentManager().transaction {
                add(R.id.bottomsheet_container, suggestionFrag as Fragment)
            }
        }

        bsBehavior?.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(p0: View, p1: Float) {

            }

            override fun onStateChanged(p0: View, p1: Int) {
                when (p1) {
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        val height = mBinding.cpEditText.paddingTop +
                                mBinding.cpEditText.lineHeight * if (mBinding.cpEditText.lineCount > 4)
                                4 else mBinding.cpEditText.lineCount
                        mBinding.postScrollView.updateLayoutParams<RelativeLayout.LayoutParams> {
                            this.height = height + resources.getDimensionPixelSize(R.dimen.create_post_edit_text_search_padding)
                            this.setMargins(0, p0.context.dimensionFromAttribute(R.attr.actionBarSize),0,0)
                        }
                        bsvisibilityLiveData.postValue(p1 == BottomSheetBehavior.STATE_EXPANDED)
                        mBinding.postScrollView.forceLayout()
                        p0.postDelayed({
                            val scrollPos = getCursorPos()
                            val layout = mBinding.cpEditText.layout
                            if (layout != null) {
                                val lineTop = layout!!.getLineTop(scrollPos)
                                mBinding.postScrollView.scrollTo(0, lineTop)
                            }
                        },50)

                    }
                    BottomSheetBehavior.STATE_HIDDEN, BottomSheetBehavior.STATE_COLLAPSED -> {
                        mBinding.postScrollView.updateLayoutParams {
                            height = ViewGroup.LayoutParams.MATCH_PARENT
                        }
                        mBinding.postScrollView.forceLayout()
                        p0.postDelayed({
                            if(p1 == BottomSheetBehavior.STATE_HIDDEN || p1 == BottomSheetBehavior.STATE_COLLAPSED) {
                                suggestionFrag?.resetListing()
                            }
                        },100)
                        bsvisibilityLiveData.postValue(false)
                    }
                    else -> {
                        bsvisibilityLiveData.postValue(false)
                    }
                }
            }
        })

        mBinding.cpEditText.visibility().observe(this, Observer {
            if (it) return@Observer
            AndroidUtils.getMainThreadHandler().postDelayed({
                showSuggestionView(it)
            },500)
        })
    }

    override fun showSuggestionView(state: Boolean) {
        Logger.d(TAG, "suggestion called")
        if (state && mBinding.bottomSheetContainerCoordinator.visibility != View.VISIBLE) {
            scrollToCursor()
        }
        if (!state) Logger.d(TAG, "Bottom sheet visibility gone")
        mBinding.bottomSheetContainerCoordinator.visibility = if (state) View.VISIBLE else View.GONE
        AndroidUtils.getMainThreadHandler().postDelayed({
            bsBehavior?.state = if (state) BottomSheetBehavior.STATE_EXPANDED
            else BottomSheetBehavior.STATE_COLLAPSED
        }, 200)
    }

    override fun suggestionClicked(pos: Int, item: SearchSuggestionItem) {
        super.suggestionClicked(pos, item)
        mBinding.cpEditText.insertMention(NHCPMention(item))
        bsBehavior?.state = BottomSheetBehavior.STATE_HIDDEN
        mBinding.bottomSheetContainerCoordinator.visibility = View.GONE
        AndroidUtils.showKeyBoard(baseContext, mBinding.cpEditText)
    }

    override fun onHandleClicked() {
        mBinding.cpEditText.appendText(Constants.AT_SYMBOL)
    }

    override fun onHashClicked() {
        mBinding.cpEditText.appendText(Constants.HASH_CHARACTER)
    }

    private fun observeLoginChanges() {
        SSO.getInstance().userDetailsLiveData.observe(this, Observer {
            if (SSO.getInstance().isLoggedIn(false)) {
                publishPost()
            }
        })
    }

    private fun publishPost() {
        if ((config.mode == CreatePostUiMode.COMMENT || config.mode == CreatePostUiMode.REPLY) &&
            !NetworkSDKUtils.isNetworkAvailable(NetworkSDK.getContext())
        ) {
            Toast.makeText(
                this, CommonUtils.getString(R.string.error_no_connection),
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        val pView = (mBinding.cpAttachmentContainer.getViewOfType(VIEW_TYPE.POLL_VIEW) as? PollView)
        viewModel.createPost(
                body = mBinding.cpEditText.getFormattedText(),
                p_duration = pView?.getPVPollDuration() ?: -1,
                pollOptions = *pView?.getPVPollOptionList()?.toTypedArray() ?: emptyArray()
        )

        if(pView == null || pView.getPVPollOptionList()?.isEmpty() == true) {
            viewModel.cpCreationUseCase.data().observe(this, Observer {
                //invoke upload service. pass in post_id by Bundle
                if(it.isSuccess && it.getOrNull()?.operation == CreatePostID.CP_OP.UPDATE) {
                    callUploadService()
                    callPostEvent()
                }
            })
        }

        viewModel.cpPollUseCase.data().take1().observe(this, Observer {
            if (it.isSuccess) {
                callUploadService()
            }
        })
        if (!isInternalDeeplink && NewsNavigator.shouldNavigateToHome(this, referrerFlow,
                false,referrerRaw)) {
            NewsNavigator.navigateToHomeOnLastExitedTab(this, referrer)
        }


        NotificationActionExecutionHelper.executePendingAction(GenericAppStatePreference.NEXT_COMMENT_CREATION)
        finish()
    }

    private fun callPostEvent() {
        viewModel.cpReadUseCase.apply {
            execute(bundleOf(CpImageInsertUseCase.POST_ID to viewModel.cpId))
        }.data().observe(this, Observer {
            if (it.isSuccess) {
                CreatePostAnalyticsHelper.logCreatePostPublishEvent(currentPageReferrer!!,
                        referrerFlow!!, it.getOrNull(), config.mode, parentId, parentPostId,
                        sourceId, sourceType,groupInfo)
            }
        })
    }

    private fun callUploadService() {
        viewModel.cpPollUseCase.status().take1().observe(this, Observer {status: Boolean ->
            if(status) callPostEvent()
        })
        UploadJobService.enqueueWork(bundle = bundleOf(UploadJobService.POST_CP_ID to viewModel
                .cpId, UploadJobService.BUNDLE_PAGE_REFERRER to currentPageReferrer,
            UploadJobService.IS_EXTERNAL_SHARE to isExternalShare))
    }

    private fun addUrlMatcher() {
        val urlPatternMatcher = TextWatcherPattern()
        urlPatternMatcher.addMatcher(Patterns.WEB_URL) {
            if (this.first == Patterns.WEB_URL) {
                //Do not fetch if OG view is already added
                val oembedView = mBinding.cpAttachmentContainer.getViewOfType(VIEW_TYPE.OG_VIEW) as?
                        OEmbedView
                if (!isImageShare && isOgViewAllowed && oembedView == null) {
                    viewModel.fetchOmbed(this.second[0])
                    viewModel.embedUseCase.status().observe(this@CreatePostActivity, embedStatusObserver)
                    updateAttachmentButtons(enableGallery = false, enablePoll = false,
                            isAllowed = false)
                }
            }
        }
        mBinding.cpEditText.addTextChangedListener(urlPatternMatcher)
    }

    private fun addTextChangeListener() {
        mBinding.cpEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                updatePostButton()
                if (mBinding.cpEditText.selectionStart > 1) {
                    val hashOrHandle = s.toString()[mBinding.cpEditText.selectionStart - 1]
                    if (hashOrHandle == '#' || hashOrHandle == '@') return
                }

                if(bsBehavior?.state == BottomSheetBehavior.STATE_EXPANDED) return

                val scrollPos = getCursorPos()
                val layout = mBinding.cpEditText.layout
                if (layout != null) {
                    val lineTop = layout!!.getLineTop(scrollPos)
                    mBinding.postScrollView.scrollTo(0, lineTop + 10)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {


            }


        })
    }

    private fun getCursorPos(): Int {
        val selectionStart = mBinding.cpEditText.getSelectionStart()
        val layout = mBinding.cpEditText.layout
        return if (layout != null && selectionStart != -1) {
            layout!!.getLineForOffset(selectionStart)
        } else -1
    }

    override fun checkPermission(rc: Int, permission: MutableList<Permission>) {
        val provider = DefaultRationaleProvider()
        when(rc){
            CAMERA_REQUEST,
            PICK_IMAGE_MULTIPLE ->{
                provider.setStorageSubtitle(CommonUtils.getString(R.string.cp_permission_storage_access))
                provider.setStorageDesc(CommonUtils.getString(R.string.cp_permission_storage_rationale))
            }
            LOCATION_REQUEST ->{
                provider.setLocationDesc(CommonUtils.getString(R.string.cp_permission_location_rationale))
            }
        }
        PostPermissionAdapter(rc, this, provider, permission) {
            when (it) {
                PICK_IMAGE_MULTIPLE -> {
                    pickImageFromGallery()
                }
                CAMERA_REQUEST -> {
                    startCamera()
                }
                LOCATION_REQUEST -> {
                    startLocationActivity()
                }
            }
        }
    }

    private fun startLocationActivity() {
        val intent = Intent(this@CreatePostActivity, PostLocationActivity::class.java)
        val locationView =
                mBinding.cpAttachmentContainer.getViewOfType(VIEW_TYPE.LOCATION_VIEW) as?
                        PostCreationLocationView
        locationView?.getLocationResponse()?.also {
            intent.putExtra(PostConstants.POST_SELECTED_LOCATION, it)
        }
        startActivityForResult(intent, LOCATION_REQUEST)
    }


    override fun startSettingActivity() {
        startActivityForResult(Intent(this, PostPrivacyActivity::class.java).apply {
            putExtra(POST_META_RESULT, pMeta)
        }, PRIVACY_STATE_REQUEST)
    }


    override fun getProvidedReferrer(): PageReferrer? {
        return currentPageReferrer
    }

    override fun getReferrerEventSection(): NhAnalyticsEventSection {
        return NhAnalyticsEventSection.APP
    }

    override fun getLatestPageReferrer(): PageReferrer {
        return currentPageReferrer!!
    }


    private fun startCamera() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(packageManager)?.also {
                // Create the File where the photo should go
                val photoFile: File? = try {
                    createImageFile(this)
                } catch (ex: IOException) {
                    null
                }
                photoFile?.also {
                    cameraImagePath = it.path
                    val path = FileProvider.getUriForFile(
                            this,
                            "$packageName.fileprovider",
                            File(it.toString())
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, path)
                    startActivityForResult(takePictureIntent, CAMERA_REQUEST)
                }
            }
        }
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        startActivityForResult(intent, PICK_IMAGE_MULTIPLE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        try {
            var intent = data
            if (resultCode == Activity.RESULT_OK) {
                when (requestCode) {
                    CAMERA_REQUEST -> {
                        intent = Intent()
                        intent.putExtra(CAMERA_IMAGE_PATH, cameraImagePath)
                        cameraImagePath?.also {
                            addImageToGallery(this, cameraImagePath)
                        }
                    }
                    PRIVACY_STATE_REQUEST -> {
                        pMeta = intent?.getSerializableExtra(POST_META_RESULT) as? PostMeta
                    }
                }
            }
            viewModel.intentFromActivityResult(requestCode, resultCode, intent)
            super.onActivityResult(requestCode, resultCode, intent)
        } catch (throwable: Throwable) {
            Logger.caughtException(throwable)
        }
    }

    override fun onStop() {
        super.onStop()
        //discard entries created in table
        if(isFinishing) {
            viewModel.stop()
            suggestionFrag?.let {
                getPreFragmentManager().beginTransaction()
                        .remove(it).commitAllowingStateLoss()
            }
            suggestionFrag == null
        }
    }

    private fun updateImageAttachmentButton() {
        val count = gridAdapter?.basicItemCount ?: 0
        when {
            count == 0 -> {
                val view = mBinding.cpAttachmentContainer.getViewOfType(VIEW_TYPE.IMAGE_GRID)
                view?.let { mBinding.cpAttachmentContainer.removeView(view) }
                updateAttachmentButtons()
            }
            count < gridAdapter?.getAttachmentSize() ?: MAX_IMAGE_COUNT -> {
                updateAttachmentButtons(enableGallery = true,
                        enablePoll = false, isAllowed = false)
            }
            else -> updateAttachmentButtons(enableGallery = false, enablePoll = false,
                    isAllowed = false)
        }
    }

    private fun updateAttachmentButtons(enableGallery: Boolean = true, enablePoll: Boolean =
            true, isAllowed: Boolean = true, enablePostButton: Boolean = true) {
        mBinding.cpBottomToolbarContainer.createPostActionCamera.isEnabled = enableGallery &&
                !isPollRepost && isCameraHwAvailable && config.enableCamera && !isRepost
        mBinding.cpBottomToolbarContainer.createPostActionGallery.isEnabled = enableGallery &&
                !isPollRepost && config.enableGallery && !isRepost
        isOgViewAllowed = isAllowed && !isPollRepost
        updatePostButton(enablePostButton)
    }

    private fun updatePostButton(enabled: Boolean = true) {
        if (enabled.not()) {
            mBinding.toolBar.postCreationBtn.isEnabled = enabled
            mBinding.toolBar.postCreationBtn.setTextColor(
                ThemeUtils.getThemeColorByAttribute(this, R.attr.cp_btn_text_color_disable)
            )
            return
        }
        val isPostUseful = isPostNotEmpty()
        mBinding.toolBar.postCreationBtn.isEnabled = isPostUseful
        if (isPostUseful) {
            mBinding.toolBar.postCreationBtn.setTextColor(
                ThemeUtils.getThemeColorByAttribute(this, R.attr.cp_btn_text_color_enable)
            )
        } else {
            mBinding.toolBar.postCreationBtn.setTextColor(
                ThemeUtils.getThemeColorByAttribute(this, R.attr.cp_btn_text_color_disable)
            )
        }
    }

    private class PostPermissionAdapter(
            private val id: Int,
            activity: CreatePostActivity,
            rationale: PermissionRationaleProvider,
            private val permission: MutableList<Permission>,
            private val showRationale: Boolean = true,
            private val f: (Int) -> Unit) : PermissionAdapter(id, activity, rationale) {
        val helper: PermissionHelper

        init {
            BusProvider.getUIBusInstance().register(this)
            helper = PermissionHelper(this)
            helper.requestPermissions()
        }

        override fun getPermissions(): MutableList<Permission> = permission
        override fun onPermissionResult(
                grantedPermissions: MutableList<Permission>,
                deniedPermissions: MutableList<Permission>,
                blockedPermissions: MutableList<Permission>) {
            if (deniedPermissions.isNotEmpty() || blockedPermissions.isNotEmpty()) {
                return
            }
            f(id)
        }

        override fun shouldShowRationale(): Boolean {
            return showRationale
        }

        @Subscribe
        fun perResultListener(pr: PermissionResult) {
            helper.handlePermissionCallback(activity, pr.permissions)
            BusProvider.getUIBusInstance().unregister(this)
        }
    }

    override fun activity(): Activity = this

    override fun onBackPressed() {
        if (isPostNotEmpty()) {
            showPostDiscardDialog()
            return
        }
        handleBackPress(isSystemBackKeyPressed)
    }

    fun handleBackPress(systemBackKeyPressed: Boolean) {
        if (NewsNavigator.shouldNavigateToHome(this, referrer, systemBackKeyPressed,referrerRaw)) {
            NewsNavigator.navigateToHomeOnLastExitedTab(this, PageReferrer(NhGenericReferrer.CREATE_POST_HOME))
        } else {
            super.onBackPressed()
        }
    }

    override fun initPollView() {
        val pollView =
                mBinding.cpAttachmentContainer.getViewOfType(VIEW_TYPE.POLL_VIEW) as? PollView
        if (pollView != null) return
        val view = PollView(this)
        view.setPollViewRemoveCallback(this)
        mBinding.cpAttachmentContainer.addViewOfType(VIEW_TYPE.POLL_VIEW, view)
        updateAttachmentButtons(enableGallery = false, enablePoll = false, isAllowed = false)
        scrollToBottom()
    }

    override fun onPollViewRemoved(view: View) {
        mBinding.cpAttachmentContainer.removeView(view)
        updateAttachmentButtons()
        requestEditFocus()
    }

    override fun onOgViewRemove(view: View) {
        viewModel.removeOmbed()
        mBinding.cpAttachmentContainer.removeView(view)
        viewModel.embedUseCase.status().removeObserver(embedStatusObserver)
        updateAttachmentButtons()
    }

    override fun onPollTextChanged(isValidLength: Boolean) {
        updatePostButton(isValidLength)
    }

    override fun onPollOptionAdded() {
        scrollToBottom()
    }

    override fun lifecyleOwner(): LifecycleOwner = this

    private fun showPostDiscardDialog() {
        val ft = supportFragmentManager.beginTransaction()
        val previousFrag = supportFragmentManager.findFragmentByTag("dialog")
        previousFrag?.also {
            ft.remove(previousFrag)
        }
        PostCreationDiscardDialog(isSystemBackKeyPressed).show(ft, "dialog")
    }

    private fun isPostNotEmpty(): Boolean {
        if (config.mode == CreatePostUiMode.REPOST) {
            return true
        }
        val imageCount = gridAdapter?.basicItemCount ?: 0
        val pollView =
                mBinding.cpAttachmentContainer.getViewOfType(VIEW_TYPE.POLL_VIEW) as? PollView
        val textLength = mBinding.cpEditText.text.trim().length

        return if (pollView != null) pollView.isPollViewValid() && textLength > 0 else
            (imageCount > 0 || textLength > 0)
    }

    private fun scrollToCursor() {
        mBinding.postScrollView.post {
            mBinding.postScrollView.smoothScrollTo(0, mBinding.cpEditText.bottom)
        }
    }

    private fun scrollToBottom(){
        mBinding.postScrollView.post {
            mBinding.postScrollView.smoothScrollTo(0, mBinding.cpAttachmentContainer.bottom)
        }
    }

    override fun onDestroy() {
        gridAdapter?.destroy()
        AndroidUtils.hideKeyBoard(this)
        super.onDestroy()
    }
}


interface CreatePostView {
    fun checkPermission(rc: Int, permission: MutableList<Permission>)
    fun activity(): Activity?
    fun startSettingActivity()
    fun initPollView()
    fun initSuggestions()
    fun showSuggestionView(state: Boolean)
    fun lifecyleOwner(): LifecycleOwner
    fun onHashClicked()
    fun onHandleClicked()
}


internal data class CreatePostUiConfig(
        val mode: CreatePostUiMode = CreatePostUiMode.POST,
        var enableCamera: Boolean = false,
        var enableGallery: Boolean = false,
        val enableLocation: Boolean = false,
        var enablePoll: Boolean = false,
        var isPostButtonActive: Boolean = true,
        val enablePrivacy: Boolean = true)

internal class UIConfigBuilder(val mode: CreatePostUiMode, isCameraAvailable: Boolean = true,
                               isLocationEnable: Boolean = true) {
    private var enableCamera = false
    private var enableGallery = false
    private var enableLocation = false
    private var enablePoll = false
    private var isPostButtonActive = false
    private var enablePrivacy = true

    init {
        when (mode) {
            CreatePostUiMode.COMMENT, CreatePostUiMode.REPLY-> {
                enablePrivacy = false
            }
            CreatePostUiMode.POST-> {
                enableCamera = isCameraAvailable
                enableGallery = true
                enableLocation = isLocationEnable
                enablePoll = true
            }
            CreatePostUiMode.REPOST-> {
                enableCamera = isCameraAvailable
                enableGallery = true
                isPostButtonActive = true
                enableLocation = isLocationEnable
            }
        }
    }

    fun build(): CreatePostUiConfig =
            CreatePostUiConfig(
                    mode, enableCamera, enableGallery, enableLocation, enablePoll, isPostButtonActive, enablePrivacy
            )
}