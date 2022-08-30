/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */

package com.newshunt.news.view.adapter

import android.content.Context
import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.newshunt.appview.R
import com.newshunt.common.helper.common.AndroidUtils
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dhutil.helper.theme.ThemeUtils
import com.newshunt.news.view.viewholder.SeeAllClickListener
import com.newshunt.onboarding.view.adapter.LanguageSelectViewHolderV2
import com.newshunt.onboarding.view.listener.LanguageSelectListener

/**
 * @author anshul.jain
 */
class LanguageSelectionAdapter(val languageList: List<com.newshunt.dataentity.common.model.entity.language.Language>, val context: Context, val
listener: LanguageSelectListener, val seeAllListener: SeeAllClickListener) : androidx.recyclerview.widget.RecyclerView
.Adapter<RecyclerView.ViewHolder>() {

    private val itemCount = languageList.size + 1
    private val LANGUAGE_ITEM = 1
    private val SEE_ALL_ITEM = 2

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): androidx.recyclerview.widget.RecyclerView.ViewHolder {
        if (viewType == LANGUAGE_ITEM) {
            val view = LayoutInflater.from(parent.context).inflate(com.newshunt.onboarding.R.layout
                    .layout_language_card_individual_item, parent, false)
            val ratio = CommonUtils.getDeviceScreenWidthInDp() / 360f
            val viewWidth = CommonUtils.getDimension(R.dimen.language_card_individual_item_w) * ratio
            val viewHeight = CommonUtils.getDimension(R.dimen.language_card_individual_item_h) * ratio
            val radius = CommonUtils.getDimension(com
                    .newshunt.onboarding.R.dimen.onboarding_popup_item_radius) * ratio
            return LanguageSelectViewHolderV2(view, listener, radius.toInt(), viewWidth.toInt(),
                    viewHeight.toInt(), true, true)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout
                    .language_selection_see_all_viewholder, parent, false)
            return SeeAllViewHolder(view, parent.context, seeAllListener)
        }
    }

    override fun getItemCount() = itemCount

    override fun getItemViewType(position: Int): Int {
        return if (position == itemCount - 1) {
            SEE_ALL_ITEM
        } else {
            LANGUAGE_ITEM
        }
    }

    override fun onBindViewHolder(holder: androidx.recyclerview.widget.RecyclerView.ViewHolder, position: Int) {
        if (position != itemCount - 1) {
            (holder as? LanguageSelectViewHolderV2)?.onBindView(languageList[position])
        }
    }
}

class SeeAllViewHolder(val view: View, val context: Context?, val seeAllListener: SeeAllClickListener) : androidx.recyclerview.widget.RecyclerView.ViewHolder
(view) {

    init {
        val ratio = CommonUtils.getDeviceScreenWidthInDp() / 360f
        val radius = CommonUtils.getDimension(com
                .newshunt.onboarding.R.dimen.onboarding_popup_item_radius) * ratio
        val borderStrokeWidth = CommonUtils.getDimension(com.newshunt.onboarding.R.dimen.onboarding_item_stroke_width)
        val drawable = AndroidUtils.makeRoundedRectDrawable(radius.toInt(), ThemeUtils
                .getThemeColorByAttribute(view.context, R.attr.default_background), borderStrokeWidth,
                CommonUtils.getColor(R.color.language_see_all_button_background))
        val parent = view.findViewById<ConstraintLayout>(R.id.see_all_parent)
        parent.background = drawable
        val viewWidth = CommonUtils.getDimension(R.dimen.language_card_individual_item_w) * ratio
        val viewHeight = CommonUtils.getDimension(R.dimen.language_card_individual_item_h) * ratio
        val params = view.layoutParams
        params.width = viewWidth.toInt()
        params.height = viewHeight.toInt()
        view.layoutParams = params
        view.setOnClickListener {
            seeAllListener.onSeeAllClicked()
        }
    }
}

class LanguageItemDecoration(val rightMargin: Int) : androidx.recyclerview.widget.RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {

        outRect.right = rightMargin
    }
}
