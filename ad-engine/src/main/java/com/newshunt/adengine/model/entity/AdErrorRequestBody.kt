/*
 * Copyright (c) 2021 Newshunt. All rights reserved.
 */
package com.newshunt.adengine.model.entity

/**
 * @author raunak.yadav
 */
import java.io.Serializable

data class AdErrorRequestBody(
        var errorCode: Int? = null,  //integer code taken from ads Handshake against appropriate error
        val url: String? = null,
        val errorMessage: String? = null,
        val playerErrorCode: Int? = null,
        val playerErrorType: String? = null,
        val playerErrorMessage: String? = null) : Serializable