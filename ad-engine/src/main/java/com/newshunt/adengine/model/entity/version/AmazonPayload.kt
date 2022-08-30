/*
 * Copyright (c) 2021 NewsHunt. All rights reserved.
 */

package com.newshunt.adengine.model.entity.version


/**
 *
 * Amazon SDK params payload to be sent to Ads BE
 *
 * Created by helly.p on 13/12/21.
 */

class AmazonSdkPayload(val requestBody:MutableList<HashMap<String, List<AmazonBidPayload>>>? = null)

data class AmazonBidPayload(var slotUUID: String,
                         var amazonHost:List<String>?,
                         var amazonSlot:List<String>?,
                         var amazonBid:List<String>?,
                         var amazonp:List<String>?,
                         var dc:List<String>?)