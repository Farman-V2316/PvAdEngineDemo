/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.appview.common.ui.helper

import android.content.Context
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import com.newshunt.adengine.model.entity.ContentAdDelegate
import com.newshunt.analytics.client.AnalyticsClient
import com.newshunt.appview.R
import com.newshunt.appview.common.postcreation.view.service.UploadJobService
import com.newshunt.appview.common.profile.view.activity.ProfileViewState
import com.newshunt.appview.common.ui.viewholder.PerspectiveState
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.common.helper.share.ShareUi
import com.newshunt.dataentity.analytics.entity.NhAnalyticsEventParam
import com.newshunt.dataentity.analytics.referrer.NhGenericReferrer
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.analytics.section.NhAnalyticsEventSection
import com.newshunt.dataentity.common.asset.ApprovalCounts
import com.newshunt.dataentity.common.asset.AssetType2
import com.newshunt.dataentity.common.asset.CardLabel2
import com.newshunt.dataentity.common.asset.CommonAsset
import com.newshunt.dataentity.common.asset.Counts2
import com.newshunt.dataentity.common.asset.DetailListCard
import com.newshunt.dataentity.common.asset.EntityItem
import com.newshunt.dataentity.common.asset.Format
import com.newshunt.dataentity.common.asset.LinkAsset
import com.newshunt.dataentity.common.asset.PollOptions
import com.newshunt.dataentity.common.asset.PostDisplayType
import com.newshunt.dataentity.common.asset.PostEntity
import com.newshunt.dataentity.common.asset.PostEntityLevel
import com.newshunt.dataentity.common.asset.PostPrivacy
import com.newshunt.dataentity.common.asset.PostSourceAsset
import com.newshunt.dataentity.common.asset.PostSourceType
import com.newshunt.dataentity.common.asset.RepostDisplayType
import com.newshunt.dataentity.common.asset.SubFormat
import com.newshunt.dataentity.common.asset.UiType2
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.common.helper.share.ShareApplication
import com.newshunt.dataentity.common.pages.ActionableEntity
import com.newshunt.dataentity.common.pages.PageEntity
import com.newshunt.dataentity.common.pages.UserFollowView
import com.newshunt.dataentity.model.entity.GroupInfo
import com.newshunt.dataentity.model.entity.Member
import com.newshunt.dataentity.model.entity.MemberRole
import com.newshunt.dataentity.model.entity.MembershipStatus
import com.newshunt.dataentity.model.entity.ReviewActionBody
import com.newshunt.dataentity.model.entity.ReviewItem
import com.newshunt.dataentity.model.entity.SettingState
import com.newshunt.dataentity.model.entity.SocialHandleInfo
import com.newshunt.dataentity.model.entity.SocialPrivacy
import com.newshunt.dataentity.news.model.entity.DetailCardType
import com.newshunt.dataentity.news.model.entity.server.asset.AssetType
import com.newshunt.dataentity.news.model.entity.server.asset.CardLabelBGType
import com.newshunt.dataentity.news.model.entity.server.asset.PostState
import com.newshunt.dataentity.social.entity.CreatePostEntity
import com.newshunt.dataentity.social.entity.MenuOption
import com.newshunt.dataentity.social.entity.PostUploadStatus
import com.newshunt.dhutil.bundleOf
import com.newshunt.dhutil.displayCount
import com.newshunt.dhutil.helper.preference.AppStatePreference
import com.newshunt.dhutil.helper.theme.ThemeUtils
import com.newshunt.helper.ImageUrlReplacer
import com.newshunt.news.analytics.NhAnalyticsNewsEvent
import com.newshunt.news.analytics.NhAnalyticsNewsEventParam
import com.newshunt.notification.analytics.devEvent.LOG_TAG
import com.newshunt.sso.SSO
import kotlinx.android.synthetic.main.follow_button.view.*
import kotlin.math.roundToInt
import android.graphics.drawable.*
import android.util.Log
import com.newshunt.common.helper.common.*
import com.newshunt.dhutil.helper.AppSettingsProvider
import com.newshunt.news.view.fragment.DetailsBindUtils
import com.newshunt.onboarding.model.entity.datacollection.InstalledAppInfo

