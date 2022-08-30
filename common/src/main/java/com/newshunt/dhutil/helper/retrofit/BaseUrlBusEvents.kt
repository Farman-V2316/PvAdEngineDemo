/*
 *
 *  * Copyright (c) 2022 Newshunt. All rights reserved.
 *
 */

package com.newshunt.dhutil.helper.retrofit

/**
* These event classes are used in capturing and logging NewsBaseUrl init errors
 *
* @author satosh.dhanyamraju
*/

class NewsBaseUrlInitException(val location: String, val throwable: Throwable, val json: String)

class AnalyticsInitDone