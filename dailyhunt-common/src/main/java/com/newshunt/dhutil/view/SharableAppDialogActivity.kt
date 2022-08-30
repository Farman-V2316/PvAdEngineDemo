/*
 * Copyright (c) 2022 NewsHunt. All rights reserved.
 */
package com.newshunt.dhutil.view

import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.newshunt.common.helper.common.AndroidUtils
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.common.view.customview.NHBaseActivity
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dhutil.R
import com.newshunt.dhutil.analytics.DialogAnalyticsHelper
import com.newshunt.dhutil.databinding.DefaultShareAppDialogBinding
import com.newshunt.dhutil.helper.AppSettingsProvider
import com.newshunt.dhutil.helper.preference.AppStatePreference
import com.newshunt.onboarding.model.entity.datacollection.InstalledAppInfo

/**
 * Created by kajal.kumari on 31/05/22.
 */

class SharableAppDialogActivity: NHBaseActivity(), View.OnClickListener {

    private lateinit var viewbinding: DefaultShareAppDialogBinding
    private var title: String = Constants.EMPTY_STRING
    private var pkgName: String = Constants.EMPTY_STRING
    private var referrer: PageReferrer ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewbinding = DataBindingUtil.setContentView(this, R.layout.default_share_app_dialog)
        pkgName = intent?.getStringExtra(Constants.SHARABLE_APP_PKG_NAME) ?: Constants.EMPTY_STRING
        title = intent?.getStringExtra(Constants.SHARABLE_APP_DIALOG_TITLE) ?: Constants.EMPTY_STRING
        referrer = intent?.getSerializableExtra(Constants.BUNDLE_ACTIVITY_REFERRER) as? PageReferrer
        viewbinding.sharableAppText.text = title
        viewbinding.tickBtn.setOnClickListener(this)
        viewbinding.cancelBtn.setOnClickListener(this)
        viewbinding.crossIcon.setOnClickListener(this)
        viewbinding.root.setOnClickListener(this)
        DialogAnalyticsHelper.logLinkedInDialogBoxViewedEvent(referrer)
    }

    override fun onClick(v: View?) {
        v ?: return
        when (v.id) {
            R.id.tick_btn -> {
                DialogAnalyticsHelper.logLinkedInDialogBoxActionEvent(referrer,Constants.YES)
                PreferenceManager.savePreference(AppStatePreference.SELECTED_APP_TO_SHARE,pkgName)
                if(!pkgName.isNullOrEmpty() && AndroidUtils.isAppInstalled(pkgName)) {
                    try {
                        val packageManager = CommonUtils.getApplication().packageManager
                        val label: String = packageManager.getApplicationLabel(packageManager.getApplicationInfo(pkgName, 0)).toString()
                        val icon: Drawable = CommonUtils.getApplication().packageManager.getApplicationIcon(pkgName)
                        val bitmap = CommonUtils.getBitmapFromDrawable(icon)
                        val iconDrawable: Drawable = BitmapDrawable(CommonUtils.getApplication().resources,
                            bitmap.let {
                                Bitmap.createScaledBitmap(it, CommonUtils.getDimension(com.newshunt.common.util.R.dimen.share_drawable_size), CommonUtils.getDimension(
                                    com.newshunt.common.util.R.dimen.share_drawable_size), true)
                            })
                        AppSettingsProvider.preferredSharableAppLiveData.postValue(InstalledAppInfo(pkgName, iconDrawable, label))
                        Toast.makeText(this,CommonUtils.getString(com.newshunt.common.util.R.string.default_share_app_toast,label), Toast.LENGTH_SHORT).show()
                    } catch (e: PackageManager.NameNotFoundException) {
                        Logger.caughtException(e)
                    }
                }
                finish()
            }
            R.id.cancel_btn -> {
                DialogAnalyticsHelper.logLinkedInDialogBoxActionEvent(referrer,Constants.NO)
                var neverShowPromptForPkgs = PreferenceManager.getPreference(AppStatePreference.NEVER_SHOW_DEFAULT_SHARE_APP_PROMPT_PKG,Constants.EMPTY_STRING)
                PreferenceManager.savePreference(AppStatePreference.NEVER_SHOW_DEFAULT_SHARE_APP_PROMPT_PKG, "$pkgName,$neverShowPromptForPkgs")
                finish()
            }

            R.id.cross_icon -> {
                DialogAnalyticsHelper.logLinkedInDialogBoxActionEvent(referrer,Constants.CROSS_DELETE)
                finish()
            }

            else -> {
                finish()
            }
        }
    }
}