/*
 * Copyright (c) 2020 Newshunt. All rights reserved.
 */

package com.newshunt.news.view.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.newshunt.appview.R
import com.newshunt.common.helper.appconfig.AppConfig
import com.newshunt.common.helper.common.DHConstants
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.news.util.NewsConstants

/**
 * @author priya.gupta
 */

class LocationBottomBarActivity : NewsBaseActivity() {

    private lateinit var progressbarContainer:LinearLayout
    private lateinit var framelayout:FrameLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_dh_base_activity)
        progressbarContainer = findViewById(R.id.progressbar_parent)
        framelayout = findViewById(R.id.dh_base_container_fragment)
//        showProgressBar()
//        SocialDB.instance().followEntityDao().getAllFollowedLocations().observe(this,
//                Observer<List<FollowSyncEntity?>?> { follows ->
//
//                    if (follows != null && !follows.isEmpty()) {
//
//                        launchLocationVideoActivity()
//
//                    } else {
//                        launchLocationActivity()
//                    }
//
//
//                })
        if(CommonUtils.getFollowedLocationsCount() > 0)
        {
            launchLocationVideoActivity()
        }
        else{
            launchLocationActivity()
        }
    }

    private fun launchLocationActivity() {

        val intent = Intent(DHConstants.OPEN_LOCATION_SELECTION)
        intent.setPackage(AppConfig.getInstance().packageName)
        startActivityForResult(intent, NewsConstants
                .REQUEST_CODE_LOCATION_SEARCH)
        finish()
    }

    private fun launchLocationVideoActivity() {
        val intent = Intent(DHConstants.OPEN_LOCAL_VIDEO)
        intent.setPackage(AppConfig.getInstance().packageName)
        startActivityForResult(intent, NewsConstants
                .REQUEST_CODE_LOCATION_SEARCH)
        finish()
    }

    private fun showProgressBar()
    {
        framelayout.visibility = View.GONE
    }

}