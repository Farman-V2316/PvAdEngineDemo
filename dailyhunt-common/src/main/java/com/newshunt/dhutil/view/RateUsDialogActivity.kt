/*
* Copyright (c) 2021 Newshunt. All rights reserved.
*/

package com.newshunt.dhutil.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManager
import com.google.android.play.core.review.ReviewManagerFactory
import com.newshunt.analytics.entity.NhAnalyticsDialogEvent
import com.newshunt.common.helper.common.AndroidUtils
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.font.FontHelper
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.common.view.customview.NHBaseActivity
import com.newshunt.dataentity.analytics.section.NhAnalyticsEventSection
import com.newshunt.dataentity.common.helper.analytics.NhAnalyticsReferrer
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dhutil.R
import com.newshunt.dhutil.analytics.DialogAnalyticsHelper
import com.newshunt.dhutil.databinding.ActivityRateusNewBinding
import com.newshunt.dhutil.helper.RateUsConfigCheckHelper
import com.newshunt.dhutil.helper.RateUsDialogAction
import com.newshunt.dhutil.helper.RateUsDialogHelper
import com.newshunt.dhutil.helper.RateUsTriggerAction
import com.newshunt.dhutil.helper.preference.AppRatePreference
import com.newshunt.dhutil.helper.preference.AppStatePreference
import com.newshunt.dhutil.helper.theme.ThemeType
import com.newshunt.dhutil.helper.theme.ThemeUtils
import com.newshunt.news.util.NewsConstants

/**
 * App rate us activity.
 *
 *
 * Created by aman.roy on 12/8/2021.
 */

