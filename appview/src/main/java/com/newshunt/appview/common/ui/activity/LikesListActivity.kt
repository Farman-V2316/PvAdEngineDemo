/*
 * Copyright (c) 2020 Newshunt. All rights reserved.
 */

package com.newshunt.appview.common.ui.activity

import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.viewpager.widget.ViewPager
import com.newshunt.appview.R
import com.newshunt.appview.common.ui.fragment.LikesListFragment
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.view.adapter.NHFragmentStatePagerAdapter
import com.newshunt.common.view.customview.fontview.NHTextView
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.common.pages.PageSection
import com.newshunt.dataentity.social.entity.LikeType
import com.newshunt.news.view.activity.NewsBaseActivity
import com.newshunt.news.view.customview.SlidingTabLayout
import com.newshunt.dhutil.analytics.AnalyticsHelper2
import com.newshunt.news.helper.LikeEmojiBindingUtils
import com.newshunt.news.util.NewsConstants

/**
 * Activity to show complete list of all likes for a story.
 */
class LikesListActivity : NewsBaseActivity() {

    lateinit var postId: String
    lateinit var viewpager : ViewPager
    private lateinit var tabLayout : SlidingTabLayout
    private var referrer:PageReferrer? = null
    private lateinit var section: String
    private var referrerType = Constants.EMPTY_STRING

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_likes_list_open)
        postId = intent?.extras?.getString(Constants.BUNDLE_POST_ID) ?: ""
        referrer = intent?.extras?.get(Constants.BUNDLE_ACTIVITY_REFERRER) as? PageReferrer
        referrerType = intent?.extras?.getString(Constants.BUNDLE_ACTIVITY_REFERRER_TYPE) ?: Constants.EMPTY_STRING
        section = intent?.extras?.getString(NewsConstants.DH_SECTION) ?: PageSection.NEWS.section
        val guestCount = intent?.extras?.getInt(Constants.BUNDLE_GUEST_COUNT)

        val totalCount = intent?.extras?.getString(Constants.BUNDLE_LIKES_COUNTS) ?: Constants.ZERO_STRING
        val angryCount = intent?.extras?.getString(Constants.BUNDLE_LIKES_COUNTS + LikeType.ANGRY.name) ?: Constants.ZERO_STRING
        val happyCount = intent?.extras?.getString(Constants.BUNDLE_LIKES_COUNTS + LikeType.HAPPY.name) ?: Constants.ZERO_STRING
        val loveCount = intent?.extras?.getString(Constants.BUNDLE_LIKES_COUNTS + LikeType.LOVE.name) ?: Constants.ZERO_STRING
        val likeCount = intent?.extras?.getString(Constants.BUNDLE_LIKES_COUNTS + LikeType.LIKE.name) ?: Constants.ZERO_STRING
        val wowCount = intent?.extras?.getString(Constants.BUNDLE_LIKES_COUNTS + LikeType.WOW.name) ?: Constants.ZERO_STRING
        val sadCount = intent?.extras?.getString(Constants.BUNDLE_LIKES_COUNTS + LikeType.SAD.name) ?: Constants.ZERO_STRING

        viewpager = findViewById(R.id.likes_viewpager)
        val adapter = LikesViewPagerAdapter(supportFragmentManager, postId, referrer, guestCount)
        viewpager.adapter = adapter

        tabLayout = findViewById(R.id.likes_tab)
        tabLayout.setCustomTabView(R.layout.like_tab_item, View.NO_ID, View.NO_ID)
        tabLayout.setDrawBottomLine(false)
        tabLayout.setDrawSelectionIndicator(false)
        tabLayout.setViewBinder { view, position ->
            val likeType = adapter.getEmojiItem(position)
            val icon = view.findViewById<ImageView>(R.id.emoji_icon)
            val text = view.findViewById<TextView>(R.id.emoji_text)
            val bgView = view.findViewById<ConstraintLayout>(R.id.detail_reactions)

            if (position == 0) {
                text.text = CommonUtils.getString(R.string.all).plus(" ").plus(totalCount)
                bgView.setPadding(CommonUtils.getDimension(R.dimen.story_card_padding_left),
                        CommonUtils.getDimension(R.dimen.dialog_padding), CommonUtils
                        .getDimension(R.dimen.story_card_padding_left), CommonUtils.getDimension(R.dimen.dialog_padding))
                icon.visibility = View.GONE
            } else {
                when (likeType) {
                    LikeType.ANGRY -> text.text = angryCount
                    LikeType.WOW -> text.text = wowCount
                    LikeType.LIKE -> text.text = likeCount
                    LikeType.SAD -> text.text = sadCount
                    LikeType.LOVE -> text.text = loveCount
                    LikeType.HAPPY -> text.text = happyCount
                    else -> text.text = likeType?.name
                }

                val res = LikeEmojiBindingUtils.getEmojiIconResource(likeType, null, true)
                icon.setImageResource(res)
            }
        }
        tabLayout.setDisplayDefaultIconForEmptyTitle(true)
        tabLayout.setViewPager(viewpager)
        setUpToolbar()

        AnalyticsHelper2.logEntityListViewEventForLikeList(section, referrer, referrer,
                referrerType)
    }

    private fun setUpToolbar() {
        val actionBarBackButton = findViewById<View>(com.newshunt.navigation.R.id.toolbar_back_button_container) as FrameLayout
        val actionBarTitle = findViewById<View>(com.newshunt.navigation.R.id.actionbar_title) as NHTextView
        actionBarBackButton.setOnClickListener {
            onBackPressed()
        }

        actionBarTitle.setText(R.string.detail_likes)
    }
}

class LikesViewPagerAdapter(val fm: FragmentManager, val postId: String?,
                            val referrer: PageReferrer?,
                            val guestCount: Int?) :
        NHFragmentStatePagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        val currentFragment = LikesListFragment()
        val bundle = Bundle()
        if (position > 0) {
            bundle.putString(Constants.BUNDLE_LIKE_TYPE, LikeType.values()[position - 1].name)
        }
        else {
            if (guestCount != null) {
                bundle.putInt(Constants.BUNDLE_GUEST_COUNT, guestCount)
            }
        }

        bundle.putString(Constants.BUNDLE_POST_ID, postId)
        bundle.putSerializable(Constants.BUNDLE_ACTIVITY_REFERRER, referrer)
        currentFragment.arguments = bundle
        return currentFragment
    }

    override fun getCount(): Int {
        return LikeType.values().size + 1
    }

    override fun getPageTitle(position: Int): CharSequence? {
        if (position > 0) {
            return LikeType.values()[position - 1].name
        }

        return super.getPageTitle(position)
    }

    fun getEmojiItem(position: Int): LikeType? {
        if (position > 0) {
            return LikeType.values()[position - 1]
        }

        return null

    }

}

