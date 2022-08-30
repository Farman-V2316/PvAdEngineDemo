/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.common.helper.share

import java.io.Serializable

/**
 * Part of Pojo returned from JSCallback.
 * Contains information for hitting shareAPI
 *
 * @author satosh.dhanyamraju
 */
data class ShareAPIParams(var itemId: String?,
                     var entityType: String?): Serializable