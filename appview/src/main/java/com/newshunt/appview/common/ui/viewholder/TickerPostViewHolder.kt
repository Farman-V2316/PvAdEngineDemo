/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.appview.common.ui.viewholder

import android.app.Activity
import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.util.Pair
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.widget.ImageView
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.newshunt.analytics.client.AnalyticsClient
import com.newshunt.appview.BR
import com.newshunt.appview.R
import com.newshunt.appview.common.viewmodel.CardsViewModel
import com.newshunt.appview.databinding.TickerImageItemBinding
import com.newshunt.appview.databinding.TickerWebItemBinding
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.JsonUtils
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.common.NHWebViewUtils
import com.newshunt.common.helper.common.ViewUtils
import com.newshunt.common.helper.font.HtmlFontHelper
import com.newshunt.common.view.customview.NHRoundedCornerImageView
import com.newshunt.common.view.customview.RoundedCornersWebView
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.analytics.section.NhAnalyticsEventSection
import com.newshunt.dataentity.common.asset.CommonAsset
import com.newshunt.dataentity.common.asset.SubFormat
import com.newshunt.dataentity.common.asset.Ticker2
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.common.view.customview.FIT_TYPE
import com.newshunt.dataentity.viral.model.entity.server.BackgroundOption
import com.newshunt.deeplink.navigator.CommonNavigator
import com.newshunt.dhutil.commons.listener.ReferrerProviderlistener
import com.newshunt.dhutil.helper.TickerHelper3
import com.newshunt.dhutil.helper.theme.ThemeUtils
import com.newshunt.helper.ImageUrlReplacer
import com.newshunt.news.analytics.NewsAnalyticsHelper
import com.newshunt.news.analytics.NhAnalyticsNewsEvent
import com.newshunt.news.helper.DefaultNavigatorCallback
import com.newshunt.news.helper.NHWebViewJSInterface
import com.newshunt.news.util.NewsConstants
import com.newshunt.news.view.customview.TickerViewPager
import com.newshunt.news.view.listener.TickerViewCircularPageChangeListener
import com.newshunt.sdk.network.image.Image
import com.newshunt.viral.utils.visibility_utils.VisibilityAwareViewHolder
import java.util.HashMap

/**
 * @author madhuri.pa
 */
