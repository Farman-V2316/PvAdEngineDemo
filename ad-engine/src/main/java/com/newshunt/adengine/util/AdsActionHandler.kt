package com.newshunt.adengine.util

import android.net.Uri
import android.view.View
import com.newshunt.adengine.client.AsyncAdImpressionReporter
import com.newshunt.adengine.model.entity.BaseDisplayAdEntity
import com.newshunt.common.helper.common.Constants
import com.newshunt.dataentity.common.asset.PostEntity
import com.newshunt.dataentity.social.entity.LikeType
import com.newshunt.news.helper.LikeEmojiBindingUtils
import com.newshunt.news.viewmodel.EmojiClickHandlingViewModel

/**
 * Class to handle reaction clicks on Ads.
 *
 * @author raunak.yadav
 */
class AdsActionHandler : EmojiClickHandlingViewModel {
    private var baseDisplayAdEntity: BaseDisplayAdEntity? = null
    private var cvm: EmojiClickHandlingViewModel? = null

    fun onReactionClick(view :View, adEntity: BaseDisplayAdEntity, vm: EmojiClickHandlingViewModel) {
        LikeEmojiBindingUtils.showLikePopup(view, adEntity.contentAsset, null,
            this, false, Constants.EMPTY_STRING)
        baseDisplayAdEntity = adEntity
        cvm = vm
    }

    override fun onEmojiClick(view: View, item: Any, parent: Any?, likeType: LikeType, isComment: Boolean?, commentType: String?) {
        cvm?.onEmojiClick(view, item, parent, likeType, isComment, commentType)

        baseDisplayAdEntity?.let {
            if (it.adReactionBeaconUrl.isNullOrBlank()) {
                return@let
            }
            val uriBuilder = Uri.parse(it.adReactionBeaconUrl)
                .buildUpon()
                .appendQueryParameter(AdConstants.AD_ACTION_QUERY_PARAM, likeType.name)

            if (item is PostEntity && item.selectedLikeType != null) {
                uriBuilder.appendQueryParameter(AdConstants.AD_DESELECT_QUERY_PARAM, AdConstants.TRUE)
            }
            AsyncAdImpressionReporter(it).hitTrackerUrl(uriBuilder.build().toString())
        }
    }
}