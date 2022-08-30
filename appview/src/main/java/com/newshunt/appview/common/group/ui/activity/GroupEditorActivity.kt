/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.appview.common.group.ui.activity

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.snackbar.Snackbar
import com.newshunt.appview.R
import com.newshunt.appview.common.group.DaggerEditGroupComponent
import com.newshunt.appview.common.group.GroupBaseModule
import com.newshunt.appview.common.group.ImageUploadModule
import com.newshunt.appview.common.group.SocialHandleModule
import com.newshunt.appview.common.group.getImagePickerIntent
import com.newshunt.appview.common.group.viewmodel.EditGroupVMFactory
import com.newshunt.appview.common.group.viewmodel.EditGroupViewModel
import com.newshunt.appview.common.ui.activity.AuthorizationBaseActivity
import com.newshunt.appview.common.ui.helper.ErrorHelperUtils
import com.newshunt.appview.common.ui.helper.LiveDataEventHelper
import com.newshunt.appview.common.ui.helper.NewGroupEvent
import com.newshunt.appview.databinding.ActivityGroupEditorBinding
import com.newshunt.common.helper.common.BusProvider
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.font.FontHelper
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.common.view.customview.CommonMessageDialog
import com.newshunt.common.view.customview.CommonMessageDialogOptions
import com.newshunt.common.view.customview.CommonMessageEvents
import com.newshunt.common.view.customview.GenericCustomSnackBar
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.common.model.entity.PermissionResult
import com.newshunt.dataentity.common.view.customview.FIT_TYPE
import com.newshunt.dataentity.model.entity.DEFAULT_DESCRIPTION_CHAR_LIMIT
import com.newshunt.dataentity.model.entity.DEFAULT_PROFILE_NAME_CHAR_LIMIT
import com.newshunt.dataentity.model.entity.EditMode
import com.newshunt.dataentity.model.entity.GROUP_INFO_KEY
import com.newshunt.dataentity.model.entity.GroupInfo
import com.newshunt.dataentity.model.entity.INVALID_SIZE
import com.newshunt.dataentity.model.entity.NOT_FOUND
import com.newshunt.dataentity.model.entity.SUCCESS
import com.newshunt.dataentity.model.entity.UIResponseWrapper
import com.newshunt.dhutil.analytics.AnalyticsHelper2
import com.newshunt.dhutil.analytics.NhAnalyticsGroupEvent
import com.newshunt.dhutil.disableCopyPaste
import com.newshunt.dhutil.enableTextViewScroll
import com.newshunt.dhutil.helper.common.DefaultRationaleProvider
import com.newshunt.dhutil.helper.preference.AppStatePreference
import com.newshunt.dhutil.helper.theme.ThemeUtils
import com.newshunt.dhutil.placeCursorAtEnd
import com.newshunt.helper.ImageUrlReplacer
import com.newshunt.news.helper.NewsListCardLayoutUtil
import com.newshunt.news.model.sqlite.SocialDB
import com.newshunt.permissionhelper.PermissionAdapter
import com.newshunt.permissionhelper.PermissionHelper
import com.newshunt.permissionhelper.utilities.Permission
import com.newshunt.profile.FragmentCommunicationsViewModel
import com.newshunt.profile.OptionsBottomSheetFragment
import com.newshunt.profile.SimpleOptionItem
import com.newshunt.profile.SimpleOptions
import com.newshunt.sdk.network.image.Image
import com.squareup.otto.Subscribe
import javax.inject.Inject

private const val MAX_HANDLE_CHARS = 20
private const val GALLERY_REQUEST_CODE: Int = 1223
private const val STORAGE_PERM_REQ_CODE: Int = 1224
private const val TAG = "GroupEditorActivity"

/**
 * Creates/Edit a Group
 * TODO - Validations (length limits, handle availability)
 *      - error messages
 *      - progress bars
 *
 * @author raunak.yadav
 */
class GroupEditorActivity : AuthorizationBaseActivity() {

    @Inject
    lateinit var editGroupModelF: EditGroupVMFactory
    private lateinit var viewModel: EditGroupViewModel
    private lateinit var viewBinding: ActivityGroupEditorBinding

