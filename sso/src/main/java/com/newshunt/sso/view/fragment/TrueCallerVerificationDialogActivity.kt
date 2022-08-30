/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.sso.view.fragment

import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.text.Html
import android.text.Spannable
import android.text.method.LinkMovementMethod
import android.view.View
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.Group
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.credentials.Credential
import com.google.android.gms.auth.api.credentials.HintRequest
import com.google.android.gms.common.api.GoogleApiClient
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.font.FontHelper
import com.newshunt.common.helper.info.ClientInfoHelper
import com.newshunt.common.view.customview.NHBaseActivity
import com.newshunt.common.view.customview.fontview.NHTextView
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.model.entity.AuthType
import com.newshunt.dataentity.sso.model.entity.LoginPayload
import com.newshunt.dhutil.helper.theme.ThemeType
import com.newshunt.dhutil.helper.theme.ThemeUtils
import com.newshunt.sso.R
import com.newshunt.sso.helper.CustomHashGenerator
import com.newshunt.sso.helper.social.TrueCallerLoginCallback
import com.newshunt.sso.helper.social.TruecallerLoginHelper
import com.newshunt.sso.helper.social.TruecallerLoginHelper.getLoginPayloadFromTrueProfile
import com.newshunt.sso.helper.social.TruecallerLoginHelper.getPhoneNumber
import com.newshunt.sso.model.entity.UserExplicit
import com.newshunt.sso.view.EnterMobileNumberDialog
import com.newshunt.sso.view.EnterNameDialog
import com.newshunt.sso.view.EnterOtpDialog
import com.truecaller.android.sdk.TrueButton
import com.truecaller.android.sdk.TrueException
import com.truecaller.android.sdk.TrueProfile
import com.truecaller.android.sdk.TruecallerSDK
import com.truecaller.android.sdk.clients.VerificationCallback


/**
 * @author anshul.jain
 */

const val MOBILE_NUMBER_LENGTH = 10
private const val RESOLVE_HINT = 500

class TrueCallerVerificationDialogActivity : NHBaseActivity(), EnterOtpDialogListener, EnterMobileNumberDialogListener, EnterNameDialogListener, TrueCallerLoginCallback {
    private val TAG = "TCVerificationActivity"
    private lateinit var enterMobileNumberLayout: ConstraintLayout
    private lateinit var phoneNumberVerificationInProgress: ConstraintLayout
    private lateinit var enterOtpLayout: ConstraintLayout
    private lateinit var enterNameLayout: ConstraintLayout
    private var trueProfile: TrueProfile? = null
    private var phoneNumber: String = Constants.EMPTY_STRING
    private var verificationMode = 0
    private var otp: String? = null
    private var enterMobileNumberDialog: EnterMobileNumberDialog? = null
    private lateinit var name: String
    private lateinit var requestCode: String
    private var enterOtpDialog: EnterOtpDialog? = null
    private lateinit var trueButton: TrueButton
    private val handler = Handler()
    private var dialogState: TrueCallerVerificationDialogStates? = null
    private var enableOneTouchLogin = true
    private var isFirstResume = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val themeID = if (ThemeUtils.preferredTheme == ThemeType.DAY)
            com.newshunt.dhutil.R.style.TruecallerBackgroundDay
        else
            com.newshunt.dhutil.R.style.TruecallerBackgroundNight
        setTheme(themeID)
        setContentView(R.layout.activity_true_caller_dialog)
        enterMobileNumberLayout = findViewById(R.id.layout_enter_mobile_number)
        phoneNumberVerificationInProgress = findViewById(R.id.phone_number_verification_in_progress)
        enterOtpLayout = findViewById(R.id.layout_enter_otp)
        enterNameLayout = findViewById(R.id.layout_enter_name)
        trueButton = findViewById(R.id.com_truecaller_android_sdk_truebutton)
        TruecallerLoginHelper.setCallback(this)

        val intent: Intent? = intent
        name = intent?.getStringExtra(Constants.NAME).orEmpty()
        phoneNumber = intent?.getStringExtra(Constants.MOBILE_NUMBER).orEmpty()
        requestCode = intent?.getStringExtra(Constants.REQUEST_CODE).orEmpty()
        enableOneTouchLogin = intent?.getBooleanExtra(Constants.BUNDLE_ENABLE_ONE_TOUCH_LOGIN, true)
                ?: true

