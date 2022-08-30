/*
* Copyright (c) 2019 Newshunt. All rights reserved.
*/
package com.newshunt.adengine.view.helper

import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.databinding.BindingAdapter
import androidx.lifecycle.MutableLiveData
import com.newshunt.adengine.R
import com.newshunt.adengine.listeners.AdExitListener
import com.newshunt.adengine.listeners.ReportAdsMenuListener
import com.newshunt.adengine.model.entity.BaseAdEntity
import com.newshunt.adengine.model.entity.BaseDisplayAdEntity
import com.newshunt.adengine.model.entity.ExternalSdkAd
import com.newshunt.adengine.model.entity.NativeAdHtml
import com.newshunt.adengine.model.entity.NativeData
import com.newshunt.adengine.model.entity.version.AdPosition
import com.newshunt.adengine.model.entity.version.AdTemplate
import com.newshunt.adengine.model.entity.version.AdUIType
import com.newshunt.adengine.model.entity.version.ShareIconPosition
import com.newshunt.adengine.util.AdConstants
import com.newshunt.adengine.util.AdsShareViewHelper
import com.newshunt.adengine.util.AdsUtil
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.ViewUtils
import com.newshunt.common.helper.font.FontWeight
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.common.view.customview.fontview.NHTextView
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dhutil.helper.preference.AppStatePreference
import com.newshunt.dhutil.helper.theme.ThemeUtils
import com.newshunt.news.util.NewsConstants
import com.newshunt.onboarding.model.entity.datacollection.InstalledAppInfo
import com.newshunt.pref.NewsPreference
import com.newshunt.sdk.network.image.Image


/**
 * @author raunak.yadav
 */
@BindingAdapter("bind:imageUrl")
fun loadImageUrl(view: ImageView, url: String?) {
    if (!CommonUtils.isEmpty(url)) {
        Image.load(url).placeHolder(R.color.empty_image_color).into(view)
    }
}

@BindingAdapter(("bind:sourceIconVisibility"))
fun bindSourceIconVisibility(view: View, isLogoVisible:Boolean) {
    val isVisible = PreferenceManager.getPreference(AppStatePreference.SHOW_SOURCE_LOGO_AT_CARD_LEVEL,Constants.SHOW_SOURCE_LOGO_AT_CARD_LEVEL)
    view.visibility = if (isVisible && isLogoVisible) View.VISIBLE else View.GONE
}

@BindingAdapter(("bind:marginStartTitle"))
fun setMarginStartForTitle(view: View, dimen: Float) {
    val params = view.layoutParams as ConstraintLayout.LayoutParams
    val isSourceVisible = PreferenceManager.getPreference(AppStatePreference.SHOW_SOURCE_LOGO_AT_CARD_LEVEL,Constants.SHOW_SOURCE_LOGO_AT_CARD_LEVEL)
    if(isSourceVisible) {
        params.marginStart = dimen.toInt()
        view.layoutParams = params
    } else {
        params.marginStart = 0
        view.layoutParams = params
    }
}

@BindingAdapter("bind:marginBottomDetail", "bind:storyPageAd", requireAll = false)
fun setMarginBottomForDetail(view: View, dimen: Float, spAd: Boolean = false) {
    val params = view.layoutParams as ConstraintLayout.LayoutParams
    if(spAd) {
        params.bottomMargin = dimen.toInt()
        view.layoutParams = params
    } else {
        params.bottomMargin = 0
        view.layoutParams = params
    }
}

@BindingAdapter(value = ["bind:adText", "bind:adUnbold", "bind:storyPageAd"], requireAll = false)
fun setUpTitle(view: TextView, title: String?, unbold: Boolean = false, spAd: Boolean = false) {
    if (!title.isNullOrBlank()) {
        if (unbold || AdsUtil.makeTitleUnBold(title)) {
            view.setTypeface(view.typeface, Typeface.NORMAL)
        }
    }
    if (spAd) {
        val progress = PreferenceManager.getPreference(NewsPreference.USER_PREF_FONT_PROGRESS, NewsConstants.DEFAULT_PROGRESS_COUNT)
        (view as NHTextView).setCustomFontWeight(FontWeight.NORMAL)
        val fontDiff = progress - NewsConstants.DEFAULT_PROGRESS_COUNT
        val fontSize = NewsConstants.DEFAULT_FONT_SIZE + (fontDiff * 2)
        view.setTextSize(TypedValue.COMPLEX_UNIT_DIP, fontSize.toFloat())
    }
    view.text = title
}

