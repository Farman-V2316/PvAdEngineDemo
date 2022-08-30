/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.appview.common.ui.viewholder

import android.content.Context
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.newshunt.adengine.listeners.ReportAdsMenuListener
import com.newshunt.adengine.model.entity.ContentAdDelegate
import com.newshunt.appview.BR
import com.newshunt.appview.R
import com.newshunt.appview.common.ui.adapter.CollectionViewHolder
import com.newshunt.appview.common.ui.helper.CardsBindUtils
import com.newshunt.appview.common.viewmodel.ClickHandlingViewModel
import com.newshunt.appview.databinding.SimplePostVhCollectionItemBinding
import com.newshunt.appview.databinding.SimplePostViralCollectionItemBinding
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.common.ViewUtils
import com.newshunt.common.helper.preference.GenericAppStatePreference
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.common.view.customview.CarouselProgressCallback
import com.newshunt.common.view.customview.LongPress
import com.newshunt.common.view.customview.MultimediaCarouselViewPager
import com.newshunt.common.view.customview.MultimediaViewPagerCallback
import com.newshunt.common.view.customview.kenburns.NhKenBurnsImageView
import com.newshunt.dataentity.analytics.entity.AnalyticsParam
import com.newshunt.dataentity.analytics.entity.NhAnalyticsEventParam
import com.newshunt.dataentity.analytics.entity.NhAnalyticsUserAction
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.common.asset.CommonAsset
import com.newshunt.dataentity.common.asset.ParentIdHolderCommenAsset
import com.newshunt.dataentity.common.asset.PostDisplayType
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.news.model.entity.server.asset.AnimationType
import com.newshunt.dhutil.analytics.AnalyticsHelper2
import com.newshunt.dhutil.commons.listener.ReferrerProviderlistener
import com.newshunt.news.analytics.NhAnalyticsNewsEvent
import com.newshunt.news.util.EventDedupHelper
import com.newshunt.news.util.EventKey
import com.newshunt.viral.utils.visibility_utils.VisibilityAwareViewHolder

private const val VISIBILITY_THRESHOLD = 90
private const val DEFAULT_AUTO_SWIPE_INTERVAL = 5L

