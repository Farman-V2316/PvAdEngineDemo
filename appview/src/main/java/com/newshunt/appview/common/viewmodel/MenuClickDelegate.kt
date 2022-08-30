/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.appview.common.viewmodel

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.google.gson.Gson
import com.newshunt.analytics.client.AnalyticsClient
import com.newshunt.analytics.entity.NhAnalyticsAppEvent
import com.newshunt.appview.R
import com.newshunt.appview.common.postcreation.view.customview.PostDeleteDialog
import com.newshunt.appview.common.profile.helper.makeBookmarkApiPostBody
import com.newshunt.appview.common.ui.fragment.MenuFragment
import com.newshunt.appview.common.ui.helper.CardsBindUtils
import com.newshunt.appview.common.ui.helper.NavigationEvent
import com.newshunt.appview.common.ui.helper.NavigationHelper
import com.newshunt.appview.common.video.utils.DownloadRequestData
import com.newshunt.appview.common.video.utils.DownloadUtils
import com.newshunt.common.helper.common.AndroidUtils
import com.newshunt.common.helper.common.BusProvider
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.common.UrlUtil
import com.newshunt.common.helper.font.FontHelper
import com.newshunt.common.helper.info.ClientInfoHelper
import com.newshunt.common.helper.preference.GenericAppStatePreference
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.common.helper.share.ShareContent
import com.newshunt.common.helper.share.ShareFactory
import com.newshunt.common.helper.share.ShareUi
import com.newshunt.common.model.usecase.ShareUsecase
import com.newshunt.common.view.customview.CommonMessageDialogOptions
import com.newshunt.dataentity.analytics.entity.AnalyticsParam
import com.newshunt.dataentity.analytics.entity.NhAnalyticsAppEventParam
import com.newshunt.dataentity.analytics.entity.NhAnalyticsEventParam
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.analytics.section.NhAnalyticsEventSection
import com.newshunt.dataentity.common.asset.*
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.common.model.entity.PermissionResult
import com.newshunt.dataentity.common.model.entity.ShareContentType
import com.newshunt.dataentity.common.pages.FollowActionType
import com.newshunt.dataentity.common.pages.PageEntity
import com.newshunt.dataentity.common.pages.SourceFollowBlockEntity
import com.newshunt.dataentity.model.entity.BookMarkAction
import com.newshunt.dataentity.model.entity.BookmarkList
import com.newshunt.dataentity.model.entity.GroupInfo
import com.newshunt.dataentity.model.entity.ProfileTabType
import com.newshunt.dataentity.model.entity.UserBaseProfile
import com.newshunt.dataentity.news.analytics.NewsReferrer
import com.newshunt.dataentity.news.model.entity.MenuL1PostClkAction
import com.newshunt.dataentity.social.entity.MenuL1
import com.newshunt.dataentity.social.entity.MenuL1Id
import com.newshunt.dataentity.social.entity.MenuLocation
import com.newshunt.dataentity.social.entity.MenuOption
import com.newshunt.dataentity.social.entity.MenuPayload
import com.newshunt.deeplink.navigator.CommonNavigator
import com.newshunt.deeplink.navigator.NhBrowserNavigator
import com.newshunt.dhutil.BlurTransformation
import com.newshunt.dhutil.analytics.AnalyticsHelper2
import com.newshunt.dhutil.analytics.ExploreButtonType
import com.newshunt.dhutil.helper.autoplay.AutoPlayHelper
import com.newshunt.dhutil.helper.common.DailyhuntConstants
import com.newshunt.dhutil.helper.common.DefaultRationaleProvider
import com.newshunt.dhutil.helper.preference.UserPreferenceUtil
import com.newshunt.helper.ImageUrlReplacer
import com.newshunt.news.analytics.NhAnalyticsAppState
import com.newshunt.news.helper.ImageShareHelper
import com.newshunt.news.helper.ImageShareHelperCallback
import com.newshunt.news.helper.NHJsInterfaceWithMenuClickHandling
import com.newshunt.news.model.usecase.FollowFromMenuUsecase
import com.newshunt.news.model.usecase.MediatorUsecase
import com.newshunt.news.model.usecase.PostL1Usecase
import com.newshunt.news.util.NewsConstants
import com.newshunt.news.view.customview.FontSizeChangeView2
import com.newshunt.permissionhelper.PermissionAdapter
import com.newshunt.permissionhelper.PermissionHelper
import com.newshunt.permissionhelper.utilities.Permission
import com.newshunt.sdk.network.image.Image
import com.newshunt.sdk.network.internal.NetworkSDKUtils
import com.newshunt.sso.SSO
import com.newshunt.viral.helper.VHShareHelper
import com.squareup.otto.Subscribe
import java.io.Serializable
import java.lang.ref.WeakReference
import java.util.HashMap
import javax.inject.Inject
import javax.inject.Named

