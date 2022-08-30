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
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isGone
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import com.newshunt.appview.BR
import com.newshunt.appview.R
import com.newshunt.appview.common.ui.fragment.NewsDetailFragment2
import com.newshunt.appview.common.ui.helper.CardsBindUtils
import com.newshunt.appview.common.viewmodel.CardsViewModel
import com.newshunt.appview.databinding.NewsDetailCommentBarBinding
import com.newshunt.appview.databinding.NewsDetailSocialIconsBinding
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.view.view.UniqueIdHelper
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.common.asset.AssetType2
import com.newshunt.dataentity.common.asset.CommonAsset
import com.newshunt.dataentity.common.asset.ParentIdHolderCommenAsset
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.model.entity.GroupInfo
import com.newshunt.dhutil.helper.AppSettingsProvider
import com.newshunt.dhutil.helper.theme.ThemeUtils

/**
 * For top bar
 * @author satosh.dhanyamraju
 */
class PostDetailUnifiedTopToolbarHelper : PostDetailUnifiedToolbarHelper {
    private var lifecycleOwner: LifecycleOwner? = null
    var commentDataBinding: NewsDetailSocialIconsBinding? = null
    lateinit var toolbar: Toolbar
    private val LOG_TAG: String =
        "PostDetailUnifiedTopToo${UniqueIdHelper.getInstance().generateUniqueId()}"
    private val bookmarkHelper = ActionbarBookmarkHelper()

    @SuppressLint("ClickableViewAccessibility")
    override fun inflateCommentsBar(
        isInBottomSheet: Boolean,
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
        Logger.d(LOG_TAG, "inflateCommentsBar: ")
        if (isInBottomSheet) {
            commentBarHolder.visibility = View.GONE
            return
        }

        if (commentDataBinding != null) {
            updateCommentsBar(card, cvm, parentStoryId)
            return
        }
        // TODO: 16/6/21 ignore holder and inflate to topbar; handle commentbar called before actionbar case -> detach and change parent
        //todo save button, pull it out from menu; for both top and bottom
        val container =
            if (::toolbar.isInitialized) toolbar.findViewById<LinearLayout>(R.id.social_icon_holder) else null
        val nonNulContainer = if (container != null) {
            commentBarHolder.isGone = true
            container
        } else commentBarHolder
        commentDataBinding = DataBindingUtil
            .inflate<NewsDetailSocialIconsBinding>(
                layoutInflater,
                R.layout.news_detail_social_icons, nonNulContainer, false
            )

        commentDataBinding?.setVariable(BR.vm, cvm)
        card?.let { card1 ->
            commentDataBinding?.setVariable(
                BR.card,
                ParentIdHolderCommenAsset(parentStoryId, card1)
            )
//            bookmarkHelper.setUp(
//                card,
//                lifecycleOwner,
//                commentDataBinding?.root?.findViewById(R.id.bookmark_tv)
//            )
        }

        commentDataBinding?.lifecycleOwner = lifecycleOwner
        commentDataBinding?.setVariable(BR.appSettingsProvider, AppSettingsProvider)
        commentDataBinding?.setVariable(BR.v, card?.i_type() == AssetType2.COMMENT.name)
        commentDataBinding?.setVariable(BR.isComment, card?.i_type() == AssetType2.COMMENT.name)
        commentBarHolder.findViewById<View>(R.id.share_count_tv)?.isEnabled =
            CardsBindUtils.isViewEnabledPrivacy(card) && (card?.i_isDeleted() != true)
        // todo handle insidedetail page :  commentBarHolder.social_icon_container_ll.like_count_tv.isEnabled = card?.i_isDeleted() != true
        nonNulContainer.addView(commentDataBinding?.root)
        commentDataBinding?.executePendingBindings()
    }


    private fun updateCommentsBar(
        card: CommonAsset?,
        cvm: CardsViewModel,
        parentStoryId: String?
    ) {
        commentDataBinding?.setVariable(BR.vm, cvm)
        card?.let { card1 ->
            commentDataBinding?.setVariable(
                BR.card,
                ParentIdHolderCommenAsset(parentStoryId, card1)
            )
//            bookmarkHelper.setUp(
//                card,
//                lifecycleOwner,
//                commentDataBinding?.root?.findViewById(R.id.bookmark_tv)
//            )
        }
        commentDataBinding?.executePendingBindings()
    }

