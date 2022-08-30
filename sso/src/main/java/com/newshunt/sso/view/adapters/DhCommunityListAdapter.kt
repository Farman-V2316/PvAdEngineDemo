/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.sso.view.adapters

import android.content.Context
import android.graphics.Rect
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.request.RequestOptions
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dhutil.helper.theme.ThemeUtils
import com.newshunt.helper.ImageUrlReplacer
import com.newshunt.sdk.network.image.Image
import com.newshunt.sso.R

/**
 * @author anshul.jain
 */
class DHProfilesAdapter(private val profileList: List<String>, private val context: Context) :
        RecyclerView.Adapter<DhProfileViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DhProfileViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.layout_third_party_view,
                parent, false)
        return DhProfileViewHolder(view)
    }

    override fun getItemCount(): Int {
        return profileList.size
    }

    override fun onBindViewHolder(holder: DhProfileViewHolder, position: Int) {
        holder.update(profileList[position])
    }

}

class DhProfileViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
    private val profileImage = view.findViewById<ImageView>(R.id.user_profile_image)

    init {
        val drawable = profileImage.background as? GradientDrawable
        drawable?.setStroke(CommonUtils.getDimension(R.dimen.divider_height), ThemeUtils
                .getThemeColorByAttribute(view.context, R.attr.followed_entities_background_color))

    }

    fun update(profile: String) {
        val imageUrl = ImageUrlReplacer.getQualifiedImageUrl(profile, CommonUtils.getDimension(R.dimen
                .user_profile_image_w), CommonUtils.getDimension(R.dimen.user_profile_image_w))
        Image.load(imageUrl, true).apply(RequestOptions.circleCropTransform()).into(profileImage)
    }
}

class ProfileListItemDecorator : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val pos = parent.getChildAdapterPosition(view)
        if (pos != 0) {
            outRect.left = -18
        }
    }
}