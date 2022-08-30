package com.newshunt.appview.common.postcreation.analytics.helper

import com.newshunt.analytics.client.AnalyticsClient
import com.newshunt.appview.common.postcreation.analytics.entity.CreatePostActionType
import com.newshunt.appview.common.postcreation.analytics.entity.CreatePostHomeEntranceActionType
import com.newshunt.appview.common.postcreation.analytics.entity.CreatePostImageAttachmentType
import com.newshunt.appview.common.postcreation.analytics.entity.CreatePostPublishStatus
import com.newshunt.appview.common.postcreation.analytics.entity.DHCreatePostEvent
import com.newshunt.appview.common.postcreation.analytics.entity.DHCreatePostEventParams
import com.newshunt.appview.common.postcreation.view.helper.PostConstants.Companion.UGC_SOURCE_TYPE
import com.newshunt.common.helper.common.Constants
import com.newshunt.dataentity.analytics.entity.NhAnalyticsAppEventParam
import com.newshunt.dataentity.analytics.entity.NhAnalyticsEventParam
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.analytics.section.NhAnalyticsEventSection
import com.newshunt.dataentity.common.asset.CreatePostUiMode
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.common.model.entity.BaseError
import com.newshunt.dataentity.model.entity.GroupInfo
import com.newshunt.dataentity.social.entity.CreatePost
import com.newshunt.deeplink.navigator.CommonNavigator
import java.util.*

class CreatePostAnalyticsHelper {
    companion object {
        private const val LOG_TAG = "CreatePostAnalyticsHelper"

        @JvmStatic
        fun logCreatePostClickEvent(pageReferrer: PageReferrer?) {
            val map = HashMap<NhAnalyticsEventParam, Any>()
            AnalyticsClient.log(DHCreatePostEvent.CREATE_POST_CLICK, NhAnalyticsEventSection.APP,
                    map, pageReferrer)
        }

        @JvmStatic
        fun logCreatePostHomeEvent(pageReferrer: PageReferrer, mode: CreatePostUiMode?) {
            val map = HashMap<NhAnalyticsEventParam, Any>()
            // In case of deeplink or deeplink notification the action type will be null
            if (!CommonNavigator.isFromNotificationTray(pageReferrer) ||
                    !CommonNavigator.isDeeplinkReferrer(pageReferrer)) {
                map[DHCreatePostEventParams.ACTION_TYPE] = getActionTypeFromConfig(mode)
            }
            AnalyticsClient.log(DHCreatePostEvent.CREATE_POST_HOME, NhAnalyticsEventSection.APP,
                    map, pageReferrer)
        }

        private fun getActionTypeFromConfig(mode: CreatePostUiMode?):
                CreatePostHomeEntranceActionType {
            if (mode == null) {
                return CreatePostHomeEntranceActionType.CREATE_POST_ICON
            }
            when (mode) {
                CreatePostUiMode.POST -> {
                    return CreatePostHomeEntranceActionType.CREATE_POST_ICON
                }
                CreatePostUiMode.REPOST -> {
                    return CreatePostHomeEntranceActionType.REPOST_CLICK
                }
                CreatePostUiMode.COMMENT -> {
                    return CreatePostHomeEntranceActionType.COMMENTS_CLICK
                }
                CreatePostUiMode.REPLY -> {
                    return CreatePostHomeEntranceActionType.REPLY_CLICK
                }
                else -> {
                    return CreatePostHomeEntranceActionType.CREATE_POST_ICON
                }
            }
        }

        @JvmStatic
        fun logCreatePostUIActionEvent(cpActionType: CreatePostActionType,
                                       cpImageAttachmentType: CreatePostImageAttachmentType,
                                       pageReferrer: PageReferrer) {

            val map = HashMap<NhAnalyticsEventParam, Any>()
            if (cpImageAttachmentType != CreatePostImageAttachmentType.NONE) {
                map[DHCreatePostEventParams.ATTACHMENT_TYPE] = cpImageAttachmentType
            }
            map[DHCreatePostEventParams.ACTION_TYPE] = cpActionType
            AnalyticsClient.log(DHCreatePostEvent.UI_POST_ACTION, NhAnalyticsEventSection.APP,
                    map, pageReferrer)
        }

        @JvmStatic
        fun logCreatePostPublishEvent(pageReferrer: PageReferrer?, pageReferrerFlow: PageReferrer?,
                                      cp: CreatePost?, mode: CreatePostUiMode,
                                      parentId: String, parentPostId: String, sourceId: String,
                                      sourceType: String,groupInfo : GroupInfo?) {
            if (cp == null) {
                return
            }
            val map = HashMap<NhAnalyticsEventParam, Any>()
            // This becomes item_id/post_id/comment_id/reply_id
            map[DHCreatePostEventParams.ITEM_ID] = cp.cpEntity.postId
            // This becomes parent_id
            map[DHCreatePostEventParams.PARENT_ID] = parentId
            // if reply parent_comment_id
            if (mode == CreatePostUiMode.REPLY) {
                map[DHCreatePostEventParams.PARENT_ID] = parentPostId
                map[DHCreatePostEventParams.COMMENT_ID] = parentId
            }
            if (mode != CreatePostUiMode.POST && !CommonUtils.isEmpty(sourceId)) {
                map[DHCreatePostEventParams.TARGET_USER_ID] = sourceId
            }

            if (mode == CreatePostUiMode.POST) {
                map[NhAnalyticsAppEventParam.SOURCE_TYPE] = UGC_SOURCE_TYPE
            } else {
                map[NhAnalyticsAppEventParam.SOURCE_TYPE] = sourceType
            }

            // In case of deeplink or deeplink notification the action type will be null
            if (!CommonNavigator.isFromNotificationTray(pageReferrer) ||
                    !CommonNavigator.isDeeplinkReferrer(pageReferrer)) {
                map[DHCreatePostEventParams.ACTION_TYPE] = getActionTypeFromConfig(mode)
            }
            if (pageReferrerFlow != null && pageReferrerFlow.referrer != null) {
                map[NhAnalyticsAppEventParam.REFERRER_FLOW] = pageReferrerFlow.referrer.referrerName
            }

            if (pageReferrerFlow != null && pageReferrerFlow.id != null) {
                map[NhAnalyticsAppEventParam.REFERRER_FLOW_ID] = pageReferrerFlow.id
            }

            if (groupInfo!= null ){
                map[NhAnalyticsAppEventParam.TARGET_COMMUNITY_ID] = groupInfo.id
            }

            AnalyticsClient.log(DHCreatePostEvent.POST_PUBLISH, NhAnalyticsEventSection.APP,
                    map, pageReferrer)
        }

        @JvmStatic
        fun logCreatePostPublishStatusEvent(pageReferrer: PageReferrer,
                                            status: CreatePostPublishStatus, baseError:
                                            BaseError?) {
            val map = HashMap<NhAnalyticsEventParam, Any>()
            map[DHCreatePostEventParams.PUBLISH_STATUS] = status
            if (baseError != null && status == CreatePostPublishStatus.FAILURE) {
                map[DHCreatePostEventParams.ERROR_CODE] = baseError.statusAsInt
                map[DHCreatePostEventParams.ERROR_REASON] = baseError.message
                        ?: Constants.EMPTY_STRING
            }
            AnalyticsClient.log(DHCreatePostEvent.UI_POST_PUBLISH_STATE, NhAnalyticsEventSection.APP,
                    map, pageReferrer)
        }
    }
}