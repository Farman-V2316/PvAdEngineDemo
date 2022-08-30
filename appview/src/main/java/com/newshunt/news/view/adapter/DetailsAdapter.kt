package com.newshunt.news.view.adapter

import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.newshunt.adengine.listeners.InteractiveAdListener
import com.newshunt.adengine.listeners.ReportAdsMenuListener
import com.newshunt.adengine.model.entity.BaseAdEntity
import com.newshunt.adengine.model.entity.ContentAd
import com.newshunt.adengine.model.entity.ContentAdDelegate
import com.newshunt.adengine.model.entity.EmptyAd
import com.newshunt.adengine.model.entity.version.AdPosition
import com.newshunt.adengine.util.AdLogger
import com.newshunt.adengine.util.AdsUtil
import com.newshunt.adengine.view.AdEntityReplaceHandler
import com.newshunt.adengine.view.UpdateableAdView
import com.newshunt.adengine.view.helper.PostAdsHelper
import com.newshunt.adengine.view.viewholder.NativeAdHtmlViewHolder
import com.newshunt.appview.common.entity.CardsPojo
import com.newshunt.appview.common.ui.helper.CardsBindUtils
import com.newshunt.appview.common.ui.helper.ObservableDataBinding
import com.newshunt.appview.common.viewmodel.CardsViewModel
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.view.view.BaseSupportFragment
import com.newshunt.common.view.view.UniqueIdHelper
import com.newshunt.dataentity.common.asset.AssetType2
import com.newshunt.dataentity.common.asset.Card
import com.newshunt.dataentity.common.asset.CommonAsset
import com.newshunt.dataentity.common.asset.DetailListCard
import com.newshunt.dataentity.common.asset.DiscussionPojo
import com.newshunt.dataentity.common.asset.Format
import com.newshunt.dataentity.common.asset.LikeAsset
import com.newshunt.dataentity.common.asset.LikeListPojo
import com.newshunt.dataentity.common.asset.PhotoChildPojo
import com.newshunt.dataentity.common.asset.PostEntity
import com.newshunt.dataentity.common.asset.SubFormat
import com.newshunt.dataentity.common.asset.SuggestedFollowsPojo
import com.newshunt.dataentity.common.asset.UiType2
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.common.model.entity.BaseError
import com.newshunt.dataentity.common.model.entity.server.asset.NewsAppJS
import com.newshunt.dataentity.common.pages.ActionableEntity
import com.newshunt.dataentity.news.model.entity.DetailCardType
import com.newshunt.dataentity.social.entity.Interaction
import com.newshunt.dataentity.social.entity.Position
import com.newshunt.dataentity.social.entity.ReplyCount
import com.newshunt.news.helper.DetailAdapterHelper
import com.newshunt.news.helper.LikeEmojiBindingUtils
import com.newshunt.news.model.daos.CardDao
import com.newshunt.news.view.fragment.DetailAdapterDiffUtilCallback
import com.newshunt.news.view.fragment.PostActions
import com.newshunt.news.view.fragment.UpdateableDetailView
import com.newshunt.news.viewmodel.DetailsViewModel
import com.newshunt.sso.SSO

