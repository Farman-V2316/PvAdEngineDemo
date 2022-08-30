/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.appview.common.ui.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.ListPreloader
import com.bumptech.glide.RequestBuilder
import com.coolfie_exo.download.ExoDownloadHelper
import com.dailyhunt.tv.exolibrary.download.config.CacheConfigHelper
import com.dailyhunt.tv.exolibrary.entities.BaseMediaItem
import com.dailyhunt.tv.players.autoplay.VideoRequester
import com.dailyhunt.tv.players.utils.PlayerUtils
import com.newshunt.adengine.listeners.ReportAdsMenuListener
import com.newshunt.adengine.model.entity.BaseAdEntity
import com.newshunt.adengine.model.entity.version.AdContentType
import com.newshunt.adengine.util.AdsUtil
import com.newshunt.adengine.view.AdEntityReplaceHandler
import com.newshunt.adengine.view.UpdateableAdView
import com.newshunt.adengine.view.helper.AdBinderRepo
import com.newshunt.adengine.view.helper.AdsViewHolderFactory
import com.newshunt.adengine.view.viewholder.NativeAdHtmlViewHolder
import com.newshunt.analytics.client.AnalyticsClient
import com.newshunt.analytics.entity.NhAnalyticsDevEvent
import com.newshunt.appview.BR
import com.newshunt.appview.R
import com.newshunt.appview.common.ui.helper.CardsBindUtils
import com.newshunt.appview.common.ui.viewholder.*
import com.newshunt.appview.common.video.helpers.ExoRequestHelper
import com.newshunt.appview.common.video.utils.DHVideoUtils
import com.newshunt.appview.common.viewmodel.CardsViewModel
import com.newshunt.appview.databinding.*
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.preference.GenericAppStatePreference
import com.newshunt.common.view.ClearableCard
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.analytics.section.NhAnalyticsEventSection
import com.newshunt.dataentity.common.asset.*
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.common.model.entity.Extra
import com.newshunt.dataentity.common.model.entity.ExtraListObjType
import com.newshunt.dataentity.common.pages.FollowActionType
import com.newshunt.dataentity.common.pages.PageSection
import com.newshunt.dataentity.common.pages.UserFollowView
import com.newshunt.dataentity.model.entity.*
import com.newshunt.dataentity.news.analytics.NewsReferrer
import com.newshunt.dhutil.commons.listener.ReferrerProviderlistener
import com.newshunt.dhutil.getFormattedCountForLikesAndComments
import com.newshunt.dhutil.helper.AppSettingsProvider
import com.newshunt.dhutil.helper.LiveSharedPreference
import com.newshunt.dhutil.helper.TickerHelper3
import com.newshunt.dhutil.helper.autoplay.AutoPlayHelper
import com.newshunt.dhutil.model.entity.players.AutoPlayable
import com.newshunt.helper.ImageUrlReplacer
import com.newshunt.helper.player.AutoPlayManager
import com.newshunt.news.helper.NHJsInterfaceWithMenuClickHandling
import com.newshunt.news.model.daos.CardDao
import com.newshunt.news.util.EventDedupHelper
import com.newshunt.news.view.ListEditInterface
import com.newshunt.news.view.viewholder.LanguageSelectionViewHolder
import com.newshunt.news.view.viewholder.LocationSelectionViewHolder
import com.newshunt.news.view.viewholder.NestedCollectionViewHolder
import com.newshunt.news.view.viewholder.UserInteractionViewHolder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val LOG_TAG = "CardsAdapter"
private const val LOG_TAG_CACHE = "CardsAdapter::Cache"
private const val MAX_DISPLAY_TYPE_VALUE = 1000

interface UpdateableCardView : ClearableCard {
    fun bind(item: Any?, lifecycleOwner: LifecycleOwner?, cardPosition: Int) {}
}

interface UpdateableCollectionChildItemView {
    fun bind(item: Any?, parent: Any?, lifecycleOwner: LifecycleOwner?, position: Int,
             parentCardPosition:Int)
}

interface VideoPrefetchCallback {
    fun onRenderedFirstFrame(position: Int, asset: CommonAsset?)
    fun onCardVisibility(position: Int, asset: CommonAsset?)
}

interface CollectionViewHolder {
    fun getViewForAnimationByItemId(storyId: String): View?
}

class CardsAdapter(private val context: Context?,
                   private val cardsViewModel: CardsViewModel,
                   private val parentLifeCycle: LifecycleOwner? = null,
                   private val isDetailView: Boolean = false,
                   private val videoRequester: VideoRequester?,
                   private val autoPlayManager: AutoPlayManager?,
                   private val listEditInterface: ListEditInterface? = null,
                   private val uniqueRequestId: Int = -1,
                   private val deeplinkUrl: String? = null,
                   private val adEntityReplaceHandler: AdEntityReplaceHandler? = null,
                   private val webCacheProvider: NativeAdHtmlViewHolder.CachedWebViewProvider? = null,
                   private val itemLocation: String? = null,
                   private val tabType: String? = null,
                   private val pageReferrer: PageReferrer? = null,
                   private val section: String = PageSection.NEWS.section,
                   private val showAddPageButton: Boolean = false,
                   private val nhJsInterfaceWithMenuClickHandling:
                   NHJsInterfaceWithMenuClickHandling? = null,
                   private val tickerHelper3: TickerHelper3? = null,
                   val eventDedupHelper: EventDedupHelper = EventDedupHelper(mapOf()),
                   private val referrerProviderlistener: ReferrerProviderlistener? = null,
                   private val diffCallback: DiffUtil.ItemCallback<Any?> =
                           CardsAdapterDiffUtilCallback(),
                   private val reportAdsMenuListener: ReportAdsMenuListener? = null) : CustomPagedAdapter<Any?,
        RecyclerView.ViewHolder>
