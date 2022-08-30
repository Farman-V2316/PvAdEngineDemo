/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.news.view.fragment

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Activity
import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.Spannable
import android.text.style.URLSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.request.RequestOptions
import com.newshunt.adengine.model.entity.BaseDisplayAdEntity
import com.newshunt.adengine.model.entity.NativePgiAdAsset
import com.newshunt.adengine.model.entity.version.AdPosition
import com.newshunt.adengine.util.AdsUtil
import com.newshunt.adengine.view.helper.PgiAdHandler
import com.newshunt.appview.R
import com.newshunt.common.view.customview.InternalUrlSpan
import com.newshunt.common.view.customview.internalLinkTouchListener
import com.newshunt.appview.common.ui.helper.NavigationEvent
import com.newshunt.appview.common.ui.helper.NavigationHelper
import com.newshunt.common.helper.common.AndroidUtils
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.DataUtil
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.font.FontHelper
import com.newshunt.common.helper.share.NHShareView
import com.newshunt.common.helper.share.ShareUi
import com.newshunt.common.helper.share.ShareViewShowListener
import com.newshunt.common.model.retrofit.RestAdapterContainer
import com.newshunt.common.model.usecase.ShareUsecase
import com.newshunt.common.model.usecase.SyncShareUsecase
import com.newshunt.common.track.ApiResponseOperator
import com.newshunt.common.view.adapter.NHFragmentStatePagerAdapter
import com.newshunt.common.view.customview.NHImageView
import com.newshunt.common.view.customview.fontview.NHTextView
import com.newshunt.common.view.view.BaseFragment
import com.newshunt.dataentity.analytics.entity.NhAnalyticsUserAction
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.common.asset.BackgroundType2
import com.newshunt.dataentity.common.asset.BaseDetailList
import com.newshunt.dataentity.common.asset.CommonAsset
import com.newshunt.dataentity.common.asset.Format
import com.newshunt.dataentity.common.asset.PostEntity
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.common.model.entity.BaseError
import com.newshunt.dataentity.common.view.customview.FIT_TYPE
import com.newshunt.dataentity.news.analytics.NewsReferrer
import com.newshunt.dataentity.social.entity.DetailCard
import com.newshunt.dataentity.social.entity.PhotoChild
import com.newshunt.deeplink.DeeplinkUtils
import com.newshunt.deeplink.navigator.CommonNavigator
import com.newshunt.deeplink.navigator.NewsNavigator
import com.newshunt.dhutil.bundleOf
import com.newshunt.dhutil.helper.interceptor.NewsListErrorResponseInterceptor
import com.newshunt.dhutil.helper.retrofit.NewsBaseUrlContainer
import com.newshunt.dhutil.helper.theme.DeeplinkableDetail
import com.newshunt.dhutil.view.ErrorMessageBuilder
import com.newshunt.dhutil.view.customview.CustomViewPager
import com.newshunt.helper.ImageUrlReplacer
import com.newshunt.news.helper.DefaultNavigatorCallback
import com.newshunt.news.helper.NewsListCardLayoutUtil
import com.newshunt.news.helper.StoryShareUtil
import com.newshunt.news.model.apis.NewsDetailAPI
import com.newshunt.news.model.sqlite.SocialDB
import com.newshunt.news.model.usecase.BundleUsecase
import com.newshunt.news.model.usecase.FollowBlockUpdateUsecase
import com.newshunt.news.model.usecase.MediatorUsecase
import com.newshunt.news.model.usecase.toMediator2
import com.newshunt.news.util.NewsConstants
import com.newshunt.news.view.customview.SlowNetworkImageView
import com.newshunt.sdk.network.Priority
import com.newshunt.sdk.network.image.Image
import dagger.Component
import dagger.Module
import dagger.Provides
import io.reactivex.Observable
import java.util.Collections
import javax.inject.Inject
import javax.inject.Named
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set

/**
 * Container for list of all Photos. This class uses <code>PhotoSlideDetailFragment</code> for
 * displaying individual photos.
 *
 * Created by karthik.r on 2019-10-16.
 */
