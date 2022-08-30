/*
 * Copyright (c) 2020 Newshunt. All rights reserved.
 */
package com.newshunt.appview.common.ui.fragment

import android.app.PendingIntent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import com.newshunt.appview.R
import com.newshunt.appview.common.CardsExternalListener
import com.newshunt.appview.common.CardsFragment
import com.newshunt.appview.common.viewmodel.ImportFollowViewModel
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.preference.GenericAppStatePreference
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.common.view.customview.NHImageView
import com.newshunt.common.view.customview.fontview.NHTextView
import com.newshunt.common.view.view.BaseFragment
import com.newshunt.dataentity.analytics.referrer.NhGenericReferrer
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.analytics.section.NhAnalyticsEventSection
import com.newshunt.dataentity.common.asset.Format
import com.newshunt.dataentity.notification.BaseModel
import com.newshunt.deeplink.navigator.NewsNavigator
import com.newshunt.dhutil.analytics.AnalyticsHelper2
import com.newshunt.dhutil.bundleOf
import com.newshunt.dhutil.helper.common.DailyhuntConstants
import com.newshunt.news.util.NewsConstants
import java.net.URLDecoder

/**
 * @author shrikant.agrawal
 */
class ImportFollowFragment : BaseFragment(), CardsExternalListener {

	private lateinit var importFollowViewModel: ImportFollowViewModel
	private var pageReferrer: PageReferrer? = null
	private var referrerRaw:String? = null
	private var customTitle: String? = null

	override fun onCreate(savedState: Bundle?) {
		super.onCreate(savedState)
		pageReferrer = arguments?.getSerializable(Constants.BUNDLE_ACTIVITY_REFERRER) as? PageReferrer
		referrerRaw = arguments?.getString(Constants.REFERRER_RAW)
		importFollowViewModel = ViewModelProviders.of(this).get(ImportFollowViewModel::class.java)
		var apiReferrer = DEFAULT_API_REFERRER
		(activity?.intent?.getSerializableExtra(Constants.BUNDLE_CONTACT_RECO_MODEL) as? BaseModel?)?.let { baseModel ->
			baseModel.baseInfo?.queryParams?.let { queryParams ->
				apiReferrer = queryParams[Constants.REFERRER] ?: DEFAULT_API_REFERRER
				customTitle = try {
					URLDecoder.decode(queryParams[DailyhuntConstants.TITLE], Constants.TEXT_ENCODING_UTF_8)
				} catch (ex: Exception) {
					Logger.caughtException(ex)
					null
				}
			}
		}
		importFollowViewModel.insertPage(apiReferrer)
	}


	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		val view = inflater.inflate(R.layout.activity_followed_entities, container, false)

		setupActionbar(view)
		activity?.let { Glide.with(it).load(R.raw.finding_friends).into(view.findViewById<NHImageView>(R
				.id.img_loading)) }
		addCardsFragments()
		return view
	}

	private fun addCardsFragments() {
		childFragmentManager.beginTransaction().replace(R.id.follow_fragment_view, CardsFragment.create(createBundle())).commit()
	}

	private fun createBundle(): Bundle {
		val bundle = bundleOf(
				Constants.PAGE_ID to Constants.IMPORT_FOLLOW_PAGE_ID,
				Constants.LIST_TYPE to Format.ENTITY.name,
				Constants.REFERRER to pageReferrer,
				NewsConstants.DH_SECTION to NhAnalyticsEventSection.APP.eventSection,
				Constants.CARDS_FRAG_DISABLE_PULL_TO_REFRESH to true,
                Constants.BUNDLE_IS_IMPORT_CONTACTS_FRAGMENT to true)
		return bundle
	}

	override fun updateCurrentCardPosition(visibleItemCount: Int, firstVisibleItem: Int, totalItemCount: Int) {
		//do nothing
	}

	override fun onCardsLoaded() {
		super.onCardsLoaded()
		hideWatingLayout()
	}

	override fun onCardsLoadError(fullPageError: Boolean) {
		super.onCardsLoadError(fullPageError)
		hideWatingLayout()
	}
	private fun hideWatingLayout(){
		view?.findViewById<ConstraintLayout>(R.id.layout_waiting)?.visibility = View.GONE
	}

	fun markImportSuccess() {
		PreferenceManager.savePreference(GenericAppStatePreference.IMPORT_CONTACTS_WT_SHOWN, true)
	}

	private fun setupActionbar(rootView: View) {
		val discoverTitle = rootView.findViewById<NHTextView>(R.id.discover_title)
		discoverTitle.visibility = View.VISIBLE

		val backButton = rootView.findViewById<FrameLayout>(R.id.toolbar_back_button)
		rootView.findViewById<NHTextView>(R.id.title).visibility = View.GONE

		val doneView = rootView.findViewById<NHTextView>(R.id.discover_done_action)
		if (customTitle == null) {
			doneView.setOnClickListener {
				AnalyticsHelper2.logImportFollowDoneClick(followMap.size, pageReferrer, TYPE_FOLLOWED)
				handleBack()
			}
			doneView.visibility = View.VISIBLE
			backButton.visibility = View.GONE
		} else {
			discoverTitle.text = customTitle
			backButton.visibility = View.VISIBLE
			doneView.visibility = View.GONE
			backButton.setOnClickListener {
				handleBack()
			}
		}
	}

	private fun handleBack() {
		val pendingIntent = activity?.intent?.getParcelableExtra<PendingIntent>(Constants.BUNDLE_IMPORT_CONTACTS_PENDING_INTENT)
		pendingIntent?.let {
			activity?.onBackPressed()
			return
		}
		if (NewsNavigator.shouldNavigateToHome(activity, pageReferrer, false,referrerRaw)) {
			NewsNavigator.navigateToHomeOnLastExitedTab(activity, null)
		} else {
			activity?.onBackPressed()
		}
	}

	companion object {
		const val TYPE_FOLLOWED = "followed"
		const val DEFAULT_API_REFERRER = "ONBOARDING"
		private val followMap = HashSet<String>()

		@JvmStatic
		fun onFollowChange(entityId: String, isFollowed: Boolean) {
			if (isFollowed) {
				followMap.add(entityId)
			} else {
				followMap.remove(entityId)
			}
		}
	}
}
