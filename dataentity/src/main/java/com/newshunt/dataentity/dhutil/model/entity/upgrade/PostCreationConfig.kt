package com.newshunt.dataentity.dhutil.model.entity.upgrade

import com.newshunt.dataentity.common.asset.PollDuration
import com.newshunt.dataentity.social.entity.CACHE_DEFAULT_TIME
import com.newshunt.dataentity.social.entity.DEFAULT_IMAGE_COMPRESS_QUALITY
import com.newshunt.dataentity.social.entity.DEFAULT_NOTIFICATION_REMOVAL_DELAY
import com.newshunt.dataentity.social.entity.DEFAULT_RETRY_LIMIT
import com.newshunt.dataentity.social.entity.MAX_IMAGE_COUNT
import com.newshunt.dataentity.social.entity.MAX_POLL_OPTION_LENGTH

data class PostCreationConfig(
    val pcLocationEnable: Boolean = true,
    val pcLocationAutoCompleteEnable: Boolean = true,
    val pcLocationNearByEnable: Boolean = true,
    val pcLocationNearByCacheTimeInMs: Long = CACHE_DEFAULT_TIME,
    val pcImageAttachmentSize: Int = MAX_IMAGE_COUNT,
    val pcFailUploadRetry: Int = DEFAULT_RETRY_LIMIT,
    val pcPollOptionLength: Int = MAX_POLL_OPTION_LENGTH,
    val pcPollDurationList: List<PollDuration>? = null,
    val pcCompressImage: Boolean = false,
    val pcCompressImageQuality: Int = DEFAULT_IMAGE_COMPRESS_QUALITY,
    val pcNotificationRemovalDelay: Long = DEFAULT_NOTIFICATION_REMOVAL_DELAY
)