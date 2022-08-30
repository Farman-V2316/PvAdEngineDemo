/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.news.view.viewholder

import android.view.View
import android.view.animation.AlphaAnimation
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import androidx.recyclerview.widget.RecyclerView
import com.newshunt.appview.BR
import com.newshunt.appview.common.entity.CardsPojo
import com.newshunt.appview.common.viewmodel.CardsViewModel
import com.newshunt.appview.databinding.NewsDetailChunk1VhBinding
import com.newshunt.appview.databinding.NewsDetailChunk2VhBinding
import com.newshunt.common.helper.common.Logger
import com.newshunt.dataentity.common.asset.CommonAsset
import com.newshunt.dataentity.common.asset.DiscussionPojo
import com.newshunt.dataentity.common.asset.LikeListPojo
import com.newshunt.dataentity.common.asset.PhotoChildPojo
import com.newshunt.dataentity.common.asset.SuggestedFollowsPojo
import com.newshunt.dataentity.news.model.entity.DetailCardType
import com.newshunt.dataentity.social.entity.Interaction
import com.newshunt.dataentity.social.entity.ReplyCount
import com.newshunt.news.view.fragment.UpdateableDetailView
import com.newshunt.news.viewmodel.DetailsViewModel
import com.newshunt.viral.utils.visibility_utils.VisibilityAwareViewHolder
import com.newshunt.viral.utils.visibility_utils.VisibilityCalculator

