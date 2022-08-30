package com.newshunt.appview.common.ui.adapter

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.newshunt.appview.R
import com.newshunt.appview.common.ui.helper.ItemTouchHelperAdapter
import com.newshunt.appview.common.ui.listeners.OnStartDragListener
import com.newshunt.appview.common.ui.viewholder.ReorderHeaderViewHolder
import com.newshunt.appview.common.ui.viewholder.ReorderTabViewHolder
import com.newshunt.appview.common.viewmodel.ReorderViewModel
import com.newshunt.appview.databinding.ReorderTabItemBinding
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.common.view.customview.HeaderRecyclerViewAdapter
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.common.pages.PageEntity
import com.newshunt.dhutil.helper.preference.CoachMarksPreference
import kotlinx.android.synthetic.main.reorder_tab_item.view.*
import kotlinx.android.synthetic.main.tool_tip_pop_up_window.view.*
import java.util.*

class ReorderTabsAdapter(private val reorderViewModel : ReorderViewModel, private val context: Context,
                         private val dragListener: OnStartDragListener) : HeaderRecyclerViewAdapter(), ItemTouchHelperAdapter {

  private val tabList = ArrayList<PageEntity>()
  private val handler = Handler(Looper.getMainLooper())

  fun updateList(tabList: List<PageEntity>) {
    this.tabList.clear()
    this.tabList.addAll(tabList)
    notifyDataSetChanged()
  }

  fun getTabList(): List<PageEntity> {
    return tabList
  }

  override fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
    // removing one value because the header is present
    Collections.swap(tabList, fromPosition - 1, toPosition - 1)
    notifyItemMoved(fromPosition, toPosition)
    return true
  }

  override fun onItemDismiss(position: Int) {
    // remove position-1 because the header is added
    if (position < 1 || position > tabList.size) {
      return
    }
    tabList.removeAt(position - 1)
    notifyItemRemoved(position)
  }

  fun onItemRestored(position: Int, pageEntity: PageEntity) {
    // add at position-1 because header is there
    tabList.add(position - 1, pageEntity)
    notifyItemInserted(position)
  }

  fun getItemPosition(id: String): Int {
    tabList.forEachIndexed { index, it ->
      if (it.id == id) {
        return if (useHeader()) index+1 else index
      }
    }
    return  -1
  }

  override fun useFooter(): Boolean {
    return false
  }

  override fun useHeader(): Boolean {
    return true
  }

  override fun getBasicItemCount(): Int {
    return tabList.size
  }

  override fun onCreateBasicItemViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
    val layoutInflater = LayoutInflater.from(parent.context)
    val viewBinding = DataBindingUtil.inflate<ReorderTabItemBinding>(
        layoutInflater, R.layout.reorder_tab_item, parent, false)
    viewBinding.vm = reorderViewModel
    viewBinding.listener = dragListener
    return ReorderTabViewHolder(viewBinding)
  }

  override fun onBindFooterView(holder: RecyclerView.ViewHolder, position: Int) {

  }

  override fun onCreateFooterViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder? {
    return null
  }

  override fun onCreateHeaderViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
    val itemView = LayoutInflater.from(context).inflate(R.layout.reorder_header_item, parent, false)
    return ReorderHeaderViewHolder(itemView)
  }

  override fun onBindHeaderView(holder: RecyclerView.ViewHolder, position: Int) {
    // do nothing
  }

  override fun onBindBasicItemView(holder: RecyclerView.ViewHolder, position: Int) {
    val entity = tabList[position]
    (holder as ReorderTabViewHolder).bindView(entity, position + 1) //adding + 1 because of header
    val removePosition = if (itemCount > 5) 5 else itemCount - 1
    val canShowRemove = PreferenceManager.getPreference(CoachMarksPreference.PREFERENCE_TOOL_TIP_REMOVE_TAB, true)
    if ((position == 0 || position == removePosition) && canShowRemove) {
      val popupWindow = PopupWindow(context)
      val layout = LayoutInflater.from(context).inflate(R.layout.tool_tip_pop_up_window, null)
      popupWindow.setBackgroundDrawable(null)
      if (position == removePosition) {
        layout.title_text.text = CommonUtils.getString(R.string.removable_tab)
        PreferenceManager.savePreference(CoachMarksPreference.PREFERENCE_TOOL_TIP_REMOVE_TAB, false)
      }
      popupWindow.contentView = layout
      val contentView = popupWindow.contentView
      contentView.measure(
        CommonUtils.makeDropDownMeasureSpec(popupWindow.width),
        CommonUtils.makeDropDownMeasureSpec(popupWindow.height)
      )
      holder.itemView.reorder_frame_dismiss.post(Runnable {
        popupWindow.showAsDropDown(holder.itemView.reorder_dismiss_image, 0, -holder.itemView.reorder_frame_dismiss.measuredHeight - popupWindow.contentView.measuredHeight/2)
      })
      popupWindow.isOutsideTouchable = true
      popupWindow.isFocusable = true
      handler.postDelayed({
        popupWindow.dismiss()
      }, Constants.DEFAULT_COACH_TOOL_TIP_TIME)
    }
    val dragThumbPosition = if (itemCount > 7) 7 else itemCount - 2
    val canShowThumb = PreferenceManager.getPreference(CoachMarksPreference.PREFERENCE_TOOL_TIP_REORDER_TAB, true)
    if (dragThumbPosition == position && canShowThumb) {
      val popupWindow = PopupWindow(context)
      popupWindow.setBackgroundDrawable(null)
      val layout: View = LayoutInflater.from(context).inflate(R.layout.drag_up_down_tab, null)
      popupWindow.contentView = layout
      popupWindow.showAsDropDown(holder.itemView.reorder_frame_handle, -20, -220)
      popupWindow.isOutsideTouchable = true
      popupWindow.isFocusable = true
      PreferenceManager.savePreference(CoachMarksPreference.PREFERENCE_TOOL_TIP_REORDER_TAB, false)
      handler.postDelayed({
        popupWindow.dismiss()
      }, Constants.DEFAULT_COACH_TOOL_TIP_TIME)
    }
  }

  override fun getBasicItemType(position: Int): Int {
    return 0
  }
}