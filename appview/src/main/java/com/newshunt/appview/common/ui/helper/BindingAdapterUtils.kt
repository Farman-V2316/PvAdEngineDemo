/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.appview.common.ui.helper

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ClipDrawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.text.Html
import android.text.Spannable
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.style.ForegroundColorSpan
import android.text.style.URLSpan
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.TextView.BufferType
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.text.HtmlCompat
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import androidx.databinding.BindingAdapter
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.gson.reflect.TypeToken
import com.newshunt.appview.BR
import com.newshunt.appview.R
import com.newshunt.appview.common.postcreation.view.helper.PostConstants
import com.newshunt.appview.common.profile.helper.timeFormat


import com.newshunt.appview.common.ui.viewholder.PerspectiveState
import com.newshunt.appview.common.viewmodel.CardsViewModel
import com.newshunt.appview.common.viewmodel.ClickHandlingViewModel
import com.newshunt.appview.common.viewmodel.ViewAllCommentsViewModel
import com.newshunt.common.helper.common.AndroidUtils
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.DataUtil
import com.newshunt.common.helper.common.JsonUtils
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.common.ViewUtils
import com.newshunt.common.helper.font.FontHelper
import com.newshunt.common.helper.font.HtmlFontHelper
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.common.view.customview.HeightAwareWebView
import com.newshunt.common.view.customview.InternalUrlSpan
import com.newshunt.common.view.customview.NHImageView
import com.newshunt.common.view.customview.NHRoundedCornerImageView
import com.newshunt.common.view.customview.fontview.CapTextView
import com.newshunt.common.view.customview.fontview.NHTextView
import com.newshunt.common.view.customview.internalLinkTouchListener
import com.newshunt.common.view.customview.kenburns.NhKenBurnsImageView
import com.newshunt.dataentity.common.asset.ApprovalCounts
import com.newshunt.dataentity.common.asset.AssetType2
import com.newshunt.dataentity.common.asset.BackgroundOption2
import com.newshunt.dataentity.common.asset.BackgroundType2
import com.newshunt.dataentity.common.asset.BaseDetailList
import com.newshunt.dataentity.common.asset.CardLabel2
import com.newshunt.dataentity.common.asset.ColdStartEntityType
import com.newshunt.dataentity.common.asset.CommonAsset
import com.newshunt.dataentity.common.asset.CreatePostUiMode
import com.newshunt.dataentity.common.asset.DetailListCard
import com.newshunt.dataentity.common.asset.EntityItem
import com.newshunt.dataentity.common.asset.Format
import com.newshunt.dataentity.common.asset.LinkAsset
import com.newshunt.dataentity.common.asset.PostDisplayType
import com.newshunt.dataentity.common.asset.PostSourceAsset
import com.newshunt.dataentity.common.asset.RepostDisplayType
import com.newshunt.dataentity.common.asset.SubFormat
import com.newshunt.dataentity.common.asset.UiType2
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.common.model.entity.server.asset.NewsAppJS
import com.newshunt.dataentity.common.pages.*
import com.newshunt.dataentity.common.view.customview.FIT_TYPE
import com.newshunt.dataentity.dhutil.model.entity.upgrade.IconsList
import com.newshunt.dataentity.model.entity.SettingState
import com.newshunt.dataentity.news.model.entity.DetailCardType
import com.newshunt.dataentity.social.entity.CreatePostEntity
import com.newshunt.dataentity.social.entity.MenuOption
import com.newshunt.dataentity.social.entity.PostUploadStatus
import com.newshunt.dhutil.BlurTransformation
import com.newshunt.dhutil.UiUtils
import com.newshunt.dhutil.analytics.AnalyticsHelper
import com.newshunt.dhutil.helper.BoldStyleHelper
import com.newshunt.dhutil.helper.preference.AppStatePreference
import com.newshunt.dhutil.helper.preference.UserPreferenceUtil
import com.newshunt.dhutil.helper.retrofit.NewsBaseUrlContainer
import com.newshunt.dhutil.helper.theme.ThemeUtils
import com.newshunt.dhutil.view.EntityImageUtils
import com.newshunt.dhutil.view.customview.ExpandableRecyclingTextView
import com.newshunt.dhutil.view.customview.ExpandableTextView
import com.newshunt.dhutil.view.listener.TextDescriptionSizeChangeListener
import com.newshunt.helper.ImageUrlReplacer
import com.newshunt.news.helper.LikeEmojiBindingUtils
import com.newshunt.news.helper.NewsDetailTimespentHelper
import com.newshunt.news.helper.NewsDetailUtil
import com.newshunt.news.util.NewsConstants
import com.newshunt.news.view.adapter.CommentsRepostFeedCardAdapter
import com.newshunt.news.view.adapter.PerspectiveCarouselCardsAdapter
import com.newshunt.news.view.fragment.DetailsBindUtils
import com.newshunt.onboarding.model.entity.datacollection.InstalledAppInfo
import com.newshunt.sdk.network.image.Image
import com.newshunt.sso.SSO
import java.io.File
import kotlin.math.roundToInt

//NOTE : DO NOT CHANGE BINDING ADAPTER FUNCTION FOR NO SPECIFIC CASE

fun loadThumbnail(view: NHImageView,
                  item: CommonAsset?,
                  cardType: Int,
                  index: Int,
                  placeholderId: Int) {
    var url = item?.i_thumbnailUrls()?.getOrNull(index)
    if (item?.i_linkAsset() != null) {
        url = item.i_linkAsset()?.thumbnailUrl
    }
    //To remove old image in case of re-used image view
    view.setImageDrawable(null)
    if (CommonUtils.isEmpty(url)) {
        view.setImageResource(placeholderId)
        return
    }
    val imgIndex = index
    view.setFitType(FIT_TYPE.TOP_CROP)
    val imagaeDimension: Pair<Int, Int>? = CardsBindUtils.getImageDimension(cardType, imgIndex)
    imagaeDimension?.let {
        loadImageUrl(view, url!!, it.first, it.second)
        view.visibility = View.VISIBLE
        return
    }
}

@BindingAdapter("bind:thumbnail", "bind:cardtype", "bind:index", requireAll = true)
fun loadThumbnail(view: NHImageView,
                  item: CommonAsset?,
                  cardType: Int,
                  index: Int) {
    loadThumbnail(view, item, cardType, index, R.drawable.default_stry_detail_img)
}

@BindingAdapter("bind:contentImage", "bind:cardtype", "bind:index", requireAll = true)
fun loadContentImage(view: NHImageView, item: CommonAsset?, cardType: Int, index: Int) {
    item?:return
    val url = item.i_contentImageInfo()?.url ?: item.i_thumbnailUrls()?.getOrNull(0)
    view.setFitType(FIT_TYPE.TOP_CROP)
    if (CommonUtils.isEmpty(url)) {
        if (item.i_format() == Format.VIDEO) {
            view.visibility = View.VISIBLE
        } else {
            view.visibility = View.GONE
        }
        return
    }
    val imgIndex = index
    val imagaeDimension: Pair<Int, Int>? = CardsBindUtils.getImageDimension(cardType, imgIndex)
    imagaeDimension?.let {
        loadImageUrl(view, url!!, it.first, it.second)
        view.visibility = View.VISIBLE
        return
    }
    view.visibility = View.GONE
}

@BindingAdapter("bind:contentImageDynamic", "bind:availableHeight", "bind:availableWidth",
        requireAll =
        true)
fun loadDynamicHeroContentImage(view: NHImageView, item: CommonAsset?, availableHeight: Int?, availableWidth: Int?) {
    item?:run{
        view.visibility=View.GONE
        return
    }
    val imageDetail = (item.i_contentImageInfo() ?: item.i_thumbnailUrlDetails()?.getOrNull(0))
    if (CommonUtils.isEmpty(imageDetail?.url)) {
        view.visibility = View.GONE
        return
    }
    if(imageDetail!!.height==0f || imageDetail.width==0f){
        AnalyticsHelper.logDevErrorEvent("Invalid dimension of image : item id : ${item.i_id()} " +
                "iw : ${imageDetail.width} ih : ${imageDetail.height}")
        view.visibility = View.GONE
        return
    }
    view.visibility=View.VISIBLE
    view.setFitType(FIT_TYPE.FIT_CENTER)
    val ah = availableHeight ?: CommonUtils.getDeviceScreenHeight()
    val aw = availableWidth ?: CommonUtils.getDeviceScreenWidth()
    val iRatio = imageDetail.width / imageDetail.height
    val maxAvailableHeight = (item.i_maxImageViewportHeightPercentage().toFloat() / 100f) * ah
    val aRatio = aw.toFloat() / maxAvailableHeight
    val widthToSet = aw.toFloat()
    val heightToSet = Math.min(maxAvailableHeight, widthToSet / iRatio)

    view.maxHeight = heightToSet.roundToInt()
    view.layoutParams.height = heightToSet.roundToInt()
    Logger.i("DynamicHeightImage", "Item id : ${item.i_id()} Available width :$aw Available "
            + "height :$ah " + "IRatio :$iRatio" + " " +
            "MaxHeightWithPercentage : $maxAvailableHeight aRatio : $aRatio heightToSet " + ":$heightToSet " +
            " widthToSet : $widthToSet " +
            "iw : ${imageDetail.width} ih : ${imageDetail.height}")
    val imageDimension: Pair<Int, Int>? = CardsBindUtils.getImageDimension(PostDisplayType.SIMPLE_POST_DYNAMIC_HERO.index, 0)
    if(imageDimension!=null) {
        loadImageUrl(view, imageDetail.url, imageDimension.first, imageDimension.second)
    }else{
        loadImageUrl(view, imageDetail.url, widthToSet.toInt(), heightToSet.toInt())
    }
}


@BindingAdapter("bind:collection_thumbnail", "bind:aspectRatio", requireAll = true)
fun loadCollectionThumbnail(view: NHImageView, item: CommonAsset?, aspectRatio: Float?) {
    item?:return
    var width = 0
    var height = 0
    aspectRatio?.let {
        width = CommonUtils.getDeviceScreenWidth() - ((CommonUtils.getDeviceScreenWidth())*(CardsBindUtils.getSecondItemVisiblePercentage()/100f)).toInt() - 2*(CommonUtils.getDimension(R.dimen.collection_margin))
        height =  (width / it).toInt()
        view.layoutParams.width = width
        view.layoutParams.height = height
    }
    val url = item.i_contentImageInfo()?.url ?: item.i_thumbnailUrls()?.getOrNull(0)
    view.setFitType(FIT_TYPE.TOP_CROP)
    if (CommonUtils.isEmpty(url)) {
        return
    }
    if (view is NhKenBurnsImageView) {
        if (view.loadId == item.i_id()) {
            return
        } else {
            view.loadId = item.i_id()
        }
    }
    aspectRatio?.let {
        loadImageUrl(view, url!!, width, height)
    }
}


@BindingAdapter("bind:thumbnail", "bind:cardtype", requireAll = true)
fun loadThumbnail(view: NHImageView, item: LinkAsset?, cardType: Int) {
    val url = item?.thumbnailUrl
    view.setFitType(FIT_TYPE.TOP_CROP)
    if (CommonUtils.isEmpty(url)) {
        Glide.with(view).load(R.drawable.default_news_img).into(view)
        return
    }
    val imagaeDimension: Pair<Int, Int>? = CardsBindUtils.getSimpleLinkPostImageDimension()
    imagaeDimension?.let {
        val qualifiedUrl = ImageUrlReplacer.getQualifiedImageUrl(url, it.first,
                it.second)
        CustomLoader(qualifiedUrl).apply(RequestOptions().dontTransform())
                .placeHolder(R.drawable.default_news_img).into(view)
        view.visibility = View.VISIBLE
        return
    }
    view.visibility = View.GONE
}
private const val TAG = "BindingAdapterUtils"
/**
 * @bind:title : to set link clickable text into textview.
 * */
