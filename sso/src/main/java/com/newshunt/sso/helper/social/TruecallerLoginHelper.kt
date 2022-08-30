/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.sso.helper.social

import android.content.Context
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import com.newshunt.common.helper.common.AndroidUtils
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.info.ClientInfoHelper
import com.newshunt.common.helper.preference.AppUserPreferenceUtils
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.model.entity.AuthType
import com.newshunt.dataentity.sso.model.entity.LoginPayload
import com.newshunt.dataentity.sso.model.entity.TrueCallerPayload
import com.newshunt.sso.R
import com.newshunt.sso.helper.CustomHashGenerator
import com.newshunt.sso.model.entity.UserExplicit
import com.truecaller.android.sdk.ITrueCallback
import com.truecaller.android.sdk.TrueButton
import com.truecaller.android.sdk.TrueError
import com.truecaller.android.sdk.TrueException
import com.truecaller.android.sdk.TrueProfile
import com.truecaller.android.sdk.TruecallerSDK
import com.truecaller.android.sdk.TruecallerSdkScope
import com.truecaller.android.sdk.clients.VerificationCallback
import java.lang.ref.WeakReference
import java.util.*

/**
 * @author anshul.jain
 * A helper class for managing true caller login related tasks.
 */
object TruecallerLoginHelper {

    private val TAG = "TruecallerLoginHelper"
    private val COUNTRY_ISO = "IN"
    private var callbackWeakRef: WeakReference<TrueCallerLoginCallback>? = null
    private var isSDKInitDone = false

    fun oneClickLogin(trueButton: TrueButton) {
        initTrueCallerSDK(CommonUtils.getApplication())
        trueButton.callOnClick()
    }

    fun setCallback(callback: TrueCallerLoginCallback) {
        callbackWeakRef = WeakReference(callback)
    }

    private fun initTrueCallerSDK(context: Context) {
        if (isSDKInitDone) {
            return
        }
        isSDKInitDone = true
        Logger.d(TAG, " inside init of Truecaller SDK ")
        val trueScope = TruecallerSdkScope.Builder(context, sdkCallback)
                .sdkOptions(TruecallerSdkScope.SDK_OPTION_WITH_OTP)
                .consentMode(TruecallerSdkScope.CONSENT_MODE_POPUP)
                .consentTitleOption(TruecallerSdkScope.SDK_CONSENT_TITLE_VERIFY)
                .footerType(TruecallerSdkScope.FOOTER_TYPE_CONTINUE)
                .build()

        TruecallerSDK.init(trueScope)
        //For Urdu, show the dialog in English only.
        if (!Constants.URDU_LANGUAGE_CODE.equals(AppUserPreferenceUtils.getUserNavigationLanguage
                ())) {
            val locale = Locale(AppUserPreferenceUtils.getUserNavigationLanguage())
            TruecallerSDK.getInstance().setLocale(locale)
        }
    }

    fun requestNumberVerification(phoneNumber: String?) {
        phoneNumber ?: return
        initTrueCallerSDK(CommonUtils.getApplication())
        TruecallerSDK.getInstance().requestVerification(COUNTRY_ISO, phoneNumber, apiCallback)
    }

    fun requestMissedCallVerification(trueProfile: TrueProfile?) {
        Logger.d(TAG, "Inside requestMissed Call Verification")
        trueProfile ?: return
        initTrueCallerSDK(CommonUtils.getApplication())
        TruecallerSDK.getInstance().verifyMissedCall(trueProfile, apiCallback)
    }

    fun requestOtpVerification(trueProfile: TrueProfile?, otp: String?) {
        Logger.d(TAG, "Inside reqestOtp Verification method")
        if (trueProfile == null || otp == null) return
        initTrueCallerSDK(CommonUtils.getApplication())
        TruecallerSDK.getInstance().verifyOtp(trueProfile, otp, apiCallback)
    }