    override fun initActionBar(
        parent: View,
        parentFragment: Fragment?,
        activity: Activity?,
        menuClickListener: Toolbar.OnMenuItemClickListener,
        inflater: LayoutInflater
    ) {
        Logger.d(LOG_TAG, "initActionBar: ")
        toolbar = parent.findViewById<Toolbar>(R.id.actionbar)

        val toolbarBackButtonContainer: RelativeLayout = toolbar.findViewById(
            R.id
                .actionbar_back_button_layout
        )
        toolbarBackButtonContainer.setOnClickListener {
            if (parentFragment is NewsDetailFragment2) {
                (parentFragment as? NewsDetailFragment2)?.handleActionBarBackPress(false)
            }
            activity?.onBackPressed()
        }
        lifecycleOwner = parentFragment
        val backIcon =
            if (ThemeUtils.isNightMode()) R.drawable.ic_news_detail_comment_bar_back_arrow_night else
                R.drawable.ic_news_detail_comment_bar_back_arrow
        toolbar.findViewById<ImageView>(R.id.actionbar_back_button).setImageResource(backIcon)

        toolbar.inflateMenu(R.menu.menu_post_detail)
        toolbar.setOnMenuItemClickListener(menuClickListener)
    }

    override fun isOnTheTop() = true
    override fun setTransparentToolbar(alpha: Float) {
        var baseColor: Int
        if (ThemeUtils.isNightMode()) {
            baseColor = (CommonUtils.getColor(R.color.theme_night_background))
        } else {
            baseColor = (CommonUtils.getColor(R.color.theme_action_bar_day_color))
        }
        if (alpha < 0.5) {
            setTransparentToolbar(true)
        } else {
            setTransparentToolbar(false)
        }
        toolbar.setBackgroundColor(getColorWithAlpha(alpha, baseColor))

    }


    private fun setTransparentToolbar(isTransparent: Boolean) {
        val backIcon =
            if (ThemeUtils.isNightMode() || isTransparent)
                R.drawable.ic_news_detail_comment_bar_back_arrow_night
            else
                R.drawable.ic_news_detail_comment_bar_back_arrow

        val menuIcon =
            if (ThemeUtils.isNightMode() || isTransparent)
                R.drawable.ic_three_dots_detail_night
            else
                R.drawable.ic_three_dots_detail

        toolbar.findViewById<ImageView>(R.id.actionbar_back_button).setImageResource(backIcon)

        commentDataBinding?.setVariable(BR.isTransparent, isTransparent)
        commentDataBinding?.executePendingBindings()
        toolbar.menu.findItem(R.id.action_more_newsdetail).icon =
            CommonUtils.getDrawable(menuIcon)
    }


    fun getColorWithAlpha(alpha: Float, baseColor: Int): Int {
        val a = Math.min(255, Math.max(0, (alpha * 255).toInt())) shl 24
        val rgb = 0x00ffffff and baseColor
        return a + rgb
    }

override fun setToolbar() {
    if (ThemeUtils.isNightMode()) {
        toolbar.setBackgroundColor(CommonUtils.getColor(R.color.theme_night_background))
    } else {
        toolbar.setBackgroundColor(CommonUtils.getColor(R.color.theme_action_bar_day_color))
    }
}

override fun setActionMoreVisible(b: Boolean) {
    toolbar.menu.findItem(R.id.action_more_newsdetail).isVisible = b
}

override fun setActionDisclaimerVisible(b: Boolean) {
    toolbar.menu.findItem(R.id.action_disclaimer_newsdetail).isVisible = b
}

override fun hideActionMoreView() {
    toolbar.findViewById<View>(R.id.action_more_newsdetail)?.visibility = View.GONE
}

override fun updateToolbarVisibility(v: Int) {
    toolbar.visibility = v
}


override fun hideWithAnimation() {
}

override fun showWithAnimation() {
}
}