class CardsBindUtils {
    companion object {
        private const val LOG_TAG = "CardsBindUtils"
        @JvmStatic
        fun canShowLiveTag(item: Any?): Boolean {
            val commonItem = item as? CommonAsset ?: return false
            return commonItem.i_videoAsset()?.liveStream == true && !commonItem.i_showVideoIcon()
        }

        @JvmStatic
        fun canShowLiveTagForRepost(item: Any?): Boolean {
            val commonItem = item as? CommonAsset ?: return false
            return commonItem.i_videoAsset()?.liveStream == true
        }


        @JvmStatic
        fun canShowCreatorBadge(source: PostSourceAsset?): Boolean {
            source ?: return false
            if (!CommonUtils.isEmpty(source.entityImageUrl)) {
                return false
            }
            return when (source.type) {
                PostSourceType.ICC.name, PostSourceType.OGC.name, PostSourceType.PGC.name -> true
                else -> {
                    false
                }
            }
        }

        @JvmStatic
        fun canShowCreatorBadgeInFeed(source: PostSourceAsset?): Boolean {
            source ?: return false
            return when (source.type) {
                PostSourceType.ICC.name, PostSourceType.OGC.name, PostSourceType.PGC.name -> true
                else -> {
                    false
                }
            }
        }


        @JvmStatic
        fun showBigSourceImage(item: CommonAsset?): Boolean {
            //Return false as new design we don't support big image icon
            return false
        }

        @JvmStatic
        fun showBigSourceImage(item: DetailListCard?): Boolean {
            //Return false as new design we don't support big image icon
            return false
        }

        @JvmStatic
        fun showRepostOg(cardType: Int): Boolean {
            return cardType == PostDisplayType.REPOST_OG.index
        }

        @JvmStatic
        fun canShowSourceHeader(item: CommonAsset?): Boolean {
            return item?.i_source() != null
        }

        @JvmStatic
        fun canShowBreakingNewsTag(item: Any?): Boolean {
            (item as? CommonAsset)?.let { asset ->
                val publishTime:Long = asset.i_publishTime() ?: 0
                var ttl:Long = (asset.i_cardLabel()?.ttl ?: 0) * SECOND_MILLIS
                if (ttl < 0) {
                    ttl = Long.MAX_VALUE
                }
                return ttl != 0L && !CommonUtils.isTimeExpired(publishTime, ttl)
            }
            return false
        }

        @JvmStatic
        fun getCardLabelTextColor(item: Any?): Int? {
            val label = (item as? CommonAsset)?.i_cardLabel() ?: return null
            return ViewUtils.getColor(label.fgColor, Color.WHITE)
        }

        @JvmStatic
        fun canShowPlayIconForRepost(item: Any?): Boolean {
            if (item is CommonAsset) {
                if (item.i_showVideoIcon() && item.hasCardImage()) {
                    return true
                }
                return (item as? CommonAsset)?.i_videoAsset() != null
            } else if (item is LinkAsset) {
                return item.type == AssetType.VIDEO.name
            }
            return false
        }

        @JvmStatic
        fun canShowPlayIcon(item: Any?): Boolean {
            if (item is CommonAsset) {
                if (item.i_showVideoIcon() && item.hasCardImage()) {
                    return true
                }
                return (item as? CommonAsset)?.i_videoAsset() != null && !canShowLiveTag(item)
            } else if (item is LinkAsset) {
                return item.type == AssetType.VIDEO.name
            }
            return false
        }

        @JvmStatic
        fun ogTitle(item: Any?): Spanned? {
            return (item as? CommonAsset)?.i_linkAsset()?.title?.let {
                HtmlCompat.fromHtml(it, HtmlCompat.FROM_HTML_MODE_LEGACY)
            } ?: SpannableString("``")
        }

        @JvmStatic
        fun ogDesc(item: Any?): Spanned? {
            return (item as? CommonAsset)?.i_linkAsset()?.description?.let {
                HtmlCompat.fromHtml(it, HtmlCompat.FROM_HTML_MODE_LEGACY)
            } ?: SpannableString("``")
        }

        @JvmStatic
        fun ogProvider(item: Any?): Spanned? {
            return (item as? CommonAsset)?.i_linkAsset()?.providerName?.let {
                HtmlCompat.fromHtml(it, HtmlCompat.FROM_HTML_MODE_LEGACY)
            } ?: SpannableString("``")
        }

        @JvmStatic
        fun canShowPlayIconInRepost(item: Any?): Boolean {
            if (item is CommonAsset) {
                if (item.i_showVideoIcon() && item.hasCardImage()) {
                    return true
                }
                return (item as? CommonAsset)?.i_videoAsset() != null
            } else if (item is LinkAsset) {
                return item.type == AssetType.VIDEO.name
            }
            return false
        }

        @JvmStatic
        fun showRepostPoll(item: CommonAsset?): Boolean {
            return item?.i_format() == Format.POLL
        }

        @JvmStatic
        fun canShowVideoDuration(item: Any?): Boolean {
            return !CommonUtils.isEmpty((item as? CommonAsset)?.i_videoAsset()?.duration)
        }

        @JvmStatic
        fun canShowVideoView(item: Any?) : Boolean {
            if (item is CommonAsset) {
                return (item as? CommonAsset)?.i_videoAsset() != null &&
                        (item as? CommonAsset)?.i_videoAsset()?.liveStream != true && !CommonUtils.isEmpty(getJustViewCount((item as? CommonAsset)?.i_counts()))
            }
            return false
        }

        @JvmStatic
        fun getDrawableForCardLabel(label: CardLabel2): Drawable? {
            if (CommonUtils.isEmpty(label.text)) {
                run {
                    Logger.e(LOG_TAG, "label.text is empty".getLogMessage())
                    return null
                }
            }
            if (label.bgType == CardLabelBGType.RECTANGLE) {
                val bg = ContextCompat.getDrawable(CommonUtils.getApplication(),
                        R.drawable.card_label_bg) as LayerDrawable
                val rectangale = bg.findDrawableByLayerId(R.id.card_label_bg_rectangle) as GradientDrawable
                rectangale.setColor(ViewUtils.getColor(label.bgColor, Color.TRANSPARENT))
                return rectangale
            }
            run {
                Logger.e(LOG_TAG, "not supported bgType ${label.bgType}".getLogMessage())
                return null
            }
        }

        @JvmStatic
        fun canShowLocalRetry(item: CommonAsset?): Boolean {
            return item?.i_status() == PostUploadStatus.FAILED.name
        }

        @JvmStatic
        fun handlePostProgressRetryClick(item: CommonAsset?) {
            val id = item?.i_localInfo()?.cpId?.toLong()
            val eventParams = HashMap<NhAnalyticsEventParam, Any>()
            eventParams[NhAnalyticsNewsEventParam.TYPE] = Constants.LOCAL_CARD_RETRY
            AnalyticsClient.log(NhAnalyticsNewsEvent.EXPLOREBUTTON_CLICK, NhAnalyticsEventSection
                    .APP, eventParams, PageReferrer(NhGenericReferrer.LOCAL_CARD))
            if (id != null) {
                UploadJobService.retry(id)
            }
        }

        @JvmStatic
        fun getImageColorFilterForGallery5(item: CommonAsset?): ColorFilter? {
            val imgCount = item?.i_thumbnailUrls()?.size ?: 0
            if (imgCount > 5) {
                return ColorMatrixColorFilter(ColorMatrix().apply { setSaturation(0f) })
            } else {
                return null
            }
        }

        @JvmStatic
        fun getMoreImageTextGallery5(item: CommonAsset?): String? {
            val imgCount = item?.i_thumbnailUrls()?.size ?: 0
            if (imgCount > 5) {
                return "+${imgCount - 5}"
            } else {
                return null
            }
        }

        @JvmStatic
        fun getMoreImageTextGallery5Visibility(item: CommonAsset?): Int {
            val imgCount = item?.i_thumbnailUrls()?.size ?: 0
            if (imgCount > 5) {
                return View.VISIBLE
            } else {
                return View.GONE
            }
        }

        @JvmStatic
        fun getHandle(item: CommonAsset?): String? {
            val handle = item?.i_source()?.handle ?: return null
            if (handle.getOrNull(0)?.toString() != Constants.AT_SYMBOL) {
                return Constants.AT_SYMBOL + handle
            } else {
                return handle
            }
        }

        @JvmStatic
        fun getHandle(item: DetailListCard?): String? {
            val handle = item?.source?.handle ?: return null
            if (handle.getOrNull(0)?.toString() != Constants.AT_SYMBOL) {
                return Constants.AT_SYMBOL + handle
            } else {
                return handle
            }
        }

        @JvmStatic
        fun getFollowers(item: CommonAsset?): String? {
            //todo change to follow :Priya
            val followcount = item?.i_source()?.counts?.FOLLOW?.value
            val followers = CommonUtils.getString(R.string.followers)

            return if (followcount != null)
                "$followcount $followers"
            else
                null

        }

        @JvmStatic
        fun getFollowers(item: DetailListCard?): String? {
            //todo change to follow :Priya
            val followcount = item?.source?.counts?.FOLLOW?.value
            val followers = CommonUtils.getString(R.string.followers)

            return if (followcount != null)
                "$followcount $followers"
            else
                null

        }

        @JvmStatic
        fun getEntitySubText(item: ActionableEntity?): String {
            return if (item?.handle?.isNotEmpty() == true) {
                item.handle!!
            } else {
                val followcount = item?.counts?.FOLLOW?.value
                val followers = CommonUtils.getString(R.string.followers)

                if (followcount != null) {
                    "$followcount $followers"
                } else
                    Constants.EMPTY_STRING
            }
        }

        @JvmStatic
        fun getDisplayName(item: CommonAsset?): String? {
            return item?.i_source()?.displayName
        }

        @JvmStatic
        fun getDisplayName(item: PostSourceAsset?): String? {
            return item?.displayName
        }

        @JvmStatic
        fun getDisplayName(item: DetailListCard?): String? {
            return item?.source?.displayName
        }

        @JvmStatic
        fun canAllowComment(card: CommonAsset?): Boolean {
            if (card == null)
                return false
            return card.i_allowComments() && !card.i_isDeleted()
        }

        @JvmStatic
        fun commentVisibilty(card: CommonAsset?,isToolbarTransparent: Boolean): Boolean {
            if (card == null)
                return false
            return card.i_allowComments() && !card.i_isDeleted() && !isToolbarTransparent
        }


        @JvmStatic
        fun setBottomBarVisibility(item: CommonAsset?, isDetailView: Boolean): Boolean {
            if (item == null)
                return false

            if (item.isLocalCard())
                return false
            else return !item.i_isApprovalPending() && !isDetailView

        }

        @JvmStatic
        fun setFollowVisibility(item: CommonAsset?): Boolean {
            item ?: return false
            if (isSameUserId(item))
                return false
            else return item.i_isFollowable()
        }


        @JvmStatic
        fun setpostProgressDrawable(item: CommonAsset?): Int? {
            if (item == null)
                return null
            val uploadStatus = item.i_progress()
            if (uploadStatus == -1)
                return R.drawable.ic_wrong
            if (uploadStatus == 100)
                return R.drawable.ic_post_success
            return null
        }


        @JvmStatic
        fun showSeeOtherPerspective(item: CommonAsset?): Boolean {
            return !item?.i_moreStories().isNullOrEmpty() && item?.i_isApprovalPending() == false
        }

        @JvmStatic
        fun showSeeOtherPerspectiveitemURL(item: CommonAsset, position: Int): String {
            item.i_moreCoverageIcons()?.let {
                if (it.size >= position)
                    return it.get(position - 1)
            }

            return Constants.EMPTY_STRING


        }

        @JvmStatic
        fun showCommentsRepostCards(item: CommonAsset?): Boolean {
            return !item?.i_commentRepostItems().isNullOrEmpty() && !showSeeOtherPerspective(item)
        }

        @JvmStatic
        fun showRepostIcon(item: CommonAsset?): Boolean {
            return item?.i_type() == AssetType2.REPOST.name
        }

        @JvmStatic
        fun showCommentIcon(item: CommonAsset?): Boolean {
            return item?.i_type() == AssetType2.COMMENT.name
        }

        @JvmStatic
        fun countsText(count: String?): String? {
            if (CommonUtils.isEmpty(count) || CommonUtils.equals(count, "0"))
                return null
            else
                return count
        }

        /**
         * Reply null  if current item is empty and other two siblings are empty too.
         * Reply EMPTY_STRING, if current item is empty and at least one of the sibling is not
         * empty.
         *
         * Reply count of counts, otherwise
         */
        @JvmStatic
        fun commentBarCountsText(count: String?, sibling1Text : String?,
                                 sibling2Text : String?, sibling3Text: String?): String? {
            if (CommonUtils.isEmpty(count) || CommonUtils.equals(count, "0"))
                if ((CommonUtils.isEmpty(sibling1Text) || CommonUtils.equals(sibling1Text, "0"))
                        && (CommonUtils.isEmpty(sibling2Text) || CommonUtils.equals(sibling2Text, "0")) && (CommonUtils.isEmpty(sibling3Text) || CommonUtils.equals
                        (sibling3Text, "0"))) {
                    return null
                }
                else {
                    return Constants.SPACE_STRING
                }
            else
                return count
        }

        @JvmStatic
        fun commentBarPaddingTop(count1: String?, count2 : String?,
                                 count3 : String?, count4 : String?): Int? {
            if ((CommonUtils.isEmpty(count1) || CommonUtils.equals(count1, "0")) &&
                    (CommonUtils.isEmpty(count2) || CommonUtils.equals(count2, "0")) &&
                    (CommonUtils.isEmpty(count3) || CommonUtils.equals(count3, "0")) &&
                    (CommonUtils.isEmpty(count3) || CommonUtils.equals(count3, "0"))) {
                return CommonUtils.getDimension(R.dimen.bottom_bar_padding_top_empty)
            }
            else {
                return CommonUtils.getDimension(R.dimen.bottom_bar_padding_bottom)
            }
        }

        @JvmStatic
        fun privacyVisibilty(item: CommonAsset?, isGone: Boolean): Boolean {
            if (item?.i_postPrivacy() == PostPrivacy.PRIVATE || isGone) {
                return false
            }

            return true
        }

        @JvmStatic
        fun shareVisibilty(item: CommonAsset?, isGone: Boolean,isToolbarTransparent: Boolean): Boolean {
            if (item?.i_postPrivacy() == PostPrivacy.PRIVATE || isGone) {
                return false
            }
            if(isToolbarTransparent){
                return false
            }

            return true
        }

        @JvmStatic
        fun handleSubHeading(item: PostSourceAsset,state: Boolean):String {
            item.let {
                if (state) {
                    return CommonUtils.getString(R.string.block_subheading_to_follow_updates)

                }
                return CommonUtils.getString(R.string.follow_subheading_to_follow_updates)
            }
            return ""
        }

        @JvmStatic
        fun shouldShowDivider(item: CommonAsset): Boolean {
            if (item.i_postLocation() != null || item.rootPostEntity()?.source?.type.equals("UGC",true)) {
                return true
            }
            return false
        }

        @JvmStatic
        fun otherPerspectivetext(item: CommonAsset?, position: Int): String {

            val iconCount = item?.i_moreCoverageIcons()?.size ?: 0
            if (iconCount > position) {
                return "+" + (iconCount - 3).toString()
            } else {
                return Constants.EMPTY_STRING
            }

        }

        @JvmStatic
        fun showotherPerspectivetext(item: CommonAsset?, position: Int): Boolean {

            val iconCount = item?.i_moreCoverageIcons()?.size ?: 0
            return iconCount > position

        }

        @JvmStatic
        fun onClickPerspective(view: View, item: CommonAsset?, state: PerspectiveState) {
            state.collapsed = !state.collapsed
        }

        @JvmStatic
        fun hasLocalInfo(item: Any?): Boolean {
            if (item !is CommonAsset) return false
            return item.i_level() == PostEntityLevel.LOCAL
        }


        @JvmStatic
        fun getImageDimension(cardTypeIndex: Int, imgIndex: Int): Pair<Int, Int>? {
            return when (cardTypeIndex) {
                PostDisplayType.SIMPLE_POST.index, PostDisplayType.LOCAL_NORMAL.index,
                PostDisplayType.SIMPLE_POST_DYNAMIC_HERO.index -> {
                    getSimplePostCardImageDimension(imgIndex)
                }

                PostDisplayType.SIMPLE_POST_LOW.index -> {
                    getSmallPostCardImageDimension(imgIndex)
                }

                PostDisplayType.IMAGES_2.index -> {
                    getGallery2CardImageDimension(imgIndex)
                }

                PostDisplayType.IMAGES_3.index -> {
                    getGallery3CardImageDimension(imgIndex)
                }

                PostDisplayType.IMAGES_4.index -> {
                    getGallery4CardImageDimension(imgIndex)
                }

                PostDisplayType.IMAGES_5.index -> {
                    getGallery5CardImageDimension(imgIndex)
                }
                PostDisplayType.REPOST_BIG_IMAGE.index -> {
                    getRepostBigImageDimension()
                }

                PostDisplayType.BANNER.index -> {
                    getBannerImageDimension()
                }

                PostDisplayType.REPOST_OG.index -> {
                    getRepostSmallImageDimension()
                }
                DetailCardType.REPOST_BIG_IMAGE.index -> {
                    getRepostBigImageDimension()
                }
                PostDisplayType.REPOST_NORMAL.index -> {
                    getRepostSmallImageDimension()
                }

                PostDisplayType.SEARCH_PHOTO_GRID.index -> {
                    getSearchPhotoImageDimension()
                }

                PostDisplayType.POST_COLLECTION_HTML.index,
                PostDisplayType.POST_COLLECTION_VIDEO.index,
                PostDisplayType.POST_COLLECTION_AUTOPLAY.index -> {
                    getCollectionNewsImageDimension()
                }

                PostDisplayType.POST_COLLECTION_IMAGE.index -> {
                    getCollectionViralImageDimension()
                }
                // TODO: Using detail card type will create conflict while deciding dimension better
                // remove earlier
                DetailCardType.REPOST_NORMAL.index -> {
                    getRepostSmallImageDimension()
                }
                RepostDisplayType.REPOST_NORMAL.index,
                RepostDisplayType.REPOST_OEMBED.index,
                RepostDisplayType.REPOST_POLL.index -> {
                    getRepostSmallImageDimension()
                }
                RepostDisplayType.REPOST_HERO.index -> {
                    getRepostBigImageDimension()
                }
                PostDisplayType.AUTOPLAY_EXO.index,
                PostDisplayType.AUTOPLAY_WEB.index -> {
                    getSimplePostCardImageDimension(imgIndex)
                }
                PostDisplayType.USER_INTERACTION.index -> {
                    getProfileInteractionCardDimensions()
                }
                PostDisplayType.SAVED_STORY_LIST_ITEM.index -> {
                    getImageDimensionForBookmarkListItem()
                }
                PostDisplayType.SAVED_VIDEO_LIST_ITEM.index -> {
                    getDimenForBookmarkVideo()
                }
                PostDisplayType.POST_COLLECTION_SAVED_VIDEOS.index,
                PostDisplayType.POST_COLLECTION_SAVED_STORIES.index -> {
                    getImageDimensionForSavedCarouselItem()
                }
                PostDisplayType.ASTRO.index -> {
                    getImageDimensionForAstroCard()
                }
                else -> {
                    Logger.e(LOG_TAG, "not findining card type for image dimension".getLogMessage())
                    null
                }
            }
        }

        private fun getImageDimensionForAstroCard(): Pair<Int, Int>? {
            val iconSize = CommonUtils.getDimension(R.dimen.astro_card_zodiac_sign_icon_width)
            return iconSize to iconSize
        }

        private fun getSearchPhotoImageDimension(): Pair<Int, Int>? {
            val width = CommonUtils.getDeviceScreenWidth() - 2 * CommonUtils.getDimension(R
                    .dimen.story_card_padding_left)
            val imageWidth = width / 2
            val height = CommonUtils.getDimension(R.dimen.search_photo_h)
            return height to imageWidth
        }

        private fun getBannerImageDimension(): Pair<Int, Int>? {
            val imageWidth = CommonUtils.getDeviceScreenWidth() - 2 * CommonUtils.getDimension(R
                    .dimen.story_card_padding_left)
            val aspectRatio = ImageUrlReplacer.getContentImageAspectRatio()
            val imageHeight: Int
            if (java.lang.Float.compare(aspectRatio, 1.0f) == 0) {
                imageHeight = CommonUtils.getDimensionInDp(R.dimen.news_detail_image_height)
            } else {
                imageHeight = Math.round(imageWidth / ImageUrlReplacer.getContentImageAspectRatio())
            }

            return imageHeight to imageWidth
        }


        private fun getCollectionViralImageDimension(): Pair<Int, Int>? {
            return CommonUtils.getDimension(R.dimen.carousel_viral_img_width) to
                    CommonUtils.getDimension(R.dimen.carousel_viral_img_height)
        }

        private fun getCollectionNewsImageDimension(): Pair<Int, Int>? {
            return CommonUtils.getDimension(R.dimen.carousel_news_img_width) to
                    CommonUtils.getDimension(R.dimen.carousel_news_img_height)
        }


        @JvmStatic
        fun getImageDimensionForEntityIcon(cardType: Int): Pair<Int, Int>? {
            return CommonUtils.getDimension(R.dimen.entity_icon_img_size) to
                    CommonUtils.getDimension(R.dimen.entity_icon_img_size)
        }

        @JvmStatic
        fun getImageDimensionForEntityBannerImage(cardType: Int, item: CommonAsset?): Pair<Int, Int>? {
            if (item?.i_hideSelection() == true) {
                return CommonUtils.getDimension(R.dimen.entity_banner_image_width) to CommonUtils
                        .getDimension(R.dimen.entity_banner_image_height)
            }
            return CommonUtils.getDimension(R.dimen.entity_banner_image_width) to
                    CommonUtils.getDimension(R.dimen.entity_banner_image_width)
        }

        @JvmStatic
        fun getImageDimensionForSavedCarouselItem(): Pair<Int, Int>? {
            return CommonUtils.getDimension(R.dimen.entity_banner_image_width) to
                    CommonUtils.getDimension(R.dimen.source_image_width_height)
        }

        @JvmStatic
        fun getImageDimensionForBookmarkListItem(): Pair<Int, Int>? {
            return CommonUtils.getDimension(R.dimen.history_thumbnail_width) to
                    CommonUtils.getDimension(R.dimen.history_thumbnail_width)
        }

        @JvmStatic
        fun getDimenForBookmarkVideo(): Pair<Int, Int>? {
            return CommonUtils.getDimension(R.dimen.bookmark_video_image_width) to CommonUtils
                    .getDimension(R.dimen.bookmark_video_image_height)
        }

        fun getSimpleLinkPostImageDimension(): Pair<Int, Int> {
            return CommonUtils.getDimension(R.dimen.link_post_image_dimension_width) to
                    CommonUtils.getDimension(R.dimen.link_post_image_dimension_height)
        }

        /*return image dimension width to height*/
        private fun getSimplePostCardImageDimension(imageIndex: Int): Pair<Int, Int> {
            val imageWidth = CommonUtils.getDeviceScreenWidth()
            val aspectRatio = ImageUrlReplacer.getContentImageAspectRatio()
            val imageHeight: Int
            if (java.lang.Float.compare(aspectRatio, 1.0f) == 0) {
                imageHeight = CommonUtils.getDimensionInDp(R.dimen.news_detail_image_height)
            } else {
                imageHeight = Math.round(imageWidth / ImageUrlReplacer.getContentImageAspectRatio())
            }

            return imageWidth to imageHeight
        }

        /*return image dimension width to height*/
        private fun getSmallPostCardImageDimension(imageIndex: Int): Pair<Int, Int> {
            return CommonUtils.getDimension(R.dimen.post_img_size_small_sq) to
                    CommonUtils.getDimension(R.dimen.post_img_size_small_sq)
        }

        private fun getGallery5CardImageDimension(imageIndex: Int): Pair<Int, Int> {
            if (imageIndex == 2) {
                return CommonUtils.getDimension(R.dimen.post_img_rec_gallery_5_i2_width) to
                        CommonUtils.getDimension(R.dimen.post_img_rec_gallery_5_i2_height)
            }
            return CommonUtils.getDimension(R.dimen.post_img_sq_gallery_5_i0_width) to
                    CommonUtils.getDimension(R.dimen.post_img_sq_gallery_5_i0_height)
        }

        private fun getGallery2CardImageDimension(imageIndex: Int): Pair<Int, Int> {
            return CommonUtils.getDimension(R.dimen.post_img_rec_gallery_2_i0_width) to
                    CommonUtils.getDimension(R.dimen.post_img_rec_gallery_2_i0_height)
        }

        private fun getGallery4CardImageDimension(imageIndex: Int): Pair<Int, Int> {
            return CommonUtils.getDimension(R.dimen.post_img_rec_gallery_4_width) to
                    CommonUtils.getDimension(R.dimen.post_img_rec_gallery_4_height)
        }

        private fun getGallery3CardImageDimension(imageIndex: Int): Pair<Int, Int> {
            if (imageIndex == 0) {
                return CommonUtils.getDimension(R.dimen.post_img_rec_gallery_3_i0_height) to
                        CommonUtils.getDimension(R.dimen.post_img_rec_gallery_3_i0_height)
            }
            return CommonUtils.getDimension(R.dimen.post_img_rec_gallery_3_i1_height) to
                    CommonUtils.getDimension(R.dimen.post_img_rec_gallery_3_i1_height)
        }

        private fun getRepostBigImageDimension(): Pair<Int, Int>? {
            return CommonUtils.getDimension(R.dimen.repost_big_img_width) to
                    CommonUtils.getDimension(R.dimen.repost_big_img_height)
        }

        private fun getRepostSmallImageDimension(): Pair<Int, Int>? {
            return CommonUtils.getDimension(R.dimen.post_img_size_small_sq) to
                    CommonUtils.getDimension(R.dimen.post_img_size_small_sq)
        }

        @JvmStatic
        fun getMenuOptionL1IconSize(): Pair<Int, Int> {
            val size = CommonUtils.getDimension(R.dimen.dislike_l1_opt_icon_size)
            return size to size
        }

        @JvmStatic
        fun showSourceSmall(item: CommonAsset): Boolean {
            return CommonUtils.isEmpty(item.i_source()?.entityImageUrl)
        }

        @JvmStatic
        fun showRepostImage(item: CommonAsset?, cardType: Int): Boolean {
            if (isRepostThumbnailEmpty(item, 0)) {
                return false
            }
            return cardType == PostDisplayType.REPOST_BIG_IMAGE.index || cardType == DetailCardType.REPOST_BIG_IMAGE.index
        }

        @JvmStatic
        fun showRepostPollOrSmall(item: CommonAsset?, cardType: Int): Boolean {
            if (item?.i_format() == Format.POLL) {
                return true
            }
            if (isRepostThumbnailEmpty(item, 0)) {
                return false
            }
            return cardType == PostDisplayType.REPOST_POLL.index || cardType == PostDisplayType
                    .REPOST_NORMAL.index || cardType == DetailCardType.REPOST_POLL.index || cardType == DetailCardType
                    .REPOST_NORMAL.index || cardType == PostDisplayType.REPOST_OG.index
        }

        /**
         * @see [BindingAdapterUtils.loadRepostThumbnail]
         */
        private fun isRepostThumbnailEmpty(item: CommonAsset?, index: Int): Boolean {
            val thumbnail = (item?.i_thumbnailUrls()?.getOrNull(0))
                    ?: (item?.i_linkAsset()?.thumbnailUrl)
                    ?: return true
            return CommonUtils.isEmpty(thumbnail)
        }

        @JvmStatic
        fun showTimeStamp(item: CreatePostEntity?): String {
            val timeInfo = getDisplayTimeTextAsStoryCard(item)
            return timeInfo ?: ""
        }

        @JvmStatic
        fun showTimeStamp(item: CommonAsset?): String {
            val timeInfo = getDisplayTimeTextAsStoryCard(item)
            val timestamp = showCounts(timeInfo, item)
            return timestamp ?: ""
        }

        @JvmStatic
        fun showTimeStampWithViewsCount(item: CommonAsset?): String {
            val viewCount = getViewCount(item?.i_counts())
            val timeInfo = DetailsBindUtils.getDisplayTimeTextAsStoryCard(item)
            if (CommonUtils.isEmpty(viewCount) && CommonUtils.isEmpty(timeInfo)) {
                return ""
            } else if (CommonUtils.isEmpty(viewCount)) {
                return timeInfo!!
            } else if (CommonUtils.isEmpty(timeInfo)) {
                return viewCount!!
            }
            val dot = CommonUtils.getString(R.string.middle_dot)
            return "$viewCount $dot $timeInfo"
        }

        @JvmStatic
        fun showTimeStampWithoutCount(item: CommonAsset?): String {
            val timeInfo = getDisplayTimeTextAsStoryCard(item)
            return timeInfo ?: Constants.EMPTY_STRING
        }

        @JvmStatic
        fun getPromotedTag(tag: String?): String {
            return tag ?: Constants.EMPTY_STRING
        }

        private fun getDisplayTimeTextAsStoryCard(asset: CreatePostEntity?): String? {
            val displayTime = asset?.creationDate
            return displayTime?.let {
                DateFormatter.getTimeAgoRoundedToMinute(displayTime)
            }
        }

        private fun getDisplayTimeTextAsStoryCard(asset: CommonAsset?): String? {
            asset ?: return null
            val publishTime: Long = asset.i_publishTime() ?: 0
            val recommendationTime: Long = asset.i_recommendationTs() ?: 0
            val displayTime = if (recommendationTime > 0) {
                recommendationTime
            } else {
                publishTime
            }

            if (displayTime <= 0 || (!asset.i_showTsAlways() &&
                            CommonUtils.isTimeExpired(displayTime, getOldestListDisplayTimeGap()))) {
                    return null
                }

            return if (CommonUtils.isEmpty(asset.i_tsString()))
                displayTime.let { DateFormatter.getTimeAgoRoundedToMinute(it) }
            else
                asset.i_tsString()
        }

        fun getOldestListDisplayTimeGap(): Long {
            val gapInSecs = PreferenceManager.getPreference(AppStatePreference.OLDEST_LIST_DISPLAY_TIME_GAP,
                    java.lang.Long.MAX_VALUE)
            return if (gapInSecs * DateFormatter.SECOND_MILLIS < 0) Long.MAX_VALUE else gapInSecs * DateFormatter.SECOND_MILLIS
        }

        @JvmStatic
        fun showTimestampDot(showViewCount: Boolean?, commonAsset: CommonAsset?,isNestedCollection:Boolean=false, isTopStoriesCarousel:Boolean=false): Boolean {
            if(isNestedCollection || isTopStoriesCarousel) return false
            if(showViewCount == true) {
                return !CommonUtils.isEmpty(showTimeStamp(commonAsset))
            } else {
                return !CommonUtils.isEmpty(showTimeStampWithoutCount(commonAsset))
            }
        }

        private fun showCounts(timeInfo: String?, commonAsset: CommonAsset?): String? {
            val viewCount = getViewCount(commonAsset?.i_counts())
            if (CommonUtils.isEmpty(timeInfo) && CommonUtils.isEmpty(viewCount)) {
                return Constants.EMPTY_STRING
            }
            val dot = CommonUtils.getString(R.string.middle_dot)
                if (CommonUtils.isEmpty(viewCount)) {
                    return "$timeInfo"
                } else if (CommonUtils.isEmpty(timeInfo)) {
                    return "$viewCount"
                } else {
                    return "$timeInfo $dot $viewCount"
                }
        }

        @JvmStatic
        fun showViews(item: CommonAsset): String {
            val views = getViewCount(item.i_counts())
            if (CommonUtils.isEmpty(views))
                return Constants.EMPTY_STRING
            return "· $views "
        }

        @JvmStatic
        fun shouldShowViews(item: CommonAsset?): Boolean {
            val views = getViewCount(item?.i_counts())
            if (CommonUtils.isEmpty(views))
                return false
            return true
        }

        @JvmStatic
        fun isErrorState(item: CommonAsset?): Boolean{
            return item?.i_postState() != PostState.PUBLISHED
        }

        fun getViewCount(counts: Counts2?): String? {
            return when {
                counts?.VIEWS == null -> Constants.EMPTY_STRING
                CommonUtils.isValidInteger(counts.VIEWS?.value) -> {
                    val count = Integer.parseInt(counts.VIEWS?.value!!)
                    val countStr = displayCount(counts.VIEWS?.value!!)
                    if (CommonUtils.isEmpty(countStr)) null else CommonUtils.getQuantifiedString(R.plurals.story_view_count, count, countStr)
                }
                else -> {
                    val count = if (CommonUtils.equals(Constants.COUNT_ONE_STRING, counts.VIEWS?.value)) Constants.ONE
                    else Int.MAX_VALUE
                    CommonUtils.getQuantifiedString(R.plurals.story_view_count, count, counts.VIEWS?.value)
                }
            }
        }

        fun getJustViewCount(counts: Counts2?): String? {
            return when {
                counts?.VIEWS == null -> Constants.EMPTY_STRING
                CommonUtils.isValidInteger(counts.VIEWS?.value) -> {
                    val count = Integer.parseInt(counts.VIEWS?.value!!)
                    val countStr = displayCount(counts.VIEWS?.value!!)
                    if (CommonUtils.isEmpty(countStr)) null else displayCount(count.toString())
                }
                else -> {
                    val count = if (CommonUtils.equals(Constants.COUNT_ONE_STRING, counts.VIEWS?.value)) Constants.ONE
                    else counts.VIEWS?.value
                    displayCount(count.toString())
                }
            }
        }

        @JvmStatic
        fun showPollTimeStamp(card: CommonAsset?): String? {
            val poll = card?.i_poll() ?: run {
                Logger.d(LOG_TAG, "poll object is null".getLogMessage())
                return null
            }

            val pollStartTime = poll.startDate ?: 0
            val pollEndTime = poll.endDate ?: 0
            val responseCount = poll.responseCount ?: 0
            val pollRemainingTime = pollEndTime - System.currentTimeMillis()
            val pollDuration = pollEndTime - (poll.originalStartDate ?: 0L)

            val endTimeFormattedString = DateFormatter.getDateTimeByDiff(Math.min(pollRemainingTime, pollDuration))
            val timeLeft = CommonUtils.getString(R.string.time_left)
            val votes = CommonUtils.getString(R.string.votes)
            val pollExpired = CommonUtils.getString(R.string.poll_expired)

            if (CommonUtils.isCurrentTimeInBounds(pollStartTime, pollEndTime)) {
                return "$responseCount $votes· $endTimeFormattedString $timeLeft"
            } else {
                return "$responseCount $votes· $pollExpired"
            }
        }

        @JvmStatic
        fun showRepost(item: CommonAsset?): Boolean {
            return item?.i_repostAsset() != null
        }


        @JvmStatic
        fun getPollVoteReverseSortIndex(item: CommonAsset, index: Int?): Int {
            index ?: run {
                Logger.e(LOG_TAG, "index is null".getLogMessage())
                return 0
            }
            val allCounts = item.i_poll()?.options?.map { it.count }?.toList() ?: emptyList()
            if (index >= allCounts.size) {
                return 0
            }
            var result = 0
            for (itemCount in allCounts) {
                if (itemCount ?: 0 > allCounts[index] ?: 0) {
                    result++
                }
            }
            return result
        }

        @JvmStatic
        fun showPollPercentage(item: CommonAsset?, index: Int?): Int? {

            item ?: return null
            val pollAsset = item.i_poll() ?: return null
            val responseCount = pollAsset.responseCount ?: 0
            if (index != null && responseCount > 0) {
                val selectedPollFromList = getListItem(pollAsset.options, index)
                return selectedPollFromList?.count?.div(responseCount.toFloat())?.times(100)?.roundToInt()
            }
            return null
        }

        @JvmStatic
        fun getPollVoteColor(context: Context, sortedIndex: Int): Int {
            //GREEN BLUE PURPE RED
            val colorAttr = when (sortedIndex) {
                0 -> R.attr.poll_opt_color_1
                1 -> R.attr.poll_opt_color_2
                2 -> R.attr.poll_opt_color_3
                else -> R.attr.poll_opt_color_4
            }
            return ThemeUtils.getThemeColorByAttribute(context, colorAttr)
        }

        @JvmStatic
        fun getListItem(list: List<PollOptions>?, index: Int): PollOptions? {
            return list?.getOrNull(index)
        }

        @JvmStatic
        fun getAspectRatioString(widthHeightRatio: Float): String {
            return "H, $widthHeightRatio:1"
        }

        @JvmStatic
        fun getPostPrivacyIcon(): Int? {
            //item.i_postPrivacy() ?: return null
            /*return if (item.i_postPrivacy() == PostPrivacy.PRIVATE) {
                R.drawable.ic_private_post
            } */
            return R.drawable.ic_public_post

        }

        @JvmStatic
        fun getTitleVisibility(indetail: Any?): Int {
            return if (indetail == null) View.VISIBLE else View.GONE
        }

        @JvmStatic
        fun getPerspectiveCollapse(state: Boolean): Int {
            return if (state) {
                R.drawable.perspective_expand
            } else {
                R.drawable.perspective_collapse
            }
        }

        @JvmStatic
        fun showExpandButton(item: CommonAsset?, diffThreshould: Float): Boolean {
            return showExpandButton(item, diffThreshould, null)
        }


        @JvmStatic
        fun showExpandButton(item: CommonAsset?, diffThreshould: Float, aspectRatio: Float?):
                Boolean {
            val viral = item?.i_viral() ?: run {
                Logger.i(LOG_TAG, "not viral card".getLogMessage())
                return false
            }
            val backgroundOption = viral.backgroundOption ?: run {
                Logger.e(LOG_TAG, "background option null".getLogMessage())
                return false
            }
            val viewAspectRatio = aspectRatio ?: getViralAspectRatio(item)
            if (backgroundOption.width > 0 && backgroundOption.height > 0) {
                val imageAspectRatio = backgroundOption.width.toFloat() / backgroundOption.height.toFloat()
                return (Math.abs(imageAspectRatio - viewAspectRatio) / viewAspectRatio) > diffThreshould
            } else {
                Logger.e(LOG_TAG, ("backgroundoptions.width or backgroundoptions" +
                        ".height is not positive value").getLogMessage())
                return false
            }
        }

        @JvmStatic
        fun getImageReplaceUrl(item: SocialHandleInfo?): String {
            item?.s_profileImage() ?: Constants.EMPTY_STRING
            return ImageUrlReplacer.getQualifiedImageUrl(item?.s_profileImage(),
                    CommonUtils.getDeviceScreenWidthInDp(), Constants.IMAGE_ASPECT_RATIO_16_9)
        }

        @JvmStatic
        fun displayBadge(item: SocialHandleInfo?): Boolean {
            return item?.s_badge() == MemberRole.OWNER.name ||
                    item?.s_badge() == MemberRole.ADMIN.name
        }

        @JvmStatic
        fun displayMember3dots(item: Member?): Boolean {
            return item?.membership == MembershipStatus.APPROVED &&
                    item.userId != SSO.getInstance().userDetails.userID
                    && item.uiType == UiType2.GRP_MEMBER
        }

        @JvmStatic
        fun getReviewBody(item: SocialHandleInfo?, type: ReviewItem): ReviewActionBody {
            return ReviewActionBody(item?.s_id(), type, item?.s_groupId())
        }

        @JvmStatic
        fun getReviewBody(item: CommonAsset?, type: ReviewItem): ReviewActionBody {
            return ReviewActionBody(item?.i_id(), type, item?.i_groupId())
        }

        @JvmStatic
        fun getApproveText(reviewItem: ReviewItem?): String {
            return CommonUtils.getString(
                    when (reviewItem) {
                        ReviewItem.GROUP_INVITATION -> R.string.join_text
                        else -> R.string.approve
                    })
        }

        @JvmStatic
        fun bundle(key: String, arg: Any?): Bundle {
            return bundleOf(key to arg)
        }

        @JvmStatic
        fun bundle(key: String, arg: Any?, key1: String, arg1: Any?):
                Bundle {
            return bundleOf(key to arg, key1 to arg1)
        }

        @JvmStatic
        fun bundle(key: String, arg: Any?, key1: String, arg1: Any?, key2: String, arg2: Any?):
                Bundle {
            return bundleOf(key to arg, key1 to arg1, key2 to arg2)
        }

        @JvmStatic
        fun bundle(key: String, arg: Any?, key1: String, arg1: Any?, key2: String, arg2: Any?,
                   key3: String, arg3: Any?): Bundle {
            return bundleOf(key to arg, key1 to arg1, key2 to arg2, key3 to arg3)
        }

        @JvmStatic
        fun canShowOgItem(item: CommonAsset?): Boolean {
            return !CommonUtils.isEmpty(item?.i_linkAsset()?.thumbnailUrl)
        }

        @JvmStatic
        fun isViewEnabledPrivacy(item: CommonAsset?): Boolean {
            if (item == null)
                return false
            if (item.i_postPrivacy() == null)
                return true
            return item.i_postPrivacy() == PostPrivacy.PUBLIC

        }

        @JvmStatic
        fun isViewEnabledPrivacyOrDeleted(item: CommonAsset?): Boolean {
            if (item == null) {
                return false
            }

            if (item.i_postPrivacy() == null) {
                return !item.i_isDeleted()
            }

            return item.i_postPrivacy() == PostPrivacy.PUBLIC && !item.i_isDeleted()
        }

        @JvmStatic
        fun isCommentsEnabled(item: CommonAsset?): Boolean {
            if (item == null)
                return false
            return item.i_allowComments()

        }

        @JvmStatic
        fun isLikeEnabled(item: CommonAsset?): Boolean {
            if (item == null)
                return false
            return !item.i_isDeleted()
        }

        @JvmStatic
        fun canShowCPRepostSmallImage(cardType: Int, asset: CommonAsset?): Boolean {
            if(asset == null) {
                return false
            }
           return when (cardType) {
                RepostDisplayType.REPOST_HERO.index -> {
                    false
                }
                RepostDisplayType.REPOST_NORMAL.index -> {
                    if (asset.i_format() == Format.IMAGE) {
                        false
                    } else {
                        asset.i_thumbnailUrls()?.size != 0
                    }
                }
                RepostDisplayType.REPOST_OEMBED.index -> {
                    asset.i_linkAsset() != null
                }
                RepostDisplayType.REPOST_POLL.index -> {
                    true
                }
                else -> {
                    asset.i_thumbnailUrls()?.size != 0
                }
            }
        }

        @JvmStatic
        fun canShowCPRepostSmallViralImage(cardType: Int, asset: CommonAsset?): Boolean {
            if(asset == null) {
                return false
            }
            return when (cardType) {
                RepostDisplayType.REPOST_NORMAL.index -> {
                    asset.i_format() == Format.IMAGE
                }
                else -> {
                    false
                }
            }
        }


        @JvmStatic
        fun canShowRepostSmallViralImage(cardType: Int): Boolean {
            return cardType == PostDisplayType.REPOST_VIRAL.index || cardType == DetailCardType
                    .REPOST_VIRAL
                    .index
        }



        @JvmStatic
        fun canShowCPRepostBigImage(cardType: Int): Boolean {
            return cardType == RepostDisplayType.REPOST_HERO.index
        }

        fun canShowPollResult(item: CommonAsset): Boolean {
            return item.i_pollSelectedOptionId() != null ||
                    !CommonUtils.isCurrentTimeInBounds(item.i_poll()?.startDate
                            ?: 0, item.i_poll()?.endDate ?: 0) || isSameUserId(item)
        }


        @JvmStatic
        fun isSameUserId(item: CommonAsset): Boolean {
            if (item is UserFollowView)
                return item.actionableEntity.entityId == SSO.getLoginResponse()?.userId
            else
                return CommonUtils.equals(SSO.getLoginResponse()?.userId, item.i_source()?.id)
        }

        @JvmStatic
        fun showPollSelected(userSelectedPollOptionId: String?, item: CommonAsset?, index: Int):
                Boolean {
            userSelectedPollOptionId ?: return false
            val listItem = item?.i_poll()?.options?.let { getListItem(it, index) }
            return CommonUtils.equals(userSelectedPollOptionId, listItem?.id)
        }

        @JvmStatic
        fun canShowEntityActionButton(item: EntityItem?, parentItem: CommonAsset?): Boolean {
            item ?: return false
            parentItem ?: return false
            val hideSelection = parentItem.i_hideSelection() ?: false ||
                    parentItem.i_uiType() == UiType2.CAROUSEL_5

            return !hideSelection
        }

        @JvmStatic
        fun canShowBannerForeground(item:EntityItem?):Boolean{
            return !CommonUtils.isEmpty(item?.i_displayName())
        }

        @JvmStatic
        fun canShowGroupFeedOverlap(groupInfo: GroupInfo?): Boolean {
            groupInfo ?: return false
            return (groupInfo.memberApproval == SettingState.REQUIRED || groupInfo.privacy ==
                    SocialPrivacy.PRIVATE) && groupInfo.userRole == MemberRole.NONE
        }

        @JvmStatic
        fun canShowGroupMembers(groupInfo: GroupInfo?): Boolean {
            groupInfo ?: return false
            return when (groupInfo.privacy) {
                SocialPrivacy.PRIVATE -> {
                    groupInfo.userRole != MemberRole.NONE
                }
                else -> {
                    true
                }
            }
        }

        @JvmStatic
        fun menuOptionTitle(menuOption: MenuOption?,
                            asset: CommonAsset?,
                            pageEntity: PageEntity?): String {
            val title = menuOption?.menuL1?.title ?: return Constants.EMPTY_STRING
            val sourceName = asset?.i_source()?.displayName ?: (pageEntity?.displayName
                    ?: Constants.EMPTY_STRING)
            val sourceHandle = asset?.i_source()?.handle ?: (pageEntity?.handle
                    ?: Constants.EMPTY_STRING)
            return title.replace(MENU_OPTION_TITLE_SOURCE_NAME_MACRO, sourceName)
                    .replace(MENU_OPTION_USER_HANDLE_MACRO, sourceHandle)
        }

        @JvmStatic
        fun hideThreeDotsMenu(adDelegate: ContentAdDelegate?, isInCollection: Boolean = false, isTopStoriesCarousel: Boolean?= false): Boolean {
            if(isTopStoriesCarousel == true) return true
            val hideAdMenu = adDelegate?.adEntity?.let {
                it.reportAdsMenuFeedBackEntity?.feedbackUrl?.isBlank() ?: true
            } ?: false
            return isInCollection || hideAdMenu
        }

        private fun getProfileInteractionCardDimensions(): Pair<Int, Int> {
            val width = CommonUtils.getDimension(R.dimen.history_thumbnail_width)
            return Pair(width, width)
        }

        @JvmStatic
        fun canShowViewAllButton(item: CommonAsset?): Boolean {
            return !CommonUtils.isEmpty(item?.i_viewAllDeeplink()) &&
                    !CommonUtils.isEmpty(item?.i_viewallText())
        }

        @JvmStatic
        fun getViralAspectRatio(item: CommonAsset?): Float {
            val backgroundOption = item?.i_viral()?.backgroundOption
                    ?: return Constants.VH_DEFAULT_IMAGE_ASPECT_RATIO

            return when (item.i_uiType()) {
                UiType2.VH_BIG -> Constants.VH_BIG_IMAGE_ASPECT_RATIO
                UiType2.VH_SMALL -> Constants.VH_SMALL_IMAGE_ASPECT_RATIO
                else -> if (backgroundOption.width == 0 || backgroundOption.height == 0) {
                    Constants.VH_DEFAULT_IMAGE_ASPECT_RATIO
                } else {
                    backgroundOption.width.toFloat() / backgroundOption.height.toFloat()
                }
            }
        }

        @JvmStatic
        fun setupShareIcon(
            view: TextView,
            top: Boolean,
            isInCommentbar: Boolean,
            isDetail: Boolean,
            isNewsBrief: Boolean?=false
        ) {
            val clickArgs = Bundle()
            val packageName: String = PreferenceManager.getPreference(
                AppStatePreference.SELECTED_APP_TO_SHARE,
                Constants.EMPTY_STRING
            )
            val selectedApp: InstalledAppInfo? =
                AppSettingsProvider.preferredSharableAppLiveData.value
            if (!packageName.isNullOrEmpty() && AndroidUtils.isAppInstalled(packageName) && selectedApp != null) {
                if (top) {
                    view.setCompoundDrawablesWithIntrinsicBounds(null, selectedApp.icon, null, null)
                } else {
                    view.setCompoundDrawablesWithIntrinsicBounds(selectedApp.icon, null, null, null)
                }
                clickArgs.putString(Constants.BUNDLE_SHARE_PACKAGE_NAME, selectedApp.packageName)
                if (isInCommentbar) {
                    clickArgs.putSerializable(
                        Constants.SHARE_UI_TYPE,
                        selectedApp.label + "_commentbar1"
                    )
                } else {
                    clickArgs.putSerializable(Constants.SHARE_UI_TYPE, ShareUi.ONCARD)
                }
                view.setTag(R.id.share_click_argument_tag, clickArgs)
            } else if (AndroidUtils.isAppInstalled(ShareApplication.WHATS_APP_PACKAGE.packageName)) {
                if (top) {
                    setCompoudDrawable(
                        view, top = ThemeUtils.getThemeDrawableByAttribute(
                            view.context, R.attr.share_icon, R.drawable.share_vector
                        )
                    )
                } else {
                    if(isNewsBrief == true) {
                        setCompoudDrawable(
                            view, start = R.drawable.viral_whatsapp)
                    } else if(isDetail == true) {
                        setCompoudDrawable(
                                view, start = ThemeUtils.getThemeDrawableByAttribute(
                                view.context, R.attr.share_icon_detail, R.drawable.share_vector_detail
                        )
                        )
                    }else {
                        setCompoudDrawable(
                            view, start = ThemeUtils.getThemeDrawableByAttribute(
                                view.context, R.attr.share_icon, R.drawable.share_vector
                            )
                        )
                    }
                }
                clickArgs.putString(
                    Constants.BUNDLE_SHARE_PACKAGE_NAME,
                    ShareApplication.WHATS_APP_PACKAGE.packageName
                )
                if (isInCommentbar) {
                    clickArgs.putSerializable(
                        Constants.SHARE_UI_TYPE,
                        ShareUi.COMMENT_BAR_SHARE_WHATSAPP
                    )
                } else {
                    clickArgs.putSerializable(Constants.SHARE_UI_TYPE, ShareUi.ONCARD)
                }
                view.setTag(R.id.share_click_argument_tag, clickArgs)
            } else {
                if (top) {
                    setCompoudDrawable(
                        view, top = ThemeUtils.getThemeDrawableByAttribute(
                            view.context,
                            R.attr.default_share_icon, R.drawable.default_share_vector
                        )
                    )
                } else {
                    if (isNewsBrief == true) {
                        setCompoudDrawable(view, start = R.drawable.default_share_vector_night)
                    } else if(isDetail == true) {
                        setCompoudDrawable(
                                view,
                                start = ThemeUtils.getThemeDrawableByAttribute(
                                        view.context,
                                        R.attr.default_share_icon_detail,
                                        R.drawable.default_share_detail
                                )
                        )
                    }else {
                        setCompoudDrawable(
                            view,
                            start = ThemeUtils.getThemeDrawableByAttribute(
                                view.context,
                                R.attr.default_share_icon,
                                R.drawable.default_share_vector
                            )
                        )
                    }
                }
                if (isInCommentbar) {
                    clickArgs.putSerializable(
                        Constants.SHARE_UI_TYPE,
                        ShareUi.COMMENT_BAR_SHARE_ICON
                    )
                } else {
                    clickArgs.putSerializable(Constants.SHARE_UI_TYPE, ShareUi.ONCARD)
                }
                view.setTag(R.id.share_click_argument_tag, clickArgs)
            }
        }

        @JvmStatic
        fun getApprovalCardVisibility(groupInfo: GroupInfo?, pendingApprovalCounts: ApprovalCounts?): Int {
            return groupInfo?.let { info ->
                if (info.userRole?.isOwnerOrAdmin() == true &&
                        pendingApprovalCounts?.TOTAL_PENDING_APPROVALS?.value?.isNotEmpty() == true &&
                        pendingApprovalCounts.TOTAL_PENDING_APPROVALS?.value != Constants.ZERO_STRING) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
            } ?: return View.GONE
        }

        @JvmStatic
        fun getApprovalCardVisibility(viewState: ProfileViewState?, pendingApprovalCounts: ApprovalCounts?): Int {
            if (viewState == null || viewState.isTPV()) {
                return View.GONE
            }

            return if (pendingApprovalCounts?.TOTAL_PENDING_APPROVALS?.value?.isNotEmpty() == true &&
                    pendingApprovalCounts.TOTAL_PENDING_APPROVALS?.value != Constants.ZERO_STRING) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }

        fun isViralPost(postEntity: PostEntity) : Boolean {
            when (postEntity.subFormat) {
                SubFormat.VHGIF,
                SubFormat.VHMEME,
                SubFormat.VHMEMETEXT,
                SubFormat.VHTEXT -> {
                    return true
                }
            }

            return false
        }

        @JvmStatic
        fun canShowThumbnail(item: CommonAsset?, index: Int): Boolean {
            return item?.i_format() == Format.VIDEO ||
                    item?.i_thumbnailUrls()?.getOrNull(index)?:item?.i_linkAsset()?.thumbnailUrl != null
        }

        @JvmStatic
        fun getInviteButtonText(member: Member): String {
            return if (member.membership == MembershipStatus.INVITED || member.membership == MembershipStatus.AWAITED) {
                CommonUtils.getString(R.string.invited)
            } else if (member.role == MemberRole.NONE) {
                CommonUtils.getString((R.string.invite_btn_text))
            } else {
                CommonUtils.getString(R.string.joined)
            }
        }

        @JvmStatic
        fun actionbarLayoutcommentIcon(isToolbarTransparent: Boolean): Int {
            val useDark = ThemeUtils.isNightMode()
            val i = if (isToolbarTransparent || useDark)
                R.drawable.ic_comments_night_2
             else
                R.drawable.comment_vector_2
            return i
        }

        @JvmStatic
        fun actionbarLayoutTextColor(isToolbarTransparent: Boolean): Int {
            val useDark = ThemeUtils.isNightMode()
            val i = if (isToolbarTransparent || useDark)
                R.color.bottom_bar_text_color_night
            else
                R.color.bottom_bar_text_color_day

            return CommonUtils.getColor(i)
        }

        fun getSecondItemVisiblePercentage(): Int{
            return PreferenceManager.getPreference(AppStatePreference.COLLECTION_SECOND_ITEM_VISIBLE_PERCENTAGE,Constants.COLLECTION_SECOND_ITEM_VISIBLE_PERCENTAGE)
        }

        @JvmStatic
        fun isSquareCardCarouselItem(cardTypeIndex: Int): Boolean {
            return cardTypeIndex == PostDisplayType.SQUARE_CARD_CAROUSEL.index
        }

        @JvmStatic
        fun chooseOGLayoutType(item: CommonAsset?): Boolean {
            if(CommonUtils.isEmpty(item?.i_content())) {
                return true
            }
            return false
        }
    }
}

private fun CommonAsset.hasCardImage() =
        i_contentImageInfo()?.url ?: i_thumbnailUrls()?.getOrNull(0) != null

private fun String.getLogMessage(): String {
    var method: String = "Unknown"
    try {
        method = Thread.currentThread().stackTrace[4].methodName
    } catch (e: Exception) {
        Logger.e(LOG_TAG, "getLogMessage - ${e.message}")
    }
    return "$method : $this"
}


private const val MENU_OPTION_TITLE_SOURCE_NAME_MACRO = "##NEWSPAPER_NAME##"
private const val MENU_OPTION_USER_HANDLE_MACRO = "##USER_HANDLE##"
private const val SECOND_MILLIS = 1000L