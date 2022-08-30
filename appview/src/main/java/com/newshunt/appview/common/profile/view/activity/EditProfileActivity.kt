package com.newshunt.appview.common.profile.view.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.InputType
import android.text.Spanned
import android.text.TextWatcher
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.View.OnClickListener
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.ScrollView
import android.widget.Toast
import android.widget.ToggleButton
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.util.Pair
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.snackbar.Snackbar
import com.newshunt.analytics.client.AnalyticsClient
import com.newshunt.appview.R
import com.newshunt.appview.common.accounts.AccountsAnalyticsHelper
import com.newshunt.appview.common.group.ImageUploadModule
import com.newshunt.appview.common.group.SocialHandleModule
import com.newshunt.appview.common.group.getImagePickerIntent
import com.newshunt.appview.common.postcreation.view.activity.PostLocationActivity
import com.newshunt.appview.common.postcreation.view.helper.PostConstants
import com.newshunt.appview.common.profile.BaseProfileModule
import com.newshunt.appview.common.profile.DaggerEditProfileComponent
import com.newshunt.appview.common.profile.view.ProfileViewBindingUtils
import com.newshunt.appview.common.profile.viewmodel.EditProfileViewModel
import com.newshunt.appview.common.profile.viewmodel.EditProfileViewModelFactory
import com.newshunt.appview.databinding.EditProfileActivityBinding
import com.newshunt.common.helper.appconfig.AppConfig
import com.newshunt.common.helper.common.BusProvider
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.JsonUtils
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.font.FontHelper
import com.newshunt.common.helper.preference.AppUserPreferenceUtils
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.common.view.customview.CommonMessageDialog
import com.newshunt.common.view.customview.CommonMessageDialogOptions
import com.newshunt.common.view.customview.CommonMessageEvents
import com.newshunt.common.view.customview.GenericCustomSnackBar
import com.newshunt.common.view.customview.NHBaseActivity
import com.newshunt.common.view.customview.NHImageView
import com.newshunt.common.view.customview.fontview.NHEditText
import com.newshunt.common.view.customview.fontview.NHTextView
import com.newshunt.common.view.view.UniqueIdHelper
import com.newshunt.dataentity.analytics.entity.NhAnalyticsEventParam
import com.newshunt.dataentity.analytics.referrer.NhGenericReferrer
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.analytics.section.NhAnalyticsEventSection
import com.newshunt.dataentity.common.asset.PostCurrentPlace
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.common.model.entity.AppSection
import com.newshunt.dataentity.common.model.entity.BaseError
import com.newshunt.dataentity.common.model.entity.PermissionResult
import com.newshunt.dataentity.common.view.customview.FIT_TYPE
import com.newshunt.dataentity.model.entity.AccountPermission
import com.newshunt.dataentity.model.entity.AuthType
import com.newshunt.dataentity.model.entity.DEFAULT_DESCRIPTION_CHAR_LIMIT
import com.newshunt.dataentity.model.entity.DEFAULT_PROFILE_NAME_CHAR_LIMIT
import com.newshunt.dataentity.model.entity.INVALID_SIZE
import com.newshunt.dataentity.model.entity.LoginType
import com.newshunt.dataentity.model.entity.MyProfile
import com.newshunt.dataentity.model.entity.NOT_FOUND
import com.newshunt.dataentity.model.entity.SUCCESS
import com.newshunt.dataentity.model.entity.SocialPrivacy
import com.newshunt.dataentity.model.entity.UIResponseWrapper
import com.newshunt.dataentity.model.entity.UpdateProfileBody
import com.newshunt.dataentity.model.entity.UserBaseProfile
import com.newshunt.dataentity.news.analytics.NHProfileAnalyticsEvent
import com.newshunt.dataentity.news.analytics.NHProfileAnalyticsEventParam
import com.newshunt.dataentity.news.view.entity.Gender
import com.newshunt.dataentity.sso.model.entity.AccountLinkingResult
import com.newshunt.deeplink.navigator.CommonNavigator
import com.newshunt.deeplink.navigator.CommonNavigator.launchNewsHome
import com.newshunt.dhutil.disableCopyPaste
import com.newshunt.dhutil.enableTextViewScroll
import com.newshunt.dhutil.helper.appsection.AppSectionsProvider
import com.newshunt.dhutil.helper.common.DefaultRationaleProvider
import com.newshunt.dhutil.helper.preference.AppStatePreference
import com.newshunt.dhutil.helper.theme.ThemeUtils
import com.newshunt.dhutil.isInRange
import com.newshunt.dhutil.view.ErrorMessageBuilder
import com.newshunt.helper.ImageUrlReplacer
import com.newshunt.news.helper.NewsListCardLayoutUtil
import com.newshunt.permissionhelper.PermissionAdapter
import com.newshunt.permissionhelper.PermissionHelper
import com.newshunt.permissionhelper.utilities.Permission
import com.newshunt.profile.FragmentCommunicationsViewModel
import com.newshunt.sdk.network.image.Image
import com.squareup.otto.Subscribe
import kotlinx.android.synthetic.main.accounts_type_row.view.connectOrConnected
import kotlinx.android.synthetic.main.connect_accounts.view.fb
import kotlinx.android.synthetic.main.connect_accounts.view.google
import kotlinx.android.synthetic.main.connect_accounts.view.tc
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.math.abs

/**
 * EDIT Profile activity. Edits  User profile
 * <p>
 * Created by priya.gupta on 07/10/2019.
 */

