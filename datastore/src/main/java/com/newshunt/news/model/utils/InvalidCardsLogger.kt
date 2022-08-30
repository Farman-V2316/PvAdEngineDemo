/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.news.model.utils

/**
 * For dev events. To be used by classes that filter-out invalid cards, across layers (DB, View)
 * @author satosh.dhanyamraju
 */
interface InvalidCardsLogger {
    fun log(message : String, pojo: Any)
}
