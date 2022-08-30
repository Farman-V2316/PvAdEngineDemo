/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.adengine.model.entity

import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout

/**
 * @author raunak.yadav
 */
interface NativeViewHelper {

    /**
     * Get all the native assets related to this ad.
     */
    fun getNativeAssets(): NativeData?

    fun getAutoplayVisibility(): Int = 100

    /**
     * Since ad views are reused, clicklisteners might need to be reset in some cases.
     */
    fun shouldClearClickListeners(): Boolean {
        return true
    }

    /**
     * Display AdChoicesView for FB ads(direct/mediated).
     * The AdChoicesView is returned so as to be able to remove it when the viewholder is reused.
     *
     * @param adContainer the adViewContainer
     * @return AdChoicesView
     */
    fun addAdChoicesView(adContainer: ViewGroup): View?

    /**
     * If the ad has fb mediaView via mediation or direct, handle its display.
     *
     * @param mediaViewLayout Layout to which mediaView must be added.
     * @return whether mediaView was added and wide image display should not be done as a result.
     */
    fun getMediaViewIfApplicable(mediaViewLayout: RelativeLayout): View?

    /**
     * Return null, if no preferred height
     */
    fun getPreferredHeightMediaView(assets: NativeData): Int? {
        return null
    }

    /**
     * Register the view for click Interaction.
     */
    fun recordImpression() {}

    /**
     * Register the view for click Interaction.
     */
    fun registerViewForInteraction(view: View, clickableViews: List<View>)

    /**
     * Destroy ad resources.
     *
     * @param parentId Id of view where ad has been added.
     */
    fun destroy(parentId: Int, view: View? = null)
}
