/*
 * Copyright (c) 2021 Newshunt. All rights reserved.
 */
package com.newshunt.dataentity.common.model

/**
 * Data classes for adjunct related static config objects.
 *
 * @author aman.roy
 */
data class AdjunctLangResponse(val version: String = "0",
                               val notificationTextMap:Map<String,String>?=null,
                               val bannerTextMap:Map<String,String>?=null,
                               val snackbarTextMap:Map<String,String>?=null,
                               val langNotiImages:Map<String,String>?=null,
                               val langPopupImages:Map<String,String>?=null)

data class AdjunctSnackbarInfo(val adjunctLang: String,
                               val primaryLang: String,
                               val langFlow: String?= null)