(diffCallback),
        ListPreloader.PreloadModelProvider<String>, ListPreloader.PreloadSizeProvider<String>,
        VideoPrefetchCallback, ExoDownloadHelper.VideoCacheListener {

    private val followList = mutableMapOf<String, CardDao.Follow>()
    private val voteList = mutableMapOf<String, CardDao.Vote>()
    private val likeTypeList = mutableMapOf<String, CardDao.Interaction>()
    private val readIdsSet = mutableSetOf<String>()
    private val memberShip = mutableMapOf<String, MembershipStatus>()
    private val dislikeSet = mutableSetOf<String>()
    private val viewPool = RecyclerView.RecycledViewPool()
    private val extraCards = mutableMapOf<String, Card>()

    var availableHeight: Int? = CommonUtils.getDeviceScreenHeight()
    var availableWidth: Int? = CommonUtils.getDeviceScreenWidth()
    private var configType = ConfigType.NEWS_LIST
    private var isNextVideoPrefetchInProgress = false
    private var isVerticalListVideosPrefetchInProgress = false

    init {
        if (parentLifeCycle != null) {
            cardsViewModel.followList.observe(parentLifeCycle, Observer {
                if(it!=null) {
                    followList.clear()
                    followList.putAll(
                            it.map {
                                "${it.id}${it.type}" to it
                            }
                    )
                    dispatchCalculatedFieldChange()
                }
            })

            cardsViewModel.voteList.observe(parentLifeCycle, Observer {
                if(it!=null) {
                    voteList.clear()
                    voteList.putAll(
                            it.map {
                                it.pollId to it
                            }
                    )
                    dispatchCalculatedFieldChange()
                }
            })

            cardsViewModel.dislikeList.observe(parentLifeCycle, Observer {
                if (it != null) {
                    dislikeSet.clear()
                    dislikeSet.addAll(it)
                    dispatchCalculatedFieldChange()
                }
            })

            cardsViewModel.likeList.observe(parentLifeCycle, Observer {
                if(it!=null) {
                    likeTypeList.clear()
                    likeTypeList.putAll(
                            it.map {
                                it.entity_id to it
                            }
                    )
                    dispatchCalculatedFieldChange()
                }
            })

            cardsViewModel.readIds.observe(parentLifeCycle, Observer {
                if(it!=null){
                    readIdsSet.clear()
                    readIdsSet.addAll(it)
                    dispatchCalculatedFieldChange()
                }
            })

            cardsViewModel.membership.observe(parentLifeCycle, Observer {
                if (it != null) {
                    memberShip.clear()
                    memberShip.putAll(
                            it.map {
                                it.id to it.membership
                            }
                    )
                    dispatchCalculatedFieldChange()
                }
            })
            cardsViewModel.extraCards.observe(parentLifeCycle, Observer {
                if (it != null) {
                    extraCards.clear()
                    extraCards.putAll(it.map { card ->
                        card.id to card
                    })
                    dispatchCalculatedFieldChange()
                }
            })
        }
        configType = if(section == PageSection.NEWS.section) {
            ConfigType.NEWS_LIST
        } else {
            ConfigType.BUZZ_LIST
        }
    }

    private fun dispatchCalculatedFieldChange() {
        val snapshot = getSnapshot() ?: return
        val oldList = mutableListOf<Any?>()
        oldList.addAll(snapshot)
        val newList = oldList.map { transformCardItem(it) }
        val diffCallback = CardsAdapterDiffUtilCallback2(oldList, newList, diffCallback)
        val result = DiffUtil.calculateDiff(diffCallback)
        result.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val parentCardType = viewType / MAX_DISPLAY_TYPE_VALUE
        val repostCardType = viewType % MAX_DISPLAY_TYPE_VALUE
        val vh = getViewHolder(
                displayCardTypeIndex = parentCardType,
                parent = parent,
                cardsViewModel = cardsViewModel,
                isDetailView = isDetailView,
                repostCardType = repostCardType,
                context = context,
                videoRequester = videoRequester,
                listEditInterface = listEditInterface,
                uniqueRequestId = uniqueRequestId,
                adEntityReplaceHandler = adEntityReplaceHandler,
                pageReferrer = pageReferrer,
                section = section,
                showAddPageButton = showAddPageButton,
                webViewProvider = webCacheProvider,
                nhJsInterfaceWithMenuClickHandling = nhJsInterfaceWithMenuClickHandling,
                tickerHelper3 = tickerHelper3,
                parentLifeCycle = parentLifeCycle,
                eventDedupHelper = eventDedupHelper,
                referrerProviderlistener = referrerProviderlistener,
                deeplinkUrl = deeplinkUrl,
                availableHeight = availableHeight,
                availableWidth = availableWidth,
                reportAdsMenuListener = reportAdsMenuListener,
                videoPrefetchCallback = this,
                viewPool = viewPool)
        if (vh is AutoPlayable) {
            vh.setAutoPlayManager(autoPlayManager)
        }
        vh.itemView.findViewById<RecyclerView>(R.id.comments_repost_item_list)?.let {
            it.setRecycledViewPool(viewPool)
        }
        return vh
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        when (holder) {
            is UpdateableCardView -> {
                holder.bind(transformCardItem(item), parentLifeCycle, position)
            }
            is UpdateableAdView -> holder.updateView(context as Activity, transformCardItem(item) as BaseAdEntity)
        }
    }

    private fun transformCardItem(item: Any?, level: Int = 0): Any? {
        if (level > 2) {
            /*To stop going into loop*/
            return item
        }
        if (item is CommonAsset) {
            val itemId =  item.i_id()
            val filteredFollowList = if(isBlockCarousels(item)) followList else
                followList.filterNot { it.value.socialAction == FollowActionType.BLOCK.name }
            val key = filteredFollowList["${item.i_source()?.id}${item.i_source()?.entityType}"] != null
            val extraCard = extraCards[itemId]
            val liketype = likeTypeList[itemId]?.col_action
            val optionId = voteList[itemId]?.optionId
            val isRead = readIdsSet.contains(itemId)
            val coldstartItems = item.i_coldStartAsset()?.coldStartCollectionItems?.map { csi ->
                csi.copy(isSelected = filteredFollowList["${csi.entityId}${csi.entityType}"] != null,
                        isGroupSelected = (memberShip[csi.entityId] != null && memberShip[csi.entityId] != MembershipStatus.NONE))
            }
            var collectionItems = item.i_collectionItems()
                    ?.filterIsInstance<PostEntity>()
                    ?.filter {
                        if(item.isSavedCarousel()) // for profile > saved, filter out disliked ones.
                            !(dislikeSet.contains(it.i_id()))
                        else if(it.i_source() != null && it.i_source()?.id != null && isCollectionItemBlocked(it.i_source()?.id+"SOURCE"))
                            false
                        else true
                    }?.map {
                        transformCardItem(it, level + 1) as PostEntity
                    }

            if (item is SavedCard) {
                val storyCount = (item.rootPostEntity()?.counts?:Counts2()).copy(STORY =  EntityConfig2(item.count_story_value))
                item.rootPostEntity()?.counts = storyCount
                Logger.d(LOG_TAG, "updated ${item.i_id()} storyCount to ${item.count_story_value}")
            }
            if (item is BaseAdEntity && item.type != AdContentType.CONTENT_AD) {
                item.contentAsset = item.contentAsset?.let {
                    transformCardItem(item.contentAsset, level + 1) as PostEntity
                }
            }
            extraCard?.rootPostEntity()?.counts?.let {
                item.rootPostEntity()?.counts = it
            }
            if(item.i_format()==Format.NESTED_COLLECTION) {
                collectionItems = collectionItems?.filter { (it.i_collectionItems()?.size ?: 0) >= 3 }?.toList()
                if(collectionItems.isNullOrEmpty()) return null
            }

            return item.copyWith(
                    isFollowin = key,
                    selectedLikeType = liketype,
                    pollSelectedOptionId = optionId,
                    isRead = isRead,
                    coldStartItems = coldstartItems,
                    collectionItems = collectionItems)
        } else {
            return item
        }
    }

    private fun isCollectionItemBlocked(id:String): Boolean{
        return followList.containsKey(id) && followList[id]?.socialAction == FollowActionType.BLOCK.name
    }

    private fun isBlockCarousels(item:CommonAsset):Boolean {
        return FollowActionType.BLOCK.name == item.i_coldStartAsset()?.actionType
    }

    override fun getItemViewType(position: Int): Int {
        val pair = findCardTypeIndex(getItem(position), itemLocation, tabType, videoRequester)
        return (pair.first * MAX_DISPLAY_TYPE_VALUE) + pair.second
    }


    override fun getPreloadItems(position: Int): List<String> {
        if (position >= itemCount) {
            return ArrayList()
        }

        val asset = getItem(position)
        val displayCardType = findCardTypeIndex(asset).first
        return (getItem(position) as? CommonAsset)?.i_thumbnailUrls()?.mapIndexed { index, url ->
            val dimen = CardsBindUtils.getImageDimension(displayCardType, index)
            if (dimen == null) {
                null
            } else {
                ImageUrlReplacer.getQualifiedImageUrl(url, dimen.first, dimen.second)
            }
        }?.filterNotNull() ?: return emptyList()
    }

    override fun getPreloadRequestBuilder(url: String): RequestBuilder<*>? {
        context ?: return null
        if (CommonUtils.isEmpty(url)) {
            return null
        }
        return Glide.with(context).load(url)
    }

    override fun getPreloadSize(url: String, adapterPosition: Int, itemPosition: Int): IntArray? {
        if (adapterPosition >= itemCount) {
            return null
        }
        val asset = getItem(adapterPosition)
        val displayCardType = findCardTypeIndex(asset).first
        val pair = CardsBindUtils.getImageDimension(displayCardType, itemPosition) ?: return null
        return intArrayOf(pair.first, pair.second)
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        if (holder is ClearableCard) {
            holder.recycleView()
        }
    }

    fun showFooter(show: Boolean) {
        val item = if (show) Extra(ExtraListObjType.FOOTER) else null
        showFooterItem(item, ExtraListObjType.FOOTER.ordinal)
    }

    fun showGuestUserFooter(totalCount: Long) {
        val count = totalCount - getOriginalItemCount()
        val item =if (count > 0) Extra(ExtraListObjType.GUEST_USERS,
                CommonUtils.getString(R.string.follower_guest_user_count, getFormattedCountForLikesAndComments(count))) else null
        showFooterItem(item, ExtraListObjType.GUEST_USERS.ordinal)
    }

    //For Ads & NLFC
    /**
     * Returns id of post present at index <= index
     * null implies empty list or 0th position.
     */
    fun getItemIdBeforeIndex(index: Int): String? {
        return if (itemCount == 0 || index == 0)
            null
        else {
            val snapShot = getSnapshot()
            val item = if (index in 1..itemCount) getItem(index - 1) else snapShot?.last()
            if (item is CommonAsset) {
                item.i_id()
            } else {
                null
            }
        }
    }

    fun validateAndGetPosition(position: Int, distancingSpec: DistancingSpec?): Int {
        return if (itemCount == 0 || position == 0)
            -1
        else {
            var newPosition = position
            val snapShot = getSnapshot()
            loop@ for (i in position downTo 0) {
                val item = snapShot?.get(i)
                if (item is CommonAsset) {
                    Logger.d("NestedCollectionCardsHelper", "item format: ${item.i_format()} and position :  $i")
                    when (item.i_format()) {
                        Format.AD -> {
                            if (position - i < (distancingSpec?.ads ?: 0)) {
                                newPosition += (distancingSpec?.ads ?: 0) + (position - i + 1)
                                break@loop
                            }
                        }
                        Format.COLLECTION, Format.POST_COLLECTION -> {
                            if (position - i < (distancingSpec?.ads ?: 0)) {
                                newPosition += (distancingSpec?.collection ?: 0) + (position - i + 1)
                                break@loop
                            }
                        }
                        Format.NESTED_COLLECTION -> {
                            if (position - i < (distancingSpec?.ads ?: 0)) {
                                newPosition += (distancingSpec?.nestedCollection ?: 0) + (position - i + 1)
                                break@loop
                            }
                        }
                        else -> {}
                    }
                }
            }
            Logger.d("NestedCollectionCardsHelper", "new position:   $newPosition")
            return newPosition
        }
    }

    /**
     * Get Item Id of entity before
     * index argument excluding empty ads.
     */
    fun getItemIdBeforeIndexExEmpAd(index: Int): String? {
        return if (itemCount == 0 || index == 0)
            null
        else {
            val snapShot = getSnapshot()
            val count = snapShot?.subList(0,index)?.filter { it is CommonAsset && it.rootPostEntity()?.type == AdContentType.EMPTY_AD.getName()}?.size ?: 0
            val item = if (index+count in 1..itemCount) getItem(index+count - 1) else snapShot?.last()
            if (item is CommonAsset) {
                item.i_id()
            } else {
                null
            }
        }
    }

    /**
     * Get list size excluding empty ad.
     */
    fun getItemCountExEmpAd():Int {
        val snapShot = getSnapshot()
        return snapShot?.count { it is CommonAsset && it.rootPostEntity()?.type != AdContentType.EMPTY_AD.getName()} ?: 0
    }

    fun insertAd(baseAdEntity: BaseAdEntity, adapterPos: Int): Boolean {
        if (itemCount == 0 || itemCount < adapterPos) {
            return false
        }
        /*Empty function because its adding duplicate ads in list again*/
        //addExtraItems(baseAdEntity, adapterPos)
        val x = getPagedListWrapper()?.clone()
        val adCard = if (baseAdEntity.type == AdContentType.CONTENT_AD && baseAdEntity.contentAsset is PostEntity)
                (baseAdEntity.contentAsset as PostEntity).toCard2()
            else AdsUtil.toPostEntity(baseAdEntity).toCard2().copy(uniqueId = baseAdEntity.i_id())
        adCard.adId = baseAdEntity.i_id()

        x?.addExtraItem(adapterPos, adCard)
        submitList(x)
        val aboveCard = getItem(adapterPos - 1)
        val belowCard = getItem(adapterPos + 1)
        if (adapterPos >= 1) {
            baseAdEntity.isBelowContentAutoplayable = isAutoplayCard(aboveCard)
        }
        if (adapterPos + 1 < itemCount) {
            baseAdEntity.isAboveContentAutoplayable = isAutoplayCard(belowCard)
        }
        if (baseAdEntity.minAdDistance ?: 0 > 0 && (isAd(aboveCard) || isAd(belowCard))) {
            val prevAdCard = (if (isAd(aboveCard)) aboveCard else belowCard) as CommonAsset
            val ad = AdBinderRepo.getAdById(prevAdCard.i_adId() ?: Constants.EMPTY_STRING)
            val map = hashMapOf("error_type" to "Consecutive Ads",
                    "adPosition" to baseAdEntity.adPosition?.value,
                    "adPosition1" to ad?.adPosition?.value,
                    "adIndex" to adapterPos.toString(),
                    "adIndex1" to (if (prevAdCard == aboveCard) adapterPos - 1 else adapterPos + 1).toString(),
                    "adId" to baseAdEntity.uniqueAdIdentifier,
                    "adId1" to ad?.uniqueAdIdentifier
            )
            AnalyticsClient.logDynamic(NhAnalyticsDevEvent.DEV_CUSTOM_ERROR, NhAnalyticsEventSection.APP, null, map, false)
        }
        if (baseAdEntity.type != AdContentType.CONTENT_AD && baseAdEntity.contentAsset is PostEntity) {
            cardsViewModel.addExtraAdId(baseAdEntity.contentAsset?.i_id())
        }
        return true
    }

    private fun isAd(card: Any?) : Boolean {
        return card is CommonAsset && (card.i_format() == Format.AD || card.i_adId() != null)
    }

    public override fun getItem(position: Int): Any? {
        val item = super.getItem(position)
        if (item is CommonAsset && item.i_format() == Format.POST_COLLECTION) {
            val transformed = transformCardItem(item)
            // saved carousels have a certain UI for 0 children. We should not filter them out.
            if (((transformed as? CommonAsset)?.i_collectionItems()).isNullOrEmpty() && !item.isSavedCarousel()) {
                return null
            }
        }
        if (item is CommonAsset && item.i_format() == Format.POLL) {
            return transformCardItem(item)
        }
        return item
    }

    private fun isAutoplayCard(card: Any?): Boolean {
        return card is CommonAsset && card.i_uiType() == UiType2.AUTOPLAY
    }

    fun getCardByDownloadId(downloadId: Long): CommonAsset? {
        val currentListSnapShot = getSnapshot() ?: return null
        for (item in currentListSnapShot) {
            if(item is CommonAsset) {
                if (downloadId == item.i_videoAsset()?.downloadRequestId) {
                    return item
                }
            }
        }
        return null
    }

    fun updateDownloadIdForCard(downloadId: Long?, itemId: String): CommonAsset? {
        val currentListSnapshot = getSnapshot() ?: return null
        if(downloadId == null || CommonUtils.isEmpty(itemId)) return null
        for (item in currentListSnapshot) {
            if(item is CommonAsset) {
                if (itemId.equals(item.i_id())) {
                    item.i_videoAsset()?.downloadRequestId = downloadId
                }
            }
        }
        return null
    }

    /**
     * Add first m videos to prefetch list
     */
    fun pushForVideoPrefetch(startPosition: Int) {
        if (CacheConfigHelper.disableCache || !AutoPlayHelper.isAutoPlayAllowed() ||
            isVerticalListVideosPrefetchInProgress || startPosition < 0) {
            Logger.d(LOG_TAG_CACHE, "pushForVideoPrefetch() disableCache == true" +
                    " in_progress : $isVerticalListVideosPrefetchInProgress and startPosition : $startPosition")
            return
        }

        var noOfVideosToPrefetch = ExoRequestHelper.remainingToPrefetch(configType)
        val currentListSnapshot = getSnapshot()

        Logger.d(LOG_TAG_CACHE, "pushForVideoPrefetch() startPosition = $startPosition" +
                " noOfVideosToPrefetch = $noOfVideosToPrefetch" +
                " currentListSnapshotSize = ${currentListSnapshot?.size}")

        if (noOfVideosToPrefetch <= 0) {
            Logger.d(LOG_TAG_CACHE, "pushForVideoPrefetch() return prefetch is full")
            return
        }

        isVerticalListVideosPrefetchInProgress = true
        var itemAddedCount = 0
        GlobalScope.launch(Dispatchers.IO) {
            val delayTime = PlayerUtils.getTimeBasedOnNetwork()
            delay(delayTime)
            if(currentListSnapshot != null) {
                for (index in startPosition until currentListSnapshot.size) {
                    if(index >= currentListSnapshot.size) {
                        break
                    }
                    val asset = currentListSnapshot[index]
                    if(asset is CommonAsset && DHVideoUtils.isEligibleToPrefetch(asset) &&
                        !ExoRequestHelper.isPresentInRequestQueue(asset.i_id())) {
                        Logger.d(LOG_TAG_CACHE, "pushForVideoPrefetch Added to prefetch List : $index, contentId : " + asset?.i_id())
                        Logger.d(LOG_TAG_CACHE, "pushForVideoPrefetch ListSize : $itemAddedCount " +
                                " && noOfVideosToPrefetch : $noOfVideosToPrefetch")
                        if (noOfVideosToPrefetch > 0) {
                            Logger.d(LOG_TAG_CACHE, "pushForVideoPrefetch prefetchVideo >>")
                            noOfVideosToPrefetch--
                            itemAddedCount++
                            ExoRequestHelper.prefetchVideo(index, asset, configType)
                        } else {
                            Logger.d(LOG_TAG_CACHE, "pushForVideoPrefetch break at Size : " + "$itemAddedCount")
                            break
                        }
                    }
                }
            }
            isVerticalListVideosPrefetchInProgress = false
        }
    }

    private fun getItemById(itemId: String?): CommonAsset? {
        if(CommonUtils.isEmpty(itemId)) return null
        val currentListSnapshot = getSnapshot() ?: return null
        for (item in currentListSnapshot) {
            if(item is CommonAsset) {
                if (itemId == item.i_id()) {
                    return item
                }
            }
        }
        return null
    }

    /**
     * 1. Prefetch current item on priority - if not prefetched
     * 2. If m-config videos are already prefetched - prefetch m + 1, if user view video m
     */
    override fun onRenderedFirstFrame(position: Int, asset: CommonAsset?) {
        if(isNextVideoPrefetchInProgress || CacheConfigHelper.disableCache ||
            !AutoPlayHelper.isAutoPlayAllowed()) {
            return
        }
        isNextVideoPrefetchInProgress = true
        GlobalScope.launch(Dispatchers.IO) {
            delay(1000)
            if (DHVideoUtils.isEligibleToPrefetch(asset)) {
                ExoRequestHelper.prefetchVideo(position, asset, true, configType)
                Logger.d(LOG_TAG_CACHE, "onRenderedFirstFrame() cacheVideo")
            } else {
                Logger.d(LOG_TAG_CACHE, "onRenderedFirstFrame() isEligibleToPrefetch = false")
            }
            prefetchNextVideoOnOverFlow(position)
            isNextVideoPrefetchInProgress = false
        }
    }

    /**
     * If m-config videos are prefetched, prefetch m+1 video if user views video m
     */
    private fun prefetchNextVideoOnOverFlow(position: Int) {
        Logger.d(LOG_TAG_CACHE, "prefetchNextVideoOnOverFlow position " + position)
        var noOfVideosToPrefetch = ExoRequestHelper.remainingToPrefetch(configType)
        if(noOfVideosToPrefetch > 0) {
            Logger.d(LOG_TAG_CACHE, "prefetchNextVideoOnOverFlow return")
            return
        }
        val currentListSnapshot = getSnapshot()
        if(currentListSnapshot != null) {
            for (index in (position + 1) until currentListSnapshot.size) {
                if(index >= currentListSnapshot.size) {
                    break
                }
                val asset = currentListSnapshot[index] as? CommonAsset ?: continue
                if(ExoRequestHelper.isItemAdded(asset?.i_id())) {
                    Logger.d(LOG_TAG_CACHE, "Next eligible video is already added for prefetch")
                    break
                }
                Logger.d(LOG_TAG_CACHE, "prefetchNextVideoOnOverFlow id : " + asset?.i_id() + ", configType" + asset?.i_videoAsset()?.configType +
                        " DHVideoUtils.isEligibleToPrefetch(asset) : " + DHVideoUtils.isEligibleToPrefetch(asset) +
                        " !ExoRequestHelper.isPresentInRequestQueue(asset.i_id()) " + !ExoRequestHelper.isPresentInRequestQueue(asset.i_id()))
                if(asset?.i_videoAsset()?.configType == null && DHVideoUtils.isEligibleToPrefetch(asset) &&
                    !ExoRequestHelper.isPresentInRequestQueue(asset.i_id())) {
                    Logger.d(LOG_TAG_CACHE, "prefetchNextVideoOnOverFlow Added to prefetch List : 0, contentId : " + asset?.i_id())
                    ExoRequestHelper.prefetchVideo(0, asset, configType)
                    break
                }
            }
        }
    }

    override fun onCardVisibility(position: Int, asset: CommonAsset?) {
    }

    fun stopVideoPrefetch() {
        ExoRequestHelper?.reset()
        ExoDownloadHelper.removeListener(this)
    }

    fun startVideoPrefetch() {
        ExoRequestHelper?.start()
        ExoDownloadHelper.addListener(this)
    }

    fun destroy() {
        ExoRequestHelper?.destroy(configType)
        ExoDownloadHelper.removeListener(this)
        ExoDownloadHelper.cancelAndClearDownloadQueue()
    }

    fun clearCachedItems() {
        ExoRequestHelper?.clearCachedItems(configType)
    }

    override fun updateVideoUrlFromDownload(
        mediaItem: BaseMediaItem?, cacheStatus: ExoDownloadHelper.CacheStatus) {
        Logger.d(LOG_TAG_CACHE, " updateVideoUrlFromDownload : id = " + mediaItem?.contentId)
        markVideoAsStreamCached(
            mediaItem, ExoRequestHelper.getStreamCachedStatus(cacheStatus), true)
    }

    override fun updateVideoUrlFromExo(
        mediaItem: BaseMediaItem?, cacheStatus: ExoDownloadHelper.CacheStatus) {
        Logger.d(LOG_TAG_CACHE, " updateVideoUrlFromExo : id = " + mediaItem?.contentId)
        markVideoAsStreamCached(
            mediaItem, ExoRequestHelper.getStreamCachedStatus(cacheStatus), false)
    }

    override fun updateVideoCachedPercentage(mediaItem: BaseMediaItem?, percentage: Float, downloadedVideoDuration: Float) {
//        Logger.d(LOG_TAG_CACHE, " updateVideoCachedPercentage : id = " + mediaItem?.contentId + ", percentage = $percentage");
        updateDownloadPercentage(mediaItem?.contentId, percentage, downloadedVideoDuration)
    }

    override fun updateDownloadException(mediaItem: BaseMediaItem?, errorMsg: String?) {
        Logger.d(LOG_TAG_CACHE, " updateDownloadException : id = " + mediaItem?.contentId + ", errorMsg = $errorMsg");
        updateDownloadException(mediaItem?.contentId, errorMsg)
    }

    /**
     * Only for log enabled builds
     */
    @Synchronized
    fun updateDownloadException(itemId: String?, errorMsg: String?) {
        if(CommonUtils.isEmpty(itemId)) {
            return
        }
        val asset = getItemById(itemId)
        if (asset != null) {
            if(!TextUtils.isEmpty(errorMsg)) {
                asset?.i_videoAsset()?.downloadErrorMsg = errorMsg
            } else {
                asset?.i_videoAsset()?.downloadErrorMsg = "unknown"
            }
        }
    }

    @Synchronized
    fun updateDownloadPercentage(itemId: String?, percentage: Float?, downloadedVideoDuration: Float) {
        if(CommonUtils.isEmpty(itemId)) {
            return
        }
//        Logger.d(LOG_TAG_CACHE, " updateDownloadPercentage :  $percentage, duration: $downloadedVideoDuration")
        val asset = getItemById(itemId)
        if (asset != null) {
            asset?.i_videoAsset()?.streamDownloadPercentage = percentage ?: 0F
            asset?.i_videoAsset()?.streamCachedDuration = downloadedVideoDuration
//            Logger.d(LOG_TAG_CACHE, "updateDownloadPercentage Item ID :  ${asset?.i_id()}")
//            Logger.d(LOG_TAG_CACHE, "updateDownloadPercentage Item percentage :  $percentage");
//            Logger.d(LOG_TAG_CACHE, "updateDownloadPercentage Item cachedDuration :  $downloadedVideoDuration");
        }
    }

    @Synchronized
    fun markVideoAsStreamCached(
        mediaItem: BaseMediaItem?, cacheStatus: StreamCacheStatus, forceVariant: Boolean) {
        if (mediaItem == null || CommonUtils.isEmpty(mediaItem.contentId) ||
            CommonUtils.isEmpty(mediaItem.uri.toString())) {
            return
        }
        val currentListSnapshot = getSnapshot()
        if(CommonUtils.isEmpty(currentListSnapshot)) {
            return
        }
        try {
            for (i in currentListSnapshot!!.indices) {
                if(currentListSnapshot[i] is CommonAsset) {
                    val asset: CommonAsset = currentListSnapshot[i] as CommonAsset
                    val videoAsset = asset?.i_videoAsset()
                    if (asset?.i_id()?.equals(mediaItem.contentId, true)) {
                        videoAsset?.streamCachedUrl = mediaItem.uri.toString()
                        videoAsset?.streamCachedStatus = cacheStatus
                        if (forceVariant) {
                            videoAsset?.variantIndex = mediaItem.variantIndex
                            videoAsset?.isForceVariant = forceVariant
                            Logger.d(LOG_TAG_CACHE, "markVideoAsStreamCached PREFETCH Item updated at pos : $i, is : ${asset?.i_id()}")
                            Logger.d(LOG_TAG_CACHE,
                                ("markVideoAsStreamCached PREFETCH Status : " + videoAsset?.streamCachedStatus.toString()
                                        + " streamDownloadPercentage : " + videoAsset?.streamDownloadPercentage)
                                        + " streamCachedDuration : " + videoAsset?.streamCachedDuration)
                        }
                        break
                    }
                }
            }
        } catch (e: Exception) {
            Logger.caughtException(e)
        }
    }
}

