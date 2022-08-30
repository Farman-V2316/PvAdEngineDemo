/*
* Copyright (c) 2019 Newshunt. All rights reserved.
*/
package com.newshunt.appview.common.ui.listeners;

import androidx.recyclerview.widget.RecyclerView

/**
 * Observer to listen when a list adapter has completed diff calculations.
 *
 * @author raunak.yadav
 */
class AdapterChangeObserver(private val onUpdateDone: () -> Unit) : RecyclerView.AdapterDataObserver() {
    override fun onChanged() {
        onUpdateDone()
    }

    override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
        onUpdateDone()
    }

    override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
        onUpdateDone()
    }

    override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
        onUpdateDone()
    }

    override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
        onUpdateDone()
    }
}
