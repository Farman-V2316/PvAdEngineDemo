package com.newshunt.appview.common.ui.adapter

import android.os.Bundle
import android.util.SparseArray
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.PagerAdapter
import com.newshunt.appview.common.ui.fragment.PageableTopicFragment
import com.newshunt.common.helper.common.Constants
import com.newshunt.news.view.fragment.ScrollTabHolderFragment

class AddPageTabAdapter(fragmentManager: FragmentManager,
                        private val section: String,
                        private val addPageTabs: Array<String>) : FragmentPagerAdapter(fragmentManager) {

  private val registeredFragments = SparseArray<Fragment>()

  override fun getItem(position: Int): Fragment {
    val bundleToSend = Bundle()
    bundleToSend.putBoolean(Constants.SHOW_FOLLOW_BUTTON, false)
    return PageableTopicFragment.newsInstance(section)
  }

  override fun instantiateItem(container: ViewGroup, position: Int): Any {
    val fragment = super.instantiateItem(container, position) as Fragment
    registeredFragments.put(position, fragment)
    return fragment
  }

  override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
    registeredFragments.remove(position)
    super.destroyItem(container, position, `object`)
  }

  fun getRegisteredFragment(position: Int): Fragment {
    return registeredFragments.get(position)
  }

  override fun getCount(): Int {
    return addPageTabs.size
  }

  override fun getItemPosition(`object`: Any): Int {
    return PagerAdapter.POSITION_UNCHANGED
  }

  override fun getPageTitle(position: Int): CharSequence? {
    return addPageTabs[position]
  }

  fun refreshTabs() {
    for (i in 0 until registeredFragments.size()) {
      (registeredFragments.get(i) as ScrollTabHolderFragment).refresh()
    }
  }

}