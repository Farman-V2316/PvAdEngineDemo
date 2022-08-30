/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.news.view.adapter

import androidx.constraintlayout.widget.ConstraintLayout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.newshunt.common.helper.common.Constants
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.common.view.customview.fontview.NHTextView
import com.newshunt.dataentity.news.model.entity.MenuL1Meta
import com.newshunt.dhutil.R
import com.newshunt.dhutil.helper.preference.UserPreferenceUtil
import com.newshunt.dhutil.helper.theme.ThemeUtils
import com.newshunt.helper.ImageUrlReplacer

@Deprecated(message = "Not to be used")
class MenuOptionsAdapter(private val menuOptionItemClickListener:
                         MenuOptionItemClickListener?) :
        androidx.recyclerview.widget.RecyclerView
        .Adapter<MenuOptionViewHolder>() {
    private val metaList: MutableList<MenuL1Meta> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuOptionViewHolder {
        val v = MenuOptionViewHolder(createView(parent), menuOptionItemClickListener)
        return v
    }

    override fun getItemCount(): Int {
        return metaList.size
    }

    override fun onBindViewHolder(holder: MenuOptionViewHolder, position: Int) {
        holder.bindData(metaList.get(position))
    }

    private fun createView(parent: ViewGroup): View {
        return LayoutInflater.from(parent.context).inflate(R.layout.dislike_option_item, null)
    }

    fun updateItem(items: List<MenuL1Meta>) {
        metaList.clear()
        metaList.addAll(items)
        notifyDataSetChanged()
    }
}

interface MenuOptionItemClickListener {
    fun onMenuOptionClick(index: Int)
}


class MenuOptionViewHolder(itemview: View, onclickCallback: MenuOptionItemClickListener?) :
        androidx.recyclerview.widget.RecyclerView.ViewHolder
        (itemview) {
    private var titleView: NHTextView = itemview.findViewById(R.id.dislike_option_title)
    private var iconView: ImageView = itemview.findViewById(R.id.dislike_options_icon)
    private val isNightMode = ThemeUtils.isNightMode()
    private val iconSize = CommonUtils.getDimension(R.dimen.dislike_l1_opt_icon_size)
    init {
        itemview.setOnClickListener({ onclickCallback?.onMenuOptionClick(adapterPosition) })
        itemview.layoutParams = ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        if (isNightMode) {
            titleView.setTextColor(CommonUtils.getColor(R.color.dislike_l1_option_color_night))
        } else {
            titleView.setTextColor(CommonUtils.getColor(R.color.color_333333))
        }
        if (CommonUtils.equals(UserPreferenceUtil.getUserNavigationLanguage(), Constants.URDU_LANGUAGE_CODE)) {
            titleView.layoutDirection = View.LAYOUT_DIRECTION_RTL
        } else {
            titleView.layoutDirection = View.LAYOUT_DIRECTION_LTR
        }
    }

    fun bindData(metaItem: MenuL1Meta) {
        titleView.text = metaItem.title
        val url = if (isNightMode) metaItem.nIcon else metaItem.icon
        if (CommonUtils.isEmpty(url)) {
            iconView.visibility = View.GONE
        } else {
            Glide.with(itemView.context).load(ImageUrlReplacer.getQualifiedImageUrl(url,
                    iconSize, iconSize)).into(iconView)
        }
    }
}