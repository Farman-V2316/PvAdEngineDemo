/*
 *
 *  * Copyright (c) 2021 Newshunt. All rights reserved.
 *
 */

package com.newshunt.common.helper.font

/**
 * Same structure as FontEngineOutput. Use it to prevent direct dependency on the library.
 * @author satosh.dhanyamraju
 */
data class FEOutput(val fontIndicesString: StringBuilder,
                    val isSupportedLanguageFound: Boolean) {
    constructor(str: String, supportedLanguageFound: Boolean) : this(java.lang.StringBuilder(str), supportedLanguageFound)
}