/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */
package com.newshunt.dataentity.common.model.entity.model

import com.newshunt.common.helper.common.Constants

/**
 * Holds various timeout values.
 *
 * Created by karthik on 05/04/18.
 */
class Timeout {

    var connect : Long = Constants.DEFAULT_HTTP_CONNECT_TIMEOUT;
    var read : Long = Constants.DEFAULT_HTTP_CLIENT_TIMEOUT;
    var write : Long = Constants.DEFAULT_HTTP_CLIENT_TIMEOUT;
}