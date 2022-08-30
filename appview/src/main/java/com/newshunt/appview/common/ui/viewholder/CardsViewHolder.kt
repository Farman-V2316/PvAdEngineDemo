package com.newshunt.appview.common.ui.viewholder

import android.view.View
import android.widget.ImageView
import com.newshunt.appview.R
import com.newshunt.appview.common.ui.adapter.UpdateableCardView
import java.util.ArrayList

abstract class CardsViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView
.ViewHolder(itemView), UpdateableCardView {
    protected val clearableImageViews: MutableList<ImageView?> = ArrayList()
    var displayTypeIndex = -1

    override fun recycleView() {
        clearableImageViews.forEach {
            if (it != null) {
                it.setImageBitmap(null)
                it.setImageDrawable(null)
                it.setTag(R.id.viral_image_extra, null)
            }
        }
    }
}