/*
 * Created by Rahul Ravindran at 13/9/19 12:28 PM
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.dataentity.common.asset

import com.linkedin.android.spyglass.tokenization.QueryToken

sealed class Phrase(val query: QueryToken? = null)

data class Delete(
    var queryToken: QueryToken? = null,
    var editable: String? = queryToken?.keywords,
    val start: Int,
    val end: Int
) : Phrase(queryToken)

data class PartialDelete(
    var queryToken: QueryToken? = null,
    var editable: String? = queryToken?.keywords,
    val start: Int,
    val end: Int
) : Phrase(queryToken)

data class Added(
    val queryToken: QueryToken? = null,
    var editable: String? = queryToken?.keywords,
    val start: Int, val end: Int
) : Phrase(queryToken)
