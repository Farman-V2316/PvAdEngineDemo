/*
 * Created by Rahul Ravindran at 13/9/19 5:31 PM
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.appview.common.postcreation.view.listener

import java.util.regex.Pattern

interface PatternMatcherCallback {
    fun onMatch(pattern: Pattern, vararg matches: String)
}