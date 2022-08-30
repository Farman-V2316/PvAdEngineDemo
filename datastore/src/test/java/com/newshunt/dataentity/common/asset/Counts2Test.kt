/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.dataentity.common.asset

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class Counts2Test {

    @Test
    fun testIncCount() {
        val counts2 = Counts2()
        assertThat(counts2.SHARE?.value).isEqualTo("0")
        assertThat(counts2.SHARE?.ts).isEqualTo(0)

        val c3 = counts2.incCount(counts2.SHARE)
        assertThat(c3.value).isEqualTo("1")
        assertThat(c3.ts).isEqualTo(1)


        val ec = EntityConfig2("1k", null)
        val ec3 = counts2.incCount(ec)
        assertThat(ec3.value).isEqualTo("1k")
        assertThat(ec3.ts).isEqualTo(1)
    }

    @Test
    fun testDecCount() {
        val counts2 = Counts2()
        assertThat(counts2.SHARE?.value).isEqualTo("0")
        assertThat(counts2.SHARE?.ts).isEqualTo(0)

        val c3 = counts2.decCount(EntityConfig2("2", 0))
        assertThat(c3.value).isEqualTo("1")
        assertThat(c3.ts).isEqualTo(1)


        val ec = EntityConfig2("1k", null)
        val ec3 = counts2.decCount(ec)
        assertThat(ec3.value).isEqualTo("1k")
        assertThat(ec3.ts).isEqualTo(1)

        val ec4 = counts2.decCount(counts2.SHARE)
        assertThat(ec4.value).isEqualTo("0")
        assertThat(ec4.ts).isEqualTo(1)

    }
}