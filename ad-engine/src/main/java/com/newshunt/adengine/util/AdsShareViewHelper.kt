package com.newshunt.adengine.util

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.os.bundleOf
import com.newshunt.adengine.R
import com.newshunt.adengine.client.AsyncAdImpressionReporter
import com.newshunt.adengine.model.entity.BaseDisplayAdEntity
import com.newshunt.adengine.model.entity.Shareability
import com.newshunt.adengine.model.entity.version.AdPosition
import com.newshunt.analytics.client.AnalyticsClient
import com.newshunt.analytics.entity.NhAnalyticsAppEvent
import com.newshunt.common.helper.common.AndroidUtils
import com.newshunt.common.helper.common.BusProvider
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.DataUtil
import com.newshunt.common.helper.common.FileUtil
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.common.ViewUtils
import com.newshunt.common.helper.font.FontHelper
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.common.helper.share.ImageSaveCallback
import com.newshunt.common.helper.share.ImageSaveTask
import com.newshunt.common.helper.share.ShareContent
import com.newshunt.common.helper.share.ShareFactory
import com.newshunt.common.helper.share.ShareUi
import com.newshunt.dataentity.analytics.entity.AnalyticsParam
import com.newshunt.dataentity.analytics.entity.NhAnalyticsEventParam
import com.newshunt.dataentity.analytics.referrer.NhGenericReferrer
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.analytics.section.NhAnalyticsEventSection
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.common.helper.share.ShareApplication
import com.newshunt.dataentity.common.model.entity.AppExitEvent
import com.newshunt.dhutil.helper.AppSettingsProvider
import com.newshunt.dhutil.helper.preference.AppStatePreference
import com.newshunt.dhutil.helper.theme.ThemeUtils
import com.newshunt.news.viewmodel.SocialInteractionViewModel
import com.newshunt.onboarding.model.entity.datacollection.InstalledAppInfo
import com.squareup.otto.Subscribe
import java.io.File
import java.lang.ref.SoftReference

const val AD_SHARE_ICON_DEFAULT = 1
const val AD_SHARE_ICON_PGI = 2

