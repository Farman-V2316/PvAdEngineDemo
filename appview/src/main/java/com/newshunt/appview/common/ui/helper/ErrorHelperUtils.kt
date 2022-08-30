package com.newshunt.appview.common.ui.helper

import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import com.google.android.material.snackbar.Snackbar
import com.newshunt.appview.BR
import com.newshunt.appview.R
import com.newshunt.appview.databinding.LayoutGenericCustomSnackbarBinding
import com.newshunt.common.helper.common.AndroidUtils
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.view.DbgCode
import com.newshunt.common.view.dbgCode
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.common.model.entity.BaseError
import com.newshunt.dhutil.helper.theme.ThemeUtils
import com.newshunt.sdk.network.connection.ConnectionSpeed
import com.newshunt.sdk.network.connection.ConnectionSpeedEvent

class ErrorHelperUtils {
    companion object {
        @JvmStatic
        fun getErrorMessageIcon(baseError: BaseError?, view: View): Int? {
            if (baseError == null) {
                return null
            }
            val errorCode = baseError.dbgCode()
            return when (errorCode) {
                is DbgCode.DbgNoConnectivityCode -> {
                    ThemeUtils.getThemeDrawableByAttribute(view.context, R.attr.connectivity_error, View.NO_ID)
                }
                is DbgCode.DbgHttpCode -> {
                    if (errorCode.get().equals("BB04")) {
                        ThemeUtils.getThemeDrawableByAttribute(view.context, R.attr.content_error,
                                View.NO_ID)
                    } else {
                        ThemeUtils.getThemeDrawableByAttribute(view.context, R.attr.connection_error,
                                View.NO_ID)
                    }
                }
                is DbgCode.DbgBroswerServer, is DbgCode.DbgVersionedApiCorrupt, is DbgCode
                .DbgResponseErrorNull, is DbgCode.DbgErrorConnectivity, is DbgCode.DbgOnBoardingRequest -> {
                    ThemeUtils.getThemeDrawableByAttribute(view.context, R.attr.connection_error,
                            View.NO_ID)
                }

                is DbgCode.DbgUnexpectedCode, is DbgCode.DbgNotFoundInCache, is DbgCode
                .DbgBroswerGeneric -> {
                    ThemeUtils.getThemeDrawableByAttribute(view.context, R.attr.bad_error,
                            View.NO_ID)
                }
                else -> {
                    ThemeUtils.getThemeDrawableByAttribute(view.context, R.attr.content_error,
                            View.NO_ID)
                }
            }
        }

        @JvmStatic
        fun getActionText(baseError: BaseError): String {
            val errorCode = baseError.dbgCode()
            val retry = CommonUtils.getString(R.string.dialog_button_retry)
            val settings = CommonUtils.getString(R.string.action_settings)
            val unblock = CommonUtils.getString(R.string.action_unblock)
            val home = CommonUtils.getString(R.string.btn_home)
            return when (errorCode) {
                is DbgCode.DbgHttpCode -> {
                    if (errorCode.get().equals("BB04")) {
                        home
                    } else {
                        retry
                    }
                }
                else -> {
                    if(baseError.status.equals(Constants.ERROR_NO_FEED_ITEMS_BECAUSE_OF_BLOCKED_SOURCES)){
                        unblock
                    }else{
                        retry
                    }
                }
            }
        }

        @JvmStatic
        fun showErrorSnackbar(baseError: BaseError?, view: View?): Snackbar {
            val inflater = LayoutInflater.from(view?.context)
            val viewBinding =
                    DataBindingUtil.inflate<LayoutGenericCustomSnackbarBinding>(inflater, R
                            .layout.layout_generic_custom_snackbar, view as ViewGroup, false)
            viewBinding.setVariable(BR.baseError, baseError)
            val snackBar = Snackbar.make(view, Constants.EMPTY_STRING, Snackbar.LENGTH_LONG)
            val snackBarView = snackBar.view as Snackbar.SnackbarLayout
            val params = snackBarView.layoutParams as ViewGroup.MarginLayoutParams
            params.bottomMargin = CommonUtils.getDimension(R.dimen.snackbar_bottom_margin_no_bottom_bar)
            params.leftMargin = CommonUtils.getDimension(R.dimen.snackbar_margin)
            params.rightMargin = CommonUtils.getDimension(R.dimen.snackbar_margin)
            snackBarView.layoutParams = params

            val baseErrorMessage = baseError?.message ?: ""
            val errorCode = baseError?.dbgCode()?.get() ?: ""
            val spannableMessage: SpannableString
            spannableMessage = SpannableString("$baseErrorMessage $errorCode")
            spannableMessage.setSpan(RelativeSizeSpan(0.7f), baseErrorMessage.length + 1,
                    spannableMessage.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            spannableMessage.setSpan(ForegroundColorSpan(CommonUtils.getColor(com.newshunt.dhutil.R.color.white_color)),
                    baseErrorMessage.length + 1, spannableMessage.length, Spannable
                    .SPAN_EXCLUSIVE_EXCLUSIVE)

            val mainTextView = snackBarView.findViewById(R.id.snackbar_text) as TextView
            val actionTextView = snackBarView.findViewById(R.id.snackbar_action) as TextView
            mainTextView.visibility = View.INVISIBLE
            actionTextView.visibility = View.INVISIBLE
            val message = viewBinding.snackbarMessage
            message.setSpannableText(spannableMessage, baseErrorMessage)
            viewBinding.executePendingBindings()
            snackBarView.setBackgroundResource(R.drawable.snackbar_rounded_corner)
            snackBarView.addView(viewBinding.root)
            return snackBar
        }

        @JvmStatic
        fun getTextFromConnectivityEvent(speedEvent: ConnectionSpeedEvent?,
                                         baseError: BaseError?): String? {
            if (baseError == null) {
                return null
            }

            val retry = CommonUtils.getString(R.string.dialog_button_retry)
            val settings = CommonUtils.getString(R.string.action_settings)

            return if (baseError.dbgCode() is DbgCode.DbgNoConnectivityCode) {
                if (speedEvent?.connectionSpeed == null ||
                        speedEvent.connectionSpeed == ConnectionSpeed.NO_CONNECTION)
                    settings
                else
                    retry
            } else {
                getActionText(baseError)
            }
        }

        /**
         * Helper method to take throwable as parameter, cast it to BaseError and only if its a
         * BaseError, create and show the snackbar
         */
        @JvmStatic
        fun showErrorSnackbar(throwable: Throwable?, view: View?) {
            (throwable as? BaseError?)?.let { error ->
                showErrorSnackbar(error, view).show()
            }
        }
    }
}