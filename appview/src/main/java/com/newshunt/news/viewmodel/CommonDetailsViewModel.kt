/*
 * Copyright (c) 2020 Newshunt. All rights reserved.
 */
package com.newshunt.news.viewmodel

import android.app.Application
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.newshunt.analytics.entity.DialogBoxType
import com.newshunt.appview.R
import com.newshunt.appview.common.ui.helper.NavigationEvent
import com.newshunt.appview.common.ui.helper.NavigationHelper
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.font.FontHelper
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.analytics.section.NhAnalyticsEventSection
import com.newshunt.dataentity.common.asset.AssetType2
import com.newshunt.dataentity.common.asset.CommonAsset
import com.newshunt.news.helper.toMinimizedCommonAsset
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.news.analytics.NewsReferrer
import com.newshunt.dataentity.social.entity.MenuLocation
import com.newshunt.dhutil.analytics.DialogAnalyticsHelper
import com.newshunt.dhutil.bundleOf
import com.newshunt.news.model.usecase.MediatorUsecase
import com.newshunt.socialfeatures.util.SocialFeaturesConstants.COMMENT_TYPE_MAIN
import com.newshunt.socialfeatures.util.SocialFeaturesConstants.COMMENT_TYPE_REPLY
import java.io.Serializable

/**
 * Created by karthik.r on 2020-02-12.
 */
open class CommonDetailsViewModel(context: Application,
                                  private val section: String,
                                  private val postId: String,
                                  private val referrerFlow: PageReferrer?,
                                  private val deleteCommentUC: MediatorUsecase<Bundle, Boolean>,
                                  private val reportCommentUsecase: MediatorUsecase<Bundle, String?>)
    : AndroidViewModel(context) {

    var pageReferrer : PageReferrer = PageReferrer(NewsReferrer.STORY_DETAIL, postId)
    val deletePostData = deleteCommentUC.data()

    fun getParentReferrer(parent: CommonAsset?) : PageReferrer {
        if (parent?.i_type() == AssetType2.COMMENT.name) {
            PageReferrer(NewsReferrer.COMMENT_DETAIL, postId)
        }

        return pageReferrer
    }

    fun deleteComment(view: android.view.View, item: CommonAsset?, isReply : Boolean) {
        deleteComment(view, item, false, isReply)
    }

    fun deleteComment(view: android.view.View, item: CommonAsset?, isPrimaryContent: Boolean, isReply : Boolean) {
        if (item == null) {
            FontHelper.showCustomFontToast(view.context,
                    CommonUtils.getString(R.string.error_generic), android.widget.Toast.LENGTH_SHORT)
            return
        }

        val title = CommonUtils.getString(R.string.delete_comment_dialog_title)
        val message = CommonUtils.getString(R.string.delete_comment_dialog_msg)
        showConfirmDialog(view, title, message, object : com.newshunt.dhutil.view.DhDialogListener {
            override fun onDialogPositiveClick() {
                val eventItemType = if (isReply) COMMENT_TYPE_REPLY else COMMENT_TYPE_MAIN
                executeDeleteComment(item.i_id(), eventItemType, isPrimaryContent, item.i_type())
            }

            override fun onDialogNegativeClick() {
                DialogAnalyticsHelper.logDialogBoxActionEvent(DialogBoxType.DELETE_COMMENT, pageReferrer,
                        DialogAnalyticsHelper.DIALOG_ACTION_CANCEL,
                        NhAnalyticsEventSection.getSection(section), null)
            }
        })
    }

    fun reportComment(view: android.view.View, item: CommonAsset?) {
        if (item == null) {
            FontHelper.showCustomFontToast(view.context,
                    CommonUtils.getString(R.string.error_generic), android.widget.Toast.LENGTH_SHORT)
            return
        }

        val title = CommonUtils.getString(R.string.report_comment_dialog_title)
        val message = CommonUtils.getString(com.newshunt.appview.R.string.report_comment_dialog_msg)

        DialogAnalyticsHelper.logDialogBoxViewedEvent(DialogBoxType.REPORT_SPAM_COMMENT, pageReferrer,
                NhAnalyticsEventSection.getSection(section), null)
        showConfirmDialog(view, title, message, object : com.newshunt.dhutil.view.DhDialogListener {
            override fun onDialogPositiveClick() {
                executeReportComment(item)
            }

            override fun onDialogNegativeClick() {
                DialogAnalyticsHelper.logDialogBoxActionEvent(DialogBoxType.REPORT_STORY, pageReferrer,
                        DialogAnalyticsHelper.DIALOG_ACTION_CANCEL,
                        NhAnalyticsEventSection.getSection(section), null)
            }
        })
    }

    val reportCommentTriggered = MutableLiveData(false)

    fun executeReportComment(comment: CommonAsset) {
        reportCommentTriggered.postValue(true)
        reportCommentUsecase.execute(bundleOf(
                Constants.BUNDLE_POST_ID to comment.i_id(),
                Constants.REFERRER to pageReferrer))
    }

    private fun showConfirmDialog(view: android.view.View, title: String, message: String, listener: com.newshunt.dhutil.view.DhDialogListener) {
        val dialogDetail = com.newshunt.dataentity.dhutil.model.entity.asset.DialogDetail()
        dialogDetail.title = title
        dialogDetail.message = message
        dialogDetail.positiveButtonText = CommonUtils.getString(com.newshunt.appview.R.string.dialog_yes)
        dialogDetail.negativeButtonText = CommonUtils.getString(com.newshunt.appview.R.string.dialog_no)
        var fragmentManager: androidx.fragment.app.FragmentManager? = null
        if (view.context is androidx.appcompat.app.AppCompatActivity) {
            fragmentManager = (view.context as androidx.appcompat.app.AppCompatActivity).supportFragmentManager
        } else if (view.context is android.view.ContextThemeWrapper) {
            val themeWrapper = view.context as android.view.ContextThemeWrapper
            fragmentManager = (themeWrapper.baseContext as androidx.appcompat.app.AppCompatActivity).supportFragmentManager
        } else {
            return
        }
        com.newshunt.dhutil.view.DhDialogFragment.showDialog(fragmentManager, dialogDetail, listener)
    }

    fun onProfileViewClick(view: android.view.View, id: String?, handle: String?) {
        if (id == null && handle == null) {
            FontHelper.showCustomFontToast(view.context,
                    CommonUtils.getString(com.newshunt.appview.R.string.error_generic), android.widget.Toast.LENGTH_SHORT)
            return
        }

        com.newshunt.deeplink.navigator.CommonNavigator.launchProfileActivity(view.context, com
                .newshunt.dataentity.model.entity.UserBaseProfile().apply {
            this.userId = id ?: ""
            this.handle = handle
        }, PageReferrer(NewsReferrer.WIDGET_CARD))
    }


    fun executeDeleteComment(commentId: String, eventItemType: String, isPrimaryContent: Boolean?,
                             itemType: String) {
        deleteCommentUC.execute(bundleOf(
                Constants.BUNDLE_POST_ID to commentId,
                Constants.EVENT_ITEM_TYPE to eventItemType,
                Constants.ITEM_TYPE to itemType,
                Constants.BUNDLE_IS_PRIMARY_CONTENT to isPrimaryContent,
                Constants.REFERRER to pageReferrer))
    }
}