class CollectionPostViewHolder(private val viewBinding: ViewDataBinding,
                               private val vm: ViewModel,
                               cardType: Int,
                               private val section: String = Constants.EMPTY_STRING,
                               private val referrer: PageReferrer? = null,
                               private val eventDedupHelper: EventDedupHelper,
                               private val referrerProviderlistener: ReferrerProviderlistener?,
                               uniqueScreenId: Int,
                               private val adsMenuListener: ReportAdsMenuListener?,
                               displayIndex: Int) : CardsViewHolder(viewBinding.root),
        CarouselProgressCallback, VisibilityAwareViewHolder, CollectionCard,
        ViewPager.OnPageChangeListener, CardItemClickListener, CommonAssetViewHolder, CollectionViewHolder {
    init {
        displayTypeIndex = displayIndex
        Logger.d(LOG_TAG, "Create collection card $cardType")
    }

    private val autoplayVisibility = PreferenceManager.getPreference(GenericAppStatePreference
            .MIN_VISIBILITY_FOR_ANIMATION, VISIBILITY_THRESHOLD)
    private val viewHolderWidth = CommonUtils.getDeviceScreenWidth() -
            2 * CommonUtils.getDimension(R.dimen.story_card_padding)
    private val viewPager = viewBinding.root.findViewById<MultimediaCarouselViewPager>(R.id.viewpager)
    private var currentItem: CommonAsset? = null

    //Need to update this value on each bind call
    private var canCircularSwipeValue: Boolean = false
    private var viewPagerSelectedPage = 0
    private val viewPagerCallback: MultimediaViewPagerCallback = object : MultimediaViewPagerCallback {
        override fun onLongPress(longPressEvent: LongPress, longPressedPosition: Int) {
            if (longPressEvent == LongPress.PRESSED) {
                pauseAnimation()
                adapter.onAnimationPause()
            } else {
                resumeAnimation()
                adapter.onAnimationResume()
            }
        }
    }

    override fun resumeAnimation() {
        Logger.d(LOG_TAG, "Resume animation")
    }

    override fun pauseAnimation() {
        Logger.d(LOG_TAG, "Pause animation")
    }

    override fun onVisible(viewVisibilityPercentage: Int, percentageOfScreen: Float) {
        adapter.triggerAdBeacon(viewPagerSelectedPage)
        if (viewVisibilityPercentage > autoplayVisibility) {
            resumeAnimation()
        } else {
            pauseAnimation()
        }
    }

    override fun onInVisible() {
        pauseAnimation()
    }

    override fun onUserLeftFragment() {
        pauseAnimation()
    }

    override fun onUserEnteredFragment(viewVisibilityPercentage: Int, percentageOfScreen: Float) {
        if (viewVisibilityPercentage > autoplayVisibility) {
            resumeAnimation()
        } else {
            pauseAnimation()
        }
    }

    private val adapter = CarouselAdapter(vm = vm, parentCardType = cardType, context = viewBinding.root.context,
            cardItemClickListener = this, uniqueScreenId = uniqueScreenId)

    override fun bind(item: Any?, lifecycleOwner: LifecycleOwner?, cardPosition: Int) {
        Logger.d(TAG, "bind $adapterPosition")
        if (item !is CommonAsset) return
        //Validation  for required fields in collection display
        if (!validateItem(item)) {
            Logger.e(LOG_TAG, "Not valid collection item")
            return
        }
        if (!updateRequired(currentItem, item)) {
            Logger.i(LOG_TAG, "Post collection update not required")
            return
        }
        if (lifecycleOwner != null) {
            viewBinding.lifecycleOwner = lifecycleOwner
        }

        val isNewItem = item.i_id() != currentItem?.i_id()
        val isListSizeChanges = item.i_collectionItems()?.size != currentItem?.i_collectionItems()?.size
        currentItem = item
        if (isNewItem) {
            Logger.d(TAG, "new binding ${item.i_id()} old = ${currentItem?.i_id()}")
            viewBinding.setVariable(BR.item, item)
            viewBinding.executePendingBindings()
            canCircularSwipeValue = canCircularSwipe(item)

            triggerCardViewEvent()
            triggerCollectionItemViewEvent(0, NhAnalyticsUserAction.VIEW)
        }
        setupViewPager(viewPager, item, isNewItem, isListSizeChanges, lifecycleOwner)
    }

    override fun getViewForAnimationByItemId(storyId: String): View? {
        return adapter.getViewForItemId(storyId)
    }

    private fun updateRequired(oldItem: CommonAsset?, newItem: CommonAsset): Boolean {
        oldItem ?: return true
        if (oldItem.i_id() != newItem.i_id()) {
            return true
        }
        return oldItem.i_collectionItems() != newItem.i_collectionItems()
    }

    private fun setupViewPager(viewPager: MultimediaCarouselViewPager,
                               item: CommonAsset,
                               isNewBinding: Boolean,
                               isListSizeChanged: Boolean,
                               lifecycleOwner: LifecycleOwner?) {
        //adapter.addClearableImageView(clearableImageViews)
        adapter.update(item, item.i_collectionItems()!!, canCircularSwipe(item), lifecycleOwner,
                adapterPosition)

        if (!isNewBinding && !isListSizeChanged) {
            return
        }

        if (isNewBinding) {
            viewPager.adapter = adapter
            viewPager.clipToPadding = false
            viewPager.setPageTransformer(false,
                ViewPager.PageTransformer { page, position ->
                    if (viewPager.currentItem == adapter.count - 1) {
                        page.translationX = CommonUtils.getDeviceScreenWidth() - itemView.width - CommonUtils.getDimension(R.dimen.collection_margin).toFloat()
                        viewPager.setPadding(((CommonUtils.getDeviceScreenWidth())*(CardsBindUtils.getSecondItemVisiblePercentage()/100f)).toInt(), 0, 0, 0)
                    } else {
                        viewPager.setPadding(0, 0, ((CommonUtils.getDeviceScreenWidth())*(CardsBindUtils.getSecondItemVisiblePercentage()/100f)).toInt(), 0)
                        page.translationX = CommonUtils.getDimension(R.dimen.collection_margin).toFloat()
                    }
                })

            viewPager.addOnPageChangeListener(this)
        }
        if (isNewBinding) {
            viewPager.resetView()
            viewPager.setCallback(viewPagerCallback)
        }

        if (canCircularSwipe(item)) {
            viewPager.currentItem = 1
            viewPagerSelectedPage = 1
        } else {
            viewPager.currentItem = 0
            viewPagerSelectedPage = 0
        }
    }

    private fun validateItem(item: CommonAsset): Boolean {
        item.i_collectionItems() ?: run {
            Logger.e(LOG_TAG, "Collection item list is null")
            return false
        }
        if (item.i_collectionItems()!!.isEmpty()) {
            Logger.e(LOG_TAG, "Collection item list is empty")
            return false
        }
        return true
    }

    private fun canCircularSwipe(item: CommonAsset): Boolean {
        return false
    }

    private fun getSwipeIntervalTime(item: CommonAsset): Long {
        val serverInterval = item.i_carouselProperties()?.autoSwipeListIntervalInSeconds ?: 0
        if (serverInterval > 0) {
            return Math.max(serverInterval, DEFAULT_AUTO_SWIPE_INTERVAL)
        }
        return 0
    }

    private fun canAutoSwipe(item: CommonAsset): Boolean {
        return getSwipeIntervalTime(item) > 0
    }


    //PROGRESS BAR CALLBACK
    override fun selectPage(index: Int) {
        Logger.d(LOG_TAG, "Progress Callback : Selected page : $index")
        if (canCircularSwipeValue) {
            viewPager.setCurrentItem(index + 1, true)
        } else {
            viewPager.setCurrentItem(index, true)
        }
    }

    //VIEW PAGER CALLBACK
    override fun onPageScrollStateChanged(state: Int) {
        if (state == ViewPager.SCROLL_STATE_IDLE) {
            //Handle dummy page selection
            if (canCircularSwipeValue) {
                if (viewPagerSelectedPage == adapter.count - 1) {
                    viewPager.setCurrentItem(1, false)
                } else if (viewPagerSelectedPage == 0) {
                    viewPager.setCurrentItem((adapter.count - 1)/*last dummy page*/ - 1/*last data item page*/, false)
                } else {
                    //Do nothing
                }
            } else {
                //Do nothing
            }
        }
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

    }

    override fun onPageSelected(position: Int) {
        viewPagerSelectedPage = position
        if (isVisibleOnScreen) {
            adapter.triggerAdBeacon(viewPagerSelectedPage)
        }

        triggerCollectionItemViewEvent(position, NhAnalyticsUserAction.SWIPE)
    }

    private val isVisibleOnScreen: Boolean
        get() {
          return ViewUtils.getVisibilityPercentage(viewPager) > 0
        }

    private fun getTotalPageCount(item: CommonAsset): Int {
        return item.i_collectionItems()!!.size
    }

    companion object {
        const val TAG = "SimplePostViewHolder"
    }

    private fun triggerCollectionItemViewEvent(position: Int, action: NhAnalyticsUserAction) {
        val item = currentItem ?: return
        val eventKey = EventKey(NhAnalyticsNewsEvent.COLLECTION_PREVIEW_VIEW, mapOf(
                Constants.ITEM_ID to item.i_id(),
                Constants.ITEM_LOCATION to position
        ))
        eventDedupHelper.fireEvent(eventKey, Runnable {
            referrer?.referrerAction = action
            val eventParams = HashMap<NhAnalyticsEventParam, Any?>()
            eventParams[AnalyticsParam.COLLECTION_ID] = currentItem?.i_id()
            eventParams[AnalyticsParam.COLLECTION_TYPE] = Constants.MM_CAROUSEL
            AnalyticsHelper2.logCollectionEventViewItemInCard(collection = currentItem,
                    pageReferrer = referrer,
                    position = position,
                    mapParam = eventParams,
                    section = section
            )
        })
    }

    private fun triggerCardViewEvent() {
        val item = currentItem ?: return
        val eventKey = EventKey(NhAnalyticsNewsEvent.COLLECTION_PREVIEW_VIEW, mapOf(
                Constants.ITEM_ID to item.i_id()
        ))
        eventDedupHelper.fireEvent(eventKey, Runnable {
            AnalyticsHelper2.logCollectionViewEvent(item,
                    referrerFlow = null,
                    referrerLead = null,
                    referrer = referrer,
                    referrerRaw = null,
                    mapParam = null,
                    section = AnalyticsHelper2.getSection(section))
        })
    }

    override fun onItemClicked() {
    }

    override fun curData(): Pair<View, CommonAsset?>  = itemView to currentItem
}