class MenuClickDelegate @Inject constructor(private val location: MenuLocation,
                                            @Named("followUsecase")
                                            private val followUsecase: MediatorUsecase<Bundle, Boolean>,
                                            @Named("postL1")
                                            private val postL1: MediatorUsecase<Bundle, Boolean>,
                                            @Named("dislikeUsecase")
                                            private val dislikeUsecase: MediatorUsecase<Bundle, Boolean>,
                                            @Named("hideUsecase")
                                            private val hidePostUsecase: MediatorUsecase<Bundle, Boolean>,
                                            @Named("deletePostUsecase")
                                            private val deletePostUsecase: MediatorUsecase<Bundle, Boolean>,
                                            @Named("shareUsecase")
                                            private val shareUsecase: MediatorUsecase<Bundle, Boolean>,
                                            @Named("arguments")
                                            private val arguments: Bundle,
                                            @Named("saveUnsaveUsecase")
                                            private val saveUnsavePostUsecase:
                                            MediatorUsecase<BookmarkList, Boolean>,
                                            @Named("reportPostUsecase")
                                            private val reportPostUsecase: MediatorUsecase<Bundle, Boolean>,
                                            @Named("uniqueScreenId")
                                            private val uniqueScreenId: Int,
                                            @Named("targetNavigationId")
                                            private val targetNavId : Long,
                                            @Named("followBlockUpdateMediatorUC")
                                            private val followBlockUpdateUsecase:MediatorUsecase<SourceFollowBlockEntity?,Boolean>) : MenuClickHandlingViewModel {
    private var storagePermissionHelper: PermissionHelper? = null

    val deletePostData = deletePostUsecase.data()

    override fun onMenuL1OptionClick(view: View,
                                     menuOption: MenuOption,
                                     asset: CommonAsset?,
                                     pageEntity: PageEntity?,
                                     activity: Activity?) {
        super.onMenuL1OptionClick(view, menuOption, asset, pageEntity, activity)
        val actionValue = menuOption.menuL1.postAction ?: MenuL1PostClkAction.NA.name
        val action = MenuL1PostClkAction.valueOf(actionValue)
        arguments.putSerializable(Constants.BUNDLE_STORY, asset as? Serializable)
        arguments.putString(Constants.BUNDLE_L1_IDS, menuOption.menuL1.id)
        val referrer = arguments.getSerializable(Constants.REFERRER) as? PageReferrer

        logEventsForSelectedL1(menuOption, asset)
        val menuPayload = asset.toMenuPayload(location, menuOption.menuL1, referrer)
        if (menuOption.menuL1.clickAction == "POST" && menuPayload != null) {
            postL1.execute(PostL1Usecase.create(menuPayload))
        }
        if (menuOption.menuL1.isDislike == true) {

            dislikeUsecase.execute(arguments)
        }

        if (menuOption.menuL1.hideCard == true) {
            val arg=Bundle()
            arg.putString(MenuFragment.ARG_JS_ACTION_KEY,MenuFragment.ARG_JS_ACTION_HIDE_STORY)
            arg.putAll(arguments)
            NHJsInterfaceWithMenuClickHandling.JS_MENU_ACTION.postValue(arg)
            hidePostUsecase.execute(arguments)
        }

        val menuL2 = menuOption.menuL2
        when {
            action == MenuL1PostClkAction.ADD_COMMENT -> {
                handleComment(asset, activity, referrer)
            }
            action == MenuL1PostClkAction.BROWSER -> {
                handleBrowser(activity, menuOption.menuL1.browserUrl)
            }
            action == MenuL1PostClkAction.BROWSER_SOURCE -> {
                handleVhSourceBrowser(asset, activity, targetNavId)
            }
            action == MenuL1PostClkAction.BROWSE_BY_SOURCE -> {
                handleBrowseBySource(asset, activity,arguments, targetNavId)
            }
            action == MenuL1PostClkAction.ENABLE_NSFW_FILTER -> {
                handleNsfwToggle()
            }
            action == MenuL1PostClkAction.SHARE -> {

                if (location == MenuLocation.HASHTAG) {
                    handlePageEntityShare(pageEntity, activity)
                    pageEntity?.let {
                        shareUsecase.execute(ShareUsecase.args(it.id, pageEntity.entityType, postSourceAsset = asset?.i_source(), sourceLang = asset?.i_langCode()))
                    }
                    pageEntity?.let {
                        AnalyticsHelper2.logStorySharedEvent(null, ShareUi.CARD_MENU, it, referrer,
                                NhAnalyticsEventSection.NEWS)
                    }
                } else {
                    val section = AnalyticsHelper2.getSection(arguments.getString(NewsConstants.DH_SECTION, Constants.EMPTY_STRING))
                    val groupInfo = arguments.getSerializable(Constants.BUNDLE_GROUP_INFO) as? GroupInfo
                    AnalyticsHelper2.logStorySharedEvent(null, ShareUi.CARD_MENU, asset, referrer,
                            NhAnalyticsEventSection.NEWS, groupInfo)
                    handleShare(asset, activity)
                    asset?.let {
                        shareUsecase.execute(ShareUsecase.args(it.i_id(), "POST", parentId = it.i_parentPostId(), postSourceAsset = it.i_source(), sourceLang = it.i_langCode()))
                    }
                }
            }

            action == MenuL1PostClkAction.ENABLE_AUTOPLAY -> {
                handleToggleAutoPlay(true)
            }

            action == MenuL1PostClkAction.DISABLE_AUTOPLAY -> {
                handleToggleAutoPlay(false)
            }

            action.isBlock() -> {
                handleFollowAction(asset, pageEntity, FollowActionType.BLOCK)
            }

            action.isUnBlock() -> {
                handleFollowAction(asset, pageEntity, FollowActionType.UNBLOCK)
            }

            action == MenuL1PostClkAction.FOLLOW -> {
                handleFollowAction(asset, pageEntity, FollowActionType.FOLLOW)
            }

            action == MenuL1PostClkAction.CHANGE_FONT -> {
                handleFontSizeChange(activity)
            }

            action == MenuL1PostClkAction.SAVE -> {
                handleSavePostAction(saveUnsavePostUsecase, asset, activity, menuOption.menuL1.id == MenuL1Id.L1_SAVE_VIDEO.name)
            }

            action == MenuL1PostClkAction.DOWNLOAD_VIDEO -> {
                handleVideoDownload(activity, asset)
            }

            action == MenuL1PostClkAction.UNSAVE -> {
                handleUnSavePostAction(saveUnsavePostUsecase, asset, activity, menuOption.menuL1.id == MenuL1Id.L1_SAVE_VIDEO.name)
            }
            action == MenuL1PostClkAction.DELETE_POST -> {
                handleDeletePostAction(asset, activity, uniqueScreenId, arguments)
            }

            action == MenuL1PostClkAction.REPORT_POST -> {
                handleReportPostAction(arguments)
            }

            else -> {
                Logger.e(LOG_TAG, "Unhandled option click action")
            }
        }

        if (menuL2 != null) {
            val intent = NhBrowserNavigator.getTargetIntent().apply {
                putExtra(DailyhuntConstants.MENU_PAYLOAD, menuPayload)
                putExtra(Constants.BUNDLE_MENU_ARGUMENTS, arguments)
                putExtra(DailyhuntConstants.URL_STR, transformMenuL2Url(menuL2.content))
                putExtra(Constants.VALIDATE_DEEPLINK, false)
            }
            NavigationHelper.navigationLiveData.value = NavigationEvent(intent, targetId = targetNavId)
        }
    }

    private fun transformMenuL2Url(url: String?): String? {
        url ?: return null
        return UrlUtil.getUrlWithQueryParamns(url, mapOf("lang" to UserPreferenceUtil
                .getUserNavigationLanguage()))
    }

    private fun handleToggleAutoPlay(enable:Boolean) {
        val newState = if (enable) {
            AutoPlayHelper.AutoPlayPreference.AUTO_PLAY_ALWAYS
        } else {
            AutoPlayHelper.AutoPlayPreference.AUTO_PLAY_OFF
        }
        AutoPlayHelper.saveAutoPlayPreference(newState)
    }

    private fun handleFollowAction(asset: CommonAsset?,
                                   pageEntity: PageEntity?,
                                   followActionType: FollowActionType) {
        if (pageEntity == null && asset == null) {
            return
        }

        val entityId = ((asset?.i_source()?.id) ?: (pageEntity?.id)) ?: kotlin.run {
            Logger.e(LOG_TAG, "entity id missing")
            return
        }
        val entityType = ((asset?.i_source()?.entityType)?:(pageEntity?.entityType)) ?: kotlin.run {
            Logger.e(LOG_TAG, "entity type missing")
            return
        }
        val entitySubType = ((asset?.i_source()?.type)?:(pageEntity?.subType))

        followUsecase.execute(FollowFromMenuUsecase.createBundle(FollowFromMenuUsecase.Pojo(entityId, entityType, entitySubType, followActionType)))
    }


    private fun CommonAsset?.toMenuPayload(displayLocation: MenuLocation, option: MenuL1, referrer: PageReferrer?):
            MenuPayload? {
        if (this == null) return null
        val itemId = i_id()
        val format = i_format()
        val subFormat = i_subFormat()
        val uiType = i_uiType()
        val source = i_source()?.id
        //val eventParam = i_experiments()?.let { JsonUtils.toJson(it) }


        val map = HashMap<String, Any?>()

        try {
            map[NhAnalyticsAppEventParam.CLIENT_ID.getName()] = ClientInfoHelper.getClientId()
            map[AnalyticsParam.ITEM_ID.getName()] = this.i_id()
            if (!CommonUtils.isEmpty(this.i_type())) {
                map[AnalyticsParam.ASSET_TYPE.getName()] = this.i_type()
            }
            map[AnalyticsParam.ITEM_TYPE.getName()] = if (this.i_format() == Format.POST_COLLECTION) {
                NewsReferrer.COLLECTION.referrerName
            } else {
                NewsConstants.ITEM_TYPE_ITEM
            }
            if (this.i_landingType() != null) {
                map[AnalyticsParam.CARD_TYPE.getName()] = this.i_landingType()
            }
            map[AnalyticsParam.UI_TYPE.getName()] = this.i_uiType()?.name
            map[AnalyticsParam.GROUP_ID.getName()] = this.i_groupId()
            map[AnalyticsParam.CONTENT_TYPE.getName()] = this.i_contentType()
            map[AnalyticsParam.ITEM_PUBLISHER_ID.getName()] = this.i_source()?.id
            map[AnalyticsParam.ITEM_LANGUAGE.getName()] = this.i_langCode()
            map[com.dailyhunt.huntlytics.sdk.Constants.ATTR_PROPERTY_USER_APP_VERSION] = ClientInfoHelper.getClientInfo().appVersion// TODO (satosh.dhanyamraju): verify
            map[NhAnalyticsAppEventParam.USER_CONNECTION.getName()] = NetworkSDKUtils
                    .getLastKnownConnectionType()
            map[NhAnalyticsAppEventParam.EVENT_ATTRIBUTION.getName()] = NhAnalyticsAppState.getInstance().eventAttribution.referrerName
            map[NhAnalyticsAppEventParam.SESSION_SOURCE.getName()] = NhAnalyticsAppState.getInstance().sessionSource.referrerName
            map[NhAnalyticsAppEventParam.REFERRER.getName()] = referrer?.referrer?.referrerName
            map[NhAnalyticsAppEventParam.REFERRER_ID.getName()] = referrer?.id
        } catch (ex: Exception) {
            Logger.caughtException(ex)
        }
        this.i_experiments()?.let {
            map.putAll(it)
        }

        val params = Gson().toJson(map)


        return when {
            format != null && subFormat != null && uiType != null -> {
                MenuPayload(
                        itemId = itemId,
                        format = format,
                        subFormat = subFormat,
                        uiType = uiType,
                        sourceId = source,
                        displayLocation = displayLocation,
                        option = option.id,
                        l1 = option,
                        eventParam = params
                )
            }
            else -> null
        }
    }

    private fun handleVideoDownload(activity: Activity?,
                                    card: CommonAsset?) {
        if (activity?.isFinishing != false) return
        card ?: return

        val adapter = object : PermissionAdapter(101, activity, DefaultRationaleProvider()) {
            override fun getPermissions(): List<Permission> {
                return listOf(Permission.WRITE_EXTERNAL_STORAGE)
            }

            override fun onPermissionResult(grantedPermissions: List<Permission>,
                                            deniedPermissions: List<Permission>,
                                            blockedPermissions: List<Permission>) {

                if (deniedPermissions.isNotEmpty() || blockedPermissions.isNotEmpty()) {
                    return
                }
                if (card.i_videoAsset()?.downloadVideoUrl.isNullOrEmpty()) return

                val downloadUrl = card.i_videoAsset()?.downloadVideoUrl!!
                val downloadRequestId = DownloadUtils.downloadSingleFile(
                        activity, downloadUrl, card.i_title())

                DownloadUtils.downloadRequestEvent.postValue(DownloadRequestData(
                        requestId = downloadRequestId,
                        asset = card
                ))
            }

            override fun shouldShowRationale() = true


            @Subscribe
            fun onPermissionResult(permissionResult: PermissionResult) {
                onPermissionResultListener(permissionResult.activity,
                        permissionResult.permissions)
                BusProvider.getUIBusInstance().unregister(this)
            }
        }

        storagePermissionHelper = PermissionHelper(adapter)
        BusProvider.getUIBusInstance().register(adapter)
        storagePermissionHelper?.requestPermissions()
    }

    private fun onPermissionResultListener(activity: Activity?, permissions: Array<String>?) {
        storagePermissionHelper?.handlePermissionCallback(activity, permissions)
    }

    override fun onDialogConformDelete(arguments: Bundle?) {
        super.onDialogConformDelete(arguments)
        arguments ?: kotlin.run {
            Logger.e(LOG_TAG, "Argument can not be null")
            return
        }
        /**
         * written here because sso can not be accessed from datastore
         */
        arguments.putString(Constants.BUNDLE_DELETE_HEADER, SSO.getInstance().encryptedSessionData)
        deletePostUsecase.execute(arguments)
    }

    private fun handleReportPostAction(arguments: Bundle) {
        arguments.putString(Constants.BUNDLE_DELETE_HEADER, SSO.getInstance().encryptedSessionData)

        reportPostUsecase.execute(arguments)
    }


    fun clear() {

    }

    private fun logEventsForSelectedL1(
            meta: MenuOption,
            story: CommonAsset?): Boolean {
        val referrer = arguments.getSerializable(Constants.REFERRER) as? PageReferrer
        val section = AnalyticsHelper2.getSection(arguments.getString(NewsConstants.DH_SECTION, Constants.EMPTY_STRING))
        return when (meta.menuL1.postAction) {
            MenuL1PostClkAction.ADD_COMMENT.name -> {
                val map = hashMapOf<NhAnalyticsEventParam, Any?>(
                        AnalyticsParam.GROUP_ID to story?.i_groupId(),
                        NhAnalyticsAppEventParam.TYPE to ExploreButtonType.CARD_MENU_ADD_COMMENT)
                AnalyticsClient.log(NhAnalyticsAppEvent.EXPLOREBUTTON_CLICK,
                        section, map, referrer)
                true
            }
            else -> false
        }
    }

}

