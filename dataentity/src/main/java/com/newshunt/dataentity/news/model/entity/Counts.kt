/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */

package com.newshunt.dataentity.news.model.entity

import com.newshunt.common.helper.common.Constants
import java.io.Serializable

/**
 * @author anshul.jain
 */
data class Counts(var STORY: EntityConfig? = null, var SOURCES: EntityConfig? = null, var FOLLOW:
EntityConfig? = null, var LIKE: EntityConfig? = null, var COMMENTS: EntityConfig? = null, var
                  VIEWS: EntityConfig? = null, var SHARE: EntityConfig? = null,
                  var SAD: EntityConfig? = null, var HAPPY: EntityConfig? = null,
                  var LOVE: EntityConfig? = null, var ANGRY: EntityConfig? = null,
                  var WOW: EntityConfig? = null, var TOTAL_LIKE: EntityConfig? = null, var
                  WATCH: EntityConfig? = null, var DOWNLOAD: EntityConfig? = null) :
        Serializable {
    constructor() : this(
            EntityConfig(Constants.ZERO_STRING, 0),
            EntityConfig(Constants.ZERO_STRING, 0),
            EntityConfig(Constants.ZERO_STRING, 0),
            EntityConfig(Constants.ZERO_STRING, 0),
            EntityConfig(Constants.ZERO_STRING, 0),
            EntityConfig(Constants.ZERO_STRING, 0),
            EntityConfig(Constants.ZERO_STRING, 0),
            EntityConfig(Constants.ZERO_STRING, 0),
            EntityConfig(Constants.ZERO_STRING, 0),
            EntityConfig(Constants.ZERO_STRING, 0),
            EntityConfig(Constants.ZERO_STRING, 0),
            EntityConfig(Constants.ZERO_STRING, 0),
            EntityConfig(Constants.ZERO_STRING, 0),
            EntityConfig(Constants.ZERO_STRING, 0),
            EntityConfig(Constants.ZERO_STRING, 0))
}

data class EntityConfig(var value: String, val ts: Long = 0) : Serializable