private class CarouselAdapter(private val vm: ViewModel,
                              private val parentCardType: Int,
                              private val context: Context,
                              private val cardItemClickListener: CardItemClickListener,
                              private val uniqueScreenId: Int) : CachedPagerAdapter() {

    private var parentItem: CommonAsset? = null
    private val items: MutableList<Any> = mutableListOf()
    private var imageList: MutableList<ImageView?> = mutableListOf()
    private var lifecycleOwner: LifecycleOwner? = null
    private var adapterPosition:Int=-1
    var isCircular: Boolean = false
        private set

    private var currentView: View? = null
    private var currentPosition: Int = 0

    private var adDelegates = HashMap<String, ContentAdDelegate>()

    override fun getCount(): Int {
        return items.size +
                (if (isCircular) EXTRA_VIEWS_COUNT else 0)
    }

    override fun getViewBinding(convert: ViewDataBinding?, position: Int, parent: ViewGroup): ViewDataBinding {
        if (convert != null) {
            return run {
                bindView(convert, position)
                clearAnimationPropertyForPage(convert.root)
                convert
            }
        } else {
            val binding = createView(position, parent)
            clearAnimationPropertyForPage(binding.root)
            return binding
        }
    }

    private fun clearAnimationPropertyForPage(page: View) {
        page.clearAnimation()
        page.translationX = 0f
    }

    fun addClearableImageView(list: MutableList<ImageView?>) {
        imageList = list
    }

    fun getViewForItemId(itemId: String): View? {
        if (items.isEmpty()) return null
        val itemIndex = items.indexOfFirst {
            (it as? CommonAsset)?.i_id() == itemId
        }
        if (itemIndex >= 0) {
            getActivePageViews(itemIndex)?.let {
                return it.root.findViewById(R.id.image)
            }
        }
        return null
    }

    private fun createView(position: Int, parent: ViewGroup): ViewDataBinding {
        val inflator = LayoutInflater.from(context)
        val binding = if (parentCardType == PostDisplayType.POST_COLLECTION_IMAGE.index || parentCardType == PostDisplayType.SQUARE_CARD_CAROUSEL.index) {
            DataBindingUtil.inflate<SimplePostViralCollectionItemBinding>(inflator,
                    R.layout.simple_post_viral_collection_item, parent, false)
        } else {
            DataBindingUtil.inflate<SimplePostVhCollectionItemBinding>(inflator,
                    R.layout.simple_post_vh_collection_item, parent, false)
        }
        bindView(binding, position)
        val imageView = binding.root.findViewById<ImageView>(R.id.image)
        imageList.add(imageView)
        /*Remove transformation properties for reused views*/
        lifecycleOwner?.let {
            binding.lifecycleOwner = it
        }
        binding.root.translationX = 0f
        return binding
    }

    private fun bindView(viewBinding: ViewDataBinding, position: Int) {
        viewBinding.setVariable(BR.vm, vm)
        viewBinding.setVariable(BR.cardTypeIndex, parentCardType)
        var contentAdDelegate: ContentAdDelegate? = null
        val item = getItemForPosition(position)
        if (item is CommonAsset) {
            viewBinding.setVariable(BR.item, ParentIdHolderCommenAsset(parentItem?.i_id(), item))
            contentAdDelegate = adDelegates[item.i_id()] ?: parentItem?.i_adId()?.let { adId ->
                ContentAdDelegate(uniqueScreenId)
                        .also { adDelegates[item.i_id()] = it }
            }
            contentAdDelegate?.bindAd(parentItem?.i_adId(), item.i_id())
        } else {
            contentAdDelegate?.reset()
            viewBinding.setVariable(BR.item, item)
        }
        viewBinding.setVariable(BR.adDelegate, contentAdDelegate)
        viewBinding.setVariable(BR.parentItem, parentItem)
        viewBinding.root.tag = position
        val rootView = viewBinding.root.findViewById<View>(R.id.root_collection_item)
        rootView.setOnClickListener {
            cardItemClickListener.onItemClicked()
            contentAdDelegate?.onCardClick()
            (vm as? ClickHandlingViewModel)?.onCollectionItemClick(it, item as? CommonAsset, parentItem, adapterPosition)
        }
        viewBinding.executePendingBindings()
    }

    fun onAnimationPause() {
        getImage(currentView)?.let {
            if (isDummyPage(currentPosition)) {
                it.stop()
            } else {
                if (canStartPanZoomAnimation()) {
                    it.pause()
                }
            }
        }
    }

    fun onAnimationResume() {
        getImage(currentView)?.let {
            if (isDummyPage(currentPosition)) {
                it.stop()
            } else {
                if (canStartPanZoomAnimation()) {
                    it.resume()
                }
            }
        }
    }

    private fun canStartPanZoomAnimation(): Boolean {
        return parentItem?.i_carouselProperties()?.animationType == AnimationType.PAN_AND_ZOOM
    }

    private fun getImage(view: View?): NhKenBurnsImageView? {
        return view?.findViewById<NhKenBurnsImageView>(R.id.image)
    }

    override fun setPrimaryItem(container: ViewGroup, position: Int, `object`: Any) {
        super.setPrimaryItem(container, position, `object`)
        getImage(currentView)?.stop()
        currentView = `object` as View
        currentPosition = position
        if (!isDummyPage(position) && canStartPanZoomAnimation()) {
            getImage(currentView)?.resume()
        } else {
            getImage(currentView)?.stop()
        }
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return `object` is View && `object` == view
    }

    private fun isDummyPage(position: Int): Boolean {
        return (position == 0 || position == count - 1) && isCircular
    }

    private fun getItemForDummyPage(position: Int): Any {
        if (position == 0) {
            return items.last()
        } else {
            return items.first()
        }
    }

    private fun getItemForPosition(position: Int): Any {
        if (isDummyPage(position)) {
            return getItemForDummyPage(position)
        }
        if (isCircular) {
            return items[position - 1/*Removing offset for first dummy page*/]
        }
        return items[position]
    }

    private fun mapListItemPositionToAdapterPosition(position: Int): Int {
        if (isCircular) {
            return position + 1
        }
        return position
    }

    fun update(parent: CommonAsset, list: List<Any>, circular: Boolean, lifecycleOwner: LifecycleOwner?,
               adapterPosition: Int) {
        this.isCircular = circular
        this.lifecycleOwner = lifecycleOwner
        this.parentItem = parent
        this.adapterPosition = adapterPosition
        val doSoftUpdate = this.parentItem?.i_id() == parent.i_id() && items.size == list.size
        items.clear()
        items.addAll(list)
        if (doSoftUpdate) {
            softUpdate()
            return
        }
        notifyDataSetChanged()
    }

    private fun softUpdate() {
        items.forEachIndexed { index, asset ->
            val viewBinding = getActivePageViews(mapListItemPositionToAdapterPosition(index))
            if (viewBinding != null) {
                bindView(viewBinding, mapListItemPositionToAdapterPosition(index))
            }
        }
    }

    override fun canCache(position: Int): Boolean {
        if (isDummyPage(position)) {
            return false
        }
        return super.canCache(position)
    }

    fun getCurrentAdDelegate(): ContentAdDelegate? {
        if (adDelegates.isEmpty()) return null

        val post = getItemForPosition(currentPosition) as? CommonAsset ?: return null
        return adDelegates[post.i_id()]
    }

    fun triggerAdBeacon(position: Int) {
        val item = getItemForPosition(position) as? CommonAsset ?: return
        adDelegates[item.i_id()]?.onCardView(adAdapterPosition = adapterPosition)
    }
}


