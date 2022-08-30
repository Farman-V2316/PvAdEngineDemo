/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */
package com.newshunt.helper.player

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.preference.GenericAppStatePreference
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.dhutil.helper.autoplay.AutoPlayHelper
import com.newshunt.dhutil.model.entity.players.AutoPlayable
import com.newshunt.dhutil.model.entity.players.StubbornPlayable

/**
 * This class decides which playable view must play, in case multiple such views are present in
 * viewable area.
 *
 * Criteria:
 *  - Video eligibility criterion to be based on mediaframe height - not card height.
 *  - Minimum frame height threshold % to be maintained for each video
 *  - View with higher % of frame visibility wins.
 *  - If 2 or more video frames are equally visible, the bottom-most one wins.
 *      - If one of them is Ad, Ad wins.
 *  - Carousel cards with animation are treated as playable items.
 *  - Sensitivity of calculation of these criteria to be as high as feasible - to allow a smoother
 *    play experience.
 *
 * @author raunak.yadav
 */
class AutoPlayManager(private var recyclerView: RecyclerView?,
                      private var layoutManager: LinearLayoutManager?) :
        LifecycleObserver {
    private var currentPlayingView: AutoPlayable? = null
    private var max: Int = 0
    var pause = false
    private var started = false
    var isMenuShown = false

    init {
        recyclerView?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            var totalDy: Int = 0
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                when (newState) {
                    RecyclerView.SCROLL_STATE_IDLE -> {
                        max = 0
                        findViewToAutoplay(true)
                    }
                    RecyclerView.SCROLL_STATE_DRAGGING -> {
                        max = 30
                    }
                    RecyclerView.SCROLL_STATE_SETTLING -> {
                        max = 200
                    }
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (pause) return

                if (dy > max || -max > dy) {
                    //User is scrolling too fast. Drop check for autoplay.
                    currentPlayingView?.pause()
                    currentPlayingView = null
                    return
                }

                totalDy += Math.abs(dy)
                if (totalDy > max) {
                    findViewToAutoplay(true)
                    totalDy = 0
                }
            }
        })

        recyclerView?.onFlingListener = object : RecyclerView.OnFlingListener() {
            override fun onFling(velocityX: Int, velocityY: Int): Boolean {
                max = 1000
                return false
            }

        }
    }

    fun registerLifecycle(lifecycleOwner: LifecycleOwner) {
        lifecycleOwner.lifecycle.addObserver(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun on_Destroy() {
        Logger.d(TAG, "OnLifecycleEvent : on_Destroy")
        reset()
    }


    fun stop() {
        Logger.d(TAG, "stop : onStop - ${this.hashCode()}")
        currentPlayingView?.pause()
        started = false
    }

    fun restart() {
        started = false
        start()
    }

    /**
     * Starts searching for an Autoplayable view and plays it.
     * Ads to have greater priority than other videos with same visible percent
     */
    fun start() {
        if (!started) {
            Logger.d(TAG, " start started >> $started  - ${this.hashCode()}")
            started = true
            findViewToAutoplay(true)
        }
    }

    fun isStarted(): Boolean {
        return started
    }


    private fun findViewToAutoplay(fresh: Boolean) {

        if (!started || !AutoPlayHelper.isAutoPlayAllowed() || layoutManager == null) {
            Logger.d(TAG, " findViewToAutoplay return > started : $started")
            return
        }

        val firstVisibleItemIndex = layoutManager!!.findFirstVisibleItemPosition()
        val lastVisibleItemIndex = layoutManager!!.findLastVisibleItemPosition()

        Logger.d(TAG, " findViewToAutoplay currentPlayingView : $currentPlayingView")
        Logger.d(TAG, " findViewToAutoplay firstVisibleItemIndex : $firstVisibleItemIndex + " +
                "lastVisibleItemIndex : $lastVisibleItemIndex")

        var localWinner: AutoPlayable? = null
        var localPriority = -1

        for (i in firstVisibleItemIndex..lastVisibleItemIndex) {
            val holder = recyclerView?.findViewHolderForAdapterPosition(i)

            if (holder is StubbornPlayable) {
                if (holder.getAutoplayPriority(fresh) != -1) {
                    Logger.d(TAG, " Stubborn player found : $holder")
                    localWinner = holder
                    break
                }
            } else if (holder is AutoPlayable) {
                val priority = holder.getAutoplayPriority(fresh)
                Logger.d(TAG, " Priority $priority found for $holder")
                if (priority != -1) {
                    if ((firstVisibleItemIndex == 0 && priority > localPriority) ||
                            (firstVisibleItemIndex > 0 && priority >= localPriority)) {
                        localWinner = holder
                        localPriority = priority
                    }
                }
            }
        }

        Logger.i(TAG, "findViewToAutoplay $localWinner == $currentPlayingView")
        //New candidate found, pause any current playing view
        if (localWinner != currentPlayingView) {
            currentPlayingView?.pause()
            Logger.i(TAG, "Setting the currentPlayer view as $localWinner")
            currentPlayingView = localWinner
        }
        Logger.i(TAG, "findViewToAutoplay playing at : ${localWinner?.getPositionInList()}")
        localWinner?.play()
    }


    fun updateFocusedPlayer() {
        findViewToAutoplay(true)
    }

    /**
     * Check if provided asset is the current playing asset.
     *
     * @param asset
     * @return
     */
    fun isCurrentPlayingAsset(asset: Any?): Boolean {
        return currentPlayingView?.asset == asset
    }

    /**
     * When autoplay view is not created due to configs : autoplay = off OR autoplay settings
     * currentPlayingView will be null
     */
    fun isCurrentPlayingViewCreated(): Boolean {
        return currentPlayingView != null
    }

    /**
     * Replace the existing winner with the provided new View.
     * Applicable when a different view is explicitly selected by user.
     *
     * @param newPlayer if null, will simply pause the current player and remove its current
     * playing status.
     */
    fun replaceCurrentPlayingView(newPlayer: AutoPlayable?) {
        if (isCurrentPlayingAsset(newPlayer?.asset)) {
            return
        }
        currentPlayingView?.pause()
        Logger.d(TAG, "Replacing current : $currentPlayingView with new : $newPlayer")
        currentPlayingView = newPlayer
    }

    fun reset() {
        Logger.i(TAG, ">> Reset is called >> releasing player at ${currentPlayingView?.getPositionInList()}")
        currentPlayingView?.pause()
        currentPlayingView = null
        currentPlayingView?.releaseVideo()
        started = false
    }

    fun destroy() {
        recyclerView = null
        layoutManager = null
    }

    companion object {
        private const val TAG = "AutoPlayManager"
    }

    fun canLoadPlayer(): Boolean {
        return (max < 100) // if the scroll is settling then wait for the scroll to stop to load the player
    }

    fun canExchangeAutoPlayer(newAutoplayable: AutoPlayable, oldAutoPlayable: AutoPlayable?): Boolean {
        Logger.d(TAG, "canExchangeAutoPlayer newAutoplayable : ${newAutoplayable.getVisibilityPercentage()} + " +
                "oldAutoPlayable : ${oldAutoPlayable?.getVisibilityPercentage()}")
        // if the current playing view is asked to release the video then return null

        if (oldAutoPlayable == null) {
            return true
        }

        Logger.d(TAG, "canExchangeAutoPlayer newAutoplayable : ${newAutoplayable.getPositionInList()} " +
                "+ oldAutoPlayable : ${oldAutoPlayable?.getPositionInList()} + " +
                "currentPlayingView : ${currentPlayingView?.getPositionInList()}")

        if (currentPlayingView == oldAutoPlayable) {
            Logger.d(TAG, "currentPlayingView & oldAutoPlayable are same >> return false")
            return false
        }

        val autoPlayVisibility = PreferenceManager.getPreference(
                GenericAppStatePreference.MIN_VISIBILITY_FOR_ANIMATION, 90)
        // if the old autoplayable is less than 90%
        if (oldAutoPlayable.getVisibilityPercentage() >= autoPlayVisibility) {
            return false
        }

        // if the new video holder is at lower position in the list or the new video view holder is more than
        // autoplay visibility criteria then the video can be released
        return newAutoplayable.getVisibilityPercentage() >= autoPlayVisibility
    }

    fun isAnyActivePlayer(): Boolean {
        return currentPlayingView?.isPLaying() ?: false
    }

    fun getCurrentPlayerHolderIndex(): Int {
        return currentPlayingView?.getPositionInList() ?: -1
    }

    fun setMenuState(isShowing: Boolean, isHideCard: Boolean) {
        Logger.d(TAG, "setMenuState isShowing : $isShowing & isHideCard : $isHideCard")
        val prevMenuState = isMenuShown
        isMenuShown = isShowing
        if (!started) {
            Logger.d(TAG, "setMenuState > !started")
            return
        }

        if (currentPlayingView == null) {
            Logger.d(TAG, "setMenuState currentPlayingView is NULL")
            if (prevMenuState && !isShowing && AutoPlayHelper.isAutoPlayAllowed()) {
                findViewToAutoplay(true)
            }
            return
        }

        if (isShowing) {
            Logger.d(TAG, "setMenuState currentPlayingView - pause")
            currentPlayingView?.pause()
        } else if (prevMenuState) {
            if (isHideCard) {
                //isHideCard indicates, Card will be removed hence releasing the player
                //Its true for the menu "Block" & "Show less" options
                Logger.d(TAG, "setMenuState currentPlayingView - release")
                currentPlayingView?.releaseVideo()
                currentPlayingView = null
            } else {
                Logger.d(TAG, "setMenuState currentPlayingView - play")
                currentPlayingView?.play()
            }
        }
    }
}
