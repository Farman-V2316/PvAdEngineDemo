package com.newshunt.news.view.activity

import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.newshunt.appview.R
import com.newshunt.appview.common.di.DaggerLocationVideoListComponent
import com.newshunt.appview.common.ui.helper.NavigationHelper
import com.newshunt.appview.common.ui.helper.SnackbarViewModel
import com.newshunt.appview.common.video.localzone.LocalZoneFragment
import com.newshunt.common.helper.appconfig.AppConfig
import com.newshunt.common.helper.common.Constants
import com.newshunt.dhutil.bundleOf
import javax.inject.Inject

class LocationVideoListingActivity : NewsBaseActivity() {
    private var acceptableTimeStamp = System.currentTimeMillis()

    @Inject
    lateinit var snackbarViewModelFactory: SnackbarViewModel.Factory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_local_zone)
        val fragment = LocalZoneFragment()
        fragment.arguments = bundleOf(
                Constants.BUNDLE_IS_LOCAL_ZONE to true,
                Constants.BUNDLE_CONTENT_URL to AppConfig.getInstance().localZoneUrl)
        supportFragmentManager.beginTransaction().replace(R.id.frameLayout, fragment, "frag").commit()
        val containerId = R.id.frameLayout
        NavigationHelper.navigationLiveData.observe(this, Observer {
            if (it.timeStamp < acceptableTimeStamp) {
                return@Observer
            } else {
                NavigationHelper.handleNavigationEvents(it, this, containerId)
            }
        })

        DaggerLocationVideoListComponent.create().inject(this)
        ViewModelProviders.of(this, snackbarViewModelFactory).get(SnackbarViewModel::class.java)
                .also {
                    it.followChanges.observe(this, Observer { res ->
                        SnackbarViewModel.onFollowChangeEvent(res, findViewById(containerId))
                    })
                    it.newPostChanges.observe(this, Observer { res ->
                        SnackbarViewModel.onPostUploaded(res, findViewById(containerId), true, null, R.string.view_photo_in_lite_mode_message)
                    })
                    it.start()
                }
    }

    override fun onStart() {
        super.onStart()
        acceptableTimeStamp = System.currentTimeMillis()
    }

    override fun onBackPressed() {
        NavigationHelper.onBackPressed(this, R.id.frameLayout)
    }
}