fun getViewHolder(displayCardTypeIndex: Int,
                  parent: ViewGroup,
                  cardsViewModel: CardsViewModel,
                  isDetailView: Boolean,
                  repostCardType: Int, context: Context?,
                  videoRequester: VideoRequester?,
                  eventDedupHelper: EventDedupHelper,
                  listEditInterface: ListEditInterface? = null,
                  uniqueRequestId: Int = -1,
                  adEntityReplaceHandler: AdEntityReplaceHandler? = null,
                  pageReferrer: PageReferrer? = null,
                  section: String = PageSection.NEWS.section,
                  showAddPageButton: Boolean = false,
                  webViewProvider: NativeAdHtmlViewHolder.CachedWebViewProvider? = null,
                  nhJsInterfaceWithMenuClickHandling:
                  NHJsInterfaceWithMenuClickHandling? = null,
                  tickerHelper3: TickerHelper3? = null,
                  parentLifeCycle: LifecycleOwner? = null,
                  referrerProviderlistener: ReferrerProviderlistener? = null,
                  deeplinkUrl: String? = null,
                  availableHeight:Int? = null,
                  availableWidth:Int? = null,
                  reportAdsMenuListener: ReportAdsMenuListener? = null,
                  videoPrefetchCallback: VideoPrefetchCallback? = null,
                  viewPool: RecyclerView.RecycledViewPool):
        RecyclerView.ViewHolder {
    if (displayCardTypeIndex == PostDisplayType.EMPTY.index) {
        val layoutInflater = LayoutInflater.from(parent.context)
        return SimplePostViewHolder(DataBindingUtil.inflate<ViewDataBinding>(
            layoutInflater, R.layout.empty_binding_layout, parent, false, ),
             null, uniqueRequestId, section, parentLifeCycle, isDetailView, videoPrefetchCallback, -1)
    }
    val viewDataBinding: ViewDataBinding = getViewBinding(displayCardTypeIndex, parent, isDetailView)
    viewDataBinding.setVariable(BR.vm, cardsViewModel)
    viewDataBinding.setVariable(BR.appSettingsProvider, AppSettingsProvider)
    viewDataBinding.setVariable(BR.cardTypeIndex, displayCardTypeIndex)
    viewDataBinding.setVariable(BR.repostCardIndex, repostCardType)
    viewDataBinding.setVariable(BR.isDetailView, isDetailView)
    viewDataBinding.setVariable(BR.adsMenuListener, reportAdsMenuListener)
    if (displayCardTypeIndex == PostDisplayType.SIMPLE_POST_DYNAMIC_HERO.index) {
        viewDataBinding.setVariable(BR.availableHeight, availableHeight)
        viewDataBinding.setVariable(BR.availableWidth, availableWidth)
    }

    //Check if card is an Ad
    AdsViewHolderFactory.getViewHolder(displayCardTypeIndex, viewDataBinding, uniqueRequestId,
        parent, deeplinkUrl, parentLifeCycle, adEntityReplaceHandler, webViewProvider = webViewProvider,
            reportAdsMenuListener = reportAdsMenuListener)?.let {
        return it
    }

    when (displayCardTypeIndex) {
        PostDisplayType.QMC_CAROUSEL3.index,
        PostDisplayType.QMC_CAROUSEL2.index,
        PostDisplayType.QMC_CAROUSEL5.index,
        PostDisplayType.QMC_CAROUSEL1.index,
        PostDisplayType.QMC_CAROUSEL4.index,
        PostDisplayType.QMC_GRID_2.index,
        PostDisplayType.QMC_TAGS.index,
        PostDisplayType.QMC_GRID.index -> {
            return PostColdStartViewHolder(viewDataBinding, cardsViewModel, displayCardTypeIndex,
                    section, pageReferrer, eventDedupHelper)
        }
        PostDisplayType.VIRAL.index -> {
            return SimpleViralViewHolder(viewBinding = viewDataBinding, pageReferrer = pageReferrer,
                parentLifecycleOwner = parentLifeCycle, uniqueScreenId = uniqueRequestId, section = section,
                isDetailView = isDetailView, displayCardTypeIndex)
        }

        PostDisplayType.LOCAL.index -> {
            return PostProgressViewHolder(viewDataBinding, cardsViewModel, parent, isDetailView,
                    videoRequester, context,eventDedupHelper, viewPool)
        }

        PostDisplayType.POST_COLLECTION_VIDEO.index,
        PostDisplayType.POST_COLLECTION_HTML.index,
        PostDisplayType.POST_COLLECTION_IMAGE.index,
        PostDisplayType.SQUARE_CARD_CAROUSEL.index,
        PostDisplayType.HTML_AND_VIDEO_CAROUSEL.index-> {
            return CollectionPostViewHolder(viewBinding = viewDataBinding,
                    cardType = displayCardTypeIndex,
                    vm = cardsViewModel,
                    referrer = pageReferrer,
                    section = section,
                    eventDedupHelper = eventDedupHelper,
                    referrerProviderlistener = referrerProviderlistener,
                    uniqueScreenId = uniqueRequestId,
                    adsMenuListener = reportAdsMenuListener, displayIndex = displayCardTypeIndex)
        }
        PostDisplayType.AUTOPLAY_EXO.index -> {
            if (isDetailView) {
                return SimplePostViewHolder(viewBinding = viewDataBinding,
                    pageReferrer = pageReferrer, uniqueScreenId = uniqueRequestId, section = section,
                    parentLifecycleOwner = parentLifeCycle, isDetailView = isDetailView, videoPrefetchCallback, -1)
            }
            return ExoAutoplayViewHolder(viewDataBinding, pageReferrer, context!!, videoRequester,
                    false, cardsViewModel, parentLifeCycle, section,
                    displayCardTypeIndex = displayCardTypeIndex, parentItem = null, uniqueScreenId = uniqueRequestId,
                    videoPrefetchCallback)

        }
        PostDisplayType.TICKER.index -> {
            return TickerPostViewHolder(viewDataBinding, cardsViewModel, displayCardTypeIndex,
                    context, tickerHelper3, pageReferrer,
                    referrerProviderlistener, section)
        }
        PostDisplayType.AUTOPLAY_WEB.index -> {
            if (isDetailView) {
                return SimplePostViewHolder(viewBinding = viewDataBinding,
                    pageReferrer=pageReferrer, uniqueScreenId = uniqueRequestId, section = section,
                    parentLifecycleOwner = parentLifeCycle, isDetailView = isDetailView, videoPrefetchCallback, -1)
            }
            return WebAutoplayViewHolder(viewDataBinding, pageReferrer, context!!, videoRequester, false,
                    cardsViewModel, parentLifeCycle, section, displayCardTypeIndex = displayCardTypeIndex,
                    parentItem = null, uniqueScreenId = uniqueRequestId, videoPrefetchCallback)
        }
        PostDisplayType.POST_COLLECTION_AUTOPLAY.index -> {
            return CollectionAutoplayViewHolder(viewBinding = viewDataBinding,
                    cardType = displayCardTypeIndex,
                    cardsViewModel = cardsViewModel,
                    videoRequester = videoRequester,
                    context = context,
                    pageRef = PageReferrer(NewsReferrer.CARD_WIDGET),
                    displayCardTypeIndex = repostCardType,
                    section = section,
                    referrerProviderlistener = referrerProviderlistener,
                    eventDedupHelper = eventDedupHelper,
                    uniqueScreenId = uniqueRequestId)
        }

        PostDisplayType.SIMPLE_WEB.index -> {
            return WebCardViewHolder(viewDataBinding, vm = cardsViewModel, context = context,
                    pageReferrer = PageReferrer(NewsReferrer.CARD_WIDGET), cardType =
            displayCardTypeIndex, nhJsInterfaceWithMenuClickHandling = nhJsInterfaceWithMenuClickHandling)
        }

        PostDisplayType.USER_INTERACTION.index -> {
            return UserInteractionViewHolder(viewBinding = viewDataBinding,
                    listEditInterface = listEditInterface,
                    pageReferrer = pageReferrer,
                    nsfwLiveData = LiveSharedPreference.pref(GenericAppStatePreference.SHOW_NSFW_FILTER, context ?: CommonUtils.getApplication(), true),
                    referrerProviderlistener = referrerProviderlistener)
        }
        PostDisplayType.ENTITY_INFO.index -> {
            return EntityViewHolder(
                    viewBinding = viewDataBinding,
                    pageReferrer = pageReferrer,
                    showAddPageButton = showAddPageButton,
                    cardsViewModel = cardsViewModel,
                    eventDedupHelper = eventDedupHelper,
                    section = section
            )
        }
        PostDisplayType.LANGUAGE_SELECT_CARD.index -> {
            return LanguageSelectionViewHolder(viewDataBinding, cardsViewModel, uniqueRequestId,
                    section,pageReferrer)
        }

        PostDisplayType.LOCATION_SELECT_CARD.index -> {
            return LocationSelectionViewHolder(viewDataBinding, cardsViewModel,section, eventDedupHelper,
                    pageReferrer)
        }

        PostDisplayType.COLLECTION_OF_COLLECTION_CARD.index -> {
            return NestedCollectionViewHolder(viewDataBinding,cardsViewModel,section,eventDedupHelper,pageReferrer, viewPool = viewPool)
        }
    }
    return SimplePostViewHolder(viewBinding = viewDataBinding,
        pageReferrer=pageReferrer, uniqueScreenId = uniqueRequestId, section = section,
        parentLifecycleOwner = parentLifeCycle, isDetailView = isDetailView, videoPrefetchCallback, displayCardTypeIndex)
}