        findViewById<ImageView>(R.id.sign_in_cross_button).setOnClickListener { onBackPressed() }
    }

    override fun onResume() {
        super.onResume()
        if (isFirstResume) {
            //if mobile number already there start from mobile verification
            //used in edit profile flow to update mobile number
            if (phoneNumber.isNotEmpty()) {
                onMobileNumberEntered(phoneNumber)
            } else {
                handler.post {
                    tryOneClickLogin()
                }
            }
            isFirstResume = false
        }
    }

    private fun setUIAccordingToVerificationState(state: TrueCallerVerificationDialogStates,
                                                  name: String? = null) {
        this.dialogState = state
        showDialogUI(true)
        when (state) {
            TrueCallerVerificationDialogStates.ENTER_MOBILE_NUMBER -> enterMobileNumberUI()
            TrueCallerVerificationDialogStates.PHONE_NUMBER_VERIFICATION_IN_PROGRESS -> phoneNumberVerificationInProgress()
            TrueCallerVerificationDialogStates.ENTER_OTP -> enterOtpLayout()
            TrueCallerVerificationDialogStates.ENTER_NAME -> enterNameLayout()
            else -> {
            }
        }

        val id = when (state) {
            TrueCallerVerificationDialogStates.ENTER_MOBILE_NUMBER -> enterMobileNumberLayout
            TrueCallerVerificationDialogStates.PHONE_NUMBER_VERIFICATION_IN_PROGRESS -> phoneNumberVerificationInProgress
            TrueCallerVerificationDialogStates.ENTER_OTP -> enterOtpLayout
            TrueCallerVerificationDialogStates.ENTER_NAME -> enterNameLayout
            else -> null
        }


        id?.let {
            for (view in listOf(enterMobileNumberLayout,
                    phoneNumberVerificationInProgress,
                    enterOtpLayout,
                    enterNameLayout)) {
                view.visibility = if (view.id == it.id) View.VISIBLE else View.GONE
            }
        }
    }

    private fun enterMobileNumberUI() {
        enterMobileNumberDialog = EnterMobileNumberDialog(enterMobileNumberLayout, this)
        enterMobileNumberDialog?.init()
        requestHint()
    }

    // Construct a request for phone numbers and show the picker
    private fun requestHint() {
        val hintRequest = HintRequest.Builder().setPhoneNumberIdentifierSupported(true)
                .build()

        val apiClient = GoogleApiClient.Builder(this).addApi(Auth.CREDENTIALS_API).build()

        val intent = Auth.CredentialsApi.getHintPickerIntent(apiClient, hintRequest)
        startIntentSenderForResult(intent.intentSender, RESOLVE_HINT, null, 0, 0, 0)
    }

    // Obtain the phone number from the result
    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RESOLVE_HINT) {
            if (resultCode == Activity.RESULT_OK) {
                val credential = data?.getParcelableExtra(Credential.EXTRA_KEY) as? Credential
                if (!CommonUtils.isEmpty(credential?.id)) {
                    enterMobileNumberDialog?.fillPhoneNumber(credential?.id)
                }
            }
        }

        if (requestCode == Constants.REQ_CODE_TRUECALLER) {
            TruecallerSDK.getInstance().onActivityResultObtained(this, resultCode, data)
        }
    }

    override fun onRequestVerificationAgain() {
        Logger.d(TAG, "requesting verification again")
        TruecallerLoginHelper.requestNumberVerification(phoneNumber)
    }

    override fun onOtpEntered(otp: String) {
        this.otp = otp
        Logger.d(TAG, "On otp entered : $otp")
        TruecallerLoginHelper.requestOtpVerification(trueProfile, otp)
    }

    override fun onOTPLimitExceeded() {
        sendResponseAndFinishActivity(null, false)
    }

    override fun onMobileNumberEntered(phoneNumber: String) {
        Logger.d(TAG, "On mobile number entered : $phoneNumber")
        this.phoneNumber = phoneNumber
        setUIAccordingToVerificationState(TrueCallerVerificationDialogStates.PHONE_NUMBER_VERIFICATION_IN_PROGRESS)
        TruecallerLoginHelper.requestNumberVerification(phoneNumber)
    }

    override fun onNameEntered(name: String) {
        Logger.d(TAG, "On name entered : $name")
        trueProfile = TrueProfile.Builder(name, name).build()

        if (verificationMode == VerificationCallback.TYPE_MISSED_CALL) {
            TruecallerLoginHelper.requestMissedCallVerification(trueProfile)
        } else if (verificationMode == VerificationCallback.TYPE_OTP) {
            setUIAccordingToVerificationState(TrueCallerVerificationDialogStates.ENTER_OTP)
        }
    }

    override fun onDestroy() {
        enterOtpDialog?.cleanup()
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }

    private fun enterOtpLayout() {
        enterOtpDialog = EnterOtpDialog(enterOtpLayout, this).apply {
            init()
        }
    }

    private fun enterNameLayout() {
        EnterNameDialog(enterNameLayout, this).init()
    }

    private fun phoneNumberVerificationInProgress() {
        val termsAndConditions = findViewById<NHTextView>(R.id
                .number_verification_terms_and_conditions)
        val originalString = getString(R.string.truecaller_number_verification_t_and_c)
        val fontConvertedString = Html.fromHtml(FontHelper.getFontConvertedString(originalString)) as
                Spannable
        termsAndConditions.setSpannableText(fontConvertedString, originalString)
        termsAndConditions.movementMethod = LinkMovementMethod.getInstance();
    }

    private fun buildLoginPayload(accessToken: String?): LoginPayload {
        return LoginPayload(CustomHashGenerator.getHash(ClientInfoHelper.getClientId())!!,
                AuthType.TRUE_CALLER.name,
                accessToken,
                UserExplicit.YES.value,
                trueProfile?.firstName,
                getPhoneNumber(phoneNumber))
    }

    override fun onBackPressed() {
        setResult(Activity.RESULT_CANCELED)
        super.onBackPressed()
    }

    override fun onTrueCallerLoginSuccess(trueProfile: TrueProfile) {
        Logger.d(TAG, "onTrueCallerLoginSuccess, request code: ${this.requestCode}")
        sendResponseAndFinishActivity(getLoginPayloadFromTrueProfile(trueProfile), true)
        return
    }

    override fun onRequestSuccess(requestCode: Int, accessToken: String?) {
        if (requestCode == VerificationCallback.TYPE_MISSED_CALL) {
            setUIAccordingToVerificationState(TrueCallerVerificationDialogStates.ENTER_NAME)
            verificationMode = VerificationCallback.TYPE_MISSED_CALL
        } else if (requestCode == VerificationCallback.TYPE_OTP) {
            if (trueProfile == null) {
                verificationMode = VerificationCallback.TYPE_OTP
                if (name.isNotEmpty()) {
                    onNameEntered(name)
                } else {
                    setUIAccordingToVerificationState(TrueCallerVerificationDialogStates.ENTER_NAME)
                }
            }
        } else if (requestCode == VerificationCallback.TYPE_VERIFY) {
            sendResponseAndFinishActivity(buildLoginPayload(accessToken), true)
        }
    }

    private fun sendResponseAndFinishActivity(loginPayload: LoginPayload?,
                                              isSuccess: Boolean) {
        val resultIntent = Intent().apply {
            putExtra(Constants.BUNDLE_LOGIN_RESULT_SUCCESSFUL, isSuccess)
            putExtra(Constants.LOGIN_PAYLOAD, loginPayload)
        }
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }

    override fun onRequestFailure(requestCode: Int, e: TrueException) {
        sendResponseAndFinishActivity(null, isSuccess = false)
    }

    private fun tryOneClickLogin() {
        if (TruecallerLoginHelper.isTrueCallerAppInstalled() && enableOneTouchLogin) {
            TruecallerLoginHelper.oneClickLogin(trueButton)
            if (dialogState == null) {
                showDialogUI(false)
            }
        } else {
            setUIAccordingToVerificationState(TrueCallerVerificationDialogStates.ENTER_MOBILE_NUMBER)
        }
    }

    private fun showDialogUI(show: Boolean) {
        val visibility = if (show) View.VISIBLE else View.GONE
        findViewById<Group>(R.id.tc_dialog_grp)?.let {
            it.visibility = visibility
        }
    }

    override fun onTrueCallerLoginFailure() {
        Logger.d(TAG, "onTrueCallerLoginFailure, setting failure result")
        sendResponseAndFinishActivity(null, false)
    }

    override fun onTrueCallerVerificationRequired() {
        Logger.d(TAG, "onTrueCallerVerificationRequired, ENTER_MOBILE_NUMBER")
        setUIAccordingToVerificationState(TrueCallerVerificationDialogStates.ENTER_MOBILE_NUMBER)
    }
}


enum class TrueCallerVerificationDialogStates {
    ENTER_MOBILE_NUMBER,
    ENTER_NAME,
    PHONE_NUMBER_VERIFICATION_IN_PROGRESS,
    ENTER_OTP,
}

interface EnterOtpDialogListener {

    fun onRequestVerificationAgain()

    fun onOTPLimitExceeded()

    fun onOtpEntered(otp: String)
}

interface EnterMobileNumberDialogListener {

    fun onMobileNumberEntered(phoneNumber: String)
}

interface EnterNameDialogListener {

    fun onNameEntered(name: String)
}

