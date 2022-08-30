/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.common.view.customview

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.newshunt.dataentity.analytics.section.NhAnalyticsEventSection
import com.newshunt.dataentity.analytics.entity.NhAnalyticsUserAction
import com.newshunt.dataentity.analytics.referrer.NhGenericReferrer
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.news.analytics.ProfileReferrerSource
import com.newshunt.dhutil.R
import com.newshunt.dhutil.commons.listener.ReferrerProviderlistener
import com.newshunt.dhutil.helper.AppSettingsProvider
import com.newshunt.deeplink.navigator.CommonNavigator

/**
 * Customized notification icon with red dot
 *
 * @author helly.patel
 */

class NHNotificationIcon : ConstraintLayout, View.OnClickListener {

    private lateinit var notificationBell : ImageButton
    private lateinit var redDot : ImageView

    constructor(context: Context) : super(context) {
        init(context, null, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context, attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context, attrs, defStyleAttr)
    }

    fun onNotificationEventChanged(notificationLiveData: Boolean) {
        if(notificationLiveData){
            redDot.visibility = View.VISIBLE
        }
        else{
            redDot.visibility = View.GONE
        }
    }

    override fun onClick(v: View?) {

        val clickSection = (v?.context as? ReferrerProviderlistener)?.referrerEventSection
        val referrer: PageReferrer? = when (clickSection) {
            NhAnalyticsEventSection.PROFILE -> PageReferrer(NhGenericReferrer.PROFILE, null, null,
                    NhAnalyticsUserAction.CLICK,
                    ProfileReferrerSource.PROFILE_HOME_VIEW)
            else -> (v?.context as? ReferrerProviderlistener)?.latestPageReferrer
        }
        CommonNavigator.launchNotificationInbox(v?.context, true, referrer)
        redDot.visibility = View.GONE
    }

    private fun init(context: Context, attrs: AttributeSet?, defStyleAttr: Int) {
        val v = LayoutInflater.from(context).inflate(R.layout.layout_notification_icon, this, true)
        notificationBell = v.findViewById(R.id.notification_image)
        redDot = v.findViewById(R.id.notification_dot_icon)

        notificationBell.setOnClickListener(this)
    }
}