private const val  LOG_TAG = "RateUsDialogActivity"
class RateUsDialogActivity : NHBaseActivity() {
    private var trigger_action: String? = null
    private var referrer: NhAnalyticsReferrer? = null
    private var section: NhAnalyticsEventSection? = null
    private lateinit var binding: ActivityRateusNewBinding
    private var isPositiveClick = false
    private var isInAppReviewFlow = Constants.SHOW_IN_APP_RATING_FLOW
    private var manager: ReviewManager? = null
    private var reviewInfo: ReviewInfo? = null
    private var rating = 5f;  // Initialised with maximum rating.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Setting up custom day and night theme.
        val themeID = if (ThemeUtils.preferredTheme == ThemeType.DAY) R.style.NoActionBarDay else R.style.NoActionBarNight
        setTheme(themeID)
        setContentView(R.layout.activity_rateus_new)
        binding =  DataBindingUtil.setContentView(this, R.layout.activity_rateus_new)
        intent?.extras?.let{ bundle ->
            trigger_action = bundle.getString(Constants.TRIGGER_ACTION, RateUsTriggerAction.CLICK.triggerAction)
            referrer = bundle[Constants.BUNDLE_ACTIVITY_REFERRER] as NhAnalyticsReferrer?
            section = bundle[NewsConstants.DH_SECTION] as NhAnalyticsEventSection?
            DialogAnalyticsHelper.deployRateUsViewedEvent(trigger_action, referrer,
                    section)
        }
        isInAppReviewFlow = PreferenceManager.getPreference(AppStatePreference.SHOW_IN_APP_RATING_FLOW,Constants.SHOW_IN_APP_RATING_FLOW)
        setUpDialog()
        RateUsConfigCheckHelper.setRateUsShownTime(System.currentTimeMillis())
        RateUsConfigCheckHelper.incrementAppRateShowCount()
        RateUsDialogHelper.resetAppLaunchCountOnShowingRateUs()
        RateUsDialogHelper.resetBookReadCountOnShowingRateUs()
        RateUsDialogHelper.resetStoryViewedCountPerSessionOnSessionClose()
        RateUsDialogHelper.updateRateScreenShownAfterUpgrade()
    }

    private fun setUpDialog() {
        binding.dialogTitleTextInApp.text = CommonUtils.getString(R.string.app_rate_dialog_title_text)
        binding.dialogTitleTextPlayStore.text = CommonUtils.getString(R.string.app_rate_dialog_title_text)
        binding.appRateBackground.setOnClickListener { finish() }
        binding.appRateDialog.setOnClickListener {
            // Do nothing, kept here to avoid dialogBackground.onClick action
        }

        if(isInAppReviewFlow) {
            requestReviewInfoObject()
        }

        binding.appRateCancelBtn.setOnClickListener {
            DialogAnalyticsHelper.triggerRateUsAnalyticsEvent(trigger_action, RateUsDialogAction.CROSS_DISMISS.dialogAction, referrer, section,
                    NhAnalyticsDialogEvent.DIALOGBOX_ACTION)
            finish()
        }
    }

    private fun requestReviewInfoObject() {
        manager = ReviewManagerFactory.create(this)
        val request = manager?.requestReviewFlow()
        request?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // We got the ReviewInfo object
                reviewInfo = task.result
            } else {
                // There was some problem, log or handle the error code.
                Logger.caughtException(task.getException())
                finish()
            }
        }
    }

    fun onClick(v:View) {
        when(v) {
            binding.rating1 -> {
                rating = 1f
                isPositiveClick = false
                handleStarsUI(true,false,false,false,false)
            }
            binding.rating2 -> {
                rating = 2f
                isPositiveClick = false
                handleStarsUI(true,true,false,false,false)
            }
            binding.rating3 -> {
                rating = 3f
                isPositiveClick = false
                handleStarsUI(true,true,true,false,false)
            }
            binding.rating4 -> {
                rating = 4f
                isPositiveClick = true
                handleStarsUI(true,true,true,true,false)
            }
            binding.rating5 -> {
                rating = 5f
                isPositiveClick = true
                handleStarsUI(true,true,true,true,true)
            }
        }

        DialogAnalyticsHelper.triggerRateUsAnalyticsEvent(trigger_action,
                RateUsDialogAction.RATING_BAR.dialogAction, referrer, section,
                NhAnalyticsDialogEvent.DIALOGBOX_ACTION, rating)

        if(!isPositiveClick) {
            DialogAnalyticsHelper.triggerRateUsAnalyticsEvent(trigger_action, RateUsDialogAction.FEEDBACK.dialogAction, referrer, section,
                    NhAnalyticsDialogEvent.DIALOGBOX_ACTION)
            setUpFeedBackUI()
        } else if(isInAppReviewFlow) {
            openInappReview()
        } else {
            DialogAnalyticsHelper.triggerRateUsAnalyticsEvent(trigger_action, RateUsDialogAction.RATE_NOW.dialogAction, referrer, section,
                    NhAnalyticsDialogEvent.DIALOGBOX_ACTION)
            setUpGooglePlayUI()
        }
    }

    private fun handleStarsUI(first : Boolean, second : Boolean, third : Boolean, fourth : Boolean, fifth : Boolean) {
        binding.rating1.isSelected = first
        binding.rating2.isSelected = second
        binding.rating3.isSelected = third
        binding.rating4.isSelected = fourth
        binding.rating5.isSelected = fifth
    }

    private fun openInappReview() {
        DialogAnalyticsHelper.triggerRateUsAnalyticsEvent(trigger_action, RateUsDialogAction.RATE_NOW.dialogAction, referrer, section,
                NhAnalyticsDialogEvent.DIALOGBOX_ACTION)
        binding.appRateBackground.visibility = View.GONE
        reviewInfo?.let {
            val flow = manager?.launchReviewFlow(this, it)
            flow?.addOnCompleteListener({
                Logger.d(LOG_TAG, "Review Flow completed")
                finish()
            })
            updateRateNowClicked()
        }
    }

    private fun setUpFeedBackUI() {
        binding.submitText.text = CommonUtils.getString(R.string.help_us_improve)
        setUpPlaystoreOrFeedbackUI()
    }

    private fun setUpGooglePlayUI() {
        binding.submitText.text = CommonUtils.getString(R.string.rate_us_playstore_string)
        setUpPlaystoreOrFeedbackUI()
    }

    private fun setUpPlaystoreOrFeedbackUI() {
        binding.dialogTitleTextPlayStore.visibility = View.VISIBLE
        binding.dialogTitleTextInApp.visibility = View.GONE
        binding.submitButton.visibility = View.VISIBLE
        binding.submitButton.setOnClickListener({
            openGooglePlayOrFeedbackActivity()
            finish()
        })

    }

    private fun openGooglePlayOrFeedbackActivity() {
        if(isPositiveClick) {
            openGooglePlayRating()
        } else {
            openFeedBackActivity()
        }
    }

    private fun openFeedBackActivity() {
        val feedbackIntent = Intent()
        feedbackIntent.setPackage(CommonUtils.getApplication().packageName)
        feedbackIntent.action = Constants.FEEDBACK_OPEN
        startActivity(feedbackIntent)
        updateRateNowClicked()
    }

    private fun openGooglePlayRating() {
        FontHelper.showCustomFontToast(this,CommonUtils.getString(R.string.playstore_toast_text),Toast.LENGTH_LONG)
        AndroidUtils.openPlayStoreForApp(this@RateUsDialogActivity, Constants.APP_PLAY_STORE_LINK,
                Constants.APP_MARKET_LINK)
        updateRateNowClicked()
    }

    private fun updateRateNowClicked() {
        PreferenceManager.savePreference(AppRatePreference.APPRATE_IS_USER_CLICKED_RATE_NOW, true)
    }

    public override fun onStop() {
        super.onStop()
        finish()
    }

    companion object {
        fun openDialog(action: RateUsTriggerAction, referrer: NhAnalyticsReferrer?, section: NhAnalyticsEventSection?) {
            val context: Context = CommonUtils.getApplication()
            val rateUsIntent = Intent(context, RateUsDialogActivity::class.java)
            rateUsIntent.putExtra(Constants.TRIGGER_ACTION, action.triggerAction)
            rateUsIntent.putExtra(Constants.BUNDLE_ACTIVITY_REFERRER, referrer)
            rateUsIntent.putExtra(NewsConstants.DH_SECTION, section)
            rateUsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(rateUsIntent)
        }
    }
}