fun getViewBinding(displayCardTypeIndex: Int, parent: ViewGroup, isDetailView: Boolean):
        ViewDataBinding {
    val layoutInflater = LayoutInflater.from(parent.context)

    //Check if card is an Ad.
    AdsViewHolderFactory.getViewBinding(displayCardTypeIndex, layoutInflater, parent)?.let {
        return it
    }

    return when (displayCardTypeIndex) {
        PostDisplayType.COLD_START_HEADER_CARD.index -> {
            DataBindingUtil.inflate<ViewDataBinding>(layoutInflater, R.layout
                    .layout_following_tab_header, parent, false)
        }
        PostDisplayType.IMAGES_5.index -> {
            DataBindingUtil
                    .inflate<com.newshunt.appview.databinding.Gallery5PostVhBinding>(layoutInflater,
                            R.layout.gallery5_post_vh,
                            parent,
                            false)
        }

        PostDisplayType.SIMPLE_POST_LOW.index -> {
            DataBindingUtil
                    .inflate<com.newshunt.appview.databinding.SimplePostSmallBinding>(layoutInflater,
                            if (isDetailView)
                                R.layout.simple_post_related
                            else
                                R.layout.simple_post_small,
                            parent,
                            false)
        }

        PostDisplayType.IMAGES_2.index -> {
            DataBindingUtil
                    .inflate<com.newshunt.appview.databinding.Gallery2PostVhBinding>(layoutInflater,
                            R.layout.gallery2_post_vh,
                            parent,
                            false)
        }
        PostDisplayType.IMAGES_4.index -> {
            DataBindingUtil
                    .inflate<com.newshunt.appview.databinding.Gallery4PostVhBinding>(layoutInflater,
                            R.layout.gallery4_post_vh,
                            parent,
                            false)
        }
        PostDisplayType.IMAGES_3.index -> {
            DataBindingUtil
                    .inflate<com.newshunt.appview.databinding.Gallery3PostVhBinding>(layoutInflater,
                            R.layout.gallery3_post_vh,
                            parent,
                            false)
        }
        PostDisplayType.VIRAL.index -> {
            DataBindingUtil
                    .inflate<com.newshunt.appview.databinding.SimplePostViralBinding>(layoutInflater,
                            if (isDetailView)
                                R.layout.simple_post_viral_related
                            else
                                R.layout.simple_post_viral,
                            parent,
                            false)
        }

        PostDisplayType.POLL.index -> {
            DataBindingUtil.inflate<com.newshunt.appview.databinding
            .PollPostVhBinding>(layoutInflater, R.layout.poll_post_vh, parent, false)
        }

        PostDisplayType.POLL_RESULT.index -> {
            DataBindingUtil.inflate<com.newshunt.appview.databinding
            .PollPostResultVhBinding>(layoutInflater, R.layout.poll_post_result_vh, parent, false)
        }

        PostDisplayType.MEMBER_INFO.index -> {
            DataBindingUtil.inflate(layoutInflater, R.layout.layout_group_member_card, parent, false)
        }

        PostDisplayType.GROUP_INVITE.index -> {
            DataBindingUtil.inflate(layoutInflater, R.layout.layout_group_invite_card, parent, false)
        }

        PostDisplayType.ENTITY_INFO.index -> {
            DataBindingUtil.inflate(layoutInflater, R.layout.entity_item, parent, false)
        }

        PostDisplayType.BANNER.index -> {
            DataBindingUtil.inflate<com.newshunt.appview.databinding.BannerVhBinding>(layoutInflater, R.layout.banner_vh, parent, false)
        }

        PostDisplayType.OG_ITEM.index -> {
            DataBindingUtil.inflate<com.newshunt.appview.databinding.SimpleOgVhBinding>(layoutInflater,
                    R.layout.simple_og_vh,
                    parent,
                    false)
        }
        PostDisplayType.AUTOPLAY_EXO.index,
        PostDisplayType.AUTOPLAY_WEB.index -> {
            if (isDetailView) {
                DataBindingUtil
                        .inflate<com.newshunt.appview.databinding.SimplePostSmallBinding>(layoutInflater,
                                R.layout.autoplay_related_vh, parent, false)
            }
            else {
                DataBindingUtil
                        .inflate<AutoplayVhBinding>(layoutInflater,
                                R.layout.autoplay_vh, parent, false)
            }
        }
        PostDisplayType.FOOTER.index -> {
            DataBindingUtil.inflate<com.newshunt.appview.databinding.FooterLayoutBinding>(
                    layoutInflater, R.layout.footer_layout, parent, false)
        }

        PostDisplayType.LOCAL.index -> {
            DataBindingUtil.inflate<com.newshunt.appview.databinding.FeedProgressLayoutBinding>(layoutInflater, R.layout.feed_progress_layout, parent, false)
        }

        PostDisplayType.QMC_CAROUSEL3.index,
        PostDisplayType.QMC_CAROUSEL1.index,
        PostDisplayType.QMC_CAROUSEL2.index,
        PostDisplayType.QMC_CAROUSEL4.index,
        PostDisplayType.QMC_CAROUSEL5.index,
        PostDisplayType.QMC_GRID_2.index,
        PostDisplayType.QMC_TAGS.index,
        PostDisplayType.QMC_GRID.index -> {
            DataBindingUtil.inflate<com.newshunt.appview.databinding.PostColdStartVhBinding>(layoutInflater,
                    R.layout.post_cold_start_vh,
                    parent,
                    false)
        }
        PostDisplayType.APPROVAL_CARD.index -> {
            DataBindingUtil.inflate<com.newshunt.appview.databinding
            .LayoutGroupApprovalCardBinding>(layoutInflater, R.layout.layout_approval_feed_card,
                    parent, false)
        }

        PostDisplayType.POST_COLLECTION_VIDEO.index,
        PostDisplayType.POST_COLLECTION_HTML.index,
        PostDisplayType.POST_COLLECTION_IMAGE.index,
        PostDisplayType.SQUARE_CARD_CAROUSEL.index,
        PostDisplayType.HTML_AND_VIDEO_CAROUSEL.index-> {
            DataBindingUtil.inflate<CollectionVhBinding>(layoutInflater,
                    R.layout.collection_vh,
                    parent,
                    false)
        }

        PostDisplayType.ASTRO.index -> {
            DataBindingUtil.inflate<com.newshunt.appview.databinding.LayoutCardAstroNewBinding>(
                    layoutInflater,
                    R.layout.layout_card_astro_new,
                    parent,
                    false)
        }
        PostDisplayType.POST_COLLECTION_AUTOPLAY.index -> {
            DataBindingUtil.inflate<CollectionAutoplayVhBinding>(layoutInflater,
                    R.layout.collection_autoplay_vh,
                    parent,
                    false)
        }

        PostDisplayType.TICKER.index -> {
            DataBindingUtil.inflate<TickerVhBinding>(layoutInflater,
                    R.layout.ticker_vh,
                    parent,
                    false)
        }

        PostDisplayType.SIMPLE_WEB.index -> {
            DataBindingUtil.inflate<com.newshunt.appview.databinding.LayoutViewholderWebcardBinding>(layoutInflater,
                    R.layout.layout_viewholder_webcard,
                    parent,
                    false)

        }

        PostDisplayType.USER_INTERACTION.index -> {
            DataBindingUtil.inflate<LayoutProfileActivityCardBinding>(layoutInflater,
                    R.layout.layout_profile_activity_card,
                    parent,
                    false)
        }

        PostDisplayType.LOCAL_NORMAL.index -> {
            DataBindingUtil.inflate<com.newshunt.appview.databinding.LocalCardNormalBinding>(layoutInflater, R.layout.local_card_normal, parent, false)
        }

        PostDisplayType.LOCAL_POLL.index -> {
            DataBindingUtil.inflate<com.newshunt.appview.databinding.PollPostResultVhBinding>(layoutInflater, R.layout.poll_post_result_vh, parent, false)
        }

        PostDisplayType.LOCAL_OG.index -> {
            DataBindingUtil.inflate<com.newshunt.appview.databinding.SimpleOgVhBinding>(layoutInflater,
                    R.layout.simple_og_vh,
                    parent,
                    false)
        }

        PostDisplayType.SEARCH_PHOTO_GRID.index -> {
            DataBindingUtil.inflate<com.newshunt.appview.databinding.SearchPhotoGridBinding>(
                    layoutInflater,
                    R.layout.search_photo_grid,
                    parent,
                    false
            )
        }

        PostDisplayType.LANGUAGE_SELECT_CARD.index -> {
            DataBindingUtil.inflate<ViewDataBinding>(layoutInflater, R.layout
                    .layout_language_selection_viewholder, parent, false)
        }

        PostDisplayType.LOCATION_SELECT_CARD.index -> {
            DataBindingUtil.inflate<ViewDataBinding>(layoutInflater, R.layout
                    .layout_location_selection_viewholder, parent, false)
        }
        PostDisplayType.DATE_SEPARATOR.index -> {
            DataBindingUtil.inflate<ViewDataBinding>(layoutInflater, R.layout
                    .layout_profile_activity_group_date, parent, false)
        }
        PostDisplayType.GUEST_USER.index -> {
            DataBindingUtil.inflate<ViewDataBinding>(layoutInflater, R.layout
                .layout_guest_user, parent, false)
        }
        PostDisplayType.SIMPLE_POST_DYNAMIC_HERO.index->{
            DataBindingUtil.inflate<ViewDataBinding>(layoutInflater, R.layout
                    .simple_post_vh_dynamic_hero, parent, false)
        }
        PostDisplayType.COLLECTION_OF_COLLECTION_CARD.index->{
            DataBindingUtil.inflate<ViewDataBinding>(layoutInflater, R.layout
                .simple_post_vh_collection_of_collection, parent, false)
        }
        else -> {
            DataBindingUtil
                    .inflate<SimplePostVhBinding>(layoutInflater,
                            if (isDetailView)
                                R.layout.simple_post_related
                            else
                                R.layout.simple_post_vh,
                            parent,
                            false)
        }
    }
}