fun handlePageEntityShare(pageEntity: PageEntity?, activity: Activity?, packageName: String? = null) {
    pageEntity ?: run {
        Logger.e(LOG_TAG, "page entity can not be null")
        return
    }

    activity ?: run {
        Logger.e(LOG_TAG, "activity can not be null")
        return
    }

    val sendIntent = Intent()
    sendIntent.action = Intent.ACTION_SEND
    sendIntent.type = Constants.INTENT_TYPE_TEXT
    val shareContent = ShareContent()
    val title = CommonUtils.getString(R.string.entity_share_message, pageEntity.displayName)
    shareContent.title = title + Constants.BR_TAG
    shareContent.packageName = packageName
    shareContent.sourceName = pageEntity.displayName + Constants.BR_TAG
    shareContent.shareUrl = listOf<String?>(pageEntity.shareUrl, pageEntity.deeplinkUrl).find {
        it != null
    }
    shareContent.subject = title
    handleShareContentResult(shareContent, activity, packageName)
}

fun handleShare(commonAsset: CommonAsset?, activity: Activity?, packageName: String? = null) {
    val asset = commonAsset ?: run {
        Logger.e(LOG_TAG, "asset can not be null")
        return
    }

    activity ?: run {
        Logger.e(LOG_TAG, "activity can not be null")
        return
    }

    if (asset.i_postPrivacy() == PostPrivacy.PRIVATE) {
        Logger.e(LOG_TAG, "can not share private card")
        return
    }

    if (commonAsset.i_viral() != null) {
        handleViralShare(commonAsset, activity, packageName)
        return
    }

    val sendIntent = Intent()
    sendIntent.action = Intent.ACTION_SEND
    sendIntent.type = Constants.INTENT_TYPE_TEXT
    val shareContent = ShareContent()
    val title = asset.i_title()
    shareContent.title = title + Constants.BR_TAG
    if (CommonUtils.isEmpty(title)) {
        shareContent.title = createShareTitleFromContent(asset.i_content()) + Constants.BR_TAG
    }
    shareContent.packageName = packageName
    shareContent.sourceName = asset.i_source()?.displayName
    shareContent.shareUrl = asset.i_shareUrl()
    shareContent.subject = title
    handleShareContentResult(shareContent, activity, packageName)
}

