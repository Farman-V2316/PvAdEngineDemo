package com.newshunt.news.helper

import com.newshunt.common.helper.common.AndroidUtils
import com.newshunt.dataentity.common.asset.CommonAsset
import com.newshunt.dataentity.common.asset.Format
import com.newshunt.dataentity.common.asset.MinimizedPostEntity
import com.newshunt.dataentity.common.asset.ParentIdHolderCommenAsset
import com.newshunt.dataentity.common.asset.SubFormat
import com.newshunt.dataentity.common.asset.UiType2
import com.newshunt.dataentity.common.asset.toMinimizedRepostAsset

fun CommonAsset.toMinimizedCommonAsset(): ParentIdHolderCommenAsset {
    val nonHtmlContent = AndroidUtils.getTextFromHtml(this.i_content())
    return ParentIdHolderCommenAsset(
            parentPostId = this.i_parentPostId()
            , asset = MinimizedPostEntity(
            id = this.i_id(),
            type = this.i_type(),
            smallNonHtmlContent = nonHtmlContent.substring(0, Math.min(50, nonHtmlContent.length)),
            format = this.i_format() ?: Format.HTML,
            subFormat = this.i_subFormat() ?: SubFormat.STORY,
            uiType = this.i_uiType() ?: UiType2.NORMAL,
            source = this.i_source(),
            title = this.i_title(),
            contentImage = this.i_contentImageInfo()?.url,
            detailUiType = this.i_detailUiType() ?: UiType2.NORMAL,
            detailAttachLocation = this.i_detailAttachLocation(),
            shareUrl = this.i_shareUrl(),
            experiments = this.i_experiments(),
            allowComments = this.i_allowComments(),
            deeplinkUrl = this.i_deeplinkUrl(),
            poll = this.i_poll(),
            viral = this.i_viral(),
            thumbnailInfos = this.i_thumbnailUrlDetails(),
            repostAsset = this.i_repostAsset()?.toMinimizedRepostAsset(),
            videoAsset = this.i_videoAsset(),
            linkAsset = this.i_linkAsset(),
            groupId = this.i_groupId(),
            postPrivacy = this.i_postPrivacy(),
            contentType = this.i_contentType(),
            landingType = this.i_landingType(),
            langCode = this.i_langCode(),
            maxImageViewportHeightPercentage = this.i_maxImageViewportHeightPercentage()
    ))
}