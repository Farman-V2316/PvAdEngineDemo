/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */
package com.newshunt.dhutil.view

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.newshunt.common.helper.common.AndroidUtils
import com.newshunt.common.view.customview.fontview.NHTextView
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.sdk.network.connection.ConnectionSpeed
import com.newshunt.sdk.network.connection.ConnectionSpeedEvent

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

    interface ErrorMessageClickedListener {
        fun onRetryClicked(view: View?)

        fun onNoContentClicked(view: View?)
    }

    companion object {
        const val IMG = "img"

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

    }

    enum class ActionType { Retry, Home }
}
