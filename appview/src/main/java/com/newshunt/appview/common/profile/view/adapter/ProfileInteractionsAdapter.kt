/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.appview.common.profile.view.adapter

import android.os.Parcelable
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.model.entity.ProfileTabType
import com.newshunt.dataentity.model.entity.ProfileTabs
import com.newshunt.news.view.listener.FragmentScrollListener
import com.newshunt.appview.common.profile.view.activity.ProfileViewState
import com.newshunt.appview.common.profile.view.fragment.ActivityAndResponsesFragment
import com.newshunt.appview.common.profile.view.fragment.HistoryFragment

/**
 * Adapter to show tabs of interactions
 * <p>
 * Created by srikanth.ramaswamy on 05/27/2019.
 */
class ProfileInteractionsAdapter(fragmentManager: FragmentManager,
                                 private val fragmentScrollListener: FragmentScrollListener,
                                 private val viewState: ProfileViewState)
    : FragmentStatePagerAdapter(fragmentManager) {
    var interactionTabList: List<ProfileTabs>? = null
    var currentFragment: Fragment? = null

    override fun getItem(position: Int): Fragment {

        return interactionTabList?.let {
            when (it[position].tabType) {
                ProfileTabType.SAVED -> {
                    createSavedFragment(position, it[position])
                }
                ProfileTabType.FPV_POSTS, ProfileTabType.TPV_POSTS -> {
                    createMyPostsFragment(position, it[position])
                }
                ProfileTabType.FPV_ACTIVITY, ProfileTabType.TPV_ACTIVITY -> {
                    createActivityFragment(position, it[position])
                }
                ProfileTabType.GENERIC_HASHTAG, ProfileTabType.GENERIC_WEB -> {
                    createActivityFragment(position,it[position])
                }
                ProfileTabType.HISTORY -> {
                    HistoryFragment()
                }
                else -> {
                    HistoryFragment()
                }
            }
        } ?: HistoryFragment()
    }

    override fun getItemPosition(obj: Any): Int {
        return androidx.viewpager.widget.PagerAdapter.POSITION_NONE
    }

    override fun saveState(): Parcelable? {
        return null
    }

    override fun getCount(): Int {
        return interactionTabList?.size ?: 0
    }

    override fun getPageTitle(position: Int): CharSequence? {
        interactionTabList ?: return null
        return interactionTabList!![position].name
    }

    override fun setPrimaryItem(container: ViewGroup, position: Int, obj: Any) {
        super.setPrimaryItem(container, position, obj)
        if (obj is Fragment) {
            currentFragment = obj
        }
    }

    fun getIndexForTabType(profileTabType: ProfileTabType?): Int {
        if (CommonUtils.isEmpty(interactionTabList)) {
            return -1
        }
        interactionTabList!!.forEachIndexed { index, profileTabs ->
            if (profileTabs.tabType == profileTabType) {
                return index
            }
        }
        return 0
    }

    fun getIndexForTabId(id: String?): Int {
        if(interactionTabList.isNullOrEmpty()) return -1
        interactionTabList?.indexOfFirst { it.id == id } ?: return -1
        return 0
    }

    private fun createActivityFragment(position: Int, profileTabs: ProfileTabs): Fragment {
        //TODO (santhosh.kc) - to move this piece of code to inside when if not big
        return ActivityAndResponsesFragment.newInstance(position, profileTabs,
                fragmentScrollListener, viewState)
    }

    private fun createMyPostsFragment(position: Int, profileTabs: ProfileTabs): Fragment {
        //TODO (santhosh.kc) - to move this piece of code to inside when if not big
        return ActivityAndResponsesFragment.newInstance(position, profileTabs,
                fragmentScrollListener, viewState)
    }

    private fun createSavedFragment(position: Int, profileTabs: ProfileTabs): Fragment {
        //TODO (santhosh.kc) - to move this piece of code to inside when if not big
        return ActivityAndResponsesFragment.newInstance(position, profileTabs,
                fragmentScrollListener, viewState)
    }
}