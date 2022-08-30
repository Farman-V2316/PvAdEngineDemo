/*
 * Copyright (c) 2020 Newshunt. All rights reserved.
 */
package com.newshunt.appview.common.ui.fragment

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.newshunt.appview.R
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.preference.GenericAppStatePreference
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.common.view.view.BaseSupportFragment
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.common.model.entity.ImportContactsEvents
import com.newshunt.deeplink.navigator.NewsNavigator
import com.newshunt.dhutil.analytics.AnalyticsHelper2
import com.newshunt.permissionhelper.utilities.Permission
import com.newshunt.profile.FragmentCommunicationsViewModel
import com.newshunt.sso.SSO
import com.newshunt.sso.SignInUIModes
import com.newshunt.sso.view.fragment.SignOnFragment

private const val IMPORT_CONTACTS_TAG = "ImportContacts"
private const val IMPORT_CONTACTS_SIGNIN_TAG = "ImportContactsSignIn"
private const val LOG_TAG = "ImportContactsHomeFragment"

/**
 * @author shrikant.agrawal
 */
class ImportContactsHomeFragment : BaseSupportFragment() {

	private var pendingIntent: PendingIntent? = null
	companion object {

		const val EVENT_TYPE_SKIP_SIGN_IN = "skip_launch_signin"
		const val EVENT_TYPE_SKIP_IMPORT_CONTACT = "skip_import_contact"

		@JvmStatic
		fun newInstance(intent: Intent) : ImportContactsHomeFragment {
			val fragment = ImportContactsHomeFragment()
			fragment.arguments = intent.extras
			return fragment
		}
	}

	private var pageReferrer: PageReferrer? = null
	private var referrerRaw:String? = null
	private var isSocialLogin = SSO.getInstance().isLoggedIn(false)

	override fun onCreate(savedState: Bundle?) {
		super.onCreate(savedState)
		pageReferrer = arguments?.getSerializable(Constants.BUNDLE_ACTIVITY_REFERRER) as? PageReferrer
		referrerRaw = arguments?.getString(Constants.REFERRER_RAW)
		val directLaunch = arguments?.getBoolean(Constants.IMPORT_CONTACTS_DIRECT_LAUNCH) ?: false
		if (directLaunch) {
			PreferenceManager.savePreference(GenericAppStatePreference.IMPORT_CONTACTS_WT_SHOWN, true)
		}
		observeSession()
	}

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		val rootView = inflater.inflate(R.layout.layout_dh_base_activity, container, false)
		if (!isSocialLogin) {
			addSignInFragment()
		} else {
			addImportContactsFragment()
		}
		return rootView
	}

	override fun onActivityCreated(savedInstanceState: Bundle?) {
		super.onActivityCreated(savedInstanceState)
		pendingIntent = activity?.intent?.getParcelableExtra(Constants.BUNDLE_IMPORT_CONTACTS_PENDING_INTENT)
	}

	override fun onAttach(context: Context) {
		super.onAttach(context)
		activity?.let {
			ViewModelProviders.of(it).get(FragmentCommunicationsViewModel::class.java)
					.fragmentCommunicationLiveData.observe(this, Observer { event ->
				if (event.hostId != uniqueScreenId || event.anyEnum !is ImportContactsEvents) {
					return@Observer
				}

				when (event.anyEnum) {
					ImportContactsEvents.CONTACT_PERMISSION_ALLOWED -> {
						addDiscoverFollowFragment()
						Logger.d(LOG_TAG, "Contacts permission allowed, show follow fragment")
					}
					ImportContactsEvents.IMPORT_CONTACT_SKIP -> {
						Logger.d(LOG_TAG, "Contact import skipped, lets move on!")
						AnalyticsHelper2.logWalkThroughExploreButtonClickEvent(EVENT_TYPE_SKIP_IMPORT_CONTACT, null, pageReferrer)
						onImportContactSkipped()
					}
					ImportContactsEvents.SIGN_IN_SKIP -> {
						Logger.d(LOG_TAG, "SignIn skipped, lets move on!")
						AnalyticsHelper2.logWalkThroughExploreButtonClickEvent(EVENT_TYPE_SKIP_SIGN_IN, null, pageReferrer)
						onImportContactSkipped()
					}
				}
			})
		}
	}

	override fun handleBackPress(): Boolean {
		return if (pendingIntent != null) {
			pendingIntent?.send()
			activity?.finish()
			true
		} else {
			false
		}
	}

	private fun addSignInFragment() {
		val showSkip = arguments?.getBoolean(Constants.BUNDLE_FLAG_SHOW_SKIP) ?: false
		val fragment = SignOnFragment().apply {
			val args = Bundle().apply {
				putInt(Constants.BUNDLE_UI_COMPONENT_ID, getUniqueScreenId())
				putString(Constants.BUNDLE_SIGN_ON_UI_MODE, SignInUIModes.SIGN_IN_FOR_SOCIAL_ONBOARDING.name)
				putSerializable(Constants.BUNDLE_ACTIVITY_REFERRER, pageReferrer)
				putBoolean(Constants.BUNDLE_REFERRER_VIEW_IS_FVP, true)
				putBoolean(Constants.BUNDLE_FLAG_SHOW_SKIP, showSkip)
				putBoolean(Constants.BUNDLE_LINK_ACCOUNTS_POST_LOGIN, true)
			}
			this.arguments = args
		}
		childFragmentManager.beginTransaction()
				.replace(R.id.dh_base_container_fragment, fragment, IMPORT_CONTACTS_SIGNIN_TAG)
				.commit()
	}



	private fun addImportContactsFragment() {
		//If permission is already granted, no need to open the import fragment
		if (activity?.checkSelfPermission(Permission.READ_CONTACTS.permission) == PackageManager.PERMISSION_GRANTED) {
			addDiscoverFollowFragment()
			return
		}
		val fragment = ImportContactsFragment.newInstance(uniqueScreenId,
				pageReferrer)
		childFragmentManager.beginTransaction()
				.replace(R.id.dh_base_container_fragment, fragment, IMPORT_CONTACTS_TAG)
				.commit()
	}

	private fun addDiscoverFollowFragment() {
		val fragment = ImportFollowFragment()
		fragment.arguments = arguments
		childFragmentManager.beginTransaction()
			.replace(R.id.dh_base_container_fragment, fragment, IMPORT_CONTACTS_TAG)
			.commit()
	}

	private fun onImportContactSkipped() {
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

	private fun observeSession() {
		SSO.getInstance().userDetailsLiveData.observe(this, Observer {
			val newSocialLogin = SSO.getInstance().isLoggedIn(false)
			if (!isSocialLogin && newSocialLogin) {
				addImportContactsFragment()
			}
			isSocialLogin = newSocialLogin
		})
	}
}
