/*
 * Copyright (c) 2020  Newshunt. All rights reserved.
 */

package com.newshunt.appview.common.ui.activity

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.newshunt.appview.R
import com.newshunt.appview.databinding.ActivityRuntimePermissionBinding
import com.newshunt.common.helper.common.AndroidUtils
import com.newshunt.common.helper.common.BusProvider
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.track.AsyncTrackHandler
import com.newshunt.common.view.customview.NHBaseActivity
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.common.model.entity.PermissionResult
import com.newshunt.dataentity.notification.PermissionUIType
import com.newshunt.deeplink.DeeplinkUtils
import com.newshunt.deeplink.navigator.CommonNavigator
import com.newshunt.deeplink.navigator.PermissionNavigator
import com.newshunt.dhutil.helper.common.DefaultRationaleProvider
import com.newshunt.dhutil.helper.theme.ThemeUtils
import com.newshunt.permissionhelper.PermissionAdapter
import com.newshunt.permissionhelper.PermissionHelper
import com.newshunt.permissionhelper.utilities.Permission
import com.newshunt.permissionhelper.utilities.PermissionUtils
import com.squareup.otto.Subscribe

/**
 * created by mukesh.yadav on 05/06/20
 */
const val PERM_REQ_CODE = 1231
private const val LOG_TAG = "AdsRuntimePermissionActivity"

