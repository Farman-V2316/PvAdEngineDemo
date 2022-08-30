/*
 * Copyright (c) 2021 Newshunt. All rights reserved.
 */
package com.newshunt.adengine.processor

import com.newshunt.adengine.model.entity.version.AdRequest

/**
 * Class implementing this interface does post fetch processing of ad.
 * It can be downloading content, validating json or just nothing.
 *
 * @author raunak.yadav
 */
interface BaseAdProcessor {
    fun processAdContent(adRequest: AdRequest? = null)
}