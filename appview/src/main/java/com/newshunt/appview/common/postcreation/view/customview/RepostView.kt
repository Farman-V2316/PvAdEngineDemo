/*
 * Created by Rahul Ravindran at 8/10/19 6:04 PM
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.appview.common.postcreation.view.customview

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import com.newshunt.appview.BR
import com.newshunt.appview.R
import com.newshunt.appview.databinding.RepostViewLayoutBinding
import com.newshunt.common.helper.preference.GenericAppStatePreference
import com.newshunt.dataentity.common.asset.Format
import com.newshunt.dataentity.common.asset.PostEntity
import com.newshunt.dataentity.common.asset.RepostDisplayType
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dhutil.helper.LiveSharedPreference

class RepostView : ConstraintLayout {
    private var repostEntity: PostEntity? = null
    private var showRepostLoader = false
    private lateinit var repostViewBinding: RepostViewLayoutBinding

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context,
            attrs, defStyleAttr) {
        init()
    }

    private fun init() {
        initView()
    }

    private fun initView() {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        repostViewBinding = DataBindingUtil.inflate<RepostViewLayoutBinding>(inflater,
                R.layout.repost_view_layout,
                this,
                true)
    }

    fun showRepostLoader(value: Boolean){
        showRepostLoader = value
        repostViewBinding.showLoader = showRepostLoader
        repostViewBinding.executePendingBindings()
    }

    fun setRepostData(data: PostEntity) {
        this.repostEntity = data
        updateView()
        repostViewBinding.executePendingBindings()
    }

    fun hideView(){
        removeAllViews()
    }

    fun updateView() {
        if (repostEntity != null) {
            val nsfwLiveData = LiveSharedPreference.pref(GenericAppStatePreference
                    .SHOW_NSFW_FILTER, context, true)
            repostViewBinding.setVariable(BR.item, repostEntity)
            repostViewBinding.setVariable(BR.nsfwLiveData, nsfwLiveData)
            repostViewBinding.setVariable(BR.cardTypeIndex, getDisplayCardTypeForRepost(repostEntity!!))
        }
    }

    private fun getDisplayCardTypeForRepost(repostEntity: PostEntity): Int {
        val format = repostEntity.i_format()
        return if (format == Format.HTML) {
            if (repostEntity.i_linkAsset() != null) {
                RepostDisplayType.REPOST_OEMBED.index
            } else if (CommonUtils.isEmpty(repostEntity.i_title()) &&
                    CommonUtils.isEmpty(repostEntity.i_content()) &&
                    repostEntity.i_thumbnailUrls() != null) {
                RepostDisplayType.REPOST_HERO.index
            } else {
                RepostDisplayType.REPOST_NORMAL.index
            }
        } else if (format == Format.POLL) {
            RepostDisplayType.REPOST_POLL.index
        } else {
            RepostDisplayType.REPOST_NORMAL.index
        }
    }
}