/*
* If asset is viral asset
* */

private fun getFirstNonEmptyString(vararg values: String?): String? {
    val iterator = values.iterator()
    while (iterator.hasNext()) {
        val current = iterator.next()
        if (current != null) {
            return current
        }
    }
    return null
}

private fun createShareTitleFromContent(content: String?): String? {
    content ?: return null
    val simpleTextContent = AndroidUtils.getTextFromHtml(content)
    val ellipsizeEnd = if (simpleTextContent.length > 50) Constants.ELLIPSIZE_END else Constants.EMPTY_STRING
    return simpleTextContent.substring(0, Math.min(simpleTextContent.length, 50)) + ellipsizeEnd
}

fun handleViralShare(commonAsset: CommonAsset?,
                     activity: Activity?,
                     packageName: String?) {
    activity ?: return
    val vhAsset = commonAsset?.i_viral() ?: return
    val backgroundOption = vhAsset.backgroundOption ?: return
    val shareContent = ShareContent()
    val shareText = VHShareHelper.getInstance().getShareText(vhAsset.topics?.toTypedArray())
    val titleText = StringBuilder()
    if (CommonUtils.isEmpty(vhAsset.itemText)) {
        if (!CommonUtils.isEmpty(commonAsset.i_title())) {
            titleText.append(commonAsset.i_title())
        } else {
            titleText.append(createShareTitleFromContent(commonAsset.i_content()))
        }
    } else {
        titleText.append(vhAsset.itemText)
    }
    titleText.append(Constants.BR_TAG + Constants.BR_TAG + shareText)
    if (!CommonUtils.isEmpty(commonAsset.i_shareUrl())) {
        shareContent.shareUrl = commonAsset.i_shareUrl()
    }
    val imageAspectRatio = CardsBindUtils.getViralAspectRatio(commonAsset)
    val width = CommonUtils.getDeviceScreenWidth() - 2 * CommonUtils.getDimension(R
            .dimen.story_card_padding)
    val height = width / imageAspectRatio
    val url = ImageUrlReplacer.getQualifiedImageUrl(backgroundOption.imageUrl, width, height.toInt())
    shareContent.title = titleText.toString()
    shareContent.subject = commonAsset.i_title()
    shareContent.sourceName = commonAsset.i_source()?.displayName
    shareContent.packageName = packageName

    if (!CommonUtils.isEmpty(url)) {
        if (commonAsset.i_subFormat() == SubFormat.VHGIF) {
            shareContent.imageUrl = url
            shareContent.shareContentType = ShareContentType.VIRAL_GIF
            handleViralGifShare(activity, shareContent)
            return // AVOID FALLBACK
        } else if (commonAsset.i_subFormat() == SubFormat.VHMEME) {
            handleViralImageShare(commonAsset, activity, shareContent, url)
            return // AVOID FALLBACK
        }
    }
    handleShareContentResult(shareContent, activity, packageName)
}


