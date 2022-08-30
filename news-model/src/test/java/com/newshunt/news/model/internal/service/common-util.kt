/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */

package com.newshunt.news.model.internal.service

import org.junit.Assert

/**
 * @author satosh.dhanymaraju
 */

infix fun <T> T.shouldBe(t: T) {
    Assert.assertEquals(t, this)
}
