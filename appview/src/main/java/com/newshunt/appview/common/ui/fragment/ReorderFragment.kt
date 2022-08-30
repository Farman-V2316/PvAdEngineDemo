package com.newshunt.appview.common.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.*
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.newshunt.appview.R
import com.newshunt.appview.common.ui.adapter.ReorderTabsAdapter
import com.newshunt.appview.common.ui.helper.SimpleItemTouchHelperCallback
import com.newshunt.appview.common.ui.listeners.OnStartDragListener
import com.newshunt.appview.common.viewmodel.ReorderViewModel
import com.newshunt.appview.common.viewmodel.ReorderViewModelFactory
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.font.FontHelper
import com.newshunt.common.view.view.BaseFragment
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.common.pages.PageEntity
import com.newshunt.dataentity.common.pages.PageSection
import com.newshunt.dhutil.view.customview.DividerItemDecoration
import com.newshunt.news.analytics.NewsAnalyticsHelper
import com.newshunt.news.util.NewsConstants

class ReorderFragment: BaseFragment(), OnStartDragListener, View.OnClickListener {

  private lateinit var section: String
  private lateinit var reorderViewModel: ReorderViewModel
  private lateinit var recyclerView: RecyclerView
  private lateinit var viewBinding:View
  var pageEntity: PageEntity? = null

  private var adapter: ReorderTabsAdapter? = null
  private var callback: SimpleItemTouchHelperCallback? = null
  private var mItemTouchHelper: ItemTouchHelper? = null

  companion object {
    @JvmStatic
    fun newInstance(intent: Intent) : ReorderFragment {
      val fragment = ReorderFragment()
      fragment.arguments = intent.extras
      return fragment
    }
  }

  override fun onCreate(savedState: Bundle?) {
    super.onCreate(savedState)
    section = arguments?.getString(NewsConstants.DH_SECTION)?:PageSection.NEWS.section
    reorderViewModel = ViewModelProviders.of(this, ReorderViewModelFactory(section)).
        get(ReorderViewModel::class.java)
    reorderViewModel.pageLiveData.observe(this, Observer { result ->
      if (result.isSuccess) {
        result.getOrNull()?.let {
          showTabs(it)
        }
      }
    })
    reorderViewModel.deletePageLiveData.observe(this, Observer {
      onDismiss(it)
    })
  }

  override fun onActivityCreated(savedInstanceState: Bundle?) {
    super.onActivityCreated(savedInstanceState)

    viewLifecycleOwner.lifecycle.addObserver(object : LifecycleObserver {

      @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
      fun onLifecycleResume() {
      }
    })
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
    viewBinding = inflater.inflate(R.layout.activity_reorder, container,false)
    val toolbar = viewBinding.findViewById<View>(R.id.toolbar) as Toolbar
    val toolbarTitleText = toolbar.findViewById<View>(R.id.toolbar_title) as TextView
    toolbarTitleText.text = CommonUtils.getString(R.string.reorder_title)


    val backButton = viewBinding.findViewById<View>(R.id.toolbar_back_button) as FrameLayout
    backButton.setOnClickListener(this)

    viewBinding.findViewById<View>(R.id.toolbar_settings_button).visibility = View.GONE

    recyclerView = viewBinding.findViewById<View>(R.id.reorder_recycler_view) as RecyclerView
    recyclerView.setHasFixedSize(true)
    recyclerView.layoutManager = LinearLayoutManager(viewBinding.context)
    recyclerView.addItemDecoration(DividerItemDecoration(viewBinding.context, DividerItemDecoration.VERTICAL_LIST))
    return viewBinding
  }

  private fun showTabs(tabs: List<PageEntity>) {
    if (adapter == null) {
      adapter = ReorderTabsAdapter(reorderViewModel, requireContext(), this)
      recyclerView.adapter = adapter

      callback = SimpleItemTouchHelperCallback(adapter)
      mItemTouchHelper = ItemTouchHelper(callback!!)
      mItemTouchHelper!!.attachToRecyclerView(recyclerView)
      adapter?.updateList(tabs)

      tabs.forEachIndexed { index, item ->
        if (item.allowReorder) {
          setFixedLimit(if (adapter?.useHeader() == true) {
            index + 1
          } else {
            index
          }
          )
          return
        }
      }
    }
    // ignore the updates in the list
  }

  private fun setFixedLimit(limit: Int) {
    callback?.setTopLimit(limit)
  }

  private fun onDismiss(id: String) {
    val position = adapter?.getItemPosition(id) ?: -1
    if (position != -1) {
      pageEntity = adapter?.getTabList()?.get(position - 1)
      adapter?.onItemDismiss(position)
      if (pageEntity != null){
        FontHelper.showCustomSnackBar(viewBinding,CommonUtils.getString(R.string.tab_removed_snack_bar_msg, pageEntity!!.displayName),Snackbar.LENGTH_SHORT,
          Constants.UNDO_LOWER_CASE
        ) {
          onRestore(position, pageEntity!!)
          FontHelper.showCustomSnackBar(
            viewBinding,
            CommonUtils.getString(R.string.tab_added_snack_bar_msg, pageEntity!!.displayName),
            Snackbar.LENGTH_SHORT,
            null,
            null
          ).show()
        }.show()
      }
    }
  }

  fun onRestore(position:Int, pageEntity: PageEntity){
    adapter?.onItemRestored(position, pageEntity)
  }

  private fun saveChanges() {
    updateViewOrder()
  }

  private fun updateViewOrder() {
    val tabsList = adapter?.getTabList()
    if (CommonUtils.isEmpty(tabsList)) {
      return
    }
    tabsList?.forEachIndexed { index, pageEntity ->
      pageEntity.viewOrder = index
    }
    tabsList?.let {
      reorderViewModel.onReorderDone(tabsList)
    }
    NewsAnalyticsHelper.logTabsReorderEvent(tabsList)
  }


  override fun onStartDrag(viewHolder: RecyclerView.ViewHolder) {
    mItemTouchHelper?.startDrag(viewHolder)
  }

  override fun onClick(v: View) {
    if (v.id == R.id.toolbar_back_button) {
      activity?.onBackPressed()
    }
  }

  override fun handleBackPress(): Boolean {
    saveChanges()
    return false
  }
}