@BindingAdapter("bind:title", "bind:hide_on_empty", "bind:vm", "bind:cardType",requireAll = true)
fun bindLinkableText(view: NHTextView, item: CommonAsset?, hideOnEmpty: Boolean, vm: ClickHandlingViewModel?, cardType:Int?) {

    item?.let {
        // First non empty title
        val repostText: String? = if (item.i_repostAsset() != null) CommonUtils.getString(R.string.repost)
        else null
        val titleText = listOf(it.i_title(), it.i_content(), repostText).find { !CommonUtils.isEmpty(it) }
        if (CommonUtils.isEmpty(titleText)) {
            if (hideOnEmpty) {
                view.visibility = View.GONE
            }
            return
        }
        if (hideOnEmpty) {
            view.visibility = View.VISIBLE
        }
        val titleConvertedText = FontHelper.getFontConvertedString(titleText!!)
        val s: Spannable = HtmlCompat.fromHtml(titleConvertedText, HtmlCompat.FROM_HTML_MODE_LEGACY)
                as Spannable

        for (u in s.getSpans(0, s.length, URLSpan::class.java)) {
            s.setSpan(InternalUrlSpan(u.url, vm?.let { it::onInternalUrlClick }),
                    s.getSpanStart(u), s
                    .getSpanEnd(u), 0)
        }

        if (item.i_isRead() == true) {
            view.setTextColor(CommonUtils.getColor(R.color.story_card_title_read_text_color))
        } else if(cardType == PostDisplayType.POST_COLLECTION_IMAGE.index || cardType == PostDisplayType.SQUARE_CARD_CAROUSEL.index) {
        view.setTextColor(CommonUtils.getColor(R.color.white))
        } else if(cardType == PostDisplayType.POST_COLLECTION_HTML.index || cardType == PostDisplayType.HTML_AND_VIDEO_CAROUSEL.index) {
        view.setTextColor(ThemeUtils.getThemeColorByAttribute(view.context, R.attr.post_collection_title_color))
        } else{
            view.setTextColor(ThemeUtils.getThemeColorByAttribute(view.context, R.attr.story_card_title_text_color))
        }
        updateDirectionFromContentLang(it.i_langCode(), view)

        view.setOnTouchListener(internalLinkTouchListener(s))
        /*
        * Setting BufferType Normal will not give compatibility of changing spannable style after
        *  setting text.
        * */
        view.setSpannableTextWithLangSpecificTypeFaceChanges(
            s,
            titleText,
            BufferType.NORMAL,
            item.i_langCode()
        )
    }
}

fun updateDirectionFromContentLang(itemLang: String?, view: CapTextView) {
    if (itemLang == null) {
        Logger.e(TAG, "updateDirectionFromContentLang: itemLang is null")
        return
    }
    if (UserPreferenceUtil.isRtlLang(itemLang)) {
        view.textAlignment = View.TEXT_ALIGNMENT_TEXT_END
//        view.layoutDirection = View.LAYOUT_DIRECTION_RTL
    } else {
        view.textAlignment = View.TEXT_ALIGNMENT_TEXT_START
//        view.layoutDirection = View.LAYOUT_DIRECTION_LTR
    }
}

fun updateDirectionFromContentLang(itemLang: String?, view: TextView) {
    if (itemLang == null) {
        Logger.e(TAG, "updateDirectionFromContentLang: itemLang is null")
        return
    }
    if (UserPreferenceUtil.isRtlLang(itemLang)) {
        view.textAlignment = View.TEXT_ALIGNMENT_TEXT_END
        view.textDirection=View.TEXT_DIRECTION_RTL
    } else {
        view.textAlignment = View.TEXT_ALIGNMENT_TEXT_START
        view.textDirection=View.TEXT_DIRECTION_LTR
    }
}

@BindingAdapter("bind:isTopStoryCarousel")
fun bindShareContainerMargin(view:ConstraintLayout, isTopStoriesCarousel:Boolean?=false){
    if(isTopStoriesCarousel == true) {
        view.setPadding(0, 0, 0, 0)
    } else{
        view.setPadding(CommonUtils.getDimension(R.dimen.story_card_padding_left), 0, 0, 0)
    }
}


@BindingAdapter("bind:setCommonSourceAndShareMarginIfTopStoryCarousel")
fun bindCommonSourceAndShareMargin(view: ConstraintLayout,
                                   setCommonSourceAndShareMarginIfTopStoryCarousel: Boolean? = false) {
    val layoutParams = view.layoutParams as ConstraintLayout.LayoutParams
    if (setCommonSourceAndShareMarginIfTopStoryCarousel == true) {
        layoutParams.goneStartMargin = 0
    } else {
        layoutParams.goneStartMargin =
                CommonUtils.getApplication().resources.getDimension(R.dimen.common_source_and_share_margin).toInt()
    }
    view.layoutParams = layoutParams
}

@BindingAdapter("bind:setCommonSourceAndShareIcon1MarginIfTopStoryCarousel")
fun bindCommonSourceAndShareIcon1Margin(view: NHTextView,
                                        setCommonSourceAndShareIcon1MarginIfTopStoryCarousel: Boolean? = false) {
    val layoutParams = view.layoutParams as ConstraintLayout.LayoutParams
    if (setCommonSourceAndShareIcon1MarginIfTopStoryCarousel == true) {
        layoutParams.goneEndMargin = 0
    } else {
        layoutParams.goneEndMargin =
                CommonUtils.getApplication().resources.getDimension(R.dimen.common_source_and_share_margin).toInt()
    }
    view.layoutParams = layoutParams
}


@BindingAdapter("bind:isTopStoryCarousel")
fun bindShareContainerMargin(view:NHImageView, isTopStoriesCarousel:Boolean?=false){
    if(isTopStoriesCarousel == true) {
        view.setPadding(0, 0, 0, 0)
    }
}

@BindingAdapter("bind:morePerspectiveDividerColor", requireAll = true)
fun bindMorePerspectiveDividerColor(view:View, canSeeOtherPerspective: Boolean){
    if(canSeeOtherPerspective) {
        view.setBackgroundColor(ThemeUtils.getThemeColorByAttribute(view.context,R.attr.more_perspective_separator_line))
    } else {
        view.setBackgroundColor(ThemeUtils.getThemeColorByAttribute(view.context,R.attr.separator_line))
    }

}

@BindingAdapter("bind:title", "bind:vm", requireAll = true)
fun bindTitleToLinkableText(view: NHTextView, text: String?, vm: ClickHandlingViewModel?) {

    view.text = Constants.EMPTY_STRING
    text ?: return

    val s: Spannable = HtmlCompat.fromHtml(FontHelper.getFontConvertedString(text), HtmlCompat.FROM_HTML_MODE_LEGACY) as Spannable
    for (u in s.getSpans(0, s.length, URLSpan::class.java)) {
        s.setSpan(
            InternalUrlSpan(u.url, vm?.let { it::onInternalUrlClick }),
            s.getSpanStart(u), s
                .getSpanEnd(u), 0
        )
    }

    view.setOnTouchListener(internalLinkTouchListener(s))
    /*
    * Setting BufferType Normal will not give compatibility of changing spannable style after
    *  setting text.
    * */
    view.setSpannableText(s, text, TextView.BufferType.NORMAL)
}

@BindingAdapter("bind:text_title", "bind:hide_on_empty", requireAll = true)
fun bindSimpleTextTitle(view: NHTextView, text: String?, hideOnEmpty: Boolean?) {
    if (hideOnEmpty == true) {
        if (CommonUtils.isEmpty(text)) {
            view.visibility = View.GONE
        } else {
            view.visibility = View.VISIBLE
        }
    }
    view.text = text
}

@BindingAdapter(
    "bind:titleDetailCard",
    "bind:titleDetailParent",
    "bind:vm",
    "bind:extraCard",
    requireAll = true
)
fun bindLinkableTextDetail(
    view: NHTextView, item: CommonAsset?, parent: CommonAsset?,
    vm: ClickHandlingViewModel?, booStrapCard: DetailListCard?
) {
    if (item == null || parent == null || booStrapCard == null) {
        view.visibility = View.GONE
    }

    if (item?.i_type() == AssetType2.COMMENT.name && parent == null) {
        view.setText(Constants.EMPTY_STRING, TextView.BufferType.NORMAL)
        view.visibility = View.GONE
        return
    }

    if (item?.i_type() == AssetType2.COMMENT.name && (parent?.i_title() == null && parent?.i_content() == null)) {
        view.setText(Constants.EMPTY_STRING, TextView.BufferType.NORMAL)
        view.visibility = View.GONE
        return
    }

    var titleText =
        parent?.i_title() ?: parent?.i_content() ?: item?.i_title() ?: booStrapCard?.title
        ?: item?.i_content()
    val itemLang = parent?.i_langCode() ?: item?.i_langCode() ?: booStrapCard?.i_langCode()
    titleText?.let {
        // First non empty title
        if (CommonUtils.isEmpty(titleText)) {
            view.visibility = View.GONE
            return
        }
        view.visibility = View.VISIBLE
        val s: Spannable = HtmlCompat.fromHtml(titleText, HtmlCompat.FROM_HTML_MODE_LEGACY) as Spannable
        for (u in s.getSpans(0, s.length, URLSpan::class.java)) {
            s.setSpan(InternalUrlSpan(u.url, vm?.let { it::onInternalUrlClick }),
                    s.getSpanStart(u), s
                    .getSpanEnd(u), 0)
        }
        view.setLineSpacing(0f, Constants.TITLE_LINE_SPACING)
        BoldStyleHelper.setBoldTextForBigCardIfApplicable2(view, item)
        view.setTextIsSelectable(item?.i_isSelectAndCopyPasteDisabled() == false)
        view.setOnTouchListener(internalLinkTouchListener(s))
        updateDirectionFromContentLang(itemLang, view)
        view.setSpannableTextWithLangSpecificTypeFaceChanges(s, titleText, BufferType.SPANNABLE, item?.i_langCode()?:parent?.i_langCode()?:booStrapCard?.i_langCode())
    }
}

@BindingAdapter(
        "bind:titleDetailCard",
        "bind:titleDetailParent",
        "bind:vm",
        "bind:user_font_setting",
        requireAll = true
)
fun bindLinkableTextDetail(
        view: CapTextView, item: CommonAsset?, parent: CommonAsset?,
        vm: ClickHandlingViewModel?,progress: Int?
) {
    if (item == null || parent == null ) {
        view.visibility = View.GONE
    }

    if (item?.i_type() == AssetType2.COMMENT.name && parent == null) {
        view.visibility = View.GONE
        return
    }

    if (item?.i_type() == AssetType2.COMMENT.name && (parent?.i_title() == null && parent?.i_content() == null)) {
        view.visibility = View.GONE
        return
    }
    val titleText =
            parent?.i_title() ?: parent?.i_content() ?: item?.i_title() ?: item?.i_content()
    val itemLang = parent?.i_langCode() ?: item?.i_langCode()
    titleText?.let {
        // First non empty title
        if (CommonUtils.isEmpty(titleText)) {
            view.visibility = View.GONE
            return
        }
        view.visibility = View.VISIBLE
        val s: Spannable = HtmlCompat.fromHtml(titleText, HtmlCompat.FROM_HTML_MODE_LEGACY) as Spannable
        for (u in s.getSpans(0, s.length, URLSpan::class.java)) {
            s.setSpan(InternalUrlSpan(u.url, vm?.let { it::onInternalUrlClick }),
                    s.getSpanStart(u), s
                    .getSpanEnd(u), 0)
        }
        progress ?: return
        val fontDiff = progress - NewsConstants.DEFAULT_PROGRESS_COUNT
        val fontSizeMultiplier = (fontDiff * 2)
        view.setCapDropNumber(item?.i_titleFirstAksharCharCount())
        view.enableCapText(item?.i_enableBigtextTitle())
        view.setTextIsSelectable(item?.i_isSelectAndCopyPasteDisabled() == false)
        view.setOnTouchListener(internalLinkTouchListener(s))
        updateDirectionFromContentLang(itemLang, view)
        view.setHtmlText(s, titleText,itemLang)
    }
}

@BindingAdapter("bind:contentText","bind:item","bind:vm","bind:titleLineCount","bind:user_font_setting")
fun bindcapTextViewChunk1(view: CapTextView, contentText: String?, item: CommonAsset?, vm: ClickHandlingViewModel?,titleLineCount:Int,progress: Int?,) {
    contentText?.let {
        val s: Spannable = HtmlCompat.fromHtml(contentText, HtmlCompat.FROM_HTML_MODE_LEGACY) as Spannable
        for (u in s.getSpans(0, s.length, URLSpan::class.java)) {
            s.setSpan(InternalUrlSpan(u.url, vm?.let { it::onInternalUrlClick }),
                s.getSpanStart(u), s
                    .getSpanEnd(u), 0)
        }

        val itemLang = item?.i_langCode()

        progress ?: return
        val fontDiff = progress - NewsConstants.DEFAULT_PROGRESS_COUNT
        val fontSizeMultiplier = (fontDiff * 2)
        view.setCapDropNumber(item?.i_c1FirstAksharCharCount())
        val isUrdu = item?.i_langCode()?.equals(NewsConstants.URDU_LANGUAGE_CODE) ?: false
        val enable = canShowTitle(item,titleLineCount) && (item?.i_enableBigtextChunk1()?:true) && !isUrdu
        view.enableCapText(enable)
        view.setTextIsSelectable(item?.i_isSelectAndCopyPasteDisabled() == false)
        updateDirectionFromContentLang(itemLang, view)
        view.setOnTouchListener(internalLinkTouchListener(s))

        view.setHtmlText(s,contentText,item?.i_langCode(),fontSizeMultiplier)
    }
}

