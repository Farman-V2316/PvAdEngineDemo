/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.dailyhunt.tv.exolibrary.util

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Testcases for [LoadEntryAgg]
 * @author satosh.dhanyamraju
 */
class LoadEntryAggTest {

  @Test
  fun test_analyticsStrDoesNotIncludeValues0() {
    val str = LoadEntryAgg("http://someui/path/v.m3u8", 10L, attempts = 1).analyticsStr()
    assertThat(str).doesNotContain("ends=")
    assertThat(str).doesNotContain("cancells=")
    assertThat(str).doesNotContain("completes=")
    assertThat(str).doesNotContain("errors=")
  }
}