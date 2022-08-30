/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.sso.view

import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.constraintlayout.widget.Group
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.common.helper.preference.GenericAppStatePreference
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.common.view.customview.fontview.NHButton
import com.newshunt.dhutil.helper.theme.ThemeUtils
import com.newshunt.sso.R
import com.newshunt.sso.view.fragment.EnterOtpDialogListener
import java.lang.Exception

/**
 * @author anshul.jain
 * A class which takes care of the otp screen for mobile based verification.
 */

const val OTP_LENGTH = 6;

class EnterOtpDialog(view: View, private var otpEnterOtpDialogListener: EnterOtpDialogListener?) : OtpCheckListener {

    private val firstDigit = view.findViewById<EditText>(R.id.first_digit)
    private val secondDigit = view.findViewById<EditText>(R.id.second_digit)
    private val thirdDigit = view.findViewById<EditText>(R.id.third_digit)
    private val fourthDigit = view.findViewById<EditText>(R.id.fourth_digit)
    private val fifthDigit = view.findViewById<EditText>(R.id.fifth_digit)
    private val sixthDigit = view.findViewById<EditText>(R.id.sixth_digit)
    private val verifyButton = view.findViewById<NHButton>(R.id.otp_verify_button)
    private val otpResendAction = view.findViewById<TextView>(R.id.otp_resend_msg_action)
    private val otpResendDurationTimer = view.findViewById<TextView>(R.id.otp_resend_duration_timer)
    private val otpResendTimerWithGroup = view.findViewById<Group>(R.id.otp_resend_msg_with_timer)
    private val otpNotReceivedGroup = view.findViewById<Group>(R.id.otp_not_received_group)

    private var otp: String = Constants.EMPTY_STRING
    private var verifyingProgressBar = view.findViewById<ProgressBar>(R.id
            .otp_verification_progress_bar)
    private var verifyingTextView = view.findViewById<TextView>(R.id.otp_verifying)
    private val digitsList = listOf<EditText>(firstDigit, secondDigit, thirdDigit, fourthDigit,
            fifthDigit, sixthDigit)
    private val handler = Handler()
    private val initialTimer = 120 // in seconds
    private var currentOtpCount = 1
    private val TOTAL_OTP_ATTEMPTS = PreferenceManager.getPreference(GenericAppStatePreference
            .TRUECALLER_MAX_OTP_ATTEMPTS, 3)


    fun init() {

        firstDigit.requestFocus()
        setVerifyButtonBackgroundColor(verifyButton, false)

        for (i in 0 until digitsList.size) {
            digitsList[i].apply {
                background = CommonUtils.getDrawable(if (ThemeUtils.isNightMode()) R.drawable
                        .layout_otp_digit_background_night else R.drawable.otp_digit_background)
                addTextChangedListener(CustomTextWatcher(if (i + 1 < digitsList.size)
                    digitsList[i + 1] else null, this@EnterOtpDialog))
            }

        }

        verifyButton.setOnClickListener {
            enableUI(false)
            verifyButton.visibility = View.GONE
            verifyingTextView.visibility = View.VISIBLE
            verifyingProgressBar.visibility = View.VISIBLE
            otpEnterOtpDialogListener?.onOtpEntered(otp)
        }

        otpResendAction.setOnClickListener {
            if (currentOtpCount == TOTAL_OTP_ATTEMPTS) {
                otpEnterOtpDialogListener?.onOTPLimitExceeded()
            }
            otpNotReceivedGroup.visibility = View.GONE
            otpResendTimerWithGroup.visibility = View.VISIBLE
            setTimer(initialTimer)
            currentOtpCount++
            otpEnterOtpDialogListener?.onRequestVerificationAgain()
        }

        setTimer(initialTimer)
    }

    private fun setTimer(durations: Int) {
        if (durations < 0) {
            otpNotReceivedGroup.visibility = View.VISIBLE
            otpResendTimerWithGroup.visibility = View.GONE
            return
        }
        val minutes = Constants.ZERO_STRING.plus((durations / 60).toString())
        val seconds = durations % 60
        otpResendDurationTimer.text = minutes.plus(":").plus(if (seconds < 10) Constants.ZERO_STRING else Constants.EMPTY_STRING).plus(seconds)
        handler.postDelayed({ setTimer(durations - 1) }, 1000)
    }

    private fun enableUI(enable: Boolean) {
        for (view in digitsList) {
            view.isEnabled = enable
        }
        verifyButton.isEnabled = enable
    }

    override fun checkOtp() {
        otp = Constants.EMPTY_STRING
        for (view in digitsList) {
            if (!view.text.toString().trim().isEmpty()) {
                otp = otp.plus(view.text.toString().trim())
            }
        }
        val isEnabled = otp.trim().length == OTP_LENGTH
        verifyButton.isEnabled = isEnabled
        setVerifyButtonBackgroundColor(verifyButton, isEnabled)
    }

    fun cleanup() {
        try {
            otpEnterOtpDialogListener = null
            handler.removeCallbacksAndMessages(null)
        } catch (ex: Exception) {
            Logger.caughtException(ex)
        }
    }

    class CustomTextWatcher(private val nextDigit: EditText?, private val listener: OtpCheckListener) : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            if (s?.trim()?.length == 1) {
                nextDigit?.requestFocus()
                listener.checkOtp()
            }
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        }

    }
}

interface OtpCheckListener {
    fun checkOtp()
}