private const val LOG_TAG = "ProfileEditActivity"
private const val GALLERY_REQUEST_CODE: Int = 1223
private const val STORAGE_PERM_REQ_CODE: Int = 1224
private const val LOCATION_PERM_REQ_CODE: Int = 1225
private const val LOCATION_REQUEST_CODE = 1227
private const val SAVE_PROFILE = "save"
private const val ACC_LINK_REQUEST_CODE = 1229

@SuppressLint("ClickableViewAccessibility")
class EditProfileActivity : NHBaseActivity(), OnClickListener, TextWatcher {

    private lateinit var date: NHEditText
    private lateinit var month: NHEditText
    private lateinit var year: NHEditText
    private lateinit var name: NHEditText
    private lateinit var handler: NHEditText
    private lateinit var bio: NHEditText
    private lateinit var location: NHTextView
    private lateinit var privacyToggle: ToggleButton
    private lateinit var handler_error: NHTextView
    private lateinit var profileImageView: NHImageView
    private lateinit var disclaimer: NHImageView
    private lateinit var saveButton: NHTextView
    private lateinit var disclaimerText: String
    private lateinit var saveDisclaimerText: String
    private lateinit var currentImage: String
    private lateinit var editProfileViewModel: EditProfileViewModel
    private lateinit var editProfileRootView: CoordinatorLayout
    private lateinit var popup: PopupWindow
    private lateinit var mProgressDialog: ProgressDialog
    private lateinit var taggingCB: ToggleButton
    private lateinit var editPictureText: NHTextView
    private lateinit var errorParent: LinearLayout
    private lateinit var viewBinding: EditProfileActivityBinding

    @Inject
    lateinit var editProfileViewModelF: EditProfileViewModelFactory

    private var errorMessageBuilder: ErrorMessageBuilder? = null

    private var currentHandler: String? = ""
    private var currentName: String? = ""
    private var genderType: Gender? = null
    private var currentBio: String? = null
    private var pageReferrer: PageReferrer? = null
    private val maximum_character: Int = PreferenceManager.getPreference(AppStatePreference.PROFILE_NAME_CHAR_LIMT, DEFAULT_PROFILE_NAME_CHAR_LIMIT)
            ?: DEFAULT_PROFILE_NAME_CHAR_LIMIT
    private val maximum_character_handler: Int = 20
    private val maximum_desc_char: Int = PreferenceManager.getPreference(AppStatePreference.PROFILE_DESC_CHAR_LIMIT, DEFAULT_DESCRIPTION_CHAR_LIMIT)
            ?: DEFAULT_DESCRIPTION_CHAR_LIMIT
    private val activityId = UniqueIdHelper.getInstance().generateUniqueId()

    private val calendar = Calendar.getInstance(TimeZone.getDefault())
    private var appLanguage = AppUserPreferenceUtils.getUserNavigationLanguage()

    private var privacy: SocialPrivacy = SocialPrivacy.PUBLIC


    private var myServerProfile: MyProfile? = null
    private var myChangedProfile: UserBaseProfile? = null

    private var taggingPermission: AccountPermission = AccountPermission.ALLOWED
    private var invitesPermission: AccountPermission = AccountPermission.ALLOWED
    private var chosenImagePath: String? = null
    private var storagePermissionHelper: PermissionHelper? = null
    private var locationPermissionHelper: PermissionHelper? = null
    private var currentLocation: PostCurrentPlace? = null


    private val storagePermissionAdapter = object : PermissionAdapter(STORAGE_PERM_REQ_CODE, this, DefaultRationaleProvider()) {
        override fun getPermissions(): MutableList<Permission> {
            return mutableListOf(Permission.READ_EXTERNAL_STORAGE)
        }

        override fun onPermissionResult(grantedPermissions: MutableList<Permission>, deniedPermissions: MutableList<Permission>, blockedPermissions: MutableList<Permission>) {
            if (deniedPermissions.isNotEmpty() || blockedPermissions.isNotEmpty()) {
                Logger.d(LOG_TAG, "Storage Permission was denied")
                return
            } else {
                startActivityForResult(getImagePickerIntent(), GALLERY_REQUEST_CODE)
            }
        }

        override fun shouldShowRationale(): Boolean {
            return true
        }
    }