@BindingAdapter("bind:border")
fun setUpBorder(view: View, adEntity: BaseDisplayAdEntity?) {
    AdsUtil.setupAdBorder(adEntity, view, null)
}

/**
 * [DFP custom ads] : Add tags for the views that need click tracking.
 */
@BindingAdapter("bind:adTag")
fun setUpCustomTag(view: View, tag: String) {
    view.setTag(R.id.ad_click_tag_id, tag)
}

@BindingAdapter("bind:brandImageBg")
fun showBrandBg(view: View, adEntity: BaseDisplayAdEntity?) {
    adEntity ?: return
    val color = adEntity.brand?.brandFallbackText?.getThemeBasedBgColor(ThemeUtils.isNightMode())
    color?.let {
        view.setBackgroundColor(Color.parseColor(it))
    }
}

@BindingAdapter("android:layout_marginStart")
fun setMarginStart(view: View, dimen: Float) {
    val params = view.layoutParams as ConstraintLayout.LayoutParams
    params.marginStart = dimen.toInt()
    view.layoutParams = params
}

@BindingAdapter("android:layout_marginEnd")
fun setMarginEnd(view: View, dimen: Float) {
    val params = view.layoutParams as ConstraintLayout.LayoutParams
    params.marginEnd = dimen.toInt()
    view.layoutParams = params
}

@BindingAdapter("android:layout_marginTop")
fun setMarginTop(view: View, dimen: Int) {
    val params = view.layoutParams as ConstraintLayout.LayoutParams
    params.topMargin = dimen
    view.layoutParams = params
}

