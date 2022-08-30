/*
* Copyright (c) 2018 Newshunt. All rights reserved.
*/
package com.newshunt.adengine.model.entity.omsdk

import java.io.Serializable

/**
 * @author raunak.yadav
 */
class OMVendorInfo : Serializable {

    val vendorKey: String? = null
    val javascriptResourceUrl: String? = null
    val verificationParameters: String? = null

    companion object {
        private const val serialVersionUID = 6379735536041786471L
    }
}