    private val locationPermissionAdapter = object : PermissionAdapter(LOCATION_PERM_REQ_CODE, this, DefaultRationaleProvider()) {
        override fun getPermissions(): MutableList<Permission> {
            return mutableListOf(Permission.ACCESS_FINE_LOCATION)
        }

        override fun onPermissionResult(grantedPermissions: MutableList<Permission>, deniedPermissions: MutableList<Permission>, blockedPermissions: MutableList<Permission>) {
            if (deniedPermissions.isNotEmpty() || blockedPermissions.isNotEmpty()) {
                Logger.d(LOG_TAG, "Location Permission was denied")
                return
            }
            launchLocationActivity()
        }

        override fun shouldShowRationale(): Boolean {
            return true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val themeID = ThemeUtils.preferredTheme.themeId
        setTheme(themeID)
        super.onCreate(savedInstanceState)
        DaggerEditProfileComponent.builder()
                .baseProfileModule(BaseProfileModule())
                .socialHandleModule(SocialHandleModule())
                .imageUploadModule(ImageUploadModule())
                .build()
                .inject(this)
        viewBinding = DataBindingUtil.setContentView(this, R.layout.edit_profile_activity)
        editProfileRootView = findViewById(R.id.edit_profile_root_view)
        NewsListCardLayoutUtil.manageLayoutDirection(editProfileRootView)
        setUpActionBar(themeID)
        editProfileViewModel = ViewModelProviders.of(this, editProfileViewModelF).get(EditProfileViewModel::class.java)

        myChangedProfile = UserBaseProfile()

        val myProfile = intent.getSerializableExtra(Constants.BUNDLE_MY_PROFILE) as UserBaseProfile
        setupViews(myProfile)

        editProfileViewModel.fetchMyProfile(appLanguage)
        pageReferrer = intent.getSerializableExtra(Constants.BUNDLE_ACTIVITY_REFERRER) as? PageReferrer

        editProfileViewModel.myProfileLiveData.observe(this, androidx.lifecycle.Observer {
            if (it.isSuccess) {
                populateViews(it.getOrNull())
            } else {
                showError(it.exceptionOrNull())
                editProfileViewModel.checkProfileValid(null)
            }
        })


        editProfileViewModel.handleAvailabilityLiveData().observe(this, androidx.lifecycle.Observer { handleMap ->
            handler.text?.toString()?.let {
                handleMap[it]?.let { uiResponseWrapper ->
                    handleError(uiResponseWrapper)
                }
            }
        })

        editProfileViewModel.imageValidStatus.observe(this, Observer {
            when (it.code) {
                SUCCESS -> {
                    chosenImagePath = it.response
                    Image.load(chosenImagePath)
                            .placeHolder(R.drawable.default_user_avatar)
                            .into(viewBinding.profilePicture)
                }
                INVALID_SIZE -> {
                    supportFragmentManager?.let {
                        val commonMessageDialogOptions = CommonMessageDialogOptions(
                            this.activityId,
                            Constants.EMPTY_STRING,
                            CommonUtils.getString(R.string.image_size_error_text),
                            Constants.EMPTY_STRING,
                            CommonUtils.getString(R.string.ok_text),
                            CommonUtils.getDrawable(R.drawable.ic_info_icon)
                        )
                        CommonMessageDialog.newInstance(commonMessageDialogOptions).show(it, "CommonMessageDialog")
                    }
                }
                NOT_FOUND -> {
                    GenericCustomSnackBar.showSnackBar(editProfileRootView, this, CommonUtils.getString(R.string.image_path_invalid), Snackbar.LENGTH_LONG).show()
                }
            }
        })


        editProfileViewModel.updatedProfileLiveData.observe(this, Observer {
            sentResult(it)
        })

        editProfileViewModel.profileValidation.observe(this, Observer {
            enableSave(it)
        })

        observeFragmentCommunications()
    }

    private fun setupViews(myProfile: UserBaseProfile) {

        disclaimer = findViewById(R.id.disclaimer) as NHImageView
        val iv_calendar = findViewById(R.id.iv_calendar) as ImageView
        date = findViewById(R.id.date) as NHEditText
        month = findViewById(R.id.month) as NHEditText
        year = findViewById(R.id.year) as NHEditText
        name = findViewById(R.id.et_name) as NHEditText
        handler = findViewById(R.id.et_user_name) as NHEditText
        handler_error = findViewById(R.id.user_error) as NHTextView
        errorParent = findViewById(R.id.error_parent)
        saveButton = findViewById<View>(R.id.save_profile) as NHTextView
        profileImageView = findViewById(R.id.profile_picture)
        profileImageView.setFitType(FIT_TYPE.FIT_CENTER)
        taggingCB = findViewById(R.id.tagging_toggle)
        taggingCB.setOnClickListener(this)
        editPictureText = findViewById(R.id.edit_picture_text)
        editPictureText.setOnClickListener(this)
        location = findViewById(R.id.et_location)
        location.setOnClickListener(this)
        bio = findViewById(R.id.et_description)
        privacyToggle = findViewById(R.id.privacy_toggle)
        privacyToggle.setOnClickListener(this)
        popup = PopupWindow(this)
        mProgressDialog = ProgressDialog(this)

        handler_error.visibility = View.INVISIBLE


        name.filters = (arrayOf<InputFilter>(InputFilter.LengthFilter(maximum_character)))
        handler.filters = (arrayOf<InputFilter>(InputFilter.LengthFilter(maximum_character_handler)))
        bio.filters = (arrayOf<InputFilter>(InputFilter.LengthFilter(maximum_desc_char)))
        handler.isLongClickable = false
        name.disableCopyPaste()
        handler.disableCopyPaste()

        val currentyear = calendar.get(Calendar.YEAR)

        month.filters = arrayOf<InputFilter>(InputFilterMinMax(1, 12))
        date.filters = arrayOf<InputFilter>(InputFilterMinMax(1, 31))
        year.filters = arrayOf<InputFilter>(InputFilterMinMax(1, currentyear))

        month.disableCopyPaste()
        date.disableCopyPaste()
        year.disableCopyPaste()

        iv_calendar.setOnClickListener {

            onDatePicker(DatePickerDialog.OnDateSetListener { view, yearofyear, monthOfYear,
                                                              dayOfMonth ->
                calendar.set(Calendar.YEAR, yearofyear)
                calendar.set(Calendar.MONTH, monthOfYear)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                date.setText(dayOfMonth.toString())
                month.setText((monthOfYear + 1).toString())
                year.setText(yearofyear.toString())
            })
        }


        val radioGroup = findViewById<View>(R.id.gender_radiogroup) as RadioGroup

        radioGroup.setOnCheckedChangeListener { group, checkedId ->
            if (checkedId == R.id.male) {
                genderType = Gender.MALE
            } else if (checkedId == R.id.female) {
                genderType = Gender.FEMALE
            } else {
                genderType = Gender.OTHER
            }
        }

        handler.addTextChangedListener(this)

        handler.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus && handler.text?.length == maximum_character_handler) {
                val toastText = CommonUtils.getString(R.string.edit_profile_max_charater_handler, maximum_character_handler)
                FontHelper.showCustomFontToast(this, toastText, Toast
                        .LENGTH_SHORT)
            }
        }

