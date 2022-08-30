/*
 * Copyright (c) 2020 Newshunt. All rights reserved.
 */

package com.newshunt.appview.common.ui.viewholder

import android.view.View
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.newshunt.appview.R
import com.newshunt.common.view.customview.fontview.NHTextView
import com.newshunt.dataentity.common.model.entity.BaseError
import com.newshunt.dhutil.view.ErrorMessageBuilder

/**
 * @author priya.gupta
 */

class AddPageErrorViewHolder(private val view: View, private val listener: ErrorMessageBuilder.ErrorMessageClickedListener?) :
        RecyclerView.ViewHolder(view) {

    val errorParent: LinearLayout = view.findViewById(R.id.error_parent)
    val errorMessageBuilder: ErrorMessageBuilder = ErrorMessageBuilder(errorParent!!, view.context!!,
            listener)


    fun updateError(error: BaseError?) {
        error?:return
        if (!errorMessageBuilder!!.isErrorShown) {
            errorMessageBuilder!!.showError(error)
        }
    }
}