private fun handleShareContentResult(shareContent: ShareContent,
                                     activity: Activity,
                                     packageName: String?) {
    val sendIntent = Intent()
    sendIntent.action = Intent.ACTION_SEND
    sendIntent.type = Constants.INTENT_TYPE_TEXT
    val shareContentHelper = ShareFactory.getShareHelper(packageName, activity, sendIntent, shareContent)
    shareContentHelper.share()
}

/*
* If viral asset is for gif
* */
fun handleViralGifShare(activity: Activity, shareContent: ShareContent) {
    val imageShareHelper = ImageShareHelper(object : ImageShareHelperCallback {
        override fun onFinish(shareContent: ShareContent) {
            var packageName = shareContent.packageName

            if (!CommonUtils.isEmpty(packageName) && AndroidUtils.isAppDisabled(packageName)) {
                packageName = Constants.EMPTY_STRING
            }
            handleShareContentResult(shareContent, activity, packageName)
        }

        override fun getActivity(): Activity {
            return activity
        }
    }, shareContent)

    imageShareHelper.loadImageAndShare()
}

/*
* If viral asset is for image
* */
fun handleViralImageShare(commonAsset: CommonAsset,
                          activity: Activity,
                          shareContent: ShareContent,
                          imageUrl: String) {
    val vhAsset: ViralAsset = commonAsset.i_viral() ?: return
    var packageName = shareContent.packageName

    if (!CommonUtils.isEmpty(packageName) && AndroidUtils.isAppDisabled(packageName)) {
        packageName = Constants.EMPTY_STRING
    }
    val imageTarget = object : SimpleTarget<Bitmap>() {
        override fun onResourceReady(p0: Bitmap, p1: Transition<in Bitmap>?) {
            val fileName = "image_" + commonAsset.i_id() + ".png"
            val overlapBitmap = WeakReference(VHShareHelper.getInstance()
                    .overlayBitmap(p0, VHShareHelper.getInstance()
                            .getShareBannerBitmap(vhAsset.topics?.toTypedArray())))
            val uri = VHShareHelper.getInstance()
                    .getUriAndSaveBitmap(overlapBitmap.get(), fileName, false)
            if (uri != null) {
                shareContent.fileUri = uri
            }

            handleShareContentResult(shareContent, activity, packageName)
        }

        override fun onLoadFailed(p0: Drawable?) {
            super.onLoadFailed(p0)
            Logger.e(LOG_TAG, "Viral image load failed")
            handleShareContentResult(shareContent, activity, packageName)
        }
    }
    val nsfwEnabled = PreferenceManager.getPreference(GenericAppStatePreference.SHOW_NSFW_FILTER, true)
    val requestOption = if (vhAsset.nsfw && nsfwEnabled) {
        RequestOptions().transform(BlurTransformation()).dontAnimate()
    } else {
        RequestOptions().dontTransform()
    }
    Image.load(imageUrl, true).apply(requestOption).into(imageTarget)
}