fun findCardTypeIndex(
        asset: Any?,
        itemLocation: String? = null,
        tabType: String? = null,
        videoRequester: VideoRequester? = null
): Pair<Int, Int> {
    asset ?: return PostDisplayType.EMPTY.index to 0
    var repostCardType: Int = 0
    if (asset is Extra) {
        return when (asset.type) {
            ExtraListObjType.DATE_SEPARATOR -> PostDisplayType.DATE_SEPARATOR.index to 0
            ExtraListObjType.GUEST_USERS -> PostDisplayType.GUEST_USER.index to 0
            else -> PostDisplayType.FOOTER.index to 0
        }
    }
    if (CommonUtils.equals(tabType, Format.PHOTO.name) && asset is CommonAsset)
        return PostDisplayType.SEARCH_PHOTO_GRID.index to 0
    val parentCardType: Int

    if (asset is UserFollowView) {
        parentCardType = PostDisplayType.ENTITY_INFO.index
    } else if ((asset as? CommonAsset)?.isLocalCard() == true && itemLocation != Constants.ITEM_LOCATION_LOCAL
            && (asset as? CommonAsset)?.i_format() == Format.LOCAL) {
        parentCardType = PostDisplayType.LOCAL.index
    } else if (asset is CommonAsset) {
        parentCardType = getPrimaryCardType(asset, itemLocation, videoRequester)
        if (asset.i_repostAsset() != null) {
            repostCardType = getDisplayCardTypeForRepost(asset)
        }
    } else if (asset is Member) {
        parentCardType = PostDisplayType.MEMBER_INFO.index
    } else if (asset is GroupInfo) {
        parentCardType = PostDisplayType.GROUP_INVITE.index
    } else {
        parentCardType = PostDisplayType.SIMPLE_POST.index
    }
    return parentCardType to repostCardType
}