class AdsShareViewHelper(private val context: Context?) :
        ImageSaveCallback {
    private var iconType: Int? = AD_SHARE_ICON_DEFAULT

    var sharePackageName: String? = null
    private var progressDialog: ProgressDialog? = null
    private var imageSaveTask: ImageSaveTask? = null
    private var baseDisplayAdEntity: BaseDisplayAdEntity? = null

    fun setAdEntity(adEntity: BaseDisplayAdEntity?) {
        this.baseDisplayAdEntity = adEntity
        if (adEntity?.adPosition == AdPosition.PGI) {
            iconType = AD_SHARE_ICON_PGI
        }
    }

    fun getShareIconResource(): Int {
        sharePackageName?.let {
            return getWhatsappIcon()
        }
        sharePackageName =
            if (AndroidUtils.isAppInstalled(ShareApplication.WHATS_APP_PACKAGE.packageName)) {
                ShareApplication.WHATS_APP_PACKAGE.packageName
            } else {
                null
            }
        return if (sharePackageName == null) R.drawable.ic_share else getWhatsappIcon()
    }

    fun getSharableAppIcon(): Drawable{
        val selectedAppToShare = PreferenceManager.getPreference(AppStatePreference.SELECTED_APP_TO_SHARE,Constants.EMPTY_STRING)
        val selectedApp: InstalledAppInfo? = AppSettingsProvider.preferredSharableAppLiveData.value
        if(!selectedAppToShare.isNullOrEmpty() && AndroidUtils.isAppInstalled(selectedAppToShare) && selectedApp != null) {
            sharePackageName = selectedApp.packageName
            return selectedApp.icon
        } else if (AndroidUtils.isAppInstalled(ShareApplication.WHATS_APP_PACKAGE.packageName)) {
            sharePackageName = ShareApplication.WHATS_APP_PACKAGE.packageName
            return CommonUtils.getDrawable(getWhatsappIcon())
        }
        sharePackageName = null
        val tintColor = if (ThemeUtils.isNightMode()) R.color.white else R.color.black
        return CommonUtils.getTintedDrawable(R.drawable.ic_share, tintColor)
    }

    private fun getWhatsappIcon(): Int {
        return if (iconType == AD_SHARE_ICON_PGI) {
            R.drawable.whatsapp_with_fill
        } else if(ThemeUtils.isNightMode()){
            R.drawable.ic_whatsapp_new_night
        } else {
            R.drawable.ic_whatsapp_new
        }
    }

    fun onShareIconClick(view: View, adEntity:BaseDisplayAdEntity?, vm: SocialInteractionViewModel? = null) {
        adEntity?.let {
            onShareIconClick(it.shareability)

            val args = bundleOf(
                Constants.BUNDLE_SHARE_PACKAGE_NAME to sharePackageName,
                Constants.SHARE_UI_TYPE to ShareUi.ONCARD
            )
            it.contentAsset?.let { asset ->
                vm?.onShareClick(view, asset, args)
            }
        }
    }

    fun onShareIconClick(shareability: Shareability?) {
        context?.let {
            if (!CommonUtils.isNetworkAvailable(context)) {
                FontHelper.showCustomFontToast(context, CommonUtils.getString(
                        R.string
                            .error_no_connection
                    ), Toast.LENGTH_SHORT)
                return
            }
            shareability?.let {
                if (!DataUtil.isEmpty(it.image)) {
                    val downloadPath =
                        CommonUtils.getCacheDir(Constants.DH_SHARE_HIDDEN_DIR).absolutePath +
                                Constants.FORWARD_SLASH +
                                FileUtil.getHashCodeBasedFileName(it.image)
                    if (ShareContentController.getInstance().isFileDownloaded(downloadPath)) {
                        val file = File(downloadPath)
                        shareContent(Uri.fromFile(file))
                    } else {
                        imageSaveTask = ImageSaveTask(SoftReference(context), this)
                        imageSaveTask?.execute(it.image, downloadPath)
                    }

                } else {
                    shareContent()
                }
            }
        }
    }


    override fun onStart() {
        //TODO :: Since this is deperecated will require to remove @AC.
        progressDialog = ProgressDialog(context)
        progressDialog?.let {
            it.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
            it.isIndeterminate = true
            it.setProgressPercentFormat(null)
            it.setProgressNumberFormat(null)
            it.setTitle("Downloading")
            it.setCancelable(true)
            it.setOnCancelListener { imageSaveTask?.clear() }
            it.show()
        }
    }

    override fun onDownloaded(filePath: String, failed: Boolean) {
        imageSaveTask?.clear()
        progressDialog?.let {
            if (it.isShowing) {
                it.dismiss()
            }
        }

        if (failed || DataUtil.isEmpty(filePath)) {
            shareContent()
            return
        }

        filePath.let {
            ShareContentController.getInstance().setFileDownloaded(it)
            shareContent(FileUtil.getFileUri(context, it))
        }
        return
    }

    override fun onError(e: Throwable?) {
        imageSaveTask?.clear()
        progressDialog?.let {
            if (it.isShowing) {
                it.dismiss()
            }
        }
        context?.let {
            FontHelper.showCustomFontToast(it, CommonUtils.getString(R.string.error_image_save_failed),
                    Toast.LENGTH_SHORT)
        }
        Logger.caughtException(e)
    }

    private fun shareContent(fileUri: Uri? = null) {
        if (baseDisplayAdEntity?.shareability == null) {
            return
        }
        val sendIntent = Intent()
        sendIntent.type = Constants.INTENT_TYPE_TEXT
        sendIntent.action = Intent.ACTION_SEND
        val shareContent = ShareContent()
        fileUri?.let {
            shareContent.fileUri = it
        }

        baseDisplayAdEntity?.shareability?.let {
            shareContent.title = it.text
            shareContent.subject = it.subject
            shareContent.isDisplayViaDailyhunt = true
        }


        val shareHelper = ShareFactory.getShareHelper(sharePackageName, context as Activity, sendIntent,
                shareContent, true, true)
        shareHelper.share()
        logShareAnalytics()
    }

    private fun logShareAnalytics() {
        baseDisplayAdEntity?.let {
            AsyncAdImpressionReporter(it).hitTrackerUrl(it.adShareBeaconUrl)

            val map = mapOf<NhAnalyticsEventParam, Any?>(
                AnalyticsParam.CARD_TYPE to it.adPosition?.value,
                AnalyticsParam.ITEM_ID to it.aduid,
                AnalyticsParam.ITEM_CATEGORY_ID to it.campaignId
            )
            AnalyticsClient.log(
                NhAnalyticsAppEvent.STORY_SHARED, NhAnalyticsEventSection.ADS, map,
                PageReferrer(NhGenericReferrer.STORY_CARD, it.id)
            )
        }
    }

}


