/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.sso.view

import android.content.res.ColorStateList
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.core.view.ViewCompat
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.common.view.customview.fontview.NHButton
import com.newshunt.dhutil.helper.theme.ThemeUtils
import com.newshunt.sso.R
import com.newshunt.sso.helper.social.TruecallerLoginHelper.getPhoneNumberIn
import com.newshunt.sso.view.fragment.EnterMobileNumberDialogListener
import com.newshunt.sso.view.fragment.EnterNameDialogListener
import com.newshunt.sso.view.fragment.MOBILE_NUMBER_LENGTH

/**
 * @author anshul.jain
 * A class which takes care when the user enters the mobile number
 */
class EnterMobileNumberDialog(val view: View, val listener: EnterMobileNumberDialogListener) {

    private val mobileNumber = view.findViewById<EditText>(R.id.mobile_number)
    private val verifyButton = view.findViewById<NHButton>(R.id.phone_number_verify_button)

    fun init() {
        setVerifyButtonBackgroundColor(verifyButton, false)
        mobileNumber.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val isEnabled = s?.length == MOBILE_NUMBER_LENGTH
                verifyButton.isEnabled = isEnabled
                setVerifyButtonBackgroundColor(verifyButton, isEnabled)
                if (s?.length ?: 0 > 0) {
                    mobileNumber.textSize = CommonUtils.getDimensionInDp(com.newshunt.dhutil.R.dimen.facebook_icon_padding).toFloat()
                } else {
                    mobileNumber.textSize = CommonUtils.getDimensionInDp(R.dimen.enter_name_textSize).toFloat()
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })

        verifyButton.setOnClickListener {
            listener.onMobileNumberEntered(mobileNumber.text.toString())
        }
    }

    fun fillPhoneNumber(number: String?){
        number ?: return
        mobileNumber.setText(getPhoneNumberIn(number))
    }
}

/**
 * @author anshul.jain
 * A class which takes care when the user enters the name.
 */
class EnterNameDialog(val view: View, val listener: EnterNameDialogListener) {

    val name = view.findViewById<EditText>(R.id.name)
    val verifyName = view.findViewById<Button>(R.id.name_verify)

    fun init() {

        setVerifyButtonBackgroundColor(verifyName, false)
        name.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val isEnabled = s?.trim()?.length ?: 0 > 0
                verifyName.isEnabled = isEnabled
                setVerifyButtonBackgroundColor(verifyName, isEnabled)
                if (isEnabled) {
                    name.textSize = CommonUtils.getDimensionInDp(com.newshunt.dhutil.R.dimen.facebook_icon_padding).toFloat()
                } else {
                    name.textSize = CommonUtils.getDimensionInDp(R.dimen.enter_name_textSize).toFloat()
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

        })
        verifyName.setOnClickListener {
            listener.onNameEntered(name.text.toString())
        }
    }
}

fun setVerifyButtonBackgroundColor(view: View, isEnabled: Boolean) {
    if (isEnabled) {
        ViewCompat.setBackgroundTintList(view, ColorStateList.valueOf(CommonUtils.getColor(com.newshunt.common.util.R.color.follow_color)));
    } else {
        ViewCompat.setBackgroundTintList(view, ColorStateList.valueOf(
                ThemeUtils.getThemeColorByAttribute(view.context, com.newshunt.dhutil.R.attr.truecaller_verify_button_background)))
    }
}