fun getDisplayCardTypeForLocalCard(asset: CommonAsset): Int {
    if (asset.i_linkAsset() != null) {
        return PostDisplayType.OG_ITEM.index
    }
    val uiType = asset.i_uiType()
    return when (uiType) {
        UiType2.NORMAL -> PostDisplayType.LOCAL_NORMAL.index
        UiType2.HORIZONTAL_BAR -> PostDisplayType.LOCAL_POLL.index
        else -> {
            PostDisplayType.LOCAL_NORMAL.index
        }
    }
}


fun getPrimaryCardType(asset: CommonAsset, itemLocation: String? = null, videoRequester: VideoRequester?): Int {
    return when (asset.i_format()) {
        Format.HTML -> getHtmlCardType(asset, itemLocation ?: Constants.EMPTY_STRING)
        Format.NESTED_COLLECTION -> PostDisplayType.COLLECTION_OF_COLLECTION_CARD.index
        Format.COLLECTION -> getCollectionCardType(asset)
        Format.POST_COLLECTION -> getPostCollectionCardType(asset)
        Format.EMBEDDED_VIDEO -> getEmbeddedCardType(asset)
        Format.IMAGE -> getImageCardType(asset)
        Format.POLL -> getPollCardType(asset)
        Format.VIDEO -> getVideoCardType(asset, itemLocation ?: Constants.EMPTY_STRING, videoRequester)
        Format.NATIVE_CARD -> getNativeCardType(asset)
        Format.LOCAL -> getDisplayCardTypeForLocalCard(asset)
        Format.BANNER -> getBannerCardType(asset)
        Format.AD -> getAdCardType(asset)
        Format.WEB -> PostDisplayType.SIMPLE_WEB.index
        Format.TICKER -> PostDisplayType.TICKER.index
        Format.LANGUAGE -> getLanguageSelectCard(asset)
        else -> {
            PostDisplayType.SIMPLE_POST.index
        }
    }
}

