/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.appview.common.group.ui.adapter

import android.content.Context
import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.newshunt.appview.BR
import com.newshunt.appview.R
import com.newshunt.appview.common.group.viewmodel.GroupInvitationViewModel
import com.newshunt.appview.common.ui.adapter.UpdateableCardView
import com.newshunt.appview.common.ui.viewholder.CardsViewHolder
import com.newshunt.appview.databinding.GroupInviteOptionBinding
import com.newshunt.common.helper.common.AndroidUtils
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.model.entity.InvitationMedium
import com.newshunt.dataentity.model.entity.InvitationOption
import com.newshunt.helper.ImageUrlReplacer
import com.newshunt.sdk.network.image.Image

/**
 * Adapter and view holder implementation for invite mediums
 * <p>
 * Created by srikanth.ramaswamy on 09/27/2019.
 */
class InviteOptionsAdapter(context: Context,
                           private var inviteOptionsList: List<InvitationMedium>,
                           private val lifecycleOwner: LifecycleOwner?,
                           private val viewModel: GroupInvitationViewModel) : RecyclerView.Adapter<InviteOptionViewHolder>() {
    private val inflater = LayoutInflater.from(context)

    init {
        filterInstalledApps()
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InviteOptionViewHolder {
        val viewBinding = DataBindingUtil.inflate<GroupInviteOptionBinding>(inflater, R.layout.group_invite_option,
                parent, false)
        viewBinding.setVariable(BR.vm, viewModel)
        return InviteOptionViewHolder(viewBinding)
    }

    override fun getItemCount(): Int {
        return inviteOptionsList.size
    }

    override fun onBindViewHolder(holder: InviteOptionViewHolder, position: Int) {
        holder.bind(inviteOptionsList[position], lifecycleOwner, -1)
    }

    fun filterInstalledApps() {
        val newInviteOptionsList = inviteOptionsList.filter {
            (it.invitationOption != InvitationOption.APP ||
                    (it.invitationOption == InvitationOption.APP && AndroidUtils.isAppInstalled(it.invitationAppData.pkgName)))
        }
        inviteOptionsList = newInviteOptionsList
        notifyDataSetChanged()
    }
}

class InviteOptionViewHolder(private val viewBinding: GroupInviteOptionBinding) : CardsViewHolder(viewBinding
        .root) {
    private val width = CommonUtils.getDimension(R.dimen.invite_option_item_width)

    override fun bind(item: Any?, lifecycleOwner: LifecycleOwner?, cardPosition: Int) {
        if (item is InvitationMedium) {
            viewBinding.setVariable(BR.item, item)
            item.invitationAppData.iconUrl?.let {
                Image.load(ImageUrlReplacer.getQualifiedImageUrl(it, width, width))
                        .into(viewBinding.inviteOptionIcon)
            }
        }
        lifecycleOwner?.let {
            viewBinding.lifecycleOwner = it
        }
        viewBinding.executePendingBindings()
    }
}

class InviteItemDecoration : RecyclerView.ItemDecoration() {
    private val horzMargin = CommonUtils.getDimension(R.dimen.invite_apps_horzMargin)
    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        outRect.left = horzMargin
        val position = parent.getChildAdapterPosition(view)
        if (position == parent.adapter?.itemCount?.minus(1)) {
            outRect.right = horzMargin
        }
    }
}
