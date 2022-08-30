package com.newshunt.news.view.activity

import android.os.Bundle
import com.newshunt.appview.R
import com.newshunt.common.view.customview.NHBaseActivity

/**
 * Transparent activity to be launched with clear flags to kill ad SDK activities.
 */
class AdDummyActivity : NHBaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ad_dummy)
        finish()
    }
}