class TickerPostViewHolder(private val viewBinding: ViewDataBinding,
                           val vm: CardsViewModel,
                           val cardType: Int, val context: Context?, private val tickerHelper3:
                           TickerHelper3?,
                           val pageReferrer: PageReferrer?, val referrerProviderlistener:
                           ReferrerProviderlistener?, private val section: String)
    : CardsViewHolder(viewBinding.root), ViewPager.OnPageChangeListener, VisibilityAwareViewHolder {

    private lateinit var viewPagerTicker: TickerViewPager
    private var circularPageChangeListener: TickerViewCircularPageChangeListener? = null
    private var tickerPagerAdapter: TickerAdapter? = null
    private var tickers: List<Ticker2>? = null
    private var currentPage = -1
    private var isVisible: Boolean = false
    private lateinit var lifecycleOwner: LifecycleOwner
    private var parentAsset: CommonAsset? = null

    override fun bind(item: Any?, lifecycleOwner: LifecycleOwner?, cardPosition:Int) {
        if (item !is CommonAsset)
            return
        tickers = item.i_ticker()
        parentAsset = item
        viewBinding.setVariable(BR.item, item)
        viewBinding.setVariable(BR.vm, vm)
        if (lifecycleOwner != null) {
            viewBinding.lifecycleOwner = lifecycleOwner
            this.lifecycleOwner = lifecycleOwner
        }
        updateTicker(tickers)
    }

    private fun updateTicker(tickers: List<Ticker2>?) {
        if (tickers == null) {
            return
        }

        if (tickers.size == 1) {
            currentPage = 0
        }
        var tickerHeight = CommonUtils.getDimension(R.dimen.ticker_default_height)
        viewPagerTicker = viewBinding.root.findViewById(R.id.viewpager_ticker)
        viewPagerTicker.addOnPageChangeListener(this)
        val tickerMaxHeight = tickers.maxByOrNull { it.tickerHeight ?: 0 }?.tickerHeight
        if (tickerMaxHeight ?: -1 >= 0) {
            val verticalPadding = CommonUtils.getDimension(R.dimen.story_card_padding_left)
            tickerHeight = CommonUtils.getPixelFromDP(tickerMaxHeight ?: -1, context) + 2 *
                    verticalPadding
        }
        viewPagerTicker.layoutParams.height = tickerHeight

        viewPagerTicker.tickerNodeCount = tickers.size
        //tickerNode?.swipeInterval?.let { viewPagerTicker.setTickerSwipeInterval(it) }
        if (tickerPagerAdapter == null) {
            tickerPagerAdapter = pageReferrer?.let {
                TickerAdapter(vm, cardType, context, tickers, viewPagerTicker,
                        tickerHeight, it, section)
            }
            viewPagerTicker.adapter = tickerPagerAdapter
            viewPagerTicker.pageMargin = 10
            circularPageChangeListener = TickerViewCircularPageChangeListener(viewPagerTicker)
        } else {
            if (pageReferrer != null) {
                tickerPagerAdapter?.update(tickers, lifecycleOwner, pageReferrer)
            }
        }

        //Add the circular page change listener only for tickers > 1 case
        if (tickers.size > 1) {
            circularPageChangeListener?.let { viewPagerTicker.addOnPageChangeListener(it) }
            //If this ticker viewholder is already visible, start auto slide also
            if (isVisible) {
                tickerPagerAdapter?.count?.let { viewPagerTicker.scheduleSlider(it) }
            }
        } else {
            circularPageChangeListener?.let { viewPagerTicker.removeOnPageChangeListener(it) }
        }
    }

    override fun onPageScrollStateChanged(state: Int) {

    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

    }

    override fun onPageSelected(position: Int) {
        if (tickers == null) {
            return
        }
        val tickerPosition = position % (tickers?.size ?: 0)

        val previousTicker = if (currentPage >= 0 && currentPage < tickers?.size ?: 0) {
            tickers?.get(currentPage)
        } else
            null
        val newTicker = tickers?.get(tickerPosition)
        notifyTickerVisibility(previousTicker, currentPage, false)
        notifyTickerVisibility(newTicker, tickerPosition, true)
        currentPage = tickerPosition
    }

    override fun onVisible(viewVisibilityPercentage: Int, percentageOfScreen: Float) {
        if (!isVisible) {
            notifyTickerVisibility(true)
            isVisible = true
        }
        AnalyticsClient._logDynamic(NhAnalyticsNewsEvent.TICKER_VIEW, NewsAnalyticsHelper
                .getReferrerEventSectionFrom(referrerProviderlistener), null, tickers?.get(currentPage)?.i_experiments(), null,
                pageReferrer,
                false)

        tickerHelper3?.reschedule(false)
    }

    override fun onInVisible() {
        if (isVisible) {
            notifyTickerVisibility(false)
            isVisible = false
        }
        tickerHelper3?.reschedule(true)
    }

    private fun notifyTickerVisibility(isResumed: Boolean) {
        if (tickers == null) {
            return
        }
        val tickers = tickers
        if (tickers != null) {
            for (i in tickers.indices) {
                notifyTickerVisibility(tickers[i], i, isResumed && currentPage == i)
            }
        }
    }

    private fun notifyTickerVisibility(ticker: Ticker2?, position: Int,
                                       visible: Boolean) {
        if (ticker != null && ticker.i_subFormat() == SubFormat.HTML) {
            val viewHolder = tickerPagerAdapter?.getViewHolderForPosition(position)
            val webview = viewHolder?.view?.rootView?.findViewById<RoundedCornersWebView>(R.id.ticker_content_1)
            if (webview != null) {
                if (visible) {
                    Logger.d(TAG, "Position: " + position + " onResume, tickerId: " + ticker.id)
                    webview.webviewResume()
                } else {
                    Logger.d(TAG, "Position: " + position + " onPause, tickerId: " + ticker.id)
                    webview.webviewPaused()
                }
            }
        }
    }


    override fun onUserLeftFragment() {
        Logger.d(TAG, "onUserLeftFragment")
        notifyTickerVisibility(false)
        viewPagerTicker.cancelAutoSlide()
    }

    override fun onUserEnteredFragment(viewVisibilityPercentage: Int, percentageOfScreen: Float) {
        Logger.d(TAG, "onUserEnteredFragment")
        //If ticker was visible on the screen and user entered fragment, notify the webviews and viewpager
        if (isVisible && tickers != null && tickerPagerAdapter != null) {
            notifyTickerVisibility(true)
            tickerPagerAdapter!!.count.let { viewPagerTicker.scheduleSlider(it) }
        }
    }

}