    fun getPhoneNumber(phoneNumber: String?): String? {
        phoneNumber ?: return phoneNumber

        return if (phoneNumber.startsWith(CommonUtils.getString(com.newshunt.common.util.R.string.india_iso_code))) {
            Constants.INDIA_ISO_CODE_PROTOCOL_FORMAT.plus(phoneNumber.removePrefix(CommonUtils.getString(
                com.newshunt.common.util.R.string.india_iso_code)))
        } else if (phoneNumber.length == 12 && phoneNumber.startsWith(Constants.TRUECALLER_INDIA_ISO_CODE_FORMAT)) {
            Constants.INDIA_ISO_CODE_PROTOCOL_FORMAT.plus(phoneNumber.removePrefix(Constants
                    .TRUECALLER_INDIA_ISO_CODE_FORMAT))
        } else if (phoneNumber.length == 10) {
            Constants.INDIA_ISO_CODE_PROTOCOL_FORMAT.plus(phoneNumber)
        } else phoneNumber
    }

    fun getPhoneNumberIn(phoneNumber: String?): String? {
        phoneNumber ?: return phoneNumber
        return if (phoneNumber.startsWith(CommonUtils.getString(com.newshunt.common.util.R.string.india_iso_code))) {
            phoneNumber.removePrefix(CommonUtils.getString(com.newshunt.common.util.R.string.india_iso_code))
        } else phoneNumber
    }

    fun getLoginPayloadFromTrueProfile(trueProfile: TrueProfile?): LoginPayload {
        trueProfile ?: return LoginPayload()
        val payload = getTCPayload(trueProfile)
        return LoginPayload(CustomHashGenerator.getHash(ClientInfoHelper.getClientId())!!,
                AuthType.TRUE_CALLER.name, trueProfile.accessToken,
                UserExplicit.YES.value, trueProfile.firstName, getPhoneNumber(trueProfile
                .phoneNumber), payload)
    }
    fun getTCPayload(trueProfile: TrueProfile?): TrueCallerPayload? {
        return if (trueProfile?.payload != null) TrueCallerPayload(trueProfile.signature,
                trueProfile.payload, trueProfile.signatureAlgorithm) else null
    }

    fun getPhoneNumberForDisplay(number: String?): String? {
        return getPhoneNumberIn(number)?.let {
            if (it.startsWith(Constants.INDIA_ISO_CODE_PROTOCOL_FORMAT)) {
                it.removePrefix(Constants.INDIA_ISO_CODE_PROTOCOL_FORMAT)
            } else {
                it
            }
        }
    }

    fun isTrueCallerAppInstalled(): Boolean {
        return AndroidUtils.isAppInstalled(Constants.TRUECALLER_PACKAGE_NAME)
    }

    private val sdkCallback = object : ITrueCallback {

        override fun onSuccessProfileShared(trueProfile: TrueProfile) {

            Logger.d(TAG, "Verified without OTP! (Truecaller User): " + trueProfile.firstName)
            callbackWeakRef?.get()?.onTrueCallerLoginSuccess(trueProfile)

        }

        override fun onFailureProfileShared(trueError: TrueError) {
            Logger.d(TAG, "onFailureProfileShared: " + trueError.errorType)
        }

        override fun onVerificationRequired() {
            Logger.d(TAG, "onVerificationRequired")
            callbackWeakRef?.get()?.onTrueCallerVerificationRequired()
        }
    }

    private val apiCallback = object : VerificationCallback {

        override fun onRequestSuccess(requestCode: Int, @Nullable accessToken: String?) {
            Logger.d(TAG, "Inside onRequestSuccess callback with requestCode $requestCode and " +
                    "accessToken is $accessToken")
            callbackWeakRef?.get()?.onRequestSuccess(requestCode, accessToken)
        }

        override fun onRequestFailure(requestCode: Int, @NonNull e: TrueException) {
            Logger.d(TAG, "OnFailureApiCallback: " + e.exceptionMessage)
            callbackWeakRef?.get()?.onRequestFailure(requestCode, e)
        }
    }

}

interface TrueCallerLoginCallback {

    fun onTrueCallerLoginSuccess(trueProfile: TrueProfile) {}

    fun onTrueCallerLoginFailure() {}

    fun onTrueCallerVerificationRequired() {}

    fun onRequestSuccess(requestCode: Int, @Nullable accessToken: String?) {}

    fun onRequestFailure(requestCode: Int, @NonNull e: TrueException) {}
}