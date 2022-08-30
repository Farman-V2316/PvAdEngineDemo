package com.newshunt.appview.common.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.newshunt.appview.BR
import com.newshunt.appview.R
import com.newshunt.appview.common.ui.helper.NavigationEvent
import com.newshunt.appview.common.ui.helper.NavigationHelper
import com.newshunt.appview.databinding.WalkthroughItemBinding
import com.newshunt.appview.databinding.WalkthroughMainBinding
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.preference.GenericAppStatePreference
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.common.view.customview.fontview.NHTextView
import com.newshunt.common.view.view.BaseFragment
import com.newshunt.dataentity.analytics.entity.NhAnalyticsAppEventParam
import com.newshunt.dataentity.analytics.referrer.NhGenericReferrer
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dhutil.analytics.AnalyticsHelper2
import com.newshunt.dhutil.bundleOf
import com.newshunt.dhutil.helper.theme.ThemeUtils
import com.newshunt.news.util.NewsConstants

class WalkThroughFragment : BaseFragment(), ViewPager.OnPageChangeListener {

	companion object {
		const val EVENT_TYPE_VIEW = "walkthrough_screen_viewed"
	}

	lateinit var binding: WalkthroughMainBinding

	private var showImportContacts = false

	override fun onCreate(savedState: Bundle?) {
		super.onCreate(savedState)
		showImportContacts = arguments?.getBoolean(NewsConstants.EXTRA_SHOW_IMPORT_CONTACTS)?:false
	}

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		binding = DataBindingUtil.inflate(inflater, R.layout.walkthrough_main, container, false)
		val adapter = WalkThroughAdapter(childFragmentManager, showImportContacts)
		binding.wtViewPager.adapter = adapter
		binding.wtViewPager.addOnPageChangeListener(this)
		adapter.notifyDataSetChanged()

		binding.setVariable(BR.vm, this)
		binding.executePendingBindings()
		onPageSelected(0)

		AnalyticsHelper2.logFeatureNudgeEvent(EVENT_TYPE_VIEW)
		PreferenceManager.savePreference(GenericAppStatePreference.SOCIAL_WALKTHROUGH_SHOWN, true)
		return binding.root
	}

	fun onViewClick(view: View) {
		val item = binding.wtViewPager.currentItem
		if (view.id == R.id.next_btn) {
			AnalyticsHelper2.logWalkThroughExploreButtonClickEvent(WalkThroughItemFragment.EVENT_TYPE_NEXT, mapOf(NhAnalyticsAppEventParam.SCREEN_NO to item + 1))
			if (item < 1) {
				binding.wtViewPager.setCurrentItem(item + 1, true)
			} else {
				if (showImportContacts) {
					val intent = Intent(Constants.IMPORT_CONTACTS_ACTIONS)
					intent.putExtra(Constants.BUNDLE_ACTIVITY_REFERRER, PageReferrer(NhGenericReferrer.LAUNCH_SIGN_IN))
					NavigationHelper.navigationLiveData.postValue(NavigationEvent(intent))
				}
				activity?.onBackPressed()
			}
		}
	}

	override fun onPageScrollStateChanged(state: Int) {

	}

	override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

	}

	override fun onPageSelected(position: Int) {
		when(position) {
			0 -> {
				if (ThemeUtils.isNightMode()) {
					binding.indicator1.setBackgroundResource(R.drawable.indicator_unselected)
					binding.indicator2.setBackgroundResource(R.drawable.indicator_selected)
				} else {
					binding.indicator1.setBackgroundResource(R.drawable.indicator_selected)
					binding.indicator2.setBackgroundResource(R.drawable.indicator_unselected)
				}
			}
			1-> {
				if (ThemeUtils.isNightMode()) {
					binding.indicator2.setBackgroundResource(R.drawable.indicator_unselected)
					binding.indicator1.setBackgroundResource(R.drawable.indicator_selected)
				} else {
					binding.indicator2.setBackgroundResource(R.drawable.indicator_selected)
					binding.indicator1.setBackgroundResource(R.drawable.indicator_unselected)
				}
			}
		}
	}
}

class WalkThroughAdapter(fragmentManager: FragmentManager, val showImports: Boolean): FragmentPagerAdapter(fragmentManager) {

	private val COUNT = 2

	override fun getItem(position: Int): Fragment {
		return WalkThroughItemFragment.newInstance(position, showImports)
	}

	override fun getCount(): Int {
		return COUNT
	}

}

class WalkThroughItemFragment: BaseFragment() {

	companion object {

		const val BUNDLE_POSITION = "position"
		const val BUNDLE_SHOW_IMPORTS = "showImports"
		const val EVENT_TYPE_SKIP_WALKTHROUGH = "skip_walkthrough"
		const val EVENT_TYPE_NEXT = "next"

		@JvmStatic
		fun newInstance(position: Int, showImports: Boolean) : WalkThroughItemFragment {
			val fragment = WalkThroughItemFragment()
			fragment.arguments = bundleOf(BUNDLE_POSITION to position, BUNDLE_SHOW_IMPORTS to showImports)
			return fragment
		}
	}

	private lateinit var binding: WalkthroughItemBinding
	private var position: Int = 0
	private var showImportContacts = false

	override fun onCreate(savedState: Bundle?) {
		super.onCreate(savedState)
		position = arguments?.getInt(BUNDLE_POSITION)?:0
		showImportContacts = arguments?.getBoolean(BUNDLE_SHOW_IMPORTS)?:false
	}

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		binding = DataBindingUtil.inflate(inflater, R.layout.walkthrough_item, container, false)
		binding.setVariable(BR.position, position)
		binding.setVariable(BR.vm, this)
		binding.executePendingBindings()

		if (position == 1) {
			binding.skipText.visibility = View.GONE
		}
		return binding.root
	}

	fun onViewClick(view: View, position: Int) {
		if (view.id == R.id.skip_text) {
			AnalyticsHelper2.logWalkThroughExploreButtonClickEvent(EVENT_TYPE_SKIP_WALKTHROUGH)
			if (showImportContacts) {
				val intent = Intent(Constants.IMPORT_CONTACTS_ACTIONS)
				intent.putExtra(Constants.BUNDLE_ACTIVITY_REFERRER, PageReferrer(NhGenericReferrer.LAUNCH_SIGN_IN))
				NavigationHelper.navigationLiveData.postValue(NavigationEvent(intent))
			}
			activity?.onBackPressed()
		}
	}
}

@BindingAdapter("bind:walkThroughImage")
fun handleImageDisplay(view: ImageView, position: Int) {
	val imageList = listOf(R.drawable.ic_profile_intro, R.drawable.ic_post_intro)
	view.setImageResource(imageList[position])

}


@BindingAdapter("bind:walkThroughImageBg")
fun handleBgImageDisplay(view: ImageView, position: Int) {
	val imageList = listOf(R.drawable.bg_profile, R.drawable.bg_repost)

	val metrics = view.context.resources.displayMetrics
	val screenWidth = metrics.widthPixels

	val params = view.layoutParams
	params.width = screenWidth
	params.height = (screenWidth*1.5f).toInt()

	view.setImageResource(imageList[position])

}

@BindingAdapter("bind:walkThroughHeader")
fun handleHeader(view: NHTextView, position: Int) {
	val textList = listOf(R.string.intro_profile, R.string.intro_post)
	val color = if (ThemeUtils.isNightMode()) R.color.theme_day_background  else R.color.black_color
	val text: String = view.context.resources.getString(textList[position],view.context.resources.getColor(color))
	view.text = Html.fromHtml(text)
}