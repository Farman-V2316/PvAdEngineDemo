/**
 * Copyright (c) 2018 Newshunt. All rights reserved.
 * */
package com.newshunt.dataentity.dhutil.model.entity.players

data class PlayerAutoplaySupport(val isAutoplaySupported: Boolean = false,
                                 val minVersion : String?,
                                 val excludeVersion : List<String>?)