class DetailsAdapter(
    private val activity: AppCompatActivity,
    private val fragment: BaseSupportFragment,
    private val parentLifecycleOwner: LifecycleOwner,
    private val detailsViewModel: DetailsViewModel,
    private val cvm: CardsViewModel,
    private var detailListingOrder: List<String>,
    private var card: CommonAsset?,
    private var bootStrapCard: DetailListCard?,
    var parentCard: CommonAsset?,
    private var suggedtedFollowsPojo: SuggestedFollowsPojo?,
    private var likedListPojo: LikeListPojo?,
    private var relatedStories: CardsPojo?,
    private var isInBottomSheet: Boolean,
    private var postListener: PostActions,
    private var error: ObservableDataBinding<BaseError>,
    private var uniqueRequestId: Int,
    private val adEntityReplaceHandler: AdEntityReplaceHandler,
    private val interactiveAdListener: InteractiveAdListener,
    private val webCacheProvider: NativeAdHtmlViewHolder.CachedWebViewProvider,
    private var timeSpentEventId: Long,
    private var section: String,
    private val detailList: RecyclerView,
    private var adsHelper: PostAdsHelper?,
    private val contentAdDelegate: ContentAdDelegate?,
    private var reportAdsMenuListener: ReportAdsMenuListener? = null,
    private val detailAdapterHelper: DetailAdapterHelper =
        DetailAdapterHelper(
            detailsViewModel, cvm, postListener, fragment,
            parentLifecycleOwner, uniqueRequestId, card?.i_deeplinkUrl(),
            adEntityReplaceHandler, interactiveAdListener, section,
            webCacheProvider, detailList, isInBottomSheet,
            parentLifecycleOwner, contentAdDelegate,
            reportAdsMenuListener = reportAdsMenuListener
        ),
    var titleLength: Int = 0,
    var titleLengthOld: Int = 0
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val TAG = "DetailsAdapter${UniqueIdHelper.getInstance().generateUniqueId()}"
    var oldCard: CommonAsset? = null
    var discussionPojo: DiscussionPojo? = null
    var oldDiscussionPojo: DiscussionPojo? = null
    var associationPojo: CardsPojo? = null
    var replyCount: List<ReplyCount>? = null
    var myInteraction: Interaction? = null
    var likedListPojoParam: LikeListPojo? = null

    private val visibleListingOrder = ArrayList<String>()
    private var photoChildPojo: PhotoChildPojo? = null
    private var newsAppJSChunk1: NewsAppJS? = null
    private var newsAppJSChunk2: NewsAppJS? = null
    var adsMap = mutableMapOf<String, BaseAdEntity>()
    private val extraCards = mutableMapOf<String, Card>()
    private val likeTypeList = mutableMapOf<String, CardDao.Interaction>()
    var secondChunkLoaded = false
    private val TITLE_TOP_LINE_COUNT_THRESHOLD = 4

    init {
        updateVisibleList()
    }

    internal fun updateVisibleList() {
        val visibleList = ArrayList<String>()

        detailListingOrder.forEach { type ->
            when (type) {
                DetailCardType.SOURCE.name -> {
                    if (card?.i_type() != AssetType2.COMMENT.name) {
                        if ((card != null || bootStrapCard != null) && secondChunkLoaded) {
                            visibleList.add(type)
                        }
                    }
                }
                DetailCardType.SOURCE_TIME.name -> {
                    if (canShowSource() ) visibleList.add(type)

                }
                DetailCardType.TITLE.name -> {
                    if (canShowTitle())
                            visibleList.add(type)

                }
                DetailCardType.OGCARD.name -> card?.i_linkAsset()?.let { visibleList.add(type) }
                DetailCardType.REPOST.name -> card?.i_repostAsset()?.let { visibleList.add(type) }
                DetailCardType.IMAGE.name -> {
                    if (card?.i_uiType() != UiType2.HERO_DYNAMIC) {
                        if (card?.i_subFormat() == SubFormat.S_W_ATTACHED_IMAGES ||
                            card?.i_subFormat() == SubFormat.S_W_IMAGES ||
                            card?.i_subFormat() == SubFormat.STORY|| card?.i_subFormat() == SubFormat.S_W_PHOTOGALLERY
                        ) {
                            visibleList.add(type)
                        }
                    }
                }
                DetailCardType.IMAGE_DYNAMIC.name -> {
                    if (card?.i_uiType() == UiType2.HERO_DYNAMIC) {
                        if (card?.i_subFormat() == SubFormat.S_W_ATTACHED_IMAGES ||
                            card?.i_subFormat() == SubFormat.S_W_IMAGES ||
                            card?.i_subFormat() == SubFormat.STORY|| card?.i_subFormat() == SubFormat.S_W_PHOTOGALLERY
                        ) {
                            visibleList.add(type)
                        } else if (bootStrapCard?.i_subFormat() == SubFormat.S_W_ATTACHED_IMAGES ||
                            bootStrapCard?.i_subFormat() == SubFormat.S_W_IMAGES ||
                            bootStrapCard?.i_subFormat() == SubFormat.STORY
                        ) {
                            visibleList.add(type)
                        }
                    }
                }
                DetailCardType.POLL.name -> card?.let { visibleList.add(type) }
                DetailCardType.TIME.name -> card?.let { visibleList.add(type) }
                DetailCardType.LOCATION.name -> card?.let {
                    visibleList.add(type)
                }
                DetailCardType.CHUNK1.name -> {
                    if (card?.i_type() != AssetType2.COMMENT.name) {
                        if (card?.i_content() != null && this.newsAppJSChunk1 != null) {
                            visibleList.add(type)
                        }
                    }
                }
                DetailCardType.CHUNK2.name -> if (card?.i_content2() != null && this.newsAppJSChunk2 != null) {
                    visibleList.add(type)
                }

                DetailCardType.SEEPOST.name -> if (card?.i_type() != null && card?.i_type() == AssetType2.COMMENT.name) {
                    visibleList.add(type)
                }
                DetailCardType.DISCLAIMER.name -> card?.i_disclaimer()?.let {
                    if (secondChunkLoaded) {
                        visibleList.add(type)
                    }
                }
                DetailCardType.HASHTAGS.name -> if (card?.i_hashtags()
                        ?.isNotEmpty() == true && secondChunkLoaded
                ) {
                    visibleList.add(type)
                }
                DetailCardType.OTHER_PERSPECTIVES.name -> if (!card?.i_moreStories()
                        .isNullOrEmpty() && secondChunkLoaded && !detailsViewModel.isInCollection
                ) {
                    visibleList.add(type)
                }
                DetailCardType.LIKES_LIST.name -> if (card != null && secondChunkLoaded) {
                    visibleList.add(type)
                }
                DetailCardType.SPACER.name -> {
                    visibleList.add(type)
                }
                DetailCardType.DISCUSSION_HEADER.name -> {
                    if (secondChunkLoaded) {
                        if (card != null && !detailsViewModel.hadDiscussionItems) {
                            val discussionSize = discussionPojo?.data?.size ?: 0
                            detailsViewModel.hadDiscussionItems = (discussionSize > 0)
                        }

                        if (card != null && card?.i_type() != AssetType2.COMMENT.name) {
                            visibleList.add(type)
                        }
                    }
                }
                DetailCardType.DISCUSSION_LOADER.name -> {
                    if (secondChunkLoaded && card != null &&
                        detailsViewModel.discussionLoading.value == true &&
                        card?.i_type() != AssetType2.COMMENT.name
                    ) {
                        visibleList.add(type)
                    }
                }
                DetailCardType.DISCUSSION_SHOW_ALL.name -> {
                    if (secondChunkLoaded && card != null &&
                        detailsViewModel.discussionLoading.value == false &&
                        card?.i_type() != AssetType2.COMMENT.name
                    ) {
                        visibleList.add(type) // always add, as this layout has 'repost' button
                    }
                }
                DetailCardType.SECOND_CHUNK_LOADING.name -> {
                    if (detailsViewModel.secondChunkLoading.value == true) {
                        visibleList.add(type)
                    }
                }
                DetailCardType.DISCUSSION.name -> if (((discussionPojo?.data != null &&
                            discussionPojo?.data?.size!! > 0)) && secondChunkLoaded
                ) {
                    detailsViewModel.discussionIndex = visibleList.size
                    for (i in 0 until (discussionPojo?.data?.size ?: 0)) {
                        visibleList.add(type)
                    }
                }
                DetailCardType.DISCUSSION_NS.name -> if (((discussionPojo?.data != null &&
                            discussionPojo?.data?.size!! > 0)) && secondChunkLoaded
                ) {
                    detailsViewModel.discussionIndex = visibleList.size
                    for (i in 0 until (discussionPojo?.data?.size ?: 0)) {
                        visibleList.add(type)
                    }
                }
                DetailCardType.MAIN_COMMENT.name -> if (card != null && card?.i_type() == AssetType2.COMMENT.name) {
                    visibleList.add(type)
                }
                DetailCardType.SEE_IN_VIDEO.name -> if (associationPojo?.data?.isNotEmpty() == true && secondChunkLoaded) {
                    visibleList.add(type)
                }
                DetailCardType.SUPPLEMENTARY_RELATED.name -> if (relatedStories != null &&
                    (relatedStories?.data?.size ?: 0) > 0 && secondChunkLoaded
                ) {
                    visibleList.add(type)
                }
                DetailCardType.VIRAL.name -> card?.let { visibleList.add(type) }
                DetailCardType.RICH_GALLERY.name -> if (card?.i_subFormat() == SubFormat.RICH_PHOTOGALLERY &&
                    card?.i_thumbnailUrls()?.isNotEmpty() == true
                ) {
                    visibleList.add(type)
                }
//				DetailCardType.PHOTO_GALLERY.name -> if (photoChildPojo?.data?.isNotEmpty() == true) {
//					photoChildPojo?.index = visibleList.size
//					for (i in 0 until (photoChildPojo?.data?.size ?: 0)) {
//						visibleList.add(type)
//					}
//				}
                DetailCardType.READMORE.name -> if (detailsViewModel.failedNetworkCalls.isNotEmpty()) {
                    visibleList.add(type)
                }
                DetailCardType.STORYPAGE.name -> if (checkAdValidity(
                        visibleList,
                        AdPosition.STORY.value
                    )
                ) {
                    visibleList.add(type)
                }
                DetailCardType.MASTHEAD.name -> if (checkAdValidity(
                        visibleList,
                        AdPosition.MASTHEAD.value
                    )
                ) {
                    visibleList.add(type)
                }
            }
            // SUPPLEMENT would have been replaced by keys like 'supplement-sp1 ...'
            if (secondChunkLoaded && type.startsWith(
                    DetailCardType.SUPPLEMENT.name,
                    ignoreCase = true
                ) &&
                checkAdValidity(visibleList, AdPosition.SUPPLEMENT.value, type)
            ) {
                if (isAdRenderable(AdPosition.SUPPLEMENT.value, type) &&
                    !visibleList.contains(DetailCardType.AD_SUPPLEMENT_HEADER.name)
                ) {
                    visibleList.add(DetailCardType.AD_SUPPLEMENT_HEADER.name)
                }
                visibleList.add(type)
            }
        }

        val diffCallback = DetailAdapterDiffUtilCallback(
            visibleListingOrder, visibleList,
            oldDiscussionPojo, discussionPojo, oldCard, card, titleLength, titleLengthOld
        )

        val diffResult = DiffUtil.calculateDiff(diffCallback)
        visibleListingOrder.clear()
        visibleListingOrder.addAll(visibleList)
        diffResult.dispatchUpdatesTo(this)
        oldDiscussionPojo = discussionPojo
        oldCard = card
        titleLengthOld = titleLength
    }

    fun updateReplyCount(replyCount: List<ReplyCount>?) {
        this.replyCount = replyCount
        notifyDiscussions()
    }

    /**
     * Pass cardType for cases when adsMap key is different from zone.
     * e.g. cardType : supplement-sp2, but zone : supplement
     */
    private fun checkAdValidity(
        visibleList: ArrayList<String>,
        zone: String,
        cardType: String? = null
    ): Boolean {
        return adsHelper != null && adsMap.containsKey(cardType ?: zone) &&
                AdsUtil.checkShowIf(visibleList, adsHelper?.getAdConfigByAdZone(zone))
    }
    private fun canShowSource(): Boolean {
        return card?.i_source()?.type.equals("UGC", true) || titleLength > TITLE_TOP_LINE_COUNT_THRESHOLD ||(card != null && CommonUtils.isEmpty(card?.i_title()))||card?.i_uiType()==UiType2.GRID_2||card?.i_uiType()==UiType2.GRID_3||card?.i_uiType()==UiType2.GRID_4||card?.i_uiType()==UiType2.GRID_5|| card?.i_subFormat()==SubFormat.S_W_VIDEO
    }
    private fun canShowTitle(): Boolean {
        return card != null  && !CommonUtils.isEmpty(card?.i_title())&& (titleLength > TITLE_TOP_LINE_COUNT_THRESHOLD ||card?.i_uiType()==UiType2.GRID_2||card?.i_uiType()==UiType2.GRID_3||card?.i_uiType()==UiType2.GRID_4||card?.i_uiType()==UiType2.GRID_5|| card?.i_subFormat()==SubFormat.S_W_VIDEO ||  card?.i_source()?.type.equals("UGC", true)) && card?.i_type() != AssetType2.COMMENT.name
    }


    /**
     * Can the ad show up on UI.
     */
    private fun isAdRenderable(zone: String, cardType: String? = null): Boolean {
        if (adsHelper == null) {
            return false
        }
        val ad = adsMap[cardType ?: zone] ?: return false
        return ad !is EmptyAd
    }

    fun updateAdsWithExtraMeta(cards: List<Card>) {
        extraCards.clear()
        extraCards.putAll(cards.map { card ->
            card.id to card
        })
        adsMap.map {
            val ad = it.value
            if (extraCards.containsKey(ad.contentAsset?.i_id())) {
                transformCardItem(ad)
                notifyDetailWidget(if (ad.adPosition != AdPosition.SUPPLEMENT) it.key.uppercase() else it.key)
            }
        }
    }

    fun updateLikeTypeList(likes: List<CardDao.Interaction>) {
        likeTypeList.clear()
        likeTypeList.putAll(
            likes.map {
                it.entity_id to it
            }
        )
        adsMap.map {
            val ad = it.value
            if (extraCards.containsKey(ad.contentAsset?.i_id())) {
                transformCardItem(ad)
                notifyDetailWidget(if (ad.adPosition != AdPosition.SUPPLEMENT) it.key.uppercase() else it.key)
            }
        }
    }

    private fun transformCardItem(item: Any?, level: Int = 0): Any? {
        if (level > 1) {
            /*To stop going into loop*/
            return item
        }
        if (item is CommonAsset) {
            val extraCard = extraCards[item.i_id()]
            val liketype = likeTypeList[item.i_id()]?.col_action

            if (item is BaseAdEntity && item !is ContentAd) {
                item.contentAsset = item.contentAsset?.let {
                    transformCardItem(item.contentAsset, level + 1) as PostEntity
                }
            }
            extraCard?.rootPostEntity()?.counts?.let {
                item.rootPostEntity()?.counts = it
            }
            return item.copyWith(
                selectedLikeType = liketype,
                coldStartItems = null,
                collectionItems = null
            )
        } else {
            return item
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return detailAdapterHelper.getViewHolder(
            viewType, parent, activity, card, bootStrapCard,
            cvm, relatedStories, newsAppJSChunk1, newsAppJSChunk2, error, timeSpentEventId
        )
    }

    override fun getItemCount(): Int {
        return visibleListingOrder.size
    }

    fun getPositionofEndofList(): Int {

        if (visibleListingOrder.indexOf(DetailCardType.CHUNK2.name) == -1) {
            return visibleListingOrder.size
        }
        return visibleListingOrder.indexOf(DetailCardType.CHUNK2.name)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is UpdateableDetailView -> holder.onBindView(
                position, cvm, detailsViewModel, card,
                parentCard, suggedtedFollowsPojo, likedListPojoParam, myInteraction, discussionPojo,
                relatedStories, associationPojo, photoChildPojo, isInBottomSheet, replyCount
            )
            is UpdateableAdView -> holder.updateView(
                activity,
                transformCardItem(getAdForPosition(position)) as BaseAdEntity
            )
        }
    }

    override fun getItemViewType(position: Int): Int {
        when (visibleListingOrder[position]) {
            DetailCardType.SOURCE.name -> return DetailCardType.SOURCE.index
            DetailCardType.TITLE.name -> return DetailCardType.TITLE.index
            DetailCardType.TIME.name -> return DetailCardType.TIME.index
            DetailCardType.IMAGE.name -> return getContentTypeForAlbum()
            DetailCardType.IMAGE_DYNAMIC.name -> return DetailCardType.IMAGE_DYNAMIC.index
            DetailCardType.CHUNK1.name -> return DetailCardType.CHUNK1.index
            DetailCardType.CHUNK2.name -> return DetailCardType.CHUNK2.index
            DetailCardType.LOCATION.name -> return DetailCardType.LOCATION.index
            DetailCardType.HASHTAGS.name -> return DetailCardType.HASHTAGS.index
            DetailCardType.SUGGESTED_FOLLOW.name -> return DetailCardType.SUGGESTED_FOLLOW.index
            DetailCardType.LIKES_LIST.name -> return DetailCardType.LIKES_LIST.index
            DetailCardType.SPACER.name -> return DetailCardType.SPACER.index
            DetailCardType.DISCUSSION_HEADER.name -> return DetailCardType.DISCUSSION_HEADER.index
            DetailCardType.DISCUSSION_LOADER.name -> return DetailCardType.DISCUSSION_LOADER.index
            DetailCardType.DISCUSSION_SHOW_ALL.name -> return DetailCardType.DISCUSSION_SHOW_ALL.index
            DetailCardType.DISCUSSION.name -> return DetailCardType.DISCUSSION.index
            DetailCardType.DISCUSSION_NS.name -> return DetailCardType.DISCUSSION_NS.index
            DetailCardType.VIRAL.name -> return DetailCardType.VIRAL.index
            DetailCardType.SUPPLEMENTARY_RELATED.name -> return DetailCardType.SUPPLEMENTARY_RELATED.index
            DetailCardType.OTHER_PERSPECTIVES.name -> return DetailCardType.OTHER_PERSPECTIVES.index
            DetailCardType.OGCARD.name -> return DetailCardType.OGCARD.index
            DetailCardType.REPOST.name -> return DetailCardType.REPOST.index
            DetailCardType.DISCLAIMER.name -> return DetailCardType.DISCLAIMER.index
            DetailCardType.RICH_GALLERY.name -> return DetailCardType.RICH_GALLERY.index
            DetailCardType.SEE_IN_VIDEO.name -> return DetailCardType.SEE_IN_VIDEO.index
            DetailCardType.READMORE.name -> return DetailCardType.READMORE.index
            DetailCardType.POLL.name -> return getContentTypeForPolls()
            DetailCardType.SHIMMER.name -> return DetailCardType.SHIMMER.index
            DetailCardType.SEEPOST.name -> return DetailCardType.SEEPOST.index
            DetailCardType.SECOND_CHUNK_LOADING.name -> return DetailCardType.SECOND_CHUNK_LOADING.index
            DetailCardType.MAIN_COMMENT.name -> return DetailCardType.MAIN_COMMENT.index
            DetailCardType.AD_SUPPLEMENT_HEADER.name -> return DetailCardType.AD_SUPPLEMENT_HEADER.index
            DetailCardType.STORYPAGE.name,
            DetailCardType.MASTHEAD.name ->
                return AdsUtil.getCardTypeForAds(getAdForPosition(position))
            DetailCardType.SOURCE_TIME.name -> return DetailCardType.SOURCE_TIME.index
        }
        if (visibleListingOrder[position].startsWith(DetailCardType.SUPPLEMENT.name, true)) {
            return AdsUtil.getCardTypeForAds(getAdForPosition(position))
        }

        return super.getItemViewType(position)
    }

    private fun getAdForPosition(position: Int): BaseAdEntity {
        val cardType = visibleListingOrder[position]
        return when {
            cardType == DetailCardType.STORYPAGE.name -> adsMap[AdPosition.STORY.value]!!
            cardType == DetailCardType.MASTHEAD.name -> adsMap[AdPosition.MASTHEAD.value]!!
            cardType.startsWith(DetailCardType.SUPPLEMENT.name, true) ->
                adsMap[cardType]!!
            else -> {
                throw(IllegalStateException("${visibleListingOrder[position]} is not an Ad Index"))
            }
        }
    }

    private fun getContentTypeForAlbum(): Int {
        val count = card?.i_thumbnailUrls()?.size ?: return DetailCardType.IMAGE.index

        val subFormat = card?.i_subFormat()
        val uiType = card?.i_uiType()
        when (uiType) {
            UiType2.GRID_2 -> return DetailCardType.GALLERY_2.index
            UiType2.GRID_3 -> return DetailCardType.GALLERY_3.index
            UiType2.GRID_4 -> return DetailCardType.GALLERY_4.index
            UiType2.GRID_5 -> return DetailCardType.GALLERY_5.index
            else -> {
                //Do nothing let fallback handle
            }
        }

        return DetailCardType.IMAGE.index
    }

    private fun getContentTypeForPolls(): Int {
        val format = card?.i_format()

        if (card != null && format != null && format.equals(Format.POLL)) {
            if (card!!.i_pollSelectedOptionId() != null ||
                !CommonUtils.isCurrentTimeInBounds(
                    card!!.i_poll()?.startDate
                        ?: 0, card!!.i_poll()?.endDate
                        ?: 0
                ) || CardsBindUtils.isSameUserId(card!!)
            )
                return DetailCardType.POLL_RESULT.index
            else
                return DetailCardType.POLL.index

        }

        return -1
    }

    fun updateLikedList(likedList: LikeListPojo?) {
        this.likedListPojo = likedList
        mergeLikes()
        updateVisibleList()
        notifyLikedList()
    }

    private fun mergeLikes() {
        val localLikedListPojo: LikeListPojo =
            if (likedListPojo == null) LikeListPojo() else likedListPojo!!
        val likesList = localLikedListPojo.data
        val items = mutableListOf<LikeAsset>()
        if (likesList != null) {
            items.addAll(likesList)
        }

        val interaction = myInteraction
        val myUserId = SSO.getInstance().userDetails.userID
        var needLocal = true
        var localRemoved = false
        if (myUserId != null && interaction != null) {
            // Merge local like if available
            if (interaction.actionToggle) {
                // Remove remote and add local
                localLikedListPojo?.data?.forEach {
                    if (it.entityKey == myUserId) {
                        needLocal = false
                        if (it.action != interaction.action) {
                            // Replace remote data
                            items.remove(it)
                            items.add(
                                0,
                                it.copy(action = interaction.action, actionTime = interaction.ts)
                            )
                        }
                    }
                }

                if (needLocal) {
                    val actionableEntity =
                        ActionableEntity(
                            entityImageUrl = SSO.getLoginResponse()?.profileImage,
                            entityId = myUserId, entityType = Constants.EMPTY_STRING
                        )
                    items.add(
                        0, LikeAsset(
                            action = interaction.action, entityKey = myUserId,
                            actionTime = interaction.ts, actionableEntity = actionableEntity,
                            description = null, pageScrollValue = null
                        )
                    )
                }
            } else if (!interaction.actionToggle) {
                // Removed based on local
                needLocal = false
                localLikedListPojo.data?.forEach {
                    if (it.entityKey == myUserId) {
                        // Remove remote data
                        items.remove(it)
                        localRemoved = true
                    }
                }
            }
        } else {
            needLocal = false
        }

        var loggedInUserCount = (localLikedListPojo.loggedInUserCount ?: 0)
        if (loggedInUserCount == 0 && needLocal) {
            loggedInUserCount = 1
            needLocal = false
        }

        if (localRemoved) {
            loggedInUserCount -= 1
        }

        val mergedLikedListPojo = localLikedListPojo.copy(
            data = items,
            count = items.size,
            guestUserCount = localLikedListPojo.guestUserCount,
            loggedInUserCount = loggedInUserCount,
            total = (localLikedListPojo.guestUserCount ?: 0) +
                    loggedInUserCount + (if (needLocal) 1 else 0) - (if (localRemoved) 1 else 0)
        )
        likedListPojoParam = mergedLikedListPojo
    }

    fun updateLocalLikes(myInteraction: Interaction?) {
        this.myInteraction = myInteraction
        updateLikedList(likedListPojo)
    }

    private fun notifyLikedList() {
        val index = visibleListingOrder.indexOf(DetailCardType.LIKES_LIST.name)
        Logger.d(TAG, "notifyLikedList: ind=$index")
        notifyItemChanged(index)
    }

    fun updateRelatedStories(relatedStories: CardsPojo?) {
        var needUIRefresh = false
        if (this.relatedStories?.data?.size != relatedStories?.data?.size) {
            needUIRefresh = true
        }

        this.relatedStories = relatedStories
        updateVisibleList()
        if (needUIRefresh) {
            notifyRelated()
        }
    }

    fun updateCard(
        card: CommonAsset?,
        order: List<String> = detailListingOrder,
        notifyViral: Boolean = false
    ) {
        Logger.d(
            TAG,
            "updateCard: notifV=$notifyViral,counts=${LikeEmojiBindingUtils.debugCountsString(card)} ,order=$order"
        )
        var needBootstrapCardsNotified = false
        if (this.card == null && card != null && bootStrapCard != null) {
            needBootstrapCardsNotified = true
        }

        var needSourceNotified = false
        if (this.card?.i_source() == null && card?.i_source() != null) {
            needSourceNotified = true
        } else if (this.card?.i_isFollowin() != card?.i_isFollowin()) {
            needSourceNotified = true
        }

        this.card = card
        this.detailListingOrder = order
        updateVisibleList()
        if (needBootstrapCardsNotified) {
            if (!card?.i_title().equals(bootStrapCard?.title)) {
                notifyTitle()
            }

            if (!(card?.i_contentImageInfo()?.url ?: "").equals(bootStrapCard?.imageDetails?.url)) {
                notifyImage()
            }
        }

        if (needSourceNotified || needBootstrapCardsNotified) {
            notifySource()
        }
        if (notifyViral) {
            notifyViralImage()
        }
    }

    fun setAdsHelper(adsHelper: PostAdsHelper?) {
        this.adsHelper = adsHelper
    }

    fun updateDiscussion(discussionPojo: DiscussionPojo?) {
        this.discussionPojo = discussionPojo
        updateVisibleList()
    }

    fun updateAssociation(associationPojo: CardsPojo?) {
        this.associationPojo = associationPojo
        updateVisibleList()
    }

    fun updateError(error: ObservableDataBinding<BaseError>) {
        this.error = error
        val readMoreIndex = visibleListingOrder.indexOf(DetailCardType.READMORE.name)
        if (readMoreIndex >= 0) {
            notifyItemChanged(readMoreIndex)
        } else {
            updateVisibleList()

        }
    }

    fun updateSuggestedFollow(suggestedFollow: SuggestedFollowsPojo?) {
        this.suggedtedFollowsPojo = suggestedFollow
        updateVisibleList()
    }

    fun updatePhotoChild(photoChildPojo: PhotoChildPojo?) {
        this.photoChildPojo = photoChildPojo
        updateVisibleList()
    }

    fun updateTitlePosition(titleLength: Int) {
        this.titleLength = titleLength
        updateVisibleList()
    }

    private fun notifyImage() {
        val index = visibleListingOrder.indexOf(DetailCardType.IMAGE.name)
        if (index >= 0) {
            notifyItemChanged(index)
        }
    }

    private fun notifyViralImage() {
        val index = visibleListingOrder.indexOf(DetailCardType.VIRAL.name)
        if (index >= 0) {
            notifyItemChanged(index)
        }
    }

    private fun notifyTitle() {
        val index = visibleListingOrder.indexOf(DetailCardType.TITLE.name)
        if (index >= 0) {
            notifyItemChanged(index)
        }
    }

    private fun notifyDetailWidget(cardType: String) {
        val index = visibleListingOrder.indexOf(cardType)
        if (index >= 0) {
            notifyItemChanged(index)
        }
    }

    private fun notifySource() {
        val index = visibleListingOrder.indexOf(DetailCardType.SOURCE.name)
        val indexSourceTime = visibleListingOrder.indexOf(DetailCardType.SOURCE_TIME.name)
        if (index >= 0) {
            notifyItemChanged(index)
        }
        if (indexSourceTime >= 0) {
            notifyItemChanged(indexSourceTime)
        }
    }

    private fun notifyDiscussions() {
        var index = visibleListingOrder.indexOf(DetailCardType.DISCUSSION.name)
        var lastIndex = visibleListingOrder.lastIndexOf(DetailCardType.DISCUSSION.name)
        if (index == -1 && lastIndex == -1) {
            index = visibleListingOrder.indexOf(DetailCardType.DISCUSSION_NS.name)
            lastIndex = visibleListingOrder.lastIndexOf(DetailCardType.DISCUSSION_NS.name)
        }

        for (i in index until (lastIndex + 1)) {
            notifyItemChanged(i)
        }
    }

    private fun notifyRelated() {
        val index = visibleListingOrder.indexOf(DetailCardType.SUPPLEMENTARY_RELATED.name)
        notifyItemChanged(index)
    }

    /**
     * whenever count changes we notify header. ViewAll also needs to be notified because,
     * 'view all comments' button visibility , 'View N reposts' button text and CTA will be affected. */
    fun notifyDiscussionHeader(notifyViewAll: Boolean = true) {
        val discussionHeaderIndex =
            visibleListingOrder.indexOf(DetailCardType.DISCUSSION_HEADER.name)
        if (discussionHeaderIndex >= 0) {
            notifyItemChanged(discussionHeaderIndex)
        }
        if (notifyViewAll) {
            val discussionShowAllIndex =
                visibleListingOrder.indexOf(DetailCardType.DISCUSSION_SHOW_ALL.name)
            if (discussionShowAllIndex >= 0) {
                notifyItemChanged(discussionShowAllIndex)
            }
        }
    }

    fun updateNewsAppJS(newsAppJSChunk1: NewsAppJS?, newsAppJSChunk2: NewsAppJS?) {
        this.newsAppJSChunk1 = newsAppJSChunk1
        this.newsAppJSChunk2 = newsAppJSChunk2
        updateVisibleList()
    }

    fun parentCardChange() {
        var index = visibleListingOrder.indexOf(DetailCardType.TITLE.name)
        if (index >= 0) {
            notifyItemChanged(index)
        }

        index = visibleListingOrder.indexOf(DetailCardType.SOURCE.name)
        if (index >= 0) {
            notifyItemChanged(index)
        }

        index = visibleListingOrder.indexOf(DetailCardType.SEEPOST.name)
        if (index >= 0) {
            notifyItemChanged(index)
        }

        index = visibleListingOrder.indexOf(DetailCardType.MAIN_COMMENT.name)
        if (index >= 0) {
            notifyItemChanged(index)
        }
    }

    fun updateAd(adPosition: String, adEntity: BaseAdEntity) {
        AdLogger.d(TAG, "Inserting ad for $adPosition ${adEntity.uniqueAdIdentifier}")
        adsMap[adPosition] = adEntity
        updateVisibleList()
    }

    fun replaceAd(oldAd: BaseAdEntity, newAd: BaseAdEntity) {
        AdLogger.d(TAG, "Replace ad for ${oldAd.adPosition} ${oldAd.uniqueAdIdentifier}")
        adsMap[oldAd.adPosition!!.value] = newAd
        updateVisibleList()
    }

    /**
     * For cases of ads seen in different views and inserted here too.
     */
    fun removeAd(adPosition: String) {
        AdLogger.d(TAG, "Removing ad for $adPosition")
        adsMap.remove(adPosition)
        updateVisibleList()
    }

    fun getDisplayPositionFor(adCard: String): Position? {
        val adIndex = visibleListingOrder.indexOf(adCard)
        return when {
            adIndex == -1 -> null
            adIndex + 1 < visibleListingOrder.size -> Position(
                "Above",
                visibleListingOrder[adIndex + 1]
            )
            else -> Position("Below", visibleListingOrder[adIndex - 1])
        }
    }

    fun onClickOtherPerspective(): Int {
        return visibleListingOrder.indexOf(DetailCardType.OTHER_PERSPECTIVES.name)
    }
}