private class ShareContentController {
    var downloadedFiles: Set<String>? = null
    private val handler: Handler = Handler(Looper.getMainLooper())

    init {
        handler.post {
            BusProvider.getUIBusInstance().register(this)
        }
    }

    fun isFileDownloaded(filePath: String): Boolean {
        if (downloadedFiles == null) {
            return false
        }
        return downloadedFiles?.contains(filePath) ?: false
    }

    fun setFileDownloaded(filePath: String) {
        if (downloadedFiles == null) {
            downloadedFiles = emptySet()
        }
        downloadedFiles?.plus(filePath)
    }

    companion object {
        private var mInstance: ShareContentController? = null
        private const val LOG_CONTENT_CONTROLLER = "AdsShareContentController"
        @JvmStatic
        fun getInstance(): ShareContentController {
            if (mInstance == null) {
                mInstance = ShareContentController()
            }
            return mInstance!!
        }
    }

    @Subscribe
    fun onExit(appExitEvent: AppExitEvent) {
        CommonUtils.runInBackground {
            val f = CommonUtils.getCacheDir(Constants.DH_SHARE_HIDDEN_DIR)
            if (f != null) {
                Logger.d(LOG_CONTENT_CONTROLLER, "Cache directory removed")
                f.deleteRecursively()
            }
            handler.post {
                BusProvider.getUIBusInstance().unregister(this)
                mInstance = null
                downloadedFiles = null
            }
        }
    }
}

fun setupPgiIconPosition(ctaButton: View?, shareIcon: View?, isFullScreen: Boolean) {
    if (shareIcon == null) {
        return
    }

    val parent = shareIcon.parent as? ConstraintLayout ?: return
    ViewUtils.setMissingViewIds(parent)
    val cs = ConstraintSet()
    cs.clone(parent)
    if (isFullScreen) {
        cs.connect(shareIcon.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet
                .BOTTOM, CommonUtils.getDimension(R.dimen.ad_share_full_screen_pgi_icon_margin_bottom))
        cs.connect(shareIcon.id, ConstraintSet.RIGHT, ConstraintSet.PARENT_ID, ConstraintSet
                .RIGHT, CommonUtils.getDimension(R.dimen.ad_share_full_screen_pgi_icon_margin_right))
    } else {
        cs.connect(shareIcon.id, ConstraintSet.BOTTOM, ctaButton?.id ?: ConstraintSet.PARENT_ID,
                ConstraintSet.TOP, CommonUtils.getDimension(R.dimen.ad_share_native_pgi_icon_margin_bottom))
        cs.connect(shareIcon.id, ConstraintSet.RIGHT, ConstraintSet.PARENT_ID, ConstraintSet
                .RIGHT, CommonUtils.getDimension(R.dimen.ad_share_native_pgi_icon_margin_right))
    }
    cs.applyTo(parent)
}