/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.news.helper

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.newshunt.adengine.listeners.InteractiveAdListener
import com.newshunt.adengine.listeners.ReportAdsMenuListener
import com.newshunt.adengine.model.entity.ContentAdDelegate
import com.newshunt.adengine.view.AdEntityReplaceHandler
import com.newshunt.adengine.view.helper.AdsViewHolderFactory
import com.newshunt.adengine.view.viewholder.NativeAdHtmlViewHolder
import com.newshunt.appview.BR
import com.newshunt.appview.R
import com.newshunt.appview.common.entity.CardsPojo
import com.newshunt.appview.common.ui.helper.ObservableDataBinding
import com.newshunt.appview.common.viewmodel.CardsViewModel
import com.newshunt.appview.databinding.*
import com.newshunt.common.helper.common.NHWebViewUtils
import com.newshunt.common.view.customview.HeightAwareWebView
import com.newshunt.common.view.customview.NHRoundedCornerImageView
import com.newshunt.common.view.customview.NhWebView
import com.newshunt.common.view.view.BaseSupportFragment
import com.newshunt.dataentity.common.asset.AssetType2
import com.newshunt.dataentity.common.asset.CommonAsset
import com.newshunt.dataentity.common.asset.DetailListCard
import com.newshunt.dataentity.common.asset.Format
import com.newshunt.dataentity.common.asset.SubFormat
import com.newshunt.dataentity.common.asset.UiType2
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.common.model.entity.BaseError
import com.newshunt.dataentity.common.model.entity.server.asset.NewsAppJS
import com.newshunt.dataentity.news.model.entity.DetailCardType
import com.newshunt.dhutil.helper.AppSettingsProvider
import com.newshunt.dhutil.helper.theme.ThemeUtils
import com.newshunt.helper.ImageUrlReplacer
import com.newshunt.news.view.fragment.PostActions
import com.newshunt.news.view.fragment.PostWebViewClient
import com.newshunt.news.view.viewholder.NHRecyclerViewHolder
import com.newshunt.news.viewmodel.DetailsViewModel
import com.newshunt.viral.utils.visibility_utils.VisibilityCalculator