    private var editMode: EditMode = EditMode.CREATE
    private var groupInfo: GroupInfo? = null
    private var copyGroupInfo: GroupInfo? = null
    private var chosenImagePath: String? = null
    private var permissionHelper: PermissionHelper? = null
    private var isChanged: Boolean = false
    private lateinit var mProgressDialog: ProgressDialog
    private val maximum_character: Int = PreferenceManager.getPreference(AppStatePreference.PROFILE_NAME_CHAR_LIMT, DEFAULT_PROFILE_NAME_CHAR_LIMIT) ?: DEFAULT_PROFILE_NAME_CHAR_LIMIT
    private val maximum_desc_char: Int = PreferenceManager.getPreference(AppStatePreference.PROFILE_DESC_CHAR_LIMIT, DEFAULT_DESCRIPTION_CHAR_LIMIT) ?: DEFAULT_DESCRIPTION_CHAR_LIMIT

    private val errorColor = CommonUtils.getColor(R.color.create_group_error_color)
    private val handleColor = CommonUtils.getColor(R.color.social_handle_text_color)
    private var referrerRaw:String? = null

    private val permissionAdapter = object : PermissionAdapter(STORAGE_PERM_REQ_CODE, this,
            DefaultRationaleProvider()) {
        override fun getPermissions(): MutableList<Permission> {
            return mutableListOf(Permission.READ_EXTERNAL_STORAGE)
        }

        override fun onPermissionResult(grantedPermissions: MutableList<Permission>, deniedPermissions: MutableList<Permission>, blockedPermissions: MutableList<Permission>) {
            if (deniedPermissions.isNotEmpty() || blockedPermissions.isNotEmpty()) {
                Logger.d(TAG, "Storage Permission was denied")
                return
            } else {
                openGalleryActivity()
            }
        }

        override fun shouldShowRationale(): Boolean {
            return true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(ThemeUtils.preferredTheme.themeId)
        viewBinding = DataBindingUtil.setContentView(this, R.layout.activity_group_editor)
        NewsListCardLayoutUtil.manageLayoutDirection(findViewById(R.id.create_group_rootview))

        DaggerEditGroupComponent.builder()
                .socialHandleModule(SocialHandleModule(debounceDelay = 300L))
                .imageUploadModule(ImageUploadModule())
                .groupBaseModule(GroupBaseModule(SocialDB.instance()))
                .build().inject(this)
        viewModel = ViewModelProviders.of(this, editGroupModelF).get(EditGroupViewModel::class.java)

        //Create or Edit
        groupInfo = intent.getSerializableExtra(GROUP_INFO_KEY) as? GroupInfo
        referrerRaw = intent.extras?.getString(Constants.REFERRER_RAW)
        pageReferrer = intent.extras?.get(Constants.BUNDLE_ACTIVITY_REFERRER) as? PageReferrer
        editMode = if (groupInfo == null) {
            groupInfo = GroupInfo()
            EditMode.CREATE
        } else {
            copyGroupInfo = GroupInfo()
            copyGroupInfo?.name = groupInfo?.name
            copyGroupInfo?.description = groupInfo?.description
            populateData(groupInfo!!)
            EditMode.UPDATE
        }

        if (editMode == EditMode.CREATE) {
            AnalyticsHelper2.logCreateGroupCardClick(pageReferrer,
                    NhAnalyticsGroupEvent.CREATE_GROUP_SHOWN, Constants.CREATE_GROUP_SHOWN)
        } else {
            AnalyticsHelper2.logCreateGroupCardClick(pageReferrer,
                    NhAnalyticsGroupEvent.CREATE_GROUP_SHOWN, Constants.EDIT_GROUP_SHOWN)
        }

        observeFragmentCommunications()
        setUpViews()
        observeViewData()
        observeMenuEvents()
    }

    override fun onStart() {
        super.onStart()
        if (!CommonUtils.isNetworkAvailable(this)) {
            FontHelper.showCustomSnackBar(viewBinding.root, CommonUtils.getString(R.string.error_no_connection),
                    Snackbar.LENGTH_SHORT, null, null)
        }
    }

    override fun onBackPressed() {
        if (isGroupDetailNotEmpty()) {
            showPostDiscardDialog()
            return
        }
        handleBack(true,referrerRaw)
    }

    private fun isGroupDetailNotEmpty(): Boolean {
        if (editMode == EditMode.CREATE) {
            return !CommonUtils.isEmpty(groupInfo?.name)
                    || !CommonUtils.isEmpty(groupInfo?.description)
                    || !CommonUtils.isEmpty(groupInfo?.handle)
                    || chosenImagePath != null
        } else {
            return !CommonUtils.equals(copyGroupInfo?.name, groupInfo?.name)
                    || !CommonUtils.equals(copyGroupInfo?.description, groupInfo?.description)
                    || isChanged
        }
    }

    private fun showPostDiscardDialog() {
        supportFragmentManager?.let {
            val commonMessageDialogOptions = CommonMessageDialogOptions(
                this.activityID,
                CommonUtils.getString(R.string.discard_changes),
                CommonUtils.getString(R.string.discard_changes_desc),
                CommonUtils.getString(R.string.discard),
                CommonUtils.getString(R.string.cancel_text)
            )

            CommonMessageDialog.newInstance(commonMessageDialogOptions).show(it, "CommonMessageDialog")
        }
    }

    private fun observeFragmentCommunications() {
        ViewModelProviders.of(this).get(FragmentCommunicationsViewModel::class.java).fragmentCommunicationLiveData.observe(this, Observer {
            if (it.hostId != activityID) {
                return@Observer
            }

            when (it.anyEnum) {
                is CommonMessageEvents -> {
                    if (it.anyEnum == CommonMessageEvents.POSITIVE_CLICK) {
                        finish()
                    }
                }
            }
        })
    }

    private fun setUpViews() {
        viewBinding.editMode = editMode
        viewBinding.actionbar.toolbarBack.setOnClickListener {
            handleBack(false)
        }

        viewBinding.saveGroupBtn.setOnClickListener {
            saveGroup()
        }
        mProgressDialog = ProgressDialog(this)
        viewBinding.groupNameInput.filters = (arrayOf<InputFilter>(InputFilter.LengthFilter(maximum_character)))
        viewBinding.groupIdInput.filters = (arrayOf<InputFilter>(InputFilter.LengthFilter(MAX_HANDLE_CHARS)))
        viewBinding.descriptionInput.filters = (arrayOf<InputFilter>(InputFilter.LengthFilter(maximum_desc_char)))
        viewBinding.descriptionInput.enableTextViewScroll(viewBinding.groupEditorScroll)
        viewBinding.editPictureText.setOnClickListener {
            if (groupInfo?.coverImage.isNullOrEmpty() && chosenImagePath == null) {
                pickImage()
            } else {
                showImageEditOptions()
            }
            AnalyticsHelper2.logEditPhotoClickEvent(pageReferrer)
        }

        viewBinding.groupNameInput.disableCopyPaste()
        viewBinding.groupNameInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(name: Editable?) {
                name ?: return
                if (name.length == maximum_character) {
                    val toastText = CommonUtils.getString(R.string
                            .edit_profile_max_charater, maximum_character)
                    FontHelper.showCustomFontToast(this@GroupEditorActivity, toastText, Toast
                            .LENGTH_SHORT)
                }
                groupInfo?.name = name.toString()
                viewModel.checkGroupInfoValid(groupInfo)
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
        })

        if (editMode == EditMode.CREATE) {
            viewBinding.groupIdInput.disableCopyPaste()
            viewBinding.groupIdInput.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(handle: Editable?) {
                    handle ?: return
                    if (handle.length >= MAX_HANDLE_CHARS) {
                        FontHelper.showCustomFontToast(this@GroupEditorActivity,
                                CommonUtils.getString(R.string.edit_profile_max_charater_handler),
                                Toast.LENGTH_SHORT)
                    }
                    groupInfo?.handle = handle.toString()
                    viewModel.checkGroupInfoValid(groupInfo)
                    viewModel.validateHandle(handle.toString())?.let {
                        showHandleStatus(it)
                    }
                }

                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }
            })
        }

        viewBinding.descriptionInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(desc: Editable?) {
                desc ?: return
                groupInfo?.description = desc.toString()
                if (desc.length >= maximum_desc_char) {
                    FontHelper.showCustomFontToast(this@GroupEditorActivity,
                            CommonUtils.getString(R.string.edit_profile_max_charater, maximum_desc_char),
                            Toast.LENGTH_SHORT)
                    viewBinding.descriptionInput.setText(desc.substring(0, maximum_desc_char))
                    viewBinding.descriptionInput.placeCursorAtEnd()
                    return
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
        })
    }

    private fun populateData(info: GroupInfo) {
        viewModel.setApprovedHandle(info.handle)
        viewBinding.item = info
        Image.load(ImageUrlReplacer.getQualifiedImageUrl(info.coverImage,
                CommonUtils.getDeviceScreenWidthInDp(), Constants.IMAGE_ASPECT_RATIO_16_9))
                .placeHolder(R.drawable.default_image)
                .into(viewBinding.groupImage)
        viewBinding.groupImage.setFitType(FIT_TYPE.TOP_CROP)
        viewModel.checkGroupInfoValid(groupInfo)
        viewBinding.executePendingBindings()
    }

    private fun observeViewData() {
        viewModel.groupInfoValidStatus.observe(this, Observer {
            viewBinding.saveGroupBtn.isEnabled = it
        })
        viewModel.handleAvailabilityLiveData().observe(this, Observer { handleMap ->
            handleMap[viewBinding.groupIdInput.text?.toString()]?.let {
                if (CommonUtils.isEmpty(groupInfo?.handle)) {
                    resetHandleValidationUI()
                    return@Observer
                }
                showHandleStatus(it)
            }
        })

        viewModel.imageValidStatus.observe(this, Observer {
            when (it.code) {
                SUCCESS -> {
                    chosenImagePath = it.response
                    Image.load(chosenImagePath)
                            .placeHolder(R.drawable.ic_group_image)
                            .into(viewBinding.groupImage)
                    viewBinding.groupImage.setFitType(FIT_TYPE.TOP_CROP)
                }
                INVALID_SIZE -> {
                    supportFragmentManager?.let {
                        val commonMessageDialogOptions = CommonMessageDialogOptions(
                            this.activityID,
                            Constants.EMPTY_STRING,
                            CommonUtils.getString(R.string.image_size_error_text),
                            CommonUtils.getString(R.string.ok_text),
                            Constants.EMPTY_STRING,
                            CommonUtils.getDrawable(R.drawable.ic_info_icon)
                        )
                        CommonMessageDialog.newInstance(commonMessageDialogOptions).show(it, "CommonMessageDialog")
                    }
                }
                NOT_FOUND -> {
                    GenericCustomSnackBar.showSnackBar(viewBinding.root, this, CommonUtils.getString(R.string.image_path_invalid), Snackbar.LENGTH_LONG).show()
                }
            }
        })

        viewModel.editGroupLiveData.observe(this, Observer {
            if(::mProgressDialog.isInitialized)  mProgressDialog.dismiss()

            if (it.isSuccess) {
                it.getOrNull()?.response?.let { groupInfo ->
                    Logger.d(TAG, "Group saved ${groupInfo.id}")
                    if (editMode == EditMode.CREATE) {
                        val intent = Intent(this, GroupDetailActivity::class.java)
                        intent.putExtra(GROUP_INFO_KEY, groupInfo)
                        startActivity(intent)
                    }

                    if (editMode == EditMode.UPDATE) {
                        FontHelper.showCustomFontToast(this@GroupEditorActivity, CommonUtils.getString(R
                                .string.group_detail_saved), Toast.LENGTH_SHORT)
                    } else {
                        AnalyticsHelper2.logCreateGroupEvent(pageReferrer)
                    }
                    LiveDataEventHelper.newGroupLiveData.postValue(NewGroupEvent(System.currentTimeMillis(), groupInfo.id))
                    finish()
                    return@Observer
                }
                it.getOrNull()?.message?.let {
                    GenericCustomSnackBar.showSnackBar(viewBinding.root, this, it, Snackbar.LENGTH_LONG).show()
                }
                return@Observer
            } else {
                ErrorHelperUtils.showErrorSnackbar(it.exceptionOrNull(), viewBinding.root)
            }
        })
        viewModel.editGroupStatus.observe(this, Observer {
            viewBinding.inProgress = it
            viewBinding.saveGroupBtn.isEnabled = !it
        })
    }

    private fun removeImage() {
        chosenImagePath = null
        groupInfo?.coverImage = null
        viewBinding.groupImage.setImageResource(R.drawable.ic_group_image)
        isChanged = true
    }

    private fun showHandleStatus(response: UIResponseWrapper<Int>?) {
        response ?: return
        Logger.d(TAG, "Handle validation response $response")
        viewModel.checkGroupInfoValid(groupInfo)
        if (response.message.isNullOrBlank()) {
            viewBinding.errorGroup.visibility = View.GONE
            viewBinding.correctSymbol.visibility = View.VISIBLE
            viewBinding.handleSymbol.setTextColor(handleColor)
            viewBinding.groupIdInput.setTextColor(handleColor)
        } else {
            viewBinding.errorGroupId.text = response.message
            viewBinding.errorGroup.visibility = View.VISIBLE
            viewBinding.correctSymbol.visibility = View.GONE
            viewBinding.handleSymbol.setTextColor(errorColor)
            viewBinding.groupIdInput.setTextColor(errorColor)
            viewBinding.saveGroupBtn.isEnabled = false
        }
    }

    private fun resetHandleValidationUI() {
        viewBinding.errorGroup.visibility = View.GONE
        viewBinding.correctSymbol.visibility = View.GONE
    }

    @Subscribe
    fun onPermissionResult(result: PermissionResult) {
        permissionHelper?.handlePermissionCallback(this, result.permissions)
        BusProvider.getUIBusInstance().unregister(this)
    }

    //1. Upload image, if any change
    //2. Create/save group
    private fun saveGroup() {
        groupInfo?.let { info ->
            if(::mProgressDialog.isInitialized) {
                mProgressDialog.isIndeterminate = true
                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER)
                mProgressDialog.setCanceledOnTouchOutside(false)
                if(editMode == EditMode.CREATE) {
                    mProgressDialog.setMessage(CommonUtils.getString(R.string.creating_group))
                } else {
                    mProgressDialog.setMessage(CommonUtils.getString(R.string.saving_group))
                }
                mProgressDialog.show()
            }
            info.userId = myUserId.userId ?: Constants.EMPTY_STRING
            viewModel.saveGroup(chosenImagePath, info, editMode)
        }
    }

    private fun pickImage() {
        if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestStoragePermission()
            return
        } else {
            openGalleryActivity()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        intent?.data ?: return

        if (resultCode == Activity.RESULT_OK)
            when (requestCode) {
                GALLERY_REQUEST_CODE -> {
                    viewModel.validateImage(intent.data)
                    isChanged = true
                }
            }
    }

    private fun showImageEditOptions() {
        val menuList = ArrayList<SimpleOptionItem>()

        menuList.add(SimpleOptionItem(R.drawable.ic_image,
                CommonUtils.getString(R.string.change_picture), GroupMenuOptions.CHANGE_PICTURE))

        menuList.add(SimpleOptionItem(R.drawable.ic_delete,
                CommonUtils.getString(R.string.remove_picture), GroupMenuOptions.REMOVE_PICTURE))

        if (menuList.isNotEmpty()) {
            val menuOptions = SimpleOptions(menuList, activityID)
            supportFragmentManager?.let {
                OptionsBottomSheetFragment.newInstance(menuOptions).show(it, "GroupOptionsMenu")
            }
        }
    }

    private fun observeMenuEvents() {
        ViewModelProviders.of(this).get(FragmentCommunicationsViewModel::class.java).fragmentCommunicationLiveData.observe(this, Observer {
            if (it.hostId != activityID || it.anyEnum !is GroupMenuOptions) {
                return@Observer
            }
            when (it.anyEnum) {
                GroupMenuOptions.CHANGE_PICTURE -> {
                    pickImage()
                }
                GroupMenuOptions.REMOVE_PICTURE -> {
                    removeImage()
                }
            }
        })
    }

    private fun requestStoragePermission() {
        BusProvider.getUIBusInstance().register(this)
        permissionHelper = PermissionHelper(permissionAdapter).apply {
            requestPermissions()
        }
    }

    override fun getLogTag(): String = TAG

    override fun showLoginError() {
        //No deeplink to this screen. No need to handle login error
    }

    private fun openGalleryActivity(){
        getImagePickerIntent().let {imagePickerItent ->
            imagePickerItent.resolveActivity(packageManager)?.let {
                startActivityForResult(imagePickerItent, GALLERY_REQUEST_CODE)
            }
        }
    }

    private enum class GroupMenuOptions {
        REMOVE_PICTURE,
        CHANGE_PICTURE
    }
}
