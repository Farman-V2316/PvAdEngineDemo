/**
 * Copyright (c) 2016 Newshunt. All rights reserved.
 */
package com.newshunt.appview.common.ui.adapter

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.dailyhunt.tv.players.autoplay.VideoRequester
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.newshunt.appview.common.CardsFragment
import com.newshunt.appview.common.ui.fragment.WebFragment
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.share.NHShareView
import com.newshunt.common.helper.share.ShareUi
import com.newshunt.dataentity.analytics.entity.NhAnalyticsUserAction
import com.newshunt.dataentity.common.asset.Format
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.common.pages.CampaignMeta
import com.newshunt.dataentity.common.pages.PageEntity
import com.newshunt.dataentity.common.pages.PageSection
import com.newshunt.dataentity.news.model.util.NewsPageLayout
import com.newshunt.dataentity.search.SearchQuery
import com.newshunt.dhutil.bundleOf
import com.newshunt.dhutil.helper.preference.UserPreferenceUtil
import com.newshunt.dhutil.transaction
import com.newshunt.news.model.repo.HomeAdap
import com.newshunt.news.util.NewsConstants
import com.newshunt.news.view.adapter.SlidingTabLayoutAdapter
import com.newshunt.news.view.fragment.ScrollTabHolderFragment
import com.newshunt.news.view.listener.FragmentScrollListener

/**
 * @author shrikant.agrawal
 */
