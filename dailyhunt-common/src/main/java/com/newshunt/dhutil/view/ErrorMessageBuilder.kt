/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */
package com.newshunt.dhutil.view

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.provider.Settings
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.google.android.material.snackbar.Snackbar
import com.newshunt.common.helper.common.AndroidUtils
import com.newshunt.common.helper.common.BaseErrorBuilder
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.font.FontHelper
import com.newshunt.common.view.DbgCode
import com.newshunt.common.view.customview.GenericCustomSnackBar
import com.newshunt.common.view.customview.fontview.NHTextView
import com.newshunt.common.view.dbgCode
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.common.model.entity.BaseError
import com.newshunt.dhutil.R
import com.newshunt.dhutil.helper.FullPageErrorMessage
import com.newshunt.sdk.network.connection.ConnectionSpeed
import com.newshunt.sdk.network.connection.ConnectionSpeedEvent
import java.net.HttpURLConnection

/**
 * Responsible for building UI for the error message.
 *
 * @author satosh.dhanyamraju
 */
class ErrorMessageBuilder @JvmOverloads constructor(private val errorParentLayout: LinearLayout,
                                                    private val context: Context,
                                                    private val listener: ErrorMessageClickedListener?,
                                                    private val lifecycleOwner: LifecycleOwner =
                                                            errorParentLayout.context as LifecycleOwner,
                                                    private val connectivityData:
                                                    LiveData<ConnectionSpeedEvent>
                                                    = AndroidUtils.connectionSpeedLiveData,
                                                    private val contentLayoutParams :
                                                    LinearLayout.LayoutParams? = null) {
    var isErrorShown: Boolean = false
        private set
    var errorMsgIcon: ImageView? = null
    var errorMsg: NHTextView? = null
    var errorMsgAction: NHTextView? = null
    var errorCode: NHTextView? = null
    private var errorMessageView: View? = null
    var errorHeaderContainer: ViewGroup? = null
        private set
    private val actionListener = View.OnClickListener {
        val action = (it as? NHTextView)?.originalText
        when (action) {
            actionSettings -> errorParentLayout.context.startActivity(nwSettingIntent);
            btnHome -> listener?.onNoContentClicked(it)
            errorNoContentAction -> listener?.onNoContentClicked(it)
            dialogButtonRetry -> listener?.onRetryClicked(it)
        }
    }

    init {
        connectivityData.observe(lifecycleOwner, Observer {
            if (isErrorShown && errorMsg?.originalText == errorNoConnection) {
                errorMsgAction?.text = when (it.connectionSpeed) {
                    ConnectionSpeed.NO_CONNECTION -> actionSettings
                    else -> dialogButtonRetry
                }
            }
        })
    }

    fun noContentError(hideButtons: Boolean = false,
                       isDhTv: Boolean = false, isLocalZone: Boolean) {
        if (errorMessageView == null) {
            errorMessageView = LayoutInflater.from(context).inflate(R.layout.error_message, errorParentLayout, false)
            errorMsgIcon = errorMessageView?.findViewById<View>(R.id.connection_error_msg_icon) as ImageView
            errorMsg = errorMessageView?.findViewById(R.id.error_msg)
            errorMsgAction = errorMessageView?.findViewById(R.id.error_action)
            this.errorCode = errorMessageView?.findViewById(R.id.error_code_msg)
            errorMsg?.keepOriginalText(true)
            errorMsgAction?.keepOriginalText(true)
        }
        errorMsgIcon?.visibility = View.GONE
        val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
        layoutParams.gravity = Gravity.CENTER
        errorParentLayout.removeAllViews()
        errorParentLayout.addView(errorMessageView, layoutParams)
        setupNoContentScreen(false, true, isLocalZone)
        isErrorShown = true
    }


    fun showCustomError(error: BaseError?, showRetryOnNoContent: Boolean = false,resId :Int) {
        if (errorMessageView == null) {
            errorMessageView = LayoutInflater.from(context).inflate(R.layout.error_message, errorParentLayout, false)
            errorMsgIcon = errorMessageView?.findViewById<View>(R.id.connection_error_msg_icon) as ImageView
            errorMsg = errorMessageView?.findViewById(R.id.error_msg)
            errorMsgAction = errorMessageView?.findViewById(R.id.error_action)
            this.errorCode = errorMessageView?.findViewById(R.id.error_code_msg)
            errorMsg?.keepOriginalText(true)
            errorMsgAction?.keepOriginalText(true)
        }
        errorMsgIcon?.visibility = View.GONE
        val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
        layoutParams.gravity = Gravity.CENTER
        errorParentLayout.removeAllViews()
        errorParentLayout.addView(errorMessageView, layoutParams)
        setUpCustomErrorScreen(error,false, false,resId)
        isErrorShown = true
    }

    private fun setUpCustomErrorScreen(error: BaseError?, hideButtons: Boolean, dhTv: Boolean,
                                       resId :Int) {
        errorMsg?.text = error?.message ?: CommonUtils.getString(com.newshunt.common.util.R.string.error_no_content_msg)
        errorMsgIcon?.run {
            val drawableResId = resId
            visibility = View.VISIBLE
            setImageResource(drawableResId)
            setTag(drawableResId, IMG)
        }
        errorMsgAction?.run {
            text = dialogButtonRetry
            setOnClickListener(actionListener)
        }
    }

    @JvmOverloads
    fun showError(error: BaseError?, showRetryOnNoContent: Boolean = false,
                  error204Message : FullPageErrorMessage? = null, hideButtons: Boolean = false,
                  isDhTv: Boolean = false, isFromOnBoardingScreen : Boolean = false
                  ) {
        val errorCode = error?.dbgCode()
        val baseError = error
        var error = error?.message
        if (CommonUtils.isEmpty(error)) {
            error = Constants.EMPTY_STRING
        }
        if (errorMessageView == null) {
            errorMessageView = LayoutInflater.from(context).inflate(R.layout.error_message, errorParentLayout, false)
            if (contentLayoutParams != null) {
                errorMessageView?.findViewById<ViewGroup>(R.id.error_message_content_container)?.layoutParams = contentLayoutParams
            }
            errorHeaderContainer = errorMessageView?.findViewById<View>(R.id.error_header_container) as ViewGroup
            errorMsgIcon = errorMessageView?.findViewById<View>(R.id.connection_error_msg_icon) as ImageView
            errorMsg = errorMessageView?.findViewById(R.id.error_msg)
            errorMsgAction = errorMessageView?.findViewById(R.id.error_action)
            errorMsg?.keepOriginalText(true)
            errorMsgAction?.keepOriginalText(true)
            this.errorCode = errorMessageView?.findViewById(R.id.error_code_msg)
        }
        errorMsgIcon?.visibility = View.GONE
        this.errorCode?.text = errorCode?.get()
        val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
        layoutParams.gravity = Gravity.CENTER
        errorParentLayout.removeAllViews()
        errorParentLayout.addView(errorMessageView, layoutParams)
        when (errorCode) {
            is DbgCode.DbgNoConnectivityCode -> {
                setupConnectionScreen(baseError, hideButtons, isDhTv)
            }
            is DbgCode.DbgHttpCode -> {
                if (errorCode.get().equals("BB04")) {
                    setupContentScreen(baseError, showRetryOnNoContent, error204Message,
                            hideButtons, isDhTv, isFromOnBoardingScreen)
                } else {
                    setUpServerErrorScreen(baseError, hideButtons, isDhTv)
                }
            }
            is DbgCode.DbgBroswerServer, is DbgCode.DbgVersionedApiCorrupt, is DbgCode
            .DbgResponseErrorNull, is DbgCode.DbgErrorConnectivity, is DbgCode.DbgOnBoardingRequest -> {
                setUpServerErrorScreen(baseError, hideButtons, isDhTv)
            }

            is DbgCode.DbgUnexpectedCode, is DbgCode.DbgNotFoundInCache, is DbgCode
            .DbgBroswerGeneric-> {
                setUpGenericErrorScreen(baseError, hideButtons, isDhTv)
            }
            else -> {
                setupContentScreen(baseError, showRetryOnNoContent, error204Message,
                        hideButtons, isDhTv, isFromOnBoardingScreen)
            }
        }
        when (error) {
            savedArticleEmptyList -> error?.let { setupOfflineContentScreen() }
        }
        isErrorShown = true
    }


    fun hideError() {
        errorParentLayout.removeAllViews()
        isErrorShown = false
    }

    fun setUpServerErrorScreen(baseError: BaseError?, hideButtons: Boolean, dhTv: Boolean) {
        errorMsg?.text = baseError?.message
        errorMsgIcon?.run {
            val drawableResId = if (dhTv) {
                R.drawable.error_no_connection
            } else {
                CommonUtils.getResourceIdFromAttribute(context, R.attr.connection_error)
            }
            visibility = View.VISIBLE
            setImageResource(drawableResId)
            setTag(drawableResId, IMG)
        }
        errorMsgAction?.run {
            text = dialogButtonRetry
            setOnClickListener(actionListener)
            if (hideButtons) visibility = View.GONE
        }

    }

    private fun setupConnectionScreen(baseError: BaseError?, hideButtons: Boolean, dhTv: Boolean) {
        if (baseError != null) {
            errorMsg?.text = baseError.message
        }
        errorMsgIcon?.run {
            val drawableResId = if (dhTv) {
                R.drawable.error_no_connection
            } else {
                CommonUtils.getResourceIdFromAttribute(context, R.attr.connectivity_error)
            }
            visibility = View.VISIBLE
            setImageResource(drawableResId)
            setTag(drawableResId, IMG)
        }
        errorMsgAction?.run {
            text = if (connectivityData.value?.connectionSpeed == ConnectionSpeed.NO_CONNECTION
                    || !CommonUtils.isNetworkAvailable(CommonUtils.getApplication())) actionSettings else dialogButtonRetry
            setOnClickListener(actionListener)
            visibility = if (hideButtons) View.GONE
            else View.VISIBLE
        }
    }

    private fun setupNoContentScreen(hideButtons: Boolean, dhTv: Boolean, isLocalZone: Boolean) {

        errorMsg?.text = errorNoContent
        errorCode?.text = errorNoContentsub
        errorMsg?.setTextColor(context.resources.getColor(com.newshunt.common.util.R.color.white_color))
        errorCode?.setTextColor(context.resources.getColor(com.newshunt.common.util.R.color.white_color))
        errorMsg?.setTypeface(null, Typeface.BOLD)
        errorMsgIcon?.run {
            val drawableResId = if (dhTv) {
                R.drawable.ic_no_content_error
            } else {
                CommonUtils.getResourceIdFromAttribute(context, R.attr.connectivity_error)
            }
            visibility = View.VISIBLE
            setImageResource(drawableResId)
            setTag(drawableResId, IMG)
        }
        errorMsgAction?.run {
            text = errorNoContentAction
            if(isLocalZone) {
                setTextColor(context.resources.getColor(com.newshunt.common.util.R.color.black))
                this.setBackgroundColor(CommonUtils.getColor(com.newshunt.common.util.R.color.white_color))
            } else {
                setTextColor(context.resources.getColor(com.newshunt.common.util.R.color.black))
            }
            setOnClickListener(actionListener)
            setTypeface(null, Typeface.BOLD)
            visibility = if (hideButtons) View.GONE
            else View.VISIBLE
        }
    }

    private fun setUpGenericErrorScreen(error: BaseError?, hideButtons: Boolean, dhTv: Boolean) {
        errorMsg?.text = error?.message ?: CommonUtils.getString(com.newshunt.common.util.R.string.error_no_content_msg)
        errorMsgIcon?.run {
            val drawableResId = if (dhTv) {
                R.drawable.error_generic_dhtv_night
            } else {
                CommonUtils.getResourceIdFromAttribute(context, R.attr.bad_error)
            }
            visibility = View.VISIBLE
            setImageResource(drawableResId)
            setTag(drawableResId, IMG)
        }
        errorMsgAction?.run {
            text = dialogButtonRetry
            setOnClickListener(actionListener)
        }
    }

    private fun setupContentScreen(
            error: BaseError?,
            showRetryOnNoContent: Boolean,
            fullPageErrorMessage: FullPageErrorMessage? = null,
            hideButtons: Boolean,
            dhTv: Boolean,
            fromOnBoardingScreen: Boolean
    ) {
        // If it is story detail page of cards list, then we should not show message error
        // corresponding to news. Also show No Content error, only if error is empty.
        errorMsgIcon?.run {
            visibility = View.VISIBLE
            val drawableResId = if (dhTv) {
                R.drawable.no_content_found_dhtv_night
            } else {
                fullPageErrorMessage?.messageIcon?.let { icon ->
                    if (icon.isAttribute) CommonUtils.getResourceIdFromAttribute(context, icon.resId)
                    else icon.resId
                } ?: CommonUtils.getResourceIdFromAttribute(context, R.attr.content_error)
            }
            fullPageErrorMessage?.messageIcon?.scaleType?.let { scaleType = it }
            setImageResource(drawableResId)
            setTag(drawableResId, IMG)
        }
        errorMsgAction?.run {
            text = if (showRetryOnNoContent || fromOnBoardingScreen) dialogButtonRetry else btnHome
            setOnClickListener(actionListener)
            if (hideButtons) visibility = View.GONE
        }
        errorMsg?.text = fullPageErrorMessage?.message ?: (error?.message ?: CommonUtils.getString(
            com.newshunt.common.util.R
                .string.error_no_content_msg))
    }

    private fun setupOfflineContentScreen() {
        val message = savedArticleEmptyList
        errorMsg?.text = message
        errorMsgIcon?.run {
            val drawableResId = errorIcon(context, message)
            visibility = View.VISIBLE
            setImageResource(drawableResId)
            setTag(drawableResId, IMG)
        }
        errorMsgAction?.visibility = View.GONE
    }

    interface ErrorMessageClickedListener {
        fun onRetryClicked(view: View?)

        fun onNoContentClicked(view: View?)
    }

    companion object {
        const val IMG = "img"

        val nwSettingIntent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
        internal val errorConnectivity: String
            get() = CommonUtils.getString(com.newshunt.common.util.R.string.error_connectivity)
        internal val errorNoConnection: String
            get() = CommonUtils.getString(com.newshunt.common.util.R.string.error_no_connection)
        internal val errorNoConnectionSnackbar: String
            get() = CommonUtils.getString(com.newshunt.common.util.R.string.error_no_connection_snackbar)
        internal val errorServerIssue: String
            get() = CommonUtils.getString(com.newshunt.common.util.R.string.error_server_issue)
        internal val savedArticleEmptyList: String
            get() = CommonUtils.getString(com.newshunt.common.util.R.string.saved_article_empty_list)
        internal val noContentFound: String
            get() = CommonUtils.getString(com.newshunt.common.util.R.string.no_content_found)
        internal val errorGeneric: String
            get() = CommonUtils.getString(com.newshunt.common.util.R.string.error_generic)
        internal val noConnectionError: String
            get() = CommonUtils.getString(com.newshunt.common.util.R.string.no_connection_error)
        internal val errorNoContentMsg: String
            get() = CommonUtils.getString(com.newshunt.common.util.R.string.error_no_content_msg)
        internal val errorNoContentMsgSnackbar: String
            get() = CommonUtils.getString(com.newshunt.common.util.R.string.error_no_content_msg_snackbar)
        internal val offlineSavingFailed: String
            get() = CommonUtils.getString(com.newshunt.common.util.R.string.offline_saving_failed)
        internal val dialogButtonRetry: String
            get() = CommonUtils.getString(com.newshunt.common.util.R.string.dialog_button_retry)
        internal val btnHome: String
            get() = CommonUtils.getString(com.newshunt.common.util.R.string.btn_home)
        internal val actionSettings: String
            get() = CommonUtils.getString(com.newshunt.common.util.R.string.action_settings)
        internal val errorNoContent: String
            get() = CommonUtils.getString(com.newshunt.common.util.R.string.no_content_error_title)
        internal val errorNoContentsub: String
            get() = CommonUtils.getString(com.newshunt.common.util.R.string.no_content_error_sub_title)
        internal val errorNoContentAction: String
            get() = CommonUtils.getString(com.newshunt.common.util.R.string.no_content_error_action)

        /**
         *
         * action will be decided based on [message]
         * [forceToast] is for testing; currently only snackbar is required
         */
        @JvmStatic
        @JvmOverloads
        fun showErrorSnackbar(
                view: View,
                message: String,
                listener: ErrorMessageClickedListener? = null,
                duration: Int = Snackbar.LENGTH_LONG,
                forceToast: Boolean = false, lifecycleOwner: LifecycleOwner? = null,
                baseError: BaseError? = null) {
            try {
                if (forceToast) {
                    Toast.makeText(view.context, message, Toast.LENGTH_SHORT).show()
                } else {

                    val action = getCTASnackbar(message)
                    val connectivityData: LiveData<ConnectionSpeedEvent> = AndroidUtils.connectionSpeedLiveData
                    var baseError: BaseError? = baseError
                    var snackbarMessage: String?
                    when (message) {
                        errorNoContentMsg, noContentFound -> {
                            snackbarMessage = errorNoContentMsgSnackbar
                            if (baseError == null)
                                baseError = BaseError(DbgCode.DbgHttpCode(HttpURLConnection.HTTP_NO_CONTENT), message)
                        }
                        errorNoConnection -> {
                            snackbarMessage = errorNoConnectionSnackbar
                            if (baseError == null)
                                baseError = BaseErrorBuilder.getBaseError(errorNoConnection, Constants
                                        .ERROR_NO_INTERNET)
                        }
                        else -> {
                            snackbarMessage = message
                        }
                    }
                    var dbgcode = baseError?.dbgCode()?.get()
                    var spannableMessage: SpannableString? = null
                    var baseString: String? = null
                    if (dbgcode != null) {
                        baseString = "$snackbarMessage"
                        val convertedString = FontHelper.getFontConvertedString(baseString)
                        spannableMessage = SpannableString("$convertedString $dbgcode")
                        spannableMessage.setSpan(RelativeSizeSpan(0.7f), convertedString.length + 1,
                                spannableMessage.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                        spannableMessage.setSpan(ForegroundColorSpan(CommonUtils.getColor(com.newshunt.common.util.R.color.white_color)),
                                convertedString.length + 1, spannableMessage.length, Spannable
                                .SPAN_EXCLUSIVE_EXCLUSIVE)
                    }
                    if (action != null && listener != null) {
                        val snackbar = GenericCustomSnackBar.showSnackBar(view, view.context,
                                snackbarMessage,
                                duration, actionType(action), listener, action, spannableString =
                        spannableMessage, originalString = baseString)
                        snackbar.show()
                        if (lifecycleOwner != null) {
                            connectivityData.observe(lifecycleOwner, Observer {
                                if (snackbarMessage.equals(errorNoConnectionSnackbar) && duration ==
                                        Snackbar.LENGTH_INDEFINITE)
                                    if (it.connectionSpeed != ConnectionSpeed.NO_CONNECTION) {
                                        snackbar.dismiss()
                                    }
                            })
                        }


                    } else {
                        GenericCustomSnackBar.showSnackBar(view, view.context, snackbarMessage, Snackbar
                                .LENGTH_SHORT, spannableString = spannableMessage, originalString = baseString)
                                .show()
                    }
                }
            } catch (exception: Exception) {
                Logger.caughtException(exception)
            }
        }

        fun errorIcon(context: Context, message: String) = when (message) {
            errorConnectivity,
            errorNoConnection -> CommonUtils.getResourceIdFromAttribute(context, R.attr.connectivity_error)
            errorServerIssue -> CommonUtils.getResourceIdFromAttribute(context, R.attr.connection_error)
            savedArticleEmptyList -> CommonUtils.getResourceIdFromAttribute(context, R.attr.no_saved_artcles)
            errorGeneric -> CommonUtils.getResourceIdFromAttribute(context, R.attr.bad_error)
            errorNoContentMsg, noContentFound -> CommonUtils.getResourceIdFromAttribute(context, R.attr.content_error)
            else -> CommonUtils.getResourceIdFromAttribute(context, R.attr.content_error)
        }

        fun getCTA(errorMessage: String) = when (errorMessage) {
            errorConnectivity, errorNoConnection, errorGeneric, errorServerIssue -> dialogButtonRetry
            noContentFound, errorNoContentMsg -> btnHome
            else -> null
        }

        fun getCTASnackbar(errorMessage: String) = when (errorMessage) {
            errorConnectivity, errorNoConnection, noConnectionError, offlineSavingFailed -> dialogButtonRetry
            noContentFound, errorNoContentMsg -> btnHome
            else -> null
        }

        private fun actionType(action: String) = when (action) {
            dialogButtonRetry -> ActionType.Retry
            btnHome -> ActionType.Home
            else -> null
        }
    }

    enum class ActionType { Retry, Home }
}
