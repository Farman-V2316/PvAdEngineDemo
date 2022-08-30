/*
 * Created by Rahul Ravindran at 19/9/19 5:44 PM
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.appview.common.postcreation.view.customview

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.google.android.flexbox.FlexboxLayout
import com.newshunt.common.helper.common.Logger
import com.newshunt.dhutil.children


/*
* View order based sorting of views. Calls invalidate() every time on view addition
* */
class ViewOrderBasedLinearLayout : FlexboxLayout {
    private val TAG = ViewOrderBasedLinearLayout::class.java.simpleName
    private var viewStack = mutableSetOf<View>()
    private val TAG_VIEW_TYPE = 1001

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    var isPollViewEnabled = false
        set(value) {
            if (value) {
                // restrict views to be added here for poll condition
            }
        }



    fun getViewOfType(type: VIEW_TYPE): View? = this.children.find { (it.tag as? VIEW_TYPE) == type}


    fun addViewOfType(type: VIEW_TYPE, v: View){
        v.tag
        var view: View? = null
        try {
            when {
                type == VIEW_TYPE.REPOST_VIEW && !isPollViewEnabled -> {
                    view = v
                }
                type == VIEW_TYPE.IMAGE_GRID && !isPollViewEnabled -> {
                    view = v
                }
                type == VIEW_TYPE.OG_VIEW -> {
                    view = v
                }
                type == VIEW_TYPE.POLL_VIEW -> {
                    view = v
                }
                type.equals(VIEW_TYPE.LOCATION_VIEW) -> {
                    view = v
                }
                else -> {
                }
            }
            if (!checkContainsView(type)){
                view?.let {
                    val params = FlexboxLayout.LayoutParams(FlexboxLayout.LayoutParams.MATCH_PARENT,
                            FlexboxLayout.LayoutParams.WRAP_CONTENT)
                    params.order = type.order
                    it.tag = type
                    addView(view, params)
                    viewStack.add(view)
                    invalidate()
                }
            }
        } catch (e: Exception){
            Logger.d(TAG, "exception occured while addding view")
            Logger.caughtException(e)
        }
    }

    fun checkContainsView(type: VIEW_TYPE): Boolean = viewStack.any { it.tag as? VIEW_TYPE == type} ?: false

    override fun onDetachedFromWindow() {
        viewStack.clear()
        super.onDetachedFromWindow()
    }

    override fun removeView(view: View?) {
        viewStack.remove(view)
        super.removeView(view)
    }

}



enum class VIEW_TYPE(val type: String, val id: Int, val order: Int) {
    IMAGE_GRID("image_grid", 0, 1),
    OG_VIEW("og_view", 1, 2),
    LOCATION_VIEW("location_view", 2, 5),
    POLL_VIEW("poll_view", 3, 3),
    REPOST_VIEW("repost_view", 4, 4),
    NONE("none", 5, -1);

    companion object {
        fun get(type: VIEW_TYPE) = values().find { it == type }
    }
}