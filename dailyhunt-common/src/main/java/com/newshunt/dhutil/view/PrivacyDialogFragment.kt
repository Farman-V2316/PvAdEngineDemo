/*
 * Copyright (c) 2021 Newshunt. All rights reserved.
 */
package com.newshunt.dhutil.view

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.*
import androidx.constraintlayout.widget.ConstraintSet
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProviders
import com.newshunt.analytics.entity.DialogBoxType
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.NHWebViewUtils
import com.newshunt.common.helper.font.HtmlFontHelper
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.common.view.customview.CommonMessageEvents
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.analytics.section.NhAnalyticsEventSection
import com.newshunt.dhutil.R
import com.newshunt.dhutil.analytics.DialogAnalyticsHelper
import com.newshunt.dhutil.databinding.NewPrivacyDialogBinding
import com.newshunt.dhutil.helper.common.DailyhuntConstants
import com.newshunt.dhutil.helper.preference.AppStatePreference
import com.newshunt.dhutil.helper.theme.ThemeUtils
import com.newshunt.news.util.NewsConstants
import com.newshunt.profile.FragmentCommunicationEvent
import com.newshunt.profile.FragmentCommunicationsViewModel

/**
 * Privacy dialog implementation
 *
 * Created by srikanth.r on 12/24/21.
 */
class PrivacyDialogFragment: DialogFragment(), View.OnClickListener {
    private var viewbinding: NewPrivacyDialogBinding? = null
    private var hostId: Int = 0
    private var referrer: PageReferrer? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return Dialog(activity as Context).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            val inflater = LayoutInflater.from(activity)
            viewbinding = DataBindingUtil.inflate(inflater, R.layout.new_privacy_dialog, null, false)
            viewbinding?.let { binding ->
                setContentView(binding.root)
                binding.title = arguments?.getString(DailyhuntConstants.PRIVACY_TITLE) ?: Constants.EMPTY_STRING
                binding.positive = arguments?.getString(DailyhuntConstants.PRIVACY_POSITIVE_BTN) ?: Constants.EMPTY_STRING
                binding.negative = arguments?.getString(DailyhuntConstants.PRIVACY_NEGATIVE_BTN) ?: Constants.EMPTY_STRING
                val canUserIgnore = arguments?.getBoolean(DailyhuntConstants.PRIVACY_CAN_USER_IGNORE) ?: true
                binding.canUserIgnore = canUserIgnore
                NHWebViewUtils.initializeWebView(binding.privacyHtml)
                binding.privacyHtml.settings.domStorageEnabled = true
                binding.privacyHtml.settings.useWideViewPort = true
                binding.privacyHtml.settings.javaScriptEnabled = true
                if (!canUserIgnore) {
                    val constraintSet = ConstraintSet()
                    constraintSet.clone(binding.privacyContentParent)
                    constraintSet.constrainPercentHeight(R.id.privacy_html, 0.54f)
                    constraintSet.applyTo(binding.privacyContentParent)
                }
                hostId = arguments?.getInt(DailyhuntConstants.HOST_ID, 0) ?: 0
                referrer = arguments?.getSerializable(Constants.REFERRER) as? PageReferrer?
                val html = HtmlFontHelper.wrapToFontHTML(
                    true,
                    arguments?.getString(DailyhuntConstants.PRIVACY_DESC),
                    Constants.EMPTY_STRING,
                    Constants.EMPTY_STRING,
                    false,
                    ThemeUtils.isNightMode(),
                    null,0
                )
                binding.privacyHtml.loadDataWithBaseURL(Constants.EMPTY_STRING, html, NewsConstants.HTML_MIME_TYPE, NewsConstants.HTML_UTF_ENCODING, null)
                binding.privacyHtml.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
                setCanceledOnTouchOutside(true)
                window?.let {
                    it.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                    it.setGravity(Gravity.CENTER)
                    it.setBackgroundDrawableResource(R.color.transparent)
                }
                binding.privacyPositive.setOnClickListener(this@PrivacyDialogFragment)
                binding.privacyNegative.setOnClickListener(this@PrivacyDialogFragment)
                binding.executePendingBindings()
                DialogAnalyticsHelper.logDialogBoxViewedEvent(DialogBoxType.PERMISSION_APPS_ON_DEVICES,
                    referrer,
                    NhAnalyticsEventSection.APP,
                    null)
            }
        }
    }

    override fun onClick(v: View?) {
        v ?: return
        when(v.id) {
            R.id.privacy_positive -> {
                PreferenceManager.savePreference(AppStatePreference.PRIVACY_V2_ACCEPTED, true)
                notifyViewModel(CommonMessageEvents.POSITIVE_CLICK)
                DialogAnalyticsHelper.logDialogBoxActionEvent(DialogBoxType.PERMISSION_APPS_ON_DEVICES,
                    referrer,
                    Constants.DIALOG_ACCEPT,
                    NhAnalyticsEventSection.APP,
                    null)
                dismiss()
            }
            R.id.privacy_negative -> {
                PreferenceManager.savePreference(AppStatePreference.PRIVACY_V2_ACCEPTED, false)
                notifyViewModel(CommonMessageEvents.NEGATIVE_CLICK)
                DialogAnalyticsHelper.logDialogBoxActionEvent(DialogBoxType.PERMISSION_APPS_ON_DEVICES,
                    referrer,
                    Constants.DIALOG_LATER,
                    NhAnalyticsEventSection.APP,
                    null)
                dismiss()
            }
            else -> {}
        }
    }

    override fun onCancel(dialog: DialogInterface) {
        notifyViewModel(CommonMessageEvents.DISMISS)
        super.onCancel(dialog)
    }

    private fun notifyViewModel(event: CommonMessageEvents) {
        activity?.run {
            val viewModel = ViewModelProviders.of(this).get(FragmentCommunicationsViewModel::class.java)
            viewModel.fragmentCommunicationLiveData.postValue(
                FragmentCommunicationEvent(hostId, event)
            )
        }
    }

    companion object {
        const val LOG_TAG = "PrivacyDialogFragment"
        @JvmStatic
        fun newInstance(hostId: Int,
                        title: String?,
                        description: String?,
                        positiveButton: String?,
                        negativeButton: String?,
                        referrer: PageReferrer?,
                        canUserIgnore: Boolean = true): PrivacyDialogFragment? {
            return if(title.isNullOrBlank() ||
                description.isNullOrBlank() ||
                positiveButton.isNullOrBlank() ||
                negativeButton.isNullOrBlank()) {
                null
            } else {
                PrivacyDialogFragment().apply {
                    val bundle = Bundle()
                    bundle.putInt(DailyhuntConstants.HOST_ID, hostId)
                    bundle.putString(DailyhuntConstants.PRIVACY_TITLE, title)
                    bundle.putString(DailyhuntConstants.PRIVACY_DESC, description)
                    bundle.putString(DailyhuntConstants.PRIVACY_POSITIVE_BTN, positiveButton)
                    bundle.putString(DailyhuntConstants.PRIVACY_NEGATIVE_BTN, negativeButton)
                    bundle.putBoolean(DailyhuntConstants.PRIVACY_CAN_USER_IGNORE, canUserIgnore)
                    bundle.putSerializable(Constants.REFERRER, referrer)
                    arguments = bundle
                }
            }
        }
    }
}