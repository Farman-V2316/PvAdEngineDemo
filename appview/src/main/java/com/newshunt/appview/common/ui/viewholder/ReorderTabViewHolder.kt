package com.newshunt.appview.common.ui.viewholder

import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.newshunt.appview.BR
import com.newshunt.dataentity.common.pages.PageEntity

class ReorderTabViewHolder(val viewBinding: ViewDataBinding) : RecyclerView.ViewHolder(viewBinding.root) {

  init {
    viewBinding.setVariable(BR.holder, this)
  }


  fun bindView(entity: PageEntity, position: Int) {
    viewBinding.setVariable(BR.item, entity)
    viewBinding.setVariable(BR.index, position)
    viewBinding.executePendingBindings()
  }
}