class TickerAdapter(private val vm: ViewModel,
                    private val parentCardType: Int,
                    private val context: Context?,
                    private var tickers: List<Ticker2>?,
                    val viewPager: ViewPager, val tickerHeight: Int?, val pageReferrer:
                    PageReferrer, private val section: String) : PagerAdapter() {

    private var ticker: Ticker2? = null
    private var lifecycleOwner: LifecycleOwner? = null
    private var viewHolderMap: SparseArray<TickerPagerViewHolder> = SparseArray()
    private val recycledTickers: MutableMap<String?, Pair<Ticker2, TickerPagerViewHolder>> =
            HashMap()


    override fun isViewFromObject(view: View, obj: Any): Boolean {
        return view === (obj as TickerPagerViewHolder).view
    }

    override fun getCount(): Int {
        if (tickers == null) {
            return 0
        }

        return if (tickers?.size ?: 0 > 1) Constants.TICKER_MAX_SIZE else 1
    }

    fun getViewBinding(convert: ViewDataBinding?, position: Int, parent: ViewGroup): ViewDataBinding {
        return createView(position, parent)
    }

    private fun createView(position: Int, parent: ViewGroup): ViewDataBinding {
        val inflator = LayoutInflater.from(context)
        val binding: ViewDataBinding
        if (ticker?.i_subFormat() == SubFormat.IMAGE) {
            binding = DataBindingUtil.inflate<TickerImageItemBinding>(inflator, R.layout
                    .ticker_image_item,
                    parent, false)
        } else {
            binding = DataBindingUtil.inflate<TickerWebItemBinding>(inflator, R.layout
                    .ticker_web_item,
                    parent, false)
        }
        bindView(binding, position)
        lifecycleOwner?.let {
            binding.lifecycleOwner = it
        }
        return binding
    }

    private fun bindView(binding: ViewDataBinding?, position: Int) {
        binding?.setVariable(BR.item, ticker)
        binding?.setVariable(BR.vm, vm)
    }

    private fun isTickerVisible(tickerId: String): Boolean {
        if (tickers == null || CommonUtils.isEmpty(tickerId)) {
            return false
        }
        val selectedItem = viewPager.currentItem
        val selectedTickerPosition = selectedItem % (tickers?.size ?: 0)
        val selectedTicker = tickers?.get(selectedTickerPosition)
        return selectedTicker != null && tickerId == selectedTicker.id
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val tickerSize = tickers?.size
        val tickerPosition = tickerSize?.let { position.rem(it) } //circular repeating stream of tickers.
        ticker = tickerPosition?.let { tickers?.get(it) }

        val tickerPair = recycledTickers.get(ticker?.id)
        var tickerPagerViewHolder: TickerPagerViewHolder? = null
        if (tickerPair?.first != null &&
                CommonUtils.equals(ticker?.isTickerTypeImage(), tickerPair.first
                        .isTickerTypeImage())) {
            tickerPagerViewHolder = tickerPair.second
        }
        //If no views for this ticker position are cached, inflate a new view
        if (tickerPagerViewHolder == null) {

            if (ticker?.i_subFormat() == SubFormat.IMAGE) {
                tickerPagerViewHolder = TickerPagerViewHolder()
                tickerPagerViewHolder.view = getViewBinding(null, position, container).root
            } else {
                tickerPagerViewHolder = TickerPagerViewHolder()
                tickerPagerViewHolder.view = getViewBinding(null, position, container).root
                val tickerContent = tickerPagerViewHolder.view!!.findViewById<RoundedCornersWebView>(R.id.ticker_content_1)
                if (tickerContent != null) {
                    NHWebViewUtils.initializeWebView(tickerContent)
                }
                tickerContent?.setBackgroundColor(ThemeUtils.getBackgroundColor(context as?
                        Activity))

                tickerContent?.addJavascriptInterface(TickerJsInterface(tickerContent, context as
                        Activity),
                        NHWebViewJSInterface.INTERFACE_NAME)
                tickerPagerViewHolder.tickerContentWebView = tickerContent
            }
        }
            //If we reused a view here, make sure we remove it from the map of recycled views as well.
            if (ticker != null) {
                recycledTickers.remove(ticker?.id)
            }


        container.addView(tickerPagerViewHolder.view)

        tickerPagerViewHolder.ticker = ticker
        if (ticker?.i_subFormat() == SubFormat.IMAGE) {
                val imageView = tickerPagerViewHolder.view?.findViewById<NHRoundedCornerImageView>(R.id.ticker_image)
                val width = CommonUtils.getDeviceScreenWidthInDp() - 2 * CommonUtils.getDimensionInDp(R.dimen
                        .story_card_padding_left)
            val height = tickerHeight?.let { CommonUtils.getDpFromPixels(it, container.context) }
            val imageUrl = height?.let { ImageUrlReplacer.getQualifiedImageUrl(ticker?.imageUrl, width, it) }
            Image.load(imageUrl).placeHolder(R.color.logout_color).into(imageView, ImageView.ScaleType
                    .MATRIX)
                if (imageView != null) {
                    imageView.setFitType(FIT_TYPE.TOP_CROP)
                }
            val deeplinkUrl = ticker?.i_deeplinkUrl()
            imageView?.setOnClickListener {
                if (!CommonUtils.isEmpty(deeplinkUrl))
                    CommonNavigator.launchInternalDeeplink(it.context,
                            deeplinkUrl,
                            pageReferrer,
                            true,
                            DefaultNavigatorCallback(), section)
            }
            } else {
            initWebView(tickerPagerViewHolder.view, ticker, tickerPosition)
        }
        if (tickerPosition != null) {
            viewHolderMap.put(tickerPosition, tickerPagerViewHolder)
        }
        return tickerPagerViewHolder
    }

    override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
        val holder = obj as TickerPagerViewHolder
        container.removeView(holder.view)
        recycledTickers[holder.ticker?.id] = Pair<Ticker2, TickerPagerViewHolder>(holder.ticker, holder)
    }

    fun getViewHolderForPosition(position: Int): TickerPagerViewHolder? {
        return viewHolderMap.get(position)
    }

    override fun getPageWidth(position: Int): Float {
        return 1.0f
    }

    private fun initWebView(tickerview: View?,
                            ticker: Ticker2?,
                            tickerPosition: Int?) {

        if (ticker == null)
            return
        /*
    Ticker with Hardware acceleration ON flickers and causes jerk while scrolling through headlines,
    But without hardware acceleration, videos dont work on Webview. Hence, controlling layer type
    via B.E flag. Here, double checking the current isHardwareAccelerated() to make sure we reset
    the HW acceleration even when views are recycled.
  */
        val webView = tickerview?.findViewById<RoundedCornersWebView>(R.id.ticker_content_1)
        val sidePadding = CommonUtils.getDimension(R.dimen.story_card_padding_left)
        tickerview?.setPadding(sidePadding, sidePadding, sidePadding, sidePadding)
        val backgroundOption = ticker.backgroundOption
        if (backgroundOption != null && BackgroundOption.BackgroundType.BG_COLOR.equals
                (backgroundOption.type.toString(), ignoreCase = true)) {
            val bgColor = ViewUtils.getColor(backgroundOption.bgColor)
            if (bgColor != null) {
                tickerview?.setBackgroundColor(bgColor)
            }
            val borderColor = ViewUtils.getColor(backgroundOption.borderColor)
            if (borderColor != null) {
                val borderWidth = CommonUtils.getDimension(R.dimen.ticker_border_width)
                val tickerParent = tickerview?.findViewById<View>(R.id.ticker_webview_parent)
                val drawable = GradientDrawable()
                drawable.shape = GradientDrawable.RECTANGLE
                drawable.cornerRadius = CommonUtils.getDimension(R.dimen.ticker_corner_radius).toFloat()
                drawable.setStroke(borderWidth, borderColor)
                if (tickerParent != null) {
                    tickerParent.background = drawable
                }
            }
        }

        if (ticker.content != null) {
            val html = HtmlFontHelper.wrapDataWithHTML(ticker.content)
            webView?.loadDataWithBaseURL(
                "", html, "text/html; " +
                        "charset=utf-8", NewsConstants.HTML_UTF_ENCODING, null
            )
        }
    }

    fun update(tickerNode: List<Ticker2>?, lifecycleOwner: LifecycleOwner?, pageReferrer:
    PageReferrer) {
        this.tickers = tickerNode
        this.lifecycleOwner = lifecycleOwner
        recycledTickers.clear()
        viewHolderMap.clear()
        notifyDataSetChanged()
    }


    override fun getItemPosition(`object`: Any): Int {
        return POSITION_NONE
    }


    inner class TickerPagerViewHolder {
        var view: View? = null
        var tickerContentWebView: WebView? = null
        var ticker: Ticker2? = null
    }

    inner class TickerJsInterface(webView: WebView, parentActivity: Activity) : NHWebViewJSInterface
    (webView, parentActivity, pageReferrer) {

        @JavascriptInterface
        override fun handleAction(url: String) {
            super.handleAction(url)
        }

        @JavascriptInterface
        fun isTickerVisible(tickerId: String): Boolean {
            return this@TickerAdapter.isTickerVisible(tickerId)
        }

        @JavascriptInterface
        fun logTickerClickEvent(jsonParams: String) {
            var params: HashMap<String, String>? = HashMap()
            params = JsonUtils.fromJson(jsonParams, params!!.javaClass)
            params.putAll(ticker?.experiment?: mapOf())
            AnalyticsClient.logDynamic(NhAnalyticsNewsEvent.TICKER_CLICK, NhAnalyticsEventSection.NEWS,
                    null, params, pageReferrer, false)

        }
    }
}
private const val TAG = "TickerPostViewHolder"