private fun canShowTitle( card: CommonAsset?,titleLineCount:Int): Boolean {
    return card != null && ( card?.i_uiType()!= UiType2.GRID_2 && card?.i_uiType()!= UiType2.GRID_3 && card?.i_uiType()!= UiType2.GRID_4 && card?.i_uiType()!= UiType2.GRID_5 && card?.i_subFormat()!= SubFormat.S_W_VIDEO) && titleLineCount <= 4
}

@BindingAdapter("bind:contentText","bind:item","bind:vm","bind:user_font_setting")
fun bindLinkableTextDetail(view: NHTextView, contentText: String?, item: CommonAsset?, vm: ClickHandlingViewModel?,progress: Int?) {
    contentText?.let {
        val s: Spannable = HtmlCompat.fromHtml(contentText, HtmlCompat.FROM_HTML_MODE_LEGACY) as Spannable
        for (u in s.getSpans(0, s.length, URLSpan::class.java)) {
            s.setSpan(InternalUrlSpan(u.url, vm?.let { it::onInternalUrlClick }),
                s.getSpanStart(u), s
                    .getSpanEnd(u), 0)
        }
        val spannableString =
                SpannableString(s.trim())
        progress ?: return
        val fontDiff = progress - NewsConstants.DEFAULT_PROGRESS_COUNT
        val fontSize = NewsConstants.DEFAULT_FONT_SIZE + (fontDiff * 2)
        view.setTextSize(TypedValue.COMPLEX_UNIT_DIP, fontSize.toFloat())
        val lineSpacing = (9* fontSize)/16.0F
        view.setLineSpacing(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, lineSpacing,  view.context.resources.displayMetrics), 1.0f)
        view.setTextIsSelectable(item?.i_isSelectAndCopyPasteDisabled() == false)
        view.setOnTouchListener(internalLinkTouchListener(spannableString))
        view.setClickable(true)

        view.setMovementMethod (LinkMovementMethod.getInstance())
        view.setSpannableTextWithLangSpecificTypeFaceChanges(spannableString, contentText, BufferType.SPANNABLE, item?.i_langCode())
    }
}

@BindingAdapter("bind:htmlText", requireAll = true)
fun bindHtmlText(view: NHTextView, htmlText: String?) {
    //Need to reset the text incase of view reuse
    view.text = Constants.EMPTY_STRING
    htmlText ?: return
    val s: Spannable = HtmlCompat.fromHtml(FontHelper.getFontConvertedString(htmlText), HtmlCompat
            .FROM_HTML_MODE_LEGACY) as Spannable
    view.setSpannableText(s, htmlText)
}

@BindingAdapter("bind:htmlText", "bind:item",requireAll = true)
fun bindHtmlText(view: NHTextView, htmlText: String?, item: CommonAsset?) {
    //Need to reset the text incase of view reuse
    view.text = Constants.EMPTY_STRING
    htmlText ?: return
    val s: Spannable = HtmlCompat.fromHtml(FontHelper.getFontConvertedString(htmlText), HtmlCompat
        .FROM_HTML_MODE_LEGACY) as Spannable
    view.setSpannableTextWithLangSpecificTypeFaceChanges(s, htmlText, BufferType.SPANNABLE, item?.i_langCode())
}

@BindingAdapter("bind:htmlText", "bind:vm", requireAll = true)
fun bindHtmlClickableText(view: NHTextView, htmlText: String?, vm: ClickHandlingViewModel?) {
    view.text = Constants.EMPTY_STRING
    htmlText ?: return
    val s: Spannable = HtmlCompat.fromHtml(FontHelper.getFontConvertedString(htmlText), HtmlCompat
            .FROM_HTML_MODE_LEGACY) as Spannable
    for (u in s.getSpans(0, s.length, URLSpan::class.java)) {
        s.setSpan(InternalUrlSpan(u.url, vm?.let { it::onInternalUrlClick }),
                s.getSpanStart(u), s
                .getSpanEnd(u), 0)
    }

    view.setOnTouchListener(internalLinkTouchListener(s))
    view.setSpannableText(s, htmlText)
}

@BindingAdapter("bind:htmlText", "bind:vm", "bind:item", requireAll = true)
fun bindHtmlClickableText(view: NHTextView, htmlText: String?, vm: ClickHandlingViewModel?, item: CommonAsset?) {
    view.text = Constants.EMPTY_STRING
    htmlText ?: return
    val s: Spannable = HtmlCompat.fromHtml(FontHelper.getFontConvertedString(htmlText), HtmlCompat
        .FROM_HTML_MODE_LEGACY) as Spannable
    for (u in s.getSpans(0, s.length, URLSpan::class.java)) {
        s.setSpan(InternalUrlSpan(u.url, vm?.let { it::onInternalUrlClick }),
            s.getSpanStart(u), s
                .getSpanEnd(u), 0)
    }

    view.setOnTouchListener(internalLinkTouchListener(s))
    view.setSpannableTextWithLangSpecificTypeFaceChanges(s, htmlText, BufferType.SPANNABLE, item?.i_langCode())
}

@BindingAdapter("bind:viewAllParentTitle", requireAll = true)
fun bindExpandableText(view: NHTextView, item: CommonAsset?) {
    val text = item?.i_title() ?: item?.i_content() ?: Constants.EMPTY_STRING
    if (CommonUtils.isEmpty(text)) {
        view.visibility = View.GONE
    }
    else {
        view.visibility = View.VISIBLE
        view.setText(AndroidUtils.getTextFromHtml(text), TextView.BufferType.NORMAL)
    }
}

@BindingAdapter("bind:discussionText", "bind:vm", requireAll = true)
fun bindDiscussionText(view: ExpandableRecyclingTextView, item: CommonAsset?, vm: CardsViewModel?) {
     var text: String? = null
    if (item?.i_isReported() == true) {
        text = CommonUtils.getString(R.string.reported)
        view.setTextColor(CommonUtils.getColor(R.color.reported_text_color))
    } else {
        text = item?.i_content()
        view.setTextColor(
                ThemeUtils.getThemeColorByAttribute(view.context, R.attr.pd_discussion_post_color))
    }

    if (CommonUtils.isEmpty(text)) {
        return
    }

    val titleConvertedText = FontHelper.getFontConvertedString(text!!)
    val s: Spannable = HtmlCompat.fromHtml(titleConvertedText, HtmlCompat.FROM_HTML_MODE_LEGACY)
            as Spannable

    for (u in s.getSpans(0, s.length, URLSpan::class.java)) {
        s.setSpan(InternalUrlSpan(u.url, vm?.let { it::onInternalUrlClick }),
                s.getSpanStart(u), s
                .getSpanEnd(u), 0)
    }

    view.setOnTouchListener(internalLinkTouchListener(s))
    view.setSpannableText(s, text, TextView.BufferType.NORMAL)
    val moreText = " " + CommonUtils.getString(R.string.read_more_botton)
    ExpandableRecyclingTextView.addLayoutEllipsize(view, 4, text, "<b><font color='#1f9ee1'>", moreText, "</font></b>")
    view.setSpannableText(s, text, TextView.BufferType.NORMAL)
}

@BindingAdapter("bind:repliesText", "bind:vm", requireAll = true)
fun bindRepliesText(view: ExpandableRecyclingTextView, item: CommonAsset?, vm: CardsViewModel?) {

    var text: String? = null
    if (item?.i_isReported() == true) {
        text = CommonUtils.getString(R.string.reported)
        view.setTextColor(CommonUtils.getColor(R.color.reported_text_color))
    } else {
        text = item?.i_content()
        view.setTextColor(
                ThemeUtils.getThemeColorByAttribute(view.context, R.attr.pd_discussion_post_color))
    }
    if (CommonUtils.isEmpty(text)) {
        return
    }

    val titleConvertedText = FontHelper.getFontConvertedString(text!!)
    val s: Spannable = HtmlCompat.fromHtml(titleConvertedText, HtmlCompat.FROM_HTML_MODE_LEGACY)
            as Spannable

    for (u in s.getSpans(0, s.length, URLSpan::class.java)) {
        s.setSpan(InternalUrlSpan(u.url, vm?.let { it::onInternalUrlClick }),
                s.getSpanStart(u), s
                .getSpanEnd(u), 0)
    }

    view.setOnTouchListener(internalLinkTouchListener(s))
    view.setSpannableText(s, text, TextView.BufferType.NORMAL)
    val moreText = " " + CommonUtils.getString(R.string.read_more_botton)
    ExpandableRecyclingTextView.addLayoutEllipsize(view, 4, text, "<b><font color='#1f9ee1'>", moreText, "</font></b>")
    view.setSpannableText(s, text, TextView.BufferType.NORMAL)
}

@BindingAdapter("bind:discussionTextExpanded", "bind:vm", requireAll = true)
fun bindDiscussionTextExpanded(view: NHTextView, item: CommonAsset?, vm: CardsViewModel?) {
    var text: String? = null
    if (item?.i_isReported() == true) {
        text = CommonUtils.getString(R.string.reported)
        view.setTextColor(CommonUtils.getColor(R.color.reported_text_color))
    } else {
        text = item?.i_content()
        view.setTextColor(
                ThemeUtils.getThemeColorByAttribute(view.context, R.attr.pd_discussion_post_color))
    }

    if (CommonUtils.isEmpty(text)) {
        return
    }

    val titleConvertedText = FontHelper.getFontConvertedString(text!!)
    val s: Spannable = HtmlCompat.fromHtml(titleConvertedText, HtmlCompat.FROM_HTML_MODE_LEGACY)
            as Spannable

    for (u in s.getSpans(0, s.length, URLSpan::class.java)) {
        s.setSpan(InternalUrlSpan(u.url, vm?.let { it::onInternalUrlClick }),
                s.getSpanStart(u), s
                .getSpanEnd(u), 0)
    }

    view.setOnTouchListener(internalLinkTouchListener(s))
    view.setSpannableText(s, text, TextView.BufferType.NORMAL)
    view.setSpannableText(s, text, TextView.BufferType.NORMAL)
}

@BindingAdapter("bind:discussionText", "bind:vm", requireAll = true)
fun bindDiscussionText(view: ExpandableRecyclingTextView, item: CommonAsset?, vm: ViewAllCommentsViewModel?) {
    var text: String? = null
    if (item?.i_isReported() == true) {
        text = CommonUtils.getString(R.string.reported)
        view.setTextColor(CommonUtils.getColor(R.color.reported_text_color))
    } else {
        text = item?.i_content()
        view.setTextColor(
                ThemeUtils.getThemeColorByAttribute(view.context, R.attr.pd_discussion_post_color))
    }
    if (CommonUtils.isEmpty(text)) {
        return
    }
    val titleConvertedText = FontHelper.getFontConvertedString(text!!)
    val s: Spannable = HtmlCompat.fromHtml(titleConvertedText, HtmlCompat.FROM_HTML_MODE_LEGACY)
            as Spannable

    for (u in s.getSpans(0, s.length, URLSpan::class.java)) {
        s.setSpan(InternalUrlSpan(u.url, vm?.let { it::onInternalUrlClick }),
                s.getSpanStart(u), s
                .getSpanEnd(u), 0)
    }

    view.setOnTouchListener(internalLinkTouchListener(s))
    view.setSpannableText(s, text, TextView.BufferType.NORMAL)
    val moreText = " " + CommonUtils.getString(R.string.read_more_botton)
    ExpandableRecyclingTextView.addLayoutEllipsize(view, 4, text, "<b><font color='#1f9ee1'>", moreText, "</font></b>")
    view.setSpannableText(s, text, TextView.BufferType.NORMAL)
}

/**
 * @bind:disclaimer
 * */

@BindingAdapter("bind:disclaimer", requireAll = true)
fun bindLinkableDisclaimer(view: NHTextView, item: CommonAsset?) {
    item?:return
    // First non empty disclaimer
    val disclaimerText = item.i_disclaimer()
    if (CommonUtils.isEmpty(disclaimerText)) {
        view.visibility = View.GONE
        return
    }
    view.visibility = View.VISIBLE
    val s: Spannable = HtmlCompat.fromHtml(FontHelper.getFontConvertedString(disclaimerText), HtmlCompat
            .FROM_HTML_MODE_LEGACY) as Spannable
    view.setSpannableText(s, disclaimerText)
}

@BindingAdapter("bind:user_font_setting", "bind:enable_font_setting","bind:is_title", requireAll = true)
fun bindFontSizeForTitle(view: TextView, progress: Int?, applyFontSizeSetting: Boolean?,isTitle :Boolean?) {
    if (applyFontSizeSetting != true) {
        return
    }
    progress ?: return
    isTitle?:return
    val fontDiff = progress - NewsConstants.DEFAULT_PROGRESS_COUNT
    val fontSize = NewsConstants.DEFAULT_TITLE_SIZE + (fontDiff * 2)
    view.setTextSize(TypedValue.COMPLEX_UNIT_DIP, fontSize.toFloat());
}


