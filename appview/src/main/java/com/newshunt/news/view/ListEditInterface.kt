/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.news.view

/**
 * @author santhosh.kc
 */
interface ListEditInterface {

    fun goToEditMode() {}

    fun onEditModeDone() {}

    fun onEditModeCancel() {}

    fun clearAllInList() {}

    fun isInEditMode(): Boolean {
        return false
    }
}