/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.news.helper

import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.newshunt.common.helper.common.Logger
import com.newshunt.dataentity.searchhint.entity.SearchHint
import com.newshunt.dataentity.searchhint.entity.SearchLocation
import com.newshunt.dhutil.zipWith
import com.newshunt.helper.hintFor
import com.newshunt.searchhint.HintsService

private const val STR_DEFAULT = "DEFAULT"

class SearchHintUtils(private val searchView: View,
                      private val lifecycleOwner: LifecycleOwner) {

    private var pageIdAndTypeLiveData: MutableLiveData<Pair<String, String>>? = null

    fun updateHint(location: SearchLocation,
                   id: String = STR_DEFAULT,
                   type: String = STR_DEFAULT) {
        if (pageIdAndTypeLiveData == null) {
            pageIdAndTypeLiveData = MutableLiveData()
            pageIdAndTypeLiveData?.zipWith<Pair<String, String>, List<SearchHint>, SearchHint>(
                    HintsService.performHintSync(location)) {

                pageIdAndType: Pair<String, String>,
                hintData: List<SearchHint> ->
                hintFor(pageIdAndType, hintData)
            }?.observe(lifecycleOwner, Observer { hint ->
                Logger.d(LOG_TAG, "settingHint: matched: $hint")
                if (searchView is EditText) {
                    searchView.hint = hint.displayText
                } else if (searchView is TextView) {
                    searchView.text = hint.displayText
                }
            })
        }
        pageIdAndTypeLiveData?.value = id to type
    }
}

private const val LOG_TAG = "SearchHintUtils"