class RuntimePermissionActivity : NHBaseActivity() {
    private lateinit var viewBinding: ActivityRuntimePermissionBinding
    private var permissionAdapter: PermissionAdapter? = null
    private var permissionHelper: PermissionHelper? = null
    private var requestedPermissions = hashMapOf<Permission, Boolean>()
    private var url: String? = null
    private var image: String? = null
    private var heading: String? = null
    private var subHeading: String? = null
    private var ctaAllowText: String? = null
    private var ctaDenyText: String? = null
    private var landingPage: PermissionUIType? = PermissionUIType.INTERIM
    private var pageReferrer: PageReferrer? = null
    private var successTrackers: ArrayList<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(ThemeUtils.preferredTheme.themeId)
        viewBinding = DataBindingUtil.setContentView(this, R.layout.activity_runtime_permission)
        fetchIntentData()
        /*hide all UI component if link{PermissionUIType.FINAL*/
        if (landingPage == PermissionUIType.FINAL) {
            viewBinding.group.visibility = View.GONE
            if (openAppSettingPage()) {
                PermissionUtils.openAppSettingActivity(this)
            } else {
                requestPermission()
            }
        } else {
            uiSetup()
        }
    }

    private fun fetchIntentData() {
        val permissions: ArrayList<com.newshunt.dataentity.notification.Permission>? = intent.getSerializableExtra(PermissionNavigator.PERMISSIONS) as? ArrayList<com.newshunt.dataentity.notification.Permission>
        permissions?.forEach {
            try {
                requestedPermissions[Permission.valueOf(it.name)] = it.required
            } catch (rte: RuntimeException) {
                Logger.e(LOG_TAG, rte.message)
                finish()
            }
            if (Permission.valueOf(it.name) == Permission.RECORD_AUDIO
                    && !packageManager.hasSystemFeature(PackageManager.FEATURE_MICROPHONE)) {
                finish()
                return
            }
        }
        if (CommonUtils.isEmpty(requestedPermissions)) {
            finish()
        }
        successTrackers = intent.getStringArrayListExtra(PermissionNavigator.SUCCESS_TRACKERS)
        url = intent.getStringExtra(PermissionNavigator.URL)
        image = intent.getStringExtra(PermissionNavigator.IMAGE)
        heading = intent.getStringExtra(PermissionNavigator.Heading)
        subHeading = intent.getStringExtra(PermissionNavigator.SUB_HEADING)
        ctaAllowText = intent.getStringExtra(PermissionNavigator.CTA_ALLOW)
        ctaDenyText = intent.getStringExtra(PermissionNavigator.CTA_DENY)
        val landingPageString = intent.getStringExtra(PermissionNavigator.LANDING_PAGE)
        landingPageString?.let {
            try {
                landingPage = PermissionUIType.valueOf(it)
            } catch (rte: RuntimeException) {
                Logger.e(LOG_TAG, rte.message)
            }
        }
    }

    override fun onRestart() {
        super.onRestart()
        if (openAppSettingPage()) {
            updateCTAButtonText(ctaDenyText)
            if (landingPage == PermissionUIType.FINAL) {
                finish()
            }
        } else {
            updateCTAButtonText(ctaAllowText)
            notifyPermissionAllowed(false)
        }
    }

    /**
     * Handling the permission button click event
     */
    private fun uiSetup() {
        viewBinding.heading.text = heading
        viewBinding.subHeading.text = subHeading
        if (openAppSettingPage()) {
            viewBinding.ctaButton.text = ctaDenyText
        } else {
            viewBinding.ctaButton.text = ctaAllowText
        }
        image?.let {
            Glide.with(this).load(it).into(viewBinding.imgPermission)
        }
        viewBinding.ctaButton.setOnClickListener {
            if (openAppSettingPage()) {
                PermissionUtils.openAppSettingActivity(this)
            } else {
                requestPermission()
            }
        }
        viewBinding.skipRuntimePermission.setOnClickListener {
            finish()
        }
    }

    private fun requestPermission() {
        if (permissionAdapter == null) {
            permissionAdapter = makePermissionAdapter(ArrayList(requestedPermissions.keys))
        }
        BusProvider.getUIBusInstance().register(this)
        permissionAdapter?.let {
            if (permissionHelper == null) {
                permissionHelper = PermissionHelper(it)
            }
            permissionHelper?.requestPermissions()
        }
    }

    private fun openAppSettingPage(): Boolean {
        /*If permission is blocked return true,  and remove already allowed permissions*/
        var openAppSettingpage = false
        val iter = requestedPermissions.iterator()
        while (iter.hasNext()) {
            val entry = iter.next()
            if (checkPermissionGranted(entry.key)) {
                iter.remove()
                continue
            } else if (PermissionUtils.isPermissionBlocked(this, entry.key) && entry.value) {
                openAppSettingpage = true

            }
        }
        return openAppSettingpage
    }

    private fun updateCTAButtonText(text: String?) {
        viewBinding.ctaButton.text = text
    }

    private fun makePermissionAdapter(requestPermission: MutableList<Permission>?): PermissionAdapter? {
        return object : PermissionAdapter(PERM_REQ_CODE, this, DefaultRationaleProvider()) {
            override fun getPermissions(): MutableList<Permission> {
                val mutableListOfPermission = mutableListOf<Permission>()
                requestPermission?.let { mutableListOfPermission.addAll(requestPermission) }
                return mutableListOfPermission
            }

            override fun onPermissionResult(grantedPermissions: MutableList<Permission>, deniedPermissions: MutableList<Permission>, blockedPermissions: MutableList<Permission>) {
                if (deniedPermissions.isNotEmpty() || blockedPermissions.isNotEmpty()) {
                    deniedPermissions.addAll(blockedPermissions)
                    deniedPermissions.forEach {
                        if (requestedPermissions[it] == true) {
                            finish()
                            return
                        }
                    }
                }
                notifyPermissionAllowed(true)
            }

            override fun showAppSettingsSnackbar(message: String?, action: String?) {
                //Don't show snackbar.
            }
        }
    }

    @Subscribe
    fun onPermissionResult(result: PermissionResult) {
        permissionHelper?.handlePermissionCallback(this, result.permissions)
        BusProvider.getUIBusInstance().unregister(this)
    }

    private fun notifyPermissionAllowed(isOptionalPermissionQueried: Boolean) {
        var isAllPermisisonGranted = true
        requestedPermissions.keys.forEach {
            if (!checkPermissionGranted(it)) {
                isAllPermisisonGranted = false
            }
        }
        if (isOptionalPermissionQueried || isAllPermisisonGranted) {
            if (DeeplinkUtils.isDHDeeplink(url)) {
                CommonNavigator.launchDeeplink(this, url, pageReferrer)
            } else if (DeeplinkUtils.isValidHost(url)) {
                AndroidUtils.launchExternalLink(this, url)
            }
            successTrackers?.let {
                AsyncTrackHandler.getInstance().fireSuccessTrackers(successTrackers)
            }
            finish()
        }
    }

    private fun checkPermissionGranted(permission: Permission): Boolean {
        return checkSelfPermission(permission.permission) == PackageManager.PERMISSION_GRANTED
    }

    override fun onDestroy() {
        try {
            BusProvider.getUIBusInstance().unregister(this)
        } catch (ex: Exception) {
            Logger.caughtException(ex)
        }
        super.onDestroy()
    }

}