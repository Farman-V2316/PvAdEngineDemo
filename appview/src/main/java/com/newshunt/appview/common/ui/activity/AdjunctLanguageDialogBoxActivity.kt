/*
 * Copyright (c) 2021 Newshunt. All rights reserved.
 */
package com.newshunt.appview.common.ui.activity

import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.newshunt.appview.R
import com.newshunt.appview.common.viewmodel.AdjunctLanguageViewModel
import com.newshunt.appview.databinding.ActivityAdjunctLanguageBinding
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.common.view.customview.NHBaseActivity
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.analytics.section.NhAnalyticsEventSection
import com.newshunt.dataentity.common.model.AdjunctLangResponse
import com.newshunt.dhutil.analytics.AnalyticsHelper2
import com.newshunt.dhutil.analytics.DialogAnalyticsHelper
import com.newshunt.dhutil.helper.preference.AdjunctLangPreference
import com.newshunt.dhutil.helper.preference.UserPreferenceUtil
import com.newshunt.news.util.NewsConstants
import com.newshunt.onboarding.helper.AdjunctLanguageUtils
import com.newshunt.sdk.network.image.Image

/**
 * Adjunct language dialog box activity which pops up when user comes from
 * adjunct lang notification and clicks back from detail page.
 * @author aman.roy
 */
class AdjunctLanguageDialogBoxActivity: NHBaseActivity() {
    lateinit var binding:ActivityAdjunctLanguageBinding
    lateinit var vmFactory:AdjunctLanguageViewModel.AdjunctLanguageViewModelF
    lateinit var vm:AdjunctLanguageViewModel
    private var layoutType = Constants.SHOW_ADJUNCT_LANG_DISPLAY_TYPE_2;
    private var adjunctLang = Constants.EMPTY_STRING;
    private var defaultLang = Constants.EMPTY_STRING;
    private var providedReferrer: PageReferrer? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_adjunct_language)
        extractArguments(intent.extras)
        initView()
        initViewModel()
        DialogAnalyticsHelper.logAdjunctDialogBoxViewedEvent(providedReferrer,
            NhAnalyticsEventSection.NEWS,adjunctLang)
    }

    private fun initViewModel() {
        vmFactory = AdjunctLanguageViewModel.AdjunctLanguageViewModelF()
        vm = ViewModelProviders.of(this,vmFactory).get(AdjunctLanguageViewModel::class.java)
        vm.adjunctResponseLiveData.observe(this, {
            val data = it.getOrNull()
            if(it.isSuccess) {
                data?.let {
                    updateUI(it)
                }
            }
        })
        vm.getAdjunctLanguageInfo()
    }

    private fun updateUI(adjunctLangResponse: AdjunctLangResponse) {
        val adjMsgText = adjunctLangResponse.notificationTextMap?.get(adjunctLang+Constants.COMMA_CHARACTER+adjunctLang)
        val defaultMsgText = adjunctLangResponse.notificationTextMap?.get(adjunctLang+Constants.COMMA_CHARACTER+defaultLang)
        val imageUrl = adjunctLangResponse.langPopupImages?.get(adjunctLang)
        imageUrl?.let {
            Image.load(it)
                .into(binding.secondLayout.langImage)
        }
        if (adjMsgText != null) {
            if (defaultMsgText != null) {
                if(adjunctLang == defaultLang) {
                    updateTextView(adjMsgText, defaultMsgText,true)
                } else {
                    updateTextView(adjMsgText, defaultMsgText,false)
                }
            }
        }
    }

    private fun extractArguments(bundle:Bundle?) {
        bundle?.let {
            layoutType = it.getInt(NewsConstants.ADJUNCT_POPUP_DISPLAY_TYPE)
            adjunctLang = it.getString(NewsConstants.ADJUNCT_LANGUAGE) ?: Constants.DEFAULT_LANGUAGE
            providedReferrer = it.getSerializable(Constants.BUNDLE_ACTIVITY_REFERRER) as? PageReferrer
            defaultLang = UserPreferenceUtil.getUserPrimaryLanguage()
        }
    }

    private fun initView() {

        if(AdjunctLanguageUtils.isUserOrSystemOrBlackListedLanguage(adjunctLang)) {
            finish()
            return
        }
        if(layoutType == Constants.SHOW_ADJUNCT_LANG_DISPLAY_TYPE_1) {
            binding.firstLayout.root.visibility = View.VISIBLE
            binding.secondLayout.root.visibility = View.GONE
            binding.firstLayout.tickBtn.setOnClickListener{
                onPositiveClick()
            }
            binding.firstLayout.cancelBtn.setOnClickListener{
                onNegativeClick()
            }
        } else {
            binding.firstLayout.root.visibility = View.GONE
            binding.secondLayout.root.visibility = View.VISIBLE
            binding.secondLayout.tickBtn.setOnClickListener{
                onPositiveClick()
            }
            binding.secondLayout.cancelBtn.setOnClickListener{
                onNegativeClick()
            }
        }
    }

    private fun updateTextView(primaryText:String, secondaryText:String,hideDefaultText:Boolean) {
        if(layoutType == Constants.SHOW_ADJUNCT_LANG_DISPLAY_TYPE_1) {
            binding.firstLayout.adjunctLangText.text = primaryText
            if(hideDefaultText) {
                binding.firstLayout.defaultLangText.visibility = View.GONE
            } else {
                binding.firstLayout.defaultLangText.text = secondaryText
            }
        } else {
            binding.secondLayout.adjunctLangText.text = primaryText
            if(hideDefaultText) {
                binding.secondLayout.defaultLangText.visibility = View.GONE
            } else {
                binding.secondLayout.defaultLangText.text = secondaryText
            }
        }
    }
    private fun onPositiveClick() {
        PreferenceManager.savePreference(AdjunctLangPreference.PENDING_USER_WRITE_FLAG, true)
        AdjunctLanguageUtils.addUserSelectedLanguage(adjunctLang,false,null)
        AdjunctLanguageUtils.setUserActedOnAdjunctLang(true)
        DialogAnalyticsHelper.logAdjunctDialogBoxActionEvent(providedReferrer,
            NhAnalyticsEventSection.NEWS,adjunctLang,Constants.YES)
        finish()
    }
    private fun onNegativeClick() {
        PreferenceManager.savePreference(AdjunctLangPreference.PENDING_USER_WRITE_FLAG, true)
        AdjunctLanguageUtils.blackListAdjLanguage(adjunctLang)
        DialogAnalyticsHelper.logAdjunctDialogBoxActionEvent(providedReferrer,
            NhAnalyticsEventSection.NEWS,adjunctLang,Constants.NO)
        finish()
    }
}