@BindingAdapter("bind:item", "bind:content", "bind:newsappjs", "bind:user_font_setting",
        "bind:timeSpentEventId", "bind:isInBottomSheet")
fun bindContentText(view: HeightAwareWebView, item: CommonAsset?, content: String?,
                    newsAppJS: NewsAppJS?, progress: Int?, timeSpentEventId: Long?,
                    isInBottomSheet: Boolean?) {

    item?.let {
        var baseUrl: String? = item.i_contentBaseUrl()
        if (baseUrl == null) {
            baseUrl = NewsBaseUrlContainer.getApplicationUrl()
        }
        view.settings.domStorageEnabled = true
        view.isHorizontalScrollBarEnabled = false
        view.isVerticalScrollBarEnabled = false

        var fontDiff = 0
        progress?.let {

            fontDiff = progress - NewsConstants.DEFAULT_PROGRESS_COUNT

        }
        val webViewFontSize = NewsConstants.DEFAULT_FONT_SIZE + (fontDiff * 2)

        view.settings.defaultFontSize = webViewFontSize

        Logger.d(TAG, "showNewsDescription : before replace: $content")
        val liteModeDesc = NewsDetailUtil.modifyImageTagsInHTMLContent(content)

        val selectCopyDisabled = item.i_isSelectAndCopyPasteDisabled() ?: false
        val css = newsAppJS?.css ?: Constants.EMPTY_STRING
        val js = newsAppJS?.jsScript ?: Constants.EMPTY_STRING
        // TODO: Get Is Urdu flag
        val isUrdu = item.i_langCode()?.equals(NewsConstants.URDU_LANGUAGE_CODE) ?: false
        var inBottomSheet : Boolean = isInBottomSheet ?: false
        val html = HtmlFontHelper.wrapToFontHTML(
            selectCopyDisabled, liteModeDesc, css, js,
            isUrdu, inBottomSheet ||ThemeUtils.isNightMode(), item?.i_langCode(),webViewFontSize)
        Logger.d(TAG, "showNewsDescription : final data in ww \n$html")
        Logger.d(TAG, "css : css \n$css")
        view.loadDataWithBaseURL(baseUrl, html,
                NewsConstants.HTML_MIME_TYPE, NewsConstants.HTML_UTF_ENCODING, null)

        NewsDetailTimespentHelper.getInstance().postUpdateTimespentEvent(timeSpentEventId,
                NewsDetailTimespentHelper.FONT_SIZE, Integer.toString(
                NewsDetailTimespentHelper.getFontSizeForTimespentEvent(webViewFontSize)))
    }
}

/**
 * @bind:disclaimer
 * */

@BindingAdapter("bind:postProgress")
fun setProgress(view: ProgressBar, item: CommonAsset?) {
    if (item == null)
        return
    view.progress = when (item.i_status()) {
        PostUploadStatus.UPLOADING.name -> 50
        PostUploadStatus.SUCCESS.name -> 100
        PostUploadStatus.FAILED.name -> 0
        else -> {
            0
        }
    }

}

@BindingAdapter("bind:postProgressText")
fun setPostText(view: NHTextView, item: CommonAsset?) {
    if (item == null)
        return

    val success = CommonUtils.getString(R.string.cp_success_message)
    val retry = CommonUtils.getString(R.string.cp_error_message)
    val inProgress = CommonUtils.getString(R.string.post_in_progress)
    view.text = when (item.i_status()) {
        PostUploadStatus.UPLOADING.name -> inProgress
        PostUploadStatus.SUCCESS.name -> success
        else -> {
            ""
        }
    }
}

@BindingAdapter("bind:opLabel", "bind:opLabelAlternative", requireAll = true)
fun bindOPLabel(view: NHTextView, item: CommonAsset?, booStrapCard: DetailListCard?) {
    if (item == null && booStrapCard == null) {
        return
    }

    // First non empty disclaimer
    view.text = CommonUtils.getString(R.string.other_perspective_image_label,
            if  (item != null)
                item.i_moreStories()?.size
            else
                booStrapCard?.moreStoryCount
    )
}


/**
 * @bind:cardLabel bind card label to post ex."breaking"
 * */
@BindingAdapter("bind:cardLabel", requireAll = true)
fun bindCardLabel(view: NHTextView, cardLabel: CardLabel2?) {
    if (cardLabel == null ||
            (System.currentTimeMillis()/1000L) > (cardLabel.ttl ?: 0) ||
            CommonUtils.isEmpty(cardLabel.text)) {
        view.visibility = View.GONE
        return
    }
    view.visibility = View.GONE
    view.text = cardLabel.text
    view.background = CardsBindUtils.getDrawableForCardLabel(cardLabel)
    view.setTextColor(ViewUtils.getColor(cardLabel.fgColor, Color.WHITE))
}

fun CustomLoader(urlOrPath: String?): Image.Loader {
    urlOrPath ?: return Image.load(Constants.EMPTY_STRING)
    if (urlOrPath.contains(Constants.URL_HTTP_FORMAT, true)) {
        return Image.load(urlOrPath)
    } else {
        return Image.load(File(urlOrPath), false)
    }
}


/*
* @bind: viralimage bind background option for i_viral()
* */
@BindingAdapter("bind:viralBg", "bind:showNsfw", "bind:hide_on_empty", requireAll = true)
fun bindBackgroundOption(view: NHImageView,
                         item: CommonAsset?,
                         showNsfwFilter: Boolean,
                         hideOnEmpty: Boolean = true) {
    bindBackgroundOption(view, item, showNsfwFilter, hideOnEmpty, null)
}


/*
* @bind: viralimage bind background option for i_viral()
* */
@BindingAdapter("bind:viralBg", "bind:showNsfw", "bind:hide_on_empty", "bind:aspectRatio", requireAll = true)
fun bindBackgroundOption(view: NHImageView, item: CommonAsset?, showNsfwFilter: Boolean,
                         hideOnEmpty: Boolean = true, aspectRatio: Float?) {
    val viral = item?.i_viral()
    val backgroundOption = viral?.backgroundOption
    if (backgroundOption == null) {
        if (hideOnEmpty) {
            view.visibility = View.GONE
        }
        return
    }
    val prevState = view.getTag(R.id.viral_image_extra) as? Bundle
    val update = if (prevState != null) {
        val prevNsfw = prevState.getBoolean("nsfw")
        val prevId = prevState.getString("id")
        prevId != item.i_id() || prevNsfw != showNsfwFilter
    } else {
        true
    }
    if (!update) {
        return
    }

    val newState = Bundle()
    newState.putBoolean("nsfw", showNsfwFilter)
    newState.putString("id", item.i_id())
    view.setTag(R.id.viral_image_extra, newState)

    view.visibility = View.VISIBLE
    when (backgroundOption.type) {
        BackgroundType2.BG_COLOR -> {
            view.setBackgroundColor(ViewUtils.getColor(backgroundOption.bgColor, Color.TRANSPARENT))
        }

        BackgroundType2.GRADIENT -> {
            Glide.with(view)
                    .applyDefaultRequestOptions(RequestOptions().dontTransform())
                    .load(getGradientDrawable(backgroundOption)).into(view)
        }

        BackgroundType2.IMAGE_BG -> {
            view.setFitType(getFitTypeConverted(backgroundOption.fitType))
            val imageAspectRatio = aspectRatio ?: CardsBindUtils.getViralAspectRatio(item)
            val width = CommonUtils.getDeviceScreenWidth() - 2 * CommonUtils.getDimension(R
                    .dimen.story_card_padding)
            val height = width / imageAspectRatio
            val url = ImageUrlReplacer.getQualifiedImageUrl(backgroundOption.imageUrl, width,
                    height.toInt())
            val requestOption = if (showNsfwFilter && viral.nsfw) {
                RequestOptions().transform(BlurTransformation()).dontAnimate()
            } else {
                RequestOptions().dontTransform()
            }
            CustomLoader(url).apply(requestOption).into(view)
        }
    }
}

@BindingAdapter("bind:viralCollectionBg", "bind:showNsfw", "bind:hide_on_empty", "bind:aspectRatio", requireAll = true)
fun bindBackgroundOptionForViralCarousel(view: NHImageView, item: CommonAsset?, showNsfwFilter: Boolean,
                         hideOnEmpty: Boolean = true, aspectRatio: Float?) {
    if(item?.i_format() == Format.VIDEO) {
        loadCollectionThumbnail(view, item, aspectRatio)
        return
    }
    var width = 0
    var height = 0
    val imageAspectRatio = aspectRatio ?: CardsBindUtils.getViralAspectRatio(item)
    width = CommonUtils.getDeviceScreenWidth() - ((CommonUtils.getDeviceScreenWidth())*(CardsBindUtils.getSecondItemVisiblePercentage()/100f)).toInt() - 2*(CommonUtils.getDimension(R.dimen.collection_margin))
    height =  (width / imageAspectRatio).toInt()
    view.layoutParams.width = width
    view.layoutParams.height = height

    val viral = item?.i_viral()
    val backgroundOption = viral?.backgroundOption
    if (backgroundOption == null) {
        if (hideOnEmpty) {
            view.visibility = View.GONE
        }
        return
    }

    val prevState = view.getTag(R.id.viral_image_extra) as? Bundle
    val update = if (prevState != null) {
        val prevNsfw = prevState.getBoolean("nsfw")
        val prevId = prevState.getString("id")
        prevId != item.i_id() || prevNsfw != showNsfwFilter
    } else {
        true
    }
    if (!update) {
        return
    }

    val newState = Bundle()
    newState.putBoolean("nsfw", showNsfwFilter)
    newState.putString("id", item.i_id())
    view.setTag(R.id.viral_image_extra, newState)

    view.visibility = View.VISIBLE
    when (backgroundOption.type) {
        BackgroundType2.BG_COLOR -> {
            view.setBackgroundColor(ViewUtils.getColor(backgroundOption.bgColor, Color.TRANSPARENT))
        }

        BackgroundType2.GRADIENT -> {
            Glide.with(view)
                .applyDefaultRequestOptions(RequestOptions().dontTransform())
                .load(getGradientDrawable(backgroundOption)).into(view)
        }

        BackgroundType2.IMAGE_BG -> {
            view.setFitType(getFitTypeConverted(backgroundOption.fitType))
            val url = ImageUrlReplacer.getQualifiedImageUrl(backgroundOption.imageUrl, width,
                height.toInt())
            val requestOption = if (showNsfwFilter && viral.nsfw) {
                RequestOptions().transform(BlurTransformation()).dontAnimate()
            } else {
                RequestOptions().dontTransform()
            }
            CustomLoader(url).apply(requestOption).into(view)
        }
    }
}

@BindingAdapter("bind:dimensionAspectRatio")
fun bindDimensionAspectRatio(view: View, aspectRatio: Float? = null) {
    aspectRatio ?: return
    val parent = view.parent as? ConstraintLayout ?: return
    val set = ConstraintSet()
    set.clone(parent)
    set.setDimensionRatio(view.id, CardsBindUtils.getAspectRatioString(aspectRatio))
    set.applyTo(parent)
}


fun getGradientDrawable(backgroundOption: BackgroundOption2): GradientDrawable {
    val colors = IntArray(3)
    colors[0] = ViewUtils.getColor(backgroundOption.startColor, Color.TRANSPARENT)
    colors[1] = ViewUtils.getColor(backgroundOption.midColor, Color.TRANSPARENT)
    colors[2] = ViewUtils.getColor(backgroundOption.endColor, Color.TRANSPARENT)
    val orientation = ViewUtils.getGradientType(backgroundOption.gradientType)
    return GradientDrawable(orientation, colors)
}

fun getFitTypeConverted(fitType: String?): FIT_TYPE {
    return FIT_TYPE.fromName(fitType!!) ?: return FIT_TYPE.TOP_CROP
}

@BindingAdapter("bind:src", requireAll = true)
fun setDrawable(view: NHImageView, resId: Int) {
    view.setImageResource(resId)
}

@BindingAdapter("bind:srcFollow", requireAll = true)
fun setFollowIcon(view: ImageView, isFollowing: Boolean) {
    val icon: Int
    if (isFollowing) {
        icon = ThemeUtils.getThemeDrawableByAttribute(view.context, R.attr.follow_star_fill, R
                .drawable.ic_follow_star_fill)
    } else {
        icon = ThemeUtils.getThemeDrawableByAttribute(view.context, R.attr.follow_star, R
                .drawable.ic_follow_star)
    }
    view.setImageResource(icon)
}

@BindingAdapter("bind:srcFollowInbox", requireAll = true)
fun setFollowIconInbox(view: ImageView, isFollowing: Boolean) {
    val icon: Int
    if (isFollowing) {
        view.visibility = View.GONE
    } else {
        view.visibility = View.VISIBLE
        icon = ThemeUtils.getThemeDrawableByAttribute(view.context, R.attr.follow_star, R
            .drawable.ic_follow_star)
        view.setImageResource(icon)
    }
}

