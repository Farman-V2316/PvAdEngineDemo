package com.newshunt.adengine.other.news.viewmodel

import android.os.Bundle
import android.view.View
import com.newshunt.dataentity.social.entity.LikeType

interface EmojiClickHandlingViewModel {

    fun onEmojiClick(view: View, item: Any, parent: Any?, likeType: LikeType, isComment: Boolean?, commentType: String?) {}

    fun isDetail(): Boolean {
        return false
    }
}

interface ShareHandlingViewModel {
    fun onShareClick(view: View, item: Any, args: Bundle?) {}
}

interface SocialInteractionViewModel : EmojiClickHandlingViewModel, ShareHandlingViewModel