class NHRecyclerViewHolder(val viewType: Int,
                                private val viewBinding: ViewDataBinding,
                                private val onItemVisible: ((String?) -> Unit)?,
                                private val vC: VisibilityCalculator?,
                                lifecycleOwner: LifecycleOwner,
                           private val uniqueRequestId: Int) :
	RecyclerView.ViewHolder(viewBinding.root), UpdateableDetailView, VisibilityAwareViewHolder, LifecycleObserver {

	private var visibilityCalled = false
	private var viewParam: String? = null

	private var webViewResumeCalled = false


	init {
		lifecycleOwner.lifecycle.addObserver(this)
	}

	override fun onVisible(viewVisibilityPercentage: Int, percentageOfScreen: Float) {
		Logger.d("DetailViewHolder" , "visiblity is $viewVisibilityPercentage")
		if (!visibilityCalled && viewVisibilityPercentage > 50) {
			visibilityCalled = true
			onItemVisible?.invoke(viewParam)
		}

		if (!webViewResumeCalled && viewVisibilityPercentage > 0) {
			webViewResumeCalled = true
			handleWebViewResume()
		}

		vC?.update()
	}

	override fun onInVisible() {
		// Not interested
		webViewResumeCalled = false
		handleWebViewPause()
	}

	override fun onUserLeftFragment() {
		// Not interested
	}

	override fun onUserEnteredFragment(viewVisibilityPercentage: Int, percentageOfScreen: Float) {
		// Not interested
	}

	override fun onBindView(position: Int,
	                        cvm: CardsViewModel?,
	                        dvm: DetailsViewModel?,
	                        card: CommonAsset?,
	                        parentCard: CommonAsset?,
	                        suggedtedFollowsPojo: SuggestedFollowsPojo?,
							likedListPojo: LikeListPojo?,
							myInteraction: Interaction?,
	                        discussionPojo: DiscussionPojo?,
	                        relatedStories: CardsPojo?,
	                        associatedStories: CardsPojo?,
	                        photoChildPojo: PhotoChildPojo?,
	                        isInBottomSheet: Boolean,
	                        replyCountList: List<ReplyCount>?) {
		card.let {
			viewBinding.setVariable(BR.card, card)
		}

		cvm.let {
			viewBinding.setVariable(BR.vm, cvm)
		}

		if (parentCard != null) {
			viewBinding.setVariable(BR.parent_card, parentCard)
			viewBinding.setVariable(BR.isCommentView, true)
		}

		suggedtedFollowsPojo.let {
			viewBinding.setVariable(BR.suggestedFollows, suggedtedFollowsPojo)
		}

		viewBinding.setVariable(BR.isInBottomSheet, isInBottomSheet)

		discussionPojo.let {
			viewBinding.setVariable(BR.discussionPojo, discussionPojo)
		}

		associatedStories.let {
			viewBinding.setVariable(BR.associations, associatedStories)
		}

		relatedStories.let {
			viewBinding.setVariable(BR.relatedstories, relatedStories)
		}

		if ((position >= (photoChildPojo?.index ?: -1)) && ((position - (photoChildPojo?.index
				?: 0)) < (photoChildPojo?.data?.size ?: 0))) {
			// Photo Child available
			val photoId = position - (photoChildPojo?.index ?: 0)
			viewBinding.setVariable(BR.photo_child, photoChildPojo?.data?.get(photoId))
		}

		if (discussionPojo?.data?.isNotEmpty() == true &&
			position >= (dvm?.discussionIndex ?: 0) &&
			position < ((dvm?.discussionIndex ?: 0) + (discussionPojo.data?.size ?: 0))) {
			val discussionPosition = position - (dvm?.discussionIndex ?: 0)
			val discussion = discussionPojo.data!![discussionPosition]
			viewBinding.setVariable(BR.discussion, discussion)
			val discussionId = discussion.i_id()
			viewParam = discussionId
			val replyTs = discussion.i_counts()?.COMMENTS?.ts ?: Long.MAX_VALUE
			var replyCount = 0
			replyCountList?.forEach {
				if (it.parentId == discussionId && it.creationDate > replyTs) {
					replyCount++
				}
			}

			viewBinding.setVariable(BR.replyCount, replyCount)
			setFadeAnimation(viewBinding.root)

			if (viewType == DetailCardType.DISCUSSION.index) {
				//prefetch the next page 3 item ahead or if the first page response is less than three then prefetch with that size ahead
				val size = discussionPojo.data?.size ?: 0
				val prefetchSize = if ( size< 3) {
					discussionPojo.data?.size ?: 0
				} else {
					3
				}
				if (position == ((dvm?.discussionIndex ?: 0) + size) - prefetchSize) {
					dvm?.fetchNextDiscussionPage()
				}
			}
		}

		viewBinding.executePendingBindings()
	}

	private val FADE_DURATION = 1000L

	private fun setFadeAnimation(view: View) {
		val anim = AlphaAnimation(0.0f, 1.0f)
		anim.setDuration(FADE_DURATION)
		view.startAnimation(anim)
	}

	@OnLifecycleEvent(Lifecycle.Event.ON_STOP)
	fun onStop() {
		Logger.d("DetailViewHolder", "onStop $adapterPosition")
		handleWebViewPause()
	}

	@OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
	fun onResume() {
		Logger.d("DetailViewHolder", "onResume $adapterPosition")
		handleWebViewResume()
	}

	private fun handleWebViewResume() {
		if (viewBinding is NewsDetailChunk1VhBinding) {
			viewBinding.newsDetailsWebview.webviewResume()
			Logger.d("WebViewDetail" , "Calling resume on chunk 1 for $uniqueRequestId")
		} else if (viewBinding is NewsDetailChunk2VhBinding) {
			Logger.d("WebViewDetail" , "Calling resume on chunk 2 for $uniqueRequestId")
			viewBinding.newsDetailsWebview.webviewResume()
		}
	}

	private fun handleWebViewPause() {
		if (viewBinding is NewsDetailChunk1VhBinding) {
			Logger.d("WebViewDetail" , "Calling pause on chunk 1 for $uniqueRequestId")
			viewBinding.newsDetailsWebview.webviewPaused()
		} else if (viewBinding is NewsDetailChunk2VhBinding) {
			Logger.d("WebViewDetail" , "Calling pause on chunk 2 for $uniqueRequestId")
			viewBinding.newsDetailsWebview.webviewPaused()
		}
	}
}