fun handleComment(commonAsset: CommonAsset?, activity: Activity?, referrer: PageReferrer?) {
    val asset = commonAsset ?: run {
        Logger.e(LOG_TAG, "asset can not be null")
        return
    }

    val ctx = activity ?: run {
        Logger.e(LOG_TAG, "activity can not be null")
        return
    }

    try {
        val intent = CommonNavigator.getPostCreationIntent(asset.i_id(),
                CreatePostUiMode.COMMENT, null, referrer, null,
                asset.i_source()?.id, asset.i_source()?.type, asset.i_parentPostId())
        ctx.startActivity(intent)
    } catch (ex: Exception) {
        Logger.caughtException(ex)
    }
}

fun handleVhSourceBrowser(commonAsset: CommonAsset?, activity: Activity?, targetNavId: Long) {
    commonAsset?.i_viral()?.vhSourcePageUrl?.let { url ->
        val urlWithParams = UrlUtil.getUrlWithQueryParamns(url,
                hashMapOf(NewsConstants.URL_PARAM_CLIENT_ID to ClientInfoHelper.getClientId(),
                        NewsConstants.URL_PARAM_ITEM_ID to commonAsset.i_id()))

        val browserIntent = NhBrowserNavigator.getTargetIntent()
        browserIntent.putExtra(DailyhuntConstants.URL_STR, urlWithParams)
        browserIntent.putExtra(Constants.VALIDATE_DEEPLINK, false)
        val event = NavigationEvent(browserIntent, timeStamp = System.currentTimeMillis(), targetId = targetNavId)
        NavigationHelper.navigationLiveData.postValue(event)
    }
}

