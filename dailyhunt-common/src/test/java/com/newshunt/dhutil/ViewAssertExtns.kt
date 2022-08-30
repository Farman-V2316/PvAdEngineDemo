/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.dhutil

import org.junit.Assert

/**
 * File contains extension functions for commonly used view assertions
 *
 * @author satosh.dhanyamraju
 */

infix fun <T> T.shouldBe(t: T) = Assert.assertEquals(t, this)