fun getLanguageSelectCard(asset: CommonAsset): Int {
    return when (asset.i_subFormat()) {
        SubFormat.LANGUAGE_SELECT -> {
            PostDisplayType.LANGUAGE_SELECT_CARD.index
        }
        else -> {
            PostDisplayType.SIMPLE_POST.index
        }
    }
}

fun getAdCardType(asset: CommonAsset): Int {
    check(asset is BaseAdEntity) { "Only BaseAdEntity can have Format.AD" }
    return AdsUtil.getCardTypeForAds(asset)
}

fun getNativeCardType(asset: CommonAsset): Int {
    val subFormat = asset.i_subFormat()
    val uiType = asset.i_uiType()
    return when (subFormat) {
        SubFormat.ASTRO -> {
            when (uiType) {
                UiType2.NORMAL, UiType2.SUBSCRIBE -> PostDisplayType.ASTRO.index
                UiType2.USER_INTERACTION -> PostDisplayType.USER_INTERACTION.index
                else -> PostDisplayType.SIMPLE_POST.index
            }
        }
        SubFormat.COLD_START_HEADER_CARD -> {
            when (uiType) {
                UiType2.NORMAL -> PostDisplayType.COLD_START_HEADER_CARD.index
                else -> PostDisplayType.SIMPLE_POST.index
            }
        }

        SubFormat.LOCATION -> {
            PostDisplayType.LOCATION_SELECT_CARD.index
        }
        else -> PostDisplayType.SIMPLE_POST.index
    }
}

fun getBannerCardType(asset: CommonAsset): Int {
    val subFormat = asset.i_subFormat()
    when (subFormat) {
        SubFormat.PENDING_APPROVAL -> return PostDisplayType.APPROVAL_CARD.index
        else -> {
            return PostDisplayType.BANNER.index
        }
    }
}

fun getDisplayCardTypeForRepost(asset: CommonAsset): Int {
    val subFormat = asset.i_repostAsset()?.subFormat
    val format = asset.i_repostAsset()?.format
    val uiType = asset.i_repostAsset()?.uiType
    if (format == Format.POLL) {
        return PostDisplayType.REPOST_POLL.index
    }
    if (asset.i_repostAsset()?.i_viral() != null) {
        return PostDisplayType.REPOST_VIRAL.index
    }
    if (asset.i_repostAsset()?.i_linkAsset() != null)
        return PostDisplayType.REPOST_OG.index
    if (subFormat == SubFormat.STORY || subFormat == SubFormat.S_W_IMAGES || subFormat == SubFormat.S_W_PHOTOGALLERY) {
        when (uiType) {
            UiType2.NORMAL -> return PostDisplayType.REPOST_NORMAL.index
            UiType2.HERO -> return PostDisplayType.REPOST_BIG_IMAGE.index
            else -> {
                //Do nothing let fallback handle
            }
        }
    }
    if (asset.i_repostAsset()?.title.isNullOrEmpty() && asset.i_repostAsset()?.content.isNullOrEmpty())
        return PostDisplayType.REPOST_BIG_IMAGE.index

    return PostDisplayType.REPOST_NORMAL.index
}

fun getVideoCardType(asset: CommonAsset, itemLocation: String, videoRequester: VideoRequester?): Int {
    val subFormat = asset.i_subFormat()
    if (CommonUtils.equals(itemLocation, GroupLocations.BOOKMARKS_LIST.name)) {
        return PostDisplayType.SAVED_VIDEO_LIST_ITEM.index
    } else if (itemLocation == Constants.LIST_TYPE_BOOKMARKS) {
        return PostDisplayType.USER_INTERACTION.index
    }
    if (subFormat == SubFormat.TVVIDEO || subFormat == SubFormat.TVGIF) {
        when (asset.i_uiType()) {
            UiType2.AUTOPLAY -> {
                if(videoRequester == null ||
                        !DHVideoUtils.isAutoPlaySupported(asset.i_videoAsset())) {
                    //Player is not supported fallback to simple post
                    return PostDisplayType.SIMPLE_POST.index
                }
                if (DHVideoUtils.isExoPlayer(asset.i_videoAsset()))
                    return PostDisplayType.AUTOPLAY_EXO.index
                else
                    return PostDisplayType.AUTOPLAY_WEB.index
            }
            UiType2.USER_INTERACTION -> {
                return PostDisplayType.USER_INTERACTION.index
            }

            UiType2.NORMAL -> {
                return PostDisplayType.SIMPLE_POST_LOW.index
            }
            else -> {
                return PostDisplayType.SIMPLE_POST.index
            }
        }
    }
    return PostDisplayType.SIMPLE_POST.index
}

fun getPollCardType(asset: CommonAsset): Int {
    val subFormat = asset.i_subFormat()
    val uiType = asset.i_uiType()
    if (subFormat == SubFormat.SINGLE_SELECT) {
        when (uiType) {
            UiType2.HORIZONTAL_BAR -> {
                if (CardsBindUtils.canShowPollResult(asset))
                    return PostDisplayType.POLL_RESULT.index
                else
                    return PostDisplayType.POLL.index
            }
            UiType2.USER_INTERACTION -> {
                return PostDisplayType.USER_INTERACTION.index
            }
            else -> {
                Logger.e(LOG_TAG, "subformat : $subFormat -> Falling back for ui type : $uiType")
                //Let the foll back handle
            }
        }
    }
    return PostDisplayType.SIMPLE_POST.index
}

fun getImageCardType(asset: CommonAsset): Int {
    val subFormat = asset.i_subFormat()
    val uiType = asset.i_uiType()
    return when (subFormat) {
        SubFormat.VHGIF,
        SubFormat.VHMEME,
        SubFormat.VHMEMETEXT,
        SubFormat.VHTEXT -> {
            when (uiType) {
                UiType2.VH_FIT_BACKGROUND,
                UiType2.VH_BIG,
                UiType2.VH_SMALL -> {
                    PostDisplayType.VIRAL.index
                }
                UiType2.USER_INTERACTION -> {
                    return PostDisplayType.USER_INTERACTION.index
                }
                else -> {
                    PostDisplayType.SIMPLE_POST.index
                }
            }

        }
        else -> PostDisplayType.SIMPLE_POST.index
    }
}

fun getEmbeddedCardType(asset: CommonAsset): Int {
    val subFormat = asset.i_subFormat()
    val uiType = asset.i_uiType()
    return if (subFormat == SubFormat.TVVIDEO || subFormat == SubFormat.TVGIF) {
        when (uiType) {
            UiType2.NORMAL -> PostDisplayType.SIMPLE_POST_LOW.index
            UiType2.AUTOPLAY -> {
                if (!DHVideoUtils.isAutoPlaySupported(asset.i_videoAsset())) {
                    //Player is not supported fallback to simple post
                    return PostDisplayType.SIMPLE_POST.index
                }
                if (DHVideoUtils.isExoPlayer(asset.i_videoAsset()))
                    return PostDisplayType.AUTOPLAY_EXO.index
                else
                    return PostDisplayType.AUTOPLAY_WEB.index
            }
            UiType2.USER_INTERACTION -> PostDisplayType.USER_INTERACTION.index
            else -> PostDisplayType.SIMPLE_POST.index

        }
    } else {
        PostDisplayType.SIMPLE_POST.index
    }
}