@BindingAdapter("bind:isFollowing", requireAll = true)
fun setFollowButtonBg(view: ConstraintLayout, isFollowing: Boolean) {
    val bg: Int
    if (isFollowing) {
        bg = R.drawable.following_btn_bg
    } else {
        bg = ThemeUtils.getThemeDrawableByAttribute(view.context, R.attr.inbox_follow_button_bg, R.drawable.following_btn_bg)
    }

    view.setBackgroundResource(bg)
}


@BindingAdapter("bind:otherPerspectiveCard", "bind:vm", "bind:state", requireAll = true)
fun bindOtherPerspective(view: RecyclerView, item: CommonAsset?, vm: CardsViewModel, state: PerspectiveState) {
    if (view.adapter == null) {
        view.adapter = PerspectiveCarouselCardsAdapter(vm, state)
        view.layoutManager = LinearLayoutManager(view.context, RecyclerView.HORIZONTAL, false)
    }

    (view.adapter as PerspectiveCarouselCardsAdapter).setItems(item?.i_moreStories())
    (view.adapter as PerspectiveCarouselCardsAdapter).setParent(item)
}


@BindingAdapter("bind:drawableStart", "bind:drawableEnd", "bind:drawableTop", "bind:drawableBottom",
        requireAll = false)
fun setCompoudDrawable(view: TextView, start: Int? = null, end: Int? = null, top: Int? = null,
                       bottom: Int? =
                               null) {
    val drwStart = start?.run {
        CommonUtils.getDrawable(this)
    }

    val drwEnd = end?.run {
        CommonUtils.getDrawable(this)
    }

    val drwTop = top?.run {
        CommonUtils.getDrawable(this)
    }

    val drwBottom = bottom?.run {
        CommonUtils.getDrawable(this)
    }

    view.setCompoundDrawablesWithIntrinsicBounds(drwStart, drwTop, drwEnd, drwBottom)
}

@BindingAdapter("bind:set_poll_background", "bind:index", "bind:userSelectedId", requireAll = true)
fun setProgressDrawable(view: ProgressBar, item: CommonAsset?, index: Int, userSelectedId:
String?) {
    item ?: return
    val itemOrderIndex = CardsBindUtils.getPollVoteReverseSortIndex(item, index)
    val isSelected = CardsBindUtils.showPollSelected(userSelectedId, item, index)
    val pollColor: Int
    pollColor = if (isSelected)
        CommonUtils.getColor(R.color.poll_selected_color)
    else
        ThemeUtils.getThemeColorByAttribute(view.context, R.attr.poll_selected_color)

    val transparent = GradientDrawable()
    transparent.setColor(ThemeUtils.getThemeColorByAttribute(view.context, R.attr.poll_bg_color))
    val colorBg = GradientDrawable()
    colorBg.cornerRadius = CommonUtils.getDimension(R.dimen.poll_bg_radius).toFloat()
    colorBg.setColor(pollColor)
    val clipBg = ClipDrawable(colorBg, Gravity.START, ClipDrawable.HORIZONTAL)
    val ld = LayerDrawable(arrayOf(transparent, clipBg))
    ld.setId(1, android.R.id.progress)
    ld.setId(0, android.R.id.background)
    view.progressDrawable = ld
}


@BindingAdapter("bind:viraltext", "bind:showNsfw", requireAll = true)
fun bindViralText(view: NHTextView, item: CommonAsset?, showNsfwFilter: Boolean) {
    val viral = item?.i_viral()

    if (viral == null)
        return

    val showFilter = showNsfwFilter && viral.nsfw
    if (showFilter) {
        view.visibility = View.GONE
        return
    }
    val text = viral.itemText
    if (CommonUtils.isEmpty(text)) {
        view.visibility = View.GONE
        return
    }
    view.visibility = View.VISIBLE
    val color = ViewUtils.getColor(viral.itemTextColor, Color.WHITE)
    view.setSpannableTextWithLangSpecificTypeFaceChanges(text, BufferType.SPANNABLE, item?.i_langCode())
    view.setTextColor(color)
}

@BindingAdapter("bind:viewCount", requireAll = true)
fun bindViralViewCount(view: NHTextView, item: CommonAsset?) {
    if(item == null)
        return
    var count = item.i_counts()?.VIEWS?.value
    if(CommonUtils.equals(count, Constants.ZERO_STRING)) {
        count = "1"
        view.text = count.plus(" ").plus(CommonUtils.getString(R.string.view))
    } else {
        view.text = count.plus(" ").plus(CommonUtils.getString(R.string.views))
    }
}

@BindingAdapter("bind:videoViewCount", requireAll = true)
fun bindVideoViewCount(view: NHTextView, item: CommonAsset?) {
    if (item == null)
        return
    val count = CardsBindUtils.getJustViewCount(item.i_counts())
    if (!CommonUtils.isEmpty(count)) {
        view.setSpannableTextWithLangSpecificTypeFaceChanges(count, BufferType.SPANNABLE, item?.i_langCode())
    }
}

@BindingAdapter("bind:sourceIcon", requireAll = true)
fun bindSourceIcon(view: ImageView, item: CommonAsset?) {
    //To remove old image in case of re-used image view
    view.setImageDrawable(null)
    val url = item?.i_source()?.imageUrl
    if (CardsBindUtils.showBigSourceImage(item)) {
        view.visibility = View.GONE
        return
    }

    view.visibility = View.VISIBLE
    val viewSize = CommonUtils.getDimension(R.dimen.post_source_circle_icon_size)
    val qualifiedUrl = ImageUrlReplacer.getQualifiedImageUrl(url, viewSize, viewSize)
    EntityImageUtils.loadImage(qualifiedUrl, item?.i_source()?.nameEnglish, view, R.drawable.ic_default_hashtag,false)
}

@BindingAdapter("bind:sourceCOCIcon", requireAll = true)
fun bindCOCSourceIcon(view: ImageView, item: CommonAsset?) {
    //To remove old image in case of re-used image view
    view.setImageDrawable(null)
    val url = item?.i_source()?.imageUrl
    url ?: return
    val viewSize = CommonUtils.getDimension(R.dimen.coc_source_square_icon_size)
    val qualifiedUrl = ImageUrlReplacer.getQualifiedImageUrl(url, viewSize, viewSize)
    EntityImageUtils.loadImage(qualifiedUrl, item.i_source()?.nameEnglish, view, R.drawable.ic_default_hashtag,false)
}

@BindingAdapter("bind:tabSourceIcon", requireAll = true)
fun bindLiveCarouselTabSourceIcon(view: ImageView, item: CommonAsset?) {
    //To remove old image in case of re-used image view
    view.setImageDrawable(null)
    val url = item?.i_source()?.imageUrl
    if (CardsBindUtils.showBigSourceImage(item)) {
        view.visibility = View.GONE
        return
    }

    view.visibility = View.VISIBLE
    val viewSize = CommonUtils.getDimension(R.dimen.post_source_circle_icon_size)
    val qualifiedUrl = ImageUrlReplacer.getQualifiedImageUrl(url, viewSize, viewSize)
    EntityImageUtils.loadImage(qualifiedUrl, item?.i_source()?.nameEnglish, view, R.drawable.ic_default_hashtag, false)
}

@BindingAdapter("bind:postsourceIcon", requireAll = true)
fun bindSourceIcon(view: ImageView, item: PostSourceAsset?) {
    //To remove old image in case of re-used image view

    val url = item?.imageUrl
    view.visibility = View.VISIBLE
    val viewSize = CommonUtils.getDimension(R.dimen.post_source_circle_icon_size)
    val qualifiedUrl = ImageUrlReplacer.getQualifiedImageUrl(url, viewSize, viewSize)
    val defaultId = if (PostConstants.UGC_SOURCE_TYPE == item?.type) R.drawable.default_user_avatar else R.drawable.ic_default_hashtag

    view.setImageResource(defaultId)
    Image.load(qualifiedUrl)
        .placeHolder(defaultId)
        .into(view)
}

@BindingAdapter("bind:sourceIcon", requireAll = true)
fun bindSourceIcon(view: ImageView, item: DetailListCard?) {
    //To remove old image in case of re-used image view
    view.setImageDrawable(null)
    val url = item?.source?.imageUrl
    if (CardsBindUtils.showBigSourceImage(item)) {
        view.visibility = View.GONE
        return
    }

    view.visibility = View.VISIBLE
    val viewSize = CommonUtils.getDimension(R.dimen.post_source_circle_icon_size)
    val qualifiedUrl = ImageUrlReplacer.getQualifiedImageUrl(url, viewSize, viewSize)
    EntityImageUtils.loadImage(qualifiedUrl, item?.source?.nameEnglish, view, R.drawable.ic_default_hashtag)
}

@BindingAdapter("bind:entityIcon", requireAll = true)
fun bindEntityIcon(view: ImageView, item: UserFollowView?) {
    val url = item?.actionableEntity?.iconUrl
    val defaultId = if ("UGC" == item?.actionableEntity?.entitySubType) R.drawable.default_user_avatar else R.drawable.ic_default_hashtag
    view.setImageDrawable(null)

    view.visibility = View.VISIBLE
    val viewSize = CommonUtils.getDimension(R.dimen.entity_image_w_h)
    val qualifiedUrl = ImageUrlReplacer.getQualifiedImageUrl(url, viewSize, viewSize)
    EntityImageUtils.loadImage(qualifiedUrl, item?.actionableEntity?.nameEnglish, view, defaultId,true)
}

@BindingAdapter("bind:nestedCollectionIcon", requireAll = true)
fun bindNestedCollectionIcon(view: ImageView, item: CommonAsset?) {
    view.setImageDrawable(null)
    val url = item?.i_contentImageInfo()?.url ?: item?.i_thumbnailUrls()?.getOrNull(0)
    if (CardsBindUtils.showBigSourceImage(item)) {
        view.visibility = View.GONE
        return
    }

    view.visibility = View.VISIBLE
    val viewSize = CommonUtils.getDimension(R.dimen.nested_collection_source_icon_size)
    val qualifiedUrl = ImageUrlReplacer.getQualifiedImageUrl(url, viewSize, viewSize)
    EntityImageUtils.loadImage(qualifiedUrl, item?.i_source()?.nameEnglish, view, R.drawable.ic_default_hashtag)
}

@BindingAdapter("bind:userIcon", requireAll = true)
fun bindUserIcon(view: ImageView, item: CommonAsset?) {
    val url = item?.i_source()?.imageUrl
    val viewSize = CommonUtils.getDimension(R.dimen.post_source_circle_icon_size)
    val qualifiedUrl = ImageUrlReplacer.getQualifiedImageUrl(url, viewSize, viewSize)
    view.setImageResource(R.drawable.default_user_avatar)
          Image.load(qualifiedUrl)
            .apply(RequestOptions.circleCropTransform())
            .placeHolder(R.drawable.default_user_avatar)
            .into(view)
}


@BindingAdapter("bind:userIcon1", requireAll = true)
fun bindUserIcon1(view: ImageView, item: CommonAsset?) {
    val url = SSO.getLoginResponse()?.profileImage
    val viewSize = CommonUtils.getDimension(R.dimen.post_source_circle_icon_size)
    val qualifiedUrl = ImageUrlReplacer.getQualifiedImageUrl(url, viewSize, viewSize)
    view.setImageResource(R.drawable.default_user_avatar)
    Image.load(qualifiedUrl)
            .apply(RequestOptions.circleCropTransform())
            .placeHolder(R.drawable.default_user_avatar)
            .into(view)
}

@BindingAdapter("bind:userProfileImage", requireAll = true)
fun bindUserProfileImage(view: NHImageView, url: String?) {
    val viewSize = CommonUtils.getDimension(R.dimen.post_source_circle_icon_size)
    val qualifiedUrl = ImageUrlReplacer.getQualifiedImageUrl(url, viewSize, viewSize)
    Image.load(qualifiedUrl)
            .placeHolder(R.drawable.default_user_avatar)
            .into(view)
}

@BindingAdapter("bind:postbody", requireAll = true)
fun postbody(view: NHTextView, item: CreatePostEntity?) {
    view.text = Html.fromHtml(item?.text ?: "").toString()
}

/*
* Using different method as visibility logic is different than normal card thumbnail
* */
@BindingAdapter("bind:repostThumbnail", "bind:cardtype", requireAll = true)
fun loadRepostThumbnail(view: NHImageView, item: CommonAsset?, cardType: Int) {
    if (item == null)
        return
    if (item.i_format() == Format.POLL) {
        view.setImageDrawable(CommonUtils.getDrawable(R.drawable.cp_ic_poll))
        return
    }
    var url: String? = null
    if (item.i_linkAsset() != null) {
        url = item.i_linkAsset()?.thumbnailUrl
    } else {
        url = item.i_thumbnailUrls()?.getOrNull(0)
    }
    if (CommonUtils.isEmpty(url)) {
        return
    }
    val imageDimension: Pair<Int, Int>? = CardsBindUtils.getImageDimension(cardType, 0)
    imageDimension?.let {
        loadImageUrl(view, url!!, it.first, it.second)
    }
}