class DetailAdapterHelper(private val vm: DetailsViewModel,
                          private val cardsViewModel: CardsViewModel,
                          private val postListener: PostActions,
                          private val fragment: BaseSupportFragment,
                          private val parentLifecycleOwner: LifecycleOwner,
                          private val uniqueRequestId: Int,
                          private val deeplinkUrl: String?,
                          private val adEntityReplaceHandler: AdEntityReplaceHandler,
                          private val interactiveAdListener: InteractiveAdListener,
                          private val section: String,
                          private val webCacheProvider: NativeAdHtmlViewHolder.CachedWebViewProvider,
                          private val detailList: RecyclerView,
                          private val isInBottomSheet: Boolean,
                          private val lifecycleOwner: LifecycleOwner,
                          private val contentAdDelegate: ContentAdDelegate? = null,
                          private val reportAdsMenuListener: ReportAdsMenuListener?) {

    fun getViewHolder(displayCardTypeIndex: Int, parent: ViewGroup, activity: AppCompatActivity,
                      card: CommonAsset?, bootStrapCard: DetailListCard?, cvm: CardsViewModel?,
                      relatedStories: CardsPojo?, newsAppJSChunk1: NewsAppJS?,
                      newsAppJSChunk2: NewsAppJS?, error: ObservableDataBinding<BaseError>,
                      timeSpentEventId: Long):
            RecyclerView.ViewHolder {

        val layoutInflater = LayoutInflater.from(parent.context)
        val viewDataBinding: ViewDataBinding
        val defaultBackground = ThemeUtils.getThemeColorByAttribute(parent.context,
                R.attr.default_background)

        //Check if card is Ad.
        AdsViewHolderFactory.getViewBinding(displayCardTypeIndex, layoutInflater, parent)?.let { vb ->
            vb.setVariable(BR.vm, cardsViewModel)
            vb.setVariable(BR.appSettingsProvider, AppSettingsProvider)
            AdsViewHolderFactory.getViewHolder(displayCardTypeIndex, vb,
                    uniqueRequestId, parent, deeplinkUrl, parentLifecycleOwner, adEntityReplaceHandler,
                    interactiveAdListener, webCacheProvider,
                    reportAdsMenuListener = reportAdsMenuListener)?.let {
                return it
            }
        }

        var onItemVisibleCall: ((String?) -> Unit)? = null
        var vc: VisibilityCalculator? = null

        when (displayCardTypeIndex) {
            DetailCardType.SOURCE.index -> {
                viewDataBinding = DataBindingUtil
                        .inflate<NewsDetailSourceVhBinding>(layoutInflater,
                                R.layout.news_detail_source_vh,
                                parent,
                                false)

            }
            DetailCardType.SOURCE_TIME.index ->{
                viewDataBinding = DataBindingUtil
                        .inflate<SourceTimeDetailVhBinding>(layoutInflater,
                                R.layout.source_time_detail_vh,
                                parent,
                                false)
                viewDataBinding.setVariable(BR.promotedTag, contentAdDelegate?.getPromotedTag())
                viewDataBinding.lifecycleOwner = fragment

            }
            DetailCardType.TITLE.index -> {
                viewDataBinding = DataBindingUtil
                        .inflate<NewsDetailTitleVhBinding>(layoutInflater,
                                R.layout.news_detail_title_vh,
                                parent,
                                false)
                viewDataBinding.lifecycleOwner = fragment
            }
            DetailCardType.TIME.index -> {
                viewDataBinding = DataBindingUtil
                        .inflate<NewsDetailTimeVhBinding>(layoutInflater,
                                R.layout.news_detail_time_vh,
                                parent,
                                false)
                viewDataBinding.setVariable(BR.promotedTag, contentAdDelegate?.getPromotedTag())
            }
            DetailCardType.SEEPOST.index -> {
                viewDataBinding = DataBindingUtil
                        .inflate<NewsDetailSeePostVhBinding>(layoutInflater,
                                R.layout.news_detail_see_post_vh,
                                parent,
                                false)
            }

            DetailCardType.IMAGE.index -> {
                viewDataBinding = DataBindingUtil
                        .inflate<NewsDetailImageVhBinding>(layoutInflater,
                                R.layout.news_detail_image_vh,
                                parent,
                                false)
                val newsDetailImage = viewDataBinding.root.findViewById<NHRoundedCornerImageView>(R.id.news_detail_image)
                val imageWidth = CommonUtils.getDeviceScreenWidth()
                val aspectRatio = ImageUrlReplacer.getContentImageAspectRatio()
                val imageHeight: Int
                if (java.lang.Float.compare(aspectRatio, 1.0f) == 0) {
                    imageHeight = CommonUtils.getDimensionInDp(R.dimen.news_detail_image_height)
                } else {
                    imageHeight = Math.round(imageWidth / ImageUrlReplacer
                            .getContentImageAspectRatio())
                }
                viewDataBinding.lifecycleOwner = fragment
                newsDetailImage.minimumHeight = imageHeight
                viewDataBinding.root.findViewById<View?>(R.id
                    .news_title).visibility = View.GONE
            }

            DetailCardType.IMAGE_DYNAMIC.index -> {
                viewDataBinding = DataBindingUtil
                        .inflate<NewsDetailImageVhBinding>(layoutInflater,
                                R.layout.news_detail_image_vh,
                                parent,
                                false)
                val newsDetailImage = viewDataBinding.root.findViewById<NHRoundedCornerImageView>(R.id.news_detail_image)
                val imageWidth = CommonUtils.getDeviceScreenWidth()
                val aspectRatio = ImageUrlReplacer.getContentImageAspectRatio()
                val imageHeight: Int
                if (java.lang.Float.compare(aspectRatio, 1.0f) == 0) {
                    imageHeight = CommonUtils.getDimensionInDp(R.dimen.news_detail_image_height)
                } else {
                    imageHeight = Math.round(imageWidth / ImageUrlReplacer
                            .getContentImageAspectRatio())
                }
                viewDataBinding.lifecycleOwner = fragment
                newsDetailImage.minimumHeight = imageHeight
                viewDataBinding.root.findViewById<View?>(R.id
                    .news_title).visibility = View.GONE
            }

            DetailCardType.GALLERY_2.index -> {
                viewDataBinding = DataBindingUtil
                        .inflate<NewsDetailGallery2VhBinding>(layoutInflater,
                                R.layout.news_detail_gallery2_vh,
                                parent,
                                false)

            }
            DetailCardType.GALLERY_3.index -> {
                viewDataBinding = DataBindingUtil
                        .inflate<NewsDetailGallery3VhBinding>(layoutInflater,
                                R.layout.news_detail_gallery3_vh,
                                parent,
                                false)

            }
            DetailCardType.GALLERY_4.index -> {
                viewDataBinding = DataBindingUtil
                        .inflate<NewsDetailGallery4VhBinding>(layoutInflater,
                                R.layout.news_detail_gallery4_vh,
                                parent,
                                false)

            }
            DetailCardType.GALLERY_5.index -> {
                viewDataBinding = DataBindingUtil
                        .inflate<NewsDetailGallery5VhBinding>(layoutInflater,
                                R.layout.news_detail_gallery5_vh,
                                parent,
                                false)

            }
            DetailCardType.CHUNK1.index -> {
                viewDataBinding = DataBindingUtil
                        .inflate<NewsDetailChunk1VhBinding>(layoutInflater,
                                R.layout.news_detail_chunk1_vh,
                                parent,
                                false)

                val newsDetailWebView = viewDataBinding.root.findViewById<HeightAwareWebView>(R.id
                        .news_details_webview)
                viewDataBinding.lifecycleOwner = fragment
                newsDetailWebView.settings.domStorageEnabled = true
                if (card?.i_isFocusableInTouchMode() == true) {
                    newsDetailWebView.isFocusableInTouchMode = true
                }

                newsDetailWebView.setBackgroundColor(defaultBackground)
                NHWebViewUtils.initializeWebView(newsDetailWebView)
                val webContentInterface = NewsDetailWebContentInterface(newsDetailWebView, activity,
                        fragment, null, null).also { it.observeBus() }
                newsDetailWebView.addJavascriptInterface(
                        webContentInterface, CommonUtils.getString(R.string.nh_js_command))
                setWebViewClient(newsDetailWebView, true, false, detailList)
                viewDataBinding.setVariable(BR.newsappjs, newsAppJSChunk1)
            }
            DetailCardType.CHUNK2.index -> {
                viewDataBinding = DataBindingUtil
                        .inflate<NewsDetailChunk2VhBinding>(layoutInflater,
                                R.layout.news_detail_chunk2_vh,
                                parent,
                                false)

                val newsDetailWebView = viewDataBinding.root.findViewById<HeightAwareWebView>(R.id
                        .news_details_webview)
                viewDataBinding.lifecycleOwner = fragment
                newsDetailWebView.settings.domStorageEnabled = true
                if (card?.i_isFocusableInTouchMode() == true) {
                    newsDetailWebView.isFocusableInTouchMode = true
                }

                newsDetailWebView.setBackgroundColor(defaultBackground)
                NHWebViewUtils.initializeWebView(newsDetailWebView)
                newsDetailWebView.addJavascriptInterface(
                        NewsDetailWebContentInterface(newsDetailWebView, activity, fragment,
                                null, null), CommonUtils.getString(R.string.nh_js_command))
                setWebViewClient(newsDetailWebView, false, true, detailList)
                viewDataBinding.setVariable(BR.newsappjs, newsAppJSChunk2)
            }

            DetailCardType.DISCLAIMER.index -> {
                viewDataBinding = DataBindingUtil
                        .inflate<NewsDetailDisclaimerVhBinding>(layoutInflater,
                                R.layout.news_detail_disclaimer_vh,
                                parent,
                                false)

                val newsDetailWebView = viewDataBinding.root.findViewById<HeightAwareWebView>(R.id
                        .news_details_webview)
                newsDetailWebView.settings.domStorageEnabled = true
                if (card?.i_isFocusableInTouchMode() == true) {
                    newsDetailWebView.isFocusableInTouchMode = true
                }

                newsDetailWebView.setBackgroundColor(defaultBackground)
                NHWebViewUtils.initializeWebView(newsDetailWebView)
                newsDetailWebView.addJavascriptInterface(
                        NewsDetailWebContentInterface(newsDetailWebView, activity, fragment,
                                null, null), CommonUtils.getString(R.string.nh_js_command))
                setWebViewClient(newsDetailWebView, false, false, detailList)
                viewDataBinding.setVariable(BR.newsappjs, newsAppJSChunk2)
            }
            DetailCardType.LOCATION.index -> {
                viewDataBinding = DataBindingUtil
                        .inflate<NewsDetailLocationVhBinding>(layoutInflater,
                                R.layout.news_detail_location_vh,
                                parent,
                                false)

            }
            DetailCardType.HASHTAGS.index -> {
                onItemVisibleCall = vm::onHashtagVisible
                viewDataBinding = DataBindingUtil
                        .inflate<NewsDetailHashcodeVhBinding>(layoutInflater,
                                R.layout.news_detail_hashcode_vh,
                                parent,
                                false)
            }
            DetailCardType.SUGGESTED_FOLLOW.index -> {
                viewDataBinding = DataBindingUtil
                        .inflate<NewsDetailSuggestedFollowVhBinding>(layoutInflater,
                                R.layout.news_detail_suggested_follow_vh,
                                parent,
                                false)

            }
            DetailCardType.LIKES_LIST.index -> {
                if (!isInBottomSheet) {
                    vc = VisibilityCalculator()
                    onItemVisibleCall = vm::onLikeListVisible
                }
                viewDataBinding = DataBindingUtil
                        .inflate<PostDetiailLikeLayoutBinding>(layoutInflater,
                                R.layout.post_detiail_like_layout,
                                parent,
                                false)
            }
            DetailCardType.SPACER.index -> {
                viewDataBinding = DataBindingUtil
                        .inflate<NewsDetailSpacerVhBinding>(layoutInflater,
                                R.layout.news_detail_spacer_vh,
                                parent,
                                false)
            }
            DetailCardType.DISCUSSION_HEADER.index -> {
                onItemVisibleCall = vm::onDiscussionHeaderVisible
                viewDataBinding = DataBindingUtil
                                .inflate<NewsDetailDiscussionHeaderVh2Binding>(layoutInflater,
                                        R.layout.news_detail_discussion_header_vh_2,
                                        parent,
                                        false)

            }
            DetailCardType.DISCUSSION_LOADER.index -> {
                viewDataBinding = DataBindingUtil
                        .inflate<NewsDetailDiscussionLoaderVhBinding>(layoutInflater,
                                R.layout.news_detail_discussion_loader_vh,
                                parent,
                                false)
            }
            DetailCardType.DISCUSSION_SHOW_ALL.index -> {
                viewDataBinding =
                            DataBindingUtil
                                    .inflate<NewsDetailDiscussionShowAllVh2Binding>(layoutInflater,
                                            R.layout.news_detail_discussion_show_all_vh_2,
                                            parent,
                                            false)
                if (card?.i_format() == Format.VIDEO) {
                    //If buton is in video detail discussion then set BG as black
                    viewDataBinding.root.findViewById<View>(R.id.see_all_btn)?.setBackgroundColor(Color.BLACK)
                }

            }
            DetailCardType.SECOND_CHUNK_LOADING.index -> {
                viewDataBinding = DataBindingUtil
                        .inflate<NewsDetailSecondChunkLoaderVhBinding>(layoutInflater,
                                R.layout.news_detail_second_chunk_loader_vh,
                                parent,
                                false)
            }
            DetailCardType.DISCUSSION_NS.index, DetailCardType.DISCUSSION.index -> {
                if (!isInBottomSheet) {
                    onItemVisibleCall = vm::onDiscussionVisible
                }
                if (card?.i_type() == AssetType2.COMMENT.name) {
                    viewDataBinding = DataBindingUtil
                            .inflate<NewsDetailRepliesVhBinding>(layoutInflater,
                                    R.layout.news_detail_replies_vh,
                                    parent,
                                    false)
                } else {
                    viewDataBinding =
                                DataBindingUtil.inflate<NewsDetailDiscussionsVhBinding>(
                                        layoutInflater, R.layout.news_detail_discussions_vh,
                                        parent, false)
                }
            }
            DetailCardType.MAIN_COMMENT.index -> {
                onItemVisibleCall = vm::onDiscussionVisible
                viewDataBinding = DataBindingUtil
                        .inflate<NewsDetailMainDiscussionsVhBinding>(layoutInflater,
                                R.layout.news_detail_main_discussions_vh,
                                parent,
                                false)
            }
            DetailCardType.SUPPLEMENTARY_RELATED.index -> {
                viewDataBinding = DataBindingUtil
                        .inflate<NewsDetailRelatedVhBinding>(layoutInflater,
                                R.layout.news_detail_related_vh,
                                parent,
                                false)
                viewDataBinding.lifecycleOwner = lifecycleOwner
                viewDataBinding.setVariable(BR.lifecycle, parentLifecycleOwner)
            }
            DetailCardType.OTHER_PERSPECTIVES.index -> {
                onItemVisibleCall = vm::onOtherPerspectiveVisible
                viewDataBinding = DataBindingUtil
                        .inflate<NewsDetailOtherPerspectiveVhBinding>(layoutInflater,
                                R.layout.news_detail_other_perspective_vh,
                                parent,
                                false)
            }
            DetailCardType.VIRAL.index -> {
                viewDataBinding = DataBindingUtil
                        .inflate<NewsDetailViralBodyBinding>(layoutInflater,
                                R.layout.news_detail_viral_body,
                                parent,
                                false)
                viewDataBinding.lifecycleOwner = lifecycleOwner
                viewDataBinding.setVariable(BR.promotedTag, contentAdDelegate?.getPromotedTag())
            }

            DetailCardType.POLL.index -> {
                viewDataBinding = DataBindingUtil
                        .inflate<NewsDetailPollVhBinding>(layoutInflater,
                                R.layout.news_detail_poll_vh,
                                parent,
                                false)
            }

            DetailCardType.POLL_RESULT.index -> {
                viewDataBinding = DataBindingUtil
                        .inflate<NewsDetailPollResultVhBinding>(layoutInflater,
                                R.layout.news_detail_poll_result_vh,
                                parent,
                                false)
            }

            DetailCardType.OGCARD.index -> {
                viewDataBinding = DataBindingUtil
                        .inflate<NewsDetailOgVhBinding>(layoutInflater,
                                R.layout.news_detail_og_vh,
                                parent,
                                false)
            }

            DetailCardType.REPOST.index -> {
                viewDataBinding = DataBindingUtil
                        .inflate<NewsDetailRepostVhBinding>(layoutInflater,
                                R.layout.news_detail_repost_vh,
                                parent,
                                false)
            }

            DetailCardType.RICH_GALLERY.index -> {
                viewDataBinding = DataBindingUtil
                        .inflate<NewsDetailRichGalleryVhBinding>(layoutInflater,
                                R.layout.news_detail_rich_gallery_vh,
                                parent,
                                false)
            }

            DetailCardType.SEE_IN_VIDEO.index -> {
                onItemVisibleCall = vm::onAssociatedVideoVisible
                viewDataBinding = DataBindingUtil
                        .inflate<NewsDetailSeeInVideoVhBinding>(layoutInflater,
                                R.layout.news_detail_see_in_video_vh,
                                parent,
                                false)
                viewDataBinding.setVariable(BR.lifecycle, parentLifecycleOwner)
            }

            DetailCardType.READMORE.index -> {
                viewDataBinding = DataBindingUtil
                        .inflate<NewsDetailReadmoreVhBinding>(layoutInflater,
                                R.layout.news_detail_readmore_vh,
                                parent,
                                false)
                viewDataBinding.also {
                    it.lifecycleOwner = fragment
                }
            }
            DetailCardType.SHIMMER.index -> {
                viewDataBinding = DataBindingUtil
                        .inflate<ViewDataBinding>(layoutInflater,
                                R.layout.post_detail_progress_viewholder,
                                parent,
                                false)
            }
            DetailCardType.AD_SUPPLEMENT_HEADER.index -> {
                viewDataBinding = DataBindingUtil
                        .inflate<LayoutSupplementAdsHeaderBinding>(layoutInflater,
                                R.layout.layout_supplement_ads_header,
                                parent,
                                false)
                viewDataBinding.lifecycleOwner = fragment
            }
            else -> {
                viewDataBinding = DataBindingUtil
                        .inflate<NewsDetailDummyVhBinding>(layoutInflater,
                                R.layout.news_detail_dummy_vh,
                                parent,
                                false)
            }
        }

        viewDataBinding.setVariable(BR.vm, cardsViewModel)
        viewDataBinding.setVariable(BR.section, section)
        viewDataBinding.setVariable(BR.baseError, error.value)
        viewDataBinding.setVariable(BR.dvm, vm)
        viewDataBinding.setVariable(BR.listener, postListener)

        if (card != null) {
            viewDataBinding.setVariable(BR.card, card)
        }

        if (bootStrapCard != null) {
            viewDataBinding.setVariable(BR.bootstrap_card, bootStrapCard)
        }

        if (relatedStories != null) {
            viewDataBinding.setVariable(BR.relatedstories, relatedStories)
        }
        
        val repostCardType = getDisplayCardTypeForRepost(card)
        viewDataBinding.setVariable(BR.indetail, true)
        viewDataBinding.setVariable(BR.cardTypeIndex, displayCardTypeIndex)
        viewDataBinding.setVariable(BR.repostCardIndex, repostCardType)
        viewDataBinding.setVariable(BR.timeSpentEventId, timeSpentEventId)

        return NHRecyclerViewHolder(viewType = displayCardTypeIndex, viewBinding = viewDataBinding,
                onItemVisible = onItemVisibleCall, vC = vc, lifecycleOwner = lifecycleOwner, uniqueRequestId = uniqueRequestId)
    }

    fun getDisplayCardTypeForRepost(card: CommonAsset?): Int {
        val subFormat = card?.i_repostAsset()?.subFormat
        val format = card?.i_repostAsset()?.format
        val uiType = card?.i_repostAsset()?.uiType
        if (format == Format.POLL) {
            return DetailCardType.REPOST_POLL.index
        }
        if (card?.i_repostAsset()?.i_viral() != null) {
            return DetailCardType.REPOST_VIRAL.index
        }
        if (card?.i_repostAsset()?.i_linkAsset() != null)
            return DetailCardType.REPOST_OG.index
        if (subFormat == SubFormat.STORY || subFormat == SubFormat.S_W_IMAGES || subFormat == SubFormat.S_W_PHOTOGALLERY) {
            when (uiType) {
                UiType2.NORMAL -> return DetailCardType.REPOST_NORMAL.index
                UiType2.HERO -> return DetailCardType.REPOST_BIG_IMAGE.index
                else -> {
                    //Do nothing let fallback handle
                }
            }
        }
        if (card?.i_repostAsset()?.title == null && card?.i_repostAsset()?.content == null)
            return DetailCardType.REPOST_BIG_IMAGE.index

        return DetailCardType.REPOST_NORMAL.index
    }


    private fun setWebViewClient(newsDetailWebView: NhWebView?, firstChunk: Boolean,
                                 secondChunk: Boolean, detailList: RecyclerView) {
        if (newsDetailWebView == null) {
            return
        }

        val objNewsDetailWebViewClient = PostWebViewClient(fragment, postListener, vm, firstChunk,
                secondChunk, detailList, isInBottomSheet)
        newsDetailWebView.webViewClient = objNewsDetailWebViewClient
    }
}