abstract class CachedPagerAdapter(private val strictCaching: Boolean = false) : PagerAdapter() {

    private val mPageViews = SparseArray<ViewDataBinding>()

    private val mPageViewPool = ArrayList<ViewDataBinding>()

    private val activeViewPool = SparseArray<ViewDataBinding>()

    //A viewbinding cache where viewbinding is maintained strictly against a position
    private val strictViewPool = HashMap<Int, ViewDataBinding>()

    protected abstract fun getViewBinding(convert: ViewDataBinding?, position: Int, parent: ViewGroup):
            ViewDataBinding


    override fun getItemPosition(`object`: Any): Int {
        return POSITION_NONE
    }

    override fun isViewFromObject(view: View, obj: Any): Boolean {
        return obj is View && obj == view
    }

    open fun canCache(position: Int): Boolean {
        return true
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val view: ViewDataBinding
        if (canCache(position)) {
            val convertView = if (!strictCaching) {
                pullViewFromPool()
            } else {
                pullViewFromStrictPool(position)
            }
            view = getViewBinding(convertView, position, container)
            mPageViews.put(position, view)
        } else {
            view = getViewBinding(null, position, container)
        }
        activeViewPool.put(position, view)
        view.root.setTag(R.id.page_tag, position)
        container.addView(view.root)
        return view.root
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        val binding: ViewDataBinding? = mPageViews.get(position)
        activeViewPool.put(position, null)
        binding?.let {
            val bindedPosition = it.root.getTag(R.id.page_tag) as Int
            if (bindedPosition != position) {
                Logger.e(LOG_TAG, "did not remove view as already binded to some other positon")
                return
            }
        }
        if (canCache(position) && binding != null) {
            if (!strictCaching) {
                pushViewToPool(binding)
            } else {
                pushViewToStrictPool(binding, position)
            }

        }
        if (binding != null) {
            container.removeView(binding.root)
        } else if (`object` is View) {
            container.removeView(`object`)
        }
    }