@BindingAdapter("bind:adReportStyle", "bind:parentView", "bind:container", requireAll = true)
fun applyStyleToAdreport(
    view: NHTextView,
    adEntity: BaseDisplayAdEntity?,
    parentView: ConstraintLayout?,
    borderContainer: View?
) {
    adEntity ?: return
    if(adEntity is ExternalSdkAd) {
        if(adEntity.external?.data?.startsWith(AdConstants.FB_AD) == true)
            return
    }
    val isFullPage = AdsUtil.isFullScreenAd(adEntity)
    val textColor = adEntity.content?.reportText?.getThemeBasedTextColor(ThemeUtils.isNightMode())
    val bgColor = adEntity.content?.reportText?.getThemeBasedBgColor(ThemeUtils.isNightMode())
    textColor?.let {
        view.setTextColor(Color.parseColor(it))
        var drawable: Drawable? = ContextCompat.getDrawable(view.context, R.drawable.ic_report_ads_menu_icon_grey)
        drawable ?: return

        drawable = DrawableCompat.wrap(drawable)
        DrawableCompat.setTint(drawable.mutate(), Color.parseColor(it))
        drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
        view.setCompoundDrawables(null, null, drawable, null)
    }
    val drawableBackground = GradientDrawable()
    drawableBackground.shape = GradientDrawable.RECTANGLE
    drawableBackground.setColor(ViewUtils.getColor(bgColor, Color.BLACK))
    view.background = drawableBackground

    parentView ?: return
    borderContainer ?: return
    val topBottomGap = if (adEntity.adPosition == AdPosition.EXIT_SPLASH) {
        AdBindUtils.getExitSplashTopMargin(adEntity)
    } else CommonUtils.getDimension(R.dimen.ad_report_margin_start_end)
    val sideGap = CommonUtils.getDimension(if (isFullPage) R.dimen.ad_content_top_bottom_margin
    else R.dimen.ad_report_margin_start_end)

    (adEntity.adTagPosition ?: BaseDisplayAdEntity.AdTagPositionType.TOP_OVERLAY_LEFT).let {
        val set = ConstraintSet()
        set.clone(parentView)
        set.clear(view.id, ConstraintSet.END)
        set.clear(view.id, ConstraintSet.TOP)
        set.clear(view.id, ConstraintSet.START)
        set.clear(view.id, ConstraintSet.BOTTOM)
        when(it) {
            BaseDisplayAdEntity.AdTagPositionType.TOP_OVERLAY_LEFT -> {
                if(!isFullPage) {
                    set.connect(
                        borderContainer.id,
                        ConstraintSet.TOP,
                        parentView.id,
                        ConstraintSet.TOP,
                        CommonUtils.getDimension(R.dimen.ad_bottombar_margin_top)
                    )
                }
                set.connect(
                    view.id,
                    ConstraintSet.START,
                    borderContainer.id,
                    ConstraintSet.START,
                    sideGap
                )
                set.connect(
                    view.id,
                    ConstraintSet.TOP,
                    borderContainer.id,
                    ConstraintSet.TOP,
                    topBottomGap
                )
            }
            BaseDisplayAdEntity.AdTagPositionType.TOP_OVERLAY_RIGHT -> {
                if(!isFullPage) {
                    set.connect(
                        borderContainer.id,
                        ConstraintSet.TOP,
                        parentView.id,
                        ConstraintSet.TOP,
                        CommonUtils.getDimension(R.dimen.ad_bottombar_margin_top)
                    )
                }
                set.connect(
                    view.id,
                    ConstraintSet.END,
                    borderContainer.id,
                    ConstraintSet.END,
                    sideGap
                )
                set.connect(
                    view.id,
                    ConstraintSet.TOP,
                    borderContainer.id,
                    ConstraintSet.TOP,
                    topBottomGap
                )
            }
            BaseDisplayAdEntity.AdTagPositionType.BOTTOM_OVERLAY_LEFT -> {
                if(!isFullPage) {
                    set.connect(
                        borderContainer.id,
                        ConstraintSet.TOP,
                        parentView.id,
                        ConstraintSet.TOP,
                        CommonUtils.getDimension(R.dimen.ad_bottombar_margin_top)
                    )
                }
                set.connect(
                    view.id,
                    ConstraintSet.START,
                    borderContainer.id,
                    ConstraintSet.START,
                    sideGap
                )
                set.connect(
                    view.id,
                    ConstraintSet.BOTTOM,
                    borderContainer.id,
                    ConstraintSet.BOTTOM,
                    topBottomGap
                )
            }
            BaseDisplayAdEntity.AdTagPositionType.BOTTOM_OVERLAY_RIGHT -> {
                if(!isFullPage) {
                    set.connect(
                        borderContainer.id,
                        ConstraintSet.TOP,
                        parentView.id,
                        ConstraintSet.TOP,
                        CommonUtils.getDimension(R.dimen.ad_bottombar_margin_top)
                    )
                }
                set.connect(
                    view.id,
                    ConstraintSet.END,
                    borderContainer.id,
                    ConstraintSet.END,
                    sideGap
                )
                set.connect(
                    view.id,
                    ConstraintSet.BOTTOM,
                    borderContainer.id,
                    ConstraintSet.BOTTOM,
                    topBottomGap
                )
            }
            BaseDisplayAdEntity.AdTagPositionType.TOP_RIGHT -> {
                if(!isFullPage) {
                    val params = view.layoutParams as ConstraintLayout.LayoutParams
                    params.marginEnd = 0
                    view.layoutParams = params

                    set.connect(
                        view.id,
                        ConstraintSet.END,
                        parentView.id,
                        ConstraintSet.END,
                        CommonUtils.getDimension(R.dimen.ad_report_margin_top)
                    )
                    set.connect(
                        view.id,
                        ConstraintSet.TOP,
                        parentView.id,
                        ConstraintSet.TOP,
                        0
                    )
                    set.connect(
                        borderContainer.id,
                        ConstraintSet.TOP,
                        view.id,
                        ConstraintSet.BOTTOM,
                        0
                    )
                    view.setBackgroundResource(0)
                }
            }
        }
        set.applyTo(parentView)
    }
}

@BindingAdapter("bind:titleColor")
fun applyTitleColor(view: NHTextView, color: String?) {
    color ?: return
    view.setTextColor(Color.parseColor(color))
}

