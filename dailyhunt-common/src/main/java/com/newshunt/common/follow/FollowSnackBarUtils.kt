/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.common.follow

import com.newshunt.common.helper.common.Constants
import com.newshunt.common.view.customview.*
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dhutil.R

/**
 * @author santhosh.kc
 */

@JvmOverloads
fun constructFollowSnackBarMetaData(title: String?, snackBarActionClickListener:
SnackBarActionClickListener? = getDefaultSnackBarClickListener(),
                                    actionMessage: String? = "Start check now",
                                    snackBarLayoutParams : SnackBarLayoutParams? = getDefaultSnackBarParams())
        : FollowSnackBarMetaData {
    return FollowSnackBarMetaData(title ?: Constants.EMPTY_STRING, actionMessage = actionMessage,
            snackBarActionClickListener = snackBarActionClickListener,
            snackBarLayoutParams = snackBarLayoutParams)
}