fun handleBrowseBySource(commonAsset: CommonAsset?, activity: Activity?,arguments: Bundle, targetNavId: Long) {
    val asset = commonAsset ?: run {
        Logger.e(LOG_TAG, "asset can not be null")
        return
    }

    val ctx = activity ?: run {
        Logger.e(LOG_TAG, "activity can not be null")
        return
    }
    openNPLanding(asset, ctx,arguments, targetNavId)
}


fun openNPLanding(item: CommonAsset, context: Context,arguments: Bundle, targetNavId: Long) {
    val sourceAsset = item.i_source() ?: return
    if ("SOURCE" == sourceAsset.feedType) {
        sourceAsset.let {
            val intent = Intent(Constants.ENTITY_OPEN_ACTION)
            intent.setPackage(CommonUtils.getApplication().packageName)
            intent.putExtra(NewsConstants.ENTITY_KEY, sourceAsset.id)
            intent.putExtra(NewsConstants.ENTITY_TYPE, sourceAsset.entityType)
            NavigationHelper.navigationLiveData.value = NavigationEvent(intent, targetId = targetNavId)
        }
    } else {
        val loggedInUserId = SSO.getInstance().userDetails?.userID

        val pageReferrer = arguments.getSerializable(Constants.REFERRER) as? PageReferrer
        val tabType = if (loggedInUserId == sourceAsset.id) ProfileTabType.FPV_POSTS else ProfileTabType.TPV_POSTS
        val profile = UserBaseProfile()
        profile.userId = sourceAsset.id ?: Constants.EMPTY_STRING
        val intent = CommonNavigator.getProfileHomeIntent(profile,
                pageReferrer,
                tabType)
        val event = NavigationEvent(intent = intent,
                timeStamp = System.currentTimeMillis(), targetId = targetNavId)
        NavigationHelper.navigationLiveData.postValue(event)

    }
}