class HomeTabsAdapter @JvmOverloads constructor(
        val fragmentManager: FragmentManager,
        private var scrollListener: FragmentScrollListener?,
        private val videoRequester: VideoRequester?,
        private val onPageChange: (Int, PageEntity?) -> Unit = { i, n -> Unit },
        private val section: String,
        private val parentEntityId: String? = null,
        private val sourceId: String? = null,
        private val sourceType: String? = null,
        private val searchQuery: SearchQuery? = null,
        private val extraArguments: Bundle? = null,
        private val nhShareView: NHShareView? = null,
        private val createPost: FloatingActionButton? = null
) : FragmentStatePagerAdapter(fragmentManager), SlidingTabLayoutAdapter, HomeAdap {

    var pageList: List<PageEntity>? = null
        private set
    private var fragment: ScrollTabHolderFragment? = null
    private var previousFragment: ScrollTabHolderFragment? = null

    val currentFragment: ScrollTabHolderFragment?
        get() = fragment

    val shareIntent: Intent?
        get() = fragment?.shareIntent


    fun updateList(pageList: List<PageEntity>) {
        this.pageList = pageList
    }

    override fun pages(): List<PageEntity>? {
        return pageList
    }

    override fun getItem(position: Int): Fragment {
      val page = pageList?.get(position)
      return when (page?.entityLayout) {
          NewsPageLayout.WEB_ITEMS.layout -> {
              val fragment = WebFragment()
              val args = bundleOf(NewsConstants.NEWS_PAGE_ENTITY to page,
                  NewsConstants.BUNDLE_ADAPTER_POSITION to position)
              if (extraArguments != null) {
                  args.putAll(extraArguments)
              }
              fragment.arguments = args
              fragment
          }
          else -> {
              val listType = listType(page)
              val supportsAds = listType == null && isAdAllowedOnSection(section)
              val args = bundleOf(Constants.PAGE_ID to page?.id,
                      NewsConstants.NEWS_PAGE_ENTITY to page,
                      NewsConstants.PARENT_ENTITY_ID to parentEntityId,
                      NewsConstants.SOURCE_ID to sourceId,
                      NewsConstants.SOURCE_TYPE to sourceType,
                      NewsConstants.BUNDLE_ADAPTER_POSITION to position,
                      NewsConstants.DH_SECTION to section,
                      Constants.LIST_TYPE to listType,
                      Constants.SUPPORT_ADS to supportsAds,
                      Constants.BUNDLE_SEARCH_QUERY to searchQuery)
              if (extraArguments != null) {
                  args.putAll(extraArguments)
              }
              CardsFragment.create(args, videoRequester)
          }
      }
    }

    private fun isAdAllowedOnSection(section: String?): Boolean {
        return when (section) {
            PageSection.TV.section,
            PageSection.NEWS.section -> true
            else -> false
        }
    }

    private fun listType(page: PageEntity?): String? {
        if(page == null) return null
        return when (page.entityLayout) {
            NewsPageLayout.ENTITY_LISTING.layout ->  com.newshunt.dataentity.common.asset.Format.ENTITY.name
            NewsPageLayout.PHOTO_GRID.layout -> Format.PHOTO.name
            else -> null
        }
    }


    override fun getCount(): Int {
        return pageList?.size ?: 0
    }

    override fun setPrimaryItem(container: ViewGroup, position: Int, obj: Any) {
        previousFragment = fragment
        if (obj is ScrollTabHolderFragment) {
            fragment = obj
        }

        if (fragment !== previousFragment) {
            showShareView()
        }
        onPageChange(position, getPage(position))
        super.setPrimaryItem(container, position, obj)
    }

    fun onPageSelected(action: NhAnalyticsUserAction) {
        logTimeSpent(action)
        fragment?.onPageSelected()
    }

    override fun getPageTitle(position: Int): CharSequence {
        val entity = pageList?.get(position) ?: return Constants.EMPTY_STRING

        if (!TextUtils.isEmpty(entity.displayName)) {
            return entity.displayName?:Constants.EMPTY_STRING
        }

        val title = getStringFromResource(
                UserPreferenceUtil.getUserEdition(), entity.name?:Constants.EMPTY_STRING, entity.id)

        return (if (!TextUtils.isEmpty(title)) {
            title
        } else entity.name) ?: Constants.EMPTY_STRING
    }

    override fun getCampaignMetaItem(position: Int): CampaignMeta? {
        return pageList?.get(position)?.campaignMeta
    }

    override fun getPageIconUrl(position: Int): String? {
        return null
    }

    override fun applyCustomStyles(view: View, position: Int) {
    }

    fun getPage(position: Int): PageEntity? {
        return pageList?.let {
            if (position in it.indices) {
                it[position]
            } else null
        }
    }

    override fun saveState(): Parcelable? {
        return null
    }

    override fun getItemPosition(obj: Any): Int {
        return if (obj is ScrollTabHolderFragment) {
            val pair = obj.itemAndPosition
            if (pair != null && CommonUtils.equals(pair.first, getPage(pair.second))) {
                POSITION_UNCHANGED
            } else {
                POSITION_NONE
            }
        }
        else POSITION_NONE
    }

    private fun getStringFromResource(edition: String, name: String, entityId: String): String? {
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(edition)
                || TextUtils.isEmpty(entityId)) {
            return Constants.EMPTY_STRING
        }

        val resource = StringBuilder(edition.toLowerCase())
                .append(Constants.UNDERSCORE_CHARACTER)
                .append(name.toLowerCase())
                .append(Constants.UNDERSCORE_CHARACTER)
                .append(entityId).toString()

        return CommonUtils.getStringFromResource(resource)
    }

    private fun logTimeSpent(exitAction: NhAnalyticsUserAction) {
        previousFragment?.logTimeSpentEvent(exitAction)
    }

    fun onShareClick(packageName: String, shareUi: ShareUi) {
        fragment?.onShareClick(packageName, shareUi)
    }

    fun destroy() {
        fragment = null
        previousFragment = null
        scrollListener = null
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        try {
            super.destroyItem(container, position, `object`)
        } catch (throwable: Throwable) {
            Logger.caughtException(throwable)
        } finally {
            try {
                fragmentManager.transaction {
                    remove(`object` as Fragment)
                }
            } catch (throwable: Throwable) {
                Logger.caughtException(throwable)
            }
        }
    }

    companion object {
        /**
         * Returns all the types of fragments it can instantiate.
         */
        @JvmStatic
        fun fragmentClasses(): List<Class<out Any>> {
            return listOf()
        }
    }

    fun getPageType(id: String): String? {
        pageList?.forEach {
            if (it.id == id) {
                return it.entityType
            }
        }
        return null
    }

    private fun showShareView() {
        if (fragment is WebFragment) {
            createPost?.hide()
            nhShareView?.let {
                fragment?.showShareView(it)
            }
        } else {
            createPost?.show()
            nhShareView?.visibility = View.GONE
        }

    }
}