    fun getActivePageViews(position: Int): ViewDataBinding? {
        return activeViewPool.get(position)
    }

    private fun pullViewFromPool(): ViewDataBinding? {
        var view: ViewDataBinding? = null
        for (v in mPageViewPool) {
            view = v
            break
        }
        if (view != null) {
            mPageViewPool.remove(view)
        }
        return view
    }

    private fun pushViewToPool(view: ViewDataBinding) {
        if (!mPageViewPool.contains(view)) {
            mPageViewPool.add(view)
        }
    }

    private fun pushViewToStrictPool(view: ViewDataBinding, position: Int) {
        strictViewPool[position] = view
    }

    private fun pullViewFromStrictPool(position: Int): ViewDataBinding? {
        val viewBinding = strictViewPool[position]
        if (viewBinding != null) {
            strictViewPool.remove(position)
        }
        return viewBinding
    }
}


/*abstract class CarouselAdapter : CachedPagerAdapter() {
    abstract fun update(parent: CommonAsset,
                        list: List<Any>,
                        circular: Boolean,
                        collectionProperties: CollectionProperties,
                        lifecycleOwner: LifecycleOwner?)

    abstract fun onAnimationPause()
    abstract fun onAnimationResume()
}*/

private const val EXTRA_VIEWS_COUNT = 2
private const val LOG_TAG = "CollectionViewHolder"

interface CollectionCard {
    fun resumeAnimation()
    fun pauseAnimation()
}

interface CardItemClickListener{
    fun onItemClicked()
}