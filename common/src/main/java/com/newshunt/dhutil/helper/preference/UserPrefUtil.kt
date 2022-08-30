/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.dhutil.helper.preference

/**
 * kotlin part of [UserPreferenceUtil]
 *
 * @author satosh.dhanyamraju
 */
private fun String?.toList(): List<String>? {
    return when {
        this == null -> null
        else -> split(",")
                .map { it.trim() }
                .filter { it.isNotEmpty() }
    }
}

fun langsFirst(langs: String?): String? {
    return langs.toList()
            ?.take(1)
            ?.joinToString(",")
}

fun langsRest(langs: String?): String? {
    return langs.toList()
            ?.drop(1)
            ?.joinToString(",")
}
