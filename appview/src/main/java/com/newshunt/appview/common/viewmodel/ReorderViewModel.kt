package com.newshunt.appview.common.viewmodel

import android.view.MotionEvent
import android.view.View
import androidx.core.view.MotionEventCompat
import androidx.databinding.BindingAdapter
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.newshunt.appview.R
import com.newshunt.appview.common.ui.listeners.OnStartDragListener
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.common.pages.PageEntity
import com.newshunt.dataentity.news.analytics.NewsReferrer
import com.newshunt.dhutil.analytics.AnalyticsHelper2
import com.newshunt.dhutil.bundleOf
import com.newshunt.news.model.usecase.MediatorHomePageUsecase
import com.newshunt.news.model.usecase.RemovePageUsecase
import com.newshunt.news.model.usecase.ReorderPageUsecase
import com.newshunt.news.model.usecase.Result0
import com.newshunt.news.model.usecase.toMediator2

class ReorderViewModel(val section: String) : ViewModel() {

  private val observablePageUsecase = MediatorHomePageUsecase().toMediator2()
  val pageLiveData : LiveData<Result0<List<PageEntity>>>
  val deletePageLiveData = MutableLiveData<String>()

  init {
    observablePageUsecase.execute(section)
    pageLiveData = observablePageUsecase.data()
  }

  fun onReorderDone(pageList : List<PageEntity>) {
    ReorderPageUsecase().toMediator2().execute(
        bundleOf(ReorderPageUsecase.BUNDLE_LIST to pageList,
        ReorderPageUsecase.BUNDLE_SECTION to section))
  }

  fun onViewClick(view: View, item: Any, index: Int) {
    if (view.id == R.id.reorder_frame_dismiss) {
      if (!(item as PageEntity).isRemovable) return
      RemovePageUsecase().toMediator2().execute(bundleOf(RemovePageUsecase.BUNDLE_PAGE to item, RemovePageUsecase.BUNDLE_SECTION to section))
      AnalyticsHelper2.logTabItemAddedOrRemoved(
          PageReferrer(NewsReferrer.MANAGE_NEWS_HOME), item,true, section)
      deletePageLiveData.postValue(item.id)
    }
  }
}

class ReorderViewModelFactory(val section: String) : ViewModelProvider.Factory {

  override fun <T : ViewModel?> create(modelClass: Class<T>): T {
    return ReorderViewModel(section) as T
  }

}

@BindingAdapter("bind:draglistener" , "bind:viewholder" , requireAll = true)
fun onTouchListener(view: View, dragListener: OnStartDragListener, viewHolder: RecyclerView.ViewHolder) {
  view.setOnTouchListener {
    v, event ->
    if (MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_DOWN) {
      dragListener.onStartDrag(viewHolder)
    }
    false
  }
}