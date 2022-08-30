package com.newshunt.appview.common.ui.viewholder

import android.content.Context
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.dailyhunt.tv.players.autoplay.VideoRequester
import com.newshunt.appview.BR
import com.newshunt.appview.R
import com.newshunt.appview.common.ui.adapter.UpdateableCardView
import com.newshunt.appview.common.ui.adapter.findCardTypeIndex
import com.newshunt.appview.common.ui.adapter.getViewHolder
import com.newshunt.appview.common.viewmodel.CardsViewModel
import com.newshunt.common.helper.common.Constants
import com.newshunt.dataentity.common.asset.CommonAsset
import com.newshunt.news.util.EventDedupHelper

class PostProgressViewHolder(private val viewBinding: ViewDataBinding, val cardsViewModel:
CardsViewModel, val parent: ViewGroup, val detailView: Boolean, val videoRequester:
                             VideoRequester?, val context: Context?, val eventDedupHelper:
                             EventDedupHelper,
                             private val viewPool: RecyclerView.RecycledViewPool) : CardsViewHolder(viewBinding.root),
        UpdateableCardView {
    private var prevCard: CommonAsset? = null
    private var childViewHolder: RecyclerView.ViewHolder? = null
    override fun bind(item: Any?, lifecycleOwner: LifecycleOwner?, cardPosition: Int) {
        if ((item as? CommonAsset)?.isLocalCard() == true) {
            val cardType = findCardTypeIndex(item, Constants.ITEM_LOCATION_LOCAL)
            if (item.i_id() != prevCard?.i_id()) {
                childViewHolder = getViewHolder(
                        displayCardTypeIndex = cardType.first,
                        parent = parent,
                        cardsViewModel = cardsViewModel,
                        isDetailView = detailView,
                        repostCardType = cardType.second,
                        context = context,
                        videoRequester = videoRequester,
                        listEditInterface = null,
                        eventDedupHelper = eventDedupHelper, viewPool = viewPool
                )
                val linearLayout = viewBinding.root.findViewById<LinearLayout>(R.id.feed_progress_body)
                linearLayout.removeAllViews()
                childViewHolder!!.itemView.let { v ->
                    v.setPadding(v.paddingLeft, 0, v.paddingRight, v.paddingBottom)
                }
                linearLayout.addView(childViewHolder!!.itemView)
            }
            (childViewHolder as? UpdateableCardView)?.bind(item, lifecycleOwner, cardPosition)
            viewBinding.setVariable(BR.item, item)
            viewBinding.executePendingBindings()
            prevCard = item
        }
    }

}