class PhotoSlideFragment : BaseFragment(), ViewPager.OnPageChangeListener,
        PgiNativeAdFragment.PgiNativeAdFragmentInterface {

    private val DELAYED_EMPTY_CONTENT = 1

    companion object {
        @JvmStatic
        fun newInstance(bundle: Bundle?): PhotoSlideFragment {
            return PhotoSlideFragment().apply {
                arguments = bundle
            }
        }

        @JvmStatic
        fun newInstance(intent: Intent): PhotoSlideFragment {
            return PhotoSlideFragment().apply {
                arguments = intent.extras
            }
        }
    }

    @Inject
    lateinit var photosViewModelF: PhotosViewModel.Factory
    private lateinit var vm: PhotosViewModel

    private lateinit var adapter: PhotosDetailAdapter
    private var photoSliderPager: CustomViewPager? = null
    private var landingStoryIndex: Int = -1
    private lateinit var progressbar: ProgressBar
    //pgi
    private var pgiAdHandler = PgiAdHandler
    private var nativePgiAdAsset: NativePgiAdAsset? = null
    private var localPGIAds: HashMap<Int, NativePgiAdAsset> = HashMap()
    private lateinit var errorParent: LinearLayout
    private var childFetchUrl: String? = null
    private var parentId: String? = null
    private var v4BackUrl: String? = null
    private var landingStoryId: String? = null
    private var pageReferrer: PageReferrer? = null
    private var referrerRaw:String? = null
    private var prevPos : Int = -1

    private var handler = Handler {
        if (it.what == DELAYED_EMPTY_CONTENT) {
            showError(null)
        }

        true
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.photo_slide_fragment, container, false)
        progressbar = view.findViewById<ProgressBar>(R.id.progressbar)
        errorParent = view.findViewById<LinearLayout>(R.id.error_parent)

        if (arguments != null) {
            photoSliderPager = view?.findViewById(R.id.news_detail_pager)
            photoSliderPager?.let {
                it.addOnPageChangeListener(this)
                /*this is to make sure on page selected(for ressolving bug that for oth positon page
                 selected is not getting called ) get called after pager view fully created*/
                it.post {
                    onPageSelected(it.currentItem)
                }
            }
            landingStoryIndex = arguments?.getInt(Constants.COLLECTION_SELECTED_INDEX, -1) ?: -1
            adapter = PhotosDetailAdapter(childFragmentManager, arguments)
            photoSliderPager?.adapter = adapter
            val parentId = arguments?.getString(NewsConstants.PARENT_STORY_ID)
            val storyId = arguments?.getString(Constants.BUNDLE_POST_ID) ?: parentId ?: ""
            val childFetchUrl = arguments?.getString(Constants.CONTENT_URL)
            landingStoryId = arguments?.getString(Constants.STORY_ID)
            v4BackUrl = arguments?.getString(Constants.V4BACKURL)
            pageReferrer = arguments?.get(NewsConstants.BUNDLE_ACTIVITY_REFERRER) as PageReferrer?
            referrerRaw = arguments?.getString(Constants.REFERRER_RAW)
            DaggerPhotosComponent.builder()
                    .photosModule(PhotosModule(
                            CommonUtils.getApplication(),
                            SocialDB.instance(),
                            storyId
                    ))
                    .build()
                    .inject(this)
            vm = ViewModelProviders.of(this, photosViewModelF)[PhotosViewModel::class.java]

            if (CommonUtils.isEmpty(parentId)) {
                initView(childFetchUrl, landingStoryId)
            }
            else {
                // Fetch parent, the load child
                vm.fetchParentUsecase.data().observe(viewLifecycleOwner, Observer {
                    if (it.isFailure) {
                        showError(it.exceptionOrNull())
                    }
                    else {
                        errorParent.visibility = View.GONE
                        initView(it.getOrNull()?.i_childFetchUrl(), landingStoryId)
                    }
                })

                this.parentId = parentId
                vm.fetchParent(parentId)
            }

            vm.photoGalleryNwUsecase.data().observe(viewLifecycleOwner,  Observer{
                if (it.exceptionOrNull() != null) {
                    showError(it.exceptionOrNull())
                }
            })

            vm.fetchParentUsecase.data().observe(viewLifecycleOwner,  Observer{
                if (it.exceptionOrNull() != null) {
                    showError(it.exceptionOrNull())
                }
            })
        }
        return view
    }

    fun retryLoading() {
        if (parentId != null) {
            vm.fetchParent(parentId)
        }
        else if (childFetchUrl != null) {
            vm.fetchPhotoGalleryFromNetwork(childFetchUrl!!)
        }
        else {
            handleBackPress()
        }
    }

    private fun initView(childFetchUrl: String?, landingStoryId: String?) {
        this.childFetchUrl = childFetchUrl
        if (!CommonUtils.isEmpty(childFetchUrl)) {
            var fetchFromNetwork = false
            vm.photos.observe(viewLifecycleOwner, Observer {
                if (it.isEmpty() && !fetchFromNetwork) {
                    vm.fetchPhotoGalleryFromNetwork(childFetchUrl!!)
                    fetchFromNetwork = true
                }

                adapter.items.clear()
                if (!it.isEmpty()) {
                    handler.removeMessages(DELAYED_EMPTY_CONTENT)
                    progressbar.visibility = View.GONE
                    errorParent.visibility = View.GONE
                    adapter.items.addAll(it)
                    insetLoacalPGIAdsIfAvailable()
                    adapter.notifyDataSetChanged()
                    if (landingStoryIndex == -1 && landingStoryId != null) {
                        adapter.items.forEachIndexed { index, baseDetailList ->
                            if (baseDetailList.i_id().endsWith(landingStoryId)) {
                                landingStoryIndex = index
                            }
                        }
                    }

                    if (landingStoryIndex == -1 && !adapter.items.isEmpty()) {
                        landingStoryIndex = 0
                    }

                    if (landingStoryIndex < adapter.items.size) {
                        photoSliderPager?.setCurrentItem(landingStoryIndex, true)
                        landingStoryIndex = Integer.MAX_VALUE // Prevent repeat landing
                    }
                }
                else {
                    handler.sendEmptyMessageDelayed(DELAYED_EMPTY_CONTENT, 1000)
                }
            })
        } else {
            vm.card.observe(viewLifecycleOwner, Observer {
                progressbar.visibility = View.GONE
                adapter.downloadAllowed = it?.i_viral() == null
                if (it?.i_viral() != null) {
                    adapter.items.clear()
                    val viral = it.i_viral()
                    val backgroundOption = viral?.backgroundOption
                    val thumbnail = backgroundOption?.imageUrl ?: ""
                    val id = it.i_id()
                    backgroundOption?.type?.let { bg ->
                        if (bg == BackgroundType2.IMAGE_BG) {
                            adapter.items.add(PhotoChild(id, "", "", System.currentTimeMillis(),
                                    thumbnail, "",
                                    false, it.i_title(), 0))
                            adapter.isViral = true
                            adapter.notifyDataSetChanged()
                        }
                    }

                } else if (it?.i_thumbnailUrls() != null && thumbnails != it?.i_thumbnailUrls()) {
                    adapter.items.clear()
                    for (thumbnail in it.i_thumbnailUrls()!!) {
                        adapter.items.add(PhotoChild(it.i_id(), "", it.i_shareUrl(), System.currentTimeMillis(),
                                thumbnail, "",
                                false, it.i_title(), 0))
                    }
                    insetLoacalPGIAdsIfAvailable()
                    adapter.notifyDataSetChanged()
                    thumbnails = it?.i_thumbnailUrls()
                }

                if (landingStoryIndex < adapter.items.size) {
                    photoSliderPager?.setCurrentItem(landingStoryIndex, false)
                    landingStoryIndex = Integer.MAX_VALUE // Prevent repeat landing
                }
            })
        }
    }

    private var thumbnails: List<String>? = null

    private fun showError(throwable: Throwable?) {
        if (!::errorParent.isInitialized) return
        if (context == null) return

        Logger.d(TAG, "showing error for ${throwable?.message}")
        progressbar.visibility = View.GONE
        errorParent.visibility = View.VISIBLE
        var errorMessageBuilder : ErrorMessageBuilder? = null
        errorMessageBuilder = ErrorMessageBuilder(errorParent, context!!, object : ErrorMessageBuilder.ErrorMessageClickedListener {
            override fun onNoContentClicked(view: View?) {
                // Do nothing
            }

            override fun onRetryClicked(view: View?) {
                Logger.d(TAG, "Retrying to fetch userProfile")
                errorMessageBuilder?.hideError()
                retryLoading()
            }
        })

        if (throwable is BaseError) {
            errorMessageBuilder.showError(throwable)
        }
        else {
            errorMessageBuilder.showError(null)
        }
    }

    private fun handlePgiNativeAdInsertion(position: Int) {
        pgiAdHandler.nativePgiAdAsset?.let {
            val tempNativePgiAdAsset: NativePgiAdAsset = it
            if (tempNativePgiAdAsset == nativePgiAdAsset) {
                nativePgiAdAsset = tempNativePgiAdAsset
                return
            }
            nativePgiAdAsset = tempNativePgiAdAsset
            nativePgiAdAsset?.let { it1 ->
                adapter.items.add(position + 1, it1)
                localPGIAds[position + 1] = it1
                adapter.numberOfPGIAds = localPGIAds.size
            }
            adapter.notifyDataSetChanged()
        }

    }


    private fun handleReportedPgiNativeAdDeletion(reportedAdEntity: BaseDisplayAdEntity) {
        adapter.items.removeAt(photoSliderPager!!.currentItem)
        adapter.notifyDataSetChanged()
        //remove from local pgi map too as we dont need in future
        localPGIAds.remove(photoSliderPager!!.currentItem)
        this.adapter.numberOfPGIAds = localPGIAds.size
        AdsUtil.destroyAd(reportedAdEntity, uniqueScreenId)
    }

    /**
     * This is to insert pgi ads if ads already seen and photos get updated from db in between
     *
     */
    private fun insetLoacalPGIAdsIfAvailable() {
        for ((key, value) in localPGIAds) {
            adapter.items.add(key, value)
        }
        adapter.numberOfPGIAds = localPGIAds.size
    }

    override fun onPageScrollStateChanged(state: Int) {
        //do nothing
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
        //do nothing
    }

    override fun onPageSelected(position: Int) {
        // Remove any redundant or unused ad if any, at location currentIndex + 2
        if (position + 2 < adapter.items.size && adapter.items[position + 2] is
                        NativePgiAdAsset) {
            val nativePgiAdAsset = adapter.items[position + 2] as NativePgiAdAsset
            if (this.nativePgiAdAsset === nativePgiAdAsset) {
                this.nativePgiAdAsset = null
            }
            adapter.items.removeAt(position + 2)
            //remove from local pgi map too as we dont need in future
            localPGIAds.remove(position + 2)
            adapter.numberOfPGIAds = localPGIAds.size
            AdsUtil.destroyAd(nativePgiAdAsset.baseAdEntity, uniqueScreenId)
            adapter.notifyDataSetChanged()
        }
        if (position <= adapter.items.size - 1
                && adapter.items[position].i_format() != Format.AD) {
            // if prevPos == -1 means user just landed here, so don't show PGI ads, just increment the pgi count
            if (prevPos == -1) {
                pgiAdHandler.updateSwipeCount()
            } else {
                pgiAdHandler.updatePageInfoAndSwipeCount(activity, lifecycleOwner = viewLifecycleOwner)
            }

            // Insert ad only on right swipe and if current position is not zero due to bug as in
            // http://speakman.net.nz/blog/2014/02/20/a-bug-in-and-a-fix-for-the-way-fragmentstatepageradapter-handles-fragment-restoration/
            if (prevPos < position && position != 0 && adapter.items[position] !is NativePgiAdAsset) {
                handlePgiNativeAdInsertion(position)
            }
            prevPos = position
        }
    }

    override fun onFragmentBackPressed() {
        //Do nothing
    }

    override fun onPgiAdClosed() {
        //TODO MUkesh
    }

    fun handleActionBarBackPress() {
        if (!CommonUtils.isEmpty(v4BackUrl)) {
            val pageReferrer = PageReferrer(NewsReferrer.STORY_DETAIL, landingStoryId)
            pageReferrer.referrerAction = NhAnalyticsUserAction.BACK
            CommonNavigator.launchDeeplink(requireContext(), v4BackUrl, pageReferrer)
        } else if (NewsNavigator.shouldNavigateToHome(activity, pageReferrer, false,referrerRaw)) {
            val pageReferrer = PageReferrer(NewsReferrer.STORY_DETAIL, landingStoryId)
            pageReferrer.referrerAction = NhAnalyticsUserAction.BACK
            NewsNavigator.navigateToHomeOnLastExitedTab(activity, pageReferrer)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK)
            when (requestCode) {
                Constants.REPORTED_ADS_RESULT_CODE -> {
                    val reportedAdEntity = (data?.getSerializableExtra(Constants
                            .REPORTED_ADS_ENTITY) as? BaseDisplayAdEntity?)
                    if(reportedAdEntity != null && reportedAdEntity.adPosition == AdPosition.PGI){
                        handleReportedPgiNativeAdDeletion(reportedAdEntity)
                    }
                }
            }
    }
}

