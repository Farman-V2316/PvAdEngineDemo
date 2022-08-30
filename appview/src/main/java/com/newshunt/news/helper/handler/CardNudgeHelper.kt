/*
 * Copyright (c) 2020 Newshunt. All rights reserved.
 */

package com.newshunt.news.helper.handler

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.PopupWindow
import com.newshunt.appview.R
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.view.customview.fontview.NHTextView
import com.newshunt.dataentity.common.asset.CardNudge
import com.newshunt.dataentity.common.asset.CardNudgeTerminateType
import com.newshunt.dataentity.common.asset.CommonAsset
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dhutil.analytics.AnalyticsHelper2
import com.newshunt.dhutil.helper.preference.UserPreferenceUtil
import com.newshunt.news.util.NewsConstants
import java.util.concurrent.TimeUnit

/**
 * For showing nudges on cards
 * @author satosh.dhanyamraju
 */
class CardNudgeHelper {
    private var isNudgeShowing = false

    fun showNudge(nudges: Map<String, CardNudge?>, view: View?, card: CommonAsset?, scrollViewRect: Rect, f: (CardNudge, Boolean) -> Any?): CardNudge? {
        if (isNudgeShowing) return null
        val id = card?.i_id() ?: return null
        view ?: return null
        if (nudges.containsKey(id)) {
            val nudgeToShow = nudges[id] ?: return null
            val d = mappings[nudgeToShow.terminationType]
            if (d != null) {
                val shown = showIt(view.context, view, nudgeToShow, d, scrollViewRect, f)
                Logger.d(LOG_TAG, "showNudge: redirect: ${nudgeToShow.id} matched. shown=$shown")
                return shown
            }
        }
        return null
    }

    private fun showIt(context: Context, parentView: View, nudge: CardNudge, d: D, scrollViewRect: Rect, f: (CardNudge, Boolean) -> Any?): CardNudge? {
        val anchor = parentView.findViewById<View>(d.anchorId) ?: return null
        val clickableView = parentView.matchId(d.clickableViewId)
        if (!anchor.isVisibleInRect(scrollViewRect)) {
            return null
        }
        val cView: View = LayoutInflater.from(context).inflate(d.layoutToInflate, null)
        cView.findViewById<NHTextView>(R.id.title).apply { text = nudge.text }
        val popupWindow: PopupWindow = PopupWindow(context).apply {
            height = WindowManager.LayoutParams.WRAP_CONTENT
            width = WindowManager.LayoutParams.WRAP_CONTENT
            isOutsideTouchable = true
            isFocusable = true
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setOnDismissListener { isNudgeShowing = false ; f(nudge, false)}
            contentView = cView.also { v ->
                v.setOnClickListener { clickableView?.performClick();dismiss() }
            }
        }
        anchor.post {
            (context as? Activity)?.let {
                if (!it.isFinishing) {
                    val screen_pos = Rect()
                    anchor.getGlobalVisibleRect(screen_pos)
                    cView.measure(WindowManager.LayoutParams.WRAP_CONTENT,
                            WindowManager.LayoutParams.WRAP_CONTENT)
                    val contentViewWidth = cView.getMeasuredWidth()
                    val (x, y) = d.locationF(screen_pos, contentViewWidth, d)
                    popupWindow.showAtLocation(anchor, Gravity.NO_GRAVITY, x, y)
                    isNudgeShowing = true
                    f(nudge, true)
                }
            }
        }
        anchor.postDelayed({ popupWindow.dismiss() },
                TimeUnit.SECONDS.toMillis(nudge.tooltipDurationSec.toLong()))
        AnalyticsHelper2.logFeatureNudgeEvent(d.eventName)
        return nudge
    }

    companion object {
        private val ID_REPOST_TV = R.id.repost_icon_tv
        private val ID_COMMENT_TV = R.id.comment_count_tv
        private val ID_COMMENT_REPOST_LAYOUT = R.id.comments_reposts_card
        private val ID_SHARE_TV = R.id.share_count_tv
        private val LOG_TAG: String = "CardNudgeHelper"
        private val isUrduSelected : Boolean = CommonUtils.equals(UserPreferenceUtil.getUserNavigationLanguage(), NewsConstants.URDU_LANGUAGE_CODE)

        private fun getLayout(isSelected : Boolean) : Int {
            var layoutId : Int = 0

            if(isSelected){
                layoutId = R.layout.nudge_tooltip_repost_carosel_urdu
            }
            else{
                layoutId = R.layout.nudge_tooltip_repost_carousel
            }

            return layoutId
        }


        private val mappings = mapOf(
                CardNudgeTerminateType.share.name to D(ID_SHARE_TV, ID_SHARE_TV,
                        R.layout.nudge_tooltip_share, "share_nudge", ::posShare),
                CardNudgeTerminateType.comment.name to D(ID_COMMENT_REPOST_LAYOUT, ID_COMMENT_TV,
                        R.layout.nudge_tooltip_comment, "comment_nudge", ::posComment),
                CardNudgeTerminateType.repost.name to D(ID_REPOST_TV, ID_REPOST_TV,
                        getLayout(isUrduSelected), "repost_nudge", ::posRepost))

        private fun posRepost(anchor_rect: Rect, contentViewWidth: Int, d: D): Pair<Int, Int> {
            var position_x = anchor_rect.right - anchor_rect.left
            val position_y = anchor_rect.bottom + CommonUtils.getDimension(R.dimen.discussion_scroll_margin)

            if (d.layoutToInflate == R.layout.nudge_tooltip_repost_carousel) {
                position_x = anchor_rect.left
            }

            else if(d.layoutToInflate == R.layout.nudge_tooltip_repost_carosel_urdu) {
                position_x = anchor_rect.left - R.dimen.discussion_hori_margin
            }

            return position_x to position_y
        }

        private fun posComment(anchor_rect: Rect, contentViewWidth: Int, d: D): Pair<Int, Int> {
            val position_x = anchor_rect.left +
                    CommonUtils.getDimension(R.dimen.discussion_hori_margin)
            val position_y = anchor_rect.bottom +
                    CommonUtils.getDimension(R.dimen.discussion_scroll_margin)
            return position_x to position_y
        }

        private fun posShare(anchor_rect: Rect, contentViewWidth: Int, d: D): Pair<Int, Int> {
            val position_x = anchor_rect.right - (contentViewWidth) / 2 - anchor_rect.width() / 2
            val position_y = anchor_rect.bottom + CommonUtils.getDimension(R.dimen.discussion_hori_margin)
            return position_x to position_y
        }

        private fun View?.isVisibleInRect(scrollViewRect: Rect): Boolean {
            if (this == null) {
                return false
            }
            if (!isShown) {
                return false
            }
            val actualPosition = Rect()
            getGlobalVisibleRect(actualPosition)

            val intersects = actualPosition.intersect(scrollViewRect)
            val bottom = actualPosition.bottom
            val _75percentOfScreen = scrollViewRect.top + scrollViewRect.height() * 3 / 4.0
            val b = intersects && (_75percentOfScreen > bottom)
            Logger.d(LOG_TAG, "isVisibleOnScreen: $intersects, ${_75percentOfScreen}, $bottom => $b")
            return b
        }

        private fun View.matchId(searchForId: Int): View? {
            if (getId() == searchForId) return this
            return this.findViewById(searchForId)
        }

        data class D(
                val anchorId: Int,
                val clickableViewId: Int,
                val layoutToInflate: Int,
                val eventName: String,
                val locationF: (Rect, Int, D) -> Pair<Int, Int> // anchor rect, contentViewWidth
        )
    }
}