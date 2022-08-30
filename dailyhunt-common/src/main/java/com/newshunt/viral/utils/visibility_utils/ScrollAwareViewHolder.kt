package com.newshunt.viral.utils.visibility_utils

/**
 * VH that needs to be aware of scrolling dx,dy in a Scrolling view.
 *
 * @author raunak.yadav
 */
interface ScrollAwareViewHolder : VisibilityAwareViewHolder {

    fun onScrolled(dx:Int, dy:Int)
}