@Component(modules = [PhotosModule::class])
interface PhotosComponent {
    fun inject(photosComponent: PhotoSlideFragment)
}

@Module
class PhotosModule(private val app: Application,
                   private val socialDB: SocialDB,
                   private val postId: String) {
    @Provides
    fun app() = app

    @Provides
    @Named("postId")
    fun postId(): String = postId

    @Provides
    fun fetchDao() = socialDB.fetchDao()

    @Provides
    fun detailAPI() = RestAdapterContainer.getInstance().getDynamicRestAdapterRx(
            CommonUtils.formatBaseUrlForRetrofit(NewsBaseUrlContainer.getApplicationUrl()),
            Priority.PRIORITY_HIGHEST,
            "",
            NewsListErrorResponseInterceptor())
            .create(NewsDetailAPI::class.java)
}

class PhotosViewModel(private val context: Application,
                      private val postId: String,
                      val fetchParentUsecase: MediatorUsecase<Bundle, PostEntity?>,
                      val photoGalleryNwUsecase: MediatorUsecase<Bundle, List<PhotoChild>>)
    : AndroidViewModel(context) {

    fun fetchPhotoGalleryFromNetwork(childFetchUrl: String) {
        photoGalleryNwUsecase.execute(bundleOf(Constants.CONTENT_URL to childFetchUrl))
    }

    fun fetchParent(parentId: String?) {
        fetchParentUsecase.execute(bundleOf(Constants.BUNDLE_POST_ID to parentId))
    }

    val photos: LiveData<List<PhotoChild>> = SocialDB.instance().photoChildDao().getPhotosForPost(postId)
    val card: LiveData<DetailCard?> = SocialDB.instance().fetchDao().detailCardByPostId(postId)

    class Factory @Inject constructor(private val app: Application,
                                      @Named("postId") val postId: String,
                                      private val fetchParentUsecase: FetchParentUsecase,
                                      private val photoGalleryNwUsecase: FetchPhotoGalleryFromNetworkUsecase
    ) : ViewModelProvider.AndroidViewModelFactory(app) {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return PhotosViewModel(
                    app,
                    postId,
                    fetchParentUsecase.toMediator2(true),
                    photoGalleryNwUsecase.toMediator2(true)
            ) as T
        }
    }
}

