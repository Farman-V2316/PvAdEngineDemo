package com.newshunt.dhutil.helper

import android.view.View
import android.widget.ImageView
import com.newshunt.dhutil.R

data class FullPageErrorMessage(val message: String? = null, val messageIcon: MessageIcon? = defaultIcon())

data class MessageIcon(val resId: Int = View.NO_ID, val isAttribute: Boolean = false,
                       val scaleType: ImageView.ScaleType? = null)

fun defaultIcon() = MessageIcon(R.attr.content_error, true)