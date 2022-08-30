/*
 * Copyright (c) 2017 Newshunt. All rights reserved.
 */
package com.newshunt.dhutil.helper

import android.graphics.Typeface
import android.widget.TextView
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.font.FontHelper
import com.newshunt.dataentity.common.asset.CommonAsset
import com.newshunt.dhutil.helper.preference.UserPreferenceUtil

/**
 * @author arun.babu
 */
class BoldStyleHelper {

    companion object {

        // bold should be applied only to specific languages. places getting affected are newslist cards
        // title, news detail title and slidingtab layout.the else condition is used in the case of
        // sliding tab layout only

        @JvmStatic
        fun setBoldTextForBigCardIfApplicable(textView: TextView?) {
            if (textView == null) {
                return
            }
            when (UserPreferenceUtil.getUserNavigationLanguage()) {
                Constants.ENGLISH_LANGUAGE_CODE, Constants.HINDI_LANGUAGE_CODE, Constants
                        .MARATHI_LANGUAGE_CODE, Constants.BHOJPURI_LANGUAGE_CODE, Constants
                        .NEPALI_LANGUAGE_CODE-> textView
                        .setTypeface(null, Typeface.BOLD)
                else -> textView.setTypeface(null, Typeface.NORMAL)
            }
            handleOriyaForDhFont(textView, UserPreferenceUtil.getUserNavigationLanguage())
        }


        @JvmStatic
        fun setBoldTextForBigCardIfApplicable2(textView: TextView?, item: CommonAsset?) {
            if (textView == null) {
                return
            }
            if (item != null) {
                when (item.i_langCode()) {
                    Constants.ENGLISH_LANGUAGE_CODE, Constants.HINDI_LANGUAGE_CODE, Constants
                            .MARATHI_LANGUAGE_CODE, Constants.BHOJPURI_LANGUAGE_CODE, Constants
                            .NEPALI_LANGUAGE_CODE -> textView.setTypeface(null, Typeface.BOLD)
                    else -> textView.setTypeface(null, Typeface.NORMAL)
                }
                item.i_langCode()?.let { handleOriyaForDhFont(textView, it) }
            } else {
                when (UserPreferenceUtil.getUserNavigationLanguage()) {
                    Constants.ENGLISH_LANGUAGE_CODE, Constants.HINDI_LANGUAGE_CODE, Constants
                            .MARATHI_LANGUAGE_CODE, Constants.BHOJPURI_LANGUAGE_CODE, Constants
                            .NEPALI_LANGUAGE_CODE-> textView
                            .setTypeface(null, Typeface.BOLD)
                    else -> textView.setTypeface(null, Typeface.NORMAL)
                }
                handleOriyaForDhFont(textView, UserPreferenceUtil.getUserNavigationLanguage())
            }
        }

        /**
         * Enable bold oriya only if DH fonts are applied.
         */
        private fun handleOriyaForDhFont(textView: TextView, lang: String) {
            if (FontHelper.enableDhFont() && lang == Constants.ORIYA_LANGUAGE_CODE) {
                textView.setTypeface(null, Typeface.BOLD)
            }
        }
    }
}