@BindingAdapter("bind:adEntity", "bind:adsShareViewHelper", "bind:active", "bind:defaultShareChange", requireAll = false)
fun setupShareIcon(view: View, adEntity: BaseDisplayAdEntity?, shareHelper: AdsShareViewHelper?, active: Boolean, defaultShareChange: MutableLiveData<InstalledAppInfo>?) {
    view.visibility = if (active && shareHelper != null) View.VISIBLE else View.GONE
    shareHelper ?: return

    shareHelper.setAdEntity(adEntity)
    if (active) {
        adEntity?.shareability?.let {
            if (it.image.isNullOrBlank() && it.text.isNullOrBlank()) {
                return
            }
            if (view is ImageView) {
                view.setImageDrawable(shareHelper.getSharableAppIcon())
            }
        }
    }
}

@BindingAdapter("bind:drawableStart", "bind:drawableEnd", "bind:drawableTop", "bind:drawableBottom",
    requireAll = false)
fun setCompoudDrawable(view: TextView, start: Int? = null, end: Int? = null, top: Int? = null,
                       bottom: Int? = null) {
    val drwStart = if (start != null && start > 0)
        CommonUtils.getDrawable(start) else null
    val drwEnd = if (end != null && end > 0)
        CommonUtils.getDrawable(end) else null
    val drwTop = if (top != null && top > 0)
        CommonUtils.getDrawable(top) else null
    val drwBottom = if (bottom != null && bottom > 0)
        CommonUtils.getDrawable(bottom) else null
    view.setCompoundDrawablesWithIntrinsicBounds(drwStart, drwTop, drwEnd, drwBottom)
}

@BindingAdapter("bind:shareDrawableStart", "bind:shareDrawableEnd", "bind:shareDrawableTop", "bind:shareDrawableBottom",requireAll = false)
fun setAdShareIcon(view:TextView, start: Drawable? = null, end: Drawable? = null, top: Drawable? = null,
                   bottom: Drawable? = null){
    view.setCompoundDrawablesWithIntrinsicBounds(start, top, end, bottom)
}

