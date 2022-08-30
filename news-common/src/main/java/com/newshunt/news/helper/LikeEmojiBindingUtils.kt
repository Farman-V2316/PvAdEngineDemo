/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.news.helper

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.view.ContextThemeWrapper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.newshunt.news.view.customview.LikeEmojiPopup
import com.newshunt.common.helper.common.Constants
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.common.asset.AssetType2
import com.newshunt.dataentity.common.asset.CommonAsset
import com.newshunt.dataentity.common.asset.EntityConfig2
import com.newshunt.dataentity.common.asset.LikeListPojo
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.news.analytics.NewsReferrer
import com.newshunt.dataentity.social.entity.CreatePostEntity
import com.newshunt.dataentity.social.entity.LikeType
import com.newshunt.dhutil.getFormattedCountForLikesAndComments
import com.newshunt.dhutil.helper.theme.ThemeUtils
import com.newshunt.news.common.R
import com.newshunt.news.util.NewsConstants
import com.newshunt.news.viewmodel.EmojiClickHandlingViewModel
import com.newshunt.socialfeatures.util.SocialFeaturesConstants

private const val LOG_TAG = "LikeEmBindUtil"

class LikeEmojiBindingUtils {
    companion object {

        @JvmStatic
        fun showLikePopup(view: View, item: Any?, parentItem: Any? = null, vm: EmojiClickHandlingViewModel, isComment: Boolean?, commentType: String?) {
            item?:return
            (item as? CommonAsset)?.i_selectedLikeType()?.let {
                val enumValue = LikeType.fromName(it)
                if (enumValue != null) {
                    vm.onEmojiClick(view, item, parentItem, enumValue, isComment, commentType)
                    return
                }
            }

            (item as? CreatePostEntity)?.selectedLikeType?.let {
                val enumValue = LikeType.fromName(it)
                if (enumValue != null) {
                    vm.onEmojiClick(view, item, parentItem, enumValue, isComment, commentType)
                    return
                }
            }
            val layoutInflater = LayoutInflater.from(view.context)
            val rootView = layoutInflater.inflate(R.layout.like_emoji_popup, view.parent as ViewGroup, false)
            val popupWindow = LikeEmojiPopup(view.context, rootView, item, parentItem, vm, isComment, commentType)
            if (vm.isDetail()) {
                popupWindow.showAsDropDown(view, 0,
                        -(2.6 * (view.height - view.paddingTop)).toInt(), Gravity.TOP)
            }
            else {
                popupWindow.showAsDropDown(view, 0, -(3 * view.height), Gravity.TOP)
            }
        }

        @JvmStatic
        fun hasLoggedInLikes(item: LikeListPojo?): Boolean {
            return (item?.loggedInUserCount?:0) > 0 /*todo should always show. even not-logged in user?, or item is not deleted?*/
        }

        @JvmStatic
        fun openLikesList(view: View, item: CommonAsset) {
            openLikesList(view, item, PageReferrer(NewsReferrer.STORY_DETAIL), null, null)
        }

        @JvmStatic
        fun openLikesList(view: View, item: CommonAsset, section: String?) {
            openLikesList(view, item, PageReferrer(NewsReferrer.STORY_DETAIL), section, null)
        }

        @JvmStatic
        fun openLikesList(view: View, item: CommonAsset, section: String?,
                          likeListPojo: LikeListPojo?) {
            openLikesList(view, item, PageReferrer(NewsReferrer.STORY_DETAIL), section, likeListPojo)
        }

        @JvmStatic
        fun openLikesList(view: View, item: CommonAsset, referrer: PageReferrer?,
                          section : String?, likeListPojo: LikeListPojo?) {
            val openLikesListIntent = Intent()
            openLikesListIntent.action = Constants.LIKES_LIST_OPEN_ACTION
            openLikesListIntent.putExtra(Constants.BUNDLE_POST_ID, item.i_id())
            openLikesListIntent.putExtra(Constants.BUNDLE_LIKES_COUNTS, item.i_counts()?.TOTAL_LIKE?.value)

            openLikesListIntent.putExtra(Constants.BUNDLE_LIKES_COUNTS + LikeType.ANGRY.name,
                    item.i_counts()?.ANGRY?.value)
            openLikesListIntent.putExtra(Constants.BUNDLE_LIKES_COUNTS + LikeType.HAPPY.name,
                    item.i_counts()?.HAPPY?.value)
            openLikesListIntent.putExtra(Constants.BUNDLE_LIKES_COUNTS + LikeType.LIKE.name,
                    item.i_counts()?.LIKE?.value)
            openLikesListIntent.putExtra(Constants.BUNDLE_LIKES_COUNTS + LikeType.LOVE.name,
                    item.i_counts()?.LOVE?.value)
            openLikesListIntent.putExtra(Constants.BUNDLE_LIKES_COUNTS + LikeType.SAD.name,
                    item.i_counts()?.SAD?.value)
            openLikesListIntent.putExtra(Constants.BUNDLE_LIKES_COUNTS + LikeType.WOW.name,
                    item.i_counts()?.WOW?.value)

            openLikesListIntent.setPackage(CommonUtils.getApplication().packageName)
            openLikesListIntent.putExtra(Constants.BUNDLE_ACTIVITY_REFERRER, referrer)
            openLikesListIntent.putExtra(Constants.BUNDLE_ACTIVITY_REFERRER_TYPE, item.i_type())
            openLikesListIntent.putExtra(Constants.BUNDLE_GUEST_COUNT, likeListPojo?.guestUserCount ?: -1)
            openLikesListIntent.putExtra(NewsConstants.DH_SECTION, section)

            if (view.context is Activity) {
                (view.context as Activity).startActivityForResult(openLikesListIntent, 0)
            }
            else if (view.context is ContextThemeWrapper) {
                ((view.context as ContextThemeWrapper).baseContext as Activity).startActivityForResult(openLikesListIntent, 0)
            }
        }

        @JvmStatic
        fun getEmojiIconResource(likeType: LikeType?, context: Context?, isInPopup: Boolean): Int {
            return when (likeType) {
                LikeType.LIKE -> {
                    if (isInPopup) R.drawable.ic_emoji_blue_like
                    else
                        R.drawable.ic_selected_blue_like
                }
                LikeType.LOVE -> R.drawable.ic_emoji_blue_like
                LikeType.HAPPY -> R.drawable.ic_emoji_blue_like
                LikeType.WOW -> R.drawable.ic_emoji_blue_like
                LikeType.SAD -> {
                    if(isInPopup) R.drawable.ic_crying
                    else R.drawable.ic_vector_emoji_sad
                }
                LikeType.ANGRY -> {
                    if(isInPopup) R.drawable.ic_angry
                    else R.drawable.ic_vector_emoji_angry
                }
                else -> {
                    if (context != null) {
                        ThemeUtils.getThemeDrawableByAttribute(context, R.attr.like_icon, R.drawable.ic_like)
                    } else {
                        R.drawable.ic_like
                    }
                }
            }
        }

        @JvmStatic
        fun getEmojiIconResource(likeType: String?, context: Context, isInPopup: Boolean): Int {
            return getEmojiIconResource(LikeType.fromName(likeType), context, isInPopup)
        }

        @JvmStatic
        fun getCommentType(card : CommonAsset?) : String {
            if (card?.i_type() == AssetType2.COMMENT.name) {
                return SocialFeaturesConstants.COMMENT_TYPE_REPLY
            }

            return SocialFeaturesConstants.COMMENT_TYPE_MAIN
        }

        @JvmStatic
        fun hideCount(vm: EmojiClickHandlingViewModel): Boolean {
            return vm.isDetail()
        }

        @JvmStatic
        fun getEmojiCount(likeType: LikeType, item: Any): String {
            val counts = (item as? CommonAsset)?.i_counts() ?: return Constants.ZERO_STRING
            val config = when (likeType) {
                LikeType.LIKE -> counts.LIKE
                LikeType.LOVE -> counts.LOVE
                LikeType.HAPPY -> counts.HAPPY
                LikeType.SAD -> counts.SAD
                LikeType.WOW -> counts.WOW
                LikeType.ANGRY -> counts.ANGRY
                else -> null
            }
            return config?.value ?: Constants.ZERO_STRING
        }

        @JvmStatic
        fun onEmojiViewItemClick(view: View,
                                 item: Any?,
                                 parentItem: Any?,
                                 vm: EmojiClickHandlingViewModel,
                                 likeType: LikeType,
                                 popupView: LikeEmojiPopup?,
                                 isComment: Boolean?,
                                 commentType: String?) {
            popupView?.dismissPopup()
            item?.let {
                vm.onEmojiClick(view, item, parentItem, likeType, isComment, commentType)
            }
        }

        @JvmStatic
        fun onEmojiViewItemClick(view: View,
                                 item: Any?,
                                 parentItem: Any?,
                                 vm: EmojiClickHandlingViewModel,
                                 likeType: LikeType,
                                 isComment: Boolean?,
                                 commentType: String?) {

            item?.let {
                vm.onEmojiClick(view, item, parentItem, likeType, isComment, commentType)
            }
        }

        @JvmStatic
        fun likeLayoutIsCurrentLikeSelected(card: CommonAsset, likeIndex: Int): Boolean {
            val selectedLikeType = likeLayoutSelectedLikeType(card)
            return when {
                likeIndex == 0 -> {
                    val likeTypeMappedToSmile = listOf(LikeType.LIKE, LikeType.LOVE, LikeType.HAPPY, LikeType.WOW)
                    likeTypeMappedToSmile.contains(selectedLikeType)
                }
                likeIndex == 1 -> selectedLikeType == LikeType.SAD
                likeIndex == 2 -> selectedLikeType == LikeType.ANGRY
                else -> false
            }
        }

        @JvmStatic
        fun likeLayoutCalculatedTotalLikeCount(card: CommonAsset): String {
            return getFormattedCountForLikesAndComments(likeLayoutSmileSadAndAngryCounts(card).sum())
        }

        @JvmStatic
        fun likeLayoutCalculatedTotalLikeVisibility(card: CommonAsset): Int {
            val text = likeLayoutCalculatedTotalLikeCount(card)
            return if(text.isEmpty() || text == "0") View.GONE else View.VISIBLE
        }

        @JvmStatic
        fun likeLayoutTitle(card: CommonAsset): String? {
            return if (likeLayoutSelectedLikeType(card) == null) {
                CommonUtils.getString(R.string.detail_like_layout_title_default)
            } else {
                CommonUtils.getString(R.string.detail_like_layout_title_responded)
            }
        }

        @JvmStatic
        fun likeLayoutEmojiIcon(card: CommonAsset?, likeType: LikeType, isInBottomSheet: Boolean): Int {

            val useDark = ThemeUtils.isNightMode() || isInBottomSheet
            val selected = likeLayoutSelectedLikeType(card)?.equals(likeType)?:false
            val i =
                if(useDark) {
                    if (selected) {
                        when (likeType) {
                            LikeType.LIKE -> R.drawable.ic_like_thumb_selected_night

                            LikeType.ANGRY -> R.drawable.ic_dislike_thumb_selected_night
                            else -> {
                                R.drawable.ic_like_thumb_selected_night
                            }
                        }
                    } else {
                        when (likeType) {
                            LikeType.LIKE -> R.drawable.ic_like_thumb_night

                            LikeType.ANGRY -> R.drawable.ic_dislike_thumb_night
                            else -> {
                                R.drawable.ic_like_thumb
                            }
                        }
                    }
                }else{
                    if (selected) {
                        when (likeType) {
                            LikeType.LIKE -> R.drawable.ic_like_thumb_selected

                            LikeType.ANGRY -> R.drawable.ic_dislike_thumb_selected
                            else -> {
                                R.drawable.ic_like_thumb_selected
                            }
                        }
                    } else {
                        when (likeType) {
                            LikeType.LIKE -> R.drawable.ic_like_thumb

                            LikeType.ANGRY -> R.drawable.ic_dislike_thumb
                            else -> {
                                R.drawable.ic_like_thumb
                            }
                        }
                    }
                }
            return i
        }


        @JvmStatic
        fun likeLayoutEmojiIcon(card: CommonAsset, likeIndex: Int, isInBottomSheet: Boolean): Int {
            val useDark = ThemeUtils.isNightMode() || isInBottomSheet
            val selected = likeLayoutIsCurrentLikeSelected(card, likeIndex)
            val i =
                if(useDark) {
                    if (selected) {
                        when (likeIndex) {
                            0 -> R.drawable.ic_emoji_smile_selected
                            1 -> R.drawable.ic_emoji_sad_selected
                            else -> R.drawable.ic_emoji_angry_selected
                        }
                    } else {
                        when (likeIndex) {
                            0 -> R.drawable.ic_emoji_smile_unselected_night
                            1 -> R.drawable.ic_emoji_sad_unselected_night
                            else -> R.drawable.ic_emoji_angry_unselected_night
                        }
                    }
                }else{
                    if (selected) {
                        when (likeIndex) {
                            0 -> R.drawable.ic_emoji_smile_selected
                            1 -> R.drawable.ic_emoji_sad_selected
                            else -> R.drawable.ic_emoji_angry_selected
                        }
                    } else {
                        when (likeIndex) {
                            0 -> R.drawable.ic_emoji_smile_unselected
                            1 -> R.drawable.ic_emoji_sad_unselected
                            else -> R.drawable.ic_emoji_angry_unselected
                        }
                    }
                }
            return i
        }

        @JvmStatic
        fun likeLayoutEmojiText(card: CommonAsset, likeIndex: Int): String {
            val likeCounts2 = likeLayoutSmileSadAndAngryCounts(card)
            val sum = likeCounts2.sum()
            return if (sum > 0 && likeIndex < likeCounts2.size) {
                StringBuilder().append((likeCounts2[likeIndex]*100) / sum).append("%").toString()
            } else {
                "0%"
            }
        }

        private val selectedColors = arrayOf(
                ContextCompat.getColor(CommonUtils.getApplication(), R.color.detail_emoji_smile_seleceted_text_color),
                ContextCompat.getColor(CommonUtils.getApplication(), R.color.detail_emoji_sad_seleceted_text_color),
                ContextCompat.getColor(CommonUtils.getApplication(), R.color.detail_emoji_angry_seleceted_text_color)
        )

        private val selectedColorsNight = arrayOf(
                ContextCompat.getColor(CommonUtils.getApplication(), R.color.detail_emoji_smile_seleceted_text_color),
                ContextCompat.getColor(CommonUtils.getApplication(), R.color.detail_emoji_all_unseleceted_text_color_night),
                ContextCompat.getColor(CommonUtils.getApplication(), R.color.detail_emoji_angry_seleceted_text_color)
        )

        @JvmStatic
        fun likeLayoutEmojiTextColor(card: CommonAsset, likeIndex: Int, isInBottomSheet: Boolean): Int {
            val useDark = ThemeUtils.isNightMode() || isInBottomSheet
            return if (likeLayoutIsCurrentLikeSelected(card, likeIndex)) {
                if (useDark) {
                    ContextCompat.getColor(CommonUtils.getApplication(), R.color.white)
                } else {
                    ContextCompat.getColor(CommonUtils.getApplication(), R.color.black)
                }
            } else if (useDark) {
                ContextCompat.getColor(CommonUtils.getApplication(), R.color.detail_emoji_all_unseleceted_text_color_night)
            } else {
                ContextCompat.getColor(CommonUtils.getApplication(), R.color.detail_emoji_all_unseleceted_text_color)
            }

        }

        @JvmStatic
        fun likeLayoutEmojiBgDrawable(card: CommonAsset, likeIndex: Int, isInBottomSheet: Boolean): Drawable {
            val useDark = ThemeUtils.isNightMode() || isInBottomSheet
            val selected = likeLayoutIsCurrentLikeSelected(card, likeIndex)

            val i =
                if(useDark) {
                    if (selected) {
                        R.drawable.news_detail_like_selected_night

                    } else {
                        R.drawable.news_detail_like_night
                    }
                }else{
                    if (selected) {
                        R.drawable.news_detail_like_selected_day
                    } else {
                        R.drawable.news_detail_like_day
                    }
                }
            return  CommonUtils.getDrawable(i)

        }
        @JvmStatic
        fun toggleLike(view: View, item: Any?, parentItem: Any? = null, vm: EmojiClickHandlingViewModel, isComment: Boolean?, commentType: String?, likeIndex: Int) {
            item?:return
            val likeType = arrayOf(LikeType.LIKE, LikeType.SAD, LikeType.ANGRY)
            vm.onEmojiClick(view, item, parentItem, likeType[likeIndex], isComment, commentType)
        }
        private fun EntityConfig2?.toLongOrNull(): Long? {
            if (this == null) return 0;
            return this.value.toLongOrNull()
        }

        fun likeLayoutSmileSadAndAngryCounts(card: CommonAsset): Array<Long> {
            val counts = card.i_counts() ?: return emptyArray()
            val like = counts.LIKE.toLongOrNull() ?: return emptyArray()
            val love = counts.LOVE.toLongOrNull() ?: return emptyArray()
            val happy = counts.HAPPY.toLongOrNull() ?: return emptyArray()
            val wow = counts.WOW.toLongOrNull() ?:  return emptyArray()
            val sad = counts.SAD.toLongOrNull() ?:  return emptyArray()
            val angry = counts.ANGRY.toLongOrNull() ?:  return emptyArray()
            val smile = like + love + happy + wow // :)
            return arrayOf(smile, sad, angry)
        }
        @JvmStatic
        fun commentLayoutLikeCounts(card: CommonAsset?): String {
            val counts = card?.i_counts() ?: return  ""
            val like = counts.LIKE.toLongOrNull() ?: 0
            val love = counts.LOVE.toLongOrNull() ?: 0
            val happy = counts.HAPPY.toLongOrNull() ?: 0
            val wow = counts.WOW.toLongOrNull() ?:  0
            val smile = like + love + happy + wow
            return smile.toString()
        }
        @JvmStatic
        fun commentLayoutDislikeCounts(card: CommonAsset?): String {
            val counts = card?.i_counts() ?: return  ""
            val sad = counts.SAD.toLongOrNull() ?:   0
            val angry = counts.ANGRY.toLongOrNull() ?:   0
            val dislike = sad + angry
            return dislike.toString()
        }

        fun debugCountsString(card: CommonAsset?): String {
            return card?.let { likeLayoutSmileSadAndAngryCounts(it) }?.toList().toString()
        }

        private fun likeLayoutSelectedLikeType(card: CommonAsset?): LikeType? {
            val selectedLikeType = card?.i_selectedLikeType()?.let { LikeType.fromName(it) }
            return selectedLikeType
        }
    }
}
