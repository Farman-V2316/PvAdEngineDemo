/*
 * Copyright (c) 2022 Newshunt. All rights reserved.
 */
package com.newshunt.news.model.repo

import com.newshunt.dataentity.common.pages.PageEntity

/**
 * Interface to get pages from HomeTabsAdapter
 *
 * @author satosh.dhanyamraju
 */
interface HomeAdap {
    fun pages(): List<PageEntity>?
}