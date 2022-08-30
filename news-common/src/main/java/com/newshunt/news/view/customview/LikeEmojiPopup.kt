/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.news.view.customview

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.PopupWindow
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.newshunt.common.view.customview.SpanningLinearLayoutManager
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.social.entity.LikeType
import com.newshunt.news.common.R
import com.newshunt.news.common.BR
import com.newshunt.news.common.databinding.LikeEmojiPopupItemBinding
import com.newshunt.news.viewmodel.EmojiClickHandlingViewModel

class LikeEmojiPopup(private val context: Context,
                     rootView: View,
                     private val item: Any? = null,
                     private val parentItem: Any? = null,
                     private val vm: EmojiClickHandlingViewModel,
                     private val isComment: Boolean?,
                     private val commentType: String?) : PopupWindow(rootView) {
    private val recyclerView: RecyclerView? = rootView.findViewById(R.id.emoticon_recycler_view)

    init {
        isFocusable = true
        isOutsideTouchable = true
        width = CommonUtils.getDimension(R.dimen.like_popup_width)
        height = ViewGroup.LayoutParams.WRAP_CONTENT
        setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        elevation = 5f
        buildRecyclerView(context)
    }

    private fun buildRecyclerView(context: Context) {
        recyclerView ?: return
        val linearLayoutManager = SpanningLinearLayoutManager(context, CommonUtils.getDimension(R.dimen.like_popup_width))
        val tvEmojiAdapter = LikeEmojiAdapter(item, parentItem, vm, this, isComment, commentType)
        linearLayoutManager.orientation = LinearLayoutManager.HORIZONTAL
        recyclerView.layoutManager = linearLayoutManager
        recyclerView.adapter = tvEmojiAdapter
        val animation = AnimationUtils.loadLayoutAnimation(context, R.anim.emoticon_layout_animation_enter)
        recyclerView.layoutAnimation = animation
    }

    fun dismissPopup() {
        recyclerView ?: return
        recyclerView.layoutAnimation = AnimationUtils
                .loadLayoutAnimation(context, R.anim.emoticon_layout_animation_exit)
        recyclerView.startLayoutAnimation()
        Handler().postDelayed({ dismiss() }, 125)
    }
}


class LikeEmojiAdapter(private val item: Any?,
                       private val parentItem: Any? = null,
                       private val vm: EmojiClickHandlingViewModel,
                       private val popupView: LikeEmojiPopup,
                       private val isComment: Boolean?,
                       private val commentType: String?) : RecyclerView.Adapter<SimpleEmojiViewHolder>() {

    private val emojis = arrayOf(LikeType.LIKE, LikeType.SAD, LikeType.ANGRY)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SimpleEmojiViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val viewDataBinding = DataBindingUtil.inflate<LikeEmojiPopupItemBinding>(
                inflater,
                R.layout.like_emoji_popup_item,
                parent,
                false)
        viewDataBinding.popupView = popupView
        viewDataBinding.vm = vm
        viewDataBinding.isComment = isComment
        viewDataBinding.isComment = isComment
        viewDataBinding.commentType = commentType
        return SimpleEmojiViewHolder(viewDataBinding)
    }

    override fun onBindViewHolder(holder: SimpleEmojiViewHolder, position: Int) {
        holder.bind(item, parentItem ,emojis[position])
    }

    override fun getItemCount(): Int {
        return emojis.size
    }

}


class SimpleEmojiViewHolder(private val viewBinding: ViewDataBinding) : RecyclerView.ViewHolder(viewBinding.root) {

    fun bind(item: Any?, parentItem: Any?, likeType: LikeType) {
        viewBinding.setVariable(BR.item, item)
        viewBinding.setVariable(BR.parentItem, parentItem)
        viewBinding.setVariable(BR.emojiType, likeType)
        viewBinding.executePendingBindings()
    }
}