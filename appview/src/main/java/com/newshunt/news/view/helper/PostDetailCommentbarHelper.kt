/*
 *
 *  * Copyright (c) 2021 Newshunt. All rights reserved.
 *  
 */

package com.newshunt.news.view.helper

import android.annotation.SuppressLint
import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.newshunt.appview.BR
import com.newshunt.appview.R
import com.newshunt.appview.common.ui.helper.CardsBindUtils
import com.newshunt.appview.common.viewmodel.CardsViewModel
import com.newshunt.appview.databinding.NewsDetailCommentBarBinding
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.view.customview.fontview.NHTextView
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.common.asset.AssetType2
import com.newshunt.dataentity.common.asset.CommonAsset
import com.newshunt.dataentity.common.asset.CreatePostUiMode
import com.newshunt.dataentity.common.asset.ParentIdHolderCommenAsset
import com.newshunt.dataentity.model.entity.GroupInfo
import com.newshunt.deeplink.navigator.CommonNavigator
import kotlinx.android.synthetic.main.news_detail_comment_bar.view.edit_comment
import kotlinx.android.synthetic.main.news_detail_comment_bar.view.like_count_tv
import kotlinx.android.synthetic.main.news_detail_comment_bar.view.repost_icon_tv
import kotlinx.android.synthetic.main.news_detail_comment_bar.view.share_count_tv
/**
* Existing comment-bar implementation
* @author satosh.dhanyamraju
*/
class PostDetailCommentbarHelper : PostDetailCommentbarInterface {
    var commentDataBinding: ViewDataBinding? = null

    @SuppressLint("ClickableViewAccessibility")
    override fun inflateCommentsBar(isInBottomSheet: Boolean,
                                   context: Activity?,
                                   card: CommonAsset?,
                                   commentBarHolder: ViewGroup,
                                   layoutInflater: LayoutInflater,
                                   cvm: CardsViewModel,
                                   parentStoryId: String?,
                                   postId: String?,
                                   currentPageReferrer: PageReferrer?,
                                   groupInfo: GroupInfo?
    ) {
        if (isInBottomSheet) {
            commentBarHolder.visibility = View.GONE
            return
        }

        if (commentDataBinding != null) {
            updateCommentsBar(card, cvm, parentStoryId)
            return
        }


        commentDataBinding = DataBindingUtil
                .inflate<NewsDetailCommentBarBinding>(layoutInflater,
                        R.layout.news_detail_comment_bar, commentBarHolder, false)

        if (commentBarHolder.childCount > 0) {
            commentBarHolder.removeAllViews()
        }

        commentDataBinding?.setVariable(BR.vm, cvm)
        card?.let { card1 ->
            commentDataBinding?.setVariable(BR.card, ParentIdHolderCommenAsset(parentStoryId, card1))
        }

        commentDataBinding?.setVariable(BR.v, card?.i_type() == AssetType2.COMMENT.name)
        commentDataBinding?.setVariable(BR.isComment, card?.i_type() == AssetType2.COMMENT.name)
        commentBarHolder.addView(commentDataBinding?.root)
        commentBarHolder.edit_comment.isEnabled = CardsBindUtils.isCommentsEnabled(card) && (card?.i_isDeleted() != true)
        commentBarHolder.like_count_tv.isEnabled = card?.i_isDeleted() != true
        commentBarHolder.repost_icon_tv.isEnabled = CardsBindUtils.isViewEnabledPrivacy(card) && (card?.i_isDeleted() != true)
        commentBarHolder.share_count_tv.isEnabled = CardsBindUtils.isViewEnabledPrivacy(card) && (card?.i_isDeleted() != true)
        commentDataBinding?.root?.findViewById<NHTextView>(R.id.edit_comment)?.setOnClickListener { v ->
            try {
                val intent = CommonNavigator.getPostCreationIntent(
                        postId,
                        CreatePostUiMode.COMMENT,
                        null,
                        currentPageReferrer,
                        null,
                        card?.i_source()?.id,
                        card?.i_source()?.type,
                        card?.i_parentPostId(), groupInfo)
                context?.startActivityForResult(intent, 0)
            } catch (ex: Exception) {
                // Activity not found
                Logger.caughtException(ex)
            }
        }
        commentDataBinding?.executePendingBindings()
    }


    private fun updateCommentsBar(card: CommonAsset?,
                                  cvm: CardsViewModel,
                                  parentStoryId: String?) {
        commentDataBinding?.setVariable(BR.vm, cvm)
        card?.let { card1 ->
            commentDataBinding?.setVariable(BR.card, ParentIdHolderCommenAsset(parentStoryId, card1))
        }
        commentDataBinding?.executePendingBindings()
    }


}