// TODO(satosh.dhanyamraju): need to check moreLoadUrl ?
class FetchParentUsecase
@Inject constructor(private val api: NewsDetailAPI) : BundleUsecase<PostEntity?> {

    override fun invoke(p1: Bundle): Observable<PostEntity?> {
        val parentPostId = p1.getString(Constants.BUNDLE_POST_ID)
        if (CommonUtils.isEmpty(parentPostId)) {
            return Observable.just(null)
        }

        return api.getFullPost(parentPostId, false, true).lift(ApiResponseOperator()).map {
            it.data
        }.onErrorReturn {
            throw it
        }
    }
}


class FetchPhotoGalleryFromNetworkUsecase
@Inject constructor(@Named("postId") private val postId: String,
                    private val api: NewsDetailAPI) : BundleUsecase<List<PhotoChild>> {

    var executed: Boolean = false

    override fun invoke(p1: Bundle): Observable<List<PhotoChild>> {
        if (executed) {
            return Observable.just(Collections.emptyList())
        }

        val url: String = p1.getString(Constants.CONTENT_URL)
                ?: return Observable.just(Collections.emptyList())
        executed = true
        return api.getChildPhotos(url).lift(ApiResponseOperator()).map {
            var i = 0
            it.data.rows.forEach { child ->
                child.postId = postId
                child.viewOrder = i++
            }

            SocialDB.instance(CommonUtils.getApplication()).photoChildDao().insReplace(it.data.rows)
            it.data.rows
        }.onErrorReturn {
            throw it
        }
    }
}

