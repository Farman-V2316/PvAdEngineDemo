/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */

package com.newshunt.dataentity.common.model.entity

/**
 * To be fired by section home activities when they are finishing due to double-back-exit
 *
 * @author satosh.dhanyamraju
 */
class DoubleBackExitEvent(val fromWhere: String /* for debugging purpose*/)