@BindingAdapter("bind:repostText", "bind:cardtype", requireAll = true)
fun setRepostText(view: NHTextView, item: CommonAsset?, cardType: Int) {
    val titleText: String?
    if (cardType == PostDisplayType.REPOST_OG.index) {
        titleText = item?.i_linkAsset()?.title
        view.maxLines = 2
    } else {
        titleText = listOf(item?.i_title(), item?.i_content()).find { !CommonUtils.isEmpty(it) }
    }
    if (CommonUtils.isEmpty(titleText)) {
        return
    }
    val titleConvertedText = FontHelper.getFontConvertedString(titleText)
    val s: Spannable = HtmlCompat.fromHtml(titleConvertedText, HtmlCompat.FROM_HTML_MODE_LEGACY)
            as Spannable
    for (u in s.getSpans(0, s.length, URLSpan::class.java)) {
        val ColorSpan = ForegroundColorSpan(ThemeUtils.getThemeColorByAttribute(view.context, R.attr.repost_hashtag_color))
        s.setSpan(ColorSpan,
                s.getSpanStart(u), s
                .getSpanEnd(u), 0)

        s.setSpan(InternalUrlSpan(),
                s.getSpanStart(u), s
                .getSpanEnd(u), 0)

    }
    view.setSpannableTextWithLangSpecificTypeFaceChanges(s, titleText, TextView.BufferType.NORMAL, item?.i_langCode())

}

@BindingAdapter("bind:repostOverlayText", requireAll = true)
fun getRepostOverlay(view: NHTextView, item: CommonAsset?) {
    if (item == null)
        return
    val repostAsset: CommonAsset = item
    if (repostAsset.i_thumbnailUrls()?.size ?: 0 > 1) {
        view.setSpannableTextWithLangSpecificTypeFaceChanges(("""+ ${(repostAsset.i_thumbnailUrls()!!.size - 1)}"""),BufferType.SPANNABLE, item?.i_langCode() )
        view.visibility = View.VISIBLE
    } else {
        view.visibility = View.GONE
    }
}

@BindingAdapter("bind:sourceBigImg", requireAll = true)
fun bindSourceBigImg(view: ImageView, item: CommonAsset?) {
    if (item == null)
        return
    val url = item.i_source()?.entityImageUrl
    if (!CardsBindUtils.showBigSourceImage(item)) {
        view.visibility = View.GONE
        return
    }
    view.visibility = View.VISIBLE
    val viewHeight = CommonUtils.getDimension(R.dimen.post_source_rectangle_img_height)
    val maxViewWidth = CommonUtils.getDimension(R.dimen.post_source_rectangle_img_max_width)
    val qualifiedUrl = ImageUrlReplacer.getQualifiedImageUrl(url, maxViewWidth, viewHeight)
    Image.load(qualifiedUrl).apply(RequestOptions().dontTransform()).into(view,ImageView.ScaleType.FIT_START)
}

@BindingAdapter("bind:sourceBigImg", requireAll = true)
fun bindSourceBigImg(view: ImageView, item: DetailListCard?) {
    if (item == null)
        return

    val url = item.source?.entityImageUrl
    if (!CardsBindUtils.showBigSourceImage(item)) {
        view.visibility = View.GONE
        return
    }
    view.visibility = View.VISIBLE
    val viewHeight = CommonUtils.getDimension(R.dimen.post_source_rectangle_img_height)
    val maxViewWidth = CommonUtils.getDimension(R.dimen.post_source_rectangle_img_max_width)
    val qualifiedUrl = ImageUrlReplacer.getQualifiedImageUrl(url, maxViewWidth, viewHeight)
    Image.load(qualifiedUrl).apply(RequestOptions().dontTransform()).into(view,ImageView.ScaleType.FIT_START)
}

@BindingAdapter("bind:text_hideIfEmpty", "bind:forcehide", "bind:item", requireAll = true)
fun setTextHideIfEmpty(view: NHTextView, text: String? = null, forcehide: Boolean, item: Any?) {
    item?:return
    if (CommonUtils.isEmpty(text) || forcehide) {
        view.visibility = View.GONE
        return
    }
    view.visibility = View.VISIBLE

    var languageCode: String? = null
        if((item as? CommonAsset) != null){
            languageCode = item.i_langCode()
        }else if((item as? BaseDetailList) != null){
            languageCode = item.i_langCode()
        }
        view.setSpannableTextWithLangSpecificTypeFaceChanges(text, BufferType.SPANNABLE, languageCode)
}

@BindingAdapter("bind:text_commentCount", "bind:replyCount", requireAll = true)
fun setCommentReplyCount(view: TextView, discussion: CommonAsset? = null, replyCount: Int) {
    val commentCount = discussion?.i_counts()?.COMMENTS?.value?: "0"
    val commentCountVal = Integer.parseInt(commentCount) + replyCount
    if (commentCountVal > 0) {
        val replyCountString = CommonUtils.getQuantifiedString(R.plurals.q_c_replies,
                commentCountVal, commentCountVal)

        view.visibility = View.VISIBLE
        view.text = replyCountString
        return
    }

    view.visibility = View.GONE
}

@BindingAdapter("bind:text_commentCountInt", requireAll = true)
fun setCommentReplyCount(view: TextView, replyCount: Int = 0) {
    if (replyCount > 0) {
        val replyCountString = CommonUtils.getQuantifiedString(R.plurals.reply_count_display,
                replyCount, replyCount)
        view.visibility = View.VISIBLE
        view.text = replyCountString
        return
    }

    view.visibility = View.GONE
}

/*
* If height and width not present than pass expected height and width for image if not required
* pass 0. overloading will cause issue if complex functionality added in this function later.
* */
@BindingAdapter("bind:imageUrl", "bind:img_width", "bind:img_height", requireAll = true)
fun loadImageUrl(view: ImageView, url: String?, width: Int, height: Int) {
    if (!CommonUtils.isEmpty(url)) {
        val qualifiedUrl = ImageUrlReplacer.getQualifiedImageUrl(url, width,
                height)
        CustomLoader(qualifiedUrl).apply(RequestOptions().dontTransform()).into(view)
    }
}


@BindingAdapter("bind:cp_repost_OverlayIfMultiImages", requireAll = true)
fun bindCPRepostOverlayIfMultiImages(view: NHTextView, item: CommonAsset?) {
    if(item == null){
        view.visibility = View.GONE
    }
    when {
        item?.i_totalThumbnails() ?: 0 > 1 -> {
            view.text = """+ ${(item?.i_totalThumbnails())}"""
            view.visibility = View.VISIBLE
        }
        item?.i_thumbnailUrls()?.size ?: 0 > 1 -> {
            view.text = """+ ${(item?.i_thumbnailUrls()!!.size - 1)}"""
            view.visibility = View.VISIBLE
        }
        else -> view.visibility = View.GONE
    }
}

@BindingAdapter("bind:cp_repost_HtmlText", requireAll = true)
fun bindCPRepostHtmlText(view: NHTextView, item: CommonAsset?) {
    if(item != null){
        var htmlText: String = item.i_title() ?: Constants.EMPTY_STRING
        if (CommonUtils.isEmpty(htmlText)) htmlText = item.i_content() ?: return
        val s: Spannable = HtmlCompat.fromHtml(FontHelper.getFontConvertedString(htmlText), HtmlCompat
                .FROM_HTML_MODE_LEGACY) as Spannable
        view.setSpannableText(s, htmlText)
    }
}

/*
* Using different method as visibility logic is different than normal card thumbnail
* */
@BindingAdapter("bind:cp_repost_Thumbnail", "bind:cardtype", requireAll = true)
fun bindCPRepostThumbnali(view: NHImageView, item: CommonAsset?, cardType: Int) {
    if (item == null) return
    when (cardType) {
        RepostDisplayType.REPOST_OEMBED.index -> {
            if (item.i_linkAsset() != null) { val url = item.i_linkAsset()?.thumbnailUrl
                val imageDimension: Pair<Int, Int>? = CardsBindUtils.getImageDimension(cardType, 0)
                imageDimension?.let {
                    loadImageUrl(view, url, it.first, it.second)
                }
                return
            } else {
                // dont do nothing
                return
            }
        }
        RepostDisplayType.REPOST_HERO.index,
        RepostDisplayType.REPOST_NORMAL.index -> {
            if (item.i_thumbnailUrls() != null) {
                val url = item.i_thumbnailUrls()?.getOrNull(0)
                if (CommonUtils.isEmpty(url)) return
                val imageDimension: Pair<Int, Int>? = CardsBindUtils.getImageDimension(cardType, 0)
                imageDimension?.let {
                    loadImageUrl(view, url, it.first, it.second)
                }
                return
            } else {
                return
            }
        }
        RepostDisplayType.REPOST_POLL.index -> {
            view.setImageDrawable(CommonUtils.getDrawable(R.drawable.cp_ic_poll))
        }
    }
}


@BindingAdapter("bind:entity_icon_image", "bind:cardtype", requireAll = true)
fun bindEntityImageCircle(view: NHImageView, item: EntityItem?, cardType: Int?) {
    item ?: return
    cardType ?: return
    view.setImageDrawable(null)
    val url = item.i_iconUrl()?:item.i_imageUrl()
    val dimen = CardsBindUtils.getImageDimensionForEntityIcon(cardType)
    dimen?.let {
        val qualifiedUrl = ImageUrlReplacer.getQualifiedImageUrl(url, it.first, it.second)
        EntityImageUtils.loadImage(qualifiedUrl, item.i_nameEnglish(), view, R.drawable.ic_default_hashtag)
        return
    }
}

@BindingAdapter("bind:entity_banner_image", "bind:cardtype", "bind:entity_parent_item",
        requireAll = true)
fun bindEntityBannerImage(view: NHRoundedCornerImageView, item: EntityItem?, cardType: Int?, parent: CommonAsset?) {
    item ?: return
    cardType ?: return
    view.setImageDrawable(null)
    val url = (item.i_entityImageUrl() ?: item.i_imageUrl()) ?: item.i_iconUrl()
    view.setFitType(FIT_TYPE.TOP_CROP)
    val dimen = CardsBindUtils.getImageDimensionForEntityBannerImage(cardType, parent)
    dimen?.let {
        val qualifiedUrl = ImageUrlReplacer.getQualifiedImageUrl(url, it.first, it.second)
        if (url.isNullOrEmpty()) {
            if (item.i_entityType() == ColdStartEntityType.COMMUNITY_GROUP.name) {
                view.setImageResource(R.drawable.ic_group_image)
                return
            }
        }
        EntityImageUtils.loadImage(qualifiedUrl, item.i_nameEnglish(), view, R.drawable.ic_default_hashtag, false)
    }
}

@BindingAdapter("bind:entity_item", "bind:parent_item", requireAll = true)
fun bindEntityItemActionText(view: NHTextView, item: EntityItem?, parent: CommonAsset?) {
    item ?: return
    parent ?: return
    if (item.i_entityType() == ColdStartEntityType.COMMUNITY_GROUP.name) {
        if (item.i_selected()) {
            if (item.i_memberApproval() == SettingState.NOT_REQUIRED)
                view.text = CommonUtils.getString(R.string.joined)
            else
                view.text = CommonUtils.getString(R.string.requested)
        } else {
            view.text = parent.i_unSelectText()
        }
    } else {
        if (item.i_selected()) {
            view.text = parent.i_selectText()
        } else {
            view.text = parent.i_unSelectText()
        }
    }
}

@BindingAdapter("bind:entity_bg_item", "bind:parent_bg_item", requireAll = true)
fun bindEntityBackgroundColor(view: ConstraintLayout, item: EntityItem?, parent: CommonAsset?) {
    item ?: return
    parent ?: return

    if (FollowActionType.BLOCK.name == parent.i_coldStartAsset()?.actionType) {

        view.background = if (item.i_selected()) {
            CommonUtils.getDrawable(R.drawable.blocked_btn_bg)

        } else {
            CommonUtils.getDrawable(R.drawable.block_grey_bg_outlined)        }

    } else {
        if (item.i_selected()) {
            view.background = CommonUtils.getDrawable(R.drawable.following_btn_bg)
        } else {
            view.setBackgroundColor(ThemeUtils.getThemeColorByAttribute(view.context,R.attr.new_default_text_color))
        }
    }
}