fun handleBrowser(activity: Activity?, url: String?) {
    url ?: run {
        Logger.e(LOG_TAG, "url can not be null")
        return
    }
    val ctx = activity ?: run {
        Logger.e(LOG_TAG, "activity can not be null")
        return
    }

    val browserIntent = NhBrowserNavigator.getTargetIntent()
    browserIntent.putExtra(DailyhuntConstants.URL_STR, url)
    browserIntent.putExtra(Constants.VALIDATE_DEEPLINK, false)
    ctx.startActivity(browserIntent)
}

fun handleNsfwToggle() {
    PreferenceManager.savePreference(GenericAppStatePreference.SHOW_NSFW_FILTER, false)
}

fun handleFontSizeChange(activity: Activity?) {
    activity ?: run {
        Logger.e(LOG_TAG, "Can not show popup for null activity")
        return
    }
    val fontSizeChangeView2 = FontSizeChangeView2(activity)
    fontSizeChangeView2.show()
}

fun handleDeletePostAction(asset: CommonAsset?, activity: Activity?,
                           uniqueScreenId: Int, arguments: Bundle) {
    if (asset == null || activity == null || activity !is AppCompatActivity) {
        Logger.e(LOG_TAG, "Activity or asset can not be null")
        return
    }

    if (activity.isFinishing) {
        Logger.e(LOG_TAG, "Activity is finishing")
        return
    }
    val commonMessageDialogOptions = CommonMessageDialogOptions(
        uniqueScreenId,
        CommonUtils.getString(R.string.delete_post_dialog_heading),
        CommonUtils.getString(R.string.delete_post_dialog_text),
        CommonUtils.getString(R.string.delete),
        CommonUtils.getString(R.string.cancel_text),
        null,
        Constants.DELETE_POST_DIALOG_USECASE,
        arguments
    )
    PostDeleteDialog.newInstance(commonMessageDialogOptions).show(activity
            .supportFragmentManager, "CommonMessageDialog")

}

fun handleSavePostAction(saveUnsavePostUsecase: MediatorUsecase<BookmarkList, Boolean>,
                         asset: CommonAsset?,
                         context: Context?,
                         isVideo: Boolean = false) {
    asset ?: run {
        Logger.e(LOG_TAG, "asset can not be null for save action")
        return
    }
    val saved = saveUnsavePostUsecase.execute(makeBookmarkApiPostBody(asset, BookMarkAction.ADD))
    if (saved) {
        val message = if (isVideo) {
            CommonUtils.getString(R.string.offline_video_saved_success)
        } else {
            CommonUtils.getString(R.string.offline_saved_succes)
        }
        FontHelper.showCustomFontToast(context, message, Toast.LENGTH_LONG)
    } else {
        val message = if (isVideo) {
            CommonUtils.getString(R.string.offline_video_saving_failed)
        } else {
            CommonUtils.getString(R.string.offline_saving_failed)
        }

        FontHelper.showCustomFontToast(context, message, Toast.LENGTH_LONG)
    }
}

fun handleUnSavePostAction(saveUnsavePostUsecase: MediatorUsecase<BookmarkList, Boolean>,
                           asset: CommonAsset?,
                           context: Context?,
                           isVideo: Boolean = false) {
    asset ?: run {
        Logger.e(LOG_TAG, "asset can not be null for unsave action")
        return
    }
    val unsaved = saveUnsavePostUsecase.execute(makeBookmarkApiPostBody(asset, BookMarkAction.DELETE))

    if (unsaved) {
        val message = if (isVideo) {
            CommonUtils.getString(R.string.offline_video_unsaved_success)
        } else {
            CommonUtils.getString(R.string.offline_unsaved_success)
        }
        FontHelper.showCustomFontToast(context, message, Toast.LENGTH_LONG)
    } else {
        val message = if (isVideo) {
            CommonUtils.getString(R.string.offline_video_unsaving_failed)
        } else {
            CommonUtils.getString(R.string.offline_unsaving_failed)
        }

        FontHelper.showCustomFontToast(context, message, Toast.LENGTH_LONG)
    }
}


private const val LOG_TAG = "MenuClickDelegate"