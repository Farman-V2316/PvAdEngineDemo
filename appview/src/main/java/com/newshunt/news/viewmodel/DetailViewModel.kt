/*
 * Copyright (c) 2020 Newshunt. All rights reserved.
 */
package com.newshunt.news.viewmodel

import android.app.Activity
import android.app.Application
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Spannable
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.text.HtmlCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.newshunt.adengine.ClearAdsDataUsecase
import com.newshunt.adengine.InsertProxyAdUsecase
import com.newshunt.adengine.model.entity.BaseAdEntity
import com.newshunt.adengine.model.entity.version.AdContentType
import com.newshunt.adengine.util.AdConstants
import com.newshunt.appview.R
import com.newshunt.appview.common.entity.CardPojo
import com.newshunt.appview.common.entity.CardsPojo
import com.newshunt.appview.common.profile.helper.buildHistoryEntity
import com.newshunt.appview.common.profile.model.usecase.AddToHistoryUsecase
import com.newshunt.appview.common.ui.fragment.FullPagePojo
import com.newshunt.appview.common.ui.helper.NavigationEvent
import com.newshunt.appview.common.ui.helper.NavigationHelper
import com.newshunt.appview.common.viewmodel.CardsViewModel
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.font.FontHelper
import com.newshunt.common.view.customview.fontview.LengthNotifyingTextView
import com.newshunt.common.view.customview.fontview.NHTextView
import com.newshunt.dataentity.analytics.entity.AnalyticsParam
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.analytics.section.NhAnalyticsEventSection
import com.newshunt.dataentity.common.asset.AssetType2
import com.newshunt.dataentity.common.asset.Chunk2Pojo
import com.newshunt.dataentity.common.asset.CommonAsset
import com.newshunt.dataentity.common.asset.CreatePostUiMode
import com.newshunt.dataentity.common.asset.DetailCardPojo
import com.newshunt.dataentity.common.asset.DiscussionFilter
import com.newshunt.dataentity.common.asset.DiscussionPojo
import com.newshunt.dataentity.common.asset.Format
import com.newshunt.dataentity.common.asset.LikeListPojo
import com.newshunt.dataentity.common.asset.MoreStoriesPojo
import com.newshunt.dataentity.common.asset.PhotoChildPojo
import com.newshunt.dataentity.common.asset.PostEntity
import com.newshunt.dataentity.common.asset.PostEntityLevel
import com.newshunt.dataentity.common.asset.PostSuggestedFollow
import com.newshunt.dataentity.common.asset.SubFormat
import com.newshunt.dataentity.common.asset.SuggestedFollowsPojo
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.common.model.entity.model.LikesResponse
import com.newshunt.dataentity.common.model.entity.model.MultiValueResponse
import com.newshunt.dataentity.common.pages.FollowSyncEntity
import com.newshunt.dataentity.dhutil.model.entity.appsflyer.AppsFlyerEvents
import com.newshunt.dataentity.dhutil.model.entity.asset.ImageDetail
import com.newshunt.dataentity.model.entity.HistoryEntity
import com.newshunt.dataentity.news.analytics.StorySupplementSectionPosition
import com.newshunt.dataentity.social.entity.AdditionalContents
import com.newshunt.dataentity.social.entity.AllLevelCards
import com.newshunt.dataentity.social.entity.Interaction
import com.newshunt.dataentity.social.entity.LikeType
import com.newshunt.dataentity.social.entity.PhotoChild
import com.newshunt.dataentity.social.entity.ReplyCount
import com.newshunt.dataentity.social.entity.TopLevelCard
import com.newshunt.deeplink.navigator.CommonNavigator
import com.newshunt.dhutil.analytics.AnalyticsHelper2
import com.newshunt.dhutil.bundleOf
import com.newshunt.dhutil.helper.LiveSharedPreference
import com.newshunt.dhutil.helper.appsflyer.AppsFlyerHelper
import com.newshunt.dhutil.scan
import com.newshunt.helper.ImageUrlReplacer
import com.newshunt.news.analytics.NewsAnalyticsHelper
import com.newshunt.news.analytics.NhAnalyticsNewsEvent
import com.newshunt.news.helper.LikeEmojiBindingUtils
import com.newshunt.news.helper.NewsDetailTimespentHelper
import com.newshunt.news.helper.RecentArticleTimestampStoreHelper
import com.newshunt.news.model.daos.CardDao
import com.newshunt.news.model.service.NewsAppJSProviderService
import com.newshunt.news.model.sqlite.SocialDB
import com.newshunt.news.model.usecase.CloneFetchForNewsDetailUsecase
import com.newshunt.news.model.usecase.MediatorUsecase
import com.newshunt.news.model.usecase.NonLinearConsumedUsecase
import com.newshunt.news.model.usecase.ReadFullPostUsecase
import com.newshunt.news.model.usecase.Result0
import com.newshunt.news.model.usecase.toMediator2
import com.newshunt.news.util.NewsConstants
import com.newshunt.news.view.fragment.DetailsBindUtils
import com.newshunt.news.view.present.*
import com.newshunt.pref.NewsPreference
import dagger.Lazy
import java.util.*
import javax.inject.Inject
import javax.inject.Named
import kotlinx.android.synthetic.main.disclaimer_dialog_layout.view.disclaimer_close
import kotlinx.android.synthetic.main.disclaimer_dialog_layout.view.disclaimer_display_text_view
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class DetailsViewModel(context: Application,
                       private val lifecycleOwner: LifecycleOwner,
                       private val entityId: String,
                       private val postId: String,
                       private val section: String,
                       private val level: String,
                       private val location: String,
                       private val listLocation: String,
                       private val adId: String?,
                       private val timeSpentEventId: Long,
                       private val isInBottomSheet: Boolean,
                       private val referrerFlow: PageReferrer,
                       private val networkAndUpdatePostUsecase: MediatorUsecase<Bundle, PostEntity>, //NPUsecase,
                       val readDetailCardUsecase: MediatorUsecase<Bundle, Boolean>,
                       val readFullPostUsecase: MediatorUsecase<Bundle, PostEntity?>,
                       private val relatedStoriesFrommAddContentUsecase: MediatorUsecase<Bundle, AdditionalContents?>,
                       private val relatedStoriesForVideoContentUsecase: MediatorUsecase<Bundle, Boolean>,
                       private val suggestedFollowsUsecase: MediatorUsecase<Bundle, List<PostSuggestedFollow>>,
                       private val discussionNwUsecase: MediatorUsecase<Bundle, String?>,
                       private val clearAndDownloadFirstDiscussion: MediatorUsecase<Bundle, String?>,
                       private val addNewDiscussionUsecase: MediatorUsecase<Bundle, Boolean>,
                       private val relatedStoriesUsecase: Lazy<MediatorUsecase<Bundle, List<TopLevelCard>>>,
                       private val fetchMoreStoriesUsecase: Lazy<MediatorUsecase<Bundle, MultiValueResponse<CommonAsset>>>,
                       private val fetchCarouselMoreStoriesUsecase: Lazy<MediatorUsecase<Bundle, MultiValueResponse<CommonAsset>>>,
                       private val readLikesFirstPageUsecase: Lazy<MediatorUsecase<Bundle, LikesResponse>>,
                       private val photoChildUsecase: MediatorUsecase<Bundle, List<PhotoChild>>,
                       private val newsAppJSProviderService: NewsAppJSProviderService,
                       private val cloneFetchForNewsDetailUsecase: MediatorUsecase<Bundle, String>,
                       private val addToHistoryUsecase: MediatorUsecase<HistoryEntity, Unit>,
                       private val fetchParentNwUsecase: MediatorUsecase<Bundle, AllLevelCards?>,
                       private val deleteCommentUsecase: MediatorUsecase<Bundle, Boolean>,
                       private val reportCommentUsecase: MediatorUsecase<Bundle, String?>,
                       private val additionalContentFetchUsecase: MediatorUsecase<String, AdditionalContents?>,
                       private val nonLinearConsumeUsecase: MediatorUsecase<String, Any>,
                       private val clearAdsUseCase: MediatorUsecase<Bundle, Unit>,
                       private val insertProxyAdUsecase:MediatorUsecase<Bundle, Boolean>,
                       cardDao: CardDao) :
        CommonDetailsViewModel(context, section, postId, referrerFlow, deleteCommentUsecase,
                reportCommentUsecase), LengthNotifyingTextView.LineCountListener {

    val userFontSizeConfigProgress = LiveSharedPreference.pref(NewsPreference
            .USER_PREF_FONT_PROGRESS, context, NewsConstants.DEFAULT_PROGRESS_COUNT)

    val READ_DETAIL_UC = "READ_DETAIL_UC"
    val SHORT_LIKES_UC = "SHORT_LIKES_UC"
    private val NW_POST_UC = "NW_POST_UC"
    private val PHOTO_CHILD_US = "PHOTO_CHILD_US"

    var secondChunkPending = true
    var isInCollection = false
    /*in post detail, chips are removed. Only comments are shown.*/
    var discussionMode: CreatePostUiMode = if(isInBottomSheet) CreatePostUiMode.ALL else CreatePostUiMode.COMMENT
    var fetchChildUrl: String? = null
    var discussionIndex: Int = -1
    var isLikeListEventFired = false
    var isFirstChunkOnlyPost = false
    var isFirstChunkLoaded: Boolean = false

    val secondChunkLoading = MutableLiveData<Boolean>(false)
    private var isImageOpened: Boolean = false
    private val seenDiscussions : MutableSet<String> = mutableSetOf()
    private val seenLikes: MutableSet<String> = mutableSetOf()
    val failedNetworkCalls: MutableSet<String> = mutableSetOf()
    private val recentArticleTimestampStoreHelper = RecentArticleTimestampStoreHelper()
    val titleLengthCount = MutableLiveData<Int>()
    var titleLength : Int = 0
    private val extraAdCardIds = ArrayList<String>()
    private val extraCardsIds = MutableLiveData<List<String>>()
    val extraCards = Transformations.switchMap(extraCardsIds){cardDao.cardsById(it)}
    val likeList: LiveData<List<CardDao.Interaction>> = cardDao.allLikeTypes()

    fun retryFailedUsecases(isDiscussionDetail: Boolean) {
        failedNetworkCalls.forEach {
            when (it) {
                SHORT_LIKES_UC -> {
                    discussionLoading.postValue(true)
                    if (isDiscussionDetail) {
                        readLikesFirstPageUsecase.get().execute(bundleOf(Constants.BUNDLE_LEVEL to
                                PostEntityLevel.DISCUSSION.name))
                    } else {
                        readLikesFirstPageUsecase.get().execute(bundleOf(Constants.BUNDLE_LEVEL to
                                level))
                    }
                }
                NW_POST_UC -> {
                    fetchMoreDetails(detailCardScan.value?.data?.i_moreContentLoadUrl(), level)
                }
                PHOTO_CHILD_US -> {
                    photoChildUsecase.execute(bundleOf(Constants.CONTENT_URL to fetchChildUrl))
                }
                READ_DETAIL_UC -> {
                    executeDetailCardUsecase(level)
                }
            }
        }
    }

    val detailCardScan: LiveData<DetailCardPojo> = SocialDB.instance().fetchDao()
            .detailCardByFetchInfo(entityId, section, postId, location, listLocation, adId)
            .scan(DetailCardPojo()) { acc, t ->
        acc.copy(t, tsData = System.currentTimeMillis())
    }

    val detailFullPost: LiveData<FullPagePojo> = readFullPostUsecase.data().scan(FullPagePojo()) { acc, t ->
        if (t.isSuccess) {
            acc.copy(data = t.getOrNull(), tsData = System.currentTimeMillis(), error = null, tsError = null)
        } else {
            acc.copy(error = t.exceptionOrNull(), tsError = System.currentTimeMillis())
        }
    }

    fun fetchFullPost(postId: String?, adId: String? = null) {
        readFullPostUsecase.execute(bundleOf(Constants.BUNDLE_POST_ID to postId,
                Constants.BUNDLE_AD_ID to adId))
    }

    fun getLatestFollowedLocationData(): LiveData<List<FollowSyncEntity>> {
        return SocialDB.instance().followEntityDao().getLatestFollowedLocation()
    }

    fun getFollowedLocationsFIFOData(): LiveData<List<FollowSyncEntity>> {
        return SocialDB.instance().followEntityDao().getFollowedLocationsFIFO()
    }

    var opCardList: LiveData<List<TopLevelCard>>? = null
    fun fetchOP(): LiveData<List<TopLevelCard>>? {
        if (opCardList == null) {
            opCardList = SocialDB.instance().fetchDao().opCardList(entityId, location, section)
        }

        return opCardList
    }

    var discussionScan: LiveData<DiscussionPojo>? = null
    fun fetchDiscussion(uniqueId: String) {
        if (discussionScan == null) {
            discussionScan = SocialDB.instance().fetchDao().discussionsForPost(uniqueId).scan(DiscussionPojo()) { acc, t ->
                val tempList = t.filter { it.i_type() == AssetType2.COMMENT.name }.toMutableList() // detailpage only shows comments; no reponsts.
                acc.copy(com.newshunt.dhutil.getSorted(tempList), tsData = System.currentTimeMillis())
            }
        }
    }

    var associationScan: LiveData<CardsPojo>? = null
    fun fetchAssociation(uniqueId: String) {
        if (associationScan == null) {
            associationScan = SocialDB.instance().fetchDao().associationForPost(uniqueId).scan(CardsPojo()) { acc, t ->
                acc.copy(t.distinctBy { it.i_id() }, tsData = System.currentTimeMillis())
            }
        }
    }

    var supplementAdsTitle = Transformations.map(additionalContentFetchUsecase.data()) {
        if (it.isSuccess) {
            it.getOrNull()?.title
        } else {
            null
        }
    }

    var relatedStoriesTitle = Transformations.map(relatedStoriesFrommAddContentUsecase.data()) {
        if (it.isSuccess) {
            it.getOrNull()?.title?: CommonUtils.getString(R.string.related_stories_text_header)
        } else {
            null
        }
    }


    fun reloadDiscussionFirstPage(isDiscussionDetail: Boolean) {
        discussionLoading.postValue(true)
        if (isDiscussionDetail) {
            readLikesFirstPageUsecase.get().execute(bundleOf(Constants.BUNDLE_LEVEL to
                    PostEntityLevel.DISCUSSION.name))
        } else {
            readLikesFirstPageUsecase.get().execute(bundleOf(Constants.BUNDLE_LEVEL to
                    level))
        }
    }

    val chunk2: LiveData<Chunk2Pojo> = networkAndUpdatePostUsecase.data().scan(Chunk2Pojo()) { acc, t ->
        secondChunkLoading.postValue(false)
        if (t.isSuccess) {
            // view wants stories; not posts
            failedNetworkCalls.remove(NW_POST_UC)
            val data = t.getOrNull()
            isFirstChunkOnlyPost =  data?.i_content2() == null
            acc.copy(data = t.getOrNull(), tsData = System.currentTimeMillis())

        } else {
            failedNetworkCalls.add(NW_POST_UC)
            AnalyticsHelper2.logDevCustomErrorEvent(("chunk2 api returns null: ${t.exceptionOrNull()}"))
            acc.copy(error = t.exceptionOrNull(), tsError = System.currentTimeMillis())
        }
    }

    val suggestedFolows: LiveData<SuggestedFollowsPojo> = suggestedFollowsUsecase.data().scan(SuggestedFollowsPojo()) { acc, t ->
        if (t.isSuccess) {
            acc.copy(data = t.getOrNull(), tsData = System.currentTimeMillis())
        } else {
            acc.copy(error = t.exceptionOrNull(), tsError = System.currentTimeMillis())
        }
    }

    var discussionNextPageUrl: String? = null
    var firstPageUrl: String? = null
    var filterTypes: List<DiscussionFilter>? = null
    val discussionLoading: MutableLiveData<Boolean> = MutableLiveData(isInBottomSheet)
    var hadDiscussionItems = false
    val discussionFirstPage: LiveData<Boolean> = discussionNwUsecase.data().scan(false, { acc, t ->
        discussionNextPageUrl = if (t.getOrNull()?.isEmpty() == true) null else t.getOrNull()
        false
    })

    val deletedPrimaryItem = deleteCommentUsecase.data()

    val parentCard: LiveData<CardPojo> = fetchParentNwUsecase.data().scan(CardPojo(), { acc, t ->
        if (t.isSuccess) {
            acc.copy(data = t.getOrNull(), tsData = System.currentTimeMillis())
        } else {
            acc.copy(error = t.exceptionOrNull(), tsError = System.currentTimeMillis())
        }
    })

    fun fetchParentCard() {
        fetchParentNwUsecase.execute(Bundle.EMPTY)
    }

    var dislikeStories: LiveData<List<String>>? = SocialDB.instance().fetchDao().getDislikeStories()

    val lastestDiscussionLoadingState: LiveData<Boolean> = clearAndDownloadFirstDiscussion.data().scan(false, {
        acc, t ->
        discussionNextPageUrl = if (t.getOrNull()?.isEmpty() == true) null else t.getOrNull()
        (t.getOrNull() ?: Constants.EMPTY_STRING).isNotEmpty()
    })

    var relatedstories: LiveData<CardsPojo>? = null
    lateinit var shortLikes: LiveData<LikeListPojo>
    var relatedVideos: LiveData<CardsPojo>? = null

    val photoChild: LiveData<PhotoChildPojo> = photoChildUsecase.data().scan(PhotoChildPojo(), { photoChildPojo, result ->
        if (result.isSuccess) {
            failedNetworkCalls.remove(PHOTO_CHILD_US)
            photoChildPojo.copy(data = result.getOrNull(), tsData = System.currentTimeMillis())
        } else {
            failedNetworkCalls.add(PHOTO_CHILD_US)
            photoChildPojo.copy(error = result.exceptionOrNull(), tsError = System.currentTimeMillis())
        }
    })

    val getNewsAppJSProviderService = newsAppJSProviderService

    fun onHashtagVisible(param: String?) {
        NewsDetailTimespentHelper.getInstance().postUpdateTimespentEvent(timeSpentEventId,
                NewsDetailTimespentHelper.HASHTAG_SEEN, Constants.YES)
    }

    fun onLikeListVisible(param: String?) {
        if (isLikeListEventFired) {
            return
        }

        val card = detailCardScan.value?.data
        if (card != null) {
            isLikeListEventFired = true
            AnalyticsHelper2.logEntityListViewEventForLikeList(section, pageReferrer, referrerFlow, card.i_type())
        }
    }

    fun onOtherPerspectiveVisible(param: String?) {
        NewsDetailTimespentHelper.getInstance().postUpdateTimespentEvent(timeSpentEventId,
                NewsDetailTimespentHelper.PERSPECTIVE_SEEN, Constants.YES)
    }

    fun onLikeVisible(id: String?) {
        if (id != null) {
            seenLikes.add(id)
            NewsDetailTimespentHelper.getInstance().postUpdateTimespentEvent(timeSpentEventId,
                    NewsDetailTimespentHelper.LIKES_SEEN, seenLikes.toString())
        }
    }

    fun onDiscussionHeaderVisible(param: String?) {
        val experimentParams = detailCardScan.value?.data?.i_experiments()?: Collections.emptyMap()
        NewsAnalyticsHelper.logPFPWidgetViewEvent(NhAnalyticsNewsEvent.WIDGET_PFP_VIEW,
                CommonUtils.getString(R.string.related_stories_widget_name), postId,
                StorySupplementSectionPosition.ENDOFSTORY,  experimentParams, false)
    }

    fun onAssociatedVideoVisible(param: String?) {
        val experimentParams = detailCardScan.value?.data?.i_experiments()?: Collections.emptyMap()
        NewsAnalyticsHelper.logPFPWidgetViewEvent(NhAnalyticsNewsEvent.CARD_WIDGET_VIEW,
                CommonUtils.getString(R.string.related_stories_widget_name), postId,
                StorySupplementSectionPosition.ENDOFSTORY,  experimentParams, true)
    }

    fun onDiscussionVisible(param: String?) {
        if (param != null) {
            seenDiscussions.add(param)
            NewsDetailTimespentHelper.getInstance().postUpdateTimespentEvent(timeSpentEventId,
                    NewsDetailTimespentHelper.DISCUSSIONS_SEEN, seenDiscussions.toString())

            val discussionIndex : Pair<Int, AllLevelCards>? = getDiscussionAndIndex(param)
            val rootPostEntity = discussionIndex?.second?.rootPostEntity()
            if (discussionIndex != null && rootPostEntity != null) {
                AnalyticsHelper2.logStoryCardViewEvent(rootPostEntity,
                        pageReferrer, discussionIndex.first, rootPostEntity.uiType.name,
                        null, null, false)
            }
        }
    }

    private fun getDiscussionAndIndex(discussionId: String): Pair<Int, AllLevelCards>? {
        discussionScan?.value?.data?.forEachIndexed { index, allLevelCards ->
            if (allLevelCards.i_id() == discussionId) {
                return Pair(index, allLevelCards)
            }
        }

        return null
    }

    fun onLikeViewClick(view: View, item: CommonAsset, likeListPojo: LikeListPojo?) {
        LikeEmojiBindingUtils.openLikesList(view, item, pageReferrer, section, likeListPojo)
    }

    fun onPhotoClick(view: View, photoChild: PhotoChild, index: Int?) {
        openPhoto(photoChild.postId, index ?: 0, fetchChildUrl, null)
    }

    fun onPostClick(view: View, commonAsset: CommonAsset, index: Int?) {
        openPhoto(commonAsset.i_id(), index ?: 0, fetchChildUrl, commonAsset)
        if (!isImageOpened) {
            isImageOpened = true
            NewsDetailTimespentHelper.getInstance().postUpdateTimespentEvent(timeSpentEventId,
                    NewsDetailTimespentHelper.IS_IMAGE_OPENED, java.lang.Boolean.toString(isImageOpened))
        }
    }

    fun onPostClick(view: View, index: Int?) {
        val commonAsset = detailCardScan.value?.data ?: return
        openPhoto(commonAsset.i_id(), index ?: 0, fetchChildUrl, commonAsset)
        if (!isImageOpened) {
            isImageOpened = true
            NewsDetailTimespentHelper.getInstance().postUpdateTimespentEvent(timeSpentEventId,
                    NewsDetailTimespentHelper.IS_IMAGE_OPENED, java.lang.Boolean.toString(isImageOpened))
        }
    }

    fun isAllFilter() : Boolean {
        return discussionMode == CreatePostUiMode.ALL
    }

    fun onPostClick( commonAsset: CommonAsset, url: String?) {
        var indexPosition = -1
        commonAsset.i_thumbnailUrls()?.forEachIndexed { index, thumbnail ->
            if (thumbnail.contains(ImageUrlReplacer.DEFAULT_EMBEDDED_IMAGE_MACRO)) {
                val prefix = thumbnail.substring(0, thumbnail.indexOf(ImageUrlReplacer.DEFAULT_EMBEDDED_IMAGE_MACRO))
                val suffix = thumbnail.substring(thumbnail.indexOf(ImageUrlReplacer.DEFAULT_EMBEDDED_IMAGE_MACRO) + ImageUrlReplacer.DEFAULT_EMBEDDED_IMAGE_MACRO.length)
                if (url?.startsWith(prefix) == true && url.endsWith(suffix)) {
                    indexPosition = index
                }
            }
            else {
                if(thumbnail.equals(url)) {
                    indexPosition = index
                }
            }
        }

        if (indexPosition == -1) {
            if (url != null) {
                openPhotoByUrl(url, commonAsset)
            }
        }
        else {
            openPhoto(commonAsset.i_id(), indexPosition, null, commonAsset)
        }
    }

    fun openPhotoByUrl(url: String, post: CommonAsset?) {
        val intent = android.content.Intent(Constants.VIEW_PHOTO_ACTION)
        intent.putExtra(Constants.BUNDLE_POST_ID, postId)
        intent.putExtra(Constants.BUNDLE_IMAGE_URL, url)
        intent.putExtra(Constants.BUNDLE_SHARE_URL, post?.i_shareUrl())
        intent.putExtra(Constants.BUNDLE_DESCRIPTION, post?.i_title())
        intent.putExtra(Constants.SHOW_SHARE_VIEW, false)
        intent.putExtra(Constants.BUNDLE_DOWNLOAD_ALLOWED, true)
        intent.putExtra(Constants.BUNDLE_STORY, post)
        NavigationHelper.navigationLiveData.postValue(NavigationEvent(intent))
    }

    fun showDisclaimerOnVideo(view : View, disclaimer :String) {
        var aContext : Activity? = null
        if (view.context is Activity)
            aContext = (view.context as Activity)
        else if (view.context is ContextThemeWrapper)
            aContext = ((view.context as ContextThemeWrapper).baseContext as Activity)

        if (aContext != null) {
            val customLayout = LayoutInflater.from(aContext).inflate(R.layout.disclaimer_dialog_layout, null)
            val dialog = AlertDialog.Builder(aContext!!).create()

            val spannable : Spannable = HtmlCompat.fromHtml(FontHelper.getFontConvertedString(disclaimer), HtmlCompat.FROM_HTML_MODE_LEGACY) as Spannable
            val str = spannable.toString()
            var lines = str.lines()
            val disclaimerText = lines[2]+" : "+lines[4]

            customLayout.disclaimer_display_text_view.text = disclaimerText
            customLayout.disclaimer_close.setOnClickListener {
               dialog.cancel()
            }
            dialog.setView(customLayout)
            dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.show()
        }
    }

    fun openPhoto(postId: String, index: Int, childFetchUrl: String?, post: CommonAsset?) {
        val intent = android.content.Intent(Constants.GALLERY_PHOTO_ACTION)
        intent.putExtra(Constants.BUNDLE_EVENT_NAME, Constants.GALLERY_PHOTO_ACTION)
        intent.putExtra(Constants.BUNDLE_POST_ID, postId)
        intent.putExtra(Constants.CONTENT_URL, childFetchUrl)
        intent.putExtra(Constants.COLLECTION_SELECTED_INDEX, index)
        intent.putExtra(Constants.BUNDLE_STORY, post)
        intent.putExtra(Constants.SHOW_SHARE_VIEW, false)
        NavigationHelper.navigationLiveData.postValue(NavigationEvent(intent))
    }

    fun executeDetailCardUsecase(postEntityLevel: String, adId: String? = null) {
        readDetailCardUsecase.execute(bundleOf(Constants.POST_ENTITY_LEVEL to postEntityLevel,
                Constants.BUNDLE_AD_ID to adId))
    }

    fun fetchNextDiscussionPage() {
        if (discussionNextPageUrl != null) {
            val bundle = android.os.Bundle()
            bundle.putString(Constants.CONTENT_URL, discussionNextPageUrl)
            discussionLoading.postValue(true)
            discussionNwUsecase.execute(bundle)
        }
    }

    fun markNlfcConsumed(id: String) {
        nonLinearConsumeUsecase.execute(id)
    }

    fun onShowAllCommentsClick(view: View, card: CommonAsset?) {
        onShowAllCommentsClick(view, card, false)
    }

    fun onShowAllRepostsClick(view: View, card: CommonAsset?, isRepost: Boolean, vm: CardsViewModel) {
        val value = card?.i_counts()?.REPOST?.value
        if (card != null && (value.isNullOrBlank() || value == "0")) {
            vm.onViewClick(view, card)
            return
        }
        onShowAllCommentsClick(view, card, isRepost)
    }
    fun onShowAllCommentsClick(view: View, card: CommonAsset?, isRepost: Boolean) {
        if (card == null) {
            return
        }

        val intent = Intent(Constants.ALL_COMMENTS_ACTION)
        intent.putExtra(Constants.BUNDLE_POST_ID, card.i_id())
        intent.putExtra(Constants.BUNDLE_PARENT_ID, card.i_parentPostId())
        intent.putExtra(Constants.BUNDLE_ACTIVITY_TITLE, card.i_title())
        intent.putExtra(Constants.BUNDLE_ACTIVITY_REFERRER, pageReferrer)
        intent.putExtra(Constants.BUNDLE_SOURCE_ID, card.i_source()?.id)
        intent.putExtra(Constants.BUNDLE_SOURCE_TYPE, card.i_source()?.type)
        intent.putExtra(NewsConstants.DH_SECTION, section)
        intent.putExtra("isAllFilter", filterTypes?.any { it.displayName == "All" })
        intent.putExtra("isCommentsFilter", filterTypes?.any { it.displayName == "Comments" })
        intent.putExtra("isRepostFilter", filterTypes?.any { it.displayName == "Repost" })
        if(isRepost)
            intent.putExtra("preSelectFilter", AssetType2.REPOST.name)
        NavigationHelper.navigationLiveData.postValue(NavigationEvent(intent))
    }

    fun newComment(v: View, card: CommonAsset?){
        try {
            card?.let {logCommentClick(it)}
            val intent = CommonNavigator.getPostCreationIntent(
                    postId,
                    CreatePostUiMode.COMMENT,
                    null,
                    pageReferrer,
                    null,
                    card?.i_source()?.id,
                    card?.i_source()?.type,
                    null)
            if(v.context as? Activity == null) {
                (((v.context as? ContextWrapper)?.baseContext) as? Activity)?.startActivityForResult(intent, 0)
            } else {
                (v.context as? Activity)?.startActivityForResult(intent, 0)
            }
        } catch (ex: Exception) {
            // Activity not found
            Logger.caughtException(ex)
        }
    }

    private fun logCommentClick(item : CommonAsset) {
        AnalyticsHelper2.logCommentClick(NhAnalyticsEventSection.NEWS, item.i_id(),
                pageReferrer, null, item, COMMENT_CLICK_DETAIL)
    }

    fun onDiscussionFilterClick(view: View, mode: CreatePostUiMode?, card: CommonAsset?) {
        val newMode: CreatePostUiMode = mode ?: CreatePostUiMode.ALL

        if (discussionMode == newMode) {
            // Already is selected mode
            return
        }

        discussionLoading.postValue(true)
        discussionMode = newMode
        discussionNextPageUrl = null



        (view as NHTextView).setTextColor(android.graphics.Color.WHITE)

        val bundle = Bundle()
        val firstDiscussionUrl = firstPageUrl
        bundle.putString(Constants.CONTENT_URL, firstDiscussionUrl)
        bundle.putString(Constants.BUNDLE_MODE, newMode.name)
        val isDiscussionDetail = DetailsBindUtils.isDiscussion(card)
        if (isDiscussionDetail) {
            bundle.putString(Constants.BUNDLE_LEVEL, PostEntityLevel.DISCUSSION.name)
        }
        else {
            bundle.putString(Constants.BUNDLE_LEVEL, level)
        }

        clearAndDownloadFirstDiscussion.execute(bundle)
    }

    fun getEmptyDiscussionText(card: CommonAsset?): String {
        try {
            if (discussionMode == CreatePostUiMode.REPOST) {
                    return CommonUtils.getString(R.string.empty_repost)
            }
        }
        catch (ex: NumberFormatException) {
            return ""
        }

        return if (card?.i_allowComments() != false) {
            CommonUtils.getString(R.string.first_person_start_conversation)
        } else {
            CommonUtils.getString(R.string.comments_have_disabled)
        }
    }

    fun fetchMoreDetails(url: String?, postEntityLevel :String) {
        secondChunkLoading.postValue(true)
        val bundle = Bundle()
        bundle.putString(Constants.CONTENT_URL, url)
        bundle.putString(Constants.POST_ENTITY_LEVEL, postEntityLevel)
        networkAndUpdatePostUsecase.execute(bundle)
    }

    var mydiscussions: LiveData<Int>? = null
    var mydiscussionsRepliesCount: LiveData<List<ReplyCount>>? = null
    var myInteraction: LiveData<Interaction?>? = null
    lateinit var moreStoriesNP: LiveData<MoreStoriesPojo>

    fun includeLoadDiscussion() {
        addNewDiscussionUsecase.execute(bundleOf(
                Constants.BUNDLE_LEVEL to level,
                Constants.BUNDLE_MODE to discussionMode.name))
    }

    fun fetchMoreStoriesNextPage(nextPageUrl: String?) {
        val bundle = android.os.Bundle()
        bundle.putString(Constants.CONTENT_URL, nextPageUrl)
        fetchMoreStoriesUsecase.get().execute(bundle)
        moreStoriesNP = fetchMoreStoriesUsecase.get().data().scan(MoreStoriesPojo())
        { acc, t ->

            if (t.isSuccess) {

                if (t.getOrNull()?.rows != null) {
                    acc.data?.clear()
                    acc.data?.addAll(t.getOrNull()?.rows!!)
                }
                acc.copy(nextPageUrl = t.getOrNull()?.nextPageUrl, tsData = System.currentTimeMillis())
            } else {
                acc.copy(error = t.exceptionOrNull(), tsError = System.currentTimeMillis())
            }
        }
    }

    fun fetchsuggestedFollows() {
        suggestedFollowsUsecase.execute(android.os.Bundle())
    }


    lateinit var carouselMoreStoriesNP: LiveData<MoreStoriesPojo>
    fun fetchCarousalMoreStoriesNextPage(nextPageUrl: String?) {
        val bundle = android.os.Bundle()
        bundle.putString(Constants.CONTENT_URL, nextPageUrl)
        fetchCarouselMoreStoriesUsecase.get().execute(bundle)
        carouselMoreStoriesNP = fetchCarouselMoreStoriesUsecase.get().data().scan(MoreStoriesPojo()) { acc, t ->
            if (t.isSuccess) {
                if (t.getOrNull()?.rows != null) {
                    acc.data?.clear()
                    acc.data?.addAll(t.getOrNull()?.rows!!)
                }
                acc.copy(nextPageUrl = t.getOrNull()?.nextPageUrl, tsData = System.currentTimeMillis())
            } else {
                acc.copy(error = t.exceptionOrNull(), tsError = System.currentTimeMillis())
            }
        }
    }

    fun fetchPhotoChildIfRequired(card: CommonAsset?) {
        val subFormat = card?.i_subFormat()
        if (subFormat == SubFormat.S_W_PHOTOGALLERY || subFormat == SubFormat.RICH_PHOTOGALLERY) {
            fetchChildUrl = card.i_childFetchUrl()
            if (fetchChildUrl != null) {
                photoChildUsecase.execute(bundleOf(Constants.CONTENT_URL to fetchChildUrl))
            }
        }
    }

    override fun onLineCountAvailable(lineCount: Int) {
        titleLength = lineCount
        titleLengthCount.postValue(lineCount)
    }

    fun contentRead(post: CommonAsset?, referrer: PageReferrer?) {
        if (AssetType2.COMMENT.name == post?.i_type()) {
            return
        }
        post?.let { asset ->
            asset.i_format()?.let { format ->
                val thumbnailUrl = if (asset.i_linkAsset() != null) {
                    ImageDetail(asset.i_linkAsset()!!.thumbnailUrl,
                            asset.i_linkAsset()!!.thumbnailWidth?.toFloatOrNull()?:0f,
                            asset.i_linkAsset()!!.thumbnailHeight?.toFloatOrNull()?:0f)
                } else {
                    asset.i_thumbnailUrlDetails()?.firstOrNull()
                }
                addToHistoryUsecase.execute(buildHistoryEntity(
                        id = asset.i_id(),
                        format = format,
                        subFormat = asset.i_subFormat(),
                        uiType = asset.i_uiType(),
                        duration = asset.i_videoAsset()?.duration,
                        thumbnailUrl = thumbnailUrl,
                        title = asset.i_title(),
                        content = if (asset.i_title().isNullOrEmpty()) asset.i_content() else null,
                        srcImgUrl = asset.i_source()?.imageUrl,
                        srcName = asset.i_source()?.displayName,
                        isNSFW = asset.i_viral()?.nsfw ?: false,
                        hideControl = asset.i_videoAsset()?.hideControl ?: false
                ))
                AppsFlyerHelper.trackEvent(AppsFlyerEvents.EVENT_CONTENT_CONSUMED, null)
                AppsFlyerHelper.trackEvent(AppsFlyerEvents.EVENT_FIRST_DETAIL_VIEW, null)
                addToRecentArticles(asset, format, referrer)
            }
        }
    }
    private fun addToRecentArticles(asset: CommonAsset, format: Format, referrer: PageReferrer?) {
        val ref = "${referrer?.referrer?.referrerName}/${referrer?.referrerSource}"
        Logger.d(TAG, "addToRecentArticles(${asset.i_id()}, $format, $ref)")
        val subformat = asset.i_subFormat()
        if (subformat != null) {
            recentArticleTimestampStoreHelper.trackTimeSpentForArticle(
                    mapOf(
                            AnalyticsParam.ITEM_ID.getName() to asset.i_id(),
                            AnalyticsParam.FORMAT.getName() to format.name,
                            AnalyticsParam.SUB_FORMAT.getName() to subformat.name
                    ), emptyMap(), emptyArray(), 0, ref)
        } else {
            Logger.e(TAG, "not recording recentArticleTimestampStoreHelper. SubFormat null. id=${asset.i_id()}")
        }
    }

    fun loadRichGalleryContents() {
        secondChunkPending = false
        shortLikes = readLikesFirstPageUsecase.get().data().scan(LikeListPojo()) { acc, t ->
            hadDiscussionItems = t.getOrNull()?.discussions?.count?:0 > 0
            handleLikeFirstPageResponse(acc, t)
        }

        discussionLoading.postValue(true)
        readLikesFirstPageUsecase.get().execute(bundleOf(Constants.BUNDLE_LEVEL to level))
        if (mydiscussions == null) {
            mydiscussions = SocialDB.instance().cpDao().getByParent(postId)
            mydiscussionsRepliesCount = SocialDB.instance().cpDao().getReplyCount(postId)
            val likeTypesList = mutableListOf<String>()
            LikeType.values().forEach {
                likeTypesList.add(it.name)
            }

            myInteraction = SocialDB.instance().fetchDao().getInteractionsCount(postId, likeTypesList)
        }
    }

    fun fetchRelatedVideos(relatedUrl: String) {
        relatedVideos = relatedStoriesUsecase.get().data().scan(CardsPojo()) { acc, t ->
            if (t.isSuccess) {
                requestInProgress = false
                acc.copy(data = t.getOrNull(), tsData = System.currentTimeMillis())
            } else {
                requestInProgress = false
                acc.copy(error = t.exceptionOrNull(), tsError = System.currentTimeMillis())
            }
        }

        val bundle = Bundle()
        bundle.putString(Constants.CONTENT_URL, relatedUrl)
        bundle.putString(Constants.REQUEST_METHOD, Constants.HTTP_POST)

        relatedStoriesForVideoContentUsecase.execute(bundle)
        relatedStoriesUsecase.get().execute(bundle)
    }

    fun updateNextPageUrl() {
        GlobalScope.launch(Dispatchers.IO) {
            Logger.d("VINOD", "before nextUrl : $nextRelatedPageUrl")
            val fetchInfo = SocialDB.instance().fetchDao().fetchInfo(entityId, location, section)
            Logger.d("VINOD", "after nextUrl : ${fetchInfo?.nextPageUrl}")
            nextRelatedPageUrl = fetchInfo?.nextPageUrl
        }
    }

    private var requestInProgress: Boolean = false
    private var nextRelatedPageUrl: String? = null
    fun updateCurrentCardLocation(visibleItemCount: Int, firstVisibleItem: Int, totalItemCount: Int) {
        Logger.v(TAG, "$entityId: nVis=$visibleItemCount, 1st=$firstVisibleItem, nTot=$totalItemCount")
        if (requestInProgress || nextRelatedPageUrl.isNullOrEmpty()) {
            Logger.v(TAG, "$entityId: nextPageUrl is NULL")
            return
        }
        if (totalItemCount > 1) {
            if (totalItemCount <= firstVisibleItem + PREFETCH_ITEM_THRESHOLD) {
                requestInProgress = true
                fetchRelatedVideos(nextRelatedPageUrl!!)
            }
        }
    }

    fun loadSeconChunkContents(isDiscussionDetail: Boolean) {
        if (secondChunkPending) {
            secondChunkPending = false
            relatedstories = relatedStoriesUsecase.get().data().scan(CardsPojo()) { acc, t ->
                if (t.isSuccess) {
                    acc.copy(data = t.getOrNull(), tsData = System.currentTimeMillis())
                } else {
                    acc.copy(error = t.exceptionOrNull(), tsError = System.currentTimeMillis())
                }
            }

            shortLikes = readLikesFirstPageUsecase.get().data().scan(LikeListPojo()) { acc, t ->
                discussionLoading.postValue(false)
                handleLikeFirstPageResponse(acc, t)
            }

            relatedStoriesFrommAddContentUsecase.execute(Bundle.EMPTY)
            relatedStoriesUsecase.get().execute(Bundle.EMPTY)

            val bundle = Bundle()
            if (isDiscussionDetail) {
                bundle.putString(Constants.BUNDLE_LEVEL, PostEntityLevel.DISCUSSION.name)
            } else {
                bundle.putString(Constants.BUNDLE_LEVEL, level)
            }

            discussionLoading.postValue(true)
            readLikesFirstPageUsecase.get().execute(bundle)

            if (mydiscussions == null) {
                mydiscussions = SocialDB.instance().cpDao().getByParent(postId)
                mydiscussionsRepliesCount = SocialDB.instance().cpDao().getReplyCount(postId)
                val likeTypesList = mutableListOf<String>()
                LikeType.values().forEach {
                    likeTypesList.add(it.name)
                }

                myInteraction = SocialDB.instance().fetchDao().getInteractionsCount(postId, likeTypesList)
            }

            fetchSupplementAdsTitle()
        }
    }

    private fun handleLikeFirstPageResponse(acc: LikeListPojo, t: Result0<LikesResponse>): LikeListPojo {
        if (t.isSuccess) {
            failedNetworkCalls.remove(SHORT_LIKES_UC)
            discussionNextPageUrl = t.getOrNull()?.discussions?.nextPageUrl
            if (t.getOrNull()?.discussions == null) {
                discussionLoading.postValue(false)
                discussionFirstPage
            }

            firstPageUrl = t.getOrNull()?.discussions?.firstPageUrl
            filterTypes = t.getOrNull()?.discussions?.filter
            if (!filterTypes.isNullOrEmpty() && filterTypes?.get(0)?.filterValue != discussionMode.name) {
                when (filterTypes?.get(0)?.filterValue) {
                    CreatePostUiMode.COMMENT.name -> {
                        discussionMode = CreatePostUiMode.COMMENT
                    }
                    CreatePostUiMode.REPOST.name -> {
                        discussionMode = CreatePostUiMode.REPOST
                    }
                }
            }

            var discussions = t.getOrNull()?.discussions?.rows
            if (discussions != null && discussionMode != CreatePostUiMode.ALL) {
                val filterValue = if (discussionMode == CreatePostUiMode.REPOST)
                    AssetType2.REPOST.name
                else
                    AssetType2.COMMENT.name
                discussions = discussions.filter {
                    it.i_type() == filterValue
                }
            }

            return acc.copy(data = t.getOrNull()?.likes?.rows,
                    count = t.getOrNull()?.likes?.count?.toInt(),
                    guestUserCount = t.getOrNull()?.likes?.guestUserCount,
                    total = t.getOrNull()?.likes?.total,
                    loggedInUserCount = t.getOrNull()?.likes?.loggedInUserCount,
                    discussions = discussions,
                    error = null,
                    discussionNextPageUrl = t.getOrNull()?.discussions?.nextPageUrl,
                    tsData = System.currentTimeMillis())
        } else {
            failedNetworkCalls.add(SHORT_LIKES_UC)
            discussionLoading.postValue(false)
            return acc.copy(error = t.exceptionOrNull(), tsError = System.currentTimeMillis())
        }
    }

    private fun fetchSupplementAdsTitle() {
         additionalContentFetchUsecase.execute(Constants.ADS)
    }

    fun reportAd(adId: String?) {
        adId ?: return
        clearAdsUseCase.execute(ClearAdsDataUsecase.bundle(adId, reported = true))
    }

    override fun onCleared() {
        super.onCleared()
        clearAndDownloadFirstDiscussion.dispose()
        cloneFetchForNewsDetailUsecase.dispose()
        addToHistoryUsecase.dispose()
        insertProxyAdUsecase.dispose()
    }

    fun insertAdCardIfNeeded(baseAdEntity: BaseAdEntity) {
        if (baseAdEntity.type != AdContentType.CONTENT_AD && baseAdEntity.contentAsset is PostEntity) {
            insertProxyAdUsecase.execute(InsertProxyAdUsecase.bundle(baseAdEntity))
            addExtraAdId(baseAdEntity.contentAsset?.i_id())
        }
    }

    private fun addExtraAdId(id: String?) {
        id ?: return
        if (extraAdCardIds.contains(id)) return
        extraAdCardIds.add(AdConstants.AD_PROXY_FETCH_ID.plus(Constants.UNDERSCORE_CHARACTER).plus(id))
        extraCardsIds.value = extraAdCardIds
    }

    class Factory @Inject constructor(private val app: Application,
                                      private val lifecycleOwner: LifecycleOwner,
                                      @Named("section") private val section: String,
                                      @Named("entityId") private val entityId: String,
                                      @Named("postId") private val postId: String,
                                      @Named("level") private val level: String,
                                      @Named("location") private val location: String,
                                      @Named("listLocation") private val listLocation: String,
                                      @Named("adId") private val adId: String?,
                                      @Named("timeSpentEventId")
                                      private val timeSpentEventId: Long,
                                      @Named("isInBottomSheet")
                                      private val isInBottomSheet: Boolean,
                                      @Named("referrerFlow")
                                      private val referrerFlow: PageReferrer,
                                      private val networkAndUpdatePostUsecase: ReadDetailedFromNetworkAndUpdatePostUsecase,
                                      private val readDetailCardUsecase: ReadDetailCardUsecase,
                                      private val readFullPostUsecase: ReadFullPostUsecase,
                                      private val relatedStoriesFrommAddContentUsecase:
                                      RelatedStoriesFrommAddContentUsecase,
                                      private val relatedStoriesForVideoContentUsecase:
                                      RelatedStoriesForVideoContentUsecase,
                                      private val suggestedFollowUsecase: SuggestedFollowUsecase,
                                      private val discussionNetworkUsecase: DiscussionNetworkUsecase,
                                      private val clearAndDownloadFirstDiscussion: ClearAndDownloadFirstDiscussion,
                                      private val addNewDiscussionUsecase: AddNewDiscussionUsecase,
                                      @Named("lazyRelatedStoriesUsecase")
                                      private val lazyRelatedStoriesUsecase: Lazy<MediatorUsecase<Bundle, List<TopLevelCard>>>,
                                      @Named("lazyFetchMoreStoriesUsecase")
                                      private val lazyFetchMoreStoriesUsecase: Lazy<MediatorUsecase<Bundle, MultiValueResponse<CommonAsset>>>,
                                      @Named("lazyFetchCarouselMoreStoriesUsecase")
                                      private val lazyFetchCarouselMoreStoriesUsecase: Lazy<MediatorUsecase<Bundle, MultiValueResponse<CommonAsset>>>,
                                      @Named("lazyReadLikesFirstPageUsecase")
                                      private val lazyReadLikesFirstPageUsecase: Lazy<MediatorUsecase<Bundle, LikesResponse>>,
                                      private val photoChildUsecase: PhotoChildUsecase,
                                      private val newsAppJSProviderService: NewsAppJSProviderService,
                                      private val cloneFetchForNewsDetailUsecase: CloneFetchForNewsDetailUsecase,
                                      private val addToHistoryUsecase: AddToHistoryUsecase,
                                      private val fetchParentNwUsecase: FetchParentNwUsecase,
                                      private val deleteCommentUsecase: DeleteCommentUsecase,
                                      private val reportCommentUsecase: ReportCommentUsecase,
                                      private val additionalContentFetchUsecase: AdditionalContentFetchUsecase,
                                      private val nonLinearConsumeUsecase: NonLinearConsumedUsecase,
                                      private val clearAdsUseCase: ClearAdsDataUsecase,
                                      private val insertProxyAdUsecase: InsertProxyAdUsecase,
                                      private val cardDao: CardDao) :
            ViewModelProvider.AndroidViewModelFactory(app) {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return DetailsViewModel(app, lifecycleOwner, entityId, postId, section, level,
                    location, listLocation, adId,
                    timeSpentEventId, isInBottomSheet, referrerFlow,
                    networkAndUpdatePostUsecase.toMediator2(true),
                    readDetailCardUsecase.toMediator2(true),
                    readFullPostUsecase.toMediator2(true),
                    relatedStoriesFrommAddContentUsecase.toMediator2(true),
                    relatedStoriesForVideoContentUsecase.toMediator2(true),
                    suggestedFollowUsecase.toMediator2(true),
                    discussionNetworkUsecase.toMediator2(true),
                    clearAndDownloadFirstDiscussion.toMediator2(false),
                    addNewDiscussionUsecase.toMediator2(false),
                    lazyRelatedStoriesUsecase,
                    lazyFetchMoreStoriesUsecase,
                    lazyFetchCarouselMoreStoriesUsecase,
                    lazyReadLikesFirstPageUsecase,
                    photoChildUsecase.toMediator2(true),
                    newsAppJSProviderService,
                    cloneFetchForNewsDetailUsecase.toMediator2(true),
                    addToHistoryUsecase.toMediator2(),
                    fetchParentNwUsecase.toMediator2(),
                    deleteCommentUsecase.toMediator2(),
                    reportCommentUsecase.toMediator2(),
                    additionalContentFetchUsecase.toMediator2(),
                    nonLinearConsumeUsecase.toMediator2(),
                    clearAdsUseCase.toMediator2(),
                    insertProxyAdUsecase.toMediator2(),
                    cardDao)
                    as T
        }
    }

    companion object {
        const val TAG = "DetailsViewModel"
        private const val PREFETCH_ITEM_THRESHOLD = 3
        const val COMMENT_CLICK_DETAIL = "comment_click_detail"
    }
}