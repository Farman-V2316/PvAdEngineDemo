/*
 * Copyright (c) 2020 Newshunt. All rights reserved.
 */
package com.newshunt.appview.common.ui.fragment

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.snackbar.Snackbar
import com.newshunt.appview.R
import com.newshunt.appview.databinding.ImportContactsBinding
import com.newshunt.common.helper.common.BusProvider
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.view.customview.GenericCustomSnackBar
import com.newshunt.common.view.view.BaseSupportFragment
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.common.model.entity.ImportContactsEvents
import com.newshunt.dataentity.common.model.entity.PermissionResult
import com.newshunt.dhutil.analytics.AnalyticsHelper2
import com.newshunt.dhutil.helper.common.DefaultRationaleProvider
import com.newshunt.permissionhelper.PermissionAdapter
import com.newshunt.permissionhelper.PermissionHelper
import com.newshunt.permissionhelper.utilities.Permission
import com.newshunt.permissionhelper.utilities.PermissionUtils
import com.newshunt.profile.FragmentCommunicationEvent
import com.newshunt.profile.FragmentCommunicationsViewModel
import com.squareup.otto.Subscribe

/**
 * A fragment to show the import contacts illustration and request contact permission
 * <p>
 * Created by srikanth.ramaswamy on 03/27/2020.
 */

private const val CONTACT_PERM_REQ_CODE = 12424
private const val LOG_TAG = "ImportContactsFragment"
class ImportContactsFragment: BaseSupportFragment(), View.OnClickListener {
    private lateinit var viewBinding: ImportContactsBinding
    private var hostId: Int = uniqueScreenId
    private var pageReferrer: PageReferrer? = null
    private var permissionAdapter: PermissionAdapter? = null
    private var permissionHelper: PermissionHelper? = null
    private var isPermissionGranted = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewBinding = DataBindingUtil.inflate(inflater, R.layout.import_contacts, container, false)
        arguments?.let {
            hostId = it.getInt(Constants.BUNDLE_UI_COMPONENT_ID, uniqueScreenId)
            pageReferrer = it.getSerializable(Constants.REFERRER) as? PageReferrer?
        }

        viewBinding.skipContacts.setOnClickListener(this)
        viewBinding.importContactsAllow.setOnClickListener(this)
        //If permission is blocked, show SETTINGS instead of Allow
        if (activity != null && PermissionUtils.isPermissionBlocked(activity, Permission.READ_CONTACTS.permission)) {
            viewBinding.importContactsAllow.text =  CommonUtils.getString(R.string.action_settings)
        }

        AnalyticsHelper2.logImportContactsShown(pageReferrer)
        return viewBinding.root
    }

    override fun onClick(view: View?) {
        view ?: return
        when(view.id) {
            R.id.skipContacts -> {
                getFragmentCommunicationViewModel()?.let {
                    val event = FragmentCommunicationEvent(hostId, ImportContactsEvents.IMPORT_CONTACT_SKIP)
                    it.fragmentCommunicationLiveData.value = event
                }
            }
            R.id.import_contacts_allow -> {
                activity?.let {
                    AnalyticsHelper2.logImportContactsAllowClick(pageReferrer)
                        //If permission is blocked, take the user to settings screen
                        if(PermissionUtils.isPermissionBlocked(it, Permission.READ_CONTACTS.permission)) {
                            PermissionUtils.openAppSettingActivity(it)
                        } else {
                            //else request for contacts permission
                            requestContactsPermission()
                        }
                }
            }
        }
    }

    override fun onDestroy() {
        try {
            BusProvider.getUIBusInstance().unregister(this)
        } catch (ex: Exception) {
            Logger.caughtException(ex)
        }
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        activity?.let {
            notifyContactPermissionAllowed()
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (permissionAdapter == null) {
            permissionAdapter = makePermissionAdapter()
        }
        isPermissionGranted = checkContactsPermissionGranted()
    }

    @Subscribe
    fun onPermissionResult(result: PermissionResult) {
        activity?.let {
            permissionHelper?.handlePermissionCallback(it, result.permissions)
            BusProvider.getUIBusInstance().unregister(this)
        }
    }

    private fun getFragmentCommunicationViewModel(): FragmentCommunicationsViewModel? {
        return activity?.run {
            ViewModelProviders.of(this).get(FragmentCommunicationsViewModel::class.java)
        }
    }

    private fun notifyContactPermissionAllowed() {
        if (!isPermissionGranted) {
            isPermissionGranted = checkContactsPermissionGranted()
            if (isPermissionGranted) {
                getFragmentCommunicationViewModel()?.let {
                    val event = FragmentCommunicationEvent(hostId, ImportContactsEvents.CONTACT_PERMISSION_ALLOWED)
                    it.fragmentCommunicationLiveData.value = event
                }
            }
        }
    }

    private fun requestContactsPermission() {
        BusProvider.getUIBusInstance().register(this)
        permissionAdapter?.let {
            if (permissionHelper == null) {
                permissionHelper = PermissionHelper(it)
            }
            permissionHelper?.requestPermissions()
        }
    }

    private fun makePermissionAdapter(): PermissionAdapter? {
        return activity?.let {
            return object : PermissionAdapter(CONTACT_PERM_REQ_CODE, activity, DefaultRationaleProvider()) {
                override fun getPermissions(): MutableList<Permission> {
                    return mutableListOf(Permission.READ_CONTACTS)
                }

                override fun onPermissionResult(grantedPermissions: MutableList<Permission>, deniedPermissions: MutableList<Permission>, blockedPermissions: MutableList<Permission>) {
                    if (deniedPermissions.isNotEmpty() || blockedPermissions.isNotEmpty()) {
                        Logger.d(LOG_TAG, "Contacts Permission was denied")

                        if (blockedPermissions.isNotEmpty()) {
                            viewBinding.importContactsAllow.text = CommonUtils.getString(R.string.action_settings)
                        }
                        activity?.let {
                            GenericCustomSnackBar.showSnackBar(viewBinding.root, it,
                                    CommonUtils.getString(R.string.contact_permission_denied_msg),
                                    Snackbar.LENGTH_LONG).show()
                        }
                        return
                    }

                    notifyContactPermissionAllowed()
                }

                override fun showAppSettingsSnackbar(message: String?, action: String?) {
                    //Don't show snackbar.
                }
            }
        }
    }

    private fun checkContactsPermissionGranted(): Boolean {
        return activity?.let {
            it.checkSelfPermission(Permission.READ_CONTACTS.permission) == PackageManager.PERMISSION_GRANTED
        } ?: false
    }

    companion object {
        @JvmStatic
        fun newInstance(hostId: Int,
                        referrer: PageReferrer? = null): ImportContactsFragment {
            return ImportContactsFragment().apply {
                val bundle = Bundle().apply {
                    putInt(Constants.BUNDLE_UI_COMPONENT_ID, hostId)
                    putSerializable(Constants.REFERRER, referrer)
                }
                arguments = bundle
            }
        }
    }
}