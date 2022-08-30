/*
* Copyright (c) 2019 Newshunt. All rights reserved.
*/
package com.newshunt.dataentity.dhutil.model.entity.status

import com.newshunt.dataentity.common.model.entity.status.ClientInfo
import com.newshunt.dataentity.common.model.entity.status.ConnectionInfo
import com.newshunt.dataentity.common.model.entity.status.LocationInfo
import java.io.Serializable

/**
 * Container to hold information required for ads targeting.
 *
 * @author raunak.yadav
 */
class CurrentAdProfile(var version: String? = null,
                       var clientInfo: ClientInfo? = null,
                       var locationInfo: LocationInfo? = null,
                       var connectionInfo: ConnectionInfo? = null,
                       var androidId: String? = null,
                       var packageName: String? = null,
                       var mimeTypes: String? = null,
                       var isSupplementAdsSupported: Boolean = true) : Serializable