        handler.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_UP -> {
                    myServerProfile?.let {
                        if (!it.handleEditable) {
                            val currentTime = System.currentTimeMillis()
                            var editInDays: Int = 0
                            val lasteditedTime = it.handleNextEditTime ?: System.currentTimeMillis()
                            if (lasteditedTime != -1L) {
                                val difference = (lasteditedTime - currentTime) / TimeUnit.DAYS.toMillis(1)
                                editInDays = abs(difference).toInt()
                            }
                            if (editInDays > 0 && !myProfile.isCreator()) {
                                val toastText = CommonUtils.getString(R.string.edit_profile_remaing_days, editInDays)
                                FontHelper.showCustomFontToast(this, toastText, Toast
                                        .LENGTH_SHORT)
                            }
                        }
                    }
                }
            }
            false
        }

        disclaimerText = CommonUtils.getString(R.string.edit_profile_disclaimer)
        saveDisclaimerText = CommonUtils.getString(R.string.edit_profile_confirmation)

        name.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable?) {
                s?.let {
                    myChangedProfile?.let {

                        it.name = s.toString()
                        editProfileViewModel.checkProfileValid(it)
                    }
                    if (!CommonUtils.isEmpty(currentName) && CommonUtils.equals(currentName, it.toString()) ||
                            CommonUtils.isEmpty(it.toString())) {
                        return
                    }

                    if (it.toString().length == maximum_character) {

                        val toastText = CommonUtils.getString(R.string
                                .edit_profile_max_charater, maximum_character)
                        FontHelper.showCustomFontToast(this@EditProfileActivity, toastText, Toast
                                .LENGTH_SHORT)
                    }
                }

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

        })

        name.setOnFocusChangeListener { v, hasFocus ->

            if (hasFocus && name.text?.length == maximum_character) {

                val toastText = CommonUtils.getString(R.string.edit_profile_max_charater, maximum_character)
                FontHelper.showCustomFontToast(applicationContext, toastText, Toast
                        .LENGTH_SHORT)
            }
        }
        bio.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                s?.let {
                    myChangedProfile?.let {
                        it.bio = s.toString()
                        editProfileViewModel.checkProfileValid(it)
                    }
                    if (!CommonUtils.isEmpty(currentBio) && CommonUtils.equals(currentBio, it.toString()) ||
                            CommonUtils.isEmpty(it.toString())) {
                        return
                    }
                    if (it.toString().length >= maximum_desc_char) {

                        val toastText = CommonUtils.getString(R.string
                                .edit_profile_max_charater, maximum_desc_char)
                        FontHelper.showCustomFontToast(this@EditProfileActivity, toastText, Toast
                                .LENGTH_SHORT)
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })
        bio.enableTextViewScroll(findViewById<ScrollView>(R.id.edit_profile_scroll))

        if (myProfile != null) {
            if (!CommonUtils.isEmpty(myProfile.name)) {
                currentName = myProfile.name
                name.setText(myProfile.name)
            }
            if (!CommonUtils.isEmpty(myProfile.handle)) {
                currentHandler = myProfile.handle
                handler.setText(myProfile.handle)
            }

            if (!CommonUtils.isEmpty(myProfile.bio)) {
                currentBio = myProfile.bio
                bio.setText(myProfile.bio)
            }

            if (!CommonUtils.isEmpty(myProfile.uiLocation?.name)) {
                currentLocation = myProfile.uiLocation
                location.text = myProfile.uiLocation?.name
            }

            myProfile.profileImage?.let {
                loadProfileImage(it)
            }

            privacyToggle.isChecked = !myProfile.isPrivateProfile()
        }
        viewBinding.executePendingBindings()
    }

    private fun loadProfileImage(imgUrl: String) {
        val profilePicSize = CommonUtils.getDimension(R.dimen.edit_profile_pic_height)
        currentImage = imgUrl
        val liveDimension = getImageDimension()
        Image.load(ImageUrlReplacer.getQualifiedImageUrl(imgUrl, CommonUtils.getDeviceScreenWidthInDp
        (), profilePicSize))
                .placeHolder(ContextCompat.getDrawable(this, R.drawable.default_user_avatar))
                .apply(RequestOptions.circleCropTransform())
                .into(findViewById<ImageView>(R.id.profile_picture))
    }

    private fun populateViews(myProfile: MyProfile?) {
        myProfile ?: return
        editProfileViewModel.setApprovedHandle(myProfile.handle)

        myServerProfile = myProfile

        myChangedProfile?.name = myProfile.name
        myChangedProfile?.handle = myProfile.handle

        if (!CommonUtils.isEmpty(myProfile.name))
            currentName = myProfile.name
        name.setText(myProfile.name)
        if (!CommonUtils.isEmpty(myProfile.handle)) {
            currentHandler = myProfile.handle
            handler.setText(myProfile.handle)
        }
        if (!CommonUtils.isEmpty(myProfile.bio)) {
            currentBio = myProfile.bio
            bio.setText(myProfile.bio)
        }

        if (!CommonUtils.isEmpty(myProfile.uiLocation?.name)) {
            currentLocation = myProfile.uiLocation
            location.text = myProfile.uiLocation?.name
        }

        taggingCB.isChecked = myProfile.taggingPermission == AccountPermission.ALLOWED

        if (!myProfile.handleEditable) {
            handler.isFocusable = false
            handler.inputType = InputType.TYPE_NULL
        } else {
            handler.isFocusable = true
            handler.inputType = InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
        }

        myProfile.gender()?.let {
            genderType = myProfile.gender()
            when (genderType) {
                Gender.MALE -> (findViewById<View>(R.id.male) as RadioButton).isChecked = true
                Gender.FEMALE -> (findViewById<View>(R.id.female) as RadioButton).isChecked = true
                Gender.OTHER -> (findViewById<View>(R.id.others) as RadioButton).isChecked = true
            }
        }
        myProfile.profileImage?.let {
            val profilePicSize = CommonUtils.getDimension(R.dimen.edit_profile_pic_height)
            Image.load(ImageUrlReplacer.getQualifiedImageUrl(it, CommonUtils.getDeviceScreenWidthInDp(), profilePicSize))
                    .placeHolder(ContextCompat.getDrawable(this, R.drawable.default_user_avatar))
                    .apply(RequestOptions.circleCropTransform())
                    .into(findViewById<ImageView>(R.id.profile_picture))
        }

        privacyToggle.isChecked = !myProfile.isPrivateProfile()

        disclaimerText = if (myProfile.handleEditPrompt != null) {
            myProfile.handleEditPrompt!!
        } else if (myProfile.isCreator()) {
            CommonUtils.getString(R.string.edit_profile_disclaimer_creater)
        } else {
            CommonUtils.getString(R.string.edit_profile_disclaimer)
        }

        if (myProfile.handleSavePrompt != null) {
            saveDisclaimerText = myProfile.handleSavePrompt!!
        } else {
            saveDisclaimerText = CommonUtils.getString(R.string.edit_profile_confirmation)
        }

        disclaimer.setOnClickListener {
            displayPopupWindow(disclaimer)

        }

        if (myProfile.isCreator()) {
            findViewById<View>(R.id.privacy_layout).visibility = View.GONE
        }

        myProfile.dob?.let {
            val parts = it.split("-")
            if (!CommonUtils.isEmpty(parts)) {
                if (parts.size > 2) {
                    year.setText(parts[2])
                    month.setText(parts[1])
                    date.setText(parts[0])
                } else if (parts.size > 1) {
                    month.setText(parts[1])
                    date.setText(parts[0])
                } else if (parts.isNotEmpty()) {
                    date.setText(parts[0])
                }
            }

            val sdf = SimpleDateFormat("dd-MM-yyyy")
            val date = sdf.parse(it)
            calendar.time = date
        }
        editProfileViewModel.checkProfileValid(myProfile)
        if (myProfile.linkedAccounts?.isNullOrEmpty()?.not() == true) {
            viewBinding.connectAccountsContainer.linkAccountsParent.visibility = View.VISIBLE
            viewBinding.connectAccountsContainer.myProfile = myProfile
            viewBinding.connectAccountsContainer.linkAccountsParent.tc.connectOrConnected.setOnClickListener(this)
            viewBinding.connectAccountsContainer.linkAccountsParent.fb.connectOrConnected.setOnClickListener(this)
            viewBinding.connectAccountsContainer.linkAccountsParent.google.connectOrConnected.setOnClickListener(this)
        }
        viewBinding.executePendingBindings()
    }

    private fun onDatePicker(onDateSetListener: DatePickerDialog.OnDateSetListener) {
        val dialog = DatePickerDialog(
                this, R.style.DatePickerDialogTheme, onDateSetListener,
                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        )

        dialog.datePicker.maxDate = System.currentTimeMillis()

        dialog.setTitle(getString(R.string.edit_profile_select_birthday))
        dialog.show()

    }


    private fun enableSave(enable: Boolean) {
        saveButton.isEnabled = enable
    }

    private fun displayPopupWindow(anchorView: View) {

        val contentView = layoutInflater.inflate(R.layout.disclaimer_popup, null)
        val caption = contentView.findViewById(R.id.tvCaption) as NHTextView
        caption.text = disclaimerText
        popup.contentView = contentView
        popup.height = WindowManager.LayoutParams.WRAP_CONTENT
        popup.width = WindowManager.LayoutParams.WRAP_CONTENT
        popup.isOutsideTouchable = true
        popup.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val screenPos = IntArray(2)
        anchorView.getLocationOnScreen(screenPos)

        val anchorRect = Rect(screenPos[0], screenPos[1], screenPos[0]
                + anchorView.getWidth(), screenPos[1] + anchorView.getHeight());


        contentView.measure(WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT)

        val contentViewHeight = contentView.measuredHeight
        val contentViewWidth = contentView.measuredWidth
        val positionX = anchorRect.right - (contentViewWidth)
        val positionY = anchorRect.top - (contentViewHeight)

        popup.showAtLocation(anchorView, Gravity.NO_GRAVITY, positionX, positionY)

    }


    private fun setUpActionBar(themeID: Int) {
        val actionBar = findViewById(R.id.actionbar) as Toolbar
        if (themeID == R.style.AppThemeDay) {
            actionBar.background = CommonUtils.getDrawable(R.drawable.action_bar_drawable)
        }
        findViewById<View>(R.id.save_profile).setOnClickListener(this)
        findViewById<View>(R.id.toolbar_back_button_container).setOnClickListener(this)
        setSupportActionBar(actionBar)
        supportActionBar!!.setDisplayShowTitleEnabled(false)
    }

    override fun onClick(view: View?) {
        view ?: return
        when (view.id) {

            R.id.toolbar_back_button_container -> {
                onBackPressed()
            }
            R.id.save_profile -> {
                saveProfileData()
            }
            R.id.edit_picture_text -> {
                pickImage()
            }
            R.id.et_location -> {
                handleLocationClick()
            }
            R.id.connectOrConnected -> {
                (view.tag as? LoginType?)?.let {
                    handleAccountLinking(it)
                }
            }
        }
    }

    private fun pickImage() {
        if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestStoragePermission()
            return
        } else {
            startActivityForResult(getImagePickerIntent(), GALLERY_REQUEST_CODE)
        }
    }

    private fun requestStoragePermission() {
        BusProvider.getUIBusInstance().register(this)
        storagePermissionHelper = PermissionHelper(storagePermissionAdapter).apply {
            requestPermissions()
        }
    }
    @Subscribe
    fun onPermissionResult(result: PermissionResult) {
        result.permissions.forEach {
            when(it) {
                Permission.ACCESS_FINE_LOCATION.permission -> {
                    locationPermissionHelper?.handlePermissionCallback(this, result.permissions)
                }
                Permission.READ_EXTERNAL_STORAGE.permission -> {
                    storagePermissionHelper?.handlePermissionCallback(this, result.permissions)
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)

        if (resultCode == Activity.RESULT_OK)
            when (requestCode) {
                GALLERY_REQUEST_CODE -> {
                    intent?.data ?: return
                    editProfileViewModel.validateImage(intent.data)
                }
                LOCATION_REQUEST_CODE -> {
                    val postCurrentPlace = (intent?.getSerializableExtra(PostConstants.POST_SELECTED_LOCATION) as? PostCurrentPlace?)
                    location.text = postCurrentPlace?.name
                    currentLocation = postCurrentPlace
                    myServerProfile?.location = postCurrentPlace?.name
                }
                ACC_LINK_REQUEST_CODE -> {
                    handleAccountLinkingResult(intent)
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


    private fun showToast(message: String?) {
        if (this?.isFinishing || message == null) {
            return
        }
        FontHelper.showCustomFontToast(this, message, Toast.LENGTH_LONG)
    }

    private fun saveProfileData() {
        if (!CommonUtils.isEmpty(handler.text?.toString()) && !CommonUtils.equals(currentHandler, handler.text?.toString())) {
            val title = Constants.EMPTY_STRING
            val message = saveDisclaimerText
            supportFragmentManager?.let {
                val commonMessageDialogOptions = CommonMessageDialogOptions(
                    this.activityId,
                    title,
                    message,
                    CommonUtils.getString(R.string.dialog_yes),
                    CommonUtils.getString(R.string.dialog_no)
                )

                CommonMessageDialog.newInstance(commonMessageDialogOptions).show(it, "CommonMessageDialog")
            }
        } else {
            sendData()
        }
    }

    private fun observeFragmentCommunications() {
        ViewModelProviders.of(this).get(FragmentCommunicationsViewModel::class.java).fragmentCommunicationLiveData.observe(this, Observer {
            if (it.hostId != activityId) {
                return@Observer
            }

            when (it.anyEnum) {
                is CommonMessageEvents -> {
                    if (it.anyEnum == CommonMessageEvents.POSITIVE_CLICK) {
                        if (it.useCase.equals(SAVE_PROFILE)) {
                            saveProfileData()
                        } else {
                            sendData()
                        }
                    }
                }
            }
        })
    }

    private fun sendData() {
        mProgressDialog.setIndeterminate(true)
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        mProgressDialog.setCanceledOnTouchOutside(false)
        mProgressDialog.setMessage(CommonUtils.getString(R.string.saving_group))
        mProgressDialog.show()
        val name: String = name.text?.toString() ?: Constants.EMPTY_STRING
        val handle: String = handler.text?.toString() ?: Constants.EMPTY_STRING

        var dob: String? = null
        if (!CommonUtils.isEmpty(date.text.toString()) || !CommonUtils.isEmpty(month.text.toString()) || !CommonUtils
                        .isEmpty(year.text.toString())) {
            dob = date.text.toString() + "-" + month.text.toString() + "-" + year.text
                    .toString()
        }

        privacy = if (privacyToggle.isChecked) {
            SocialPrivacy.PUBLIC
        } else {
            SocialPrivacy.PRIVATE
        }

        if (taggingCB.isChecked) {
            taggingPermission = AccountPermission.ALLOWED
        } else {
            taggingPermission = AccountPermission.NOT_ALLOWED
        }

        val locationStr = currentLocation?.let {
            JsonUtils.toJson(it)
        }
        val updateProfileBody = UpdateProfileBody(name,
                handle,
                genderType?.gender,
                dob,
                privacy.name,
                taggingPermission,
                invitesPermission,
                bio.text?.toString(),
                chosenImagePath,
                locationStr)
        editProfileViewModel.updateProfile(chosenImagePath, updateProfileBody, appLanguage)
    }

    private fun sentResult(it: Result<UIResponseWrapper<MyProfile>>) {
        mProgressDialog.dismiss()
        if (it.isSuccess) {
            val result = it.getOrNull()
            if (result != null && result.response != null) {
                val myprofile = result.response
                myprofile?.let {
                    logAnalyticsEvent(it.name, it.handle, it.privacy?.name ?: SocialPrivacy.PUBLIC.name,
                            myprofile.gender()?.gender, currentLocation?.name, it.mobile, it.email, it.invitesPermission!!.name,
                            it.taggingPermission!!.name, pageReferrer)
                }
                val returnIntent = Intent()
                returnIntent.putExtra(Constants.BUNDLE_UPDATED_PROFILE, myprofile)
                setResult(RESULT_OK, returnIntent)
                finish()
            } else {
                if (result != null && result.message != null) {
                    FontHelper.showCustomFontToast(this, result.message, Toast
                            .LENGTH_SHORT)
                }
            }
        } else {
            FontHelper.showCustomFontToast(this, it.exceptionOrNull()?.message, Toast
                    .LENGTH_SHORT)
        }
    }

    fun logAnalyticsEvent(fullName: String?, userName: String?, privacy: String, gender: String?,
                          location: String?, mobileNo: String?, emailId: String?, invite: String,
                          tagging: String, pageReferrer: PageReferrer?) {
        var map = HashMap<NhAnalyticsEventParam, Any>()

        map[NHProfileAnalyticsEventParam.FULLNAME_NEW] = fullName ?: Constants.EMPTY_STRING
        map[NHProfileAnalyticsEventParam.USERNAME_NEW] = userName ?: Constants.EMPTY_STRING
        map[NHProfileAnalyticsEventParam.GENDER_NEW] = gender ?: Constants.EMPTY_STRING
        map[NHProfileAnalyticsEventParam.PRIVACY_NEW] = privacy
        map[NHProfileAnalyticsEventParam.LOCATION_NEW] = location ?: Constants.EMPTY_STRING
        map[NHProfileAnalyticsEventParam.MOBILE_NUMBER_NEW] = mobileNo ?: Constants.EMPTY_STRING
        map[NHProfileAnalyticsEventParam.EMAIL_ID_NEW] = emailId ?: Constants.EMPTY_STRING
        map[NHProfileAnalyticsEventParam.INVITE_NEW] = invite
        map[NHProfileAnalyticsEventParam.TAGGING_NEW] = tagging

        if (myServerProfile != null) {
            map[NHProfileAnalyticsEventParam.FULLNAME_OLD] = myServerProfile!!.name
                    ?: Constants.EMPTY_STRING
            map[NHProfileAnalyticsEventParam.USERNAME_OLD] = myServerProfile!!.handle
                    ?: Constants.EMPTY_STRING
            map[NHProfileAnalyticsEventParam.GENDER_OLD] = myServerProfile!!.gender()?.toString()
                    ?: Constants.EMPTY_STRING
            map[NHProfileAnalyticsEventParam.PRIVACY_OLD] = myServerProfile!!.privacy?.name ?: SocialPrivacy.PUBLIC.name
            map[NHProfileAnalyticsEventParam.LOCATION_OLD] = currentLocation?.name
                    ?: Constants.EMPTY_STRING
            map[NHProfileAnalyticsEventParam.MOBILE_NUMBER_OLD] = myServerProfile!!.mobile
                    ?: Constants.EMPTY_STRING
            map[NHProfileAnalyticsEventParam.EMAIL_ID_OLD] = myServerProfile!!.email
                    ?: Constants.EMPTY_STRING
            map[NHProfileAnalyticsEventParam.INVITE_OLD] = myServerProfile!!.invitesPermission
                    ?: AccountPermission.ALLOWED.name
            map[NHProfileAnalyticsEventParam.TAGGING_OLD] = myServerProfile!!.taggingPermission
                    ?: AccountPermission.ALLOWED.name
        }

        AnalyticsClient.log(NHProfileAnalyticsEvent.PROFILE_EDIT, NhAnalyticsEventSection.PROFILE,
                map, pageReferrer)
    }

    override fun afterTextChanged(s: Editable?) {

        s?.let {
            myChangedProfile?.let {
                it.handle = s.toString()
                editProfileViewModel.checkProfileValid(it)
            }

            if (CommonUtils.equals(currentHandler, it.toString())) {
                handler_error.visibility = View.GONE
                return
            }

            if (it.toString().length == maximum_character_handler) {

                val toastText = CommonUtils.getString(R.string.edit_profile_max_charater_handler)
                FontHelper.showCustomFontToast(this, toastText, Toast
                        .LENGTH_SHORT)
            }

            editProfileViewModel.validateHandle(it.toString())?.let { uiResponseWrapper ->
                handleError(uiResponseWrapper)
            }
        }
    }

    private fun handleError(it: UIResponseWrapper<Int>?) {
        myChangedProfile?.let {
            editProfileViewModel.checkProfileValid(it)
        }
        if (it != null) {
            if (!CommonUtils.isEmpty(it.message)) {
                val error = it.message
                handler_error.visibility = View.VISIBLE
                handler_error.text = error
            } else {
                handler_error.visibility = View.GONE
            }
        }
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        //Do nothing.
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        //Do nothing.
    }


    private fun showError(throwable: Throwable?) {
        if (!::errorParent.isInitialized) return
        if (throwable is BaseError) {
            Logger.d(LOG_TAG, "showing error for ${throwable.message}")
            popup.dismiss()
            mProgressDialog?.dismiss()
            errorParent.visibility = View.VISIBLE
            errorMessageBuilder = ErrorMessageBuilder(errorParent, this, object : ErrorMessageBuilder.ErrorMessageClickedListener {
                override fun onRetryClicked(view: View?) {
                    Logger.d(LOG_TAG, "Retrying to fetch userProfile")
                    hideError()
                    editProfileViewModel.fetchMyProfile(appLanguage)
                }

                override fun onNoContentClicked(view: View?) {
                    Logger.d(LOG_TAG, "Navigating back to news home")
                    launchNewsHome()
                }
            })
            errorMessageBuilder!!.showError(throwable)
        }
    }


    fun launchNewsHome() {
        val prevNewsAppSection = AppSectionsProvider.getAnyUserAppSectionOfType(AppSection.NEWS)
        prevNewsAppSection ?: return
        launchNewsHome(this, false, prevNewsAppSection.id, prevNewsAppSection.appSectionEntityKey)
        finish()
    }


    private fun hideError() {
        if (!::errorParent.isInitialized) return
        errorMessageBuilder?.hideError()
        errorParent.visibility = View.GONE
        errorMessageBuilder = null
    }

    fun getImageDimension(): Pair<Int, Int> {
        val imageWidth = CommonUtils.getDeviceScreenWidth()
        val aspectRatio = ImageUrlReplacer.getContentImageAspectRatio()
        val imageHeight: Int
        if (java.lang.Float.compare(aspectRatio, 1.0f) == 0) {
            imageHeight = CommonUtils.getDimensionInDp(R.dimen.edit_profile_pic_height)
        } else {
            imageHeight = Math.round(imageWidth / ImageUrlReplacer.getContentImageAspectRatio()).toInt()
        }

        return Pair.create(imageWidth, imageHeight)
    }

    private fun launchLocationActivity() {
        Intent(this, PostLocationActivity::class.java).apply {
            this.setPackage(AppConfig.getInstance().packageName)
            this.putExtra(PostConstants.POST_SELECTED_LOCATION, currentLocation)
            startActivityForResult(this, LOCATION_REQUEST_CODE)
        }
    }

    private fun handleLocationClick() {
        if (ContextCompat.checkSelfPermission(this, Permission.ACCESS_FINE_LOCATION.permission) != PackageManager.PERMISSION_GRANTED) {
            requestLocationPermission()
            return
        } else {
            launchLocationActivity()
        }
    }

    private fun requestLocationPermission() {
        BusProvider.getUIBusInstance().register(this)
        locationPermissionHelper = PermissionHelper(locationPermissionAdapter).apply {
            requestPermissions()
        }
    }

    private fun handleAccountLinking(loginType: LoginType) {
        Logger.d(LOG_TAG, "Lets connect $loginType")
        val enableOneTouchLogin = !(loginType == LoginType.MOBILE && ProfileViewBindingUtils.isAccountVerified(myServerProfile?.linkedAccounts, loginType))
        CommonNavigator.getAccountLinkingActivityIntent(null,
                loginType,
                enableOneTouchLogin,
                null,
                PageReferrer(NhGenericReferrer.EDIT_PROFILE))?.let {
            val referrer = PageReferrer(NhGenericReferrer.EDIT_PROFILE)
            if (loginType == LoginType.MOBILE && ProfileViewBindingUtils.isAccountVerified(myServerProfile?.linkedAccounts, loginType)) {
                AccountsAnalyticsHelper.logMobileNumberEditClicked(referrer)
            } else {
                AccountsAnalyticsHelper.logAccountOptionSelectedEvent(referrer, AuthType.getAuthTypeFromLoginType(loginType)?.name)
            }
            startActivityForResult(it, ACC_LINK_REQUEST_CODE)
        }
    }

    private fun handleAccountLinkingResult(intent: Intent?) {
        intent ?: return
        val result = intent.getSerializableExtra(Constants.BUNDLE_ACCOUNT_LINKING_RESULT) as? AccountLinkingResult?
        result?.let { it ->
            when (it) {
                AccountLinkingResult.DIFFERENT_ACC_LINKED -> {
                    Logger.d(LOG_TAG, "User linked a different account, finish!")
                    //Account session changed, finish and go back to profile activity
                    finish()
                }
                else -> {
                    Logger.d(LOG_TAG, "Refresh the profile, result: $it")
                    editProfileViewModel.fetchMyProfile(appLanguage)
                }
            }
        }
    }
}


class InputFilterMinMax : InputFilter {
    private var min: Int = 0
    private var max: Int = 0

    constructor(min: Int, max: Int) {
        this.min = min
        this.max = max
    }

    constructor(min: String, max: String) {
        this.min = Integer.parseInt(min)
        this.max = Integer.parseInt(max)
    }

    override fun filter(source: CharSequence, start: Int, end: Int, dest: Spanned, dstart: Int, dend: Int): CharSequence? {
        try {
            val input = Integer.parseInt(dest.toString() + source.toString())
            if (input.isInRange(min, max))
                return null
        } catch (nfe: NumberFormatException) {
        }

        return Constants.EMPTY_STRING
    }


}