@BindingAdapter("bind:text_entity_item", "bind:text_parent_item", requireAll = true)
fun bindEntityTextColor(view: NHTextView, item: EntityItem?, parent: CommonAsset?) {
    item ?: return
    parent ?: return

    if (FollowActionType.BLOCK.name == parent.i_coldStartAsset()?.actionType) {
        if (item.i_selected()) {
            view.setTextColor(CommonUtils.getColor(R.color.following_button_text_color))
        } else {
            view.setTextColor(
                ThemeUtils.getThemeColorByAttribute(
                    view.context,
                    R.attr.block_button_text_color
                )
            )
        }
    } else {
        if (item.i_selected()) {
            view.setTextColor(CommonUtils.getColor(R.color.following_button_text_color))

        } else {
            view.setTextColor(
                ThemeUtils.getThemeColorByAttribute(
                    view.context,
                    R.attr.follow_button_text_color
                ))
        }
    }
}

@BindingAdapter("bind:image_entity_item", "bind:image_parent_item", requireAll = true)
fun bindEntityActionIcon(view: NHImageView, item: EntityItem?, parent: CommonAsset?) {
    item ?: return
    parent ?: return
    if ((FollowActionType.FOLLOW.name == parent.i_coldStartAsset()?.actionType) || (parent.i_coldStartAsset()?.actionType == null)) {
        view.setImageDrawable(CommonUtils.getDrawable(R.drawable.ic_follow_star))
        if (item.i_selected()) {
            view.visibility = View.GONE

        } else {
            view.visibility = View.VISIBLE
        }
    }
    else {
        view.visibility = View.GONE
    }
}

@BindingAdapter("bind:isProfileFollowing", requireAll = true)
fun bindNerFollowBackgroundColor(view: LinearLayout, isFollowing: Boolean) {
        if (isFollowing) {
            view.background = CommonUtils.getDrawable(R.drawable.following_btn_bg)
        } else {
            view.setBackgroundColor(ThemeUtils.getThemeColorByAttribute(view.context,R.attr.new_default_text_color))
        }
}

@BindingAdapter("bind:handleImageUrl")
fun loadHandleImageUrl(view: ImageView, url: String?) {
    if(url.isNullOrEmpty()) {
        view.setImageResource(R.drawable.vector_user_avatar)
        return
    }
    val requestOptions = RequestOptions().placeholder(R.drawable.vector_user_avatar)
            .error(R.drawable.vector_user_avatar)
    Image.load(ImageUrlReplacer.getQualifiedImageUrl(url, 40, 40)).apply(requestOptions).into(view)
}

@BindingAdapter("bind:menuL1Icon", "bind:isNightMode", requireAll = true)
fun loadMenuIcon(view: ImageView, menuOption: MenuOption?, isNightMode: Boolean?) {
    val iconUrl: String? = if (isNightMode == true) {
        menuOption?.menuL1?.nIcon
    } else {
        menuOption?.menuL1?.icon
    }
    val dimension = CardsBindUtils.getMenuOptionL1IconSize()
    val qualifiedUrl = ImageUrlReplacer.getQualifiedImageUrl(iconUrl, dimension.first,
            dimension.second)
    Image.load(qualifiedUrl).apply(RequestOptions().dontTransform()).into(view)
}


@BindingAdapter("bind:astro_description", requireAll = true)
fun loadAstroDescription(view: NHTextView, item: CommonAsset?) {
    val content = item?.i_content()
    if (!DataUtil.isEmpty(content)) {
        AndroidUtils.truncateAndSetTextFromHtml(
            view, content,
            NewsConstants.STORY_1ST_CHUNK_MAX_CHARS,
            item?.i_langCode())
    } else {
        view.text = Constants.EMPTY_STRING
    }
}

@BindingAdapter("bind:activity_time", requireAll = true)
fun formatUserInteractionTime(view: NHTextView, item: CommonAsset?) {
    //Need to reset the text incase of view reuse
    view.text = Constants.EMPTY_STRING
    item?.i_userInteractionAsset()?.let { userInteraction ->
        view.text = timeFormat.format(userInteraction.activityTime)
        return
    }

    item?.i_publishTime()?.let {
        view.text = timeFormat.format(it)
    }
}

class ObservableDataBinding<T> : BaseObservable() {
    var value: T? = null
        @Bindable
        get
        set(value) {
            field = value
            notifyPropertyChanged(BR.value)
        }
}

@BindingAdapter("bind:post_create_title", requireAll = true)
fun postCreateScreenTitle(view: TextView, mode: CreatePostUiMode) {
    when (mode) {
        CreatePostUiMode.COMMENT -> {
            view.text = CommonUtils.getString(R.string.comments_fragment_name)
        }
        CreatePostUiMode.REPOST -> {
            view.text = CommonUtils.getString(R.string.repost)
        }
        CreatePostUiMode.REPLY -> {
            view.text = CommonUtils.getString(R.string.reply)
        }
        else -> {
            view.text = CommonUtils.getString(R.string.create_post)
        }
    }
}

@BindingAdapter("bind:userInteractionTitle", requireAll = true)
fun bindUserInteractionTitle(view: NHTextView, asset: CommonAsset?) {
    asset?.let {
        val titleText = listOf(it.i_userInteractionAsset()?.htmlTitle, it.i_title(), it.i_content())
                .find {
                    !it.isNullOrEmpty()
                }
        bindHtmlText(view, titleText)
    }
}

@BindingAdapter("bind:userInteractionThumbnail", "bind:showNsfw", requireAll = true)
fun bindUserInteractionThumbnail(view: NHRoundedCornerImageView, asset: CommonAsset?, showNsfwFilter: Boolean) {
    asset?.let {
        if (showNsfwFilter) {
            view.setImageResource(R.drawable.dailyshare_activity_vector)
            return
        } else if (Format.POLL == asset.i_format()) {
            view.setImageResource(R.drawable.ic_poll_thumbnail)
            return
        }
        return loadThumbnail(view, asset, PostDisplayType.USER_INTERACTION.index, 0, R.drawable.ic_generic_icon)
    }
}

@BindingAdapter("bind:item", "bind:title", requireAll = true)
fun bindSavedCarouselItemCount(view: NHTextView, item: CommonAsset, title: String?) {
    val count = item.i_counts()?.STORY?.value?.toIntOrNull()
            ?:item.i_collectionItems()?.size
    if(count == null) {
        view.text = title
    }
    else {
        view.text = title.plus(Constants.OPENING_BRACES).plus(count).plus(Constants.CLOSING_BRACES)
    }
}

@BindingAdapter("bind:isSelected", requireAll = true)
fun bindImageSelected(view: NHImageView, value: Boolean?) {
    view.isSelected = value ?: false
}

@BindingAdapter("bind:isSelected", requireAll = true)
fun bindTextSelected(view: NHTextView, value: Boolean?) {
    view.isSelected = value ?: false
}

/*We do not need any argument but share is very general case therefore we need binding adapter to
 invoke by itself without concerning where it is getting inflated*/
@BindingAdapter("bind:shareIcon", "bind:defaultShareChange", "bind:top","bind:inDetail", requireAll = false)
fun bindShareIcon(view: TextView, isInCommentBar: Boolean? = null, defaultShareChange: MutableLiveData<InstalledAppInfo>?, top: Boolean?,inDetail:Boolean?) {
    val tag: Bundle? = view.getTag(R.id.share_click_argument_tag) as Bundle?
    val packageName: String? = tag?.getString(Constants.BUNDLE_SHARE_PACKAGE_NAME)
    if (view.getTag(R.id.share_click_argument_tag) != null && packageName.equals(defaultShareChange?.value?.packageName)) {
        /*No need to bind icon if it is already bound*/
        return
    }
    CardsBindUtils.setupShareIcon(view, top ?: false,isInCommentBar?:false,inDetail?:false)
}

@BindingAdapter("bind:approvalCounts", requireAll = true)
fun bindApprovalCount(view: NHTextView, counts: ApprovalCounts?) {
    view.text = counts?.let {
        it.TOTAL_PENDING_APPROVALS?.value ?: Constants.ZERO_STRING
    } ?: Constants.ZERO_STRING
}

@BindingAdapter("bind:item", requireAll = true)
fun loadImageUrl1(view: ImageView, item: CommonAsset?) {
    var width = item?.i_videoAsset()?.width ?: 0
    var height = item?.i_videoAsset()?.height ?: 0
    if(width == 0 || height == 0) {
        width = CommonUtils.getDeviceScreenWidth()
        height = width * 9 / 16
    }
    val imageDetail = item?.i_thumbnailUrlDetails()?.getOrNull(0)
    if (!CommonUtils.isEmpty(imageDetail?.url)) {
        val qualifiedUrl = ImageUrlReplacer.getQualifiedImageUrl(imageDetail?.url, width, height)
        CustomLoader(qualifiedUrl).apply(RequestOptions().dontTransform()).into(view)
    }
}

@BindingAdapter("bind:commentRepostCard", "bind:vm", requireAll = true)
fun bindCommentRepostFeedView(view: RecyclerView, item: CommonAsset?, vm: CardsViewModel) {
    if (view.adapter == null) {
        view.adapter = CommentsRepostFeedCardAdapter(vm)
        view.layoutManager = LinearLayoutManager(view.context, RecyclerView.VERTICAL, false)
    }
    view.isNestedScrollingEnabled = false
    (view.adapter as CommentsRepostFeedCardAdapter).setItems(item?.i_commentRepostItems())
    (view.adapter as CommentsRepostFeedCardAdapter).setParent(item)
}

@BindingAdapter("bind:commentRepostSourceImage", requireAll = true)
fun bindCommentRepostSourceImage(view: NHImageView, item: CommonAsset?) {
    val url = item?.i_source()?.imageUrl
    val viewSize = CommonUtils.getDimension(R.dimen.comment_repost_card_profile_icon_w_h)
    val qualifiedUrl = ImageUrlReplacer.getQualifiedImageUrl(url, viewSize, viewSize)
    Image.load(qualifiedUrl)
            .placeHolder(R.drawable.default_user_avatar)
            .into(view)
}

@BindingAdapter(("bind:sourceIconVisibility"))
fun bindSourceIconVisibility(view: View,isNestedCollection:Boolean):Boolean {
    val isVisible = PreferenceManager.getPreference(AppStatePreference.SHOW_SOURCE_LOGO_AT_CARD_LEVEL,Constants.SHOW_SOURCE_LOGO_AT_CARD_LEVEL)
    view.visibility = if (isVisible && !isNestedCollection) View.VISIBLE else View.GONE
    return isVisible
}

@BindingAdapter(("bind:sourceCOCIconVisibility"))
fun bindCOCSourceIconVisibility(view: View,item:CommonAsset?) {
    if(bindSourceIconVisibility(view,false) && (item?.i_source()?.entityType?.equals(EntityType.SOURCE.name) == true)) {
        view.visibility = View.VISIBLE
    } else {
        view.visibility = View.GONE
    }
}

@BindingAdapter("bind:icon", "bind:rank", "bind:isLive",  "bind:defaultShareChange","bind:isNewsBrief",requireAll = false)
fun bindIcon(view: NHTextView, item: CommonAsset?, rank: Int, isLive: Boolean?, defaultShareChange: MutableLiveData<InstalledAppInfo>?, isNewsBrief:Boolean?= false ) {
    bindIconWithCOC(view,item,rank,isLive,defaultShareChange,isNewsBrief)
}

