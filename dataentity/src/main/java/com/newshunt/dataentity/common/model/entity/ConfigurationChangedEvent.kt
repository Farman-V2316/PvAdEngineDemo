/*
 * Copyright (c) 2021 Newshunt. All rights reserved.
 */
package com.newshunt.dataentity.common.model.entity


/**
 * Event to notify a uiMode config change
 *
 * @author srikanth.r on 11/22/2021
 */
data class ConfigurationChangedEvent(val timestamp: Long = System.currentTimeMillis())