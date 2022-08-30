/*
 * Copyright (c) 2022 Newshunt. All rights reserved.
 */

package com.newshunt.news.util

import android.content.Context
import androidx.annotation.NonNull
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.newshunt.common.helper.common.Logger

/**
 * Created by mukesh.yadav on 20/01/2022.
 */
class LinearLayoutManagerWrapper(context: Context?, private val deviceHeight: Int) : LinearLayoutManager(context) {

    override fun onLayoutChildren(recycler: RecyclerView.Recycler, state: RecyclerView.State) {
        try {
            super.onLayoutChildren(recycler, state)
        } catch (e: IndexOutOfBoundsException) {
            Logger.e("WrapContentLinearLayoutManager", "IndexOutOfBoundsException in RecyclerView")
        }

    }

    override fun calculateExtraLayoutSpace(@NonNull state: RecyclerView.State, @NonNull ints: IntArray) {
        Logger.e("WrapContentLinearLayoutManager", "calculateExtraLayoutSpace =" +deviceHeight/2)
           ints[0] = deviceHeight/2
            ints[1] = deviceHeight/2
    }
}