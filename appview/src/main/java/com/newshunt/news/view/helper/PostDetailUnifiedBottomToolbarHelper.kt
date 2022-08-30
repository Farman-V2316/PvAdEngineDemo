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
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.view.view.UniqueIdHelper
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.common.asset.AssetType2
import com.newshunt.dataentity.common.asset.CommonAsset
import com.newshunt.dataentity.common.asset.ParentIdHolderCommenAsset
import com.newshunt.dataentity.model.entity.GroupInfo
import com.newshunt.dhutil.helper.AppSettingsProvider
import com.newshunt.news.helper.NewsListCardLayoutUtil

/**
* For Bottom bar
* @author satosh.dhanyamraju
*/
class PostDetailUnifiedBottomToolbarHelper : PostDetailUnifiedToolbarHelper {
    private lateinit var menuClickListener: Toolbar.OnMenuItemClickListener
    private var parentFragment: Fragment? = null
    var commentDataBinding: ViewDataBinding? = null
    private var lifecycleOwner: LifecycleOwner? = null
    private lateinit var toolbar: Toolbar
    private val bookmarkHelper = ActionbarBookmarkHelper()
    private var actionMoreToBeHidden = false
    private val LOG_TAG: String = "PostDetailUnifiedBottom${UniqueIdHelper.getInstance().generateUniqueId()}"

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
                        R.layout.news_detail_comment_bar_2, commentBarHolder, false)
        commentDataBinding?.lifecycleOwner = lifecycleOwner
        commentDataBinding?.setVariable(BR.appSettingsProvider, AppSettingsProvider)
        commentDataBinding?.setVariable(BR.vm, cvm)
        card?.let { card1 ->
            commentDataBinding?.setVariable(BR.card, ParentIdHolderCommenAsset(parentStoryId, card1))
            bookmarkHelper.setUp(card, lifecycleOwner, commentDataBinding?.root?.findViewById(R.id.bookmark_tv))
        }

        commentDataBinding?.setVariable(BR.v, card?.i_type() == AssetType2.COMMENT.name)
        commentDataBinding?.setVariable(BR.isComment, card?.i_type() == AssetType2.COMMENT.name)
        commentBarHolder.findViewById<View>(R.id.share_count_tv)?.isEnabled = CardsBindUtils.isViewEnabledPrivacy(card) && (card?.i_isDeleted() != true)
        commentBarHolder.addView(commentDataBinding?.root)
        setupActionbarInBottom(commentDataBinding?.root, context)
        commentDataBinding?.executePendingBindings()
    }


    private fun updateCommentsBar(card: CommonAsset?,
                                   cvm: CardsViewModel,
                                   parentStoryId: String?) {
        commentDataBinding?.setVariable(BR.vm, cvm)
        card?.let { card1 ->
            commentDataBinding?.setVariable(BR.card, ParentIdHolderCommenAsset(parentStoryId, card1))
            bookmarkHelper.setUp(card, lifecycleOwner, commentDataBinding?.root?.findViewById(R.id.bookmark_tv))
        }
        commentDataBinding?.executePendingBindings()
    }

    override fun initActionBar(parent: View, parentFragment: Fragment?, activity: Activity?, menuClickListener: Toolbar.OnMenuItemClickListener, inflater: LayoutInflater) {
        Logger.d(LOG_TAG, "initActionBar: ")
        this.parentFragment = parentFragment
        this.menuClickListener = menuClickListener
        lifecycleOwner = parentFragment
        parent.findViewById<Toolbar>(R.id.actionbar).isGone = true
    }

    override fun isOnTheTop() = false

    private fun setupActionbarInBottom(parent: View?, activity: Activity?) {
        parent?:return
        toolbar = parent.findViewById<Toolbar>(R.id.actionbar_bottom)

        val toolbarBackButtonContainer: View = toolbar.findViewById(R.id
                .actionbar_back_button)
        toolbarBackButtonContainer.setOnClickListener {
            if (parentFragment is NewsDetailFragment2) {
                (parentFragment as? NewsDetailFragment2)?.handleActionBarBackPress(false)
            }
            activity?.onBackPressed()
        }
        toolbar.inflateMenu(R.menu.menu_post_detail)
        toolbar.menu.findItem(R.id.action_disclaimer_newsdetail).isVisible = false
        toolbar.setOnMenuItemClickListener(menuClickListener)
        NewsListCardLayoutUtil.manageLayoutDirection(toolbar)
        if(actionMoreToBeHidden) hideActionMoreView()
    }

    override fun setActionMoreVisible(b: Boolean) {
    }

    override fun setActionDisclaimerVisible(b: Boolean) {
    }

    override fun hideActionMoreView() {
        if (::toolbar.isInitialized) {
            toolbar.findViewById<View>(R.id.action_more_newsdetail)?.visibility = View.GONE
        } else {
            actionMoreToBeHidden = true;
        }
    }

    override fun updateToolbarVisibility(v: Int) {
    }


    override fun hideWithAnimation() {
    }

    override fun showWithAnimation() {
    }

    override fun setToolbar() {
    }

    override fun setTransparentToolbar(alpha:Float) {

    }

}