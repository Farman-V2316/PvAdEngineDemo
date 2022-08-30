/*
 * Copyright (c) 2020 Newshunt. All rights reserved.
 */
package com.newshunt.notification.view.activity

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Html
import android.text.Spannable
import android.view.View
import android.widget.CheckBox
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.preference.GenericAppStatePreference
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.common.view.customview.NHImageView
import com.newshunt.common.view.customview.fontview.NHTextView
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dhutil.analytics.AnalyticsHelper
import com.newshunt.notification.R
import com.newshunt.sdk.network.image.Image

/**
 * UI to display message from remote actionable payload
 *
 * Created by karthik.r on 26/06/20.
 */
open class ActionableNotificationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.actionable_notification_activity)
        val displayStr = intent.getStringExtra(Constants.DH_BDT)
        val actionId = intent.getStringExtra(Constants.ACTION_ID)

        findViewById<View>(R.id.parent_view).setOnClickListener {
            finish()
            overridePendingTransition(0, 0)
            AnalyticsHelper.logActionableActivityClosed(actionId, Constants.DESIGN_1)
        }

        findViewById<View>(R.id.notification_message).setOnClickListener {
            val pendingIntent: Intent? = intent.getParcelableExtra(Constants.BUNDLE_EXTRA)
            if (pendingIntent != null) {
                startActivity(pendingIntent)
            }

            finish()
            overridePendingTransition(0, 0)
            AnalyticsHelper.logActionableActivityClicked(actionId, Constants.DESIGN_1)
        }

        if (displayStr != null) {
            findViewById<TextView>(R.id.notification_message).text = displayStr
        }

        AnalyticsHelper.logActionableActivityView(actionId, Constants.DESIGN_1)
    }
}

class BigActionableNotificationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setBackgroundDrawable(ColorDrawable(CommonUtils.getColor(com.newshunt.common.util.R.color.notification_activity_background)))
        setContentView(R.layout.big_actionable_notification_activity)
        val imageUrl = intent.getStringExtra(Constants.DH_IMG)
        val imgUrl1 = intent.getStringExtra(Constants.DH_IMG1)
        val imgUrl2 = intent.getStringExtra(Constants.DH_IMG2)
        val imgUrl3 = intent.getStringExtra(Constants.DH_IMG3)
        val msg1 = intent.getStringExtra(Constants.DH_M1)
        val msg2 = intent.getStringExtra(Constants.DH_M2)
        val msg3 = intent.getStringExtra(Constants.DH_M3)
        val checkboxText = intent.getStringExtra(Constants.DH_DND)
        val displayStr = intent.getStringExtra(Constants.DH_BDT)
        val actionId = intent.getStringExtra(Constants.ACTION_ID)

        if (displayStr != null) {
            findViewById<NHTextView>(R.id.notification_message).text = displayStr
        }

        if (imgUrl1 != null) {
            Image.load(imgUrl1).placeHolder(R.mipmap.icon).into(findViewById<NHImageView>(R.id.card_profile_pic_1))
        }
        if (imgUrl2 != null) {
            Image.load(imgUrl2).placeHolder(R.mipmap.icon).into(findViewById<NHImageView>(R.id.card_profile_pic_2))
        }
        if (imgUrl3 != null) {
            Image.load(imgUrl3).placeHolder(R.mipmap.icon).into(findViewById<NHImageView>(R.id.card_profile_pic_3))
        }

        if (msg1 != null) {
            findViewById<NHTextView>(R.id.card_text_1).setSpannableText(Html.fromHtml(msg1) as Spannable?, msg1)
        }
        if (msg2 != null) {
            findViewById<NHTextView>(R.id.card_text_2).setSpannableText(Html.fromHtml(msg2) as Spannable?, msg2)
        }
        if (msg3 != null) {
            findViewById<NHTextView>(R.id.card_text_3).setSpannableText(Html.fromHtml(msg3) as Spannable?, msg3)
        }

        findViewById<CheckBox>(R.id.enable_disable_checkbox).text = checkboxText
        findViewById<View>(R.id.parent_view).setOnClickListener {
            finish()
            overridePendingTransition(0, 0)
            AnalyticsHelper.logActionableActivityClosed(actionId, Constants.DESIGN_3)
        }

        val checkbox = findViewById<CheckBox>(R.id.enable_disable_checkbox)

        findViewById<NHTextView>(R.id.notification_message).setOnClickListener {
            val pendingIntent: Intent? = intent.getParcelableExtra<Intent>(Constants.BUNDLE_EXTRA)
            if (pendingIntent != null && !checkbox.isChecked) {
                startActivity(pendingIntent)
                AnalyticsHelper.logActionableActivityClicked(actionId, Constants.DESIGN_3)
            }
            else {
                // Mark as blocked
                markAsBlocked(actionId)
            }

            finish()
            overridePendingTransition(0, 0)
        }

        val button = findViewById<NHTextView>(R.id.enable_disable_notificaton_button)
        val positiveBtnText = intent.getStringExtra(Constants.DH_PT)
        if (positiveBtnText != null && button.isEnabled) {
            button.text = positiveBtnText
        }

        val negativeBtnText = intent.getStringExtra(Constants.DH_NT)
        if (positiveBtnText != null && !button.isEnabled) {
            button.text = negativeBtnText
        }

        checkbox.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                button.setBackgroundColor(resources.getColor(com.newshunt.dhutil.R.color.text_red))
                button.text = negativeBtnText
            } else {
                button.setBackgroundColor(resources.getColor(com.newshunt.dhutil.R.color.social_handle_text_color))
                button.text = positiveBtnText
            }
        }

        button.setOnClickListener {
            val pendingIntent: Intent? = intent.getParcelableExtra<Intent>(Constants.BUNDLE_EXTRA)
            if (pendingIntent != null && !checkbox.isChecked) {
                startActivity(pendingIntent)
                AnalyticsHelper.logActionableActivityClicked(actionId, Constants.DESIGN_3)
            }
            else {
                // Mark as blocked
                markAsBlocked(actionId)
            }
            finish()
            overridePendingTransition(0, 0)
        }

        findViewById<View>(R.id.clickable_area).setOnClickListener {
            val pendingIntent: Intent? = intent.getParcelableExtra<Intent>(Constants.BUNDLE_EXTRA)
            if (pendingIntent != null && !checkbox.isChecked) {
                startActivity(pendingIntent)
                AnalyticsHelper.logActionableActivityClicked(actionId, Constants.DESIGN_3)
            }
            else {
                // Mark as blocked
                markAsBlocked(actionId)
            }

            finish()
            overridePendingTransition(0, 0)
        }

        AnalyticsHelper.logActionableActivityView(actionId, Constants.DESIGN_3)
    }

    private fun markAsBlocked(actionId: String?) {
        if (actionId != null) {
            val blockedIds : HashSet<String> =
                    PreferenceManager.getPreference(GenericAppStatePreference.BLOCKED_ACTION_IDS, HashSet<String>())
            blockedIds.add(actionId)
            PreferenceManager.savePreference(GenericAppStatePreference.BLOCKED_ACTION_IDS, blockedIds)
        }

        AnalyticsHelper.logActionableActivityBlocked(actionId ?: "UNKNOWN", Constants.DESIGN_3)
    }
}