class AdBindUtils {
    companion object {
        @JvmStatic
        fun getTitle(item: NativeData?): String {
            return AdsUtil.getMergedTitle(item?.title, item?.body)
        }

        @JvmStatic
        fun canShowTopDivider(item: NativeData?, adEntity: BaseAdEntity?): Boolean {
            item ?: return false
            if (adEntity !is BaseDisplayAdEntity || adEntity.showOnlyImage == true) {
                return false
            }
            return when (adEntity.adPosition) {
                AdPosition.P0,
                AdPosition.PP1,
                AdPosition.CARD_P1,
                AdPosition.SUPPLEMENT -> false
                else -> !CommonUtils.isEmpty(AdsUtil.getMergedTitle(item.title, item.body))
            }
        }

        @JvmStatic
        fun canIMAShowTitle(item: NativeData?, adEntity: ExternalSdkAd?): Boolean {
            item ?: return false
            adEntity ?: return false
            return adEntity.showTitle == true && !item.title.isNullOrBlank() &&
                    canShowNonMediaView(adEntity)
        }

        @JvmStatic
        fun canShowBottomBanner(baseAdEntity: BaseDisplayAdEntity?,
                                cta: String?, sponsored: String?): Boolean {
            baseAdEntity ?: return false
            if (baseAdEntity.showOnlyImage == true) {
                return false
            }
            if(baseAdEntity.brand != null) {
                return false
            }
            return !(cta.isNullOrBlank() ||
                    (baseAdEntity !is ExternalSdkAd && CommonUtils.isEmpty(baseAdEntity.action))) ||
                    !sponsored.isNullOrBlank() ||
                    baseAdEntity.shareability != null
        }

        @JvmStatic
        fun canShowOSVBottomBanner(adEntity: BaseDisplayAdEntity?): Boolean {
            adEntity ?: return false
            return canShowNonMediaView(adEntity) && !isEnhancedAd(adEntity)
                    && canShowBrandBottomBar(adEntity)
        }

        @JvmStatic
        fun canShowNonMediaView(adEntity: BaseDisplayAdEntity?): Boolean {
            return adEntity?.showOnlyImage != true
        }

        @JvmStatic
        fun canShow(text: String?): Boolean {
            return !text.isNullOrBlank()
        }

        @JvmStatic
        fun getCounts(count: String?): String {
            return if (count.isNullOrBlank() || count.equals("0")) Constants.EMPTY_STRING else count
        }

        @JvmStatic
        fun isEnhancedAd(adEntity: BaseDisplayAdEntity?): Boolean {
            return adEntity?.adTemplate == AdTemplate.ENHANCED_HIGH
        }

        @JvmStatic
        fun isShareSupported(adEntity: BaseDisplayAdEntity?): Boolean {
            return adEntity?.shareability?.let {
                !(it.image.isNullOrBlank() && it.text.isNullOrBlank())
            } ?: false
        }

        @JvmStatic
        fun canShowPGIShareIconTop(adEntity: BaseDisplayAdEntity?, ctaText:String?): Boolean {
            return ctaText.isNullOrBlank() && adEntity?.shareability?.shareIconPosition == ShareIconPosition.FIXED
        }

        @JvmStatic
        fun canShowShareLayout(text: String?, adEntity: BaseDisplayAdEntity?) : Boolean{
            return text.isNullOrBlank() && isShareSupported(adEntity)
        }

        @JvmStatic
        fun canShowShareIcon(text: String?, adEntity: BaseDisplayAdEntity?) : Boolean {
            return !text.isNullOrBlank() && isShareSupported(adEntity)
        }

        @JvmStatic
        fun hideBrandingBar(adEntity: BaseDisplayAdEntity?, item: NativeData?, category: String?): Boolean {
            return adEntity?.showOnlyImage == true || (item?.iconUrl.isNullOrBlank() && category.isNullOrBlank())
        }

        @JvmStatic
        fun getCoolAdTitle(adEntity: BaseDisplayAdEntity?): String {
            return (adEntity as? NativeAdHtml)?.coolAd?.title ?: Constants.EMPTY_STRING
        }

        @JvmStatic
        fun canShowTapToEngText(adEntity: BaseDisplayAdEntity?): Boolean {
            return !(adEntity as? NativeAdHtml)?.coolAd?.tapToEng.isNullOrBlank()
        }

        @JvmStatic
        fun getTapToEngText(adEntity: BaseDisplayAdEntity?): String {
            return (adEntity as? NativeAdHtml)?.coolAd?.tapToEng ?: Constants.EMPTY_STRING
        }

        @JvmStatic
        fun canShowAdsReportIcon(adEntity: BaseDisplayAdEntity?): Boolean {
            return !adEntity?.reportAdsMenuFeedBackEntity?.feedbackUrl.isNullOrEmpty()
        }

        @JvmStatic
        fun reportAd(adReportListener: ReportAdsMenuListener?, adEntity: BaseAdEntity,
                     adParentIdIfCarousel: String?) {
            reportAd(adReportListener, adEntity, adParentIdIfCarousel, null)
        }

        @JvmStatic
        fun reportAd(adReportListener: ReportAdsMenuListener?, adEntity: BaseAdEntity,
                     adParentIdIfCarousel: String?, adExitListener: AdExitListener?) {
            adExitListener?.cancelExitApp()
            adReportListener?.onReportAdsMenuClick(adEntity, adParentIdIfCarousel
                ?: Constants.EMPTY_STRING)
        }

        @JvmStatic
        fun canShowBrandBottomBar(adEntity: BaseDisplayAdEntity?): Boolean {
            return adEntity?.brand != null
        }

        @JvmStatic
        fun canShowFallbackText(adEntity: BaseDisplayAdEntity?): Boolean {
            return adEntity?.brand?.brandFallbackText?.data.isNullOrEmpty().not()
        }

        @JvmStatic
        fun getExitSplashTopMargin(adEntity: BaseDisplayAdEntity?): Int {
            return CommonUtils.getDimension(
                if (adEntity?.displayType == AdUIType.MINI_SCREEN)
                    R.dimen.ad_content_top_bottom_margin
                else R.dimen.ad_full_screen_top_icon_margin
            )
        }
    }
}
