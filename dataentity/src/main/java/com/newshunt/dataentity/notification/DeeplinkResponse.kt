/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.dataentity.notification


/**
 * An response for [com.newshunt.app.helper.Deeplinker]
 *
 * @author anshul.jain
 */
data class DeeplinkResponse(val uniqueRequestId: Int, val navigationModel: BaseModel? = null, val
deeplinkUrl: String? = null, val deeplinkModel: DeeplinkModel? = null)
