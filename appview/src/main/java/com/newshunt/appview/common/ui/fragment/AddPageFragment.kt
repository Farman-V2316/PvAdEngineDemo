/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.appview.common.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.*
import com.newshunt.appview.BR
import com.newshunt.appview.R
import com.newshunt.appview.common.ui.adapter.AddPageTabAdapter
import com.newshunt.appview.common.ui.helper.NavigationEvent
import com.newshunt.appview.common.ui.helper.NavigationHelper
import com.newshunt.appview.common.viewmodel.AddPageViewModel
import com.newshunt.appview.common.viewmodel.AddPageViewModelFactory
import com.newshunt.appview.databinding.ActivityAddPageBinding
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.font.FontHelper
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.common.view.view.BaseFragment
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.common.model.NewsPageMode
import com.newshunt.dataentity.common.pages.AddPageEntity
import com.newshunt.dataentity.common.pages.PageSection
import com.newshunt.dataentity.news.analytics.NewsReferrer
import com.newshunt.dhutil.analytics.AnalyticsHelper2
import com.newshunt.dhutil.helper.preference.CoachMarksPreference
import com.newshunt.news.util.NewsConstants


class AddPageFragment : BaseFragment() {

  private lateinit var section: String
  private lateinit var topicTitle: String
  private lateinit var binding: ActivityAddPageBinding
  private lateinit var addPageViewModel : AddPageViewModel
  private var changePagesList: List<AddPageEntity>? = null
  private val handler = Handler(Looper.getMainLooper())

  companion object {
    @JvmStatic
    fun newInstance(intent: Intent) : AddPageFragment {
      val fragment = AddPageFragment()
      fragment.arguments = intent.extras
      return fragment
    }
  }

  override fun onCreate(savedState: Bundle?) {
    super.onCreate(savedState)
    section = arguments?.getString(NewsConstants.DH_SECTION)?: PageSection.NEWS.section
    addPageViewModel = ViewModelProviders.of(this,
        AddPageViewModelFactory(section)).get(AddPageViewModel::class.java)
    addPageViewModel.addPageLiveData.observe(this , Observer {
      if (it.isSuccess) {
        changePagesList = it.getOrNull()
      }
    })

    if (section == PageSection.NEWS.section) {
      topicTitle = getString(R.string.location_topics)
    }else{
      topicTitle = getString(R.string.topics)
    }

  }

  override fun onActivityCreated(savedInstanceState: Bundle?) {
    super.onActivityCreated(savedInstanceState)

    viewLifecycleOwner.lifecycle.addObserver(object : LifecycleObserver {

      @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
      fun onLifecycleResume() {
      }
    })
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    binding = DataBindingUtil.inflate(inflater, R.layout.activity_add_page, container, false)
    binding.setVariable(BR.vm, addPageViewModel)

    binding.toolbar.toolbarBackButton.setOnClickListener {
      activity?.onBackPressed()
    }

    binding.toolbar.toolbarTitle.setText(topicTitle)
    binding.toolbar.toolbarSettingsButton.setOnClickListener { view:View->
      // create the navigation intent to open reorder activity
      handler.removeCallbacksAndMessages(null)
      if (view.id == R.id.toolbar_settings_button) {
        val intent = Intent(Constants.REORDER_PAGE_OPEN_ACTION)
        intent.putExtra(NewsConstants.DH_SECTION, section)
        NavigationHelper.navigationLiveData.postValue(NavigationEvent(intent))
      }
    }
    val addPageTabs = arrayOf(topicTitle)
    val addPageAdater = AddPageTabAdapter(childFragmentManager, section, addPageTabs)
    binding.viewPager.adapter = addPageAdater
    val canShowPopupWindow = PreferenceManager.getPreference(CoachMarksPreference.PREFERENCE_TOOL_ADD_TAB, true)
    if (canShowPopupWindow) {
      handler.postDelayed({
        showPopUp(
          binding.toolbar.editTopics,
          binding.toolbar.toolbarSettingsButton.measuredWidth / 2
        )
      }, 2000)
    }
    return binding.root
  }

  override fun handleBackPress(): Boolean {
    // handling the add page list
    showToast()
    return false
  }

  override fun onDestroy() {
    handler.removeCallbacksAndMessages(null)
    super.onDestroy()
  }

  override fun onDestroyView() {
    handler.removeCallbacksAndMessages(null)
    super.onDestroyView()
  }

  private fun showToast() {
    if (CommonUtils.isEmpty(changePagesList)) {
      return
    }

    var addedPages = 0
    var deletedPages = 0

    var firstAddedTab: String? = null
    var firstDeletedTab: String? = null

    changePagesList?.forEach {
      if (NewsPageMode.ADDED.mode == it.mode) {
        firstAddedTab = if (addedPages == 0) it.displayName else firstAddedTab
        addedPages++
        AnalyticsHelper2.logTabItemAddedOrRemoved(PageReferrer(NewsReferrer.TABSELECTION_VIEW), it.toPageEntity(),false, section)
      } else if (NewsPageMode.DELETED.mode == it.mode) {
        firstDeletedTab = if (deletedPages == 0) it.displayName else firstDeletedTab
        deletedPages++
        AnalyticsHelper2.logTabItemAddedOrRemoved(PageReferrer(NewsReferrer.TABSELECTION_VIEW), it.toPageEntity(),true, section)
      }
    }

    val text: String = if (addedPages == 1 && deletedPages == 0) {
      CommonUtils.getString(R.string.single_tab_added, firstAddedTab)
    } else if (addedPages > 1 && deletedPages == 0) {
      CommonUtils.getString(R.string.multiple_tab_added, addedPages)
    } else if (addedPages == 0 && deletedPages == 1) {
      CommonUtils.getString(R.string.single_tab_deleted, firstDeletedTab)
    } else if (addedPages == 0 && deletedPages > 1) {
      CommonUtils.getString(R.string.multiple_tab_deleted, deletedPages)
    } else {
      CommonUtils.getString(R.string.multiple_tab_modified, addedPages + deletedPages)
    }
    FontHelper.showCustomFontToast(requireContext(), text, Toast.LENGTH_SHORT)
  }

  private fun showPopUp(anchor: View, halfAnchorWidth: Int) {
    val popupWindow = PopupWindow(context)
    popupWindow.setBackgroundDrawable(null)
    val layout: View = layoutInflater.inflate(R.layout.edit_tabs_tooltip, null)
    popupWindow.contentView = layout
    popupWindow.isOutsideTouchable = true
    popupWindow.isFocusable = true
    // measure contentView size
    val contentView = popupWindow.contentView
    contentView.measure(
      CommonUtils.makeDropDownMeasureSpec(popupWindow.width),
      CommonUtils.makeDropDownMeasureSpec(popupWindow.height)
    )
    val offsetX = -popupWindow.contentView.measuredWidth
    popupWindow.showAsDropDown(anchor, offsetX + halfAnchorWidth, 0)
    PreferenceManager.savePreference(CoachMarksPreference.PREFERENCE_TOOL_ADD_TAB, false)
    handler.postDelayed({
      popupWindow.dismiss()
    }, Constants.DEFAULT_COACH_TOOL_TIP_TIME)
  }
}

