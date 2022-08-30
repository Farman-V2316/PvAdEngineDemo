/*
 *
 *  * Copyright (c) 2021 Newshunt. All rights reserved.
 *  
 */

package com.newshunt.news.view.helper

import android.view.View
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations
import com.newshunt.appview.R
import com.newshunt.appview.common.profile.model.usecase.PostBookmarksUsecase
import com.newshunt.appview.common.viewmodel.handleSavePostAction
import com.newshunt.appview.common.viewmodel.handleUnSavePostAction
import com.newshunt.common.helper.common.Logger
import com.newshunt.dataentity.common.asset.CommonAsset
import com.newshunt.dataentity.common.asset.Format
import com.newshunt.dhutil.helper.theme.ThemeUtils
import com.newshunt.news.model.daos.BookmarksDao
import com.newshunt.news.model.sqlite.SocialDB
import com.newshunt.news.model.usecase.toMediator2
import com.newshunt.news.view.fragment.DetailsBindUtils
import java.lang.ref.WeakReference

/**
 * To set up & update the bookmark button on actionbar.
 * @author satosh.dhanyamraju
 */
class ActionbarBookmarkHelper : Observer<Int>, View.OnClickListener {
    private var saved: Boolean = false
    private var textView: WeakReference<TextView?>? = null
    private val LOG_TAG: String = "ActionbarBookmarkHelper"
    private val cardLd: MutableLiveData<CommonAsset?> = MutableLiveData()
    private val icon = Transformations.switchMap(cardLd) { a ->
        a?.let { bookmarkIcon(it) }
    }

    val saveUnsaveUsecase = PostBookmarksUsecase(com.newshunt.appview.common.profile.helper.createBookmarkService(),
            SocialDB.instance().bookmarkDao()).toMediator2()
    fun setUp(card: CommonAsset, lifecycleOwner: LifecycleOwner?, textView: TextView?) {
        Logger.d(LOG_TAG, "setUp() called with: card = $card, lifecycleOwner = $lifecycleOwner, textView = $textView")
        this.textView = WeakReference(textView)
        textView?.visibility = visibility(card)
        textView?.setOnClickListener(this)
        cardLd.value = card
        icon.removeObserver(this)
        lifecycleOwner?.let { icon.observe(it, this) }
    }


    private fun bookmarkIcon(card: CommonAsset): LiveData<Int> {
        return Transformations.map(isSaved(card)) {
            Logger.d(LOG_TAG, "bookmarkIcon: ${card.i_id()}, saved-count=$it")
            val nightMode = ThemeUtils.isNightMode()
            saved = it > 0
            if (saved) { // saved
                if (nightMode) R.drawable.ic_detail_saved_night
                else R.drawable.ic_detail_saved
            } else { // not saved
                if (nightMode) R.drawable.ic_detail_unsaved_night
                else R.drawable.ic_detail_unsaved
            }
        }
    }

    private fun visibility(card: CommonAsset): Int {
        // Below is as per v2/menu/feedback/dictionary API
        return if (card.i_format() in listOf(Format.HTML, Format.VIDEO, Format.EMBEDDED_VIDEO)
                && !(DetailsBindUtils.isComment(card))) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    private fun isSaved(card: CommonAsset,
                        bookmarksDao: BookmarksDao = SocialDB.instance().bookmarkDao()): LiveData<Int> {
        return bookmarksDao.countByAddedForItem(card.i_id())
    }

    override fun onChanged(t: Int?) {
        if (t != null) {
            textView?.get()?.setCompoundDrawablesWithIntrinsicBounds(t, 0, 0, 0)
        }
    }

    override fun onClick(p0: View?) {
        if (p0 != null) {
            val isVideo = cardLd.value?.i_format() == Format.VIDEO // todo check
            if (saved) {
                handleUnSavePostAction(saveUnsaveUsecase, cardLd.value, p0.context, isVideo)
            } else {
                handleSavePostAction(saveUnsaveUsecase, cardLd.value, p0.context, isVideo)
            }
        }
    }

}