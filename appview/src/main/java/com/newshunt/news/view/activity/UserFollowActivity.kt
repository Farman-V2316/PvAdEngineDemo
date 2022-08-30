package com.newshunt.news.view.activity

import android.os.Bundle
import androidx.lifecycle.Observer
import com.newshunt.appview.R
import com.newshunt.appview.common.ui.helper.NavigationHelper
import com.newshunt.news.view.fragment.UserFollowEntityFragment

class UserFollowActivity : NewsBaseActivity() {

  private var acceptableTimeStamp: Long = System.currentTimeMillis()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.layout_dh_base_activity)
    supportFragmentManager.beginTransaction().replace(
        R.id.dh_base_container_fragment, UserFollowEntityFragment.newInstance(intent),
        "entity_list").commitAllowingStateLoss()

    NavigationHelper.navigationLiveData.observe(this, Observer {
      if (it.timeStamp < acceptableTimeStamp) {
        return@Observer
      } else {
        NavigationHelper.handleNavigationEvents(it, this, R.id.dh_base_container_fragment)
      }

    })
  }

  override fun onStart() {
    super.onStart()
    acceptableTimeStamp = System.currentTimeMillis()
  }



}