class PhotosDetailAdapter(fm: FragmentManager, private val parentStoryBundle: Bundle?) :
        NHFragmentStatePagerAdapter(fm) {

    var downloadAllowed: Boolean = true
    var isViral = false
    var numberOfPGIAds = 0
    val showShareView = parentStoryBundle?.getBoolean(Constants.SHOW_SHARE_VIEW,true) ?: true


    override fun getItemPosition(`object`: Any): Int {
        return PagerAdapter.POSITION_NONE
    }

    override fun getItem(position: Int): Fragment {

        val title = if (items.size > 1) {
            getTitilePosition(position).toString() + "/" + (items.size - numberOfPGIAds)
        } else {
            Constants.EMPTY_STRING
        }

        return getFragment(items[position], title)
    }

    private fun getTitilePosition(position: Int): Int {
        if (numberOfPGIAds == 0) {
            position + 1
        }
        var numberOfAds = 0
        for (i in 0..position) {
            if (items[i].i_format() == Format.AD)
                numberOfAds++
        }
        return position - numberOfAds + 1
    }

    private fun getFragment(baseDetailList: BaseDetailList?, title: String): Fragment {
        return if (baseDetailList?.i_format() == Format.AD) {
            PgiNativeAdFragment.newInstance(baseDetailList)
        } else {
            val photoChild = baseDetailList as PhotoChild
            PhotoSlideDetailFragment.newInstance(photoChild.postId, photoChild.imgUrl,
                    photoChild.shareUrl, title, photoChild.description, downloadAllowed, parentStoryBundle,
                    isViral, showShareView)
        }
    }

    val items: ArrayList<BaseDetailList> = ArrayList()

    override fun getCount(): Int {
        return items.size
    }
}