fun getCollectionCardType(asset: CommonAsset): Int {
    val subFormat = asset.i_subFormat() ?: run {
        Logger.e(LOG_TAG, "Subformat should not be null")
        return PostDisplayType.SIMPLE_POST.index
    }
    val uiType = asset.i_uiType() ?: run {
        Logger.e(LOG_TAG, "UiType should not be null")
        return PostDisplayType.SIMPLE_POST.index
    }
    return when (subFormat) {
        SubFormat.ENTITY -> {
            when (uiType) {
                UiType2.GRID -> PostDisplayType.QMC_GRID.index
                UiType2.GRID_2 -> PostDisplayType.QMC_GRID_2.index
                UiType2.CAROUSEL_1 -> PostDisplayType.QMC_CAROUSEL1.index
                UiType2.CAROUSEL_2 -> PostDisplayType.QMC_CAROUSEL2.index
                UiType2.CAROUSEL_3 -> PostDisplayType.QMC_CAROUSEL3.index
                UiType2.CAROUSEL_4 -> PostDisplayType.QMC_CAROUSEL4.index
                UiType2.CAROUSEL_5 -> PostDisplayType.QMC_CAROUSEL5.index
                UiType2.CAROUSEL_6 -> PostDisplayType.QMC_CAROUSEL6.index
                UiType2.USER_INTERACTION -> PostDisplayType.USER_INTERACTION.index

                UiType2.TAGS -> PostDisplayType.QMC_TAGS.index
                else -> PostDisplayType.QMC_CAROUSEL1.index
            }
        }
        else -> PostDisplayType.SIMPLE_POST.index
    }
}

fun getPostCollectionCardType(asset: CommonAsset): Int {
    val subFormat = asset.i_subFormat() ?: run {
        Logger.e(LOG_TAG, "Subformat should not be null")
        return PostDisplayType.SIMPLE_POST.index
    }
    val uiType = asset.i_uiType() ?: run {
        Logger.e(LOG_TAG, "UiType should not be null")
        return PostDisplayType.SIMPLE_POST.index
    }
    if (uiType == UiType2.USER_INTERACTION) {
        return PostDisplayType.USER_INTERACTION.index
    }
    if (asset.i_collectionItems().isNullOrEmpty() && uiType != UiType2.CAROUSEL_6) {
        return PostDisplayType.EMPTY.index
    }
    return when (subFormat) {
        SubFormat.HTML -> {
            if (uiType == UiType2.CAROUSEL_1) {
                return PostDisplayType.POST_COLLECTION_HTML.index
            } else if (uiType == UiType2.CAROUSEL_6) {
                return PostDisplayType.POST_COLLECTION_SAVED_STORIES.index
            } else {
                return PostDisplayType.SIMPLE_POST.index
            }
        }

        SubFormat.VIDEO -> {
            if (uiType == UiType2.CAROUSEL_7 || uiType == UiType2.CAROUSEL_1) {
                return PostDisplayType.POST_COLLECTION_VIDEO.index
            } else if (uiType == UiType2.CAROUSEL_8) {
                return PostDisplayType.POST_COLLECTION_AUTOPLAY.index
            } else if (uiType == UiType2.CAROUSEL_6) {
                return PostDisplayType.POST_COLLECTION_SAVED_VIDEOS.index
            } else {
                return PostDisplayType.SIMPLE_POST.index
            }
        }

        SubFormat.HTML_AND_VIDEO -> {
                return PostDisplayType.HTML_AND_VIDEO_CAROUSEL.index
        }

        SubFormat.IMAGE -> {
            if (uiType == UiType2.CAROUSEL_1) {
                return PostDisplayType.POST_COLLECTION_IMAGE.index
            } else {
                return PostDisplayType.SIMPLE_POST.index
            }
        }

        SubFormat.VIRAL_AND_VIDEO -> {
            if (uiType == UiType2.CAROUSEL_1) {
                return PostDisplayType.POST_COLLECTION_IMAGE.index
            } else if(uiType == UiType2.CAROUSEL_9){
                return PostDisplayType.SQUARE_CARD_CAROUSEL.index
            } else {
                return PostDisplayType.SIMPLE_POST.index
            }
        }
        else -> PostDisplayType.SIMPLE_POST.index
    }
}

fun getHtmlCardType(asset: CommonAsset, itemLocation: String): Int {
    if (CommonUtils.equals(itemLocation, GroupLocations.BOOKMARKS_LIST.name)) {
        return PostDisplayType.SAVED_STORY_LIST_ITEM.index
    } else if (itemLocation == Constants.LIST_TYPE_BOOKMARKS) {
        return PostDisplayType.USER_INTERACTION.index
    }

    val uiType = asset.i_uiType()
    if (uiType == UiType2.USER_INTERACTION) {
        return PostDisplayType.USER_INTERACTION.index
    }
    val subFormat = asset.i_subFormat()
    if (asset.i_linkAsset() != null) {
        return PostDisplayType.OG_ITEM.index
    }

    if (subFormat == SubFormat.STORY) {
        when (uiType) {
            UiType2.NORMAL -> return PostDisplayType.SIMPLE_POST_LOW.index
            UiType2.HERO -> return PostDisplayType.SIMPLE_POST.index
            UiType2.HERO_DYNAMIC -> return PostDisplayType.SIMPLE_POST_DYNAMIC_HERO.index
            else -> {
                //Do nothing let fallback handle
            }
        }
    }
    if (subFormat == SubFormat.S_W_VIDEO) {
        when (uiType) {
            UiType2.NORMAL -> return PostDisplayType.SIMPLE_POST_LOW.index
            UiType2.HERO_DYNAMIC -> return PostDisplayType.SIMPLE_POST_DYNAMIC_HERO.index
            else -> {
                //Do nothing let fallback handle
            }
        }
    }
    if (subFormat == SubFormat.S_W_IMAGES) {
        when (uiType) {
            UiType2.NORMAL -> return PostDisplayType.SIMPLE_POST_LOW.index
            UiType2.HERO -> return PostDisplayType.SIMPLE_POST.index
            UiType2.GRID_3 -> return PostDisplayType.IMAGES_3.index
            UiType2.GRID_2 -> return PostDisplayType.IMAGES_2.index
            UiType2.HERO_DYNAMIC -> return PostDisplayType.SIMPLE_POST_DYNAMIC_HERO.index
            else -> {
                //Do nothing let fallback handle
            }
        }
    }
    if (subFormat == SubFormat.S_W_PHOTOGALLERY) {
        when (uiType) {
            UiType2.GRID_3 -> return PostDisplayType.IMAGES_3.index
            UiType2.GRID_5 -> return PostDisplayType.IMAGES_5.index
            UiType2.HERO_DYNAMIC -> return PostDisplayType.SIMPLE_POST_DYNAMIC_HERO.index
            else -> {
                //Do nothing let fallback handle
            }
        }
    }

    if (subFormat == SubFormat.RICH_PHOTOGALLERY) {
        when (uiType) {
            UiType2.GRID_3 -> return PostDisplayType.IMAGES_3.index
            UiType2.GRID_5 -> return PostDisplayType.IMAGES_5.index
            UiType2.GRID_4 -> return PostDisplayType.IMAGES_4.index
            UiType2.HERO_DYNAMIC -> return PostDisplayType.SIMPLE_POST_DYNAMIC_HERO.index
            else -> {
                //Do nothing let fallback handle
            }
        }
    }

    if (subFormat == SubFormat.S_W_ATTACHED_IMAGES) {
        when (uiType) {
            UiType2.NORMAL -> return PostDisplayType.SIMPLE_POST_LOW.index
            UiType2.HERO -> return PostDisplayType.SIMPLE_POST.index
            UiType2.GRID_2 -> return PostDisplayType.IMAGES_2.index
            UiType2.GRID_3 -> return PostDisplayType.IMAGES_3.index
            UiType2.GRID_4 -> return PostDisplayType.IMAGES_4.index
            UiType2.GRID_5 -> return PostDisplayType.IMAGES_5.index
            UiType2.HERO_DYNAMIC -> return PostDisplayType.SIMPLE_POST_DYNAMIC_HERO.index
            else -> {
                //Do nothing let fallback handle
            }
        }
    }

    return PostDisplayType.SIMPLE_POST.index
}

class CardsAdapterDiffUtilCallback : DiffUtil.ItemCallback<Any?>() {
    override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
        if (oldItem is CommonAsset && newItem is CommonAsset) {
            return newItem.i_id() == oldItem.i_id()
        } else if (oldItem is SocialHandleInfo && newItem is SocialHandleInfo) {
            return newItem.s_id() == oldItem.s_id()
        } else if (oldItem is Extra && newItem is Extra) {
            return oldItem == newItem
        }
        return false
    }

    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean {
        return oldItem == newItem
    }

    override fun getChangePayload(oldItem: Any, newItem: Any): Any? {
        return Bundle()
    }
}

class CardsAdapterDiffUtilCallback2(private val oldList: List<Any?>,
                                    private val newList: List<Any?>,
                                    private val callback: DiffUtil.ItemCallback<Any?>) : DiffUtil
.Callback() {
    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItem = oldList[oldItemPosition]
        val newItem = newList[newItemPosition]
        if (oldItem === newItem) {
            return true
        }
        return if (oldItem == null || newItem == null) {
            false
        } else callback.areItemsTheSame(oldItem, newItem)
    }

    override fun getOldListSize(): Int {
        return oldList.size
    }

    override fun getNewListSize(): Int {
        return newList.size
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItem = oldList[oldItemPosition]
        val newItem = newList[newItemPosition]
        if (oldItem === newItem) {
            return true
        }
        return if (oldItem == null || newItem == null) {
            false
        } else callback.areContentsTheSame(oldItem, newItem)
    }

    override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
        val oldItem = oldList[oldItemPosition]
        val newItem = newList[newItemPosition]
        return if (oldItem == null || newItem == null) {
            null
        } else callback.getChangePayload(oldItem, newItem)
    }
}