/*
 * Created by Rahul Ravindran at 4/9/19 3:53 PM
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.appview.common.postcreation.view.helper

import android.text.Editable
import android.text.TextWatcher
import androidx.lifecycle.Observer
import com.newshunt.appview.common.postcreation.view.listener.PatternMatcherCallback
import java.util.regex.Pattern


/*
* class that takes in pattern that need to recognised from the edittext
* */
open class TextWatcherPattern : TextWatcher {

    private val matchers: MutableMap<Pattern, Pair<Pattern, Array<String>>.() -> Unit> = mutableMapOf()

    fun addMatcher(pattern: Pattern, callback: Pair<Pattern, Array<String>>.() -> Unit) {
        if (!matchers.containsKey(pattern)) matchers[pattern] = callback
    }

    override fun afterTextChanged(p0: Editable?) {}

    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        if (p0?.length!! > 0 && p1 < p0.length && p0[p1] == ' ') {
            matchers.forEach {
                val matcher = it.key.matcher(p0.toString())
                val matches = mutableListOf<String>()
                while (matcher.find()) {
                    matches.add(p0.subSequence(matcher.start(0), matcher.end(0)).toString())
                }
                if (matches.isNotEmpty()) it.value(Pair(it.key, matches.toTypedArray()))
            }
        }
    }
}