@BindingAdapter("bind:icon", "bind:rank", "bind:isLive",  "bind:defaultShareChange","bind:isNewsBrief","bind:isNestedCollection",requireAll = false)
fun bindIconWithCOC(view: NHTextView, item: CommonAsset?, rank: Int, isLive: Boolean?, defaultShareChange: MutableLiveData<InstalledAppInfo>?, isNewsBrief:Boolean?= false,isNestedCollection: Boolean?=false ) {
    item ?: return
    val json = PreferenceManager.getPreference(
        AppStatePreference.ICONS_CONFIG_FEED_CARD,
        Constants.EMPTY_STRING
    )
    if(isNestedCollection == true) {
        view.setPadding(0,view.paddingTop,0,view.paddingBottom)
    }
    val type = object : TypeToken<List<IconsList>?>() {}.type
    val iconsConfig: List<IconsList>? = JsonUtils.fromJson(json, type)
//    var iconsConfig: Array<IconsList>? = arrayOf(IconsList.REPOST, IconsList.REACTION, IconsList
//        .WA_SHARE, IconsList.COMMENT, IconsList.SAVE)

    iconsConfig?.let {
        if (rank > it.size)
            return
    }

    iconsConfig?.filter {
        it != IconsList.SAVE
    }?.let {
        if (it.isEmpty() || rank >= it.size) {
            return
        }
        when (it[rank]) {
            IconsList.WA_SHARE -> {
                view.isEnabled = CardsBindUtils.isViewEnabledPrivacy(item)
                CardsBindUtils.countsText(item.i_counts()?.SHARE?.value)?.let { count ->
                    view.compoundDrawablePadding = CommonUtils.getDimension(R.dimen.share_icons_drawable_padding)
                    view.setSpannableTextWithLangSpecificTypeFaceChanges(count, BufferType.SPANNABLE, item.i_langCode())
                }
                val tag: Bundle? = view.getTag(R.id.share_click_argument_tag) as Bundle?
                val packageName: String? = tag?.getString(Constants.BUNDLE_SHARE_PACKAGE_NAME)
                if (view.getTag(R.id.share_click_argument_tag) != null && packageName.equals(defaultShareChange?.value?.packageName)) {
                    /*No need to bind icon if it is already bound*/
                    return
                }
                CardsBindUtils.setupShareIcon(view, false, false,false,isNewsBrief)
                view.setTag(view.id, IconsList.WA_SHARE)
            }
            IconsList.COMMENT -> {
                setCompoudDrawable(
                    view, start = ThemeUtils.getThemeDrawableByAttribute(
                        view.context, R.attr.commment_icon, R.drawable.comment_vector
                    )
                )
                view.isEnabled = CardsBindUtils.isCommentsEnabled(item)
                CardsBindUtils.countsText(item.i_counts()?.COMMENTS?.value)?.let { count ->
                    view.compoundDrawablePadding = CommonUtils.getDimension(R.dimen.share_icons_drawable_padding)
                    view.setSpannableTextWithLangSpecificTypeFaceChanges(count, BufferType.SPANNABLE, item.i_langCode())
                }

                view.setTag(view.id, IconsList.COMMENT)
            }
            IconsList.REACTION -> {
                if(isLive != true) {
                    setCompoudDrawable(
                        view, start = LikeEmojiBindingUtils.getEmojiIconResource(
                            item.i_selectedLikeType(), view.context, false
                        )
                    )
                    CardsBindUtils.countsText(item.i_counts()?.TOTAL_LIKE?.value)?.let { count ->
                        view.compoundDrawablePadding = CommonUtils.getDimension(R.dimen.share_icons_drawable_padding)
                        view.setSpannableTextWithLangSpecificTypeFaceChanges(count, BufferType.SPANNABLE, item.i_langCode())
                    }
                    view.setTag(view.id, IconsList.REACTION)
                }
            }
            IconsList.REPOST -> {
                if(isLive != true) {
                    view.isEnabled = CardsBindUtils.isViewEnabledPrivacy(item)
                    CardsBindUtils.countsText(item.i_counts()?.REPOST?.value)?.let { count ->
                        view.compoundDrawablePadding = CommonUtils.getDimension(R.dimen.share_icons_drawable_padding)
                        view.setSpannableTextWithLangSpecificTypeFaceChanges(count, BufferType.SPANNABLE, item.i_langCode())
                    }
                    setCompoudDrawable(
                        view, start = ThemeUtils.getThemeDrawableByAttribute(
                            view.context, R.attr.repost_icon, R.drawable.repost_vector
                        )
                    )
                    view.setTag(view.id, IconsList.REPOST)
                }
            }
            else -> {
            }
        }
    }
}

@BindingAdapter(
        "bind:cardDisplayTypeIndex",
        "bind:hideViewOnEmpty",
        "bind:cropCircle",
        "bind:card",
        "bind:extraCard",
        "bind:imageUrl",
        "bind:imageIndex",
        requireAll = false)
fun setDetailImage(view: NHImageView,
                   cardDisplayTypeIndex: Int? = null,
                   hideViewOnEmptyUrl: Boolean? = null,
                   circleCrop: Boolean? = null,
                   card: CommonAsset? = null,
                   booStrapCard: DetailListCard?,
                   imageUrl: String? = null,
                   imageIndex: Int? = null) {
    val requestOptions = RequestOptions().dontTransform()

    var url = imageUrl ?: card?.i_contentImageInfo()?.url
    if (url == null) {
        url = booStrapCard?.imageDetails?.url
    }

    if (CommonUtils.isEmpty(url)) {
        if (hideViewOnEmptyUrl == true) {
            view.visibility = View.GONE
        } else {
            view.setImageResource(R.drawable.default_stry_detail_img)
        }
        return
    }
    val imageDetail = card?.i_contentImageInfo()
    if(imageDetail!=null && (imageDetail.height==0f || imageDetail.width==0f)){
        AnalyticsHelper.logDevErrorEvent("Invalid dimension of image : item id : ${card.i_id()} " +
                "iw : ${imageDetail.width} ih : ${imageDetail.height}")
        view.visibility = View.GONE
        return
    }

    if (cardDisplayTypeIndex == DetailCardType.IMAGE_DYNAMIC.index) {
        view.setFitType(FIT_TYPE.FIT_CENTER)
        val params = view.layoutParams
        val screenWidth = CommonUtils.getDeviceScreenWidth()
        val imageHeight : Float  = card?.i_contentImageInfo()?.height ?: 0.0F
        val imageWidth : Float = card?.i_contentImageInfo()?.width ?: 0.0F
        val maxHeight = CommonUtils.getDeviceScreenHeight() - getActionBarHeight(view.context)
        if (imageWidth > 0.0 && imageHeight > 0.0) {
            var heightBasednImageInfo = (imageHeight * screenWidth) / imageWidth
            if (heightBasednImageInfo > maxHeight) {
               heightBasednImageInfo = maxHeight.toFloat()
            }

            params.height = heightBasednImageInfo.toInt()
            view.visibility = View.VISIBLE
        }
        else {
            AnalyticsHelper.logDevErrorEvent("HERO_DYNAMIC_ZERO_" + card?.i_id())
            view.visibility = View.GONE
        }

        view.layoutParams = params
        view.maxHeight = maxHeight
    }
    else {
        view.setFitType(FIT_TYPE.TOP_CROP)
    }

    view.visibility = View.VISIBLE

    if (cardDisplayTypeIndex != null) {
        val imgIndex = imageIndex ?: 0
        val imagaeDimension: Pair<Int, Int>? = DetailsBindUtils.getImageDimension(cardDisplayTypeIndex, imgIndex)
        imagaeDimension?.let {
            val qualifiedUrl = ImageUrlReplacer.getQualifiedImageUrl(url, imagaeDimension.second,
                    imagaeDimension.first)

            view.minimumHeight = imagaeDimension.first

            Image.load(qualifiedUrl)
                    .apply(requestOptions)
                    .placeHolder(R.color.empty_image_color)
                    .into(view)
            return
        }
    }

    Image.load(url)
            .apply(requestOptions)
            .placeHolder(R.color.empty_image_color)
            .into(view)
}

private fun getActionBarHeight(context: Context): Int {
    if (context is Activity) {
        val typedValue = TypedValue()
        context.theme?.resolveAttribute(R.attr.actionBarSize, typedValue, true)
        return TypedValue.complexToDimensionPixelSize(
                typedValue.data, context.resources.displayMetrics)
    }

    return 0
}

@BindingAdapter("bind:item", "bind:textview_text", requireAll = true)
fun bindTextDetail(view: NHTextView, item: CommonAsset?, textViewText: CharSequence?) {
    view.setSpannableTextWithLangSpecificTypeFaceChanges(textViewText, BufferType.NORMAL, item?.i_langCode())
}

@BindingAdapter("bind:item", "bind:coc_item_textview_text", requireAll = true)
fun bindCOCItemText(view: NHTextView, item: CommonAsset?, textViewText: CharSequence?) {
    if (item?.i_isRead() == true) {
        view.setTextColor(CommonUtils.getColor(R.color.story_card_title_read_text_color))
    }
    view.setSpannableTextWithLangSpecificTypeFaceChanges(textViewText, BufferType.SPANNABLE, item?.i_langCode())
}

@BindingAdapter("bind:item", "bind:textview_coll_text", requireAll = true)
fun bindTextCollectionDetail(view: NHTextView, item: CommonAsset?, textViewText: CharSequence?) {
    if(textViewText.isNullOrEmpty()) {
        view.visibility = View.GONE
        return
    }
    view.visibility = View.VISIBLE
    view.setSpannableTextWithLangSpecificTypeFaceChanges(textViewText, BufferType.NORMAL, item?.i_langCode())
}

@BindingAdapter("bind:profileImage")
fun loadProfileImage(view: NHImageView, item: EntityInfoList?) {
    if (CommonUtils.isEmpty(item?.parent?.pageEntity?.header?.logoUrl) || item?.parent?.pageEntity?.header?.hideMastHead == true) {
        // in case then banner image url is null or hideMast is true then do not display the banner
        //view.visibility = View.GONE
        EntityImageUtils.loadImage(null, item?.parent?.nameEnglish(), view, R.drawable.ic_default_hashtag)
        return
    }
    val size = CommonUtils.getDimension(R.dimen.source_image_width_height)
    view.visibility = View.VISIBLE
    val url = ImageUrlReplacer.getQualifiedImageUrl(item?.parent?.pageEntity?.header?.logoUrl,size, size)
    EntityImageUtils.loadImage(url, item?.parent?.nameEnglish(), view, R.drawable.ic_default_hashtag)
}

@BindingAdapter("bind:bannerImage")
fun loadBannerImage(view: NHImageView, item: EntityInfoList?) {
    if (CommonUtils.isEmpty(item?.parent?.pageEntity?.header?.bannerImageUrl) || item?.parent?.pageEntity?.header?.hideMastHead == true) {
        // in case then banner image url is null or hideMast is true then do not display the banner
        view.visibility = View.GONE
        return
    }
    view.visibility = View.VISIBLE
    Image.load(item?.parent?.pageEntity?.header?.bannerImageUrl, true).
    placeHolder(R.drawable.default_stry_detail_img).into(view)
}

@BindingAdapter("bind:entityToolbar")
fun updateToolbar(view: Toolbar, item: EntityInfoList?) {
    if (CommonUtils.isEmpty(item?.parent?.pageEntity?.header?.bannerImageUrl) || item?.parent?.pageEntity?.header?.hideMastHead == true) {
        // in case then banner image url is null or hideMast is true then do not display the banner
        view.setBackgroundColor(CommonUtils.getColor(R.color.transparent))
        if(ThemeUtils.isNightMode()) {
            view.findViewById<ImageView>(R.id.actionbar_back_button).setImageResource(R.drawable.ic_back_arrow_white)
            view.findViewById<NHImageView>(R.id.dislike_icon).setImageResource(R.drawable.ic_white_3_dots)
        } else {
            view.findViewById<ImageView>(R.id.actionbar_back_button).setImageResource(R.drawable.ic_back_arrow)
            view.findViewById<NHImageView>(R.id.dislike_icon).setImageResource(R.drawable.ic_3_dots_black)
        }
    } else {
        if (item?.parent?.isHeaderProfile() == true) {
            view.setBackgroundResource(ThemeUtils.getThemeDrawableByAttribute(view.context,
                R.attr.presearch_list_bg, R.drawable.layout_entity_gradient))
        } else {
            view.setBackgroundResource(R.drawable.layout_entity_gradient)
            view.findViewById<ImageView>(R.id.actionbar_back_button).setImageResource(R.drawable.ic_back_arrow_white)
            view.findViewById<NHImageView>(R.id.dislike_icon).setImageResource(R.drawable.ic_white_3_dots)
        }
    }
}

@BindingAdapter("bind:followText")
fun updateFollowText(view: NHTextView, entity: PageEntity?) {
    if (entity?.counts == null) {
        view.visibility = View.GONE
        return
    }

    val text = UiUtils.getStringForEntityText(entity.counts?.FOLLOW?.value, entity.counts?.STORY?.value)
    if (CommonUtils.isEmpty(text)) {
        view.visibility = View.GONE
    } else {
        view.visibility = View.VISIBLE
        view.text = text
    }



}

@BindingAdapter("bind:nerBannerImage")
fun loadNERBannerImage(view: NHImageView, item: EntityInfoList?) {

    if (item?.parent?.pageEntity?.header?.bannerImageUrl?.isEmpty() == true) {
        // in case then banner image url is null or hideMast is true then do not display the banner
        view.visibility = View.GONE
        return
    }
    view.visibility = View.VISIBLE
    Image.load(item?.parent?.pageEntity?.header?.bannerImageUrl, true).
    placeHolder(R.drawable.default_stry_detail_img).into(view)
}

@BindingAdapter("bind:NERDescText")
fun loadNERDescText(view: NHTextView, item: EntityInfoList?) {
    item?.parent?.pageEntity?.description?.let{
        view.text = it
    }
}


fun getbindLinkableTextDetail(
    item: CommonAsset?, parent: CommonAsset?, booStrapCard: DetailListCard?
): CharSequence {


    var titleText =
        parent?.i_title() ?: parent?.i_content() ?: item?.i_title() ?: booStrapCard?.title
        ?: item?.i_content()
    val itemLang = parent?.i_langCode() ?: item?.i_langCode() ?: booStrapCard?.i_langCode()

    if (titleText.isNullOrEmpty()) {
        return Constants.EMPTY_STRING
    }

    val s: Spannable = HtmlCompat.fromHtml(titleText, HtmlCompat.FROM_HTML_MODE_LEGACY) as Spannable

    return titleText
}