class PhotoSlideDetailFragment : BaseFragment(), ShareViewShowListener, DeeplinkableDetail {

    private var shareUrl: String? = null
    private lateinit var storyId: String
    private var allowDownload = true
    private var isViral = false
    private var srcId: String? = null
    private var toolbar: Toolbar? = null
    private var bottomPanel: View? = null
    private var screenVisibilityController = OnScreenVisibilityController()
    private var description: String? = null
    private var sourceName: String? = null
    private var progressbar : ProgressBar? = null
    var card: CommonAsset? = null

    lateinit var shareUsecase: MediatorUsecase<Bundle,Boolean>

    override fun getIntentOnShareClicked(shareUi: ShareUi?): Intent {
        val shareIntent = Intent(Intent.ACTION_SEND)

        shareIntent.type = Constants.INTENT_TYPE_TEXT
        shareIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, description)
        shareIntent.putExtra(Intent.EXTRA_TEXT, StoryShareUtil.getShareableString(shareUrl,
                description, null, true, sourceName))
        return Intent.createChooser(shareIntent, CommonUtils.getString(R.string.share_source))
    }

    override fun onShareViewClick(packageName: String?, shareUi: ShareUi?) {
        val item = arguments?.getSerializable(Constants.BUNDLE_STORY) as? CommonAsset?
        if (item == null) {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.`package` = packageName
            shareIntent.type = Constants.INTENT_TYPE_TEXT
            shareIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, description)
            shareIntent.putExtra(Intent.EXTRA_TEXT, StoryShareUtil.getShareableString(shareUrl,
                    description, null, true, sourceName))
            AndroidUtils.startActivity(activity, shareIntent)
        } else {
            Intent().apply {
                action = Constants.SHARE_POST_ACTION
                val shareItem = if (CommonUtils.isEmpty(shareUrl)) item.rootPostEntity() else item.rootPostEntity()?.copy(shareUrl = shareUrl)
                putExtra(Constants.BUNDLE_STORY, shareItem)
                putExtra(Constants.BUNDLE_SHARE_PACKAGE_NAME, packageName)
                NavigationHelper.navigationLiveData.postValue(NavigationEvent(this))
            }
            shareUsecase.execute(ShareUsecase.args(item.i_id(), "POST", parentId = item.i_parentPostId(), postSourceAsset =item.i_source(), sourceLang = item.i_langCode()))
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(postId: String, imageUrl: String?, shareUrl: String?, title: String,
                        description: String?, downloadAllowed: Boolean, parentBundle: Bundle?,
                        isViral: Boolean, showShareView: Boolean):
                PhotoSlideDetailFragment {
            val photoSlideDetailFragment = PhotoSlideDetailFragment()
            photoSlideDetailFragment.arguments = Bundle().apply {
                putString(Constants.BUNDLE_POST_ID, postId)
                putString(Constants.BUNDLE_IMAGE_URL, imageUrl)
                putString(Constants.BUNDLE_SHARE_URL, shareUrl)
                putString(Constants.BUNDLE_SHARE_TITLE, title)
                putString(Constants.BUNDLE_DESCRIPTION, description)
                putBoolean(Constants.BUNDLE_DOWNLOAD_ALLOWED, downloadAllowed)
                putBoolean(Constants.BUNDLE_IS_VIRAL, isViral)
                putBoolean(Constants.SHOW_SHARE_VIEW, showShareView)
                putSerializable(Constants.BUNDLE_STORY, parentBundle?.getSerializable(Constants.BUNDLE_STORY))
            }
            return photoSlideDetailFragment
        }

        @JvmStatic
        fun newInstance(intent: Intent): PhotoSlideDetailFragment {
            val photoSlideDetailFragment = PhotoSlideDetailFragment()
            photoSlideDetailFragment.arguments = intent.extras
            return photoSlideDetailFragment
        }
    }

    override fun onCreate(savedState: Bundle?) {
        super.onCreate(savedState)
        shareUsecase=ShareUsecase(SocialDB.instance().interactionsDao(), SyncShareUsecase(),
            FollowBlockUpdateUsecase(SocialDB.instance().followBlockRecoDao())).toMediator2()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.photo_slide_detail_fragment, container, false)
        progressbar = view.findViewById<ProgressBar>(R.id.progress_bar)
        toolbar = view.findViewById(R.id.toolbar)
        bottomPanel = view.findViewById(R.id.photo_bottom_panel)
        val imageView: SlowNetworkImageView = view.findViewById(R.id.slow_network_touch_image_view)
        val downloadImageView = view.findViewById<ImageView>(R.id.photo_download)
        val title = arguments?.getString(Constants.BUNDLE_SHARE_TITLE) ?: Constants.EMPTY_STRING
        description = arguments?.getString(Constants.BUNDLE_DESCRIPTION)
                ?: Constants.EMPTY_STRING
        val imageUrl = arguments?.getString(Constants.BUNDLE_IMAGE_URL)
        sourceName = arguments?.getString(Constants.BUNDLE_SOURCE_NAME)
        val sourceImageUrl = arguments?.getString(Constants.BUNDLE_SOURCE_URL)
        val showShareView = arguments?.getBoolean(Constants.SHOW_SHARE_VIEW, true) ?: true
        shareUrl = arguments?.getString(Constants.BUNDLE_SHARE_URL)
        storyId = arguments?.getString(Constants.BUNDLE_POST_ID) ?: ""
        allowDownload = arguments?.getBoolean(Constants.BUNDLE_DOWNLOAD_ALLOWED, true) ?: true
        isViral = arguments?.getBoolean(Constants.BUNDLE_IS_VIRAL, false) ?: false
        srcId = arguments?.getString(Constants.BUNDLE_SOURCE_ID)
        card = arguments?.getSerializable(Constants.BUNDLE_STORY) as? CommonAsset?
        val url = NewsListCardLayoutUtil.getNewsContentImageUrl(imageUrl, false)
        val slowUrl = NewsListCardLayoutUtil.getNewsContentImageUrl(imageUrl, true)
        val descView = view.findViewById<NHTextView>(R.id.photo_description)
        val descriptionParent = view.findViewById<View>(R.id.photo_description_parent)
        val moreText = view.findViewById<NHTextView>(R.id.photo_desc_more)
        var collapsed = true
        if (isViral) {
            downloadImageView.setImageResource(R.drawable.share_floating_icon_bent_arrow_selector)
            downloadImageView.setOnClickListener {
                onShareViewClick(null, ShareUi.FLOATING_ICON_BENT_ARROW)
            }
        } else {
            if (allowDownload) {
                downloadImageView.setOnClickListener {
                    imageView.savePhoto(activity, null, storyId)
                }
            } else {
                downloadImageView.visibility = View.GONE
            }
        }
        val callback = object : SlowNetworkImageView.Callback {
            override fun onPhotoDownloadSuccess(slowNetworkImageView: SlowNetworkImageView?) {
                progressbar?.visibility = View.GONE
            }

            override fun onPhotoDownloadFailure(slowNetworkImageView: SlowNetworkImageView?) {
                progressbar?.visibility = View.GONE
            }

            override fun onPhotoSaveSuccess(slowNetworkImageView: SlowNetworkImageView?) {
                if (activity != null) {
                    activity!!.runOnUiThread {
                        FontHelper.showCustomFontToast(activity,
                                CommonUtils.getString(R.string.image_saved),
                                Toast.LENGTH_SHORT)
                    }
                }
            }

            override fun onPhotoSaveFailure(slowNetworkImageView: SlowNetworkImageView?) {
                activity?.let {
                    it.runOnUiThread {
                        FontHelper.showCustomFontToast(it,
                                CommonUtils.getString(R.string.error_image_save_failed),
                                Toast.LENGTH_SHORT)
                    }
                }
            }

            override fun onPhotoTouch(slowNetworkImageView: SlowNetworkImageView?, regularPhotoRequested: Boolean) {
                screenVisibilityController.toggle()
            }
        }

        imageView.startLoading(slowUrl, url,
                callback, FIT_TYPE.FIT_CENTER, FIT_TYPE.FIT_CENTER, Priority
                .PRIORITY_NORMAL, Priority.PRIORITY_HIGHEST, false, true)

        val nhShareView = view.findViewById(R.id.nh_share_view) as NHShareView

        if (!shareUrl.isNullOrEmpty() && showShareView) {
            nhShareView.visibility = View.VISIBLE
            nhShareView.setShareListener(this)
        } else {
            nhShareView.visibility = View.GONE
        }

        view.findViewById<ImageView>(R.id.actionbar_back_button).setOnClickListener {
            if (parentFragment is PhotoSlideFragment) {
                (parentFragment as PhotoSlideFragment).handleActionBarBackPress()
            }
            activity?.onBackPressed()
        }
        if(title.isNullOrEmpty()) {
            view.findViewById<NHTextView>(R.id.photo_title).visibility = View.GONE
        } else {
            view.findViewById<NHTextView>(R.id.photo_title).text = title
        }
        if(sourceName.isNullOrEmpty()) {
            view.findViewById<NHTextView>(R.id.np_source_name).visibility = View.GONE
        } else {
            view.findViewById<NHTextView>(R.id.np_source_name).text = sourceName
        }
        if(!description.isNullOrEmpty()) {
            setDescriptionText(descView, description)
        }
        var expandable = true;
        descView.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                if (expandable) {
                    expandable = false
                    if(descView.lineCount > 3) {
                        moreText.visibility = View.VISIBLE
                        descView.maxLines = 3
                    }
                }
            }
        })
        moreText.setOnClickListener {
            if (collapsed) {
                descView.maxLines = Integer.MAX_VALUE
                moreText.text = CommonUtils.getString(R.string.photo_gallery_description_less)
                collapsed = false
            } else {
                descView.maxLines = 3
                moreText.text = CommonUtils.getString(R.string.photo_gallery_description_more)
                collapsed = true
            }
        }
        view.findViewById<NHTextView>(R.id.photo_description).setOnClickListener {
            CommonNavigator.launchInternalDeeplink(context,shareUrl, PageReferrer(NewsReferrer
                    .STORY_DETAIL),true, DefaultNavigatorCallback())
        }
        if (sourceImageUrl != null) {
            val imageLoc = ImageUrlReplacer.getQualifiedUrl(sourceImageUrl,
                    NewsListCardLayoutUtil.getNewsPaperIconImageDimension()[0])
            val loader = Image.load(imageLoc)
            loader.apply(RequestOptions.circleCropTransform())
            loader.into(view.findViewById<NHImageView>(R.id.news_source_image), ImageView.ScaleType.FIT_END)
        }

        if (DataUtil.isEmpty(sourceImageUrl) &&DataUtil.isEmpty(sourceName) &&DataUtil.isEmpty
                (description)) {
            descriptionParent.visibility = View.GONE
        }

        screenVisibilityController.show()
        return view
    }

    fun setDescriptionText(view: NHTextView, text: String?) {
        if (CommonUtils.isEmpty(text)) {
            return
        }

        val titleConvertedText = FontHelper.getFontConvertedString(text!!)
        val s: Spannable = HtmlCompat.fromHtml(titleConvertedText, HtmlCompat.FROM_HTML_MODE_LEGACY)
                as Spannable

        for (u in s.getSpans(0, s.length, URLSpan::class.java)) {
            s.setSpan(InternalUrlSpan(u.url, this::onInternalUrlClick),
                    s.getSpanStart(u), s
                    .getSpanEnd(u), 0)
        }

        view.setOnTouchListener(internalLinkTouchListener(s))
        view.setSpannableText(s, text, TextView.BufferType.NORMAL)
    }

    fun onInternalUrlClick(view: View, url: String) {
        Logger.d(TAG, "launching deeplink $url")
        if (DeeplinkUtils.isDHDeeplink(url)) {
            CommonNavigator.launchDeeplink(view.context, url, null)
        } else if (DeeplinkUtils.isValidHost(url)) {
            AndroidUtils.launchExternalLink(view.context, url)
        }
    }

    private inner class OnScreenVisibilityController {
        internal var screenCleared: Boolean = false
        internal var animatorSet: AnimatorSet? = null

        fun toggle() {
            screenCleared = !screenCleared
            startAnimation()
        }

        fun show() {
            if (screenCleared) {
                screenCleared = false
                if (toolbar != null) {
                    toolbar?.translationY = 0f
                }
                if (bottomPanel != null) {
                    bottomPanel?.translationY = 0f
                }
            }
        }

        private fun startAnimation() {
            if (animatorSet != null && (animatorSet!!.isRunning || animatorSet!!.isStarted)) {
                animatorSet!!.cancel()
            }
            animatorSet = AnimatorSet()
            val toolBarAnimator = ObjectAnimator.ofFloat(toolbar, View.TRANSLATION_Y,
                    if (screenCleared) -toolbar!!.height.toFloat() else 0f)
            val bottomPanelAnimator = ObjectAnimator.ofFloat(bottomPanel, View.TRANSLATION_Y,
                    if (screenCleared) bottomPanel!!.height.toFloat() else 0f)
            animatorSet!!.playTogether(toolBarAnimator, bottomPanelAnimator)
            animatorSet!!.start()
        }
    }

    override fun deeplinkUrl(): String? {
        return card?.i_deeplinkUrl()
    }
}

